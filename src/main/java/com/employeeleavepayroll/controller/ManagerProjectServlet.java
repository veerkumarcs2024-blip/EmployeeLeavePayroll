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

@WebServlet("/ManagerProjectServlet")
public class ManagerProjectServlet extends HttpServlet {

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
            "SELECT id, project_name, description, status, " +
            "start_date, end_date " +
            "FROM project " +
            "ORDER BY start_date DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                 connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            StringBuilder json = new StringBuilder("[");
            boolean first = true;

            while (result.next()) {

                if (!first) {
                    json.append(",");
                }

                json.append("{");
                json.append("\"id\":")
                    .append(result.getInt("id"))
                    .append(",");

                json.append("\"projectName\":\"")
                    .append(escapeJson(
                        result.getString("project_name")))
                    .append("\",");

                json.append("\"description\":\"")
                    .append(escapeJson(
                        result.getString("description")))
                    .append("\",");

                json.append("\"status\":\"")
                    .append(escapeJson(
                        result.getString("status")))
                    .append("\",");

                json.append("\"startDate\":\"")
                    .append(result.getDate("start_date"))
                    .append("\",");

                json.append("\"endDate\":");

                if (result.getDate("end_date") == null) {
                    json.append("null");
                } else {
                    json.append("\"")
                        .append(result.getDate("end_date"))
                        .append("\"");
                }

                json.append("}");

                first = false;
            }

            json.append("]");

            response.getWriter().write(json.toString());

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