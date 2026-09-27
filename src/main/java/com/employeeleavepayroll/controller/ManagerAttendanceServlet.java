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

@WebServlet("/ManagerAttendanceServlet")
public class ManagerAttendanceServlet extends HttpServlet {

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
        
        String selectedDate = request.getParameter("date");

        String sql =
        	    "SELECT u.full_name, e.employee_id, " +
        	    "a.attendance_date, a.check_in, a.check_out, a.status " +
        	    "FROM attendance a " +
        	    "JOIN users u ON a.user_id = u.id " +
        	    "JOIN employees e ON a.user_id = e.user_id " +
        	    "WHERE (? IS NULL OR a.attendance_date = ?) " +
        	    "ORDER BY a.attendance_date DESC, u.full_name";

        String summarySql =
            "SELECT " +
            "COALESCE(SUM(CASE WHEN status = 'Present' THEN 1 ELSE 0 END), 0) AS present_count, " +
            "COALESCE(SUM(CASE WHEN status = 'Absent' THEN 1 ELSE 0 END), 0) AS absent_count, " +
            "COALESCE(SUM(CASE WHEN status = 'Late' THEN 1 ELSE 0 END), 0) AS late_count " +
            "FROM attendance " +
            "WHERE attendance_date = (SELECT MAX(attendance_date) FROM attendance)";

        String leaveSql =
            "SELECT COUNT(*) AS leave_count " +
            "FROM leave_requests " +
            "WHERE status = 'Approved' " +
            "AND start_date <= (SELECT MAX(attendance_date) FROM attendance) " +
            "AND end_date >= (SELECT MAX(attendance_date) FROM attendance)";

        try (Connection connection = DBConnection.getConnection();
        	     PreparedStatement statement =
        	         connection.prepareStatement(sql);
        	     PreparedStatement summaryStatement =
        	         connection.prepareStatement(summarySql);
        	     PreparedStatement leaveStatement =
        	         connection.prepareStatement(leaveSql)) {

        	    if (selectedDate == null || selectedDate.isEmpty()) {
        	        statement.setNull(1, java.sql.Types.DATE);
        	        statement.setNull(2, java.sql.Types.DATE);
        	    } else {
        	        statement.setDate(1, java.sql.Date.valueOf(selectedDate));
        	        statement.setDate(2, java.sql.Date.valueOf(selectedDate));
        	    }

        	    ResultSet resultSet = statement.executeQuery();
        	    ResultSet summaryResult = summaryStatement.executeQuery();
        	    ResultSet leaveResult = leaveStatement.executeQuery();

        	    PrintWriter out = response.getWriter();

            int present = 0;
            int absent = 0;
            int late = 0;
            int onLeave = 0;

            if (summaryResult.next()) {
                present = summaryResult.getInt("present_count");
                absent = summaryResult.getInt("absent_count");
                late = summaryResult.getInt("late_count");
            }

            if (leaveResult.next()) {
                onLeave = leaveResult.getInt("leave_count");
            }

            out.print("{");

            out.print("\"records\":[");

            boolean first = true;

            while (resultSet.next()) {

                if (!first) {
                    out.print(",");
                }

                out.print("{");

                out.print("\"fullName\":\"" +
                    escapeJson(resultSet.getString("full_name")) + "\",");

                out.print("\"employeeId\":\"" +
                    escapeJson(resultSet.getString("employee_id")) + "\",");

                out.print("\"attendanceDate\":\"" +
                    resultSet.getDate("attendance_date") + "\",");

                out.print("\"checkIn\":\"" +
                    (resultSet.getTime("check_in") == null
                        ? ""
                        : resultSet.getTime("check_in")) + "\",");

                out.print("\"checkOut\":\"" +
                    (resultSet.getTime("check_out") == null
                        ? ""
                        : resultSet.getTime("check_out")) + "\",");

                out.print("\"status\":\"" +
                    escapeJson(resultSet.getString("status")) + "\"");

                out.print("}");

                first = false;
            }

            out.print("],");

            out.print("\"summary\":{");
            out.print("\"present\":" + present + ",");
            out.print("\"absent\":" + absent + ",");
            out.print("\"onLeave\":" + onLeave + ",");
            out.print("\"late\":" + late);
            out.print("}");

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

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"");
    }
}