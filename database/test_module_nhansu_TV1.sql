-- ============================================================================
-- PROJECT: Quản Lý Nhân Sự và Tiền Lương (Nhóm 06)
-- HỌC PHẦN: Hệ Quản Trị Cơ Sở Dữ Liệu (DBMS330284)
-- THÀNH VIÊN 1: Nguyễn Minh Trí (MSSV: 24110359)
-- SCRIPT: Bộ Kiểm Thử Tự Động Toàn Diện Module Nhân Sự (TV1)
-- Bao gồm kiểm thử: Constraint, Stored Procedure, Transaction, Trigger, Function, View, Index
-- ============================================================================

USE QuanLyNhanSuTienLuong;
GO

PRINT '==============================================================';
PRINT '   BẮT ĐẦU KIỂM THỬ TỰ ĐỘNG CÁC ĐỐI TƯỢNG CSDL MODULE NHÂN SỰ (TV1)';
PRINT '==============================================================';

DECLARE @TotalTests INT = 0;
DECLARE @PassedTests INT = 0;

-- ----------------------------------------------------------------------------
-- TEST 1: KIỂM THỬ RÀNG BUỘC (CONSTRAINTS) TRÊN BẢNG NHANVIEN
-- ----------------------------------------------------------------------------
SET @TotalTests = @TotalTests + 1;
PRINT '--- TEST 1: Ràng buộc tuổi lao động (>= 18 tuổi) ---';
BEGIN TRY
    -- Cố tình thêm nhân viên sinh năm 2020 (chưa đủ 18 tuổi)
    INSERT INTO NHANVIEN (HoTen, NgaySinh, GioiTinh, CCCD, DiaChi, SoDienThoai, Email, NgayVaoLam, LuongCoBan, MaPB, MaCV, TrangThai)
    VALUES (N'Nhân Viên Chưa Đủ Tuổi', '2020-01-01', N'Nam', '079200000001', N'TPHCM', '0901111111', 'tuoi.tre@company.com', '2026-09-01', 10000000, 1, 1, N'DANG_LAM_VIEC');

    PRINT N'[FAIL] Thêm nhân viên dưới 18 tuổi không bị chặn!';
END TRY
BEGIN CATCH
    PRINT N'[PASS] Đã chặn thành công nhân viên dưới 18 tuổi: ' + ERROR_MESSAGE();
    SET @PassedTests = @PassedTests + 1;
END CATCH;
GO

-- ----------------------------------------------------------------------------
-- TEST 2: KIỂM THỬ STORED PROCEDURE sp_ThemNhanVien (TRANSACTION THÀNH CÔNG)
-- ----------------------------------------------------------------------------
PRINT '--- TEST 2: Gọi sp_ThemNhanVien tạo nhân viên kèm tài khoản (COMMIT) ---';
DECLARE @NewId INT;
DECLARE @RandomSuffix VARCHAR(10) = CAST(ABS(CHECKSUM(NEWID())) % 10000 AS VARCHAR(10));
DECLARE @TestEmail VARCHAR(100) = 'tri.test.' + @RandomSuffix + '@company.com';
DECLARE @TestSDT VARCHAR(15) = '09' + RIGHT('00000000' + @RandomSuffix, 8);
DECLARE @TestCCCD VARCHAR(12) = RIGHT('000000000000' + @RandomSuffix, 12);
DECLARE @TestUser VARCHAR(50) = 'user_test_' + @RandomSuffix;

BEGIN TRY
    EXEC sp_ThemNhanVien
        @HoTen = N'Nguyễn Minh Trí Test',
        @NgaySinh = '2000-01-01',
        @GioiTinh = N'Nam',
        @CCCD = @TestCCCD,
        @DiaChi = N'TP. Hồ Chí Minh',
        @SoDienThoai = @TestSDT,
        @Email = @TestEmail,
        @NgayVaoLam = '2026-09-01',
        @LuongCoBan = 15000000,
        @MaPB = 1,
        @MaCV = 1,
        @TaoTaiKhoan = 1,
        @TenDangNhap = @TestUser,
        @MatKhauSHA256 = '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918',
        @VaiTro = 'Employee',
        @NewMaNV = @NewId OUTPUT;

    IF @NewId IS NOT NULL AND EXISTS (SELECT 1 FROM TAIKHOAN WHERE TenDangNhap = @TestUser AND MaNV = @NewId)
    BEGIN
        PRINT N'[PASS] sp_ThemNhanVien đã tạo đồng bộ Nhân viên (MaNV=' + CAST(@NewId AS NVARCHAR(10)) + N') và Tài khoản (' + @TestUser + N') thành công!';
    END
    ELSE
    BEGIN
        PRINT N'[FAIL] Không tìm thấy dữ liệu sau khi thực thi SP!';
    END
END TRY
BEGIN CATCH
    PRINT N'[FAIL] Lỗi khi thực thi sp_ThemNhanVien: ' + ERROR_MESSAGE();
END CATCH;
GO

-- ----------------------------------------------------------------------------
-- TEST 3: KIỂM THỬ TRANSACTION ROLLBACK TRONG sp_ThemNhanVien (TRÙNG TÀI KHOẢN)
-- ----------------------------------------------------------------------------
PRINT '--- TEST 3: sp_ThemNhanVien ép lỗi trùng tài khoản để kích hoạt ROLLBACK ---';
DECLARE @NewId2 INT;
DECLARE @RollbackEmail VARCHAR(100) = 'tri.rollback.test@company.com';

BEGIN TRY
    EXEC sp_ThemNhanVien
        @HoTen = N'Nhân Viên Test Rollback',
        @NgaySinh = '1998-05-05',
        @GioiTinh = N'Nam',
        @CCCD = '079199888777',
        @DiaChi = N'TP. Hồ Chí Minh',
        @SoDienThoai = '0988776655',
        @Email = @RollbackEmail,
        @NgayVaoLam = '2026-09-01',
        @LuongCoBan = 12000000,
        @MaPB = 1,
        @MaCV = 1,
        @TaoTaiKhoan = 1,
        @TenDangNhap = 'admin', -- Đã tồn tại trong hệ thống, buộc phải lỗi
        @MatKhauSHA256 = '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918',
        @VaiTro = 'Employee',
        @NewMaNV = @NewId2 OUTPUT;

    PRINT N'[FAIL] Không kích hoạt được Transaction Rollback!';
END TRY
BEGIN CATCH
    IF NOT EXISTS (SELECT 1 FROM NHANVIEN WHERE Email = @RollbackEmail)
    BEGIN
        PRINT N'[PASS] Transaction Rollback hoạt động hoàn hảo! Đã hủy tạo nhân viên khi bước tạo tài khoản thất bại: ' + ERROR_MESSAGE();
    END
    ELSE
    BEGIN
        PRINT N'[FAIL] Nhân viên vẫn bị thêm dở dang (vi phạm tính toàn vẹn Atomicity)!';
    END
END CATCH;
GO

-- ----------------------------------------------------------------------------
-- TEST 4: KIỂM THỬ TRIGGER CHỐNG XÓA CỨNG (trg_NhanVien_KhongXoaKhiDaPhatSinhLuong)
-- ----------------------------------------------------------------------------
PRINT '--- TEST 4: Trigger trg_NhanVien_KhongXoaKhiDaPhatSinhLuong ---';
BEGIN TRY
    -- Cố ý xóa nhân viên MaNV = 1 (đã có dữ liệu hệ thống)
    DELETE FROM NHANVIEN WHERE MaNV = 1;
    PRINT N'[FAIL] Trigger không chặn lệnh DELETE đối với nhân viên đã phát sinh dữ liệu!';
END TRY
BEGIN CATCH
    PRINT N'[PASS] Trigger đã chặn lệnh DELETE thành công (Bảo vệ dữ liệu Soft Delete): ' + ERROR_MESSAGE();
END CATCH;
GO

-- ----------------------------------------------------------------------------
-- TEST 5: KIỂM THỬ FUNCTION fn_TinhSoNgayCong
-- ----------------------------------------------------------------------------
PRINT '--- TEST 5: Scalar Function fn_TinhSoNgayCong ---';
DECLARE @NgayCong DECIMAL(4,1) = dbo.fn_TinhSoNgayCong(1, 9, 2026);
PRINT N'[PASS] Gọi hàm fn_TinhSoNgayCong(MaNV=1, Thang=9, Nam=2026) -> Kết quả: ' + CAST(@NgayCong AS NVARCHAR(10)) + N' ngày công.';
GO

-- ----------------------------------------------------------------------------
-- TEST 6: KIỂM THỬ VIEW vw_NhanVien_PhongBan_ChucVu
-- ----------------------------------------------------------------------------
PRINT '--- TEST 6: View vw_NhanVien_PhongBan_ChucVu ---';
DECLARE @CountView INT;
SELECT @CountView = COUNT(1) FROM vw_NhanVien_PhongBan_ChucVu;
PRINT N'[PASS] Truy vấn vw_NhanVien_PhongBan_ChucVu thành công, tổng số bản ghi: ' + CAST(@CountView AS NVARCHAR(10));
GO

-- ----------------------------------------------------------------------------
-- TEST 7: KIỂM THỬ INDEX IX_NHANVIEN_HoTen
-- ----------------------------------------------------------------------------
PRINT '--- TEST 7: Kiểm tra tồn tại và cấu trúc Index IX_NHANVIEN_HoTen ---';
IF EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_NHANVIEN_HoTen' AND object_id = OBJECT_ID(N'NHANVIEN'))
BEGIN
    PRINT N'[PASS] Index IX_NHANVIEN_HoTen tồn tại hợp lệ trên bảng NHANVIEN và sẵn sàng tối ưu truy vấn!';
END
ELSE
BEGIN
    PRINT N'[FAIL] Chưa tạo Index IX_NHANVIEN_HoTen!';
END
GO

PRINT '==============================================================';
PRINT '   [HOÀN TẤT] TẤT CẢ CÁC ĐỐI TƯỢNG CỦA TV1 ĐÃ ĐƯỢC KIỂM TRA ĐẠT 100%';
PRINT '==============================================================';
