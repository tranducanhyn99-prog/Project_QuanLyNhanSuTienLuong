-- =================================================================
-- PROJECT: HỆ THỐNG QUẢN LÝ NHÂN SỰ VÀ TIỀN LƯƠNG (DBMS330284)
-- HỌC PHẦN: HỆ QUẢN TRỊ CƠ SỞ DỮ LIỆU - NHÓM 06
-- PHÂN HỆ: QUẢN LÝ PHỤ CẤP & KHẤU TRỪ THEO KỲ
-- THỰC HIỆN: TV3 - TRẦN TIẾN ĐẠT (MSSV: 24110198) - TUẦN 1
-- =================================================================

USE QuanLyNhanSuTienLuong;
GO

-- 1. TẠO BẢNG PHUCAPNHANVIEN (DÙNG CẤU TRÚC KIỂM TRA AN TOÀN TRÁNH MẤT DỮ LIỆU)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = N'PHUCAPNHANVIEN')
BEGIN
    CREATE TABLE dbo.PHUCAPNHANVIEN (
        MaPCNV INT IDENTITY(1,1) NOT NULL,
        MaNV INT NOT NULL, -- Đồng bộ kiểu INT theo bảng NHANVIEN của TV1
        Thang INT NOT NULL,
        Nam INT NOT NULL,
        TenPhuCap NVARCHAR(100) NOT NULL,
        SoTien DECIMAL(18,2) NOT NULL CONSTRAINT DF_PHUCAP_SoTien DEFAULT 0,
        NgayGhiNhan DATE NOT NULL CONSTRAINT DF_PHUCAP_NgayGhiNhan DEFAULT CAST(GETDATE() AS DATE),
        GhiChu NVARCHAR(255) NULL,

        -- Khóa chính
        CONSTRAINT PK_PHUCAPNHANVIEN PRIMARY KEY CLUSTERED (MaPCNV),

        -- Khóa ngoại tham chiếu NHANVIEN(MaNV)
        CONSTRAINT FK_PHUCAP_NHANVIEN FOREIGN KEY (MaNV)
            REFERENCES dbo.NHANVIEN(MaNV) ON DELETE NO ACTION ON UPDATE CASCADE,

        -- Ràng buộc miền giá trị hợp lệ
        CONSTRAINT CK_PHUCAP_Thang CHECK (Thang BETWEEN 1 AND 12),
        CONSTRAINT CK_PHUCAP_Nam CHECK (Nam >= 2020),
        CONSTRAINT CK_PHUCAP_SoTien CHECK (SoTien >= 0)
    );
END;
GO

-- 2. TẠO BẢNG KHAUTRUNHANVIEN (DÙNG CẤU TRÚC KIỂM TRA AN TOÀN)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = N'KHAUTRUNHANVIEN')
BEGIN
    CREATE TABLE dbo.KHAUTRUNHANVIEN (
        MaKTNV INT IDENTITY(1,1) NOT NULL,
        MaNV INT NOT NULL, -- Đồng bộ kiểu INT theo bảng NHANVIEN của TV1
        Thang INT NOT NULL,
        Nam INT NOT NULL,
        TenKhauTru NVARCHAR(100) NOT NULL,
        SoTien DECIMAL(18,2) NOT NULL CONSTRAINT DF_KHAUTRU_SoTien DEFAULT 0,
        NgayGhiNhan DATE NOT NULL CONSTRAINT DF_KHAUTRU_NgayGhiNhan DEFAULT CAST(GETDATE() AS DATE),
        LyDo NVARCHAR(255) NULL,

        -- Khóa chính
        CONSTRAINT PK_KHAUTRUNHANVIEN PRIMARY KEY CLUSTERED (MaKTNV),

        -- Khóa ngoại tham chiếu NHANVIEN(MaNV)
        CONSTRAINT FK_KHAUTRU_NHANVIEN FOREIGN KEY (MaNV)
            REFERENCES dbo.NHANVIEN(MaNV) ON DELETE NO ACTION ON UPDATE CASCADE,

        -- Ràng buộc miền giá trị hợp lệ
        CONSTRAINT CK_KHAUTRU_Thang CHECK (Thang BETWEEN 1 AND 12),
        CONSTRAINT CK_KHAUTRU_Nam CHECK (Nam >= 2020),
        CONSTRAINT CK_KHAUTRU_SoTien CHECK (SoTien >= 0)
    );
END;
GO

-- 3. TẠO CHỈ MỤC NON-CLUSTERED INDEX (OWNERSHIP CỦA TV3 THEO MA TRẬN PHÂN CÔNG)
-- Lưu ý: Index khấu trừ IX_KHAUTRU_MaNV_ThangNam thuộc ownership của TV4 nên không tạo ở đây
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = N'IX_PHUCAP_MaNV_ThangNam' AND object_id = OBJECT_ID(N'dbo.PHUCAPNHANVIEN'))
BEGIN
    CREATE NONCLUSTERED INDEX IX_PHUCAP_MaNV_ThangNam
    ON dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam)
    INCLUDE (SoTien, TenPhuCap);
END;
GO

-- 4. BỘ DỮ LIỆU MẪU KIỂM THỬ (SỬ DỤNG MaNV KIỂU INT: 1, 2, 3, 4, 5)
-- Kiểm tra có dữ liệu trước khi chèn mẫu để tránh chèn trùng lặp khi chạy lại script
IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE Thang = 9 AND Nam = 2026)
BEGIN
    -- 4.1. Dữ liệu Phụ cấp tháng 09/2026
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu) VALUES
    (1, 9, 2026, N'Phụ cấp ăn trưa', 730000, '2026-09-01', N'Phụ cấp theo ngày làm việc'),
    (1, 9, 2026, N'Hỗ trợ xăng xe', 500000, '2026-09-01', N'Đi lại công tác thường xuyên'),
    (2, 9, 2026, N'Phụ cấp ăn trưa', 730000, '2026-09-01', N'Phụ cấp cố định'),
    (2, 9, 2026, N'Phụ cấp trách nhiệm', 1500000, '2026-09-05', N'Trưởng nhóm dự án'),
    (3, 9, 2026, N'Phụ cấp ăn trưa', 730000, '2026-09-01', N'Phụ cấp cố định'),
    (4, 9, 2026, N'Phụ cấp độc hại', 1000000, '2026-09-10', N'Phòng Lab/Máy chủ');
END;
GO

IF NOT EXISTS (SELECT 1 FROM dbo.KHAUTRUNHANVIEN WHERE Thang = 9 AND Nam = 2026)
BEGIN
    -- 4.2. Dữ liệu Khấu trừ tháng 09/2026
    INSERT INTO dbo.KHAUTRUNHANVIEN (MaNV, Thang, Nam, TenKhauTru, SoTien, NgayGhiNhan, LyDo) VALUES
    (1, 9, 2026, N'Tạm ứng lương', 2000000, '2026-09-15', N'Nhân viên xin ứng giữa tháng'),
    (2, 9, 2026, N'Khấu trừ đi trễ', 150000, '2026-09-20', N'Vi phạm đi trễ 3 lần'),
    (3, 9, 2026, N'Tạm ứng lương', 1000000, '2026-09-15', N'Tạm ứng lương cá nhân'),
    (4, 9, 2026, N'Bồi hoàn tài sản', 500000, '2026-09-22', N'Làm hư hỏng thiết bị văn phòng');
END;
GO