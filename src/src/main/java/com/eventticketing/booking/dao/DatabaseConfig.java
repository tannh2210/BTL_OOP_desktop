package com.eventticketing.booking.dao;
import java.sql.*;

public final class DatabaseConfig {
    private static final String URL =
        "jdbc:mysql://localhost:3306/event_ticketing?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh";
    private static final String USER = "root";
    private static final String PASSWORD = "YOUR_MYSQL_PASSWORD";
    private DatabaseConfig() {}
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
