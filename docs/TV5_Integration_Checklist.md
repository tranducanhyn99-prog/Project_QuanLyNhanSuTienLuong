# TV5 – Checklist Tích hợp (Integration Checklist)
# Quản lý Nhân sự và Tiền lương – Nhóm 06 – DBMS330284

**Tác giả:** Trần Đức Anh (TV5, MSSV 24110155)  
**Ngày:** 21/09/2026  
**Phiên bản:** 1.0

> Tài liệu này dành cho **toàn bộ thành viên nhóm** sử dụng khi tích hợp module của mình vào hệ thống chung ở Tuần 2 và Tuần 3.

---

## PHẦN 1 – Checklist trước khi mở Pull Request

Mỗi thành viên tự kiểm tra trước khi tạo PR vào `main`:

### 1.1 SQL Server

- [ ] Script SQL chạy được trên SQL Server sạch (không bị lỗi syntax)
- [ ] Không hardcode tên database — dùng `USE QuanLyNhanSuTienLuong;` hoặc tham chiếu qua tên đầy đủ
- [ ] Tên bảng/cột/SP/Function/View/Index đúng như đã thống nhất trong tài liệu thiết kế
- [ ] Có `GO` ngăn cách các batch lệnh
- [ ] Script chạy lại được (dùng `IF NOT EXISTS` hoặc `DROP IF EXISTS` trước `CREATE`)
- [ ] Không có password/token trong script SQL

### 1.2 Java Code

- [ ] Code build thành công (không có lỗi compile)
- [ ] Không có `System.exit()` ở tầng DAO/Service
- [ ] Không dùng `Statement` (phải dùng `PreparedStatement` hoặc `CallableStatement`)
- [ ] Tất cả `Connection`, `Statement`, `ResultSet` được đóng trong `try-with-resources` hoặc `finally`
- [ ] Không hardcode connection string — đọc từ `config.properties`
- [ ] Không commit `config.properties` (chỉ commit `config.properties.template`)
- [ ] Không có file `.class`, `.jar`, `*.bak` trong commit

### 1.3 Phân quyền

- [ ] DAO gọi đúng Login SQL Server phù hợp với role của nghiệp vụ
- [ ] Đã kiểm tra với cả role có quyền và role không có quyền
- [ ] Nếu gọi SP → đã kiểm tra `GRANT EXECUTE` cho role tương ứng

### 1.4 Commit & PR

- [ ] Commit message đúng quy ước (`feat:`, `fix:`, `db:`, `docs:`, `test:`, `refactor:`)
- [ ] PR mô tả rõ: chức năng gì, SQL object nào bị ảnh hưởng, cách đã kiểm thử
- [ ] Không tự merge PR của mình (chờ Code Owner review)

---

## PHẦN 2 – Checklist tích hợp DAO với SQL Server

Khi một thành viên viết DAO gọi SP/View của thành viên khác:

- [ ] Xác nhận tên SP/View/Function với tác giả (TV1–TV5)
- [ ] Kiểm tra tham số INPUT/OUTPUT của SP khớp với `CallableStatement`
- [ ] Test với dữ liệu hợp lệ → kết quả đúng
- [ ] Test với dữ liệu biên (null, rỗng, vượt range) → DAO bắt exception đúng
- [ ] Kiểm tra `ResultSet` mapping vào Java model đủ cột, đúng kiểu dữ liệu
- [ ] Không gọi `conn.close()` thủ công trong DAO nếu đang trong transaction từ Service

---

## PHẦN 3 – Checklist kiểm tra phân quyền

Khi cài đặt tính năng mới cần kiểm tra với tất cả 4 role:

| Bước | Hành động | Công cụ |
|---|---|---|
| 1 | Đăng nhập bằng tài khoản `DB_Admin` | Ứng dụng Java |
| 2 | Kiểm tra menu hiển thị đúng (tất cả menu) | Ứng dụng Java |
| 3 | Đăng nhập bằng `HR_Manager` | Ứng dụng Java |
| 4 | Kiểm tra menu lương bị ẩn | Ứng dụng Java |
| 5 | Thử gọi SP lương từ SSMS với login `HR_Manager` | SSMS |
| 6 | Đăng nhập bằng `Payroll_Officer` | Ứng dụng Java |
| 7 | Kiểm tra menu Nhân viên bị ẩn | Ứng dụng Java |
| 8 | Thử UPDATE NHANVIEN từ SSMS với login `Payroll_Officer` | SSMS |
| 9 | Đăng nhập bằng `Employee` | Ứng dụng Java |
| 10 | Kiểm tra chỉ thấy phiếu lương cá nhân | Ứng dụng Java |
| 11 | Thử SELECT CHAMCONG từ SSMS với login `Employee` | SSMS |

---

## PHẦN 4 – Checklist kiểm tra Transaction và Rollback

### 4.1 Quy tắc chung

- [ ] Mỗi transaction nghiệp vụ phải có `BEGIN TRANSACTION` / `COMMIT` / `ROLLBACK`
- [ ] Luôn có `TRY…CATCH` bọc toàn bộ transaction
- [ ] Trong `CATCH`: kiểm tra `@@TRANCOUNT > 0` trước khi `ROLLBACK`
- [ ] `RAISERROR` trong `CATCH` phải có message tiếng Việt rõ ràng

### 4.2 Kịch bản rollback theo module

| Module | Tình huống phải Rollback | Người kiểm tra |
|---|---|---|
| **Tạo nhân viên + tài khoản** (TV1) | Tạo nhân viên thành công nhưng tạo tài khoản thất bại | TV1 + TV5 |
| **Nhập chấm công theo lô** (TV2) | Một dòng trong lô bị lỗi (NV nghỉ việc, trùng ngày) | TV2 |
| **Xóa kỳ lương chưa chốt** (TV3) | Xóa chi tiết thất bại → rollback, không xóa kỳ | TV3 |
| **Tính bảng lương** (TV4) | Lỗi insert CHITIETBANGLUONG → rollback toàn bộ BANGLUONG | TV4 |
| **Chốt bảng lương** (TV5) | Kỳ đã chốt → rollback, thông báo lỗi | TV5 |

### 4.3 Cách kiểm tra rollback

```sql
-- Bước 1: Đếm số dòng trước khi chạy SP
SELECT COUNT(*) FROM BANGLUONG;         -- ví dụ: 3 dòng
SELECT COUNT(*) FROM CHITIETBANGLUONG;  -- ví dụ: 30 dòng

-- Bước 2: Chạy SP với dữ liệu khiến transaction thất bại giữa chừng
EXEC sp_TinhBangLuongThang @Thang=99, @Nam=2026;  -- Tháng 99 invalid

-- Bước 3: Đếm lại số dòng → phải giống Bước 1
SELECT COUNT(*) FROM BANGLUONG;         -- vẫn phải là 3 dòng
SELECT COUNT(*) FROM CHITIETBANGLUONG;  -- vẫn phải là 30 dòng
```

---

## PHẦN 5 – Checklist cuối tuần 2 (trước khi bắt đầu Tuần 3)

Toàn nhóm tự đánh giá trước khi chuyển sang giai đoạn test:

### 5.1 SQL Server (hoàn chỉnh)

- [ ] 9 bảng tạo xong với đầy đủ PK, FK, CHECK, DEFAULT, UNIQUE
- [ ] 5 Stored Procedure tạo xong và chạy được
- [ ] 5 User-Defined Function tạo xong và trả về kết quả đúng
- [ ] 5 Trigger tạo xong, đã test trigger firing
- [ ] 5 View tạo xong và trả về dữ liệu đúng
- [ ] 5 Index tạo xong
- [ ] 4 Login/User/Role tạo xong, GRANT/REVOKE/DENY áp dụng
- [ ] Dữ liệu mẫu đủ cho demo (ít nhất 5–10 nhân viên, 2–3 tháng chấm công)

### 5.2 Java (hoàn chỉnh)

- [ ] `LoginFrame` → đăng nhập đúng/sai hoạt động
- [ ] `MainFrame` → menu ẩn/hiện đúng theo role
- [ ] `NhanVienPanel` → CRUD cơ bản hoạt động (TV1)
- [ ] `ChamCongPanel` → nhập chấm công hoạt động (TV2)
- [ ] Module phụ cấp/khấu trừ hoạt động (TV3)
- [ ] `BangLuongPanel` → tính lương hoạt động (TV4)
- [ ] `BangLuongPanel` → chốt lương hoạt động (TV5)
- [ ] `BaoCaoPanel` → đọc được dữ liệu qua View (TV5)

### 5.3 Tích hợp

- [ ] Luồng đầy đủ chạy được: login → nhân viên → chấm công → tính lương → chốt → báo cáo
- [ ] Không có NullPointerException không xử lý khi thao tác bình thường
- [ ] Mất kết nối SQL Server → ứng dụng hiện thông báo, không crash

---

## PHẦN 6 – Người phụ trách và liên lạc

| Vấn đề | Liên hệ |
|---|---|
| Kiến trúc Java, Session, phân quyền | TV5 – Trần Đức Anh |
| Cấu trúc thư mục `src/`, quy ước code | TV5 – Trần Đức Anh |
| Naming convention bảng/cột, ERD | TV1 – Nguyễn Minh Trí |
| Git workflow, PR, merge | Code Owner – TV5 (tranducanhyn99-prog) |
| SP tính lương, chốt lương | TV4 (tính) + TV5 (chốt) |
| Dữ liệu mẫu | TV3 – Trần Tiến Đạt |
