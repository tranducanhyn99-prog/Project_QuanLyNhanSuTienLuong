# TV5 tích hợp TV3 và kiểm package ngày 15 tháng 10

Trần Đức Anh — 24110155. Làm trên `main` sau PR #48 (`a98371d`), nhánh `codex/tv5-integrate-tv3`. TV5 đã hoàn thành phần tích hợp TV3, cập nhật tài liệu nhóm và kiểm package có thể thực hiện trên máy hiện tại. Bộ nộp vẫn còn các đầu việc cần bàn giao hoặc kiểm thực tế bên dưới.

## Đã hoàn thành

| Task | Kết quả và bằng chứng |
|---|---|
| TV5-08 nhận TV3 | [13 file TV3](../Bo_nop_Nhom06/evidence/TV3/README_EVIDENCE.md) đã có trong package, thay marker chờ. 12 file nguyên byte PR #43; Word đúng bản hiệu đính PR #48. CSV ghi nguồn/hash và giới hạn source SQL chưa xác minh. |
| TV5-09 Word | Hai bản Word repo/package đồng nhất; cập nhật TV3 trong bảng SQL, benchmark, bàn giao và nguồn. Word xuất native vẫn 61 trang tổng/55 trang nội dung; sáu trang đổi đã xem, 55 trang còn lại trùng pixel. |
| TV5-09 deck | Giữ 15 slide và phần hiển thị, nhận notes 7–9 của Đạt; đường dẫn minh chứng đổi theo package. Kiểm cấu trúc/import/font/bốn bảng native đạt; render PowerPoint 15/15 slide trùng pixel với bản trước. |
| TV5-10 package | Đã đồng bộ README/trạng thái bàn giao và manifest. [Biên bản QA](../Bo_nop_Nhom06/evidence/TV5/package-integration-20261015/README.md) có log và hash; inventory 511/511, manifest 537/537 và 273 liên kết nội bộ đạt; 396 file evidence cũ giữ nguyên hash. |
| Build/offline trên bản sao độc lập | Không có Git hoặc build cũ; 54 source biên dịch Java 11 thành công, Security 104 + GuiQa 88 + LightTheme 45 + LoginChip 20 = 257 assertions. 81 file source/resource/JDBC/database giữ nguyên so với trước tích hợp. |

Số TV3 642→2 thuộc log/plan ngày 13/10; Messages elapsed 37/27 ms và XML 4/0 ms là hai nguồn đo khác nhau. Không lấy log bốn PASS của bài header làm bằng chứng đã kiểm trigger hoặc SP OUTPUT; ảnh cũ chỉ minh họa lịch sử.

## Còn chờ

| Người phụ trách | Việc còn lại | Điều kiện hoàn tất |
|---|---|---|
| TV1 | ERD cuối đủ 10 bảng, log/benchmark/plan nhân sự, xác nhận phần Word và slide 1–3 | TV5 nhận file gốc, đối chiếu source/giới hạn rồi tích hợp; không dùng ảnh ERD cũ 9 bảng |
| TV2 + TV5 | Xác nhận yêu cầu TRY…CATCH của `sp_GhiNhanChamCong` trong kế hoạch | Ghi rõ caller Java đang giữ transaction/rollback lô; không thêm catch hình thức hoặc tự nhận lỗi runtime chưa chứng minh |
| TV5 + máy nhận | Cài schema/provisioning/JDBC và chạy luồng thật trên môi trường Windows khác | Có log máy/môi trường/phiên bản và kết quả; build cùng máy không thay phép thử này |
| TV5 | Windows DPI thật và màn hình trình chiếu | Kiểm ở mức scaling thực, lưu ảnh/kết quả; ảnh Swing scale không thay thao tác này |
| Cả nhóm, TV5 điều phối | Rehearsal tay nối tiếp 10/2026 | Ghi phiếu [TV5_TAP_DEMO](../Bo_nop_Nhom06/TV5_TAP_DEMO.md), xác nhận A 730.000đ/B 0đ và cuối kỳ CHUA_CHOT/NgayChot NULL, nguồn không đổi |
| TV5 | PDF nộp nếu hình thức yêu cầu | Chốt Word sau TV1, xuất PDF và kiểm lại; PDF QA hiện tại chưa được gọi là bản cuối |

Không đổi SQL/Java và không chạy lại SQL/live GUI trong lượt này. Những checkbox còn mở trong [tasklist](TV5_TASKLIST_HOAN_THIEN.md) không được tự đánh dấu PASS từ các kiểm offline.

## Cập nhật sau PR #51/#52 — 16/10/2026

TV1 PR #51 và TV2 PR #52 đã merge. TV5 nhận và đối chiếu ERD 10 bảng, test nhân sự 7/7, benchmark 75→10 cùng deck riêng TV1; slide nhóm 1–3 vẫn là tóm tắt và notes dẫn tới bộ chi tiết. TV2 Option B cuối đạt 55 nguồn compile, controlled 179/179, SQL 21/21, concurrency 4/4, JDBC 105; evidence đã chép vào package. Hai bản Word đã đồng bộ byte, cập nhật TRY…CATCH + bare THROW và transaction Java; 69 trang tổng/56 nội dung, chỉ trang 11/33 đổi, đã rà. Notes 2/3/5 trong deck cập nhật; render trước/sau không đổi và Artifact Tool checks đạt.

Còn chờ Windows DPI thật, cài trên máy khác, rehearsal nhóm và PDF nếu được yêu cầu. Package chưa được kiểm trên máy nhận.
