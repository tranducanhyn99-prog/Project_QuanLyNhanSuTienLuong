> Ghi chú đóng gói TV5 ngày 16/10/2026: bốn tệp companion nêu trong báo cáo không có trong nguồn bàn giao nên không được đưa vào package. Các tên tệp được giữ dạng văn bản để tránh liên kết hỏng; phần log/evidence thực có trong thư mục này được giữ nguyên.

# PR #52 — Resolve-SqlEndpoint: báo cáo xác minh cuối

STATUS: PASS — kiểm thử và review độc lập cuối đều thành công.

[Review cuối](codex_review_round2.md): PASS; không có finding cần sửa. Codex đã đối chiếu kết luận với diff, hash nguồn và log cuối. Hai finding VALID/TASK_CAUSED của vòng 1 đã được xử lý trong cùng vòng khắc phục được ủy quyền.

## Nguyên nhân và phạm vi sửa

Resolver trước đây gán `InstanceName = MSSQLSERVER` cho endpoint không khai báo instance. Vì kiểm tra trước `CREATE DATABASE` so sánh giá trị này với identity thực tế, `qa-host,14333` kết nối tới `QA_INST` bị từ chối sai.

Chỉ hai tệp mã được sửa:
- `database/tests_TV2/run_tv2_jdbc_optionb.ps1`, dòng 99–146: tách yêu cầu `MSSQLSERVER` tường minh khỏi alias cục bộ; host/port và hostname thường trả instance kỳ vọng null. Endpoint host/port và instance tường minh giữ nguyên cách dựng SQLCMD/JDBC.
- `database/tests_TV2/test_tv5_findings.ps1`, dòng 150 trở đi và 603 trở đi: mở rộng kiểm thử endpoint, production runner A–H; driver JDBC tạm thời chạy main thật ở ca G; `exit 0` sau khi mọi assertion thành công để không giữ exit code của ca âm cuối.

Không sửa SQL nghiệp vụ, Service/DAO, trigger, Java integration harness, Word/PPTX, manifest hoặc benchmark. Thay đổi đường dẫn evidence mặc định ở bản trung gian đã được hoàn nguyên.

## Ma trận endpoint

Các dòng mô phỏng dưới đây là bằng chứng hành vi, không phải kết nối thật tới qa-host hay QA_INST.

| Ca | Cấu hình / identity quan sát | Kết quả |
|---|---|---|
| A | `qa-host,14333 → QA_INST` | Qua identity trước CREATE; mock pipeline exit 0 |
| B | `qa-host:14333 → QA_INST` | Qua identity trước CREATE; exit 0 |
| C | `qa-host\QA_INST,14333 → QA_INST` | Instance tường minh khớp; exit 0 |
| D | `qa-host\QA_INST,14333 → WRONG_INST` | Từ chối, exit 1; không gọi CREATE |
| E | `MSSQLSERVER → QA_INST` | Từ chối, exit 1; không gọi CREATE |
| F | `.` mặc định | Mock và live local thành công |
| G | SQLCMD `QA_INST`, JDBC `OTHER_INST` | Main Java thật từ chối; số lần ghi fixture = 0; runner exit 1 |
| H | Unported instance, delimiter injection, cú pháp mơ hồ, port ngoài khoảng | Runner từ chối, exit 1 |

Codex kiểm tra riêng dưới Windows PowerShell 5.1: 9/9 cấu hình gồm `.`, `localhost`, `(local)`, hostname, hai dạng port, named instance, và hai dạng MSSQLSERVER tường minh. Xem [ma trận độc lập](codex_endpoint_matrix_ps51.json).

## Kiểm thử cuối: bằng chứng mới từ Anti

Thư mục evidence: [20261016_003726_1ade8e9e](20261016_003726_1ade8e9e/commands_hash_count_summary.md).
Run ID thực tế của runner: `20261016_003727_46446d86`. Tên thư mục evidence được truyền bằng `-EvidenceOutputDir`, do đó khác Run ID của database.

| Kiểm tra | Kết quả thực tế | Exit |
|---|---|---|
| Windows PowerShell 5.1 syntax | 2 tệp, 0 lỗi | 0 |
| Java `--release 11` | 55 nguồn biên dịch | 0 |
| Controlled suite | 179/179 | 0 |
| SecurityRegressionTest (offline) | 104 assertion | 0 |
| GuiQaRegressionTest (offline) | 88 assertion | 0 |
| SQL suite | 21/21 | 0 |
| Concurrency | 4/4, có cleanup fixture | 0 |
| JDBC live | 6 scenario, 105 assertion | 0 |
| QA runner tổng thể | Thành công và cleanup | 0 |

Codex đã đọc log cuối, đếm 105 dòng JDBC PASS, 0 failure marker, 1 completion marker và đúng total 105. Controlled log ghi 179/179 và các assertion chuyển identity, main thật, mismatch thật, zero-write. Không chạy lại các kiểm thử thành công chỉ để lặp bằng chứng.

## Bảo vệ identity và dữ liệu

SQLCMD vẫn truy vấn ServerName, InstanceName, MachineName, ProcessID; kiểm tra kỳ vọng instance chỉ áp dụng khi cấu hình khai báo nó. Cả bốn identity thực tế được truyền sang Java bằng `qa.expected.*`, kể cả khi resolver không có tên instance kỳ vọng.

Ca G dùng JDBC SPI và proxy chuẩn của Java, chỉ trong thư mục test tạm. Runner vẫn chạy `com.test.Tv2JdbcIntegrationTest`; driver trả `OTHER_INST`, guard thật ném SecurityException. Driver quan sát các lệnh ghi, shutdown hook báo 0. Không dùng thông báo lỗi viết sẵn hoặc main thay thế.

Kiểm tra tên database, ownership marker và cleanup có guard giữ nguyên. Canonical và package SQL vẫn cùng SHA-256 `4DBB78FC0B3946D4048FB779EB5111F10748BB81D7F608B6DDF1F69ACACFD471`.

## Database QA và cleanup

Database cuối: `PRJ_Fix_QA_20261016_003727_46446d86`.
Log Anti xác nhận ownership marker khớp trước DROP và truy vấn absence trả count 0, sqlcmd exit 0:
- [cleanup](20261016_003726_1ade8e9e/environment_cleanup.log)
- [database absence](20261016_003726_1ade8e9e/database_absence_verification.log)

Codex thử truy vấn SQL đọc độc lập từ sandbox nhưng sqlcmd trả exit 1 với lỗi credential/SSL của Windows. Không nâng quyền hoặc thay credentials; bằng chứng live/absence ở trên do Anti thực thi và Codex kiểm tra artifact.

## Lịch sử và bảo toàn evidence

Đối chiếu SHA-256 trước/sau: 161/161 tệp evidence cũ không thay đổi, gồm toàn bộ cây bàn giao TV2 hiện có, `20261015_175527_ea6da333`, `20261015_220912_5d4fb6eb` và review cũ. Xem `codex_historical_sha256.json` (không có trong nguồn nhận) và [đối chiếu](codex_preservation_check.json).

Các lượt mới trung gian đều được giữ lại:
- `20261016_001552_86d55526`: runner exit 1 trước tạo database, do suite giữ lại exit code của ca âm; đã thêm exit 0 cuối suite sau các assertion.
- `20261016_001640_f452428b`: 170 controlled checks và live thành công; review xác định ca G còn là lỗi mô phỏng viết sẵn.
- `20261016_002851_f39f464a`: 177 checks và live thành công; probe vẫn chưa kiểm tra luồng main thực tế.
- `20261016_003726_1ade8e9e`: 179 checks, main thật và driver theo dõi ghi; bằng chứng cuối.

Không coi các lượt trung gian là proof cho ca G cuối. Không ghi đè log cũ và không chạy benchmark covering-index.

## Xác minh độc lập và bàn giao Git

Codex xác nhận parser Desktop `5.1.26100.7920`, 0 lỗi trên đúng hash cuối; `codex_ps51_parse.json` (không có trong nguồn nhận). `codex_focused_diff.patch` (không có trong nguồn nhận) được lưu UTF-8; `git diff --check` exit 0.

Hash cuối:
- Runner: `60AB7EC6C6CFD10849B5BC60A636D49B83E18789A97D03FCA3A6E53776EF9111`
- Controlled tests: `8EE3AA211E9EB7EEF4FB8F757874C1F8BCB32803BD30257FC6C24682CF8DD09F`

HEAD vẫn `fd3db422556d3a703bc51b6671acb01d03405ebc`, nhánh `fix/tv2-sp-try-catch`. Chỉ dùng Git đọc status/diff/HEAD; không fetch, pull, checkout, stage, commit, push, merge/rebase hoặc sửa PR. PR #52 chưa được cập nhật bởi lượt này.

Danh sách đầy đủ tệp sửa/tạo của lượt này: `codex_changed_files.txt`. Những tệp untracked đã tồn tại trước lượt làm việc không thuộc danh sách này. Bridge job nội bộ: `2026-10-15-011`, `2026-10-15-012`.

Giới hạn: live chỉ kiểm tra SQL Server local mặc định; QA_INST là mô phỏng có thực thi Java thật. GUI suite không thay thế thao tác UI thủ công. Không có commit/push; người dùng tự chọn tệp để stage/commit/push.
