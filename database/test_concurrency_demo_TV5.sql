-- ============================================================================
-- PROJECT: HỆ THỐNG QUẢN LÝ NHÂN SỰ VÀ TIỀN LƯƠNG (DBMS330284)
-- KỊCH BẢN MINH CHỨNG TRANH CHẤP ĐỒNG THỜI (CONCURRENCY DEMO) - TV5
-- TÁC GIẢ: TRẦN ĐỨC ANH (TV5 - MSSV: 24110155) - TUẦN 3
-- ĐỐI TƯỢNG SỞ HỮU: sp_ChotBangLuong VỚI KHÓA BI QUAN WITH (UPDLOCK, HOLDLOCK)
-- ============================================================================

/*
HƯỚNG DẪN THỰC HIỆN TRÊN SQL SERVER MANAGEMENT STUDIO (SSMS):
1. Mở Cửa sổ Query 1 (đóng vai trò Kế toán A - Session 1)
2. Mở Cửa sổ Query 2 (đóng vai trò Kế toán B - Session 2)
3. Chạy từng khối lệnh bên dưới theo đúng thứ tự mốc thời gian T0 -> T1 -> T2 -> T3
*/

-- ============================================================================
-- PHẦN A: NỘI DUNG CHẠY TRÊN SESSION 1 (KẾ TOÁN A)
-- ============================================================================
/*
USE QuanLyNhanSuTienLuong;
GO

PRINT '>>> SESSION 1: Kế toán A bắt đầu giao dịch chốt bảng lương tháng 09/2026...';
BEGIN TRANSACTION;

-- Bước 1: Khóa bi quan bản ghi kỳ lương với UPDLOCK, HOLDLOCK
-- Ngăn chặn toàn bộ session khác đọc để cập nhật hoặc sửa đổi cùng kỳ
SELECT MaBangLuong, Thang, Nam, TrangThai
FROM dbo.BANGLUONG WITH (UPDLOCK, HOLDLOCK)
WHERE Thang = 9 AND Nam = 2026;

PRINT '-> SESSION 1: Đã giữ khóa độc quyền trên kỳ lương tháng 9/2026!';
PRINT '-> Giả lập độ trễ xử lý 15 giây (kế toán đang kiểm tra số liệu cuối cùng)...';

-- Giả lập độ trễ kiểm tra trước khi ghi
WAITFOR DELAY '00:00:15';

-- Bước 2: Cập nhật chốt sổ kỳ lương
UPDATE dbo.BANGLUONG
SET TrangThai = 'DA_CHOT',
    NgayChot = GETDATE(),
    NguoiChot = N'Kế toán A (Session 1)'
WHERE Thang = 9 AND Nam = 2026;

COMMIT TRANSACTION;
PRINT '-> SESSION 1: ĐÃ CHỐT LƯƠNG THÀNH CÔNG VÀ GIẢI PHÓNG KHÓA (COMMITTED)!';
GO
*/

-- ============================================================================
-- PHẦN B: NỘI DUNG CHẠY TRÊN SESSION 2 (KẾ TOÁN B)
-- (Chạy ngay sau khi Session 1 vừa chạy được 2-3 giây)
-- ============================================================================
/*
USE QuanLyNhanSuTienLuong;
GO

PRINT '>>> SESSION 2: Kế toán B cũng gửi yêu cầu chốt kỳ lương tháng 09/2026...';
-- Lệnh này sẽ ngay lập tức bị BLOCK (treo chờ) bởi khóa UPDLOCK của Session 1
EXEC dbo.sp_ChotBangLuong 
    @Thang = 9, 
    @Nam = 2026, 
    @NguoiChot = N'Kế toán B (Session 2)';

-- HIỆN TƯỢNG QUAN SÁT ĐƯỢC:
-- 1. Trong 15 giây đầu: Session 2 hiển thị trạng thái "Executing query..." (bị chặn lại an toàn, không bị Lost Update).
-- 2. Ngay khi Session 1 COMMIT xong: Session 2 được đánh thức, đọc thấy TrangThai đã là 'DA_CHOT'
--    và lập tức ném thông báo: "Kỳ lương tháng 9/2026 đã được chốt trước đó bởi Kế toán A!".
PRINT '-> SESSION 2: HOÀN TẤT THỬ NGHIỆM! Hệ thống chống Lost Update 100% thành công!';
GO
*/

-- ============================================================================
-- PHẦN C: TRUY VẤN KIỂM TRA TRẠNG THÁI KHÓA ĐANG DIỄN RA (TẠI THỜI ĐIỂM T1)
-- Mở tab Query 3 chạy lệnh này khi Session 2 đang bị Block để xem sơ đồ khóa:
-- ============================================================================
/*
SELECT 
    tl.resource_type,
    tl.request_mode,
    tl.request_status,
    tl.request_session_id AS Session_Dang_Giu_Khoa,
    wt.blocking_session_id AS Session_Chan
FROM sys.dm_tran_locks tl
LEFT JOIN sys.dm_os_waiting_tasks wt ON tl.lock_owner_address = wt.resource_address
WHERE tl.resource_database_id = DB_ID('QuanLyNhanSuTienLuong');
*/
