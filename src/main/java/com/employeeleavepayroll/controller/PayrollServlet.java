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

@WebServlet("/PayrollServlet")
public class PayrollServlet extends HttpServlet {

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
                "SELECT payroll_month, basic_salary, allowances, "
              + "gross_salary, tax_deduction, provident_fund, "
              + "deductions, net_salary, processed_date "
              + "FROM payroll "
              + "WHERE user_id = ? "
              + "ORDER BY processed_date DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {

                PrintWriter out = response.getWriter();

                out.println("{");

                if (resultSet.next()) {

                    out.println("\"month\":\""
                            + resultSet.getString("payroll_month")
                            + "\",");

                    out.println("\"netSalary\":"
                            + resultSet.getBigDecimal("net_salary")
                            + ",");

                    out.println("\"basicSalary\":"
                            + resultSet.getBigDecimal("basic_salary")
                            + ",");

                    out.println("\"allowances\":"
                            + resultSet.getBigDecimal("allowances")
                            + ",");

                    out.println("\"deductions\":"
                            + resultSet.getBigDecimal("deductions")
                            + ",");

                    out.println("\"grossSalary\":"
                            + resultSet.getBigDecimal("gross_salary")
                            + ",");

                    out.println("\"taxDeduction\":"
                            + resultSet.getBigDecimal("tax_deduction")
                            + ",");

                    out.println("\"providentFund\":"
                            + resultSet.getBigDecimal("provident_fund")
                            + ",");

                    out.println("\"processedDate\":\""
                            + resultSet.getDate("processed_date")
                            + "\",");

                    out.println("\"payslips\":[");
                    boolean first = true;

                    do {
                        if (!first) {
                            out.println(",");
                        }

                        out.println("{");

                        out.println("\"month\":\""
                                + resultSet.getString("payroll_month")
                                + "\",");

                        out.println("\"basicSalary\":"
                                + resultSet.getBigDecimal("basic_salary")
                                + ",");

                        out.println("\"allowances\":"
                                + resultSet.getBigDecimal("allowances")
                                + ",");

                        out.println("\"grossSalary\":"
                                + resultSet.getBigDecimal("gross_salary")
                                + ",");

                        out.println("\"taxDeduction\":"
                                + resultSet.getBigDecimal("tax_deduction")
                                + ",");

                        out.println("\"providentFund\":"
                                + resultSet.getBigDecimal("provident_fund")
                                + ",");

                        out.println("\"deductions\":"
                                + resultSet.getBigDecimal("deductions")
                                + ",");

                        out.println("\"netSalary\":"
                                + resultSet.getBigDecimal("net_salary")
                                + ",");

                        out.println("\"processedDate\":\""
                                + resultSet.getDate("processed_date")
                                + "\"");

                        out.println("}");

                        first = false;

                    } while (resultSet.next());

                    out.println("]");
                    out.println("}");

                } else {

                    out.println("\"month\":\"—\",");
                    out.println("\"netSalary\":0,");
                    out.println("\"basicSalary\":0,");
                    out.println("\"allowances\":0,");
                    out.println("\"deductions\":0,");
                    out.println("\"grossSalary\":0,");
                    out.println("\"taxDeduction\":0,");
                    out.println("\"providentFund\":0,");
                    out.println("\"processedDate\":\"—\",");
                    out.println("\"payslips\":[]");
                    out.println("}");
                }
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