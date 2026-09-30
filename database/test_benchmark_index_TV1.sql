-- ============================================================================
-- PROJECT: Quản Lý Nhân Sự và Tiền Lương (Nhóm 06)
-- HỌC PHẦN: Hệ Quản Trị Cơ Sở Dữ Liệu (DBMS330284)
-- THÀNH VIÊN 1: Nguyễn Minh Trí (MSSV: 24110359)
-- SCRIPT: Đo kiểm hiệu năng Chỉ mục (Index Benchmark) - IX_NHANVIEN_HoTen
-- ============================================================================

USE QuanLyNhanSuTienLuong;
GO

-- ============================================================================
-- 1. TẠO DỮ LIỆU MẪU LỚN ĐỂ KIỂM THỬ BENCHMARK (NẾU CHƯA ĐỦ)
-- ============================================================================
PRINT '=== 1. KIỂM TRA VÀ TẠO DỮ LIỆU MẪU CHO BẢNG NHANVIEN ===';

IF (SELECT COUNT(1) FROM NHANVIEN) < 1000
BEGIN
    PRINT N'Đang chèn dữ liệu mẫu bổ sung để kiểm thử hiệu năng chỉ mục...';
    
    DECLARE @i INT = 1;
    DECLARE @HoTen NVARCHAR(100);
    DECLARE @Email VARCHAR(100);
    DECLARE @SDT VARCHAR(15);
    DECLARE @CCCD VARCHAR(12);
    
    WHILE @i <= 5000
    BEGIN
        SET @HoTen = CASE (@i % 10)
            WHEN 0 THEN N'Nguyễn Văn ' + CAST(@i AS NVARCHAR(10))
            WHEN 1 THEN N'Trần Thị ' + CAST(@i AS NVARCHAR(10))
            WHEN 2 THEN N'Lê Hoàng ' + CAST(@i AS NVARCHAR(10))
            WHEN 3 THEN N'Phạm Minh ' + CAST(@i AS NVARCHAR(10))
            WHEN 4 THEN N'Hoàng Quốc ' + CAST(@i AS NVARCHAR(10))
            WHEN 5 THEN N'Nguyễn Minh ' + CAST(@i AS NVARCHAR(10))
            WHEN 6 THEN N'Đỗ Hải ' + CAST(@i AS NVARCHAR(10))
            WHEN 7 THEN N'Bùi Thanh ' + CAST(@i AS NVARCHAR(10))
            WHEN 8 THEN N'Vũ Ngọc ' + CAST(@i AS NVARCHAR(10))
            ELSE N'Ngô Đình ' + CAST(@i AS NVARCHAR(10))
        END;
        
        SET @CCCD = RIGHT('000000000000' + CAST(100000000000 + @i AS VARCHAR(12)), 12);
        SET @SDT = '09' + RIGHT('00000000' + CAST(@i AS VARCHAR(8)), 8);
        SET @Email = 'nv_bench_' + CAST(@i AS VARCHAR(10)) + '@company.com';
        
        IF NOT EXISTS (SELECT 1 FROM NHANVIEN WHERE CCCD = @CCCD OR Email = @Email OR SoDienThoai = @SDT)
        BEGIN
            INSERT INTO NHANVIEN (HoTen, NgaySinh, GioiTinh, CCCD, DiaChi, SoDienThoai, Email, NgayVaoLam, LuongCoBan, MaPB, MaCV, TrangThai)
            VALUES (@HoTen, '1995-01-01', N'Nam', @CCCD, N'TP. Hồ Chí Minh', @SDT, @Email, '2020-01-01', 10000000, 1, 1, N'DANG_LAM_VIEC');
        END
        
        SET @i = @i + 1;
    END
    PRINT N'Đã hoàn thành chèn dữ liệu mẫu!';
END
ELSE
BEGIN
    PRINT N'Bảng NHANVIEN đã có đủ dữ liệu kiểm thử.';
END
GO

-- ============================================================================
-- 2. ĐO LƯỜNG HIỆU NĂNG TRƯỚC VÀ SAU KHI DÙNG INDEX IX_NHANVIEN_HoTen
-- Hướng dẫn: Nhấn Ctrl + M trong SSMS để bật "Include Actual Execution Plan"
-- ============================================================================

PRINT '=== 2. THỰC THI SO SÁNH HIỆU NĂNG TRUY VẤN (STATISTICS IO & TIME) ===';
GO

SET STATISTICS IO, TIME ON;
GO

-- [TEST A]: Truy vấn không dùng Index tối ưu (Ép duyệt tuần tự Clustered Scan)
PRINT '--------------------------------------------------------------';
PRINT '>>> [TEST A] TRUY VẤN DÙNG CLUSTERED INDEX SCAN (QUÉT TOÀN BỘ BẢNG)';
PRINT '--------------------------------------------------------------';

SELECT MaNV, HoTen, SoDienThoai, Email, MaPB, MaCV, TrangThai
FROM NHANVIEN WITH (INDEX(PK__NHANVIEN))
WHERE HoTen LIKE N'Nguyễn Minh%';
GO

-- [TEST B]: Truy vấn tận dụng Non-clustered Covering Index IX_NHANVIEN_HoTen (Index Seek)
PRINT '--------------------------------------------------------------';
PRINT '>>> [TEST B] TRUY VẤN TẬN DỤNG COVERING INDEX SEEK (IX_NHANVIEN_HoTen)';
PRINT '--------------------------------------------------------------';

SELECT MaNV, HoTen, SoDienThoai, Email, MaPB, MaCV, TrangThai
FROM NHANVIEN WITH (INDEX(IX_NHANVIEN_HoTen))
WHERE HoTen LIKE N'Nguyễn Minh%';
GO

SET STATISTICS IO, TIME OFF;
GO

-- ============================================================================
-- 3. XEM THÔNG TIN ĐẶC TẢ INDEX TRONG CSDL
-- ============================================================================
PRINT '=== 3. THÔNG TIN CHI TIẾT CHỈ MỤC IX_NHANVIEN_HoTen ===';

SELECT 
    i.name AS IndexName,
    i.type_desc AS IndexType,
    c.name AS ColumnName,
    ic.is_included_column AS IsIncludedColumn,
    i.is_unique AS IsUnique
FROM sys.indexes i
JOIN sys.index_columns ic ON i.object_id = ic.object_id AND i.index_id = ic.index_id
JOIN sys.columns c ON ic.object_id = c.object_id AND ic.column_id = c.column_id
WHERE i.object_id = OBJECT_ID(N'NHANVIEN') AND i.name = N'IX_NHANVIEN_HoTen';
GO
