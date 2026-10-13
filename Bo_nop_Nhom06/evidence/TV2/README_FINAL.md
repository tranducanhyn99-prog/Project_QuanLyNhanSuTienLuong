# TV2 — GÓI MINH CHỨNG VÀ BÀN GIAO CHÍNH THỨC TOÀN DIỆN (README FINAL V3)

- **Thành viên phụ trách:** Phạm Minh Quân (TV2 — MSSV: 24110311)
- **Phân hệ:** Module 02 — Chấm công (`dbo.CHAMCONG`, `dbo.sp_GhiNhanChamCong`, triggers, `dbo.vw_TongHopChamCongThang`)
- **Mã nguồn căn cứ:** Git commit `main` @ `f2a41f7c9bfc0b911613ed4355ccc79f8281ddd5` (Working tree clean)
- **Database QA ủy quyền cố định:** `localhost` / `PRJ_Fix_QA_20261013_001534_ae185d81`
- **Trạng thái tài liệu:** **FINAL CANONICAL PACKAGE (Final Remediation Pass Completed)**
- **Chính sách phân loại & Bảo toàn:** File này là tài liệu điều hướng trung tâm cao nhất. Chỉ đúng **12 file do máy chủ ghi nhận** trong thư mục `run_20261013_140800/` là bằng chứng thô nguyên bản (Final Raw Evidence). Các báo cáo cũ trong cùng thư mục là tài liệu phái sinh lịch sử đã bị thay thế (Superseded Derived Reports). Mọi đường dẫn trong tài liệu này đều là đường dẫn tương đối có thật và đã được kiểm định tồn tại trên đĩa.

---

## 1. Cấu Trúc và Phân Loại Thành Phần Gói Minh Chứng

### Nhóm 1: Bằng Chứng Thô Gốc Được Máy Chủ Ghi Nhận (Final Raw Evidence — 12 Files Duy Nhất)
*Thư mục lưu trữ: `run_20261013_140800/` — Giữ nguyên 100% byte-for-byte, không chỉnh sửa.*

- [TV2_BEFORE_1_1.sqlplan](run_20261013_140800/TV2_BEFORE_1_1.sqlplan): Actual Execution Plan của truy vấn chi tiết Baseline (Index Seek + 273 Key Lookup).
- [TV2_BEFORE_1_1.csv](run_20261013_140800/TV2_BEFORE_1_1.csv): Dữ liệu xuất thô của truy vấn chi tiết Baseline (273 dòng).
- [TV2_BEFORE_1_2.sqlplan](run_20261013_140800/TV2_BEFORE_1_2.sqlplan): Actual Execution Plan của truy vấn aggregate Baseline (Seek + Lookup + Stream Aggregate).
- [TV2_BEFORE_1_2.csv](run_20261013_140800/TV2_BEFORE_1_2.csv): Dữ liệu xuất thô của truy vấn aggregate Baseline (9 dòng).
- [TV2_AFTER_3_1.sqlplan](run_20261013_140800/TV2_AFTER_3_1.sqlplan): Actual Execution Plan của truy vấn chi tiết Covering Index (Pure Index Seek, 0 Key Lookup).
- [TV2_AFTER_3_1.csv](run_20261013_140800/TV2_AFTER_3_1.csv): Dữ liệu xuất thô của truy vấn chi tiết Covering Index (273 dòng).
- [TV2_AFTER_3_2.sqlplan](run_20261013_140800/TV2_AFTER_3_2.sqlplan): Actual Execution Plan của truy vấn aggregate Covering Index (Pure Index Seek + Stream Aggregate, 0 Key Lookup).
- [TV2_AFTER_3_2.csv](run_20261013_140800/TV2_AFTER_3_2.csv): Dữ liệu xuất thô của truy vấn aggregate Covering Index (9 dòng).
- [TV2_benchmark_messages.log](run_20261013_140800/TV2_benchmark_messages.log): Log tin nhắn máy chủ ghi nhận `SET STATISTICS IO` (574 ➔ 5 reads) và `SET STATISTICS TIME`.
- [TV2_cleanup_safety.log](run_20261013_140800/TV2_cleanup_safety.log): Log an toàn sau chạy tại SPID 88 (`TranCount=0, XactState=1`, DROP bảng tạm thành công).
- [TV2_environment_schema.log](run_20261013_140800/TV2_environment_schema.log): Định danh phiên kết nối và kiểm tra bảng `dbo.CHAMCONG` có 0 dòng dữ liệu.
- [TV2_index_metadata.csv](run_20261013_140800/TV2_index_metadata.csv): Metadata chỉ mục vật lý của bảng `dbo.CHAMCONG` trên database QA.

### Nhóm 2: Báo Cáo Phái Sinh Đã Kiểm Chứng (Derived Verified Reports)
- [TV2_results_comparison.md](run_20261013_140800/TV2_results_comparison.md): Báo cáo đối soát xác nhận kết quả Before và After khớp 100% từng dòng.
- [TV2_live_schema_inspection.log](schema-inspection/TV2_live_schema_inspection.log): Trích xuất thực tế từ database QA ủy quyền xác thực 7 cột của `dbo.CHAMCONG`, `PK_CHAMCONG` (CLUSTERED), `UQ_CHAMCONG_MaNV_Ngay`, `FK_CHAMCONG_NHANVIEN`, 2 CHECK constraints, 3 triggers và định nghĩa View `vw_TongHopChamCongThang`.

### Nhóm 3: Tài Liệu Phân Tích Hiện Hành & Phụ Lục Giới Hạn (Derived Material & Canonical Addendum)
- [TV2_benchmark_summary_v2.md](reviewed_v2/TV2_benchmark_summary_v2.md): Báo cáo phân tích chuyên sâu về hiệu năng Covering Index, giải trình chênh lệch I/O (574 vs 522).
- [TV2_benchmark_summary_v3_addendum.md](reviewed_v2/TV2_benchmark_summary_v3_addendum.md): Phụ lục công bố tường minh các giới hạn kỹ thuật (chỉ thị forced INDEX hint, bảng tạm 30.000 dòng, warm cache, độ phân giải timer) và phân định rõ log gốc SPID 88 vs đối soát chẩn đoán SPID 56.

### Nhóm 4: Hướng Dẫn Tái Lập An Toàn & Kịch Bản Phòng Vệ (Canonical Reproduction & Guarded SQL)
- [reproduction_provenance_v3.md](reproduction/reproduction_provenance_v3.md): Thuyết minh nguồn gốc chính thức và hướng dẫn tái lập an toàn fail-closed qua `sqlcmd -b -V16`.
- [test_benchmark_index_TV2_guarded.sql](reproduction/test_benchmark_index_TV2_guarded.sql): Kịch bản SQL phái sinh có chốt chặn `IF DB_NAME() <> ... THROW 53410` ở đầu 100% các batch (*Tài liệu kiểm chuẩn offline — TUYỆT ĐỐI KHÔNG CHẠY trong luồng kiểm định này*).

### Nhóm 5: Gói Trình Chiếu Chỉnh Sửa Được & Ảnh Xem Trước (Final Presentation Suite)
*Được tạo trực tiếp qua Microsoft PowerPoint COM đã cài đặt sẵn trên máy tính.*
- [TV2_Slides_4_6.pptx](presentation/final/TV2_Slides_4_6.pptx): File PowerPoint gồm **ĐÚNG 3 SLIDE** (vị trí nhóm 4–6), có thể chỉnh sửa tự do, kèm **Speaker Notes chi tiết đầy đủ trên từng slide**.
- [slide_4_preview.png](presentation/final/slide_4_preview.png): Ảnh render xem trước của Slide 4 (Nghiệp vụ và Ràng buộc CSDL — đã chuẩn hóa phạm vi bảo vệ SQL trực tiếp).
- [slide_5_preview.png](presentation/final/slide_5_preview.png): Ảnh render xem trước của Slide 5 (SP, Trigger, Giao dịch nhập lô).
- [slide_6_preview.png](presentation/final/slide_6_preview.png): Ảnh render xem trước của Slide 6 (View tổng hợp, Covering Index cắt giảm 99.1289% / 99.0421% Logical Reads).

### Nhóm 6: Văn Bản Đề Xuất & Tài Liệu Chuyển Giao Cho TV5
- [TV5_Proposed_Corrections_v3.md](presentation/reviewed_v2/TV5_Proposed_Corrections_v3.md): Bảng số liệu benchmark và đoạn văn giải trình chuẩn xác (thời gian XML Plan 0ms/0ms cả trước và sau; Messages 31ms ➔ 2ms; tỷ lệ giảm logical reads tách bạch 99.1289% và 99.0421%) để TV5 đưa vào Báo cáo cuối kỳ (§3.2).
- [TV2_Slides_4_6_Content_v2.md](presentation/reviewed_v2/TV2_Slides_4_6_Content_v2.md): Cấu trúc nội dung Slide 4, 5, 6 dạng Markdown.
- [TV2_Speaking_Script_v2.md](presentation/reviewed_v2/TV2_Speaking_Script_v2.md): Kịch bản thuyết trình (02:00), kịch bản dry-run demo (00:45) và 12 câu hỏi vấn đáp chuẩn.
- [TV2_GUI_Demo_Checklist_v2.md](presentation/reviewed_v2/TV2_GUI_Demo_Checklist_v2.md): Checklist demo giao diện (unconfirmed on empty QA).
- [TV2_Documentation_Audit_v2.md](presentation/reviewed_v2/TV2_Documentation_Audit_v2.md): Báo cáo rà soát đối chiếu tài liệu và mã nguồn (Finding A1–A8).
- [TV2_Presentation_Handoff_v2.md](presentation/reviewed_v2/TV2_Presentation_Handoff_v2.md): Biên bản bàn giao kỹ thuật cho TV5.

### Nhóm 7: Minh Chứng Kiểm Thử Tự Động Lịch Sử Tái Sử Dụng (Historical Validated Test Evidence)
*Lưu trữ tham khảo (REFERENCE ONLY) — không chạy lại.*
- [test_module_chamcong_TV2.log](sql-verification/test_module_chamcong_TV2.log): Log thực thi suite kiểm thử Chấm công đạt **21 PASS / 0 FAIL** (Historical Validated Test Evidence).
- [test_module_chamcong_TV2.sql](sql-verification/test_module_chamcong_TV2.sql), [install_02_Module_ChamCong_TV2.sql](sql-verification/install_02_Module_ChamCong_TV2.sql), [schema_environment.log](sql-verification/schema_environment.log): Snapshot mã nguồn kiểm thử (Reference Test Snapshots).

### Nhóm 8: Tài Liệu Phái Sinh Lịch Sử Đã Bị Thay Thế (Superseded & Archive — Traceability Only)
- [run_20261013_140800/TV2_benchmark_summary.md](run_20261013_140800/TV2_benchmark_summary.md): Báo cáo benchmark ban đầu trong thư mục chạy (Superseded Derived Report — được thay thế bởi `reviewed_v2/TV2_benchmark_summary_v2.md`).
- [run_20261013_140800/TV5_Proposed_Corrections.md](run_20261013_140800/TV5_Proposed_Corrections.md): Đề xuất ban đầu cho TV5 (Superseded Derived Report — được thay thế bởi `presentation/reviewed_v2/TV5_Proposed_Corrections_v3.md`).
- [run_20261013_140800/evidence_manifest.csv](run_20261013_140800/evidence_manifest.csv): Manifest nội bộ của riêng thư mục chạy (Historical Run Manifest).
- [run_20261013_140800/test_benchmark_index_TV2.sql](run_20261013_140800/test_benchmark_index_TV2.sql): Mã nguồn SQL benchmark gốc (Reference Benchmark SQL).
- [reproduction/run_tv2_benchmark_final.ps1](reproduction/run_tv2_benchmark_final.ps1): Script runner lịch sử đã phục hồi (Archive Reference Only — DO NOT RUN).
- [reproduction/reproduction_provenance.md](reproduction/reproduction_provenance.md): Thuyết minh nguồn gốc sơ bộ v1 (Superseded).
- [presentation/reviewed_v2/TV5_Proposed_Corrections_v2.md](presentation/reviewed_v2/TV5_Proposed_Corrections_v2.md): Bản đề xuất v2 (Superseded bởi v3).
- [README.md](README.md), [README_VERIFIED.md](README_VERIFIED.md), [manifest_verified.csv](manifest_verified.csv): Các bản mục lục và manifest giai đoạn trước (Superseded).
- [presentation/TV2_Slides_4_6_Content.md](presentation/TV2_Slides_4_6_Content.md), [TV2_Speaking_Script.md](presentation/TV2_Speaking_Script.md), [TV2_GUI_Demo_Checklist.md](presentation/TV2_GUI_Demo_Checklist.md), [TV2_Documentation_Audit.md](presentation/TV2_Documentation_Audit.md), [TV2_Presentation_Handoff.md](presentation/TV2_Presentation_Handoff.md), [TV5_Proposed_Corrections.md](presentation/TV5_Proposed_Corrections.md): Các bản nháp thuyết trình v1 ban đầu (Superseded).
- [archive/backup_pre_remediation_20261013_150800.zip](archive/backup_pre_remediation_20261013_150800.zip): File nén lưu trữ các bản trước khi thực hiện lượt remediation ban đầu (Historical Non-Authoritative Backup Archive).
- [archive/backup_pre_job005_20261013_154000.zip](archive/backup_pre_job005_20261013_154000.zip): File nén lưu trữ các bản trước khi thực hiện lượt kiểm định Job 005 (Historical Non-Authoritative Backup Archive).

---

## 2. Bảng Tóm Tắt Số Liệu Benchmark Chính Thức (Run ID `run_20261013_140800`)

| Chỉ số kỹ thuật | Truy vấn chi tiết (1.1 ➔ 3.1, 273 dòng) | Truy vấn tổng hợp (1.2 ➔ 3.2, 9 dòng) | Cơ sở kiểm chứng |
|---|:---:|:---:|---|
| **Cấu trúc Baseline** | Index Seek + Key Lookup | Index Seek + Key Lookup + Aggregate | File `.sqlplan` Before |
| **Cấu trúc Covering Index** | Pure Index Seek (0 Key Lookup) | Pure Index Seek + Aggregate (0 Lookup) | File `.sqlplan` After |
| **Logical Reads (Messages)** | **574 ➔ 5 (Giảm 99.1289%)** | **574 ➔ 5 (Giảm 99.1289%)** | `TV2_benchmark_messages.log` |
| **Logical Reads (XML Plan)** | **522 ➔ 5 (Giảm 99.0421%)** | **522 ➔ 5 (Giảm 99.0421%)** | Cây toán tử trong XML Plan |
| **Thời gian (Messages)** | **CPU 15ms / Elapsed 31ms ➔ 0ms / 2ms** | *Không xuất dòng kết thúc trong Messages* | `TV2_benchmark_messages.log` |
| **Thời gian (XML QueryTimeStats)**| **CPU 0ms / Elapsed 0ms ➔ 0ms / 0ms** | **CPU 1ms / Elapsed 1ms ➔ 0ms / 0ms** | Thuộc tính `QueryTimeStats` của Plan |
| **Độ tương đồng dữ liệu** | **100% khớp tuyệt đối (273/273 dòng)** | **100% khớp tuyệt đối (9/9 dòng)** | `TV2_results_comparison.md` |

---

## 3. Các Điểm Kỹ Thuật Đã Được Thẩm Định và Thống Nhất

1. **Bản chất độ chênh lệch I/O 574 vs 522:** Con số 574 đo từ `SET STATISTICS IO` (tầng buffer pool page reads) và 522 từ bộ đếm toán tử XML runtime. Nguyên nhân cụ thể của 52 trang là UNKNOWN (không đưa ra giả định chưa chứng minh về b-tree navigation). Cả hai phương thức đo đều ghi nhận chính xác **5 logical reads** khi có covering index. Không đưa ra tuyên bố đo lường về cắt giảm đọc đĩa vật lý.
2. **Cảnh báo NULL:** Phát sinh từ phép toán `COUNT(CASE WHEN TrangThai IN (...) THEN 1 END)` không có mệnh đề `ELSE`, theo đúng chuẩn xử lý NULL của ANSI SQL.
3. **Phạm vi bảo vệ của ràng buộc CSDL (Slide 4):** Ràng buộc `CHECK`, `UNIQUE`, `FOREIGN KEY` và Trigger bảo đảm thực thi đúng các điều kiện toàn vẹn cụ thể đã định nghĩa ngay cả khi ghi bằng SQL ngoài app; câu lệnh SQL trực tiếp hợp lệ vẫn thực thi bình thường; cơ chế này bảo vệ theo phạm vi ràng buộc đã định nghĩa và không thay thế hoàn toàn quyền quản trị của DBA cấp cao.
4. **Phân định quyền sở hữu:**
   - TV2 sở hữu: Bảng `dbo.CHAMCONG`, `sp_GhiNhanChamCong`, 2 trigger module 02 (`TC-CC-07`, `TC-CC-06`), view `vw_TongHopChamCongThang`, covering index `IX_CHAMCONG_MaNV_Ngay`, giao dịch nhập lô All-or-Nothing (`TC-CC-08`, `TC-CC-09`), và hàm `dbo.fn_TongPhuCap` (đặt tại module 03 của TV3).
   - TV1 sở hữu: Hàm `fn_TinhSoNgayCong` (dù đặt ở script 02). Phân hệ Lương (TV4) đếm trực tiếp bảng `CHAMCONG` có khóa `HOLDLOCK`, không gọi hàm này.
   - TV5 sở hữu: Trigger khóa kỳ lương đã chốt `trg_ChamCong_KhongSuaKhiDaChotLuong` (module 05).
5. **Ranh giới an toàn giới hạn (Bounded Safety):** Các kiểm tra an toàn sau chạy xác nhận tính toàn vẹn cục bộ của bảng mục tiêu `dbo.CHAMCONG` (0 dòng) và danh mục bảng (table inventory), không cấu thành phép lấy dấu vân tay độc lập (independent fingerprinting) trên toàn bộ các cơ sở dữ liệu sản xuất.
6. **Tình trạng Demo GUI:** Trên database QA ủy quyền rỗng hiện hành (0 dòng trong `dbo.CHAMCONG`), việc demo live trên GUI chưa được kiểm chứng thực tế. Gói nộp cung cấp kịch bản dry-run và minh chứng tự động 21/21 PASS cùng các file execution plan thực tế làm minh chứng chính.
