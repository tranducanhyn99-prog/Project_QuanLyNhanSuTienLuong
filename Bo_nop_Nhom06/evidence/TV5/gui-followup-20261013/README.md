# TV5 — kiểm GUI tích hợp tự động 13/10/2026

QA riêng `PRJ_Fix_QA_20261013_followup_8e2bedb8`, năm login `tv5f_d54a94da_*`, mật khẩu fixture môn học từ manifest. Modules 01→05 + seed 06, provisioning, năm application login thật và wrong-password rejection PASS. SQL Server được khởi động theo ủy quyền trước lượt này; không dừng instance để gây outage. Đây là QA mới trên **cùng máy**, chưa nghiệm thu máy khác.

[LiveGuiQa.java](LiveGuiQa.java) chạy Swing frame/panel thật trên EDT, gọi nút hoặc handler production, DAO/service và JDBC thật. Dialog được đáp tự động trên fixture được phép. Các ảnh từ harness là **render `printAll`, không phải native desktop screenshots**; log ghi từng dialog và SQL assertions. Không gọi lượt này là demo tay/rehearsal.

| Ca / log | Assertions PASS | Kiểm thực tế |
|---|---:|---|
| [Admin thiếu quyền](admin-denied.log) | 5 | List năm profile; lock/reset bị từ chối; profile/login password/role giữ nguyên |
| [Admin đủ quyền](admin-allowed.log) | 12 | Lock SQL disabled và login trả 18470; unlock/login cũ; reset từ chối password cũ/nhận password mới; profile/SQL role HR_Manager |
| [Báo cáo outage](report-outage.log) | 6 | Đổi kỳ 12 lần, kỳ cuối có cùng row count/tổng SQL 1.230.000đ; ngắt relay 14514, lỗi ngắn, nút phục hồi, row cũ được xóa, nối lại/load thành công |
| [Write đang chạy](inflight-java.stdout.log), [SQL controller](inflight-controller.log) | 6 | SQL DMV chứng minh SPID56 đang trong `sp_AdminSetStatus`, chờ LCK_M_U/UPDLOCK trên SPID55; một request; ngắt relay thật, EDT/nút phục hồi, không tự gửi lại |
| [Payroll lifecycle](payroll-lifecycle.log) | 11 | Nguồn fixture SQL→tính GUI 1.230.000đ→chốt DA_CHOT→Employee A hai own slips→HR/Payroll nav theo role→mở lại CHUA_CHOT |
| [Phiên cũ trả chậm](session-java.stdout.log), [controller](session-controller.log) | 9 | Xác minh Admin SQL bị block trước logout; login Employee A/gửi request mới khi blocker còn giữ; callback Admin bị bỏ, Employee hai phiếu riêng/9 cột/menu home-report đúng |
| [TV1 CRUD sau sửa](tv1-crud-fixed.log) | 17 | PB/CV CRUD và dữ liệu sai; NV chưa đủ 18 bị từ chối, duplicate profile rollback, NV create/update/delete và protected delete |
| [Phone optional](phone-optional.log) | 3 | GUI add/update phone trống hoặc whitespace lưu SQL NULL; xóa fixture |
| [TV3](tv3-allowances.log) | 10 | Từ chối thêm/xóa nguồn/header kỳ chốt; thêm/xóa PC/KT ở nháp, xóa header/detail nháp không orphan |
| [TV2](tv2-attendance.log) | 8 | GUI batch rollback sau hai INSERT trước duplicate ở dòng cuối, batch hợp lệ commit ba dòng; monthly dialog đổi A thành DI_TRE/09:00, view tháng khớp |
| **Tổng lõi** | **87** | Không cộng assertions SQL/identity lịch sử hoặc layout/offline vào số này |

TV2 được instrument bằng sequence/trigger **chỉ trong QA**: sequence không rollback, tăng hai bước trong lần lỗi trong khi các row A/B không tồn tại sau Java rollback. Đây là bằng chứng đường Java transaction thật; không sửa SP TV2 để thêm TRY…CATCH hình thức. `ChamCongService` giữ transaction/rollback toàn lô. Fixture và instrumentation bị xóa cùng database khi cleanup.

Outage write xảy ra khi transaction đã bắt đầu và đang chờ lấy profile, **trước ALTER LOGIN/UPDATE**. [Controller](inflight-controller.log) kiểm audit=0/profile active=1/login enabled=1 sau khi bỏ blocker, trước GUI reload. Không suy ra mọi tình huống mất mạng sau COMMIT đều rollback; ứng dụng không tự retry và người dùng phải đối chiếu trạng thái nếu commit không rõ. Relay chỉ ảnh hưởng kết nối QA 14514→1433.

Admin cần server permission `ALTER ANY LOGIN` cho ENABLE/DISABLE; grant riêng LOGIN không đủ trong thử trước đó. Quyền server được cấp tạm cho owned Admin trong wrapper, chỉ target owned B; REVOKE trong finally. [State sau admin](after-admin-seed.tsv), [controller](inflight-controller.log) và [cleanup](cleanup-first.log) xác nhận không còn ALTER ANY LOGIN/CONTROL SERVER. Không cấp sysadmin/CONTROL SERVER. Xem [Microsoft Learn: ALTER LOGIN](https://learn.microsoft.com/en-us/sql/t-sql/statements/alter-login-transact-sql?view=sql-server-ver17).

Lỗi phone ban đầu trả raw CHECK exception dài; `DanhMucService` dùng validation chung cho add/update, ASCII 10–15 digits, blank/null→NULL. [Focused retest cuối](phone-final.log) **7 PASS riêng**, không cộng vào 87, kiểm câu cuối ngắn, reject giữ SQL và optional NULL. [Native modal](phone-native-1.png) và [native frame](phone-native-0.png) do Computer Use chụp thực tế cho thấy câu cuối đọc đủ. Đây là probe offline dùng production Service để nhận cùng lỗi; SQL effects ở focused live retest. `printAll` dùng BufferedImage context khác native font/DPI nên các ảnh dialog render có thể cắt cuối; chúng không được dùng để kết luận lỗi layout native. Không sửa ảnh để che phần cắt.

Layout/scale xem [lượt render riêng](../layout-20261013/README.md). GUI ở MainFrame minimum 1024×680 có dữ liệu thực; bảng dùng horizontal scrollbar. Chưa thay Windows DPI của người dùng hoặc kiểm mọi kích thước/dữ liệu lớn. Giai đoạn GUI lõi dùng module01 trước birthday DDL fix; Java age đã kiểm đúng. Birthday CHECK SQL được chứng minh trên **fresh age QA riêng**, [evidence](../age-boundaries-20261013/README.md), không tự migration GUI/database cũ.

[GuiQa cuối](GuiQaRegressionTest-88-final.log) **88**, [Security](SecurityRegressionTest-104.log) **104** đã rerun sau đổi age tests/phone Service. LightTheme45/LoginChip20 từ lượt trước không bị ảnh hưởng; tổng current regression **257**. Bản source package được copy vào thư mục sạch không Git, compile54 / target11 và chạy lại [GuiQa88](package-guiqa-final.log), [Security104](package-security.log); đây là cùng máy, chưa phải máy nhận.

[Cleanup lõi](cleanup-first.log) và [cleanup retest cuối](cleanup-phone-final.log): database và năm owned login count0, grant tạm0, relay/Java QA được dọn. Lượt phone retest tái tạo đúng namespace đã dọn bằng fresh schema, rồi dọn lại. [Probe cleanup](dialog-probe-cleanup.log) đóng owned native dialog probe. SQL Server giữ Running; không phát lệnh DML/DDL vào database/login dùng chung. Một fixture session thử ban đầu chờ quá lâu và hết timeout; bản cuối thêm xác minh SQL trước logout/release sớm, không coi fixture lỗi là production bug.
