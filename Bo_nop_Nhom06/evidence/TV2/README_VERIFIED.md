# TV2 — GÓI MINH CHỨNG VÀ BÀN GIAO ĐÃ ĐƯỢC KIỂM ĐỊNH ĐỘC LẬP (README VERIFIED V2)

- **Thành viên phụ trách:** Phạm Minh Quân (TV2 — MSSV: 24110311)
- **Phân hệ:** Module 02 — Chấm công (`dbo.CHAMCONG`, `dbo.sp_GhiNhanChamCong`, triggers, `dbo.vw_TongHopChamCongThang`)
- **Mã nguồn căn cứ:** Git commit `main` @ `f2a41f7c9bfc0b911613ed4355ccc79f8281ddd5` (Working tree clean)
- **Môi trường Database QA:** `localhost` / `PRJ_Fix_QA_20261013_001534_ae185d81`
- **Trạng thái gói bàn giao:** **VERIFIED (Audit Correction Loop 1 Completed)**
- **Thông báo phiên bản:** Toàn bộ các file tài liệu phiên bản v1 ban đầu trong thư mục này được giữ nguyên để bảo tồn tính lịch sử nhưng đã được **thay thế chính thức (SUPERSEDED)** bởi các tài liệu trong thư mục `reviewed_v2/` và `presentation/reviewed_v2/`.

---

## 1. Cấu Trúc Gói Minh Chứng Chuẩn Hóa

```text
Bo_nop_Nhom06/evidence/TV2/
├── README_VERIFIED.md                          # Tài liệu điều hướng trung tâm (File hiện tại)
├── manifest_verified.csv                       # Bảng kê mã hash SHA-256 toàn bộ thành phần gói
│
├── run_20261013_140800/                        # DỮ LIỆU ĐO BENCHMARK THÔ NGUYÊN BẢN (KHÔNG SỬA ĐỔI)
│   ├── TV2_BEFORE_1_1_Detail.sqlplan           # Plan thực tế Baseline truy vấn chi tiết (Seek + Lookup)
│   ├── TV2_BEFORE_1_2_Aggregate.sqlplan        # Plan thực tế Baseline truy vấn aggregate
│   ├── TV2_AFTER_3_1_Detail.sqlplan            # Plan thực tế Covering Index truy vấn chi tiết (Pure Seek)
│   ├── TV2_AFTER_3_2_Aggregate.sqlplan         # Plan thực tế Covering Index truy vấn aggregate
│   ├── TV2_BEFORE_1_1_Detail_Result.csv        # Dữ liệu xuất Baseline chi tiết (273 dòng)
│   ├── TV2_BEFORE_1_2_Aggregate_Result.csv     # Dữ liệu xuất Baseline tổng hợp (9 dòng)
│   ├── TV2_AFTER_3_1_Detail_Result.csv         # Dữ liệu xuất Covering Index chi tiết (273 dòng)
│   ├── TV2_AFTER_3_2_Aggregate_Result.csv      # Dữ liệu xuất Covering Index tổng hợp (9 dòng)
│   ├── TV2_benchmark_messages.log              # Log máy chủ: IO Reads (574 ➔ 5), Execution Times, Warnings
│   ├── TV2_cleanup_safety.log                  # Log an toàn: @@TRANCOUNT=0, kiểm tra DROP bảng tạm
│   ├── TV2_index_metadata.csv                  # Metadata chỉ mục bảng thật dbo.CHAMCONG
│   ├── TV2_environment_identity.log            # Thông tin định danh SQL Server, DB, user
│   ├── TV2_results_comparison.md               # Đối soát dữ liệu 100% đồng nhất giữa Before và After
│   └── evidence_manifest.csv                   # Manifest gốc của lượt chạy run_20261013_140800
│
├── reviewed_v2/                                # BÁO CÁO PHÂN TÍCH BENCHMARK ĐÃ HIỆU CHỈNH THEO AUDIT
│   └── TV2_benchmark_summary_v2.md             # Báo cáo phân tích sâu: I/O (574 vs 522), thời gian, XACT_STATE
│
├── schema-inspection/                          # TRÍCH XUẤT SCHEMA THỰC TẾ TRỰC TIẾP TỪ QA DB
│   └── TV2_live_schema_inspection.log          # Bảng CHAMCONG (cột, kiểu, defaults, keys, triggers) & View
│
├── reproduction/                               # KỊCH BẢN TÁI LẬP VÀ XÁC ĐỊNH NGUỒN GỐC (PROVENANCE)
│   ├── run_tv2_benchmark_final.ps1             # Script PowerShell thực thi kết nối và bắt InfoMessage
│   └── reproduction_provenance.md              # Thuyết minh kỹ thuật tái lập và công bố các lần chạy trước
│
├── sql-verification/                           # MINH CHỨNG KIỂM THỬ TỰ ĐỘNG TÁI SỬ DỤNG HỢP LỆ
│   ├── test_module_chamcong_TV2.log            # Test suite Chấm công đạt 21 PASS / 0 FAIL
│   ├── test_module_chamcong_TV2_executed.sql   # Script test đã thực thi
│   ├── schema_environment.log                  # Môi trường kiểm thử
│   └── install_02_Module_ChamCong_TV2.log      # Log cài đặt phân hệ 02
│
└── presentation/reviewed_v2/                   # TÀI LIỆU CHUYỂN GIAO THUYẾT TRÌNH V2 (ĐÃ AUDIT)
    ├── TV2_Slides_4_6_Content_v2.md            # Nội dung Slide 4, 5, 6 chuẩn hóa cho TV5 ghép deck
    ├── TV2_Speaking_Script_v2.md               # Kịch bản thuyết trình 02:00, demo 00:45, 12 câu vấn đáp
    ├── TV2_GUI_Demo_Checklist_v2.md            # Checklist demo giao diện, cảnh báo môi trường QA rỗng
    ├── TV2_Documentation_Audit_v2.md           # Rà soát 8 phát hiện A1–A8 và giải trình kỹ thuật
    ├── TV2_Presentation_Handoff_v2.md          # Biên bản chuyển giao kỹ thuật cho TV5
    └── TV5_Proposed_Corrections_v2.md          # Đề xuất sẵn bảng và văn bản để TV5 đưa vào Báo cáo Word
```

---

## 2. Bảng Tổng Hợp 9 Điểm Hiệu Chỉnh Theo Audit Độc Lập

| STT | Vấn đề phát hiện tại Audit | Giải trình kỹ thuật & Biện pháp khắc phục đã thực hiện | Căn cứ minh chứng |
|:---:|---|---|---|
| **1** | **`XACT_STATE() = 1` trong log an toàn** | Nguyên nhân gốc: Khi chạy khối lệnh ghép `SELECT OBJECT_ID('tempdb..#...'), XACT_STATE()`, hàm `OBJECT_ID(...)` truy vấn catalog `tempdb` dưới ngữ cảnh đọc, khiến `XACT_STATE()` trả về 1 (giao dịch đọc ở mức câu lệnh). Đã kiểm chứng: Câu lệnh độc lập `SELECT XACT_STATE()` trả về 0. `@@TRANCOUNT` luôn bằng 0 và bảng tạm đã bị DROP hoàn toàn. Không tuyên bố sai lệch `XACT_STATE=0`. | `reviewed_v2/TV2_benchmark_summary_v2.md` (§5), `run_20261013_140800/TV2_cleanup_safety.log` |
| **2** | **Độ chênh lệch I/O 574 vs 522** | Con số 574 đo từ `SET STATISTICS IO` (Messages) phản ánh tổng số trang đọc ở tầng Storage Engine buffer pool. Con số 522 ghi nhận từ bộ đếm per-operator runtime (`RunTimeCountersPerThread`) trong XML Execution Plan. Nguyên nhân cụ thể của 52 trang chênh lệch chưa có căn cứ kỹ thuật nội bộ của SQL Server để khẳng định. Nhóm minh bạch không đưa ra giả định chưa chứng minh về b-tree navigation. Điểm cốt lõi: Cả hai công cụ đều ghi nhận chính xác **5 logical reads** khi có covering index. | `reviewed_v2/TV2_benchmark_summary_v2.md` (§2), `run_20261013_140800/TV2_benchmark_messages.log` |
| **3** | **Thời gian thực thi của truy vấn tổng hợp** | Messages không xuất dòng thời gian kết thúc riêng cho câu lệnh aggregate (1.2 và 3.2). Nhóm tách bạch báo cáo thời gian từ XML Plan `QueryTimeStats`: Before ghi nhận CPU 1ms, Elapsed 1ms; After ghi nhận CPU 0ms, Elapsed 0ms. Nhóm minh bạch không tuyên bố độ tăng tốc thời gian ở độ phân giải dưới 1ms. | `reviewed_v2/TV2_benchmark_summary_v2.md` (§3), các file `.sqlplan` |
| **4** | **Nguồn gốc cảnh báo NULL elimination** | Cảnh báo *"Null value is eliminated by an aggregate or other SET operation"* phát sinh từ `COUNT(CASE WHEN TrangThai IN (...) THEN 1 END)` do mệnh đề `CASE` không có `ELSE` sẽ trả về `NULL` khi không khớp điều kiện (hàm `SUM` có `ELSE 0` nên không gây ra cảnh báo này). Đây là hành vi chuẩn mực của ANSI SQL. | `reviewed_v2/TV2_benchmark_summary_v2.md` (§4), `run_20261013_140800/TV2_benchmark_messages.log` |
| **5** | **Ranh giới an toàn và Kiểm chứng Schema** | Các kiểm tra số lượng object và 0 bản ghi trong `dbo.CHAMCONG` bảo đảm tính toàn vẹn của bảng mục tiêu. Đã xuất log kiểm chứng schema trực tiếp từ live QA (`TV2_live_schema_inspection.log`) xác thực toàn bộ cột, nullability, defaults, PK, FK, UQ, CHECK, 3 trigger và định nghĩa View. | `schema-inspection/TV2_live_schema_inspection.log` |
| **6** | **Harness thực thi & Công bố lịch sử chạy** | Đã lưu lại script thực thi gốc `reproduction/run_tv2_benchmark_final.ps1` cùng tài liệu thuyết minh `reproduction/reproduction_provenance.md` công bố đầy đủ môi trường, kết nối Windows Auth, cơ chế bắt InfoMessage và lịch sử các lần chạy sơ bộ trước đó. | `reproduction/run_tv2_benchmark_final.ps1`, `reproduction/reproduction_provenance.md` |
| **7** | **Chuẩn hóa mã Testcase** | Đã chuẩn hóa chính xác mã kiểm thử: `TC-CC-06` (Kiểm tra trạng thái nhân viên qua `trg_ChamCong_KiemTraNhanVien`), `TC-CC-07` (Kiểm tra giờ ra > giờ vào qua `trg_ChamCong_KiemTraGio`), `TC-CC-08` (Commit nhập lô thành công), `TC-CC-09` (Rollback nhập lô toàn vẹn khi lỗi). | `sql-verification/test_module_chamcong_TV2.log`, `presentation/reviewed_v2/` |
| **8** | **Tên chỉ mục thực nghiệm đo lường** | Nêu rõ thực nghiệm đo lường trên các chỉ mục của bảng tạm session-local: `UQ_Bench_MaNV_Ngay` (Baseline) và `IX_Bench_CHAMCONG_MaNV_Ngay` (Covering Index) trên `#CHAMCONG_BENCHMARK`, mô phỏng cấu trúc persistent tương ứng `UQ_CHAMCONG_MaNV_Ngay` và `IX_CHAMCONG_MaNV_Ngay` trên `dbo.CHAMCONG`. | `reviewed_v2/TV2_benchmark_summary_v2.md` (§1), `presentation/reviewed_v2/` |
| **9** | **Tình trạng chuẩn bị thuyết trình & Demo** | Xác nhận rõ: Database QA ủy quyền hiện hành có 0 bản ghi công, demo live trên GUI chưa được kiểm chứng thực tế. Checklist demo là kịch bản chuẩn bị sẵn (dry-run); minh chứng cốt lõi là log tự động 21/21 PASS và 4 file `.sqlplan`. Markdown slides và speaker notes đã sẵn sàng; việc ghép slide PPTX toàn nhóm và tổng duyệt do TV5 chủ trì. | `presentation/reviewed_v2/TV2_GUI_Demo_Checklist_v2.md`, `TV2_Presentation_Handoff_v2.md` |

---

## 3. Hướng Dẫn Truy Cập và Tái Lập (Quick Navigation)

1. **Xem phân tích benchmark chính thức:** Mở `reviewed_v2/TV2_benchmark_summary_v2.md`.
2. **Xem số liệu đưa vào Slide & Báo cáo:** Mở `presentation/reviewed_v2/TV5_Proposed_Corrections_v2.md` và `presentation/reviewed_v2/TV2_Slides_4_6_Content_v2.md`.
3. **Xem schema thực tế live QA:** Mở `schema-inspection/TV2_live_schema_inspection.log`.
4. **Kiểm tra tính toàn vẹn gói tài liệu:** Kiểm tra mã hash tại `manifest_verified.csv`.
