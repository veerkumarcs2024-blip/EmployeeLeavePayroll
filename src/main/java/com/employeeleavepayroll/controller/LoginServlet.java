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
import jakarta.servlet.http.HttpSession;

import com.employeeleavepayroll.util.DBConnection;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        String sql = "SELECT id, full_name, email, role, status "
                   + "FROM users WHERE email = ? AND password = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);
            statement.setString(2, password);

            ResultSet result = statement.executeQuery();

            if (result.next()) {

                String status = result.getString("status");
                String role = result.getString("role");
                String fullName = result.getString("full_name");
                int userId = result.getInt("id");

                if (!"ACTIVE".equals(status)) {

                    response.sendRedirect(
                        "login.jsp?error=Account is inactive"
                    );
                    return;
                }

                // Create login session
                HttpSession session = request.getSession();

                session.setAttribute("userId", userId);
                session.setAttribute("fullName", fullName);
                session.setAttribute("email", email);
                session.setAttribute("role", role);

                // Role-based dashboard
                switch (role) {

                    case "ADMIN":
                        response.sendRedirect("admin/admin.html");
                        break;

                    case "HR":
                        response.sendRedirect("hr/payroll_hr.html");
                        break;

                    case "MANAGER":
                        response.sendRedirect("manager/manager.html");
                        break;

                    case "TEAM_LEADER":
                        response.sendRedirect("teamleader/teamleader.html");
                        break;

                    case "EMPLOYEE":
                        response.sendRedirect("employee.html");
                        break;

                    case "INTERN":
                        response.sendRedirect("intern/intern.html");
                        break;

                    default:
                        response.sendRedirect(
                            "login.jsp?error=Invalid role"
                        );
                }

            } else {

                response.sendRedirect(
                    "login.jsp?error=Invalid email or password"
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.sendRedirect(
                "login.jsp?error=Database connection error"
            );
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.sendRedirect("login.jsp");
    }
}