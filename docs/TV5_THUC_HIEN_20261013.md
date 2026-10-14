# TV5 — Kết quả checklist 13/10/2026

**Trần Đức Anh — 24110155 · Nhóm 06**

Đã hoàn thiện phần sửa/QA TV5 có thể chạy độc lập, nhận evidence TV2, cập nhật Word và deck chung 15 slide. Bộ nhóm chưa nghiệm thu cuối: còn bàn giao/chốt ownership TV1/TV3, sửa handoff TV2, kiểm máy nhận, Windows DPI thật và rehearsal. Không đánh dấu các việc chưa chạy là PASS. [Checklist](TV5_TASKLIST_HOAN_THIEN.md).

| Phần việc | Kết quả và phạm vi |
|---|---|
| TV5-01 | Function/fallback tiền DECIMAL(18,2); chín case biên, ba overflow8115, payroll header/detail và transaction caller rollback nguyên trạng. Word khớp mô tả mới. |
| TV5-02 | 10.000 dòng tạm, predicate MaPB2/MaCV1, 833 dòng; Messages **126→13 reads**, actual scan/seek, đầy đủ Results/bidirectional EXCEPT/TSV/cleanup. INDEX hints, warm cache, MAXDOP1; không cam kết tốc độ production. 142→15 là lượt plan lịch sử riêng. |
| SQL hồi quy | Schema/rerun, module TV1–TV3, Fix, money, security, payroll E2E và bốn source races PASS trong lượt trước; không đổi log lịch sử hoặc gọi identity50 cũ là đã chạy mới. |
| TV5-03 | GUI tự động: Admin denied5/allowed12 assertions; lock SQL disabled/login18470, unlock, reset old/new password, role/profile/SQL role đúng. Server permission tạm revoked, owned QA cleanup. |
| TV5-04 | Login/đổi role outage ở relay14513 trong lượt trước; report6/in-flight6 ở relay14514 lượt mới. DMV thấy một SP request chờ UPDLOCK trong transaction; disconnect thật, EDT/nút phục hồi, audit0/profile active/login enabled trước reload, không auto retry. Không suy ra mất mạng sau COMMIT luôn rollback. |
| TV5-05 | Đổi kỳ12 lần và đối chiếu total SQL; Admin SQL bịblock trước logout/loginEmployeeA, callback cũ bịbỏ; repeat click khi write busy không gửi thêm. Minimum Login1000×700/Main1024×680/report760×550, Java scale1/1.25/1.5 có21 bounds assertions. Windows DPI thật chưa đổi; printAll có giới hạn font/capture. |
| TV5-06 | Fresh QA same machine: schema01–05/seed06/provision năm SQL identities/bốn roles, own slips và payroll GUI flow PASS. Package copy sạch khôngGit compile54 / target11, GuiQa88/Security104 PASS; SQL standalone money/benchmark lượt trước PASS. Chưa thử máy khác. |
| TV5-07 | TV1CRUDfixed17 +optionalphone3; TV2batch/adjust8 (hai INSERT rollback trước final duplicate, ba row valid commit, monthly dialog/view khớp); TV3PC/KT10; payrolllifecycle11. Tổng các mode **87 live assertions**, dialogs automated, không rehearsal tay. |
| Lỗi mới đã sửa | CHECK tuổi TV1 dùng birthday-aware DATEADD trên fresh schema; chín case/18 SQL operations và bảy TV1 caller PASS. Create-only, không migration DB cũ. DanhMucService chung add/update phone ASCII10–15digits, blank→NULL và lỗi ngắn; focused retest7 riêng/native modal đọc đủ. |
| Java | Current **257=88+104+45+20**; GuiQa88/Security104 rerun sau các phần bị ảnh hưởng, Light45/Login20 từ lượt trước không đổi. Không cộng SQL/live/layout vào257. Lượt package231 trước đó vẫn giữ số/ngày lịch sử. |
| Word | Source/package cùng SHA256 **7BCB91822EBFE7A409907D6E6C9CD5D11DEC0BF61574A61F8593D779FBAD06C5**; native render61 tổng/55 nội dung, mục lục updated. So với bản414f trước tổng hợp: chín trang2,19,26,54,55,58,59,60,61 thay đổi,52 trang trùng pixel; đã xem chín trang, không tràn. |
| Deck | [Deck chung15 slide](../Bo_nop_Nhom06/slides/Nhom06_ThuyetTrinh_15_Slides.pptx), nativeOffice/structural/font/notes/preview15 PASS, SHA256 **E414AEAC526CBB79EF4875B9A104FF05273197FD94BFF054FD6C91963C524C71**. TV5 tổng hợp từ guide/source và bàn giao; không thay owner TV1/TV3 nghiệm thu cuối. |

## Minh chứng

- [Money/benchmark](../Bo_nop_Nhom06/evidence/TV5/verification-20261013/summary.txt), [SQL hồi quy](../Bo_nop_Nhom06/evidence/TV5/sql-regression-20261013/cleanup.log), [standalone package lượt trước](../Bo_nop_Nhom06/evidence/TV5/package-verification-20261013/summary.txt).
- [GUI87/focusedphone7](../Bo_nop_Nhom06/evidence/TV5/gui-followup-20261013/README.md): testcase/log/SQL/ảnh, giới hạn automated Swing/printAll/native capture, temporary permission, fixture và cleanup. [Native phone modal](../Bo_nop_Nhom06/evidence/TV5/gui-followup-20261013/phone-native-1.png) đọc đủ; ảnh printAll không được dùng để suy ra clipping native.
- [Age fresh QA](../Bo_nop_Nhom06/evidence/TV5/age-boundaries-20261013/README.md) và [layout/Java scale](../Bo_nop_Nhom06/evidence/TV5/layout-20261013/README.md).
- [TV2 receipt52/52 bytes](../Bo_nop_Nhom06/evidence/TV2/README_TV5_RECEIPT.md): canonical v3, Messages574→5/XML522→5, four actual plans/Results/PPTX4–6. Local receipt từ PR44 OPEN head7efd9dd, không merge/approve; handoffv2 còn chỉ nhầm superseded.
- [Word rà soát](../Bo_nop_Nhom06/report/TV5_Ra_Soat_Bao_Cao.md), [slides README](../Bo_nop_Nhom06/slides/README_SLIDES.md) và [package](../Bo_nop_Nhom06/README_BO_NOP.md).

QA lõi `PRJ_Fix_QA_20261013_followup_8e2bedb8` / `tv5f_d54a94da_*` và QA age đã dọn. Namespace GUI tái tạo riêng cho focusedphone cuối sau cleanup đầu, rồi dọn lại. Loginfixture/password từ manifest môn học được phép, không credential DBA/Git. Shared QA/login không bị thao tác; SQL giữ Running sau user-authorized startup. JDBC WAITFOR timeout/HY008 ở lượt trước là probe, không giả GUI query timeout.

## Trạng thái nghiệm thu

| Mã | Trạng thái | Còn thiếu/giới hạn |
|---|---|---|
| TV5-01/02 | Hoàn thành các case đã kiểm | Giữ số đo/phạm vi từng lượt |
| TV5-03/04 | Hoàn thành GUI tự động các case yêu cầu | Không nhận mọi ambiguous post-COMMIT outage là rollback |
| TV5-05 | Core async/selection/session/repeat PASS | Windows DPI thật và dữ liệu lớn chưa kiểm |
| TV5-06 | Fresh same-machine/package checks PASS | Máy nhận khác còn chờ |
| TV5-07 | Các ca GUI tích hợp đã kiểm | TV2 xác nhận yêu cầu kế hoạch TRY…CATCH; caller Java giữ transaction, không thêm catch hình thức |
| TV5-08 | Đã nhậnTV2/TV4 | TV1 ERD/logs và TV3 sửa baP2/bàn giao cuối; handoffTV2 sửa mộtP2 |
| TV5-09 | Word/deck15 đã render/kiểm | Owner TV1/TV3 final acceptance, máy trình chiếu; PDF nộp nếu hình thức yêu cầu |
| TV5-10 | Package source/evidence/metadata/manifest được đồng bộ | Bộ nhóm chưa nghiệm thu máy nhận/owner; không commit/push/merge khi chưa yêu cầu |

[PR43 review](PR43_TV3_REVIEW_20261013.md) và [PR44 review](PR44_TV2_REVIEW_20261013.md) giữ findings/scope riêng. Không public comment/approve/merge trong lượt này; không lấy evidence cũ làm proof code mới.
