# TV2 — PHỤ LỤC GIỚI HẠN THỰC NGHIỆM VÀ ĐỐI SOÁT CHẨN ĐOÁN (SUMMARY V3 ADDENDUM)

- **Người lập:** Phạm Minh Quân (TV2 — MSSV: 24110311)
- **Ngày bổ sung:** 13/10/2026 (Hiệu chỉnh Loop 2 theo thẩm định Codex)
- **Tài liệu gốc liên quan:** `reviewed_v2/TV2_benchmark_summary_v2.md`
- **Mục đích:** Bổ sung tường minh các giới hạn kỹ thuật của bài đo lường benchmark và làm rõ phương pháp luận chẩn đoán phiên giao dịch `XACT_STATE()`.

---

## 1. Các Giới Hạn Kỹ Thuật Của Thực Nghiệm Benchmark (Explicit Limitations)

Bài đo lường benchmark thực nghiệm trong Run ID `run_20261013_140800` được thiết kế nhằm mục đích khoa học là đo lường sự chênh lệch chi phí I/O giữa cơ chế **Key Lookup** và cơ chế **Covering Index** trong môi trường kiểm chuẩn kiểm soát được. Người đọc cần lưu ý các giới hạn kỹ thuật sau:

1. **Sử dụng Index Hint cưỡng bức (`WITH (INDEX(...))`):**
   - Kịch bản kiểm thử áp dụng tường minh chỉ thị `WITH (INDEX(UQ_Bench_MaNV_Ngay))` cho nhóm Baseline (1.1, 1.2) và `WITH (INDEX(IX_Bench_CHAMCONG_MaNV_Ngay))` cho nhóm Covering Index (3.1, 3.2).
   - Chỉ thị này nhằm cô lập và buộc Query Optimizer sử dụng đúng cây kế hoạch mục tiêu cần so sánh, tránh trường hợp bộ tối ưu hóa tự động chuyển sang Clustered Index Scan do kích thước dữ liệu hoặc thống kê phân phối. Đây không phải là hành vi mặc định không có hint trong môi trường sản xuất.
2. **Quy mô dữ liệu mô phỏng trên Bảng tạm (30.000 dòng):**
   - Thực nghiệm được thực hiện trên 30.000 dòng dữ liệu tổng hợp sinh tự động trong bảng tạm cục bộ `#CHAMCONG_BENCHMARK`. Bảng tạm nằm trong `tempdb`, có cấu trúc trang và cơ chế ghi log khác biệt một phần so với bảng người dùng vật lý trên đĩa.
3. **Đo lường một lần duy nhất (Single-run Capture) & Cache nóng (Warm Cache):**
   - Số liệu được thu thập từ một lượt chạy đồng nhất (Single Capture Run) sau quá trình nạp dữ liệu. Các trang dữ liệu và trang chỉ mục đã nằm sẵn trên bộ đệm (Buffer Pool / Warm Cache), do đó số lượt đọc phản ánh **Logical Reads** (đọc từ bộ nhớ đệm), không đo lường **Physical Reads** (đọc từ đĩa vật lý).
4. **Độ phân giải thời gian thực thi (Timer Resolution):**
   - Độ phân giải của bộ đếm thời gian CPU / Elapsed trong SQL Server ở mức mili-giây (1ms – 15.6ms tùy thuộc vào Windows clock interrupt). Các giá trị 0ms hoặc 1ms không phản ánh thời gian thực thi tuyệt đối bằng 0 mà nằm dưới ngưỡng phân giải tối thiểu.
5. **Không phải là cam kết độ trễ hay tải sản xuất (No Production/Latency Guarantee):**
   - Kết quả thực nghiệm là minh chứng định lượng về **sự cắt giảm I/O (99.13% logical reads)** của cấu trúc Covering Index. Số liệu này không mang ý nghĩa là cam kết SLA về độ trễ đáp ứng (latency guarantee) hay khả năng chịu tải đồng thời (throughput guarantee) dưới môi trường tải sản xuất quy mô lớn nhiều kết nối đồng thời.

---

## 2. Làm Rõ Bản Chất Chẩn Đoán Giao Dịch `XACT_STATE` (SPID Inference Note)

1. **Bảo tồn nguyên trạng Log gốc:**
   - Trong file log dọn dẹp gốc `run_20261013_140800/TV2_cleanup_safety.log` (ghi nhận tại SPID 88 trong phiên benchmark ngày 13/10/2026), dòng lệnh kiểm tra an toàn sau lệnh `DROP TABLE #CHAMCONG_BENCHMARK` đã xuất ra:
     `TranCount=0, XactState=1`. Giá trị gốc này được **bảo tồn nguyên vẹn** và không bị sửa đổi.
2. **Đối soát chẩn đoán ở phiên kế tiếp (SPID 56):**
   - Nhằm giải mã nguyên nhân vì sao `@@TRANCOUNT = 0` nhưng `XACT_STATE()` lại trả về 1, một phiên chẩn đoán read-only tách biệt đã được thực thi tại SPID 56:
     - Câu lệnh đơn lẻ: `SELECT @@TRANCOUNT AS TranCount, XACT_STATE() AS StandaloneXactState` ➔ Kết quả: `TranCount = 0, StandaloneXactState = 0`.
     - Câu lệnh ghép: `SELECT OBJECT_ID('tempdb..#...'), XACT_STATE() AS CompoundXactState` ➔ Kết quả: `CompoundXactState = 1`.
3. **Kết luận suy luận kỹ thuật (Technical Inference):**
   - Giải trình về việc *"hàm `OBJECT_ID(...)` truy vấn catalog `tempdb` dưới ngữ cảnh đọc khiến `XACT_STATE()` tạm thời mang giá trị 1"* là một **suy luận kỹ thuật (inference)** được rút ra từ phép so sánh thực nghiệm giữa hai câu lệnh tại SPID 56.
   - Suy luận này khẳng định phiên ban đầu (SPID 88) hoàn toàn không có giao dịch người dùng nào bị treo (`@@TRANCOUNT = 0`), bảng tạm đã được DROP sạch sẽ, và cơ sở dữ liệu hoàn toàn an toàn.
