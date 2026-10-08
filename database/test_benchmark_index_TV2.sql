-- ============================================================================
-- PROJECT: Quản Lý Nhân Sự và Tiền Lương (DBMS330284) - Nhóm 06
-- HỌC PHẦN: Hệ Quản Trị Cơ Sở Dữ Liệu
-- PHÂN HỆ: QUẢN LÝ CHẤM CÔNG (ATTENDANCE) - TUẦN 3
-- TÁC GIẢ: TV2 - Phạm Minh Quân (MSSV: 24110311)
-- CHUYÊN ĐỀ: ĐO LƯỜNG VÀ ĐÁNH GIÁ HIỆU NĂNG NON-CLUSTERED COVERING INDEX
-- ĐỐI TƯỢNG SỞ HỮU: IX_CHAMCONG_MaNV_Ngay TRÊN BẢNG CHAMCONG
-- ============================================================================

-- Keep the database selected by the caller.

PRINT N'============================================================================';
PRINT N'  BẮT ĐẦU CHƯƠNG TRÌNH BENCHMARK INDEX: IX_CHAMCONG_MaNV_Ngay (TV2)';
PRINT N'============================================================================';
PRINT N'LƯU Ý KỸ THUẬT:';
PRINT N'  - Bảng sản xuất dbo.CHAMCONG đã có ràng buộc UQ_CHAMCONG_MaNV_Ngay trên (MaNV, NgayChamCong).';
PRINT N'  - Ràng buộc UNIQUE này đã tự động tạo một Non-clustered Index tìm kiếm theo khóa.';
PRINT N'  - Do đó, so sánh Table Scan (bỏ index) là phi thực tế và gây hiểu nhầm.';
PRINT N'  - Giá trị cốt lõi của IX_CHAMCONG_MaNV_Ngay là COVERING INDEX với mệnh đề INCLUDE';
PRINT N'    (GioVao, GioRa, TrangThai), giúp loại bỏ hoàn toàn Key Lookup (Clustered Lookup)';
PRINT N'    khi tra cứu chi tiết công và tổng hợp tháng (vw_TongHopChamCongThang).';
PRINT N'  - Kịch bản sử dụng bảng tạm cục bộ session-local (#CHAMCONG_BENCHMARK) để cách ly 100%';
PRINT N'    an toàn, tuyệt đối không tạo/xóa bảng trong schema dbo và không can thiệp cache hệ thống.';
PRINT N'============================================================================';
GO

-- ============================================================================
-- BƯỚC 1: TẠO BẢNG TẠM CỤC BỘ ĐỘC LẬP ĐỂ BENCHMARK AN TOÀN (ISOLATION)
-- Sử dụng #CHAMCONG_BENCHMARK trong tempdb, hoàn toàn không đụng chạm dbo.CHAMCONG
-- ============================================================================
IF OBJECT_ID('tempdb..#CHAMCONG_BENCHMARK') IS NOT NULL
    DROP TABLE #CHAMCONG_BENCHMARK;
GO

CREATE TABLE #CHAMCONG_BENCHMARK (
    MaChamCong    INT           IDENTITY(1,1) NOT NULL,
    MaNV          INT           NOT NULL,
    NgayChamCong  DATE          NOT NULL,
    GioVao        TIME(0)       NOT NULL,
    GioRa         TIME(0)       NULL,
    TrangThai     NVARCHAR(20)  NOT NULL CONSTRAINT DF_Bench_TrangThai DEFAULT N'CO_MAT',
    GhiChu        NVARCHAR(255) NULL,

    -- Khóa chính Clustered tương tự dbo.CHAMCONG
    CONSTRAINT PK_Bench_ChamCong PRIMARY KEY CLUSTERED (MaChamCong),

    -- Ràng buộc duy nhất tương tự UQ_CHAMCONG_MaNV_Ngay, cung cấp chỉ mục cơ sở trên (MaNV, NgayChamCong)
    CONSTRAINT UQ_Bench_MaNV_Ngay UNIQUE NONCLUSTERED (MaNV, NgayChamCong)
);
GO

-- ============================================================================
-- BƯỚC 2: SINH 30.000 BẢN GHI DỮ LIỆU ĐỊNH CHẾ (DETERMINISTIC DATA)
-- 100 nhân viên (MaNV 1..100) x 300 ngày phân biệt -> Đảm bảo 100% không trùng khóa
-- ============================================================================
PRINT N'Đang nạp 30.000 bản ghi chấm công thử nghiệm vào bảng tạm #CHAMCONG_BENCHMARK...';
SET NOCOUNT ON;

;WITH Numbers AS (
    SELECT TOP (30000) ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) AS N
    FROM sys.all_columns a CROSS JOIN sys.all_columns b
)
INSERT INTO #CHAMCONG_BENCHMARK (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
SELECT
    ((N - 1) % 100) + 1 AS MaNV,
    DATEADD(DAY, -((N - 1) / 100), '2026-09-30') AS NgayChamCong,
    CASE (N % 10)
        WHEN 0 THEN CAST('08:20:00' AS TIME(0))
        WHEN 1 THEN CAST('08:35:00' AS TIME(0))
        ELSE CAST('07:55:00' AS TIME(0))
    END AS GioVao,
    CASE (N % 12)
        WHEN 0 THEN CAST('16:30:00' AS TIME(0))
        WHEN 1 THEN CAST('16:45:00' AS TIME(0))
        ELSE CAST('17:15:00' AS TIME(0))
    END AS GioRa,
    CASE
        WHEN (N % 20) = 0 THEN N'VANG'
        WHEN (N % 10) IN (0, 1) THEN N'DI_TRE'
        WHEN (N % 12) IN (0, 1) THEN N'VE_SOM'
        ELSE N'CO_MAT'
    END AS TrangThai,
    N'Dữ liệu sinh tự động phục vụ Benchmark Index TV2' AS GhiChu
FROM Numbers;

PRINT N'Nạp dữ liệu hoàn tất: ' + CAST(@@ROWCOUNT AS VARCHAR(10)) + N' bản ghi.';
GO

-- ============================================================================
-- BƯỚC 3: ĐO LƯỜNG TRƯỚC KHI CÓ COVERING INDEX (BEFORE COVERING INDEX)
-- Tình trạng: Chỉ có UQ_Bench_MaNV_Ngay (Non-clustered Index Seek)
-- nhưng thiếu cột INCLUDE -> Phải thực hiện Key Lookup (Clustered) từng dòng.
-- Hướng dẫn: Bật "Include Actual Execution Plan" (Ctrl + M) trong SSMS để quan sát.
-- ============================================================================
PRINT N'----------------------------------------------------------------------------';
PRINT N'1. TRUY VẤN KHI CHƯA CÓ COVERING INDEX (INDEX SEEK + KEY LOOKUP)';
PRINT N'----------------------------------------------------------------------------';

SET STATISTICS IO ON;
SET STATISTICS TIME ON;

-- 1.1. Truy vấn tra cứu chi tiết công theo khoảng thời gian của nhân viên 25
PRINT N'>>> [BEFORE - 1.1] Tra cứu chi tiết chấm công nhân viên 25 (Key Lookup):';
SELECT MaNV, NgayChamCong, GioVao, GioRa, TrangThai
FROM #CHAMCONG_BENCHMARK WITH (INDEX(UQ_Bench_MaNV_Ngay))
WHERE MaNV = 25 AND NgayChamCong BETWEEN '2026-01-01' AND '2026-09-30';

-- 1.2. Truy vấn tổng hợp số ngày làm và tổng giờ làm (mô phỏng vw_TongHopChamCongThang)
PRINT N'>>> [BEFORE - 1.2] Tổng hợp công và giờ làm nhân viên 25 (Key Lookup / Aggregate):';
SELECT
    MaNV,
    MONTH(NgayChamCong) AS Thang,
    YEAR(NgayChamCong)  AS Nam,
    COUNT(CASE WHEN TrangThai IN (N'CO_MAT', N'DI_TRE', N'VE_SOM') THEN 1 END) AS SoNgayDiLam,
    COUNT(CASE WHEN TrangThai = N'DI_TRE' THEN 1 END) AS SoLanDiTre,
    COUNT(CASE WHEN TrangThai = N'VE_SOM' THEN 1 END) AS SoLanVeSom,
    COUNT(CASE WHEN TrangThai = N'VANG' THEN 1 END) AS SoNgayVang,
    SUM(CASE
        WHEN GioRa IS NOT NULL AND GioRa > GioVao
        THEN DATEDIFF(MINUTE, GioVao, GioRa)
        ELSE 0
    END) / 60.0 AS TongSoGioLam
FROM #CHAMCONG_BENCHMARK WITH (INDEX(UQ_Bench_MaNV_Ngay))
WHERE MaNV = 25 AND NgayChamCong BETWEEN '2026-01-01' AND '2026-09-30'
GROUP BY MaNV, YEAR(NgayChamCong), MONTH(NgayChamCong);

SET STATISTICS IO OFF;
SET STATISTICS TIME OFF;
GO

-- ============================================================================
-- BƯỚC 4: TẠO COVERING INDEX TRÊN BẢNG TẠM THEO THIẾT KẾ IX_CHAMCONG_MaNV_Ngay
-- Bổ sung INCLUDE (GioVao, GioRa, TrangThai) để bao phủ toàn bộ câu truy vấn
-- ============================================================================
PRINT N'----------------------------------------------------------------------------';
PRINT N'2. TẠO NON-CLUSTERED COVERING INDEX: IX_Bench_CHAMCONG_MaNV_Ngay';
PRINT N'----------------------------------------------------------------------------';

CREATE NONCLUSTERED INDEX IX_Bench_CHAMCONG_MaNV_Ngay
ON #CHAMCONG_BENCHMARK (MaNV, NgayChamCong)
INCLUDE (GioVao, GioRa, TrangThai);
GO

-- ============================================================================
-- BƯỚC 5: ĐO LƯỜNG SAU KHI CÓ COVERING INDEX (AFTER COVERING INDEX)
-- Kỳ vọng: Index Seek 100% trên IX_Bench_CHAMCONG_MaNV_Ngay, 0 Key Lookup,
-- số Logical Reads giảm mạnh tương ứng số trang lá của Non-clustered Index.
-- ============================================================================
PRINT N'----------------------------------------------------------------------------';
PRINT N'3. TRUY VẤN KHI ĐÃ CÓ COVERING INDEX (PURE INDEX SEEK - ZERO KEY LOOKUP)';
PRINT N'----------------------------------------------------------------------------';

SET STATISTICS IO ON;
SET STATISTICS TIME ON;

-- 3.1. Truy vấn tra cứu chi tiết công theo khoảng thời gian của nhân viên 25
PRINT N'>>> [AFTER - 3.1] Tra cứu chi tiết chấm công nhân viên 25 (Covering Index Seek):';
SELECT MaNV, NgayChamCong, GioVao, GioRa, TrangThai
FROM #CHAMCONG_BENCHMARK WITH (INDEX(IX_Bench_CHAMCONG_MaNV_Ngay))
WHERE MaNV = 25 AND NgayChamCong BETWEEN '2026-01-01' AND '2026-09-30';

-- 3.2. Truy vấn tổng hợp số ngày làm và tổng giờ làm (mô phỏng vw_TongHopChamCongThang)
PRINT N'>>> [AFTER - 3.2] Tổng hợp công và giờ làm nhân viên 25 (Covering Index Seek):';
SELECT
    MaNV,
    MONTH(NgayChamCong) AS Thang,
    YEAR(NgayChamCong)  AS Nam,
    COUNT(CASE WHEN TrangThai IN (N'CO_MAT', N'DI_TRE', N'VE_SOM') THEN 1 END) AS SoNgayDiLam,
    COUNT(CASE WHEN TrangThai = N'DI_TRE' THEN 1 END) AS SoLanDiTre,
    COUNT(CASE WHEN TrangThai = N'VE_SOM' THEN 1 END) AS SoLanVeSom,
    COUNT(CASE WHEN TrangThai = N'VANG' THEN 1 END) AS SoNgayVang,
    SUM(CASE
        WHEN GioRa IS NOT NULL AND GioRa > GioVao
        THEN DATEDIFF(MINUTE, GioVao, GioRa)
        ELSE 0
    END) / 60.0 AS TongSoGioLam
FROM #CHAMCONG_BENCHMARK WITH (INDEX(IX_Bench_CHAMCONG_MaNV_Ngay))
WHERE MaNV = 25 AND NgayChamCong BETWEEN '2026-01-01' AND '2026-09-30'
GROUP BY MaNV, YEAR(NgayChamCong), MONTH(NgayChamCong);

SET STATISTICS IO OFF;
SET STATISTICS TIME OFF;
GO

-- ============================================================================
-- BƯỚC 6: XÁC THỰC THÔNG TIN METADATA CỦA CHỈ MỤC TRÊN BẢNG SẢN XUẤT THẬT
-- ============================================================================
PRINT N'----------------------------------------------------------------------------';
PRINT N'4. KIỂM TRA METADATA CHỈ MỤC TRÊN BẢNG CHÍNH THỨC dbo.CHAMCONG';
PRINT N'----------------------------------------------------------------------------';

SELECT
    i.name AS TenChiMuc,
    i.type_desc AS LoaiChiMuc,
    i.is_unique AS RBiDuyNhat,
    c.name AS TenCot,
    ic.key_ordinal AS ThuTuKhoa,
    ic.is_included_column AS CotInclude
FROM sys.indexes i
JOIN sys.index_columns ic ON i.object_id = ic.object_id AND i.index_id = ic.index_id
JOIN sys.columns c ON ic.object_id = c.object_id AND ic.column_id = c.column_id
WHERE i.object_id = OBJECT_ID(N'dbo.CHAMCONG')
  AND i.name IN (N'IX_CHAMCONG_MaNV_Ngay', N'UQ_CHAMCONG_MaNV_Ngay')
ORDER BY i.name, ic.is_included_column, ic.key_ordinal;
GO

-- ============================================================================
-- BƯỚC 7: DỌN DẸP BẢNG TẠM BENCHMARK (ĐẢM BẢO KHÔNG ẢNH HƯỞNG DỮ LIỆU THẬT)
-- ============================================================================
IF OBJECT_ID('tempdb..#CHAMCONG_BENCHMARK') IS NOT NULL
    DROP TABLE #CHAMCONG_BENCHMARK;
GO

PRINT N'============================================================================';
PRINT N'  HOÀN TẤT KỊCH BẢN BENCHMARK INDEX IX_CHAMCONG_MaNV_Ngay CỦA TV2.';
IF OBJECT_ID('tempdb..#CHAMCONG_BENCHMARK') IS NOT NULL THROW 53411,N'Benchmark cleanup failed.',1;
PRINT N'  Đã dọn bảng tạm benchmark.';
PRINT N'============================================================================';
GO
