-- ============================================================================
-- PROJECT: HỆ THỐNG QUẢN LÝ NHÂN SỰ VÀ TIỀN LƯƠNG (DBMS330284)
-- BỘ MÔ PHỎNG & ĐO LƯỜNG HIỆU NĂNG CHỈ MỤC (INDEX BENCHMARK) - TV5
-- TÁC GIẢ: TRẦN ĐỨC ANH (TV5 - MSSV: 24110155) - TUẦN 3
-- ĐỐI TƯỢNG SỞ HỮU: IX_NHANVIEN_MaPB_MaCV (COVERING INDEX)
-- ============================================================================

-- Keep the database selected by the caller.

PRINT '============================================================================';
PRINT '   BẮT ĐẦU ĐO LƯỜNG HIỆU NĂNG CHỈ MỤC IX_NHANVIEN_MaPB_MaCV (TV5)';
PRINT '============================================================================';
GO

-- 1. TẠO DỮ LIỆU THỬ NGHIỆM QUY MÔ LỚN (NẾU CẦN ĐO TRÊN TẬP DỮ LIỆU LỚN)
-- Tạo 5,000 dòng nhân viên giả lập vào bảng tạm để kiểm tra hiệu năng
IF OBJECT_ID('tempdb..#BenchmarkNhanVien') IS NOT NULL
    DROP TABLE #BenchmarkNhanVien;
GO

SELECT TOP 10000
    ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) AS MaNV,
    N'Nhân viên test ' + CAST(ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) AS NVARCHAR(10)) AS HoTen,
    (ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) % 4) + 1 AS MaPB,
    (ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) % 3) + 1 AS MaCV,
    CAST(10000000 + (ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) % 20) * 500000 AS DECIMAL(18,2)) AS LuongCoBan,
    N'DANG_LAM_VIEC' AS TrangThai
INTO #BenchmarkNhanVien
FROM sys.all_objects a
CROSS JOIN sys.all_objects b;
GO

PRINT '-> Đã sinh 10,000 bản ghi thử nghiệm trong bảng tạm #BenchmarkNhanVien.';
GO

-- 2. TẠO INDEX TRÊN BẢNG TẠM ĐỂ MINH CHỨNG SO SÁNH TRỰC DIỆN
CREATE CLUSTERED INDEX PK_Bench_MaNV ON #BenchmarkNhanVien(MaNV);
GO

-- Tạo Covering Index giống cấu trúc IX_NHANVIEN_MaPB_MaCV
CREATE NONCLUSTERED INDEX IX_Bench_MaPB_MaCV 
ON #BenchmarkNhanVien(MaPB, MaCV)
INCLUDE (HoTen, LuongCoBan, TrangThai);
GO

PRINT '----------------------------------------------------------------------------';
PRINT 'THỬ NGHIỆM 1: TRUY VẤN KHI KHÔNG SỬ DỤNG CHỈ MỤC (TABLE SCAN / CLUSTERED SCAN)';
PRINT '----------------------------------------------------------------------------';
SET STATISTICS IO ON;
SET STATISTICS TIME ON;
GO

-- Ép SQL Server dùng Clustered Index Scan (quét toàn bộ bảng)
SELECT MaNV, HoTen, LuongCoBan, TrangThai
FROM #BenchmarkNhanVien WITH (INDEX(PK_Bench_MaNV))
WHERE MaPB = 2 AND MaCV = 1;
GO

SET STATISTICS IO OFF;
SET STATISTICS TIME OFF;
GO

PRINT '----------------------------------------------------------------------------';
PRINT 'THỬ NGHIỆM 2: TRUY VẤN KHI SỬ DỤNG COVERING INDEX (IX_Bench_MaPB_MaCV)';
PRINT '----------------------------------------------------------------------------';
SET STATISTICS IO ON;
SET STATISTICS TIME ON;
GO

-- Dùng Covering Non-Clustered Index: Index Seek 100%, triệt tiêu Bookmark Lookup
SELECT MaNV, HoTen, LuongCoBan, TrangThai
FROM #BenchmarkNhanVien WITH (INDEX(IX_Bench_MaPB_MaCV))
WHERE MaPB = 2 AND MaCV = 1;
GO

SET STATISTICS IO OFF;
SET STATISTICS TIME OFF;
GO

-- 3. ĐO KIỂM THỰC TẾ TRÊN BẢNG CHÍNH NHANVIEN (DATABASE PRODUCTION)
PRINT '----------------------------------------------------------------------------';
PRINT 'THỬ NGHIỆM 3: TRUY VẤN THỰC TẾ TRÊN BẢNG NHANVIEN VỚI IX_NHANVIEN_MaPB_MaCV';
PRINT '----------------------------------------------------------------------------';
SET STATISTICS IO ON;
SET STATISTICS TIME ON;
GO

SELECT MaNV, HoTen, LuongCoBan, TrangThai
FROM dbo.NHANVIEN
WHERE MaPB = 1 AND MaCV = 1;
GO

SET STATISTICS IO OFF;
SET STATISTICS TIME OFF;
GO

-- DỌN DẸP BẢNG TẠM
IF OBJECT_ID('tempdb..#BenchmarkNhanVien') IS NOT NULL
    DROP TABLE #BenchmarkNhanVien;
GO

PRINT '============================================================================';
PRINT '   KẾT LUẬN BENCHMARK INDEX TV5:';
PRINT '   - So sánh logical reads từ STATISTICS IO của chính lần chạy này.';
PRINT '   - Kiểm tra operator và Key Lookup trong actual execution plan; không dùng tỷ lệ hardcode.';
PRINT '   - Kết quả temp dataset không phải cam kết hiệu năng dữ liệu thực.';
PRINT '============================================================================';
GO
