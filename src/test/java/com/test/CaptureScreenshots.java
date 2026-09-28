package com.test;

import com.model.TaiKhoan;
import com.session.Session;
import com.ui.admin.TaiKhoanPanel;
import com.ui.auth.LoginFrame;
import com.ui.baocao.BaoCaoPanel;
import com.ui.chamcong.ChamCongPanel;
import com.ui.chamcong.DieuChinhChamCongDialog;
import com.ui.luong.BangLuongPanel;
import com.ui.main.MainFrame;
import com.ui.nhanvien.DanhMucPanel;
import com.ui.nhanvien.NhanVienPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Tự động hóa kết xuất ảnh chụp màn hình (Automated Screenshot Capture Engine)
 * Render toàn bộ 10 màn hình giao diện của dự án thành file ảnh PNG chuẩn HD
 * lưu vào thư mục docs/screenshots/ để chèn trực tiếp vào báo cáo Word/PDF đồ án.
 */
public class CaptureScreenshots {

    public static void main(String[] args) {
        // Cấu hình Look & Feel hệ thống
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        File dir = new File("docs/screenshots");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        System.out.println("==============================================================");
        System.out.println("   KHỞI ĐỘNG HỆ THỐNG TỰ ĐỘNG CHỤP ẢNH GIAO DIỆN (UI CAPTURE)  ");
        System.out.println("==============================================================");

        try {
            // 1. Màn hình Đăng nhập (LoginFrame)
            System.out.println("\n--- [1/10] Chụp LoginFrame ---");
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.pack();
            loginFrame.setLocationRelativeTo(null);
            loginFrame.setVisible(true);
            Thread.sleep(300);
            saveComponent(loginFrame, "docs/screenshots/01_login_frame.png");
            loginFrame.dispose();

            // Đăng nhập giả lập vai trò DB_Admin để mở toàn bộ quyền
            TaiKhoan admin = new TaiKhoan(1, 5, "admin", "DB_Admin", null, "HOAT_DONG");
            admin.setHoTenNV("Trần Đức Anh");
            Session.getInstance().login(admin);

            // 2. Màn hình Trang chủ Dashboard (MainFrame - Tab 0)
            System.out.println("\n--- [2/10] Chụp MainFrame Dashboard ---");
            MainFrame mainFrame = new MainFrame();
            mainFrame.setSize(1240, 820);
            mainFrame.setLocationRelativeTo(null);
            mainFrame.setVisible(true);
            Thread.sleep(400);
            saveComponent(mainFrame, "docs/screenshots/02_main_dashboard.png");

            // 3. Màn hình Quản lý Hồ sơ nhân sự (NhanVienPanel)
            System.out.println("\n--- [3/10] Chụp NhanVienPanel ---");
            JFrame fNV = createWrapper("Quản lý Hồ sơ Nhân viên & Danh mục (TV1)", new NhanVienPanel(), 1240, 780);
            fNV.setVisible(true);
            Thread.sleep(400);
            saveComponent(fNV, "docs/screenshots/03_nhanvien_panel.png");
            fNV.dispose();

            // 4. Màn hình Quản lý Danh mục Phòng ban & Chức vụ (DanhMucPanel)
            System.out.println("\n--- [4/10] Chụp DanhMucPanel ---");
            JFrame fDM = createWrapper("Quản lý Phòng ban & Chức vụ (TV1)", new DanhMucPanel(), 1240, 780);
            fDM.setVisible(true);
            Thread.sleep(400);
            saveComponent(fDM, "docs/screenshots/04_danhmuc_panel.png");
            fDM.dispose();

            // 5. Màn hình Chấm công Tab 1 (Nhật ký & Ghi nhận)
            System.out.println("\n--- [5/10] Chụp ChamCongPanel Tab 1 ---");
            ChamCongPanel cc1 = new ChamCongPanel(false);
            JFrame fCC1 = createWrapper("Ghi nhận Chấm công & Điểm danh theo lô (TV2)", cc1, 1240, 780);
            fCC1.setVisible(true);
            Thread.sleep(400);
            saveComponent(fCC1, "docs/screenshots/05_chamcong_tab1_nhatky.png");
            fCC1.dispose();

            // 6. Màn hình Chấm công Tab 2 (Tổng hợp công theo tháng View)
            System.out.println("\n--- [6/10] Chụp ChamCongPanel Tab 2 (View tổng hợp) ---");
            ChamCongPanel cc2 = new ChamCongPanel(true);
            JFrame fCC2 = createWrapper("Tổng hợp công theo tháng – View vw_TongHopChamCongThang (TV2)", cc2, 1240, 780);
            fCC2.setVisible(true);
            Thread.sleep(400);
            saveComponent(fCC2, "docs/screenshots/06_chamcong_tab2_tonghop.png");
            fCC2.dispose();

            // 7. Hộp thoại Điều chỉnh ngày công chi tiết (DieuChinhChamCongDialog - Drill-down)
            System.out.println("\n--- [7/10] Chụp DieuChinhChamCongDialog ---");
            DieuChinhChamCongDialog dcDialog = new DieuChinhChamCongDialog(mainFrame, 1, "Nguyễn Minh Trí", 9, 2026);
            dcDialog.setModal(false);
            dcDialog.setSize(960, 620);
            dcDialog.setLocationRelativeTo(null);
            dcDialog.setVisible(true);
            Thread.sleep(400);
            saveComponent(dcDialog, "docs/screenshots/07_dieuchinh_chamcong_dialog.png");
            dcDialog.dispose();

            // 8. Màn hình Tính toán Tiền lương (BangLuongPanel)
            System.out.println("\n--- [8/10] Chụp BangLuongPanel ---");
            BangLuongPanel blPanel = new BangLuongPanel();
            JFrame fBL = createWrapper("Tính toán & Quản lý Tiền lương tháng (TV4)", blPanel, 1240, 780);
            fBL.setVisible(true);
            Thread.sleep(1200);
            saveComponent(fBL, "docs/screenshots/08_bangluong_panel.png");
            fBL.dispose();

            // 9. Màn hình Báo cáo tổng hợp & Chốt lương (BaoCaoPanel)
            System.out.println("\n--- [9/10] Chụp BaoCaoPanel ---");
            BaoCaoPanel bcPanel = new BaoCaoPanel();
            JFrame fBC = createWrapper("Báo cáo Bảng lương & Chốt lương an toàn UPDLOCK (TV5)", bcPanel, 1240, 780);
            fBC.setVisible(true);
            Thread.sleep(1200);
            saveComponent(fBC, "docs/screenshots/09_baocao_panel.png");
            fBC.dispose();

            // 10. Màn hình Quản trị Tài khoản & Phân quyền (TaiKhoanPanel)
            System.out.println("\n--- [10/10] Chụp TaiKhoanPanel ---");
            JFrame fTK = createWrapper("Quản trị Tài khoản & Cấp phát quyền người dùng (TV5)", new TaiKhoanPanel(), 1240, 780);
            fTK.setVisible(true);
            Thread.sleep(400);
            saveComponent(fTK, "docs/screenshots/10_taikhoan_panel.png");
            fTK.dispose();

            mainFrame.dispose();
            Session.getInstance().logout();

            System.out.println("==============================================================");
            System.out.println(">>> ĐÃ XUẤT THÀNH CÔNG 10/10 ẢNH MÀN HÌNH VÀO docs/screenshots/ <<<");
            System.out.println("==============================================================");

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    private static JFrame createWrapper(String title, JPanel panel, int w, int h) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(panel);
        frame.setSize(w, h);
        frame.setLocationRelativeTo(null);
        return frame;
    }

    private static void saveComponent(Window win, String outputPath) {
        try {
            int w = win.getWidth();
            int h = win.getHeight();
            if (w <= 0 || h <= 0) {
                w = 1200;
                h = 750;
            }
            BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            win.paint(g2);
            g2.dispose();

            ImageIO.write(image, "png", new File(outputPath));
            System.out.println("  [XUẤT ẢNH THÀNH CÔNG] -> " + outputPath + " (" + w + "x" + h + " px)");
        } catch (Exception ex) {
            System.err.println("  [LỖI XUẤT ẢNH] " + outputPath + ": " + ex.getMessage());
        }
    }
}
