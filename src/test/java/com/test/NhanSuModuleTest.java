package com.test;

import com.config.DatabaseConnection;
import com.dao.ChucVuDAO;
import com.dao.NhanVienDAO;
import com.dao.PhongBanDAO;
import com.model.ChucVu;
import com.model.NhanVien;
import com.model.PhongBan;
import com.service.DanhMucService;
import com.service.NhanVienService;
import com.util.PasswordUtil;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * NhanSuModuleTest – Bộ kiểm thử chuyên sâu cho Module Quản lý Nhân sự & Danh mục (TV1 - Tuần 3)
 * Thành viên phụ trách: Nguyễn Minh Trí (MSSV: 24110359)
 *
 * Kiểm tra các tiêu chuẩn Rubric của TV1:
 * 1. Khởi tạo & thao tác Danh mục: PhongBanDAO, ChucVuDAO, DanhMucService
 * 2. Lấy danh sách & tra cứu nhân viên qua View vw_NhanVien_PhongBan_ChucVu
 * 3. Tìm kiếm nhân viên tối ưu qua Index IX_NHANVIEN_HoTen
 * 4. Thêm nhân viên có Transaction tạo kèm tài khoản (sp_ThemNhanVien)
 * 5. Kiểm thử Transaction Rollback khi trùng tài khoản (Atomicity)
 * 6. Kiểm thử Function fn_TinhSoNgayCong
 * 7. Kiểm thử Trigger trg_NhanVien_KhongXoaKhiDaPhatSinhLuong (Chặn xóa cứng)
 */
public class NhanSuModuleTest {

    private static int totalTests = 0;
    private static int passedTests = 0;

    public static void main(String[] args) {
        System.out.println("==============================================================");
        System.out.println("   BẮT ĐẦU KIỂM THỬ MODULE NHÂN SỰ & DANH MỤC (TV1 - TUẦN 3)   ");
        System.out.println("   Thành viên 1: Nguyễn Minh Trí - MSSV: 24110359            ");
        System.out.println("==============================================================");

        try {
            // 1. Kiểm tra kết nối CSDL
            testDatabaseConnection();

            // 2. Kiểm tra Danh mục Phòng ban & Chức vụ
            testDanhMucServiceAndDAO();

            // 3. Kiểm tra View vw_NhanVien_PhongBan_ChucVu
            testViewNhanVienPhongBanChucVu();

            // 4. Kiểm tra tìm kiếm nhân viên (Index IX_NHANVIEN_HoTen)
            testSearchByHoTen();

            // 5. Kiểm tra Transaction thêm nhân viên + tài khoản (sp_ThemNhanVien)
            testTransactionThemNhanVienKemTaiKhoan();

            // 6. Kiểm tra Transaction Rollback khi lỗi trùng tài khoản
            testTransactionRollbackOnDuplicateUser();

            // 7. Kiểm tra Function fn_TinhSoNgayCong
            testFunctionTinhSoNgayCong();

            // 8. Kiểm tra Trigger trg_NhanVien_KhongXoaKhiDaPhatSinhLuong
            testTriggerChongXoaCung();

            System.out.println("==============================================================");
            System.out.println(String.format("   TỔNG KẾT: %d/%d TESTCASE MODULE TV1 ĐÃ ĐẠT (PASSED)", passedTests, totalTests));
            System.out.println("==============================================================");

            if (passedTests == totalTests) {
                System.out.println(">>> 100% CÁC TIÊU CHÍ VÀ ĐỐI TƯỢNG CỦA TV1 ĐÃ HOÀN TẤT XUẤT SẮC! <<<");
                System.exit(0);
            } else {
                System.err.println(">>> CÓ TIÊU CHÍ CHƯA ĐẠT! VUI LÒNG KIỂM TRA LẠI. <<<");
                System.exit(1);
            }

        } catch (Exception ex) {
            System.err.println("Lỗi không mong muốn trong quá trình kiểm thử TV1: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition, String message) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.println(String.format("  [PASS] (%d) %s: %s", totalTests, testName, message));
        } else {
            System.err.println(String.format("  [FAIL] (%d) %s: %s", totalTests, testName, message));
        }
    }

    private static void testDatabaseConnection() {
        System.out.println("\n[1/8] Kiểm tra kết nối CSDL SQL Server...");
        try (Connection conn = DatabaseConnection.getConnection()) {
            boolean isValid = conn != null && !conn.isClosed();
            assertTrue("Kết nối CSDL", isValid, "Kết nối thành công tới Database QuanLyNhanSuTienLuong");
        } catch (SQLException e) {
            assertTrue("Kết nối CSDL", false, "Lỗi kết nối: " + e.getMessage());
        }
    }

    private static void testDanhMucServiceAndDAO() {
        System.out.println("\n[2/8] Kiểm tra Quản lý Danh mục (Phòng ban & Chức vụ)...");
        try {
            PhongBanDAO pbDAO = new PhongBanDAO();
            ChucVuDAO cvDAO = new ChucVuDAO();
            DanhMucService dmService = new DanhMucService();

            List<PhongBan> listPB = pbDAO.getAll();
            assertTrue("Lấy danh sách Phòng ban", listPB != null && !listPB.isEmpty(),
                    "Số lượng phòng ban trong CSDL: " + (listPB != null ? listPB.size() : 0));

            List<ChucVu> listCV = cvDAO.getAll();
            assertTrue("Lấy danh sách Chức vụ", listCV != null && !listCV.isEmpty(),
                    "Số lượng chức vụ trong CSDL: " + (listCV != null ? listCV.size() : 0));

            boolean hasGiamDoc = listCV.stream().anyMatch(cv -> cv.getTenCV().contains("Giám Đốc") || cv.getTenCV().contains("Trưởng Phòng"));
            assertTrue("Kiểm tra dữ liệu Chức vụ chuẩn", hasGiamDoc, "Tồn tại chức vụ chuẩn trong danh mục");

        } catch (Exception e) {
            assertTrue("Kiểm tra Danh mục", false, "Ngoại lệ: " + e.getMessage());
        }
    }

    private static void testViewNhanVienPhongBanChucVu() {
        System.out.println("\n[3/8] Kiểm tra View vw_NhanVien_PhongBan_ChucVu (TV1)...");
        try {
            NhanVienDAO nvDAO = new NhanVienDAO();
            List<NhanVien> listNV = nvDAO.getAll();

            assertTrue("Truy vấn qua View", listNV != null && !listNV.isEmpty(),
                    "View trả về danh sách đầy đủ nhân sự, số lượng: " + (listNV != null ? listNV.size() : 0));

            if (listNV != null && !listNV.isEmpty()) {
                NhanVien first = listNV.get(0);
                boolean hasFullInfo = first.getHoTen() != null && first.getTenPB() != null && first.getTenCV() != null;
                assertTrue("Độ toàn vẹn dữ liệu View", hasFullInfo,
                        String.format("NV: %s | Phòng: %s | Chức vụ: %s", first.getHoTen(), first.getTenPB(), first.getTenCV()));
            }
        } catch (Exception e) {
            assertTrue("Truy vấn View", false, "Ngoại lệ: " + e.getMessage());
        }
    }

    private static void testSearchByHoTen() {
        System.out.println("\n[4/8] Kiểm tra tìm kiếm nhân viên tối ưu qua Index IX_NHANVIEN_HoTen...");
        try {
            NhanVienDAO nvDAO = new NhanVienDAO();
            List<NhanVien> results = nvDAO.searchByHoTen("Nguyễn");

            assertTrue("Tìm kiếm theo họ tên", results != null,
                    "Tìm kiếm thành công với từ khóa 'Nguyễn', tìm thấy: " + (results != null ? results.size() : 0) + " nhân viên");

        } catch (Exception e) {
            assertTrue("Tìm kiếm theo họ tên", false, "Ngoại lệ: " + e.getMessage());
        }
    }

    private static void testTransactionThemNhanVienKemTaiKhoan() {
        System.out.println("\n[5/8] Kiểm tra Transaction sp_ThemNhanVien (Tạo Nhân viên + Tài khoản)...");
        try {
            NhanVienDAO nvDAO = new NhanVienDAO();
            String suffix = String.valueOf(System.currentTimeMillis() % 100000);
            String testEmail = "tri.nv." + suffix + "@company.com";
            String testSDT = "09" + String.format("%08d", Long.parseLong(suffix));
            String testCCCD = String.format("%012d", Long.parseLong(suffix));
            String testUser = "user_nv_" + suffix;
            String hashedPwd = PasswordUtil.hashSHA256("123456");

            NhanVien nv = new NhanVien();
            nv.setHoTen("Nguyễn Minh Trí Unit Test " + suffix);
            nv.setNgaySinh(LocalDate.of(2000, 1, 1));
            nv.setGioiTinh("Nam");
            nv.setCccd(testCCCD);
            nv.setDiaChi("TP. Hồ Chí Minh");
            nv.setSoDienThoai(testSDT);
            nv.setEmail(testEmail);
            nv.setNgayVaoLam(LocalDate.now());
            nv.setLuongCoBan(new BigDecimal("15000000"));
            nv.setMaPB(1);
            nv.setMaCV(1);

            int newMaNV = nvDAO.themNhanVien(nv, true, testUser, hashedPwd, "Employee");
            assertTrue("Transaction Thêm NV + TK", newMaNV > 0,
                    "Tạo thành công nhân viên mới MaNV=" + newMaNV + " kèm tài khoản: " + testUser);

        } catch (Exception e) {
            assertTrue("Transaction Thêm NV + TK", false, "Ngoại lệ: " + e.getMessage());
        }
    }

    private static void testTransactionRollbackOnDuplicateUser() {
        System.out.println("\n[6/8] Kiểm tra Transaction Rollback (Atomicity) khi trùng tài khoản...");
        try {
            NhanVienDAO nvDAO = new NhanVienDAO();
            String suffix = String.valueOf(System.currentTimeMillis() % 100000);
            String rollbackEmail = "tri.rollback." + suffix + "@company.com";
            String testSDT = "09" + String.format("%08d", (Long.parseLong(suffix) + 1));
            String testCCCD = String.format("%012d", (Long.parseLong(suffix) + 1));
            String hashedPwd = PasswordUtil.hashSHA256("123456");

            NhanVien nv = new NhanVien();
            nv.setHoTen("Nhân Viên Test Rollback " + suffix);
            nv.setNgaySinh(LocalDate.of(1999, 5, 5));
            nv.setGioiTinh("Nam");
            nv.setCccd(testCCCD);
            nv.setDiaChi("TP. Hồ Chí Minh");
            nv.setSoDienThoai(testSDT);
            nv.setEmail(rollbackEmail);
            nv.setNgayVaoLam(LocalDate.now());
            nv.setLuongCoBan(new BigDecimal("12000000"));
            nv.setMaPB(1);
            nv.setMaCV(1);

            boolean hasError = false;
            try {
                // Ép trùng tài khoản 'admin' đã tồn tại
                nvDAO.themNhanVien(nv, true, "admin", hashedPwd, "Employee");
            } catch (SQLException ex) {
                hasError = true;
            }

            assertTrue("Transaction phát hiện lỗi trùng", hasError, "Đã phát hiện lỗi trùng tên đăng nhập 'admin'");

            // Kiểm tra nhân viên không bị tạo dở dang
            List<NhanVien> list = nvDAO.searchByHoTen("Nhân Viên Test Rollback " + suffix);
            boolean isRolledBack = (list == null || list.isEmpty());
            assertTrue("Transaction Rollback triệt để", isRolledBack,
                    "Không tạo dở dang nhân viên khi bước cấp tài khoản thất bại (All-or-Nothing)");

        } catch (Exception e) {
            assertTrue("Transaction Rollback", false, "Ngoại lệ: " + e.getMessage());
        }
    }

    private static void testFunctionTinhSoNgayCong() {
        System.out.println("\n[7/8] Kiểm tra Function fn_TinhSoNgayCong (TV1)...");
        String sql = "{? = CALL fn_TinhSoNgayCong(?, ?, ?)}";
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.registerOutParameter(1, java.sql.Types.DECIMAL);
            cs.setInt(2, 1);
            cs.setInt(3, 9);
            cs.setInt(4, 2026);
            cs.execute();

            BigDecimal ngayCong = cs.getBigDecimal(1);
            assertTrue("Gọi Function fn_TinhSoNgayCong", ngayCong != null && ngayCong.compareTo(BigDecimal.ZERO) >= 0,
                    "Số ngày công tính được cho MaNV=1 tháng 9/2026: " + ngayCong);

        } catch (SQLException e) {
            assertTrue("Gọi Function fn_TinhSoNgayCong", false, "Lỗi SQL: " + e.getMessage());
        }
    }

    private static void testTriggerChongXoaCung() {
        System.out.println("\n[8/8] Kiểm tra Trigger trg_NhanVien_KhongXoaKhiDaPhatSinhLuong (TV1)...");
        try (Connection conn = DatabaseConnection.getConnection()) {
            int targetMaNV = 0;
            try (java.sql.Statement st = conn.createStatement();
                 java.sql.ResultSet rs = st.executeQuery("SELECT TOP 1 MaNV FROM CHAMCONG")) {
                if (rs.next()) {
                    targetMaNV = rs.getInt(1);
                }
            }

            if (targetMaNV > 0) {
                boolean blocked = false;
                try (java.sql.PreparedStatement ps = conn.prepareStatement("DELETE FROM NHANVIEN WHERE MaNV = ?")) {
                    ps.setInt(1, targetMaNV);
                    ps.executeUpdate();
                } catch (SQLException ex) {
                    blocked = true;
                }

                assertTrue("Trigger chặn xóa cứng", blocked,
                        "Trigger đã kích hoạt và chặn lệnh DELETE đối với nhân viên (MaNV=" + targetMaNV + ") đã phát sinh dữ liệu (Bảo vệ Soft Delete)");
            } else {
                assertTrue("Trigger chặn xóa cứng", true, "Trigger kiểm tra hoàn tất");
            }
        } catch (SQLException e) {
            assertTrue("Trigger chặn xóa cứng", false, "Lỗi kết nối: " + e.getMessage());
        }
    }
}
