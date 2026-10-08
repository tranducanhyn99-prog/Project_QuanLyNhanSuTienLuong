package com.test;

import com.config.DatabaseConnection;
import com.session.Session;
import com.ui.auth.LoginFrame;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javax.swing.*;

/** Desktop-only UI check. Frames stay hidden; this test must never authenticate or query SQL. */
public final class LoginChipRegressionTest {
    private static int checks;
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }
    public static void main(String[] args) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("SKIPPED LoginFrame chips: desktop display unavailable.");
            System.exit(2);
        }
        System.setProperty("db.url", "jdbc:prj-loginchips:no-database");
        AtomicInteger connections = new AtomicInteger();
        DriverManager.registerDriver(new Driver() {
            public boolean acceptsURL(String url) { return url.startsWith("jdbc:prj-loginchips:"); }
            public Connection connect(String url, Properties properties) throws SQLException {
                if (!acceptsURL(url)) return null;
                connections.incrementAndGet();
                throw new SQLException("Login chip test must not connect to SQL");
            }
            public DriverPropertyInfo[] getPropertyInfo(String url, Properties properties) { return new DriverPropertyInfo[0]; }
            public int getMajorVersion() { return 1; }
            public int getMinorVersion() { return 0; }
            public boolean jdbcCompliant() { return false; }
            public Logger getParentLogger() { return Logger.getGlobal(); }
        });
        Map<String,String> chips = new LinkedHashMap<>();
        chips.put("Admin", "admin"); chips.put("HR Manager", "hr_manager");
        chips.put("Kế toán", "payroll_officer"); chips.put("Nhân viên", "employee01");
        String previousDemo = System.getProperty("app.demo");
        try {
            SwingUtilities.invokeAndWait(() -> {
                for (boolean demo : new boolean[]{false,true}) {
                    System.setProperty("app.demo",Boolean.toString(demo));
                    LoginFrame frame = new LoginFrame();
                    try {
                        check(!frame.isVisible(), "Test displayed the login window");
                        JTextField username = field(frame,"txtTenDangNhap");
                        JPasswordField password = field(frame,"txtMatKhau");
                        for (Map.Entry<String,String> chip : chips.entrySet()) {
                            JButton button = findButton(frame.getContentPane(),chip.getKey());
                            if (!demo) {
                                check(button == null,"Production component tree includes demo chip " + chip.getKey());
                            } else {
                                check(button != null,"Missing demo chip " + chip.getKey());
                                password.setText("Previous input must be cleared");
                                button.doClick(0);
                                check(chip.getValue().equals(username.getText()),"Wrong username for " + chip.getKey());
                                check(password.getPassword().length == 0,"Demo chip filled a password for " + chip.getKey());
                            }
                        }
                    } finally { frame.dispose(); }
                }
            });
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof HeadlessException || cause instanceof java.awt.AWTError) {
                System.out.println("SKIPPED LoginFrame chips: desktop display unavailable (" + cause.getClass().getSimpleName() + ").");
                System.exit(2);
            }
            throw ex;
        } finally {
            if (previousDemo == null) System.clearProperty("app.demo");
            else System.setProperty("app.demo",previousDemo);
        }
        check(connections.get() == 0,"Chip click attempted authentication/JDBC");
        check(!Session.getInstance().isLoggedIn() && DatabaseConnection.identityToken() == null,"Chip click established an authenticated session");
        System.out.println("PASS hidden LoginFrame demo chips: " + checks + " assertions; no SQL connections.");
    }
    private static JButton findButton(Container container,String label) {
        for (Component component : container.getComponents()) {
            if (component instanceof JButton && label.equals(((JButton)component).getText())) return (JButton)component;
            if (component instanceof Container) {
                JButton found = findButton((Container)component,label);
                if (found != null) return found;
            }
        }
        return null;
    }
    @SuppressWarnings("unchecked") private static <T> T field(Object owner,String name) {
        try {
            Field field = owner.getClass().getDeclaredField(name); field.setAccessible(true); return (T)field.get(owner);
        } catch (ReflectiveOperationException ex) { throw new AssertionError(ex); }
    }
}
