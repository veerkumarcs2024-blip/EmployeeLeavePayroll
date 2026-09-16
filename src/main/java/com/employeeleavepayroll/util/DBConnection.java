package com.employeeleavepayroll.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    public static Connection getConnection() throws SQLException {

        Properties properties = new Properties();

        try (InputStream input = DBConnection.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (input == null) {
                throw new SQLException("db.properties file not found");
            }

            properties.load(input);

        } catch (Exception e) {
            throw new SQLException("Unable to load database configuration", e);
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            return DriverManager.getConnection(
                    properties.getProperty("db.url"),
                    properties.getProperty("db.user"),
                    properties.getProperty("db.password")
            );

        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Driver not found", e);
        }
    }
}