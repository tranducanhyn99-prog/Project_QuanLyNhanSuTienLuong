-- Isolated index benchmark: no production rows, no global cache changes.
-- Keep the database selected by the caller.
SET NOCOUNT ON;
CREATE TABLE #NhanVienBenchmark(MaNV INT NOT NULL PRIMARY KEY, HoTen NVARCHAR(100) NOT NULL,
    SoDienThoai VARCHAR(15), Email VARCHAR(100), MaPB INT, MaCV INT, TrangThai NVARCHAR(20));
;WITH n AS (SELECT TOP(5000) ROW_NUMBER() OVER(ORDER BY (SELECT NULL)) AS id
    FROM sys.all_objects a CROSS JOIN sys.all_objects b)
INSERT #NhanVienBenchmark
SELECT id, CASE WHEN id%10=0 THEN N'Nguyễn Minh ' ELSE N'Trần Văn ' END + CONVERT(NVARCHAR(20),id),
       '0900000000', 'benchmark@example.invalid',1,1,N'DANG_LAM_VIEC' FROM n;
CREATE INDEX IX_Benchmark_HoTen ON #NhanVienBenchmark(HoTen)
    INCLUDE(SoDienThoai,Email,MaPB,MaCV,TrangThai);
SET STATISTICS IO, TIME ON;
SELECT MaNV,HoTen,SoDienThoai,Email,MaPB,MaCV,TrangThai
FROM #NhanVienBenchmark WITH(INDEX(1)) WHERE HoTen LIKE N'Nguyễn Minh%' OPTION(RECOMPILE);
SELECT MaNV,HoTen,SoDienThoai,Email,MaPB,MaCV,TrangThai
FROM #NhanVienBenchmark WITH(INDEX(IX_Benchmark_HoTen)) WHERE HoTen LIKE N'Nguyễn Minh%' OPTION(RECOMPILE);
SET STATISTICS IO, TIME OFF;
DROP TABLE #NhanVienBenchmark;
IF OBJECT_ID('tempdb..#NhanVienBenchmark') IS NOT NULL THROW 51011,N'Benchmark cleanup failed.',1;
GO
