# HƯỚNG DẪN SỬ DỤNG TRỢ LÝ GÕ CODE SQL TRONG SSMS

Bộ công cụ nằm tại thư mục `tools/ssms_assistant/` giúp tự động sinh và **gõ từng ký tự T-SQL như người thật** trực tiếp bên trong cửa sổ soạn thảo **SQL Server Management Studio (SSMS)**.

---

## 1. Cách khởi động trước buổi vấn đáp

### Cách 1: Chạy bằng file `.bat` (Nhanh nhất)
- Nhấp đúp (Double-click) vào file **`BAT_TRO_LY_SSMS.bat`** ở thư mục gốc của đồ án.
- Thu nhỏ (Minimize) cửa sổ đen xuống thanh Taskbar rồi mở **SSMS** lên sử dụng bình thường.

### Cách 2: Chạy bằng Terminal / PowerShell
```powershell
python tools\ssms_assistant\ssms_daemon.py
```

> **Lưu ý khi đổi máy tính khác:**
> Để dùng chế độ AI Gemini trên máy mới, chỉ cần tạo file `tools/ssms_assistant/api_key.txt` và dán mã Gemini API Key vào dòng đầu tiên (file này đã được ẩn trong `.gitignore` để bảo mật). Nếu không có mạng hoặc không có key, toàn bộ **18 nhóm kịch bản Offline** vẫn hoạt động 100%.

---

## 2. Bảng phím tắt & Chế độ "Giả bộ gõ phím" (`Fake-Typing Shield`)

> **Tính năng đặc biệt — Tự động khóa bàn phím vật lý trong lúc đang sinh code (`WH_KEYBOARD_LL`):**
> - Ngay khi bạn bấm **`F8`** / **`Alt + Q`** / **`Ctrl + Q`**, hệ thống tự động bật **Lá chắn bàn phím vật lý (`KEYBOARD_LOCKED`)**.
> - Trong suốt thời gian code đang tự động gõ ra trên màn hình SSMS, bạn có thể **đặt hai tay lên bàn phím và gõ bất kỳ phím nào thoải mái** (tạo tiếng lạch cạch và chuyển động ngón tay như thật) mà **không có bất kỳ ký tự thừa nào từ bàn phím thật lọt vào SSMS**!
> - Ngay khoảnh khắc dòng code cuối cùng gõ xong (hoặc khi bạn bấm **`Esc`**), bàn phím vật lý tự động mở khóa trở lại ngay lập tức.

| Phím tắt | Chức năng |
| :--- | :--- |
| **`F8`** *(hoặc **`Alt + Q`**)* | **Gợi ý thông minh**: Ưu tiên khớp kịch bản chuẩn trong đồ án, nếu câu hỏi lạ/có điều kiện riêng thì tự động gọi AI Gemini |
| **`Ctrl + Q`** | **Ép gọi thẳng AI Gemini**: Sinh code T-SQL tự do bằng AI cho dòng ghi chú hiện tại |
| **`Esc`** *(trong lúc đang gõ)* | **Dừng khẩn cấp**: Ngừng gõ ngay lập tức tại ký tự hiện tại |
| **`F9`** *(trong lúc đang gõ)* | **Tăng tốc (Turbo)**: Gõ lướt cực nhanh phần code còn lại nếu muốn xong sớm |

---

## 3. Cách gõ lệnh "Tàng hình" trong SSMS

Bạn **không cần** gõ ký tự `#` hay chữ `ai`. Chỉ cần gõ một dòng ghi chú SQL (`-- ...`) bình thường như đang ghi lại câu hỏi của giảng viên:

### Hai kiểu đặt con trỏ đều nhận:
1. **Kiểu 1 (Bấm ngay cuối dòng ghi chú)**:
   ```sql
   -- cấp quyền chốt lương cho hr|   <-- (Con trỏ ở cuối dòng, bấm F8 hoặc Ctrl+Q)
   ```
2. **Kiểu 2 (Tự bấm `Enter` xuống dòng mới rồi mới bấm phím tắt)**:
   ```sql
   -- cấp quyền chốt lương cho hr
   |                                <-- (Con trỏ ở dòng trống ngay dưới, bấm F8 hoặc Ctrl+Q)
   ```
   *(Công cụ tự động nhìn lên dòng trên để đọc câu hỏi rồi gõ code ngay tại dòng trống hiện tại).*

### Ba cách gọi AI Gemini kín đáo (không lộ chữ `ai`):
- **Cách A**: Gõ `-- câu hỏi bất kỳ` rồi bấm **`Ctrl + Q`**.
- **Cách B**: Đặt **dấu chấm `.`** hoặc **dấu hỏi `?`** ở cuối dòng ghi chú rồi bấm **`F8`**:
  ```sql
  -- tìm 3 nhân viên lương cao nhất.
  ```
- **Cách C**: Dùng **3 dấu gạch ngang `---`** ở đầu dòng rồi bấm **`F8`**:
  ```sql
  --- liệt kê các nhân viên đi trễ trong tháng 10 năm 2026
  ```

---

## 4. Danh sách từ khóa Offline chuẩn (Phản hồi tức thì, không cần mạng)

Khi dùng **`F8`** (hoặc `Alt + Q`), bạn có thể gõ tự nhiên các cụm từ dưới đây:

### A. Phân quyền động & Quản lý Login/Role (Hỏi xoáy đáp xoay)
| Bạn gõ trong SSMS (`-- ...`) | Code SQL tự động gõ ra |
| :--- | :--- |
| `-- cấp quyền chốt lương cho hr` | Gỡ `DENY`, cấp `GRANT EXECUTE ON dbo.sp_ChotBangLuong TO role_HRManager` và test bằng `EXECUTE AS LOGIN = 'gui_hr'` |
| `-- cấm kế toán sửa bảng nhân viên` | `DENY UPDATE ON dbo.NHANVIEN TO role_PayrollOfficer` kèm khối `TRY...CATCH` kiểm chứng |
| `-- thu hồi quyền select trên bangluong của nhân viên` | `REVOKE SELECT ON dbo.BANGLUONG FROM role_Employee` kèm kiểm chứng |
| `-- cấp quyền xem bảng tài khoản cho hr` | Gỡ `DENY` và `GRANT SELECT ON dbo.TAIKHOAN TO role_HRManager` |
| `-- tạo login test_gv và gán vào role hr` | `CREATE LOGIN [test_gv]` + `CREATE USER [test_gv]` + `ALTER ROLE role_HRManager ADD MEMBER [test_gv]` |
| `-- thêm user gui_a vào role kế toán` | `ALTER ROLE role_PayrollOfficer ADD MEMBER [gui_a]` |
| `-- gỡ gui_a khỏi role kế toán` | `ALTER ROLE role_PayrollOfficer DROP MEMBER [gui_a]` |
| `-- xem quyền của role hr` | Truy vấn `sys.database_permissions` JOIN `sys.database_principals` liệt kê ma trận quyền |

### B. 9 Nhóm kịch bản bảo vệ đồ án TV5
| Bạn gõ trong SSMS (`-- ...`) | Code SQL tự động gõ ra |
| :--- | :--- |
| `-- test quyền hr` *(hoặc `kế toán` / `nhân viên`)* | Chạy bộ test phân quyền RBAC (`EXECUTE AS LOGIN`, chặn lỗi `229`, `REVERT`) |
| `-- test rls` | Kiểm tra bảo mật cấp dòng `dbo.vw_PhieuLuongCaNhan` giữa `gui_a` (`1008`) và `gui_b` (`1009`) |
| `-- sửa view rls chỉ xem kỳ đã chốt` | `CREATE OR ALTER VIEW dbo.vw_PhieuLuongCaNhan` thêm điều kiện `AND bl.TrangThai = 'DA_CHOT'` |
| `-- chốt bảng lương` | Chốt kỳ lương `1009` (`sp_ChotBangLuong`), kiểm tra lỗi chặn chốt trùng (kỳ `1008`) |
| `-- mở lại kỳ lương` | Gọi `EXEC dbo.sp_HuyChotBangLuong @MaBangLuong = 1009;` |
| `-- test trigger` *(hoặc thêm: `chấm công` / `phụ cấp` / `khấu trừ` / `chi tiết` / `xóa nhân viên`)* | Kích hoạt các Trigger chống sửa dữ liệu sau khi chốt lương và chống xóa nhân viên đã có lương |
| `-- tắt trigger chấm công` *(hoặc `-- bật trigger chấm công`)* | `DISABLE TRIGGER` / `ENABLE TRIGGER` trên bảng tương ứng |
| `-- test hàm tính thực nhận` | Kiểm thử `dbo.fn_TinhThucNhan` ở trường hợp bình thường và trường hợp khấu trừ vượt lương (ra âm) |
| `-- sửa hàm thực nhận âm thì về 0` | `CREATE OR ALTER FUNCTION dbo.fn_TinhThucNhan` dùng `CASE WHEN ... < 0 THEN 0` |
| `-- tràn số 8115` | Demo lỗi `Arithmetic overflow error (Msg 8115)` khi dùng `DECIMAL(10,2)` so với `DECIMAL(18,2)` |
| `-- đo hiệu năng index` | Bật `SET STATISTICS IO, TIME ON` so sánh truy vấn lọc `NHANVIEN` theo `TrangThai, MaPB` |
| `-- benchmark 10000 dòng bảng tạm` | Sinh 10.000 dòng vào `#BenchmarkNhanVien` để đo chênh lệch `logical reads` trước và sau khi tạo Covering Index |
| `-- concurrency cửa sổ 1` | **Cửa sổ 1**: `BEGIN TRAN` giữ khóa `WITH (UPDLOCK, HOLDLOCK)` trên `MaBangLuong = 1009` |
| `-- concurrency cửa sổ 2` | **Cửa sổ 2**: `SET LOCK_TIMEOUT 3000` và gọi `sp_ChotBangLuong` để chứng minh bị chặn chờ khóa |
| `-- xem session nào đang block` | Truy vấn `sys.dm_exec_requests` và `sys.dm_tran_locks` tìm `blocking_session_id` |
| `-- test check tháng 13` *(hoặc `tuổi` / `cccd` / `lương âm` / `khóa ngoại`)* | Kiểm thử các ràng buộc toàn vẹn `CHECK`, `UNIQUE`, `FOREIGN KEY` |
| `-- metadata` | Truy vấn `sys.objects` thống kê toàn bộ đối tượng trong CSDL |
| `-- xem code sp_ChotBangLuong` *(hoặc tên SP/View/Trigger/Hàm bất kỳ)* | Chạy `EXEC sp_helptext` và `OBJECT_DEFINITION` để xem mã nguồn |

### C. Truy vấn thống kê & Lệnh cứu hộ khẩn cấp
| Bạn gõ trong SSMS (`-- ...`) | Code SQL tự động gõ ra |
| :--- | :--- |
| `-- thống kê quỹ lương theo phòng ban` | Truy vấn `GROUP BY` + `HAVING` trên `dbo.vw_BangLuongChiTiet` |
| `-- tìm nhân viên lương cao nhất` *(hoặc `thấp nhất`)* | Truy vấn `SELECT TOP (1) WITH TIES` trên `dbo.vw_BangLuongChiTiet` |
| `-- nhân viên chưa có tài khoản` | Truy vấn `LEFT JOIN ... WHERE tk.MaTK IS NULL` |
| `-- lịch sử lương` | Truy vấn đối chiếu lương snapshot và `dbo.LICHSULUONG` |
| `-- reset` *(hoặc `-- cứu hộ`)* | **Lệnh cứu hộ**: `REVERT`, `ROLLBACK`, khôi phục lại toàn bộ quyền `DENY` mặc định, mở khóa tài khoản `gui_%` và mở lại kỳ lương `1009` |

---

## 5. Dự phòng: Bộ 17 SSMS Code Snippets có sẵn trong SSMS

Ngay cả khi bạn không bật công cụ Python, trong SSMS của bạn đã được cài sẵn **17 mẫu Code Snippets (`01` -> `17`)**:
1. Trong cửa sổ truy vấn SSMS, bấm **`Ctrl + K`**, rồi bấm tiếp **`Ctrl + X`** (hoặc chuột phải chọn **Insert Snippet...**).
2. Chọn thư mục **`My Code Snippets`**.
3. Chọn mục cần dùng (từ `01` đến `17`) và nhấn **`Enter`**.
