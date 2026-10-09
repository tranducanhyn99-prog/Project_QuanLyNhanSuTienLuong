package com.test;

import com.model.NhanVien;
import com.service.NhanVienService;
import com.session.Session;
import com.ui.auth.LoginFrame;
import com.ui.main.MainFrame;
import com.ui.theme.UITheme;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.time.LocalDate;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/** Reproduces the three GUI QA failures without connecting to a database. */
public final class GuiQaRegressionTest {
    private static int checks;
    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
        checks++;
    }

    public static void main(String[] args) throws Exception {
        checkEmails();
        checkLoginMessages();
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("SKIPPED dashboard: desktop display unavailable.");
            System.exit(2);
        }
        SwingUtilities.invokeAndWait(() -> {
            for (String role : new String[]{"DB_Admin", "HR_Manager", "Payroll_Officer", "Employee"}) {
                for (int employeeId : new int[]{-1, 1008}) {
                    Session.getInstance().login(1, employeeId, "qa", role, null);
                    MainFrame frame = new MainFrame();
                    try {
                        JLabel title = findLabel(frame.getContentPane(), "Tài khoản của bạn");
                        check(title != null, "Missing account card");
                        String expected = employeeId > 0 ? "NV #1008" : Session.getInstance().getVaiTroDisplayName();
                        JLabel value = findLabel(title.getParent(), expected);
                        check(value != null, "Wrong dashboard account for " + role + "/" + employeeId);
                        check(value.getFontMetrics(value.getFont()).stringWidth(expected) <= 265,
                                "Account label exceeds the default card width");
                    } finally { frame.dispose(); }
                }
            }
            Session.getInstance().logout();
        });
        System.out.println("PASS GUI QA regressions: " + checks + " assertions; no SQL connections.");
    }

    private static void checkEmails() throws Exception {
        NhanVien nv = new NhanVien();
        nv.setHoTen("QA"); nv.setNgaySinh(LocalDate.of(1995, 1, 15));
        nv.setNgayVaoLam(LocalDate.of(2026, 8, 1)); nv.setCccd("900000000101");
        nv.setSoDienThoai("0900000101"); nv.setLuongCoBan(BigDecimal.valueOf(26000000));
        nv.setMaPB(1); nv.setMaCV(1);
        Method validate = NhanVienService.class.getDeclaredMethod("validateNhanVien", NhanVien.class);
        validate.setAccessible(true);
        NhanVienService service = new NhanVienService();
        for (String email : new String[]{"gui-a@example.invalid", "qa@example.school", "qa@example.technology",
                "qa@example.com", "qa@sub.example.vn", "qa@example." + "a".repeat(63)}) {
            nv.setEmail(email); validate.invoke(service, nv);
            check(true, "Rejected valid email: " + email);
        }
        for (String email : new String[]{null, "", "qa.example.com", "qa@@example.com", "qa@.com",
                "qa@example.", "qa@example.c", "qa@example." + "a".repeat(64), "qa @example.com"}) {
            nv.setEmail(email);
            try { validate.invoke(service, nv); throw new AssertionError("Accepted invalid email: " + email); }
            catch (InvocationTargetException ex) {
                check("Định dạng email không hợp lệ!".equals(ex.getCause().getMessage()), "Wrong validation failure");
            }
        }
    }

    private static void checkLoginMessages() throws Exception {
        Method format = LoginFrame.class.getDeclaredMethod("loginErrorMessage", Throwable.class);
        format.setAccessible(true);
        Throwable[] errors = {
            new SQLException("Login failed; ClientConnectionId:technical", "S0001", 18456),
            new SQLException("Authentication rejected", "28000"),
            new SQLException("Connection refused; server:technical", "08S01"),
            new SQLTimeoutException("Timed out"),
            new SQLException("Database error", "42000"),
            new SQLException("Database error without SQLState"),
            new SecurityException("Internal mapping details"), new IllegalStateException("Internal details")
        };
        String[] messages = {
            "Tên đăng nhập hoặc mật khẩu không đúng.", "Tên đăng nhập hoặc mật khẩu không đúng.",
            "Không kết nối được SQL Server. Vui lòng thử lại.", "Không kết nối được SQL Server. Vui lòng thử lại.",
            "Không thể truy cập dữ liệu. Liên hệ quản trị viên.", "Không thể truy cập dữ liệu. Liên hệ quản trị viên.",
            "Tài khoản bị khóa hoặc chưa được mapping.", "Đăng nhập thất bại. Vui lòng thử lại."
        };
        UITheme.setupGlobalUI();
        for (int i = 0; i < errors.length; i++) {
            String message = (String) format.invoke(null, errors[i]);
            check(messages[i].equals(message), "Wrong login error category " + i);
            JLabel label = new JLabel(message); label.setFont(new java.awt.Font(UITheme.FONT_FAMILY, java.awt.Font.PLAIN, 12));
            check(label.getFontMetrics(label.getFont()).stringWidth(message) <= 420,
                    "Login error will be clipped at the minimum login size");
        }
    }

    private static JLabel findLabel(Container parent, String text) {
        for (Component c : parent.getComponents()) {
            if (c instanceof JLabel && text.equals(((JLabel)c).getText())) return (JLabel)c;
            if (c instanceof Container) {
                JLabel found = findLabel((Container)c, text);
                if (found != null) return found;
            }
        }
        return null;
    }
}
