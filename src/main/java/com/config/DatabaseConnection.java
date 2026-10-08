package com.config;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/** Connections always use the SQL identity authenticated by LoginFrame. */
public final class DatabaseConnection {
    private static final String URL;
    private static volatile Credentials credentials;
    private static final ThreadLocal<Credentials> workerIdentity = new ThreadLocal<>();
    static {
        try { Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); }
        catch (ClassNotFoundException ex) { throw new ExceptionInInitializerError(ex); }
        Properties config = new Properties();
        try (InputStream in = DatabaseConnection.class.getResourceAsStream("/config.properties")) {
            if (in != null) config.load(in);
        } catch (java.io.IOException ex) { throw new ExceptionInInitializerError(ex); }
        String configured = System.getProperty("db.url", System.getenv("DB_URL"));
        URL = configured != null && !configured.trim().isEmpty() ? configured : config.getProperty("db.url",
                "jdbc:sqlserver://localhost:1433;databaseName=QuanLyNhanSuTienLuong;encrypt=true;trustServerCertificate=false");
        if (URL.matches("(?is).*;\\s*(user|username|password|integratedSecurity|authentication|accessToken|accessTokenCallbackClass)\\s*=.*")) {
            throw new ExceptionInInitializerError("db.url không được chứa thông tin xác thực.");
        }
    }
    private DatabaseConnection() { }
    private static final class Credentials {
        final String user, password;
        Credentials(String user, String password) { this.user = user; this.password = password; }
    }
    public static Connection openForLogin(String user, String password) throws SQLException {
        Properties properties = new Properties();
        properties.setProperty("user", user);
        properties.setProperty("password", password);
        properties.setProperty("loginTimeout", "5");
        properties.setProperty("queryTimeout", "30");
        properties.setProperty("socketTimeout", "60000");
        return DriverManager.getConnection(URL, properties);
    }
    public static synchronized void authenticated(String user, String password) {
        credentials = new Credentials(user, password);
    }
    public static synchronized void logout() { credentials = null; }
    public static Object identityToken() { return credentials; }
    public static <T> T withIdentity(Object token, java.util.concurrent.Callable<T> work) throws Exception {
        if (token == null || token != credentials) throw new SecurityException("Phiên đăng nhập đã thay đổi.");
        Credentials previous = workerIdentity.get();
        workerIdentity.set((Credentials) token);
        try { return work.call(); }
        finally {
            if (previous == null) workerIdentity.remove(); else workerIdentity.set(previous);
        }
    }
    public static Connection getConnection() throws SQLException {
        Credentials current = workerIdentity.get() != null ? workerIdentity.get() : credentials;
        if (current == null) throw new SQLException("Vui lòng đăng nhập bằng SQL login cá nhân.", "28000");
        if (current != credentials) throw new SQLException("Phiên đăng nhập đã thay đổi.", "28000");
        Connection connection = openForLogin(current.user, current.password);
        try (CallableStatement statement = connection.prepareCall("{call dbo.sp_LayTaiKhoanHienTai}")) {
            statement.setQueryTimeout(30);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next() || !"HOAT_DONG".equals(result.getString("TrangThai"))) {
                    throw new SQLException("Danh tính SQL chưa được mapping hoặc tài khoản đã khóa.", "28000");
                }
            }
            return connection;
        } catch (SQLException ex) {
            try { connection.close(); } catch (SQLException close) { ex.addSuppressed(close); }
            throw ex;
        }
    }
}
