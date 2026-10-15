# Deck chung và slide bàn giao

[Nhom06_ThuyetTrinh_15_Slides.pptx](Nhom06_ThuyetTrinh_15_Slides.pptx) là deck chung **15 slide**, cập nhật 15/10/2026. TV5 nhận notes TV3 từ PR #43/#48, đổi đường dẫn theo package; toàn bộ phần hiển thị của deck giữ nguyên, không thêm slide.

| Slide | Người trình bày | Nội dung |
|---|---|---|
|1–3|Nguyễn Minh Trí — 24110359|Bài toán, ERD 10 bảng, nhân sự và transaction hồ sơ|
|4–6|Phạm Minh Quân — 24110311|Chấm công, batch, benchmark canonical v3|
|7–9|Trần Tiến Đạt — 24110198|Khoản phát sinh, kỳ nháp, log/plan 13/10 và ảnh lịch sử|
|10–12|Nguyễn Quang Vinh — 24110385|Công thức, snapshot, transaction và JDBC payroll|
|13–15|Trần Đức Anh — 24110155|Danh tính SQL, chốt/quyền, kiểm thử và demo|

Nền sáng, chữ navy, primary theo PeopleOS và font Segoe UI. Bốn bảng là đối tượng native editable; speaker notes ghi ownership, nguồn và giới hạn của từng phép đo. TV2 giữ riêng Messages 574/5 và XML 522/5; TV5 dùng benchmark13/10 là 126/13. Java offline 257 được tách khỏi kiểm SQL/GUI live. Demo 10/2026 giữ A 730.000đ/B 0đ và kết thúc `CHUA_CHOT`, `NgayChot IS NULL`.

Đã kiểm cấu trúc, font, thứ tự/count, import và render bằng PowerPoint. Lượt 15/10 chỉ đổi notes 7–9; đã so bản render trước/sau và kiểm notes mới. [Preview slide 1](preview/group/slide-1.png) đến [slide 15](preview/group/slide-15.png) vẫn khớp vì phần hiển thị giữ nguyên. TV1 còn chờ xác nhận slide 1–3; rehearsal chung và máy trình chiếu khác chưa kiểm.

SHA-256 deck chung:

```text
E26D60798D5A099678D28461587B6F501BF4024FE00E3CE35472658B2088A35F
```

## Bản bàn giao gốc được giữ riêng

- [TV2 slide 4–6](../evidence/TV2/presentation/final/TV2_Slides_4_6.pptx), đúng Quân và notes, nhận nguyên byte từ PR #44 head 7efd9dd. Dùng [biên nhận và hiệu đính TV5](../evidence/TV2/README_TV5_RECEIPT.md) theo canonical v3; biên bản v2 gốc được giữ nguyên để truy vết.
- [TV3 slide 7–9](../evidence/TV3/Minh_chung/TV3_Slides_7-9.pptx), đúng Đạt, nguyên byte PR #43; [biên nhận](../evidence/TV3/README_EVIDENCE.md) phân biệt log/plan 13/10 với ảnh lịch sử.
- [TV4 slide 10–12](../evidence/TV4/TV4_Slides_10-12.pptx), đúng Vinh, từ PR #41. Giữ nguyên cạnh `Minh_chung/` để nguồn trong notes truy được.
- [TV5 slide 13–15](TV5_Slide_13-15.pptx) là deck từng phần dựng 11/10. Notes/số liệu ghi phạm vi lịch sử, không dùng nó thay deck chung cập nhật 13/10. [Ba preview cũ](preview/TV5_Slide_13.png) giữ nguồn cũ.

[Kịch bản và phiếu ghi lượt tập](../TV5_TAP_DEMO.md) vẫn để kết quả trống đến khi chạy thật. Hướng dẫn 1–5 nằm trong docs của repo; chúng không thay PPTX. Nguồn dựng/QA deck chung lưu riêng `build/tv5-final-docs-20261013/`, không phải log thực thi nghiệp vụ.
