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
        // Nạp tường minh SQL Server JDBC Driver để phát hiện sớm lỗi thiếu thư viện trên classpath
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            DriverManager.setLoginTimeout(2);
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                "Không tìm thấy SQL Server JDBC Driver (com.microsoft.sqlserver.jdbc.SQLServerDriver)! "
                + "Vui lòng kiểm tra classpath hoặc biến môi trường MSSQL_JDBC_JAR."
            );
        }

        Properties props = new Properties();
        try (InputStream in = DatabaseConnection.class.getResourceAsStream("/config.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception ignored) {}

        // Ưu tiên đọc từ System Properties hoặc Biến môi trường (Environment Variables) để bảo mật
        url = System.getProperty("db.url");
        if (url == null || url.trim().isEmpty()) {
            url = System.getenv("DB_URL");
        }
        if (url == null || url.trim().isEmpty()) {
            url = props.getProperty("db.url", "jdbc:sqlserver://localhost:1433;databaseName=QuanLyNhanSuTienLuong;encrypt=false;trustServerCertificate=true");
        }

        user = System.getProperty("db.user");
        if (user == null || user.trim().isEmpty()) {
            user = System.getenv("DB_USER");
        }
        if (user == null || user.trim().isEmpty()) {
            user = props.getProperty("db.user");
        }

        password = System.getProperty("db.password");
        if (password == null || password.trim().isEmpty()) {
            password = System.getenv("DB_PASSWORD");
        }
        if (password == null || password.trim().isEmpty()) {
            password = props.getProperty("db.password");
        }
    }

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
