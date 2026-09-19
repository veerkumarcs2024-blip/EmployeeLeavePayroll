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

@WebServlet("/AttendanceServlet")
public class AttendanceServlet extends HttpServlet {

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
                "SELECT attendance_date, check_in, check_out, status "
              + "FROM attendance "
              + "WHERE user_id = ? "
              + "ORDER BY attendance_date DESC";

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

                    String date =
                            resultSet.getDate("attendance_date").toString();

                    String checkIn =
                            resultSet.getTime("check_in") != null
                            ? resultSet.getTime("check_in").toString()
                            : "";

                    String checkOut =
                            resultSet.getTime("check_out") != null
                            ? resultSet.getTime("check_out").toString()
                            : "";

                    String status =
                            resultSet.getString("status");

                    out.println("{");
                    out.println("\"date\":\"" + date + "\",");
                    out.println("\"checkIn\":\"" + checkIn + "\",");
                    out.println("\"checkOut\":\"" + checkOut + "\",");
                    out.println("\"status\":\"" + status + "\"");
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