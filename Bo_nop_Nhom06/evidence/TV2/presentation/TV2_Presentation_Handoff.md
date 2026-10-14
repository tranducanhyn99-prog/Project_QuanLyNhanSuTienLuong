# TV2 — HANDOFF TÀI LIỆU TRÌNH BÀY VÀ MINH CHỨNG (BẢN CHÍNH THỨC)

- **Ngày bàn giao:** 13/10/2026.
- **Source căn cứ:** `main` @ `f2a41f7c9bfc0b911613ed4355ccc79f8281ddd5` (Working tree clean).
- **Người bàn giao:** Phạm Minh Quân (TV2 — 24110311).
- **Người nhận chính:** TV5 (Trần Đức Anh) — Tổng hợp báo cáo Word và Slide tổng thể nhóm.

---

## 1. Danh Mục Tài Liệu Bàn Giao

Toàn bộ gói bàn giao được đóng gói tại: `Bo_nop_Nhom06/evidence/TV2/` và `build/tv2-presentation-draft/updated/`:

| Tên tài liệu | Nội dung bàn giao | Trạng thái |
|---|---|---|
| `TV2_Slides_4_6_Content.md` | Nội dung chữ và mô tả sơ đồ cho Slide 4, 5, 6; số liệu benchmark chính thức | **HOÀN TẤT** (Đã điền số thực) |
| `TV2_Speaking_Script.md` | Kịch bản nói 02:00, kịch bản demo 00:45, câu chuyển giao TV3, 12 câu vấn đáp | **HOÀN TẤT** |
| `TV2_GUI_Demo_Checklist.md` | Checklist demo, cảnh báo môi trường QA, phương án fallback SQL | **HOÀN TẤT** |
| `TV2_Documentation_Audit.md` | Báo cáo rà soát 8 finding (A1–A8) và đề xuất chỉnh sửa cho TV5 | **HOÀN TẤT** |
| `TV2_benchmark_summary.md` | Báo cáo đo lường benchmark chi tiết, giải trình 574 vs 522 | **HOÀN TẤT** |
| `TV2_results_comparison.md` | Báo cáo kiểm chứng 100% tương đương dữ liệu giữa Before và After | **HOÀN TẤT** |
| `TV2_index_metadata.csv` | Metadata chỉ mục thật của bảng `dbo.CHAMCONG` | **HOÀN TẤT** (7 dòng) |
| 4 file `.sqlplan` | Actual execution plan thực tế từ SQL Server | **HOÀN TẤT** |
| 4 file `.csv` | Dữ liệu thô xuất từ các câu truy vấn benchmark | **HOÀN TẤT** |
| `evidence_manifest.csv` | Bảng kê hash SHA-256 toàn bộ file minh chứng | **HOÀN TẤT** |

---

## 2. Hướng Dẫn Nhanh Cho TV5 (Trần Đức Anh) Ghép Deck và Báo Cáo

1. **Slide thuyết trình (Slide 4, 5, 6):**
   - Lấy nội dung trực tiếp từ file `TV2_Slides_4_6_Content.md`.
   - Giữ nguyên số lượng 3 slide để đảm bảo toàn bộ nhóm không vượt quá trần 15 slide.
   - Tại Slide 6: Cập nhật số liệu Logical Reads: `574 ➔ 5` (Giảm **99.13%**).
2. **Báo cáo Word cuối kỳ:**
   - Cập nhật Bảng Benchmark phân hệ Chấm công (§3.2): Thay thế nhãn "chờ đo lường" bằng kết quả của Run ID `run_20261013_140800`.
   - Bổ sung hàm `dbo.fn_TongPhuCap` vào danh mục hàm thuộc trách nhiệm của TV2 (Chương 2).
   - Kiểm tra DDL bảng `dbo.CHAMCONG` đảm bảo có `NOT NULL` và `PRIMARY KEY CLUSTERED`.
3. **Phân định ranh giới sở hữu (Giữ vững rubric):**
   - TV1: Sở hữu hàm `fn_TinhSoNgayCong` (dù đặt ở script 02).
   - TV2: Sở hữu bảng `CHAMCONG`, `sp_GhiNhanChamCong`, 2 trigger, view `vw_TongHopChamCongThang`, index `IX_CHAMCONG_MaNV_Ngay`, transaction nhập lô, và hàm `fn_TongPhuCap` (module 03).
   - TV5: Sở hữu trigger `trg_ChamCong_KhongSuaKhiDaChotLuong` (module 05).
   - Phân hệ Lương (TV4): Đếm trực tiếp bảng `CHAMCONG` có khóa `HOLDLOCK`, không gọi `fn_TinhSoNgayCong`.
