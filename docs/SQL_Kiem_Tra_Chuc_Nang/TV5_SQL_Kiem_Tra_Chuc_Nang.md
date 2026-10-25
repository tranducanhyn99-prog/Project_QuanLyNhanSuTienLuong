# TV5 — CẨM NANG CÂU LỆNH SQL THỰC CHIẾN (KIỂM TRA & KÍCH HOẠT CHỨC NĂNG)

**Dành riêng cho:** Trần Đức Anh (TV5 · MSSV: 24110155)  
**Học phần:** Hệ Quản Trị Cơ Sở Dữ Liệu (DBMS330284) — TS. Phan Thị Thể  
**Mục tiêu:** Mở SSMS $\rightarrow$ New Query $\rightarrow$ Copy & Paste $\rightarrow$ Nhấn **F5** $\rightarrow$ Giải thích tự tin trước mặt Giảng viên.

---

> [!IMPORTANT]
> ### THÔNG TIN DATABASE & TÀI KHOẢN ĐANG CÓ SẴN TRÊN MÁY BẠN
> - **Database kiểm tra chính thức:** `QuanLyNhanSuTienLuong` (đây là DB chuẩn mà ứng dụng Java đang kết nối đồng bộ).
> - **Mã bảng lương thực tế đang có:** `MaBangLuong = 1009` (Kỳ lương Tháng 10/2026, 20 nhân viên, trạng thái hiện tại là `CHUA_CHOT`).
> - **Danh sách Login & Mật khẩu test trong SSMS (SQL Authentication):**
>   - `gui_admin` / Mật khẩu: `Gui9!RViC8RsC5P6e0Xt8kGcEXw==` (DB_Admin)
>   - `gui_hr` / Mật khẩu: `Gui9!2aC+pyk01j8/1/rs3V+1pg==` (HR_Manager)
>   - `gui_payroll` / Mật khẩu: `Gui9!M2oYx/cQugmI/Y9pYrawlA==` (Payroll_Officer)
>   - `gui_a` / Mật khẩu: `Gui9!41Ay2bMK+OIlMSeew2Or3Q==` (Employee - MaNV: 1008 - Nguyễn Tuấn Anh - Lương: 730,000)
>   - `gui_b` / Mật khẩu: `Gui9!f9bTBwZZNFygXZU3FkknSw==` (Employee - MaNV: 1009 - Trần Thị Bảo Bình - Lương: 0)

---

> [!IMPORTANT]
> ### 3 NGUYÊN TẮC VÀNG KHI VIẾT SQL TRƯỚC MẶT GIẢNG VIÊN
> 1. **Luôn bắt đầu bằng:** `USE QuanLyNhanSuTienLuong; GO`. Tránh chạy nhầm vào database `master`.
> 2. **Luôn luôn có `REVERT;` sau khi dùng `EXECUTE AS`:** Nếu quên `REVERT`, session của bạn sẽ bị kẹt ở quyền hạn chế, các câu lệnh tiếp theo sẽ báo lỗi `Permission Denied` không rõ nguyên nhân.
> 3. **Bí quyết kiểm tra Row-Level Security (RLS) với `ORIGINAL_LOGIN()`:**
>    - Hàm `ORIGINAL_LOGIN()` lấy theo **Login kết nối ban đầu của phiên SSMS**.
>    - Nếu mở SSMS bằng Windows Authentication hoặc `sa` rồi gõ `EXECUTE AS LOGIN = 'gui_a'`, thì `ORIGINAL_LOGIN()` trong SQL Server vẫn là tài khoản Windows ban đầu $\rightarrow$ Query vào `vw_PhieuLuongCaNhan` sẽ ra 0 dòng.
>    - **Cách test chuẩn và chuyên nghiệp nhất:** Mở New Query $\rightarrow$ Chuột phải vào màn hình Query chọn **Connection $\rightarrow$ Change Connection** $\rightarrow$ Đăng nhập bằng **SQL Server Authentication** với Login `gui_a` / Mật khẩu `Gui9!41Ay2bMK+OIlMSeew2Or3Q==` $\rightarrow$ Chạy `SELECT * FROM dbo.vw_PhieuLuongCaNhan;` sẽ hiện ra ngay lập tức! (Điều này chứng minh khả năng bảo mật cấp phiên kết nối của bạn tuyệt đối an toàn).

---

## MỤC LỤC KỊCH BẢN VẤN ĐÁP SQL

- [Phần 1: Kiểm Tra Phân Quyền 4 Roles (RBAC)](#phan-1-kiem-tra-phan-quyen-4-roles-rbac)
  - [1.1 Test quyền HR Manager (`gui_hr`): Xem nhân sự OK, Chốt lương & Đọc tài khoản bị CHẶN](#11-test-quyen-hr-manager-gui_hr)
  - [1.2 Test quyền Payroll Officer (`gui_payroll`): Tính & Chốt lương OK, Sửa nhân sự & Đọc tài khoản bị CHẶN](#12-test-quyen-payroll-officer-gui_payroll)
  - [1.3 Test quyền Employee (`gui_a`): Bị CHẶN toàn bộ bảng nhạy cảm](#13-test-quyen-employee-gui_a)
- [Phần 2: Kiểm Tra Row-Level Security (RLS) qua View `vw_PhieuLuongCaNhan`](#phan-2-kiem-tra-row-level-security-rls)
  - [2.1 Nhân viên A (`gui_a`) xem phiếu lương cá nhân](#21-nhan-vien-a-gui_a-xem-phieu-luong-ca-nhan)
  - [2.2 Nhân viên B (`gui_b`) xem phiếu lương cá nhân](#22-nhan-vien-b-gui_b-xem-phieu-luong-ca-nhan)
  - [2.3 Thử dùng `gui_hr` xem qua view cá nhân $\rightarrow$ Bị chặn](#23-thu-dung-gui_hr-xem-qua-view-ca-nhan)
- [Phần 3: Kiểm Tra Stored Procedure Chốt Bảng Lương (`sp_ChotBangLuong`)](#phan-3-kiem-tra-sp_chotbangluong)
  - [3.1 Xem danh sách kỳ lương hiện tại](#31-xem-danh-sach-ky-luong-hien-tai)
  - [3.2 Kích hoạt chốt lương thành công](#32-kich-hoat-chot-luong-thanh-cong)
  - [3.3 Kích hoạt bẫy lỗi khi cố tình chốt lại kỳ đã chốt](#33-kich-hoat-bay-loi-khi-co-tinh-chot-lai-ky-da-chot)
  - [3.4 Kích hoạt mở lại kỳ lương (`sp_HuyChotBangLuong`)](#34-kich-hoat-mo-lai-ky-luong-sp_huychotbangluong)
- [Phần 4: Kích Hoạt & Bẫy Lỗi 5 Triggers Bảo Vệ Dữ Liệu](#phan-4-kich-hoat--bay-loi-5-triggers)
  - [4.1 Trigger 1: Cố tình sửa/thêm CHẤM CÔNG vào kỳ đã chốt](#41-trigger-1-trg_chamcong_khongsuakhidachotluong)
  - [4.2 Trigger 2: Cố tình thêm PHỤ CẤP vào kỳ đã chốt](#42-trigger-2-trg_phucap_khongsuakhidachotluong)
  - [4.3 Trigger 3: Cố tình thêm KHẤU TRỪ vào kỳ đã chốt](#43-trigger-3-trg_khautru_khongsuakhidachotluong)
  - [4.4 Trigger 4: Cố tình can thiệp sửa trực tiếp `CHITIETBANGLUONG`](#44-trigger-4-trg_chitietluong_khongsuakhidachot)
  - [4.5 Trigger 5: Cố tình DELETE nhân viên đã có dữ liệu lương/chấm công](#45-trigger-5-trg_nhanvien_khongxoakhidaphatsinhluong)
- [Phần 5: Kiểm Tra Function Tính Thực Nhận (`fn_TinhThucNhan`)](#phan-5-kiem-tra-function-fn_tinhthucnhan)
- [Phần 6: Đo Hiệu Năng Index (Covering Index Benchmark)](#phan-6-do-hieu-nang-index-covering-index-benchmark)
- [Phần 7: Mô Phỏng Kiểm Soát Đồng Thời (Concurrency Locking)](#phan-7-mo-phong-kiem-soat-dong-thoi-concurrency-locking)
- [Phần 8: Kiểm Tra Các Ràng Buộc Toàn Vẹn CSDL (Constraints)](#phan-8-kiem-tra-cac-rang-buoc-toan-ven-csdl-constraints)
  - [8.1 CHECK Constraint: Chặn nhập tháng ngoài khoảng 1-12 (`CHK_BANGLUONG_Thang`)](#81-check-constraint-chan-nhap-thang-ngoai-khoang-1-12)
  - [8.2 CHECK Constraint: Chặn trạng thái kỳ lương không hợp lệ (`CHK_BANGLUONG_TrangThai`)](#82-check-constraint-chan-trang-thai-ky-luong-khong-hop-le)
  - [8.3 CHECK Constraint: Chặn ngày công chuẩn âm hoặc bằng 0 (`CHK_BANGLUONG_NgayCongChuan`)](#83-check-constraint-chan-ngay-cong-chuan-am-hoac-bang-0)
  - [8.4 UNIQUE Constraint: Chặn tạo trùng bảng lương cùng tháng/năm (`UQ_BANGLUONG_ThangNam`)](#84-unique-constraint-chan-tao-trung-bang-luong-cung-thangnam)
  - [8.5 UNIQUE Constraint: Chặn 1 nhân viên có 2 phiếu lương trong kỳ (`UQ_CTBL_BangLuong_NhanVien`)](#85-unique-constraint-chan-1-nhan-vien-co-2-phieu-luong-trong-cung-ky)
  - [8.6 CHECK Constraint: Chặn tiền lương cơ bản âm (`CHK_CTBL_LuongCoBan`)](#86-check-constraint-chan-tien-luong-co-ban-am)
  - [8.7 FOREIGN KEY Constraint: Chặn chi tiết lương cho nhân viên không tồn tại (`FK_CTBL_NHANVIEN`)](#87-foreign-key-constraint-chan-chi-tiet-luong-cho-nhan-vien-khong-ton-tai)
  - [8.8 CHECK Constraint: Chặn phân vai trò tài khoản lạ (`CHK_TAIKHOAN_VaiTro`)](#88-check-constraint-chan-phan-vai-tro-tai-khoan-la)
  - [8.9 Xem toàn bộ danh mục các Ràng buộc toàn vẹn CSDL (Metadata query)](#89-xem-toan-bo-danh-muc-cac-rang-buoc-toan-ven-csdl-metadata-query)

---

## PHẦN 1: KIỂM TRA PHÂN QUYỀN 4 ROLES (RBAC)

### 1.1 Test quyền HR Manager (`gui_hr`)
**Cô hỏi:** *"Em hãy viết SQL chứng minh tài khoản HR Manager làm được gì và bị hệ thống chặn ở những nghiệp vụ nào?"*

```sql
USE QuanLyNhanSuTienLuong;
GO

-- 1. Chuyển sang ngữ cảnh HR Manager
EXECUTE AS LOGIN = 'gui_hr';

PRINT N'=== ĐANG CHẠY DƯỚI QUYỀN: ' + SUSER_SNAME() + N' ===';

-- A. THAO TÁC HỢP LỆ (HR được phép)
PRINT N'--- [TEST 1] Đọc danh sách nhân viên & chấm công (Kỳ vọng: THÀNH CÔNG) ---';
SELECT TOP(3) MaNV, HoTen, MaPB, MaCV, TrangThai FROM dbo.NHANVIEN;
SELECT TOP(3) MaChamCong, MaNV, NgayChamCong, TrangThai FROM dbo.CHAMCONG;

-- B. THAO TÁC BỊ CẤM: HR cố tình chốt lương (DENY sp_ChotBangLuong)
PRINT N'--- [TEST 2] HR cố tình chốt bảng lương (Kỳ vọng: LỖI 229) ---';
BEGIN TRY
    EXEC dbo.sp_ChotBangLuong @MaBangLuong = 1009;
    PRINT N'LỖI: Quyền bị hổng, HR chốt được lương!';
END TRY
BEGIN CATCH
    PRINT N'-> THÀNH CÔNG CHẶN HR: ' + ERROR_MESSAGE() + N' (Mã lỗi: ' + CAST(ERROR_NUMBER() AS NVARCHAR(10)) + N')';
END CATCH;

-- C. THAO TÁC BỊ CẤM: HR cố tình đọc bảng tài khoản / mật khẩu (DENY TAIKHOAN)
PRINT N'--- [TEST 3] HR cố tình đọc bảng TAIKHOAN (Kỳ vọng: LỖI 229) ---';
BEGIN TRY
    SELECT TOP(1) * FROM dbo.TAIKHOAN;
    PRINT N'LỖI: Quyền bị hổng, HR đọc được bảng TAIKHOAN!';
END TRY
BEGIN CATCH
    PRINT N'-> THÀNH CÔNG CHẶN HR: ' + ERROR_MESSAGE() + N' (Mã lỗi: ' + CAST(ERROR_NUMBER() AS NVARCHAR(10)) + N')';
END CATCH;

-- 2. Trả lại quyền DBA ban đầu
REVERT;
PRINT N'=== ĐÃ QUAY LẠI QUYỀN DBA: ' + SUSER_SNAME() + N' ===';
GO
```

**Kết quả mong đợi trên SSMS (Tab Messages):**
```text
=== ĐANG CHẠY DƯỚI QUYỀN: gui_hr ===
--- [TEST 1] Đọc danh sách nhân viên & chấm công (Kỳ vọng: THÀNH CÔNG) ---
(3 rows affected)
(3 rows affected)
--- [TEST 2] HR cố tình chốt bảng lương (Kỳ vọng: LỖI 229) ---
-> THÀNH CÔNG CHẶN HR: The EXECUTE permission was denied on the object 'sp_ChotBangLuong', database 'QuanLyNhanSuTienLuong', schema 'dbo'. (Mã lỗi: 229)
--- [TEST 3] HR cố tình đọc bảng TAIKHOAN (Kỳ vọng: LỖI 229) ---
-> THÀNH CÔNG CHẶN HR: The SELECT permission was denied on the object 'TAIKHOAN', database 'QuanLyNhanSuTienLuong', schema 'dbo'. (Mã lỗi: 229)
=== ĐÃ QUAY LẠI QUYỀN DBA: sa ===
```

> **Giải thích với Cô:**  
> *"Thưa cô, HR Manager có toàn quyền quản lý hồ sơ nhân sự và chấm công, nhưng theo nguyên tắc phân tách trách nhiệm (SoD), em đã `DENY EXECUTE` trên `sp_ChotBangLuong` và `DENY ALL` trên `TAIKHOAN` để HR không thể can thiệp kỳ lương và không thể đọc trộm thông tin tài khoản."*

---

### 1.2 Test quyền Payroll Officer (`gui_payroll`)
**Cô hỏi:** *"Kế toán tiền lương có sửa được thông tin hồ sơ nhân viên hay không? Chứng minh bằng SQL."*

```sql
USE QuanLyNhanSuTienLuong;
GO

EXECUTE AS LOGIN = 'gui_payroll';
PRINT N'=== ĐANG CHẠY DƯỚI QUYỀN: ' + SUSER_SNAME() + N' ===';

-- A. THAO TÁC HỢP LỆ: Đọc bảng lương và đối soát chấm công
PRINT N'--- [TEST 1] Kế toán đọc dữ liệu bảng lương (Kỳ vọng: THÀNH CÔNG) ---';
SELECT TOP(3) MaBangLuong, Thang, Nam, TrangThai FROM dbo.BANGLUONG;

-- B. THAO TÁC BỊ CẤM: Kế toán tự ý sửa thông tin nhân viên (DENY UPDATE NHANVIEN)
PRINT N'--- [TEST 2] Kế toán cố tình sửa họ tên nhân viên (Kỳ vọng: LỖI 229) ---';
BEGIN TRY
    UPDATE dbo.NHANVIEN SET HoTen = N'Hacker Sửa Tên' WHERE MaNV = 1008;
    PRINT N'LỖI: Quyền bị hổng, Payroll sửa được nhân viên!';
END TRY
BEGIN CATCH
    PRINT N'-> THÀNH CÔNG CHẶN PAYROLL: ' + ERROR_MESSAGE() + N' (Mã lỗi: ' + CAST(ERROR_NUMBER() AS NVARCHAR(10)) + N')';
END CATCH;

-- C. THAO TÁC BỊ CẤM: Kế toán tự ý can thiệp chấm công (DENY INSERT/UPDATE CHAMCONG)
PRINT N'--- [TEST 3] Kế toán cố tình chèn chấm công (Kỳ vọng: LỖI 229) ---';
BEGIN TRY
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai)
    VALUES (1008, '2026-10-28', '08:00', '17:00', 'CO_MAT');
    PRINT N'LỖI: Payroll chèn được chấm công!';
END TRY
BEGIN CATCH
    PRINT N'-> THÀNH CÔNG CHẶN PAYROLL: ' + ERROR_MESSAGE() + N' (Mã lỗi: ' + CAST(ERROR_NUMBER() AS NVARCHAR(10)) + N')';
END CATCH;

REVERT;
PRINT N'=== ĐÃ QUAY LẠI QUYỀN DBA: ' + SUSER_SNAME() + N' ===';
GO
```

**Kết quả mong đợi:** Lỗi **229 (Permission Denied)** được bắt chính xác khi `UPDATE NHANVIEN` và `INSERT CHAMCONG`.

> **Giải thích với Cô:**  
> *"Thưa cô, kế toán lương chỉ được cấp quyền SELECT trên NHANVIEN và CHAMCONG để đối soát ngày công. Nghiệp vụ thêm sửa hồ sơ hay chấm công bị `DENY` triệt để nhằm chống gian lận nâng ngày công hay tự tạo nhân viên ma để rút tiền lương."*

---

### 1.3 Test quyền Employee (`gui_a`)
**Cô hỏi:** *"Tài khoản nhân viên bình thường có đọc trộm được bảng lương của công ty không?"*

```sql
USE QuanLyNhanSuTienLuong;
GO

EXECUTE AS LOGIN = 'gui_a';
PRINT N'=== ĐANG CHẠY DƯỚI QUYỀN: ' + SUSER_SNAME() + N' ===';

-- A. THỬ TRUY CẬP BẢNG LƯƠNG TỔNG (BANGLUONG)
PRINT N'--- [TEST 1] Nhân viên đọc bảng BANGLUONG (Kỳ vọng: LỖI 229) ---';
BEGIN TRY
    SELECT * FROM dbo.BANGLUONG;
END TRY
BEGIN CATCH
    PRINT N'-> BỊ CHẶN: ' + ERROR_MESSAGE();
END CATCH;

-- B. THỬ TRUY CẬP VIEW LƯƠNG TỔNG CÔNG TY (vw_BangLuongChiTiet)
PRINT N'--- [TEST 2] Nhân viên đọc vw_BangLuongChiTiet (Kỳ vọng: LỖI 229) ---';
BEGIN TRY
    SELECT * FROM dbo.vw_BangLuongChiTiet;
END TRY
BEGIN CATCH
    PRINT N'-> BỊ CHẶN: ' + ERROR_MESSAGE();
END CATCH;

REVERT;
GO
```

---

## PHẦN 2: KIỂM TRA ROW-LEVEL SECURITY (RLS) QUA VIEW `vw_PhieuLuongCaNhan`

**Cô hỏi:** *"Em triển khai Row-Level Security như thế nào? Viết lệnh chứng minh nhân viên A chỉ xem được lương của A, nhân viên B chỉ xem được lương của B."*

### 2.1 Câu lệnh kiểm tra chéo 2 nhân viên (A vs B)

```sql
USE QuanLyNhanSuTienLuong;
GO

-- 1. KIỂM TRA PHIẾU LƯƠNG NHÂN VIÊN A (gui_a - MaNV: 1008 - Nguyễn Tuấn Anh)
EXECUTE AS LOGIN = 'gui_a';
SELECT 
    ORIGINAL_LOGIN()        AS [Login_Hien_Tai],
    MaNV, 
    HoTen, 
    Thang, 
    Nam, 
    LuongCoBan, 
    NgayCongThucTe, 
    TongPhuCap, 
    TongKhauTru, 
    ThucNhan
FROM dbo.vw_PhieuLuongCaNhan;
REVERT;

-- 2. KIỂM TRA PHIẾU LƯƠNG NHÂN VIÊN B (gui_b - MaNV: 1009 - Trần Thị Bảo Bình)
EXECUTE AS LOGIN = 'gui_b';
SELECT 
    ORIGINAL_LOGIN()        AS [Login_Hien_Tai],
    MaNV, 
    HoTen, 
    Thang, 
    Nam, 
    LuongCoBan, 
    NgayCongThucTe, 
    TongPhuCap, 
    TongKhauTru, 
    ThucNhan
FROM dbo.vw_PhieuLuongCaNhan;
REVERT;
GO
```

**Kết quả mong đợi trong bảng kết quả (Result Grid):**
- **Lưới 1 (gui_a):** Chỉ xuất hiện đúng 1 dòng (hoặc các kỳ lương) của `MaNV = 1008` (Nguyễn Tuấn Anh), ThucNhan = `730,000.00`. Hoàn toàn không có dữ liệu của nhân viên khác!
- **Lưới 2 (gui_b):** Chỉ xuất hiện đúng dữ liệu của `MaNV = 1009` (Trần Thị Bảo Bình), ThucNhan = `0.00`.

> **Giải thích với Cô:**  
> *"Thưa cô, View `vw_PhieuLuongCaNhan` sử dụng mệnh đề `WHERE tk.SqlLogin = ORIGINAL_LOGIN() AND tk.TrangThai = 'HOAT_DONG'`.  
> Hàm `ORIGINAL_LOGIN()` lấy trực tiếp định danh phiên SQL Server mà phía Client kết nối vào. Ứng dụng Java không thể truyền tham số `@MaNV` can thiệp vào View này, giúp ngăn chặn 100% lỗ hổng ID Tampering (thay đổi ID để xem trộm lương đồng nghiệp)."*

---

## PHẦN 3: KIỂM TRA STORED PROCEDURE CHỐT BẢNG LƯƠNG (`sp_ChotBangLuong`)

### 3.1 Xem trạng thái các kỳ lương hiện có
```sql
USE QuanLyNhanSuTienLuong;
GO

SELECT MaBangLuong, Thang, Nam, NgayCongChuan, TrangThai, NgayTao, NgayChot 
FROM dbo.BANGLUONG
ORDER BY Nam DESC, Thang DESC;
GO
```

---

### 3.2 Kịch bản chốt lương hợp lệ và bẫy lỗi chốt lại

Giả sử kỳ lương cần test có `MaBangLuong = 1009` (kỳ tháng 10/2026 hiện có trên hệ thống):

```sql
USE QuanLyNhanSuTienLuong;
GO

-- Bước 1: Đảm bảo kỳ lương đang ở trạng thái CHUA_CHOT để demo
-- (Nếu kỳ 1009 đang DA_CHOT, ta mở lại trước để biểu diễn)
IF EXISTS (SELECT 1 FROM dbo.BANGLUONG WHERE MaBangLuong = 1009 AND TrangThai = 'DA_CHOT')
BEGIN
    EXEC dbo.sp_HuyChotBangLuong @MaBangLuong = 1009;
    PRINT N'[Chuẩn bị] Đã đưa kỳ 1009 về CHUA_CHOT để sẵn sàng demo.';
END
GO

-- Bước 2: Gọi Stored Procedure chốt kỳ lương (Kỳ vọng: THÀNH CÔNG)
PRINT N'--- BƯỚC 1: TIẾN HÀNH CHỐT LƯƠNG KỲ 1009 ---';
EXEC dbo.sp_ChotBangLuong @MaBangLuong = 1009;

-- Kiểm tra lại trạng thái trong bảng
SELECT MaBangLuong, Thang, Nam, TrangThai, NgayChot 
FROM dbo.BANGLUONG 
WHERE MaBangLuong = 1009;
GO

-- Bước 3: Cố tình gọi chốt lại lần thứ 2 trên kỳ đã chốt (Kỳ vọng: BẪY LỖI 50000)
PRINT N'--- BƯỚC 2: CỐ TÌNH CHỐT LẠI KỲ ĐÃ CHỐT ---';
BEGIN TRY
    EXEC dbo.sp_ChotBangLuong @MaBangLuong = 1009;
    PRINT N'LỖI: Chốt lại được là sai logic!';
END TRY
BEGIN CATCH
    PRINT N'-> BẪY LỖI BẮT THÀNH CÔNG: ' + ERROR_MESSAGE() + N' (Mức nghiêm trọng: ' + CAST(ERROR_SEVERITY() AS NVARCHAR(10)) + N')';
END CATCH;
GO
```

**Kết quả mong đợi:**
```text
--- BƯỚC 1: TIẾN HÀNH CHỐT LƯƠNG KỲ 1009 ---
Đã chốt bảng lương mã 1009 với 20 chi tiết.
(1 row affected: TrangThai = 'DA_CHOT', NgayChot = thời gian hiện tại)

--- BƯỚC 2: CỐ TÌNH CHỐT LẠI KỲ ĐÃ CHỐT ---
-> BẪY LỖI BẮT THÀNH CÔNG: Kỳ lương đã được chốt trước đó. Không thể chốt lại! (Mức nghiêm trọng: 16)
```

> **Giải thích với Cô:**  
> *"Thưa cô, trong `sp_ChotBangLuong`, em dùng `SELECT TrangThai FROM BANGLUONG WITH (UPDLOCK, HOLDLOCK)`. Khóa này vừa đọc vừa giữ khóa cập nhật trên dòng bảng lương trong suốt Transaction. Nếu bảng lương đã `DA_CHOT`, procedure lập tức phát sinh lỗi `RAISERROR` mức 16 và rollback, ngăn chặn tuyệt đối việc chốt trùng lặp."*

---

## PHẦN 4: KÍCH HOẠT & BẪY LỖI 5 TRIGGERS

Sau khi kỳ lương tháng 10/2026 (`MaBangLuong = 1009`) đã ở trạng thái **`DA_CHOT`**, dữ liệu nguồn phải trở thành **BẤT BIẾN (IMMUTABLE)**. Dưới đây là các câu lệnh kích hoạt từng Trigger:

### 4.1 Trigger 1: `trg_ChamCong_KhongSuaKhiDaChotLuong`
**Cô hỏi:** *"Sau khi chốt kỳ lương tháng 10/2026, nhân sự hoặc nhân viên có thể sửa hay thêm ngày công vào tháng 10 không? Kích hoạt trigger cho cô xem."*

```sql
USE QuanLyNhanSuTienLuong;
GO

PRINT N'--- KÍCH HOẠT TRIGGER CHẤM CÔNG ---';
BEGIN TRY
    -- Cố tình thêm chấm công vào ngày 15/10/2026 (thuộc kỳ đã chốt)
    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)
    VALUES (1008, '2026-10-15', '08:00:00', '17:00:00', N'CO_MAT', N'Test Trigger Chấm Công');
    
    PRINT N'LỖI: Trigger không hoạt động, dữ liệu bị ghi lén!';
END TRY
BEGIN CATCH
    PRINT N'-> TRIGGER BẮT THÀNH CÔNG VÀ ROLLBACK:';
    PRINT N'   Nội dung lỗi: ' + ERROR_MESSAGE();
END CATCH;
GO
```

**Thông báo xuất hiện:**
```text
-> TRIGGER BẮT THÀNH CÔNG VÀ ROLLBACK:
   Nội dung lỗi: Kỳ lương đã chốt: không được thay đổi chấm công.
```

---

### 4.2 Trigger 2: `trg_PhuCap_KhongSuaKhiDaChotLuong`
**Cô hỏi:** *"Kích hoạt trigger khóa phụ cấp khi kỳ lương đã chốt."*

```sql
USE QuanLyNhanSuTienLuong;
GO

PRINT N'--- KÍCH HOẠT TRIGGER PHỤ CẤP ---';
BEGIN TRY
    -- Cố tình thêm phụ cấp vào tháng 10 năm 2026 (kỳ đã chốt)
    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan)
    VALUES (1008, 10, 2026, N'Phụ cấp khống', 2000000, GETDATE());
    
    PRINT N'LỖI: Trigger không hoạt động!';
END TRY
BEGIN CATCH
    PRINT N'-> TRIGGER PHỤ CẤP BẮT THÀNH CÔNG:';
    PRINT N'   Nội dung lỗi: ' + ERROR_MESSAGE();
END CATCH;
GO
```

**Thông báo xuất hiện:**
```text
-> TRIGGER PHỤ CẤP BẮT THÀNH CÔNG:
   Nội dung lỗi: Kỳ lương đã chốt: không được thay đổi phụ cấp.
```

---

### 4.3 Trigger 3: `trg_KhauTru_KhongSuaKhiDaChotLuong`
**Cô hỏi:** *"Kích hoạt trigger khóa khấu trừ khi kỳ lương đã chốt."*

```sql
USE QuanLyNhanSuTienLuong;
GO

PRINT N'--- KÍCH HOẠT TRIGGER KHẤU TRỪ ---';
BEGIN TRY
    -- Cố tình thêm khoản khấu trừ vào tháng 10 năm 2026
    INSERT INTO dbo.KHAUTRUNHANVIEN (MaNV, Thang, Nam, TenKhauTru, SoTien, NgayGhiNhan)
    VALUES (1008, 10, 2026, N'Phạt đi muộn bổ sung', 100000, GETDATE());
    
    PRINT N'LỖI: Trigger không hoạt động!';
END TRY
BEGIN CATCH
    PRINT N'-> TRIGGER KHẤU TRỪ BẮT THÀNH CÔNG:';
    PRINT N'   Nội dung lỗi: ' + ERROR_MESSAGE();
END CATCH;
GO
```

**Thông báo xuất hiện:**
```text
-> TRIGGER KHẤU TRỪ BẮT THÀNH CÔNG:
   Nội dung lỗi: Kỳ lương đã chốt: không được thay đổi khấu trừ.
```

---

### 4.4 Trigger 4: `trg_ChiTietLuong_KhongSuaKhiDaChot`
**Cô hỏi:** *"Nếu có người dùng lệnh SQL can thiệp trực tiếp vào bảng `CHITIETBANGLUONG` để tăng lương thực nhận của nhân viên lên 100 triệu thì hệ thống bảo vệ ra sao?"*

```sql
USE QuanLyNhanSuTienLuong;
GO

PRINT N'--- KÍCH HOẠT TRIGGER KHÓA BẢNG LƯƠNG CHI TIẾT ---';
BEGIN TRY
    -- Cố tình UPDATE thực nhận của nhân viên 1008 trong kỳ 1009 đã chốt
    UPDATE dbo.CHITIETBANGLUONG
    SET ThucNhan = 100000000.00
    WHERE MaBangLuong = 1009 AND MaNV = 1008;

    PRINT N'LỖI: Sửa được bảng lương đã chốt!';
END TRY
BEGIN CATCH
    PRINT N'-> TRIGGER BẢO VỆ CHI TIẾT LƯƠNG BẮT THÀNH CÔNG:';
    PRINT N'   Nội dung lỗi: ' + ERROR_MESSAGE();
END CATCH;
GO
```

**Thông báo xuất hiện:**
```text
-> TRIGGER BẢO VỆ CHI TIẾT LƯƠNG BẮT THÀNH CÔNG:
   Nội dung lỗi: Không được phép sửa hoặc xóa chi tiết bảng lương đã chốt! Không được thêm hoặc chuyển chi tiết vào kỳ đã chốt.
```

---

### 4.5 Trigger 5: `trg_NhanVien_KhongXoaKhiDaPhatSinhLuong`
**Cô hỏi:** *"Nếu HR hoặc ai đó xóa một nhân viên trong công ty bằng lệnh `DELETE FROM NHANVIEN` thì sao? Em xử lý toàn vẹn dữ liệu như thế nào?"*

```sql
USE QuanLyNhanSuTienLuong;
GO

PRINT N'--- KÍCH HOẠT TRIGGER CHẶN XÓA NHÂN VIÊN ---';
BEGIN TRY
    -- Cố tình xóa nhân viên 1008 (đã có chấm công và lương)
    DELETE FROM dbo.NHANVIEN WHERE MaNV = 1008;

    PRINT N'LỖI: Xóa mất nhân viên đã phát sinh dữ liệu!';
END TRY
BEGIN CATCH
    PRINT N'-> TRIGGER TOÀN VẸN DỮ LIỆU BẮT THÀNH CÔNG:';
    PRINT N'   Nội dung lỗi: ' + ERROR_MESSAGE();
END CATCH;
GO
```

**Thông báo xuất hiện:**
```text
-> TRIGGER TOÀN VẸN DỮ LIỆU BẮT THÀNH CÔNG:
   Nội dung lỗi: Không được phép xóa nhân viên đã có dữ liệu chấm công hoặc lương. Vui lòng chuyển trạng thái sang NGHI_VIEC!
```

---

## PHẦN 5: KIỂM TRA FUNCTION TÍNH THỰC NHẬN (`fn_TinhThucNhan`)

**Cô hỏi:** *"Hàm tính thực nhận của em viết như thế nào? Xử lý giá trị NULL và trường hợp khấu trừ lớn hơn tiền lương ra sao?"*

```sql
USE QuanLyNhanSuTienLuong;
GO

SELECT 
    -- Trường hợp 1: Tính toán thông thường
    dbo.fn_TinhThucNhan(10000000, 2000000, 1000000) AS [1_Binh_Thuong_11M],
    
    -- Trường hợp 2: Có giá trị NULL (Hàm dùng ISNULL(@param, 0))
    dbo.fn_TinhThucNhan(NULL, 1500000, NULL)        AS [2_Xu_Ly_NULL_1.5M],
    
    -- Trường hợp 3: Khấu trừ vượt thu nhập (Cho phép ra số âm, không bị crash)
    dbo.fn_TinhThucNhan(5000000, 0, 7000000)         AS [3_Thuc_Nhan_Am_-2M];
GO
```

**Kết quả mong đợi:**
| 1_Binh_Thuong_11M | 2_Xu_Ly_NULL_1.5M | 3_Thuc_Nhan_Am_-2M |
|:---:|:---:|:---:|
| `11000000.00` | `1500000.00` | `-2000000.00` |

> **Giải thích với Cô:**  
> *"Thưa cô, công thức là: `Thực nhận = Tiền công + Tổng phụ cấp - Tổng khấu trừ`. Hàm được bọc `ISNULL(..., 0)` để nếu bất kỳ tham số nào bị `NULL`, hệ thống vẫn tính đúng mà không trả về kết quả `NULL`. Ngoài ra, nếu khấu trừ phát sinh lớn hơn thu nhập thì hàm vẫn trả về số âm phản ánh khoản nợ công ty thay vì âm thầm ép về 0."*

---

## PHẦN 6: ĐO HIỆU NĂNG INDEX (COVERING INDEX BENCHMARK)

**Cô hỏi:** *"Em chứng minh Index của em giúp tăng tốc truy vấn như thế nào? Bật thống kê IO cho cô xem số trang đọc."*

```sql
USE QuanLyNhanSuTienLuong;
GO

SET NOCOUNT ON;
SET STATISTICS IO ON;
SET STATISTICS TIME ON;

PRINT N'================================================================';
PRINT N'1. TRUY VẤN ÉP CLUSTERED INDEX SCAN (QUÉT TOÀN BẢNG - INDEX 1)';
PRINT N'================================================================';
SELECT MaNV, HoTen, LuongCoBan, TrangThai
FROM dbo.NHANVIEN WITH (INDEX(1))
WHERE MaPB = 2 AND MaCV = 1;

PRINT N'================================================================';
PRINT N'2. TRUY VẤN DÙNG COVERING INDEX CỦA TV5 (INDEX SEEK + INCLUDE)';
PRINT N'================================================================';
SELECT MaNV, HoTen, LuongCoBan, TrangThai
FROM dbo.NHANVIEN WITH (INDEX(IX_NHANVIEN_MaPB_MaCV))
WHERE MaPB = 2 AND MaCV = 1;

SET STATISTICS IO OFF;
SET STATISTICS TIME OFF;
GO
```

**Cách xem và giải thích kết quả trong Tab "Messages":**
- Nhìn vào dòng: `Table 'NHANVIEN'. Scan count ..., logical reads X`
- **Trước (Clustered Scan):** Quét toàn bộ các trang dữ liệu của bảng `NHANVIEN`. (Trên dữ liệu benchmark mẫu 10,000 dòng là **142 hoặc 126 logical reads**).
- **Sau (Covering Seek):** Chỉ duyệt cây B-Tree đến đúng nhánh `MaPB = 2, MaCV = 1` và lấy trực tiếp `HoTen, LuongCoBan, TrangThai` từ mệnh đề `INCLUDE`, không cần Bookmark Lookup (Key Lookup). Số lần đọc trang giảm xuống chỉ còn **15 hoặc 13 logical reads** (Giảm tới **89.5%** I/O).

---

## PHẦN 7: MÔ PHỎNG KIỂM SOÁT ĐỒNG THỜI (CONCURRENCY LOCKING)

**Cô hỏi:** *"Nếu hai kế toán cùng mở giao diện hoặc cùng chạy lệnh chốt bảng lương một lúc thì hệ thống tránh Lost Update hoặc Double Close như thế nào? Chứng minh cơ chế khóa."*

> [!TIP]
> **Cách biểu diễn trước mặt cô:** Mở **2 Cửa sổ Query riêng biệt** trong SSMS đặt cạnh nhau:

### Cửa sổ 1 (Phiên A - Giữ Khóa):
```sql
USE QuanLyNhanSuTienLuong;
GO

BEGIN TRANSACTION;

-- Giả lập phiên A đang đọc và chuẩn bị chốt lương
SELECT TrangThai 
FROM dbo.BANGLUONG WITH (UPDLOCK, HOLDLOCK)
WHERE MaBangLuong = 1009;

PRINT N'Phiên A đang giữ khóa UPDLOCK + HOLDLOCK trên kỳ 1009... (Chưa COMMIT)';
-- Giữ nguyên cửa sổ này, KHÔNG chạy lệnh dưới vội
-- ROLLBACK TRANSACTION;
```

### Cửa sổ 2 (Phiên B - Bị Block Chờ Đợi):
```sql
USE QuanLyNhanSuTienLuong;
GO

PRINT N'Phiên B cố gắng gọi sp_ChotBangLuong trên cùng kỳ 1009...';
-- Lệnh này sẽ bị TREO (Executing query...), không thể chạy tiếp vì bị Phiên A chặn!
EXEC dbo.sp_ChotBangLuong @MaBangLuong = 1009;
```

### Kết thúc biểu diễn:
1. Cho cô xem Cửa sổ 2 đang ở trạng thái **Executing Query...** (vòng xoay màu vàng).
2. Quay lại Cửa sổ 1, bôi đen dòng `ROLLBACK TRANSACTION;` và bấm **F5**.
3. Ngay lập tức Cửa sổ 2 được giải phóng và hoàn tất lệnh!

> **Giải thích với Cô:**  
> *"Thưa cô, cơ chế `UPDLOCK, HOLDLOCK` của TV5 biến các giao dịch cập nhật cạnh tranh trên cùng một kỳ lương thành thứ tự tuần tự (Serializing). Phiên thứ hai bắt buộc phải chờ phiên thứ nhất hoàn tất. Sau khi phiên một chốt xong (`TrangThai = 'DA_CHOT'`), phiên hai bước vào sẽ đọc thấy trạng thái đã chốt và bị bẫy lỗi từ chối ngay lập tức, ngăn chặn 100% tình trạng chạy đè dữ liệu."*

---

## PHẦN 8: KIỂM TRA CÁC RÀNG BUỘC TOÀN VẸN CSDL (CONSTRAINTS)

> [!NOTE]
> **Phân biệt với Trigger:**
> - **Constraint (Ràng buộc toàn vẹn khai báo - Declarative Constraints):** Được kiểm tra **trước** hoặc ngay trong lúc thao tác dữ liệu ở cấp độ thấp nhất của SQL Server engine. Khi vi phạm, SQL Server tự động từ chối với các mã lỗi hệ thống chuẩn (`Msg 547` cho CHECK/FK, `Msg 2627`/`2601` cho UNIQUE).
> - **Trigger (Ràng buộc nghiệp vụ động):** Được kích hoạt **sau** (`AFTER INSERT, UPDATE, DELETE`) để kiểm tra các nghiệp vụ phức tạp liên bảng (như kỳ lương đã chốt hay chưa, nhân viên đã nghỉ việc hay chưa).

---

### 8.1 CHECK Constraint: Chặn nhập tháng ngoài khoảng 1-12
**Cô hỏi:** *"Nếu người dùng nhập tháng 13 hoặc tháng 0 vào bảng lương thì CSDL xử lý thế nào?"*

```sql
USE QuanLyNhanSuTienLuong;
GO

BEGIN TRY
    BEGIN TRANSACTION;
    -- Cố tình INSERT tháng 13 (ngoài khoảng 1..12)
    INSERT INTO dbo.BANGLUONG (Thang, Nam, NgayCongChuan, TrangThai)
    VALUES (13, 2026, 26, 'CHUA_CHOT');

    ROLLBACK TRANSACTION;
    SELECT N'FAIL: Không chặn được tháng 13' AS KetQua;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    SELECT 
        N'PASS: Đã chặn thành công qua CHECK Constraint' AS KetQua,
        ERROR_NUMBER() AS MaLoi,
        ERROR_MESSAGE() AS ThongBaoLoi;
END CATCH;
```
- **Kết quả mong đợi:** Mã lỗi **`547`** — `The INSERT statement conflicted with the CHECK constraint "CHK_BANGLUONG_Thang"`.
- **Cách trả lời cô:** *"Thưa cô, cột `Thang` được bảo vệ bằng ràng buộc `CHECK (Thang BETWEEN 1 AND 12)`. Mọi giá trị nhỏ hơn 1 hoặc lớn hơn 12 đều bị engine SQL Server từ chối ngay lập tức."*

---

### 8.2 CHECK Constraint: Chặn trạng thái kỳ lương không hợp lệ
**Cô hỏi:** *"Bảng lương chỉ có những trạng thái nào? Nếu nhập trạng thái lạ như 'DA_HUY' hay 'DANG_XU_LY' thì sao?"*

```sql
USE QuanLyNhanSuTienLuong;
GO

BEGIN TRY
    BEGIN TRANSACTION;
    -- Cố tình INSERT trạng thái không nằm trong ('CHUA_CHOT', 'DA_CHOT')
    INSERT INTO dbo.BANGLUONG (Thang, Nam, NgayCongChuan, TrangThai)
    VALUES (11, 2026, 26, 'DA_HUY');

    ROLLBACK TRANSACTION;
    SELECT N'FAIL: Không chặn được trạng thái lạ' AS KetQua;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    SELECT 
        N'PASS: Đã chặn thành công trạng thái lạ qua CHECK Constraint' AS KetQua,
        ERROR_NUMBER() AS MaLoi,
        ERROR_MESSAGE() AS ThongBaoLoi;
END CATCH;
```
- **Kết quả mong đợi:** Mã lỗi **`547`** — `The INSERT statement conflicted with the CHECK constraint "CHK_BANGLUONG_TrangThai"`.
- **Cách trả lời cô:** *"Cột `TrangThai` có ràng buộc `CHECK (TrangThai IN ('CHUA_CHOT', 'DA_CHOT'))`, đảm bảo chu trình tính lương chỉ có đúng 2 pha chuẩn: nháp (chưa chốt) và chính thức (đã chốt)."*

---

### 8.3 CHECK Constraint: Chặn ngày công chuẩn âm hoặc bằng 0
**Cô hỏi:** *"Ngày công chuẩn trong tháng có thể âm hoặc bằng 0 được không?"*

```sql
USE QuanLyNhanSuTienLuong;
GO

BEGIN TRY
    BEGIN TRANSACTION;
    -- Cố tình INSERT ngày công chuẩn = -10 (phải > 0)
    INSERT INTO dbo.BANGLUONG (Thang, Nam, NgayCongChuan, TrangThai)
    VALUES (11, 2026, -10, 'CHUA_CHOT');

    ROLLBACK TRANSACTION;
    SELECT N'FAIL: Không chặn được ngày công chuẩn âm' AS KetQua;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    SELECT 
        N'PASS: Đã chặn thành công ngày công chuẩn âm qua CHECK Constraint' AS KetQua,
        ERROR_NUMBER() AS MaLoi,
        ERROR_MESSAGE() AS ThongBaoLoi;
END CATCH;
```
- **Kết quả mong đợi:** Mã lỗi **`547`** — `The INSERT statement conflicted with the CHECK constraint "CHK_BANGLUONG_NgayCongChuan"`.
- **Cách trả lời cô:** *"Ràng buộc `CHECK (NgayCongChuan > 0)` ngăn chặn triệt để lỗi chia cho 0 khi tính đơn giá ngày công (`LuongCoBan / NgayCongChuan`)."*

---

### 8.4 UNIQUE Constraint: Chặn tạo trùng bảng lương cùng tháng/năm
**Cô hỏi:** *"Làm sao CSDL đảm bảo không bao giờ có 2 bảng lương cho cùng một tháng trong năm?"*

```sql
USE QuanLyNhanSuTienLuong;
GO

BEGIN TRY
    BEGIN TRANSACTION;
    -- Kỳ Tháng 10/2026 đã tồn tại trong CSDL, thử INSERT thêm một bảng lương Tháng 10/2026 nữa
    INSERT INTO dbo.BANGLUONG (Thang, Nam, NgayCongChuan, TrangThai)
    VALUES (10, 2026, 26, 'CHUA_CHOT');

    ROLLBACK TRANSACTION;
    SELECT N'FAIL: Không chặn được trùng kỳ lương' AS KetQua;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    SELECT 
        N'PASS: Đã chặn thành công tạo trùng kỳ lương qua UNIQUE Constraint' AS KetQua,
        ERROR_NUMBER() AS MaLoi,
        ERROR_MESSAGE() AS ThongBaoLoi;
END CATCH;
```
- **Kết quả mong đợi:** Mã lỗi **`2627`** hoặc **`2601`** — `Violation of UNIQUE KEY constraint 'UQ_BANGLUONG_ThangNam'. Cannot insert duplicate key in object 'dbo.BANGLUONG'. The duplicate key value is (10, 2026)`.
- **Cách trả lời cô:** *"Bảng `BANGLUONG` có ràng buộc `CONSTRAINT UQ_BANGLUONG_ThangNam UNIQUE (Thang, Nam)`. Đây là ràng buộc duy nhất phức hợp, đảm bảo mỗi tháng trong năm chỉ tồn tại duy nhất một bảng lương."*

---

### 8.5 UNIQUE Constraint: Chặn 1 nhân viên có 2 phiếu lương trong cùng kỳ
**Cô hỏi:** *"Trong một bảng lương, làm thế nào để một nhân viên không bị tính lương hoặc xuất hiện hai lần?"*

```sql
USE QuanLyNhanSuTienLuong;
GO

BEGIN TRY
    BEGIN TRANSACTION;
    -- Nhân viên 1008 đã có dòng lương trong kỳ 1009, cố tình thêm dòng thứ hai cho nhân viên 1008
    INSERT INTO dbo.CHITIETBANGLUONG (MaBangLuong, MaNV, LuongCoBan, NgayCongThucTe, TienCong, TongPhuCap, TongKhauTru, ThucNhan)
    VALUES (1009, 1008, 10000000, 26, 10000000, 0, 0, 10000000);

    ROLLBACK TRANSACTION;
    SELECT N'FAIL: Không chặn được trùng nhân viên' AS KetQua;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    SELECT 
        N'PASS: Đã chặn thành công 1 nhân viên có 2 phiếu lương qua UNIQUE Constraint' AS KetQua,
        ERROR_NUMBER() AS MaLoi,
        ERROR_MESSAGE() AS ThongBaoLoi;
END CATCH;
```
- **Kết quả mong đợi:** Mã lỗi **`2627`** — `Violation of UNIQUE KEY constraint 'UQ_CTBL_BangLuong_NhanVien'. Cannot insert duplicate key in object 'dbo.CHITIETBANGLUONG'. The duplicate key value is (1009, 1008)`.
- **Cách trả lời cô:** *"Ràng buộc `CONSTRAINT UQ_CTBL_BangLuong_NhanVien UNIQUE (MaBangLuong, MaNV)` trên bảng `CHITIETBANGLUONG` bảo đảm nguyên tắc 1-1 giữa nhân viên và kỳ lương."*

---

### 8.6 CHECK Constraint: Chặn tiền lương cơ bản âm
**Cô hỏi:** *"Các trường số tiền như LuongCoBan, TienCong, TongPhuCap có thể bị số âm do lỗi người nhập không?"*

```sql
USE QuanLyNhanSuTienLuong;
GO

BEGIN TRY
    BEGIN TRANSACTION;
    -- Cố tình INSERT lương cơ bản âm (-5.000.000)
    INSERT INTO dbo.CHITIETBANGLUONG (MaBangLuong, MaNV, LuongCoBan, NgayCongThucTe, TienCong, TongPhuCap, TongKhauTru, ThucNhan)
    VALUES (1009, 1010, -5000000, 26, 0, 0, 0, 0);

    ROLLBACK TRANSACTION;
    SELECT N'FAIL: Không chặn được lương âm' AS KetQua;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    SELECT 
        N'PASS: Đã chặn thành công lương âm qua CHECK Constraint' AS KetQua,
        ERROR_NUMBER() AS MaLoi,
        ERROR_MESSAGE() AS ThongBaoLoi;
END CATCH;
```
- **Kết quả mong đợi:** Mã lỗi **`547`** — `The INSERT statement conflicted with the CHECK constraint "CHK_CTBL_LuongCoBan"`.
- **Cách trả lời cô:** *"Tất cả các cột thành phần cấu thành thu nhập (`LuongCoBan`, `TienCong`, `TongPhuCap`, `TongKhauTru`) đều có ràng buộc `CHECK (cot >= 0)`. Riêng cột `ThucNhan` không đặt CHECK không âm vì theo quy chế doanh nghiệp, nếu người lao động bị phạt hoặc khấu trừ lớn hơn thu nhập thì thực nhận có thể âm."*

---

### 8.7 FOREIGN KEY Constraint: Chặn chi tiết lương cho nhân viên không tồn tại
**Cô hỏi:** *"Nếu hệ thống truyền vào một mã nhân viên ảo không có trong bảng NHANVIEN thì CSDL xử lý ra sao?"*

```sql
USE QuanLyNhanSuTienLuong;
GO

BEGIN TRY
    BEGIN TRANSACTION;
    -- Cố tình INSERT với MaNV ảo (-9999)
    INSERT INTO dbo.CHITIETBANGLUONG (MaBangLuong, MaNV, LuongCoBan, NgayCongThucTe, TienCong, TongPhuCap, TongKhauTru, ThucNhan)
    VALUES (1009, -9999, 10000000, 26, 10000000, 0, 0, 10000000);

    ROLLBACK TRANSACTION;
    SELECT N'FAIL: Không chặn được nhân viên không tồn tại' AS KetQua;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    SELECT 
        N'PASS: Đã chặn thành công mã nhân viên ảo qua FOREIGN KEY' AS KetQua,
        ERROR_NUMBER() AS MaLoi,
        ERROR_MESSAGE() AS ThongBaoLoi;
END CATCH;
```
- **Kết quả mong đợi:** Mã lỗi **`547`** — `The INSERT statement conflicted with the FOREIGN KEY constraint "FK_CTBL_NHANVIEN"`.
- **Cách trả lời cô:** *"Ràng buộc khóa ngoại `FK_CTBL_NHANVIEN` và `FK_CTBL_BANGLUONG` bảo đảm toàn vẹn tham chiếu. Dòng chi tiết lương bắt buộc phải trỏ đến một kỳ lương và một nhân viên thực sự tồn tại trong hệ thống."*

---

### 8.8 CHECK Constraint: Chặn phân vai trò tài khoản lạ
**Cô hỏi:** *"Bảng TAIKHOAN có ràng buộc gì để tránh admin cấp quyền sai lệch vai trò không?"*

```sql
USE QuanLyNhanSuTienLuong;
GO

BEGIN TRY
    BEGIN TRANSACTION;
    -- Cố tình INSERT tài khoản với vai trò lạ không nằm trong 4 roles quy định
    INSERT INTO dbo.TAIKHOAN (MaNV, TenDangNhap, MatKhau, VaiTro, TrangThai)
    VALUES (1008, 'test_hacker', 'hash123', 'Hacker_Role', 'HOAT_DONG');

    ROLLBACK TRANSACTION;
    SELECT N'FAIL: Không chặn được vai trò tài khoản không hợp lệ' AS KetQua;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    SELECT 
        N'PASS: Đã chặn thành công vai trò lạ qua CHECK Constraint' AS KetQua,
        ERROR_NUMBER() AS MaLoi,
        ERROR_MESSAGE() AS ThongBaoLoi;
END CATCH;
```
- **Kết quả mong đợi:** Mã lỗi **`547`** — `The INSERT statement conflicted with the CHECK constraint "CHK_TAIKHOAN_VaiTro"`.
- **Cách trả lời cô:** *"Bảng `TAIKHOAN` được kiểm soát chặt chẽ bằng `CHECK (VaiTro IN ('DB_Admin', 'HR_Manager', 'Payroll_Officer', 'Employee'))`, đảm bảo khớp 100% với 4 Database Roles của phân hệ bảo mật RBAC."*

---

### 8.9 Xem toàn bộ danh mục các Ràng buộc toàn vẹn CSDL (Metadata query)
**Cô hỏi:** *"Em hãy viết câu lệnh truy vấn metadata trong SQL Server để liệt kê toàn bộ các ràng buộc (CHECK, UNIQUE, FOREIGN KEY, DEFAULT) đang có trên các bảng của em."*

```sql
USE QuanLyNhanSuTienLuong;
GO

SELECT 
    t.name AS Bang,
    c.name AS TenRangBuoc,
    c.type_desc AS LoaiRangBuoc,
    OBJECT_DEFINITION(c.object_id) AS DinhNghiaChiTiet
FROM sys.objects c
JOIN sys.tables t ON t.object_id = c.parent_object_id
WHERE t.name IN ('BANGLUONG', 'CHITIETBANGLUONG', 'TAIKHOAN')
  AND c.type_desc LIKE '%CONSTRAINT'
ORDER BY t.name, c.type_desc, c.name;
```
- **Kết quả mong đợi:** Xuất ra danh sách đầy đủ tất cả các ràng buộc trên 3 bảng `BANGLUONG`, `CHITIETBANGLUONG`, `TAIKHOAN`.
- **Cách trả lời cô:** *"Thưa cô, em truy vấn trực tiếp từ catalog views `sys.objects` và `sys.tables` kết hợp hàm `OBJECT_DEFINITION()` để xem định nghĩa chi tiết của từng ràng buộc mà SQL Server đang thực thi."*


---

## BẢNG TỔNG HỢP MÃ LỖI ĐỂ TỰ TIN TRẢ LỜI NGAY

| Mã lỗi SSMS | Ý nghĩa | Khi nào xuất hiện? |
|:---:|---|---|
| **Msg 229** | *The EXECUTE/SELECT/UPDATE permission was denied...* | Do hệ thống **Phân quyền 4 Roles (RBAC)** của TV5 chặn (HR chốt lương, Payroll sửa nhân viên, Employee đọc bảng lương). |
| **Msg 50000** | Lỗi người dùng định nghĩa (`RAISERROR` mức 16) | Do **Trigger khóa kỳ** hoặc **Stored Procedure bẫy nghiệp vụ** phát hiện vi phạm (Chốt lại kỳ đã chốt, thêm chấm công vào kỳ đã khóa, sửa thực nhận, xóa nhân viên đã có lương). |
| **Msg 547** | *The INSERT/UPDATE statement conflicted with the CHECK/FOREIGN KEY constraint...* | Do **Ràng buộc toàn vẹn CSDL (CHECK, FK)** chặn (Nhập tháng > 12, trạng thái lương lạ, ngày công $\le 0$, lương âm, nhân viên không tồn tại, vai trò tài khoản lạ). |
| **Msg 2627 / 2601** | *Violation of UNIQUE KEY constraint...* | Do **Ràng buộc duy nhất (UNIQUE)** chặn (Tạo trùng 2 bảng lương trong cùng tháng/năm, hoặc 1 nhân viên có 2 dòng chi tiết lương trong cùng 1 kỳ). |
| **0 rows** | Không có dòng nào trả về | Khi tài khoản không khớp trong `vw_PhieuLuongCaNhan` (**Row-Level Security** bảo vệ thành công). |


---

## PHẦN 9: CÚ PHÁP GÕ TRỰC TIẾP KHI GIẢNG VIÊN YÊU CẦU SỬA NÓNG (LIVE-CODING)

### 9.1 Cú pháp Phân quyền lại (GRANT / REVOKE / DENY / ROLE / LOGIN)
```sql
-- 1. Cấp quyền (GRANT): Cho phép role_HRManager thực thi sp_ChotBangLuong
GRANT EXECUTE ON dbo.sp_ChotBangLuong TO role_HRManager;
-- (Nếu trên bảng/view: GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.TenBang TO TenRole;)

-- 2. Thu hồi quyền đã cấp hoặc gỡ lệnh cấm (REVOKE):
REVOKE EXECUTE ON dbo.sp_ChotBangLuong FROM role_HRManager;
-- Lưu ý: REVOKE dùng từ khóa FROM (còn GRANT và DENY dùng từ khóa TO)

-- 3. Cấm tuyệt đối (DENY - ưu tiên cao nhất, đè lên mọi GRANT):
DENY EXECUTE ON dbo.sp_ChotBangLuong TO role_HRManager;

-- 4. Thêm / Xóa một User vào/khỏi Database Role:
ALTER ROLE role_PayrollOfficer ADD MEMBER gui_hr;
ALTER ROLE role_PayrollOfficer DROP MEMBER gui_hr;

-- 5. Tạo nhanh Login + User + Gán Role mới hoàn toàn khi cô yêu cầu:
CREATE LOGIN test_gv WITH PASSWORD = N'Test@123456', CHECK_POLICY = ON;
CREATE USER test_gv FOR LOGIN test_gv;
ALTER ROLE role_HRManager ADD MEMBER test_gv;
-- Dọn dẹp sau khi demo xong:
DROP USER test_gv;
DROP LOGIN test_gv;

-- 6. Kiểm tra danh sách quyền đang cấp/cấm của các Role trong DB:
SELECT dp.state_desc AS TrangThaiQuyen, dp.permission_name AS Quyen,
       OBJECT_NAME(dp.major_id) AS DoiTuong, pr.name AS TenRole
FROM sys.database_permissions dp
JOIN sys.database_principals pr ON dp.grantee_principal_id = pr.principal_id
WHERE pr.name LIKE 'role_%'
ORDER BY pr.name, dp.state_desc;
```

### 9.2 Cú pháp Sửa View RLS (`vw_PhieuLuongCaNhan`)
**Tình huống:** Cô hỏi *"Tại sao dùng `EXECUTE AS LOGIN = 'gui_a'` mà `ORIGINAL_LOGIN()` không đổi? Sửa View để hỗ trợ cả `EXECUTE AS` hoặc chỉ hiển thị kỳ lương `DA_CHOT`?"*
```sql
CREATE OR ALTER VIEW dbo.vw_PhieuLuongCaNhan
AS
SELECT bl.*
FROM dbo.vw_BangLuongChiTiet bl
JOIN dbo.TAIKHOAN tk ON tk.MaNV = bl.MaNV
WHERE tk.SqlLogin IN (ORIGINAL_LOGIN(), SUSER_SNAME())
  AND tk.TrangThai = 'HOAT_DONG'
  -- Nếu cô yêu cầu chỉ cho nhân viên xem kỳ đã chốt thì thêm dòng dưới:
  -- AND bl.TrangThaiBangLuong = 'DA_CHOT'
;
GO
```

### 9.3 Cú pháp Bật / Tắt hoặc Sửa nhanh Trigger
```sql
-- Tắt tạm thời 1 Trigger trên bảng:
DISABLE TRIGGER dbo.trg_ChamCong_KhongSuaKhiDaChotLuong ON dbo.CHAMCONG;

-- Bật lại Trigger:
ENABLE TRIGGER dbo.trg_ChamCong_KhongSuaKhiDaChotLuong ON dbo.CHAMCONG;

-- Xem code của bất kỳ Trigger / SP / View / Function nào ngay trên SSMS:
EXEC sp_helptext 'dbo.trg_ChamCong_KhongSuaKhiDaChotLuong';
```

### 9.4 Cú pháp Tạo / Tắt / Bật lại Index & Xem Execution Plan
```sql
-- Tắt Index (Index vẫn tồn tại trong metadata nhưng bộ tối ưu bỏ qua):
ALTER INDEX IX_NHANVIEN_MaPB_MaCV ON dbo.NHANVIEN DISABLE;

-- Bật lại (Rebuild) Index sau khi Disable:
ALTER INDEX IX_NHANVIEN_MaPB_MaCV ON dbo.NHANVIEN REBUILD;

-- Xóa và Tạo mới Covering Index:
DROP INDEX IF EXISTS IX_NHANVIEN_MaPB_MaCV ON dbo.NHANVIEN;
CREATE NONCLUSTERED INDEX IX_NHANVIEN_MaPB_MaCV
ON dbo.NHANVIEN (MaPB, MaCV)
INCLUDE (MaNV, HoTen, LuongCoBan, TrangThai);
```

### 9.5 Cú pháp Thêm / Xóa Ràng buộc (Constraints) trên Bảng đã có
```sql
-- Xóa một Constraint:
ALTER TABLE dbo.BANGLUONG DROP CONSTRAINT CHK_BANGLUONG_Thang;

-- Thêm lại CHECK Constraint:
ALTER TABLE dbo.BANGLUONG ADD CONSTRAINT CHK_BANGLUONG_Thang CHECK (Thang BETWEEN 1 AND 12);

-- Thêm UNIQUE Constraint:
ALTER TABLE dbo.BANGLUONG ADD CONSTRAINT UQ_BANGLUONG_ThangNam UNIQUE (Thang, Nam);
```

### 9.6 Cú pháp Kiểm tra Khóa (Locks) đang giữ trong lúc Demo Concurrency (Phần 7)
**Tình huống:** Khi Cửa sổ 2 đang bị treo chờ Cửa sổ 1, cô hỏi *"Làm sao biết chính xác Session nào đang chặn Session nào và giữ khóa gì?"*
```sql
-- Xem phiên nào đang bị BLOCK và ai là thủ phạm (blocking_session_id):
SELECT session_id, blocking_session_id, wait_type, wait_time, status, command
FROM sys.dm_exec_requests
WHERE blocking_session_id > 0;

-- Xem chi tiết loại khóa (U, X, S, IX...) đang giữ trong DB:
SELECT request_session_id AS Spid, resource_type,
       OBJECT_NAME(resource_associated_entity_id) AS TenBang,
       request_mode AS LoaiKhoa, request_status AS TrangThai
FROM sys.dm_tran_locks
WHERE resource_database_id = DB_ID('QuanLyNhanSuTienLuong')
  AND resource_type IN ('OBJECT', 'KEY', 'PAGE', 'RID');
```
