# TV2 Benchmark Summary v2: Covering Index Hiệu Năng Trên CHAMCONG (Bản Rà Soát Độc Lập)

- **Tác giả:** Phạm Minh Quân (TV2 — MSSV: 24110311)
- **Run ID:** `run_20261013_140800`
- **Thời gian chạy chính thức:** `2026-10-13T14:08:02+07:00`
- **Source Git:** Branch `main`, Commit HEAD `f2a41f7c9bfc0b911613ed4355ccc79f8281ddd5` (Clean worktree)
- **Script đo lường:** `database/test_benchmark_index_TV2.sql` (SHA-256: `ABA2B984309326A4947241CFC669AD0A08492101EFA6872493F79C02036A347E`)
- **SQL Server Endpoint:** `(local)` / `localhost` (`laptop-cua-mq`)
- **Cơ sở dữ liệu QA ủy quyền:** `PRJ_Fix_QA_20261013_001534_ae185d81` (Database ID: 33, Create Date: `2026-10-13 00:15:34.660`)
- **Tài khoản Windows Auth:** `LAPTOP-CUA-MQ\My Laptop`
- **Phiên làm việc (SPID):** `88` (Đồng nhất duy nhất một session xuyên suốt từ đầu tới cuối)
- **Phiên bản SQL Server:** Microsoft SQL Server 2025 (RTM) - 17.0.1000.7 (X64) Enterprise Developer Edition
- **Quan hệ phiên bản:** Bản v2 này thay thế và chuẩn hóa các nội dung chưa chặt chẽ trong bản v1 sau độc lập kiểm thử (Codex review 1). Tệp raw nguyên bản được giữ nguyên liên kết tương đối tới `../run_20261013_140800/`.

---

## 1. Bản Chất Kỹ Thuật & Môi Trường Đo Lường

1. **Phân biệt Index Đo lường thực tế vs Index Bảng sản xuất:**
   - **Chỉ mục đo lường thực tế:** Kịch bản benchmark chạy trên bảng tạm session-local `#CHAMCONG_BENCHMARK` trong `tempdb` (temp suffix `00000000000F`), gồm:
     * Baseline: `UQ_Bench_MaNV_Ngay` (Non-clustered Index Seek theo `MaNV, NgayChamCong`) + Clustered Key Lookup trên `PK_Bench_ChamCong`.
     * Covering Index: `IX_Bench_CHAMCONG_MaNV_Ngay` trên `(MaNV, NgayChamCong)` với `INCLUDE (GioVao, GioRa, TrangThai)`.
   - **Liên kết thiết kế sản xuất:** Kịch bản mô phỏng chính xác cấu trúc chỉ mục tương ứng trên bảng thật `dbo.CHAMCONG`: `UQ_CHAMCONG_MaNV_Ngay` và `IX_CHAMCONG_MaNV_Ngay`.
2. **Cách ly an toàn:**
   - Dùng 30.000 dòng định chế (100 nhân viên × 300 ngày) trong `tempdb`. Tuyệt đối không can thiệp bảng dữ liệu hay schema thật trong `dbo`.

---

## 2. Kết Quả Đo Lường Thực Nghiệm (Coherent Capture)

Toàn bộ 4 câu truy vấn được thực thi trong cùng 1 session, xuất ra 4 file actual execution plan (`.sqlplan`), 4 file CSV kết quả, và log thông điệp `STATISTICS IO/TIME`:

| STT | Câu truy vấn | Chỉ mục đo lường / Plan | Logical Reads (Messages) | Logical Reads (XML Operators) | CPU / Elapsed (Messages) | CPU / Elapsed (QueryTimeStats XML) | Số dòng trả về |
|:---:|---|---|---:|---:|---|---|---:|
| **1.1** | BEFORE: Tra cứu chi tiết công NV 25 (9 tháng) | `UQ_Bench_MaNV_Ngay` (Seek + Key Lookup) | **574** | **522** (2 Seek + 520 Lookup) | 15 ms / 31 ms | 0 ms / 0 ms | 273 |
| **3.1** | AFTER: Tra cứu chi tiết công NV 25 (9 tháng) | `IX_Bench_CHAMCONG_MaNV_Ngay` (Pure Seek, 0 Lookup) | **5** | **5** (5 Seek, 0 Lookup) | 0 ms / 2 ms | 0 ms / 0 ms | 273 |
| **1.2** | BEFORE: Tổng hợp công theo tháng NV 25 (9 tháng) | `UQ_Bench_MaNV_Ngay` (Seek + Lookup + Stream Agg) | **574** | **522** (2 Seek + 520 Lookup) | *Không có log kết thúc* | 1 ms / 1 ms | 9 |
| **3.2** | AFTER: Tổng hợp công theo tháng NV 25 (9 tháng) | `IX_Bench_CHAMCONG_MaNV_Ngay` (Seek + Stream Agg, 0 Lookup) | **5** | **5** (5 Seek, 0 Lookup) | *Không có log kết thúc* | 0 ms / 0 ms | 9 |

### Tỷ lệ cắt giảm I/O (Logical Reads Reduction):
- **Truy vấn chi tiết (1.1 ➔ 3.1):**
  $$\text{Mức giảm I/O (Messages)} = \frac{574 - 5}{574} \times 100\% = \mathbf{99.1289\%}$$
  $$\text{Mức giảm I/O (XML Operators)} = \frac{522 - 5}{522} \times 100\% = \mathbf{99.0421\%}$$
- **Truy vấn tổng hợp (1.2 ➔ 3.2):**
  $$\text{Mức giảm I/O (Messages)} = \frac{574 - 5}{574} \times 100\% = \mathbf{99.1289\%}$$
  $$\text{Mức giảm I/O (XML Operators)} = \frac{522 - 5}{522} \times 100\% = \mathbf{99.0421\%}$$

---

## 3. Ghi Nhận Minh Bạch Về Độ Chênh 574 vs 522 và Thời Gian Thực Thi

1. **Về độ chênh 574 (Messages) vs 522 (XML Operators):**
   - Cả hai con số đều được ghi nhận trực tiếp từ cùng một lượt chạy thực tế (SPID 88, temp suffix `00000000000F`).
   - `SET STATISTICS IO` (Messages) đo lường số lần truy cập trang bộ đệm ở tầng Storage Engine Buffer Pool.
   - ShowPlan XML `RunTimeCountersPerThread` đo lường số lượt đọc logic tại từng toán tử thực thi (Operator level: 2 đọc tại `Index Seek` + 520 đọc tại `Clustered Index Seek`).
   - Sự chênh lệch 52 trang (574 - 522) phản ánh sự khác biệt về cơ chế hạch toán giữa Storage Engine và Query Execution Engine khi thực hiện 273 lần Key Lookup. **Nguyên nhân chi tiết chính xác của độ chênh này là chưa được chứng minh trực tiếp (UNKNOWN)**, không được phỏng đoán là chỉ do trang lá hay trang dẫn đường b-tree.
   - Khi có Covering Index (AFTER), không còn Key Lookup, cả **Messages** lẫn **XML Plan** đều hội tụ chính xác ở con số **5 logical reads**.
2. **Về thời gian thực thi (Timing Attribution):**
   - Đối với truy vấn chi tiết: Messages ghi nhận thời gian giảm từ 15ms CPU / 31ms elapsed xuống còn 0ms CPU / 2ms elapsed.
   - Đối với truy vấn tổng hợp: Trong Messages log, thông điệp `SQL Server Execution Times: CPU time = 0 ms, elapsed time = 0 ms` xuất hiện *trước* khi câu lệnh tổng hợp hoàn tất; do đó thời gian thực thi cuối cùng của câu lệnh tổng hợp trong Messages log là **không có sẵn (Unavailable)**.
   - Thay vào đó, thời gian thực thi của câu lệnh tổng hợp được trích xuất độc lập từ thuộc tính `QueryTimeStats` của actual plan `.sqlplan`: BEFORE ghi nhận `CpuTime=1ms, ElapsedTime=1ms`; AFTER ghi nhận `CpuTime=0ms, ElapsedTime=0ms`. Không đưa ra kết luận khái quát về tốc độ xử lý chỉ từ một lượt chạy đơn lẻ.

---

## 4. Giải Trình Cảnh Báo NULL (Null Elimination Warning)

- Trong các câu lệnh 1.2 và 3.2, SQL Server phát cảnh báo: `Warning: Null value is eliminated by an aggregate or other SET operation.`
- **Nguyên nhân chính xác:** Câu truy vấn sử dụng 4 biểu thức `COUNT(CASE WHEN TrangThai ... THEN 1 END)`. Khi điều kiện `WHEN` không thỏa mãn, biểu thức `CASE` không có mệnh đề `ELSE` sẽ trả về `NULL`. Hàm `COUNT(expression)` tự động loại bỏ các giá trị `NULL` này và phát cảnh báo theo chuẩn ANSI SQL.
- Hàm `SUM(CASE ... ELSE 0 END)` không gây ra cảnh báo này do đã có `ELSE 0` xử lý triệt để giá trị NULL.

---

## 5. Metadata Chỉ Mục Trên Bảng Thật `dbo.CHAMCONG`

Trích xuất từ kết quả kiểm tra trực tiếp trên `PRJ_Fix_QA_20261013_001534_ae185d81` (xem `../schema-inspection/TV2_live_schema_inspection.log`):

- `IX_CHAMCONG_MaNV_Ngay`: Non-clustered, Non-unique. Khóa: `MaNV` (1), `NgayChamCong` (2). Bao phủ (`INCLUDE`): `GioVao`, `GioRa`, `TrangThai`.
- `UQ_CHAMCONG_MaNV_Ngay`: Non-clustered, Unique. Khóa: `MaNV` (1), `NgayChamCong` (2). Không có cột INCLUDE.
- `PK_CHAMCONG`: Clustered, Unique. Khóa: `MaChamCong` (1).

---

## 6. Dọn Dẹp Tài Nguyên & Giới Hạn Kiểm Tra An Toàn

1. **Dọn dẹp cùng phiên (Same-session cleanup):**
   - Bảng tạm `#CHAMCONG_BENCHMARK` đã được xóa sạch. Kiểm tra xác nhận: `OBJECT_ID('tempdb..#CHAMCONG_BENCHMARK') IS NULL` (PASS).
   - Biểu thức kiểm tra: `SELECT OBJECT_ID('tempdb..#CHAMCONG_BENCHMARK') AS TempTableObjectId, @@TRANCOUNT AS TranCount, XACT_STATE() AS XactState;`
   - Kết quả thô trong `TV2_cleanup_safety.log`:
     * `TranCount = 0` (Xác nhận không có transaction người dùng nào bị treo hay chưa commit).
     * `XactState = 1`: Giá trị 1 này xuất hiện do ngữ cảnh tự động của câu lệnh compound `SELECT` khi gọi hàm `OBJECT_ID(...)` tra cứu catalog `tempdb` ở mức statement-level read context. Chạy độc lập `SELECT XACT_STATE()` trên phiên rảnh trả về đúng `0`.
2. **Phạm vi kiểm tra an toàn CSDL (Safety Boundaries):**
   - Kiểm tra giới hạn (Bounded check): Bảng `dbo.CHAMCONG` có 0 bản ghi và checksum `NULL` trước và sau benchmark.
   - Số lượng các loại vật thể người dùng trong `PRJ_Fix_QA_20261013_001534_ae185d81` được giữ nguyên: 10 Table, 5 Function, 14 SP, 9 Trigger, 6 View, 30 CHECK, 19 DEFAULT, 9 FK, 10 PK, 9 UQ.
   - *Giới hạn:* Việc đếm số lượng vật thể không chứng minh toàn bộ định nghĩa schema của các bảng khác không đổi, và các CSDL khác trên instance SQL Server không nằm trong phạm vi truy cập/kiểm tra của benchmark này.
