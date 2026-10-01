-- ============================================================================
-- PROJECT: Quản Lý Nhân Sự và Tiền Lương (DBMS330284) - Nhóm 06
-- HỌC PHẦN: Hệ Quản Trị Cơ Sở Dữ Liệu
-- MODULE: Phụ Cấp, Khấu Trừ & Nghiệp Vụ Tuần 2
-- TÁC GIẢ: TV3 - Trần Tiến Đạt (MSSV: 24110198)
-- ============================================================================

USE QuanLyNhanSuTienLuong;
GO

-- ============================================================================
-- PHẦN 1: BẢNG DỮ LIỆU VÀ CHỈ MỤC (WEEK 1 & WEEK 2)
-- ============================================================================

-- 1.1. Bảng PHUCAPNHANVIEN (Chuẩn hóa 3NF)
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

-- 1.2. Bảng KHAUTRUNHANVIEN (Chuẩn hóa 3NF)
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

-- 1.3. Index sở hữu TV3: IX_PHUCAP_MaNV_ThangNam
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = N'IX_PHUCAP_MaNV_ThangNam' AND object_id = OBJECT_ID(N'dbo.PHUCAPNHANVIEN'))
BEGIN
    CREATE NONCLUSTERED INDEX IX_PHUCAP_MaNV_ThangNam
    ON dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam)
    INCLUDE (SoTien, TenPhuCap);
END;
GO

-- ============================================================================
-- PHẦN 2: ĐỐI TƯỢNG SQL TUẦN 2 THEO MA TRẬN OWNERSHIP (TV3)
-- ============================================================================

-- 2.1. VIEW SỞ HỮU: vw_TongPhuCapThang (TV3)
-- Cung cấp tổng hợp phụ cấp theo nhân viên và tháng/năm
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

-- 2.2. FUNCTION SỞ HỮU: fn_TongKhauTru (TV3)
-- Trả về tổng tiền khấu trừ của một nhân viên trong kỳ (Dùng cho TV4 tính lương)
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

-- 2.3. TRIGGER SỞ HỮU: trg_PhuCap_KhongSuaKhiDaChotLuong (TV3)
-- Ràng buộc toàn vẹn: Không cho phép sửa đổi hoặc xóa phụ cấp khi kỳ lương tương ứng đã chốt (DA_CHOT)
CREATE OR ALTER TRIGGER dbo.trg_PhuCap_KhongSuaKhiDaChotLuong
ON dbo.PHUCAPNHANVIEN
AFTER UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM deleted d
        JOIN dbo.BANGLUONG bl ON d.Thang = bl.Thang AND d.Nam = bl.Nam
        WHERE bl.TrangThai = 'DA_CHOT'
    )
    BEGIN
        RAISERROR (N'Lỗi nghiệp vụ: Không được phép sửa đổi hoặc xóa khoản phụ cấp khi kỳ lương đã được chốt!', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END;
END;
GO
-- 2.5. TRANSACTION SỞ HỮU: sp_XoaKyLuongChuaChot (TV3)
-- Xóa chi tiết bảng lương trước, xóa bảng lương sau; Rollback toàn bộ khi lỗi hoặc kỳ đã chốt
CREATE OR ALTER PROCEDURE dbo.sp_XoaKyLuongChuaChot
    @Thang INT,
    @Nam INT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRANSACTION;
    BEGIN TRY
        -- 1. Tìm MaBangLuong và TrangThai dựa trên Thang, Nam
        DECLARE @MaBangLuong INT;
        DECLARE @TrangThai VARCHAR(15);

        SELECT
            @MaBangLuong = MaBangLuong,
            @TrangThai   = TrangThai
        FROM dbo.BANGLUONG
        WHERE Thang = @Thang AND Nam = @Nam;

        -- 2. Kiểm tra tồn tại kỳ lương
        IF @MaBangLuong IS NULL
        BEGIN
            RAISERROR (N'Không tìm thấy dữ liệu bảng lương tháng %d/%d để xóa!', 16, 1, @Thang, @Nam);
        END;

        -- 3. Kiểm tra trạng thái kỳ lương (Chỉ cho phép xóa khi CHUA_CHOT)
        IF @TrangThai = 'DA_CHOT'
        BEGIN
            RAISERROR (N'Kỳ lương tháng %d/%d đã được CHỐT SỔ! Không được phép xóa dữ liệu!', 16, 1, @Thang, @Nam);
        END;

        -- 4. Xóa các dòng con trong CHITIETBANGLUONG trước
        DELETE FROM dbo.CHITIETBANGLUONG
        WHERE MaBangLuong = @MaBangLuong;

        -- 5. Xóa dòng cha trong BANGLUONG sau
        DELETE FROM dbo.BANGLUONG
        WHERE MaBangLuong = @MaBangLuong;

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

-- 2.6. PROCEDURE Ghi nhận chấm công: sp_GhiNhanChamCong (TV3)
CREATE OR ALTER PROCEDURE dbo.sp_GhiNhanChamCong
    @MaNV         INT,
    @NgayChamCong DATE,
    @GioVao       TIME(0),
    @GioRa        TIME(0) = NULL,
    @TrangThai    NVARCHAR(20),
    @GhiChu       NVARCHAR(255) = NULL,
    @MaChamCong   INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    -- 1. Kiểm tra tham số bắt buộc không được NULL hoặc không hợp lệ
    IF @MaNV IS NULL OR @MaNV <= 0
    BEGIN
        RAISERROR (N'Mã nhân viên không hợp lệ!', 16, 1);
        RETURN;
    END;

    IF @NgayChamCong IS NULL
    BEGIN
        RAISERROR (N'Ngày chấm công không được để trống!', 16, 1);
        RETURN;
    END;

    IF @GioVao IS NULL
    BEGIN
        RAISERROR (N'Giờ vào làm không được để trống!', 16, 1);
        RETURN;
    END;

    -- 2. Kiểm tra ngày chấm công không vượt quá ngày hiện tại
    IF @NgayChamCong > CAST(GETDATE() AS DATE)
    BEGIN
        RAISERROR (N'Ngày chấm công không được vượt quá ngày hiện tại!', 16, 1);
        RETURN;
    END;

    -- 3. Kiểm tra miền giá trị hợp lệ của cột TrangThai
    IF @TrangThai IS NULL OR @TrangThai NOT IN (N'CO_MAT', N'DI_TRE', N'VE_SOM', N'VANG')
    BEGIN
        RAISERROR (N'Trạng thái chấm công không hợp lệ!', 16, 1);
        RETURN;
    END;

    -- 4. Kiểm tra độ dài ghi chú không vượt quá 255 ký tự
    IF @GhiChu IS NOT NULL AND LEN(@GhiChu) > 255
    BEGIN
        RAISERROR (N'Ghi chú không được vượt quá 255 ký tự!', 16, 1);
        RETURN;
    END;

    -- 5. Kiểm tra logic giờ ra về phải lớn hơn giờ vào làm
    IF @GioRa IS NOT NULL AND @GioRa <= @GioVao
    BEGIN
        RAISERROR (N'Giờ ra về phải lớn hơn giờ vào làm!', 16, 1);
        RETURN;
    END;

    -- 6. Kiểm tra tồn tại nhân viên và trạng thái hoạt động (giá trị thực tế TV1: DANG_LAM_VIEC / NGHI_VIEC)
    DECLARE @TrangThaiNV NVARCHAR(20);
    SELECT @TrangThaiNV = TrangThai
    FROM dbo.NHANVIEN
    WHERE MaNV = @MaNV;

    IF @TrangThaiNV IS NULL
    BEGIN
        RAISERROR (N'Nhân viên không tồn tại trong hệ thống!', 16, 1);
        RETURN;
    END;

    IF @TrangThaiNV <> N'DANG_LAM_VIEC'
    BEGIN
        RAISERROR (N'Không thể ghi nhận chấm công cho nhân viên đã nghỉ việc hoặc không hoạt động!', 16, 1);
        RETURN;
    END;

    -- 7. Kiểm tra trùng lặp bản ghi chấm công trong ngày (bảo vệ trước khi insert)
    IF EXISTS (
        SELECT 1
        FROM dbo.CHAMCONG
        WHERE MaNV = @MaNV
          AND NgayChamCong = @NgayChamCong
    )
    BEGIN
        RAISERROR (N'Nhân viên đã có bản ghi chấm công trong ngày này!', 16, 1);
        RETURN;
    END;

    -- 8. Ghi nhận chấm công (không sở hữu transaction để caller điều phối)
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, @NgayChamCong, @GioVao, @GioRa, @TrangThai, @GhiChu);

    -- 9. Trả về mã chấm công vừa sinh qua tham số OUTPUT
    SET @MaChamCong = SCOPE_IDENTITY();
END;
GO

-- 2.7. TRIGGER Kiểm tra giờ chấm công: trg_ChamCong_KiemTraGio (TV3)
CREATE OR ALTER TRIGGER dbo.trg_ChamCong_KiemTraGio
ON dbo.CHAMCONG
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    -- Kiểm tra set-based: từ chối nếu có bất kỳ dòng nào có GioRa <= GioVao
    IF EXISTS (
        SELECT 1
        FROM inserted
        WHERE GioRa IS NOT NULL
          AND GioRa <= GioVao
    )
    BEGIN
        RAISERROR (N'Lỗi: Giờ ra về phải lớn hơn giờ vào làm.', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END;
END;
GO

-- ============================================================================
-- PHẦN 3: BỘ DỮ LIỆU MẪU DEMO PHỤ CẤP & KHẤU TRỪ THEO KỲ
-- ============================================================================
-- Làm sạch dữ liệu demo của kỳ 09/2026 và 10/2026 để tránh trùng lặp
DELETE FROM dbo.PHUCAPNHANVIEN WHERE Thang IN (9, 10) AND Nam = 2026;
DELETE FROM dbo.KHAUTRUNHANVIEN WHERE Thang IN (9, 10) AND Nam = 2026;
GO

-- 3.1. Dữ liệu Phụ cấp tháng 09/2026
INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu) VALUES
(1, 9, 2026, N'Phụ cấp ăn trưa', 730000, '2026-09-01', N'Phụ cấp cố định theo tháng'),
(1, 9, 2026, N'Hỗ trợ xăng xe', 500000, '2026-09-01', N'Công tác ngoại thành'),
(2, 9, 2026, N'Phụ cấp ăn trưa', 730000, '2026-09-01', N'Phụ cấp cố định'),
(3, 9, 2026, N'Phụ cấp ăn trưa', 730000, '2026-09-01', N'Phụ cấp cố định'),
(3, 9, 2026, N'Phụ cấp trách nhiệm', 1500000, '2026-09-05', N'Trưởng nhóm phân hệ lương'),
(4, 9, 2026, N'Phụ cấp ăn trưa', 730000, '2026-09-01', N'Phụ cấp cố định'),
(5, 9, 2026, N'Phụ cấp độc hại', 1000000, '2026-09-10', N'Phòng Lab/Máy chủ');

-- 3.2. Dữ liệu Khấu trừ tháng 09/2026
INSERT INTO dbo.KHAUTRUNHANVIEN (MaNV, Thang, Nam, TenKhauTru, SoTien, NgayGhiNhan, LyDo) VALUES
(1, 9, 2026, N'Tạm ứng lương', 2000000, '2026-09-15', N'Ứng lương giải quyết việc cá nhân'),
(2, 9, 2026, N'Khấu trừ đi trễ', 150000, '2026-09-20', N'Đi trễ 3 lần có biên bản'),
(3, 9, 2026, N'Tạm ứng lương', 1000000, '2026-09-15', N'Ứng lương giữa tháng'),
(4, 9, 2026, N'Bồi hoàn tài sản', 500000, '2026-09-22', N'Làm hư chuột máy tính');

-- 3.3. Dữ liệu Phụ cấp & Khấu trừ tháng 10/2026 (Kiểm thử đa kỳ)
INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu) VALUES
(1, 10, 2026, N'Phụ cấp ăn trưa', 730000, '2026-10-01', N'Kỳ tháng 10'),
(2, 10, 2026, N'Phụ cấp ăn trưa', 730000, '2026-10-01', N'Kỳ tháng 10'),
(3, 10, 2026, N'Phụ cấp ăn trưa', 730000, '2026-10-01', N'Kỳ tháng 10');

INSERT INTO dbo.KHAUTRUNHANVIEN (MaNV, Thang, Nam, TenKhauTru, SoTien, NgayGhiNhan, LyDo) VALUES
(1, 10, 2026, N'Tạm ứng lương', 1500000, '2026-10-15', N'Ứng lương tháng 10');
GO