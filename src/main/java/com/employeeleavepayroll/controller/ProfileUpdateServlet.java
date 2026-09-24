package com.employeeleavepayroll.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.employeeleavepayroll.util.DBConnection;

@WebServlet("/ProfileUpdateServlet")
public class ProfileUpdateServlet extends HttpServlet {

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
                    "{\"success\":false,\"message\":\"User not logged in\"}"
            );
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");

        if (fullName == null || fullName.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Name is required\"}"
            );
            return;
        }

        String sql =
                "UPDATE users "
              + "SET full_name = ? "
              + "WHERE id = ?";

        String employeeSql =
                "UPDATE employees "
              + "SET phone = ? "
              + "WHERE user_id = ?";

        try (Connection connection = DBConnection.getConnection()) {

            try (PreparedStatement statement =
                    connection.prepareStatement(sql)) {

                statement.setString(1, fullName.trim());
                statement.setInt(2, userId);
                statement.executeUpdate();
            }

            try (PreparedStatement statement =
                    connection.prepareStatement(employeeSql)) {

                statement.setString(1, phone);
                statement.setInt(2, userId);
                statement.executeUpdate();
            }

            session.setAttribute("fullName", fullName.trim());

            PrintWriter out = response.getWriter();

            out.write(
                    "{\"success\":true,\"message\":\"Profile updated successfully\"}"
            );

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
}