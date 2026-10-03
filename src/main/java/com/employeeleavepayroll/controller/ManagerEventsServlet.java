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

@WebServlet("/ManagerEventsServlet")
public class ManagerEventsServlet extends HttpServlet {

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
            "SELECT id, title, event_date, event_time, description " +
            "FROM events " +
            "ORDER BY event_date ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                 connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            PrintWriter out = response.getWriter();

            out.print("[");

            boolean first = true;

            while (resultSet.next()) {

                if (!first) {
                    out.print(",");
                }

                out.print("{");

                out.print("\"id\":" +
                    resultSet.getInt("id") + ",");

                out.print("\"title\":\"" +
                    escapeJson(resultSet.getString("title")) + "\",");

                out.print("\"eventDate\":\"" +
                    resultSet.getDate("event_date") + "\",");

                out.print("\"eventTime\":\"" +
                    escapeJson(resultSet.getString("event_time")) + "\",");

                out.print("\"description\":\"" +
                    escapeJson(resultSet.getString("description")) + "\"");

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