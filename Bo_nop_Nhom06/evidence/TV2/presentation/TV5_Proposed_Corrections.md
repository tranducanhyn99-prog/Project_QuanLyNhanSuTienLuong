# TV5 — ĐỀ XUẤT CẬP NHẬT BÁO CÁO VÀ DECK CHO TRẦN ĐỨC ANH

- **Người gửi:** Phạm Minh Quân (TV2 — 24110311)
- **Người nhận:** Trần Đức Anh (TV5 — Trưởng nhóm / Tổng hợp báo cáo & Slide)
- **Ngày lập:** 13/10/2026
- **Ghi chú:** Các nội dung dưới đây là đề xuất sẵn văn bản và bảng số liệu để TV5 thuận tiện copy vào Báo cáo cuối kỳ (file Word) và Slide chung của nhóm mà không cần tính toán lại. TV2 không tự ý sửa các file tracked.

---

## 1. Cập nhật Bảng Benchmark Phân hệ Chấm công (§3.2 Báo cáo cuối kỳ)

Thay thế bảng số liệu cũ (574 ➔ 5 gắn nhãn chờ đo lường) bằng bảng kết quả chính thức đã được xác thực trên database QA ủy quyền `PRJ_Fix_QA_20261013_001534_ae185d81`:

### Bảng đề xuất đưa vào Word:

| STT | Kịch bản truy vấn | Baseline (Ràng buộc UNIQUE) | Đo lường (Covering Index) | Số trang đọc logic Before | Số trang đọc logic After | Tỷ lệ giảm I/O | Thời gian CPU / Thực thi |
|:---:|---|---|---|---:|---:|:---:|:---:|
| 1 | **Truy vấn chi tiết (1.1 ➔ 3.1):** Tra cứu công 9 tháng nhân viên 25 (273 bản ghi) | `UQ_CHAMCONG_MaNV_Ngay` (Index Seek + Key Lookup) | `IX_CHAMCONG_MaNV_Ngay` (Pure Index Seek, 0 Key Lookup) | **574** | **5** | **99.13%** | 15ms / 31ms ➔ 0ms / 2ms |
| 2 | **Truy vấn tổng hợp (1.2 ➔ 3.2):** Tổng hợp công & giờ làm theo tháng (9 bản ghi) | `UQ_CHAMCONG_MaNV_Ngay` (Seek + Lookup + Stream Aggregate) | `IX_CHAMCONG_MaNV_Ngay` (Seek + Stream Aggregate, 0 Lookup) | **574** | **5** | **99.13%** | 0ms / 0ms ➔ 0ms / 0ms |

### Đoạn văn giải trình kỹ thuật đề xuất đưa vào Báo cáo:
> "Trong phân hệ Chấm công, bảng `CHAMCONG` vốn đã có ràng buộc duy nhất `UQ_CHAMCONG_MaNV_Ngay` trên `(MaNV, NgayChamCong)` để phục vụ nghiệp vụ kiểm soát một nhân viên chỉ có một bản ghi mỗi ngày. Ràng buộc này tự động tạo một Non-clustered Index tìm kiếm theo khóa. Do đó, việc so sánh hiệu năng giữa Covering Index với một bảng không có index (Table Scan) là phi thực tế. Baseline chuẩn mực là so sánh giữa Index Seek có Key Lookup và Pure Index Seek không Key Lookup.
>
> Thực nghiệm trên tập dữ liệu định chế 30.000 bản ghi cho thấy: Với truy vấn chi tiết 273 ngày công của nhân viên 25, kế hoạch ban đầu phải thực hiện 273 lần Key Lookup về Clustered Index, tiêu tốn **574 logical reads** (gồm 520 trang lá dữ liệu clustered, 2 trang lá index phụ và các trang dẫn đường b-tree). Khi áp dụng covering index `IX_CHAMCONG_MaNV_Ngay` với mệnh đề `INCLUDE (GioVao, GioRa, TrangThai)`, toàn bộ dữ liệu cần thiết nằm trọn trên lá của index phụ, loại bỏ hoàn toàn 273 lần Key Lookup. Số lượt đọc logic giảm mạnh còn đúng **5 trang** — đạt hiệu suất cắt giảm **99.13% I/O**."

---

## 2. Bổ sung Danh mục Hàm Phân công cho TV2 (Chương 2 Báo cáo cuối kỳ)

Để đảm bảo đáp ứng đầy đủ tiêu chí rubric "≥5 Function chia đều cho các thành viên trong nhóm", đề xuất TV5 bổ sung hàm sau vào phần danh mục đối tượng của TV2:

- **Tên hàm:** `dbo.fn_TongPhuCap(@MaNV INT, @Thang INT, @Nam INT)`
- **Loại hàm:** Scalar User-Defined Function (Hàm vô hướng)
- **Vị trí script:** `database/03_phucap_khautru_TV3.sql` (dòng 214–224)
- **Nghiệp vụ:** Tiếp nhận mã nhân viên, tháng, năm; tính tổng số tiền phụ cấp của nhân viên đó trong kỳ từ bảng `PHUCAPNHANVIEN` và trả về `ISNULL(SUM(SoTien), 0)`.
- **Liên kết phân hệ:** Được thủ tục tính lương tháng `sp_TinhBangLuongThang` (TV4) gọi qua dynamic SQL có kiểm tra `OBJECT_ID`.

---

## 3. Cập nhật Slide 4–6 trong Deck tổng thể của Nhóm (Trần ≤15 Slide)

1. **Slide 4 (Nghiệp vụ & Ràng buộc):**
   - Đảm bảo ghi rõ ràng buộc: `PK_CHAMCONG` (`PRIMARY KEY CLUSTERED`), `UQ_CHAMCONG_MaNV_Ngay` (`UNIQUE NONCLUSTERED`), `CHK_CHAMCONG_Ngay`, `CHK_CHAMCONG_TrangThai`.
   - Trạng thái công ghi mã nghiệp vụ: `CO_MAT, DI_TRE, VE_SOM, VANG` (kèm chú thích tiếng Việt: Có mặt, Đi trễ, Về sớm, Vắng).
2. **Slide 5 (SP, Trigger, Giao dịch nhập lô):**
   - Nêu rõ Stored Procedure không mở transaction riêng để nhường quyền điều phối All-or-Nothing cho Java Service.
   - Nhắc đến 3 trigger: 2 trigger module 02 (kiểm tra giờ, kiểm tra trạng thái nhân viên) và trigger module 05 (chặn sửa khi kỳ lương đã chốt).
3. **Slide 6 (View, Covering Index, Nối sang Lương):**
   - Đưa trực tiếp số liệu benchmark: `574 ➔ 5 logical reads (giảm 99.13%)`.
   - Giải thích ngắn gọn: Loại bỏ hoàn toàn Key Lookup nhờ mệnh đề `INCLUDE (GioVao, GioRa, TrangThai)`.
   - Khẳng định ranh giới: Phân hệ Lương đếm trực tiếp bảng CHAMCONG trong transaction có khóa `HOLDLOCK`, không gọi `fn_TinhSoNgayCong` (hàm của TV1).
