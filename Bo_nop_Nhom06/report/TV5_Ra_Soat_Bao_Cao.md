# TV5 — Rà soát và bàn giao báo cáo Word

Ngày 11/10/2026. Người phụ trách: Trần Đức Anh — 24110155. Source hiện tại `f2a41f7`; logic SQL/Java sau PR #38 ở `0223f2f`, các thay đổi sau đó thuộc tài liệu. Đây là rà soát tài liệu và minh chứng đã có, không phải lần chạy QA mới.

## Bản bàn giao

[Báo cáo Word](Nhom_06_Bao_Cao_Cuoi_Ky_DBMS330284.docx) được sao chép nguyên byte từ `docs/Nhom_06_Bao_Cao_Cuoi_Ky_DBMS330284.docx`. Không sửa số đo hay tạo kết quả mới. SHA-256:

```text
21c1b7025453804a1bfe873627b986b0cdc94310710fdb5ebaf944f2d3c26251
```

## Đã kiểm trong lượt này

- Nội dung TV5 khớp SQL module 05 và phần xác thực Java: mapping SQL login/user/role, `ORIGINAL_LOGIN()`, view cá nhân, GRANT/REVOKE/DENY và khóa header khi chốt.
- Quyền Payroll có SELECT/INSERT trực tiếp header/chi tiết; cập nhật/xóa đi qua procedure. Báo cáo không khẳng định mọi thao tác chỉ có đường GUI.
- Concurrency A/B ngày 07/10 ghi 20.631 ms (20,631 giây); bốn race nguồn ngày 08/10 là phép thử khác. Không nhầm số này thành timeout/SLA hoặc đã ngắt SQL trên GUI.
- Benchmark TV5 142 → 15 logical reads dùng actual plans 4/5, fixture 10.000 dòng, kết quả 833 dòng và hint scan/seek. Plan 6 trả 0 dòng không được dùng làm cải thiện.
- Identity suite 50 checks tách biệt với 224 assertions Java. GUI ghi 20 ca và ba ca kiểm lại sau sửa; không tuyên bố tất cả PASS ngay từ đầu.
- Word đọc được: 521 paragraphs, 57 tables đều có header lặp, bốn ảnh có mô tả. Bản render đã lưu ngày 10/10 có 61 trang tổng; metadata xác định trang nội dung 5–59, tương đương 55 trang. Đã xem lại các trang PDF 46, 55, 56 tương ứng quyền, benchmark và concurrency; không render lại toàn bộ Word trong lượt này.

## Phần vẫn chờ bàn giao/kiểm thực tế

- TV1 xác nhận ERD cuối có `LICHSULUONG` và từ điển dữ liệu; ảnh ERD cũ không đủ.
- TV2 cung cấp benchmark hiện hành, Results, IO/TIME, bốn actual plans và summary. Báo cáo đang ghi 574 → 5 là lịch sử; chỉ cập nhật sau khi có số đo thật.
- Cập nhật 13/10: TV4 đã bàn giao từ PR #41; [Word phần TV4](../evidence/TV4/TV4_Phan_Bao_Cao.docx) được sửa độ rộng/font riêng hai bảng schema, nội dung giữ nguyên, render và xem đủ 4 trang. TV1–TV3 vẫn cần rà chương và bàn giao slide/minh chứng. Báo cáo toàn nhóm 55 trang nội dung giữ nguyên; phần TV4 bàn giao bổ sung không được cộng tự động thành báo cáo mới.
- Kiểm mục lục/số trang trên Word máy nộp. Nếu phải nộp PDF, xuất lại từ bản Word cuối và kiểm pagination; PDF render cũ là tài liệu QA nội bộ, không được đóng gói thành PDF nộp mới.
- Chạy thử package trên máy khác và tập demo thật. Kiểm tra chỉ đọc ngày 11/10 cho thấy `MSSQLSERVER` đang dừng, cổng 1433 chưa mở; lượt này chưa thao tác live GUI hoặc database.

Tiếp tục theo [README bộ nộp](../README_BO_NOP.md) và [kịch bản demo TV5](../TV5_TAP_DEMO.md). Chỉ đánh dấu hoàn tất sau khi có kết quả thực tế.
