package com.employeeleavepayroll.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.employeeleavepayroll.util.DBConnection;

import java.sql.*;

@WebServlet("/LeaveApprovalServlet")
public class LeaveApprovalServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(
                    "{\"error\":\"User not logged in\"}"
            );
            return;
        }

        String role = (String) session.getAttribute("role");

        if (!"MANAGER".equals(role) && !"HR".equals(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(
                    "{\"error\":\"Access denied\"}"
            );
            return;
        }

        String sql =
                "SELECT lr.id, lr.user_id, u.full_name, "
              + "e.employee_id, lr.leave_type, lr.start_date, "
              + "lr.end_date, lr.days, lr.reason, lr.status "
              + "FROM leave_requests lr "
              + "JOIN users u ON lr.user_id = u.id "
              + "JOIN employees e ON lr.user_id = e.user_id "
              + "ORDER BY lr.applied_at DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            PrintWriter out = response.getWriter();

            out.println("[");

            boolean first = true;

            while (resultSet.next()) {

                if (!first) {
                    out.println(",");
                }

                out.println("{");
                out.println("\"id\":" + resultSet.getInt("id") + ",");
                out.println("\"userId\":" + resultSet.getInt("user_id") + ",");
                out.println("\"employeeName\":\""
                        + resultSet.getString("full_name") + "\",");
                out.println("\"employeeId\":\""
                        + resultSet.getString("employee_id") + "\",");
                out.println("\"type\":\""
                        + resultSet.getString("leave_type") + "\",");
                out.println("\"startDate\":\""
                        + resultSet.getDate("start_date") + "\",");
                out.println("\"endDate\":\""
                        + resultSet.getDate("end_date") + "\",");
                out.println("\"days\":"
                        + resultSet.getInt("days") + ",");
                out.println("\"reason\":\""
                        + resultSet.getString("reason") + "\",");
                out.println("\"status\":\""
                        + resultSet.getString("status") + "\"");
                out.println("}");

                first = false;
            }

            out.println("]");

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().write(
                    "{\"error\":\"Database error\"}"
            );
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(
                    "{\"error\":\"User not logged in\"}"
            );
            return;
        }

        String role = (String) session.getAttribute("role");

        if (!"MANAGER".equals(role) && !"HR".equals(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(
                    "{\"error\":\"Access denied\"}"
            );
            return;
        }

        String leaveId = request.getParameter("leaveId");
        String status = request.getParameter("status");

        if (!"Approved".equals(status) && !"Rejected".equals(status)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                    "{\"error\":\"Invalid status\"}"
            );
            return;
        }

        Connection connection = null;

        try {

            connection = DBConnection.getConnection();
            connection.setAutoCommit(false);

            /*
             * Get current leave request.
             */
            String selectSql =
                    "SELECT user_id, leave_type, days, status "
                  + "FROM leave_requests "
                  + "WHERE id = ? "
                  + "FOR UPDATE";

            int userId;
            String leaveType;
            int days;
            String currentStatus;

            try (PreparedStatement statement =
                         connection.prepareStatement(selectSql)) {

                statement.setInt(1, Integer.parseInt(leaveId));

                try (ResultSet result = statement.executeQuery()) {

                    if (!result.next()) {

                        connection.rollback();

                        response.setStatus(
                                HttpServletResponse.SC_NOT_FOUND
                        );

                        response.getWriter().write(
                                "{\"error\":\"Leave request not found\"}"
                        );

                        return;
                    }

                    userId = result.getInt("user_id");
                    leaveType = result.getString("leave_type");
                    days = result.getInt("days");
                    currentStatus = result.getString("status");
                }
            }

            /*
             * Prevent duplicate approval/rejection.
             */
            if (!"Pending".equalsIgnoreCase(currentStatus)) {

                connection.rollback();

                response.setStatus(
                        HttpServletResponse.SC_BAD_REQUEST
                );

                response.getWriter().write(
                        "{\"error\":\"Leave request is already processed\"}"
                );

                return;
            }

            /*
             * Update leave balance only when approved.
             * Unpaid Leave does not use paid leave balance.
             */
            if ("Approved".equals(status)
                    && !"Unpaid Leave".equalsIgnoreCase(leaveType)) {

                String balanceSql =
                        "UPDATE leave_balances "
                      + "SET used = used + ?, "
                      + "available = available - ? "
                      + "WHERE user_id = ? "
                      + "AND leave_type = ? "
                      + "AND available >= ?";

                try (PreparedStatement statement =
                             connection.prepareStatement(balanceSql)) {

                    statement.setInt(1, days);
                    statement.setInt(2, days);
                    statement.setInt(3, userId);
                    statement.setString(4, leaveType);
                    statement.setInt(5, days);

                    int balanceRows = statement.executeUpdate();

                    if (balanceRows == 0) {

                        connection.rollback();

                        response.setStatus(
                                HttpServletResponse.SC_BAD_REQUEST
                        );

                        response.getWriter().write(
                                "{\"error\":\"Insufficient leave balance\"}"
                        );

                        return;
                    }
                }
            }

            /*
             * Update leave request status.
             */
            String updateSql =
                    "UPDATE leave_requests "
                  + "SET status = ? "
                  + "WHERE id = ? "
                  + "AND status = 'Pending'";

            try (PreparedStatement statement =
                         connection.prepareStatement(updateSql)) {

                statement.setString(1, status);
                statement.setInt(2, Integer.parseInt(leaveId));

                int rows = statement.executeUpdate();

                if (rows == 0) {

                    connection.rollback();

                    response.setStatus(
                            HttpServletResponse.SC_BAD_REQUEST
                    );

                    response.getWriter().write(
                            "{\"error\":\"Leave request was already processed\"}"
                    );

                    return;
                }
            }

            connection.commit();

            response.getWriter().write(
                    "{\"success\":true}"
            );

        } catch (Exception e) {

            e.printStackTrace();

            if (connection != null) {
                try {
                    connection.rollback();
                } catch (Exception rollbackError) {
                    rollbackError.printStackTrace();
                }
            }

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().write(
                    "{\"error\":\"Database error\"}"
            );

        } finally {

            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (Exception closeError) {
                    closeError.printStackTrace();
                }
            }
        }
    }
}