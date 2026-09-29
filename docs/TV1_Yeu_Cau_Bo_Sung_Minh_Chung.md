# BẢNG YÊU CẦU BỔ SUNG MINH CHỨNG & HÌNH ẢNH DÀNH CHO TV1
**Thành viên phụ trách:** Nguyễn Minh Trí (TV1 - MSSV: 24110359)  
**Phân hệ:** Module Quản lý Nhân sự & Danh mục, Phân tích thiết kế ERD / 3NF  
**Mục đích:** Bổ sung đầy đủ hình ảnh thực nghiệm chuẩn Rubric môn HQTCSDL để ghép vào Báo cáo cuối kỳ (Word/PDF 50-100 trang) và Slide thuyết trình.

---

## 📌 TỔNG HỢP DANH MỤC HÌNH ẢNH CẦN BỔ SUNG

| STT | Tên hình ảnh cần chụp | Môi trường chụp | Mục đích sử dụng | Mức độ ưu tiên |
|:---:|---|:---:|---|:---:|
| **1** | Cây Actual Execution Plan (Trước & Sau khi có Index `IX_NHANVIEN_HoTen`) | SSMS | Chứng minh chuyển từ Scan $\rightarrow$ Seek | **BẮT BUỘC (P1)** |
| **2** | Thông số thống kê I/O & Time (`SET STATISTICS IO, TIME ON`) | SSMS | Minh chứng giảm từ 428 $\rightarrow$ 4 Logical Reads | **BẮT BUỘC (P1)** |
| **3** | Kết quả chặn xóa của Trigger `trg_NhanVien_KhongXoaKhiDaPhatSinhLuong` | SSMS | Minh chứng cơ chế Soft Delete bảo vệ dữ liệu | **BẮT BUỘC (P1)** |
| **4** | Kết quả Rollback Transaction trong `sp_ThemNhanVien` khi trùng tài khoản | SSMS | Minh chứng tính nguyên tố (All-or-Nothing) | **BẮT BUỘC (P1)** |
| **5** | Sơ đồ Database Diagram xuất trực tiếp từ CSDL | SSMS | Đưa vào Chương 2 Thiết kế CSDL & Slide | **RẤT QUAN TRỌNG (P2)** |
| **6** | Ảnh giao diện Quản lý Danh mục (`DanhMucPanel`) | Ứng dụng Java | Minh chứng nghiệp vụ quản lý Phòng ban / Chức vụ | **NÊN CÓ (P3)** |

*(Lưu ý: Màn hình Quản lý hồ sơ nhân viên đã có sẵn tại `screenshots/03_NhanVien_HoSo.png`).*

---

## 🛠️ HƯỚNG DẪN CHI TIẾT CÁC BƯỚC THỰC HIỆN & SCRIPT SQL SẴN

### 1. Chụp ảnh Benchmark Index (Ảnh 1 & Ảnh 2)
1. Mở **SQL Server Management Studio (SSMS)** và kết nối vào CSDL `QuanLyNhanSuTienLuong`.
2. Bật tính năng đo lường:
   * Nhấn tổ hợp phím **`Ctrl + M`** (hoặc chọn menu **Query** $\rightarrow$ **Include Actual Execution Plan**).
3. Mở tab truy vấn mới và dán đoạn script sau:

```sql
USE QuanLyNhanSuTienLuong;
GO

SET STATISTICS IO, TIME ON;
GO

-- 1. Truy vấn KHI CHƯA DÙNG INDEX (hoặc ép quét tuần tự Clustered Scan)
PRINT '=== 1. TEST KHÔNG CÓ INDEX (CLUSTERED SCAN) ===';
SELECT MaNV, HoTen, SoDienThoai, Email, MaPB, MaCV, TrangThai
FROM NHANVIEN WITH (INDEX(PK__NHANVIEN))
WHERE HoTen LIKE N'Nguyễn%';
GO

-- 2. Truy vấn KHI SỬ DỤNG INDEX IX_NHANVIEN_HoTen (INDEX SEEK)
PRINT '=== 2. TEST CÓ INDEX SEEK ===';
SELECT MaNV, HoTen, SoDienThoai, Email, MaPB, MaCV, TrangThai
FROM NHANVIEN WITH (INDEX(IX_NHANVIEN_HoTen))
WHERE HoTen LIKE N'Nguyễn%';
GO

SET STATISTICS IO, TIME OFF;
GO
```

4. **Thao tác chụp:**
   * **Ảnh 1:** Chuyển sang tab **Execution Plan** ở khung kết quả. Chụp rõ 2 cây thực thi: cây trên là `Clustered Index Scan (98%)`, cây dưới là `Index Seek (2%)`.
   * **Ảnh 2:** Chuyển sang tab **Messages**. Chụp rõ dòng:
     * `Table 'NHANVIEN'. Scan count 1, logical reads 428...`
     * So sánh với dòng `logical reads 4...`.

---

### 2. Chụp ảnh Trigger chống xóa cứng (Ảnh 3)
1. Chạy câu lệnh cố ý xóa nhân viên đã có phát sinh chấm công / lương:

```sql
USE QuanLyNhanSuTienLuong;
GO

-- Cố ý xóa nhân viên MaNV = 1 (đã có dữ liệu trong hệ thống)
DELETE FROM NHANVIEN WHERE MaNV = 1;
GO
```

2. **Thao tác chụp:** Chụp toàn bộ cửa sổ kết quả hiện dòng chữ thông báo lỗi màu đỏ của trigger:  
   `Msg 50000, Level 16, State 1, Procedure trg_NhanVien_KhongXoaKhiDaPhatSinhLuong... Không thể xóa nhân viên đã có dữ liệu chấm công hoặc lương! Vui lòng chỉ cập nhật trạng thái NGHI_VIEC.`

---

### 3. Chụp ảnh Transaction Rollback trong SP (Ảnh 4)
1. Chạy câu lệnh gọi Stored Procedure thêm nhân viên mới nhưng cố tình truyền `TenDangNhap = 'admin'` (đã có trong bảng `TAIKHOAN`):

```sql
USE QuanLyNhanSuTienLuong;
GO

DECLARE @NewId INT;
BEGIN TRY
    EXEC sp_ThemNhanVien
        @HoTen = N'Nguyễn Minh Trí Test Rollback',
        @NgaySinh = '2000-01-01',
        @GioiTinh = N'Nam',
        @CCCD = '079200099999',
        @DiaChi = N'TP. Hồ Chí Minh',
        @SoDienThoai = '0901234999',
        @Email = 'tri.test.rollback@company.com',
        @NgayVaoLam = '2026-09-01',
        @LuongCoBan = 12000000,
        @MaPB = 1,
        @MaCV = 1,
        @TaoTaiKhoan = 1,
        @TenDangNhap = 'admin', -- Ép lỗi trùng tài khoản để kích hoạt ROLLBACK
        @MatKhauSHA256 = '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918',
        @VaiTro = 'Employee',
        @NewMaNV = @NewId OUTPUT;
END TRY
BEGIN CATCH
    SELECT 
        ERROR_NUMBER() AS MaLoi,
        ERROR_MESSAGE() AS ThongBaoLoi,
        @@TRANCOUNT AS TranCount_Sau_Khi_Rollback;
END CATCH;
GO

-- Kiểm tra xác nhận nhân viên trên KHÔNG hề tồn tại trong bảng NHANVIEN
SELECT * FROM NHANVIEN WHERE Email = 'tri.test.rollback@company.com';
GO
```

2. **Thao tác chụp:** Chụp kết quả trả về `ThongBaoLoi: Tên đăng nhập đã tồn tại trong hệ thống!` và bảng kiểm tra phía dưới rỗng (chứng minh nhân viên không bị tạo dở dang).

---

### 4. Xuất ảnh Database Diagram từ SSMS (Ảnh 5)
1. Trong SSMS, tại khung **Object Explorer** bên trái:
   * Mở rộng thư mục `Databases` $\rightarrow$ `QuanLyNhanSuTienLuong` $\rightarrow$ Chuột phải vào **Database Diagrams** $\rightarrow$ Chọn **New Database Diagram**.
2. Chọn Add các bảng liên quan chính của hệ thống:
   * `NHANVIEN`, `PHONGBAN`, `CHUCVU`, `TAIKHOAN`, `CHAMCONG`, `BANGLUONG`.
3. Sắp xếp các bảng ngay ngắn, thể hiện rõ các đường nối Khóa chính (vàng) $\rightarrow$ Khóa ngoại.
4. Nhấn chuột phải vào vùng trống chọn **Copy Diagram to Clipboard** rồi dán vào công cụ chỉnh ảnh (Paint) để lưu dưới dạng file `.png`.

---

### 5. Chụp giao diện Quản lý Danh mục (Ảnh 6)
1. Đăng nhập vào ứng dụng Java với tài khoản `admin` (hoặc `hr_manager`).
2. Mở tab **Danh mục** (quản lý Phòng ban / Chức vụ).
3. Chụp lại giao diện bảng danh mục gồm danh sách phòng ban và chức vụ.

---

## 📁 QUY ƯỚC ĐẶT TÊN VÀ NƠI LƯU ẢNH

Sau khi chụp xong, TV1 vui lòng lưu các file ảnh vào thư mục `screenshots/` theo đúng tên chuẩn sau:

1. `screenshots/TV1_Benchmark_ExecutionPlan.png`
2. `screenshots/TV1_Benchmark_StatisticsIO.png`
3. `screenshots/TV1_Trigger_ChanXoaNhanVien.png`
4. `screenshots/TV1_Transaction_Rollback_SP.png`
5. `screenshots/TV1_Database_Diagram.png`
6. `screenshots/TV1_GiaoDien_DanhMuc.png`

> 🚀 **Sau khi có đủ ảnh:** Trưởng nhóm sẽ hỗ trợ chèn trực tiếp các hình ảnh này vào file Báo cáo tổng thể của nhóm và Slide bảo vệ để hoàn tất nghiệm thu môn học!
