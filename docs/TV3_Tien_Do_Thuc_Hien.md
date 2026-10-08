# TIẾN ĐỘ THỰC HIỆN DỰ ÁN - TV3: TRẦN TIẾN ĐẠT (MSSV: 24110198)

- **Phân hệ phụ trách:** Quản lý Danh mục Phụ Cấp, Khấu Trừ & Nghiệp Vụ Bổ Trợ
- **Nhánh Git:** `feature/hoan_thien_noi_dung_tuan_3_TV3`
- **Cập nhật rà soát:** 08/10/2026. Phần thiết kế và các hạng mục TV3 được rà lại theo scripts hiện tại.
- **Tình trạng:** Các testcase TV3 trong lần chạy tích hợp 08/10/2026 đều PASS; xem log ở phần II. Kết quả chỉ phản ánh QA database và fixture của lần chạy đó.

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
| **BR04** | Không cho phép thêm, sửa, xóa phụ cấp của kỳ đã chốt sổ. | Trigger `trg_PhuCap_KhongSuaKhiDaChotLuong` trong module 05; lấy khóa `UPDLOCK, HOLDLOCK` trên kỳ cha. |
| **BR05** | Xóa kỳ lương chưa chốt phải đảm bảo xóa bảng con trước, cha sau. | Transaction `sp_XoaKyLuongChuaChot` |

---

## 📌 PHẦN II: KẾT QUẢ RÀ SOÁT HIỆN TẠI

- [x] **Function `fn_TongPhuCap`:** Được tạo trong module 03 và được `sp_TinhBangLuongThang` gọi khi tính phụ cấp; module 04 chỉ có nhánh dự phòng cộng trực tiếp nếu function vắng mặt.
- [x] **Đóng kỳ và dữ liệu nguồn:** Các trigger module 05 cho phụ cấp, khấu trừ và chấm công khóa kỳ cha bằng `UPDLOCK, HOLDLOCK`; INSERT/UPDATE/DELETE và thay đổi kỳ nguồn đều kiểm tra `inserted` lẫn `deleted`.
- [x] **Seed demo:** `database/06_Demo_Data.sql` chỉ thêm bản ghi còn thiếu trong transaction; không xóa phụ cấp/khấu trừ theo tháng. Schema modules 01–05 không tự chạy seed.
- [x] **Test module TV3:** PASS các kiểm tra function/view rỗng, lỗi ràng buộc tiền/kỳ, xóa kỳ nháp/chặn xóa kỳ chốt và cleanup fixture.
- [x] **Benchmark TV3:** Lần chạy 08/10 trên 30.000 dòng trong temp table đo 642 logical reads trước index so với 2 sau index; đây là kết quả của fixture benchmark, không phải bảo đảm hiệu năng trên dữ liệu sản xuất.

Nguồn: [log test TV3](../build/sql-verification/20261008_101955_11b4e24c/test_module_phucap_khautru_TV3.log) và [log benchmark TV3](../build/sql-verification/20261008_101955_11b4e24c/benchmark_test_benchmark_index_TV3.log). Cả hai thuộc QA database riêng đã được runner dọn sau khi chạy. Giao diện và ảnh cũ chưa được xác minh lại trong lần chạy SQL này.
