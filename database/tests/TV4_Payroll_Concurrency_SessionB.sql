-- ============================================================================
-- TV4 - PAYROLL CONCURRENCY DEMO, SESSION B
-- Tac gia : Nguyen Quang Vinh (TV4 - MSSV 24110385)
--
-- CHI CHAY file nay sau khi Session A in:
--   "SESSION A DA GIU LOCK trong 20 giay. CHAY SESSION B NGAY BAY GIO."
--
-- Ket qua mong doi:
--   - Lenh EXEC cho khoang thoi gian con lai cua WAITFOR ben Session A.
--   - B tra cung MaBangLuong voi A; mot ky chi co mot header.
--   - Moi nhan vien co toi da mot chi tiet; khong duplicate.
--   - Session A la owner fixture va se verification + cleanup sau khi B bao DONE.
-- ============================================================================

SET NOCOUNT ON;
SET XACT_ABORT ON;
SET LOCK_TIMEOUT 60000;

DECLARE @Thang             INT = 1;
DECLARE @Nam               INT = 2020;
DECLARE @NgayCongChuan     INT = 26;
DECLARE @AppLockResource   NVARCHAR(255) = N'TV4.Payroll.Concurrency.01.2020';
DECLARE @MaBangLuongB      INT;
DECLARE @BatDau            DATETIME2(3);
DECLARE @KetThuc           DATETIME2(3);
DECLARE @ThongBaoBatDau    NVARCHAR(2048);
DECLARE @ElapsedMs         INT;
DECLARE @HeaderCount       INT;
DECLARE @DetailCount       INT;
DECLARE @DistinctEmployee  INT;
DECLARE @DuplicateGroups   INT;

IF @@TRANCOUNT <> 0
    THROW 51200, N'Session B phai bat dau khi @@TRANCOUNT = 0.', 1;

IF OBJECT_ID(N'dbo.sp_TinhBangLuongThang', N'P') IS NULL
    THROW 51201, N'Thieu dbo.sp_TinhBangLuongThang. Hay chay module TV4 truoc.', 1;

IF OBJECT_ID(N'tempdb..##TV4_Payroll_Concurrency_State', N'U') IS NULL
    THROW 51202, N'Khong tim thay Session A. Hay chay Session A truoc.', 1;

IF NOT EXISTS
(
    SELECT 1
    FROM ##TV4_Payroll_Concurrency_State
    WHERE DemoId = 1
      AND DatabaseName = DB_NAME()
      AND Thang = @Thang AND Nam = @Nam
)
    THROW 51203, N'Database/ky demo cua Session A khong khop voi Session B.', 1;

-- A giu application lock voi owner Transaction ngay truoc khi claim range
-- (Thang, Nam). APPLOCK_TEST = 0 xac nhan B khong the lay lock xung dot, nen B
-- khong the claim/chay procedure som trong khe READY -> BEGIN TRANSACTION cua A.
IF APPLOCK_TEST(N'public', @AppLockResource, N'Exclusive', N'Session') <> 0
    THROW 51209, N'Session A chua giu lock payroll. Chi chay B sau thong bao SESSION A DA GIU LOCK.', 1;

-- Claim quyen chay B mot cach atomic. Neu A da CANCELLED/cleanup hoac mot B
-- khac da claim thi script dung, khong goi procedure tre.
UPDATE ##TV4_Payroll_Concurrency_State
SET SessionBId = @@SPID,
    SessionBStatus = 'STARTED',
    SessionBError = NULL
WHERE DemoId = 1
  AND SessionBStatus = 'NOT_STARTED'
  AND SessionAStatus = 'READY'
  AND DatabaseName = DB_NAME();

IF @@ROWCOUNT <> 1
    THROW 51204, N'Session B khong claim duoc demo (da co B khac hoac A da dong cong cleanup).', 1;

BEGIN TRY
    SET @BatDau = SYSDATETIME();

    SET @ThongBaoBatDau = N'[B] Bat dau luc '
        + CONVERT(NVARCHAR(23), @BatDau, 121)
        + N'; SPID = ' + CONVERT(NVARCHAR(20), @@SPID)
        + N'. Dang goi procedure va co the bi BLOCKED...';
    RAISERROR(@ThongBaoBatDau, 10, 1) WITH NOWAIT;

    EXEC dbo.sp_TinhBangLuongThang
        @Thang = @Thang,
        @Nam = @Nam,
        @NgayCongChuan = @NgayCongChuan,
        @MaBangLuong = @MaBangLuongB OUTPUT;

    SET @KetThuc = SYSDATETIME();
    SET @ElapsedMs = DATEDIFF(MILLISECOND, @BatDau, @KetThuc);

    SELECT @HeaderCount = COUNT(*)
    FROM dbo.BANGLUONG
    WHERE Thang = @Thang AND Nam = @Nam;

    SELECT
        @DetailCount = COUNT(*),
        @DistinctEmployee = COUNT(DISTINCT ct.MaNV)
    FROM dbo.CHITIETBANGLUONG AS ct
    INNER JOIN dbo.BANGLUONG AS bl
        ON bl.MaBangLuong = ct.MaBangLuong
    WHERE bl.Thang = @Thang AND bl.Nam = @Nam;

    SELECT @DuplicateGroups = COUNT(*)
    FROM
    (
        SELECT ct.MaBangLuong, ct.MaNV
        FROM dbo.CHITIETBANGLUONG AS ct
        INNER JOIN dbo.BANGLUONG AS bl
            ON bl.MaBangLuong = ct.MaBangLuong
        WHERE bl.Thang = @Thang AND bl.Nam = @Nam
        GROUP BY ct.MaBangLuong, ct.MaNV
        HAVING COUNT(*) > 1
    ) AS d;

    IF @HeaderCount <> 1
        THROW 51205, N'FAIL: sau Session B khong co dung mot BANGLUONG cho ky.', 1;

    IF @DetailCount <= 0
        THROW 51207, N'FAIL: ky demo khong co chi tiet luong; concurrency test khong du y nghia.', 1;

    IF @DetailCount <> @DistinctEmployee OR @DuplicateGroups <> 0
        THROW 51206, N'FAIL: co duplicate CHITIETBANGLUONG trong cung ky/nhan vien.', 1;

    IF @ElapsedMs < 15000
        THROW 51208, N'FAIL: Session B khong bi block toi thieu 15000 ms.', 1;

    -- Bao DONE sau khi B da verification xong; luc do A moi duoc cleanup.
    UPDATE ##TV4_Payroll_Concurrency_State
    SET SessionBStatus = 'DONE',
        MaBangLuongB = @MaBangLuongB,
        SessionBElapsedMs = @ElapsedMs
    WHERE DemoId = 1
      AND SessionBId = @@SPID;

    RAISERROR(N'[B] Hoan tat sau %d ms. Neu lon xap xi thoi gian A con giu lock thi blocking da duoc chung minh.',
              10, 1, @ElapsedMs) WITH NOWAIT;

    SELECT
        @@SPID AS SessionBId,
        @MaBangLuongB AS MaBangLuongB,
        @BatDau AS BatDau,
        @KetThuc AS KetThuc,
        @ElapsedMs AS ElapsedMs,
        @HeaderCount AS SoHeaderCungKy,
        @DetailCount AS SoChiTiet,
        @DistinctEmployee AS SoNhanVienKhacNhau,
        @DuplicateGroups AS NhomBiTrung,
        N'PASS - procedure hoan tat sau khi A nha lock, khong duplicate' AS KetLuan;

    PRINT N'[B] Khong cleanup tai Session B. Session A dang doi DONE va se cleanup an toan.';
END TRY
BEGIN CATCH
    DECLARE @ErrorNumberB INT = ERROR_NUMBER();
    DECLARE @ErrorMessageB NVARCHAR(2048) = ERROR_MESSAGE();

    IF XACT_STATE() <> 0
        ROLLBACK TRANSACTION;

    IF OBJECT_ID(N'tempdb..##TV4_Payroll_Concurrency_State', N'U') IS NOT NULL
    BEGIN
        UPDATE ##TV4_Payroll_Concurrency_State
        SET SessionBStatus = 'ERROR',
            SessionBError = CONCAT(N'Error ', @ErrorNumberB, N': ', @ErrorMessageB)
        WHERE DemoId = 1
          AND SessionBId = @@SPID;
    END;

    PRINT N'[B] Loi ' + CONVERT(NVARCHAR(20), @ErrorNumberB) + N': ' + @ErrorMessageB;
    PRINT N'[B] Da bao ERROR cho Session A; Session A se cleanup fixture.';
    THROW;
END CATCH;
