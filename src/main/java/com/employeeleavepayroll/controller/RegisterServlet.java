
package com.employeeleavepayroll.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.employeeleavepayroll.util.DBConnection;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // Check password confirmation
        if (!password.equals(confirmPassword)) {
            response.sendRedirect(
                "register.jsp?error=Passwords do not match"
            );
            return;
        }

        String checkSql =
            "SELECT id FROM users WHERE email = ?";

        String insertSql =
            "INSERT INTO users "
            + "(full_name, email, password, role, status) "
            + "VALUES (?, ?, ?, 'EMPLOYEE', 'ACTIVE')";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement checkStatement =
                 connection.prepareStatement(checkSql)) {

            // Check if email already exists
            checkStatement.setString(1, email);

            ResultSet result = checkStatement.executeQuery();

            if (result.next()) {
                response.sendRedirect(
                    "register.jsp?error=Email already registered"
                );
                return;
            }

            // Create new account
            try (PreparedStatement insertStatement =
                     connection.prepareStatement(insertSql)) {

                insertStatement.setString(1, fullName);
                insertStatement.setString(2, email);
                insertStatement.setString(3, password);

                int rows = insertStatement.executeUpdate();

                if (rows > 0) {
                    response.sendRedirect(
                        "login.jsp?success=Account created successfully"
                    );
                } else {
                    response.sendRedirect(
                        "register.jsp?error=Account creation failed"
                    );
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.sendRedirect(
                "register.jsp?error=Database error"
            );
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.sendRedirect("register.jsp");
    }
}