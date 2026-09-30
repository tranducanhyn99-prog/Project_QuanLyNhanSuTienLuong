# HƯỚNG DẪN KHỞI TẠO CƠ SỞ DỮ LIỆU
## Dự án: Hệ Thống Quản Lý Nhân Sự và Tiền Lương (Nhóm 06 – DBMS330284)

Thư mục `database/` chứa toàn bộ mã nguồn DDL, DML, Stored Procedure, Function, View, Trigger, Index và phân quyền bảo mật trên SQL Server.

---

## 1. Thứ tự thực thi Scripts CSDL

Để khởi tạo CSDL hoàn chỉnh mà không gặp lỗi ràng buộc khóa ngoại (Foreign Keys), **bắt buộc** phải thực thi các file SQL theo đúng thứ tự sau:

| Thứ tự | Tên File Script | Module & Người phụ trách | Nội dung chính |
|:---:|---|---|---|
| **01** | `01_Module_NhanSu_TV1.sql` | **TV1 – Nguyễn Minh Trí** | Tạo bảng `PHONGBAN`, `CHUCVU`, `NHANVIEN`, `TAIKHOAN`. SP `sp_ThemNhanVien`, Trigger `trg_NhanVien_KhongXoaKhiDaPhatSinhLuong`, Function `fn_TinhSoNgayCong`, View `vw_NhanVien_PhongBan_ChucVu`, Index `IX_NHANVIEN_HoTen`. |
| **02** | `02_Module_ChamCong_TV2.sql` | **TV2 – Phạm Minh Quân** | Tạo bảng `CHAMCONG`. SP `sp_GhiNhanChamCong`, Trigger `trg_ChamCong_KiemTraGio`, Trigger `trg_ChamCong_KiemTraNhanVien`, View `vw_TongHopChamCongThang`, Index `IX_CHAMCONG_MaNV_Ngay`. |
| **03** | `03_phucap_khautru_TV3.sql` | **TV3 – Trần Tiến Đạt** | Tạo bảng `PHUCAPNHANVIEN`, `KHAUTRUNHANVIEN`. View `vw_TongPhuCapThang`, Function `fn_TongKhauTru`, Trigger `trg_PhuCap_KhongSuaKhiDaChotLuong`, SP Transaction `sp_XoaKyLuongChuaChot`, Index `IX_PHUCAP_MaNV_ThangNam`. |
| **04** | `04_Module_TinhLuong_TV4.sql` | **TV4 – Nguyễn Quang Vinh** | Tạo bảng `BANGLUONG`, `CHITIETBANGLUONG`. SP `sp_TinhBangLuongThang`, Function `fn_TinhTienCong`, Trigger `trg_BangLuong_KhongSuaKhiDaChot`, View `vw_TongKhauTruThang`, Index `IX_KHAUTRU_MaNV_ThangNam`. |
| **05** | `05_Security_Payroll_TV5.sql` | **TV5 – Trần Đức Anh** | SP `sp_ChotBangLuong`, Function `fn_TinhThucNhan`, Trigger `trg_ChiTietLuong_KhongSuaKhiDaChot`, View `vw_BangLuongChiTiet`, Index `IX_NHANVIEN_MaPB_MaCV`. Tạo 4 Roles (`DB_Admin`, `HR_Manager`, `Payroll_Officer`, `Employee`), cấu hình GRANT/DENY, tạo Logins & Users kiểm thử. |

---

## 2. Hướng dẫn chạy trên SQL Server Management Studio (SSMS)

1. Mở SSMS và kết nối vào SQL Server instance của bạn (ví dụ: `localhost` hoặc `.` hoặc `SQLEXPRESS`).
2. Mở lần lượt từng file theo thứ tự từ `01_Module_NhanSu_TV1.sql` đến `05_Security_Payroll_TV5.sql`.
3. Nhấn **Execute (F5)** cho từng file và kiểm tra thông báo hoàn thành (Messages: *Commands completed successfully*).

---

## 3. Các Script kiểm thử chuyên đề & Thực nghiệm Rubric

Sau khi cài đặt xong CSDL, có thể chạy các kịch bản kiểm thử độc lập sau:

* **`test_benchmark_index_TV5.sql`**: Kịch bản đo lường hiệu năng Covering Index `IX_NHANVIEN_MaPB_MaCV` bằng `SET STATISTICS IO, TIME ON` và Execution Plan.
* **`test_security_roles_TV5.sql`**: Kịch bản kiểm thử phân quyền 4 Database Roles bằng cơ chế `EXECUTE AS USER` và kiểm tra lệnh DENY.
* **`test_concurrency_demo_TV5.sql`**: Kịch bản mô phỏng tranh chấp đồng thời (Concurrency) giữa 2 phiên làm việc khi cùng chốt một kỳ lương.
