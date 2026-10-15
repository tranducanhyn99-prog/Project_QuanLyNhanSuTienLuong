# TV2 — NGUỒN GỐC THỰC THI VÀ HƯỚNG DẪN TÁI LẬP AN TOÀN (PROVENANCE & REPRODUCTION V3)

- **Người lập:** Phạm Minh Quân (TV2 — MSSV: 24110311)
- **Ngày cập nhật:** 13/10/2026 (Bản chuẩn hóa tối hậu — Remediation Pass)
- **Database QA ủy quyền cố định:** `localhost` / `PRJ_Fix_QA_20261013_001534_ae185d81`
- **Mã nguồn SQL chuẩn:** `Bo_nop_Nhom06/evidence/TV2/run_20261013_140800/test_benchmark_index_TV2.sql` (SHA-256: `ABA2B984309326A4947241CFC669AD0A08492101EFA6872493F79C02036A347E`)
- **Script phòng vệ có guard:** `Bo_nop_Nhom06/evidence/TV2/reproduction/test_benchmark_index_TV2_guarded.sql`

---

## 1. Phương Pháp Luận Thu Thập Đã Được Xác Thực (Verified Methodology)

Lượt chạy chính thức `run_20261013_140800` đã được kiểm định độc lập với 4 actual execution plans, kết quả 273/9 dòng đồng nhất tuyệt đối, metadata chỉ mục thật và dọn dẹp an toàn. Phương pháp luận thu thập chuẩn xác bao gồm:

1. **Một kết nối duy nhất xuyên suốt các batch (`Single Connection across GO`):**
   - Phiên làm việc duy trì một connection ID (SPID) liên tục để bảo toàn vòng đời của bảng tạm `#CHAMCONG_BENCHMARK` (30.000 dòng) xuyên suốt từ khâu chuẩn bị, đo lường Baseline, tạo chỉ mục mô phỏng, đo lường Covering Index cho đến khi dọn dẹp.
2. **Khai thác native STATISTICS XML và kênh InfoMessage:**
   - Sử dụng lệnh native `SET STATISTICS XML ON` và `SET STATISTICS IO, TIME ON`.
   - Thu nhận trực tiếp luồng thông điệp máy chủ (InfoMessage stream) và xuất nội dung XML plan chứa đầy đủ `RunTimeCountersPerThread` cho từng toán tử.
3. **Trích xuất Metadata chỉ mục với phép ép kiểu chuẩn:**
   - Trích xuất metadata từ `sys.indexes`, `sys.index_columns`, `sys.columns` trên bảng `dbo.CHAMCONG` với lệnh `CAST(ic.is_included_column AS INT)` nhằm tránh lỗi định dạng dữ liệu kiểu Boolean/Bit.

---

## 2. Công Bố Minh Bạch Về Các Lần Thu Thập Trước

Trong quá trình thực nghiệm, hai thư mục chạy sơ bộ đã được tạo ra:
- `run_20261013_135900`: Đã hoàn tất thực thi, có đủ 4 file `.sqlplan`, 4 file kết quả `.csv` và log hoàn thành, nhưng file xuất metadata `TV2_index_metadata.csv` chỉ có 5 bytes (chưa trích xuất đủ siêu dữ liệu chỉ mục).
- `run_20261013_140500`: Đã hoàn tất thực thi, có đủ 4 file `.sqlplan`, 4 file kết quả `.csv` và log hoàn thành, nhưng file xuất metadata `TV2_index_metadata.csv` cũng chỉ có 5 bytes (chưa trích xuất đủ siêu dữ liệu chỉ mục).

*Xác nhận hiện trạng:* Cả hai thư mục trên đều là các **lượt thu thập siêu dữ liệu chưa hoàn chỉnh (incomplete metadata captures)** và được phân loại là **lịch sử đã bị thay thế (SUPERSEDED)** bởi lượt chạy hoàn chỉnh `run_20261013_140800`. Nhóm loại bỏ các suy đoán không có cơ sở kỹ thuật trước đây (như giả định "thiếu XML thực tế" hay "lỗi cú pháp GO") và ghi nhận đúng quan sát khách quan về sự thiếu hụt metadata. Đồng thời, nhóm không đưa ra tuyên bố chưa kiểm chứng rằng tham số `Server=(local)` chứng minh một giao thức mạng cụ thể hay "loại trừ hoàn toàn lỗi SSPI".

---

## 3. Trạng Thái Lưu Trữ Của Script Tự Động Cũ (`run_tv2_benchmark_final.ps1`)

File `reproduction/run_tv2_benchmark_final.ps1` là mã nguồn phục hồi từ ngữ cảnh thực thi ban đầu.
- **Phân loại:** **ARCHIVE REFERENCE ONLY — TUYỆT ĐỐI KHÔNG CHẠY LẠI**.
- **Lý do an toàn:** Script chứa giá trị mặc định `$RunId = "run_20261013_140800"` (nguy cơ ghi đè bằng chứng gốc đã kiểm định), cơ chế guard phụ thuộc vào tham số do người dùng nhập, và chứa đường dẫn tuyệt đối `D:\`. Script này được lưu lại thuần túy phục vụ truy vết lịch sử, không quảng bá hay khuyến nghị sử dụng.

---

## 4. Quy Trình Tái Lập Phòng Vệ Fail-Closed (Guarded Reproduction Workflow)

Để tái lập kết quả một cách độc lập mà không có bất kỳ rủi ro nào ghi đè dữ liệu hay chạy sai database, quy trình phòng vệ chuẩn mực được thiết kế như sau:

### Cơ Chế Guard Từng Batch (Fail-Closed Architecture)
Trong script phòng vệ [test_benchmark_index_TV2_guarded.sql](test_benchmark_index_TV2_guarded.sql), **100% các batch không rỗng** đều được đặt chốt chặn cưỡng bức ở đầu batch:
```sql
IF DB_NAME() <> N'PRJ_Fix_QA_20261013_001534_ae185d81'
    THROW 53410, N'Guard violation: Database context is not PRJ_Fix_QA_20261013_001534_ae185d81', 1;
```
Script tuyệt đối **không chứa lệnh `USE`** để ngăn chặn việc chuyển đổi ngữ cảnh ngầm định.

### Phương Án Khuyến Nghị: Thực Thi Fail-Fast Qua `sqlcmd`
Khi tái lập trong tương lai (không thực hiện trong phiên kiểm định tài liệu hiện tại), phương án chuẩn là sử dụng `sqlcmd` với cờ ngắt lỗi nghiêm ngặt:
```cmd
sqlcmd -S localhost -d PRJ_Fix_QA_20261013_001534_ae185d81 -E -C -b -V16 -i "Bo_nop_Nhom06\evidence\TV2\reproduction\test_benchmark_index_TV2_guarded.sql"
```
- `-b`: Bắt buộc `sqlcmd` thoát ngay lập tức khi gặp lỗi (fail-fast), đóng kết nối và **hủy toàn bộ các batch phía sau** nếu guard kích hoạt.
- `-V16`: Thiết lập mức độ nghiêm trọng tối thiểu để trả mã lỗi về hệ điều hành.
- `-d`: Khóa cứng database mục tiêu ngay từ lúc bắt đầu bắt tay kết nối.

### Lưu Ý Quan Trọng Khi Sử Dụng SSMS Thủ Công
Nếu người thẩm định mở file trong giao diện SSMS:
- **Cảnh báo SSMS:** Mặc định trong SSMS, nếu một batch gặp lỗi `THROW`, SSMS vẫn tiếp tục thực thi các batch `GO` tiếp theo trừ khi người dùng bấm nút Cancel.
- **Tác dụng của Guard từng batch:** Nhờ có câu lệnh `IF DB_NAME() <> ... THROW 53410` ở đầu **tất cả 10 batch**, ngay cả khi SSMS cố tình chạy tiếp batch sau thì batch đó cũng lập tức bị chặn đứng và ném lỗi, bảo đảm an toàn tuyệt đối ngay cả trong môi trường không tự động ngắt kết nối.

### Khối Lệnh Trích Xuất Metadata Có Guard
Truy vấn trích xuất metadata chỉ mục cũng được bảo vệ trong cùng luồng:
```sql
IF DB_NAME() <> N'PRJ_Fix_QA_20261013_001534_ae185d81'
    THROW 53410, N'Guard violation: Database context is not PRJ_Fix_QA_20261013_001534_ae185d81', 1;

SELECT 
    i.name AS index_name,
    c.name AS column_name,
    ic.key_ordinal,
    CAST(ic.is_included_column AS INT) AS is_included_column
FROM sys.indexes i
JOIN sys.index_columns ic ON i.object_id = ic.object_id AND i.index_id = ic.index_id
JOIN sys.columns c ON ic.object_id = c.object_id AND ic.column_id = c.column_id
WHERE i.object_id = OBJECT_ID('dbo.CHAMCONG')
ORDER BY i.name, ic.key_ordinal, ic.index_column_id;
```
