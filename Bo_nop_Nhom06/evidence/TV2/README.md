# TV2 — GÓI MINH CHỨNG VÀ HỒ SƠ BÀN GIAO PHÂN HỆ CHẤM CÔNG (NHÓM 06)

- **Thành viên phụ trách:** Phạm Minh Quân (TV2 — MSSV: 24110311)
- **Học phần:** Hệ Quản Trị Cơ Sở Dữ Liệu (DBMS330284)
- **Source Git:** Branch `main`, Commit HEAD `f2a41f7c9bfc0b911613ed4355ccc79f8281ddd5`
- **Môi trường CSDL QA:** `localhost` / `PRJ_Fix_QA_20261013_001534_ae185d81`
- **Ngày hoàn tất:** 13/10/2026

---

## 1. Cấu Trúc Thư Mục Gói Bàn Giao (`Bo_nop_Nhom06/evidence/TV2/`)

```
Bo_nop_Nhom06/evidence/TV2/
├── README.md                                  # Tài liệu hướng dẫn & mục lục gói nộp (file này)
├── benchmark-run/ (run_20261013_140800/)      # Bằng chứng đo lường Covering Index chính thức
│   ├── TV2_BEFORE_1_1.sqlplan                 # Actual Plan: Chi tiết Before (Index Seek + Key Lookup)
│   ├── TV2_BEFORE_1_1.csv                     # Dữ liệu xuất chi tiết Before (273 dòng)
│   ├── TV2_BEFORE_1_2.sqlplan                 # Actual Plan: Tổng hợp Before (Seek + Lookup + Stream Agg)
│   ├── TV2_BEFORE_1_2.csv                     # Dữ liệu xuất tổng hợp Before (9 dòng)
│   ├── TV2_AFTER_3_1.sqlplan                  # Actual Plan: Chi tiết After (Pure Covering Seek)
│   ├── TV2_AFTER_3_1.csv                      # Dữ liệu xuất chi tiết After (273 dòng)
│   ├── TV2_AFTER_3_2.sqlplan                  # Actual Plan: Tổng hợp After (Covering Seek + Stream Agg)
│   ├── TV2_AFTER_3_2.csv                      # Dữ liệu xuất tổng hợp After (9 dòng)
│   ├── TV2_index_metadata.csv                 # Metadata chỉ mục bảng dbo.CHAMCONG thật (7 dòng)
│   ├── TV2_benchmark_messages.log             # Toàn bộ thông điệp STATISTICS IO/TIME & cleanup
│   ├── TV2_environment_schema.log             # Bản ghi định danh server, DB và an toàn trước chạy
│   ├── TV2_cleanup_safety.log                 # Xác nhận dọn dẹp tempdb cùng session & bảo toàn DB
│   ├── TV2_results_comparison.md              # Báo cáo đối chiếu dữ liệu 100% tương đương (0 diff)
│   ├── TV2_benchmark_summary.md               # Báo cáo tổng hợp đo lường & giải trình 574 vs 522
│   ├── evidence_manifest.csv                  # Bảng kê băm SHA-256 các file minh chứng
│   ├── TV5_Proposed_Corrections.md            # Đề xuất chỉnh sửa Word/Deck cho TV5
│   └── test_benchmark_index_TV2.sql           # Bản sao script SQL đã thực thi
├── presentation/                              # Bộ tài liệu thuyết trình chuẩn hóa
│   ├── TV2_Slides_4_6_Content.md              # Nội dung chi tiết Slide 4, 5, 6 cho TV5 ghép deck
│   ├── TV2_Speaking_Script.md                 # Kịch bản nói (02:00), demo (00:45) và 12 câu vấn đáp
│   ├── TV2_GUI_Demo_Checklist.md              # Checklist demo, cảnh báo QA, fallback SQL
│   ├── TV2_Documentation_Audit.md             # Rà soát 8 finding tài liệu vs source (A1-A8)
│   ├── TV2_Presentation_Handoff.md            # Hướng dẫn bàn giao tổng thể cho TV5
│   └── TV5_Proposed_Corrections.md            # Đề xuất cập nhật báo cáo Word & Slide nhóm
└── sql-verification/                          # Minh chứng kiểm thử chức năng tự động (Reused)
    ├── test_module_chamcong_TV2.log           # Log kiểm thử 21/21 PASS (0 FAIL)
    ├── test_module_chamcong_TV2.sql           # Snapshot script kiểm thử chức năng đã chạy
    ├── install_02_Module_ChamCong_TV2.sql     # Snapshot script cài đặt module 02 đã kiểm chứng
    └── schema_environment.log                 # Log môi trường schema QA lúc chạy kiểm thử
```

---

## 2. Tóm Tắt Kết Quả Đo Lường Hiệu Năng (Covering Index Benchmark)

- **Lượt chạy:** `run_20261013_140800` (Thực hiện trong 1 connection duy nhất, SPID: `88`).
- **Tập dữ liệu:** 30.000 bản ghi chấm công định chế cách ly trong bảng tạm `#CHAMCONG_BENCHMARK`.
- **Kết quả cắt giảm I/O:**
  - **Truy vấn chi tiết (1.1 ➔ 3.1):** Từ **574 logical reads xuống còn 5 logical reads** (Giảm **99.13%** theo Messages; từ 522 xuống 5 theo XML `RunTimeCountersPerThread`). CPU: 15ms ➔ 0ms, Elapsed: 31ms ➔ 2ms.
  - **Truy vấn tổng hợp (1.2 ➔ 3.2):** Từ **574 logical reads xuống còn 5 logical reads** (Giảm **99.13%**).
- **Giải trình độ chênh 574 vs 522:** 574 bao gồm các trang dẫn đường (b-tree navigation) qua 273 lần Key Lookup ở Storage Engine; 522 là số trang lá dữ liệu được đọc tại toán tử. Khi có covering index, cả hai phương thức đều khớp chính xác ở con số 5 trang đọc logic.
- **Tính toàn vẹn dữ liệu:** Bảng so sánh `TV2_results_comparison.md` xác nhận 100% tương đương dữ liệu (273 dòng chi tiết và 9 dòng tổng hợp trùng khớp từng giá trị, cột, số thập phân và hash SHA-256).

---

## 3. Minh Chứng Kiểm Thử Chức Năng (Reused Test Evidence)

- Sử dụng lại kết quả kiểm thử chức năng đã xác thực tại `build/sql-verification/20261013_001534_ae185d81/test_module_chamcong_TV2.log` (SHA-256: `B127A5ABB4FCE0B4FDA4A5C9F7D0C10BA430FD9F5C1823B73B09D10607439FF7`).
- **Tổng kết testcase:** **21/21 PASS (0 FAIL)**, bao gồm:
  - `TC-CC-08`: Giao dịch nhập lô commit thành công toàn bộ dòng hợp lệ.
  - `TC-CC-09`: Giao dịch nhập lô rollback trọn vẹn khi gặp lỗi giữa chừng (All-or-Nothing).
  - `TC-CC-03`, `TC-CC-04`: Trigger chặn nhân viên không ở trạng thái `DANG_LAM_VIEC`.
  - `TC-CC-05`, `TC-CC-06`: Trigger chặn giờ ra nhỏ hơn hoặc bằng giờ vào.
  - `TC-CC-14`: Dọn dẹp an toàn sau kiểm thử.

---

## 4. Bảng Kiểm Tra Hash Bàn Giao (SHA-256 Manifest)

| Đường dẫn tương đối | File | SHA-256 |
|---|---|---|
| `run_20261013_140800/` | `TV2_BEFORE_1_1.sqlplan` | `18D88026A0895B40A5B0AD0D7D6B7DAB62D1D7F9C5A4ACDAD2BD071D56412D14` |
| `run_20261013_140800/` | `TV2_BEFORE_1_1.csv` | `76B576D6B0B4E081A7A7B039993A0BDF40A53A755B49ECA63BBCB83B015F7E31` |
| `run_20261013_140800/` | `TV2_AFTER_3_1.sqlplan` | `F4710F232914EEE2832C3D28C0A2EAE3DB01B300637AB79BFD2DA86F2E6DA9C7` |
| `run_20261013_140800/` | `TV2_AFTER_3_1.csv` | `76B576D6B0B4E081A7A7B039993A0BDF40A53A755B49ECA63BBCB83B015F7E31` |
| `run_20261013_140800/` | `TV2_BEFORE_1_2.sqlplan` | `6DF4299E10BFB0CE8C9B05CFA7FAEDB9489BDBE37209065E44A0E1B30C54B532` |
| `run_20261013_140800/` | `TV2_BEFORE_1_2.csv` | `ADE24D339160184F3CB4D4DE23A1D748725075B624E1D21AFE5E569C2F67BB4F` |
| `run_20261013_140800/` | `TV2_AFTER_3_2.sqlplan` | `B254761434371C25A8A362BB5DE637D7A3D3E94523F7B968A8E61420BFBEC183` |
| `run_20261013_140800/` | `TV2_AFTER_3_2.csv` | `ADE24D339160184F3CB4D4DE23A1D748725075B624E1D21AFE5E569C2F67BB4F` |
| `run_20261013_140800/` | `TV2_index_metadata.csv` | `378227C78478CEFCE4F026AA68353213257AF8869BDB023DF8538E6C5648DE6D` |
| `run_20261013_140800/` | `TV2_benchmark_messages.log` | `26740D445DDD932F5596FC2CBAB3AC29CA78D62DFEC88B664746032D8B5C5F0C` |
| `sql-verification/` | `test_module_chamcong_TV2.log` | `B127A5ABB4FCE0B4FDA4A5C9F7D0C10BA430FD9F5C1823B73B09D10607439FF7` |

---

## 5. Hướng Dẫn Dành Cho TV5 (Trần Đức Anh)

- Vui lòng đọc kỹ file `presentation/TV5_Proposed_Corrections.md` để lấy trực tiếp các đoạn văn bản, bảng số liệu và giải trình kỹ thuật cập nhật vào Báo cáo cuối kỳ (file Word) và Slide chung của nhóm.
- Các thắc mắc về phân định ownership hàm/thủ tục xem tại `presentation/TV2_Documentation_Audit.md` và `presentation/TV2_Presentation_Handoff.md`.
