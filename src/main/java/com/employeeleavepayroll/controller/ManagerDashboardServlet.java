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

@WebServlet("/ManagerDashboardServlet")
public class ManagerDashboardServlet extends HttpServlet {

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

        String teamSql =
            "SELECT COUNT(*) AS total_team " +
            "FROM employees e " +
            "JOIN users u ON e.user_id = u.id " +
            "WHERE u.status = 'ACTIVE'";

        String attendanceSql =
            "SELECT " +
            "COALESCE(SUM(CASE WHEN status = 'Present' THEN 1 ELSE 0 END), 0) AS present_count, " +
            "COALESCE(SUM(CASE WHEN status = 'Absent' THEN 1 ELSE 0 END), 0) AS absent_count " +
            "FROM attendance " +
            "WHERE attendance_date = (SELECT MAX(attendance_date) FROM attendance)";

        String leaveSql =
            "SELECT COUNT(*) AS leave_count " +
            "FROM leave_requests " +
            "WHERE status = 'Approved' " +
            "AND start_date <= (SELECT MAX(attendance_date) FROM attendance) " +
            "AND end_date >= (SELECT MAX(attendance_date) FROM attendance)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement teamStatement =
                 connection.prepareStatement(teamSql);
             PreparedStatement attendanceStatement =
                 connection.prepareStatement(attendanceSql);
             PreparedStatement leaveStatement =
                 connection.prepareStatement(leaveSql);
             ResultSet teamResult = teamStatement.executeQuery();
             ResultSet attendanceResult =
                 attendanceStatement.executeQuery();
             ResultSet leaveResult =
                 leaveStatement.executeQuery()) {

            int totalTeam = 0;
            int present = 0;
            int absent = 0;
            int onLeave = 0;

            if (teamResult.next()) {
                totalTeam = teamResult.getInt("total_team");
            }

            if (attendanceResult.next()) {
                present = attendanceResult.getInt("present_count");
                absent = attendanceResult.getInt("absent_count");
            }

            if (leaveResult.next()) {
                onLeave = leaveResult.getInt("leave_count");
            }

            PrintWriter out = response.getWriter();

            out.print("{");
            out.print("\"totalTeam\":" + totalTeam + ",");
            out.print("\"present\":" + present + ",");
            out.print("\"onLeave\":" + onLeave + ",");
            out.print("\"absent\":" + absent);
            out.print("}");

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