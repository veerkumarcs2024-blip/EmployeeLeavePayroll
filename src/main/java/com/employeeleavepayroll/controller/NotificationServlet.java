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

@WebServlet("/NotificationServlet")
public class NotificationServlet extends HttpServlet {

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

        int userId = (Integer) session.getAttribute("userId");

        String sql =
                "SELECT id, icon, title, message, notification_time, unread "
              + "FROM notifications "
              + "WHERE user_id = ? "
              + "ORDER BY notification_time DESC";

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

                    String icon = resultSet.getString("icon");
                    String title = resultSet.getString("title");
                    String message = resultSet.getString("message");
                    boolean unread = resultSet.getBoolean("unread");

                    out.println("{");
                    out.println("\"icon\":\"" + icon + "\",");
                    out.println("\"title\":\"" + title + "\",");
                    out.println("\"message\":\"" + message + "\",");
                    out.println("\"time\":\""
                            + resultSet.getTimestamp("notification_time")
                            + "\",");
                    out.println("\"unread\":" + unread);
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