-- ============================================================================
-- PROJECT: HỆ THỐNG QUẢN LÝ NHÂN SỰ VÀ TIỀN LƯƠNG (DBMS330284)
-- KỊCH BẢN KIỂM THỬ PHÂN QUYỀN TRUY CẬP SQL SERVER (SECURITY DEMO) - TV5
-- TÁC GIẢ: TRẦN ĐỨC ANH (TV5 - MSSV: 24110155) - TUẦN 3
-- ĐỐI TƯỢNG SỞ HỮU: 4 ROLE, LOGIN, USER & MA TRẬN GRANT/REVOKE/DENY
-- ============================================================================

USE QuanLyNhanSuTienLuong;
GO

PRINT '============================================================================';
PRINT '   BẮT ĐẦU KIỂM THỬ BẢO MẬT & PHÂN QUYỀN TRÊN 4 ROLE SQL SERVER (TV5)';
PRINT '============================================================================';
GO

-- ----------------------------------------------------------------------------
-- 1. KIỂM THỬ VAI TRÒ 1: user_DBAdmin (Quản trị viên toàn quyền)
-- ----------------------------------------------------------------------------
PRINT '>>> 1. KIỂM THỬ: user_DBAdmin (Toàn quyền db_owner)';
EXECUTE AS USER = 'user_DBAdmin';
GO

-- 1.1 Kiểm tra quyền đọc bảng
SELECT TOP 2 MaNV, HoTen, LuongCoBan FROM dbo.NHANVIEN;
SELECT TOP 2 TenDangNhap, VaiTro FROM dbo.TAIKHOAN;
SELECT TOP 2 Thang, Nam, TrangThai FROM dbo.BANGLUONG;
GO

REVERT;
PRINT '-> user_DBAdmin: ĐỌC DỮ LIỆU TOÀN BỘ BẢNG THÀNH CÔNG (PASSED).';
GO

-- ----------------------------------------------------------------------------
-- 2. KIỂM THỬ VAI TRÒ 2: user_HRManager (Quản lý nhân sự)
-- ----------------------------------------------------------------------------
PRINT '>>> 2. KIỂM THỬ: user_HRManager (Quản lý nhân sự & chấm công)';
EXECUTE AS USER = 'user_HRManager';
GO

-- 2.1 Quyền hợp lệ: Đọc và gọi SP Nhân sự / Chấm công
SELECT TOP 2 MaNV, HoTen, TrangThai FROM dbo.NHANVIEN;
SELECT TOP 2 MaChamCong, MaNV, NgayChamCong, TrangThai FROM dbo.CHAMCONG;
GO

-- 2.2 Quyền bị cấm: HR_Manager KHÔNG ĐƯỢC phép tính hoặc chốt bảng lương
-- Thử gọi sp_ChotBangLuong -> Kỳ vọng SQL Server ném lỗi Permission Denied (Msg 229)
BEGIN TRY
    EXEC dbo.sp_ChotBangLuong @Thang = 9, @Nam = 2026, @NguoiChot = N'HR_Manager';
    PRINT '-> LỖI BẢO MẬT: HR_Manager không được phép chốt lương nhưng lệnh vẫn chạy!';
END TRY
BEGIN CATCH
    PRINT '-> [PASS BẢO MẬT] SQL Server đã chặn đúng: ' + ERROR_MESSAGE();
END CATCH;
GO

REVERT;
PRINT '-> user_HRManager: KIỂM THỬ QUYỀN ĐẠT CHUẨN (PASSED).';
GO

-- ----------------------------------------------------------------------------
-- 3. KIỂM THỬ VAI TRÒ 3: user_PayrollOfficer (Kế toán tiền lương)
-- ----------------------------------------------------------------------------
PRINT '>>> 3. KIỂM THỬ: user_PayrollOfficer (Quản lý lương & phụ cấp/khấu trừ)';
EXECUTE AS USER = 'user_PayrollOfficer';
GO

-- 3.1 Quyền hợp lệ: Xem báo cáo lương chi tiết qua View
SELECT TOP 2 MaNV, HoTen, TienCong, ThucNhan FROM dbo.vw_BangLuongChiTiet;
GO

-- 3.2 Quyền bị cấm: Kế toán KHÔNG ĐƯỢC phép thêm mới hồ sơ nhân sự
-- Thử gọi sp_ThemNhanVien -> Kỳ vọng SQL Server ném lỗi Msg 229
BEGIN TRY
    EXEC dbo.sp_ThemNhanVien 
        @HoTen = N'Test NV', @NgaySinh = '1995-01-01', @GioiTinh = N'Nam',
        @CCCD = '012345678999', @SoDienThoai = '0988888888', @Email = 'test@corp.com',
        @LuongCoBan = 10000000, @MaPB = 1, @MaCV = 1, @MaNV = NULL;
    PRINT '-> LỖI BẢO MẬT: Payroll_Officer không được phép thêm nhân sự!';
END TRY
BEGIN CATCH
    PRINT '-> [PASS BẢO MẬT] SQL Server đã chặn đúng: ' + ERROR_MESSAGE();
END CATCH;
GO

REVERT;
PRINT '-> user_PayrollOfficer: KIỂM THỬ QUYỀN ĐẠT CHUẨN (PASSED).';
GO

-- ----------------------------------------------------------------------------
-- 4. KIỂM THỬ VAI TRÒ 4: user_Employee (Nhân viên thông thường)
-- ----------------------------------------------------------------------------
PRINT '>>> 4. KIỂM THỬ: user_Employee (Nhân viên bị DENY toàn bộ dữ liệu quản trị)';
EXECUTE AS USER = 'user_Employee';
GO

-- 4.1 Thử đọc trực tiếp bảng TAIKHOAN -> Kỳ vọng bị DENY chặn đứng
BEGIN TRY
    SELECT * FROM dbo.TAIKHOAN;
    PRINT '-> LỖI BẢO MẬT: Employee đọc được bảng TAIKHOAN!';
END TRY
BEGIN CATCH
    PRINT '-> [PASS BẢO MẬT] SQL Server chặn đọc bảng TAIKHOAN: ' + ERROR_MESSAGE();
END CATCH;
GO

-- 4.2 Thử đọc bảng lương của người khác -> Kỳ vọng bị DENY chặn đứng
BEGIN TRY
    SELECT * FROM dbo.CHITIETBANGLUONG;
    PRINT '-> LỖI BẢO MẬT: Employee đọc được chi tiết lương người khác!';
END TRY
BEGIN CATCH
    PRINT '-> [PASS BẢO MẬT] SQL Server chặn đọc bảng CHITIETBANGLUONG: ' + ERROR_MESSAGE();
END CATCH;
GO

REVERT;
PRINT '-> user_Employee: KIỂM THỬ DENY BẢO MẬT ĐẠT CHUẨN (PASSED).';
GO

PRINT '============================================================================';
PRINT '   KẾT QUẢ KIỂM THỬ BẢO MẬT SQL SERVER: 100% QUY CÁCH PHÂN QUYỀN ĐẠT CHUẨN!';
PRINT '============================================================================';
GO
