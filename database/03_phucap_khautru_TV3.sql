-- =================================================================
-- PROJECT: HỆ THỐNG QUẢN LÝ NHÂN SỰ VÀ TIỀN LƯƠNG (DBMS330284)
-- HỌC PHẦN: HỆ QUẢN TRỊ CƠ SỞ DỮ LIỆU - NHÓM 06
-- PHÂN HỆ: QUẢN LÝ PHỤ CẤP & KHẤU TRỪ THEO KỲ
-- THỰC HIỆN: TV3 - TRẦN TIẾN ĐẠT (MSSV: 24110198) - TUẦN 1
-- =================================================================

USE QuanLyNhanSuTienLuong;
GO

-- 1. XÓA BẢNG CŨ NẾU ĐÃ TỒN TẠI ĐỂ TÁI LẬP MÔI TRƯỜNG
IF OBJECT_ID('dbo.KHAUTRUNHANVIEN', 'U') IS NOT NULL
    DROP TABLE dbo.KHAUTRUNHANVIEN;
GO

IF OBJECT_ID('dbo.PHUCAPNHANVIEN', 'U') IS NOT NULL
    DROP TABLE dbo.PHUCAPNHANVIEN;
GO

-- 2. TẠO BẢNG PHUCAPNHANVIEN (CHUẨN HÓA 3NF)
CREATE TABLE dbo.PHUCAPNHANVIEN (
    MaPCNV INT IDENTITY(1,1) NOT NULL,
    MaNV VARCHAR(20) NOT NULL,
    Thang INT NOT NULL,
    Nam INT NOT NULL,
    TenPhuCap NVARCHAR(100) NOT NULL,
    SoTien DECIMAL(18,2) NOT NULL CONSTRAINT DF_PHUCAP_SoTien DEFAULT 0,
    NgayGhiNhan DATE NOT NULL CONSTRAINT DF_PHUCAP_NgayGhiNhan DEFAULT CAST(GETDATE() AS DATE),
    GhiChu NVARCHAR(255) NULL,

    -- Khóa chính
    CONSTRAINT PK_PHUCAPNHANVIEN PRIMARY KEY CLUSTERED (MaPCNV),

    -- Khóa ngoại tham chiếu bảng NHANVIEN (do TV1 phụ trách)
    CONSTRAINT FK_PHUCAP_NHANVIEN FOREIGN KEY (MaNV)
        REFERENCES dbo.NHANVIEN(MaNV) ON DELETE NO ACTION ON UPDATE CASCADE,

    -- Ràng buộc kiểm tra tính hợp lệ dữ liệu (Business Rules Tuần 1)
    CONSTRAINT CK_PHUCAP_Thang CHECK (Thang BETWEEN 1 AND 12),
    CONSTRAINT CK_PHUCAP_Nam CHECK (Nam >= 2020),
    CONSTRAINT CK_PHUCAP_SoTien CHECK (SoTien >= 0)
);
GO

-- 3. TẠO BẢNG KHAUTRUNHANVIEN (CHUẨN HÓA 3NF)
CREATE TABLE dbo.KHAUTRUNHANVIEN (
    MaKTNV INT IDENTITY(1,1) NOT NULL,
    MaNV VARCHAR(20) NOT NULL,
    Thang INT NOT NULL,
    Nam INT NOT NULL,
    TenKhauTru NVARCHAR(100) NOT NULL,
    SoTien DECIMAL(18,2) NOT NULL CONSTRAINT DF_KHAUTRU_SoTien DEFAULT 0,
    NgayGhiNhan DATE NOT NULL CONSTRAINT DF_KHAUTRU_NgayGhiNhan DEFAULT CAST(GETDATE() AS DATE),
    LyDo NVARCHAR(255) NULL,

    -- Khóa chính
    CONSTRAINT PK_KHAUTRUNHANVIEN PRIMARY KEY CLUSTERED (MaKTNV),

    -- Khóa ngoại tham chiếu bảng NHANVIEN (do TV1 phụ trách)
    CONSTRAINT FK_KHAUTRU_NHANVIEN FOREIGN KEY (MaNV)
        REFERENCES dbo.NHANVIEN(MaNV) ON DELETE NO ACTION ON UPDATE CASCADE,

    -- Ràng buộc kiểm tra tính hợp lệ dữ liệu (Business Rules Tuần 1)
    CONSTRAINT CK_KHAUTRU_Thang CHECK (Thang BETWEEN 1 AND 12),
    CONSTRAINT CK_KHAUTRU_Nam CHECK (Nam >= 2020),
    CONSTRAINT CK_KHAUTRU_SoTien CHECK (SoTien >= 0)
);
GO

-- 4. TẠO NON-CLUSTERED INDEX ĐỂ TỐI ƯU TRUY VẤN TỔNG HỢP THEO KỲ (OWNERSHIP TV3)
CREATE NONCLUSTERED INDEX IX_PHUCAP_MaNV_ThangNam
ON dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam)
INCLUDE (SoTien, TenPhuCap);
GO

CREATE NONCLUSTERED INDEX IX_KHAUTRU_MaNV_ThangNam
ON dbo.KHAUTRUNHANVIEN (MaNV, Thang, Nam)
INCLUDE (SoTien, TenKhauTru);
GO

-- 5. BỘ DỮ LIỆU MẪU KIỂM THỬ TÍCH HỢP (SAMPLE DATASET)
-- Lưu ý: Cần có sẵn dữ liệu các mã NV001, NV002, NV003, NV004, NV005 từ bảng NHANVIEN

-- 5.1. Dữ liệu Phụ cấp tháng 09/2026
INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu) VALUES
('NV001', 9, 2026, N'Phụ cấp ăn trưa', 730000, '2026-09-01', N'Phụ cấp theo ngày làm việc'),
('NV001', 9, 2026, N'Hỗ trợ xăng xe', 500000, '2026-09-01', N'Đi lại công tác thường xuyên'),
('NV002', 9, 2026, N'Phụ cấp ăn trưa', 730000, '2026-09-01', N'Phụ cấp cố định'),
('NV002', 9, 2026, N'Phụ cấp trách nhiệm', 1500000, '2026-09-05', N'Trưởng nhóm dự án'),
('NV003', 9, 2026, N'Phụ cấp ăn trưa', 730000, '2026-09-01', N'Phụ cấp cố định'),
('NV004', 9, 2026, N'Phụ cấp độc hại', 1000000, '2026-09-10', N'Phòng Lab/Máy chủ');

-- 5.2. Dữ liệu Khấu trừ tháng 09/2026
INSERT INTO dbo.KHAUTRUNHANVIEN (MaNV, Thang, Nam, TenKhauTru, SoTien, NgayGhiNhan, LyDo) VALUES
('NV001', 9, 2026, N'Tạm ứng lương', 2000000, '2026-09-15', N'Nhân viên xin ứng giữa tháng'),
('NV002', 9, 2026, N'Khấu trừ đi trễ', 150000, '2026-09-20', N'Vi phạm đi trễ 3 lần'),
('NV003', 9, 2026, N'Tạm ứng lương', 1000000, '2026-09-15', N'Tạm ứng lương cá nhân'),
('NV004', 9, 2026, N'Bồi hoàn tài sản', 500000, '2026-09-22', N'Làm hư hỏng thiết bị văn phòng');
GO

-- 6. TRUY VẤN KIỂM CHỨNG TỔNG HỢP DỮ LIỆU
SELECT
    MaNV, Thang, Nam,
    COUNT(MaPCNV) AS SoKhoanPhuCap,
    SUM(SoTien) AS TongTienPhuCap
FROM dbo.PHUCAPNHANVIEN
WHERE Thang = 9 AND Nam = 2026
GROUP BY MaNV, Thang, Nam;

SELECT
    MaNV, Thang, Nam,
    COUNT(MaKTNV) AS SoKhoanKhauTru,
    SUM(SoTien) AS TongTienKhauTru
FROM dbo.KHAUTRUNHANVIEN
WHERE Thang = 9 AND Nam = 2026
GROUP BY MaNV, Thang, Nam;
GO