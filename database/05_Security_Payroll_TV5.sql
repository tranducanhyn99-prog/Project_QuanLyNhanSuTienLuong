SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET NUMERIC_ROUNDABORT OFF;
GO

-- ============================================================================
-- PROJECT: Quản Lý Nhân Sự và Tiền Lương
-- HỌC PHẦN: Hệ Quản Trị Cơ Sở Dữ Liệu (DBMS330284)
-- TÁC GIẢ: Trần Đức Anh (TV5 - MSSV: 24110155)
-- MODULE: Chốt bảng lương, Phân quyền, Bảo mật, Concurrency
-- TUẦN 2: 28/9 – 4/10/2026
-- ============================================================================
-- GHI CHÚ:
--   Script này chạy SAU script của TV1 (01_Module_NhanSu_TV1.sql)
--   và TV3 (03_phucap_khautru_TV3.sql).
--
--   Bảng BANGLUONG + CHITIETBANGLUONG được tạo tạm ở đây theo đúng thiết kế
--   của TV4 (TV4_Payroll_Analysis.md) với IF NOT EXISTS — để TV5 có thể
--   triển khai sp_ChotBangLuong, trigger, view mà không cần chờ TV4 commit.
--   Khi TV4 merge script của mình, IF NOT EXISTS sẽ bỏ qua không xung đột.
-- ============================================================================

USE QuanLyNhanSuTienLuong;
GO

-- ============================================================================
-- PHẦN 0: TẠO BẢNG PHỤ THUỘC (DDL theo thiết kế TV4, tạm tạo nếu chưa có)
-- ============================================================================

-- Bảng BANGLUONG (thiết kế: TV4 – Nguyễn Quang Vinh)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = N'BANGLUONG')
BEGIN
    CREATE TABLE BANGLUONG (
        MaBangLuong   INT           IDENTITY(1,1)  PRIMARY KEY,
        Thang         INT           NOT NULL
            CONSTRAINT CHK_BANGLUONG_Thang CHECK (Thang BETWEEN 1 AND 12),
        Nam           INT           NOT NULL
            CONSTRAINT CHK_BANGLUONG_Nam CHECK (Nam BETWEEN 2020 AND 2100),
        NgayCongChuan INT           NOT NULL        DEFAULT 26
            CONSTRAINT CHK_BANGLUONG_NgayCongChuan CHECK (NgayCongChuan > 0),
        TrangThai     VARCHAR(15)   NOT NULL        DEFAULT 'CHUA_CHOT'
            CONSTRAINT CHK_BANGLUONG_TrangThai CHECK (TrangThai IN ('CHUA_CHOT', 'DA_CHOT')),
        NgayTao       DATETIME      NOT NULL        DEFAULT GETDATE(),
        NgayChot      DATETIME      NULL,

        CONSTRAINT UQ_BANGLUONG_ThangNam UNIQUE (Thang, Nam)
    );

    PRINT N'[TV5] Đã tạo bảng BANGLUONG (theo thiết kế TV4).';
END
GO

-- Bảng CHITIETBANGLUONG (thiết kế: TV4 – Nguyễn Quang Vinh)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = N'CHITIETBANGLUONG')
BEGIN
    CREATE TABLE CHITIETBANGLUONG (
        MaChiTiet     INT           IDENTITY(1,1)  PRIMARY KEY,
        MaBangLuong   INT           NOT NULL,
        MaNV          INT           NOT NULL,
        LuongCoBan    DECIMAL(15,2) NOT NULL
            CONSTRAINT CHK_CTBL_LuongCoBan CHECK (LuongCoBan >= 0),
        NgayCongThucTe INT          NOT NULL        DEFAULT 0
            CONSTRAINT CHK_CTBL_NgayCongThucTe CHECK (NgayCongThucTe >= 0),
        TienCong      DECIMAL(15,2) NOT NULL        DEFAULT 0
            CONSTRAINT CHK_CTBL_TienCong CHECK (TienCong >= 0),
        TongPhuCap    DECIMAL(15,2) NOT NULL        DEFAULT 0
            CONSTRAINT CHK_CTBL_TongPhuCap CHECK (TongPhuCap >= 0),
        TongKhauTru   DECIMAL(15,2) NOT NULL        DEFAULT 0
            CONSTRAINT CHK_CTBL_TongKhauTru CHECK (TongKhauTru >= 0),
        ThucNhan      DECIMAL(15,2) NOT NULL        DEFAULT 0,

        CONSTRAINT FK_CTBL_BANGLUONG
            FOREIGN KEY (MaBangLuong) REFERENCES BANGLUONG(MaBangLuong),
        CONSTRAINT FK_CTBL_NHANVIEN
            FOREIGN KEY (MaNV) REFERENCES NHANVIEN(MaNV),
        CONSTRAINT UQ_CTBL_BangLuong_NhanVien
            UNIQUE (MaBangLuong, MaNV)
    );

    PRINT N'[TV5] Đã tạo bảng CHITIETBANGLUONG (theo thiết kế TV4).';
END
GO

-- ============================================================================
-- PHẦN A5: INDEX THEO PHÂN CÔNG (TV5: IX_NHANVIEN_MaPB_MaCV)
-- Tối ưu truy vấn lọc nhân viên theo phòng ban + chức vụ
-- ============================================================================
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = N'IX_NHANVIEN_MaPB_MaCV' AND object_id = OBJECT_ID(N'NHANVIEN'))
BEGIN
    CREATE NONCLUSTERED INDEX IX_NHANVIEN_MaPB_MaCV
    ON NHANVIEN (MaPB, MaCV)
    INCLUDE (MaNV, HoTen, LuongCoBan, TrangThai);

    PRINT N'[TV5] Đã tạo index IX_NHANVIEN_MaPB_MaCV.';
END
GO

-- ============================================================================
-- PHẦN A2: FUNCTION THEO PHÂN CÔNG (TV5: fn_TinhThucNhan)
-- Công thức: Thực nhận = Tiền công + Tổng phụ cấp – Tổng khấu trừ
-- Ghi chú: Thực nhận CÓ THỂ ÂM nếu khấu trừ lớn hơn thu nhập
-- ============================================================================
CREATE OR ALTER FUNCTION fn_TinhThucNhan
(
    @TienCong    DECIMAL(15,2),
    @TongPhuCap  DECIMAL(15,2),
    @TongKhauTru DECIMAL(15,2)
)
RETURNS DECIMAL(15,2)
AS
BEGIN
    RETURN ISNULL(@TienCong, 0) + ISNULL(@TongPhuCap, 0) - ISNULL(@TongKhauTru, 0);
END;
GO

PRINT N'[TV5] Đã tạo function fn_TinhThucNhan.';
GO

-- ============================================================================
-- PHẦN A4: VIEW THEO PHÂN CÔNG (TV5: vw_BangLuongChiTiet)
-- JOIN BANGLUONG + CHITIETBANGLUONG + NHANVIEN + PHONGBAN + CHUCVU
-- Phục vụ BaoCaoPanel đọc dữ liệu tổng hợp, Employee xem phiếu lương
-- ============================================================================
CREATE OR ALTER VIEW vw_BangLuongChiTiet
AS
SELECT
    bl.MaBangLuong,
    bl.Thang,
    bl.Nam,
    bl.NgayCongChuan,
    bl.TrangThai       AS TrangThaiBangLuong,
    bl.NgayTao         AS NgayTaoBangLuong,
    bl.NgayChot,
    ct.MaChiTiet,
    ct.MaNV,
    nv.HoTen,
    pb.TenPB,
    cv.TenCV,
    ct.LuongCoBan,
    ct.NgayCongThucTe,
    ct.TienCong,
    ct.TongPhuCap,
    ct.TongKhauTru,
    ct.ThucNhan
FROM BANGLUONG bl
JOIN CHITIETBANGLUONG ct ON bl.MaBangLuong = ct.MaBangLuong
JOIN NHANVIEN nv         ON ct.MaNV = nv.MaNV
JOIN PHONGBAN pb         ON nv.MaPB = pb.MaPB
JOIN CHUCVU cv           ON nv.MaCV = cv.MaCV;
GO

PRINT N'[TV5] Đã tạo view vw_BangLuongChiTiet.';
GO

-- ============================================================================
-- PHẦN A3: TRIGGER THEO PHÂN CÔNG (TV5: trg_ChiTietLuong_KhongSuaKhiDaChot)
-- Ngăn UPDATE hoặc DELETE trên CHITIETBANGLUONG khi bảng lương đã chốt
-- ============================================================================
CREATE OR ALTER TRIGGER dbo.trg_ChiTietLuong_KhongSuaKhiDaChot
ON dbo.CHITIETBANGLUONG
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @Closed INT;
    SELECT @Closed = MAX(CASE WHEN bl.TrangThai = 'DA_CHOT' THEN 1 ELSE 0 END)
    FROM dbo.BANGLUONG bl WITH (UPDLOCK, HOLDLOCK)
    JOIN (SELECT MaBangLuong FROM inserted UNION SELECT MaBangLuong FROM deleted) p
      ON bl.MaBangLuong = p.MaBangLuong;
    IF @Closed = 1
    BEGIN
        RAISERROR(N'Không được phép sửa hoặc xóa chi tiết bảng lương đã chốt! Không được thêm hoặc chuyển chi tiết vào kỳ đã chốt.', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END;
END;
GO

PRINT N'[TV5] Đã tạo trigger trg_ChiTietLuong_KhongSuaKhiDaChot.';
GO

-- ============================================================================
-- PHẦN A1: STORED PROCEDURE THEO PHÂN CÔNG (TV5: sp_ChotBangLuong)
-- Chốt bảng lương theo MaBangLuong.
-- Dùng UPDLOCK + HOLDLOCK để ngăn concurrency (2 Payroll_Officer cùng chốt).
-- TRY...CATCH + Transaction đầy đủ.
-- ============================================================================
-- sp_ names can also exist in master; test local object before ALTER.
IF OBJECT_ID(N'dbo.sp_ChotBangLuong',N'P') IS NULL
    EXEC(N'CREATE PROCEDURE dbo.sp_ChotBangLuong AS RETURN;');
GO
ALTER PROCEDURE dbo.sp_ChotBangLuong
    @MaBangLuong INT
AS
BEGIN
    SET NOCOUNT ON;

    -- Biến kiểm tra
    DECLARE @TrangThai VARCHAR(15);
    DECLARE @SoChiTiet INT;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- 1. Khóa dòng bảng lương bằng UPDLOCK + HOLDLOCK
        --    Nếu session khác cũng đang SELECT WITH (UPDLOCK) trên cùng dòng
        --    → session đó sẽ bị BLOCKED cho đến khi transaction này COMMIT/ROLLBACK
        SELECT @TrangThai = TrangThai
        FROM BANGLUONG WITH (UPDLOCK, HOLDLOCK)
        WHERE MaBangLuong = @MaBangLuong;

        -- 2. Kiểm tra bảng lương tồn tại
        IF @TrangThai IS NULL
        BEGIN
            RAISERROR(N'Không tìm thấy bảng lương với mã %d!', 16, 1, @MaBangLuong);
        END

        -- 3. Kiểm tra trạng thái — nếu đã chốt thì từ chối
        IF @TrangThai = 'DA_CHOT'
        BEGIN
            RAISERROR(N'Kỳ lương đã được chốt trước đó. Không thể chốt lại!', 16, 1);
        END

        -- 4. Kiểm tra có chi tiết lương hay chưa (không cho chốt bảng lương trống)
        SELECT @SoChiTiet = COUNT(*)
        FROM CHITIETBANGLUONG
        WHERE MaBangLuong = @MaBangLuong;

        IF @SoChiTiet = 0
        BEGIN
            RAISERROR(N'Bảng lương chưa có chi tiết nào. Vui lòng tính lương trước khi chốt!', 16, 1);
        END

        -- 5. Cập nhật trạng thái sang DA_CHOT
        UPDATE BANGLUONG
        SET TrangThai = 'DA_CHOT',
            NgayChot  = GETDATE()
        WHERE MaBangLuong = @MaBangLuong;

        COMMIT TRANSACTION;

        -- 6. Thông báo thành công
        PRINT N'Đã chốt bảng lương mã ' + CAST(@MaBangLuong AS NVARCHAR(10))
            + N' với ' + CAST(@SoChiTiet AS NVARCHAR(10)) + N' chi tiết.';

    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        -- Ném lại lỗi cho ứng dụng Java bắt
        DECLARE @ErrorMessage NVARCHAR(4000) = ERROR_MESSAGE();
        DECLARE @ErrorSeverity INT = ERROR_SEVERITY();
        DECLARE @ErrorState INT = ERROR_STATE();
        RAISERROR(@ErrorMessage, @ErrorSeverity, @ErrorState);
    END CATCH
END;
GO

PRINT N'[TV5] Đã tạo stored procedure sp_ChotBangLuong (UPDLOCK + TRY...CATCH).';
GO

-- ============================================================================
-- PHẦN B: PHÂN QUYỀN — TẠO LOGIN / USER / ROLE / GRANT / REVOKE / DENY
-- ============================================================================

-- ═══════════════════════════════════════════════════════════════════
-- B1: TẠO 4 DATABASE ROLE (trong database ứng dụng)
-- ═══════════════════════════════════════════════════════════════════
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'role_DBAdmin' AND type = 'R')
    CREATE ROLE role_DBAdmin;
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'role_HRManager' AND type = 'R')
    CREATE ROLE role_HRManager;
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'role_PayrollOfficer' AND type = 'R')
    CREATE ROLE role_PayrollOfficer;
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'role_Employee' AND type = 'R')
    CREATE ROLE role_Employee;
GO

PRINT N'[TV5] Đã tạo 4 Database Role.';
GO

-- ═══════════════════════════════════════════════════════════════════
-- B2: role_DBAdmin — toàn quyền (thêm vào db_owner)
-- ═══════════════════════════════════════════════════════════════════
IF NOT EXISTS (
    SELECT 1 FROM sys.database_role_members rm
    JOIN sys.database_principals r ON rm.role_principal_id = r.principal_id
    JOIN sys.database_principals m ON rm.member_principal_id = m.principal_id
    WHERE r.name = 'db_owner' AND m.name = 'role_DBAdmin'
)
BEGIN
    ALTER ROLE db_owner ADD MEMBER role_DBAdmin;
END
GO

-- ═══════════════════════════════════════════════════════════════════
-- B3: role_HRManager — Quản lý nhân sự
-- ═══════════════════════════════════════════════════════════════════

-- GRANT quyền đọc/ghi nhân sự
GRANT SELECT, INSERT, UPDATE ON PHONGBAN          TO role_HRManager;
GRANT SELECT, INSERT, UPDATE ON CHUCVU            TO role_HRManager;
GRANT SELECT, INSERT, UPDATE ON NHANVIEN          TO role_HRManager;
GRANT SELECT                 ON BANGLUONG          TO role_HRManager;
GRANT SELECT                 ON CHITIETBANGLUONG   TO role_HRManager;

-- GRANT quyền bảng phụ cấp/khấu trừ (HR cũng có thể nhập)
GRANT SELECT, INSERT, UPDATE ON PHUCAPNHANVIEN    TO role_HRManager;
GRANT SELECT, INSERT, UPDATE ON KHAUTRUNHANVIEN   TO role_HRManager;

-- GRANT quyền gọi SP nhân sự
GRANT EXECUTE ON sp_ThemNhanVien     TO role_HRManager;

-- GRANT quyền đọc View
GRANT SELECT ON vw_NhanVien_PhongBan_ChucVu  TO role_HRManager;
GRANT SELECT ON vw_BangLuongChiTiet          TO role_HRManager;

-- GRANT quyền gọi Function
GRANT EXECUTE ON fn_TinhSoNgayCong   TO role_HRManager;

-- GRANT quyền module Chấm công (TV2)
GRANT SELECT, INSERT, UPDATE, DELETE ON CHAMCONG TO role_HRManager;
GRANT EXECUTE ON sp_GhiNhanChamCong      TO role_HRManager;
GRANT SELECT ON vw_TongHopChamCongThang  TO role_HRManager;

-- DENY quyền tính/chốt lương
DENY EXECUTE ON sp_ChotBangLuong     TO role_HRManager;

-- DENY quyền truy cập TAIKHOAN
DENY SELECT, INSERT, UPDATE, DELETE ON TAIKHOAN TO role_HRManager;
GO

PRINT N'[TV5] Đã cấp quyền cho role_HRManager.';
GO

-- ═══════════════════════════════════════════════════════════════════
-- B4: role_PayrollOfficer — Nhân viên kế toán lương
-- ═══════════════════════════════════════════════════════════════════

-- GRANT quyền đọc dữ liệu nguồn
GRANT SELECT ON NHANVIEN             TO role_PayrollOfficer;
GRANT SELECT, INSERT, UPDATE ON PHUCAPNHANVIEN   TO role_PayrollOfficer;
GRANT SELECT, INSERT, UPDATE ON KHAUTRUNHANVIEN  TO role_PayrollOfficer;
GRANT SELECT, INSERT         ON BANGLUONG         TO role_PayrollOfficer;
GRANT SELECT, INSERT         ON CHITIETBANGLUONG  TO role_PayrollOfficer;
GRANT SELECT ON PHONGBAN             TO role_PayrollOfficer;
GRANT SELECT ON CHUCVU               TO role_PayrollOfficer;

-- GRANT quyền gọi SP lương
GRANT EXECUTE ON sp_ChotBangLuong    TO role_PayrollOfficer;

-- GRANT quyền View
GRANT SELECT ON vw_NhanVien_PhongBan_ChucVu  TO role_PayrollOfficer;
GRANT SELECT ON vw_BangLuongChiTiet          TO role_PayrollOfficer;
GRANT SELECT ON vw_TongHopChamCongThang      TO role_PayrollOfficer;

-- GRANT quyền Function
GRANT EXECUTE ON fn_TinhSoNgayCong   TO role_PayrollOfficer;
GRANT EXECUTE ON fn_TinhThucNhan     TO role_PayrollOfficer;

-- GRANT quyền đọc chấm công để đối soát lương
GRANT SELECT ON CHAMCONG             TO role_PayrollOfficer;

-- DENY quyền quản lý nhân sự & ghi nhận chấm công
DENY INSERT, UPDATE, DELETE ON NHANVIEN  TO role_PayrollOfficer;
DENY INSERT, UPDATE, DELETE ON PHONGBAN  TO role_PayrollOfficer;
DENY INSERT, UPDATE, DELETE ON CHUCVU    TO role_PayrollOfficer;
DENY INSERT, UPDATE, DELETE ON CHAMCONG  TO role_PayrollOfficer;
DENY EXECUTE ON sp_GhiNhanChamCong       TO role_PayrollOfficer;
DENY SELECT, INSERT, UPDATE, DELETE ON TAIKHOAN TO role_PayrollOfficer;
GO

PRINT N'[TV5] Đã cấp quyền cho role_PayrollOfficer.';
GO

-- ═══════════════════════════════════════════════════════════════════
-- B5: role_Employee — Nhân viên (chỉ xem phiếu lương)
-- ═══════════════════════════════════════════════════════════════════

-- GRANT chỉ xem phiếu lương qua View
REVOKE SELECT ON vw_BangLuongChiTiet FROM role_Employee;

-- DENY tất cả bảng nghiệp vụ
DENY SELECT, INSERT, UPDATE, DELETE ON NHANVIEN          TO role_Employee;
DENY SELECT, INSERT, UPDATE, DELETE ON PHONGBAN          TO role_Employee;
DENY SELECT, INSERT, UPDATE, DELETE ON CHUCVU            TO role_Employee;
DENY SELECT, INSERT, UPDATE, DELETE ON CHAMCONG          TO role_Employee;
DENY EXECUTE ON sp_GhiNhanChamCong                      TO role_Employee;
DENY SELECT ON vw_TongHopChamCongThang                  TO role_Employee;
DENY SELECT, INSERT, UPDATE, DELETE ON PHUCAPNHANVIEN    TO role_Employee;
DENY SELECT, INSERT, UPDATE, DELETE ON KHAUTRUNHANVIEN   TO role_Employee;
DENY SELECT, INSERT, UPDATE, DELETE ON BANGLUONG         TO role_Employee;
DENY SELECT, INSERT, UPDATE, DELETE ON CHITIETBANGLUONG  TO role_Employee;
DENY SELECT, INSERT, UPDATE, DELETE ON TAIKHOAN          TO role_Employee;
GO

PRINT N'[TV5] Đã cấp quyền cho role_Employee.';
GO

-- ═══════════════════════════════════════════════════════════════════
-- B6: TẠO LOGIN + USER (chạy riêng nếu cần, vì CREATE LOGIN ở master)
-- Ghi chú: Phần CREATE LOGIN cần chạy ở context 'master'.
--          Tách ra block riêng để có thể chạy từng phần.
-- ═══════════════════════════════════════════════════════════════════

-- Tạo Login (ở server level)
-- Demo identities/data are optional; see 06_Demo_Data.sql.

GRANT DELETE ON dbo.PHONGBAN TO role_HRManager;
GRANT DELETE ON dbo.CHUCVU TO role_HRManager;
GRANT DELETE ON dbo.NHANVIEN TO role_HRManager;
GRANT DELETE ON dbo.PHUCAPNHANVIEN TO role_HRManager;
GRANT DELETE ON dbo.KHAUTRUNHANVIEN TO role_HRManager;
GRANT DELETE ON dbo.PHUCAPNHANVIEN TO role_PayrollOfficer;
GRANT DELETE ON dbo.KHAUTRUNHANVIEN TO role_PayrollOfficer;
GRANT EXECUTE ON dbo.sp_TinhBangLuongThang TO role_PayrollOfficer;
GRANT EXECUTE ON dbo.sp_HuyChotBangLuong TO role_PayrollOfficer;
GRANT EXECUTE ON dbo.sp_XoaBangLuongChuaChot TO role_PayrollOfficer;
GRANT EXECUTE ON dbo.sp_XoaKyLuongChuaChot TO role_PayrollOfficer;
GRANT EXECUTE ON dbo.fn_TinhTienCong TO role_PayrollOfficer;
GRANT EXECUTE ON dbo.fn_TongPhuCap TO role_PayrollOfficer;
GRANT EXECUTE ON dbo.fn_TongKhauTru TO role_PayrollOfficer;
GRANT EXECUTE ON dbo.fn_TongKhauTru TO role_HRManager;
GO

CREATE OR ALTER TRIGGER dbo.trg_PhuCap_KhongSuaKhiDaChotLuong
ON dbo.PHUCAPNHANVIEN
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @Closed INT;
    SELECT @Closed = MAX(CASE WHEN bl.TrangThai = 'DA_CHOT' THEN 1 ELSE 0 END)
    FROM dbo.BANGLUONG bl WITH (UPDLOCK, HOLDLOCK)
    JOIN (SELECT Thang, Nam FROM inserted UNION SELECT Thang, Nam FROM deleted) p ON bl.Thang = p.Thang AND bl.Nam = p.Nam;
    IF @Closed = 1
    BEGIN
        RAISERROR(N'Kỳ lương đã chốt: không được thay đổi phụ cấp.', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END;
END;
GO

CREATE OR ALTER TRIGGER dbo.trg_KhauTru_KhongSuaKhiDaChotLuong
ON dbo.KHAUTRUNHANVIEN
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @Closed INT;
    SELECT @Closed = MAX(CASE WHEN bl.TrangThai = 'DA_CHOT' THEN 1 ELSE 0 END)
    FROM dbo.BANGLUONG bl WITH (UPDLOCK, HOLDLOCK)
    JOIN (SELECT Thang, Nam FROM inserted UNION SELECT Thang, Nam FROM deleted) p ON bl.Thang = p.Thang AND bl.Nam = p.Nam;
    IF @Closed = 1
    BEGIN
        RAISERROR(N'Kỳ lương đã chốt: không được thay đổi khấu trừ.', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END;
END;
GO

CREATE OR ALTER TRIGGER dbo.trg_ChamCong_KhongSuaKhiDaChotLuong
ON dbo.CHAMCONG
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @Closed INT;
    SELECT @Closed = MAX(CASE WHEN bl.TrangThai = 'DA_CHOT' THEN 1 ELSE 0 END)
    FROM dbo.BANGLUONG bl WITH (UPDLOCK, HOLDLOCK)
    JOIN (SELECT MONTH(NgayChamCong) AS Thang, YEAR(NgayChamCong) AS Nam FROM inserted UNION SELECT MONTH(NgayChamCong), YEAR(NgayChamCong) FROM deleted) p ON bl.Thang = p.Thang AND bl.Nam = p.Nam;
    IF @Closed = 1
    BEGIN
        RAISERROR(N'Kỳ lương đã chốt: không được thay đổi chấm công.', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END;
END;
GO

ALTER TABLE dbo.TAIKHOAN ALTER COLUMN MatKhau VARCHAR(255) NOT NULL;
GO

-- Trusted identity: provision SqlLogin explicitly; never take MaNV/role from client context.
IF COL_LENGTH('dbo.TAIKHOAN', 'SqlLogin') IS NULL
    ALTER TABLE dbo.TAIKHOAN ADD SqlLogin SYSNAME NULL;
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.TAIKHOAN') AND name='UQ_TAIKHOAN_SqlLogin')
    CREATE UNIQUE INDEX UQ_TAIKHOAN_SqlLogin ON dbo.TAIKHOAN(SqlLogin) WHERE SqlLogin IS NOT NULL;
GO
CREATE OR ALTER PROCEDURE dbo.sp_LayTaiKhoanHienTai
AS
BEGIN
    SET NOCOUNT ON;
    SELECT tk.MaTK,tk.MaNV,tk.TenDangNhap,tk.MatKhau,tk.VaiTro,tk.TrangThai,
           tk.NgayTao,tk.NgaySuaCuoi,nv.HoTen AS HoTenNV
    FROM dbo.TAIKHOAN tk LEFT JOIN dbo.NHANVIEN nv ON nv.MaNV=tk.MaNV
    WHERE tk.SqlLogin=ORIGINAL_LOGIN()
      AND ((tk.VaiTro='DB_Admin' AND IS_ROLEMEMBER('role_DBAdmin')=1)
        OR (tk.VaiTro='HR_Manager' AND IS_ROLEMEMBER('role_HRManager')=1)
        OR (tk.VaiTro='Payroll_Officer' AND IS_ROLEMEMBER('role_PayrollOfficer')=1)
        OR (tk.VaiTro='Employee' AND IS_ROLEMEMBER('role_Employee')=1));
END;
GO
CREATE OR ALTER PROCEDURE dbo.sp_MigrateMatKhau @Hash VARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;
    IF @Hash NOT LIKE 'pbkdf2-sha256$%' THROW 51020,N'Invalid password hash format.',1;
    UPDATE dbo.TAIKHOAN SET MatKhau=@Hash,NgaySuaCuoi=GETDATE()
    WHERE SqlLogin=ORIGINAL_LOGIN() AND TrangThai='HOAT_DONG';
    IF @@ROWCOUNT<>1 THROW 51021,N'Identity is not active/mapped.',1;
END;
GO
CREATE OR ALTER VIEW dbo.vw_PhieuLuongCaNhan
AS
SELECT bl.*
FROM dbo.vw_BangLuongChiTiet bl
JOIN dbo.TAIKHOAN tk ON tk.MaNV=bl.MaNV
WHERE tk.SqlLogin=ORIGINAL_LOGIN() AND tk.TrangThai='HOAT_DONG';
GO
GRANT EXECUTE ON dbo.sp_LayTaiKhoanHienTai TO role_DBAdmin,role_HRManager,role_PayrollOfficer,role_Employee;
GRANT EXECUTE ON dbo.sp_MigrateMatKhau TO role_DBAdmin,role_HRManager,role_PayrollOfficer,role_Employee;
GRANT SELECT ON dbo.vw_PhieuLuongCaNhan TO role_Employee;
GO
CREATE OR ALTER PROCEDURE dbo.sp_AdminResetPassword
    @MaTK INT, @Hash VARCHAR(255), @Password NVARCHAR(MAX)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    IF ISNULL(IS_ROLEMEMBER('role_DBAdmin'),0)<>1
        THROW 51022,N'Admin role required.',1;
    IF @Hash IS NULL OR @Hash NOT LIKE 'pbkdf2-sha256$%'
       OR @Password IS NULL OR DATALENGTH(@Password) NOT BETWEEN 16 AND 256
        THROW 51023,N'Password must contain 8-128 characters and a versioned hash.',1;
    DECLARE @Login SYSNAME, @Sql NVARCHAR(MAX);
    BEGIN TRY
        BEGIN TRANSACTION;
        SELECT @Login=SqlLogin FROM dbo.TAIKHOAN WITH(UPDLOCK,HOLDLOCK) WHERE MaTK=@MaTK;
        IF @Login IS NULL THROW 51024,N'DBA must provision/map the SQL login first.',1;
        SET @Sql=N'ALTER LOGIN '+QUOTENAME(@Login)+N' WITH PASSWORD = N'+QUOTENAME(@Password,CHAR(39))+N';';
        EXEC sys.sp_executesql @Sql;
        UPDATE dbo.TAIKHOAN SET MatKhau=@Hash,NgaySuaCuoi=GETDATE() WHERE MaTK=@MaTK;
        COMMIT;
    END TRY
    BEGIN CATCH
        IF XACT_STATE()<>0 ROLLBACK;
        THROW;
    END CATCH;
END;
GO
CREATE OR ALTER PROCEDURE dbo.sp_AdminSetRole @MaTK INT, @VaiTro VARCHAR(30)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    IF ISNULL(IS_ROLEMEMBER('role_DBAdmin'),0)<>1 THROW 51022,N'Admin role required.',1;
    DECLARE @Target SYSNAME=CASE @VaiTro WHEN 'DB_Admin' THEN 'role_DBAdmin'
        WHEN 'HR_Manager' THEN 'role_HRManager' WHEN 'Payroll_Officer' THEN 'role_PayrollOfficer'
        WHEN 'Employee' THEN 'role_Employee' END;
    IF @Target IS NULL THROW 51025,N'Invalid role.',1;
    DECLARE @Login SYSNAME,@User SYSNAME,@Sql NVARCHAR(MAX)=N'';
    BEGIN TRY
        BEGIN TRANSACTION;
        SELECT @Login=SqlLogin FROM dbo.TAIKHOAN WITH(UPDLOCK,HOLDLOCK) WHERE MaTK=@MaTK;
        IF @Login IS NULL THROW 51024,N'DBA must provision/map the SQL login first.',1;
        IF @Login=ORIGINAL_LOGIN() THROW 51026,N'Cannot change your own SQL role in the current session.',1;
        SELECT @User=name FROM sys.database_principals WHERE sid=SUSER_SID(@Login) AND type='S';
        IF @User IS NULL THROW 51027,N'SQL user mapping not found.',1;
        SELECT @Sql=@Sql+N'ALTER ROLE '+QUOTENAME(r.name)+N' DROP MEMBER '+QUOTENAME(@User)+N';'
        FROM sys.database_role_members m JOIN sys.database_principals r ON r.principal_id=m.role_principal_id
        WHERE m.member_principal_id=USER_ID(@User)
          AND r.name IN ('role_DBAdmin','role_HRManager','role_PayrollOfficer','role_Employee');
        SET @Sql=@Sql+N'ALTER ROLE '+QUOTENAME(@Target)+N' ADD MEMBER '+QUOTENAME(@User)+N';';
        EXEC sys.sp_executesql @Sql;
        UPDATE dbo.TAIKHOAN SET VaiTro=@VaiTro,NgaySuaCuoi=GETDATE() WHERE MaTK=@MaTK;
        COMMIT;
    END TRY
    BEGIN CATCH
        IF XACT_STATE()<>0 ROLLBACK;
        THROW;
    END CATCH;
END;
GO
GRANT EXECUTE ON dbo.sp_AdminResetPassword TO role_DBAdmin;
GRANT EXECUTE ON dbo.sp_AdminSetRole TO role_DBAdmin;
GO

CREATE OR ALTER PROCEDURE dbo.sp_AdminSetStatus @MaTK INT, @TrangThai VARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    IF ISNULL(IS_ROLEMEMBER('role_DBAdmin'),0)<>1 THROW 51022,N'Admin role required.',1;
    IF @TrangThai NOT IN ('KHOA','HOAT_DONG') OR @TrangThai IS NULL THROW 51028,N'Invalid status.',1;
    DECLARE @Login SYSNAME,@Sql NVARCHAR(MAX);
    BEGIN TRY
        BEGIN TRANSACTION;
        SELECT @Login=SqlLogin FROM dbo.TAIKHOAN WITH(UPDLOCK,HOLDLOCK) WHERE MaTK=@MaTK;
        IF @Login IS NULL THROW 51024,N'DBA must provision/map the SQL login first.',1;
        IF @Login=ORIGINAL_LOGIN() THROW 51026,N'Cannot disable your own current login.',1;
        SET @Sql=N'ALTER LOGIN '+QUOTENAME(@Login)+CASE WHEN @TrangThai='KHOA' THEN N' DISABLE;' ELSE N' ENABLE;' END;
        EXEC sys.sp_executesql @Sql;
        UPDATE dbo.TAIKHOAN SET TrangThai=@TrangThai,NgaySuaCuoi=GETDATE() WHERE MaTK=@MaTK;
        COMMIT;
    END TRY
    BEGIN CATCH
        IF XACT_STATE()<>0 ROLLBACK;
        THROW;
    END CATCH;
END;
GO
GRANT EXECUTE ON dbo.sp_AdminSetStatus TO role_DBAdmin;
GO

-- Explicit DBA operation. Schema installation never provisions passwords/logins.
CREATE OR ALTER PROCEDURE dbo.sp_DBAProvisionIdentity
    @Login NVARCHAR(MAX),@Password NVARCHAR(MAX),@Hash VARCHAR(255),@VaiTro VARCHAR(30),@MaNV INT=NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    IF ISNULL(IS_SRVROLEMEMBER('sysadmin'),0)<>1 THROW 51030,N'Provisioning requires a server DBA.',1;
    DECLARE @Role SYSNAME=CASE @VaiTro WHEN 'DB_Admin' THEN 'role_DBAdmin'
        WHEN 'HR_Manager' THEN 'role_HRManager' WHEN 'Payroll_Officer' THEN 'role_PayrollOfficer'
        WHEN 'Employee' THEN 'role_Employee' END;
    IF @Role IS NULL OR @Login IS NULL OR LEN(@Login) NOT BETWEEN 1 AND 50
       OR @Login COLLATE Latin1_General_100_BIN2 LIKE N'%[^a-zA-Z0-9_.-]%'
       OR @Hash IS NULL OR @Hash NOT LIKE 'pbkdf2-sha256$%'
       OR @Password IS NULL OR DATALENGTH(@Password) NOT BETWEEN 16 AND 256
        THROW 51031,N'Invalid provisioning input.',1;
    IF @VaiTro='Employee' AND NOT EXISTS(SELECT 1 FROM dbo.NHANVIEN WHERE MaNV=@MaNV)
        THROW 51032,N'Employee must be linked to an existing employee record.',1;
    DECLARE @Sql NVARCHAR(MAX);
    BEGIN TRY
        BEGIN TRANSACTION;
        IF SUSER_ID(@Login) IS NOT NULL OR USER_ID(@Login) IS NOT NULL
            THROW 51033,N'Login/user already exists: use the reviewed migration procedure instead.',1;
        IF EXISTS(SELECT 1 FROM dbo.TAIKHOAN WITH(UPDLOCK,HOLDLOCK) WHERE TenDangNhap=@Login AND SqlLogin IS NOT NULL)
            THROW 51034,N'Application account is already mapped.',1;
        SET @Sql=N'CREATE LOGIN '+QUOTENAME(@Login)+N' WITH PASSWORD=N'+QUOTENAME(@Password,CHAR(39))
            +N', CHECK_POLICY=ON, CHECK_EXPIRATION=OFF; CREATE USER '+QUOTENAME(@Login)
            +N' FOR LOGIN '+QUOTENAME(@Login)+N'; ALTER ROLE '+QUOTENAME(@Role)+N' ADD MEMBER '+QUOTENAME(@Login)+N';';
        EXEC sys.sp_executesql @Sql;
        UPDATE dbo.TAIKHOAN SET MaNV=@MaNV,MatKhau=@Hash,VaiTro=@VaiTro,TrangThai='HOAT_DONG',SqlLogin=@Login,NgaySuaCuoi=GETDATE()
        WHERE TenDangNhap=@Login;
        IF @@ROWCOUNT=0
            INSERT dbo.TAIKHOAN(MaNV,TenDangNhap,MatKhau,VaiTro,TrangThai,SqlLogin)
            VALUES(@MaNV,@Login,@Hash,@VaiTro,'HOAT_DONG',@Login);
        COMMIT;
    END TRY
    BEGIN CATCH
        IF XACT_STATE()<>0 ROLLBACK;
        THROW;
    END CATCH;
END;
GO

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
        DELETE FROM dbo.LICHSULUONG WHERE MaNV IN (SELECT MaNV FROM deleted);
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
