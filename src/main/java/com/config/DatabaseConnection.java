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
                throw new ExceptionInInitializerError("Không tìm thấy file config.properties! Hãy tạo từ file template.");
            }
        } catch (ExceptionInInitializerError e) {
            throw e;
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Lỗi khi nạp file cấu hình config.properties: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
