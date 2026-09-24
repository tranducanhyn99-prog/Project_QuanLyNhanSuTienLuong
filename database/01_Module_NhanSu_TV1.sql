-- ============================================================================
-- PROJECT: Quản Lý Nhân Sự và Tiền Lương
-- HỌC PHẦN: Hệ Quản Trị Cơ Sở Dữ Liệu (DBMS330284)
-- TÁC GIẢ: Nguyễn Minh Trí (TV1 - MSSV: 24110359)
-- MODULE: Phòng ban, Chức vụ, Nhân viên (DDL, Constraints, SP, Function, Trigger, View, Index, Transaction)
-- ============================================================================

USE master;
GO

-- 1. TẠO DATABASE (nếu chưa có)
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'QuanLyNhanSuTienLuong')
BEGIN
    CREATE DATABASE QuanLyNhanSuTienLuong;
END
GO

USE QuanLyNhanSuTienLuong;
GO

-- ============================================================================
-- PHẦN 1: TẠO BẢNG VÀ RÀNG BUỘC (TABLES & CONSTRAINTS)
-- ============================================================================

-- Bảng PHONGBAN
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = N'PHONGBAN')
BEGIN
    CREATE TABLE PHONGBAN (
        MaPB          INT IDENTITY(1,1) PRIMARY KEY,
        TenPB         NVARCHAR(100) NOT NULL CONSTRAINT UQ_PHONGBAN_TenPB UNIQUE,
        SoDienThoai   VARCHAR(15)   NULL,
        TrangThai     NVARCHAR(20)  NOT NULL CONSTRAINT DF_PHONGBAN_TrangThai DEFAULT N'HOAT_DONG',
        CONSTRAINT CHK_PHONGBAN_TrangThai CHECK (TrangThai IN (N'HOAT_DONG', N'NGUNG_HOAT_DONG')),
        CONSTRAINT CHK_PHONGBAN_SDT CHECK (SoDienThoai IS NULL OR (LEN(SoDienThoai) >= 10 AND SoDienThoai NOT LIKE '%[^0-9]%'))
    );
END
GO

-- Bảng CHUCVU
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = N'CHUCVU')
BEGIN
    CREATE TABLE CHUCVU (
        MaCV          INT IDENTITY(1,1) PRIMARY KEY,
        TenCV         NVARCHAR(100) NOT NULL CONSTRAINT UQ_CHUCVU_TenCV UNIQUE,
        PhuCapChucVu  DECIMAL(18,2) NOT NULL CONSTRAINT DF_CHUCVU_PhuCap DEFAULT 0,
        CONSTRAINT CHK_CHUCVU_PhuCap CHECK (PhuCapChucVu >= 0)
    );
END
GO

-- Bảng NHANVIEN
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = N'NHANVIEN')
BEGIN
    CREATE TABLE NHANVIEN (
        MaNV          INT IDENTITY(1,1) PRIMARY KEY,
        HoTen         NVARCHAR(100) NOT NULL,
        NgaySinh      DATE          NOT NULL,
        GioiTinh      NVARCHAR(10)  NOT NULL,
        CCCD          VARCHAR(12)   NOT NULL CONSTRAINT UQ_NHANVIEN_CCCD UNIQUE,
        DiaChi        NVARCHAR(255) NULL,
        SoDienThoai   VARCHAR(15)   NOT NULL CONSTRAINT UQ_NHANVIEN_SDT UNIQUE,
        Email         VARCHAR(100)  NOT NULL CONSTRAINT UQ_NHANVIEN_Email UNIQUE,
        NgayVaoLam    DATE          NOT NULL CONSTRAINT DF_NHANVIEN_NgayVaoLam DEFAULT GETDATE(),
        LuongCoBan    DECIMAL(18,2) NOT NULL,
        MaPB          INT           NOT NULL,
        MaCV          INT           NOT NULL,
        TrangThai     NVARCHAR(20)  NOT NULL CONSTRAINT DF_NHANVIEN_TrangThai DEFAULT N'DANG_LAM_VIEC',

        -- Constraints
        CONSTRAINT FK_NHANVIEN_PHONGBAN FOREIGN KEY (MaPB) REFERENCES PHONGBAN(MaPB),
        CONSTRAINT FK_NHANVIEN_CHUCVU   FOREIGN KEY (MaCV) REFERENCES CHUCVU(MaCV),
        CONSTRAINT CHK_NHANVIEN_GioiTinh CHECK (GioiTinh IN (N'Nam', N'Nữ', N'Khác')),
        CONSTRAINT CHK_NHANVIEN_TrangThai CHECK (TrangThai IN (N'DANG_LAM_VIEC', N'NGHI_VIEC')),
        CONSTRAINT CHK_NHANVIEN_LuongCoBan CHECK (LuongCoBan > 0),
        CONSTRAINT CHK_NHANVIEN_DoTuoi CHECK (DATEDIFF(YEAR, NgaySinh, NgayVaoLam) >= 18),
        CONSTRAINT CHK_NHANVIEN_CCCD CHECK (LEN(CCCD) = 12 AND CCCD NOT LIKE '%[^0-9]%'),
        CONSTRAINT CHK_NHANVIEN_SDT CHECK (LEN(SoDienThoai) = 10 AND SoDienThoai LIKE '0%' AND SoDienThoai NOT LIKE '%[^0-9]%'),
        CONSTRAINT CHK_NHANVIEN_Email CHECK (Email LIKE '%_@__%.__%')
    );
END
GO

-- Bảng TAIKHOAN (Theo thiết kế bảo mật của TV5, cần thiết để tạo quan hệ và transaction)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = N'TAIKHOAN')
BEGIN
    CREATE TABLE TAIKHOAN (
        MaTK          INT IDENTITY(1,1) PRIMARY KEY,
        MaNV          INT           NULL,
        TenDangNhap   VARCHAR(50)   NOT NULL CONSTRAINT UQ_TAIKHOAN_TenDangNhap UNIQUE,
        MatKhau       CHAR(64)      NOT NULL, -- SHA-256 (64 hex characters)
        VaiTro        VARCHAR(30)   NOT NULL,
        TrangThai     VARCHAR(10)   NOT NULL CONSTRAINT DF_TAIKHOAN_TrangThai DEFAULT 'HOAT_DONG',
        NgayTao       DATE          NOT NULL CONSTRAINT DF_TAIKHOAN_NgayTao DEFAULT GETDATE(),
        NgaySuaCuoi   DATETIME      NULL,
        CONSTRAINT FK_TAIKHOAN_NHANVIEN FOREIGN KEY (MaNV) REFERENCES NHANVIEN(MaNV),
        CONSTRAINT CHK_TAIKHOAN_VaiTro CHECK (VaiTro IN ('DB_Admin','HR_Manager','Payroll_Officer','Employee')),
        CONSTRAINT CHK_TAIKHOAN_TrangThai CHECK (TrangThai IN ('HOAT_DONG','KHOA'))
    );
END
GO

-- ============================================================================
-- PHẦN 2: INDEX THEO PHÂN CÔNG (TV1: IX_NHANVIEN_HoTen)
-- ============================================================================
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = N'IX_NHANVIEN_HoTen' AND object_id = OBJECT_ID(N'NHANVIEN'))
BEGIN
    CREATE NONCLUSTERED INDEX IX_NHANVIEN_HoTen
    ON NHANVIEN (HoTen)
    INCLUDE (MaNV, SoDienThoai, Email, MaPB, MaCV, TrangThai);
END
GO

-- ============================================================================
-- PHẦN 3: VIEW THEO PHÂN CÔNG (TV1: vw_NhanVien_PhongBan_ChucVu)
-- ============================================================================
CREATE OR ALTER VIEW vw_NhanVien_PhongBan_ChucVu
AS
SELECT 
    nv.MaNV,
    nv.HoTen,
    nv.NgaySinh,
    nv.GioiTinh,
    nv.CCCD,
    nv.DiaChi,
    nv.SoDienThoai,
    nv.Email,
    nv.NgayVaoLam,
    nv.LuongCoBan,
    nv.TrangThai,
    pb.MaPB,
    pb.TenPB,
    cv.MaCV,
    cv.TenCV,
    cv.PhuCapChucVu,
    tk.TenDangNhap,
    tk.VaiTro,
    tk.TrangThai AS TrangThaiTaiKhoan
FROM NHANVIEN nv
JOIN PHONGBAN pb ON nv.MaPB = pb.MaPB
JOIN CHUCVU cv ON nv.MaCV = cv.MaCV
LEFT JOIN TAIKHOAN tk ON nv.MaNV = tk.MaNV;
GO

-- ============================================================================
-- PHẦN 4: FUNCTION THEO PHÂN CÔNG (TV1: fn_TinhSoNgayCong)
-- ============================================================================
CREATE OR ALTER FUNCTION fn_TinhSoNgayCong
(
    @MaNV INT,
    @Thang INT,
    @Nam INT
)
RETURNS DECIMAL(4,1)
AS
BEGIN
    DECLARE @SoNgayCong DECIMAL(4,1) = 0;

    -- Kiểm tra bảng CHAMCONG nếu bảng đã tồn tại (khớp thiết kế của TV2)
    IF EXISTS (SELECT * FROM sys.tables WHERE name = N'CHAMCONG')
    BEGIN
        SELECT @SoNgayCong = COUNT(1)
        FROM CHAMCONG
        WHERE MaNV = @MaNV 
          AND MONTH(NgayChamCong) = @Thang 
          AND YEAR(NgayChamCong) = @Nam
          AND TrangThai IN (N'CO_MAT', N'DI_TRE', N'VE_SOM');
    END

    RETURN @SoNgayCong;
END;
GO

-- ============================================================================
-- PHẦN 5: TRIGGER THEO PHÂN CÔNG (TV1: trg_NhanVien_KhongXoaKhiDaPhatSinhLuong)
-- ============================================================================
CREATE OR ALTER TRIGGER trg_NhanVien_KhongXoaKhiDaPhatSinhLuong
ON NHANVIEN
INSTEAD OF DELETE
AS
BEGIN
    SET NOCOUNT ON;

    -- Kiểm tra xem nhân viên chuẩn bị xóa có phát sinh lương trong CHITIETBANGLUONG hoặc bảng chấm công không
    IF EXISTS (
        SELECT 1 
        FROM deleted d
        WHERE (EXISTS (SELECT 1 FROM sys.tables WHERE name = N'CHITIETBANGLUONG') 
               AND EXISTS (SELECT 1 FROM CHITIETBANGLUONG ct WHERE ct.MaNV = d.MaNV))
           OR (EXISTS (SELECT 1 FROM sys.tables WHERE name = N'CHAMCONG') 
               AND EXISTS (SELECT 1 FROM CHAMCONG cc WHERE cc.MaNV = d.MaNV))
    )
    BEGIN
        RAISERROR (N'Không được phép xóa nhân viên đã có dữ liệu chấm công hoặc lương. Vui lòng chuyển trạng thái sang NGHI_VIEC!', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END

    -- Nếu chưa phát sinh bất kỳ dữ liệu nghiệp vụ nào, cho phép xóa mềm/xóa tài khoản trước rồi xóa nhân viên
    BEGIN TRY
        DELETE FROM TAIKHOAN WHERE MaNV IN (SELECT MaNV FROM deleted);
        DELETE FROM NHANVIEN WHERE MaNV IN (SELECT MaNV FROM deleted);
    END TRY
    BEGIN CATCH
        DECLARE @ErrMsg NVARCHAR(4000) = ERROR_MESSAGE();
        RAISERROR (@ErrMsg, 16, 1);
        ROLLBACK TRANSACTION;
    END CATCH
END;
GO

-- ============================================================================
-- PHẦN 6: STORED PROCEDURE THEO PHÂN CÔNG (TV1: sp_ThemNhanVien)
-- Có Transaction và tùy chọn tạo luôn Tài khoản (Transaction đôi)
-- ============================================================================
CREATE OR ALTER PROCEDURE sp_ThemNhanVien
    @HoTen         NVARCHAR(100),
    @NgaySinh      DATE,
    @GioiTinh      NVARCHAR(10),
    @CCCD          VARCHAR(12),
    @DiaChi        NVARCHAR(255),
    @SoDienThoai   VARCHAR(15),
    @Email         VARCHAR(100),
    @NgayVaoLam    DATE,
    @LuongCoBan    DECIMAL(18,2),
    @MaPB          INT,
    @MaCV          INT,
    @TaoTaiKhoan   BIT = 0,
    @TenDangNhap   VARCHAR(50) = NULL,
    @MatKhauSHA256 CHAR(64) = NULL,
    @VaiTro        VARCHAR(30) = 'Employee',
    @NewMaNV       INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- 1. Kiểm tra tồn tại Phòng ban và Chức vụ
        IF NOT EXISTS (SELECT 1 FROM PHONGBAN WHERE MaPB = @MaPB AND TrangThai = N'HOAT_DONG')
        BEGIN
            RAISERROR(N'Phòng ban không tồn tại hoặc đã ngừng hoạt động!', 16, 1);
        END

        IF NOT EXISTS (SELECT 1 FROM CHUCVU WHERE MaCV = @MaCV)
        BEGIN
            RAISERROR(N'Chức vụ không tồn tại!', 16, 1);
        END

        -- 2. Thêm mới nhân viên
        INSERT INTO NHANVIEN (
            HoTen, NgaySinh, GioiTinh, CCCD, DiaChi, SoDienThoai, 
            Email, NgayVaoLam, LuongCoBan, MaPB, MaCV, TrangThai
        )
        VALUES (
            @HoTen, @NgaySinh, @GioiTinh, @CCCD, @DiaChi, @SoDienThoai, 
            @Email, ISNULL(@NgayVaoLam, GETDATE()), @LuongCoBan, @MaPB, @MaCV, N'DANG_LAM_VIEC'
        );

        SET @NewMaNV = SCOPE_IDENTITY();

        -- 3. Tạo tài khoản kèm theo (Transaction tích hợp)
        IF (@TaoTaiKhoan = 1)
        BEGIN
            IF (@TenDangNhap IS NULL OR LTRIM(RTRIM(@TenDangNhap)) = '' OR @MatKhauSHA256 IS NULL)
            BEGIN
                RAISERROR(N'Tên đăng nhập và mật khẩu không được để trống khi cấp tài khoản!', 16, 1);
            END

            IF EXISTS (SELECT 1 FROM TAIKHOAN WHERE TenDangNhap = @TenDangNhap)
            BEGIN
                RAISERROR(N'Tên đăng nhập đã tồn tại trong hệ thống!', 16, 1);
            END

            INSERT INTO TAIKHOAN (MaNV, TenDangNhap, MatKhau, VaiTro, TrangThai, NgayTao)
            VALUES (@NewMaNV, @TenDangNhap, @MatKhauSHA256, @VaiTro, 'HOAT_DONG', GETDATE());
        END

        COMMIT TRANSACTION;
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
-- PHẦN 7: DỮ LIỆU MẪU BAN ĐẦU CHO DANH MỤC (SEED DATA)
-- ============================================================================
IF NOT EXISTS (SELECT 1 FROM PHONGBAN)
BEGIN
    INSERT INTO PHONGBAN (TenPB, SoDienThoai, TrangThai) VALUES
    (N'Ban Giám Đốc', '0283896864', N'HOAT_DONG'),
    (N'Phòng Nhân Sự', '0283896865', N'HOAT_DONG'),
    (N'Phòng Kế Toán', '0283896866', N'HOAT_DONG'),
    (N'Phòng Kỹ Thuật', '0283896867', N'HOAT_DONG');
END
GO

IF NOT EXISTS (SELECT 1 FROM CHUCVU)
BEGIN
    INSERT INTO CHUCVU (TenCV, PhuCapChucVu) VALUES
    (N'Giám Đốc', 5000000),
    (N'Trưởng Phòng', 3000000),
    (N'Phó Phòng', 1500000),
    (N'Chuyên Viên', 500000),
    (N'Nhân Viên', 0);
END
GO
