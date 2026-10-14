-- TV5-02: controlled temp fixture reproducing the TV5 covering index columns.
-- This is a forced-access-path benchmark, not a production workload claim.
-- No server cache reset, persistent data writes, or product index changes.
SET NOCOUNT ON;
IF DB_NAME() NOT LIKE 'PRJ[_]Fix[_]QA[_]%'
    THROW 53520,N'Benchmark requires an isolated PRJ_Fix_QA database.',1;
IF OBJECT_ID('tempdb..#BenchmarkNhanVien') IS NOT NULL
    THROW 53521,N'Benchmark temp fixture already exists in this session.',1;
GO
SELECT CONVERT(INT,ROW_NUMBER() OVER (ORDER BY a.object_id,b.object_id)) AS MaNV,
    CONVERT(NVARCHAR(100),N'Nhan vien test '+CONVERT(NVARCHAR(10),ROW_NUMBER() OVER (ORDER BY a.object_id,b.object_id))) AS HoTen,
    CONVERT(INT,(ROW_NUMBER() OVER (ORDER BY a.object_id,b.object_id)%4)+1) AS MaPB,
    CONVERT(INT,(ROW_NUMBER() OVER (ORDER BY a.object_id,b.object_id)%3)+1) AS MaCV,
    CAST(10000000+(ROW_NUMBER() OVER (ORDER BY a.object_id,b.object_id)%20)*500000 AS DECIMAL(18,2)) AS LuongCoBan,
    CONVERT(NVARCHAR(20),N'DANG_LAM_VIEC') AS TrangThai
INTO #BenchmarkNhanVien
FROM sys.all_objects a CROSS JOIN sys.all_objects b
ORDER BY a.object_id,b.object_id OFFSET 0 ROWS FETCH NEXT 10000 ROWS ONLY;
CREATE UNIQUE CLUSTERED INDEX PK_Bench_MaNV ON #BenchmarkNhanVien(MaNV);
CREATE NONCLUSTERED INDEX IX_Bench_MaPB_MaCV ON #BenchmarkNhanVien(MaPB,MaCV)
    INCLUDE(HoTen,LuongCoBan,TrangThai);
UPDATE STATISTICS #BenchmarkNhanVien WITH FULLSCAN;
IF (SELECT COUNT(*) FROM #BenchmarkNhanVien)<>10000
    THROW 53522,N'Fixture must contain 10000 rows.',1;
SELECT DB_NAME() AS DatabaseName,ORIGINAL_LOGIN() AS SqlIdentity,@@VERSION AS SqlVersion,
    10000 AS FixtureRows,2 AS FilterMaPB,1 AS FilterMaCV,
    'warm cache; forced hints; MAXDOP 1; no cache reset' AS MeasurementMode;
-- Capture complete reference results outside the measured statements.
SELECT MaNV,HoTen,LuongCoBan,TrangThai INTO #TV5_Before
FROM #BenchmarkNhanVien WITH(INDEX(PK_Bench_MaNV)) WHERE MaPB=2 AND MaCV=1;
PRINT 'MEASURE BEFORE: forced clustered scan';
SET STATISTICS IO ON;
SET STATISTICS TIME ON;
SET STATISTICS XML ON;
GO
SELECT MaNV,HoTen,LuongCoBan,TrangThai
FROM #BenchmarkNhanVien WITH(INDEX(PK_Bench_MaNV))
WHERE MaPB=2 AND MaCV=1 OPTION(MAXDOP 1);
GO
SET STATISTICS XML OFF;
SET STATISTICS IO OFF;
SET STATISTICS TIME OFF;
SELECT MaNV,HoTen,LuongCoBan,TrangThai INTO #TV5_After
FROM #BenchmarkNhanVien WITH(INDEX(IX_Bench_MaPB_MaCV)) WHERE MaPB=2 AND MaCV=1;
PRINT 'MEASURE AFTER: forced covering seek';
SET STATISTICS IO ON;
SET STATISTICS TIME ON;
SET STATISTICS XML ON;
GO
SELECT MaNV,HoTen,LuongCoBan,TrangThai
FROM #BenchmarkNhanVien WITH(INDEX(IX_Bench_MaPB_MaCV))
WHERE MaPB=2 AND MaCV=1 OPTION(MAXDOP 1);
GO
SET STATISTICS XML OFF;
SET STATISTICS IO OFF;
SET STATISTICS TIME OFF;
IF (SELECT COUNT(*) FROM #TV5_Before)=0
    OR (SELECT COUNT(*) FROM #TV5_Before)<>(SELECT COUNT(*) FROM #TV5_After)
    OR EXISTS(SELECT * FROM #TV5_Before EXCEPT SELECT * FROM #TV5_After)
    OR EXISTS(SELECT * FROM #TV5_After EXCEPT SELECT * FROM #TV5_Before)
    THROW 53523,N'Complete before/after results differ or contain zero rows.',1;
SELECT (SELECT COUNT(*) FROM #TV5_Before) AS BeforeRows,
    (SELECT COUNT(*) FROM #TV5_After) AS AfterRows,'PASS full bidirectional EXCEPT; MaNV is unique' AS Equality;
DROP TABLE #TV5_Before;
DROP TABLE #TV5_After;
DROP TABLE #BenchmarkNhanVien;
IF OBJECT_ID('tempdb..#BenchmarkNhanVien') IS NOT NULL
   OR OBJECT_ID('tempdb..#TV5_Before') IS NOT NULL
   OR OBJECT_ID('tempdb..#TV5_After') IS NOT NULL
    THROW 53524,N'Benchmark fixture cleanup failed.',1;
PRINT 'PASS benchmark complete results equality, two actual XML plans and temp fixture cleanup';
GO
