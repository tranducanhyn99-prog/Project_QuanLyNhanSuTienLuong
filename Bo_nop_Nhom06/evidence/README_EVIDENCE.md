# Trạng thái evidence nhóm

Đóng gói TV5 ngày 11/10/2026; nhận TV4 từ PR #41 và kiểm lại ngày 13/10/2026. [TV4](TV4/README_EVIDENCE.md) và [TV5](TV5/README_EVIDENCE.md) có log/plan, phạm vi và SHA256. Những thư mục chờ không được tính là evidence đã bàn giao.

| Thành viên | Trạng thái |
|---|---|
| [TV1](TV1/CHO_BAN_GIAO.md) — Nguyễn Minh Trí, 24110359 | Chờ ERD hiện hành đủ 10 bảng và bộ log/plan do TV1 lựa chọn |
| [TV2](TV2/README_TV5_RECEIPT.md) — Phạm Minh Quân, 24110311 | Đã nhận 52 file từ PR44 head `7efd9dd`; bốn actual plans, Results/Messages/summary và slide 4–6. Canonical v3: Messages 574→5, XML 522→5; handoff v2 còn tham chiếu superseded, chưa nghiệm thu biên bản cuối |
| [TV3](TV3/CHO_BAN_GIAO.md) — Trần Tiến Đạt, 24110198 | Chờ chọn/bàn giao evidence phụ cấp/khấu trừ và benchmark |
| [TV4](TV4/README_EVIDENCE.md) — Nguyễn Quang Vinh, 24110385 | Đã nhận Word, slide 10–12, 5 ảnh, 5 log, 2 actual plans và metadata; E2E 10/10, benchmark 547→4, concurrency B 21072 ms, GUI 31 checks. Vẫn cần tập demo tay |
| [TV5](TV5/README_EVIDENCE.md) — Trần Đức Anh, 24110155 | Có object security, identity thật, A/B, bốn race, plan benchmark 4/5 và regression offline của bản package |

Chỉ file thật đã đọc/đối chiếu mới được ghi PASS. Không lấy ảnh tracked cũ làm bằng chứng runtime hiện hành. Rehearsal chung và mở package trên máy khác còn chờ.
