-- ============================================================================
-- TV4 - BENCHMARK INDEX KHAU TRU / TRUY VAN NGUON TINH LUONG
-- Tac gia : Nguyen Quang Vinh (TV4 - MSSV 24110385)
-- Muc tieu:
--   1. Xac minh cau truc IX_KHAUTRU_MaNV_ThangNam.
--   2. So sanh cung mot truy van nguon khi ep clustered scan va ep index seek.
--   3. Thu Actual Execution Plan, SET STATISTICS IO va SET STATISTICS TIME.
--   4. Tao fixture trong transaction va ROLLBACK ke ca khi benchmark bi loi.
--
-- Cach doc ket qua trong SSMS:
--   - Tab Messages: so sanh "logical reads" va "CPU time / elapsed time".
--   - Result "Microsoft SQL Server XML Showplan": mo plan va doi chieu
--       FORCED_CLUSTERED_SCAN -> Clustered Index Scan (PK_KHAUTRUNHANVIEN)
--       FORCED_COVERING_SEEK  -> Index Seek (IX_KHAUTRU_MaNV_ThangNam)
--   - Khong can DBCC DROPCLEANBUFFERS/FREEPROCCACHE; script khong lam anh huong
--     cache chung cua server.
-- ============================================================================

-- Chay tren database dang duoc connection/SSMS chon. Preflight se dung truoc
-- moi mutation neu database dich khong co day du object bat buoc.
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

DECLARE @IndexName       SYSNAME        = N'IX_KHAUTRU_MaNV_ThangNam';
DECLARE @FixtureMarker   NVARCHAR(255)  = N'TV4_PAYROLL_BENCHMARK_24110385';
DECLARE @FixtureRows     INT            = 30000;
DECLARE @RowsCanTim      INT            = 128;
DECLARE @FixtureStartId  INT            = -2100000000;
DECLARE @AllowanceStartId INT           = -2060000000;
DECLARE @AttendanceStartId INT          = -2020000000;
DECLARE @MaPB            INT            = -2100000000;
DECLARE @MaCV            INT            = -2100000000;
DECLARE @MaNV            INT            = -2100000000;
DECLARE @Thang           INT            = 12;
DECLARE @Nam             INT            = 2099;
DECLARE @AttendancePeriodStart DATE;
DECLARE @AttendancePeriodEnd   DATE;
DECLARE @AttendanceFixtureStart DATE    = CONVERT(DATE, '20200101', 112);
DECLARE @AttendanceLastDate DATE;
DECLARE @AttendanceRows INT;
DECLARE @AttendanceTargetRows INT;
DECLARE @DeductionIdentityBefore NUMERIC(38, 0);
DECLARE @DeductionIdentityAfter  NUMERIC(38, 0);
DECLARE @AllowanceIdentityBefore NUMERIC(38, 0);
DECLARE @AllowanceIdentityAfter  NUMERIC(38, 0);
DECLARE @AttendanceIdentityBefore NUMERIC(38, 0);
DECLARE @AttendanceIdentityAfter  NUMERIC(38, 0);
DECLARE @IdentityInsertTable SYSNAME    = NULL;
DECLARE @ScanCount       BIGINT;
DECLARE @SeekCount       BIGINT;
DECLARE @ScanTotal       DECIMAL(38, 2);
DECLARE @SeekTotal       DECIMAL(38, 2);
DECLARE @AllowanceCount  BIGINT;
DECLARE @AllowanceTotal  DECIMAL(38, 2);
DECLARE @AttendanceCount BIGINT;

SET @AttendanceRows = DATEDIFF(DAY, @AttendanceFixtureStart, CONVERT(DATE, GETDATE())) + 1;
IF @AttendanceRows > 3000 SET @AttendanceRows = 3000;
SET @AttendanceLastDate = DATEADD(DAY, @AttendanceRows - 1, @AttendanceFixtureStart);
SET @AttendancePeriodStart = DATEFROMPARTS(YEAR(@AttendanceLastDate), MONTH(@AttendanceLastDate), 1);
SET @AttendancePeriodEnd = DATEADD(DAY, 1, @AttendanceLastDate);
SET @AttendanceTargetRows = DATEDIFF(DAY, @AttendancePeriodStart, @AttendancePeriodEnd);

-- --------------------------------------------------------------------------
-- 1. PRE-FLIGHT: khong tu tao/sua index san pham; dung ngay neu module chua du.
-- --------------------------------------------------------------------------
IF @@TRANCOUNT <> 0
    THROW 51020, N'Benchmark phai bat dau khi @@TRANCOUNT = 0 de khong rollback transaction cua caller.', 1;

IF OBJECT_ID(N'dbo.PHONGBAN', N'U') IS NULL
   OR OBJECT_ID(N'dbo.CHUCVU', N'U') IS NULL
    THROW 51021, N'Thieu dbo.PHONGBAN/CHUCVU. Hay chay 01_Module_NhanSu_TV1.sql truoc.', 1;

IF OBJECT_ID(N'dbo.NHANVIEN', N'U') IS NULL
    THROW 51000, N'Thieu dbo.NHANVIEN. Hay chay 01_Module_NhanSu_TV1.sql truoc.', 1;

IF OBJECT_ID(N'dbo.CHAMCONG', N'U') IS NULL
   OR OBJECT_ID(N'dbo.PHUCAPNHANVIEN', N'U') IS NULL
   OR OBJECT_ID(N'dbo.KHAUTRUNHANVIEN', N'U') IS NULL
    THROW 51001, N'Thieu CHAMCONG/PHUCAPNHANVIEN/KHAUTRUNHANVIEN. Hay cai dat module 02 va 03.', 1;

IF @AttendanceRows <= 0 OR @AttendanceTargetRows <= 0
    THROW 51022, N'Ngay may chu khong tao duoc fixture cham cong hop le.', 1;

IF NOT EXISTS
(
    SELECT 1
    FROM sys.indexes
    WHERE object_id = OBJECT_ID(N'dbo.KHAUTRUNHANVIEN')
      AND name = @IndexName
      AND is_disabled = 0
)
    THROW 51002, N'Thieu index IX_KHAUTRU_MaNV_ThangNam hoac index dang bi disable. Hay chay 04_Module_TinhLuong_TV4.sql.', 1;

-- Chu ky mong doi: key (MaNV, Thang, Nam), INCLUDE (SoTien).
IF NOT EXISTS
(
    SELECT 1
    FROM sys.index_columns AS ic
    INNER JOIN sys.columns AS c
        ON c.object_id = ic.object_id
       AND c.column_id = ic.column_id
    WHERE ic.object_id = OBJECT_ID(N'dbo.KHAUTRUNHANVIEN')
      AND ic.index_id = INDEXPROPERTY(OBJECT_ID(N'dbo.KHAUTRUNHANVIEN'), @IndexName, 'IndexId')
      AND c.name = N'MaNV'
      AND ic.key_ordinal = 1
      AND ic.is_included_column = 0
)
OR NOT EXISTS
(
    SELECT 1
    FROM sys.index_columns AS ic
    INNER JOIN sys.columns AS c
        ON c.object_id = ic.object_id
       AND c.column_id = ic.column_id
    WHERE ic.object_id = OBJECT_ID(N'dbo.KHAUTRUNHANVIEN')
      AND ic.index_id = INDEXPROPERTY(OBJECT_ID(N'dbo.KHAUTRUNHANVIEN'), @IndexName, 'IndexId')
      AND c.name = N'Thang'
      AND ic.key_ordinal = 2
      AND ic.is_included_column = 0
)
OR NOT EXISTS
(
    SELECT 1
    FROM sys.index_columns AS ic
    INNER JOIN sys.columns AS c
        ON c.object_id = ic.object_id
       AND c.column_id = ic.column_id
    WHERE ic.object_id = OBJECT_ID(N'dbo.KHAUTRUNHANVIEN')
      AND ic.index_id = INDEXPROPERTY(OBJECT_ID(N'dbo.KHAUTRUNHANVIEN'), @IndexName, 'IndexId')
      AND c.name = N'Nam'
      AND ic.key_ordinal = 3
      AND ic.is_included_column = 0
)
OR NOT EXISTS
(
    SELECT 1
    FROM sys.index_columns AS ic
    INNER JOIN sys.columns AS c
        ON c.object_id = ic.object_id
       AND c.column_id = ic.column_id
    WHERE ic.object_id = OBJECT_ID(N'dbo.KHAUTRUNHANVIEN')
      AND ic.index_id = INDEXPROPERTY(OBJECT_ID(N'dbo.KHAUTRUNHANVIEN'), @IndexName, 'IndexId')
      AND c.name = N'SoTien'
      AND ic.is_included_column = 1
)
    THROW 51003, N'Index ton tai nhung khong dung chu ky (MaNV, Thang, Nam) INCLUDE (SoTien).', 1;

-- Fixture dung key am rieng, khong phu thuoc database da seed NHANVIEN hay chua.
IF EXISTS (SELECT 1 FROM dbo.PHONGBAN WHERE MaPB = @MaPB)
   OR EXISTS (SELECT 1 FROM dbo.CHUCVU WHERE MaCV = @MaCV)
   OR EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE MaNV = @MaNV)
    THROW 51004, N'Key am danh cho fixture PB/CV/NV dang duoc su dung. Script dung de khong ghi de du lieu.', 1;

IF EXISTS
(
    SELECT 1
    FROM dbo.KHAUTRUNHANVIEN
    WHERE MaKTNV BETWEEN @FixtureStartId AND @FixtureStartId + @FixtureRows - 1
)
    THROW 51006, N'Dai MaKTNV am danh cho fixture dang duoc su dung. Script dung de khong ghi de du lieu.', 1;

IF EXISTS
(
    SELECT 1
    FROM dbo.PHUCAPNHANVIEN
    WHERE MaPCNV BETWEEN @AllowanceStartId AND @AllowanceStartId + @FixtureRows - 1
)
    THROW 51013, N'Dai MaPCNV am danh cho fixture dang duoc su dung.', 1;

IF EXISTS
(
    SELECT 1
    FROM dbo.CHAMCONG
    WHERE MaChamCong BETWEEN @AttendanceStartId AND @AttendanceStartId + @AttendanceRows - 1
)
    THROW 51014, N'Dai MaChamCong am danh cho fixture dang duoc su dung.', 1;

-- Metadata de chup minh chung cau truc index.
SELECT
    i.name AS IndexName,
    i.type_desc AS IndexType,
    i.is_unique,
    i.is_disabled,
    i.has_filter,
    i.fill_factor,
    c.name AS ColumnName,
    ic.key_ordinal,
    ic.is_included_column,
    ic.is_descending_key
FROM sys.indexes AS i
INNER JOIN sys.index_columns AS ic
    ON ic.object_id = i.object_id
   AND ic.index_id = i.index_id
INNER JOIN sys.columns AS c
    ON c.object_id = ic.object_id
   AND c.column_id = ic.column_id
WHERE i.object_id = OBJECT_ID(N'dbo.KHAUTRUNHANVIEN')
  AND i.name = @IndexName
ORDER BY ic.is_included_column, ic.key_ordinal, ic.index_column_id;

SET @DeductionIdentityBefore = IDENT_CURRENT(N'dbo.KHAUTRUNHANVIEN');
SET @AllowanceIdentityBefore = IDENT_CURRENT(N'dbo.PHUCAPNHANVIEN');
SET @AttendanceIdentityBefore = IDENT_CURRENT(N'dbo.CHAMCONG');

BEGIN TRY
    -- ----------------------------------------------------------------------
    -- 2. FIXTURE AN TOAN
    -- Tat ca row benchmark nam trong transaction va luon ROLLBACK.
    -- Dung MaKTNV am voi IDENTITY_INSERT de khong lam tang identity hien tai.
    -- ----------------------------------------------------------------------
    BEGIN TRANSACTION;

    SET IDENTITY_INSERT dbo.PHONGBAN ON;
    SET @IdentityInsertTable = N'dbo.PHONGBAN';
    INSERT INTO dbo.PHONGBAN (MaPB, TenPB, SoDienThoai, TrangThai)
    VALUES (@MaPB, N'TV4 payroll benchmark fixture', NULL, N'HOAT_DONG');
    SET IDENTITY_INSERT dbo.PHONGBAN OFF;
    SET @IdentityInsertTable = NULL;

    SET IDENTITY_INSERT dbo.CHUCVU ON;
    SET @IdentityInsertTable = N'dbo.CHUCVU';
    INSERT INTO dbo.CHUCVU (MaCV, TenCV, PhuCapChucVu)
    VALUES (@MaCV, N'TV4 payroll benchmark fixture', 0);
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
        @MaNV, N'TV4 payroll benchmark fixture', '1980-01-01', N'Nam',
        '999999999996', N'Fixture se rollback', '0999999996',
        'tv4.payroll.benchmark@example.invalid', '2020-01-01', 10000000,
        @MaPB, @MaCV, N'DANG_LAM_VIEC'
    );
    SET IDENTITY_INSERT dbo.NHANVIEN OFF;
    SET @IdentityInsertTable = NULL;

    -- Nguon cham cong: toi da 3.000 ngay lien tiep, moi ngay mot dong hop le.
    SET IDENTITY_INSERT dbo.CHAMCONG ON;
    SET @IdentityInsertTable = N'dbo.CHAMCONG';

    ;WITH N AS
    (
        SELECT TOP (@AttendanceRows)
            CONVERT(INT, ROW_NUMBER() OVER (ORDER BY (SELECT NULL))) AS rn
        FROM sys.all_objects AS a
        CROSS JOIN sys.all_objects AS b
    )
    INSERT INTO dbo.CHAMCONG
    (
        MaChamCong, MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu
    )
    SELECT
        @AttendanceStartId + n.rn - 1,
        @MaNV,
        DATEADD(DAY, n.rn - 1, @AttendanceFixtureStart),
        CONVERT(TIME(0), '08:00'),
        CONVERT(TIME(0), '17:00'),
        N'CO_MAT',
        @FixtureMarker
    FROM N AS n;

    IF @@ROWCOUNT <> @AttendanceRows
        THROW 51015, N'Khong tao du so dong fixture cham cong.', 1;

    SET IDENTITY_INSERT dbo.CHAMCONG OFF;
    SET @IdentityInsertTable = NULL;

    -- Nguon phu cap: cung quy mo voi khau tru de do truy van SUM thuc te.
    SET IDENTITY_INSERT dbo.PHUCAPNHANVIEN ON;
    SET @IdentityInsertTable = N'dbo.PHUCAPNHANVIEN';

    ;WITH N AS
    (
        SELECT TOP (@FixtureRows)
            CONVERT(INT, ROW_NUMBER() OVER (ORDER BY (SELECT NULL))) AS rn
        FROM sys.all_objects AS a
        CROSS JOIN sys.all_objects AS b
    )
    INSERT INTO dbo.PHUCAPNHANVIEN
    (
        MaPCNV, MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu
    )
    SELECT
        @AllowanceStartId + n.rn - 1,
        @MaNV,
        CASE WHEN n.rn <= @RowsCanTim THEN @Thang
             ELSE ((n.rn - @RowsCanTim - 1) % 11) + 1 END,
        CASE WHEN n.rn <= @RowsCanTim THEN @Nam
             ELSE 2020 + ((n.rn - @RowsCanTim - 1) % 8) END,
        N'Fixture benchmark TV4',
        CONVERT(DECIMAL(18, 2), 2000 + (n.rn % 100)),
        CONVERT(DATE, '2026-09-01'),
        @FixtureMarker
    FROM N AS n;

    IF @@ROWCOUNT <> @FixtureRows
        THROW 51016, N'Khong tao du so dong fixture phu cap.', 1;

    SET IDENTITY_INSERT dbo.PHUCAPNHANVIEN OFF;
    SET @IdentityInsertTable = NULL;

    SET IDENTITY_INSERT dbo.KHAUTRUNHANVIEN ON;
    SET @IdentityInsertTable = N'dbo.KHAUTRUNHANVIEN';

    ;WITH N AS
    (
        SELECT TOP (@FixtureRows)
            CONVERT(INT, ROW_NUMBER() OVER (ORDER BY (SELECT NULL))) AS rn
        FROM sys.all_objects AS a
        CROSS JOIN sys.all_objects AS b
    )
    INSERT INTO dbo.KHAUTRUNHANVIEN
    (
        MaKTNV,
        MaNV,
        Thang,
        Nam,
        TenKhauTru,
        SoTien,
        NgayGhiNhan,
        LyDo
    )
    SELECT
        @FixtureStartId + n.rn - 1,
        @MaNV,
        CASE WHEN n.rn <= @RowsCanTim THEN @Thang
             ELSE ((n.rn - @RowsCanTim - 1) % 11) + 1 END,
        CASE WHEN n.rn <= @RowsCanTim THEN @Nam
             ELSE 2020 + ((n.rn - @RowsCanTim - 1) % 8) END,
        N'Fixture benchmark TV4',
        CONVERT(DECIMAL(18, 2), 1000 + (n.rn % 100)),
        CONVERT(DATE, '2026-09-01'),
        @FixtureMarker
    FROM N AS n;

    IF @@ROWCOUNT <> @FixtureRows
        THROW 51007, N'Khong tao du so dong fixture benchmark.', 1;

    SET IDENTITY_INSERT dbo.KHAUTRUNHANVIEN OFF;
    SET @IdentityInsertTable = NULL;

    SELECT
        @FixtureRows AS SoDongMoiNguonPhuCapKhauTru,
        @AttendanceRows AS SoDongChamCong,
        @RowsCanTim AS SoDongPhuCapKhauTruKyCanTim,
        @AttendanceTargetRows AS SoDongChamCongKyCanTim,
        @MaNV AS MaNVCanTim,
        @Thang AS ThangCanTim,
        @Nam AS NamCanTim,
        @AttendancePeriodStart AS DauKyChamCong,
        @AttendancePeriodEnd AS CuoiKyChamCongExclusive;

    -- ----------------------------------------------------------------------
    -- 3. BENCHMARK: hai truy van tuong duong, khac access path.
    -- STATISTICS XML tra Actual Execution Plan ma khong can bat Ctrl+M.
    -- Chay lan luot tren cung cache de phep so sanh de lap lai va it xam lan.
    -- ----------------------------------------------------------------------
    SET STATISTICS IO ON;
    SET STATISTICS TIME ON;
    SET STATISTICS XML ON;

    -- Truy van validation bat buoc cua procedure: co cham cong trong ky hay khong.
    EXEC sys.sp_executesql
        N'
        SELECT
            N''PAYROLL_ATTENDANCE_PERIOD_VALIDATION'' AS BenchmarkCase,
            COUNT_BIG(*) AS SoDongChamCong
        FROM dbo.CHAMCONG AS cc
        WHERE cc.NgayChamCong >= @PeriodStartIn
          AND cc.NgayChamCong < @PeriodEndIn
        OPTION (RECOMPILE);',
        N'@PeriodStartIn DATE, @PeriodEndIn DATE',
        @PeriodStartIn = @AttendancePeriodStart,
        @PeriodEndIn = @AttendancePeriodEnd;

    -- Truy van ngay cong thuc te theo nhan vien, dung khoang ngay nua mo.
    EXEC sys.sp_executesql
        N'
        SELECT
            N''PAYROLL_ATTENDANCE_EMPLOYEE_RANGE'' AS BenchmarkCase,
            COUNT_BIG(*) AS NgayCongThucTe
        FROM dbo.CHAMCONG AS cc
        WHERE cc.MaNV = @MaNVIn
          AND cc.NgayChamCong >= @PeriodStartIn
          AND cc.NgayChamCong < @PeriodEndIn
          AND cc.TrangThai IN (N''CO_MAT'', N''DI_TRE'', N''VE_SOM'')
        OPTION (RECOMPILE);',
        N'@MaNVIn INT, @PeriodStartIn DATE, @PeriodEndIn DATE',
        @MaNVIn = @MaNV,
        @PeriodStartIn = @AttendancePeriodStart,
        @PeriodEndIn = @AttendancePeriodEnd;

    -- Truy van phu cap thuc te trong procedure khi function tich hop khong co.
    EXEC sys.sp_executesql
        N'
        SELECT
            N''PAYROLL_ALLOWANCE_SOURCE_QUERY'' AS BenchmarkCase,
            COUNT_BIG(*) AS SoDong,
            ISNULL(SUM(pc.SoTien), 0) AS TongPhuCap
        FROM dbo.PHUCAPNHANVIEN AS pc
        WHERE pc.MaNV = @MaNVIn
          AND pc.Thang = @ThangIn
          AND pc.Nam = @NamIn
        OPTION (RECOMPILE);',
        N'@MaNVIn INT, @ThangIn INT, @NamIn INT',
        @MaNVIn = @MaNV,
        @ThangIn = @Thang,
        @NamIn = @Nam;

    EXEC sys.sp_executesql
        N'
        SELECT
            N''FORCED_CLUSTERED_SCAN'' AS BenchmarkCase,
            COUNT_BIG(*) AS SoDong,
            ISNULL(SUM(k.SoTien), 0) AS TongKhauTru
        FROM dbo.KHAUTRUNHANVIEN AS k WITH (INDEX(0), FORCESCAN)
        WHERE k.MaNV = @MaNVIn
          AND k.Thang = @ThangIn
          AND k.Nam = @NamIn
        OPTION (RECOMPILE);',
        N'@MaNVIn INT, @ThangIn INT, @NamIn INT',
        @MaNVIn = @MaNV,
        @ThangIn = @Thang,
        @NamIn = @Nam;

    EXEC sys.sp_executesql
        N'
        SELECT
            N''FORCED_COVERING_SEEK'' AS BenchmarkCase,
            COUNT_BIG(*) AS SoDong,
            ISNULL(SUM(k.SoTien), 0) AS TongKhauTru
        FROM dbo.KHAUTRUNHANVIEN AS k
             WITH (INDEX(IX_KHAUTRU_MaNV_ThangNam), FORCESEEK)
        WHERE k.MaNV = @MaNVIn
          AND k.Thang = @ThangIn
          AND k.Nam = @NamIn
        OPTION (RECOMPILE);',
        N'@MaNVIn INT, @ThangIn INT, @NamIn INT',
        @MaNVIn = @MaNV,
        @ThangIn = @Thang,
        @NamIn = @Nam;

    -- Truy van khau tru thuc te trong sp_TinhBangLuongThang (khong ep hint).
    -- Plan nay cho thay optimizer tu chon IX_KHAUTRU_MaNV_ThangNam.
    EXEC sys.sp_executesql
        N'
        SELECT
            N''PAYROLL_DEDUCTION_SOURCE_QUERY'' AS BenchmarkCase,
            ISNULL(SUM(k.SoTien), 0) AS TongKhauTru
        FROM dbo.KHAUTRUNHANVIEN AS k
        WHERE k.MaNV = @MaNVIn
          AND k.Thang = @ThangIn
          AND k.Nam = @NamIn
        OPTION (RECOMPILE);',
        N'@MaNVIn INT, @ThangIn INT, @NamIn INT',
        @MaNVIn = @MaNV,
        @ThangIn = @Thang,
        @NamIn = @Nam;

    SET STATISTICS XML OFF;
    SET STATISTICS TIME OFF;
    SET STATISTICS IO OFF;

    -- ----------------------------------------------------------------------
    -- 4. DOI CHIEU TINH DUNG: scan va seek phai tra cung tap du lieu/tong tien.
    -- ----------------------------------------------------------------------
    EXEC sys.sp_executesql
        N'
        SELECT
            @CountOut = COUNT_BIG(*),
            @TotalOut = ISNULL(SUM(k.SoTien), 0)
        FROM dbo.KHAUTRUNHANVIEN AS k WITH (INDEX(0), FORCESCAN)
        WHERE k.MaNV = @MaNVIn
          AND k.Thang = @ThangIn
          AND k.Nam = @NamIn;',
        N'@MaNVIn INT, @ThangIn INT, @NamIn INT,
          @CountOut BIGINT OUTPUT, @TotalOut DECIMAL(38,2) OUTPUT',
        @MaNVIn = @MaNV,
        @ThangIn = @Thang,
        @NamIn = @Nam,
        @CountOut = @ScanCount OUTPUT,
        @TotalOut = @ScanTotal OUTPUT;

    EXEC sys.sp_executesql
        N'
        SELECT
            @CountOut = COUNT_BIG(*),
            @TotalOut = ISNULL(SUM(k.SoTien), 0)
        FROM dbo.KHAUTRUNHANVIEN AS k
             WITH (INDEX(IX_KHAUTRU_MaNV_ThangNam), FORCESEEK)
        WHERE k.MaNV = @MaNVIn
          AND k.Thang = @ThangIn
          AND k.Nam = @NamIn;',
        N'@MaNVIn INT, @ThangIn INT, @NamIn INT,
          @CountOut BIGINT OUTPUT, @TotalOut DECIMAL(38,2) OUTPUT',
        @MaNVIn = @MaNV,
        @ThangIn = @Thang,
        @NamIn = @Nam,
        @CountOut = @SeekCount OUTPUT,
        @TotalOut = @SeekTotal OUTPUT;

    EXEC sys.sp_executesql
        N'
        SELECT @CountOut = COUNT_BIG(*)
        FROM dbo.CHAMCONG AS cc
        WHERE cc.MaNV = @MaNVIn
          AND cc.NgayChamCong >= @PeriodStartIn
          AND cc.NgayChamCong < @PeriodEndIn
          AND cc.TrangThai IN (N''CO_MAT'', N''DI_TRE'', N''VE_SOM'');',
        N'@MaNVIn INT, @PeriodStartIn DATE, @PeriodEndIn DATE, @CountOut BIGINT OUTPUT',
        @MaNVIn = @MaNV,
        @PeriodStartIn = @AttendancePeriodStart,
        @PeriodEndIn = @AttendancePeriodEnd,
        @CountOut = @AttendanceCount OUTPUT;

    EXEC sys.sp_executesql
        N'
        SELECT
            @CountOut = COUNT_BIG(*),
            @TotalOut = ISNULL(SUM(pc.SoTien), 0)
        FROM dbo.PHUCAPNHANVIEN AS pc
        WHERE pc.MaNV = @MaNVIn
          AND pc.Thang = @ThangIn
          AND pc.Nam = @NamIn;',
        N'@MaNVIn INT, @ThangIn INT, @NamIn INT,
          @CountOut BIGINT OUTPUT, @TotalOut DECIMAL(38,2) OUTPUT',
        @MaNVIn = @MaNV,
        @ThangIn = @Thang,
        @NamIn = @Nam,
        @CountOut = @AllowanceCount OUTPUT,
        @TotalOut = @AllowanceTotal OUTPUT;

    IF @ScanCount <> @SeekCount OR @ScanTotal <> @SeekTotal
        THROW 51008, N'Scan va seek tra ket qua khac nhau.', 1;

    IF @SeekCount <> @RowsCanTim
        THROW 51009, N'So dong ky can tim khong khop fixture mong doi.', 1;

    IF @AttendanceCount <> @AttendanceTargetRows
        THROW 51017, N'Truy van nguon cham cong khong tra dung so dong fixture.', 1;

    IF @AllowanceCount <> @RowsCanTim OR ISNULL(@AllowanceTotal, 0) <= 0
        THROW 51018, N'Truy van nguon phu cap khong tra dung fixture.', 1;

    SELECT
        @AttendanceCount AS AttendanceSourceRowCount,
        @AllowanceCount AS AllowanceSourceRowCount,
        @AllowanceTotal AS AllowanceSourceTotal,
        @ScanCount AS ScanRowCount,
        @SeekCount AS SeekRowCount,
        @ScanTotal AS ScanTotal,
        @SeekTotal AS SeekTotal,
        N'PASS - truy van nguon dung; scan va seek khau tru cho cung ket qua' AS KetLuan;

    -- ROLLBACK la cleanup chinh, khong COMMIT fixture vao CSDL.
    ROLLBACK TRANSACTION;

    SET @DeductionIdentityAfter = IDENT_CURRENT(N'dbo.KHAUTRUNHANVIEN');
    SET @AllowanceIdentityAfter = IDENT_CURRENT(N'dbo.PHUCAPNHANVIEN');
    SET @AttendanceIdentityAfter = IDENT_CURRENT(N'dbo.CHAMCONG');

    IF EXISTS
    (
        SELECT 1
        FROM dbo.KHAUTRUNHANVIEN
        WHERE LyDo = @FixtureMarker
           OR MaKTNV BETWEEN @FixtureStartId AND @FixtureStartId + @FixtureRows - 1
    )
        THROW 51010, N'Cleanup benchmark that bai: van con fixture sau ROLLBACK.', 1;

    IF EXISTS
    (
        SELECT 1
        FROM dbo.PHUCAPNHANVIEN
        WHERE GhiChu = @FixtureMarker
           OR MaPCNV BETWEEN @AllowanceStartId AND @AllowanceStartId + @FixtureRows - 1
    )
       OR EXISTS
    (
        SELECT 1
        FROM dbo.CHAMCONG
        WHERE GhiChu = @FixtureMarker
           OR MaChamCong BETWEEN @AttendanceStartId AND @AttendanceStartId + @AttendanceRows - 1
    )
        THROW 51019, N'Cleanup benchmark that bai: con fixture phu cap/cham cong.', 1;

    IF EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE MaNV = @MaNV)
       OR EXISTS (SELECT 1 FROM dbo.PHONGBAN WHERE MaPB = @MaPB)
       OR EXISTS (SELECT 1 FROM dbo.CHUCVU WHERE MaCV = @MaCV)
        THROW 51011, N'Cleanup benchmark that bai: van con fixture PB/CV/NV sau ROLLBACK.', 1;

    IF ISNULL(@DeductionIdentityAfter, -1) <> ISNULL(@DeductionIdentityBefore, -1)
       OR ISNULL(@AllowanceIdentityAfter, -1) <> ISNULL(@AllowanceIdentityBefore, -1)
       OR ISNULL(@AttendanceIdentityAfter, -1) <> ISNULL(@AttendanceIdentityBefore, -1)
        THROW 51012, N'Benchmark da lam thay doi identity hien hanh cua bang nguon.', 1;

    SELECT
        @DeductionIdentityBefore AS DeductionIdentityBefore,
        @DeductionIdentityAfter AS DeductionIdentityAfter,
        @AllowanceIdentityBefore AS AllowanceIdentityBefore,
        @AllowanceIdentityAfter AS AllowanceIdentityAfter,
        @AttendanceIdentityBefore AS AttendanceIdentityBefore,
        @AttendanceIdentityAfter AS AttendanceIdentityAfter,
        0 AS FixtureConLai,
        N'PASS - transaction da rollback, khong con du lieu benchmark' AS CleanupStatus;
END TRY
BEGIN CATCH
    -- SET options la session-scoped, nen luon tat trong nhanh loi.
    SET STATISTICS XML OFF;
    SET STATISTICS TIME OFF;
    SET STATISTICS IO OFF;

    IF @IdentityInsertTable IS NOT NULL
    BEGIN
        BEGIN TRY
            IF @IdentityInsertTable = N'dbo.PHONGBAN'
                SET IDENTITY_INSERT dbo.PHONGBAN OFF;
            ELSE IF @IdentityInsertTable = N'dbo.CHUCVU'
                SET IDENTITY_INSERT dbo.CHUCVU OFF;
            ELSE IF @IdentityInsertTable = N'dbo.NHANVIEN'
                SET IDENTITY_INSERT dbo.NHANVIEN OFF;
            ELSE IF @IdentityInsertTable = N'dbo.CHAMCONG'
                SET IDENTITY_INSERT dbo.CHAMCONG OFF;
            ELSE IF @IdentityInsertTable = N'dbo.PHUCAPNHANVIEN'
                SET IDENTITY_INSERT dbo.PHUCAPNHANVIEN OFF;
            ELSE IF @IdentityInsertTable = N'dbo.KHAUTRUNHANVIEN'
                SET IDENTITY_INSERT dbo.KHAUTRUNHANVIEN OFF;
            SET @IdentityInsertTable = NULL;
        END TRY
        BEGIN CATCH
            PRINT N'Canh bao: khong tat duoc IDENTITY_INSERT trong nhanh cleanup.';
        END CATCH;
    END;

    IF XACT_STATE() <> 0
        ROLLBACK TRANSACTION;

    DECLARE @FixtureConLai INT = 0;
    IF OBJECT_ID(N'dbo.KHAUTRUNHANVIEN', N'U') IS NOT NULL
    BEGIN
        SELECT @FixtureConLai = COUNT(*)
        FROM dbo.KHAUTRUNHANVIEN
        WHERE LyDo = @FixtureMarker
           OR MaKTNV BETWEEN @FixtureStartId AND @FixtureStartId + @FixtureRows - 1;

        SELECT @FixtureConLai = @FixtureConLai + COUNT(*)
        FROM dbo.PHUCAPNHANVIEN
        WHERE GhiChu = @FixtureMarker
           OR MaPCNV BETWEEN @AllowanceStartId AND @AllowanceStartId + @FixtureRows - 1;

        SELECT @FixtureConLai = @FixtureConLai + COUNT(*)
        FROM dbo.CHAMCONG
        WHERE GhiChu = @FixtureMarker
           OR MaChamCong BETWEEN @AttendanceStartId AND @AttendanceStartId + @AttendanceRows - 1;
    END;

    PRINT N'Benchmark gap loi; transaction da rollback. Fixture con lai = '
        + CONVERT(NVARCHAR(20), @FixtureConLai) + N'.';
    THROW;
END CATCH;
