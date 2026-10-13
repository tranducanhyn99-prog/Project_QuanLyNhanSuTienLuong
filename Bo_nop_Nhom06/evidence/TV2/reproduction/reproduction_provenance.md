# TV2 Benchmark Capture Harness & Reproduction Provenance

- **Thành viên phụ trách:** Phạm Minh Quân (TV2 — MSSV: 24110311)
- **Harness thực thi:** `run_tv2_benchmark_final.ps1`
- **Thời gian chạy chính thức:** `2026-10-13T14:08:02+07:00`
- **Môi trường:** SQL Server 2025 RTM (17.0.1000.7), Database QA `PRJ_Fix_QA_20261013_001534_ae185d81`

---

## 1. Thiết lập Kết nối & Cơ chế Xử lý Batch GO

- **Connection String:**
  `Server=(local);Database=PRJ_Fix_QA_20261013_001534_ae185d81;Integrated Security=True;TrustServerCertificate=True;`
  Kết nối sử dụng giao thức Shared Memory nội bộ của Windows, xác thực Windows Auth dưới tài khoản `LAPTOP-CUA-MQ\My Laptop`, tránh hoàn toàn lỗi Kerberos SSPI.
- **Duy trì phiên duy nhất (Single Connection across GO):**
  - Mở duy nhất 1 đối tượng `System.Data.SqlClient.SqlConnection` (SPID: `88`).
  - Toàn bộ nội dung tệp script gốc `database/test_benchmark_index_TV2.sql` được đọc nguyên vẹn và tách thành các batch theo biểu thức chính quy `(?m)^\s*GO\s*$`.
  - Có tổng cộng 11 batch (0 đến 10), được tuần tự thực thi trên cùng connection đó thông qua `SqlCommand`.
  - Bảng tạm `#CHAMCONG_BENCHMARK` trong `tempdb` được tạo ở Batch 2, nạp 30.000 dòng ở Batch 3, đo lường ở Batch 4 và Batch 6, truy vấn metadata ở Batch 7, và xóa ở Batch 8 trên cùng session.

---

## 2. Kỹ thuật Thu thập Actual Execution Plan (STATISTICS XML)

- **Cơ chế:** SQL Server hỗ trợ lệnh `SET STATISTICS XML ON;`. Khi bật cờ này, sau mỗi câu truy vấn SELECT, SQL Server sẽ trả về một result set bổ sung có tiêu đề cột là `Microsoft SQL Server 2005 XML Showplan`, chứa chuỗi XML đồ hình thực thi thực tế kèm các bộ đếm `RunTimeCountersPerThread` và `QueryTimeStats`.
- **Bọc Batch 4 (Truy vấn BEFORE):**
  - Thực thi: `SET STATISTICS XML ON;` + Nội dung Batch 4 + `SET STATISTICS XML OFF;`
  - Đọc tuần tự qua `SqlDataReader`:
    - Result set 1: Dữ liệu câu 1.1 ➔ Xuất ra `TV2_BEFORE_1_1.csv` (273 dòng)
    - Result set 2: ShowPlan XML câu 1.1 ➔ Xuất ra `TV2_BEFORE_1_1.sqlplan` (Seek trên `UQ_Bench_MaNV_Ngay` + Key Lookup trên `PK_Bench_ChamCong`)
    - Result set 3: Dữ liệu câu 1.2 ➔ Xuất ra `TV2_BEFORE_1_2.csv` (9 dòng)
    - Result set 4: ShowPlan XML câu 1.2 ➔ Xuất ra `TV2_BEFORE_1_2.sqlplan` (Seek + Lookup + Stream Aggregate)
- **Bọc Batch 6 (Truy vấn AFTER):**
  - Thực thi: `SET STATISTICS XML ON;` + Nội dung Batch 6 + `SET STATISTICS XML OFF;`
  - Đọc tuần tự qua `SqlDataReader`:
    - Result set 1: Dữ liệu câu 3.1 ➔ Xuất ra `TV2_AFTER_3_1.csv` (273 dòng)
    - Result set 2: ShowPlan XML câu 3.1 ➔ Xuất ra `TV2_AFTER_3_1.sqlplan` (Pure Index Seek trên `IX_Bench_CHAMCONG_MaNV_Ngay`, 0 Key Lookup)
    - Result set 3: Dữ liệu câu 3.2 ➔ Xuất ra `TV2_AFTER_3_2.csv` (9 dòng)
    - Result set 4: ShowPlan XML câu 3.2 ➔ Xuất ra `TV2_AFTER_3_2.sqlplan` (Seek + Stream Aggregate, 0 Key Lookup)

---

## 3. Thu thập Thông điệp Server (InfoMessage Stream)

- Bắt sự kiện `$conn.add_InfoMessage({ param($sender, $eventArgs) $messages.Add($eventArgs.Message) })`.
- Mọi thông báo từ `PRINT`, `SET STATISTICS IO ON` (quét trang đĩa), `SET STATISTICS TIME ON` (thời gian CPU/thực thi), cảnh báo `Warning: Null value is eliminated by an aggregate`, và xác nhận dọn dẹp bảng tạm đều được gom lại và ghi trực tiếp ra `TV2_benchmark_messages.log`.

---

## 4. Công Khai Các Lượt Chạy Thử Nghiệm Trước (Earlier Capture Disclosure)

Trong quá trình xây dựng bộ thu thập tự động hóa, đã có 2 lượt chạy trước đó và được ghi nhận minh bạch:
1. `build/evidence/TV2/run_20261013_135900/`:
   - Lượt chạy đầu tiên kiểm thử đọc multi-result-set.
   - Khi chạy Batch 7 (metadata), hàm `Read-AllResults` xử lý lặp khiến `TV2_index_metadata.csv` bị lưu 0 dòng.
   - Thư mục này bị hủy bỏ (superseded).
2. `build/evidence/TV2/run_20261013_140500/`:
   - Lượt chạy thứ hai sau khi sửa hàm đọc metadata.
   - Tuy nhiên hàm xuất CSV chung vẫn gặp lỗi ép kiểu `$true -eq [DBNull]::Value` trong PowerShell khiến các cột bit hiển thị NULL.
   - Thư mục này bị hủy bỏ (superseded).
3. `build/evidence/TV2/run_20261013_140800/` (**Lượt chạy chính thức được chấp nhận**):
   - Tách bạch hàm `Extract-BatchQueries` đọc tuần tự 4 result set xác định cho Batch 4 và Batch 6.
   - Batch 7 đọc đúng bảng metadata thật của `dbo.CHAMCONG`.
   - Một câu truy vấn có `CAST(... AS INT)` trên các cột bit được chạy tiếp nối trong cùng ngữ cảnh QA để xuất 7 dòng metadata chuẩn xác ra `TV2_index_metadata.csv`.
   - Toàn bộ 14 file minh chứng trong lượt này đạt tính toàn vẹn 100%.

---

## 5. Hướng Dẫn Tái Hiện (Reproduction Steps)

Để tái hiện lại quy trình đo lường trên máy cục bộ:
1. Đảm bảo SQL Server đang chạy và database QA `PRJ_Fix_QA_20261013_001534_ae185d81` tồn tại.
2. Mở PowerShell 5.1/7 và chạy trực tiếp:
   ```powershell
   & "Bo_nop_Nhom06/evidence/TV2/reproduction/run_tv2_benchmark_final.ps1"
   ```
3. Script sẽ tự động tạo thư mục đầu ra, thực thi toàn bộ 11 batch và xuất đầy đủ các tệp `.sqlplan`, `.csv`, `.log`.
