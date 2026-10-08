package com.test;

import com.config.DatabaseConnection;
import com.dao.BangLuongDAO;
import com.dao.ChamCongDAO;
import com.dao.NhanVienDAO;
import com.model.ChamCong;
import com.model.NhanVien;
import com.model.PhuCapNhanVien;
import com.model.KhauTruNhanVien;
import com.model.TaiKhoan;
import com.service.AuthService;
import com.service.PayrollService;
import com.service.PhuCapKhauTruService;
import com.session.Session;
import com.util.PasswordUtil;
import java.sql.*;

/** Real SQL logins, supplied by the isolated QA runner; never prints passwords. */
public final class SqlIdentityIntegrationTest {
    private static final AuthService AUTH = new AuthService();
    private static int passed;
    private static String user(String suffix) { return "qaIdentity_20261007_" + suffix; }
    private static String password(String suffix) { return System.getenv("QA_IDENTITY_PASSWORD_" + suffix); }
    private static TaiKhoan login(String suffix) throws Exception { return AUTH.login(user(suffix), password(suffix)); }
    private static void check(boolean valid, String label) {
        if (!valid) throw new AssertionError(label);
        passed++; System.out.println("PASS " + label);
    }
    private static int count(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(sql)) {
            result.next(); return result.getInt(1);
        }
    }
    private static void denied(String label, String sql) throws Exception {
        try (Connection connection = DatabaseConnection.getConnection(); Statement statement = connection.createStatement()) {
            try { statement.execute(sql); throw new AssertionError(label + " unexpectedly allowed"); }
            catch (SQLException ex) { check(ex.getErrorCode() == 229, label); }
        }
    }
    private static void rejectReset(int id, String hash, String password, String label) throws Exception {
        try (Connection connection = DatabaseConnection.getConnection();
             CallableStatement statement = connection.prepareCall("{call dbo.sp_AdminResetPassword(?,?,?)}")) {
            statement.setInt(1, id); statement.setString(2, hash); statement.setString(3, password);
            try { statement.execute(); throw new AssertionError(label + " unexpectedly accepted"); }
            catch (SQLException ex) { check(ex.getErrorCode() == 51023, label); }
        }
    }
    public static void main(String[] args) throws Exception {
        if (args.length == 1 && "--reject-config".equals(args[0])) {
            try { Class.forName("com.config.DatabaseConnection"); }
            catch (ExceptionInInitializerError ex) {
                check(ex.getMessage().contains("db.url"), "JDBC URL authentication override rejected before connection");
                return;
            }
            throw new AssertionError("Authentication property in JDBC URL was accepted");
        }
        String url = System.getenv("QA_IDENTITY_URL");
        if (url == null || !url.contains("databaseName=PRJ_Fix_QA_20261007_Identity;")) {
            System.err.println("SKIPPED: isolated identity QA runner/configuration required."); System.exit(2);
        }
        System.setProperty("db.url", url);
        for (String suffix : new String[]{"admin", "hr", "payroll", "a", "b", "legacy"}) {
            if (password(suffix) == null) throw new IllegalArgumentException("Missing QA password for " + suffix);
        }
        String[] names = {"admin", "hr", "payroll", "a"};
        String[] roles = {"DB_Admin", "HR_Manager", "Payroll_Officer", "Employee"};
        for (int i = 0; i < names.length; i++) {
            TaiKhoan account = login(names[i]);
            check(roles[i].equals(account.getVaiTro()) && account.getMatKhau() == null, "login " + roles[i]);
            try (Connection connection = DatabaseConnection.getConnection(); Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery("SELECT ORIGINAL_LOGIN()")) {
                result.next(); check(user(names[i]).equals(result.getString(1)), "SQL identity " + roles[i]);
            }
        }
        TaiKhoan a = login("a");
        int aId = a.getMaNV();
        TaiKhoan b = login("b");
        int bId = b.getMaNV(), bAccount = b.getMaTK();
        login("hr");
        NhanVienDAO personnel = new NhanVienDAO();
        NhanVien employee = personnel.getAll().stream().filter(nv -> nv.getMaNV() == aId).findFirst().orElseThrow();
        java.time.LocalDate joined = java.time.LocalDate.of(2020, 2, 3);
        employee.setNgayVaoLam(joined);
        check(personnel.update(employee), "HR updates employee including joining date");
        check(personnel.getAll().stream().filter(nv -> nv.getMaNV() == aId).findFirst().orElseThrow().getNgayVaoLam().equals(joined),
                "joining date persists and reloads through real DAO");
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(
                "INSERT dbo.CHAMCONG(MaNV,NgayChamCong,GioVao,GioRa,TrangThai) VALUES(?,'2026-08-01','08:00','17:00','CO_MAT')")) {
            statement.setInt(1, aId); statement.executeUpdate();
        }
        ChamCongDAO attendance = new ChamCongDAO();
        ChamCong day = attendance.findByNhanVienAndMonth(aId, 8, 2026).get(0);
        java.time.LocalDate moved = java.time.LocalDate.of(2026, 8, 2);
        day.setNgayChamCong(moved);
        check(attendance.updateChamCong(day), "HR updates attendance including date");
        check(attendance.findByNhanVienAndMonth(aId, 8, 2026).get(0).getNgayChamCong().equals(moved),
                "attendance date persists and reloads through real DAO");
        login("admin");
        try (Connection connection = DatabaseConnection.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("DECLARE @id INT=(SELECT MaBangLuong FROM dbo.BANGLUONG WHERE Thang=9 AND Nam=2026); EXEC dbo.sp_ChotBangLuong @id");
        }
        login("hr"); day.setNgayChamCong(java.time.LocalDate.of(2026, 9, 2));
        try { attendance.updateChamCong(day); throw new AssertionError("Attendance moved into closed period"); }
        catch (SQLException ex) { check(ex.getErrorCode() == 50000 && ex.getMessage().contains("Kỳ lương đã chốt"), "SQL rejects DAO date move into closed period"); }
        check(attendance.findByNhanVienAndMonth(aId, 8, 2026).get(0).getNgayChamCong().equals(moved), "rejected attendance date change leaves source unchanged");
        PhuCapKhauTruService adjustments = new PhuCapKhauTruService();
        adjustments.themPhuCap(new PhuCapNhanVien(aId,8,2026,"Identity QA",new java.math.BigDecimal("100"),Date.valueOf(moved),"QA"));
        adjustments.themKhauTru(new KhauTruNhanVien(aId,8,2026,"Identity QA",new java.math.BigDecimal("10"),Date.valueOf(moved),"QA"));
        check(adjustments.getListPhuCap(8,2026).size()==1 && adjustments.getTongKhauTruNV(aId,8,2026).intValueExact()==10,
                "real HR adds and reads allowance/deduction");
        login("payroll");
        PayrollService operations = new PayrollService();
        int draft = operations.tinhBangLuongThang(8,2026,26);
        check(operations.layChiTietBangLuong(draft).size()==2, "real Payroll calculates period without elevated SQL rights");
        operations.chotBangLuong(draft);
        check("DA_CHOT".equals(operations.timBangLuongTheoMa(draft).getTrangThai()), "real Payroll closes period");
        operations.huyChotBangLuong(draft);
        check("CHUA_CHOT".equals(operations.timBangLuongTheoMa(draft).getTrangThai()), "real Payroll reopens period");
        adjustments.xoaPhuCap(adjustments.getListPhuCap(8,2026).get(0).getMaPCNV());
        adjustments.xoaKhauTru(adjustments.getListKhauTru(8,2026).get(0).getMaKTNV());
        check(adjustments.getListPhuCap(8,2026).isEmpty() && adjustments.getListKhauTru(8,2026).isEmpty(), "real Payroll deletes allowance/deduction in reopened period");
        operations.xoaBangLuong(draft);
        check(operations.timBangLuongTheoKy(8,2026)==null, "real Payroll deletes draft payroll");
        login("a");
        BangLuongDAO payroll = new BangLuongDAO();
        check(payroll.getPhieuLuongByMaNV(aId).size() == 1, "A reads own payslip through DAO");
        check(payroll.getPhieuLuongByMaNV(bId).isEmpty(), "A cannot read B through DAO filter");
        try (Connection connection = DatabaseConnection.getConnection()) {
            check(count(connection, "SELECT COUNT(*) FROM dbo.vw_PhieuLuongCaNhan") == 1, "A reads own SQL view");
            check(count(connection, "SELECT COUNT(*) FROM dbo.vw_PhieuLuongCaNhan WHERE MaNV=" + bId) == 0, "A cannot read B SQL view");
        }
        denied("Employee global SQL payroll denied", "SELECT * FROM dbo.vw_BangLuongChiTiet");
        Session.getInstance().login(999, bId, user("admin"), "DB_Admin", "Forged session");
        denied("Forged Java admin cannot read SQL accounts", "SELECT * FROM dbo.TAIKHOAN");
        check(payroll.getPhieuLuongByMaNV(bId).isEmpty(), "Forged Java MaNV cannot read B");
        login("hr");
        denied("HR cannot execute payroll", "EXEC dbo.sp_TinhBangLuongThang 9,2026,26");
        login("legacy");
        login("admin");
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(
                "SELECT MatKhau FROM dbo.TAIKHOAN WHERE SqlLogin=?")) {
            statement.setString(1, user("legacy"));
            try (ResultSet result = statement.executeQuery()) {
                check(result.next() && result.getString(1).startsWith("pbkdf2-sha256$")
                        && PasswordUtil.verifyPassword(password("legacy"), result.getString(1)), "legacy hash migrated on real login");
            }
        }
        AUTH.doiTrangThaiTaiKhoan(bAccount, "KHOA");
        try { login("b"); throw new AssertionError("Disabled login accepted"); }
        catch (SQLException ex) { check(ex.getErrorCode() == 18470, "SQL login disabled"); }
        check(!Session.getInstance().isLoggedIn(), "Failed login clears Java session");
        login("admin"); AUTH.doiTrangThaiTaiKhoan(bAccount, "HOAT_DONG");
        check("Employee".equals(login("b").getVaiTro()), "SQL login re-enabled");
        login("admin");
        String proposedHash = PasswordUtil.hashPassword("Qx9!proposal");
        rejectReset(bAccount, proposedHash, null, "NULL reset password rejected at SQL boundary");
        rejectReset(bAccount, proposedHash, "Qx9!" + "x".repeat(125), "oversized reset password rejected without truncation");
        check("Employee".equals(login("b").getVaiTro()), "failed reset leaves original SQL password and hash usable");
        login("admin");
        String resetPassword = "Qr9!" + java.util.UUID.randomUUID();
        AUTH.datLaiMatKhau(bAccount, resetPassword);
        try { login("b"); throw new AssertionError("Old reset password accepted"); }
        catch (SQLException ex) { check(ex.getErrorCode() == 18456, "old SQL password rejected after reset"); }
        check("Employee".equals(AUTH.login(user("b"), resetPassword).getVaiTro()), "new password authenticates SQL and PBKDF2");
        login("admin"); AUTH.capNhatVaiTro(bAccount, "HR_Manager");
        check("HR_Manager".equals(AUTH.login(user("b"), resetPassword).getVaiTro()), "role change updates SQL and profile");
        try (Connection connection = DatabaseConnection.getConnection()) {
            check(count(connection, "SELECT COUNT(*) FROM dbo.NHANVIEN") >= 2, "promoted HR reads personnel");
        }
        login("admin"); AUTH.capNhatVaiTro(bAccount, "Employee");
        AUTH.login(user("b"), resetPassword);
        denied("downgraded Employee loses HR permissions", "SELECT * FROM dbo.NHANVIEN");
        AUTH.logout();
        try { DatabaseConnection.getConnection(); throw new AssertionError("Logout connection accepted"); }
        catch (SQLException ex) { check("28000".equals(ex.getSQLState()), "logout clears SQL credential context"); }
        System.out.println("Identity integration: " + passed + " checks PASS; no skipped checks.");
    }
}
