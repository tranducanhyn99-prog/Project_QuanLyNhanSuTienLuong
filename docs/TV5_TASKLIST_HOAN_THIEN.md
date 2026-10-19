# TV5 — Checklist hoàn thiện dự án theo rubric

**Trần Đức Anh — 24110155 · Nhóm 06 · Cập nhật 16/10/2026**

Phạm vi: phần SQL/Java của TV5, kiểm thử tích hợp và tổng hợp bộ nộp. Chưa tính luyện nói, vấn đáp hoặc chấm điểm thuyết trình; slide vẫn được theo dõi như một sản phẩm cần bàn giao. Trạng thái hoàn tất được đánh dấu theo minh chứng; các mục còn lại vẫn chờ kiểm/nghiệm thu.

Căn cứ: [phân công](Ke_hoach_phan_cong_Project_DBMS_Nhom06.md), [hướng dẫn hoàn thiện bộ nộp](HUONG_DAN_HOAN_THIEN_BO_NOP.md), [GUI QA](GUI_TEST_REPORT_20261009.md), [evidence TV5](../Bo_nop_Nhom06/evidence/TV5/README_EVIDENCE.md) và [trạng thái bộ nộp](../Bo_nop_Nhom06/README_BO_NOP.md). Tích hợp 15/10 dựa trên `a98371d`; SQL/Java giữ nguyên. Trọng số áp dụng cho cả nhóm; checklist không quy định mức trừ điểm từng việc.

**Tiến độ 16/10:** đã tích hợp TV1–TV5, cập nhật Word/deck và evidence package; TV1 PR #51 và TV2 PR #52 đã merge. Còn máy nhận, Windows DPI thật, rehearsal và PDF cuối nếu yêu cầu.

## 1. Đã có — giữ lại, chỉ kiểm lại phần bị ảnh hưởng khi sửa

- [x] SP chốt, function thực nhận, trigger khóa chi tiết, view báo cáo và index phòng ban/chức vụ đã có trong module 05.
- [x] Có log quyền object, danh tính SQL thật, Employee A/B, concurrency A/B và bốn race nguồn. Phạm vi/ngày chạy được ghi trong evidence TV5.
- [x] Có Word 55 trang nội dung, ba slide TV5 13–15, source/JDBC/script/README và bản package từng phần.
- [x] Có regression Java offline 257 assertions; đã nhận bộ TV4 gồm Word, slide 10–12 và evidence.

Các sản phẩm trên không thay cho kiểm thử mới sau thay đổi logic hoặc xác nhận bộ nộp toàn nhóm hoàn tất.

## 2. TV5 có thể làm ngay

### TV5-01 — Thống nhất kiểu tiền của function thực nhận

- [x] Rà `fn_TinhThucNhan` trong [module 05](../database/05_Security_Payroll_TV5.sql), các caller và fallback DDL; thống nhất tham số/kết quả tiền `DECIMAL(18,2)`. Vượt miền báo lỗi, không cắt số.
- [x] Kiểm NULL, 0, thực nhận âm, phần thập phân, giá trị lớn và overflow; payroll lưu được giá trị lớn và lỗi giữ nguyên toàn bộ nháp. Xem proof tại báo cáo tiến độ 13/10.

**Hoàn tất khi:** source và testcase khớp quy tắc tiền; có log kết quả, cập nhật mô tả function trong Word. Phối hợp TV4 để kiểm đường tính lương; chưa đồng bộ bản source/package hoặc manifest trước khi xác minh xong.

### TV5-02 — Bổ sung đầy đủ benchmark index TV5

- [x] Chạy [benchmark TV5](../database/test_benchmark_index_TV5.sql) trên QA riêng; có Messages IO/TIME, Results trước/sau, hai actual plans và cleanup.
- [x] Đối chiếu toàn bộ kết quả bằng bidirectional EXCEPT và TSV; ghi fixture 10.000/833 dòng, hint, warm cache, MAXDOP 1, môi trường và SHA-256 source.
- [x] Có summary/evidence lượt mới 126→13; giữ số 142→15 đúng lượt plan cũ, không ghi đè số lịch sử trong Word/slide.

**Hoàn tất khi:** log/Results/plan/summary cùng một phép đo, mở được trong package, kết quả tương đương và fixture đã dọn. Không dùng plan trả 0 dòng để chứng minh cải thiện.

### TV5-03 — Kiểm đầy đủ giao diện quản trị tài khoản

- [x] Trên tài khoản QA dành riêng cho bài thử: kiểm danh sách, đổi role, khóa/mở và reset mật khẩu qua GUI; kiểm lại trạng thái thực ở SQL và khả năng đăng nhập.
- [x] Kiểm cả quyền hợp lệ và thiếu quyền server. `DB_Admin`/`db_owner` không tự có quyền `ALTER LOGIN`; lỗi phải rõ và không để SQL login/profile lệch nhau.
- [x] Ghi trạng thái trước/sau và cleanup các tài khoản/fixture thuộc lượt thử.

**Hoàn tất khi:** có bảng testcase, kết quả SQL/GUI và log/ảnh thật; không thay đổi tài khoản dùng chung chỉ để thử.

### TV5-04 — Kiểm mất kết nối SQL và phục hồi trên GUI

- [x] Dùng môi trường QA riêng hoặc phương án chỉ ảnh hưởng kết nối QA; thử mất kết nối lúc đăng nhập, đọc báo cáo và thao tác ghi.
- [x] Kiểm thông báo, khả năng tương tác của cửa sổ, nút được phục hồi và khả năng đăng nhập/tải lại sau khi kết nối trở lại.
- [x] Với thao tác ghi bị gián đoạn, kiểm SQL để xác định đã commit hay rollback trước khi thử lại; không tự gửi lại và gây ghi trùng.

**Hoàn tất khi:** có kết quả thử mất kết nối thật và đối chiếu dữ liệu; exception giả lập chỉ là regression bổ sung. Không dừng SQL instance dùng chung trong lúc thành viên khác đang làm việc.

### TV5-05 — Kiểm giao diện chung và dữ liệu trả về chậm

Đã kiểm min-window bằng render và Java scale1/1.25/1.5, core async/period/session/repeat trên SQL thật. Windows DPI thật chưa đổi; checkbox đầu giữ mở cho phần này.

- [ ] Kiểm LoginFrame/MainFrame/BaoCaoPanel ở cửa sổ nhỏ nhất cho phép và các mức DPI có thể thử; ghi cụ thể mức đã thử.
- [x] Đổi kỳ liên tục khi đang tải, đăng xuất/đăng nhập đổi người khi request cũ chưa xong và bấm thao tác ghi lặp.
- [x] Kiểm kết quả cũ không ghi đè kỳ/phiên mới, quyền/menu đúng và thao tác ghi không bị nhân đôi.

**Hoàn tất khi:** có testcase/ảnh/log theo phiên và kỳ; sửa lỗi nếu phát hiện rồi kiểm lại đúng đường bị ảnh hưởng.

### TV5-06 — Xác nhận quy trình cài/chạy trên môi trường mới

- [ ] Dùng máy khác hoặc môi trường sạch độc lập; nếu chỉ dùng QA mới trên cùng máy, ghi rõ chưa xác nhận máy khác.
- [x] Làm theo [README bộ nộp](../Bo_nop_Nhom06/README_BO_NOP.md): cài schema, provision login/user/role/mapping, cấu hình JDBC, build và chạy GUI.
- [x] Kiểm bốn role, Employee chỉ đọc phiếu riêng và một luồng nghiệp vụ cơ bản; chuẩn bị dữ liệu QA phù hợp trước khi thử.
- [x] Ghi cách tạo fixture: seed tháng 9 hiện có không tự tạo fixture A/B tháng 10 hoặc SQL login. Sửa README nếu thao tác cài thực tế khác hướng dẫn.

**Hoàn tất khi:** người nhận có thể cài/chạy bằng file trong package, không phụ thuộc `build/` hoặc cấu hình trên máy gốc; có log môi trường và kết quả. Có thể kiểm bộ hiện tại trước, rồi kiểm lại phần thay đổi khi đóng gói cuối.

## 3. TV5 điều phối — phối hợp chủ module

### TV5-07 — Hoàn tất các ca GUI tích hợp còn thiếu

Các case dưới được chạy bằng Swing/handler/JDBC thật, dialog tự động; không phải demo tay. CHECK tuổi được sửa create-only/kiểm fresh QA. TV2 Option B hiện dùng TRY…CATCH + bare THROW; Java Service quản lý transaction lô. Kết quả cuối và giới hạn được ghi trong evidence ngày 16/10.

- [x] TV1: CRUD phòng ban/chức vụ, thêm/xóa nhân viên và dữ liệu sai; phối hợp sửa CHECK tuổi SQL đang dùng `DATEDIFF(YEAR, ...)` để kiểm đủ 18 tuổi theo ngày.
- [x] TV2: chấm công theo lô, rollback khi một dòng sai, điều chỉnh công tháng; đối chiếu xử lý lỗi `sp_GhiNhanChamCong` với yêu cầu TRY…CATCH trong kế hoạch nhóm.
- [x] TV3: thêm/xóa khoản và xóa kỳ nháp, từ chối thao tác trên kỳ chốt.
- [x] TV4 + TV5: xác nhận luồng nguồn → tính → chốt → báo cáo/phiếu cá nhân → mở lại; kết quả và trạng thái SQL khớp GUI.

**Hoàn tất khi:** từng module có người phụ trách, testcase và kết quả; TV5 tổng hợp vào báo cáo GUI. Giữ ownership TV1–TV4, không tự ghi những phần chưa thử là PASS.

## 4. Hoàn thiện sau khi nhận bàn giao

### TV5-08 — Nhận và kiểm evidence TV1–TV3

- [x] TV1: nhận ERD 10 bảng, log test 7/7, benchmark/plan 75→10 và deck riêng theo PR #51; kiểm nguồn/evidence.
- [x] TV2: nhận benchmark CHAMCONG mới gồm Messages, Results, bốn actual plans và summary; không dùng số lịch sử 574→5 làm kết quả mới.
- [x] TV3: nhận 13 file PR #43/#48 vào package; kiểm byte/hash, log/plan, Word và notes; giữ giới hạn provenance theo [biên nhận](../Bo_nop_Nhom06/evidence/TV3/README_EVIDENCE.md).
- [x] Hoàn tất kiểm file/ngày/môi trường/query/giới hạn cho toàn nhóm; TV1/TV2 đã có cập nhật PR #51/#52 và evidence nhận riêng.

**Hoàn tất khi:** evidence TV1–TV3 mở được trong bộ nộp, có nguồn rõ ràng; không bắt chạy lại log đã hợp lệ chỉ để đổi ngày.

### TV5-09 — Chốt Word và deck chung

Đã có deck 15 slide; notes TV1/TV2/TV3 đã cập nhật. Slide nhóm 1–3 được đối chiếu với handoff TV1 và giữ dạng tóm tắt trong giới hạn 15 slide; việc tập nói thuộc rehearsal chung.

- [x] Nhận TV2 slide 4–6, TV3 slide 7–9, TV4 slide 10–12 và phần TV5; deck chung đúng 15 slide, theme/notes/link và render đã kiểm.
- [x] Đối chiếu slide nhóm 1–3 với TV1 handoff PR #51; nhóm giữ bản tóm tắt, TV1 deck chi tiết vẫn được đóng gói riêng.
- [x] Cập nhật Word theo source/số đo đã xác minh; giữ đúng ownership, giải thích 3NF và snapshot, phân biệt minh chứng hiện hành với ảnh/số lịch sử.
- [x] Kiểm mục lục, số trang, bảng/hình/link và độ dài 50–100 trang nội dung trên bản cuối. Không tự cộng bốn trang báo cáo TV4 vào Word nhóm.
- [ ] Xuất và kiểm PDF nếu hình thức nộp yêu cầu; năm guide Markdown không thay deck chung.

**Hoàn tất khi:** có Word/deck cuối khớp source và evidence; PDF nếu cần. Mục này không đánh giá kỹ năng thuyết trình.

### TV5-10 — Đồng bộ và nghiệm thu package cuối

- [x] Sau khi source được sửa và kiểm xong, cập nhật bản source/SQL/Word/slide/README trong package; giữ evidence lịch sử nguyên bản và ghi rõ phạm vi khi logic đã thay đổi.
- [x] Cập nhật danh mục evidence, source provenance và manifest SHA-256 theo đúng file cuối; kiểm link tương đối và byte/hash các file đóng gói.
- [ ] Mở package/ZIP trên máy nhận; kiểm có source, JDBC/cách lấy driver, schema/provisioning, hướng dẫn fixture, phân công và mọi file minh chứng được tham chiếu.
- [x] Tổng hợp bảng nghiệm thu: đã PASS, còn thiếu, giới hạn; tạo commit/PR khi có yêu cầu cập nhật repo. Không cần `.bak` nếu bộ script tái tạo đáp ứng yêu cầu nộp.

**Hoàn tất khi:** package tái tạo được từ repo, không có tham chiếu phụ thuộc máy gốc; kiểm cài/chạy và hash áp dụng đúng phiên bản cuối. Không commit mật khẩu DBA/token; bộ tài khoản QA môn học đã được cho phép lưu trong repo.

## 5. Thứ tự thực hiện

1. TV5-01 và TV5-02; có thể yêu cầu TV1–TV3 chuẩn bị bàn giao TV5-08 từ đầu.
2. TV5-03, TV5-04, TV5-05 và phối hợp TV5-07.
3. TV5-06 để phát hiện thiếu sót cài đặt sớm.
4. Nhận đủ đầu ra TV5-08, hoàn thiện TV5-09 và nghiệm thu TV5-10.

Đánh dấu từng việc sau khi có đầu ra thực tế. Xem [tiến độ tích hợp 15/10](TV5_THUC_HIEN_20261015.md) cho Word/deck, package và các việc còn chờ.

## Cập nhật sau PR #51/#52 — 16/10/2026

TV1 PR #51 và TV2 PR #52 đã merge. TV5 đã kiểm bộ TV1 (ERD 10 bảng, test 7/7, benchmark 75→10, slide riêng) và đối chiếu nội dung slide nhóm 1–3; đã cập nhật báo cáo, notes slide 2/3/5, đồng bộ hai Word và chép evidence Option B TV2 cuối vào package. Word giữ 69 trang tổng/56 nội dung; chỉ trang 11 và 33 đổi so với bản render trước. Deck vẫn 15 slide, render 2/3/5 không đổi, checks cấu trúc/layout/import đạt.

- TV5-08: hoàn tất kiểm evidence các thành viên; raw evidence TV1/TV2 gốc giữ nguyên.
- TV5-09: Word và nội dung deck đã đồng bộ. Slide nhóm 1–3 là bản tóm tắt đã đối chiếu với handoff TV1; bản chi tiết của TV1 vẫn nằm riêng trong package. PDF chỉ phát sinh nếu hình thức nộp yêu cầu.
- TV5-10: manifest toàn package và kiểm link/hash được cập nhật sau khi hoàn tất các file này. Chưa mở package trên máy nhận.
- Còn mở: TV5-05 Windows DPI thật; TV5-06 cài/chạy trên máy khác; rehearsal tay cả nhóm; PDF cuối nếu được yêu cầu. Các việc này cần phần cứng/máy nhận/người tham gia hoặc điều kiện nộp xác nhận.
