# Deck chung và slide bàn giao

[Nhom06_ThuyetTrinh_15_Slides.pptx](Nhom06_ThuyetTrinh_15_Slides.pptx) là deck chung **15 slide**, cập nhật 16/10/2026. TV5 nhận notes TV3 từ PR #43/#48, đổi đường dẫn theo package; toàn bộ phần hiển thị của deck giữ nguyên, không thêm slide.

| Slide | Người trình bày | Nội dung |
|---|---|---|
|1–3|Nguyễn Minh Trí — 24110359|Bài toán, ERD 10 bảng, nhân sự và transaction hồ sơ|
|4–6|Phạm Minh Quân — 24110311|Chấm công, batch, benchmark canonical v3|
|7–9|Trần Tiến Đạt — 24110198|Khoản phát sinh, kỳ nháp, log/plan 13/10 và ảnh lịch sử|
|10–12|Nguyễn Quang Vinh — 24110385|Công thức, snapshot, transaction và JDBC payroll|
|13–15|Trần Đức Anh — 24110155|Danh tính SQL, chốt/quyền, kiểm thử và demo|

Nền sáng, chữ navy, primary theo PeopleOS và font Segoe UI. Bốn bảng là đối tượng native editable; speaker notes ghi ownership, nguồn và giới hạn của từng phép đo. TV2 giữ riêng Messages 574/5 và XML 522/5; notes slide 5 cũng cập nhật kết quả Option B cuối (SQL 21/21, JDBC 105, controlled 179/179, concurrency 4/4), có dẫn evidence ngày 16/10. TV5 dùng benchmark 13/10 là 126/13. Java offline 257 được tách khỏi kiểm SQL/GUI live. Demo 10/2026 giữ A 730.000đ/B 0đ và kết thúc `CHUA_CHOT`, `NgayChot IS NULL`.

Đã kiểm cấu trúc, font, thứ tự/count, import và render bằng Artifact Tool; package integrity/layout đạt, 15 slide import được và có 4 bảng native. Lượt 16/10 chỉ đổi notes 2/3/5; render trước/sau của các slide này trùng khớp, phần hiển thị giữ nguyên. [Preview slide 1](preview/group/slide-1.png) đến [slide 15](preview/group/slide-15.png) vẫn áp dụng. Nội dung slide 1–3 được đối chiếu với TV1 handoff PR #51; nhóm giữ phiên bản tóm tắt ngắn theo giới hạn 15 slide. Chưa kiểm native PowerPoint, rehearsal hoặc máy trình chiếu khác.

SHA-256 deck chung:

```text
3D9BD42FD03BA5B7AC139AEA547C62483F6965E848A031D5EF9DB407D446D6DB
```

## Bản bàn giao gốc được giữ riêng

- [TV2 slide 4–6](../evidence/TV2/presentation/final/TV2_Slides_4_6.pptx), đúng Quân và notes, nhận nguyên byte từ PR #44 head 7efd9dd. Dùng [biên nhận và hiệu đính TV5](../evidence/TV2/README_TV5_RECEIPT.md) theo canonical v3; biên bản v2 gốc được giữ nguyên để truy vết.
- [TV3 slide 7–9](../evidence/TV3/Minh_chung/TV3_Slides_7-9.pptx), đúng Đạt, nguyên byte PR #43; [biên nhận](../evidence/TV3/README_EVIDENCE.md) phân biệt log/plan 13/10 với ảnh lịch sử.
- [TV4 slide 10–12](../evidence/TV4/TV4_Slides_10-12.pptx), đúng Vinh, từ PR #41. Giữ nguyên cạnh `Minh_chung/` để nguồn trong notes truy được.
- [TV5 slide 13–15](TV5_Slide_13-15.pptx) là deck từng phần dựng 11/10. Notes/số liệu ghi phạm vi lịch sử, không dùng nó thay deck chung cập nhật 13/10. [Ba preview cũ](preview/TV5_Slide_13.png) giữ nguồn cũ.

[Kịch bản và phiếu ghi lượt tập](../TV5_TAP_DEMO.md) vẫn để kết quả trống đến khi chạy thật. Hướng dẫn 1–5 nằm trong docs của repo; chúng không thay PPTX. Nguồn dựng/QA deck chung lưu riêng `build/tv5-final-docs-20261013/`, không phải log thực thi nghiệp vụ.
