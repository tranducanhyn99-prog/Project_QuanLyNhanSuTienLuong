# TV5 — kiểm render cửa sổ tối thiểu 13/10/2026

Kiểm tự động các Swing frame thật bằng [LayoutQa.java](LayoutQa.java), JDK 21 / target Java 11, trên cùng máy Windows QA. LoginFrame **1000×700**, MainFrame **1024×680**, BaoCaoPanel trong frame thử **760×550**. Nút được kiểm nằm trong content bounds; 7 assertions mỗi lượt, tổng 21 PASS. Đã mở và xem ảnh render ở scale 1; ảnh mỗi lượt giữ riêng trong `scale-1`, `scale-1_25`, `scale-1_5`.

`sun.java2d.uiScale=1`, `1.25`, `1.5` đặt scale cho JVM mới; log xác nhận device transform tương ứng. Đây là kiểm scale của Java, **không thay đổi hoặc xác nhận Windows DPI ở ba mức**. Ảnh PNG được tạo bằng `frame.printAll` ở kích thước logical; không phải ảnh chụp desktop ở pixel vật lý. Session là fixture DB_Admin chỉ để kiểm layout. Report đang ở trạng thái tải, chưa xác nhận dữ liệu, bảng có nhiều dòng hoặc các quyền SQL trong lượt render này.

Không có SQL login/password trong harness; request report bị từ chối do không có authenticated SQL identity và ghi log. Cửa sổ được đóng sau render. SQL Server đang dừng ở thời điểm kiểm; setup follow-up thất bại trước khi tạo database hoặc login. Không gọi lỗi này là kết quả outage trên report thật.

Tái hiện từ thư mục `source/` của package sau build:

```powershell
javac --release 11 -encoding UTF-8 -cp 'build/verification/classes;lib/mssql-jdbc-12.6.1.jre11.jar' -d build/layout-qa ../evidence/TV5/layout-20261013/LayoutQa.java
java '-Dsun.java2d.uiScale=1.25' -cp 'build/layout-qa;build/verification/classes;src/resources;lib/mssql-jdbc-12.6.1.jre11.jar' LayoutQa build/layout-qa/output-1_25
```

Các case đổi kỳ/đổi phiên khi SQL trả chậm, bấm ghi lặp và report outage còn cần lượt live riêng; 21 assertions render không được cộng vào 231 assertions của bốn regression Java trước đó.
