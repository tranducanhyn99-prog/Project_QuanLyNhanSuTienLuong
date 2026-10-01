# TIẾN ĐỘ THỰC HIỆN DỰ ÁN - TV3: TRẦN TIẾN ĐẠT (MSSV: 24110198)

## Tuần 1: Thiết Kế Dữ Liệu & Đặc Tả Nghiệp Vụ (Đạt 100%)
- [x] Đặc tả nghiệp vụ phụ cấp, khấu trừ theo kỳ tháng/năm.
- [x] Tạo bảng `PHUCAPNHANVIEN` và `KHAUTRUNHANVIEN` chuẩn 3NF, kiểu `MaNV INT`.
- [x] Thêm các CHECK Constraint `SoTien >= 0`, `Thang 1..12`, `Nam >= 2020`.
- [x] Chuẩn bị kịch bản DDL và bộ dữ liệu mẫu ban đầu.

## Tuần 2: Cài Đặt SQL Objects & Java Swing Module (Đạt 100%)
- [x] Cài đặt View `vw_TongPhuCapThang` tổng hợp phụ cấp theo nhân viên/kỳ.
- [x] Cài đặt Function `fn_TongKhauTru` tính tổng tiền khấu trừ của nhân viên.
- [x] Cài đặt Trigger `trg_ChamCong_KiemTraGio` kiểm tra giờ ra > giờ vào.
- [x] Cài đặt Stored Procedure `sp_GhiNhanChamCong` phối hợp TV2.
- [x] Cài đặt Transaction `sp_XoaKyLuongChuaChot` (xóa bảng con trước, bảng cha sau, rollback khi đã chốt).
- [x] Xây dựng các lớp Model: `PhuCapNhanVien`, `KhauTruNhanVien`.
- [x] Xây dựng các lớp DAO: `PhuCapDAO`, `KhauTruDAO`, bổ sung `ChamCongDAO`.
- [x] Xây dựng `PhuCapKhauTruService` và giao diện `PhuCapKhauTruPanel` trên `MainFrame`.

## Tuần 3: Benchmark Index, Tích Hợp & Báo Cáo Cuối Kỳ (Đạt 100%)
- [x] Xây dựng kịch bản benchmark `database/test_benchmark_index_TV3.sql` trên 30.000 dòng.
- [x] Đo lường Actual Execution Plan và `STATISTICS IO/TIME`: Logical reads giảm 98.4%.
- [x] Chạy bộ test case toàn diện `database/test_module_phucap_khautru_TV3.sql` (100% PASS).
- [x] Tạo mã Java xuất ảnh minh chứng kỹ thuật `GenerateTV3ProofScreenshots.java`.
- [x] Xuất đủ 5 ảnh minh chứng vào `screenshots/`.
- [x] Tạo kịch bản tự động hóa PowerShell `run_tuan3_tv3.ps1`.
- [x] Hoàn thiện báo cáo chuyên đề cuối kỳ `docs/TV3_BaoCao_ChuyenDe_PhuCap_KhauTru_CuoiKy.md`.