package com.employeeleavepayroll.controller;

import java.io.IOException;
import java.sql.Connection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.employeeleavepayroll.util.DBConnection;

@WebServlet("/DatabaseTestServlet")
public class DatabaseTestServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        try (Connection connection = DBConnection.getConnection()) {

            if (connection != null && !connection.isClosed()) {

                response.getWriter().println("""
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <title>Database Connection Test</title>
                        <style>
                            body {
                                font-family: Arial, sans-serif;
                                background: #f0fdf4;
                                text-align: center;
                                padding-top: 100px;
                            }

                            .box {
                                background: white;
                                width: 500px;
                                margin: auto;
                                padding: 40px;
                                border-radius: 15px;
                                box-shadow: 0 5px 20px rgba(0,0,0,0.15);
                            }

                            h1 {
                                color: #16a34a;
                            }

                            p {
                                font-size: 18px;
                            }
                        </style>
                    </head>
                    <body>
                        <div class="box">
                            <h1>✅ Database Connected Successfully!</h1>
                            <p>Java + JDBC + MySQL connection is working.</p>
                            <p>Database: employee_leave_payroll</p>
                        </div>
                    </body>
                    </html>
                    """);

            } else {

                response.getWriter().println("""
                    <h1>❌ Database Connection Failed</h1>
                    <p>Please check MySQL username and password.</p>
                    """);
            }

        } catch (Exception e) {

            response.getWriter().println("<h1>❌ Database Connection Error</h1>");
            response.getWriter().println("<p>" + e.getMessage() + "</p>");

            e.printStackTrace();
        }
    }
}