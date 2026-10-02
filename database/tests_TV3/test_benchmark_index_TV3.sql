-- ============================================================================
-- PROJECT: Quản Lý Nhân Sự và Tiền Lương (DBMS330284) - Nhóm 06
-- HỌC PHẦN: Hệ Quản Trị Cơ Sở Dữ Liệu
-- CHUYÊN ĐỀ: ĐO LƯỜNG VÀ ĐÁNH GIÁ HIỆU NĂNG NON-CLUSTERED INDEX
-- TÁC GIẢ: TV3 - Trần Tiến Đạt (MSSV: 24110198)
-- ĐỐI TƯỢNG SỞ HỮU: IX_PHUCAP_MaNV_ThangNam TRÊN BẢNG PHUCAPNHANVIEN
-- ============================================================================

USE QuanLyNhanSuTienLuong;
GO

PRINT '============================================================================';
PRINT '  BẮT ĐẦU CHƯƠNG TRÌNH BENCHMARK INDEX: IX_PHUCAP_MaNV_ThangNam (TV3)';
PRINT '============================================================================';

-- BƯỚC 1: TẠO BẢNG DỮ LIỆU TẠM ĐỂ BENCHMARK ĐỘC LẬP
IF OBJECT_ID('dbo.PHUCAP_BENCHMARK', 'U') IS NOT NULL
    DROP TABLE dbo.PHUCAP_BENCHMARK;
GO

CREATE TABLE dbo.PHUCAP_BENCHMARK (
    MaPCNV      INT IDENTITY(1,1) PRIMARY KEY CLUSTERED,
    MaNV        INT NOT NULL,
    Thang       INT NOT NULL,
    Nam         INT NOT NULL,
    TenPhuCap   NVARCHAR(100) NOT NULL,
    SoTien      DECIMAL(18,2) NOT NULL DEFAULT 0,
    NgayGhiNhan DATE NOT NULL DEFAULT GETDATE(),
    GhiChu      NVARCHAR(255) NULL
);
GO

-- BƯỚC 2: SINH 30.000 DÒNG DỮ LIỆU PHỤ CẤP MÔ PHỎNG THEO NHIỀU KỲ VÀ NHÂN VIÊN
PRINT N'Đang nạp 30.000 bản ghi phụ cấp thử nghiệm...';
SET NOCOUNT ON;

;WITH Numbers AS (
    SELECT TOP (30000) ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) AS N
    FROM sys.all_columns a CROSS JOIN sys.all_columns b
)
INSERT INTO dbo.PHUCAP_BENCHMARK (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
SELECT
    (N % 200) + 1 AS MaNV,                      -- 200 nhân viên (MaNV 1..200)
    (N % 12) + 1  AS Thang,                     -- Tháng 1..12
    2020 + (N % 7) AS Nam,                      -- Năm 2020..2026
    CASE (N % 4)
        WHEN 0 THEN N'Phụ cấp ăn trưa'
        WHEN 1 THEN N'Phụ cấp xăng xe'
        WHEN 2 THEN N'Phụ cấp trách nhiệm'
        ELSE N'Phụ cấp độc hại'
    END AS TenPhuCap,
    CAST((500000 + (N % 20) * 100000) AS DECIMAL(18,2)) AS SoTien,
    DATEADD(DAY, -(N % 1000), '2026-09-30') AS NgayGhiNhan,
    N'Dữ liệu sinh tự động phục vụ Benchmark Index TV3' AS GhiChu
FROM Numbers;

PRINT N'Nạp dữ liệu hoàn tất: ' + CAST(@@ROWCOUNT AS VARCHAR) + N' bản ghi.';
GO

-- ============================================================================
-- BƯỚC 3: ĐO LƯỜNG TRƯỚC KHI CÓ INDEX (BEFORE INDEX - TABLE SCAN)
-- ============================================================================
PRINT '----------------------------------------------------------------------------';
PRINT '1. TRUY VẤN KHI CHƯA CÓ INDEX (TABLE SCAN / CLUSTERED SCAN)';
PRINT '----------------------------------------------------------------------------';

-- Xóa cache bộ đệm để đảm bảo đo lường đĩa vật lý chính xác
CHECKPOINT;
DBCC DROPCLEANBUFFERS;
DBCC FREEPROCCACHE;

SET STATISTICS IO ON;
SET STATISTICS TIME ON;

-- Câu truy vấn nghiệp vụ: Tra cứu và tính tổng phụ cấp của nhân viên 25 trong tháng 9 năm 2026
SELECT
    MaNV, Thang, Nam,
    COUNT(MaPCNV) AS SoKhoanPhuCap,
    SUM(SoTien) AS TongTienPhuCap
FROM dbo.PHUCAP_BENCHMARK
WHERE MaNV = 25 AND Thang = 9 AND Nam = 2026
GROUP BY MaNV, Thang, Nam;

SET STATISTICS IO OFF;
SET STATISTICS TIME OFF;
GO

-- ============================================================================
-- BƯỚC 4: TẠO COVERING INDEX THEO ĐÚNG THIẾT KẾ CỦA TV3
-- ============================================================================
PRINT '----------------------------------------------------------------------------';
PRINT '2. TẠO NON-CLUSTERED INDEX IX_PHUCAP_BENCHMARK_MaNV_ThangNam';
PRINT '----------------------------------------------------------------------------';

CREATE NONCLUSTERED INDEX IX_PHUCAP_BENCHMARK_MaNV_ThangNam
ON dbo.PHUCAP_BENCHMARK (MaNV, Thang, Nam)
INCLUDE (SoTien, TenPhuCap);
GO

-- ============================================================================
-- BƯỚC 5: ĐO LƯỜNG SAU KHI CÓ INDEX (AFTER INDEX - INDEX SEEK)
-- ============================================================================
PRINT '----------------------------------------------------------------------------';
PRINT '3. TRUY VẤN KHI ĐÃ CÓ INDEX (INDEX SEEK + COVERING)';
PRINT '----------------------------------------------------------------------------';

CHECKPOINT;
DBCC DROPCLEANBUFFERS;
DBCC FREEPROCCACHE;

SET STATISTICS IO ON;
SET STATISTICS TIME ON;

-- Chạy cùng câu truy vấn nghiệp vụ
SELECT
    MaNV, Thang, Nam,
    COUNT(MaPCNV) AS SoKhoanPhuCap,
    SUM(SoTien) AS TongTienPhuCap
FROM dbo.PHUCAP_BENCHMARK
WHERE MaNV = 25 AND Thang = 9 AND Nam = 2026
GROUP BY MaNV, Thang, Nam;

SET STATISTICS IO OFF;
SET STATISTICS TIME OFF;
GO

-- Dọn dẹp bảng tạm sau khi đo lường xong
DROP TABLE dbo.PHUCAP_BENCHMARK;
PRINT N'Hoàn tất kịch bản benchmark index của TV3.';
GO