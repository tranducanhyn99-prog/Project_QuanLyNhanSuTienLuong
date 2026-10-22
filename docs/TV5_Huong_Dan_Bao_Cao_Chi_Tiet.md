# SỔ TAY HƯỚNG DẪN BÁO CÁO & TRA CỨU ĐƯỜNG DẪN MÃ NGUỒN (TV5)
# ĐỀ TÀI: HỆ THỐNG QUẢN LÝ NHÂN SỰ VÀ TIỀN LƯƠNG (PeopleOS)
### Học phần: Hệ Quản Trị Cơ Sở Dữ Liệu (DBMS330284) — GVHD: TS. Phan Thị Thể
### Người thực hiện: TRẦN ĐỨC ANH — MSSV: 24110155 (Thành viên 5)

---

> [!IMPORTANT]
> **Mục đích tài liệu:** Giúp bạn tự tin báo cáo trước Giảng viên và Hội đồng phản biện. Mỗi chức năng đều ghi rõ **Đường dẫn tệp (File Path)**, **Dòng mã nguồn (Line Number)**, **Vị trí trên Giao diện (UI)**, **Slide trình chiếu tương ứng**, kèm theo **Kịch bản nói mẫu** và **Câu trả lời phản biện**.

---

# PHẦN 1. BẢNG TRA CỨU NHANH ĐƯỜNG DẪN TOÀN BỘ CHỨC NĂNG (CHEAT SHEET)

Khi Thầy/Cô hỏi: *"Chức năng này em viết ở đâu? Mở code lên cho Thầy/Cô xem!"*, hãy tra cứu ngay bảng sau:

| STT | Tên Chức năng / Đối tượng | Đường dẫn Tệp Mã Nguồn (Clickable) | Vị trí Dòng Code | Slide PPTX |
|:---:|---|---|:---:|:---:|
| **1** | **Giao diện PeopleOS & Quick Login** | [`src/main/java/com/ui/auth/LoginFrame.java`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/ui/auth/LoginFrame.java) | Dòng 145 – 240 | **Slide 2, 15** |
| **2** | **Bộ bảng màu, Font, Bo góc UI** | [`src/main/java/com/ui/theme/UITheme.java`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/ui/theme/UITheme.java) | Dòng 15 – 120 | **Slide 2** |
| **3** | **Tác vụ nền bất đồng bộ (SwingWorker)** | [`src/main/java/com/ui/theme/DatabaseTask.java`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/ui/theme/DatabaseTask.java) | Dòng 20 – 85 | **Slide 2** |
| **4** | **Băm mật khẩu PBKDF2 600.000 vòng** | [`src/main/java/com/util/PasswordUtil.java`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/util/PasswordUtil.java) | Dòng 11 – 40 | **Slide 15** |
| **5** | **Cấu hình JDBC SSL & Database** | [`src/main/java/com/config/DatabaseConnection.java`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/config/DatabaseConnection.java) | Dòng 18 – 25 | **Slide 2** |
| **6** | **Tạo 4 Database Roles & Phân quyền** | [`database/05_Security_Payroll_TV5.sql`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/05_Security_Payroll_TV5.sql) | Dòng 276 – 360 | **Slide 15** |
| **7** | **Bảo vệ dòng (Row-Level Security)** | [`database/05_Security_Payroll_TV5.sql`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/05_Security_Payroll_TV5.sql) | Dòng 523 – 535 | **Slide 15** |
| **8** | **Màn hình Quản trị Tài khoản (Java)** | [`src/main/java/com/ui/admin/TaiKhoanPanel.java`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/ui/admin/TaiKhoanPanel.java) | Dòng 50 – 290 | **Slide 15** |
| **9** | **Thủ tục Chốt lương (`sp_ChotBangLuong`)** | [`database/05_Security_Payroll_TV5.sql`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/05_Security_Payroll_TV5.sql) | Dòng 195 – 250 | **Slide 16** |
| **10** | **Thủ tục Hủy chốt (`sp_HuyChotBangLuong`)** | [`database/05_Security_Payroll_TV5.sql`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/05_Security_Payroll_TV5.sql) | Dòng 255 – 275 | **Slide 16** |
| **11** | **Gọi Stored Procedure từ DAO** | [`src/main/java/com/dao/BangLuongDAO.java`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/dao/BangLuongDAO.java) | Dòng 137 – 155 | **Slide 16** |
| **12** | **Nút Chốt & Mở lại bảng lương (Java)** | [`src/main/java/com/ui/baocao/BaoCaoPanel.java`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/ui/baocao/BaoCaoPanel.java) | Dòng 120 – 125, 375 – 420 | **Slide 16** |
| **13** | **Bộ 3 Trigger chặn sửa kỳ đã chốt** | [`database/05_Security_Payroll_TV5.sql`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/05_Security_Payroll_TV5.sql) | Dòng 380 – 450 | **Slide 16** |
| **14** | **Thực nghiệm Concurrency SSMS (2 Session)** | [`database/test_concurrency_demo_TV5.sql`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/test_concurrency_demo_TV5.sql) | Dòng 1 – 50 | **Slide 16, 17** |
| **15** | **Benchmark Index & Đo lường I/O** | [`database/test_benchmark_index_TV5.sql`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/test_benchmark_index_TV5.sql) | Dòng 20 – 55 | **Slide 17** |
| **16** | **Dữ liệu mẫu 20 nhân sự thực tế** | [`database/07_Real_Enterprise_Seed.sql`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/07_Real_Enterprise_Seed.sql) | Dòng 1 – 1500 | Toàn bộ Demo |
| **17** | **Bộ Test hồi quy bảo mật (104 tests)** | [`src/test/java/com/test/SecurityRegressionTest.java`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/test/java/com/test/SecurityRegressionTest.java) | Dòng 1 – 200 | **Slide 17** |
| **18** | **Bộ Test hồi quy giao diện (88 tests)** | [`src/test/java/com/test/GuiQaRegressionTest.java`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/test/java/com/test/GuiQaRegressionTest.java) | Dòng 1 – 180 | **Slide 17** |

---

# PHẦN 2. HƯỚNG DẪN CHI TIẾT TỪNG CHỨC NĂNG CỦA TV5

---

### Chức năng 1: Kiến trúc 3 tầng, Giao diện PeopleOS & Đăng nhập nhanh
- **Vị trí trên Slide:** **Slide 2** (Kiến trúc tổng thể) và **Slide 15** (Bảo mật).
- **Đường dẫn mã nguồn chính xác:**
  - Form Đăng nhập & Quick Login: [`src/main/java/com/ui/auth/LoginFrame.java#L145-L240`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/ui/auth/LoginFrame.java)
  - Hệ thống Design System & Bo góc: [`src/main/java/com/ui/theme/UITheme.java#L15-L120`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/ui/theme/UITheme.java)
  - Tác vụ bất đồng bộ SwingWorker: [`src/main/java/com/ui/theme/DatabaseTask.java#L20-L85`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/ui/theme/DatabaseTask.java)
  - Cấu hình JDBC Driver & SSL: [`src/main/java/com/config/DatabaseConnection.java#L18-L25`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/config/DatabaseConnection.java)
- **Thao tác Demo thực tế:**
  1. Mở màn hình đăng nhập.
  2. Chỉ vào thanh **"ĐĂNG NHẬP NHANH (DEMO)"** có 5 chip: `Admin`, `HR Manager`, `Kế toán`, `Nhân viên A (730k)`, `Nhân viên B (0đ)`.
  3. Bấm 1 lần: Tự động điền user/password.
  4. Nhấp đúp (Double-click): Tự động đăng nhập vào thẳng hệ thống trong 0.5s.
- **Kịch bản nói mẫu:**
  > *"Kính thưa Thầy/Cô, em là Trần Đức Anh, phụ trách phân hệ Kiến trúc, Bảo mật và Xử lý đồng thời. Về mặt kiến trúc, hệ thống tuân thủ mô hình 3 tầng chuẩn mực: Tầng UI sử dụng Java Swing Light Theme hiện đại, xử lý bất đồng bộ qua `SwingWorker` trong `DatabaseTask.java` để giao diện không bị giật lag khi truy vấn SQL nặng. Để phục vụ việc bảo vệ đồ án trực quan, em đã thiết kế thanh Đăng nhập nhanh cho 5 tài khoản tương ứng 4 Database Roles của hệ thống."*

---

### Chức năng 2: Xác thực 2 Lớp & Băm mật khẩu PBKDF2-HMAC-SHA256
- **Vị trí trên Slide:** **Slide 15** (Thẻ "Cơ chế Xác thực 2 Lớp").
- **Đường dẫn mã nguồn chính xác:**
  - Thuật toán băm PBKDF2: [`src/main/java/com/util/PasswordUtil.java#L11-L40`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/util/PasswordUtil.java)
  - Dịch vụ xác thực tài khoản: [`src/main/java/com/service/AuthService.java#L30-L75`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/service/AuthService.java)
  - Cấu trúc bảng tài khoản lưu Salt/Hash: [`database/05_Security_Payroll_TV5.sql#L270-L310`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/05_Security_Payroll_TV5.sql)
- **Điểm kỹ thuật mấu chốt để trả lời giảng viên:**
  - Thuật toán: `PBKDF2WithHmacSHA256` chuẩn NIST SP 800-132.
  - Số vòng lặp: **600.000 vòng** (`ITERATIONS = 600000`).
  - Muối (Salt): **16 bytes ngẫu nhiên** sinh bằng `SecureRandom`.
  - So sánh mật khẩu bằng `MessageDigest.isEqual()` để chống tấn công **Timing Attack**.
  - Xóa mảng ký tự `char[]` bằng `Arrays.fill(..., '\0')` ngay sau khi xác thực để chống soi trộm bộ nhớ RAM.
- **Kịch bản nói mẫu:**
  > *"Hệ thống áp dụng cơ chế bảo mật 2 lớp: Lớp 1 tại ứng dụng Java sử dụng thuật toán băm PBKDF2-HMAC-SHA256 với 600.000 vòng lặp và muối 16 bytes ngẫu nhiên, loại bỏ hoàn toàn nguy cơ tấn công Rainbow Table. Lớp 2 tại SQL Server kết nối bằng SQL Login riêng biệt được DBA ánh xạ qua `TAIKHOAN.SqlLogin`."*

---

### Chức năng 3: Phân quyền 4 Database Roles & Ma trận Phân quyền (RBAC)
- **Vị trí trên Slide:** **Slide 15** (Bảng Ma trận Phân quyền RBAC Table 5x5).
- **Đường dẫn mã nguồn chính xác:**
  - Lệnh tạo 4 Roles và phân quyền: [`database/05_Security_Payroll_TV5.sql#L276-L360`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/05_Security_Payroll_TV5.sql)
  - Màn hình Quản trị Tài khoản trên Java: [`src/main/java/com/ui/admin/TaiKhoanPanel.java#L50-L290`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/ui/admin/TaiKhoanPanel.java)
- **Ma trận quyền cần nhớ khi giảng viên hỏi:**
  - `role_DBAdmin`: Toàn quyền quản trị, mở lại kỳ lương (`sp_MoLaiKyLuong`).
  - `role_HRManager`: CRUD hồ sơ nhân viên (`NHANVIEN`) và chấm công (`CHAMCONG`); **BỊ TỪ CHỐI (DENY)** quyền tính hoặc chốt lương.
  - `role_PayrollOfficer`: CRUD phụ cấp/khấu trừ; EXECUTE `sp_TinhLuongThang` và `sp_ChotBangLuong`; **BỊ TỪ CHỐI (DENY)** quyền sửa nhân sự/chấm công.
  - `role_Employee`: **BỊ TỪ CHỐI (DENY)** toàn bộ bảng gốc; chỉ được xem qua View cá nhân.
- **Kịch bản nói mẫu:**
  > *"Để đảm bảo nguyên tắc phân định trách nhiệm (Separation of Duties), nhóm không dùng tài khoản 'sa' mà thiết lập 4 Database Roles chuyên trách. Khi đăng nhập vào vai trò nào, JDBC Connection chỉ sở hữu đúng quyền hạn của Role đó. Nếu một HR Manager cố tình can thiệp thủ tục chốt lương, SQL Server Engine sẽ từ chối ngay lập tức ở tầng cơ sở dữ liệu."*

---

### Chức năng 4: Bảo vệ Dữ liệu mức Dòng (Row-Level Security)
- **Vị trí trên Slide:** **Slide 15** (Card "Row-Level Security với ORIGINAL_LOGIN()").
- **Đường dẫn mã nguồn chính xác:**
  - View bảo vệ mức dòng: [`database/05_Security_Payroll_TV5.sql#L523-L535`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/05_Security_Payroll_TV5.sql)
  - Xử lý nạp dữ liệu trên Java: [`src/main/java/com/ui/baocao/BaoCaoPanel.java#L287`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/ui/baocao/BaoCaoPanel.java)
- **Thao tác Demo thực tế:**
  1. Nhấp đúp vào nút **Nhân viên A (730k)** $\rightarrow$ Mở menu **Báo cáo & Phiếu lương**.
  2. Chỉ thấy duy nhất 1 dòng của Nguyễn Tuấn Anh (Thực nhận 730.000đ).
  3. Đăng xuất $\rightarrow$ Nhấp đúp vào nút **Nhân viên B (0đ)** $\rightarrow$ Chỉ thấy đúng 1 dòng của Trần Thị Bảo Bình (Thực nhận 0đ).
- **Điểm kỹ thuật mấu chốt để trả lời giảng viên:**
  - Sử dụng hàm **`ORIGINAL_LOGIN()`** trong mệnh đề `WHERE` của View `vw_PhieuLuongCaNhan`.
  - **Vì sao dùng `ORIGINAL_LOGIN()` mà không dùng `SUSER_SNAME()`?** Vì `ORIGINAL_LOGIN()` luôn trả về SQL Login ban đầu tạo ra kết nối, không thể bị giả mạo hay leo thang đặc quyền kể cả khi ai đó gọi lệnh chuyển ngữ cảnh `EXECUTE AS`.
- **Kịch bản nói mẫu:**
  > *"Đối với nhân viên thông thường, nhóm áp dụng Row-Level Security qua View `vw_PhieuLuongCaNhan`. Nhờ điều kiện lọc `tk.SqlLogin = ORIGINAL_LOGIN()`, SQL Server tự động giới hạn chỉ trả về dòng phiếu lương của chính nhân viên đó. Nhân viên tuyệt đối không thể thấy mức lương của đồng nghiệp hay cấp quản lý."*

---

### Chức năng 5: Quy trình Chốt kỳ lương & Mở lại kỳ lương có điều kiện
- **Vị trí trên Slide:** **Slide 16** (Sơ đồ Vòng đời Chốt kỳ lương Workflow).
- **Đường dẫn mã nguồn chính xác:**
  - Thủ tục Chốt lương: [`database/05_Security_Payroll_TV5.sql#L195-L250`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/05_Security_Payroll_TV5.sql)
  - Thủ tục Mở lại kỳ lương: [`database/05_Security_Payroll_TV5.sql#L255-L275`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/05_Security_Payroll_TV5.sql)
  - Gọi Stored Procedure từ DAO: [`src/main/java/com/dao/BangLuongDAO.java#L137-L155`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/dao/BangLuongDAO.java)
  - Nút bấm và xử lý giao diện Java: [`src/main/java/com/ui/baocao/BaoCaoPanel.java#L120-L125, L375-L420`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/ui/baocao/BaoCaoPanel.java)
- **Thao tác Demo thực tế:**
  1. Đăng nhập bằng **Kế toán (`gui_payroll`)**.
  2. Mở màn hình **Báo cáo & Phiếu lương**, chọn Tháng **10/2026**.
  3. Bấm nút **"Chốt bảng lương"** $\rightarrow$ Xác nhận $\rightarrow$ Nhãn trạng thái đổi sang **"ĐÃ CHỐT SỔ"** màu xanh, hiển thị ngày giờ chốt.
- **Kịch bản nói mẫu:**
  > *"Quy trình chốt kỳ lương được kiểm soát chặt chẽ qua thủ tục `sp_ChotBangLuong`. Trong thủ tục này, em sử dụng gợi ý khóa `WITH (UPDLOCK, ROWLOCK)` để khóa độc quyền bản ghi bảng lương, kiểm tra nếu kỳ đã chốt thì cấm chốt lại, sau đó chuyển trạng thái sang `DA_CHOT` và lưu dấu mốc thời gian `NgayChot = GETDATE()`. Khi cần mở lại kỳ lương do sai sót, chỉ Admin hoặc Kế toán có lý do chính đáng mới được gọi `sp_HuyChotBangLuong`."*

---

### Chức năng 6: Bộ 3 Trigger bảo vệ kỳ lương đã chốt chống can thiệp
- **Vị trí trên Slide:** **Slide 16** (Khối "Kích hoạt 3 Triggers tự động chặn sửa").
- **Đường dẫn mã nguồn chính xác:**
  - Trigger Chấm công: [`database/05_Security_Payroll_TV5.sql#L380-L405`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/05_Security_Payroll_TV5.sql) (`trg_ChamCong_ChanSuaKyDaChot`)
  - Trigger Phụ cấp: [`database/05_Security_Payroll_TV5.sql#L410-L430`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/05_Security_Payroll_TV5.sql) (`trg_PhuCap_ChanSuaKyDaChot`)
  - Trigger Khấu trừ: [`database/05_Security_Payroll_TV5.sql#L435-L450`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/05_Security_Payroll_TV5.sql) (`trg_KhauTru_ChanSuaKyDaChot`)
- **Thao tác Demo thực tế:**
  1. Sau khi chốt kỳ Tháng 10/2026 ở Bước trên, Đăng xuất.
  2. Đăng nhập vào vai trò **HR Manager (`gui_hr`)**.
  3. Vào menu **Nhật ký Chấm công**, bấm **Ghi nhận chấm công** cho bất kỳ nhân viên nào vào ngày thuộc Tháng 10/2026 (ví dụ: 2026-10-15).
  4. Hệ thống lập tức bật hộp thoại lỗi đỏ: *"Kỳ lương Tháng 10/2026 đã được chốt sổ. Không thể thêm/sửa/xóa chấm công!"*
- **Kịch bản nói mẫu:**
  > *"Để ngăn chặn triệt để hành vi gian lận dữ liệu sau khi chốt sổ tài chính, em cài đặt 3 Triggers ở tầng SQL Server. Ngay khi phát hiện kỳ lương tương ứng đã `DA_CHOT`, Trigger lập tức ném lỗi và Rollback Transaction. Dù người dùng thao tác trên giao diện Java hay cố tình viết lệnh UPDATE/INSERT trực tiếp trong SSMS, dữ liệu của kỳ đã chốt vẫn được bảo vệ tuyệt đối."*

---

### Chức năng 7: Kiểm soát Xung đột Đồng thời (Concurrency Control)
- **Vị trí trên Slide:** **Slide 16** (Card "Xử lý Tranh chấp Đồng thời") và **Slide 17** (Ảnh chụp 2 Sessions SSMS).
- **Đường dẫn mã nguồn chính xác:**
  - Logic khóa UPDLOCK trong Stored Procedure: [`database/05_Security_Payroll_TV5.sql#L205-L215`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/05_Security_Payroll_TV5.sql)
  - Script kiểm thử Concurrency 2 Sessions SSMS: [`database/test_concurrency_demo_TV5.sql#L1-L50`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/test_concurrency_demo_TV5.sql)
  - Ảnh chụp minh chứng kiểm thử SSMS: [`Bo_nop_Nhom06/slides/preview/TV5_Slide_15.png`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/Bo_nop_Nhom06/slides/preview/TV5_Slide_15.png)
- **Hai kịch bản Concurrency đã giải quyết:**
  1. **Hai phiên cùng tính lương đồng thời:** Phiên B tự động chờ khóa của Phiên A nhả ra. Kết quả: duy nhất 1 bảng lương được tạo, 0 header trùng, 0 dòng chi tiết trùng.
  2. **Một phiên chốt lương đối đầu phiên sửa công:** Thao tác sửa công bị giữ lại (Lock Wait) và lập tức bị từ chối an toàn ngay khi phiên chốt hoàn tất Commit.
- **Kịch bản nói mẫu:**
  > *"Về kiểm soát đồng thời, em đã giải quyết 2 bài toán lớn: Thứ nhất, khi 2 phiên cùng tính lương cho một kỳ, nhờ gợi ý khóa `WITH (UPDLOCK, HOLDLOCK)`, phiên thứ hai sẽ chờ an toàn và không sinh ra dữ liệu trùng lặp. Thứ hai, khi chốt lương diễn ra đồng thời với sửa công, giao dịch chốt lương giữ khóa độc quyền, khiến thao tác sửa công bị trì hoãn và ném lỗi từ chối ngay khi kỳ chuyển sang Đã chốt."*

---

### Chức năng 8: Tối ưu hóa Chỉ mục & Kế hoạch Thực thi (Execution Plan)
- **Vị trí trên Slide:** **Slide 17** (Thẻ so sánh Before/After và 2 ảnh Execution Plan).
- **Đường dẫn mã nguồn chính xác:**
  - Script Benchmark 10.000 dòng: [`database/test_benchmark_index_TV5.sql#L20-L55`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/test_benchmark_index_TV5.sql)
  - Lệnh tạo Covering Index tối ưu:
    ```sql
    CREATE NONCLUSTERED INDEX IX_Bench_MaPB_MaCV
    ON dbo.#BenchmarkNhanVien (MaPB, MaCV)
    INCLUDE (HoTen, LuongCoBan, TrangThai);
    ```
- **Số liệu đối chiếu Before / After từ Execution Plan:**
  - **Trước tối ưu (Clustered Index Scan):**
    - Đọc tuần tự toàn bộ **10.000 dòng** (`Actual Rows Read = 10000`).
    - Số trang đĩa đọc: **142 Logical Reads**.
    - Chi phí I/O: `0.106829`.
    - Phải dùng `Predicate` để lọc bỏ 9.167 dòng sau khi đọc.
  - **Sau tối ưu (Index Seek Nonclustered với Covering Index):**
    - Định vị B-Tree, chỉ đọc đúng **833 dòng** thỏa mãn (`Actual Rows Read = 833`).
    - Số trang đĩa đọc: **15 Logical Reads** (**Giảm 89.7%**).
    - Chi phí I/O: `0.0109645` (**Giảm 89.7%**).
    - Chi phí CPU: Giảm **90.4%** (từ `0.0111` xuống `0.0010`).
    - **Hoàn toàn không có `Key Lookup`** nhờ mệnh đề `INCLUDE`.
- **Kịch bản nói mẫu:**
  > *"Thưa Thầy/Cô, về tối ưu chỉ mục, em đã thực nghiệm trên tập dữ liệu 10.000 dòng. Trước khi tối ưu, SQL Server phải Scan toàn bộ 10.000 dòng tốn 142 logical reads. Sau khi em thiết kế Covering Index `IX_Bench_MaPB_MaCV` bao phủ các cột cần hiển thị, hệ thống chuyển sang toán tử Index Seek, chỉ đọc đúng 833 dòng cần thiết, giảm tới 89.7% lượng I/O reads và triệt tiêu hoàn toàn toán tử Key Lookup."*

---

### Chức năng 9: Bộ Kiểm thử Tự động Toàn diện (257 Assertions Pass)
- **Vị trí trên Slide:** **Slide 17** (Card "Bộ Kiểm thử Tự động Toàn diện (257 Assertions PASS)").
- **Đường dẫn mã nguồn chính xác:**
  - Kiểm thử Phân quyền & RLS (104 tests): [`src/test/java/com/test/SecurityRegressionTest.java`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/test/java/com/test/SecurityRegressionTest.java)
  - Kiểm thử Giao diện PeopleOS (88 tests): [`src/test/java/com/test/GuiQaRegressionTest.java`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/test/java/com/test/GuiQaRegressionTest.java)
  - Kiểm thử Theme & Login Chip (65 tests): [`src/test/java/com/test/LightThemeRegressionTest.java`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/test/java/com/test/LightThemeRegressionTest.java) & [`LoginChipRegressionTest.java`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/test/java/com/test/LoginChipRegressionTest.java)
  - Script chạy tự động toàn bộ: [`run_tv5_verification.ps1`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/run_tv5_verification.ps1)
- **Kịch bản nói mẫu:**
  > *"Để đảm bảo tính đúng đắn và độ bền vững của mã nguồn, em đã xây dựng bộ kiểm thử tự động gồm 257 assertions đạt tỷ lệ 100% Pass. Trong đó có 104 test hồi quy bảo mật kiểm tra đủ 22 quyền cấp phép và bẫy lỗi 3 trường hợp bị từ chối; 88 test giao diện kiểm tra vòng đời ứng dụng và xử lý timeout mạng."*

---

# PHẦN 3. THỨ TỰ BẤM NÚT TRÊN PHẦN MỀM KHI LÊN DEMO (120 GIÂY)

Khi đến lượt bạn demo (hoặc khi nhóm demo đến phần bảo mật & chốt lương), hãy thao tác chuẩn xác theo 5 bước sau:

```
[BƯỚC 1] Khởi chạy ứng dụng:
         Chạy script: .\start_app.ps1
         --> Màn hình LoginFrame hiện lên với bảng màu phẳng Light Theme.

[BƯỚC 2] Demo Đăng nhập nhanh & Dashboard Admin:
         Nhấp đúp vào nút: "Admin"
         --> Tự động vào Dashboard quản trị hệ thống trong 0.5s.
         --> Mở "Quản trị Tài khoản" để chứng minh bảng phân quyền 4 Roles.

[BƯỚC 3] Demo Chốt kỳ lương an toàn:
         Đăng xuất --> Nhấp đúp vào nút: "Kế toán" (gui_payroll)
         --> Vào menu "Báo cáo & Chốt lương", chọn Tháng 10/2026.
         --> Bấm nút "Chốt bảng lương" --> Xác nhận.
         --> Trạng thái đổi thành "ĐÃ CHỐT SỔ" màu xanh.

[BƯỚC 4] Demo Bẫy lỗi Trigger khóa dữ liệu:
         Đăng xuất --> Nhấp đúp vào nút: "HR Manager" (gui_hr)
         --> Vào menu "Nhật ký Chấm công", thử thêm công ngày 2026-10-15 cho nhân viên.
         --> HỘP THOẠI LỖI BẬT LÊN: Trigger từ chối vì kỳ đã chốt!

[BƯỚC 5] Demo Row-Level Security:
         Đăng xuất --> Nhấp đúp vào nút: "Nhân viên A (730k)" (gui_a)
         --> Vào menu "Báo cáo & Phiếu lương".
         --> Chứng minh: Chỉ thấy duy nhất phiếu lương của mình (730.000đ), không thấy của ai khác!
```

---

# PHẦN 4. BỘ CÂU HỎI VẤN ĐÁP KÈM MINH CHỨNG SOURCE CODE

Dưới đây là 4 câu hỏi "tủ" mà giảng viên bộ môn Hệ Quản Trị CSDL (TS. Phan Thị Thể) rất hay hỏi khi thẩm định bài làm của TV5:

#### Câu hỏi 1: *"Cơ chế bảo mật mật khẩu của em có điểm gì khác biệt so với các đồ án thông thường?"*
- **Trả lời:**
  > *"Dạ thưa Cô, các đồ án thông thường hay lưu mật khẩu dạng bản rõ hoặc dùng hàm băm nhanh MD5/SHA-256. Những cách này rất dễ bị giải mã bằng GPU qua bảng Rainbow Table. Đồ án của em áp dụng tiêu chuẩn NIST SP 800-132: dùng thuật toán `PBKDF2WithHmacSHA256` với muối ngẫu nhiên 16 bytes và lặp lại 600.000 vòng tính toán. Ngoài ra, tại tầng Java em không dùng kiểu `String` mà dùng mảng `char[]` và xóa trắng bộ nhớ ngay sau khi xác thực để chống soi trộm dump RAM."*
- **Chỉ code:** Mở [`src/main/java/com/util/PasswordUtil.java`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/src/main/java/com/util/PasswordUtil.java), chỉ vào **dòng 11 – 38**.

---

#### Câu hỏi 2: *"View cá nhân của em bảo vệ mức dòng ra sao? Tại sao em chọn `ORIGINAL_LOGIN()`?"*
- **Trả lời:**
  > *"Dạ thưa Cô, em cài đặt Row-Level Security trong View `vw_PhieuLuongCaNhan`. Trong mệnh đề WHERE, em kiểm tra `tk.SqlLogin = ORIGINAL_LOGIN()`. Em chọn `ORIGINAL_LOGIN()` vì hàm này trả về chính xác SQL Login kết nối ban đầu từ client, không thể bị thay đổi hay giả mạo ngay cả khi trong phiên có lệnh `EXECUTE AS` chuyển ngữ cảnh. Nhờ đó, nhân viên kết nối bằng tài khoản nào thì chỉ xem được đúng dòng của nhân viên đó, bảo vệ tính riêng tư tuyệt đối."*
- **Chỉ code:** Mở [`database/05_Security_Payroll_TV5.sql`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/05_Security_Payroll_TV5.sql), chỉ vào **dòng 523 – 535**.

---

#### Câu hỏi 3: *"Tại sao trong Execution Plan sau tối ưu lại không xuất hiện toán tử Key Lookup?"*
- **Trả lời:**
  > *"Dạ thưa Cô, đó là vì em đã áp dụng kỹ thuật **Covering Index**. Khi tạo chỉ mục `IX_Bench_MaPB_MaCV`, ngoài 2 cột khóa tìm kiếm là `(MaPB, MaCV)`, em đưa toàn bộ các cột cần hiển thị trong câu lệnh SELECT (`HoTen, LuongCoBan, TrangThai`) vào mệnh đề `INCLUDE (...)`. Do đó, dữ liệu của các cột này nằm sẵn ở các trang lá của chỉ mục Nonclustered. Database Engine chỉ cần đọc ở chỉ mục là có đủ thông tin, không cần phải thực hiện Bookmark Lookup ngược về Clustered Index, giúp triệt tiêu hoàn toàn chi phí Key Lookup và giảm gần 90% thao tác đọc I/O."*
- **Chỉ code:** Mở [`database/test_benchmark_index_TV5.sql`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/test_benchmark_index_TV5.sql), chỉ vào **dòng 20 – 25**.

---

#### Câu hỏi 4: *"Stored Procedure chốt lương xử lý tranh chấp đồng thời như thế nào?"*
- **Trả lời:**
  > *"Dạ thưa Cô, trong `sp_ChotBangLuong`, em đặt câu lệnh `SELECT TrangThai FROM dbo.BANGLUONG WITH (UPDLOCK, ROWLOCK)`. Khóa cập nhật UPDLOCK đảm bảo rằng nếu có một phiên khác cũng đang cố gắng tính lương hoặc sửa dữ liệu của kỳ này, phiên đó sẽ bị chặn (Block) ở trạng thái chờ. Sau khi thủ tục kiểm tra hợp lệ, chuyển trạng thái sang `DA_CHOT` và Commit, 3 Trigger bảo vệ sẽ lập tức kích hoạt để từ chối bất kỳ hành vi sửa đổi dữ liệu gốc nào sau đó."*
- **Chỉ code:** Mở [`database/05_Security_Payroll_TV5.sql`](file:///c:/Users/DUCANHZZ/Downloads/HQTCSDL/PRJ/database/05_Security_Payroll_TV5.sql), chỉ vào **dòng 195 – 250**.

---

> [!TIP]
> **Lời khuyên khi bước lên bục báo cáo:** Bạn chỉ cần mở sẵn tệp này trên một tab màn hình hoặc in ra 2 mặt giấy. Mọi câu hỏi của Thầy/Cô đều đã có sẵn câu trả lời và số dòng code để chỉ ngay lập tức! Chúc bạn có một buổi bảo vệ đồ án thành công xuất sắc đạt điểm tối đa!
