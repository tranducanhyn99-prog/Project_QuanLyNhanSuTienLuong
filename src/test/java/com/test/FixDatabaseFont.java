package com.test;

import com.config.DatabaseConnection;
import java.sql.Connection;
import java.sql.Statement;

public class FixDatabaseFont {
    public static void main(String[] args) {
        System.out.println("Đang cập nhật lại chuẩn font chữ Unicode tiếng Việt trong CSDL...");
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {

            // 1. Cập nhật lại tên Phòng Ban chuẩn Unicode
            st.executeUpdate("UPDATE PHONGBAN SET TenPB = N'Ban Giám Đốc' WHERE MaPB = 1;");
            st.executeUpdate("UPDATE PHONGBAN SET TenPB = N'Phòng Nhân Sự' WHERE MaPB = 2;");
            st.executeUpdate("UPDATE PHONGBAN SET TenPB = N'Phòng Kế Toán' WHERE MaPB = 3;");
            st.executeUpdate("UPDATE PHONGBAN SET TenPB = N'Phòng Kỹ Thuật' WHERE MaPB = 4;");
            System.out.println("-> Đã sửa font bảng PHONGBAN thành công!");

            // 2. Cập nhật lại tên Chức Vụ chuẩn Unicode
            st.executeUpdate("UPDATE CHUCVU SET TenCV = N'Giám Đốc' WHERE MaCV = 1;");
            st.executeUpdate("UPDATE CHUCVU SET TenCV = N'Trưởng Phòng' WHERE MaCV = 2;");
            st.executeUpdate("UPDATE CHUCVU SET TenCV = N'Phó Phòng' WHERE MaCV = 3;");
            st.executeUpdate("UPDATE CHUCVU SET TenCV = N'Chuyên Viên' WHERE MaCV = 4;");
            st.executeUpdate("UPDATE CHUCVU SET TenCV = N'Nhân Viên' WHERE MaCV = 5;");
            System.out.println("-> Đã sửa font bảng CHUCVU thành công!");

            // 3. Thêm dữ liệu nhân viên mẫu nếu chưa có
            String insertNV =
                "IF NOT EXISTS (SELECT 1 FROM NHANVIEN WHERE MaNV = 1) " +
                "BEGIN " +
                "    SET IDENTITY_INSERT NHANVIEN ON; " +
                "    INSERT INTO NHANVIEN (MaNV, HoTen, NgaySinh, GioiTinh, CCCD, DiaChi, SoDienThoai, Email, NgayVaoLam, LuongCoBan, MaPB, MaCV, TrangThai) VALUES " +
                "    (1, N'Nguyễn Minh Trí', '1995-05-15', N'Nam', '012345678901', N'Hà Nội', '0901234561', 'tri@company.com', '2022-01-10', 15000000, 2, 2, N'DANG_LAM_VIEC'), " +
                "    (2, N'Phạm Minh Quân', '1996-08-20', N'Nam', '012345678902', N'TP. Hồ Chí Minh', '0901234562', 'quan@company.com', '2022-03-15', 14000000, 3, 4, N'DANG_LAM_VIEC'), " +
                "    (3, N'Trần Tiến Đạt', '1998-11-25', N'Nam', '012345678903', N'Đà Nẵng', '0901234563', 'dat@company.com', '2023-05-01', 12000000, 4, 4, N'DANG_LAM_VIEC'), " +
                "    (4, N'Nguyễn Quang Vinh', '1997-02-18', N'Nam', '012345678904', N'Hải Phòng', '0901234564', 'vinh@company.com', '2023-07-01', 13000000, 3, 4, N'DANG_LAM_VIEC'), " +
                "    (5, N'Trần Đức Anh', '1999-09-09', N'Nam', '012345678905', N'Hưng Yên', '0901234565', 'ducanh@company.com', '2023-09-01', 16000000, 1, 1, N'DANG_LAM_VIEC'); " +
                "    SET IDENTITY_INSERT NHANVIEN OFF; " +
                "END";
            st.executeUpdate(insertNV);
            System.out.println("-> Đã nạp dữ liệu mẫu bảng NHANVIEN chuẩn Unicode thành công!");

            // 4. Gắn MaNV vào TAIKHOAN nếu chưa gắn
            st.executeUpdate("UPDATE TAIKHOAN SET MaNV = 1 WHERE TenDangNhap = 'hr_manager' AND MaNV IS NULL;");
            st.executeUpdate("UPDATE TAIKHOAN SET MaNV = 2 WHERE TenDangNhap = 'payroll_officer' AND MaNV IS NULL;");
            st.executeUpdate("UPDATE TAIKHOAN SET MaNV = 3 WHERE TenDangNhap = 'employee01' AND MaNV IS NULL;");
            System.out.println("-> Đã liên kết tài khoản mẫu với nhân viên thành công!");

            // 5. Thêm dữ liệu phụ cấp & khấu trừ mẫu
            String insertPhuCap =
                "IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE Thang = 9 AND Nam = 2026) " +
                "BEGIN " +
                "    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu) VALUES " +
                "    (1, 9, 2026, N'Phụ cấp ăn trưa', 730000, '2026-09-01', N'Phụ cấp theo ngày làm việc'), " +
                "    (1, 9, 2026, N'Hỗ trợ xăng xe', 500000, '2026-09-01', N'Đi lại công tác thường xuyên'), " +
                "    (2, 9, 2026, N'Phụ cấp ăn trưa', 730000, '2026-09-01', N'Phụ cấp cố định'), " +
                "    (2, 9, 2026, N'Phụ cấp trách nhiệm', 1500000, '2026-09-05', N'Trưởng nhóm dự án'), " +
                "    (3, 9, 2026, N'Phụ cấp ăn trưa', 730000, '2026-09-01', N'Phụ cấp cố định'), " +
                "    (4, 9, 2026, N'Phụ cấp độc hại', 1000000, '2026-09-10', N'Phòng Lab/Máy chủ'); " +
                "END";
            st.executeUpdate(insertPhuCap);

            String insertKhauTru =
                "IF NOT EXISTS (SELECT 1 FROM dbo.KHAUTRUNHANVIEN WHERE Thang = 9 AND Nam = 2026) " +
                "BEGIN " +
                "    INSERT INTO dbo.KHAUTRUNHANVIEN (MaNV, Thang, Nam, TenKhauTru, SoTien, NgayGhiNhan, LyDo) VALUES " +
                "    (1, 9, 2026, N'Tạm ứng lương', 2000000, '2026-09-15', N'Nhân viên xin ứng giữa tháng'), " +
                "    (2, 9, 2026, N'Khấu trừ đi trễ', 150000, '2026-09-20', N'Vi phạm đi trễ 3 lần'), " +
                "    (3, 9, 2026, N'Tạm ứng lương', 1000000, '2026-09-15', N'Tạm ứng lương cá nhân'), " +
                "    (4, 9, 2026, N'Bồi hoàn tài sản', 500000, '2026-09-22', N'Làm hư hỏng thiết bị văn phòng'); " +
                "END";
            st.executeUpdate(insertKhauTru);
            System.out.println("-> Đã nạp phụ cấp và khấu trừ mẫu thành công!");

            System.out.println(">>> TOÀN BỘ FONT CHỮ TIẾNG VIỆT ĐÃ ĐƯỢC FIX CHUẨN XÁC 100%! <<<");

        } catch (Exception ex) {
            System.err.println("Lỗi khi cập nhật font chữ CSDL: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
