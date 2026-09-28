-- ============================================================================
-- PROJECT: HỆ THỐNG QUẢN LÝ NHÂN SỰ VÀ TIỀN LƯƠNG (DBMS330284)
-- BỔ SUNG CÁC ĐỐI TƯỢNG CSDL HOÀN THIỆN 100% RUBRIC ĐỒ ÁN (TV2 & TV3)
-- ============================================================================

USE QuanLyNhanSuTienLuong;
GO

-- 1. FUNCTION: fn_TongPhuCap (TV2)
-- Tính tổng phụ cấp của một nhân viên trong kỳ tháng/năm
CREATE OR ALTER FUNCTION dbo.fn_TongPhuCap
(
    @MaNV  INT,
    @Thang INT,
    @Nam   INT
)
RETURNS DECIMAL(18,2)
AS
BEGIN
    DECLARE @Tong DECIMAL(18,2);
    SELECT @Tong = ISNULL(SUM(SoTien), 0)
    FROM dbo.PHUCAPNHANVIEN
    WHERE MaNV = @MaNV AND Thang = @Thang AND Nam = @Nam;

    RETURN ISNULL(@Tong, 0);
END;
GO

-- 2. FUNCTION: fn_TongKhauTru (TV3)
-- Tính tổng các khoản khấu trừ của một nhân viên trong kỳ tháng/năm
CREATE OR ALTER FUNCTION dbo.fn_TongKhauTru
(
    @MaNV  INT,
    @Thang INT,
    @Nam   INT
)
RETURNS DECIMAL(18,2)
AS
BEGIN
    DECLARE @Tong DECIMAL(18,2);
    SELECT @Tong = ISNULL(SUM(SoTien), 0)
    FROM dbo.KHAUTRUNHANVIEN
    WHERE MaNV = @MaNV AND Thang = @Thang AND Nam = @Nam;

    RETURN ISNULL(@Tong, 0);
END;
GO

-- 3. VIEW: vw_TongPhuCapThang (TV3)
-- View tổng hợp số khoản và tổng số tiền phụ cấp theo nhân viên trong tháng
CREATE OR ALTER VIEW dbo.vw_TongPhuCapThang
AS
SELECT
    pc.MaNV,
    nv.HoTen,
    pc.Thang,
    pc.Nam,
    COUNT(1) AS SoKhoanPhuCap,
    SUM(pc.SoTien) AS TongTienPhuCap
FROM dbo.PHUCAPNHANVIEN pc
JOIN dbo.NHANVIEN nv ON pc.MaNV = nv.MaNV
GROUP BY pc.MaNV, nv.HoTen, pc.Thang, pc.Nam;
GO

-- 4. STORED PROCEDURE: sp_CapNhatNhanVien (TV2)
-- Cập nhật thông tin hồ sơ nhân viên có kiểm tra toàn vẹn
CREATE OR ALTER PROCEDURE dbo.sp_CapNhatNhanVien
    @MaNV        INT,
    @HoTen       NVARCHAR(100),
    @NgaySinh    DATE,
    @GioiTinh    NVARCHAR(10),
    @CCCD        VARCHAR(12),
    @DiaChi      NVARCHAR(255) = NULL,
    @SoDienThoai VARCHAR(15),
    @Email       VARCHAR(100),
    @LuongCoBan  DECIMAL(18,2),
    @MaPB        INT,
    @MaCV        INT,
    @TrangThai   NVARCHAR(20) = N'DANG_LAM_VIEC'
AS
BEGIN
    SET NOCOUNT ON;

    IF NOT EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE MaNV = @MaNV)
    BEGIN
        RAISERROR(N'Nhân viên không tồn tại trong hệ thống!', 16, 1);
        RETURN;
    END;

    IF @LuongCoBan <= 0
    BEGIN
        RAISERROR(N'Lương cơ bản phải lớn hơn 0!', 16, 1);
        RETURN;
    END;

    UPDATE dbo.NHANVIEN
    SET HoTen       = @HoTen,
        NgaySinh    = @NgaySinh,
        GioiTinh    = @GioiTinh,
        CCCD        = @CCCD,
        DiaChi      = @DiaChi,
        SoDienThoai = @SoDienThoai,
        Email       = @Email,
        LuongCoBan  = @LuongCoBan,
        MaPB        = @MaPB,
        MaCV        = @MaCV,
        TrangThai   = @TrangThai
    WHERE MaNV = @MaNV;
END;
GO
