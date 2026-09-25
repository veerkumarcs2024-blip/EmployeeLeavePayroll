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

@WebServlet("/ManagerTeamServlet")
public class ManagerTeamServlet extends HttpServlet {

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
                "{\"success\":false,\"message\":\"User not logged in\"}"
            );
            return;
        }

        String sql =
            "SELECT e.employee_id, u.full_name, e.department, " +
            "e.designation, e.manager, e.employment_type " +
            "FROM employees e " +
            "JOIN users u ON e.user_id = u.id " +
            "ORDER BY u.full_name";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            PrintWriter out = response.getWriter();

            out.print("[");

            boolean first = true;

            while (resultSet.next()) {

                if (!first) {
                    out.print(",");
                }

                out.print("{");
                out.print("\"employeeId\":\"" +
                    escapeJson(resultSet.getString("employee_id")) + "\",");
                out.print("\"fullName\":\"" +
                    escapeJson(resultSet.getString("full_name")) + "\",");
                out.print("\"department\":\"" +
                    escapeJson(resultSet.getString("department")) + "\",");
                out.print("\"designation\":\"" +
                    escapeJson(resultSet.getString("designation")) + "\",");
                out.print("\"manager\":\"" +
                    escapeJson(resultSet.getString("manager")) + "\",");
                out.print("\"employmentType\":\"" +
                    escapeJson(resultSet.getString("employment_type")) + "\"");
                out.print("}");

                first = false;
            }

            out.print("]");

        } catch (Exception e) {
            e.printStackTrace();

            response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Database error\"}"
            );
        }
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"");
    }
}