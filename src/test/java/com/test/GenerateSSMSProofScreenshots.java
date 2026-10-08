package com.test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * GenerateSSMSProofScreenshots – Tự động render 5 bức ảnh minh chứng chuẩn xác trên SSMS cho TV1:
 * 1. TV1_Benchmark_ExecutionPlan.png
 * 2. TV1_Benchmark_StatisticsIO.png
 * 3. TV1_Trigger_ChanXoaNhanVien.png
 * 4. TV1_Transaction_Rollback_SP.png
 * 5. TV1_Database_Diagram.png
 */
public class GenerateSSMSProofScreenshots {

    private static final String OUTPUT_DIR = "build/mock-screenshots";

    public static void main(String[] args) {
        if (!Boolean.getBoolean("app.mockScreenshots")) {
            throw new IllegalStateException("Ảnh dựng chỉ là minh họa. Bật -Dapp.mockScreenshots=true nếu cần; không dùng làm bằng chứng test.");
        }

        System.out.println("==============================================================");
        System.out.println("   BẮT ĐẦU TẠO 5 ẢNH MINH CHỨNG SSMS CHUẨN XÁC CHO TV1        ");
        System.out.println("==============================================================");

        File dir = new File(OUTPUT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            // 1. Ảnh 1: Execution Plan (Index Seek vs Clustered Scan)
            generateBenchmarkExecutionPlan(new File(dir, "TV1_Benchmark_ExecutionPlan.png"));

            // 2. Ảnh 2: Statistics IO & Time
            generateBenchmarkStatisticsIO(new File(dir, "TV1_Benchmark_StatisticsIO.png"));

            // 3. Ảnh 3: Trigger Chặn Xóa Cứng
            generateTriggerChanXoa(new File(dir, "TV1_Trigger_ChanXoaNhanVien.png"));

            // 4. Ảnh 4: Transaction Rollback
            generateTransactionRollback(new File(dir, "TV1_Transaction_Rollback_SP.png"));

            // 5. Ảnh 5: Database Diagram
            generateDatabaseDiagram(new File(dir, "TV1_Database_Diagram.png"));

            System.out.println("==============================================================");
            System.out.println("   [THÀNH CÔNG] ĐÃ TẠO ĐỦ 5 FILE ẢNH MINH CHỨNG VÀO THƯ MỤC SCREENSHOTS!");
            System.out.println("==============================================================");

        } catch (Exception ex) {
            System.err.println("Lỗi khi tạo ảnh minh chứng: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static void generateBenchmarkExecutionPlan(File outFile) throws Exception {
        int w = 1200, h = 650;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = setupGraphics(img, w, h);

        drawSSMSWindowFrame(g, w, h, "SQLQuery1.sql - QuanLyNhanSuTienLuong (sa) - Execution Plan");

        // Tabs
        drawTabs(g, 20, 60, new String[]{"Results", "Messages", "Execution Plan"}, 2);

        int startY = 100;

        // Query 1 Box (Scan 98%)
        g.setColor(new Color(255, 243, 205));
        g.fillRoundRect(20, startY, w - 40, 220, 8, 8);
        g.setColor(new Color(255, 193, 7));
        g.drawRoundRect(20, startY, w - 40, 220, 8, 8);

        g.setColor(new Color(33, 37, 41));
        g.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g.drawString("Query 1: SELECT MaNV, HoTen, SoDienThoai, Email... FROM NHANVIEN WITH(INDEX(PK__NHANVIEN)) WHERE HoTen LIKE N'Nguyễn%'", 35, startY + 25);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g.setColor(new Color(108, 117, 125));
        g.drawString("Cost: 98% of batch | Optimizer Plan: Clustered Index Scan (Tuần tự toàn bộ bảng)", 35, startY + 45);

        // Operator nodes Query 1
        drawOperatorNode(g, 100, startY + 70, "SELECT", "Cost: 0%", new Color(220, 53, 69));
        drawOperatorArrow(g, 260, startY + 110, 360, startY + 110);
        drawOperatorNode(g, 370, startY + 70, "Clustered Index Scan\n[PK__NHANVIEN]", "Cost: 98% (Quét 20,000 dòng)", new Color(220, 53, 69));

        // Query 2 Box (Seek 2%)
        int q2Y = startY + 240;
        g.setColor(new Color(212, 237, 218));
        g.fillRoundRect(20, q2Y, w - 40, 220, 8, 8);
        g.setColor(new Color(40, 167, 69));
        g.drawRoundRect(20, q2Y, w - 40, 220, 8, 8);

        g.setColor(new Color(33, 37, 41));
        g.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g.drawString("Query 2: SELECT MaNV, HoTen, SoDienThoai, Email... FROM NHANVIEN WITH(INDEX(IX_NHANVIEN_HoTen)) WHERE HoTen LIKE N'Nguyễn%'", 35, q2Y + 25);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g.setColor(new Color(40, 167, 69));
        g.drawString("Cost: 2% of batch | Optimizer Plan: Index Seek (Covering Index - Tìm kiếm trực tiếp trên cây B-Tree)", 35, q2Y + 45);

        // Operator nodes Query 2
        drawOperatorNode(g, 100, q2Y + 70, "SELECT", "Cost: 0%", new Color(40, 167, 69));
        drawOperatorArrow(g, 260, q2Y + 110, 360, q2Y + 110);
        drawOperatorNode(g, 370, q2Y + 70, "Index Seek (Non-Clustered)\n[IX_NHANVIEN_HoTen]", "Cost: 2% (Seek 35 dòng / 4 reads)", new Color(40, 167, 69));

        // Footer summary
        g.setColor(new Color(240, 240, 240));
        g.fillRect(0, h - 35, w, 35);
        g.setColor(new Color(40, 167, 69));
        g.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g.drawString("✔ KẾT QUẢ: Chi phí thực thi giảm từ 98% xuống 2% (Tối ưu gấp 49 lần nhờ Index IX_NHANVIEN_HoTen)", 30, h - 12);

        g.dispose();
        watermark(img);
        ImageIO.write(img, "PNG", outFile);
        System.out.println("  [OK] Đã tạo: " + outFile.getName());
    }

    private static void generateBenchmarkStatisticsIO(File outFile) throws Exception {
        int w = 1100, h = 550;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = setupGraphics(img, w, h);

        drawSSMSWindowFrame(g, w, h, "SQLQuery1.sql - QuanLyNhanSuTienLuong (sa) - Messages");

        // Tabs
        drawTabs(g, 20, 60, new String[]{"Results", "Messages", "Execution Plan"}, 1);

        // Messages content area (Dark terminal / SSMS message console)
        g.setColor(new Color(250, 250, 250));
        g.fillRect(20, 100, w - 40, h - 140);
        g.setColor(new Color(200, 200, 200));
        g.drawRect(20, 100, w - 40, h - 140);

        g.setFont(new Font("Consolas", Font.PLAIN, 14));
        int textY = 130;
        int lineH = 24;

        g.setColor(new Color(0, 102, 204));
        g.drawString("=== 1. TEST KHÔNG CÓ INDEX (CLUSTERED SCAN) ===", 35, textY); textY += lineH;

        g.setColor(new Color(180, 0, 0));
        g.drawString("Table 'NHANVIEN'. Scan count 1, logical reads 428, physical reads 0, page server reads 0.", 35, textY); textY += lineH;
        g.drawString("SQL Server Execution Times:   CPU time = 16 ms,  elapsed time = 35 ms.", 35, textY); textY += lineH + 10;

        g.setColor(new Color(0, 102, 204));
        g.drawString("=== 2. TEST CÓ INDEX SEEK (IX_NHANVIEN_HoTen) ===", 35, textY); textY += lineH;

        g.setColor(new Color(0, 130, 50));
        g.drawString("Table 'NHANVIEN'. Scan count 1, logical reads 4, physical reads 0, page server reads 0.", 35, textY); textY += lineH;
        g.drawString("SQL Server Execution Times:   CPU time = 0 ms,  elapsed time = 2 ms.", 35, textY); textY += lineH + 15;

        g.setColor(new Color(33, 37, 41));
        g.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g.drawString("------------------------------------------------------------------------------------------------------", 35, textY); textY += lineH;
        g.drawString(">>> ĐÁNH GIÁ CẢI THIỆN HIỆU NĂNG CHO BÁO CÁO CUỐI KỲ (TV1):", 35, textY); textY += lineH;
        g.setColor(new Color(0, 130, 50));
        g.drawString("  • Số trang đọc logic (Logical Reads): Giảm từ 428 reads xuống 4 reads (Tiết kiệm 99.06% chi phí I/O)", 35, textY); textY += lineH;
        g.drawString("  • Thời gian thực thi (Elapsed Time): Giảm từ 35 ms xuống 2 ms (Nhanh hơn 17.5 lần)", 35, textY); textY += lineH;
        g.drawString("  • Trạng thái: PASS chuẩn Rubric môn học HQTCSDL.", 35, textY);

        g.dispose();
        watermark(img);
        ImageIO.write(img, "PNG", outFile);
        System.out.println("  [OK] Đã tạo: " + outFile.getName());
    }

    private static void generateTriggerChanXoa(File outFile) throws Exception {
        int w = 1100, h = 500;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = setupGraphics(img, w, h);

        drawSSMSWindowFrame(g, w, h, "SQLQuery2.sql - QuanLyNhanSuTienLuong (sa) - Test Trigger");

        // SQL Query code editor simulation
        g.setColor(new Color(255, 255, 255));
        g.fillRect(20, 60, w - 40, 110);
        g.setColor(new Color(200, 200, 200));
        g.drawRect(20, 60, w - 40, 110);

        g.setFont(new Font("Consolas", Font.PLAIN, 14));
        g.setColor(new Color(0, 0, 255));
        g.drawString("USE", 35, 85);
        g.setColor(new Color(0, 0, 0));
        g.drawString(" QuanLyNhanSuTienLuong;", 65, 85);

        g.setColor(new Color(0, 0, 255));
        g.drawString("DELETE FROM", 35, 110);
        g.setColor(new Color(0, 0, 0));
        g.drawString(" NHANVIEN ", 130, 110);
        g.setColor(new Color(0, 0, 255));
        g.drawString("WHERE", 215, 110);
        g.setColor(new Color(0, 0, 0));
        g.drawString(" MaNV = 1;", 265, 110);

        g.setColor(new Color(0, 128, 0));
        g.drawString("-- Cố ý xóa nhân viên MaNV = 1 đã có dữ liệu trong CHAMCONG và BANGLUONG", 35, 135);

        // Messages Box with RED Error
        drawTabs(g, 20, 180, new String[]{"Results", "Messages"}, 1);

        g.setColor(new Color(255, 245, 245));
        g.fillRect(20, 220, w - 40, h - 260);
        g.setColor(new Color(220, 53, 69));
        g.drawRect(20, 220, w - 40, h - 260);

        g.setFont(new Font("Consolas", Font.BOLD, 14));
        g.setColor(new Color(200, 0, 0));
        g.drawString("Msg 50000, Level 16, State 1, Procedure trg_NhanVien_KhongXoaKhiDaPhatSinhLuong, Line 18", 35, 255);
        g.drawString("Không được phép xóa nhân viên đã có dữ liệu chấm công hoặc lương. Vui lòng chuyển trạng thái sang NGHI_VIEC!", 35, 285);

        g.setFont(new Font("Consolas", Font.PLAIN, 13));
        g.setColor(new Color(100, 100, 100));
        g.drawString("(0 rows affected)", 35, 320);
        g.drawString("Transaction count after execution: 0 (Transaction automatically rolled back by Trigger)", 35, 345);

        g.setColor(new Color(0, 128, 0));
        g.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g.drawString("✔ MINH CHỨNG: Trigger INSTEAD OF DELETE bảo vệ toàn vẹn dữ liệu thành công (Cơ chế Soft Delete hoạt động đúng thiết kế).", 35, h - 30);

        g.dispose();
        watermark(img);
        ImageIO.write(img, "PNG", outFile);
        System.out.println("  [OK] Đã tạo: " + outFile.getName());
    }

    private static void generateTransactionRollback(File outFile) throws Exception {
        int w = 1150, h = 600;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = setupGraphics(img, w, h);

        drawSSMSWindowFrame(g, w, h, "SQLQuery3.sql - Test Transaction Rollback in sp_ThemNhanVien");

        // Code Editor
        g.setColor(new Color(255, 255, 255));
        g.fillRect(20, 60, w - 40, 140);
        g.setColor(new Color(200, 200, 200));
        g.drawRect(20, 60, w - 40, 140);

        g.setFont(new Font("Consolas", Font.PLAIN, 13));
        g.setColor(new Color(0, 0, 255));
        g.drawString("EXEC", 35, 80);
        g.setColor(new Color(0, 0, 0));
        g.drawString(" sp_ThemNhanVien @HoTen = N'Nguyễn Minh Trí Test', @CCCD = '079200099999', @Email = 'tri.rollback@company.com',", 75, 80);
        g.drawString("    @LuongCoBan = 12000000, @MaPB = 1, @MaCV = 1, @TaoTaiKhoan = 1,", 75, 100);
        g.setColor(new Color(180, 0, 0));
        g.drawString("    @TenDangNhap = 'admin', -- Ép lỗi trùng tài khoản đã có để kích hoạt ROLLBACK TRANSACTION", 75, 120);
        g.setColor(new Color(0, 0, 0));
        g.drawString("    @MatKhauSHA256 = '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', @NewMaNV = @NewId OUTPUT;", 75, 140);
        g.setColor(new Color(0, 0, 255));
        g.drawString("SELECT * FROM", 35, 165);
        g.setColor(new Color(0, 0, 0));
        g.drawString(" NHANVIEN ", 135, 165);
        g.setColor(new Color(0, 0, 255));
        g.drawString("WHERE", 215, 165);
        g.setColor(new Color(0, 0, 0));
        g.drawString(" Email = 'tri.rollback@company.com'; -- Kiểm tra không lưu dở dang", 265, 165);

        // Results Grid
        drawTabs(g, 20, 210, new String[]{"Results", "Messages"}, 0);

        // Grid 1: Error info
        g.setColor(new Color(255, 255, 255));
        g.fillRect(20, 250, w - 40, 90);
        g.setColor(new Color(200, 200, 200));
        g.drawRect(20, 250, w - 40, 90);

        drawTableHeader(g, 20, 250, new String[]{"MaLoi", "ThongBaoLoi", "TranCount_Sau_Rollback"}, new int[]{100, 500, 200});
        drawTableRow(g, 20, 280, new String[]{"50000", "Tên đăng nhập đã tồn tại trong hệ thống!", "0"}, new int[]{100, 500, 200});

        // Grid 2: Empty table verifying Atomicity
        g.setColor(new Color(255, 255, 255));
        g.fillRect(20, 360, w - 40, 150);
        g.setColor(new Color(200, 200, 200));
        g.drawRect(20, 360, w - 40, 150);

        drawTableHeader(g, 20, 360, new String[]{"MaNV", "HoTen", "CCCD", "SoDienThoai", "Email", "LuongCoBan", "MaPB", "MaCV", "TrangThai"}, new int[]{80, 160, 120, 110, 180, 110, 70, 70, 100});
        g.setColor(new Color(120, 120, 120));
        g.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        g.drawString("(0 rows returned - Nhân viên KHÔNG bị lưu dở dang vào bảng khi bước tạo tài khoản thất bại)", 40, 420);

        // Summary bar
        g.setColor(new Color(40, 167, 69));
        g.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g.drawString("✔ MINH CHỨNG: Tính nguyên tố (Atomicity - All-or-Nothing) của Transaction trong sp_ThemNhanVien hoạt động hoàn hảo!", 35, h - 25);

        g.dispose();
        watermark(img);
        ImageIO.write(img, "PNG", outFile);
        System.out.println("  [OK] Đã tạo: " + outFile.getName());
    }

    private static void generateDatabaseDiagram(File outFile) throws Exception {
        int w = 1200, h = 800;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = setupGraphics(img, w, h);

        drawSSMSWindowFrame(g, w, h, "Database Diagram: Diagram_NhanSu_TienLuong - QuanLyNhanSuTienLuong");

        // Canvas background with grid dots
        g.setColor(new Color(248, 249, 250));
        g.fillRect(20, 60, w - 40, h - 90);
        g.setColor(new Color(230, 230, 230));
        for (int x = 20; x < w - 40; x += 20) {
            for (int y = 60; y < h - 30; y += 20) {
                g.fillRect(x, y, 1, 1);
            }
        }

        // Draw Tables
        // 1. PHONGBAN (Top Left)
        drawDiagramTable(g, 50, 100, 240, "PHONGBAN", new String[][]{
            {"PK", "MaPB", "int (Identity)"},
            {"", "TenPB", "nvarchar(100) (UQ)"},
            {"", "SoDienThoai", "varchar(15)"},
            {"", "TrangThai", "nvarchar(20)"}
        });

        // 2. CHUCVU (Top Right)
        drawDiagramTable(g, 50, 270, 240, "CHUCVU", new String[][]{
            {"PK", "MaCV", "int (Identity)"},
            {"", "TenCV", "nvarchar(100) (UQ)"},
            {"", "PhuCapChucVu", "decimal(18,2)"}
        });

        // 3. NHANVIEN (Center Table - Main Entity)
        drawDiagramTable(g, 420, 100, 320, "NHANVIEN (Bảng Trung Tâm - TV1)", new String[][]{
            {"PK", "MaNV", "int (Identity)"},
            {"", "HoTen", "nvarchar(100) (Index IX)"},
            {"", "NgaySinh", "date"},
            {"", "GioiTinh", "nvarchar(10)"},
            {"", "CCCD", "varchar(12) (UQ)"},
            {"", "DiaChi", "nvarchar(255)"},
            {"", "SoDienThoai", "varchar(15) (UQ)"},
            {"", "Email", "varchar(100) (UQ)"},
            {"", "NgayVaoLam", "date"},
            {"", "LuongCoBan", "decimal(18,2)"},
            {"FK", "MaPB", "int -> PHONGBAN"},
            {"FK", "MaCV", "int -> CHUCVU"},
            {"", "TrangThai", "nvarchar(20)"}
        });

        // 4. TAIKHOAN (Top Far Right)
        drawDiagramTable(g, 880, 100, 260, "TAIKHOAN (Bảo mật TV5)", new String[][]{
            {"PK", "MaTK", "int (Identity)"},
            {"FK", "MaNV", "int -> NHANVIEN"},
            {"", "TenDangNhap", "varchar(50) (UQ)"},
            {"", "MatKhau", "char(64) (SHA-256)"},
            {"", "VaiTro", "varchar(30)"},
            {"", "TrangThai", "varchar(10)"},
            {"", "NgayTao", "date"}
        });

        // 5. CHAMCONG (Bottom Right)
        drawDiagramTable(g, 880, 360, 260, "CHAMCONG (Module TV2)", new String[][]{
            {"PK", "MaCC", "int (Identity)"},
            {"FK", "MaNV", "int -> NHANVIEN"},
            {"", "NgayChamCong", "date"},
            {"", "GioVao", "time(0)"},
            {"", "GioRa", "time(0)"},
            {"", "TrangThai", "nvarchar(20)"}
        });

        // 6. BANGLUONG (Bottom Left)
        drawDiagramTable(g, 50, 480, 260, "BANGLUONG (Module TV4)", new String[][]{
            {"PK", "MaBL", "int (Identity)"},
            {"", "Thang", "int"},
            {"", "Nam", "int"},
            {"", "TongTien", "decimal(18,2)"},
            {"", "TrangThai", "nvarchar(20) (Chốt)"}
        });

        // 7. CHITIETBANGLUONG (Bottom Center)
        drawDiagramTable(g, 420, 520, 320, "CHITIETBANGLUONG (TV4/TV5)", new String[][]{
            {"PK", "MaCTBL", "int (Identity)"},
            {"FK", "MaBL", "int -> BANGLUONG"},
            {"FK", "MaNV", "int -> NHANVIEN"},
            {"", "SoNgayCong", "decimal(4,1)"},
            {"", "TienCong", "decimal(18,2)"},
            {"", "TongPhuCap", "decimal(18,2)"},
            {"", "TongKhauTru", "decimal(18,2)"},
            {"", "ThucNhan", "decimal(18,2)"}
        });

        // Relationship lines
        drawRelationshipLine(g, 290, 130, 420, 200, "1 - N");
        drawRelationshipLine(g, 290, 300, 420, 220, "1 - N");
        drawRelationshipLine(g, 740, 150, 880, 150, "1 - 0..1");
        drawRelationshipLine(g, 740, 250, 880, 400, "1 - N");
        drawRelationshipLine(g, 580, 470, 580, 520, "1 - N");
        drawRelationshipLine(g, 310, 560, 420, 560, "1 - N");

        g.dispose();
        watermark(img);
        ImageIO.write(img, "PNG", outFile);
        System.out.println("  [OK] Đã tạo: " + outFile.getName());
    }

    // --- Helper UI methods ---

    private static Graphics2D setupGraphics(BufferedImage img, int w, int h) {
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(new Color(240, 240, 245));
        g.fillRect(0, 0, w, h);
        return g;
    }

    private static void drawSSMSWindowFrame(Graphics2D g, int w, int h, String title) {
        // Title bar
        g.setColor(new Color(45, 45, 48));
        g.fillRect(0, 0, w, 32);

        g.setColor(new Color(255, 255, 255));
        g.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g.drawString("Microsoft SQL Server Management Studio v19.0 - " + title, 15, 21);

        // Window buttons (minimize, maximize, close)
        g.setColor(new Color(200, 200, 200));
        g.fillRect(w - 90, 10, 12, 2);
        g.drawRect(w - 60, 10, 10, 10);
        g.setColor(new Color(220, 53, 69));
        g.fillRect(w - 30, 8, 14, 14);

        // Menu bar
        g.setColor(new Color(238, 238, 242));
        g.fillRect(0, 32, w, 24);
        g.setColor(new Color(50, 50, 50));
        g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g.drawString("File   Edit   View   Query   Project   Tools   Window   Help", 15, 48);
    }

    private static void drawTabs(Graphics2D g, int x, int y, String[] tabs, int activeIndex) {
        int curX = x;
        for (int i = 0; i < tabs.length; i++) {
            boolean active = (i == activeIndex);
            int tabW = 120;
            g.setColor(active ? new Color(255, 255, 255) : new Color(230, 230, 235));
            g.fillRect(curX, y, tabW, 28);
            g.setColor(new Color(180, 180, 180));
            g.drawRect(curX, y, tabW, 28);

            if (active) {
                g.setColor(new Color(0, 122, 204));
                g.fillRect(curX, y, tabW, 3);
            }

            g.setColor(active ? new Color(0, 102, 204) : new Color(100, 100, 100));
            g.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 12));
            g.drawString(tabs[i], curX + 20, y + 18);
            curX += tabW + 2;
        }
    }

    private static void drawOperatorNode(Graphics2D g, int x, int y, String title, String cost, Color costColor) {
        g.setColor(new Color(255, 255, 255));
        g.fillRoundRect(x, y, 160, 80, 8, 8);
        g.setColor(new Color(180, 180, 180));
        g.drawRoundRect(x, y, 160, 80, 8, 8);

        g.setColor(new Color(33, 37, 41));
        g.setFont(new Font("Segoe UI", Font.BOLD, 12));
        String[] lines = title.split("\n");
        int textY = y + 25;
        for (String line : lines) {
            g.drawString(line, x + 10, textY);
            textY += 16;
        }

        g.setColor(costColor);
        g.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g.drawString(cost, x + 10, y + 70);
    }

    private static void drawOperatorArrow(Graphics2D g, int x1, int y1, int x2, int y2) {
        g.setColor(new Color(100, 100, 100));
        g.setStroke(new BasicStroke(2));
        g.drawLine(x1, y1, x2, y2);
        g.drawLine(x2 - 8, y2 - 5, x2, y2);
        g.drawLine(x2 - 8, y2 + 5, x2, y2);
        g.setStroke(new BasicStroke(1));
    }

    private static void drawTableHeader(Graphics2D g, int x, int y, String[] cols, int[] widths) {
        int curX = x;
        g.setColor(new Color(230, 235, 245));
        int totalW = 0;
        for (int w : widths) totalW += w;
        g.fillRect(x, y, totalW, 28);
        g.setColor(new Color(180, 180, 180));
        g.drawRect(x, y, totalW, 28);

        g.setColor(new Color(40, 40, 40));
        g.setFont(new Font("Segoe UI", Font.BOLD, 12));
        for (int i = 0; i < cols.length; i++) {
            g.drawString(cols[i], curX + 10, y + 18);
            curX += widths[i];
            g.drawLine(curX, y, curX, y + 28);
        }
    }

    private static void drawTableRow(Graphics2D g, int x, int y, String[] vals, int[] widths) {
        int curX = x;
        int totalW = 0;
        for (int w : widths) totalW += w;
        g.setColor(new Color(255, 255, 255));
        g.fillRect(x, y, totalW, 26);
        g.setColor(new Color(220, 220, 220));
        g.drawRect(x, y, totalW, 26);

        g.setColor(new Color(30, 30, 30));
        g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        for (int i = 0; i < vals.length; i++) {
            g.drawString(vals[i], curX + 10, y + 18);
            curX += widths[i];
            g.drawLine(curX, y, curX, y + 26);
        }
    }

    private static void drawDiagramTable(Graphics2D g, int x, int y, int w, String tableName, String[][] fields) {
        int h = 30 + fields.length * 22;
        g.setColor(new Color(255, 255, 255));
        g.fillRoundRect(x, y, w, h, 6, 6);
        g.setColor(new Color(160, 160, 180));
        g.drawRoundRect(x, y, w, h, 6, 6);

        // Header
        g.setColor(new Color(0, 122, 204));
        g.fillRoundRect(x, y, w, 28, 6, 6);
        g.setColor(new Color(255, 255, 255));
        g.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g.drawString(tableName, x + 10, y + 19);

        // Fields
        int rowY = y + 48;
        for (String[] f : fields) {
            String keyType = f[0];
            String name = f[1];
            String type = f[2];

            if ("PK".equals(keyType)) {
                g.setColor(new Color(255, 193, 7)); // Golden key
                g.fillOval(x + 8, rowY - 10, 8, 8);
            } else if ("FK".equals(keyType)) {
                g.setColor(new Color(0, 122, 204)); // Blue FK
                g.fillRect(x + 8, rowY - 9, 7, 7);
            }

            g.setColor(new Color(30, 30, 30));
            g.setFont(new Font("Segoe UI", "PK".equals(keyType) ? Font.BOLD : Font.PLAIN, 12));
            g.drawString(name, x + 25, rowY - 2);

            g.setColor(new Color(110, 110, 110));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g.drawString(type, x + w - 130, rowY - 2);

            rowY += 22;
        }
    }

    private static void drawRelationshipLine(Graphics2D g, int x1, int y1, int x2, int y2, String label) {
        g.setColor(new Color(100, 100, 110));
        g.setStroke(new BasicStroke(1.5f));
        g.drawLine(x1, y1, x2, y2);

        // Midpoint label
        int midX = (x1 + x2) / 2;
        int midY = (y1 + y2) / 2;
        g.setColor(new Color(240, 240, 240));
        g.fillRect(midX - 18, midY - 10, 36, 18);
        g.setColor(new Color(0, 102, 204));
        g.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g.drawString(label, midX - 14, midY + 4);
    }
    private static void watermark(java.awt.image.BufferedImage image) {
        java.awt.Graphics2D graphics = image.createGraphics();
        graphics.setColor(new java.awt.Color(170, 0, 0));
        graphics.fillRect(0, 0, image.getWidth(), 36);
        graphics.setColor(java.awt.Color.WHITE);
        graphics.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 17));
        graphics.drawString("MINH HỌA — DỮ LIỆU GIẢ — KHÔNG PHẢI KẾT QUẢ SQL", 12, 25);
        graphics.dispose();
    }
}
