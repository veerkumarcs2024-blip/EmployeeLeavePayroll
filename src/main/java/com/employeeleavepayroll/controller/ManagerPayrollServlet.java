
package com.employeeleavepayroll.controller;

import java.io.IOException;
import java.math.BigDecimal;
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

@WebServlet("/ManagerPayrollServlet")
public class ManagerPayrollServlet extends HttpServlet {

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

        int managerId;

        try {
            managerId = Integer.parseInt(
                String.valueOf(session.getAttribute("userId"))
            );
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(
                "{\"success\":false,\"message\":\"Invalid user session\"}"
            );
            return;
        }

        String payrollMonth = request.getParameter("month");

        if (payrollMonth == null || payrollMonth.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                "{\"success\":false,\"message\":\"Payroll month is required\"}"
            );
            return;
        }

        String sql =
        	    "SELECT u.full_name, e.employee_id, " +
        	    "p.payroll_month, p.basic_salary, p.allowances, " +
        	    "p.gross_salary, p.tax_deduction, p.provident_fund, " +
        	    "p.deductions, p.net_salary, p.processed_date " +
        	    "FROM payroll p " +
        	    "JOIN users u ON p.user_id = u.id " +
        	    "JOIN employees e ON e.user_id = u.id " +
        	    "WHERE e.manager = (" +
        	    "    SELECT full_name FROM users WHERE id = ?" +
        	    ") " +
        	    "AND p.payroll_month = ? " +
        	    "ORDER BY p.processed_date DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

        	statement.setInt(1, managerId);
        	statement.setString(2, payrollMonth);

            try (ResultSet rs = statement.executeQuery()) {

                StringBuilder json = new StringBuilder("[");
                boolean first = true;

                while (rs.next()) {
                    if (!first) {
                        json.append(",");
                    }

                    json.append("{")
                        .append("\"fullName\":\"")
                        .append(escapeJson(rs.getString("full_name")))
                        .append("\",")
                        .append("\"employeeId\":\"")
                        .append(escapeJson(rs.getString("employee_id")))
                        .append("\",")
                        .append("\"payrollMonth\":\"")
                        .append(escapeJson(rs.getString("payroll_month")))
                        .append("\",")
                        .append("\"basicSalary\":")
                        .append(number(rs, "basic_salary")).append(",")
                        .append("\"allowances\":")
                        .append(number(rs, "allowances")).append(",")
                        .append("\"grossSalary\":")
                        .append(number(rs, "gross_salary")).append(",")
                        .append("\"taxDeduction\":")
                        .append(number(rs, "tax_deduction")).append(",")
                        .append("\"providentFund\":")
                        .append(number(rs, "provident_fund")).append(",")
                        .append("\"deductions\":")
                        .append(number(rs, "deductions")).append(",")
                        .append("\"netSalary\":")
                        .append(number(rs, "net_salary")).append(",")
                        .append("\"processedDate\":\"")
                        .append(escapeJson(rs.getString("processed_date")))
                        .append("\"")
                        .append("}");

                    first = false;
                }

                json.append("]");
                response.getWriter().write(json.toString());
            }

        } catch (Exception e) {
            getServletContext().log("Manager payroll loading failed", e);
            response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );
            response.getWriter().write(
                "{\"success\":false,\"message\":\"Unable to load payroll data\"}"
            );
        }
    }

    private String number(ResultSet rs, String column)
            throws java.sql.SQLException {
        BigDecimal value = rs.getBigDecimal(column);
        return value == null ? "0" : value.toPlainString();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }
}
