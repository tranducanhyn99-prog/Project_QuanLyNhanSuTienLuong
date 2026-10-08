package com.test;

import com.model.BangLuong;
import com.model.TaiKhoan;
import com.session.Session;
import java.math.BigDecimal;
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
import com.ui.theme.UITheme;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * CaptureScreenshots – Công cụ kết xuất và chụp ảnh màn hình chất lượng cao cho toàn bộ giao diện dự án
 * Tự động render pixel-perfect giao diện các Form/Panel kèm dữ liệu mẫu hiện đại chuẩn SaaS.
 */
public class CaptureScreenshots {

    private static final String OUTPUT_DIR = "build/mock-screenshots";

    public static void main(String[] args) throws Exception {
        if (!Boolean.getBoolean("app.mockScreenshots")) {
            throw new IllegalStateException("Ảnh dựng chỉ là minh họa. Bật -Dapp.mockScreenshots=true nếu cần; không dùng làm bằng chứng test.");
        }

        System.out.println("==============================================================");
        System.out.println("   KHỞI TẠO BỘ CHỤP ẢNH MINH CHỨNG GIAO DIỆN MODERN ERP       ");
        System.out.println("==============================================================");

        UITheme.setupGlobalUI();
        File dir = new File(OUTPUT_DIR);
        if (!dir.exists()) dir.mkdirs();

        TaiKhoan admin = new TaiKhoan(1, 1, "admin", "DB_Admin", "Trần Đức Anh", "HOAT_DONG");
        Session.getInstance().login(admin);

        // 1. Màn hình đăng nhập (01_LoginFrame.png)
        captureLoginFrame();

        // Khởi tạo MainFrame chính duy nhất để chụp các phân hệ bên trong giao diện ứng dụng
        MainFrame mainFrame = new MainFrame();
        mainFrame.setSize(1280, 800);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
        Thread.sleep(600);

        // 2. MainFrame Dashboard (02_MainFrame_Dashboard.png)
        mainFrame.switchView("HOME", "TỔNG QUAN > Trang chủ Dashboard");
        Thread.sleep(300);
        saveFrame(mainFrame, "02_MainFrame_Dashboard.png", 1280, 800);

        // 3. Hồ sơ Nhân viên (03_NhanVien_HoSo.png)
        mainFrame.switchView("NHAN_VIEN", "NHÂN SỰ > Quản lý Hồ sơ nhân sự");
        Thread.sleep(300);
        populateTables(mainFrame, new Object[][]{
            {"1", "Nguyễn Minh Trí", "2000-01-15", "Nam", "079200000001", "0901234567", "tri.nguyen@company.com", "15,000,000", "Phòng Kỹ Thuật", "Trưởng Phòng", "DANG_LAM_VIEC", "tri.nguyen (HR_Manager)"},
            {"2", "Phạm Minh Quân", "2001-03-20", "Nam", "079200000002", "0901234568", "quan.pham@company.com", "14,000,000", "Phòng Nhân Sự", "Nhân Viên", "DANG_LAM_VIEC", "quan.pham (Employee)"},
            {"3", "Trần Tiến Đạt", "2000-07-10", "Nam", "079200000003", "0901234569", "dat.tran@company.com", "13,500,000", "Phòng Kế Toán", "Chuyên Viên", "DANG_LAM_VIEC", "dat.tran (Employee)"},
            {"4", "Nguyễn Quang Vinh", "2001-11-05", "Nam", "079200000004", "0901234570", "vinh.nguyen@company.com", "16,000,000", "Phòng Kế Toán", "Trưởng Phòng", "DANG_LAM_VIEC", "vinh.nguyen (Payroll_Officer)"},
            {"5", "Trần Đức Anh", "2000-09-25", "Nam", "079200000005", "0901234571", "anh.tran@company.com", "20,000,000", "Ban Giám Đốc", "Giám Đốc", "DANG_LAM_VIEC", "admin (DB_Admin)"},
            {"6", "Lê Thị Mai", "1998-04-12", "Nữ", "079200000006", "0901234572", "mai.le@company.com", "12,000,000", "Phòng Kỹ Thuật", "Nhân Viên", "DANG_LAM_VIEC", "mai.le (Employee)"},
            {"7", "Hoàng Văn Hùng", "1997-08-30", "Nam", "079200000007", "0901234573", "hung.hoang@company.com", "11,500,000", "Phòng Nhân Sự", "Nhân Viên", "DANG_LAM_VIEC", "hung.hoang (Employee)"}
        });
        saveFrame(mainFrame, "03_NhanVien_HoSo.png", 1280, 800);

        // 4. Danh mục Phòng ban & Chức vụ (03_DanhMuc_PhongBan_ChucVu.png)
        mainFrame.switchView("DANH_MUC", "DANH MỤC > Phòng ban & Chức vụ");
        Thread.sleep(300);
        populateTables(mainFrame, new Object[][]{
            {"1", "Ban Giám Đốc", "0283896864", "HOAT_DONG"},
            {"2", "Phòng Nhân Sự", "0283896865", "HOAT_DONG"},
            {"3", "Phòng Kế Toán", "0283896866", "HOAT_DONG"},
            {"4", "Phòng Kỹ Thuật", "0283896867", "HOAT_DONG"}
        }, new Object[][]{
            {"1", "Giám Đốc", "5,000,000"},
            {"2", "Trưởng Phòng", "3,000,000"},
            {"3", "Phó Phòng", "1,500,000"},
            {"4", "Chuyên Viên", "500,000"},
            {"5", "Nhân Viên", "0"}
        });
        saveFrame(mainFrame, "03_DanhMuc_PhongBan_ChucVu.png", 1280, 800);

        // 5. Nhật ký Chấm công chi tiết (04_ChamCong_ChiTiet.png)
        mainFrame.switchView("CHAM_CONG", "CHẤM CÔNG > Ghi nhận chấm công chi tiết");
        Thread.sleep(300);
        populateTables(mainFrame, new Object[][]{
            {"1", "Nguyễn Minh Trí", "2026-09-01", "08:00", "17:00", "8.0", "CO_MAT", "Đúng giờ"},
            {"1", "Nguyễn Minh Trí", "2026-09-02", "08:15", "17:00", "7.7", "DI_TRE", "Trễ 15 phút"},
            {"2", "Phạm Minh Quân", "2026-09-01", "07:55", "17:05", "8.0", "CO_MAT", "Đúng giờ"},
            {"3", "Trần Tiến Đạt", "2026-09-01", "08:00", "17:00", "8.0", "CO_MAT", "Đúng giờ"},
            {"4", "Nguyễn Quang Vinh", "2026-09-01", "08:00", "17:00", "8.0", "CO_MAT", "Đúng giờ"},
            {"5", "Trần Đức Anh", "2026-09-01", "08:00", "17:30", "8.5", "CO_MAT", "Tăng ca 0.5h"}
        });
        saveFrame(mainFrame, "04_ChamCong_ChiTiet.png", 1280, 800);

        // 6. Tổng hợp Công tháng (05_ChamCong_TongHopThang.png)
        mainFrame.switchView("TONG_HOP_CC", "CHẤM CÔNG > Tổng hợp ngày công tháng");
        Thread.sleep(300);
        populateTables(mainFrame, new Object[][]{
            {"1", "Nguyễn Minh Trí", "Phòng Kỹ Thuật", "Trưởng Phòng", "22.0", "2.0", "0", "0", "22.0"},
            {"2", "Phạm Minh Quân", "Phòng Nhân Sự", "Nhân Viên", "21.5", "1.0", "0.5", "0", "21.5"},
            {"3", "Trần Tiến Đạt", "Phòng Kế Toán", "Chuyên Viên", "22.0", "0.0", "0", "0", "22.0"},
            {"4", "Nguyễn Quang Vinh", "Phòng Kế Toán", "Trưởng Phòng", "22.0", "3.5", "0", "0", "22.0"},
            {"5", "Trần Đức Anh", "Ban Giám Đốc", "Giám Đốc", "22.0", "0.0", "0", "0", "22.0"}
        });
        saveFrame(mainFrame, "05_ChamCong_TongHopThang.png", 1280, 800);

        // 7. Phụ cấp & Khấu trừ (07_PhuCap_KhauTru_Panel.png)
        mainFrame.switchView("PHU_CAP", "LƯƠNG > Quản lý Phụ cấp & Khấu trừ");
        Thread.sleep(400);
        populateTables(mainFrame, new Object[][]{
            {"1", "1", "Nguyễn Minh Trí", "Phụ cấp ăn trưa", "730,000", "2026-09-05", "Định kỳ hàng tháng"},
            {"2", "1", "Nguyễn Minh Trí", "Phụ cấp trách nhiệm", "1,500,000", "2026-09-05", "Trưởng bộ phận"},
            {"3", "2", "Phạm Minh Quân", "Phụ cấp ăn trưa", "730,000", "2026-09-05", "Định kỳ hàng tháng"},
            {"4", "3", "Trần Tiến Đạt", "Phụ cấp ăn trưa", "730,000", "2026-09-05", "Định kỳ hàng tháng"},
            {"5", "4", "Nguyễn Quang Vinh", "Phụ cấp xăng xe", "500,000", "2026-09-05", "Hỗ trợ đi lại"}
        }, new Object[][]{
            {"1", "1", "Nguyễn Minh Trí", "Bảo hiểm Xã hội (8%)", "1,200,000", "2026-09-25", "Trừ lương tháng 9"},
            {"2", "2", "Phạm Minh Quân", "Tạm ứng lương", "2,000,000", "2026-09-15", "Đã duyệt ứng"},
            {"3", "3", "Trần Tiến Đạt", "Bảo hiểm Y tế (1.5%)", "202,500", "2026-09-25", "Định kỳ tháng 9"}
        });
        saveFrame(mainFrame, "07_PhuCap_KhauTru_Panel.png", 1280, 800);

        // 8. Tính toán Bảng lương (08_BangLuong_TinhLuong.png)
        mainFrame.switchView("BANG_LUONG", "TIỀN LƯƠNG > Quy trình tính lương tự động");
        Thread.sleep(400);
        populateTables(mainFrame, new Object[][]{
            {"1", "9", "2026", "22", "CHUA_CHOT", "5", "82,450,000", "2026-09-25", "Chưa chốt"}
        }, new Object[][]{
            {"1", "1", "Nguyễn Minh Trí", "15,000,000", "22.0", "15,000,000", "2,230,000", "1,200,000", "16,030,000"},
            {"2", "2", "Phạm Minh Quân", "14,000,000", "21.5", "13,681,818", "730,000", "2,000,000", "12,411,818"},
            {"3", "3", "Trần Tiến Đạt", "13,500,000", "22.0", "13,500,000", "730,000", "202,500", "14,027,500"},
            {"4", "4", "Nguyễn Quang Vinh", "16,000,000", "22.0", "16,000,000", "500,000", "1,280,000", "15,220,000"},
            {"5", "5", "Trần Đức Anh", "20,000,000", "22.0", "20,000,000", "5,000,000", "1,600,000", "23,400,000"}
        });
        saveFrame(mainFrame, "08_BangLuong_TinhLuong.png", 1280, 800);

        // 9. Báo cáo & Chốt lương (09_BaoCao_ChotLuong.png)
        mainFrame.switchView("BAO_CAO", "BÁO CÁO > Báo cáo tổng hợp & Phiếu lương");
        Thread.sleep(400);
        BangLuong mockBL = new BangLuong(9, 2026, 22);
        mockBL.setMaBangLuong(1);
        mockBL.setTrangThai("DA_CHOT");
        mockBL.setTongThucNhan(new BigDecimal("82450000"));
        setComboBoxItems(mainFrame, mockBL);
        updateLabel(mainFrame, "Tổng thực nhận", "Tổng thực nhận: 82,450,000 VNĐ");
        updateLabel(mainFrame, "", "  ĐÃ CHỐT SỔ (Kỳ tháng 09/2026)");
        populateTables(mainFrame, new Object[][]{
            {"1", "Nguyễn Minh Trí", "Phòng Kỹ Thuật", "Trưởng Phòng", "15,000,000", "22.0", "22.0", "15,000,000", "2,230,000", "1,200,000", "16,030,000"},
            {"2", "Phạm Minh Quân", "Phòng Nhân Sự", "Nhân Viên", "14,000,000", "22.0", "21.5", "13,681,818", "730,000", "2,000,000", "12,411,818"},
            {"3", "Trần Tiến Đạt", "Phòng Kế Toán", "Chuyên Viên", "13,500,000", "22.0", "22.0", "13,500,000", "730,000", "202,500", "14,027,500"},
            {"4", "Nguyễn Quang Vinh", "Phòng Kế Toán", "Trưởng Phòng", "16,000,000", "22.0", "22.0", "16,000,000", "500,000", "1,280,000", "15,220,000"},
            {"5", "Trần Đức Anh", "Ban Giám Đốc", "Giám Đốc", "20,000,000", "22.0", "22.0", "20,000,000", "5,000,000", "1,600,000", "23,400,000"}
        });
        saveFrame(mainFrame, "09_BaoCao_ChotLuong.png", 1280, 800);

        // 10. Quản trị Tài khoản (10_TaiKhoan_QuanTri.png)
        mainFrame.switchView("TAI_KHOAN", "QUẢN TRỊ > Phân quyền & Quản lý tài khoản");
        Thread.sleep(400);
        updateLabel(mainFrame, "Đang tải", "Tổng số: 6 tài khoản (5 hoạt động, 1 bị khóa)  |  Cơ chế kiểm soát phân quyền RBAC");
        updateLabel(mainFrame, "Tổng số", "Tổng số: 6 tài khoản (5 hoạt động, 1 bị khóa)  |  Cơ chế kiểm soát phân quyền RBAC");
        populateTables(mainFrame, new Object[][]{
            {"1", "admin", "Trần Đức Anh", "1", "DB_Admin", "HOAT_DONG", "2026-09-01", "2026-09-20"},
            {"2", "hrmanager", "Nguyễn Minh Trí", "2", "HR_Manager", "HOAT_DONG", "2026-09-01", "2026-09-18"},
            {"3", "payroll", "Nguyễn Quang Vinh", "4", "Payroll_Officer", "HOAT_DONG", "2026-09-01", "2026-09-19"},
            {"4", "employee1", "Trần Tiến Đạt", "3", "Employee", "HOAT_DONG", "2026-09-01", "2026-09-15"},
            {"5", "employee2", "Phạm Minh Quân", "5", "Employee", "HOAT_DONG", "2026-09-01", "2026-09-15"},
            {"6", "tamkhoa", "Lê Thị Mai", "6", "Employee", "KHOA", "2026-09-10", "2026-09-22"}
        });
        saveFrame(mainFrame, "10_TaiKhoan_QuanTri.png", 1280, 800);

        mainFrame.dispose();

        // 11. Hộp thoại điều chỉnh chấm công (06_ChamCong_DieuChinhDialog.png)
        captureDieuChinhDialog();

        // Đồng bộ các ảnh đại diện của TV5 sang screenshots/TV5/
        syncTv5Screenshots();

        System.out.println("==============================================================");
        System.out.println("   [HOÀN TẤT] TẤT CẢ ẢNH CHỤP MINH CHỨNG ĐÃ ĐƯỢC CẬP NHẬT!    ");
        System.out.println("==============================================================");
    }

    private static void captureLoginFrame() throws Exception {
        LoginFrame frame = new LoginFrame();
        frame.setSize(520, 620);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        Thread.sleep(500);

        saveFrame(frame, "01_LoginFrame.png", 520, 620);
        frame.dispose();
    }

    private static void captureDieuChinhDialog() throws Exception {
        DieuChinhChamCongDialog dialog = new DieuChinhChamCongDialog(null, 1, "Nguyễn Minh Trí", 9, 2026);
        dialog.setModal(false);
        dialog.setSize(880, 620);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        Thread.sleep(500);

        populateTables(dialog, new Object[][]{
            {"101", "2026-09-01", "08:00", "17:00", "CO_MAT", "Đúng giờ chuẩn"},
            {"102", "2026-09-02", "08:15", "17:00", "DI_TRE", "Trễ 15p do kẹt xe"},
            {"103", "2026-09-03", "07:55", "17:05", "CO_MAT", "Đúng giờ"},
            {"104", "2026-09-04", "08:00", "17:00", "CO_MAT", "Đúng giờ"},
            {"105", "2026-09-05", "08:00", "12:00", "NGHI_VIEC", "Nghỉ phép nửa buổi có phép"}
        });
        Thread.sleep(200);

        saveWindow(dialog, "06_ChamCong_DieuChinhDialog.png", 880, 620);
        dialog.dispose();
    }

    private static void saveFrame(JFrame frame, String fileName, int width, int height) throws Exception {
        saveWindow(frame, fileName, width, height);
    }

    private static void saveWindow(Window window, String fileName, int width, int height) throws Exception {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        window.paintAll(g);
        g.dispose();

        File out = new File(OUTPUT_DIR, fileName);
        watermark(img);
        ImageIO.write(img, "PNG", out);
        System.out.println("  [OK] Đã xuất ảnh: " + out.getName() + " (" + width + "x" + height + ") - Dung lượng: " + out.length() + " bytes");
    }

    private static void populateTables(Container container, Object[][]... tableDataSets) {
        List<JTable> tables = new ArrayList<>();
        findTables(container, tables);
        for (int i = 0; i < tables.size() && i < tableDataSets.length; i++) {
            JTable tbl = tables.get(i);
            DefaultTableModel model = (DefaultTableModel) tbl.getModel();
            model.setRowCount(0);
            for (Object[] row : tableDataSets[i]) {
                model.addRow(row);
            }
            tbl.revalidate();
            tbl.repaint();
        }
    }

    private static void findTables(Component c, List<JTable> list) {
        if (c == null) return;
        if (c instanceof JTable) {
            if (!list.contains(c)) list.add((JTable) c);
            return;
        }
        if (c instanceof JScrollPane) {
            Component view = ((JScrollPane) c).getViewport().getView();
            if (view != null) findTables(view, list);
        }
        if (c instanceof JTabbedPane) {
            JTabbedPane tp = (JTabbedPane) c;
            for (int i = 0; i < tp.getTabCount(); i++) {
                Component tab = tp.getComponentAt(i);
                if (tab != null) findTables(tab, list);
            }
        }
        if (c instanceof JSplitPane) {
            JSplitPane sp = (JSplitPane) c;
            if (sp.getLeftComponent() != null) findTables(sp.getLeftComponent(), list);
            if (sp.getRightComponent() != null) findTables(sp.getRightComponent(), list);
        }
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                findTables(child, list);
            }
        }
    }

    private static void updateLabel(Component c, String prefix, String newText) {
        if (c instanceof JLabel) {
            JLabel lbl = (JLabel) c;
            if (lbl.getText() != null && lbl.getText().startsWith(prefix)) {
                lbl.setText(newText);
            }
        }
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                updateLabel(child, prefix, newText);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void setComboBoxItems(Component c, Object... items) {
        if (c instanceof JComboBox) {
            JComboBox cb = (JComboBox) c;
            java.awt.event.ItemListener[] listeners = cb.getItemListeners();
            for (java.awt.event.ItemListener l : listeners) cb.removeItemListener(l);
            cb.removeAllItems();
            for (Object item : items) {
                cb.addItem(item);
            }
            if (items.length > 0) {
                cb.setSelectedItem(items[0]);
            }
            return;
        }
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                setComboBoxItems(child, items);
            }
        }
    }

    private static void syncTv5Screenshots() {
        try {
            File tv5Dir = new File(OUTPUT_DIR, "TV5");
            if (!tv5Dir.exists()) tv5Dir.mkdirs();

            File bcSrc = new File(OUTPUT_DIR, "09_BaoCao_ChotLuong.png");
            File bcDst = new File(tv5Dir, "TV5_BaoCao_ChotLuong.png");
            if (bcSrc.exists()) {
                Files.copy(bcSrc.toPath(), bcDst.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("  [OK] Đã đồng bộ sang TV5: " + bcDst.getName());
            }

            File tkSrc = new File(OUTPUT_DIR, "10_TaiKhoan_QuanTri.png");
            File tkDst = new File(tv5Dir, "TV5_TaiKhoan_QuanTri.png");
            if (tkSrc.exists()) {
                Files.copy(tkSrc.toPath(), tkDst.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("  [OK] Đã đồng bộ sang TV5: " + tkDst.getName());
            }
        } catch (Exception ex) {
            System.err.println("Lỗi khi đồng bộ TV5 screenshots: " + ex.getMessage());
        }
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
