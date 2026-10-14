# Deck chung và slide bàn giao

[Nhom06_ThuyetTrinh_15_Slides.pptx](Nhom06_ThuyetTrinh_15_Slides.pptx) là deck chung **15 slide**, cập nhật 13/10/2026. TV5 tổng hợp theo phân công, năm guide, source đã rà và evidence được nhận. Không thêm slide bìa ngoài 15 trang.

| Slide | Người trình bày | Nội dung |
|---|---|---|
|1–3|Nguyễn Minh Trí — 24110359|Bài toán, ERD 10 bảng, nhân sự và transaction hồ sơ|
|4–6|Phạm Minh Quân — 24110311|Chấm công, batch, benchmark canonical v3|
|7–9|Trần Tiến Đạt — 24110198|Khoản phát sinh, kỳ nháp, GUI và evidence lịch sử|
|10–12|Nguyễn Quang Vinh — 24110385|Công thức, snapshot, transaction và JDBC payroll|
|13–15|Trần Đức Anh — 24110155|Danh tính SQL, chốt/quyền, kiểm thử và demo|

Nền sáng, chữ navy, primary theo PeopleOS và font Segoe UI. Bốn bảng là đối tượng native editable; speaker notes ghi ownership, nguồn và giới hạn của từng phép đo. TV2 giữ riêng Messages 574/5 và XML 522/5; TV5 dùng benchmark13/10 là 126/13. Java offline 257 được tách khỏi kiểm SQL/GUI live. Demo 10/2026 giữ A 730.000đ/B 0đ và kết thúc `CHUA_CHOT`, `NgayChot IS NULL`.

Đã kiểm package, font, thứ tự/count, import, mở bằng PowerPoint và render/xem đủ 15 slide. [Preview slide 1](preview/group/slide-1.png) đến [slide 15](preview/group/slide-15.png) dùng xem nhanh. Chưa tập dượt chung hoặc kiểm máy thuyết trình khác. TV1/TV3 cần xác nhận phần tổng hợp; deck này không tự nghiệm thu artifacts còn lỗi ở PR #43.

SHA-256 deck chung:

```text
E414AEAC526CBB79EF4875B9A104FF05273197FD94BFF054FD6C91963C524C71
```

## Bản bàn giao gốc được giữ riêng

- [TV2 slide 4–6](../evidence/TV2/presentation/final/TV2_Slides_4_6.pptx), đúng Quân và notes, nhận nguyên byte từ PR #44 head 7efd9dd. Theo [biên nhận](../evidence/TV2/README_TV5_RECEIPT.md) và canonical v3; biên bản handoff v2 còn cần sửa tham chiếu.
- [TV4 slide 10–12](../evidence/TV4/TV4_Slides_10-12.pptx), đúng Vinh, từ PR #41. Giữ nguyên cạnh `Minh_chung/` để nguồn trong notes truy được.
- [TV5 slide 13–15](TV5_Slide_13-15.pptx) là deck từng phần dựng 11/10. Notes/số liệu ghi phạm vi lịch sử, không dùng nó thay deck chung cập nhật 13/10. [Ba preview cũ](preview/TV5_Slide_13.png) giữ nguồn cũ.

[Kịch bản và phiếu ghi lượt tập](../TV5_TAP_DEMO.md) vẫn để kết quả trống đến khi chạy thật. Hướng dẫn 1–5 nằm trong docs của repo; chúng không thay PPTX. Nguồn dựng/QA deck chung lưu riêng `build/tv5-final-docs-20261013/`, không phải log thực thi nghiệp vụ.
