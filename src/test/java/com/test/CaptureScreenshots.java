package com.test;

import com.model.TaiKhoan;
import com.session.Session;
import com.ui.admin.TaiKhoanPanel;
import com.ui.auth.LoginFrame;
import com.ui.baocao.BaoCaoPanel;
import com.ui.chamcong.ChamCongPanel;
import com.ui.chamcong.DieuChinhChamCongDialog;
import com.ui.luong.BangLuongPanel;
import com.ui.luong.PhuCapKhauTruPanel;
import com.ui.main.MainFrame;
import com.ui.nhanvien.NhanVienPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * CaptureScreenshots – Công cụ chụp ảnh màn hình tự động cho toàn bộ giao diện dự án
 * Phục vụ đưa minh chứng thực thi vào Báo cáo cuối kỳ 50-100 trang và Slide thuyết trình.
 */
public class CaptureScreenshots {

    private static final String OUTPUT_DIR = "screenshots";

    public static void main(String[] args) {
        System.out.println("==============================================================");
        System.out.println("   BẮT ĐẦU CHỤP ẢNH MÀN HÌNH MINH CHỨNG BÁO CÁO (TV5 - TUẦN 3)");
        System.out.println("==============================================================");

        File dir = new File(OUTPUT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Thiết lập Session admin để có quyền render toàn bộ màn hình
        TaiKhoan admin = new TaiKhoan(1, 1, "admin", "DB_Admin", "Trần Đức Anh", "HOAT_DONG");
        Session.getInstance().login(admin);

        SwingUtilities.invokeLater(() -> {
            try {
                // 1. Màn hình đăng nhập LoginFrame
                captureLoginFrame();

                // 2. MainFrame Dashboard
                MainFrame mainFrame = new MainFrame();
                mainFrame.setSize(1280, 800);
                mainFrame.setLocationRelativeTo(null);
                captureComponent(mainFrame, "02_MainFrame_Dashboard.png", 1280, 800);

                // 3. NhanVienPanel (TV1)
                NhanVienPanel nvPanel = new NhanVienPanel();
                capturePanel(nvPanel, "03_NhanVien_HoSo.png", 1150, 700);

                // 3b. DanhMucPanel (TV1 - Quản lý Phòng Ban & Chức Vụ)
                com.ui.nhanvien.DanhMucPanel dmPanel = new com.ui.nhanvien.DanhMucPanel();
                capturePanel(dmPanel, "03_DanhMuc_PhongBan_ChucVu.png", 1150, 700);

                // 4. ChamCongPanel (Tab 1: Ghi nhận chi tiết)
                ChamCongPanel ccPanel1 = new ChamCongPanel(false);
                capturePanel(ccPanel1, "04_ChamCong_ChiTiet.png", 1150, 700);

                // 5. ChamCongPanel (Tab 2: Tổng hợp tháng)
                ChamCongPanel ccPanel2 = new ChamCongPanel(true);
                capturePanel(ccPanel2, "05_ChamCong_TongHopThang.png", 1150, 700);

                // 6. DieuChinhChamCongDialog
                DieuChinhChamCongDialog dcDialog = new DieuChinhChamCongDialog(mainFrame, 1, "Nguyễn Văn An", 9, 2026);
                captureComponent(dcDialog, "06_ChamCong_DieuChinhDialog.png", 800, 600);
                dcDialog.dispose();

                // 7. PhuCapKhauTruPanel (TV3)
                PhuCapKhauTruPanel pcPanel = new PhuCapKhauTruPanel();
                capturePanel(pcPanel, "07_PhuCap_KhauTru_Panel.png", 1150, 700);

                // 8. BangLuongPanel (Tính lương)
                BangLuongPanel blPanel = new BangLuongPanel();
                capturePanel(blPanel, "08_BangLuong_TinhLuong.png", 1150, 700);

                // 9. BaoCaoPanel (Báo cáo chi tiết & Chốt lương)
                BaoCaoPanel bcPanel = new BaoCaoPanel();
                capturePanel(bcPanel, "09_BaoCao_ChotLuong.png", 1150, 700);

                // 10. TaiKhoanPanel (Quản trị tài khoản)
                TaiKhoanPanel tkPanel = new TaiKhoanPanel();
                capturePanel(tkPanel, "10_TaiKhoan_QuanTri.png", 1150, 700);

                mainFrame.dispose();
                Session.getInstance().logout();

                System.out.println("==============================================================");
                System.out.println("   [THÀNH CÔNG] ĐÃ XUẤT TOÀN BỘ 10 ẢNH CHỤP MÀN HÌNH VÀO: " + dir.getAbsolutePath());
                System.out.println("==============================================================");
                System.exit(0);

            } catch (Exception ex) {
                System.err.println("Lỗi khi chụp màn hình: " + ex.getMessage());
                ex.printStackTrace();
                System.exit(1);
            }
        });
    }

    private static void captureLoginFrame() {
        try {
            LoginFrame loginFrame = new LoginFrame();
            captureComponent(loginFrame, "01_LoginFrame.png", 450, 420);
            loginFrame.dispose();
        } catch (Exception e) {
            System.err.println("Bỏ qua LoginFrame: " + e.getMessage());
        }
    }

    private static void capturePanel(JPanel panel, String fileName, int width, int height) {
        JFrame frame = new JFrame();
        frame.setUndecorated(true);
        frame.getContentPane().add(panel);
        captureComponent(frame, fileName, width, height);
        frame.dispose();
    }

    private static void captureComponent(Component comp, String fileName, int width, int height) {
        try {
            comp.setSize(width, height);
            comp.doLayout();
            if (comp instanceof Container) {
                ((Container) comp).validate();
            }

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            comp.paint(g2d);
            g2d.dispose();

            File out = new File(OUTPUT_DIR, fileName);
            ImageIO.write(image, "PNG", out);
            System.out.println("  [OK] Đã lưu ảnh: " + out.getName() + " (" + width + "x" + height + ")");
        } catch (Exception ex) {
            System.err.println("  [FAIL] Không thể lưu " + fileName + ": " + ex.getMessage());
        }
    }
}
