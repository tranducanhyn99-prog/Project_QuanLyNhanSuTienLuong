-- ============================================================================
-- PROJECT: HỆ THỐNG QUẢN LÝ NHÂN SỰ VÀ TIỀN LƯƠNG (DBMS330284)
-- HỌC PHẦN: HỆ QUẢN TRỊ CƠ SỞ DỮ LIỆU - NHÓM 06
-- PHÂN HỆ: QUẢN LÝ CHẤM CÔNG (ATTENDANCE)
-- TÁC GIẢ: PHẠM MINH QUÂN (TV2 - MSSV: 24110311) - TUẦN 2 (TASK 2.1)
-- MÔ TẢ: BẢNG DỮ LIỆU CHAMCONG, CÁC RÀNG BUỘC TOÀN VẸN VÀ COVERING INDEX
-- ============================================================================

USE QuanLyNhanSuTienLuong;
GO

-- ============================================================================
-- 1. TẠO BẢNG CHAMCONG (KIỂM TRA AN TOÀN TRÁNH LỖI KHI CHẠY LẠI SCRIPT)
-- ============================================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = N'CHAMCONG')
BEGIN
    CREATE TABLE dbo.CHAMCONG (
        MaChamCong    INT           IDENTITY(1,1) NOT NULL,
        MaNV          INT           NOT NULL,
        NgayChamCong  DATE          NOT NULL,
        GioVao        TIME(0)       NOT NULL,
        GioRa         TIME(0)       NULL,
        TrangThai     NVARCHAR(20)  NOT NULL CONSTRAINT DF_CHAMCONG_TrangThai DEFAULT N'CO_MAT',
        GhiChu        NVARCHAR(255) NULL,

        -- Khóa chính
        CONSTRAINT PK_CHAMCONG PRIMARY KEY CLUSTERED (MaChamCong),

        -- Khóa ngoại tham chiếu NHANVIEN(MaNV)
        CONSTRAINT FK_CHAMCONG_NHANVIEN FOREIGN KEY (MaNV)
            REFERENCES dbo.NHANVIEN(MaNV),

        -- Ràng buộc duy nhất: Mỗi nhân viên chỉ có 1 bản ghi chấm công mỗi ngày
        CONSTRAINT UQ_CHAMCONG_MaNV_Ngay UNIQUE (MaNV, NgayChamCong),

        -- Ràng buộc miền giá trị hợp lệ cho cột TrangThai
        CONSTRAINT CHK_CHAMCONG_TrangThai CHECK (TrangThai IN (N'CO_MAT', N'DI_TRE', N'VE_SOM', N'VANG')),

        -- Ràng buộc ngày chấm công không vượt quá ngày hiện tại
        CONSTRAINT CHK_CHAMCONG_Ngay CHECK (NgayChamCong <= CAST(GETDATE() AS DATE))
    );
END;
GO

-- ============================================================================
-- 2. TẠO CHỈ MỤC NON-CLUSTERED COVERING INDEX (OWNERSHIP TV2 THEO MA TRẬN)
-- ============================================================================
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = N'IX_CHAMCONG_MaNV_Ngay' AND object_id = OBJECT_ID(N'dbo.CHAMCONG'))
BEGIN
    CREATE NONCLUSTERED INDEX IX_CHAMCONG_MaNV_Ngay
    ON dbo.CHAMCONG (MaNV, NgayChamCong)
    INCLUDE (GioVao, GioRa, TrangThai);
END;
GO
