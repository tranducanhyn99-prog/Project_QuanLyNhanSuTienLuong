# TV2 — KỊCH BẢN NÓI, DEMO VÀ VẤN ĐÁP (BẢN REVIEWED V2 ĐÃ KIỂM ĐỊNH)

- **Người trình bày:** Phạm Minh Quân (TV2 — MSSV: 24110311).
- **Source căn cứ:** `main` @ `f2a41f7c9bfc0b911613ed4355ccc79f8281ddd5`.
- **Thời lượng:** **Nói 02:00** (Slide 4–6, sau TV1 trước TV3) + **Demo 00:45** (trong mạch demo chung của cả nhóm).
- **Bằng chứng kiểm chứng:** Benchmark `run_20261013_140800` (Messages 574 ➔ 5 reads, giảm 99.1289%; XML 522 ➔ 5 reads, giảm 99.0421%); Suite SQL TV2 (21 PASS / 0 FAIL).

---

## 1. Script Thuyết trình Slide 4–6 (~02:00, 3 đoạn ~40 giây)

### Slide 4 — Nghiệp vụ và Ràng buộc CSDL (~40s)

"Em là Phạm Minh Quân, phụ trách phân hệ Chấm công. Vì chấm công là nguồn dữ liệu đầu vào trực tiếp để tính lương, nhóm áp dụng chiến lược kiểm soát nhiều tầng, chốt chặn vững chắc ngay tại CSDL chứ không chỉ dựa vào giao diện. 

Bảng CHAMCONG được chuẩn hóa 3NF với khóa chính tự sinh. Các quy tắc nghiệp vụ được ánh xạ trực tiếp thành các vật thể CSDL: tính duy nhất 'một nhân viên – một ngày đúng một bản ghi' do ràng buộc UNIQUE bảo đảm; ngày chấm công không vượt quá hiện tại, và trạng thái công chỉ nhận đúng 4 giá trị nghiệp vụ CO_MAT, DI_TRE, VE_SOM, VANG đều do CHECK constraint kiểm soát. Trên giao diện, 4 mã kỹ thuật này được nạp trực tiếp và hiển thị nguyên bản; khi trình bày người thuyết trình giải thích nghĩa tiếng Việt tương ứng. Riêng các quy tắc so sánh giữa hai cột — giờ ra phải sau giờ vào (được kiểm thử tại ca TC-CC-07) — hoặc cần kiểm tra trạng thái bảng khác — nhân viên phải đang làm việc (ca TC-CC-06) — được thực thi bởi Stored Procedure kết hợp 2 Trigger AFTER.

Nhờ mô hình phòng thủ đa tầng Service -> Procedure -> Constraint -> Trigger, các quy tắc nghiệp vụ cốt lõi luôn được chốt chặn cưỡng bức tại CSDL kể cả khi có câu lệnh ghi ngoài ứng dụng vi phạm điều kiện. Các câu lệnh SQL trực tiếp hợp lệ vẫn được thực thi bình thường, và cơ chế này bảo vệ theo phạm vi ràng buộc cụ thể chứ không bảo vệ tuyệt đối trước can thiệp đặc quyền mức quản trị viên (DBA)."

### Slide 5 — Stored Procedure, Trigger và Giao dịch nhập lô (~40s)

"Về các đối tượng xử lý: thủ tục `sp_GhiNhanChamCong` thực hiện 7 bước tiền kiểm tra nghiêm ngặt trước khi ghi và trả mã chấm công qua tham số OUTPUT. Thủ tục cố ý không tự mở transaction nội bộ, nhường quyền điều phối giao dịch cho tầng Java. Hai trigger AFTER của phân hệ kiểm tra giờ (TC-CC-07) và trạng thái nhân viên (TC-CC-06), áp dụng set-based cho mọi đường ghi. Khi kỳ lương đã chốt, trigger ở module 05 của bạn Trần Đức Anh sẽ khóa bảng lương bằng UPDLOCK và HOLDLOCK, hợp tập kỳ của cả inserted và deleted để chặn mọi hành vi thêm, sửa, xóa hay chuyển ngày vào kỳ đã khóa. 

Đặc biệt, nghiệp vụ điểm danh hàng loạt được xử lý theo nguyên tắc All-or-Nothing: Service mở duy nhất một connection, tắt auto-commit, lặp gọi thủ tục cho từng dòng; nếu toàn bộ hợp lệ thì commit (TC-CC-08), nhưng chỉ cần một dòng vi phạm thì rollback toàn bộ lô (TC-CC-09) — 0 bản ghi rác nào được lưu lại. Toàn bộ logic này đã đạt 21/21 PASS trong test suite tự động."

### Slide 6 — View tổng hợp, Covering Index và Nối sang Lương (~40s)

"Dữ liệu công được tổng hợp đa chiều qua view `vw_TongHopChamCongThang`: tính số ngày đi làm, đi trễ, về sớm, vắng và tổng giờ làm của từng nhân sự; tab Tổng hợp trên ứng dụng đọc trực tiếp view này. Cảnh báo NULL elimination phát sinh từ `COUNT(CASE...)` không có `ELSE` theo chuẩn ANSI SQL.

Để tối ưu hóa truy vấn chi tiết và tổng hợp, em thiết kế covering index với mệnh đề INCLUDE giờ vào, giờ ra, trạng thái. Thử nghiệm trên bảng tạm cách ly 30.000 dòng có forced INDEX hint cho thấy: baseline có seek từ UNIQUE constraint nhưng chịu Key Lookup từng dòng về Clustered Index; khi có covering index, kế hoạch chuyển sang Pure Index Seek. Số lượt đọc logic giảm mạnh từ 574 trang xuống chỉ còn 5 trang — cắt giảm 99.1289% lượt đọc logic theo Messages (từ 522 xuống 5, giảm 99.0421% theo XML plan operator counters), giảm thiểu truy xuất bộ đệm buffer pool. Con số 574 phản ánh buffer pool page reads của Storage Engine, còn 522 là operator counters trong XML; nguyên nhân kỹ thuật cụ thể của độ chênh 52 trang là UNKNOWN. Đây là phép đo so sánh logical reads trên warm cache, không tuyên bố về cắt giảm đọc đĩa vật lý và không phải cam kết latency môi trường sản xuất.

Về kết nối phân hệ: hàm `fn_TongPhuCap` do em phụ trách tại module 03 giúp tổng hợp phụ cấp cho thủ tục tính lương của TV4; còn ngày công, TV4 đếm trực tiếp bảng CHAMCONG trong giao dịch có khóa HOLDLOCK mà không thông qua `fn_TinhSoNgayCong` của TV1. Em xin chuyển phần trình bày cho bạn Trần Tiến Đạt với phân hệ Phụ cấp và Khấu trừ."

---

## 2. Kịch bản Demo Giao diện (00:45)

> **Cảnh báo môi trường và Xác nhận thực tế:**
> - Fixture demo 10/2026 (Nhân viên A, `gui-a@example.invalid`, `MaNV 1008`, mã 1013 ngày 09/10/2026) thuộc về môi trường **QA lịch sử (`PRJ_Fix_QA_20261007_01`)**.
> - Database QA ủy quyền hiện hành (`PRJ_Fix_QA_20261013_001534_ae185d81`) hiện có đúng **0 dòng** trong bảng `dbo.CHAMCONG` và chưa được nạp dữ liệu mẫu demo.
> - **Xác nhận thực tế:** Việc demo trực tiếp trên giao diện GUI chưa được kiểm chứng thực tế trên database QA rỗng hiện hành. 
> - **Nguyên tắc an toàn:** Tuyệt đối không tự ý chạy seed/insert vào DB live. Nếu môi trường bảo vệ chưa có sẵn dữ liệu, người trình bày trình bày báo cáo kiểm thử tự động 21/21 PASS (`test_module_chamcong_TV2.log`) và 4 file actual execution plan thực tế làm minh chứng chính. Các bước dưới đây là kịch bản chuẩn bị sẵn trong trường hợp Hội đồng cấp môi trường đã có sẵn dữ liệu demo.

| Mốc | Thao tác (Tài khoản `gui_hr`) | Lời nói đi kèm | Căn cứ xác thực |
|---|---|---|---|
| **0–10s** | Mở **Chấm công ➔ Nhật ký chấm công**, chọn kỳ Tháng **10 / 2026** | "Em mở màn hình Nhật ký chấm công và lọc kỳ tháng 10 năm 2026." | Bảng nhật ký tải dữ liệu tự động qua change listener trên giao diện. |
| **10–25s** | Chỉ dòng công của nhân viên A (08:00–17:00, trạng thái `CO_MAT`) | "Mỗi nhân viên có đúng một dòng công trong ngày — đây là dữ liệu đã ghi nhận hợp lệ, em không thao tác thêm mới để bảo toàn dữ liệu đối soát." | Ràng buộc BR-CC-01 và ca kiểm thử G06 đã PASS. |
| **25–35s** | Chuyển sang tab **Tổng hợp theo tháng**, bấm **Xem tổng hợp** | "Tab này đọc trực tiếp từ view `vw_TongHopChamCongThang`." | Hiển thị 1 ngày công, 9.00 giờ làm, trễ/sớm/vắng = 0 (khớp ca G07). |
| **35–45s** | Không thao tác thêm; nhấn mạnh giao dịch lô và bàn giao | "Nghiệp vụ nhập lô hoạt động trong một giao dịch: lỗi một dòng là rollback cả lô — phần này đã có log suite kiểm thử tự động 21/21 PASS (TC-CC-08, TC-CC-09). Em xin chuyển màn hình cho bạn Trần Tiến Đạt trình bày phân hệ Phụ cấp và Khấu trừ." | Đúng ownership, không nhận demo tay nhập lô khi chưa chuẩn bị kịch bản lỗi. |

---

## 3. Câu hỏi Vấn đáp Dự kiến và Trả lời Chuẩn (12 Câu)

1. **Vì sao cần cả SP, Constraint lẫn Trigger?**
   * *Trả lời:* SP validate đầu vào sớm và trả thông báo lỗi thân thiện cho UI; Constraint bảo vệ toàn vẹn miền giá trị và quan hệ bảng ngay tại storage engine; Trigger thực thi các quy tắc liên bảng (trạng thái nhân viên) hoặc so sánh giữa các cột (giờ ra > giờ vào) áp dụng set-based cho mọi thao tác INSERT/UPDATE. Ba tầng bổ trợ nhau chặt chẽ.

2. **Nếu hai phiên cùng ghi nhận công cho một nhân viên trong cùng ngày thì sao?**
   * *Trả lời:* Bước kiểm tra IF EXISTS trong SP là giải pháp 'best-effort' phát hiện sớm; chốt chặn tuyệt đối cuối cùng là ràng buộc `UQ_CHAMCONG_MaNV_Ngay` ở mức CSDL. Khi xảy ra tranh chấp ghi đồng thời, chỉ 1 phiên ghi thành công, phiên còn lại sẽ nhận lỗi vi phạm UNIQUE constraint từ SQL Server và tự động rollback.

3. **Khi nhập lô, nếu dòng thứ 50 bị lỗi thì 49 dòng trước xử lý thế nào?**
   * *Trả lời:* Toàn bộ 49 dòng trước đó lập tức bị hủy bỏ (ROLLBACK). Lớp Java Service mở một kết nối duy nhất với `setAutoCommit(false)`, nếu bất kỳ dòng nào ném ngoại lệ thì khối `catch` gọi `conn.rollback()`, đảm bảo nguyên tắc All-or-Nothing, không lưu rác. Minh chứng: testcase `TC-CC-09` trong suite test đạt PASS.

4. **Tại sao trigger chốt lương phải kiểm tra cả ngày cũ lẫn ngày mới khi UPDATE?**
   * *Trả lời:* Một bản ghi có thể bị sửa ngày từ một kỳ chưa chốt sang kỳ đã chốt (hoặc ngược lại). Trigger của TV5 thực hiện phép UNION giữa bảng `inserted` và `deleted` rồi khóa bảng `BANGLUONG` bằng UPDLOCK+HOLDLOCK, chặn đứng mọi hành vi lách luật từ cả hai chiều.

5. **Covering Index giải quyết bài toán gì, đánh đổi thế nào?**
   * *Trả lời:* Baseline không phải là Table Scan: ràng buộc UNIQUE đã có sẵn index seek theo `(MaNV, NgayChamCong)`, nhưng mỗi dòng tìm được phải Key Lookup về Clustered Index để lấy giờ vào, giờ ra, trạng thái. Covering Index đưa 3 cột này vào lá qua mệnh đề INCLUDE, biến kế hoạch thành Pure Index Seek. Đánh đổi: tăng dung lượng đĩa và thêm chi phí cập nhật index khi INSERT/UPDATE.

6. **Số liệu đo lường Benchmark của phân hệ Chấm công là bao nhiêu và giải thích độ chênh 574 vs 522?**
   * *Trả lời chuẩn:* "Lượt đo chính thức trên 30.000 bản ghi bảng tạm cách ly (Run ID: `run_20261013_140800`): Cả truy vấn chi tiết và tổng hợp đều giảm từ 574 xuống còn 5 logical reads theo Messages `SET STATISTICS IO` (giảm 99.1289%) và từ 522 xuống còn 5 logical reads theo XML Execution Plan runtime counters (giảm 99.0421%). Con số 574 phản ánh tổng số trang đọc ở tầng Storage Engine buffer pool; con số 522 là số trang ghi nhận từ per-operator counters trong XML Execution Plan. Nguyên nhân kỹ thuật cụ thể của 52 trang chênh lệch là UNKNOWN. Điểm mấu chốt là khi có covering index, cả hai phương thức đo đều ghi nhận chính xác 5 trang đọc logic và 0 Key Lookup; đây là phép đo logical reads trên warm cache, không tuyên bố về cắt giảm đọc đĩa vật lý."

7. **Ai sở hữu hàm `fn_TinhSoNgayCong`, phân hệ Lương có dùng hàm này không?**
   * *Trả lời:* Hàm `fn_TinhSoNgayCong` do bạn TV1 sở hữu (dù được đặt trong script 02). Phân hệ Lương (`sp_TinhBangLuongThang`, TV4) **không gọi hàm này**, mà đếm trực tiếp bảng CHAMCONG trong khoảng ngày nửa mở có khóa `WITH (HOLDLOCK)` để tối ưu hóa sargable query và chống race condition.

8. **Hàm `fn_TongPhuCap` làm nhiệm vụ gì, ai phụ trách?**
   * *Trả lời:* Theo ma trận phân công, đây là hàm do em (TV2) phụ trách, được cài đặt tại `03_phucap_khautru_TV3.sql:214–224` trên dữ liệu bảng phụ cấp của TV3. Hàm nhận `(MaNV, Thang, Nam)`, tính tổng số tiền phụ cấp trong kỳ và trả về `ISNULL(SUM(SoTien), 0)`. Thủ tục tính lương gọi hàm này có guard kiểm tra `OBJECT_ID`.

9. **Tài khoản Lương (Payroll) có quyền sửa bảng Chấm công không?**
   * *Trả lời:* Hoàn toàn không. Ở mức CSDL, vai trò `role_PayrollOfficer` chỉ được cấp quyền SELECT trên CHAMCONG và bị DENY rõ ràng với INSERT, UPDATE, DELETE. Trên giao diện, form chấm công tự động vô hiệu hóa toàn bộ nút ghi nhận, nhập lô, xóa khi phát hiện tài khoản Payroll.

10. **Khi kỳ lương đã đóng, cơ chế nào bảo vệ bảng Chấm công?**
    * *Trả lời:* Trigger `trg_ChamCong_KhongSuaKhiDaChotLuong` (module 05 của TV5) bắt mọi sự kiện INSERT/UPDATE/DELETE trên CHAMCONG, kiểm tra trạng thái kỳ trên BANGLUONG bằng khóa UPDLOCK+HOLDLOCK, chặn đứng mọi thao tác sửa đổi sau khi kỳ chuyển sang `DA_CHOT`.

11. **Tại sao trạng thái công lưu mã kỹ thuật thay vì tiếng Việt?**
    * *Trả lời:* Lưu mã kỹ thuật (`CO_MAT, DI_TRE, VE_SOM, VANG`) đảm bảo tính chuẩn hóa, tiết kiệm bộ nhớ, bất biến theo ngôn ngữ và được kiểm soát chặt chẽ bằng ràng buộc CHECK. Việc hiển thị tiếng Việt thuộc trách nhiệm tầng presentation; bản UI hiện tại hiển thị trực tiếp mã nghiệp vụ chuẩn và ánh xạ nghĩa khi giao tiếp.

12. **Minh chứng kiểm thử phân hệ Chấm công được lưu trữ ở đâu?**
    * *Trả lời:* Phân hệ có đầy đủ bộ bằng chứng kiểm thử tại gói nộp: log test suite SQL đạt 21/21 PASS (`test_module_chamcong_TV2.log`), bộ 4 file actual execution plan (`.sqlplan`), log I/O và TIME, bảng so sánh kết quả 100% khớp dữ liệu, và log xuất schema hiện hành (`TV2_live_schema_inspection.log`).
