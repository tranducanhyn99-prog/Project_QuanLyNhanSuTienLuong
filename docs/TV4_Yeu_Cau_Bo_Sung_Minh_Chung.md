# BẢNG YÊU CẦU BỔ SUNG MINH CHỨNG & HÌNH ẢNH DÀNH CHO TV4
**Thành viên phụ trách:** Nguyễn Quang Vinh (TV4 - MSSV: 24110385)  
**Phân hệ:** Module Quản lý Bảng Lương, Tính Tiền Công & Thực Nhận, Transaction & Concurrency  
**Mục đích:** Bổ sung các hình ảnh chụp thực tế từ SQL Server Management Studio (SSMS) để ghép vào Báo cáo cuối kỳ môn HQTCSDL (Word/PDF 50-100 trang) và Slide thuyết trình bảo vệ đồ án.

---

## 📌 TỔNG HỢP DANH MỤC HÌNH ẢNH CẦN CHỤP

| STT | Tên file ảnh khuyến nghị | Môi trường | Mô tả hình ảnh | Mức độ ưu tiên |
|:---:|---|:---:|---|:---:|
| **1** | `TV4_Benchmark_ExecutionPlan.png` | SSMS | Cây Actual Execution Plan so sánh Clustered Scan vs Covering Seek | **BẮT BUỘC (P1)** |
| **2** | `TV4_Benchmark_StatisticsIO.png` | SSMS | Tab Messages hiển thị `logical reads 547` giảm xuống `4 reads` (giảm 99.27%) | **BẮT BUỘC (P1)** |
| **3** | `TV4_Concurrency_2Sessions.png` | SSMS | Chụp 2 cửa sổ SSMS song song: Session A giữ lock 20s, Session B bị block 20.586 ms | **RẤT QUAN TRỌNG (P1)** |
| **4** | `TV4_E2E_10Steps_Pass.png` | SSMS | Tab Messages chạy `TV4_Payroll_E2E.sql` hiển thị đủ 10 bước PASS | **BẮT BUỘC (P1)** |
| **5** | `TV4_Trigger_KhoaKyDaChot.png` | SSMS | Thông báo lỗi màu đỏ khi cố tình UPDATE/DELETE kỳ lương đã chốt (`DA_CHOT`) | **BẮT BUỘC (P1)** |

*(Lưu ý: Màn hình giao diện Java Swing `BangLuongPanel` đã có sẵn tại `screenshots/08_BangLuong_TinhLuong.png`).*

---

## 🛠️ HƯỚNG DẪN CÁC BƯỚC CHỤP ẢNH TỪ SCRIPT SẴN CÓ CỦA TV4

### 1. Chụp ảnh Benchmark Index Khấu trừ (Ảnh 1 & Ảnh 2)
1. Mở file có sẵn trong repo: `database/tests/TV4_Payroll_Benchmark.sql`.
2. Nhấn tổ hợp phím **`Ctrl + M`** để bật **Include Actual Execution Plan**.
3. Nhấn **`F5`** để thực thi script.
4. **Thao tác chụp:**
   * **Ảnh 1 (`TV4_Benchmark_ExecutionPlan.png`):** Chuyển sang tab **Execution Plan**. Chụp rõ so sánh giữa 2 cây thực thi: `Clustered Index Scan` (547 reads) vs `Index Seek` (4 reads trên `IX_KHAUTRU_MaNV_ThangNam`).
   * **Ảnh 2 (`TV4_Benchmark_StatisticsIO.png`):** Chuyển sang tab **Messages**. Chụp rõ dòng hiển thị `logical reads 547` và `logical reads 4`.

---

### 2. Chụp ảnh Concurrency 2 Sessions (Ảnh 3 - Điểm nhấn lớn nhất)
1. Mở 2 cửa sổ truy vấn (Query Editor) trong SSMS đặt cạnh nhau (Side-by-side):
   * Cửa sổ trái: Mở file `database/tests/TV4_Payroll_Concurrency_SessionA.sql`.
   * Cửa sổ phải: Mở file `database/tests/TV4_Payroll_Concurrency_SessionB.sql`.
2. Nhấn **`F5`** ở Session A trước. Khi thấy Session A in ra dòng:  
   `[A] SESSION A DA GIU LOCK trong 20 giay. CHAY SESSION B NGAY BAY GIO.`
3. Ngay lập tức nhấn **`F5`** ở Session B. Session B sẽ rơi vào trạng thái chờ (blocked) đúng khoảng 20 giây và in ra:  
   `[B] Hoan tat sau 20586 ms. Blocking da duoc chung minh.`
4. **Thao tác chụp (`TV4_Concurrency_2Sessions.png`):** Dùng Snipping Tool chụp bao quát cả 2 màn hình SSMS thể hiện rõ thông báo của Session A và Session B.

---

### 3. Chụp ảnh kết quả Kiểm thử E2E 10/10 PASS (Ảnh 4)
1. Mở file có sẵn: `database/tests/TV4_Payroll_E2E.sql`.
2. Nhấn **`F5`** để thực thi script.
3. **Thao tác chụp (`TV4_E2E_10Steps_Pass.png`):** Chụp màn hình tab **Messages** hiển thị kết quả kiểm thử từ Bước 1 đến Bước 10 đều đạt trạng thái `PASS`.

---

### 4. Chụp ảnh Trigger khóa kỳ lương đã chốt (Ảnh 5)
1. Thực hiện câu lệnh cố tình can thiệp vào kỳ lương đã chốt:
   ```sql
   USE QuanLyNhanSuTienLuong;
   GO
   -- Cố ý cập nhật kỳ lương đã chốt
   UPDATE dbo.BANGLUONG SET NgayCongChuan = 28 WHERE TrangThai = 'DA_CHOT';
   GO
   ```
2. **Thao tác chụp (`TV4_Trigger_KhoaKyDaChot.png`):** Chụp màn hình thông báo lỗi màu đỏ của trigger `trg_BangLuong_KhongSuaKhiDaChot` chặn lệnh UPDATE.

---

## 📁 NƠI LƯU ẢNH
Sau khi chụp xong, TV4 lưu toàn bộ các file `.png` trên vào thư mục:  
👉 **`screenshots/`**  
Sau đó tạo commit đẩy lên Git để Trưởng nhóm tổng hợp chèn vào Báo cáo Word và Slide thuyết trình chung của nhóm.
