# YÊU CẦU SỬA ĐỔI, ĐỒNG BỘ VÀ HOÀN THIỆN NỘI DUNG TUẦN 3 (TV3)

- **Thành viên phụ trách:** Trần Tiến Đạt (TV3 - MSSV: 24110198)
- **Phân hệ:** Quản lý Danh mục Phụ Cấp, Khấu Trừ & Nghiệp Vụ Liên Quan
- **Người đánh giá / Reviewer:** Trần Đức Anh (TV5 - Lead Architect) & Nguyễn Minh Trí (TV1 - Leader)
- **Đối tượng rà soát:** [Pull Request #28 (`feature/hoan_thien_noi_dung_tuan_3_TV3`)](https://github.com/tranducanhyn99-prog/Project_QuanLyNhanSuTienLuong/pull/28)
- **Ngày ban hành:** 02/10/2026

---

## 📌 I. TỔNG QUAN ĐÁNH GIÁ PULL REQUEST #28

| Tiêu chí đánh giá | Trạng thái | Đánh giá sơ bộ |
|---|:---:|---|
| **Kịch bản Benchmark Index (`test_benchmark_index_TV3.sql`)** | **ĐẠT (Tốt)** | Đo lường tốt trên 30.000 dòng, chứng minh giảm 98.4% I/O và chuyển sang Index Seek. |
| **Bảo vệ toàn vẹn dữ liệu giao diện Swing** | **ĐẠT** | Đã thêm hộp thoại xác nhận `JOptionPane.showConfirmDialog` và kiểm tra chọn dòng. |
| **Quyền sở hữu đối tượng CSDL (Rubric Ownership)** | ⚠️ **XUNG ĐỘT** | Đang sở hữu trùng lặp đối tượng của TV2 (`sp_GhiNhanChamCong`, `trg_ChamCong_KiemTraGio`). |
| **Bộ kiểm thử SQL (`test_module_phucap_khautru_TV3.sql`)** | ❌ **LỖI CHƯA PASS** | Ảnh minh chứng bị in `LỖI 5.3`; Test 4 bị CHECK chặn trước; Test 6 sai schema chuẩn. |
| **Hiển thị giao diện Java Swing (`PhuCapKhauTruPanel.java`)** | ❌ **LỖI NGHIÊM TRỌNG** | Chữ các nút Thêm/Xóa bị màu trắng trên nền trắng (tàng hình) trên Windows Look & Feel. |
| **Hồ sơ tài liệu (`TV3_Tien_Do_Thuc_Hien.md`)** | ⚠️ **THIẾU SÓT** | Đã xóa mất nội dung đặc tả 3NF và quy tắc nghiệp vụ BR01–BR05 ban đầu. |

> [!IMPORTANT]
> TV3 cần xử lý triệt để **4 hạng mục bắt buộc** dưới đây trên branch `feature/hoan_thien_noi_dung_tuan_3_TV3` để Pull Request #28 đủ điều kiện được chấp thuận (Approved) và merge vào `main`.

---

## 🛠️ II. CHI TIẾT 4 HẠNG MỤC CẦN SỬA ĐỔI

### 1. Xử lý triệt để quyền sở hữu đối tượng SQL (Ownership) với TV2 (Phạm Minh Quân)

#### 🛑 Vấn đề thực tế:
- Trong PR #28, TV3 đã sao chép và tự nhận quyền sở hữu:
  - Stored Procedure `dbo.sp_GhiNhanChamCong`
  - Trigger `dbo.trg_ChamCong_KiemTraGio`
- **Hậu quả:** 
  1. Hai đối tượng này thuộc phân hệ Chấm công và đã được định nghĩa trong `database/02_Module_ChamCong_TV2.sql` do TV2 phụ trách.
  2. Nếu TV3 nhận `sp_GhiNhanChamCong`, **TV2 sẽ bị thiếu Stored Procedure trong Rubric chấm điểm cá nhân của Giảng viên**, dẫn đến điểm số cá nhân của thành viên trong nhóm bị ảnh hưởng.
  3. Bản thân TV3 vốn đã có Trigger phân hệ là `trg_PhuCap_KhongSuaKhiDaChotLuong`.

#### ✅ Hành động khắc phục:
1. Mở file `database/03_phucap_khautru_TV3.sql`, **xóa bỏ đoạn khai báo** `sp_GhiNhanChamCong` (mục 2.6) và `trg_ChamCong_KiemTraGio` (mục 2.7) để tránh trùng lặp với file `02_Module_ChamCong_TV2.sql`.
2. Khẳng định chuẩn danh mục 6 đối tượng CSDL sở hữu độc lập của TV3 theo đúng Rubric:
   - **Non-Clustered Index:** `IX_PHUCAP_MaNV_ThangNam` (Covering Index kèm `INCLUDE (SoTien, TenPhuCap)`).
   - **View:** `vw_TongPhuCapThang` (Tổng hợp số khoản và tổng tiền phụ cấp theo nhân viên và kỳ).
   - **Scalar Function:** `fn_TongKhauTru` (Tính tổng tiền khấu trừ của nhân viên trong kỳ tính lương).
   - **Trigger:** `trg_PhuCap_KhongSuaKhiDaChotLuong` (Chặn chỉnh sửa hoặc xóa phụ cấp khi kỳ lương đã `DA_CHOT`).
   - **Stored Procedure & Transaction:** `sp_XoaKyLuongChuaChot` (Quy trình xóa an toàn bảng con trước, bảng cha sau, tự động Rollback khi lỗi hoặc khi kỳ lương đã chốt).
   - *(Tùy chọn khuyến khích)*: Có thể viết thêm SP nghiệp vụ `sp_ThemPhuCapNhanVien` (có kiểm tra `TrangThai <> 'DA_CHOT'` và `SoTien > 0`) để sở hữu 1 SP nghiệp vụ riêng biệt ngoài SP Transaction.

---

### 2. Sửa lỗi bộ kịch bản kiểm thử SQL (`database/tests_TV3/test_module_phucap_khautru_TV3.sql`)

#### 🛑 Vấn đề thực tế:
1. **Lỗi Test 4 (Trigger giờ):** 
   - Lệnh kiểm tra truyền `NgayChamCong = '2026-10-05'` (ngày tương lai), bị ràng buộc kiểm tra `CHK_CHAMCONG_Ngay` ngắt lệnh trước khi dữ liệu chạm tới Trigger. Ảnh `TV3_ObjectsSQLTest.png` ghi rõ lỗi từ CHECK constraint chứ không phải Trigger.
2. **Lỗi Test 5.3 (SP chặn nhân viên nghỉ việc):**
   - Ảnh chụp minh chứng dòng 15 in rõ: `-> LỖI 5.3: SP không chặn nhân viên có trạng thái NGHI_VIEC!` do nhân viên `MaNV = 6` trong CSDL đang có trạng thái `DANG_LAM_VIEC`.
3. **Lỗi Test 6 (Transaction xóa bảng lương):**
   - Đang insert mock với cấu trúc sai lệch schema chuẩn của TV4/TV5:
     ```sql
     -- SAI: MaBangLuong là INT IDENTITY; cột ngày là NgayTao; CHITIETBANGLUONG dùng ThucNhan và LuongCoBan NOT NULL
     INSERT INTO dbo.BANGLUONG (MaBangLuong, Thang, Nam, NgayLap, TrangThai) VALUES ('BL_2026_11', ...);
     ```

#### ✅ Hành động khắc phục:
1. **Thay thế Test 4** bằng kịch bản kiểm thử trực tiếp Trigger của chính TV3: `trg_PhuCap_KhongSuaKhiDaChotLuong`:
   ```sql
   -- TEST 4: KIỂM THỬ TRIGGER trg_PhuCap_KhongSuaKhiDaChotLuong (SỞ HỮU TV3)
   PRINT N'>>> TEST 4: Trigger trg_PhuCap_KhongSuaKhiDaChotLuong';
   BEGIN TRY
       -- Giả định kỳ 09/2026 đã chốt lương, cố tình xóa một dòng phụ cấp thuộc kỳ này
       UPDATE dbo.BANGLUONG SET TrangThai = 'DA_CHOT' WHERE Thang = 9 AND Nam = 2026;

       DELETE FROM dbo.PHUCAPNHANVIEN WHERE Thang = 9 AND Nam = 2026 AND MaNV = 1;
       PRINT N'LỖI: Trigger không chặn sửa/xóa phụ cấp khi kỳ lương đã chốt!';
   END TRY
   BEGIN CATCH
       PRINT N'-> PASS 4: Trigger đã chặn thành công sửa/xóa phụ cấp kỳ đã chốt: ' + ERROR_MESSAGE();
   END CATCH;
   -- Trả lại trạng thái cho kỳ 09/2026
   UPDATE dbo.BANGLUONG SET TrangThai = 'CHUA_CHOT' WHERE Thang = 9 AND Nam = 2026;
   ```
2. **Chuẩn hóa Test 6** cho khớp 100% schema bảng lương:
   ```sql
   -- TEST 6.1: Chuẩn bị dữ liệu kỳ lương test chưa chốt (khớp schema TV4/TV5)
   DELETE FROM dbo.CHITIETBANGLUONG WHERE MaBangLuong IN (SELECT MaBangLuong FROM dbo.BANGLUONG WHERE Thang = 11 AND Nam = 2026);
   DELETE FROM dbo.BANGLUONG WHERE Thang = 11 AND Nam = 2026;

   INSERT INTO dbo.BANGLUONG (Thang, Nam, NgayCongChuan, TrangThai, NgayTao)
   VALUES (11, 2026, 26, 'CHUA_CHOT', GETDATE());

   DECLARE @NewMBL INT = SCOPE_IDENTITY();
   INSERT INTO dbo.CHITIETBANGLUONG (MaBangLuong, MaNV, LuongCoBan, NgayCongThucTe, TienCong, TongPhuCap, TongKhauTru, ThucNhan)
   VALUES (@NewMBL, 1, 10000000, 26, 10000000, 500000, 200000, 10300000);

   EXEC dbo.sp_XoaKyLuongChuaChot @Thang = 11, @Nam = 2026;
   PRINT N'-> PASS 6.1: Đã xóa thành công bảng lương và chi tiết lương của kỳ chưa chốt.';
   ```
3. Chạy lại script trên SSMS và **chụp lại ảnh `screenshots/TV3/TV3_ObjectsSQLTest.png`** thể hiện đầy đủ các test case đều in nhãn `-> PASS`.

---

### 3. Sửa lỗi giao diện nút bấm "Tàng hình" trên Java Swing (`PhuCapKhauTruPanel.java`)

#### 🛑 Vấn đề thực tế:
- Các hàm tự định nghĩa `styleAddButton` và `styleDeleteButton` thiết lập `btn.setForeground(Color.WHITE)` nhưng trên `WindowsLookAndFeel` của Java Swing, thuộc tính `setBackground` của `JButton` bị renderer mặc định của Windows bỏ qua (giữ nguyên nền màu trắng/xám).
- **Hậu quả:** Nút có chữ màu trắng hiển thị trên nền trắng $\rightarrow$ **chữ bị tàng hình, không thể nhìn thấy chữ trên nút bấm** (đã thấy rõ trong ảnh `screenshots/TV3/TV3_PhuCapKhauTruPanel_LocVaThem.png`).
- Làm mất đi tính đồng bộ Modern Fluent Design của toàn bộ dự án mà TV5 đã quy chuẩn.

#### ✅ Hành động khắc phục:
1. Xóa bỏ hoàn toàn 2 hàm cục bộ `styleAddButton(JButton btn)` và `styleDeleteButton(JButton btn)`.
2. Áp dụng chuẩn `UITheme` của hệ thống:
   ```java
   // Nút Thêm phụ cấp & Thêm khấu trừ: Dùng Primary Button
   btnThemPC = new JButton("Thêm phụ cấp");
   UITheme.stylePrimaryButton(btnThemPC);

   // Nút Xóa phụ cấp & Xóa khấu trừ: Dùng Danger Button
   btnXoaPC = new JButton("Xóa phụ cấp");
   UITheme.styleDangerButton(btnXoaPC);

   btnThemKT = new JButton("Thêm khấu trừ");
   UITheme.stylePrimaryButton(btnThemKT);

   btnXoaKT = new JButton("Xóa khấu trừ");
   UITheme.styleDangerButton(btnXoaKT);
   ```
3. Đặt lại layout cụm nút `pnlActions` và `pnlInputs` để đảm bảo bố cục hài hòa, không bị tràn viền hoặc co rúm khi thay đổi kích thước cửa sổ.
4. Chụp lại ảnh minh chứng giao diện: `TV3_PhuCapKhauTruPanel_LocVaThem.png` và `TV3_PhuCapKhauTruPanel_Xoa.png`.

---

### 4. Khôi phục hồ sơ tiến độ & Chuẩn hóa đường dẫn tài liệu

#### 🛑 Vấn đề thực tế:
- File `docs/TV3_Tien_Do_Thuc_Hien.md` bị xóa mất toàn bộ nội dung phân tích nghiệp vụ, quy tắc BR01–BR05, thiết kế bảng 3NF và ma trận test case, chỉ còn lại checklist 26 dòng.
- File `docs/TV3_BaoCao_ChuyenDe_PhuCap_KhauTru_CuoiKy.md` dẫn sai đường dẫn ảnh: `screenshots/TV3_...` thay vì `screenshots/TV3/TV3_...`.
- Mô tả PR #28 liệt kê một số file không có trong commit (`GenerateTV3ProofScreenshots.java`, `run_tuan3_tv3.ps1`).

#### ✅ Hành động khắc phục:
1. **Khôi phục `docs/TV3_Tien_Do_Thuc_Hien.md`:** Giữ nguyên các phần phân tích đặc tả 3NF, bảng quy tắc BR01–BR05 của Tuần 1, Tuần 2 và bổ sung thêm mục Tuần 3 (kết quả benchmark, kết quả test module).
2. **Sửa đường dẫn ảnh:** Trong `docs/TV3_BaoCao_ChuyenDe_PhuCap_KhauTru_CuoiKy.md`, sửa lại đúng thư mục con: `screenshots/TV3/TV3_Benchmark_ExecutionPlan.png`, `screenshots/TV3/TV3_Benchmark_StatisticsIO.png`.
3. **Cập nhật lại PR Description:** Bỏ các file không có trong commit để danh sách khớp 100% với Git diff thực tế.

---

## 🚀 III. QUY TRÌNH THỰC HIỆN DÀNH CHO TV3

TV3 thực hiện theo các bước sau ngay trên máy cá nhân:

```powershell
# 1. Chuyển sang branch PR #28
git checkout feature/hoan_thien_noi_dung_tuan_3_TV3
git pull origin feature/hoan_thien_noi_dung_tuan_3_TV3

# 2. Thực hiện sửa đổi mã nguồn và tài liệu theo hướng dẫn ở Mục II

# 3. Kiểm tra biên dịch Java cục bộ
javac -encoding UTF-8 -cp "bin;src/resources;lib/*" -d bin (Get-ChildItem -Path src -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName)

# 4. Commit các thay đổi hoàn thiện
git add database/ docs/ screenshots/ src/
git commit -m "fix(tv3): hoan thien ownership sql, sua test case, fix ui button va dong bo docs"

# 5. Đẩy lên nhánh để cập nhật tự động Pull Request #28
git push origin feature/hoan_thien_noi_dung_tuan_3_TV3
```

Sau khi TV3 đẩy commit mới lên, Lead Architect và Leader sẽ tiến hành nghiệm thu lại lần cuối để Approve và tiến hành Merge vào `main`.
