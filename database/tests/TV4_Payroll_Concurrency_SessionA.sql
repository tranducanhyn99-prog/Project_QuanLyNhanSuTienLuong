-- ============================================================================
-- TV4 - PAYROLL CONCURRENCY DEMO, SESSION A
-- Tac gia : Nguyen Quang Vinh (TV4 - MSSV 24110385)
--
-- Muc tieu: hai session goi dbo.sp_TinhBangLuongThang cho cung ky 01/2020.
-- Session A giu transaction ngoai trong 20 giay. Session B se doi range/row
-- lock do UPDLOCK + HOLDLOCK cua procedure, sau do tinh lai cung mot header.
-- UQ_BANGLUONG_ThangNam va UQ_CTBL_BangLuong_NhanVien khong bi duplicate.
--
-- THU TU CHAY:
--   1. Mo hai query window SSMS tren cung database.
--   2. Chay TOAN BO file SessionA nay.
--   3. Khi Messages hien "SESSION A DA GIU LOCK", chay TOAN BO file SessionB.
--   4. Session B bi BLOCKED den khi A COMMIT; A doi B xong, kiem chung va cleanup.
--
-- Script dung global-temp table chi de bat tay hai session. Bang tam va tat ca
-- fixture/per-period payroll do demo tao deu duoc xoa o nhanh thanh cong/CATCH.
-- ============================================================================

SET NOCOUNT ON;
SET XACT_ABORT ON;
SET LOCK_TIMEOUT 90000;

DECLARE @Thang              INT            = 1;
DECLARE @Nam                INT            = 2020;
DECLARE @NgayCongChuan      INT            = 26;
DECLARE @Marker             NVARCHAR(255)  = N'TV4_PAYROLL_CONCURRENCY_24110385';
DECLARE @MaPBFixture        INT            = -2100000100;
DECLARE @MaCVFixture        INT            = -2100000100;
DECLARE @MaNVFixture        INT            = -2100000100;
DECLARE @NgayFixture        DATE           = '2020-01-02';
DECLARE @MaChamCongFixture  INT;
DECLARE @MaBangLuongA       INT;
DECLARE @MaBangLuongB       INT;
DECLARE @AppLockResource    NVARCHAR(255)  = N'TV4.Payroll.Concurrency.01.2020';
DECLARE @AppLockResult      INT;
DECLARE @NgayTaoA           DATETIME;
DECLARE @NgayTaoSau         DATETIME;
DECLARE @SessionBStatus     VARCHAR(20);
DECLARE @SessionBElapsedMs  INT;
DECLARE @Deadline           DATETIME2(0);
DECLARE @HeaderCount        INT;
DECLARE @DetailCount        INT;
DECLARE @DistinctEmployee   INT;
DECLARE @DuplicateGroups    INT;
DECLARE @FixtureOwned       BIT = 0;
DECLARE @PhongBanOwned      BIT = 0;
DECLARE @ChucVuOwned        BIT = 0;
DECLARE @NhanVienOwned      BIT = 0;
DECLARE @OwnsPayrollPeriod  BIT = 0;
DECLARE @IdentityInsertTable SYSNAME = NULL;

IF @@TRANCOUNT <> 0
    THROW 51100, N'Session A phai bat dau khi @@TRANCOUNT = 0.', 1;

IF OBJECT_ID(N'dbo.PHONGBAN', N'U') IS NULL
   OR OBJECT_ID(N'dbo.CHUCVU', N'U') IS NULL
   OR OBJECT_ID(N'dbo.NHANVIEN', N'U') IS NULL
   OR OBJECT_ID(N'dbo.CHAMCONG', N'U') IS NULL
   OR OBJECT_ID(N'dbo.BANGLUONG', N'U') IS NULL
   OR OBJECT_ID(N'dbo.CHITIETBANGLUONG', N'U') IS NULL
   OR OBJECT_ID(N'dbo.sp_TinhBangLuongThang', N'P') IS NULL
    THROW 51101, N'Thieu object payroll/attendance. Hay chay cac script module 01 -> 04 truoc.', 1;

-- Chi mot bo demo duoc phep chay tai mot thoi diem.
IF OBJECT_ID(N'tempdb..##TV4_Payroll_Concurrency_State', N'U') IS NOT NULL
    THROW 51102, N'Dang co mot concurrency demo khac. Dong/cancel demo cu truoc khi chay lai.', 1;

CREATE TABLE ##TV4_Payroll_Concurrency_State
(
    DemoId           TINYINT       NOT NULL PRIMARY KEY,
    DatabaseName     SYSNAME       NOT NULL,
    Thang            INT           NOT NULL,
    Nam              INT           NOT NULL,
    SessionAId       INT           NOT NULL,
    SessionBId       INT           NULL,
    SessionAStatus   VARCHAR(20)    NOT NULL,
    SessionBStatus   VARCHAR(20)    NOT NULL,
    MaBangLuongA     INT           NULL,
    MaBangLuongB     INT           NULL,
    SessionBElapsedMs INT          NULL,
    SessionBError    NVARCHAR(2048) NULL
);

INSERT INTO ##TV4_Payroll_Concurrency_State
(
    DemoId, DatabaseName, Thang, Nam, SessionAId, SessionAStatus, SessionBStatus
)
VALUES
(
    1, DB_NAME(), @Thang, @Nam, @@SPID, 'PREPARING', 'NOT_STARTED'
);

BEGIN TRY
    -- Khong tu dong xoa marker stale: script chi cleanup row do chinh lan chay
    -- hien tai tao va nhan dien bang MaChamCong lay tu SCOPE_IDENTITY().
    IF EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE GhiChu = @Marker)
        THROW 51115, N'Con fixture concurrency cu. Hay kiem tra/xoa thu cong; script se khong xoa du lieu khong ro quyen so huu.', 1;

    -- Ky demo la tai nguyen rieng. Gap du lieu san co thi dung, khong ghi de/xoa.
    IF EXISTS
    (
        SELECT 1
        FROM dbo.BANGLUONG
        WHERE Thang = @Thang AND Nam = @Nam
    )
        THROW 51103, N'Ky 01/2020 da co bang luong. Hay doi @Thang/@Nam dong bo o ca hai file; script khong xoa du lieu san co.', 1;

    -- Fixture am rieng giup demo khong phu thuoc database da seed nhan vien.
    IF EXISTS (SELECT 1 FROM dbo.PHONGBAN WHERE MaPB = @MaPBFixture)
       OR EXISTS (SELECT 1 FROM dbo.CHUCVU WHERE MaCV = @MaCVFixture)
       OR EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE MaNV = @MaNVFixture)
       OR EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE CCCD = '999999999995')
       OR EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE SoDienThoai = '0999999995')
       OR EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE Email = 'tv4.payroll.concurrency@example.invalid')
        THROW 51104, N'Khoa/thuoc tinh fixture concurrency dang duoc su dung; script dung de bao ve du lieu san co.', 1;

    BEGIN TRANSACTION;

    SET IDENTITY_INSERT dbo.PHONGBAN ON;
    SET @IdentityInsertTable = N'dbo.PHONGBAN';
    INSERT INTO dbo.PHONGBAN (MaPB, TenPB, SoDienThoai, TrangThai)
    VALUES (@MaPBFixture, N'TV4 payroll concurrency fixture', NULL, N'HOAT_DONG');
    SET @PhongBanOwned = 1;
    SET IDENTITY_INSERT dbo.PHONGBAN OFF;
    SET @IdentityInsertTable = NULL;

    SET IDENTITY_INSERT dbo.CHUCVU ON;
    SET @IdentityInsertTable = N'dbo.CHUCVU';
    INSERT INTO dbo.CHUCVU (MaCV, TenCV, PhuCapChucVu)
    VALUES (@MaCVFixture, N'TV4 payroll concurrency fixture', 0);
    SET @ChucVuOwned = 1;
    SET IDENTITY_INSERT dbo.CHUCVU OFF;
    SET @IdentityInsertTable = NULL;

    SET IDENTITY_INSERT dbo.NHANVIEN ON;
    SET @IdentityInsertTable = N'dbo.NHANVIEN';
    INSERT INTO dbo.NHANVIEN
    (
        MaNV, HoTen, NgaySinh, GioiTinh, CCCD, DiaChi, SoDienThoai,
        Email, NgayVaoLam, LuongCoBan, MaPB, MaCV, TrangThai
    )
    VALUES
    (
        @MaNVFixture, N'TV4 payroll concurrency fixture', '1980-01-01', N'Nam',
        '999999999995', N'Fixture se cleanup', '0999999995',
        'tv4.payroll.concurrency@example.invalid', '2020-01-01', 10000000,
        @MaPBFixture, @MaCVFixture, N'DANG_LAM_VIEC'
    );
    SET @NhanVienOwned = 1;
    SET IDENTITY_INSERT dbo.NHANVIEN OFF;
    SET @IdentityInsertTable = NULL;

    INSERT INTO dbo.CHAMCONG
    (
        MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu
    )
    VALUES
    (
        @MaNVFixture, @NgayFixture, '08:00', '17:00', N'CO_MAT', @Marker
    );

    SET @MaChamCongFixture = CONVERT(INT, SCOPE_IDENTITY());
    SET @FixtureOwned = 1;

    COMMIT TRANSACTION;

    UPDATE ##TV4_Payroll_Concurrency_State
    SET SessionAStatus = 'READY'
    WHERE DemoId = 1;

    RAISERROR(N'[A] Fixture san sang. Session A bat dau tinh luong...', 10, 1) WITH NOWAIT;

    -- Application lock chi dong vai tro bat tay de B biet A da vao transaction;
    -- no khong khoa bang payroll. Lock du lieu ma B gap phai do chinh
    -- sp_TinhBangLuongThang tao va outer transaction cua A giu lai.
    BEGIN TRANSACTION;

    EXEC @AppLockResult = sys.sp_getapplock
        @Resource = @AppLockResource,
        @LockMode = 'Exclusive',
        @LockOwner = 'Transaction',
        @LockTimeout = 0,
        @DbPrincipal = 'public';

    IF @AppLockResult < 0
        THROW 51121, N'Khong claim duoc application lock cua concurrency demo.', 1;

    EXEC dbo.sp_TinhBangLuongThang
        @Thang = @Thang,
        @Nam = @Nam,
        @NgayCongChuan = @NgayCongChuan,
        @MaBangLuong = @MaBangLuongA OUTPUT;

    SET @OwnsPayrollPeriod = 1;

    IF @MaBangLuongA IS NULL
        THROW 51117, N'Procedure khong tra MaBangLuong sau khi script da claim ky.', 1;

    SELECT @NgayTaoA = NgayTao
    FROM dbo.BANGLUONG
    WHERE MaBangLuong = @MaBangLuongA;

    RAISERROR(N'[A] SESSION A DA GIU LOCK trong 20 giay. CHAY SESSION B NGAY BAY GIO.', 10, 1) WITH NOWAIT;
    RAISERROR(N'[A] SPID = %d, MaBangLuong tam = %d.', 10, 1, @@SPID, @MaBangLuongA) WITH NOWAIT;

    -- Khong release lock cho den khi B da claim. Nhu vay B chac chan goi SP
    -- trong luc transaction A con mo, thay vi bat dau tre sau COMMIT.
    SET @Deadline = DATEADD(SECOND, 30, SYSDATETIME());
    SET @SessionBStatus = 'NOT_STARTED';
    WHILE @SessionBStatus = 'NOT_STARTED' AND SYSDATETIME() < @Deadline
    BEGIN
        SELECT @SessionBStatus = SessionBStatus
        FROM ##TV4_Payroll_Concurrency_State
        WHERE DemoId = 1;
        IF @SessionBStatus = 'NOT_STARTED'
            WAITFOR DELAY '00:00:01';
    END;

    IF @SessionBStatus <> 'STARTED'
        THROW 51114, N'Session B khong claim trong luc A dang giu lock. Demo se rollback va cleanup.', 1;

    WAITFOR DELAY '00:00:20';

    COMMIT TRANSACTION;

    UPDATE ##TV4_Payroll_Concurrency_State
    SET SessionAStatus = 'RELEASED',
        MaBangLuongA = @MaBangLuongA
    WHERE DemoId = 1;

    RAISERROR(N'[A] Da COMMIT; lock da nha. Dang doi Session B hoan tat...', 10, 1) WITH NOWAIT;

    -- B co 60 giay de claim va hoan tat. Neu B chua bat dau, A dong cong
    -- (CANCELLED) truoc cleanup de B khong the bat dau tre gay race condition.
    SET @Deadline = DATEADD(SECOND, 60, SYSDATETIME());

    WHILE SYSDATETIME() < @Deadline
    BEGIN
        SELECT @SessionBStatus = SessionBStatus
        FROM ##TV4_Payroll_Concurrency_State
        WHERE DemoId = 1;

        IF @SessionBStatus IN ('DONE', 'ERROR')
            BREAK;

        WAITFOR DELAY '00:00:01';
    END;

    SELECT @SessionBStatus = SessionBStatus,
           @MaBangLuongB = MaBangLuongB,
           @SessionBElapsedMs = SessionBElapsedMs
    FROM ##TV4_Payroll_Concurrency_State
    WHERE DemoId = 1;

    IF @SessionBStatus = 'NOT_STARTED'
    BEGIN
        UPDATE ##TV4_Payroll_Concurrency_State
        SET SessionBStatus = 'CANCELLED'
        WHERE DemoId = 1
          AND SessionBStatus = 'NOT_STARTED';

        IF @@ROWCOUNT = 1
            THROW 51105, N'Session B khong duoc chay trong thoi gian cho. Fixture se duoc cleanup; hay chay lai demo dung thu tu.', 1;

        SELECT @SessionBStatus = SessionBStatus
        FROM ##TV4_Payroll_Concurrency_State
        WHERE DemoId = 1;
    END;

    IF @SessionBStatus = 'STARTED'
        THROW 51106, N'Session B da bat dau nhung khong hoan tat trong 60 giay.', 1;

    IF @SessionBStatus = 'ERROR'
        THROW 51107, N'Session B bao loi. Xem Messages cua Session B va cot SessionBError.', 1;

    IF @SessionBStatus <> 'DONE'
        THROW 51108, N'Trang thai bat tay Session B khong hop le.', 1;

    -- Range/row lock trong verification cung dam bao B da ket thuc transaction.
    BEGIN TRANSACTION;

    SELECT
        @HeaderCount = COUNT(*),
        @NgayTaoSau = MAX(NgayTao)
    FROM dbo.BANGLUONG WITH (UPDLOCK, HOLDLOCK)
    WHERE MaBangLuong = @MaBangLuongA
      AND Thang = @Thang AND Nam = @Nam;

    SELECT
        @DetailCount = COUNT(*),
        @DistinctEmployee = COUNT(DISTINCT ct.MaNV)
    FROM dbo.CHITIETBANGLUONG AS ct
    WHERE ct.MaBangLuong = @MaBangLuongA;

    SELECT @DuplicateGroups = COUNT(*)
    FROM
    (
        SELECT ct.MaBangLuong, ct.MaNV
        FROM dbo.CHITIETBANGLUONG AS ct
        WHERE ct.MaBangLuong = @MaBangLuongA
        GROUP BY ct.MaBangLuong, ct.MaNV
        HAVING COUNT(*) > 1
    ) AS d;

    IF @HeaderCount <> 1
        THROW 51109, N'FAIL: ky demo khong co dung mot BANGLUONG.', 1;

    IF @MaBangLuongA IS NULL OR @MaBangLuongB IS NULL OR @MaBangLuongA <> @MaBangLuongB
        THROW 51110, N'FAIL: hai session khong tra ve cung MaBangLuong.', 1;

    IF @DetailCount <= 0
        THROW 51118, N'FAIL: ky demo khong co chi tiet luong; concurrency test khong du y nghia.', 1;

    IF @DetailCount <> @DistinctEmployee OR @DuplicateGroups <> 0
        THROW 51111, N'FAIL: CHITIETBANGLUONG bi trung nhan vien trong cung ky.', 1;

    IF @SessionBElapsedMs IS NULL OR @SessionBElapsedMs < 15000
        THROW 51119, N'FAIL: Session B khong bi block toi thieu 15000 ms.', 1;

    IF @NgayTaoSau <= @NgayTaoA
        THROW 51112, N'FAIL: khong thay dau vet Session B da tinh lai ky luong.', 1;

    SELECT
        s.SessionAId,
        s.SessionBId,
        s.SessionBElapsedMs,
        @MaBangLuongA AS MaBangLuongA,
        @MaBangLuongB AS MaBangLuongB,
        @HeaderCount AS SoHeaderCungKy,
        @DetailCount AS SoChiTiet,
        @DistinctEmployee AS SoNhanVienKhacNhau,
        @DuplicateGroups AS NhomBiTrung,
        @NgayTaoA AS NgayTaoSauSessionA,
        @NgayTaoSau AS NgayTaoSauSessionB,
        N'PASS - B da cho lock; hai session dung chung 1 header va khong duplicate' AS KetLuan
    FROM ##TV4_Payroll_Concurrency_State AS s
    WHERE s.DemoId = 1;

    -- Cleanup trong cung transaction verification.
    DELETE FROM dbo.CHITIETBANGLUONG
    WHERE MaBangLuong = @MaBangLuongA;

    DELETE FROM dbo.BANGLUONG
    WHERE MaBangLuong = @MaBangLuongA
      AND Thang = @Thang AND Nam = @Nam
      AND TrangThai = 'CHUA_CHOT';

    IF @@ROWCOUNT <> 1
        THROW 51120, N'Cleanup tu choi xoa header vi no khong con khop ky CHUA_CHOT do test tao.', 1;

    DELETE FROM dbo.CHAMCONG
    WHERE MaChamCong = @MaChamCongFixture
      AND GhiChu = @Marker;

    DELETE FROM dbo.NHANVIEN
    WHERE @NhanVienOwned = 1
      AND MaNV = @MaNVFixture
      AND CCCD = '999999999995'
      AND Email = 'tv4.payroll.concurrency@example.invalid';
    DELETE FROM dbo.CHUCVU
    WHERE @ChucVuOwned = 1
      AND MaCV = @MaCVFixture
      AND TenCV = N'TV4 payroll concurrency fixture';
    DELETE FROM dbo.PHONGBAN
    WHERE @PhongBanOwned = 1
      AND MaPB = @MaPBFixture
      AND TenPB = N'TV4 payroll concurrency fixture';

    COMMIT TRANSACTION;
    SET @FixtureOwned = 0;
    SET @NhanVienOwned = 0;
    SET @ChucVuOwned = 0;
    SET @PhongBanOwned = 0;

    IF EXISTS (SELECT 1 FROM dbo.BANGLUONG WHERE Thang = @Thang AND Nam = @Nam)
       OR EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE GhiChu = @Marker)
       OR EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE MaNV = @MaNVFixture)
        THROW 51113, N'Cleanup verification that bai.', 1;

    DROP TABLE ##TV4_Payroll_Concurrency_State;

    SELECT
        0 AS BangLuongDemoConLai,
        0 AS ChamCongFixtureConLai,
        N'PASS - cleanup hoan tat' AS CleanupStatus;
END TRY
BEGIN CATCH
    DECLARE @ErrorNumberA INT = ERROR_NUMBER();
    DECLARE @ErrorMessageA NVARCHAR(2048) = ERROR_MESSAGE();

    IF XACT_STATE() <> 0
        ROLLBACK TRANSACTION;

    IF @IdentityInsertTable IS NOT NULL
    BEGIN
        BEGIN TRY
            IF @IdentityInsertTable = N'dbo.PHONGBAN'
                SET IDENTITY_INSERT dbo.PHONGBAN OFF;
            ELSE IF @IdentityInsertTable = N'dbo.CHUCVU'
                SET IDENTITY_INSERT dbo.CHUCVU OFF;
            ELSE IF @IdentityInsertTable = N'dbo.NHANVIEN'
                SET IDENTITY_INSERT dbo.NHANVIEN OFF;
            SET @IdentityInsertTable = NULL;
        END TRY
        BEGIN CATCH
            PRINT N'[A] Canh bao: khong tat duoc IDENTITY_INSERT trong cleanup.';
        END CATCH;
    END;

    -- Dong cong voi B neu B chua claim. Neu B da claim, lenh xoa payroll ben
    -- duoi se doi transaction B ket thuc roi moi cleanup.
    IF OBJECT_ID(N'tempdb..##TV4_Payroll_Concurrency_State', N'U') IS NOT NULL
    BEGIN
        UPDATE ##TV4_Payroll_Concurrency_State
        SET SessionAStatus = 'CLEANING',
            SessionBStatus = CASE WHEN SessionBStatus = 'NOT_STARTED'
                                  THEN 'CANCELLED' ELSE SessionBStatus END
        WHERE DemoId = 1;
    END;

    BEGIN TRY
        BEGIN TRANSACTION;

        DELETE ct
        FROM dbo.CHITIETBANGLUONG AS ct
        INNER JOIN dbo.BANGLUONG AS bl
            ON bl.MaBangLuong = ct.MaBangLuong
        WHERE @OwnsPayrollPeriod = 1
          AND bl.MaBangLuong = @MaBangLuongA
          AND bl.Thang = @Thang AND bl.Nam = @Nam
          AND bl.TrangThai = 'CHUA_CHOT';

        DELETE FROM dbo.BANGLUONG
        WHERE @OwnsPayrollPeriod = 1
          AND MaBangLuong = @MaBangLuongA
          AND Thang = @Thang AND Nam = @Nam
          AND TrangThai = 'CHUA_CHOT';

        DELETE FROM dbo.CHAMCONG
        WHERE @FixtureOwned = 1
          AND MaChamCong = @MaChamCongFixture
          AND GhiChu = @Marker;

        DELETE FROM dbo.NHANVIEN
        WHERE @NhanVienOwned = 1
          AND MaNV = @MaNVFixture
          AND CCCD = '999999999995'
          AND Email = 'tv4.payroll.concurrency@example.invalid';
        DELETE FROM dbo.CHUCVU
        WHERE @ChucVuOwned = 1
          AND MaCV = @MaCVFixture
          AND TenCV = N'TV4 payroll concurrency fixture';
        DELETE FROM dbo.PHONGBAN
        WHERE @PhongBanOwned = 1
          AND MaPB = @MaPBFixture
          AND TenPB = N'TV4 payroll concurrency fixture';

        COMMIT TRANSACTION;
        SET @FixtureOwned = 0;
        SET @NhanVienOwned = 0;
        SET @ChucVuOwned = 0;
        SET @PhongBanOwned = 0;
    END TRY
    BEGIN CATCH
        IF XACT_STATE() <> 0
            ROLLBACK TRANSACTION;

        PRINT N'[A] CANH BAO: cleanup gap loi: ' + ERROR_MESSAGE();
    END CATCH;

    IF OBJECT_ID(N'tempdb..##TV4_Payroll_Concurrency_State', N'U') IS NOT NULL
        DROP TABLE ##TV4_Payroll_Concurrency_State;

    PRINT N'[A] Loi ' + CONVERT(NVARCHAR(20), @ErrorNumberA) + N': ' + @ErrorMessageA;
    PRINT N'[A] Da chay nhanh cleanup cho ky 01/2020 va marker fixture.';
    THROW;
END CATCH;
