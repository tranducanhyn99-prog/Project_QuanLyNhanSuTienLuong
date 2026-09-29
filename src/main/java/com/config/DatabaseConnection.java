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
                url = props.getProperty("db.url");
                user = props.getProperty("db.user");
                password = props.getProperty("db.password");
                if (url == null || url.trim().isEmpty()) {
                    throw new IllegalArgumentException("Thuộc tính 'db.url' chưa được cấu hình trong config.properties.");
                }
            } else {
                throw new ExceptionInInitializerError(
                    "Không tìm thấy file config.properties trên classpath! "
                    + "Hãy đảm bảo file config.properties tồn tại trong src/resources và thư mục này nằm trên classpath."
                );
            }
        } catch (ExceptionInInitializerError e) {
            throw e;
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Lỗi khi nạp file cấu hình config.properties: " + e.getMessage());
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
