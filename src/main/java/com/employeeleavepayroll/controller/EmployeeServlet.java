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

@WebServlet("/EmployeeServlet")
public class EmployeeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Employees</title>");

        out.println("<style>");
        out.println("body{font-family:Arial;background:#f5f7fb;padding:30px;}");
        out.println("h1{color:#1e3a8a;}");
        out.println("table{width:100%;border-collapse:collapse;background:white;}");
        out.println("th,td{padding:12px;border:1px solid #ddd;text-align:left;}");
        out.println("th{background:#1e3a8a;color:white;}");
        out.println("</style>");

        out.println("</head>");
        out.println("<body>");

        out.println("<h1>Employee List</h1>");

        out.println("<table>");
        out.println("<tr>");
        out.println("<th>ID</th>");
        out.println("<th>Full Name</th>");
        out.println("<th>Email</th>");
        out.println("<th>Role</th>");
        out.println("<th>Status</th>");
        out.println("</tr>");

        String sql = "SELECT id, full_name, email, role, status "
                   + "FROM users "
                   + "WHERE role = 'EMPLOYEE'";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                out.println("<tr>");

                out.println("<td>" + resultSet.getInt("id") + "</td>");

                out.println("<td>"
                        + resultSet.getString("full_name")
                        + "</td>");

                out.println("<td>"
                        + resultSet.getString("email")
                        + "</td>");

                out.println("<td>"
                        + resultSet.getString("role")
                        + "</td>");

                out.println("<td>"
                        + resultSet.getString("status")
                        + "</td>");

                out.println("</tr>");
            }

        } catch (Exception e) {

            out.println("<tr>");
            out.println("<td colspan='5'>");
            out.println("Database Error: " + e.getMessage());
            out.println("</td>");
            out.println("</tr>");

            e.printStackTrace();
        }

        out.println("</table>");

        out.println("</body>");
        out.println("</html>");
    }
}