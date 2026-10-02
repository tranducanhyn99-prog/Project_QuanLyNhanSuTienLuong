package com.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static String url="jdbc:sqlserver://localhost:1433;databaseName=QuanLyNhanSuTienLuong;encrypt=false;trustServerCertificate=true;characterEncoding=UTF-8";
    private static String user="sa";
    private static String password="123456";

    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException ex) {
            String safeUser = (user != null && !user.trim().isEmpty()) ? user : "<chưa cấu hình>";
            String safeUrl = (url != null && !url.trim().isEmpty()) ? url : "<chưa cấu hình>";
            throw new SQLException(
                    "Lỗi kết nối CSDL (URL: " + safeUrl + ", User: " + safeUser + "): " + ex.getMessage(),
                    ex.getSQLState(),
                    ex.getErrorCode(),
                    ex
            );
        }
    }
}
