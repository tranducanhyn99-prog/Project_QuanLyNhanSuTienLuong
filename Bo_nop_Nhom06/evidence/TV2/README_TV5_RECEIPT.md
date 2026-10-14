# TV5 nhận bộ TV2 từ PR44 — 13/10/2026

Đã nhận **52/52 file nguyên byte** từ [PR44 head `7efd9dd88859cac08f6bcf2e755e5c1b68437fd3`](https://github.com/tranducanhyn99-prog/Project_QuanLyNhanSuTienLuong/pull/44), đối chiếu SHA-256 nguồn/sau copy. Đây là tiếp nhận artifact local từ PR đang mở; **không phải merge/approve PR** hoặc xác nhận SQL/GUI đã chạy trên máy TV5. Giữ nguyên mọi raw log, plan, CSV, PPTX, Markdown và manifest do TV2 bàn giao.

Điều hướng bắt đầu tại [README_FINAL.md](README_FINAL.md); hash toàn bộ 51 payload ở [manifest_final.csv](manifest_final.csv) (manifest không tự hash). Bốn actual plans, hai cặp Results đầy đủ, Messages, environment và cleanup đã được rà độc lập trước nhận. Fixture 30.000 dòng, detail 273 dòng, aggregate 9 dòng, INDEX hint/warm cache/một lượt. **Messages 574→5 reads**, **XML operator 522→5** là hai nguồn đo khác nhau; nguyên nhân lệch 52 reads chưa xác định. Không ghi là tốc độ sản xuất hoặc phép đo query GUI.

Chọn [bảng v3 để tổng hợp Word](presentation/reviewed_v2/TV5_Proposed_Corrections_v3.md) và [phụ lục benchmark v3](reviewed_v2/TV2_benchmark_summary_v3_addendum.md), cùng [PPTX slide 4–6](presentation/final/TV2_Slides_4_6.pptx). Notes/PPTX đã được rà bằng PowerPoint native; chưa ghép/render deck chung trong biên nhận này.

**Giới hạn handoff:** `TV2_Presentation_Handoff_v2.md` vẫn có hướng dẫn lấy bảng v2 đã superseded và runner archive. Không làm theo các dòng đó; dùng bảng v3 và [provenance reproduction v3](reproduction/reproduction_provenance_v3.md) + guarded SQL. TV2 cần sửa handoff và hash lại trước nghiệm thu biên bản cuối. TV5 không chỉnh raw file để giữ provenance.

Log 21 SQL testcase PASS/0 FAIL là bằng chứng CSDL; testcase INSERT/transaction không thay runtime chấm công lô Java/GUI. TRY…CATCH trong `sp_GhiNhanChamCong` và GUI điều chỉnh tháng còn thuộc phối hợp TV5-07. Source TV5 mới về miền tiền/thông báo lỗi không được chứng minh bởi artifacts lịch sử TV2.
