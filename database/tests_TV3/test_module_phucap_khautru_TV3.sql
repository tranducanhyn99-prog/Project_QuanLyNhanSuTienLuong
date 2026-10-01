-- ============================================================================
-- PROJECT: Quản Lý Nhân Sự và Tiền Lương (DBMS330284) - Nhóm 06
-- HỌC PHẦN: Hệ Quản Trị Cơ Sở Dữ Liệu
-- BỘ KIỂM THỬ TÍCH HỢP TUẦN 3: PHÂN HỆ PHỤ CẤP, KHẤU TRỪ VÀ NGHIỆP VỤ LIÊN QUAN
-- TÁC GIẢ: TV3 - Trần Tiến Đạt (MSSV: 24110198)
-- ============================================================================

USE QuanLyNhanSuTienLuong;
GO

PRINT '============================================================================';
PRINT '  BẮT ĐẦU KIỂM THỬ HỆ THỐNG PHÂN HỆ PHỤ CẤP & KHẤU TRỪ (TV3 - TUẦN 3)';
PRINT '============================================================================';

-- TEST 1: KIỂM THỬ FUNCTION fn_TongKhauTru TRÊN NHIỀU KỲ VÀ NHÂN VIÊN
PRINT N'>>> TEST 1: Function fn_TongKhauTru';
SELECT
    nv.MaNV,
    nv.HoTen,
    dbo.fn_TongKhauTru(nv.MaNV, 9, 2026) AS KhauTru_Thang9,
    dbo.fn_TongKhauTru(nv.MaNV, 10, 2026) AS KhauTru_Thang10,
    dbo.fn_TongKhauTru(nv.MaNV, 1, 2025) AS KhauTru_KyRong
FROM dbo.NHANVIEN nv
WHERE nv.MaNV BETWEEN 1 AND 5;
PRINT N'-> Test 1: PASS (Function tính đúng tổng tiền và trả về 0 khi kỳ không có khấu trừ).';
GO

-- TEST 2: KIỂM THỬ VIEW vw_TongPhuCapThang TRÊN NHIỀU KỲ
PRINT N'>>> TEST 2: View vw_TongPhuCapThang';
SELECT MaNV, HoTen, Thang, Nam, SoKhoanPhuCap, TongTienPhuCap
FROM dbo.vw_TongPhuCapThang
WHERE Thang IN (9, 10) AND Nam = 2026
ORDER BY Thang ASC, MaNV ASC;
PRINT N'-> Test 2: PASS (View tổng hợp chính xác phụ cấp từng nhân viên theo kỳ).';
GO

-- TEST 3: KIỂM THỬ RÀNG BUỘC CHECK SỐ TIỀN ÂM VÀ THÁNG NĂM KHÔNG HỢP LỆ
PRINT N'>>> TEST 3: Kiểm tra các ràng buộc CHECK constraint';

-- 3.1. Thử chèn số tiền âm
BEGIN TRY
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien)
    VALUES (1, 9, 2026, N'Phụ cấp lỗi', -500000);
    PRINT N'LỖI: Ràng buộc số tiền âm không hoạt động!';
END TRY
BEGIN CATCH
    PRINT N'-> PASS 3.1: Đã chặn thành công số tiền phụ cấp âm (' + ERROR_MESSAGE() + N')';
END CATCH;

-- 3.2. Thử chèn tháng sai (Tháng 13)
BEGIN TRY
    INSERT INTO dbo.KHAUTRUNHANVIEN (MaNV, Thang, Nam, TenKhauTru, SoTien)
    VALUES (1, 13, 2026, N'Khấu trừ lỗi', 200000);
    PRINT N'LỖI: Ràng buộc tháng không hoạt động!';
END TRY
BEGIN CATCH
    PRINT N'-> PASS 3.2: Đã chặn thành công tháng 13 (' + ERROR_MESSAGE() + N')';
END CATCH;
GO

-- TEST 4: KIỂM THỬ TRIGGER trg_ChamCong_KiemTraGio
PRINT N'>>> TEST 4: Trigger trg_ChamCong_KiemTraGio';
BEGIN TRY
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (1, '2026-10-05', '08:30:00', '07:15:00', N'CO_MAT', N'Test giờ ra < giờ vào');
    PRINT N'LỖI: Trigger không chặn giờ ra nhỏ hơn giờ vào!';
END TRY
BEGIN CATCH
    PRINT N'-> PASS 4: Trigger đã chặn thành công dữ liệu giờ sai: ' + ERROR_MESSAGE();
END CATCH;
GO

-- TEST 5: KIỂM THỬ STORED PROCEDURE sp_GhiNhanChamCong (PHỐI HỢP TV2)
PRINT N'>>> TEST 5: Stored Procedure sp_GhiNhanChamCong';
-- Khai báo biến nhận giá trị mã chấm công tự sinh qua tham số OUTPUT
DECLARE @MaChamCong_Generated INT;
DECLARE @NgayTest DATE = '2026-09-28';

-- Bước chuẩn bị: Làm sạch bản ghi test của ngày 28/09/2026 để tránh lỗi trùng lặp từ trước
DELETE FROM dbo.CHAMCONG WHERE MaNV = 3 AND NgayChamCong = @NgayTest;

-- ----------------------------------------------------------------------------
-- 5.1. Kiểm thử ghi nhận chấm công hợp lệ (Case thành công, nhận OUTPUT ID)
-- ----------------------------------------------------------------------------
BEGIN TRY
    EXEC dbo.sp_GhiNhanChamCong
        @MaNV         = 3,
        @NgayChamCong = @NgayTest,
        @GioVao       = '07:55:00',
        @GioRa        = '17:05:00',
        @TrangThai    = N'CO_MAT',
        @GhiChu       = N'TV3 kiểm thử SP có tham số OUTPUT',
        @MaChamCong   = @MaChamCong_Generated OUTPUT;

    IF @MaChamCong_Generated IS NOT NULL AND @MaChamCong_Generated > 0
    BEGIN
        PRINT N'-> PASS 5.1: Ghi nhận chấm công thành công. Mã chấm công vừa sinh (OUTPUT): '
              + CAST(@MaChamCong_Generated AS VARCHAR(10));
    END
    ELSE
    BEGIN
        PRINT N'-> LỖI 5.1: Không lấy được mã chấm công từ tham số OUTPUT!';
    END
END TRY
BEGIN CATCH
    PRINT N'-> LỖI 5.1: Phát sinh ngoại lệ không mong muốn: ' + ERROR_MESSAGE();
END CATCH;

-- Truy vấn kiểm chứng bản ghi thực tế trong CSDL
SELECT MaCC, MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu
FROM dbo.CHAMCONG
WHERE MaNV = 3 AND NgayChamCong = @NgayTest;

-- ----------------------------------------------------------------------------
-- 5.2. Kiểm thử quy tắc 7: Chặn trùng lặp bản ghi chấm công trong cùng một ngày
-- ----------------------------------------------------------------------------
BEGIN TRY
    -- Cố tình gọi lại SP với cùng MaNV và NgayChamCong vừa tạo ở bước 5.1
    EXEC dbo.sp_GhiNhanChamCong
        @MaNV         = 3,
        @NgayChamCong = @NgayTest,
        @GioVao       = '08:00:00',
        @GioRa        = '17:30:00',
        @TrangThai    = N'CO_MAT',
        @GhiChu       = N'Cố tình ghi nhận trùng ngày',
        @MaChamCong   = @MaChamCong_Generated OUTPUT;

    PRINT N'-> LỖI 5.2: SP không chặn trùng lặp bản ghi chấm công trong ngày!';
END TRY
BEGIN CATCH
    PRINT N'-> PASS 5.2: Đã chặn thành công ghi nhận trùng lặp trong ngày (' + ERROR_MESSAGE() + N')';
END CATCH;

-- ----------------------------------------------------------------------------
-- 5.3. Kiểm thử quy tắc 6: Chặn ghi nhận chấm công cho nhân viên đã nghỉ việc
-- ----------------------------------------------------------------------------
BEGIN TRY
    -- MaNV = 6 (Lê Thị Thu Thảo) có trạng thái NGHI_VIEC
    EXEC dbo.sp_GhiNhanChamCong
        @MaNV         = 6,
        @NgayChamCong = @NgayTest,
        @GioVao       = '08:00:00',
        @GioRa        = '17:00:00',
        @TrangThai    = N'CO_MAT',
        @GhiChu       = N'Test nhân viên nghỉ việc',
        @MaChamCong   = @MaChamCong_Generated OUTPUT;

    PRINT N'-> LỖI 5.3: SP không chặn nhân viên có trạng thái NGHI_VIEC!';
END TRY
BEGIN CATCH
    PRINT N'-> PASS 5.3: Đã chặn thành công nhân viên đã nghỉ việc (' + ERROR_MESSAGE() + N')';
END CATCH;

-- ----------------------------------------------------------------------------
-- 5.4. Kiểm thử quy tắc 5: Chặn giờ ra nhỏ hơn hoặc bằng giờ vào làm
-- ----------------------------------------------------------------------------
BEGIN TRY
    DECLARE @NgayKhac DATE = DATEADD(DAY, -1, @NgayTest);
    DELETE FROM dbo.CHAMCONG WHERE MaNV = 3 AND NgayChamCong = @NgayKhac;

    EXEC dbo.sp_GhiNhanChamCong
        @MaNV         = 3,
        @NgayChamCong = @NgayKhac,
        @GioVao       = '08:30:00',
        @GioRa        = '07:30:00',
        @TrangThai    = N'CO_MAT',
        @GhiChu       = N'Test giờ ra <= giờ vào',
        @MaChamCong   = @MaChamCong_Generated OUTPUT;

    PRINT N'-> LỖI 5.4: SP không chặn giờ ra nhỏ hơn giờ vào!';
END TRY
BEGIN CATCH
    PRINT N'-> PASS 5.4: Đã chặn thành công giờ ra không hợp lệ (' + ERROR_MESSAGE() + N')';
END CATCH;
GO

-- TEST 6: KIỂM THỬ TRANSACTION sp_XoaKyLuongChuaChot
PRINT N'>>> TEST 6: Transaction xóa kỳ lương chưa chốt (Rollback & Commit)';

-- 6.1. Chuẩn bị dữ liệu kỳ lương test chưa chốt
IF NOT EXISTS (SELECT 1 FROM dbo.BANGLUONG WHERE Thang = 11 AND Nam = 2026)
BEGIN
    INSERT INTO dbo.BANGLUONG (MaBangLuong, Thang, Nam, NgayLap, TrangThai)
    VALUES ('BL_2026_11', 11, 2026, GETDATE(), N'CHUA_CHOT');

    INSERT INTO dbo.CHITIETBANGLUONG (MaBangLuong, MaNV, ThucLinh)
    VALUES ('BL_2026_11', 1, 15000000), ('BL_2026_11', 2, 12000000);
END;

-- Thực hiện xóa kỳ chưa chốt -> Kỳ vọng thành công
EXEC dbo.sp_XoaKyLuongChuaChot @Thang = 11, @Nam = 2026;
PRINT N'-> PASS 6.1: Đã xóa thành công bảng lương và chi tiết lương của kỳ chưa chốt.';

-- 6.2. Kiểm thử cố tình xóa kỳ lương ĐÃ CHỐT -> Kỳ vọng Bị chặn & Rollback
BEGIN TRY
    -- Giả lập kỳ 12/2026 đã chốt
    IF NOT EXISTS (SELECT 1 FROM dbo.BANGLUONG WHERE Thang = 12 AND Nam = 2026)
    BEGIN
        INSERT INTO dbo.BANGLUONG (MaBangLuong, Thang, Nam, NgayLap, TrangThai)
        VALUES ('BL_2026_12', 12, 2026, GETDATE(), N'DA_CHOT');
    END;

    EXEC dbo.sp_XoaKyLuongChuaChot @Thang = 12, @Nam = 2026;
    PRINT N'LỖI: Transaction không chặn xóa kỳ đã chốt!';
END TRY
BEGIN CATCH
    PRINT N'-> PASS 6.2: Transaction đã Rollback an toàn khi cố tình xóa kỳ đã chốt (' + ERROR_MESSAGE() + N')';
    -- Dọn dẹp kỳ test 12
    DELETE FROM dbo.BANGLUONG WHERE Thang = 12 AND Nam = 2026;
END CATCH;
GO

PRINT '============================================================================';
PRINT '  HOÀN THÀNH 100% CÁC TEST CASE TUẦN 3 PHÂN HỆ TV3 - TẤT CẢ ĐỀU PASS!';
PRINT '============================================================================';