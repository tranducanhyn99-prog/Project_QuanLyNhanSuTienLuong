# TV5 — ĐỀ XUẤT CẬP NHẬT BÁO CÁO VÀ DECK CHO TRẦN ĐỨC ANH (BẢN FINAL V3)

- **Người gửi:** Phạm Minh Quân (TV2 — MSSV: 24110311)
- **Người nhận:** Trần Đức Anh (TV5 — Trưởng nhóm / Tổng hợp báo cáo & Slide)
- **Ngày lập:** 13/10/2026 (Hiệu chỉnh chuẩn hóa tối hậu — Remediation Pass)
- **Ghi chú phiên bản:** Bản v3 này thay thế chính thức bản v2, hiệu chỉnh chuẩn xác số liệu thời gian thực thi trích xuất từ các file `.sqlplan` và Messages log thô, tách bạch rõ ràng hai tỷ lệ phần trăm cắt giảm logical reads và sử dụng chuẩn thuật ngữ truy xuất bộ đệm buffer pool.

---

## 1. Cập nhật Bảng Benchmark Phân hệ Chấm công (§3.2 Báo cáo cuối kỳ)

Thay thế bảng số liệu cũ bằng bảng kết quả chính thức đã được xác thực trên database QA ủy quyền `PRJ_Fix_QA_20261013_001534_ae185d81` (Run ID: `run_20261013_140800`):

### Bảng đề xuất đưa vào Báo cáo Word (§3.2):

| STT | Kịch bản truy vấn | Baseline (`#CHAMCONG_BENCHMARK`) | Đo lường Covering Index | Logical Reads Before (Messages / XML) | Logical Reads After (Messages / XML) | Tỷ lệ giảm Logical Reads (Messages / XML) | Thời gian CPU / Elapsed (Messages `SET STATISTICS TIME`) | Thời gian CPU / Elapsed (XML Plan `QueryTimeStats`) |
|:---:|---|---|---|:---:|:---:|:---:|:---:|:---:|
| 1 | **Truy vấn chi tiết (1.1 ➔ 3.1):** Tra cứu công 9 tháng nhân viên 25 (273 bản ghi) | `UQ_Bench_MaNV_Ngay` (Index Seek + Key Lookup) | `IX_Bench_CHAMCONG_MaNV_Ngay` (Pure Index Seek, 0 Key Lookup) | **574 / 522** | **5 / 5** | **99.1289% / 99.0421%** | **15ms / 31ms ➔ 0ms / 2ms** | **0ms / 0ms ➔ 0ms / 0ms** |
| 2 | **Truy vấn tổng hợp (1.2 ➔ 3.2):** Tổng hợp công & giờ làm theo tháng (9 bản ghi) | `UQ_Bench_MaNV_Ngay` (Seek + Lookup + Stream Aggregate) | `IX_Bench_CHAMCONG_MaNV_Ngay` (Seek + Stream Aggregate, 0 Lookup) | **574 / 522** | **5 / 5** | **99.1289% / 99.0421%** | *Không xuất dòng kết thúc trong Messages* | **1ms / 1ms ➔ 0ms / 0ms** *(không tuyên bố tốc độ <1ms)* |

### Đoạn văn giải trình kỹ thuật đề xuất đưa vào Báo cáo:
> "Trong phân hệ Chấm công, cấu trúc nghiệp vụ yêu cầu kiểm soát một nhân viên chỉ có tối đa một bản ghi mỗi ngày, tương ứng ràng buộc duy nhất `UQ_CHAMCONG_MaNV_Ngay` trên `(MaNV, NgayChamCong)`. Ràng buộc này tạo ra một Non-clustered Index tìm kiếm theo khóa. Do đó, baseline chuẩn mực của hệ thống là so sánh giữa Index Seek có Key Lookup và Pure Index Seek không Key Lookup trên cùng tập dữ liệu chuẩn 30.000 bản ghi (mô phỏng cách ly trên bảng tạm `#CHAMCONG_BENCHMARK`).
>
> Kết quả thực nghiệm chính thức (Run ID `run_20261013_140800`):
> 1. Khi chưa có covering index, truy vấn chi tiết 273 ngày công buộc SQL Server phải thực hiện 273 lần Key Lookup về Clustered Index, tiêu tốn **574 logical reads** theo `SET STATISTICS IO` (tầng buffer pool) và **522 logical reads** theo bộ đếm per-operator runtime trong XML Execution Plan. Sự chênh lệch 52 trang giữa hai phương thức đo bắt nguồn từ các tầng đo lường khác nhau của SQL Server (Storage Engine buffer pool page reads vs Execution Engine operator runtime counters) và nguyên nhân chi tiết của 52 trang này là UNKNOWN (không đưa ra giả định chưa kiểm chứng về b-tree navigation).
> 2. Khi tạo covering index `IX_Bench_CHAMCONG_MaNV_Ngay` với mệnh đề `INCLUDE (GioVao, GioRa, TrangThai)`, toàn bộ dữ liệu cần thiết nằm trọn vẹn trên trang lá của index phụ. Kế hoạch loại bỏ hoàn toàn 273 lần Key Lookup. Số lượt đọc logic giảm mạnh còn đúng **5 trang** ở cả hai công cụ đo — đạt hiệu suất cắt giảm **99.1289% logical reads** (theo Messages `SET STATISTICS IO`) và **99.0421%** (theo XML Execution Plan). Đây là số liệu đo lường logical reads / giảm truy xuất bộ đệm buffer pool trên warm cache, không đưa ra tuyên bố về cắt giảm đọc đĩa vật lý.
> 3. Về thời gian thực thi: Truy vấn chi tiết ghi nhận thời gian chạy theo Messages giảm từ 31ms xuống 2ms (CPU 15ms xuống 0ms); trong khi bộ đếm `QueryTimeStats` của XML Plan ghi nhận 0ms CPU / 0ms Elapsed cho cả trước và sau do độ phân giải lấy mẫu. Truy vấn tổng hợp ghi nhận `QueryTimeStats` giảm từ 1ms/1ms xuống 0ms/0ms (Messages không xuất dòng kết thúc cho câu aggregate). Nhóm minh bạch không tuyên bố kết luận về độ tăng tốc ở cấp độ thời gian dưới 1ms.
> 4. Cảnh báo *'Null value is eliminated by an aggregate or other SET operation'* phát sinh từ phép toán `COUNT(CASE WHEN TrangThai IN (...) THEN 1 END)` không có mệnh đề `ELSE`, theo đúng chuẩn xử lý giá trị NULL của ANSI SQL."

---

## 2. Bổ sung Danh mục Hàm Phân công cho TV2 (Chương 2 Báo cáo cuối kỳ)

Để đảm bảo đáp ứng đầy đủ tiêu chí rubric "≥5 Function chia đều cho các thành viên trong nhóm", đề xuất TV5 bổ sung hàm sau vào phần danh mục đối tượng của TV2:

- **Tên hàm:** `dbo.fn_TongPhuCap(@MaNV INT, @Thang INT, @Nam INT)`
- **Loại hàm:** Scalar User-Defined Function (Hàm vô hướng)
- **Vị trí script:** `database/03_phucap_khautru_TV3.sql` (dòng 214–224)
- **Nghiệp vụ:** Tiếp nhận mã nhân viên, tháng, năm; tính tổng số tiền phụ cấp của nhân viên đó trong kỳ từ bảng `PHUCAPNHANVIEN` và trả về `ISNULL(SUM(SoTien), 0)`.
- **Liên kết phân hệ:** Được thủ tục tính lương tháng `sp_TinhBangLuongThang` (TV4) gọi qua dynamic SQL có kiểm tra `OBJECT_ID`.

---

## 3. Cập nhật Slide 4–6 trong Deck tổng thể của Nhóm (Trần ≤15 Slide)

1. **Slide 4 (Nghiệp vụ & Ràng buộc):**
   - Đảm bảo ghi rõ ràng buộc: `PK_CHAMCONG` (`PRIMARY KEY CLUSTERED`), `UQ_CHAMCONG_MaNV_Ngay` (`UNIQUE NONCLUSTERED`), `CHK_CHAMCONG_Ngay`, `CHK_CHAMCONG_TrangThai`.
   - Trạng thái công ghi mã nghiệp vụ: `CO_MAT, DI_TRE, VE_SOM, VANG` (kèm chú thích tiếng Việt: Có mặt, Đi trễ, Về sớm, Vắng).
   - Nêu rõ: Ràng buộc CSDL và Trigger bảo vệ theo các điều kiện toàn vẹn cụ thể đã định nghĩa; lệnh SQL trực tiếp hợp lệ vẫn thực thi bình thường; không thay thế hoàn toàn quyền quản trị của DBA cấp cao.
2. **Slide 5 (SP, Trigger, Giao dịch nhập lô):**
   - Nêu rõ Stored Procedure không mở transaction riêng để nhường quyền điều phối All-or-Nothing cho Java Service.
   - Nhắc đến 3 trigger: 2 trigger module 02 (`trg_ChamCong_KiemTraGio` - `TC-CC-07`, `trg_ChamCong_KiemTraNhanVien` - `TC-CC-06`) và trigger module 05 (chặn sửa khi kỳ lương đã chốt).
3. **Slide 6 (View, Covering Index, Nối sang Lương):**
   - Đưa trực tiếp số liệu benchmark: `574 ➔ 5 logical reads` (Giảm **99.1289%** theo Messages; từ `522 ➔ 5`, giảm **99.0421%** theo XML Plan).
   - Giải thích ngắn gọn: Loại bỏ hoàn toàn Key Lookup nhờ mệnh đề `INCLUDE (GioVao, GioRa, TrangThai)`.
   - Khẳng định ranh giới: Phân hệ Lương đếm trực tiếp bảng CHAMCONG trong transaction có khóa `HOLDLOCK`, không gọi `fn_TinhSoNgayCong` (hàm của TV1).
