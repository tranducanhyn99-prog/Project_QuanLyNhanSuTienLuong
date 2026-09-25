package com.ui.main;

import com.session.Session;
import com.ui.admin.TaiKhoanPanel;
import com.ui.auth.LoginFrame;
import com.ui.baocao.BaoCaoPanel;
import com.ui.luong.BangLuongPanel;
import com.ui.nhanvien.DanhMucPanel;
import com.ui.nhanvien.NhanVienPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * MainFrame – Khung chính của ứng dụng sau đăng nhập.
 *
 * Chức năng:
 * - Hiển thị menu bar với các module nghiệp vụ
 * - Ẩn/hiện menu dựa trên Session.getVaiTro()
 * - JTabbedPane chứa các Panel nghiệp vụ
 * - Thanh trạng thái hiển thị user đang đăng nhập
 * - Logout → quay về LoginFrame
 *
 * Ma trận phân quyền ứng dụng (theo TV5_Security_Design.md):
 *   Menu Nhân viên:        DB_Admin, HR_Manager
 *   Menu Danh mục:         DB_Admin, HR_Manager
 *   Menu Chấm công:        DB_Admin, HR_Manager
 *   Menu Phụ cấp/Khấu trừ: DB_Admin, HR_Manager, Payroll_Officer
 *   Menu Lương:            DB_Admin, Payroll_Officer
 *   Menu Báo cáo:          DB_Admin, HR_Manager, Payroll_Officer (+ Employee chỉ phiếu lương)
 *   Menu Quản trị:         DB_Admin
 *
 * @author Trần Đức Anh (TV5 – MSSV 24110155)
 */
public class MainFrame extends JFrame {

    private static final String APP_TITLE = "Hệ thống Quản lý Nhân sự và Tiền lương";

    private final Session session = Session.getInstance();

    // UI Components
    private JTabbedPane tabbedPane;
    private JLabel      lblStatusUser;
    private JLabel      lblStatusRole;

    // Menu items (lưu reference để ẩn/hiện)
    private JMenu menuNhanVien;
    private JMenu menuDanhMuc;
    private JMenu menuChamCong;
    private JMenu menuPhuCapKhauTru;
    private JMenu menuLuong;
    private JMenu menuBaoCao;
    private JMenu menuQuanTri;

    public MainFrame() {
        initMenuBar();
        initTabbedPane();
        initStatusBar();
        applyRolePermissions();
        setupFrame();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  MENU BAR
    // ═══════════════════════════════════════════════════════════════════

    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // ─── Menu Nhân viên ──────────────────────────────────────────
        menuNhanVien = new JMenu("Nhân viên");
        menuNhanVien.setMnemonic('N');

        JMenuItem miQuanLyNhanVien = new JMenuItem("Quản lý nhân viên");
        miQuanLyNhanVien.addActionListener(e -> openTab("Nhân viên", () -> new NhanVienPanel()));

        menuNhanVien.add(miQuanLyNhanVien);
        menuBar.add(menuNhanVien);

        // ─── Menu Danh mục ───────────────────────────────────────────
        menuDanhMuc = new JMenu("Danh mục");
        menuDanhMuc.setMnemonic('D');

        JMenuItem miPhongBanChucVu = new JMenuItem("Phòng ban & Chức vụ");
        miPhongBanChucVu.addActionListener(e -> openTab("Danh mục", () -> new DanhMucPanel()));

        menuDanhMuc.add(miPhongBanChucVu);
        menuBar.add(menuDanhMuc);

        // ─── Menu Chấm công ──────────────────────────────────────────
        menuChamCong = new JMenu("Chấm công");
        menuChamCong.setMnemonic('C');

        JMenuItem miNhapChamCong = new JMenuItem("Nhập chấm công");
        miNhapChamCong.addActionListener(e -> openTab("Chấm công", this::createPlaceholderPanel));

        JMenuItem miXemChamCong = new JMenuItem("Xem tổng hợp chấm công");
        miXemChamCong.addActionListener(e -> openTab("Tổng hợp CC", this::createPlaceholderPanel));

        menuChamCong.add(miNhapChamCong);
        menuChamCong.add(miXemChamCong);
        menuBar.add(menuChamCong);

        // ─── Menu Phụ cấp / Khấu trừ ────────────────────────────────
        menuPhuCapKhauTru = new JMenu("Phụ cấp / Khấu trừ");
        menuPhuCapKhauTru.setMnemonic('P');

        JMenuItem miPhuCap = new JMenuItem("Quản lý phụ cấp & khấu trừ");
        miPhuCap.addActionListener(e -> openTab("Phụ cấp / Khấu trừ", this::createPlaceholderPanel));

        menuPhuCapKhauTru.add(miPhuCap);
        menuBar.add(menuPhuCapKhauTru);

        // ─── Menu Lương ──────────────────────────────────────────────
        menuLuong = new JMenu("Lương");
        menuLuong.setMnemonic('L');

        JMenuItem miTinhLuong = new JMenuItem("Tính bảng lương");
        miTinhLuong.addActionListener(e -> openTab("Tính bảng lương", () -> new BangLuongPanel()));

        JMenuItem miChotLuong = new JMenuItem("Chốt bảng lương");
        miChotLuong.addActionListener(e -> openTab("Báo cáo & Chốt lương", () -> new BaoCaoPanel()));

        menuLuong.add(miTinhLuong);
        menuLuong.add(miChotLuong);
        menuBar.add(menuLuong);

        // ─── Menu Báo cáo ────────────────────────────────────────────
        menuBaoCao = new JMenu("Báo cáo");
        menuBaoCao.setMnemonic('B');

        JMenuItem miBaoCaoTongHop = new JMenuItem("Báo cáo tổng hợp");
        miBaoCaoTongHop.addActionListener(e -> openTab("Báo cáo tổng hợp", () -> new BaoCaoPanel()));

        JMenuItem miPhieuLuong = new JMenuItem("Phiếu lương cá nhân");
        miPhieuLuong.addActionListener(e -> openTab("Phiếu lương cá nhân", () -> new BaoCaoPanel()));

        menuBaoCao.add(miBaoCaoTongHop);
        menuBaoCao.add(miPhieuLuong);
        menuBar.add(menuBaoCao);

        // ─── Menu Quản trị ───────────────────────────────────────────
        menuQuanTri = new JMenu("Quản trị");
        menuQuanTri.setMnemonic('Q');

        JMenuItem miQuanLyTaiKhoan = new JMenuItem("Quản lý tài khoản");
        miQuanLyTaiKhoan.addActionListener(e -> openTab("Quản lý tài khoản", () -> new TaiKhoanPanel()));

        menuQuanTri.add(miQuanLyTaiKhoan);
        menuBar.add(menuQuanTri);

        // ─── Spacer + Menu Hệ thống (Logout) ────────────────────────
        menuBar.add(Box.createHorizontalGlue());

        JMenu menuHeThong = new JMenu("Hệ thống");
        menuHeThong.setMnemonic('H');

        JMenuItem miDangXuat = new JMenuItem("Đăng xuất");
        miDangXuat.addActionListener(e -> handleLogout());

        JMenuItem miThoat = new JMenuItem("Thoát");
        miThoat.addActionListener(e -> handleExit());

        menuHeThong.add(miDangXuat);
        menuHeThong.addSeparator();
        menuHeThong.add(miThoat);
        menuBar.add(menuHeThong);

        setJMenuBar(menuBar);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TABBED PANE (Khu vực nội dung chính)
    // ═══════════════════════════════════════════════════════════════════

    private void initTabbedPane() {
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Tab chào mừng mặc định
        JPanel welcomePanel = createWelcomePanel();
        tabbedPane.addTab("Trang chủ", welcomePanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        JPanel content = new JPanel(new GridLayout(4, 1, 0, 8));
        content.setBackground(Color.WHITE);

        JLabel lblWelcome = new JLabel("Chào mừng, " + session.getDisplayName() + "!", SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JLabel lblRole = new JLabel("Vai trò: " + session.getVaiTroDisplayName(), SwingConstants.CENTER);
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblRole.setForeground(Color.GRAY);

        JLabel lblInfo = new JLabel(APP_TITLE, SwingConstants.CENTER);
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblInfo.setForeground(new Color(100, 100, 100));

        JLabel lblHint = new JLabel("Sử dụng menu phía trên để truy cập các chức năng.", SwingConstants.CENTER);
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblHint.setForeground(new Color(120, 120, 120));

        content.add(lblWelcome);
        content.add(lblRole);
        content.add(lblInfo);
        content.add(lblHint);

        panel.add(content);
        return panel;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  STATUS BAR
    // ═══════════════════════════════════════════════════════════════════

    private void initStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout(10, 0));
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));

        lblStatusUser = new JLabel("Người dùng: " + session.getDisplayName());
        lblStatusUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        lblStatusRole = new JLabel("Vai trò: " + session.getVaiTroDisplayName());
        lblStatusRole.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatusRole.setForeground(Color.GRAY);
        lblStatusRole.setHorizontalAlignment(SwingConstants.RIGHT);

        statusBar.add(lblStatusUser, BorderLayout.WEST);
        statusBar.add(lblStatusRole, BorderLayout.EAST);

        add(statusBar, BorderLayout.SOUTH);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  PHÂN QUYỀN MENU THEO ROLE
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Ẩn/hiện menu dựa trên vai trò trong Session.
     * Áp dụng đúng ma trận phân quyền từ TV5_Security_Design.md
     */
    private void applyRolePermissions() {
        String role = session.getVaiTro();
        if (role == null) role = "";

        // Menu Nhân viên: DB_Admin, HR_Manager
        menuNhanVien.setVisible(session.hasRole("DB_Admin", "HR_Manager"));

        // Menu Danh mục: DB_Admin, HR_Manager
        menuDanhMuc.setVisible(session.hasRole("DB_Admin", "HR_Manager"));

        // Menu Chấm công: DB_Admin, HR_Manager
        menuChamCong.setVisible(session.hasRole("DB_Admin", "HR_Manager"));

        // Menu Phụ cấp / Khấu trừ: DB_Admin, HR_Manager, Payroll_Officer
        menuPhuCapKhauTru.setVisible(session.hasRole("DB_Admin", "HR_Manager", "Payroll_Officer"));

        // Menu Lương: DB_Admin, Payroll_Officer
        menuLuong.setVisible(session.hasRole("DB_Admin", "Payroll_Officer"));

        // Menu Báo cáo: Tất cả role đều thấy (Employee chỉ thấy phiếu lương cá nhân)
        menuBaoCao.setVisible(true);
        // Ẩn "Báo cáo tổng hợp" cho Employee
        if (menuBaoCao.getItemCount() >= 1) {
            menuBaoCao.getItem(0).setVisible(!role.equals("Employee"));
        }

        // Menu Quản trị: chỉ DB_Admin
        menuQuanTri.setVisible(session.hasRole("DB_Admin"));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TAB MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Mở hoặc chuyển sang tab đã tồn tại.
     * Nếu tab chưa có → tạo mới từ panelFactory.
     */
    private void openTab(String title, PanelFactory factory) {
        // Kiểm tra tab đã mở chưa
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (tabbedPane.getTitleAt(i).equals(title)) {
                tabbedPane.setSelectedIndex(i);
                return;
            }
        }

        // Tạo tab mới
        JPanel panel = factory.create();
        tabbedPane.addTab(title, panel);

        // Thêm nút đóng tab (X)
        int index = tabbedPane.indexOfTab(title);
        tabbedPane.setTabComponentAt(index, createTabHeader(title));
        tabbedPane.setSelectedIndex(index);
    }

    /**
     * Tạo header tab có nút đóng (X).
     */
    private JPanel createTabHeader(String title) {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        header.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JButton btnClose = new JButton("×");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setFocusPainted(false);
        btnClose.setMargin(new Insets(0, 4, 0, 4));
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.setForeground(Color.GRAY);
        btnClose.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnClose.setForeground(Color.RED);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnClose.setForeground(Color.GRAY);
            }
        });
        btnClose.addActionListener(e -> {
            int idx = tabbedPane.indexOfTab(title);
            if (idx >= 0 && !title.equals("Trang chủ")) {
                tabbedPane.removeTabAt(idx);
            }
        });

        header.add(lblTitle);
        header.add(btnClose);
        return header;
    }

    /**
     * Panel placeholder cho các module chưa được tích hợp (TV2, TV3, TV4).
     */
    private JPanel createPlaceholderPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(255, 255, 245));

        JLabel lbl = new JLabel("Module này đang được phát triển bởi thành viên khác...");
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lbl.setForeground(Color.GRAY);
        panel.add(lbl);

        return panel;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  HỆ THỐNG (LOGOUT / EXIT)
    // ═══════════════════════════════════════════════════════════════════

    private void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc chắn muốn đăng xuất?",
            "Xác nhận đăng xuất",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            Session.getInstance().logout();
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
                this.dispose();
            });
        }
    }

    private void handleExit() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc chắn muốn thoát ứng dụng?",
            "Xác nhận thoát",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            Session.getInstance().logout();
            System.exit(0);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  CẤU HÌNH JFRAME
    // ═══════════════════════════════════════════════════════════════════

    private void setupFrame() {
        setTitle(APP_TITLE + " – " + session.getDisplayName() + " [" + session.getVaiTroDisplayName() + "]");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════
    //  FUNCTIONAL INTERFACE (để truyền lambda tạo Panel)
    // ═══════════════════════════════════════════════════════════════════

    @FunctionalInterface
    private interface PanelFactory {
        JPanel create();
    }
}
