package com.test;

import com.model.NhanVien;
import com.service.NhanVienService;
import com.service.DanhMucService;
import com.session.Session;
import com.ui.auth.LoginFrame;
import com.ui.main.MainFrame;
import com.ui.theme.UITheme;
import com.ui.theme.DatabaseTask;
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
        checkAgeBoundaries();
        checkDepartmentPhones();
        checkLoginMessages();
        checkDatabaseMessages();
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

    private static void checkAgeBoundaries() throws Exception {
        NhanVien nv = new NhanVien();
        nv.setHoTen("Age QA"); nv.setCccd("900000000101"); nv.setSoDienThoai("0900000101");
        nv.setEmail("age@example.invalid"); nv.setLuongCoBan(BigDecimal.ONE); nv.setMaPB(1); nv.setMaCV(1);
        Method validate = NhanVienService.class.getDeclaredMethod("validateNhanVien", NhanVien.class);
        validate.setAccessible(true);
        String[][] cases = {
            {"2008-10-14", "2026-10-13", "false"}, {"2008-10-13", "2026-10-13", "true"},
            {"2008-10-12", "2026-10-13", "true"}, {"2008-12-31", "2026-01-01", "false"},
            {"2008-02-29", "2026-02-28", "false"}, {"2008-02-29", "2026-03-01", "true"},
            {"2006-02-28", "2024-02-29", "true"}, {"2010-01-01", "2026-10-13", "false"},
            {"1990-01-01", "2026-10-13", "true"}
        };
        for (String[] c : cases) {
            nv.setNgaySinh(LocalDate.parse(c[0])); nv.setNgayVaoLam(LocalDate.parse(c[1]));
            boolean accepted = true;
            try { validate.invoke(new NhanVienService(), nv); }
            catch (InvocationTargetException ex) {
                if (!"Nhân viên phải từ 18 tuổi trở lên khi vào làm việc!".equals(ex.getCause().getMessage())) throw ex;
                accepted = false;
            }
            check(accepted == Boolean.parseBoolean(c[2]), "Java age boundary disagrees with SQL case " + c[0] + "/" + c[1]);
        }
    }

    private static void checkDepartmentPhones() throws Exception {
        Method normalize = DanhMucService.class.getDeclaredMethod("normalizePhone", String.class);
        normalize.setAccessible(true);
        for (String phone : new String[]{null, "", "   ", "0123456789", " 012345678901234 "}) {
            Object expected = phone == null || phone.trim().isEmpty() ? null : phone.trim();
            check(java.util.Objects.equals(expected, normalize.invoke(null, phone)), "Optional department phone normalization");
        }
        Session.getInstance().login(1, -1, "phone_qa", "DB_Admin", null);
        try {
            for (String phone : new String[]{"123456789", "0123456789012345", "invalid", "+84901234567", "01234 56789", "０１２３４５６７８９"}) {
                for (boolean update : new boolean[]{false, true}) {
                    try {
                        if (update) new DanhMucService().capNhatPhongBan(1, "QA", phone, "HOAT_DONG");
                        else new DanhMucService().themPhongBan("QA", phone);
                        throw new AssertionError("Invalid department phone reached JDBC");
                    } catch (Exception ex) {
                        check("SĐT phòng ban: 10–15 chữ số hoặc để trống.".equals(ex.getMessage()), "Wrong department phone boundary error");
                    }
                }
            }
        } finally { Session.getInstance().logout(); }
    }

    private static void checkDatabaseMessages() throws Exception {
        Method format = DatabaseTask.class.getDeclaredMethod("errorMessage", Throwable.class);
        format.setAccessible(true);
        Throwable[] errors = {
            new Exception("Lỗi cập nhật vai trò: technical", new SQLException("Connection refused", "08S01")),
            new Exception("Wrapped timeout", new SQLTimeoutException("Timed out")),
            new SQLException("Login expired", "28000"),
            new SQLException("ALTER LOGIN technical details", "S0001", 15151),
            new SQLException("Permission denied", "42000", 229),
            new Exception("Kỳ lương đã chốt.", new SQLException("Kỳ lương đã chốt.", "S0001", 50000)),
            new IllegalArgumentException("Tháng phải từ 1 đến 12.")
        };
        String[] expected = {
            "Không kết nối được SQL Server. Vui lòng thử lại khi kết nối được khôi phục.",
            "Thao tác dữ liệu bị hủy hoặc quá thời gian. Kiểm tra kết quả trước khi thử lại.",
            "Phiên đăng nhập không còn hợp lệ. Vui lòng đăng nhập lại.",
            "Tài khoản không có quyền thực hiện thao tác này. Liên hệ quản trị viên.",
            "Tài khoản không có quyền thực hiện thao tác này. Liên hệ quản trị viên.",
            "Kỳ lương đã chốt.", "Tháng phải từ 1 đến 12."
        };
        for (int i = 0; i < errors.length; i++) {
            check(expected[i].equals(format.invoke(null, errors[i])), "Wrong database error category " + i);
        }
    }
}
