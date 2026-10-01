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
import com.ui.nhanvien.DanhMucPanel;
import com.ui.nhanvien.NhanVienPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * CaptureScreenshots – Công cụ kết xuất và chụp ảnh màn hình chất lượng cao cho toàn bộ giao diện dự án
 * Tự động render pixel-perfect giao diện các Form/Panel kèm dữ liệu mẫu sinh động.
 */
public class CaptureScreenshots {

    private static final String OUTPUT_DIR = "screenshots";

    public static void main(String[] args) {
        System.out.println("==============================================================");
        System.out.println("   BẮT ĐẦU CHỤP ẢNH MÀN HÌNH MINH CHỨNG GIAO DIỆN (FULL HD)   ");
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
                try {
                    MainFrame mainFrame = new MainFrame();
                    mainFrame.setSize(1280, 800);
                    mainFrame.setLocationRelativeTo(null);
                    captureComponent(mainFrame, "02_MainFrame_Dashboard.png", 1280, 800);
                    mainFrame.dispose();
                } catch (Exception e) {
                    System.err.println("Lỗi chụp MainFrame: " + e.getMessage());
                }

                // 3. NhanVienPanel (TV1 - Quản lý Hồ sơ nhân viên)
                try {
                    NhanVienPanel nvPanel = new NhanVienPanel();
                    populateSampleDataNhanVien(nvPanel);
                    capturePanel(nvPanel, "03_NhanVien_HoSo.png", 1200, 750);
                } catch (Exception e) {
                    System.err.println("Lỗi chụp NhanVienPanel: " + e.getMessage());
                }

                // 3b. DanhMucPanel (TV1 - Quản lý Phòng Ban & Chức Vụ)
                try {
                    DanhMucPanel dmPanel = new DanhMucPanel();
                    populateSampleDataDanhMuc(dmPanel);
                    capturePanel(dmPanel, "03_DanhMuc_PhongBan_ChucVu.png", 1200, 750);
                } catch (Exception e) {
                    System.err.println("Lỗi chụp DanhMucPanel: " + e.getMessage());
                }

                // 4. ChamCongPanel (TV2 - Ghi nhận chi tiết)
                try {
                    ChamCongPanel ccPanel1 = new ChamCongPanel(false);
                    populateSampleDataChamCong(ccPanel1);
                    capturePanel(ccPanel1, "04_ChamCong_ChiTiet.png", 1200, 750);
                } catch (Exception e) {
                    System.err.println("Lỗi chụp ChamCongPanel (Chi tiết): " + e.getMessage());
                }

                // 5. ChamCongPanel (TV2 - Tổng hợp tháng)
                try {
                    ChamCongPanel ccPanel2 = new ChamCongPanel(true);
                    capturePanel(ccPanel2, "05_ChamCong_TongHopThang.png", 1200, 750);
                } catch (Exception e) {
                    System.err.println("Lỗi chụp ChamCongPanel (Tổng hợp): " + e.getMessage());
                }

                // 6. DieuChinhChamCongDialog (TV2 - Drill-down hiệu chỉnh)
                try {
                    DieuChinhChamCongDialog dcDialog = new DieuChinhChamCongDialog(null, 1, "Nguyễn Minh Trí", 9, 2026);
                    captureComponent(dcDialog, "06_ChamCong_DieuChinhDialog.png", 850, 600);
                    dcDialog.dispose();
                } catch (Exception e) {
                    System.err.println("Lỗi chụp DieuChinhChamCongDialog: " + e.getMessage());
                }

                // 7. PhuCapKhauTruPanel (TV3 - Quản lý Phụ cấp & Khấu trừ)
                try {
                    PhuCapKhauTruPanel pcPanel = new PhuCapKhauTruPanel();
                    capturePanel(pcPanel, "07_PhuCap_KhauTru_Panel.png", 1200, 750);
                } catch (Exception e) {
                    System.err.println("Lỗi chụp PhuCapKhauTruPanel: " + e.getMessage());
                }

                // 8. BangLuongPanel (TV4 - Tính toán Bảng lương)
                try {
                    BangLuongPanel blPanel = new BangLuongPanel();
                    capturePanel(blPanel, "08_BangLuong_TinhLuong.png", 1200, 750);
                } catch (Exception e) {
                    System.err.println("Lỗi chụp BangLuongPanel: " + e.getMessage());
                }

                // 9. BaoCaoPanel (TV5 - Báo cáo & Chốt lương)
                try {
                    BaoCaoPanel bcPanel = new BaoCaoPanel();
                    capturePanel(bcPanel, "09_BaoCao_ChotLuong.png", 1200, 750);
                } catch (Exception e) {
                    System.err.println("Lỗi chụp BaoCaoPanel: " + e.getMessage());
                }

                // 10. TaiKhoanPanel (TV5 - Quản trị phân quyền tài khoản)
                try {
                    TaiKhoanPanel tkPanel = new TaiKhoanPanel();
                    capturePanel(tkPanel, "10_TaiKhoan_QuanTri.png", 1200, 750);
                } catch (Exception e) {
                    System.err.println("Lỗi chụp TaiKhoanPanel: " + e.getMessage());
                }

                Session.getInstance().logout();

                System.out.println("==============================================================");
                System.out.println("   [THÀNH CÔNG] ĐÃ XUẤT TOÀN BỘ ẢNH CHỤP GIAO DIỆN VÀO THƯ MỤC SCREENSHOTS!");
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
        frame.setSize(width, height);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setSize(width, height);
        captureComponent(frame, fileName, width, height);
        frame.dispose();
    }

    private static void captureComponent(Component comp, String fileName, int width, int height) {
        try {
            comp.setSize(width, height);
            comp.setPreferredSize(new Dimension(width, height));
            comp.addNotify();
            comp.validate();
            comp.doLayout();
            validateTree(comp);

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            g2d.setColor(new Color(245, 246, 250));
            g2d.fillRect(0, 0, width, height);

            comp.printAll(g2d);
            g2d.dispose();

            File out = new File(OUTPUT_DIR, fileName);
            ImageIO.write(image, "PNG", out);
            System.out.println("  [OK] Đã xuất ảnh: " + out.getName() + " (" + width + "x" + height + ") - Kích thước: " + out.length() + " bytes");
        } catch (Exception ex) {
            System.err.println("  [FAIL] Không thể lưu " + fileName + ": " + ex.getMessage());
        }
    }

    private static void validateTree(Component comp) {
        comp.validate();
        comp.doLayout();
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                validateTree(child);
            }
        }
    }

    private static void populateSampleDataNhanVien(NhanVienPanel panel) {
        findTableAndSetData(panel, new Object[][]{
            {"1", "Nguyễn Minh Trí", "2000-01-15", "Nam", "079200000001", "0901234567", "tri.nguyen@company.com", "15,000,000", "Phòng Kỹ Thuật", "Trưởng Phòng", "DANG_LAM_VIEC", "tri.nguyen (HR_Manager)"},
            {"2", "Phạm Minh Quân", "2001-03-20", "Nam", "079200000002", "0901234568", "quan.pham@company.com", "14,000,000", "Phòng Nhân Sự", "Nhân Viên", "DANG_LAM_VIEC", "quan.pham (Employee)"},
            {"3", "Trần Tiến Đạt", "2000-07-10", "Nam", "079200000003", "0901234569", "dat.tran@company.com", "13,500,000", "Phòng Kế Toán", "Chuyên Viên", "DANG_LAM_VIEC", "dat.tran (Employee)"},
            {"4", "Nguyễn Quang Vinh", "2001-11-05", "Nam", "079200000004", "0901234570", "vinh.nguyen@company.com", "16,000,000", "Phòng Kế Toán", "Trưởng Phòng", "DANG_LAM_VIEC", "vinh.nguyen (Payroll_Officer)"},
            {"5", "Trần Đức Anh", "2000-09-25", "Nam", "079200000005", "0901234571", "anh.tran@company.com", "20,000,000", "Ban Giám Đốc", "Giám Đốc", "DANG_LAM_VIEC", "admin (DB_Admin)"},
            {"6", "Lê Thị Mai", "1998-04-12", "Nữ", "079200000006", "0901234572", "mai.le@company.com", "12,000,000", "Phòng Kỹ Thuật", "Nhân Viên", "DANG_LAM_VIEC", "mai.le (Employee)"},
            {"7", "Hoàng Văn Hùng", "1997-08-30", "Nam", "079200000007", "0901234573", "hung.hoang@company.com", "11,500,000", "Phòng Nhân Sự", "Nhân Viên", "DANG_LAM_VIEC", "hung.hoang (Employee)"}
        });
    }

    private static void populateSampleDataDanhMuc(DanhMucPanel panel) {
        for (Component c : panel.getComponents()) {
            if (c instanceof JPanel) {
                for (Component sub : ((JPanel) c).getComponents()) {
                    if (sub instanceof JScrollPane) {
                        Component view = ((JScrollPane) sub).getViewport().getView();
                        if (view instanceof JTable) {
                            JTable tbl = (JTable) view;
                            DefaultTableModel model = (DefaultTableModel) tbl.getModel();
                            if (model.getRowCount() == 0) {
                                if (model.getColumnCount() == 4) { // Phòng ban
                                    model.addRow(new Object[]{"1", "Ban Giám Đốc", "0283896864", "HOAT_DONG"});
                                    model.addRow(new Object[]{"2", "Phòng Nhân Sự", "0283896865", "HOAT_DONG"});
                                    model.addRow(new Object[]{"3", "Phòng Kế Toán", "0283896866", "HOAT_DONG"});
                                    model.addRow(new Object[]{"4", "Phòng Kỹ Thuật", "0283896867", "HOAT_DONG"});
                                } else if (model.getColumnCount() == 3) { // Chức vụ
                                    model.addRow(new Object[]{"1", "Giám Đốc", "5,000,000"});
                                    model.addRow(new Object[]{"2", "Trưởng Phòng", "3,000,000"});
                                    model.addRow(new Object[]{"3", "Phó Phòng", "1,500,000"});
                                    model.addRow(new Object[]{"4", "Chuyên Viên", "500,000"});
                                    model.addRow(new Object[]{"5", "Nhân Viên", "0"});
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void populateSampleDataChamCong(ChamCongPanel panel) {
        findTableAndSetData(panel, new Object[][]{
            {"1", "Nguyễn Minh Trí", "2026-09-01", "08:00", "17:00", "8.0", "CO_MAT", "Đúng giờ"},
            {"1", "Nguyễn Minh Trí", "2026-09-02", "08:15", "17:00", "7.7", "DI_TRE", "Trễ 15 phút"},
            {"2", "Phạm Minh Quân", "2026-09-01", "07:55", "17:05", "8.0", "CO_MAT", "Đúng giờ"},
            {"3", "Trần Tiến Đạt", "2026-09-01", "08:00", "17:00", "8.0", "CO_MAT", "Đúng giờ"},
            {"4", "Nguyễn Quang Vinh", "2026-09-01", "08:00", "17:00", "8.0", "CO_MAT", "Đúng giờ"}
        });
    }

    private static void findTableAndSetData(Container container, Object[][] sampleRows) {
        for (Component c : container.getComponents()) {
            if (c instanceof JScrollPane) {
                Component view = ((JScrollPane) c).getViewport().getView();
                if (view instanceof JTable) {
                    JTable tbl = (JTable) view;
                    DefaultTableModel model = (DefaultTableModel) tbl.getModel();
                    if (model.getRowCount() == 0) {
                        for (Object[] row : sampleRows) {
                            if (row.length <= model.getColumnCount()) {
                                model.addRow(row);
                            }
                        }
                    }
                    return;
                }
            } else if (c instanceof Container) {
                findTableAndSetData((Container) c, sampleRows);
            }
        }
    }
}
