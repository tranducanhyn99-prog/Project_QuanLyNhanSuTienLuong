SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET NUMERIC_ROUNDABORT OFF;
GO

-- ============================================================================
-- PROJECT: Quản Lý Nhân Sự và Tiền Lương (DBMS330284) - Nhóm 06
-- MODULE: Phụ Cấp, Khấu Trừ & Nghiệp Vụ Liên Quan
-- TÁC GIẢ: TV3 - Trần Tiến Đạt (MSSV: 24110198)
-- ============================================================================

USE QuanLyNhanSuTienLuong;
GO

-- 1. BẢNG DỮ LIỆU CHUẨN HÓA 3NF
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = N'PHUCAPNHANVIEN')
BEGIN
    CREATE TABLE dbo.PHUCAPNHANVIEN (
        MaPCNV      INT IDENTITY(1,1) NOT NULL,
        MaNV        INT NOT NULL,
        Thang       INT NOT NULL,
        Nam         INT NOT NULL,
        TenPhuCap   NVARCHAR(100) NOT NULL,
        SoTien      DECIMAL(18,2) NOT NULL CONSTRAINT DF_PHUCAP_SoTien DEFAULT 0,
        NgayGhiNhan DATE NOT NULL CONSTRAINT DF_PHUCAP_NgayGhiNhan DEFAULT CAST(GETDATE() AS DATE),
        GhiChu      NVARCHAR(255) NULL,

        CONSTRAINT PK_PHUCAPNHANVIEN PRIMARY KEY CLUSTERED (MaPCNV),
        CONSTRAINT FK_PHUCAP_NHANVIEN FOREIGN KEY (MaNV)
            REFERENCES dbo.NHANVIEN(MaNV) ON DELETE NO ACTION ON UPDATE CASCADE,
        CONSTRAINT CK_PHUCAP_Thang CHECK (Thang BETWEEN 1 AND 12),
        CONSTRAINT CK_PHUCAP_Nam CHECK (Nam >= 2020),
        CONSTRAINT CK_PHUCAP_SoTien CHECK (SoTien >= 0)
    );
END;
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = N'KHAUTRUNHANVIEN')
BEGIN
    CREATE TABLE dbo.KHAUTRUNHANVIEN (
        MaKTNV      INT IDENTITY(1,1) NOT NULL,
        MaNV        INT NOT NULL,
        Thang       INT NOT NULL,
        Nam         INT NOT NULL,
        TenKhauTru  NVARCHAR(100) NOT NULL,
        SoTien      DECIMAL(18,2) NOT NULL CONSTRAINT DF_KHAUTRU_SoTien DEFAULT 0,
        NgayGhiNhan DATE NOT NULL CONSTRAINT DF_KHAUTRU_NgayGhiNhan DEFAULT CAST(GETDATE() AS DATE),
        LyDo        NVARCHAR(255) NULL,

        CONSTRAINT PK_KHAUTRUNHANVIEN PRIMARY KEY CLUSTERED (MaKTNV),
        CONSTRAINT FK_KHAUTRU_NHANVIEN FOREIGN KEY (MaNV)
            REFERENCES dbo.NHANVIEN(MaNV) ON DELETE NO ACTION ON UPDATE CASCADE,
        CONSTRAINT CK_KHAUTRU_Thang CHECK (Thang BETWEEN 1 AND 12),
        CONSTRAINT CK_KHAUTRU_Nam CHECK (Nam >= 2020),
        CONSTRAINT CK_KHAUTRU_SoTien CHECK (SoTien >= 0)
    );
END;
GO

-- 2. NON-CLUSTERED INDEX (TV3)
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = N'IX_PHUCAP_MaNV_ThangNam' AND object_id = OBJECT_ID(N'dbo.PHUCAPNHANVIEN'))
BEGIN
    CREATE NONCLUSTERED INDEX IX_PHUCAP_MaNV_ThangNam
    ON dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam)
    INCLUDE (SoTien, TenPhuCap);
END;
GO

-- 3. VIEW: vw_TongPhuCapThang (TV3)
CREATE OR ALTER VIEW dbo.vw_TongPhuCapThang
AS
SELECT
    pc.MaNV,
    nv.HoTen,
    pc.Thang,
    pc.Nam,
    COUNT(pc.MaPCNV) AS SoKhoanPhuCap,
    ISNULL(SUM(pc.SoTien), 0) AS TongTienPhuCap
FROM dbo.PHUCAPNHANVIEN pc
INNER JOIN dbo.NHANVIEN nv ON pc.MaNV = nv.MaNV
GROUP BY pc.MaNV, nv.HoTen, pc.Thang, pc.Nam;
GO

-- 4. SCALAR FUNCTION: fn_TongKhauTru (TV3)
CREATE OR ALTER FUNCTION dbo.fn_TongKhauTru (
    @MaNV INT,
    @Thang INT,
    @Nam INT
)
RETURNS DECIMAL(18,2)
AS
BEGIN
    DECLARE @TongKhauTru DECIMAL(18,2) = 0;

    SELECT @TongKhauTru = ISNULL(SUM(SoTien), 0)
    FROM dbo.KHAUTRUNHANVIEN
    WHERE MaNV = @MaNV AND Thang = @Thang AND Nam = @Nam;

    RETURN @TongKhauTru;
END;
GO

-- 5. TRIGGER: trg_PhuCap_KhongSuaKhiDaChotLuong (TV3)
-- Chặn thêm, sửa, xóa phụ cấp khi kỳ lương trong BANGLUONG đã 'DA_CHOT'
-- Closed-period guards are installed by module 05.


-- 6. STORED PROCEDURE NGHIỆP VỤ: sp_ThemPhuCapNhanVien (TV3)
CREATE OR ALTER PROCEDURE dbo.sp_ThemPhuCapNhanVien
    @MaNV        INT,
    @Thang       INT,
    @Nam         INT,
    @TenPhuCap   NVARCHAR(100),
    @SoTien      DECIMAL(18,2),
    @NgayGhiNhan DATE = NULL,
    @GhiChu      NVARCHAR(255) = NULL,
    @MaPCNV      INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    IF @MaNV IS NULL OR @MaNV <= 0
    BEGIN
        RAISERROR (N'Mã nhân viên không hợp lệ!', 16, 1);
        RETURN;
    END;

    IF @Thang < 1 OR @Thang > 12 OR @Nam < 2020
    BEGIN
        RAISERROR (N'Kỳ tháng hoặc năm không hợp lệ!', 16, 1);
        RETURN;
    END;

    IF @SoTien IS NULL OR @SoTien <= 0
    BEGIN
        RAISERROR (N'Số tiền phụ cấp phải lớn hơn 0!', 16, 1);
        RETURN;
    END;

    IF NOT EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE MaNV = @MaNV AND TrangThai = N'DANG_LAM_VIEC')
    BEGIN
        RAISERROR (N'Nhân viên không tồn tại hoặc đã nghỉ việc!', 16, 1);
        RETURN;
    END;

    IF EXISTS (SELECT 1 FROM dbo.BANGLUONG WHERE Thang = @Thang AND Nam = @Nam AND TrangThai = 'DA_CHOT')
    BEGIN
        RAISERROR (N'Kỳ lương này đã CHỐT SỔ, không thể bổ sung phụ cấp!', 16, 1);
        RETURN;
    END;

    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
    VALUES (@MaNV, @Thang, @Nam, @TenPhuCap, @SoTien, ISNULL(@NgayGhiNhan, CAST(GETDATE() AS DATE)), @GhiChu);

    SET @MaPCNV = SCOPE_IDENTITY();
END;
GO

-- 7. STORED PROCEDURE & TRANSACTION: sp_XoaKyLuongChuaChot (TV3)
CREATE OR ALTER PROCEDURE dbo.sp_XoaKyLuongChuaChot
    @Thang INT,
    @Nam   INT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRANSACTION;
    BEGIN TRY
        DECLARE @MaBangLuong INT;
        DECLARE @TrangThai VARCHAR(15);

        SELECT
            @MaBangLuong = MaBangLuong,
            @TrangThai   = TrangThai
        FROM dbo.BANGLUONG WITH (UPDLOCK, HOLDLOCK)
        WHERE Thang = @Thang AND Nam = @Nam;

        IF @MaBangLuong IS NULL
        BEGIN
            RAISERROR (N'Không tìm thấy dữ liệu bảng lương tháng %d/%d để xóa!', 16, 1, @Thang, @Nam);
        END;

        IF @TrangThai = 'DA_CHOT'
        BEGIN
            RAISERROR (N'Kỳ lương tháng %d/%d đã được CHỐT SỔ! Không được phép xóa dữ liệu!', 16, 1, @Thang, @Nam);
        END;

        DELETE FROM dbo.CHITIETBANGLUONG WHERE MaBangLuong = @MaBangLuong;
        DELETE FROM dbo.BANGLUONG WHERE MaBangLuong = @MaBangLuong;

        COMMIT TRANSACTION;
        PRINT N'Đã xóa hoàn tất kỳ lương chưa chốt tháng ' + CAST(@Thang AS VARCHAR(2)) + '/' + CAST(@Nam AS VARCHAR(4));
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        DECLARE @ErrorMessage NVARCHAR(4000) = ERROR_MESSAGE();
        RAISERROR (@ErrorMessage, 16, 1);
    END CATCH
END;
GO

-- ============================================================================
-- PHẦN 8: BỘ DỮ LIỆU MẪU DEMO PHỤ CẤP & KHẤU TRỪ THEO KỲ (SEED DATA)
-- ============================================================================
-- Optional demo data: run 06_Demo_Data.sql after all schema modules.

CREATE OR ALTER FUNCTION dbo.fn_TongPhuCap(@MaNV INT, @Thang INT, @Nam INT)
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
