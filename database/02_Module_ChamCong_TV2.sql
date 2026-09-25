-- ============================================================================
-- PROJECT: HỆ THỐNG QUẢN LÝ NHÂN SỰ VÀ TIỀN LƯƠNG (DBMS330284)
-- HỌC PHẦN: HỆ QUẢN TRỊ CƠ SỞ DỮ LIỆU - NHÓM 06
-- PHÂN HỆ: QUẢN LÝ CHẤM CÔNG (ATTENDANCE)
-- TÁC GIẢ: PHẠM MINH QUÂN (TV2 - MSSV: 24110311) - TUẦN 2 (TASK 2.1 & TASK 2.4A)
-- MÔ TẢ: BẢNG DỮ LIỆU CHAMCONG, COVERING INDEX, TRIGGER VÀ STORED PROCEDURE
-- ============================================================================

USE QuanLyNhanSuTienLuong;
GO

-- ============================================================================
-- 1. TẠO BẢNG CHAMCONG (KIỂM TRA AN TOÀN TRÁNH LỖI KHI CHẠY LẠI SCRIPT)
-- ============================================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = N'CHAMCONG')
BEGIN
    CREATE TABLE dbo.CHAMCONG (
        MaChamCong    INT           IDENTITY(1,1) NOT NULL,
        MaNV          INT           NOT NULL,
        NgayChamCong  DATE          NOT NULL,
        GioVao        TIME(0)       NOT NULL,
        GioRa         TIME(0)       NULL,
        TrangThai     NVARCHAR(20)  NOT NULL CONSTRAINT DF_CHAMCONG_TrangThai DEFAULT N'CO_MAT',
        GhiChu        NVARCHAR(255) NULL,

        -- Khóa chính
        CONSTRAINT PK_CHAMCONG PRIMARY KEY CLUSTERED (MaChamCong),

        -- Khóa ngoại tham chiếu NHANVIEN(MaNV)
        CONSTRAINT FK_CHAMCONG_NHANVIEN FOREIGN KEY (MaNV)
            REFERENCES dbo.NHANVIEN(MaNV),

        -- Ràng buộc duy nhất: Mỗi nhân viên chỉ có 1 bản ghi chấm công mỗi ngày
        CONSTRAINT UQ_CHAMCONG_MaNV_Ngay UNIQUE (MaNV, NgayChamCong),

        -- Ràng buộc miền giá trị hợp lệ cho cột TrangThai
        CONSTRAINT CHK_CHAMCONG_TrangThai CHECK (TrangThai IN (N'CO_MAT', N'DI_TRE', N'VE_SOM', N'VANG')),

        -- Ràng buộc ngày chấm công không vượt quá ngày hiện tại
        CONSTRAINT CHK_CHAMCONG_Ngay CHECK (NgayChamCong <= CAST(GETDATE() AS DATE))
    );
END;
GO

-- ============================================================================
-- 2. TẠO CHỈ MỤC NON-CLUSTERED COVERING INDEX (OWNERSHIP TV2 THEO MA TRẬN)
-- ============================================================================
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = N'IX_CHAMCONG_MaNV_Ngay' AND object_id = OBJECT_ID(N'dbo.CHAMCONG'))
BEGIN
    CREATE NONCLUSTERED INDEX IX_CHAMCONG_MaNV_Ngay
    ON dbo.CHAMCONG (MaNV, NgayChamCong)
    INCLUDE (GioVao, GioRa, TrangThai);
END;
GO

-- ============================================================================
-- 3. TRIGGER KIỂM TRA GIỜ RA PHẢI LỚN HƠN GIỜ VÀO (TASK 2.4A: dbo.trg_ChamCong_KiemTraGio)
-- ============================================================================
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
-- 4. THỦ TỤC GHI NHẬN CHẤM CÔNG (TASK 2.4A: dbo.sp_GhiNhanChamCong)
-- ============================================================================
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
