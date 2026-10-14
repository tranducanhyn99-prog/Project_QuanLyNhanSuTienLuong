# TV2 — CHECKLIST DEMO GIAO DIỆN CHẤM CÔNG (BẢN HANDOFF CẬP NHẬT)

- **Người thực hiện:** Phạm Minh Quân (TV2 — MSSV: 24110311)
- **Tài khoản sử dụng:** `gui_hr` (tra mật khẩu tại `database/demo_accounts.json`, tuyệt đối không chiếu mật khẩu lên màn hình).
- **Căn cứ kiểm chứng:** Báo cáo GUI 09/10/2026 (`docs/GUI_TEST_REPORT_20261009.md`), các ca G06, G07, G12 đã PASS.

---

## A. Lưu ý Đặc biệt Về Dữ Liệu Kiểm Thử (QA Context Warning)

> **Cảnh báo cốt lõi:**
> - Fixture demo 10/2026 (Nhân viên A, `gui-a@example.invalid`, `MaNV 1008`, mã chấm công 1013 ngày 09/10/2026) thuộc về môi trường **QA lịch sử (`PRJ_Fix_QA_20261007_01`)**.
> - Database QA ủy quyền hiện hành (`PRJ_Fix_QA_20261013_001534_ae185d81`) là môi trường kiểm chuẩn sạch: bảng `dbo.CHAMCONG` hiện có đúng **0 bản ghi** và chưa được nạp dữ liệu kiểm thử GUI 10/2026.
> - **Quy tắc an toàn bảo vệ đồ án:**
>   1. Tuyệt đối **KHÔNG** tự ý chạy script seed, insert hay thay đổi dữ liệu trong database QA ủy quyền hiện hành.
>   2. Tuyệt đối **KHÔNG** thay đổi cấu hình kết nối ứng dụng hoặc đổi quyền tài khoản.
>   3. Nếu buổi bảo vệ diễn ra trên môi trường QA đã được giảng viên/nhóm chuẩn bị sẵn dữ liệu 10/2026, thực hiện theo checklist Mục B.
>   4. Nếu môi trường không có sẵn dữ liệu, **KHÔNG tuyên bố demo live**, mà trình bày báo cáo kiểm thử GUI 09/10 và log tự động 21/21 PASS (`test_module_chamcong_TV2.log`).

---

## B. Các Bước Thao Tác Demo (Khi có môi trường dữ liệu chuẩn)

| Bước | Màn hình / Thao tác | Hành vi kỳ vọng trên giao diện | Ghi chú & Lời nói |
|:---:|---|---|---|
| **1** | Mở phân hệ **Chấm công ➔ Nhật ký chấm công** | Giao diện nạp danh sách, spinner kỳ hiển thị Tháng/Năm | "Em mở màn hình Nhật ký chấm công." |
| **2** | Chọn Tháng **10**, Năm **2026** | Change listener tự động nạp lại danh sách công tháng 10/2026 | "Hệ thống tự động lọc các bản ghi công theo kỳ lựa chọn." |
| **3** | Quan sát dòng công của nhân viên A (ngày 09/10/2026, 08:00–17:00, `CO_MAT`) | Hiển thị đúng 1 dòng công, trạng thái mã `CO_MAT` | "Mỗi nhân viên chỉ có duy nhất một dòng công trong ngày — khớp quy tắc BR-CC-01." |
| **4** | Chuyển sang tab **Tổng hợp theo tháng**, bấm **Xem tổng hợp** | Bảng hiển thị 1 dòng của nhân viên A: 1 ngày đi làm, 9.00 giờ làm, đi trễ/về sớm/vắng = 0 | "Màn hình này đọc dữ liệu trực tiếp từ view `vw_TongHopChamCongThang`." |
| **5** | Dừng thao tác, chuyển giao cho TV3 | Không thao tác thêm, không bấm ghi nhận mới | Bàn giao cho bạn Trần Tiến Đạt trình bày phân hệ Phụ cấp & Khấu trừ. |

---

## C. Kịch Bản Kiểm Tra Ràng Buộc Trùng Ngày (Chỉ làm khi Giảng viên yêu cầu)

- **Điều kiện tiên quyết:** Kỳ 10/2026 đang ở trạng thái `CHUA_CHOT`.
- **Thao tác:**
  1. Tại form ghi nhận chấm công, chọn nhân viên A.
  2. Chọn ngày `2026-10-09` (ngày đã có bản ghi).
  3. Chọn giờ vào `08:00`, giờ ra `17:00`, trạng thái `CO_MAT`.
  4. Bấm **Ghi nhận chấm công**.
- **Kết quả kỳ vọng:**
  - Hộp thoại cảnh báo xuất hiện với thông báo từ Stored Procedure: *"Nhân viên đã có bản ghi chấm công trong ngày này!"*.
  - Bản ghi gốc (mã CC 1013) được giữ nguyên vẹn, không có bản ghi thứ hai nào được chèn vào CSDL.
- **Tuyên bố bảo vệ:** "Đây là ca thử nghiệm kiểm tra tính toàn vẹn của ràng buộc UNIQUE và thủ tục `sp_GhiNhanChamCong`, CSDL đã từ chối thành công dữ liệu trùng lặp."

---

## D. Phương Án Fallback Bằng SQL Read-Only (Khi không dùng GUI)

Chạy khối lệnh SELECT sau trên database QA ủy quyền (chỉ đọc, tuyệt đối không chạy lệnh DML/DDL):

```sql
-- Xác minh dữ liệu công và view tổng hợp
SELECT MaChamCong, MaNV, NgayChamCong, GioVao, GioRa, TrangThai 
FROM dbo.CHAMCONG 
WHERE NgayChamCong BETWEEN '2026-10-01' AND '2026-10-31';

SELECT * 
FROM dbo.vw_TongHopChamCongThang 
WHERE Thang = 10 AND Nam = 2026;
```

---

## E. Những Điều Tuyệt Đối Cấm Trong Quá Trình Demo

1. **CẤM** tuyên bố đã demo tay nghiệp vụ nhập lô trên GUI (nghiệp vụ này mới kiểm thử tự động ở mức transaction CSDL đạt 21/21 PASS).
2. **CẤM** lấy 5 ảnh chụp benchmark cũ hoặc 3 ảnh UI kỳ 09/2026 làm bằng chứng trực tiếp hiện hành.
3. **CẤM** tự ý thêm dữ liệu hoặc đổi ngày để "lách" lỗi vi phạm nghiệp vụ.
4. **CẤM** để lộ mật khẩu kết nối hoặc mật khẩu tài khoản người dùng trên màn hình chiếu.
