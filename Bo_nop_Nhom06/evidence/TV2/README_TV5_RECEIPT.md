# TV5 nhận bộ TV2 từ PR44 — 13/10/2026

Đã nhận **52/52 file nguyên byte** từ [PR44 head `7efd9dd88859cac08f6bcf2e755e5c1b68437fd3`](https://github.com/tranducanhyn99-prog/Project_QuanLyNhanSuTienLuong/pull/44), đối chiếu SHA-256 nguồn/sau copy. Lúc lập biên nhận ngày 13/10, PR còn mở; PR đã được merge sau đó. Biên nhận không tự xác nhận SQL/GUI đã chạy trên máy TV5. Giữ nguyên mọi raw log, plan, CSV, PPTX, Markdown và manifest do TV2 bàn giao.

Điều hướng bắt đầu tại [README_FINAL.md](README_FINAL.md); hash toàn bộ 51 payload ở [manifest_final.csv](manifest_final.csv) (manifest không tự hash). Bốn actual plans, hai cặp Results đầy đủ, Messages, environment và cleanup đã được rà độc lập trước nhận. Fixture 30.000 dòng, detail 273 dòng, aggregate 9 dòng, INDEX hint/warm cache/một lượt. **Messages 574→5 reads**, **XML operator 522→5** là hai nguồn đo khác nhau; nguyên nhân lệch 52 reads chưa xác định. Không ghi là tốc độ sản xuất hoặc phép đo query GUI.

Chọn [bảng v3 để tổng hợp Word](presentation/reviewed_v2/TV5_Proposed_Corrections_v3.md) và [phụ lục benchmark v3](reviewed_v2/TV2_benchmark_summary_v3_addendum.md), cùng [PPTX slide 4–6](presentation/final/TV2_Slides_4_6.pptx). Notes/PPTX đã được rà bằng PowerPoint native; chưa ghép/render deck chung trong biên nhận này.

## Hiệu đính sử dụng của TV5 — 14/10/2026

[Biên bản v2 do TV2 bàn giao](presentation/reviewed_v2/TV2_Presentation_Handoff_v2.md) được giữ nguyên byte và tác giả để truy vết. Khi tích hợp bộ nhóm, TV5 dùng các chỉ dẫn đã đối chiếu dưới đây thay cho các dòng cũ trong biên bản:

| Dòng trong biên bản v2 | Chỉ dẫn TV5 sử dụng |
|---|---|
| 20, 36 — bảng Word v2 | Dùng [bảng đề xuất v3](presentation/reviewed_v2/TV5_Proposed_Corrections_v3.md); bản v2 đã bị thay thế. |
| 23 — runner/provenance cũ | Dùng [provenance v3](reproduction/reproduction_provenance_v3.md) và [SQL có guard](reproduction/test_benchmark_index_TV2_guarded.sql). `run_tv2_benchmark_final.ps1` chỉ lưu trữ, không chạy theo biên bản cũ. |
| 45 — slide từ Markdown | Dùng [PPTX slide 4–6 đã bàn giao](presentation/final/TV2_Slides_4_6.pptx); Markdown là tài liệu tham khảo. |

Đây là hiệu đính của **TV5 cho bước tích hợp**, không phải bản sửa được TV2 ký xác nhận. Các phép đo, raw evidence, 52 file gốc và hai manifest TV2 không đổi. TV5 không cần chờ TV2 sửa biên bản v2 để dùng các đầu vào v3 đã được TV2 công bố trong `README_FINAL.md`.

Log 21 SQL testcase PASS/0 FAIL là bằng chứng CSDL; testcase INSERT/transaction không thay runtime chấm công lô Java/GUI. TRY…CATCH trong `sp_GhiNhanChamCong` và GUI điều chỉnh tháng còn thuộc phối hợp TV5-07. Source TV5 mới về miền tiền/thông báo lỗi không được chứng minh bởi artifacts lịch sử TV2.
