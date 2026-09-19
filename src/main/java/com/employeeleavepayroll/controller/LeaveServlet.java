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

@WebServlet("/LeaveServlet")
public class LeaveServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

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

        int userId = (Integer) session.getAttribute("userId");

        String leaveType = request.getParameter("leaveType");
        String duration = request.getParameter("duration");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String days = request.getParameter("days");
        String reason = request.getParameter("reason");

        String sql =
                "INSERT INTO leave_requests "
              + "(user_id, leave_type, duration, start_date, end_date, days, reason) "
              + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setString(2, leaveType);
            statement.setString(3, duration);
            statement.setString(4, startDate);
            statement.setString(5, endDate);
            statement.setInt(6, Integer.parseInt(days));
            statement.setString(7, reason);

            int rows = statement.executeUpdate();

            PrintWriter out = response.getWriter();

            if (rows > 0) {
                out.write("{\"success\":true}");
            } else {
                response.setStatus(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR
                );
                out.write("{\"error\":\"Leave request failed\"}");
            }

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

        int userId = (Integer) session.getAttribute("userId");

        String sql =
                "SELECT id, leave_type, start_date, end_date, days, reason, status "
              + "FROM leave_requests "
              + "WHERE user_id = ? "
              + "ORDER BY applied_at DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {

                PrintWriter out = response.getWriter();

                out.println("[");

                boolean first = true;

                while (resultSet.next()) {

                    if (!first) {
                        out.println(",");
                    }

                    out.println("{");
                    out.println("\"id\":" + resultSet.getInt("id") + ",");
                    out.println("\"type\":\"" + resultSet.getString("leave_type") + "\",");
                    out.println("\"startDate\":\"" + resultSet.getDate("start_date") + "\",");
                    out.println("\"endDate\":\"" + resultSet.getDate("end_date") + "\",");
                    out.println("\"days\":" + resultSet.getInt("days") + ",");
                    out.println("\"reason\":\"" + resultSet.getString("reason") + "\",");
                    out.println("\"status\":\"" + resultSet.getString("status") + "\"");
                    out.println("}");

                    first = false;
                }

                out.println("]");
            }

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
}