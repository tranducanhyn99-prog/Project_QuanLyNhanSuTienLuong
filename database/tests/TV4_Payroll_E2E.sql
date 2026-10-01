-- ============================================================================
-- TV4 - NGUYEN QUANG VINH - KIEM THU PAYROLL E2E
-- End-to-end test: du lieu nguon -> tinh luong -> rollback -> tinh lai -> chot
--
-- Cach chay:
--   1. Cai dat cac script database 01 -> 05 theo dung thu tu.
--   2. Chay TOAN BO file nay trong mot session SQL Server duy nhat.
--
-- Nguyen tac an toan:
--   * Tu chon mot ky luong chua ton tai va nam tron trong qua khu.
--   * Tao nhan vien/du lieu nguon rieng, co khoa duy nhat.
--   * Moi assertion deu dung THROW; bat ky sai lech nao cung lam test that bai.
--   * Trigger gia lap loi chi tac dong session test qua SESSION_CONTEXT va luon
--     duoc DROP trong khoi finalizer.
--   * Cleanup chi xoa cac khoa fixture vua tao; khong xoa/cap nhat du lieu co san.
-- ============================================================================

-- Chay tren database dang duoc connection/SSMS chon. Preflight ben duoi se
-- dung truoc moi mutation neu thieu object bat buoc.
GO

SET NOCOUNT ON;
SET XACT_ABORT OFF;

DECLARE @InitialTranCount       INT = @@TRANCOUNT;
DECLARE @AppLockResult          INT = -999;
DECLARE @AppLockAcquired        BIT = 0;
DECLARE @TestCompleted          BIT = 0;
DECLARE @TestError              NVARCHAR(2048) = NULL;
DECLARE @CleanupError           NVARCHAR(2048) = NULL;
DECLARE @FinalizerError         NVARCHAR(2048) = NULL;

DECLARE @Token                  VARCHAR(32) = REPLACE(CONVERT(VARCHAR(36), NEWID()), '-', '');
DECLARE @MaPB                   INT = NULL;
DECLARE @MaCV                   INT = NULL;
DECLARE @MaNV                   INT = NULL;
DECLARE @MaBangLuong            INT = NULL;
DECLARE @Thang                  INT = NULL;
DECLARE @Nam                    INT = NULL;
DECLARE @PeriodStart            DATE = NULL;
DECLARE @LatestSafePeriod       DATE = NULL;
DECLARE @PayrollExecutionBegan  BIT = 0;

DECLARE @LuongCoBan             DECIMAL(18,2) = 26000000.00;
DECLARE @CCCD                   VARCHAR(12) = NULL;
DECLARE @SoDienThoai            VARCHAR(10) = NULL;
DECLARE @Email                  VARCHAR(100) = NULL;

BEGIN TRY
    IF @InitialTranCount <> 0
        THROW 52000, N'TV4 E2E phai duoc chay ngoai transaction do caller tao.', 1;

    EXEC @AppLockResult = sys.sp_getapplock
        @Resource = N'TV4_Payroll_E2E',
        @LockMode = 'Exclusive',
        @LockOwner = 'Session',
        @LockTimeout = 10000;

    IF @AppLockResult < 0
        THROW 52001, N'Khong the lay application lock cho TV4 E2E.', 1;

    SET @AppLockAcquired = 1;

    -- ------------------------------------------------------------------------
    -- 0. Preflight: day la integration test, vi vay can day du module 01 -> 05.
    -- ------------------------------------------------------------------------
    IF OBJECT_ID(N'dbo.PHONGBAN', N'U') IS NULL
       OR OBJECT_ID(N'dbo.CHUCVU', N'U') IS NULL
       OR OBJECT_ID(N'dbo.NHANVIEN', N'U') IS NULL
       OR OBJECT_ID(N'dbo.CHAMCONG', N'U') IS NULL
       OR OBJECT_ID(N'dbo.PHUCAPNHANVIEN', N'U') IS NULL
       OR OBJECT_ID(N'dbo.KHAUTRUNHANVIEN', N'U') IS NULL
       OR OBJECT_ID(N'dbo.BANGLUONG', N'U') IS NULL
       OR OBJECT_ID(N'dbo.CHITIETBANGLUONG', N'U') IS NULL
        THROW 52002, N'Thieu bang phu thuoc. Hay cai dat cac script database 01 -> 05 truoc.', 1;

    IF OBJECT_ID(N'dbo.sp_TinhBangLuongThang', N'P') IS NULL
       OR OBJECT_ID(N'dbo.sp_ChotBangLuong', N'P') IS NULL
       OR OBJECT_ID(N'dbo.sp_HuyChotBangLuong', N'P') IS NULL
       OR OBJECT_ID(N'dbo.fn_TinhTienCong', N'FN') IS NULL
        THROW 52003, N'Thieu procedure/function tinh hoac chot bang luong.', 1;

    IF OBJECT_ID(N'dbo.trg_BangLuong_KhongSuaKhiDaChot', N'TR') IS NULL
       OR OBJECT_ID(N'dbo.trg_ChiTietLuong_KhongSuaKhiDaChot', N'TR') IS NULL
        THROW 52004, N'Thieu trigger khoa du lieu bang luong da chot.', 1;

    IF EXISTS
    (
        SELECT 1
        FROM sys.triggers
        WHERE object_id IN
        (
            OBJECT_ID(N'dbo.trg_BangLuong_KhongSuaKhiDaChot'),
            OBJECT_ID(N'dbo.trg_ChiTietLuong_KhongSuaKhiDaChot')
        )
          AND is_disabled = 1
    )
        THROW 52005, N'Trigger khoa du lieu dang bi DISABLE; khong the kiem thu dung.', 1;

    -- Don trigger test bi bo lai boi mot lan chay cu bi ngat ket noi.
    IF OBJECT_ID(N'dbo.trg_TV4_E2E_ForceChiTietInsertError', N'TR') IS NOT NULL
        EXEC sys.sp_executesql
            N'DROP TRIGGER dbo.trg_TV4_E2E_ForceChiTietInsertError;';

    EXEC sys.sp_set_session_context
        @key = N'TV4_E2E_FORCE_DETAIL_ERROR',
        @value = NULL;

    IF OBJECT_ID(N'tempdb..#TV4_E2E_DetailBeforeFailure', N'U') IS NOT NULL
        DROP TABLE #TV4_E2E_DetailBeforeFailure;

    IF OBJECT_ID(N'tempdb..#TV4_E2E_HeaderBeforeFailure', N'U') IS NOT NULL
        DROP TABLE #TV4_E2E_HeaderBeforeFailure;

    -- Procedure khong duoc tu y rollback transaction do caller so huu khi loi
    -- validation xay ra truoc transaction noi bo.
    DECLARE @NestedTranErrorCaught BIT = 0;
    DECLARE @NestedTranOutput INT = NULL;
    DECLARE @NestedTranCount INT;

    SET XACT_ABORT ON;
    BEGIN TRANSACTION;
    SET @NestedTranCount = @@TRANCOUNT;

    BEGIN TRY
        EXEC dbo.sp_TinhBangLuongThang
            @Thang = 0,
            @Nam = 2026,
            @NgayCongChuan = 26,
            @MaBangLuong = @NestedTranOutput OUTPUT;
    END TRY
    BEGIN CATCH
        IF ERROR_MESSAGE() LIKE N'%1 den 12%' OR ERROR_MESSAGE() LIKE N'%1-12%'
            SET @NestedTranErrorCaught = 1;
        ELSE
        BEGIN
            IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
            SET XACT_ABORT OFF;
            THROW;
        END;
    END CATCH;

    IF @NestedTranErrorCaught = 0
       OR @@TRANCOUNT <> @NestedTranCount
       OR XACT_STATE() <> 1
       OR (16384 & @@OPTIONS) <> 16384
    BEGIN
        IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
        SET XACT_ABORT OFF;
        THROW 52040, N'Procedure da lam thay doi/vo transaction do caller so huu.', 1;
    END;

    ROLLBACK TRANSACTION;
    SET XACT_ABORT OFF;

    -- Chon thang gan nhat nam tron trong qua khu, gioi han theo constraint
    -- BANGLUONG (2020..2100). Uu tien ky khong co ca du lieu nguon san co.
    SET @LatestSafePeriod = DATEADD
    (
        MONTH,
        -1,
        DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1)
    );

    IF @LatestSafePeriod > CONVERT(DATE, '21001201', 112)
        SET @LatestSafePeriod = CONVERT(DATE, '21001201', 112);

    IF @LatestSafePeriod < CONVERT(DATE, '20200101', 112)
        THROW 52006, N'Ngay may chu khong cho phep tao ky test trong mien nam 2020..2100.', 1;

    ;WITH NumberSource AS
    (
        SELECT TOP (972)
            ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) - 1 AS MonthOffset
        FROM sys.all_objects AS a
        CROSS JOIN sys.all_objects AS b
    ),
    CandidatePeriod AS
    (
        SELECT DATEADD(MONTH, MonthOffset, CONVERT(DATE, '20200101', 112)) AS PeriodStart
        FROM NumberSource
    )
    SELECT TOP (1)
        @PeriodStart = p.PeriodStart
    FROM CandidatePeriod AS p
    WHERE p.PeriodStart <= @LatestSafePeriod
      AND NOT EXISTS
          (
              SELECT 1
              FROM dbo.BANGLUONG AS bl
              WHERE bl.Thang = MONTH(p.PeriodStart)
                AND bl.Nam = YEAR(p.PeriodStart)
          )
    ORDER BY
        CASE
            WHEN NOT EXISTS
                 (
                     SELECT 1
                     FROM dbo.CHAMCONG AS cc
                     WHERE cc.NgayChamCong >= p.PeriodStart
                       AND cc.NgayChamCong < DATEADD(MONTH, 1, p.PeriodStart)
                 )
             AND NOT EXISTS
                 (
                     SELECT 1
                     FROM dbo.PHUCAPNHANVIEN AS pc
                     WHERE pc.Thang = MONTH(p.PeriodStart)
                       AND pc.Nam = YEAR(p.PeriodStart)
                 )
             AND NOT EXISTS
                 (
                     SELECT 1
                     FROM dbo.KHAUTRUNHANVIEN AS kt
                     WHERE kt.Thang = MONTH(p.PeriodStart)
                       AND kt.Nam = YEAR(p.PeriodStart)
                 )
            THEN 0
            ELSE 1
        END,
        p.PeriodStart DESC;

    IF @PeriodStart IS NULL
        THROW 52007, N'Khong con ky luong trong de chay TV4 E2E.', 1;

    SET @Thang = MONTH(@PeriodStart);
    SET @Nam = YEAR(@PeriodStart);

    -- Loi xay ra sau SAVE TRANSACTION cung chi duoc rollback phan procedure,
    -- khong rollback/dooming transaction cua caller va phai khoi phuc XACT_ABORT.
    -- Cung kiem tra nhanh ca nhanh loi/thanh cong cua procedure mo lai TV4.
    DECLARE @NestedPostSavepointCaught BIT = 0;
    DECLARE @NestedReopenErrorCaught BIT = 0;
    DECLARE @NestedFinalizedHeaderId INT;

    SET XACT_ABORT ON;
    BEGIN TRANSACTION;
    SET @NestedTranCount = @@TRANCOUNT;

    INSERT INTO dbo.BANGLUONG (Thang, Nam, NgayCongChuan, TrangThai, NgayChot)
    VALUES (@Thang, @Nam, 26, 'DA_CHOT', GETDATE());
    SET @NestedFinalizedHeaderId = CONVERT(INT, SCOPE_IDENTITY());

    BEGIN TRY
        EXEC dbo.sp_TinhBangLuongThang
            @Thang = @Thang,
            @Nam = @Nam,
            @NgayCongChuan = 26,
            @MaBangLuong = @NestedTranOutput OUTPUT;
    END TRY
    BEGIN CATCH
        IF CHARINDEX(N'Không thể tính lại', ERROR_MESSAGE()) > 0
           OR CHARINDEX(N'Kỳ lương này đã được chốt', ERROR_MESSAGE()) > 0
            SET @NestedPostSavepointCaught = 1;
        ELSE
        BEGIN
            IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
            SET XACT_ABORT OFF;
            THROW;
        END;
    END CATCH;

    IF @NestedPostSavepointCaught = 0
       OR @@TRANCOUNT <> @NestedTranCount
       OR XACT_STATE() <> 1
       OR (16384 & @@OPTIONS) <> 16384
       OR NOT EXISTS
          (
              SELECT 1
              FROM dbo.BANGLUONG
              WHERE MaBangLuong = @NestedFinalizedHeaderId
                AND TrangThai = 'DA_CHOT'
          )
    BEGIN
        IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
        SET XACT_ABORT OFF;
        THROW 52041, N'Rollback savepoint da anh huong transaction do caller so huu.', 1;
    END;

    EXEC dbo.sp_HuyChotBangLuong
        @MaBangLuong = @NestedFinalizedHeaderId;

    IF @@TRANCOUNT <> @NestedTranCount
       OR XACT_STATE() <> 1
       OR (16384 & @@OPTIONS) <> 16384
       OR NOT EXISTS
          (
              SELECT 1
              FROM dbo.BANGLUONG
              WHERE MaBangLuong = @NestedFinalizedHeaderId
                AND TrangThai = 'CHUA_CHOT'
                AND NgayChot IS NULL
          )
    BEGIN
        IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
        SET XACT_ABORT OFF;
        THROW 52043, N'sp_HuyChotBangLuong da commit/vo transaction cua caller.', 1;
    END;

    ROLLBACK TRANSACTION;
    SET XACT_ABORT OFF;
    SET @NestedTranOutput = NULL;

    -- Khoa key-range cua ky vua chon cho den khi fixture va lan tinh dau COMMIT.
    -- Neu mot session khac chen dung ky trong khe thoi gian sau SELECT, test se
    -- dung an toan thay vi tinh lai/xoa nham bang luong cua session do.
    BEGIN TRANSACTION;

    IF EXISTS
    (
        SELECT 1
        FROM dbo.BANGLUONG WITH (UPDLOCK, HOLDLOCK)
        WHERE Thang = @Thang AND Nam = @Nam
    )
        THROW 52038, N'Ky test vua duoc session khac tao; dung de bao ve du lieu san co.', 1;

    -- ------------------------------------------------------------------------
    -- 1. Fixture rieng: danh muc, nhan vien, cham cong, phu cap, khau tru.
    -- ------------------------------------------------------------------------
    INSERT INTO dbo.PHONGBAN (TenPB, SoDienThoai, TrangThai)
    VALUES (CONCAT(N'TV4 E2E PB ', @Token), NULL, N'HOAT_DONG');

    SET @MaPB = CONVERT(INT, SCOPE_IDENTITY());

    INSERT INTO dbo.CHUCVU (TenCV, PhuCapChucVu)
    VALUES (CONCAT(N'TV4 E2E CV ', @Token), 0);

    SET @MaCV = CONVERT(INT, SCOPE_IDENTITY());

    WHILE @CCCD IS NULL OR EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE CCCD = @CCCD)
    BEGIN
        SET @CCCD = CONVERT
        (
            VARCHAR(12),
            900000000000 + ABS(CONVERT(BIGINT, CHECKSUM(NEWID()))) % 99999999999
        );
    END;

    WHILE @SoDienThoai IS NULL
       OR EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE SoDienThoai = @SoDienThoai)
    BEGIN
        SET @SoDienThoai = CONCAT
        (
            '09',
            RIGHT
            (
                CONCAT
                (
                    '00000000',
                    CONVERT(VARCHAR(8), ABS(CONVERT(BIGINT, CHECKSUM(NEWID()))) % 100000000)
                ),
                8
            )
        );
    END;

    SET @Email = CONCAT('tv4.e2e.', LOWER(@Token), '@example.test');

    INSERT INTO dbo.NHANVIEN
    (
        HoTen,
        NgaySinh,
        GioiTinh,
        CCCD,
        DiaChi,
        SoDienThoai,
        Email,
        NgayVaoLam,
        LuongCoBan,
        MaPB,
        MaCV,
        TrangThai
    )
    VALUES
    (
        CONCAT(N'TV4 E2E Nguyen Quang Vinh ', @Token),
        CONVERT(DATE, '19900101', 112),
        N'Nam',
        @CCCD,
        N'Dia chi fixture TV4 E2E',
        @SoDienThoai,
        @Email,
        @PeriodStart,
        @LuongCoBan,
        @MaPB,
        @MaCV,
        N'DANG_LAM_VIEC'
    );

    SET @MaNV = CONVERT(INT, SCOPE_IDENTITY());

    INSERT INTO dbo.CHAMCONG
    (
        MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu
    )
    VALUES
        (@MaNV, DATEADD(DAY, 0, @PeriodStart), '08:00', '17:00', N'CO_MAT', N'TV4 E2E ngay 1'),
        (@MaNV, DATEADD(DAY, 1, @PeriodStart), '08:15', '17:00', N'DI_TRE', N'TV4 E2E ngay 2');

    INSERT INTO dbo.PHUCAPNHANVIEN
    (
        MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu
    )
    VALUES
        (@MaNV, @Thang, @Nam, N'TV4 E2E phu cap 1', 300000.00, @PeriodStart, N'Fixture'),
        (@MaNV, @Thang, @Nam, N'TV4 E2E phu cap 2', 200000.00, @PeriodStart, N'Fixture');

    INSERT INTO dbo.KHAUTRUNHANVIEN
    (
        MaNV, Thang, Nam, TenKhauTru, SoTien, NgayGhiNhan, LyDo
    )
    VALUES
        (@MaNV, @Thang, @Nam, N'TV4 E2E khau tru', 125000.00, @PeriodStart, N'Fixture');

    IF (SELECT COUNT(*) FROM dbo.CHAMCONG WHERE MaNV = @MaNV) <> 2
        THROW 52008, N'Fixture cham cong khong du 2 dong.', 1;

    IF (SELECT SUM(SoTien) FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV AND Thang = @Thang AND Nam = @Nam) <> 500000.00
        THROW 52009, N'Fixture phu cap khong dung 500000.', 1;

    IF (SELECT SUM(SoTien) FROM dbo.KHAUTRUNHANVIEN WHERE MaNV = @MaNV AND Thang = @Thang AND Nam = @Nam) <> 125000.00
        THROW 52010, N'Fixture khau tru khong dung 125000.', 1;

    PRINT N'[TV4 E2E] PASS 1/10 - Da tao fixture nguon rieng.';

    -- ------------------------------------------------------------------------
    -- 2. Tinh lan dau va doi chieu du lieu nguon -> chi tiet -> cong thuc.
    -- ------------------------------------------------------------------------
    SET @PayrollExecutionBegan = 1;

    EXEC dbo.sp_TinhBangLuongThang
        @Thang = @Thang,
        @Nam = @Nam,
        @NgayCongChuan = 26,
        @MaBangLuong = @MaBangLuong OUTPUT;

    IF @MaBangLuong IS NULL
        THROW 52011, N'Lan tinh dau khong tra ve MaBangLuong.', 1;

    IF NOT EXISTS
    (
        SELECT 1
        FROM dbo.BANGLUONG
        WHERE MaBangLuong = @MaBangLuong
          AND Thang = @Thang
          AND Nam = @Nam
          AND NgayCongChuan = 26
          AND TrangThai = 'CHUA_CHOT'
    )
        THROW 52012, N'Header bang luong lan dau khong dung.', 1;

    DECLARE @ExpectedDays       INT;
    DECLARE @ExpectedAllowance  DECIMAL(18,2);
    DECLARE @ExpectedDeduction  DECIMAL(18,2);
    DECLARE @ExpectedWage       DECIMAL(18,2);
    DECLARE @ExpectedNet        DECIMAL(18,2);
    DECLARE @FirstDetailId      INT;
    DECLARE @FirstNet           DECIMAL(18,2);

    SELECT @ExpectedDays = COUNT(*)
    FROM dbo.CHAMCONG
    WHERE MaNV = @MaNV
      AND NgayChamCong >= @PeriodStart
      AND NgayChamCong < DATEADD(MONTH, 1, @PeriodStart)
      AND TrangThai IN (N'CO_MAT', N'DI_TRE', N'VE_SOM');

    SELECT @ExpectedAllowance = ISNULL(SUM(SoTien), 0)
    FROM dbo.PHUCAPNHANVIEN
    WHERE MaNV = @MaNV AND Thang = @Thang AND Nam = @Nam;

    SELECT @ExpectedDeduction = ISNULL(SUM(SoTien), 0)
    FROM dbo.KHAUTRUNHANVIEN
    WHERE MaNV = @MaNV AND Thang = @Thang AND Nam = @Nam;

    -- Oracle doc lap: tinh truc tiep theo cong thuc, khong dung function dang test.
    SET @ExpectedWage = ROUND((@LuongCoBan / CAST(26 AS DECIMAL(18,2))) * @ExpectedDays, 2);
    SET @ExpectedNet = @ExpectedWage + @ExpectedAllowance - @ExpectedDeduction;

    IF ISNULL(dbo.fn_TinhTienCong(@LuongCoBan, 26, @ExpectedDays), -1) <> @ExpectedWage
       OR ISNULL(dbo.fn_TinhTienCong(@LuongCoBan, 26, 0), -1) <> 0
       OR ISNULL(dbo.fn_TinhTienCong(NULL, 26, @ExpectedDays), -1) <> 0
        THROW 52039, N'fn_TinhTienCong khong khop oracle cong thuc/bien dau vao.', 1;

    IF (SELECT COUNT(*) FROM dbo.CHITIETBANGLUONG WHERE MaBangLuong = @MaBangLuong AND MaNV = @MaNV) <> 1
        THROW 52013, N'Nhan vien fixture khong co dung mot chi tiet luong.', 1;

    IF NOT EXISTS
    (
        SELECT 1
        FROM dbo.CHITIETBANGLUONG
        WHERE MaBangLuong = @MaBangLuong
          AND MaNV = @MaNV
          AND LuongCoBan = @LuongCoBan
          AND NgayCongThucTe = @ExpectedDays
          AND TienCong = @ExpectedWage
          AND TongPhuCap = @ExpectedAllowance
          AND TongKhauTru = @ExpectedDeduction
          AND ThucNhan = @ExpectedNet
    )
        THROW 52014, N'Chi tiet lan dau khong khop du lieu nguon/cong thuc.', 1;

    IF EXISTS
    (
        SELECT 1
        FROM dbo.CHITIETBANGLUONG AS ct
        INNER JOIN dbo.BANGLUONG AS bl ON bl.MaBangLuong = ct.MaBangLuong
        WHERE ct.MaBangLuong = @MaBangLuong
          AND
          (
              ct.TienCong <> CAST(ROUND(
                  (ct.LuongCoBan / CAST(bl.NgayCongChuan AS DECIMAL(18,2))) * ct.NgayCongThucTe,
                  2
              ) AS DECIMAL(18,2))
              OR ct.ThucNhan <> ct.TienCong + ct.TongPhuCap - ct.TongKhauTru
          )
    )
        THROW 52015, N'Co chi tiet trong ky vi pham cong thuc luong.', 1;

    SELECT
        @FirstDetailId = MaChiTiet,
        @FirstNet = ThucNhan
    FROM dbo.CHITIETBANGLUONG
    WHERE MaBangLuong = @MaBangLuong AND MaNV = @MaNV;

    COMMIT TRANSACTION;

    PRINT N'[TV4 E2E] PASS 2/10 - Du lieu nguon -> bang luong -> chi tiet/cong thuc dung.';

    -- ------------------------------------------------------------------------
    -- 3. Tinh lai khi CHUA_CHOT: giu MaBangLuong, nap lai du lieu moi nhat.
    -- ------------------------------------------------------------------------
    INSERT INTO dbo.PHUCAPNHANVIEN
    (
        MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu
    )
    VALUES
        (@MaNV, @Thang, @Nam, N'TV4 E2E phu cap bo sung', 150000.00, @PeriodStart, N'Fixture recalc');

    DECLARE @RecalculatedMaBangLuong INT = NULL;

    EXEC dbo.sp_TinhBangLuongThang
        @Thang = @Thang,
        @Nam = @Nam,
        @NgayCongChuan = 20,
        @MaBangLuong = @RecalculatedMaBangLuong OUTPUT;

    IF @RecalculatedMaBangLuong <> @MaBangLuong
        THROW 52016, N'Tinh lai CHUA_CHOT da tao MaBangLuong khac.', 1;

    SELECT @ExpectedAllowance = ISNULL(SUM(SoTien), 0)
    FROM dbo.PHUCAPNHANVIEN
    WHERE MaNV = @MaNV AND Thang = @Thang AND Nam = @Nam;

    SET @ExpectedWage = ROUND((@LuongCoBan / CAST(20 AS DECIMAL(18,2))) * @ExpectedDays, 2);
    SET @ExpectedNet = @ExpectedWage + @ExpectedAllowance - @ExpectedDeduction;

    IF NOT EXISTS
    (
        SELECT 1
        FROM dbo.BANGLUONG AS bl
        INNER JOIN dbo.CHITIETBANGLUONG AS ct ON ct.MaBangLuong = bl.MaBangLuong
        WHERE bl.MaBangLuong = @MaBangLuong
          AND bl.TrangThai = 'CHUA_CHOT'
          AND bl.NgayCongChuan = 20
          AND ct.MaNV = @MaNV
          AND ct.NgayCongThucTe = @ExpectedDays
          AND ct.TongPhuCap = 650000.00
          AND ct.TienCong = @ExpectedWage
          AND ct.ThucNhan = @ExpectedNet
          AND ct.ThucNhan <> @FirstNet
    )
        THROW 52017, N'Tinh lai CHUA_CHOT khong nap dung du lieu/cong thuc moi.', 1;

    IF EXISTS
    (
        SELECT 1
        FROM dbo.CHITIETBANGLUONG
        WHERE MaBangLuong = @MaBangLuong
          AND MaNV = @MaNV
          AND MaChiTiet = @FirstDetailId
    )
        THROW 52018, N'Tinh lai CHUA_CHOT khong thay the chi tiet cu.', 1;

    PRINT N'[TV4 E2E] PASS 3/10 - Tinh lai ky CHUA_CHOT thanh cong.';

    -- ------------------------------------------------------------------------
    -- 4. Them nguon moi, force loi INSERT chi tiet, chung minh rollback chinh xac.
    -- ------------------------------------------------------------------------
    INSERT INTO dbo.CHAMCONG
    (
        MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu
    )
    VALUES
        (@MaNV, DATEADD(DAY, 2, @PeriodStart), '08:00', '17:00', N'CO_MAT', N'TV4 E2E ngay 3');

    SELECT
        MaBangLuong,
        Thang,
        Nam,
        NgayCongChuan,
        TrangThai,
        NgayTao,
        NgayChot
    INTO #TV4_E2E_HeaderBeforeFailure
    FROM dbo.BANGLUONG
    WHERE MaBangLuong = @MaBangLuong;

    SELECT
        MaChiTiet,
        MaBangLuong,
        MaNV,
        LuongCoBan,
        NgayCongThucTe,
        TienCong,
        TongPhuCap,
        TongKhauTru,
        ThucNhan
    INTO #TV4_E2E_DetailBeforeFailure
    FROM dbo.CHITIETBANGLUONG
    WHERE MaBangLuong = @MaBangLuong;

    EXEC sys.sp_executesql N'
        CREATE TRIGGER dbo.trg_TV4_E2E_ForceChiTietInsertError
        ON dbo.CHITIETBANGLUONG
        AFTER INSERT
        AS
        BEGIN
            SET NOCOUNT ON;

            IF TRY_CONVERT(BIT, SESSION_CONTEXT(N''TV4_E2E_FORCE_DETAIL_ERROR'')) = 1
                THROW 52991, N''TV4_E2E_FORCED_DETAIL_INSERT_ERROR'', 1;
        END;';

    EXEC sys.sp_set_session_context
        @key = N'TV4_E2E_FORCE_DETAIL_ERROR',
        @value = 1;

    DECLARE @ForcedFailureCaught BIT = 0;
    DECLARE @ForcedFailureMessage NVARCHAR(2048) = NULL;
    DECLARE @UnexpectedForceError NVARCHAR(2048) = NULL;
    DECLARE @FailedRecalcMaBangLuong INT = NULL;

    BEGIN TRY
        EXEC dbo.sp_TinhBangLuongThang
            @Thang = @Thang,
            @Nam = @Nam,
            @NgayCongChuan = 21,
            @MaBangLuong = @FailedRecalcMaBangLuong OUTPUT;
    END TRY
    BEGIN CATCH
        SET @ForcedFailureMessage = ERROR_MESSAGE();

        IF CHARINDEX(N'TV4_E2E_FORCED_DETAIL_INSERT_ERROR', @ForcedFailureMessage) > 0
            SET @ForcedFailureCaught = 1;
        ELSE
            SET @UnexpectedForceError = @ForcedFailureMessage;
    END CATCH;

    EXEC sys.sp_set_session_context
        @key = N'TV4_E2E_FORCE_DETAIL_ERROR',
        @value = NULL;

    IF OBJECT_ID(N'dbo.trg_TV4_E2E_ForceChiTietInsertError', N'TR') IS NOT NULL
        EXEC sys.sp_executesql
            N'DROP TRIGGER dbo.trg_TV4_E2E_ForceChiTietInsertError;';

    IF @UnexpectedForceError IS NOT NULL
    BEGIN
        DECLARE @UnexpectedForceThrow NVARCHAR(2048) =
            LEFT(CONCAT(N'Loi khong mong doi trong force-error: ', @UnexpectedForceError), 2048);
        THROW 52019, @UnexpectedForceThrow, 1;
    END;

    IF @ForcedFailureCaught = 0
        THROW 52020, N'Force-error khong lam sp_TinhBangLuongThang that bai nhu mong doi.', 1;

    IF EXISTS
    (
        SELECT MaBangLuong, Thang, Nam, NgayCongChuan, TrangThai, NgayTao, NgayChot
        FROM dbo.BANGLUONG
        WHERE MaBangLuong = @MaBangLuong
        EXCEPT
        SELECT MaBangLuong, Thang, Nam, NgayCongChuan, TrangThai, NgayTao, NgayChot
        FROM #TV4_E2E_HeaderBeforeFailure
    )
        THROW 52021, N'Rollback khong khoi phuc nguyen trang header bang luong.', 1;

    IF EXISTS
    (
        SELECT MaBangLuong, Thang, Nam, NgayCongChuan, TrangThai, NgayTao, NgayChot
        FROM #TV4_E2E_HeaderBeforeFailure
        EXCEPT
        SELECT MaBangLuong, Thang, Nam, NgayCongChuan, TrangThai, NgayTao, NgayChot
        FROM dbo.BANGLUONG
        WHERE MaBangLuong = @MaBangLuong
    )
        THROW 52044, N'Rollback lam mat hoac thieu cot header bang luong.', 1;

    IF EXISTS
    (
        SELECT
            MaChiTiet, MaBangLuong, MaNV, LuongCoBan, NgayCongThucTe,
            TienCong, TongPhuCap, TongKhauTru, ThucNhan
        FROM dbo.CHITIETBANGLUONG
        WHERE MaBangLuong = @MaBangLuong
        EXCEPT
        SELECT
            MaChiTiet, MaBangLuong, MaNV, LuongCoBan, NgayCongThucTe,
            TienCong, TongPhuCap, TongKhauTru, ThucNhan
        FROM #TV4_E2E_DetailBeforeFailure
    )
        THROW 52022, N'Rollback de lai chi tiet moi/bi thay doi.', 1;

    IF EXISTS
    (
        SELECT
            MaChiTiet, MaBangLuong, MaNV, LuongCoBan, NgayCongThucTe,
            TienCong, TongPhuCap, TongKhauTru, ThucNhan
        FROM #TV4_E2E_DetailBeforeFailure
        EXCEPT
        SELECT
            MaChiTiet, MaBangLuong, MaNV, LuongCoBan, NgayCongThucTe,
            TienCong, TongPhuCap, TongKhauTru, ThucNhan
        FROM dbo.CHITIETBANGLUONG
        WHERE MaBangLuong = @MaBangLuong
    )
        THROW 52023, N'Rollback khong khoi phuc du cac chi tiet cu.', 1;

    IF NOT EXISTS
    (
        SELECT 1
        FROM dbo.CHITIETBANGLUONG
        WHERE MaBangLuong = @MaBangLuong
          AND MaNV = @MaNV
          AND NgayCongThucTe = 2
    )
        THROW 52024, N'Chi tiet cu khong duoc giu nguyen sau rollback.', 1;

    IF (SELECT COUNT(*) FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND TrangThai IN (N'CO_MAT', N'DI_TRE', N'VE_SOM')) <> 3
        THROW 52025, N'Du lieu nguon moi bi mat ngoai transaction tinh luong.', 1;

    PRINT N'[TV4 E2E] PASS 4/10 - Force loi INSERT chi tiet va rollback toan bo thanh cong.';

    -- ------------------------------------------------------------------------
    -- 5. Bo force-error va tinh lai thanh cong theo nguon 3 ngay cong.
    -- ------------------------------------------------------------------------
    SET @RecalculatedMaBangLuong = NULL;

    EXEC dbo.sp_TinhBangLuongThang
        @Thang = @Thang,
        @Nam = @Nam,
        @NgayCongChuan = 22,
        @MaBangLuong = @RecalculatedMaBangLuong OUTPUT;

    IF @RecalculatedMaBangLuong <> @MaBangLuong
        THROW 52026, N'Tinh lai sau force-error khong giu nguyen MaBangLuong.', 1;

    SELECT @ExpectedDays = COUNT(*)
    FROM dbo.CHAMCONG
    WHERE MaNV = @MaNV
      AND NgayChamCong >= @PeriodStart
      AND NgayChamCong < DATEADD(MONTH, 1, @PeriodStart)
      AND TrangThai IN (N'CO_MAT', N'DI_TRE', N'VE_SOM');

    SET @ExpectedWage = ROUND((@LuongCoBan / CAST(22 AS DECIMAL(18,2))) * @ExpectedDays, 2);
    SET @ExpectedNet = @ExpectedWage + @ExpectedAllowance - @ExpectedDeduction;

    IF NOT EXISTS
    (
        SELECT 1
        FROM dbo.BANGLUONG AS bl
        INNER JOIN dbo.CHITIETBANGLUONG AS ct ON ct.MaBangLuong = bl.MaBangLuong
        WHERE bl.MaBangLuong = @MaBangLuong
          AND bl.TrangThai = 'CHUA_CHOT'
          AND bl.NgayCongChuan = 22
          AND ct.MaNV = @MaNV
          AND ct.NgayCongThucTe = 3
          AND ct.TienCong = @ExpectedWage
          AND ct.TongPhuCap = @ExpectedAllowance
          AND ct.TongKhauTru = @ExpectedDeduction
          AND ct.ThucNhan = @ExpectedNet
    )
        THROW 52027, N'Tinh lai sau force-error khong dung du lieu nguon/cong thuc.', 1;

    PRINT N'[TV4 E2E] PASS 5/10 - Tinh lai thanh cong sau rollback.';

    -- ------------------------------------------------------------------------
    -- 6. Chot bang luong. Viec khoa chi tiet o buoc 8 chi kiem thu phoi hop
    -- voi trigger hien co cua TV5; script nay khong thay doi module TV5.
    -- ------------------------------------------------------------------------
    EXEC dbo.sp_ChotBangLuong @MaBangLuong = @MaBangLuong;

    IF NOT EXISTS
    (
        SELECT 1
        FROM dbo.BANGLUONG
        WHERE MaBangLuong = @MaBangLuong
          AND TrangThai = 'DA_CHOT'
          AND NgayChot IS NOT NULL
    )
        THROW 52028, N'sp_ChotBangLuong khong chot dung ky fixture.', 1;

    PRINT N'[TV4 E2E] PASS 6/10 - Chot bang luong thanh cong.';

    -- ------------------------------------------------------------------------
    -- 7. Chung minh header khong the UPDATE/DELETE sau khi chot.
    -- ------------------------------------------------------------------------

    DECLARE @LockedNgayCongChuan INT;
    DECLARE @BangLuongUpdateBlocked BIT = 0;
    DECLARE @BangLuongDeleteBlocked BIT = 0;
    DECLARE @BangLuongUpdateErrorNumber INT = NULL;
    DECLARE @BangLuongDeleteErrorNumber INT = NULL;
    DECLARE @BangLuongUpdateErrorMessage NVARCHAR(2048) = NULL;
    DECLARE @BangLuongDeleteErrorMessage NVARCHAR(2048) = NULL;

    SELECT @LockedNgayCongChuan = NgayCongChuan
    FROM dbo.BANGLUONG
    WHERE MaBangLuong = @MaBangLuong;

    BEGIN TRY
        UPDATE dbo.BANGLUONG
        SET NgayCongChuan = NgayCongChuan + 1
        WHERE MaBangLuong = @MaBangLuong;
    END TRY
    BEGIN CATCH
        SET @BangLuongUpdateBlocked = 1;
        SET @BangLuongUpdateErrorNumber = ERROR_NUMBER();
        SET @BangLuongUpdateErrorMessage = ERROR_MESSAGE();
    END CATCH;

    IF @BangLuongUpdateBlocked = 0
        THROW 52029, N'UPDATE BANGLUONG da chot khong bi khoa.', 1;

    IF @BangLuongUpdateErrorNumber <> 50000
       OR CHARINDEX(N'Khong duoc sua hoac xoa bang luong da chot.', @BangLuongUpdateErrorMessage) = 0
        THROW 52045, N'UPDATE header loi vi ly do khac, khong chung minh duoc trigger TV4 da chan.', 1;

    IF NOT EXISTS
    (
        SELECT 1
        FROM dbo.BANGLUONG
        WHERE MaBangLuong = @MaBangLuong
          AND TrangThai = 'DA_CHOT'
          AND NgayCongChuan = @LockedNgayCongChuan
    )
        THROW 52030, N'UPDATE bi bao loi nhung header da bi thay doi.', 1;

    BEGIN TRY
        DELETE FROM dbo.BANGLUONG
        WHERE MaBangLuong = @MaBangLuong;
    END TRY
    BEGIN CATCH
        SET @BangLuongDeleteBlocked = 1;
        SET @BangLuongDeleteErrorNumber = ERROR_NUMBER();
        SET @BangLuongDeleteErrorMessage = ERROR_MESSAGE();
    END CATCH;

    IF @BangLuongDeleteBlocked = 0
        THROW 52031, N'DELETE BANGLUONG da chot khong bi khoa.', 1;

    IF @BangLuongDeleteErrorNumber <> 50000
       OR CHARINDEX(N'Khong duoc sua hoac xoa bang luong da chot.', @BangLuongDeleteErrorMessage) = 0
        THROW 52046, N'DELETE header loi vi ly do khac, khong chung minh duoc trigger TV4 da chan.', 1;

    IF NOT EXISTS (SELECT 1 FROM dbo.BANGLUONG WHERE MaBangLuong = @MaBangLuong AND TrangThai = 'DA_CHOT')
        THROW 52032, N'DELETE bi bao loi nhung header da bi xoa.', 1;

    PRINT N'[TV4 E2E] PASS 7/10 - UPDATE/DELETE BANGLUONG da chot deu bi khoa.';

    -- ------------------------------------------------------------------------
    -- 8. Chung minh chi tiet cua ky da chot khong the UPDATE/DELETE.
    -- ------------------------------------------------------------------------
    DECLARE @LockedDetailId INT;
    DECLARE @LockedDetailNet DECIMAL(18,2);
    DECLARE @DetailUpdateBlocked BIT = 0;
    DECLARE @DetailDeleteBlocked BIT = 0;
    DECLARE @DetailUpdateErrorNumber INT = NULL;
    DECLARE @DetailDeleteErrorNumber INT = NULL;
    DECLARE @DetailUpdateErrorMessage NVARCHAR(2048) = NULL;
    DECLARE @DetailDeleteErrorMessage NVARCHAR(2048) = NULL;

    SELECT
        @LockedDetailId = MaChiTiet,
        @LockedDetailNet = ThucNhan
    FROM dbo.CHITIETBANGLUONG
    WHERE MaBangLuong = @MaBangLuong AND MaNV = @MaNV;

    IF @LockedDetailId IS NULL
        THROW 52033, N'Khong tim thay chi tiet fixture de test khoa.', 1;

    BEGIN TRY
        UPDATE dbo.CHITIETBANGLUONG
        SET ThucNhan = ThucNhan + 1
        WHERE MaChiTiet = @LockedDetailId;
    END TRY
    BEGIN CATCH
        SET @DetailUpdateBlocked = 1;
        SET @DetailUpdateErrorNumber = ERROR_NUMBER();
        SET @DetailUpdateErrorMessage = ERROR_MESSAGE();
    END CATCH;

    IF @DetailUpdateBlocked = 0
        THROW 52034, N'UPDATE CHITIETBANGLUONG cua ky da chot khong bi khoa.', 1;

    IF @DetailUpdateErrorNumber <> 50000
       OR CHARINDEX(N'Không được phép sửa hoặc xóa chi tiết bảng lương đã chốt', @DetailUpdateErrorMessage) = 0
        THROW 52047, N'UPDATE chi tiet loi vi ly do khac, khong chung minh duoc trigger TV5 da chan.', 1;

    IF NOT EXISTS
    (
        SELECT 1
        FROM dbo.CHITIETBANGLUONG
        WHERE MaChiTiet = @LockedDetailId
          AND MaBangLuong = @MaBangLuong
          AND ThucNhan = @LockedDetailNet
    )
        THROW 52035, N'UPDATE chi tiet bi bao loi nhung du lieu da bi thay doi.', 1;

    BEGIN TRY
        DELETE FROM dbo.CHITIETBANGLUONG
        WHERE MaChiTiet = @LockedDetailId;
    END TRY
    BEGIN CATCH
        SET @DetailDeleteBlocked = 1;
        SET @DetailDeleteErrorNumber = ERROR_NUMBER();
        SET @DetailDeleteErrorMessage = ERROR_MESSAGE();
    END CATCH;

    IF @DetailDeleteBlocked = 0
        THROW 52036, N'DELETE CHITIETBANGLUONG cua ky da chot khong bi khoa.', 1;

    IF @DetailDeleteErrorNumber <> 50000
       OR CHARINDEX(N'Không được phép sửa hoặc xóa chi tiết bảng lương đã chốt', @DetailDeleteErrorMessage) = 0
        THROW 52048, N'DELETE chi tiet loi vi ly do khac, khong chung minh duoc trigger TV5 da chan.', 1;

    IF NOT EXISTS
    (
        SELECT 1
        FROM dbo.CHITIETBANGLUONG
        WHERE MaChiTiet = @LockedDetailId
          AND MaBangLuong = @MaBangLuong
          AND ThucNhan = @LockedDetailNet
    )
        THROW 52037, N'DELETE chi tiet bi bao loi nhung dong da bi xoa/thay doi.', 1;

    PRINT N'[TV4 E2E] PASS 8/10 - UPDATE/DELETE CHITIETBANGLUONG da chot deu bi khoa.';

    -- ------------------------------------------------------------------------
    -- 9. Mo lai bang luong qua procedure sau khi da chung minh cac khoa.
    -- ------------------------------------------------------------------------
    EXEC dbo.sp_HuyChotBangLuong @MaBangLuong = @MaBangLuong;

    IF NOT EXISTS
    (
        SELECT 1
        FROM dbo.BANGLUONG
        WHERE MaBangLuong = @MaBangLuong
          AND TrangThai = 'CHUA_CHOT'
          AND NgayChot IS NULL
    )
        THROW 52042, N'sp_HuyChotBangLuong khong chuyen dung DA_CHOT -> CHUA_CHOT.', 1;

    IF NOT EXISTS
    (
        SELECT 1
        FROM dbo.CHITIETBANGLUONG
        WHERE MaChiTiet = @LockedDetailId
          AND MaBangLuong = @MaBangLuong
          AND ThucNhan = @LockedDetailNet
    )
        THROW 52043, N'Mo lai bang luong da lam thay doi/mat chi tiet.', 1;

    PRINT N'[TV4 E2E] PASS 9/10 - sp_HuyChotBangLuong mo lai dung va giu nguyen chi tiet.';

    SET @TestCompleted = 1;
END TRY
BEGIN CATCH
    SET @TestError = LEFT
    (
        CONCAT
        (
            N'Test error ', ERROR_NUMBER(),
            N' (line ', ERROR_LINE(), N'): ',
            ERROR_MESSAGE()
        ),
        2048
    );
END CATCH;

-- ============================================================================
-- FINALIZER/CLEANUP - chay ca khi test PASS lan test FAIL.
-- ============================================================================
BEGIN TRY
    IF @@TRANCOUNT > @InitialTranCount
        ROLLBACK TRANSACTION;

    EXEC sys.sp_set_session_context
        @key = N'TV4_E2E_FORCE_DETAIL_ERROR',
        @value = NULL;

    IF OBJECT_ID(N'dbo.trg_TV4_E2E_ForceChiTietInsertError', N'TR') IS NOT NULL
        EXEC sys.sp_executesql
            N'DROP TRIGGER dbo.trg_TV4_E2E_ForceChiTietInsertError;';

    -- Neu OUTPUT parameter chua duoc gan nhung procedure da tao chi tiet,
    -- chi tim header qua chinh MaNV fixture (khong lay header cua du lieu san co).
    IF @MaBangLuong IS NULL AND @MaNV IS NOT NULL
    BEGIN
        SELECT TOP (1) @MaBangLuong = ct.MaBangLuong
        FROM dbo.CHITIETBANGLUONG AS ct
        INNER JOIN dbo.BANGLUONG AS bl ON bl.MaBangLuong = ct.MaBangLuong
        WHERE ct.MaNV = @MaNV
          AND bl.Thang = @Thang
          AND bl.Nam = @Nam;
    END;

    BEGIN TRANSACTION;

    IF @MaBangLuong IS NOT NULL
       AND EXISTS
           (
               SELECT 1
               FROM dbo.BANGLUONG
               WHERE MaBangLuong = @MaBangLuong
                 AND Thang = @Thang
                 AND Nam = @Nam
           )
    BEGIN
        -- Trigger nghiep vu chi cho phep dung transition mo lai nay. Sau khi
        -- CHUA_CHOT, DELETE chi tiet/header fixture se duoc phep binh thuong.
        UPDATE dbo.BANGLUONG
        SET TrangThai = 'CHUA_CHOT',
            NgayChot = NULL
        WHERE MaBangLuong = @MaBangLuong
          AND Thang = @Thang
          AND Nam = @Nam;

        DELETE FROM dbo.CHITIETBANGLUONG
        WHERE MaBangLuong = @MaBangLuong;

        DELETE FROM dbo.BANGLUONG
        WHERE MaBangLuong = @MaBangLuong
          AND Thang = @Thang
          AND Nam = @Nam;
    END;

    IF @MaNV IS NOT NULL
    BEGIN
        DELETE FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV;
        DELETE FROM dbo.KHAUTRUNHANVIEN WHERE MaNV = @MaNV;
        DELETE FROM dbo.CHAMCONG WHERE MaNV = @MaNV;

        IF OBJECT_ID(N'dbo.TAIKHOAN', N'U') IS NOT NULL
            DELETE FROM dbo.TAIKHOAN WHERE MaNV = @MaNV;

        DELETE FROM dbo.NHANVIEN WHERE MaNV = @MaNV;
    END;

    IF @MaCV IS NOT NULL
        DELETE FROM dbo.CHUCVU WHERE MaCV = @MaCV;

    IF @MaPB IS NOT NULL
        DELETE FROM dbo.PHONGBAN WHERE MaPB = @MaPB;

    COMMIT TRANSACTION;

    IF @MaBangLuong IS NOT NULL
       AND EXISTS (SELECT 1 FROM dbo.BANGLUONG WHERE MaBangLuong = @MaBangLuong AND Thang = @Thang AND Nam = @Nam)
        THROW 52080, N'Cleanup con sot header bang luong fixture.', 1;

    IF @MaNV IS NOT NULL
       AND
       (
           EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE MaNV = @MaNV)
           OR EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV)
           OR EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV)
           OR EXISTS (SELECT 1 FROM dbo.KHAUTRUNHANVIEN WHERE MaNV = @MaNV)
           OR EXISTS (SELECT 1 FROM dbo.CHITIETBANGLUONG WHERE MaNV = @MaNV)
       )
        THROW 52081, N'Cleanup con sot du lieu cua nhan vien fixture.', 1;

    IF OBJECT_ID(N'tempdb..#TV4_E2E_DetailBeforeFailure', N'U') IS NOT NULL
        DROP TABLE #TV4_E2E_DetailBeforeFailure;

    IF OBJECT_ID(N'tempdb..#TV4_E2E_HeaderBeforeFailure', N'U') IS NOT NULL
        DROP TABLE #TV4_E2E_HeaderBeforeFailure;
END TRY
BEGIN CATCH
    SET @CleanupError = LEFT
    (
        CONCAT
        (
            N'Cleanup error ', ERROR_NUMBER(),
            N' (line ', ERROR_LINE(), N'): ',
            ERROR_MESSAGE()
        ),
        2048
    );

    IF @@TRANCOUNT > @InitialTranCount
        ROLLBACK TRANSACTION;
END CATCH;

-- Failsafe rieng: du cleanup phia tren loi o dau, trigger force-error van phai
-- duoc drop.
BEGIN TRY
    EXEC sys.sp_set_session_context
        @key = N'TV4_E2E_FORCE_DETAIL_ERROR',
        @value = NULL;

    IF OBJECT_ID(N'dbo.trg_TV4_E2E_ForceChiTietInsertError', N'TR') IS NOT NULL
        EXEC sys.sp_executesql
            N'DROP TRIGGER dbo.trg_TV4_E2E_ForceChiTietInsertError;';
END TRY
BEGIN CATCH
    SET @FinalizerError = LEFT
    (
        CONCAT
        (
            N'Finalizer error ', ERROR_NUMBER(),
            N' (line ', ERROR_LINE(), N'): ',
            ERROR_MESSAGE()
        ),
        2048
    );
END CATCH;

IF @AppLockAcquired = 1
BEGIN
    BEGIN TRY
        EXEC sys.sp_releaseapplock
            @Resource = N'TV4_Payroll_E2E',
            @LockOwner = 'Session';
    END TRY
    BEGIN CATCH
        IF @FinalizerError IS NULL
            SET @FinalizerError = LEFT(CONCAT(N'Khong release duoc application lock: ', ERROR_MESSAGE()), 2048);
    END CATCH;
END;

IF @CleanupError IS NOT NULL
    SET @TestError = LEFT
    (
        CASE
            WHEN @TestError IS NULL THEN @CleanupError
            ELSE CONCAT(@TestError, N' | ', @CleanupError)
        END,
        2048
    );

IF @FinalizerError IS NOT NULL
    SET @TestError = LEFT
    (
        CASE
            WHEN @TestError IS NULL THEN @FinalizerError
            ELSE CONCAT(@TestError, N' | ', @FinalizerError)
        END,
        2048
    );

IF @TestError IS NOT NULL
    THROW 52999, @TestError, 1;

IF @TestCompleted = 0
    THROW 52998, N'TV4 E2E ket thuc ma khong dat co hoan thanh.', 1;

IF OBJECT_ID(N'dbo.trg_TV4_E2E_ForceChiTietInsertError', N'TR') IS NOT NULL
    THROW 52997, N'Invariant cleanup: trigger force-error van con ton tai.', 1;

PRINT N'[TV4 E2E] PASS 10/10 - Cleanup sach; trigger force-error da duoc drop.';
PRINT N'[TV4 E2E] ALL TESTS PASSED.';

SELECT
    CAST(N'PASS' AS NVARCHAR(10)) AS KetQua,
    @Thang AS ThangDaTest,
    @Nam AS NamDaTest,
    @MaBangLuong AS MaBangLuongDaTest,
    CAST(N'Fixture da duoc cleanup' AS NVARCHAR(100)) AS Cleanup;
