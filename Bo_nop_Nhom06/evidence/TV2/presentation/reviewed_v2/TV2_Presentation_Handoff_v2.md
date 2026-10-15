# TV2 — HANDOFF TÀI LIỆU TRÌNH BÀY VÀ MINH CHỨNG (BẢN REVIEWED V2 ĐÃ KIỂM ĐỊNH)

- **Ngày bàn giao:** 13/10/2026.
- **Source căn cứ:** `main` @ `f2a41f7c9bfc0b911613ed4355ccc79f8281ddd5` (Working tree clean).
- **Người bàn giao:** Phạm Minh Quân (TV2 — 24110311).
- **Người nhận chính:** TV5 (Trần Đức Anh) — Tổng hợp báo cáo Word và Slide tổng thể nhóm.

---

## 1. Danh Mục Tài Liệu Bàn Giao (Reviewed V2 Package)

Toàn bộ gói bàn giao chuẩn hóa v2 được lưu trữ tại `Bo_nop_Nhom06/evidence/TV2/` và `build/tv2-presentation-draft/reviewed_v2/`:

| Tên tài liệu / Thư mục | Nội dung bàn giao | Trạng thái |
|---|---|---|
| `presentation/reviewed_v2/TV2_Slides_4_6_Content_v2.md` | Nội dung chữ & cấu trúc cho Slide 4, 5, 6; số liệu benchmark 574 ➔ 5 | **HOÀN TẤT V2** |
| `presentation/reviewed_v2/TV2_Speaking_Script_v2.md` | Kịch bản nói 02:00, dry-run demo 00:45, 12 câu hỏi vấn đáp chuẩn | **HOÀN TẤT V2** |
| `presentation/reviewed_v2/TV2_GUI_Demo_Checklist_v2.md` | Checklist demo, cảnh báo QA DB rỗng, fallback SQL inspection | **HOÀN TẤT V2** |
| `presentation/reviewed_v2/TV2_Documentation_Audit_v2.md` | Báo cáo rà soát 8 finding (A1–A8), giải trình 574 vs 522 khách quan | **HOÀN TẤT V2** |
| `presentation/reviewed_v2/TV5_Proposed_Corrections_v2.md` | Bảng số liệu & đoạn văn đề xuất sẵn cho TV5 copy vào Báo cáo Word | **HOÀN TẤT V2** |
| `reviewed_v2/TV2_benchmark_summary_v2.md` | Báo cáo phân tích benchmark chi tiết v2, giải trình IO, thời gian, NULL warning | **HOÀN TẤT V2** |
| `schema-inspection/TV2_live_schema_inspection.log` | Trích xuất schema trực tiếp từ live QA: columns, defaults, keys, triggers, view | **HOÀN TẤT** |
| `reproduction/` | Kịch bản tái lập `run_tv2_benchmark_final.ps1` & tài liệu provenance | **HOÀN TẤT** |
| `run_20261013_140800/` | Thư mục bằng chứng thô gốc: 4 file `.sqlplan`, 4 file `.csv`, các file log | **BẢO TỒN NGUYÊN VẸN** |
| `sql-verification/` | Log suite SQL kiểm thử 21/21 PASS (`test_module_chamcong_TV2.log`) | **TÁI SỬ DỤNG HỢP LỆ** |

---

## 2. Hướng Dẫn Nhanh Cho TV5 (Trần Đức Anh) Ghép Deck và Báo Cáo

1. **Slide thuyết trình (Slide 4, 5, 6):**
   - Lấy nội dung trực tiếp từ file `TV2_Slides_4_6_Content_v2.md`.
   - Giữ nguyên số lượng 3 slide để đảm bảo toàn bộ nhóm không vượt quá trần 15 slide.
   - Tại Slide 6: Cập nhật số liệu Logical Reads: `574 ➔ 5` (Giảm **99.13%**). Nêu rõ đo lường trên bảng tạm mô phỏng index persistent.
2. **Báo cáo Word cuối kỳ:**
   - Cập nhật Bảng Benchmark phân hệ Chấm công (§3.2): Lấy bảng từ `TV5_Proposed_Corrections_v2.md`.
   - Bổ sung hàm `dbo.fn_TongPhuCap` vào danh mục hàm thuộc trách nhiệm của TV2 (Chương 2).
   - Đồng bộ DDL bảng `dbo.CHAMCONG` và View `dbo.vw_TongHopChamCongThang` theo schema live xuất tại `TV2_live_schema_inspection.log`.
3. **Phân định ranh giới sở hữu (Bảo toàn Rubric):**
   - TV1: Sở hữu hàm `fn_TinhSoNgayCong` (dù đặt ở script 02).
   - TV2: Sở hữu bảng `CHAMCONG`, `sp_GhiNhanChamCong`, 2 trigger module 02 (`trg_ChamCong_KiemTraGio`, `trg_ChamCong_KiemTraNhanVien`), view `vw_TongHopChamCongThang`, index `IX_CHAMCONG_MaNV_Ngay`, transaction nhập lô, và hàm `fn_TongPhuCap` (module 03).
   - TV5: Sở hữu trigger `trg_ChamCong_KhongSuaKhiDaChotLuong` (module 05).
   - Phân hệ Lương (TV4): Đếm trực tiếp bảng `CHAMCONG` có khóa `HOLDLOCK`, không gọi `fn_TinhSoNgayCong`.
4. **Các hạng mục còn mở (Trách nhiệm phối hợp nhóm):**
   - File PowerPoint chung của nhóm (`.pptx`): TV5 tổng hợp từ Markdown của 5 thành viên.
   - Buổi tổng duyệt thuyết trình toàn nhóm (Rehearsal): Cần TV5 điều phối lịch chung.
   - Biên nhận chuyển giao chính thức từ TV5: Sẽ hoàn tất sau khi TV5 tích hợp vào deck chung.
