# HƯỚNG DẪN ĐỒNG BỘ HIỆN TRẠNG HỆ THỐNG & TỔNG HỢP BÁO CÁO CUỐI KỲ
## Đề tài: Hệ thống Quản lý Nhân sự và Tiền lương – Nhóm 06 – DBMS330284
**Giảng viên hướng dẫn:** TS. Phan Thị Thể  
**Ngày cập nhật:** 29/09/2026 (Tuần 2)  
**Phiên bản:** 2.0 (Bản chuẩn hóa theo mã nguồn thực tế trên nhánh `main`)

---

> **MỤC ĐÍCH TÀI LIỆU:**  
> Tài liệu này tổng hợp toàn bộ hiện trạng mã nguồn, cơ sở dữ liệu và công cụ kiểm thử thực tế đã được tích hợp lên nhánh `main`. Đây là **căn cứ duy nhất (Single Source of Truth)** để 5 thành viên đối chiếu, cập nhật lại phần việc của mình và đồng bộ nội dung khi viết **Báo cáo tổng hợp cuối kỳ (50–100 trang)** và **Slide thuyết trình**.

---

## 1. PHÂN TÍCH HIỆN TRẠNG VÀ CÁC ĐIỂM DỊCH CHUYỂN SO VỚI TÀI LIỆU CŨ

Trong quá trình phát triển ở Tuần 2, hệ thống đã được tối ưu hóa và mở rộng nhằm giải quyết các bài toán thực tế, dẫn đến một số điểm khác biệt tích cực so với tài liệu ban đầu:

### 1.1 Dịch chuyển quyền sở hữu (Ownership) các đối tượng SQL
* **Ban đầu (Kế hoạch):** Giao `sp_GhiNhanChamCong` và `trg_ChamCong_KiemTraGio` cho TV3.
* **Thực tế trên mã nguồn:** Do 2 đối tượng này gắn liền với bảng `CHAMCONG`, **TV2 (Phạm Minh Quân)** đã trực tiếp cài đặt hoàn chỉnh trong `database/02_Module_ChamCong_TV2.sql` để module Chấm công hoạt động độc lập và trơn tru.
* **Bổ sung chuẩn Rubric (`06_BoSung_Rubric_Full.sql`):** TV5 đã bổ sung các đối tượng CSDL còn thiếu gồm `fn_TongPhuCap` (TV2), `fn_TongKhauTru` (TV3), `vw_TongPhuCapThang` (TV3), `sp_CapNhatNhanVien` (TV2) để đảm bảo chuỗi tính toán lương và rubric 100% thông suốt.

### 1.2 Mở rộng quy trình nghiệp vụ: Quy trình linh hoạt (Flexible Workflow)
* **Quy trình cũ (Cứng nhắc):** Chấm công $\rightarrow$ Tính lương $\rightarrow$ Chốt lương $\rightarrow$ Khóa vĩnh viễn không thể sửa chữa.
* **Quy trình thực tế (Thực tế doanh nghiệp):** 
  * Bổ sung Stored Procedure **`sp_HuyChotBangLuong`** (nút *Hủy chốt lương* trên `BaoCaoPanel`).
  * Bổ sung Stored Procedure **`sp_XoaBangLuongChuaChot`** (nút *Xóa kỳ lương chưa chốt* trên `BangLuongPanel`).
  * Bổ sung **`DieuChinhChamCongDialog`**: Cho phép kế toán/nhân sự drill-down từ bảng tổng hợp tháng để xem chi tiết, sửa giờ, xóa hoặc bù công từng ngày.
  * **Ý nghĩa:** Nếu phát hiện sai sót sau khi chốt, người có thẩm quyền (`DB_Admin` hoặc `Payroll_Officer`) có thể Hủy chốt để mở lại kỳ lương, điều chỉnh chấm công/phụ cấp, rồi tính lại lương một cách an toàn.

### 1.3 Kiến trúc Bảo mật 2 tầng (Two-Tier Security Architecture)
* **Tầng 1 (Database Layer):** 4 Role SQL Server (`role_DBAdmin`, `role_HRManager`, `role_PayrollOfficer`, `role_Employee`) được cấp quyền bằng `GRANT` và chặn bằng `DENY` trực tiếp trên từng bảng, View, SP.
* **Tầng 2 (Application Layer):** `Session` Singleton quản lý phiên đăng nhập, phân quyền hiển thị MenuBar theo ma trận RBAC, kiểm tra quyền tại Service (`ChamCongService.checkPermission`, `PayrollService`, `AuthService`).
* **Mật khẩu an toàn:** Băm một chiều bằng thuật toán **SHA-256** chuẩn quốc tế trong `PasswordUtil.java`.

### 1.4 Công cụ kiểm thử & Tự động hóa mới (Tooling)
Dự án đã phát triển thêm các công cụ CLI và GUI độc lập giúp kiểm thử và chạy demo không phụ thuộc vào IDE cá nhân:
1. `start_app.ps1`: Khởi chạy ứng dụng tự động dò tìm JDBC Driver đa nền tảng.
2. `test_roles.ps1`: Mở giao diện `QuickTestLauncher` test nhanh 4 vai trò.
3. `run_tests_cli.ps1`: Chạy bộ kiểm thử tự động headless không cần mở giao diện.
4. `CaptureScreenshots.java`: Chụp tự động ảnh chụp màn hình tất cả các panel để làm minh chứng báo cáo.

---

## 2. MA TRẬN ĐỐI TƯỢNG CSDL CHÍNH THỨC CỦA NHÓM (CHUẨN RUBRIC)

Bảng này phân định rõ quyền sở hữu và vai trò của từng thành viên khi viết báo cáo cá nhân và bảo vệ vấn đáp:

| Thành viên | Bảng phụ trách | Stored Procedure | Function | Trigger | View | Index (Covering) | Transaction |
|---|---|---|---|---|---|---|---|
| **TV1**<br>Nguyễn Minh Trí | `PHONGBAN`<br>`CHUCVU`<br>`NHANVIEN`<br>`TAIKHOAN` | `sp_ThemNhanVien` | `fn_TinhSoNgayCong` | `trg_NhanVien_KhongXoaKhiDaPhatSinhLuong` | `vw_NhanVien_PhongBan_ChucVu` | `IX_NHANVIEN_HoTen`<br>*(tìm kiếm theo tên)* | Tạo nhân viên + tài khoản đồng thời |
| **TV2**<br>Phạm Minh Quân | `CHAMCONG` | `sp_GhiNhanChamCong`<br>`sp_CapNhatNhanVien` | `fn_TongPhuCap` | `trg_ChamCong_KiemTraNhanVien`<br>`trg_ChamCong_KiemTraGio` | `vw_TongHopChamCongThang` | `IX_CHAMCONG_MaNV_Ngay`<br>*(INCLUDE GioVao, GioRa, TrangThai)* | Nhập chấm công theo lô (All-or-Nothing) |
| **TV3**<br>Trần Tiến Đạt | `PHUCAPNHANVIEN`<br>`KHAUTRUNHANVIEN` | Phối hợp `sp_GhiNhanChamCong` | `fn_TongKhauTru` | Phối hợp `trg_ChamCong_KiemTraGio` | `vw_TongPhuCapThang` | `IX_PHUCAP_MaNV_ThangNam`<br>*(INCLUDE SoTien, TenPhuCap)* | Xóa kỳ lương chưa chốt |
| **TV4**<br>Nguyễn Quang Vinh | `BANGLUONG`<br>`CHITIETBANGLUONG` | `sp_TinhBangLuongThang`<br>`sp_XoaBangLuongChuaChot` | `fn_TinhTienCong` | `trg_BangLuong_KhongSuaKhiDaChot` | `vw_TongKhauTruThang` | `IX_KHAUTRU_MaNV_ThangNam`<br>*(INCLUDE SoTien, TenKhauTru)* | Tính bảng lương tự động toàn bộ nhân viên |
| **TV5**<br>Trần Đức Anh | Kiến trúc hệ thống & Bảo mật | `sp_ChotBangLuong`<br>`sp_HuyChotBangLuong` | `fn_TinhThucNhan` | `trg_ChiTietLuong_KhongSuaKhiDaChot` | `vw_BangLuongChiTiet` | `IX_NHANVIEN_MaPB_MaCV`<br>*(INCLUDE MaNV, HoTen, LuongCoBan)* | Chốt bảng lương an toàn (`UPDLOCK, HOLDLOCK`) |

---

## 3. CÔNG THỨC VÀ ĐƯỜNG ỐNG (PIPELINE) TÍNH LƯƠNG HOÀN CHỈNH

Hệ thống tính lương vận hành qua 5 bước liên kết chặt chẽ giữa các Function và Stored Procedure:

$$\text{Số ngày công} = \text{fn\_TinhSoNgayCong}(\text{MaNV}, \text{Thang}, \text{Nam})$$
$$\text{Tiền công} = \text{fn\_TinhTienCong}(\text{LuongCoBan}, \text{NgayCongChuan} = 22, \text{SoNgayCong})$$
$$\text{Tổng phụ cấp} = \text{fn\_TongPhuCap}(\text{MaNV}, \text{Thang}, \text{Nam}) + \text{PhuCapChucVu}$$
$$\text{Tổng khấu trừ} = \text{fn\_TongKhauTru}(\text{MaNV}, \text{Thang}, \text{Nam})$$
$$\text{Thực nhận} = \text{fn\_TinhThucNhan}(\text{Tiền công}, \text{Tổng phụ cấp}, \text{Tổng khấu trừ})$$

* Toàn bộ phép tính trên được thực hiện tập trung bên trong stored procedure `sp_TinhBangLuongThang` và lưu vào bảng `CHITIETBANGLUONG`.

---

## 4. DÀN Ý VÀ HƯỚNG DẪN PHÂN CÔNG VIẾT BÁO CÁO TỔNG HỢP (50–100 TRANG)

Cấu trúc chuẩn theo rubric đồ án DBMS330284:

### Chương 1. TỔNG QUAN VÀ PHÂN TÍCH NGHIỆP VỤ HỆ THỐNG (15–20 trang)
* **Thành viên chịu trách nhiệm chính:** **TV1 (Minh Trí)** phối hợp **TV5 (Đức Anh)**.
* **Nội dung:**
  1. Lý do chọn đề tài và mục tiêu xây dựng hệ thống.
  2. Bảng phân tích tác nhân (Actors) và Use Case Diagram tổng thể.
  3. Mô tả chi tiết 4 phân hệ: Nhân sự (TV1), Chấm công (TV2), Phụ cấp/Khấu trừ (TV3), Tính lương/Báo cáo (TV4 & TV5).
  4. Bảng phân tích ma trận chức năng CRUD theo vai trò người dùng.

### Chương 2. THIẾT KẾ CƠ SỞ DỮ LIỆU VÀ CÁC ĐỐI TƯỢNG SQL (25–35 trang)
* **Thành viên chịu trách nhiệm chính:** **Tất cả thành viên** (mỗi người viết phần đối tượng mình sở hữu).
* **Nội dung:**
  1. Sơ đồ thực thể kết hợp (ERD) và Lược đồ quan hệ chuẩn hóa 3NF (**TV1**).
  2. Đặc tả chi tiết 9 bảng: Kiểu dữ liệu, Khóa chính, Khóa ngoại, Ràng buộc `CHECK`, `DEFAULT`, `UNIQUE`.
  3. Trình bày chi tiết 6 Stored Procedures: Mục đích, tham số vào/ra, xử lý `TRY...CATCH`, mã nguồn SQL.
  4. Trình bày chi tiết 5 Functions tính toán.
  5. Trình bày chi tiết 5 Triggers nghiệp vụ: Giải thích cơ chế `inserted`/`deleted`, tính set-based và câu lệnh `ROLLBACK`.
  6. Trình bày chi tiết 5 Views tổng hợp.
  7. Trình bày chi tiết 5 Covering Indexes: Phân tích lý do chọn cột khóa và mệnh đề `INCLUDE`.

### Chương 3. BẢO MẬT, AN TOÀN DỮ LIỆU VÀ ĐIỀU KHIỂN ĐỒNG THỜI (15–20 trang)
* **Thành viên chịu trách nhiệm chính:** **TV5 (Đức Anh)**.
* **Nội dung:**
  1. Mô hình bảo mật 2 tầng (Two-Tier Security).
  2. Bảng ma trận phân quyền chi tiết cho 4 Role SQL Server và các lệnh `GRANT`, `DENY`.
  3. Cơ chế mã hóa băm mật khẩu SHA-256.
  4. Phân tích bài toán tranh chấp dữ liệu (Concurrency Control): Tình huống 2 kế toán cùng chốt 1 kỳ lương (Lost Update).
  5. Giải pháp kỹ thuật: Khóa bi quan với gợi ý khóa `WITH (UPDLOCK, HOLDLOCK)` ở mức cô lập cao. Kèm ảnh chụp minh chứng demo 2 session SSMS.

### Chương 4. KIẾN TRÚC VÀ CÀI ĐẶT ỨNG DỤNG JAVA SWING (15–20 trang)
* **Thành viên chịu trách nhiệm chính:** **TV5 (Đức Anh)** phối hợp **TV2 (Minh Quân)**, **TV4 (Quang Vinh)**.
* **Nội dung:**
  1. Sơ đồ kiến trúc phân lớp: Presentation $\rightarrow$ Session $\rightarrow$ Service $\rightarrow$ DAO/JDBC $\rightarrow$ Database.
  2. Thiết kế giao diện Dashboard Trang chủ, Form nhân sự, Panel Chấm công, Panel Tính lương và Báo cáo chi tiết.
  3. Triển khai 100% `PreparedStatement` và `CallableStatement` phòng chống SQL Injection.
  4. Quản lý giao dịch (Transaction Management) tại tầng Service: Cơ chế `setAutoCommit(false)`, `commit()` và `rollback()`.

### Chương 5. KIỂM THỬ HỆ THỐNG VÀ ĐO HIỆU NĂNG (BENCHMARK) (15–20 trang)
* **Thành viên chịu trách nhiệm chính:** **Toàn bộ nhóm**.
* **Nội dung:**
  1. Bảng tổng hợp kết quả chạy Test Case của từng phân hệ (PASS/FAIL).
  2. Minh chứng kích hoạt Trigger khi vi phạm ràng buộc (ảnh chụp thông báo lỗi).
  3. Minh chứng Transaction Rollback khi có lỗi dữ liệu.
  4. **Benchmark Index (Tuần 3):** Ảnh chụp so sánh `Actual Execution Plan` và thống kê `SET STATISTICS IO, TIME ON` giữa trường hợp có Index (Index Seek) và không có Index (Table Scan / Clustered Index Scan).
  5. Minh chứng kiểm thử phân quyền SQL Server với câu lệnh `EXECUTE AS USER`.

---

## 5. CHECKLIST ĐỒNG BỘ CHO CÁC THÀNH VIÊN TRƯỚC KHI NỘP BÀI

- [x] CSDL 9 bảng đã chuẩn hóa 3NF, không có dư thừa dữ liệu.
- [x] Bộ script SQL chạy theo thứ tự: `01_Module_NhanSu_TV1.sql` $\rightarrow$ `02_Module_ChamCong_TV2.sql` $\rightarrow$ `03_phucap_khautru_TV3.sql` $\rightarrow$ `04_Module_TinhLuong_TV4.sql` $\rightarrow$ `05_Security_Payroll_TV5.sql` $\rightarrow$ `06_BoSung_Rubric_Full.sql`.
- [x] Ứng dụng biên dịch 100% không có lỗi bằng lệnh `javac` qua file `start_app.ps1`.
- [x] Mỗi thành viên nắm vững phần code, đối tượng SQL và kịch bản demo cá nhân để tự tin trả lời vấn đáp độc lập với giảng viên.
