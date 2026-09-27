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
CREATE OR ALTER TRIGGER trg_ChiTietLuong_KhongSuaKhiDaChot
ON CHITIETBANGLUONG
AFTER UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;

    -- Kiểm tra xem có dòng nào bị sửa/xóa thuộc về bảng lương đã chốt
    -- (Dùng bảng 'deleted' vì cả UPDATE và DELETE đều populate bảng này)
    IF EXISTS (
        SELECT 1
        FROM deleted d
        JOIN BANGLUONG bl ON d.MaBangLuong = bl.MaBangLuong
        WHERE bl.TrangThai = 'DA_CHOT'
    )
    BEGIN
        RAISERROR(
            N'Không được phép sửa hoặc xóa chi tiết bảng lương đã chốt! Vui lòng liên hệ quản trị viên nếu cần điều chỉnh.',
            16, 1
        );
        ROLLBACK TRANSACTION;
        RETURN;
    END
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
CREATE OR ALTER PROCEDURE sp_ChotBangLuong
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

-- GRANT quyền Function
GRANT EXECUTE ON fn_TinhSoNgayCong   TO role_PayrollOfficer;
GRANT EXECUTE ON fn_TinhThucNhan     TO role_PayrollOfficer;

-- DENY quyền quản lý nhân sự
DENY INSERT, UPDATE, DELETE ON NHANVIEN  TO role_PayrollOfficer;
DENY INSERT, UPDATE, DELETE ON PHONGBAN  TO role_PayrollOfficer;
DENY INSERT, UPDATE, DELETE ON CHUCVU    TO role_PayrollOfficer;
DENY SELECT, INSERT, UPDATE, DELETE ON TAIKHOAN TO role_PayrollOfficer;
GO

PRINT N'[TV5] Đã cấp quyền cho role_PayrollOfficer.';
GO

-- ═══════════════════════════════════════════════════════════════════
-- B5: role_Employee — Nhân viên (chỉ xem phiếu lương)
-- ═══════════════════════════════════════════════════════════════════

-- GRANT chỉ xem phiếu lương qua View
GRANT SELECT ON vw_BangLuongChiTiet TO role_Employee;

-- DENY tất cả bảng nghiệp vụ
DENY SELECT, INSERT, UPDATE, DELETE ON NHANVIEN          TO role_Employee;
DENY SELECT, INSERT, UPDATE, DELETE ON PHONGBAN          TO role_Employee;
DENY SELECT, INSERT, UPDATE, DELETE ON CHUCVU            TO role_Employee;
DENY SELECT, INSERT, UPDATE, DELETE ON CHAMCONG          TO role_Employee;
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
USE master;
GO

IF NOT EXISTS (SELECT * FROM sys.server_principals WHERE name = 'login_DBAdmin')
    CREATE LOGIN login_DBAdmin WITH PASSWORD = 'Admin@2026!', CHECK_POLICY = OFF;
IF NOT EXISTS (SELECT * FROM sys.server_principals WHERE name = 'login_HRManager')
    CREATE LOGIN login_HRManager WITH PASSWORD = 'HR@2026!', CHECK_POLICY = OFF;
IF NOT EXISTS (SELECT * FROM sys.server_principals WHERE name = 'login_PayrollOfficer')
    CREATE LOGIN login_PayrollOfficer WITH PASSWORD = 'Payroll@2026!', CHECK_POLICY = OFF;
IF NOT EXISTS (SELECT * FROM sys.server_principals WHERE name = 'login_Employee')
    CREATE LOGIN login_Employee WITH PASSWORD = 'Emp@2026!', CHECK_POLICY = OFF;
GO

PRINT N'[TV5] Đã tạo 4 Login SQL Server.';
GO

-- Tạo User và gán Role (ở database level)
USE QuanLyNhanSuTienLuong;
GO

IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'user_DBAdmin')
    CREATE USER user_DBAdmin FOR LOGIN login_DBAdmin;
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'user_HRManager')
    CREATE USER user_HRManager FOR LOGIN login_HRManager;
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'user_PayrollOfficer')
    CREATE USER user_PayrollOfficer FOR LOGIN login_PayrollOfficer;
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'user_Employee')
    CREATE USER user_Employee FOR LOGIN login_Employee;
GO

-- Gán User vào Role
ALTER ROLE role_DBAdmin        ADD MEMBER user_DBAdmin;
ALTER ROLE role_HRManager      ADD MEMBER user_HRManager;
ALTER ROLE role_PayrollOfficer ADD MEMBER user_PayrollOfficer;
ALTER ROLE role_Employee       ADD MEMBER user_Employee;
GO

PRINT N'[TV5] Đã tạo 4 User và gán vào Role tương ứng.';
GO

-- ============================================================================
-- PHẦN C: DỮ LIỆU MẪU TÀI KHOẢN (để test Login)
-- Mật khẩu mặc định đều là '123456' → SHA-256 hash
-- SHA-256("123456") = "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92"
-- ============================================================================
DECLARE @hashDefault CHAR(64) = '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92';

-- Tài khoản DB_Admin (không gắn nhân viên)
IF NOT EXISTS (SELECT 1 FROM TAIKHOAN WHERE TenDangNhap = 'admin')
BEGIN
    INSERT INTO TAIKHOAN (MaNV, TenDangNhap, MatKhau, VaiTro, TrangThai)
    VALUES (NULL, 'admin', @hashDefault, 'DB_Admin', 'HOAT_DONG');
    PRINT N'[TV5] Đã tạo tài khoản admin (DB_Admin).';
END

-- Tài khoản HR_Manager (gắn nhân viên MaNV=1 nếu tồn tại)
IF NOT EXISTS (SELECT 1 FROM TAIKHOAN WHERE TenDangNhap = 'hr_manager')
BEGIN
    IF EXISTS (SELECT 1 FROM NHANVIEN WHERE MaNV = 1)
        INSERT INTO TAIKHOAN (MaNV, TenDangNhap, MatKhau, VaiTro, TrangThai)
        VALUES (1, 'hr_manager', @hashDefault, 'HR_Manager', 'HOAT_DONG');
    ELSE
        INSERT INTO TAIKHOAN (MaNV, TenDangNhap, MatKhau, VaiTro, TrangThai)
        VALUES (NULL, 'hr_manager', @hashDefault, 'HR_Manager', 'HOAT_DONG');
    PRINT N'[TV5] Đã tạo tài khoản hr_manager (HR_Manager).';
END

-- Tài khoản Payroll_Officer (gắn nhân viên MaNV=2 nếu tồn tại)
IF NOT EXISTS (SELECT 1 FROM TAIKHOAN WHERE TenDangNhap = 'payroll_officer')
BEGIN
    IF EXISTS (SELECT 1 FROM NHANVIEN WHERE MaNV = 2)
        INSERT INTO TAIKHOAN (MaNV, TenDangNhap, MatKhau, VaiTro, TrangThai)
        VALUES (2, 'payroll_officer', @hashDefault, 'Payroll_Officer', 'HOAT_DONG');
    ELSE
        INSERT INTO TAIKHOAN (MaNV, TenDangNhap, MatKhau, VaiTro, TrangThai)
        VALUES (NULL, 'payroll_officer', @hashDefault, 'Payroll_Officer', 'HOAT_DONG');
    PRINT N'[TV5] Đã tạo tài khoản payroll_officer (Payroll_Officer).';
END

-- Tài khoản Employee (gắn nhân viên MaNV=3 nếu tồn tại)
IF NOT EXISTS (SELECT 1 FROM TAIKHOAN WHERE TenDangNhap = 'employee01')
BEGIN
    IF EXISTS (SELECT 1 FROM NHANVIEN WHERE MaNV = 3)
        INSERT INTO TAIKHOAN (MaNV, TenDangNhap, MatKhau, VaiTro, TrangThai)
        VALUES (3, 'employee01', @hashDefault, 'Employee', 'HOAT_DONG');
    ELSE
        INSERT INTO TAIKHOAN (MaNV, TenDangNhap, MatKhau, VaiTro, TrangThai)
        VALUES (NULL, 'employee01', @hashDefault, 'Employee', 'HOAT_DONG');
    PRINT N'[TV5] Đã tạo tài khoản employee01 (Employee).';
END

-- Tài khoản bị khóa (dùng test tình huống tài khoản KHOA)
IF NOT EXISTS (SELECT 1 FROM TAIKHOAN WHERE TenDangNhap = 'locked_user')
BEGIN
    INSERT INTO TAIKHOAN (MaNV, TenDangNhap, MatKhau, VaiTro, TrangThai)
    VALUES (NULL, 'locked_user', @hashDefault, 'Employee', 'KHOA');
    PRINT N'[TV5] Đã tạo tài khoản locked_user (bị khóa, dùng test).';
END
GO

-- ============================================================================
-- HOÀN TẤT
-- ============================================================================
PRINT N'';
PRINT N'============================================================';
PRINT N'[TV5] Script hoàn tất. Các đối tượng đã tạo:';
PRINT N'  • BANGLUONG, CHITIETBANGLUONG (DDL theo TV4, IF NOT EXISTS)';
PRINT N'  • IX_NHANVIEN_MaPB_MaCV (Index)';
PRINT N'  • fn_TinhThucNhan (Function)';
PRINT N'  • vw_BangLuongChiTiet (View)';
PRINT N'  • trg_ChiTietLuong_KhongSuaKhiDaChot (Trigger)';
PRINT N'  • sp_ChotBangLuong (Stored Procedure + UPDLOCK)';
PRINT N'  • 4 Role + 4 Login + 4 User + GRANT/REVOKE/DENY';
PRINT N'  • 5 tài khoản mẫu (mật khẩu: 123456)';
PRINT N'============================================================';
GO
