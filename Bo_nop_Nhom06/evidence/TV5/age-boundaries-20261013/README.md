# TV1 tuổi — kiểm tích hợp TV5 ngày 13/10/2026

`CHK_NHANVIEN_DoTuoi` trong source module01 được sửa một dòng từ DATEDIFF năm sang `NgaySinh <= DATEADD(YEAR,-18,NgayVaoLam)`, khớp Java Period. **Create-only**: schema cũ không được tự migrate hoặc drop/recreate constraint; chỉ fresh table nhận CHECK mới. Ownership module vẫn là TV1; TV5 tích hợp fix và kiểm.

[Age runner](age-run.log) lưu source SHA-256, bản SQL đã chạy và cleanup fresh QA `PRJ_Fix_QA_20261013_age_aa7a3fa0`. [Messages](TV1_Age_Boundaries.sql.messages.log) ghi chín case/18 operations: trước/ngày/sau sinh nhật18, giao năm, sinh Feb29 đến Feb28 chưa đủ18/Mar1 đủ, ngày vào làm Feb29, underage và người trưởng thành. Mỗi case kiểm INSERT trực tiếp và SP tạo employee/profile; reject không để employee/profile/transaction, row counts NHANVIEN/TAIKHOAN/LICHSULUONG giữ nguyên sau cleanup.

[TV1 caller regression](test_module_nhansu_TV1.sql.messages.log) bảy PASS: employee/profile, CHECK tuổi, duplicate profile atomic rollback, hire date update, protected delete, function/view/index và cleanup. [Source testcase hiện hành](../../../database/tests/TV1_Age_Boundaries.sql) được thêm vào runner SQL/package; không sửa proof cũ để giả đã kiểm CHECK mới.

Java `GuiQaRegressionTest` dùng cùng chín boundary tại Service validator; [log88 cuối](../gui-followup-20261013/GuiQaRegressionTest-88-final.log) còn gồm phone/login/GUI lỗi hiện hành. Không gọi số operations SQL là Java assertions. QA age đã DROP; không thay constraint database shared hoặc QA GUI được cài trước fix.
