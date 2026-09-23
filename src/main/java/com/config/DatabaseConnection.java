package com.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static String url;
    private static String user;
    private static String password;

    static {
        Properties props = new Properties();
        try (InputStream in = DatabaseConnection.class.getResourceAsStream("/config.properties")) {
            if (in != null) {
                props.load(in);
                url = props.getProperty("db.url");
                user = props.getProperty("db.user");
                password = props.getProperty("db.password");
            } else {
                // Mặc định dự phòng nếu chưa tạo file config.properties
                url = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyNhanSuTienLuong;encrypt=false;trustServerCertificate=true;";
                user = "sa";
                password = "sa";
            }
        } catch (Exception e) {
            System.err.println("Cảnh báo: Không thể tải config.properties, dùng cấu hình mặc định.");
            url = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyNhanSuTienLuong;encrypt=false;trustServerCertificate=true;";
            user = "sa";
            password = "sa";
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
