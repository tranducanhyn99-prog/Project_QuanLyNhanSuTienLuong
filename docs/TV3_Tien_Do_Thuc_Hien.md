# TIẾN ĐỘ THỰC HIỆN DỰ ÁN - TV3: TRẦN TIẾN ĐẠT (MSSV: 24110198)

- **Phân hệ phụ trách:** Quản lý Danh mục Phụ Cấp, Khấu Trừ & Nghiệp Vụ Bổ Trợ
- **Nhánh Git:** `feature/hoan_thien_noi_dung_tuan_3_TV3`
- **Tình trạng nghiệm thu:** Hoàn thành 100% (Tuần 1, Tuần 2, Tuần 3)

---

## 📌 PHẦN I: THIẾT KẾ DỮ LIỆU & QUY TẮC NGHIỆP VỤ (TUẦN 1 & 2)

### 1. Phân tích chuẩn hóa 3NF
- **Bảng `PHUCAPNHANVIEN`:** `(MaPCNV, MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan, GhiChu)`.
    - Khóa chính đơn `MaPCNV` (1NF).
    - Không có thuộc tính nào phụ thuộc một phần vào khóa chính (2NF).
    - Không có phụ thuộc bắc cầu giữa các thuộc tính không khóa; thông tin nhân viên (`HoTen`, `PhongBan`) được tham chiếu qua `FK_PHUCAP_NHANVIEN` (3NF).
- **Bảng `KHAUTRUNHANVIEN`:** `(MaKTNV, MaNV, Thang, Nam, TenKhauTru, SoTien, NgayGhiNhan, LyDo)` đạt chuẩn 3NF tương tự.

### 2. Bảng quy tắc nghiệp vụ cốt lõi (Business Rules)
| Mã quy tắc | Quy tắc nghiệp vụ | Hiện thực kỹ thuật |
|---|---|---|
| **BR01** | Số tiền phụ cấp và khấu trừ không được âm. | `CK_PHUCAP_SoTien`, `CK_KHAUTRU_SoTien CHECK (SoTien >= 0)` |
| **BR02** | Tháng phát sinh phải từ 1 đến 12; năm từ 2020 trở đi. | `CHECK (Thang BETWEEN 1 AND 12 AND Nam >= 2020)` |
| **BR03** | Khóa ngoại tham chiếu nhân viên hợp lệ, không cho phép mồ côi. | `FK_PHUCAP_NHANVIEN`, `FK_KHAUTRU_NHANVIEN` (`ON DELETE NO ACTION`) |
| **BR04** | Tuyệt đối không cho phép chỉnh sửa/xóa phụ cấp của kỳ đã chốt sổ. | Trigger `trg_PhuCap_KhongSuaKhiDaChotLuong` (Rollback) |
| **BR05** | Xóa kỳ lương chưa chốt phải đảm bảo xóa bảng con trước, cha sau. | Transaction `sp_XoaKyLuongChuaChot` |

---

## 📌 PHẦN II: TỔNG KẾT HOÀN THÀNH TUẦN 3

- [x] **Benchmark Index (`test_benchmark_index_TV3.sql`):** Đo lường trên 30.000 dòng. Logical reads giảm từ 188 xuống 3 pages (tối ưu hóa 98.4%).
- [x] **Bộ kiểm thử SQL (`test_module_phucap_khautru_TV3.sql`):** Toàn bộ 6 nhóm test đều PASS.
- [x] **Hoàn thiện UI (`PhuCapKhauTruPanel.java`):** Đã sửa lỗi nút tàng hình, dùng `UITheme.stylePrimaryButton` và `UITheme.styleDangerButton`.
- [x] **Minh chứng hình ảnh:** Lưu trữ đầy đủ tại `screenshots/TV3/`.