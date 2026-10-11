# Trạng thái evidence nhóm

Đóng gói 11/10/2026. [TV5](TV5/README_EVIDENCE.md) có bản sao log/plan gốc, phạm vi và SHA256. Những thư mục chờ không được tính là evidence đã bàn giao.

| Thành viên | Trạng thái |
|---|---|
| [TV1](TV1/CHO_BAN_GIAO.md) — Nguyễn Minh Trí, 24110359 | Chờ ERD hiện hành đủ 10 bảng và bộ log/plan do TV1 lựa chọn |
| [TV2](TV2/CHO_BAN_GIAO.md) — Phạm Minh Quân, 24110311 | Chờ benchmark CHAMCONG mới, Results/Messages/4 actual plan/summary; không dùng số lịch sử 574→5 |
| [TV3](TV3/CHO_BAN_GIAO.md) — Trần Tiến Đạt, 24110198 | Chờ chọn/bàn giao evidence phụ cấp/khấu trừ và benchmark |
| [TV4](TV4/CHO_BAN_GIAO.md) — Nguyễn Quang Vinh, 24110385 | Chờ chọn/bàn giao E2E/benchmark và plan đúng phép đo; A/B dùng chung đã có ở TV5 |
| [TV5](TV5/README_EVIDENCE.md) — Trần Đức Anh, 24110155 | Có object security, identity thật, A/B, bốn race, plan benchmark 4/5 và regression offline của bản package |

Chỉ file thật đã đọc/đối chiếu mới được ghi PASS. Không lấy ảnh tracked cũ làm bằng chứng runtime hiện hành. Rehearsal chung và mở package trên máy khác còn chờ.
