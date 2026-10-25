# TRƯỜNG ĐẠI HỌC CÔNG NGHỆ KỸ THUẬT TP. HỒ CHÍ MINH (HCMUTE)

## KHOA CÔNG NGHỆ THÔNG TIN — BỘ MÔN HỆ THỐNG THÔNG TIN

### CẨM NANG THỰC HÀNH & KỊCH BẢN TRUY VẤN TEST SQL

* **Đồ án môn học:** Hệ Quản Trị Cơ Sở Dữ Liệu (DBMS330284)
* **Đề tài:** Hệ Thống Quản Lý Nhân Sự & Tiền Lương
* **Sinh viên thực hiện:** Nguyễn Minh Trí (TV1) — MSSV: **24110359**
* **Giảng viên hướng dẫn:** TS. Phan Thị Thể

---

> [!IMPORTANT]
> **Quy ước & Hướng dẫn sử dụng:**
>
> * Trước khi chạy bất kỳ câu lệnh nào trên SSMS, luôn đảm bảo mở cửa sổ **New Query** và thực thi:
>   ```sql
>   USE QuanLyNhanSuTienLuong;
>   GO
>   ```
> * Tất cả các đoạn code SQL bên dưới được đóng khối chuẩn xác, bạn chỉ cần bấm nút **Copy**, dán vào SSMS và ấn phím **`F5`** (Execute) để chạy ngay.
> * Với các bài test Benchmark Index: Hãy bấm tổ hợp phím **`Ctrl + M`** (Include Actual Execution Plan) trước khi ấn `F5` để xem cây thực thi trực quan.

---

# PHẦN 1: KỊCH BẢN TEST CỦA THÀNH VIÊN 1 (TRÍ - NHÂN SỰ & NỀN MÓNG CSDL)

## 1.1. Kiểm tra các ràng buộc toàn vẹn (Check Constraints)

### Test 1.1.1: Kiểm tra ràng buộc độ tuổi lao động >= 18 tuổi (`CHK_NHANVIEN_DoTuoi`)

* **Mục đích:** Chứng minh công thức `NgaySinh <= DATEADD(YEAR, -18, NgayVaoLam)` bắt chính xác từng ngày sinh nhật, chặn đứng trường hợp chưa đủ 18 tuổi mà hàm `DATEDIFF` thông thường hay bị bỏ lọt.

```sql
USE QuanLyNhanSuTienLuong;
GO

-- Ca vi phạm: Người sinh 31/12/2006, vào làm 01/01/2024 (chưa tới sinh nhật thứ 18)
INSERT INTO dbo.NHANVIEN (HoTen, NgaySinh, GioiTinh, CCCD, SoDienThoai, Email, NgayVaoLam, LuongCoBan, MaPB, MaCV)
VALUES (N'Lê Văn Test Tuổi', '2006-12-31', N'Nam', '079200009901', '0901239901', 'tuoi_fail@gmail.com', '2024-01-01', 12000000, 1, 1);
```

* **Kết quả mong đợi:** SQL Server báo lỗi `Msg 547`: *The INSERT statement conflicted with the CHECK constraint 'CHK_NHANVIEN_DoTuoi'*. Câu lệnh bị chặn, bảo vệ toàn vẹn dữ liệu.
* **Lời giải thích khi Vấn đáp:** *"Dạ thưa Cô, nếu dùng DATEDIFF(YEAR, ...) thì năm 2024 - 2006 = 18 sẽ bị lọt ca sai. Dùng công thức DATEADD của em bắt buộc phải qua đúng ngày sinh nhật thứ 18 mới hợp lệ ạ!"*

---

### Test 1.1.2: Kiểm tra định dạng Căn cước công dân (`CHK_NHANVIEN_CCCD`)

* **Mục đích:** Chứng minh CCCD phải đủ đúng 12 chữ số và không chứa bất kỳ ký tự chữ cái hay ký tự đặc biệt nào.

```sql
USE QuanLyNhanSuTienLuong;
GO

-- Ca vi phạm: CCCD chứa ký tự chữ cái 'A' hoặc thiếu số
INSERT INTO dbo.NHANVIEN (HoTen, NgaySinh, GioiTinh, CCCD, SoDienThoai, Email, NgayVaoLam, LuongCoBan, MaPB, MaCV)
VALUES (N'Trần Sai CCCD', '1995-05-10', N'Nam', '0792000012A', '0901239902', 'cccd_fail@gmail.com', '2023-01-01', 12000000, 1, 1);
```

* **Kết quả mong đợi:** Báo lỗi `Msg 547`: *The INSERT statement conflicted with the CHECK constraint 'CHK_NHANVIEN_CCCD'*.

---

### Test 1.1.3: Kiểm tra định dạng Số điện thoại (`CHK_NHANVIEN_SDT`)

* **Mục đích:** Số điện thoại phải đúng 10 chữ số và bắt buộc chữ số đầu tiên phải là số `0`.

```sql
USE QuanLyNhanSuTienLuong;
GO

-- Ca vi phạm: Số điện thoại không bắt đầu bằng số 0 (bắt đầu bằng số 8)
INSERT INTO dbo.NHANVIEN (HoTen, NgaySinh, GioiTinh, CCCD, SoDienThoai, Email, NgayVaoLam, LuongCoBan, MaPB, MaCV)
VALUES (N'Nguyễn Sai SĐT', '1995-05-10', N'Nam', '079200009903', '8901239903', 'sdt_fail@gmail.com', '2023-01-01', 12000000, 1, 1);
```

* **Kết quả mong đợi:** Báo lỗi `Msg 547`: *The INSERT statement conflicted with the CHECK constraint 'CHK_NHANVIEN_SDT'*.

---

### Test 1.1.4: Kiểm tra Lương cơ bản phải lớn hơn 0 (`CHK_NHANVIEN_LuongCoBan`)

* **Mục đích:** Mức lương thỏa thuận hợp đồng bắt buộc phải là số dương lớn hơn 0.

```sql
USE QuanLyNhanSuTienLuong;
GO

-- Ca vi phạm: Nhập mức lương cơ bản = 0 hoặc âm
INSERT INTO dbo.NHANVIEN (HoTen, NgaySinh, GioiTinh, CCCD, SoDienThoai, Email, NgayVaoLam, LuongCoBan, MaPB, MaCV)
VALUES (N'Phạm Lương Không Đồng', '1995-05-10', N'Nam', '079200009904', '0901239904', 'luong0_fail@gmail.com', '2023-01-01', 0, 1, 1);
```

* **Kết quả mong đợi:** Báo lỗi `Msg 547`: *The INSERT statement conflicted with the CHECK constraint 'CHK_NHANVIEN_LuongCoBan'*.

---

## 1.2. Kiểm tra Stored Procedure & Quản lý giao dịch (`sp_ThemNhanVien`)

### Test 1.2.1: Tiếp nhận nhân sự mới và tự động tạo tài khoản thành công (Happy Path)

* **Mục đích:** Chạy thủ tục `sp_ThemNhanVien` với cờ `@TaoTaiKhoan = 1` để chèn dữ liệu vào bảng `NHANVIEN` và `TAIKHOAN` trong cùng một giao dịch an toàn (ACID).

```sql
USE QuanLyNhanSuTienLuong;
GO

DECLARE @NewMaNV INT;

EXEC dbo.sp_ThemNhanVien
    @HoTen         = N'Đỗ Minh Hoàn Hảo',
    @NgaySinh      = '1998-08-20',
    @GioiTinh      = N'Nam',
    @CCCD          = '079198888001',
    @DiaChi        = N'Quận 1, TP.HCM',
    @SoDienThoai   = '0918888001',
    @Email         = 'dominhhoanhao@company.com',
    @NgayVaoLam    = '2024-01-15',
    @LuongCoBan    = 18000000,
    @MaPB          = 2, -- Phòng Nhân Sự
    @MaCV          = 4, -- Chuyên Viên
    @TaoTaiKhoan   = 1,
    @TenDangNhap   = 'nv_hoanhao',
    @MatKhauSHA256 = 'pbkdf2-sha256$600000$demo_hash_hoanhao',
    @VaiTro        = 'Employee',
    @NewMaNV       = @NewMaNV OUTPUT;

-- Kiểm tra kết quả tạo ra ở cả 2 bảng:
SELECT MaNV, HoTen, Email, LuongCoBan, TrangThai FROM dbo.NHANVIEN WHERE MaNV = @NewMaNV;
SELECT MaTK, MaNV, TenDangNhap, VaiTro, TrangThai FROM dbo.TAIKHOAN WHERE MaNV = @NewMaNV;
```

* **Kết quả mong đợi:** Thủ tục thực thi thành công. Cả 2 câu SELECT đều trả về 1 dòng: Nhân viên có mã mới và tài khoản tương ứng đã được cấp với quyền `Employee`.

---

### Test 1.2.2: Kiểm tra tính Nguyên tử (Atomicity) — Tự động Rollback khi trùng Username

* **Mục đích:** Chứng minh cơ chế `TRY...CATCH` và `ROLLBACK TRANSACTION`. Nếu bước tạo tài khoản bị lỗi trùng tên đăng nhập, toàn bộ transaction bị hủy, nhân viên vừa thêm không được phép tồn tại (không để lại dữ liệu rác).

```sql
USE QuanLyNhanSuTienLuong;
GO

DECLARE @FailedMaNV INT;

-- Cố tình dùng lại @TenDangNhap = 'nv_hoanhao' vừa tạo ở Test 1.2.1
EXEC dbo.sp_ThemNhanVien
    @HoTen         = N'Người Thất Bại Rollback',
    @NgaySinh      = '1999-09-09',
    @GioiTinh      = N'Nữ',
    @CCCD          = '079199999002',
    @DiaChi        = N'Thủ Đức, TP.HCM',
    @SoDienThoai   = '0919999002',
    @Email         = 'rollback_fail@company.com',
    @NgayVaoLam    = '2024-02-01',
    @LuongCoBan    = 16000000,
    @MaPB          = 2,
    @MaCV          = 5,
    @TaoTaiKhoan   = 1,
    @TenDangNhap   = 'nv_hoanhao', -- TRÙNG USERNAME ĐÃ CÓ!
    @MatKhauSHA256 = 'hash_dummy',
    @VaiTro        = 'Employee',
    @NewMaNV       = @FailedMaNV OUTPUT;

-- KIỂM TRA TOÀN VẸN: 'Người Thất Bại Rollback' KHÔNG ĐƯỢC PHÉP TỒN TẠI!
SELECT * FROM dbo.NHANVIEN WHERE HoTen = N'Người Thất Bại Rollback';
```

* **Kết quả mong đợi:** Báo lỗi `Msg 50000`: *'Tên đăng nhập đã tồn tại trong hệ thống!'*. Câu lệnh SELECT trả về 0 dòng, chứng minh transaction đã rollback sạch sẽ.

---

## 1.3. Kiểm tra các bộ kích hoạt tự động (Triggers của TV1)

### Test 1.3.1: Kiểm tra trigger tự động lưu lịch sử mức lương hiệu lực (`trg_NhanVien_LichSuLuong`)

* **Mục đích:** Khi cập nhật tăng/giảm lương của nhân viên, trigger tự động bắt sự kiện và ghi một bản ghi mới vào bảng `LICHSULUONG` với mốc ngày đầu tháng.

```sql
USE QuanLyNhanSuTienLuong;
GO

DECLARE @TestMaNV INT;
SELECT TOP 1 @TestMaNV = MaNV FROM dbo.NHANVIEN WHERE CCCD = '079198888001';

-- Tiến hành cập nhật tăng lương từ 18 triệu lên 25 triệu:
UPDATE dbo.NHANVIEN
SET LuongCoBan = 25000000
WHERE MaNV = @TestMaNV;

-- KIỂM TRA BẢNG LICHSULUONG XEM DÒNG MỚI ĐÃ TỰ ĐỘNG XUẤT HIỆN CHƯA:
SELECT h.MaNV, nv.HoTen, h.TuThang, h.LuongCoBan
FROM dbo.LICHSULUONG h
JOIN dbo.NHANVIEN nv ON h.MaNV = nv.MaNV
WHERE h.MaNV = @TestMaNV
ORDER BY h.TuThang DESC;
```

* **Kết quả mong đợi:** Bảng `LICHSULUONG` tự động xuất hiện dòng mới với `LuongCoBan = 25000000` và `TuThang` là ngày đầu tháng hiện tại mà không cần viết câu lệnh INSERT thủ công.

---

### Test 1.3.2: Kiểm tra trigger tự động gán ngày nghỉ việc (`trg_NhanVien_LichSuLuong`)

* **Mục đích:** Khi chuyển trạng thái nhân viên sang `'NGHI_VIEC'`, trigger tự động gán ngày hiện tại vào cột `NgayNghiViec`.

```sql
USE QuanLyNhanSuTienLuong;
GO

DECLARE @TestMaNV INT;
SELECT TOP 1 @TestMaNV = MaNV FROM dbo.NHANVIEN WHERE CCCD = '079198888001';

-- Cập nhật trạng thái sang NGHI_VIEC:
UPDATE dbo.NHANVIEN
SET TrangThai = N'NGHI_VIEC'
WHERE MaNV = @TestMaNV;

-- Kiểm tra cột NgayNghiViec:
SELECT MaNV, HoTen, TrangThai, NgayNghiViec
FROM dbo.NHANVIEN
WHERE MaNV = @TestMaNV;
```

* **Kết quả mong đợi:** Cột `TrangThai` đổi thành `'NGHI_VIEC'` và cột `NgayNghiViec` tự động nhận ngày hôm nay (không còn là `NULL`).

---

### Test 1.3.3: Kiểm tra trigger chặn xóa nhân viên đã có phát sinh lương (`trg_NhanVien_KhongXoaKhiDaPhatSinhLuong`)

* **Mục đích:** Bảo vệ toàn vẹn lịch sử tài chính doanh nghiệp — cấm xóa cứng nhân viên nếu đã có dữ liệu chấm công hoặc chi tiết bảng lương.

```sql
USE QuanLyNhanSuTienLuong;
GO

-- Thử xóa nhân viên số 3 (đã có dữ liệu chấm công hoặc lương):
DELETE FROM dbo.NHANVIEN WHERE MaNV = 3;
```

* **Kết quả mong đợi:** Báo lỗi `Msg 50000`: *'Không được phép xóa nhân viên đã có dữ liệu chấm công hoặc lương. Vui lòng chuyển trạng thái sang NGHI_VIEC!'*.

---

## 1.4. Kiểm tra Khung nhìn phẳng hóa dữ liệu (`vw_NhanVien_PhongBan_ChucVu`)

### Test 1.4.1: Tra cứu hồ sơ nhân sự kèm tên phòng ban, chức vụ và tài khoản

* **Mục đích:** View kết nối 4 bảng `NHANVIEN`, `PHONGBAN`, `CHUCVU`, `TAIKHOAN`, che giấu các phép JOIN phức tạp cho tầng Java Swing.

```sql
USE QuanLyNhanSuTienLuong;
GO

-- 1. Xem toàn bộ danh sách nhân sự đầy đủ thông tin:
SELECT MaNV, HoTen, TenPB, TenCV, PhuCapChucVu, LuongCoBan, TenDangNhap, VaiTro, TrangThai
FROM dbo.vw_NhanVien_PhongBan_ChucVu;

-- 2. Lọc nhân viên theo phòng ban:
SELECT MaNV, HoTen, TenCV, LuongCoBan
FROM dbo.vw_NhanVien_PhongBan_ChucVu
WHERE TenPB = N'Ban Giám Đốc';

-- 3. Tìm kiếm theo họ tên (mô phỏng ô tìm kiếm trên Java UI):
SELECT MaNV, HoTen, SoDienThoai, Email, TenPB, TenCV
FROM dbo.vw_NhanVien_PhongBan_ChucVu
WHERE HoTen LIKE N'%Minh%';
```

* **Kết quả mong đợi:** Dữ liệu trả về đầy đủ tên chữ rõ ràng, thuận tiện cho Java Swing đổ trực tiếp lên bảng dữ liệu (`JTable`).

---

## 1.5. Kiểm tra Hàm tính toán ngày công (`fn_TinhSoNgayCong`)

### Test 1.5.1: Gọi hàm đếm số ngày công hợp lệ trong tháng của nhân viên

* **Mục đích:** Hàm nhận vào `MaNV`, `Tháng`, `Năm` và trả về tổng số ngày công (đếm các trạng thái `CO_MAT`, `DI_TRE`, `VE_SOM`).

```sql
USE QuanLyNhanSuTienLuong;
GO

-- Kiểm tra số ngày công của nhân viên số 3 trong tháng 10 năm 2026:
SELECT 
    MaNV,
    HoTen,
    dbo.fn_TinhSoNgayCong(MaNV, 10, 2026) AS SoNgayCongThang10
FROM dbo.NHANVIEN
WHERE MaNV = 3;

-- Kiểm tra trường hợp nhân viên không đi làm ngày nào (trả về 0.0 thay vì NULL):
SELECT dbo.fn_TinhSoNgayCong(9999, 10, 2026) AS CongNhanVienKhongDiLam;
```

* **Kết quả mong đợi:** Trả về số ngày công hợp lệ (ví dụ: `22.0`). Nếu không có công thì trả về `0.0`, tránh lỗi cộng trừ `NULL` ở bước tính lương.

---

## 1.6. Kiểm tra Chỉ mục bao phủ & Đo lường Benchmark (`IX_NHANVIEN_HoTen`)

### Test 1.6.1: Đo lường so sánh Clustered Index Scan vs Index Seek trên Fixture 5.000 dòng

* **Mục đích:** Bật bộ đếm I/O và Execution Plan để chứng minh Covering Index giúp giảm từ 75 xuống 10 logical reads và triệt tiêu 100% Key Lookup.

```sql
USE QuanLyNhanSuTienLuong;
GO
SET NOCOUNT ON;

-- 1. Tạo bảng tạm độc lập để benchmark
CREATE TABLE #NhanVienBenchmark(
    MaNV INT NOT NULL PRIMARY KEY, 
    HoTen NVARCHAR(100) NOT NULL,
    SoDienThoai VARCHAR(15), 
    Email VARCHAR(100), 
    MaPB INT, 
    MaCV INT, 
    TrangThai NVARCHAR(20)
);

-- 2. Sinh đúng 5.000 dòng dữ liệu mẫu cực nhanh (10% mang họ Nguyễn Minh)
;WITH n AS (
    SELECT TOP(5000) ROW_NUMBER() OVER(ORDER BY (SELECT NULL)) AS id
    FROM sys.all_objects a CROSS JOIN sys.all_objects b
)
INSERT #NhanVienBenchmark
SELECT id, 
       CASE WHEN id%10=0 THEN N'Nguyễn Minh ' ELSE N'Trần Văn ' END + CONVERT(NVARCHAR(20),id),
       '0900000000', 'benchmark@example.invalid', 1, 1, N'DANG_LAM_VIEC' 
FROM n;

-- 3. Tạo Covering Index với mệnh đề INCLUDE
CREATE INDEX IX_Benchmark_HoTen ON #NhanVienBenchmark(HoTen)
INCLUDE(SoDienThoai, Email, MaPB, MaCV, TrangThai);

-- 4. Bật thống kê IO và Time
SET STATISTICS IO, TIME ON;

-- LƯỢT 1: Ép chạy quét toàn bộ bảng (Clustered Index Scan)
SELECT MaNV, HoTen, SoDienThoai, Email, MaPB, MaCV, TrangThai
FROM #NhanVienBenchmark WITH(INDEX(1)) 
WHERE HoTen LIKE N'Nguyễn Minh%' OPTION(RECOMPILE);

-- LƯỢT 2: Chạy với Covering Index (Index Seek + Covered)
SELECT MaNV, HoTen, SoDienThoai, Email, MaPB, MaCV, TrangThai
FROM #NhanVienBenchmark WITH(INDEX(IX_Benchmark_HoTen)) 
WHERE HoTen LIKE N'Nguyễn Minh%' OPTION(RECOMPILE);

SET STATISTICS IO, TIME OFF;
DROP TABLE #NhanVienBenchmark;
```

* **Cách quan sát kết quả:**
  1. Bấm **`Ctrl + M`** trước khi chạy `F5`.
  2. Mở tab **Messages** xem 2 dòng: Lượt 1 đọc **75 reads**, Lượt 2 đọc **10 reads**.
  3. Mở tab **Execution plan** xem Lượt 1 là Clustered Scan (88% cost), Lượt 2 là Index Seek (12% cost). Tối ưu hóa **86.67% I/O**!

---

# PHẦN 2: CÁC KỊCH BẢN TEST CỦA CÁC THÀNH VIÊN KHÁC (TRẢ LỜI CÂU HỎI MỞ RỘNG)

## 2.1. Phân hệ Chấm công (TV2 - Quân)

### Test 2.1.1: Thủ tục ghi nhận quẹt thẻ chấm công (`sp_GhiNhanChamCong`)

* **Mục đích:** Ghi nhận giờ vào, giờ ra của nhân viên, kiểm tra giờ vào < giờ ra và nhân viên còn đang làm việc.

```sql
USE QuanLyNhanSuTienLuong;
GO

DECLARE @MaCC_Moi INT;

-- Ghi nhận quẹt thẻ đúng giờ cho nhân viên số 3
EXEC dbo.sp_GhiNhanChamCong
    @MaNV = 3,
    @NgayChamCong = '2026-10-09',
    @GioVao = '08:00:00',
    @GioRa = '17:00:00',
    @TrangThai = N'CO_MAT',
    @GhiChu = N'Quẹt thẻ đúng giờ',
    @MaChamCong = @MaCC_Moi OUTPUT;

SELECT * FROM dbo.CHAMCONG WHERE MaChamCong = @MaCC_Moi;
```

* **Kết quả mong đợi:** Tạo thành công dòng công mới. Nếu nhập giờ ra nhỏ hơn giờ vào hoặc chấm công cho người đã nghỉ việc thì trigger `trg_ChamCong_KiemTraGio` sẽ chặn ngay.

---

### Test 2.1.2: Xem báo cáo tổng hợp công tháng qua View (`vw_TongHopChamCongThang`)

```sql
USE QuanLyNhanSuTienLuong;
GO

SELECT * FROM dbo.vw_TongHopChamCongThang WHERE Thang = 10 AND Nam = 2026;
```

---

## 2.2. Phân hệ Phụ cấp & Khấu trừ (TV3 - Đạt)

### Test 2.2.1: Gọi các hàm tính tổng phụ cấp và khấu trừ (`fn_TongPhuCap`, `fn_TongKhauTru`)

```sql
USE QuanLyNhanSuTienLuong;
GO

-- Lấy tổng tiền phụ cấp và khấu trừ của nhân viên số 3 trong tháng 10/2026:
SELECT 
    3 AS MaNV,
    dbo.fn_TongPhuCap(3, 10, 2026) AS TongTienPhuCap,
    dbo.fn_TongKhauTru(3, 10, 2026) AS TongTienKhauTru;
```

---

### Test 2.2.2: Xem báo cáo tổng phụ cấp qua View (`vw_TongPhuCapThang`)

```sql
USE QuanLyNhanSuTienLuong;
GO

SELECT * FROM dbo.vw_TongPhuCapThang WHERE Thang = 10 AND Nam = 2026;
```

---

## 2.3. Phân hệ Tính bảng lương (TV4 - Vinh)

### Test 2.3.1: Hàm tính tiền công theo tỷ lệ ngày công chuẩn (`fn_TinhTienCong`)

* **Mục đích:** Tiền công = (Lương cơ bản / Ngày công chuẩn) * Ngày công thực tế.

```sql
USE QuanLyNhanSuTienLuong;
GO

-- Ví dụ: Lương CB = 26 triệu, chuẩn 26 ngày, làm thực tế 1 ngày:
SELECT dbo.fn_TinhTienCong(26000000, 26, 1) AS TienCongThucTe;
-- Kết quả trả về: 1000000.00 đ
```

---

### Test 2.3.2: Chạy thủ tục tính toán bảng lương tự động (`sp_TinhBangLuongThang`)

* **Mục đích:** Kế toán chạy tính bảng lương tháng tự động gom dữ liệu từ 6 bảng.

```sql
USE QuanLyNhanSuTienLuong;
GO

DECLARE @MaBL_Moi INT;

EXEC dbo.sp_TinhBangLuongThang
    @Thang = 10,
    @Nam = 2026,
    @NgayCongChuan = 26,
    @MaBangLuong = @MaBL_Moi OUTPUT;

-- Xem kết quả bảng lương vừa lập:
SELECT * FROM dbo.BANGLUONG WHERE MaBangLuong = @MaBL_Moi;
SELECT * FROM dbo.CHITIETBANGLUONG WHERE MaBangLuong = @MaBL_Moi;
```

* **Kết quả mong đợi:** Bảng `BANGLUONG` có trạng thái `'CHUA_CHOT'`, bảng `CHITIETBANGLUONG` tự động sinh đủ các dòng chi tiết cho từng nhân viên.

---

## 2.4. Phân hệ Bảo mật, Phân quyền & Đóng băng kỳ lương (TV5 - Đức Anh)

### Test 2.4.1: Hàm tính tiền thực nhận (`fn_TinhThucNhan`)

```sql
USE QuanLyNhanSuTienLuong;
GO

-- Ví dụ: Tiền công 1 triệu, phụ cấp 730k, khấu trừ 1 triệu:
SELECT dbo.fn_TinhThucNhan(1000000, 730000, 1000000) AS ThucLinh;
-- Kết quả trả về: 730000.00 đ
```

---

### Test 2.4.2: Thủ tục chốt sổ kỳ lương (`sp_ChotBangLuong`)

* **Mục đích:** Chuyển trạng thái sang `'DA_CHOT'` và kích hoạt 5 Trigger đóng băng dữ liệu.

```sql
USE QuanLyNhanSuTienLuong;
GO

DECLARE @MaBL_CanChot INT;
SELECT TOP 1 @MaBL_CanChot = MaBangLuong FROM dbo.BANGLUONG WHERE Thang = 10 AND Nam = 2026;

-- Chốt kỳ lương:
EXEC dbo.sp_ChotBangLuong @MaBangLuong = @MaBL_CanChot;

-- Kiểm tra trạng thái đã sang DA_CHOT và có NgayChot chưa:
SELECT MaBangLuong, Thang, Nam, TrangThai, NgayChot 
FROM dbo.BANGLUONG 
WHERE MaBangLuong = @MaBL_CanChot;
```

---

### Test 2.4.3: Kiểm tra 5 Freeze Triggers — Chặn sửa đổi khi kỳ lương đã chốt

* **Mục đích:** Chứng minh sau khi chốt lương, không ai được phép sửa đổi phụ cấp, khấu trừ hay quẹt thêm công vào kỳ đó.

```sql
USE QuanLyNhanSuTienLuong;
GO

-- Cố tình thêm phụ cấp vào tháng 10/2026 đã chốt -> BỊ CHẶN BỞI TRIGGER!
INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien)
VALUES (3, 10, 2026, N'Thưởng lén sau chốt', 5000000);
```

* **Kết quả mong đợi:** Báo lỗi `Msg 50000`: *'Kỳ lương đã chốt: không được thay đổi phụ cấp.'*. 5 trigger đóng băng dữ liệu hoạt động chính xác.

---

### Test 2.4.4: Kiểm tra Row-Level Security — Nhân viên chỉ xem phiếu lương của chính mình (`vw_PhieuLuongCaNhan`)

```sql
USE QuanLyNhanSuTienLuong;
GO

-- View sử dụng điều kiện WHERE tk.SqlLogin = ORIGINAL_LOGIN()
SELECT * FROM dbo.vw_PhieuLuongCaNhan;
```

---

# PHẦN 3: BẢNG TRA CỨU MÃ LỖI SQL SERVER KHI TEST

| Mã lỗi                  | Tên loại lỗi                          | Ý nghĩa và cách giải thích với Hội đồng                                                                             |
| :------------------------ | :--------------------------------------- | :---------------------------------------------------------------------------------------------------------------------------- |
| **Msg 547**         | CHECK / FOREIGN KEY Constraint           | Vi phạm ràng buộc miền giá trị (tuổi < 18, CCCD sai, SĐT sai...) hoặc khóa ngoại không tồn tại.                 |
| **Msg 2627 / 2601** | UNIQUE Index / Constraint                | Vi phạm tính duy nhất (trùng CCCD, trùng Email, trùng Tên đăng nhập, trùng chấm công cùng ngày).               |
| **Msg 50000**       | RAISERROR / Custom Trigger / SP          | Lỗi nghiệp vụ do nhóm tự viết trong Trigger hoặc Stored Procedure (chặn xóa nhân viên, kỳ lương đã chốt...). |
| **Msg 51001**       | THROW Custom Security Error              | Lỗi vi phạm quyền hạn (ví dụ HR cố tình tạo tài khoản Admin hoặc Payroll).                                        |
| **Msg 8152**        | String or binary data would be truncated | Dữ liệu chuỗi vượt quá độ dài khai báo của cột (ví dụ CCCD dài hơn 12 ký tự).                               |
