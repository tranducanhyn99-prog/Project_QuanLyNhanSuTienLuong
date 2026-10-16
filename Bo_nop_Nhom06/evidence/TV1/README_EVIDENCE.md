# TV1 — Bộ bàn giao minh chứng và tài liệu

**Thành viên:** Nguyễn Minh Trí — MSSV: **24110359**  
**Mã phân công:** TV1 (Nhóm 06 — DBMS330284)  
**Ngày cập nhật bàn giao:** 15/10/2026.  
**Môi trường kiểm thử:** Máy Windows; Microsoft SQL Server 2025 RTM 17.0.1000.7 / Standard Developer Edition tại localhost.

---

## 1. Phạm vi trách nhiệm và quyền sở hữu (Ownership)

* **Thiết kế CSDL & Phân tích:**
  * Sơ đồ thực thể liên kết (ERD) hoàn thiện chuẩn hóa gồm **10 bảng** nghiệp vụ (bao gồm bảng `LICHSULUONG`, các trường mở rộng `NHANVIEN.NgayNghiViec`, `TAIKHOAN.SqlLogin`).
  * Đặc tả chi tiết lược đồ quan hệ, xác định rõ quan hệ $1 - N$ giữa `NHANVIEN` và `TAIKHOAN` (do trường `MaNV` tại bảng `TAIKHOAN` là nullable và không áp đặt ràng buộc UNIQUE).
  * Chuẩn hóa dữ liệu toàn hệ thống từ UNF $\rightarrow$ 1NF $\rightarrow$ 2NF $\rightarrow$ 3NF, chứng minh triệt tiêu dị thường dữ liệu.
* **Module Nhân sự (Java Swing & SQL Server):**
  * Giao diện người dùng: `NhanVienPanel` (tra cứu, quản lý hồ sơ nhân viên), `DanhMucPanel` (quản lý phòng ban, chức vụ).
  * Các tầng dịch vụ và truy xuất dữ liệu: `NhanVienService`, `NhanVienDAO`, `DanhMucDAO`.
* **Ma trận đối tượng CSDL phụ trách:**
  * **Stored Procedure:** `sp_ThemNhanVien` (tiếp nhận thông tin nhân sự và tài khoản, sinh mã tự động).
  * **Function:** `fn_TinhSoNgayCong` (tính tổng số ngày công hợp lệ của nhân viên theo tháng/năm, cài đặt tại module 02).
  * **Trigger:** `trg_NhanVien_KhongXoaKhiDaPhatSinhLuong` (chặn xóa cứng nhân viên khi đã có bản ghi chấm công hoặc bảng lương, cài đặt tại module 05).
  * **View:** `vw_NhanVien_PhongBan_ChucVu` (kết hợp thông tin nhân viên, phòng ban, chức vụ và tài khoản).
  * **Index:** `IX_NHANVIEN_HoTen` (Non-clustered Index trên cột `HoTen` kết hợp mệnh đề `INCLUDE` 5 cột phục vụ tìm kiếm nhanh).
  * **Transaction:** *Tạo nhân viên + tài khoản* (đảm bảo tính toàn vẹn ACID, rollback hoàn toàn khi tạo tài khoản hoặc hồ sơ thất bại).

---

## 2. Tài liệu báo cáo và Slide thuyết trình TV1

* [Word phần TV1](TV1_Phan_Bao_Cao.docx): Báo cáo chi tiết phân hệ TV1 gồm đặc tả bảng `PHONGBAN`, `CHUCVU`, `NHANVIEN`, `LICHSULUONG`, `TAIKHOAN`, sơ đồ ERD 10 bảng, chứng minh 3NF, phân tích Stored Procedure `sp_ThemNhanVien`, Trigger `trg_NhanVien_KhongXoaKhiDaPhatSinhLuong`, Function `fn_TinhSoNgayCong`, Covering Index `IX_NHANVIEN_HoTen` và bảng tổng hợp kết quả kiểm thử/benchmark kèm ảnh minh chứng.
* [Slide 1–3](TV1_Slides_1-3.pptx): Slide thuyết trình phân hệ TV1 (16:9 widescreen, thiết kế card chuẩn PeopleOS đồng bộ hoàn toàn với TV3, TV4) kèm đầy đủ Speaker Notes phân tích thời gian và kịch bản thuyết trình chi tiết.
* Bản sao slide cũng được lưu tại [Bo_nop_Nhom06/slides/TV1_Slides_1-3.pptx](../../slides/TV1_Slides_1-3.pptx).
* [Báo cáo chuyên đề Nhân sự cuối kỳ](../../../docs/TV1_BaoCao_ChuyenDe_NhanSu_CuoiKy.md): Báo cáo chi tiết toàn diện về phân tích thiết kế CSDL, các ràng buộc toàn vẹn, phân tích 3NF và tối ưu hóa truy vấn module nhân sự.
* [Hướng dẫn thuyết trình & Vấn đáp TV1](../../../docs/TV1_Huong_Dan_Thuyet_Trinh.md): Kịch bản trình bày, hệ thống câu hỏi vấn đáp và phân tích kỹ thuật dành cho TV1.

---

## 3. Danh mục minh chứng thực tế (Minh_chung)

Thông tin chi tiết về môi trường và quy trình kiểm thử được ghi nhận tại [THONG_TIN_MINH_CHUNG.txt](Minh_chung/THONG_TIN_MINH_CHUNG.txt).

| Hạng mục | Tập tin minh chứng | Kết quả và phạm vi xác minh |
|---|---|---|
| **ERD 10 bảng hiện hành** | [TV1_Database_Diagram.png](Minh_chung/TV1_Database_Diagram.png) | Sơ đồ Database Diagram trích xuất trực tiếp từ SSMS; đầy đủ 10 bảng, có `LICHSULUONG`, `NHANVIEN.NgayNghiViec`, `TAIKHOAN.SqlLogin`, đúng quan hệ 1-N của tài khoản. |
| **Kiểm thử Module Nhân sự** | [test_module_nhansu_TV1.log](Minh_chung/test_module_nhansu_TV1.log) | **7/7 PASS tuyệt đối**: Kiểm tra tạo nhân viên/profile, ràng buộc tuổi chính xác theo ngày sinh nhật `NgaySinh <= DATEADD(YEAR, -18, NgayVaoLam)` (đã kiểm tra 9 ca biên trong `TV1_Age_Boundaries.sql`), rollback khi trùng username, lưu trữ ngày vào làm, trigger bảo vệ xóa nhân viên, hàm tính công, view và index. Dọn dẹp fixture sạch sẽ. |
| **Benchmark Index IO/TIME** | [benchmark_test_benchmark_index_TV1.log](Minh_chung/benchmark_test_benchmark_index_TV1.log) | Fixture `#NhanVienBenchmark` 5.000 dòng độc lập. Cùng lượt chạy duy nhất với Actual Plan, logical reads giảm từ **75 reads (Clustered Scan)** xuống **10 reads (Index Seek covering)** $\rightarrow$ **Giảm 86.67% I/O**, triệt tiêu 100% Key Lookup nhờ `INCLUDE` 5 cột. Cùng trả về 500 dòng. |
| **Actual Execution Plan (XML)** | [TV1_Benchmark_ActualPlan.sqlplan](Minh_chung/TV1_Benchmark_ActualPlan.sqlplan) | ShowPlanXML từ cùng một lượt chạy duy nhất với log trên SQL Server 2025: Clustered Scan (`ActualLogicalReads = 75`, Cost: 87.8%) vs Index Seek covering (`ActualLogicalReads = 10`, Cost: 12.2%), `ActualRows = 500`, chuỗi `N'Nguyễn Minh%'` UTF-8 chuẩn xác. |
| **Ảnh minh chứng SSMS lịch sử** | [TV1_Benchmark_StatisticsIO.png](Minh_chung/TV1_Benchmark_StatisticsIO.png),<br>[TV1_Benchmark_ExecutionPlan.png](Minh_chung/TV1_Benchmark_ExecutionPlan.png) | Ảnh chụp màn hình lịch sử (ngày 30/09/2026 trên bảng `NHANVIEN` mẫu ban đầu 4 dòng, 2 reads) minh họa giao diện SSMS khi phân biệt Clustered Scan vs Index Seek trong giai đoạn phát triển ban đầu; không dùng làm bằng chứng số liệu cho lượt benchmark 5.000 dòng. |
| **Transaction Rollback** | [TV1_Transaction_Rollback_SP.png](Minh_chung/TV1_Transaction_Rollback_SP.png) | Ghi nhận lỗi trùng lặp tài khoản và rollback toàn bộ transaction tạo nhân viên, không để lại bản ghi rác trong CSDL. |
| **Trigger Chặn xóa** | [TV1_Trigger_ChanXoaNhanVien.png](Minh_chung/TV1_Trigger_ChanXoaNhanVien.png) | Thông báo lỗi nghiệp vụ từ trigger khi cố tình xóa nhân viên đã có dữ liệu chấm công. |
| **Giao diện Danh mục** | [TV1_DanhMuc_PhongBan_ChucVu.png](Minh_chung/TV1_DanhMuc_PhongBan_ChucVu.png) | Màn hình Quản lý Danh mục Phòng ban & Chức vụ (PeopleOS Light). |
| **Giao diện Hồ sơ Nhân viên** | [TV1_NhanVien_HoSo.png](Minh_chung/TV1_NhanVien_HoSo.png),<br>[TV1_NhanVien_HoSo_ChiTiet.png](Minh_chung/TV1_NhanVien_HoSo_ChiTiet.png) | Màn hình Quản lý danh sách và Form chi tiết nhân viên trên ứng dụng Java Swing. |

---

## 4. Kiểm tra tính toàn vẹn (Integrity Check)

Toàn bộ các tập tin trong gói bàn giao của TV1 được kiểm tra mã băm SHA-256 độc lập tại [SHA256_TV1.csv](SHA256_TV1.csv).
