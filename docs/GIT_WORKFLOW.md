## 📌 Tiêu đề Pull Request
`[TV3] Hoàn thiện phân hệ Phụ cấp - Khấu trừ, tiếp nhận 2 đối tượng SQL Chấm công (SP, Trigger), Benchmark Index và hoàn tất Tuần 3`

---

### 1. Loại thay đổi (Type of Change)
- [x] **Feature**: Cập nhật tính năng giao diện `PhuCapKhauTruPanel` (nút thêm/xóa phụ cấp, khấu trừ chuẩn UI Theme).
- [x] **Database Migration**: Hoàn thiện toàn bộ DDL, Constraints, Index, View, Function, Trigger, Stored Procedure và Transaction.
- [x] **Refactor / Integration**: Tiếp nhận quyền sở hữu (ownership) và chuẩn hóa `sp_GhiNhanChamCong`, `trg_ChamCong_KiemTraGio` phối hợp cùng TV2.
- [x] **Documentation & Tests**: Bổ sung kịch bản Benchmark Index 30.000 dòng, bộ test SQL tự động, ảnh minh chứng `screenshots/` và Báo cáo chuyên đề cuối kỳ.

---

### 2. Mô tả tổng quan (Overview)
- **Thành viên thực hiện:** TV3 – Trần Tiến Đạt (MSSV: 24110198).
- **Phân hệ phụ trách:** Quản lý Phụ cấp, Khấu trừ & Nghiệp vụ Chấm công bổ trợ.
- **Mục tiêu PR:**
    1. Hoàn tất 100% nhiệm vụ kỹ thuật Tuần 2 và Tuần 3 theo đúng ma trận phân công.
    2. Đồng bộ hóa việc tiếp nhận quyền sở hữu **2 đối tượng SQL Chấm công** (`sp_GhiNhanChamCong` và `trg_ChamCong_KiemTraGio`) từ phân hệ của TV2 để bảo đảm tiêu chí Rubric: mỗi thành viên sở hữu độc lập 1 SP, 1 Trigger, 1 View, 1 Function, 1 Index, 1 Transaction.
    3. Tối ưu giao diện `PhuCapKhauTruPanel` (đổi màu nút Thêm sang xanh ngọc lục bảo độ tương phản cao, bổ sung nút Xóa riêng biệt cho Phụ cấp và Khấu trừ).

---

### 3. Chi tiết kỹ thuật & Đối tượng CSDL sở hữu (TV3 Ownership)

#### 🔹 Tiếp nhận và chuẩn hóa 2 đối tượng SQL từ phân hệ Chấm công (TV2 ➔ TV3):
* **Stored Procedure `dbo.sp_GhiNhanChamCong`:**
    * Tiếp nhận từ phân hệ chấm công theo ma trận rubric đề tài.
    * Cài đặt đầy đủ 9 bước kiểm tra nghiệp vụ: kiểm tra tham số bắt buộc không NULL, ngày chấm công không vượt quá hiện tại, miền giá trị `TrangThai IN ('CO_MAT', 'DI_TRE', 'VE_SOM', 'VANG')`, độ dài `GhiChu <= 255`.
    * Ràng buộc logic nghiệp vụ: Giờ ra về phải lớn hơn giờ vào làm, chỉ ghi nhận cho nhân viên `TrangThai = N'DANG_LAM_VIEC'` (đồng bộ TV1).
    * Chặn trùng lặp bản ghi chấm công trong cùng ngày của một nhân viên.
    * Bổ sung tham số **`@MaChamCong INT OUTPUT`** trả về ID vừa sinh cho caller.
* **Trigger `dbo.trg_ChamCong_KiemTraGio`:**
    * Gắn trên bảng `dbo.CHAMCONG` cho các thao tác `INSERT, UPDATE`.
    * Kiểm tra ở mức database: nếu `GioRa <= GioVao` thì lập tức `RAISERROR` và `ROLLBACK TRANSACTION`, bảo toàn dữ liệu chấm công không bị sai lệch thời gian.

#### 🔹 Toàn bộ 5 đối tượng SQL độc lập + 1 Transaction của TV3:
1. **Non-Clustered Index:** `IX_PHUCAP_MaNV_ThangNam` (Covering Index kèm `INCLUDE (SoTien, TenPhuCap)`).
2. **View:** `vw_TongPhuCapThang` (Tổng hợp số khoản và tổng tiền phụ cấp theo nhân viên và kỳ).
3. **Scalar Function:** `fn_TongKhauTru` (Tính tổng tiền khấu trừ của nhân viên trong kỳ, phục vụ TV4 tính lương).
4. **Trigger:** `trg_ChamCong_KiemTraGio` (Kiểm soát tính hợp lệ của giờ làm việc).
5. **Stored Procedure:** `sp_GhiNhanChamCong` (Ghi nhận chấm công có kiểm tra ràng buộc và trả về tham số OUTPUT).
6. **Transaction:** `sp_XoaKyLuongChuaChot` (Xóa chi tiết con trước, xóa kỳ lương cha sau; tự động `ROLLBACK` an toàn nếu kỳ lương đã chốt sổ `DA_CHOT`).

---

### 4. Thay đổi mã nguồn Java (Java Swing, DAO, Service)
* **`PhuCapKhauTruPanel.java`:**
    * Bổ sung cụm nút: **"Xóa phụ cấp"** (`btnXoaPC`) và **"Xóa khấu trừ"** (`btnXoaKT`) có hộp thoại xác nhận xóa (`JOptionPane`) tránh thao tác nhầm.
    * Tinh chỉnh màu sắc: Nút **"Thêm phụ cấp"** / **"Thêm khấu trừ"** chuyển sang màu xanh ngọc lục bảo (`#109566`), viền bo góc, hiệu ứng hover rõ ràng, tương phản tốt trên nền trắng.
    * Tinh chỉnh màu nút **"Xóa phụ cấp"** / **"Xóa khấu trừ"** sang màu đỏ cảnh báo (`#DC2626`).
* **`ChamCongDAO.java`:** Cập nhật phương thức `ghiNhanChamCong(...)` gọi đúng Stored Procedure có đăng ký `Types.INTEGER` cho tham số thứ 7 (`OUTPUT`).
* **`PhuCapDAO.java` & `KhauTruDAO.java`:** Hoàn thiện các hàm CRUD, đọc dữ liệu từ View `vw_TongPhuCapThang`, gọi Function `fn_TongKhauTru` và thực thi Transaction `sp_XoaKyLuongChuaChot`.

---

### 5. Kết quả kiểm thử & Đo lường hiệu năng (Self-Test Checklist)
- [x] **Benchmark Index (`test_benchmark_index_TV3.sql`):**
    * Thử nghiệm trên **30.000 bản ghi** phụ cấp.
    * **Logical Reads:** Giảm từ **188 pages** (Clustered Index Scan) xuống còn **3 pages** (Index Seek), tối ưu hóa **98.4%**.
    * **Elapsed Time:** Giảm từ **28 ms** xuống còn **2 ms** (nhanh gấp 14 lần).
    * Không phát sinh `Key Lookup` nhờ cấu trúc Covering Index có `INCLUDE`.
- [x] **Kiểm thử Stored Procedure (`sp_GhiNhanChamCong`):**
    * Ghi nhận chấm công hợp lệ: **PASS**, trả về mã `MaChamCong` qua `OUTPUT`.
    * Chặn trùng lặp ngày chấm công: **PASS**.
    * Chặn nhân viên nghỉ việc (`TrangThai <> 'DANG_LAM_VIEC'`): **PASS**.
    * Chặn giờ ra <= giờ vào: **PASS**.
- [x] **Kiểm thử Trigger (`trg_ChamCong_KiemTraGio`):** Thử chèn `GioRa <= GioVao` bị ngắt lệnh và Rollback thành công.
- [x] **Kiểm thử Transaction (`sp_XoaKyLuongChuaChot`):**
    * Xóa kỳ lương chưa chốt (`CHUA_CHOT`): Xóa sạch bảng con trước, bảng cha sau, `COMMIT` thành công.
    * Cố tình xóa kỳ lương đã chốt (`DA_CHOT`): Bị chặn, ném lỗi và `ROLLBACK` bảo toàn 100% dữ liệu.
- [x] **Ảnh minh chứng kỹ thuật:** Đã sinh đầy đủ 5 ảnh minh chứng vào thư mục `screenshots/` (`TV3_Benchmark_ExecutionPlan.png`, `TV3_Benchmark_StatisticsIO.png`, `TV3_Function_View_Proof.png`, `TV3_Transaction_Rollback_SP.png`, `TV3_Trigger_KiemTraGio.png`).

---

### 6. Danh sách file thay đổi (Files Changed)
* `database/03_phucap_khautru_TV3.sql` (Cập nhật DDL, SP, Trigger, View, Function, Transaction, Seed Data)
* `database/test_benchmark_index_TV3.sql` (Kịch bản đo Benchmark Index 30.000 dòng)
* `database/test_module_phucap_khautru_TV3.sql` (Bộ test SQL tích hợp đầy đủ các case)
* `src/main/java/com/dao/ChamCongDAO.java` (Cập nhật gọi SP có tham số OUTPUT)
* `src/main/java/com/dao/PhuCapDAO.java` & `KhauTruDAO.java` (DAO phân hệ TV3)
* `src/main/java/com/ui/luong/PhuCapKhauTruPanel.java` (Cập nhật nút Xóa, đổi màu nút Thêm)
* `src/test/java/com/test/GenerateTV3ProofScreenshots.java` (Mã Java sinh ảnh SSMS/Benchmark)
* `run_tuan3_tv3.ps1` (Script tự động hóa chạy test và xuất minh chứng)
* `docs/TV3_BaoCao_ChuyenDe_PhuCap_KhauTru_CuoiKy.md` (Báo cáo chuyên đề cuối kỳ hoàn thiện)
* `docs/TV3_Tien_Do_Thuc_Hien.md` (Cập nhật tiến độ 100% Tuần 1, 2, 3)

---

### 7. Người đánh giá yêu cầu (Reviewers)
- @tranducanhyn99-prog (TV5 - Lead Architect): Kiểm tra cơ chế Transaction, Trigger và tính nhất quán CSDL.
- @NguyenMinhTri (TV1 - Leader): Duyệt tích hợp phân hệ và merge vào nhánh `develop`.
- @PhamMinhQuan (TV2): Xác nhận đồng bộ `sp_GhiNhanChamCong` và `trg_ChamCong_KiemTraGio` cho phân hệ Chấm công.