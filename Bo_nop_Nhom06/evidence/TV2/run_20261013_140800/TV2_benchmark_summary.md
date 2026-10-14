# TV2 Benchmark Summary: Covering Index Hiệu Năng Trên CHAMCONG

- **Tác giả:** Phạm Minh Quân (TV2 — MSSV: 24110311)
- **Run ID:** `run_20261013_140800`
- **Thời gian chạy:** `2026-10-13T14:08:02+07:00`
- **Source Git:** Branch `main`, Commit HEAD `f2a41f7c9bfc0b911613ed4355ccc79f8281ddd5` (Clean worktree)
- **Script đo lường:** `database/test_benchmark_index_TV2.sql` (SHA-256: `ABA2B984309326A4947241CFC669AD0A08492101EFA6872493F79C02036A347E`)
- **SQL Server Endpoint:** `(local)` / `localhost` (`laptop-cua-mq`)
- **Cơ sở dữ liệu QA ủy quyền:** `PRJ_Fix_QA_20261013_001534_ae185d81` (Database ID: 33, Create Date: `2026-10-13 00:15:34.660`)
- **Tài khoản Windows Auth:** `LAPTOP-CUA-MQ\My Laptop`
- **Phiên làm việc (SPID):** `88` (Đồng nhất duy nhất một session xuyên suốt từ đầu tới cuối)
- **Phiên bản SQL Server:** Microsoft SQL Server 2025 (RTM) - 17.0.1000.7 (X64) Enterprise Developer Edition

---

## 1. Mục tiêu và Phương pháp luận

1. **Bản chất kỹ thuật của Baseline:**
   - Bảng sản xuất `dbo.CHAMCONG` có ràng buộc duy nhất `UQ_CHAMCONG_MaNV_Ngay (MaNV, NgayChamCong)`, tự động sinh Non-clustered Index tìm kiếm theo khóa.
   - So sánh Table Scan (bỏ index) là phi thực tế và gây hiểu nhầm về bản chất tối ưu của hệ CSDL.
   - Baseline thực tế là: **Index Seek trên (MaNV, NgayChamCong) + Key Lookup (Clustered Lookup)** về `PK_CHAMCONG` để đọc các cột `GioVao`, `GioRa`, `TrangThai`.
2. **Giải pháp Covering Index:**
   - Chỉ mục `IX_CHAMCONG_MaNV_Ngay` giữ khóa `(MaNV, NgayChamCong)` và bổ sung mệnh đề `INCLUDE (GioVao, GioRa, TrangThai)`.
   - Kết quả: Đưa toàn bộ cột cần đọc lên lá của chỉ mục phụ, loại bỏ hoàn toàn toán tử Key Lookup.
3. **Cách ly an toàn 100%:**
   - Dùng bảng tạm session-local `#CHAMCONG_BENCHMARK` trong `tempdb` (temp suffix `00000000000F`), nạp 30.000 dòng định chế (100 nhân viên × 300 ngày).
   - Tuyệt đối không can thiệp schema, dữ liệu persistent của `dbo.CHAMCONG`, và không flush cache hệ thống.

---

## 2. Kết quả đo lường thực nghiệm (Coherent Capture)

Toàn bộ 4 câu truy vấn được thực thi trong cùng 1 session, xuất ra 4 file actual execution plan (`.sqlplan`), 4 file CSV kết quả, và log thông điệp `STATISTICS IO/TIME`:

| STT | Câu truy vấn | Loại chỉ mục / Plan | Logical Reads (Messages) | Logical Reads (XML Operators) | CPU Time (Messages) | Elapsed Time (Messages) | Số dòng trả về |
|:---:|---|---|---:|---:|---:|---:|---:|
| **1.1** | BEFORE: Tra cứu chi tiết công nhân viên 25 (9 tháng) | `UQ_Bench_MaNV_Ngay` (Index Seek + Key Lookup) | **574** | **522** (2 Seek + 520 Lookup) | 15 ms | 31 ms | 273 |
| **3.1** | AFTER: Tra cứu chi tiết công nhân viên 25 (9 tháng) | `IX_Bench_CHAMCONG_MaNV_Ngay` (Pure Index Seek, 0 Lookup) | **5** | **5** (5 Seek, 0 Lookup) | 0 ms | 2 ms | 273 |
| **1.2** | BEFORE: Tổng hợp công & giờ làm theo tháng (9 tháng) | `UQ_Bench_MaNV_Ngay` (Seek + Lookup + Stream Aggregate) | **574** | **522** (2 Seek + 520 Lookup) | 0 ms | 0 ms | 9 |
| **3.2** | AFTER: Tổng hợp công & giờ làm theo tháng (9 tháng) | `IX_Bench_CHAMCONG_MaNV_Ngay` (Seek + Stream Aggregate, 0 Lookup) | **5** | **5** (5 Seek, 0 Lookup) | 0 ms | 0 ms | 9 |

### Tỷ lệ cắt giảm I/O (Logical Reads Reduction):
- **Truy vấn chi tiết (1.1 ➔ 3.1):**
  $$\text{Mức giảm I/O (Messages)} = \frac{574 - 5}{574} \times 100\% = \mathbf{99.1289\%}$$
  $$\text{Mức giảm I/O (XML Operators)} = \frac{522 - 5}{522} \times 100\% = \mathbf{99.0421\%}$$
- **Truy vấn tổng hợp (1.2 ➔ 3.2):**
  $$\text{Mức giảm I/O} = \frac{574 - 5}{574} \times 100\% = \mathbf{99.1289\%}$$

---

## 3. Giải trình độ chênh 574 vs 522 giữa Messages và XML Plan

Lượt chạy đồng nhất này đã giải thích triệt để nguyên nhân kỹ thuật của con số 574 vs 522 ghi nhận trong các tài liệu kiểm thử trước:
1. **Trong `SET STATISTICS IO` (Messages log):**
   SQL Server đếm tổng số trang 8KB được đọc qua Storage Engine Buffer Pool. Khi thực hiện 273 lần Key Lookup, ngoài việc đọc 520 trang lá chứa dữ liệu clustered, Storage Engine phải đọc thêm các trang dẫn đường (b-tree root/intermediate pages) của clustered index, nâng tổng số lần đọc logical lên **574 trang**.
2. **Trong XML Execution Plan (`RunTimeCountersPerThread`):**
   Các toán tử cây thực thi ghi nhận chính xác số trang lá dữ liệu được truy cập:
   - Toán tử `Index Seek` trên `UQ_Bench_MaNV_Ngay`: `ActualLogicalReads = 2`, `ActualRows = 273`, `ActualExecutions = 1`.
   - Toán tử `Clustered Index Seek` (Key Lookup) trên `PK_Bench_ChamCong`: `ActualLogicalReads = 520`, `ActualRows = 273`, `ActualExecutions = 273`.
   - Tổng cộng: $2 + 520 = \mathbf{522}$ trang.
3. **Khi có Covering Index (AFTER):**
   Toán tử `Index Seek` trên `IX_Bench_CHAMCONG_MaNV_Ngay` quét trực tiếp trên 5 trang lá của chỉ mục phụ. Không có Key Lookup, không có b-tree traverse phụ ➔ Cả **Messages** lẫn **XML Plan** đều khớp tuyệt đối ở con số **5 logical reads** ($5 - 5 = 0$).

---

## 4. Xác nhận Metadata Chỉ Mục Trên Bảng Thật `dbo.CHAMCONG`

Trích xuất từ `TV2_index_metadata.csv` (thực hiện tại Batch 7 trên `PRJ_Fix_QA_20261013_001534_ae185d81`):

| Tên chỉ mục | Loại chỉ mục | Duy nhất (is_unique) | Tên cột | Thứ tự khóa (key_ordinal) | Cột INCLUDE (is_included_column) |
|---|---|:---:|---|:---:|:---:|
| `IX_CHAMCONG_MaNV_Ngay` | NONCLUSTERED | 0 (False) | `MaNV` | 1 | 0 (Khóa) |
| `IX_CHAMCONG_MaNV_Ngay` | NONCLUSTERED | 0 (False) | `NgayChamCong` | 2 | 0 (Khóa) |
| `IX_CHAMCONG_MaNV_Ngay` | NONCLUSTERED | 0 (False) | `GioVao` | 0 | **1 (INCLUDE)** |
| `IX_CHAMCONG_MaNV_Ngay` | NONCLUSTERED | 0 (False) | `GioRa` | 0 | **1 (INCLUDE)** |
| `IX_CHAMCONG_MaNV_Ngay` | NONCLUSTERED | 0 (False) | `TrangThai` | 0 | **1 (INCLUDE)** |
| `UQ_CHAMCONG_MaNV_Ngay` | NONCLUSTERED | 1 (True) | `MaNV` | 1 | 0 (Khóa) |
| `UQ_CHAMCONG_MaNV_Ngay` | NONCLUSTERED | 1 (True) | `NgayChamCong` | 2 | 0 (Khóa) |

Xác nhận: Chỉ mục `IX_CHAMCONG_MaNV_Ngay` trên bảng thật có khóa `(MaNV, NgayChamCong)` và bao phủ đầy đủ 3 cột `(GioVao, GioRa, TrangThai)`.

---

## 5. An toàn Hệ thống và Dọn dẹp Tài nguyên

1. **Dọn dẹp cùng phiên (Same-session cleanup):**
   - Đã thực thi lệnh `DROP TABLE #CHAMCONG_BENCHMARK`.
   - Kiểm tra xác nhận: `OBJECT_ID('tempdb..#CHAMCONG_BENCHMARK') IS NULL` (PASS).
   - Kiểm tra trạng thái giao dịch: `@@TRANCOUNT = 0`, `XACT_STATE() = 0`.
2. **Kiểm tra dữ liệu persistent `dbo.CHAMCONG`:**
   - Số dòng trước và sau benchmark: `0 ➔ 0` (Bảo toàn 100%).
   - Checksum bảng trước và sau benchmark: `NULL ➔ NULL` (Bảo toàn 100%).
3. **Kiểm tra kho vật thể người dùng (User Object Inventory):**
   - 10 Bảng người dùng, 5 Hàm vô hướng, 14 Thủ tục lưu trữ, 9 Trigger, 6 View, 30 Ràng buộc CHECK, 19 Ràng buộc DEFAULT, 9 Khóa ngoại, 10 Khóa chính, 9 Ràng buộc UNIQUE. Toàn bộ nguyên vẹn, không có vật thể nào bị thêm/xóa/sửa.

---

## 6. Cảnh báo Kỹ thuật và Giới hạn Thực nghiệm

1. **Index Hint:** Kịch bản benchmark dùng cú pháp ép chỉ mục `WITH (INDEX(...))` để đo lường chính xác chi phí I/O giữa 2 cấu trúc chỉ mục cụ thể. Trong môi trường production không có hint, Query Optimizer của SQL Server sẽ tự động cân nhắc chi phí dựa trên cardinality và selectivity.
2. **Thời gian thực thi (Elapsed Time):** Thời gian CPU và Elapsed trong các câu truy vấn nhỏ (dưới 1ms đến vài chục ms) chịu ảnh hưởng của client timing, thread scheduling và bộ nhớ đệm (warm cache). Do đó, **Logical Reads** (số trang đĩa 8KB đọc từ bộ nhớ) là thước đo chuẩn xác và khách quan nhất về mặt kiến trúc hệ CSDL.
3. **Cảnh báo giá trị NULL:** Cảnh báo `Warning: Null value is eliminated by an aggregate or other SET operation` xuất hiện ở các câu truy vấn 1.2 và 3.2 là do hàm tổng hợp `SUM` bỏ qua các giá trị NULL khi tính toán tổng giờ làm của các ngày vắng mặt. Đây là hành vi T-SQL chuẩn mực và không ảnh hưởng đến tính đúng đắn của dữ liệu.
