package com.employeeleavepayroll.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.employeeleavepayroll.util.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/ManagerProfileServlet")
public class ManagerProfileServlet extends HttpServlet {

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

        int userId = (Integer) session.getAttribute("userId");

        String sql =
        	    "SELECT u.full_name, u.email, u.role, u.status, " +
        	    "e.employee_id, e.department, e.designation, e.manager, " +
        	    "e.joining_date, e.employment_type, e.phone, e.work_location, " +
        	    "e.profile_name, e.date_of_birth, e.gender, e.address, " +
        	    "e.emergency_name, e.emergency_relationship, e.emergency_phone " +
        	    "FROM users u " +
        	    "JOIN employees e ON u.id = e.user_id " +
        	    "WHERE u.id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet result = statement.executeQuery()) {

                if (!result.next()) {
                    response.setStatus(
                        HttpServletResponse.SC_NOT_FOUND
                    );

                    response.getWriter().write(
                        "{\"success\":false,\"message\":\"Profile not found\"}"
                    );
                    return;
                }

                StringBuilder json = new StringBuilder();

                json.append("{");

                json.append("\"fullName\":\"")
                    .append(escapeJson(result.getString("profile_name")))
                    .append("\",");

                json.append("\"email\":\"")
                    .append(escapeJson(result.getString("email")))
                    .append("\",");

                json.append("\"role\":\"")
                    .append(escapeJson(result.getString("role")))
                    .append("\",");

                json.append("\"status\":\"")
                    .append(escapeJson(result.getString("status")))
                    .append("\",");

                json.append("\"employeeId\":\"")
                    .append(escapeJson(result.getString("employee_id")))
                    .append("\",");

                json.append("\"department\":\"")
                    .append(escapeJson(result.getString("department")))
                    .append("\",");

                json.append("\"designation\":\"")
                    .append(escapeJson(result.getString("designation")))
                    .append("\",");

                json.append("\"manager\":\"")
                    .append(escapeJson(result.getString("manager")))
                    .append("\",");

                json.append("\"joiningDate\":\"")
                    .append(result.getDate("joining_date"))
                    .append("\",");

                json.append("\"employmentType\":\"")
                    .append(escapeJson(result.getString("employment_type")))
                    .append("\",");

                json.append("\"phone\":\"")
                    .append(escapeJson(result.getString("phone")))
                    .append("\",");

                json.append("\"workLocation\":\"")
                    .append(escapeJson(result.getString("work_location")))
                    .append("\",");

                json.append("\"dateOfBirth\":\"")
                    .append(result.getDate("date_of_birth"))
                    .append("\",");

                json.append("\"gender\":\"")
                    .append(escapeJson(result.getString("gender")))
                    .append("\",");

                json.append("\"address\":\"")
                    .append(escapeJson(result.getString("address")))
                    .append("\",");

                json.append("\"emergencyName\":\"")
                    .append(escapeJson(result.getString("emergency_name")))
                    .append("\",");

                json.append("\"emergencyRelationship\":\"")
                    .append(escapeJson(result.getString("emergency_relationship")))
                    .append("\",");

                json.append("\"emergencyPhone\":\"")
                    .append(escapeJson(result.getString("emergency_phone")))
                    .append("\"");

                json.append("}");

                response.getWriter().write(json.toString());
            }

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