# TV5 – BÁO CÁO TIẾN ĐỘ THỰC HIỆN & THIẾT KẾ MODULE BẢO MẬT, CHỐT LƯƠNG & BÁO CÁO
# Đề tài: Hệ thống Quản lý Nhân sự và Tiền lương – Nhóm 06 – DBMS330284

- **Thành viên phụ trách:** Trần Đức Anh (TV5)  
- **MSSV:** 24110155  
- **Mã phân công:** TV5  
- **Module phụ trách:** Kiến trúc hệ thống, Phân quyền & Bảo mật (2 tầng), Chốt bảng lương (Concurrency UPDLOCK), Báo cáo chi tiết & Quản trị tài khoản  
- **Branch làm việc:** `feature/auth-security-integration`

---

## PHẦN 1. BẢNG TỔNG HỢP MỨC ĐỘ HOÀN THÀNH THEO CÁC TUẦN (T1, T2, T3)

| Tuần | Thời gian | Nội dung công việc được giao | Sản phẩm dự kiến theo kế hoạch | Mức độ hoàn thành | Ngày cập nhật | Tình trạng & Sản phẩm thực tế |
|:---:|:---:|---|---|:---:|:---:|---|
| **T1** | 21/09 – 27/09 | • Chốt kiến trúc 4 tầng (Presentation → Service → DAO/JDBC → SQL Server) và cấu trúc thư mục `src/`.<br>• Phân tích `TAIKHOAN`, `Session` singleton và 4 vai trò: `DB_Admin`, `HR_Manager`, `Payroll_Officer`, `Employee`.<br>• Thiết kế ma trận phân quyền ứng dụng và CSDL (GRANT, REVOKE, DENY).<br>• Thiết kế giải pháp Concurrency bằng `UPDLOCK + HOLDLOCK` khi 2 kế toán lương cùng chốt 1 kỳ lương.<br>• Chuẩn hóa repository GitHub, ruleset protect-main, PR template, CODEOWNERS, checklist tích hợp. | • Architecture diagram.<br>• Role/permission matrix.<br>• Cấu trúc source/repository.<br>• Kịch bản concurrency và security demo.<br>• Integration checklist. | **100%** | **25/09/2026** | Đã hoàn thành 100% các tài liệu: `TV5_Architecture.md`, `TV5_Security_Design.md`, `TV5_Concurrency_Security_Demo.md`, `TV5_Integration_Checklist.md`. |
| **T2** | 28/09 – 04/10 | • Cài đặt DDL/Constraints `BANGLUONG`, `CHITIETBANGLUONG` (dự phòng độc lập `IF NOT EXISTS` theo thiết kế TV4).<br>• Cài đặt SP `sp_ChotBangLuong` (có Transaction + `UPDLOCK, HOLDLOCK` chống Lost Update).<br>• Cài đặt Trigger `trg_ChiTietLuong_KhongSuaKhiDaChot`.<br>• Cài đặt Function `fn_TinhThucNhan`.<br>• Cài đặt View `vw_BangLuongChiTiet`.<br>• Cài đặt Non-clustered Index `IX_NHANVIEN_MaPB_MaCV`.<br>• Cài đặt 4 Role/Login/User SQL Server + kịch bản GRANT/DENY chi tiết.<br>• Lập trình Java Swing: `LoginFrame`, `MainFrame`, `BaoCaoPanel`, `TaiKhoanPanel`.<br>• Lập trình Model, DAO, Service: `TaiKhoan`, `BangLuong`, `ChiTietBangLuong`, `TaiKhoanDAO`, `BangLuongDAO`, `AuthService`, `PayrollService`, `Session`. | • SQL script bảo mật & chốt lương.<br>• Màn hình đăng nhập & khung chính phân quyền menu theo Role.<br>• Màn hình báo cáo & chốt lương an toàn transaction.<br>• Màn hình quản trị tài khoản (khóa/mở khóa, reset mật khẩu).<br>• Compile Java 100% thành công. | **95%**<br>*(Toàn bộ mã nguồn & script đã xong, chờ chạy trên DB máy giáo viên để lấy ảnh test)* | **25/09/2026** | • Script SQL: `database/05_Security_Payroll_TV5.sql` (437 dòng).<br>• Java source: đầy đủ 4 tầng, build `javac` không lỗi, sẵn sàng demo. |
| **T3** | 05/10 – 11/10 | • Tích hợp toàn diện với các module của TV2 (Chấm công), TV3 (Phụ cấp/Khấu trừ), TV4 (Tính lương).<br>• Benchmark hiệu năng Index `IX_NHANVIEN_MaPB_MaCV` (`SET STATISTICS IO/TIME`, Execution Plan).<br>• Kiểm thử kịch bản Concurrency trực tiếp trên 2 session SSMS/Java.<br>• Kiểm thử phân quyền truy cập SQL Server với 4 Login/Role (`EXECUTE AS USER`).<br>• Đóng góp nội dung Chương 2, 4, 5 trong báo cáo Word/PDF và hoàn thiện Slide thuyết trình. | • Kết quả benchmark Index.<br>• Ảnh chụp minh chứng Concurrency & Security.<br>• Báo cáo tổng hợp nhóm. | **20%** | **25/09/2026** | Đã sẵn sàng toàn bộ kịch bản testcase và câu lệnh kiểm thử, chỉ chờ nhập dữ liệu đầy đủ từ các thành viên. |

---

## PHẦN 2. MA TRẬN ĐỐI TƯỢNG CSDL DO TV5 SỞ HỮU

Căn cứ ma trận phân công tại `Ke_hoach_phan_cong_Project_DBMS_Nhom06.md`:

| STT | Loại đối tượng | Tên định danh | Mục đích kỹ thuật | Trạng thái |
|:---:|---|---|---|:---:|
| 1 | **Stored Procedure** | `sp_ChotBangLuong` | Khóa kỳ lương: Dùng `WITH (UPDLOCK, HOLDLOCK)` chống xung đột Concurrency giữa 2 kế toán; kiểm tra điều kiện (tồn tại, chưa chốt, có chi tiết lương) trước khi UPDATE `TrangThai = 'DA_CHOT'`; TRY...CATCH + Transaction | ✅ Hoàn thành (`05_Security_Payroll_TV5.sql`) |
| 2 | **Function** | `fn_TinhThucNhan` | Tính lương thực nhận: `TienCong + TongPhuCap - TongKhauTru`. Xử lý giá trị NULL an toàn bằng `ISNULL()` | ✅ Hoàn thành (`05_Security_Payroll_TV5.sql`) |
| 3 | **Trigger** | `trg_ChiTietLuong_KhongSuaKhiDaChot` | `AFTER UPDATE, DELETE` trên `CHITIETBANGLUONG`: Chặn mọi thao tác sửa hoặc xóa chi tiết lương khi kỳ lương tương ứng đã có `TrangThai = 'DA_CHOT'` | ✅ Hoàn thành (`05_Security_Payroll_TV5.sql`) |
| 4 | **View** | `vw_BangLuongChiTiet` | JOIN 5 bảng: `BANGLUONG` + `CHITIETBANGLUONG` + `NHANVIEN` + `PHONGBAN` + `CHUCVU`. Cung cấp dữ liệu chuẩn cho `BaoCaoPanel` và phiếu lương nhân viên | ✅ Hoàn thành (`05_Security_Payroll_TV5.sql`) |
| 5 | **Index** | `IX_NHANVIEN_MaPB_MaCV` | Non-clustered Index trên `NHANVIEN(MaPB, MaCV)` INCLUDE `(MaNV, HoTen, LuongCoBan, TrangThai)`. Tối ưu truy vấn lọc theo phòng ban và chức vụ | ✅ Hoàn thành (`05_Security_Payroll_TV5.sql`) |
| 6 | **Transaction** | Giao dịch chốt lương | Đảm bảo nguyên tắc ACID trong `sp_ChotBangLuong` | ✅ Hoàn thành (`05_Security_Payroll_TV5.sql`) |

---

## PHẦN 3. KIẾN TRÚC PHÂN LỚP VÀ MÃ NGUỒN JAVA DO TV5 TRIỂN KHAI

### 1. Sơ đồ các thành phần Java Swing do TV5 xây dựng

```
[Presentation Layer]
  ├── com.ui.auth.LoginFrame (Màn hình đăng nhập, SwingWorker non-blocking)
  ├── com.ui.main.MainFrame (Khung giao diện chính, JTabbedPane, lọc menu theo vai trò)
  ├── com.ui.baocao.BaoCaoPanel (Báo cáo lương chi tiết qua View, tích hợp nút Chốt lương)
  └── com.ui.admin.TaiKhoanPanel (Quản trị tài khoản: Khóa/Mở khóa, Đặt lại mật khẩu, Đổi vai trò)

[Session & Security Layer]
  ├── com.session.Session (Singleton lưu trữ phiên làm việc, hasRole(), getDisplayName())
  └── com.util.PasswordUtil (Băm mật khẩu SHA-256 an toàn chuẩn FIPS)

[Service Layer]
  ├── com.service.AuthService (Xác thực đăng nhập, kiểm tra tài khoản khóa, quản trị tài khoản)
  └── com.service.PayrollService (Kiểm tra quyền, gọi chốt bảng lương, truy vấn báo cáo qua View)

[DAO Layer]
  ├── com.dao.TaiKhoanDAO (findByCredentials, getAll, updateTrangThai, resetPassword, updateVaiTro)
  └── com.dao.BangLuongDAO (chotBangLuong via CallableStatement, getChiTietByBangLuong via View)

[Model Layer]
  ├── com.model.TaiKhoan (MaTK, MaNV, TenDangNhap, MatKhau, VaiTro, TrangThai, HoTenNV)
  ├── com.model.BangLuong (MaBangLuong, Thang, Nam, NgayCongChuan, TrangThai, NgayTao, NgayChot)
  └── com.model.ChiTietBangLuong (Chi tiết lương + thông tin hiển thị từ View)
```

---

## PHẦN 4. HỆ THỐNG PHÂN QUYỀN 2 TẦNG (TWO-TIER SECURITY)

### Tầng 1: Phân quyền tại Giao diện Ứng dụng (Java Session)
- Khi người dùng đăng nhập thành công qua `LoginFrame`, `Session.getInstance().login(...)` ghi nhận vai trò (`DB_Admin`, `HR_Manager`, `Payroll_Officer`, `Employee`).
- `MainFrame.applyRolePermissions()` tự động ẩn/hiện các menu:
  - **Menu Nhân viên / Danh mục / Chấm công:** Chỉ hiển thị cho `DB_Admin`, `HR_Manager`.
  - **Menu Phụ cấp & Khấu trừ:** Hiển thị cho `DB_Admin`, `HR_Manager`, `Payroll_Officer`.
  - **Menu Lương:** Chỉ hiển thị cho `DB_Admin`, `Payroll_Officer`.
  - **Menu Báo cáo:** Mọi vai trò đều thấy; riêng `Employee` chỉ hiển thị "Phiếu lương cá nhân".
  - **Menu Quản trị:** Chỉ hiển thị duy nhất cho `DB_Admin`.

### Tầng 2: Phân quyền tại Cơ sở Dữ liệu (SQL Server Roles & Logins)
- Được cài đặt đầy đủ trong file `database/05_Security_Payroll_TV5.sql`:
  - 4 Server Logins: `login_DBAdmin`, `login_HRManager`, `login_PayrollOfficer`, `login_Employee`.
  - 4 Database Users: `user_DBAdmin`, `user_HRManager`, `user_PayrollOfficer`, `user_Employee`.
  - 4 Database Roles:
    - `role_DBAdmin`: Thành viên của `db_owner`, có toàn quyền quản trị.
    - `role_HRManager`: Được `GRANT SELECT, INSERT, UPDATE` trên `NHANVIEN`, `PHONGBAN`, `CHUCVU`, `PHUCAPNHANVIEN`, `KHAUTRUNHANVIEN`; bị `DENY EXECUTE` trên `sp_ChotBangLuong`; bị `DENY` trên bảng `TAIKHOAN`.
    - `role_PayrollOfficer`: Được `GRANT SELECT, INSERT` trên `BANGLUONG`, `CHITIETBANGLUONG`; `GRANT EXECUTE` trên `sp_ChotBangLuong`, `fn_TinhThucNhan`; bị `DENY INSERT, UPDATE, DELETE` trên `NHANVIEN`, `PHONGBAN`, `CHUCVU`.
    - `role_Employee`: Bị `DENY` toàn bộ 9 bảng dữ liệu; chỉ được `GRANT SELECT` trên View `vw_BangLuongChiTiet`.

---

## PHẦN 5. KỊCH BẢN KIỂM THỬ SẴN SÀNG CHO BUỔI BÁO CÁO (DEMO GVHD)

### 1. Kịch bản Concurrency (sp_ChotBangLuong)
- **Tình huống:** Kế toán A và Kế toán B cùng mở kỳ lương tháng 9/2026 và cùng ấn nút "Chốt bảng lương" tại cùng một thời điểm.
- **Cơ chế xử lý:** Câu lệnh `SELECT TrangThai FROM BANGLUONG WITH (UPDLOCK, HOLDLOCK) WHERE MaBangLuong = @MaBangLuong` sẽ giữ khóa cập nhật trên dòng đó. Session thứ hai buộc phải đợi cho đến khi Session thứ nhất hoàn tất giao dịch. Khi Session thứ hai tiếp tục, giá trị `TrangThai` đọc được đã là `'DA_CHOT'`, trigger câu lệnh `RAISERROR(N'Kỳ lương đã được chốt trước đó...', 16, 1)` và tự động ROLLBACK an toàn.

### 2. Kịch bản Trigger toàn vẹn (trg_ChiTietLuong_KhongSuaKhiDaChot)
- **Tình huống:** Cố ý thực hiện lệnh `UPDATE CHITIETBANGLUONG SET ThucNhan = 99999999` trên kỳ lương đã có `TrangThai = 'DA_CHOT'`.
- **Kết quả:** Trigger `trg_ChiTietLuong_KhongSuaKhiDaChot` chặn ngay lập tức, thông báo lỗi: *"Không được phép sửa hoặc xóa chi tiết bảng lương đã chốt!"* và ROLLBACK giao dịch.

### 3. Kịch bản Tài khoản bị khóa (TaiKhoanPanel + LoginFrame)
- **Tình huống:** Quản trị viên (DB_Admin) vào màn hình `Quản trị tài khoản`, bấm nút "Khóa / Mở khóa" trên tài khoản `locked_user`. Khi người dùng thử đăng nhập tài khoản này tại `LoginFrame`, hệ thống hiển thị thông báo lỗi màu đỏ: *"Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên!"*.
