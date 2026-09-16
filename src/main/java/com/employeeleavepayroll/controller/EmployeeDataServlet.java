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

import com.employeeleavepayroll.util.DBConnection;

@WebServlet("/EmployeeDataServlet")
public class EmployeeDataServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        PrintWriter out = response.getWriter();

        String sql = "SELECT id, full_name, email, role, status "
                   + "FROM users "
                   + "WHERE role = 'EMPLOYEE' "
                   + "ORDER BY id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                int id = resultSet.getInt("id");
                String fullName = resultSet.getString("full_name");
                String email = resultSet.getString("email");
                String role = resultSet.getString("role");
                String status = resultSet.getString("status");

                String statusClass =
                        "ACTIVE".equalsIgnoreCase(status)
                        ? "active"
                        : "danger";

                out.println("<tr>");

                out.println("<td><strong>"
                        + fullName
                        + "</strong></td>");

                out.println("<td>EMP-"
                        + String.format("%04d", id)
                        + "</td>");

                out.println("<td>"
                        + email
                        + "</td>");

                out.println("<td>"
                        + role
                        + "</td>");

                out.println("<td><span class=\"status "
                        + statusClass
                        + "\">"
                        + status
                        + "</span></td>");

                out.println("<td>");

                out.println("<button class=\"small-btn secondary-btn view-btn\">Edit</button>");

                out.println("<button class=\"small-btn danger-btn delete-btn\">Delete</button>");

                out.println("</td>");

                out.println("</tr>");
            }

        } catch (Exception e) {

            out.println("<tr>");
            out.println("<td colspan='6'>");
            out.println("Database Error: " + e.getMessage());
            out.println("</td>");
            out.println("</tr>");

            e.printStackTrace();
        }
    }
}