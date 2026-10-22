-- ============================================================================
-- SEED DATA: Doanh nghiệp thực tế (20 nhân sự đa phòng ban, chấm công, phụ cấp, khấu trừ)
-- ============================================================================
SET NOCOUNT ON;
SET XACT_ABORT ON;
BEGIN TRANSACTION;

-- 1. Cập nhật tên thực tế chuyên nghiệp cho 2 nhân sự test ban đầu
UPDATE dbo.NHANVIEN SET HoTen = N'Nguyễn Tuấn Anh' WHERE MaNV = 1008;
UPDATE dbo.NHANVIEN SET HoTen = N'Trần Thị Bảo Bình' WHERE MaNV = 1009;

-- 2. Chèn 18 nhân sự mới
IF NOT EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE CCCD = '079080001122')
BEGIN
    INSERT INTO dbo.NHANVIEN (HoTen, NgaySinh, GioiTinh, CCCD, DiaChi, SoDienThoai, Email, NgayVaoLam, LuongCoBan, MaPB, MaCV, TrangThai)
    VALUES (N'Trần Hữu Nam', '1980-05-12', N'Nam', '079080001122', N'128 Nguyễn Huệ, P. Bến Nghé, Q.1, TP.HCM', '0912345678', 'nam.tran@peopleos.vn', '2020-01-15', 45000000, 1, 1, N'DANG_LAM_VIEC');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE CCCD = '079083002233')
BEGIN
    INSERT INTO dbo.NHANVIEN (HoTen, NgaySinh, GioiTinh, CCCD, DiaChi, SoDienThoai, Email, NgayVaoLam, LuongCoBan, MaPB, MaCV, TrangThai)
    VALUES (N'Lê Hoàng Yến', '1983-11-20', N'Nữ', '079083002233', N'45 Lê Duẩn, P. Bến Nghé, Q.1, TP.HCM', '0918765432', 'yen.le@peopleos.vn', '2020-06-01', 38000000, 1, 3, N'DANG_LAM_VIEC');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE CCCD = '079087003344')
BEGIN
    INSERT INTO dbo.NHANVIEN (HoTen, NgaySinh, GioiTinh, CCCD, DiaChi, SoDienThoai, Email, NgayVaoLam, LuongCoBan, MaPB, MaCV, TrangThai)
    VALUES (N'Phạm Thu Hà', '1987-03-15', N'Nữ', '079087003344', N'89 Điện Biên Phủ, P.15, Q. Bình Thạnh, TP.HCM', '0903112233', 'ha.pham@peopleos.vn', '2021-02-15', 25000000, 2, 2, N'DANG_LAM_VIEC');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE CCCD = '079093004455')
BEGIN
    INSERT INTO dbo.NHANVIEN (HoTen, NgaySinh, GioiTinh, CCCD, DiaChi, SoDienThoai, Email, NgayVaoLam, LuongCoBan, MaPB, MaCV, TrangThai)
    VALUES (N'Đỗ Thanh Tùng', '1993-08-25', N'Nam', '079093004455', N'215 Xô Viết Nghệ Tĩnh, P.17, Q. Bình Thạnh, TP.HCM', '0908223344', 'tung.do@peopleos.vn', '2022-03-10', 16000000, 2, 4, N'DANG_LAM_VIEC');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE CCCD = '079095005566')
BEGIN
    INSERT INTO dbo.NHANVIEN (HoTen, NgaySinh, GioiTinh, CCCD, DiaChi, SoDienThoai, Email, NgayVaoLam, LuongCoBan, MaPB, MaCV, TrangThai)
    VALUES (N'Vũ Thị Cẩm Ly', '1995-12-10', N'Nữ', '079095005566', N'54 Bạch Đằng, P.2, Q. Tân Bình, TP.HCM', '0909334455', 'ly.vu@peopleos.vn', '2022-09-01', 17500000, 2, 4, N'DANG_LAM_VIEC');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE CCCD = '079098006677')
BEGIN
    INSERT INTO dbo.NHANVIEN (HoTen, NgaySinh, GioiTinh, CCCD, DiaChi, SoDienThoai, Email, NgayVaoLam, LuongCoBan, MaPB, MaCV, TrangThai)
    VALUES (N'Nguyễn Minh Khang', '1998-06-18', N'Nam', '079098006677', N'102 Hoàng Văn Thụ, P.9, Q. Phú Nhuận, TP.HCM', '0938445566', 'khang.nguyen@peopleos.vn', '2023-05-15', 11000000, 2, 5, N'DANG_LAM_VIEC');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE CCCD = '079085007788')
BEGIN
    INSERT INTO dbo.NHANVIEN (HoTen, NgaySinh, GioiTinh, CCCD, DiaChi, SoDienThoai, Email, NgayVaoLam, LuongCoBan, MaPB, MaCV, TrangThai)
    VALUES (N'Hoàng Bích Thủy', '1985-09-02', N'Nữ', '079085007788', N'67 Nam Kỳ Khởi Nghĩa, P.7, Q.3, TP.HCM', '0913556677', 'thuy.hoang@peopleos.vn', '2020-03-01', 28000000, 3, 2, N'DANG_LAM_VIEC');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE CCCD = '079090008899')
BEGIN
    INSERT INTO dbo.NHANVIEN (HoTen, NgaySinh, GioiTinh, CCCD, DiaChi, SoDienThoai, Email, NgayVaoLam, LuongCoBan, MaPB, MaCV, TrangThai)
    VALUES (N'Đặng Quốc Huy', '1990-04-14', N'Nam', '079090008899', N'340 Cách Mạng Tháng 8, P.10, Q.3, TP.HCM', '0978667788', 'huy.dang@peopleos.vn', '2021-08-01', 20000000, 3, 3, N'DANG_LAM_VIEC');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE CCCD = '079094009900')
BEGIN
    INSERT INTO dbo.NHANVIEN (HoTen, NgaySinh, GioiTinh, CCCD, DiaChi, SoDienThoai, Email, NgayVaoLam, LuongCoBan, MaPB, MaCV, TrangThai)
    VALUES (N'Bùi Thu Trang', '1994-07-30', N'Nữ', '079094009900', N'78 Võ Văn Tần, P.6, Q.3, TP.HCM', '0989778899', 'trang.bui@peopleos.vn', '2022-04-15', 15500000, 3, 4, N'DANG_LAM_VIEC');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE CCCD = '079097010011')
BEGIN
    INSERT INTO dbo.NHANVIEN (HoTen, NgaySinh, GioiTinh, CCCD, DiaChi, SoDienThoai, Email, NgayVaoLam, LuongCoBan, MaPB, MaCV, TrangThai)
    VALUES (N'Lý Gia Hưng', '1997-10-05', N'Nam', '079097010011', N'45 Phan Đăng Lưu, P.3, Q. Bình Thạnh, TP.HCM', '0934889900', 'hung.ly@peopleos.vn', '2023-07-01', 12000000, 3, 5, N'DANG_LAM_VIEC');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE CCCD = '079088011122')
BEGIN
    INSERT INTO dbo.NHANVIEN (HoTen, NgaySinh, GioiTinh, CCCD, DiaChi, SoDienThoai, Email, NgayVaoLam, LuongCoBan, MaPB, MaCV, TrangThai)
    VALUES (N'Võ Thành Long', '1988-02-28', N'Nam', '079088011122', N'12 Võ Văn Ngân, TP. Thủ Đức, TP.HCM', '0907119988', 'long.vo@peopleos.vn', '2020-02-01', 35000000, 4, 2, N'DANG_LAM_VIEC');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE CCCD = '079092012233')
BEGIN
    INSERT INTO dbo.NHANVIEN (HoTen, NgaySinh, GioiTinh, CCCD, DiaChi, SoDienThoai, Email, NgayVaoLam, LuongCoBan, MaPB, MaCV, TrangThai)
    VALUES (N'Nguyễn Đình Trọng', '1992-05-19', N'Nam', '079092012233', N'23 Đặng Văn Bi, TP. Thủ Đức, TP.HCM', '0915228877', 'trong.nguyen@peopleos.vn', '2021-05-10', 24000000, 4, 4, N'DANG_LAM_VIEC');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE CCCD = '079096013344')
BEGIN
    INSERT INTO dbo.NHANVIEN (HoTen, NgaySinh, GioiTinh, CCCD, DiaChi, SoDienThoai, Email, NgayVaoLam, LuongCoBan, MaPB, MaCV, TrangThai)
    VALUES (N'Trịnh Mai Phương', '1996-09-14', N'Nữ', '079096013344', N'56 Lê Văn Việt, TP. Thủ Đức, TP.HCM', '0945337766', 'phuong.trinh@peopleos.vn', '2022-07-01', 18500000, 4, 4, N'DANG_LAM_VIEC');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE CCCD = '079097014455')
BEGIN
    INSERT INTO dbo.NHANVIEN (HoTen, NgaySinh, GioiTinh, CCCD, DiaChi, SoDienThoai, Email, NgayVaoLam, LuongCoBan, MaPB, MaCV, TrangThai)
    VALUES (N'Lâm Minh Quang', '1997-01-22', N'Nam', '079097014455', N'189 Kha Vạn Cân, TP. Thủ Đức, TP.HCM', '0968446655', 'quang.lam@peopleos.vn', '2022-10-15', 17000000, 4, 4, N'DANG_LAM_VIEC');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE CCCD = '079098015566')
BEGIN
    INSERT INTO dbo.NHANVIEN (HoTen, NgaySinh, GioiTinh, CCCD, DiaChi, SoDienThoai, Email, NgayVaoLam, LuongCoBan, MaPB, MaCV, TrangThai)
    VALUES (N'Chu Ngọc Ánh', '1998-11-08', N'Nữ', '079098015566', N'72 Tô Ngọc Vân, TP. Thủ Đức, TP.HCM', '0979554433', 'anh.chu@peopleos.vn', '2023-02-01', 14000000, 4, 4, N'DANG_LAM_VIEC');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE CCCD = '079099016677')
BEGIN
    INSERT INTO dbo.NHANVIEN (HoTen, NgaySinh, GioiTinh, CCCD, DiaChi, SoDienThoai, Email, NgayVaoLam, LuongCoBan, MaPB, MaCV, TrangThai)
    VALUES (N'Trần Đức Thịnh', '1999-04-03', N'Nam', '079099016677', N'95 Quang Trung, P.10, Q. Gò Vấp, TP.HCM', '0988663322', 'thinh.tran@peopleos.vn', '2023-08-15', 13500000, 4, 5, N'DANG_LAM_VIEC');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE CCCD = '079100017788')
BEGIN
    INSERT INTO dbo.NHANVIEN (HoTen, NgaySinh, GioiTinh, CCCD, DiaChi, SoDienThoai, Email, NgayVaoLam, LuongCoBan, MaPB, MaCV, TrangThai)
    VALUES (N'Dương Thảo Nhi', '2000-08-16', N'Nữ', '079100017788', N'114 Phan Văn Trị, P.7, Q. Gò Vấp, TP.HCM', '0933772211', 'nhi.duong@peopleos.vn', '2024-03-01', 10500000, 4, 5, N'DANG_LAM_VIEC');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.NHANVIEN WHERE CCCD = '079094018899')
BEGIN
    INSERT INTO dbo.NHANVIEN (HoTen, NgaySinh, GioiTinh, CCCD, DiaChi, SoDienThoai, Email, NgayVaoLam, LuongCoBan, MaPB, MaCV, TrangThai)
    VALUES (N'Hồ Viết Thắng', '1994-01-10', N'Nam', '079094018899', N'33 Nguyễn Oanh, P.17, Q. Gò Vấp, TP.HCM', '0911881100', 'thang.ho@peopleos.vn', '2022-01-05', 12000000, 4, 5, N'NGHI_VIEC');
END;

-- 3. Chèn chấm công, phụ cấp, khấu trừ cho các nhân sự mới
DECLARE @MaNV INT;
-- Nhân sự: Trần Hữu Nam
SELECT @MaNV = MaNV FROM dbo.NHANVIEN WHERE CCCD = '079080001122';
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-01')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-01', '07:45:00', '17:00:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-02')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-02', '07:46:00', '17:01:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-05')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-05', '07:47:00', '17:02:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-06')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-06', '07:48:00', '17:03:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-07')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-07', '07:49:00', '17:04:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-08')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-08', '07:50:00', '17:05:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-09')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-09', '07:51:00', '17:06:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-12')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-12', '07:52:00', '17:07:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-13')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-13', '07:53:00', '17:08:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-14')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-14', '07:54:00', '17:09:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-15')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-15', '07:55:00', '17:10:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-16')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-16', '07:56:00', '17:11:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-19')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-19', '07:57:00', '17:12:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-20')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-20', '07:58:00', '17:13:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenPhuCap = N'Phụ cấp ăn trưa')
BEGIN
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
    VALUES (@MaNV, 10, 2026, N'Phụ cấp ăn trưa', 730000, '2026-10-01', N'Hệ thống PeopleOS ghi nhận');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenPhuCap = N'Phụ cấp trách nhiệm CEO')
BEGIN
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
    VALUES (@MaNV, 10, 2026, N'Phụ cấp trách nhiệm CEO', 5000000, '2026-10-01', N'Hệ thống PeopleOS ghi nhận');
END;
-- Nhân sự: Lê Hoàng Yến
SELECT @MaNV = MaNV FROM dbo.NHANVIEN WHERE CCCD = '079083002233';
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-01')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-01', '07:48:00', '17:02:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-02')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-02', '07:49:00', '17:03:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-05')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-05', '07:50:00', '17:04:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-06')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-06', '08:18:00', '17:35:00', N'DI_TRE', N'Kẹt xe đầu giờ');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-07')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-07', '07:52:00', '17:06:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-08')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-08', '07:53:00', '17:07:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-09')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-09', '07:54:00', '17:08:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-12')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-12', '07:55:00', '17:09:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-13')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-13', '07:56:00', '17:10:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-14')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-14', '07:57:00', '17:11:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-15')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-15', '07:58:00', '17:12:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-16')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-16', '07:59:00', '17:13:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-19')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-19', '07:45:00', '17:14:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-20')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-20', '07:46:00', '17:15:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenPhuCap = N'Phụ cấp ăn trưa')
BEGIN
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
    VALUES (@MaNV, 10, 2026, N'Phụ cấp ăn trưa', 730000, '2026-10-01', N'Hệ thống PeopleOS ghi nhận');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenPhuCap = N'Phụ cấp quản lý điều hành')
BEGIN
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
    VALUES (@MaNV, 10, 2026, N'Phụ cấp quản lý điều hành', 3000000, '2026-10-01', N'Hệ thống PeopleOS ghi nhận');
END;
-- Nhân sự: Phạm Thu Hà
SELECT @MaNV = MaNV FROM dbo.NHANVIEN WHERE CCCD = '079087003344';
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-01')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-01', '07:51:00', '17:04:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-02')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-02', '07:52:00', '17:05:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-05')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-05', '07:53:00', '17:06:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-06')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-06', '07:54:00', '17:07:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-07')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-07', '07:55:00', '17:08:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-08')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-08', '07:56:00', '17:09:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-09')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-09', '07:57:00', '17:10:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-12')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-12', '07:55:00', '16:30:00', N'VE_SOM', N'Xin về sớm việc gia đình');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-13')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-13', '07:59:00', '17:12:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-14')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-14', '07:45:00', '17:13:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-15')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-15', '07:46:00', '17:14:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-16')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-16', '07:47:00', '17:15:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-19')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-19', '07:48:00', '17:16:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-20')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-20', '07:49:00', '17:17:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenPhuCap = N'Phụ cấp ăn trưa')
BEGIN
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
    VALUES (@MaNV, 10, 2026, N'Phụ cấp ăn trưa', 730000, '2026-10-01', N'Hệ thống PeopleOS ghi nhận');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenPhuCap = N'Phụ cấp chức vụ Trưởng phòng')
BEGIN
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
    VALUES (@MaNV, 10, 2026, N'Phụ cấp chức vụ Trưởng phòng', 2500000, '2026-10-01', N'Hệ thống PeopleOS ghi nhận');
END;
-- Nhân sự: Đỗ Thanh Tùng
SELECT @MaNV = MaNV FROM dbo.NHANVIEN WHERE CCCD = '079093004455';
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-01')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-01', '07:54:00', '17:06:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-02')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-02', '07:55:00', '17:07:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-05')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-05', '07:56:00', '17:08:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-06')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-06', '07:57:00', '17:09:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-07')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-07', '07:58:00', '17:10:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-08')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-08', '07:59:00', '17:11:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-09')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-09', '07:45:00', '17:12:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-12')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-12', '07:46:00', '17:13:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-13')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-13', '07:47:00', '17:14:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-14')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-14', '07:48:00', '17:15:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-15')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-15', '08:00:00', NULL, N'VANG', N'Nghỉ phép năm có đơn duyệt');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-16')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-16', '07:50:00', '17:17:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-19')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-19', '07:51:00', '17:18:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-20')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-20', '07:52:00', '17:19:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenPhuCap = N'Phụ cấp ăn trưa')
BEGIN
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
    VALUES (@MaNV, 10, 2026, N'Phụ cấp ăn trưa', 730000, '2026-10-01', N'Hệ thống PeopleOS ghi nhận');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenPhuCap = N'Phụ cấp xăng xe')
BEGIN
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
    VALUES (@MaNV, 10, 2026, N'Phụ cấp xăng xe', 500000, '2026-10-01', N'Hệ thống PeopleOS ghi nhận');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.KHAUTRUNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenKhauTru = N'Tạm ứng lương đợt 1')
BEGIN
    INSERT INTO dbo.KHAUTRUNHANVIEN (MaNV, Thang, Nam, TenKhauTru, SoTien, NgayGhiNhan, LyDo)
    VALUES (@MaNV, 10, 2026, N'Tạm ứng lương đợt 1', 2000000, '2026-10-05', N'Phiếu khấu trừ tháng 10/2026');
END;
-- Nhân sự: Vũ Thị Cẩm Ly
SELECT @MaNV = MaNV FROM dbo.NHANVIEN WHERE CCCD = '079095005566';
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-01')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-01', '07:57:00', '17:08:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-02')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-02', '07:58:00', '17:09:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-05')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-05', '07:59:00', '17:10:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-06')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-06', '07:45:00', '17:11:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-07')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-07', '07:46:00', '17:12:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-08')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-08', '07:47:00', '17:13:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-09')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-09', '07:48:00', '17:14:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-12')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-12', '07:49:00', '17:15:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-13')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-13', '07:50:00', '17:16:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-14')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-14', '07:51:00', '17:17:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-15')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-15', '07:52:00', '17:18:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-16')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-16', '07:53:00', '17:19:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-19')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-19', '07:54:00', '17:20:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-20')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-20', '07:55:00', '17:21:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenPhuCap = N'Phụ cấp ăn trưa')
BEGIN
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
    VALUES (@MaNV, 10, 2026, N'Phụ cấp ăn trưa', 730000, '2026-10-01', N'Hệ thống PeopleOS ghi nhận');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenPhuCap = N'Phụ cấp chuyên cần C&B')
BEGIN
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
    VALUES (@MaNV, 10, 2026, N'Phụ cấp chuyên cần C&B', 1000000, '2026-10-01', N'Hệ thống PeopleOS ghi nhận');
END;
-- Nhân sự: Nguyễn Minh Khang
SELECT @MaNV = MaNV FROM dbo.NHANVIEN WHERE CCCD = '079098006677';
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-01')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-01', '07:45:00', '17:10:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-02')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-02', '07:46:00', '17:11:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-05')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-05', '07:47:00', '17:12:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-06')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-06', '08:18:00', '17:35:00', N'DI_TRE', N'Kẹt xe đầu giờ');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-07')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-07', '07:49:00', '17:14:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-08')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-08', '07:50:00', '17:15:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-09')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-09', '07:51:00', '17:16:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-12')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-12', '07:52:00', '17:17:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-13')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-13', '07:53:00', '17:18:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-14')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-14', '07:54:00', '17:19:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-15')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-15', '07:55:00', '17:20:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-16')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-16', '07:56:00', '17:21:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-19')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-19', '07:57:00', '17:22:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-20')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-20', '07:58:00', '17:23:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenPhuCap = N'Phụ cấp ăn trưa')
BEGIN
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
    VALUES (@MaNV, 10, 2026, N'Phụ cấp ăn trưa', 730000, '2026-10-01', N'Hệ thống PeopleOS ghi nhận');
END;
-- Nhân sự: Hoàng Bích Thủy
SELECT @MaNV = MaNV FROM dbo.NHANVIEN WHERE CCCD = '079085007788';
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-01')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-01', '07:48:00', '17:12:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-02')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-02', '07:49:00', '17:13:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-05')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-05', '07:50:00', '17:14:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-06')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-06', '07:51:00', '17:15:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-07')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-07', '07:52:00', '17:16:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-08')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-08', '07:53:00', '17:17:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-09')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-09', '07:54:00', '17:18:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-12')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-12', '07:55:00', '17:19:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-13')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-13', '07:56:00', '17:20:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-14')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-14', '07:57:00', '17:21:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-15')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-15', '07:58:00', '17:22:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-16')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-16', '07:59:00', '17:23:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-19')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-19', '07:45:00', '17:24:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-20')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-20', '07:46:00', '17:25:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenPhuCap = N'Phụ cấp ăn trưa')
BEGIN
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
    VALUES (@MaNV, 10, 2026, N'Phụ cấp ăn trưa', 730000, '2026-10-01', N'Hệ thống PeopleOS ghi nhận');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenPhuCap = N'Phụ cấp trách nhiệm Kế toán trưởng')
BEGIN
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
    VALUES (@MaNV, 10, 2026, N'Phụ cấp trách nhiệm Kế toán trưởng', 3000000, '2026-10-01', N'Hệ thống PeopleOS ghi nhận');
END;
-- Nhân sự: Đặng Quốc Huy
SELECT @MaNV = MaNV FROM dbo.NHANVIEN WHERE CCCD = '079090008899';
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-01')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-01', '07:51:00', '17:14:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-02')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-02', '07:52:00', '17:15:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-05')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-05', '07:53:00', '17:16:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-06')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-06', '07:54:00', '17:17:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-07')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-07', '07:55:00', '17:18:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-08')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-08', '07:56:00', '17:19:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-09')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-09', '07:57:00', '17:20:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-12')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-12', '07:55:00', '16:30:00', N'VE_SOM', N'Xin về sớm việc gia đình');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-13')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-13', '07:59:00', '17:22:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-14')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-14', '07:45:00', '17:23:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-15')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-15', '07:46:00', '17:24:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-16')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-16', '07:47:00', '17:25:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-19')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-19', '07:48:00', '17:26:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-20')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-20', '07:49:00', '17:27:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenPhuCap = N'Phụ cấp ăn trưa')
BEGIN
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
    VALUES (@MaNV, 10, 2026, N'Phụ cấp ăn trưa', 730000, '2026-10-01', N'Hệ thống PeopleOS ghi nhận');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenPhuCap = N'Phụ cấp Phó phòng Kế toán')
BEGIN
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
    VALUES (@MaNV, 10, 2026, N'Phụ cấp Phó phòng Kế toán', 1500000, '2026-10-01', N'Hệ thống PeopleOS ghi nhận');
END;
-- Nhân sự: Bùi Thu Trang
SELECT @MaNV = MaNV FROM dbo.NHANVIEN WHERE CCCD = '079094009900';
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-01')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-01', '07:54:00', '17:16:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-02')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-02', '07:55:00', '17:17:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-05')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-05', '07:56:00', '17:18:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-06')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-06', '07:57:00', '17:19:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-07')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-07', '07:58:00', '17:20:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-08')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-08', '07:59:00', '17:21:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-09')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-09', '07:45:00', '17:22:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-12')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-12', '07:46:00', '17:23:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-13')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-13', '07:47:00', '17:24:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-14')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-14', '07:48:00', '17:25:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-15')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-15', '07:49:00', '17:26:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-16')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-16', '07:50:00', '17:27:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-19')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-19', '07:51:00', '17:28:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-20')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-20', '07:52:00', '17:29:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenPhuCap = N'Phụ cấp ăn trưa')
BEGIN
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
    VALUES (@MaNV, 10, 2026, N'Phụ cấp ăn trưa', 730000, '2026-10-01', N'Hệ thống PeopleOS ghi nhận');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenPhuCap = N'Phụ cấp báo cáo thuế')
BEGIN
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
    VALUES (@MaNV, 10, 2026, N'Phụ cấp báo cáo thuế', 800000, '2026-10-01', N'Hệ thống PeopleOS ghi nhận');
END;
-- Nhân sự: Lý Gia Hưng
SELECT @MaNV = MaNV FROM dbo.NHANVIEN WHERE CCCD = '079097010011';
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-01')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-01', '07:57:00', '17:18:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-02')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-02', '07:58:00', '17:19:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-05')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-05', '07:59:00', '17:20:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-06')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-06', '08:18:00', '17:35:00', N'DI_TRE', N'Kẹt xe đầu giờ');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-07')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-07', '07:46:00', '17:22:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-08')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-08', '07:47:00', '17:23:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-09')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-09', '07:48:00', '17:24:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-12')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-12', '07:49:00', '17:25:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-13')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-13', '07:50:00', '17:26:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-14')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-14', '07:51:00', '17:27:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-15')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-15', '07:52:00', '17:28:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-16')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-16', '07:53:00', '17:29:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-19')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-19', '07:54:00', '17:00:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-20')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-20', '07:55:00', '17:01:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenPhuCap = N'Phụ cấp ăn trưa')
BEGIN
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
    VALUES (@MaNV, 10, 2026, N'Phụ cấp ăn trưa', 730000, '2026-10-01', N'Hệ thống PeopleOS ghi nhận');
END;
-- Nhân sự: Võ Thành Long
SELECT @MaNV = MaNV FROM dbo.NHANVIEN WHERE CCCD = '079088011122';
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-01')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-01', '07:45:00', '17:20:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-02')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-02', '07:46:00', '17:21:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-05')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-05', '07:47:00', '17:22:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-06')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-06', '07:48:00', '17:23:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-07')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-07', '07:49:00', '17:24:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-08')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-08', '07:50:00', '17:25:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-09')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-09', '07:51:00', '17:26:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-12')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-12', '07:52:00', '17:27:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-13')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-13', '07:53:00', '17:28:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-14')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-14', '07:54:00', '17:29:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-15')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-15', '08:00:00', NULL, N'VANG', N'Nghỉ phép năm có đơn duyệt');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-16')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-16', '07:56:00', '17:01:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-19')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-19', '07:57:00', '17:02:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-20')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-20', '07:58:00', '17:03:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenPhuCap = N'Phụ cấp ăn trưa')
BEGIN
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
    VALUES (@MaNV, 10, 2026, N'Phụ cấp ăn trưa', 730000, '2026-10-01', N'Hệ thống PeopleOS ghi nhận');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenPhuCap = N'Phụ cấp Tech Lead / Kiến trúc sư')
BEGIN
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
    VALUES (@MaNV, 10, 2026, N'Phụ cấp Tech Lead / Kiến trúc sư', 3500000, '2026-10-01', N'Hệ thống PeopleOS ghi nhận');
END;
-- Nhân sự: Nguyễn Đình Trọng
SELECT @MaNV = MaNV FROM dbo.NHANVIEN WHERE CCCD = '079092012233';
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-01')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-01', '07:48:00', '17:22:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-02')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-02', '07:49:00', '17:23:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-05')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-05', '07:50:00', '17:24:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-06')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-06', '07:51:00', '17:25:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-07')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-07', '07:52:00', '17:26:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-08')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-08', '07:53:00', '17:27:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-09')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-09', '07:54:00', '17:28:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-12')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-12', '07:55:00', '17:29:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-13')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-13', '07:56:00', '17:00:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-14')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-14', '07:57:00', '17:01:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-15')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-15', '07:58:00', '17:02:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-16')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-16', '07:59:00', '17:03:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-19')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-19', '07:45:00', '17:04:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-20')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-20', '07:46:00', '17:05:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenPhuCap = N'Phụ cấp ăn trưa')
BEGIN
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
    VALUES (@MaNV, 10, 2026, N'Phụ cấp ăn trưa', 730000, '2026-10-01', N'Hệ thống PeopleOS ghi nhận');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenPhuCap = N'Phụ cấp dự án Core Backend')
BEGIN
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
    VALUES (@MaNV, 10, 2026, N'Phụ cấp dự án Core Backend', 2000000, '2026-10-01', N'Hệ thống PeopleOS ghi nhận');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.KHAUTRUNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenKhauTru = N'Tạm ứng viện phí gia đình')
BEGIN
    INSERT INTO dbo.KHAUTRUNHANVIEN (MaNV, Thang, Nam, TenKhauTru, SoTien, NgayGhiNhan, LyDo)
    VALUES (@MaNV, 10, 2026, N'Tạm ứng viện phí gia đình', 3000000, '2026-10-05', N'Phiếu khấu trừ tháng 10/2026');
END;
-- Nhân sự: Trịnh Mai Phương
SELECT @MaNV = MaNV FROM dbo.NHANVIEN WHERE CCCD = '079096013344';
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-01')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-01', '07:51:00', '17:24:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-02')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-02', '07:52:00', '17:25:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-05')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-05', '07:53:00', '17:26:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-06')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-06', '07:54:00', '17:27:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-07')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-07', '07:55:00', '17:28:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-08')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-08', '07:56:00', '17:29:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-09')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-09', '07:57:00', '17:00:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-12')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-12', '07:55:00', '16:30:00', N'VE_SOM', N'Xin về sớm việc gia đình');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-13')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-13', '07:59:00', '17:02:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-14')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-14', '07:45:00', '17:03:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-15')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-15', '07:46:00', '17:04:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-16')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-16', '07:47:00', '17:05:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-19')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-19', '07:48:00', '17:06:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-20')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-20', '07:49:00', '17:07:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenPhuCap = N'Phụ cấp ăn trưa')
BEGIN
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
    VALUES (@MaNV, 10, 2026, N'Phụ cấp ăn trưa', 730000, '2026-10-01', N'Hệ thống PeopleOS ghi nhận');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenPhuCap = N'Phụ cấp phân tích dữ liệu')
BEGIN
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
    VALUES (@MaNV, 10, 2026, N'Phụ cấp phân tích dữ liệu', 1000000, '2026-10-01', N'Hệ thống PeopleOS ghi nhận');
END;
-- Nhân sự: Lâm Minh Quang
SELECT @MaNV = MaNV FROM dbo.NHANVIEN WHERE CCCD = '079097014455';
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-01')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-01', '07:54:00', '17:26:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-02')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-02', '07:55:00', '17:27:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-05')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-05', '07:56:00', '17:28:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-06')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-06', '08:18:00', '17:35:00', N'DI_TRE', N'Kẹt xe đầu giờ');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-07')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-07', '07:58:00', '17:00:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-08')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-08', '07:59:00', '17:01:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-09')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-09', '07:45:00', '17:02:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-12')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-12', '07:46:00', '17:03:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-13')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-13', '07:47:00', '17:04:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-14')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-14', '07:48:00', '17:05:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-15')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-15', '07:49:00', '17:06:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-16')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-16', '07:50:00', '17:07:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-19')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-19', '07:51:00', '17:08:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-20')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-20', '07:52:00', '17:09:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenPhuCap = N'Phụ cấp ăn trưa')
BEGIN
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
    VALUES (@MaNV, 10, 2026, N'Phụ cấp ăn trưa', 730000, '2026-10-01', N'Hệ thống PeopleOS ghi nhận');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenPhuCap = N'Phụ cấp dự án UI/UX')
BEGIN
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
    VALUES (@MaNV, 10, 2026, N'Phụ cấp dự án UI/UX', 1500000, '2026-10-01', N'Hệ thống PeopleOS ghi nhận');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.KHAUTRUNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenKhauTru = N'Khấu trừ đi trễ 3 lần')
BEGIN
    INSERT INTO dbo.KHAUTRUNHANVIEN (MaNV, Thang, Nam, TenKhauTru, SoTien, NgayGhiNhan, LyDo)
    VALUES (@MaNV, 10, 2026, N'Khấu trừ đi trễ 3 lần', 100000, '2026-10-05', N'Phiếu khấu trừ tháng 10/2026');
END;
-- Nhân sự: Chu Ngọc Ánh
SELECT @MaNV = MaNV FROM dbo.NHANVIEN WHERE CCCD = '079098015566';
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-01')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-01', '07:57:00', '17:28:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-02')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-02', '07:58:00', '17:29:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-05')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-05', '07:59:00', '17:00:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-06')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-06', '07:45:00', '17:01:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-07')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-07', '07:46:00', '17:02:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-08')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-08', '07:47:00', '17:03:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-09')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-09', '07:48:00', '17:04:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-12')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-12', '07:49:00', '17:05:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-13')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-13', '07:50:00', '17:06:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-14')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-14', '07:51:00', '17:07:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-15')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-15', '07:52:00', '17:08:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-16')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-16', '07:53:00', '17:09:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-19')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-19', '07:54:00', '17:10:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-20')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-20', '07:55:00', '17:11:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenPhuCap = N'Phụ cấp ăn trưa')
BEGIN
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
    VALUES (@MaNV, 10, 2026, N'Phụ cấp ăn trưa', 730000, '2026-10-01', N'Hệ thống PeopleOS ghi nhận');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenPhuCap = N'Phụ cấp chứng chỉ ISTQB')
BEGIN
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
    VALUES (@MaNV, 10, 2026, N'Phụ cấp chứng chỉ ISTQB', 800000, '2026-10-01', N'Hệ thống PeopleOS ghi nhận');
END;
-- Nhân sự: Trần Đức Thịnh
SELECT @MaNV = MaNV FROM dbo.NHANVIEN WHERE CCCD = '079099016677';
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-01')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-01', '07:45:00', '17:00:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-02')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-02', '07:46:00', '17:01:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-05')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-05', '07:47:00', '17:02:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-06')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-06', '07:48:00', '17:03:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-07')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-07', '07:49:00', '17:04:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-08')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-08', '07:50:00', '17:05:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-09')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-09', '07:51:00', '17:06:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-12')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-12', '07:52:00', '17:07:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-13')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-13', '07:53:00', '17:08:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-14')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-14', '07:54:00', '17:09:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-15')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-15', '07:55:00', '17:10:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-16')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-16', '07:56:00', '17:11:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-19')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-19', '07:57:00', '17:12:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-20')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-20', '07:58:00', '17:13:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenPhuCap = N'Phụ cấp ăn trưa')
BEGIN
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
    VALUES (@MaNV, 10, 2026, N'Phụ cấp ăn trưa', 730000, '2026-10-01', N'Hệ thống PeopleOS ghi nhận');
END;
IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenPhuCap = N'Phụ cấp trực server cuối tuần')
BEGIN
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
    VALUES (@MaNV, 10, 2026, N'Phụ cấp trực server cuối tuần', 1200000, '2026-10-01', N'Hệ thống PeopleOS ghi nhận');
END;
-- Nhân sự: Dương Thảo Nhi
SELECT @MaNV = MaNV FROM dbo.NHANVIEN WHERE CCCD = '079100017788';
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-01')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-01', '07:48:00', '17:02:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-02')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-02', '07:49:00', '17:03:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-05')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-05', '07:50:00', '17:04:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-06')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-06', '07:51:00', '17:05:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-07')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-07', '07:52:00', '17:06:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-08')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-08', '07:53:00', '17:07:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-09')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-09', '07:54:00', '17:08:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-12')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-12', '07:55:00', '17:09:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-13')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-13', '07:56:00', '17:10:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-14')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-14', '07:57:00', '17:11:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-15')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-15', '07:58:00', '17:12:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-16')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-16', '07:59:00', '17:13:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-19')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-19', '07:45:00', '17:14:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.CHAMCONG WHERE MaNV = @MaNV AND NgayChamCong = '2026-10-20')
BEGIN
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (@MaNV, '2026-10-20', '07:46:00', '17:15:00', N'CO_MAT', NULL);
END;
IF NOT EXISTS (SELECT 1 FROM dbo.PHUCAPNHANVIEN WHERE MaNV = @MaNV AND Thang = 10 AND Nam = 2026 AND TenPhuCap = N'Phụ cấp ăn trưa')
BEGIN
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)
    VALUES (@MaNV, 10, 2026, N'Phụ cấp ăn trưa', 730000, '2026-10-01', N'Hệ thống PeopleOS ghi nhận');
END;

COMMIT TRANSACTION;
GO

-- 4. Tính lại bảng lương tháng 10/2026 để cập nhật toàn bộ nhân viên mới
DECLARE @OutMaBL INT;
EXEC dbo.sp_TinhBangLuongThang @Thang = 10, @Nam = 2026, @NgayCongChuan = 26, @MaBangLuong = @OutMaBL OUTPUT;
PRINT N'Đã hoàn tất tính bảng lương tháng 10/2026, MaBangLuong = ' + CAST(@OutMaBL AS NVARCHAR(20));
GO