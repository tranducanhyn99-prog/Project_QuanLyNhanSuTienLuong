package com.ui.main;

import com.session.Session;
import com.ui.admin.TaiKhoanPanel;
import com.ui.auth.LoginFrame;
import com.ui.baocao.BaoCaoPanel;
import com.ui.chamcong.ChamCongPanel;
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
        miNhapChamCong.addActionListener(e -> openTab("Chấm công", () -> new ChamCongPanel()));

        JMenuItem miXemChamCong = new JMenuItem("Xem tổng hợp chấm công");
        miXemChamCong.addActionListener(e -> openTab("Tổng hợp CC", () -> new ChamCongPanel(true)));

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
        JComponent welcomePanel = createWelcomePanel();
        tabbedPane.addTab("Trang chủ", welcomePanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JComponent createWelcomePanel() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(new Color(248, 250, 252));
        container.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        // 1. Hero banner (Gradient nền màu xanh doanh nghiệp)
        container.add(createHeroBanner());
        container.add(Box.createVerticalStrut(18));

        // 2. Thống kê KPI / Trạng thái hệ thống
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 14, 0));
        statsPanel.setOpaque(false);
        statsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        statsPanel.add(createStatCard("HỒ SƠ NHÂN SỰ", "5 Nhân viên", "● Đang hoạt động", new Color(14, 165, 233)));
        statsPanel.add(createStatCard("CƠ CẤU DOANH NGHIỆP", "4 PB • 5 Chức vụ", "● Chuẩn hóa danh mục", new Color(139, 92, 246)));
        statsPanel.add(createStatCard("KỲ TÍNH LƯƠNG", "Tháng 09 / 2026", "● Chu kỳ đang mở", new Color(245, 158, 11)));
        statsPanel.add(createStatCard("CƠ SỞ DỮ LIỆU", "SQL Server 2025", "● RBAC • ACID OK", new Color(16, 185, 129)));
        container.add(statsPanel);
        container.add(Box.createVerticalStrut(22));

        // 3. Tiêu đề khối thao tác nhanh
        JLabel lblSection = new JLabel("LỐI TẮT NGHIỆP VỤ NHANH");
        lblSection.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSection.setForeground(new Color(71, 85, 105));
        lblSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(lblSection);
        container.add(Box.createVerticalStrut(10));

        // 4. Lưới nút thao tác nhanh
        JPanel actionsPanel = new JPanel(new GridLayout(2, 3, 14, 14));
        actionsPanel.setOpaque(false);
        actionsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        boolean canHR = session.hasRole("DB_Admin", "HR_Manager");
        boolean canPayroll = session.hasRole("DB_Admin", "Payroll_Officer");
        boolean canCC = session.hasRole("DB_Admin", "HR_Manager", "Payroll_Officer");
        boolean canAdmin = session.hasRole("DB_Admin");

        actionsPanel.add(createActionCard("Hồ sơ Nhân viên", "Quản lý lý lịch, chức vụ, phòng ban & lương cơ bản", "NV",
            () -> openTab("Nhân viên", () -> new NhanVienPanel()), canHR));

        actionsPanel.add(createActionCard("Phòng ban & Chức vụ", "Thiết lập cơ cấu phòng ban và phụ cấp trách nhiệm", "DM",
            () -> openTab("Danh mục", () -> new DanhMucPanel()), canHR));

        actionsPanel.add(createActionCard("Chấm công Nhân sự", "Theo dõi ngày công, làm thêm giờ và nghỉ phép", "CC",
            () -> openTab("Chấm công", () -> new ChamCongPanel()), canCC));

        actionsPanel.add(createActionCard("Tính toán Bảng lương", "Quy trình tính lương tự động, BHXH và Thuế TNCN", "BL",
            () -> openTab("Tính bảng lương", () -> new BangLuongPanel()), canPayroll));

        actionsPanel.add(createActionCard("Báo cáo & Phiếu lương", "Xuất bảng lương tổng hợp và tra cứu phiếu lương", "BC",
            () -> openTab("Báo cáo tổng hợp", () -> new BaoCaoPanel()), true));

        actionsPanel.add(createActionCard("Quản trị Tài khoản", "Phân quyền người dùng, bảo mật và tài khoản đăng nhập", "QT",
            () -> openTab("Quản lý tài khoản", () -> new TaiKhoanPanel()), canAdmin));

        container.add(actionsPanel);
        container.add(Box.createVerticalStrut(20));

        // 5. Thanh thông tin phiên đăng nhập
        JPanel footerCard = new JPanel(new BorderLayout());
        footerCard.setOpaque(true);
        footerCard.setBackground(Color.WHITE);
        footerCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));
        footerCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        int maNV = session.getMaNV();
        String idInfo = (maNV > 0) ? ("Mã NV: " + maNV) : "Tài khoản quản trị";
        JLabel lblLeftInfo = new JLabel("Phiên đăng nhập: " + session.getTenDangNhap() + " (" + idInfo + ")  |  Cơ chế phân quyền RBAC");
        lblLeftInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLeftInfo.setForeground(new Color(100, 116, 139));

        JLabel lblRightInfo = new JLabel("Hệ Quản trị Cơ sở Dữ liệu – Nhóm TV5");
        lblRightInfo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblRightInfo.setForeground(new Color(148, 163, 184));

        footerCard.add(lblLeftInfo, BorderLayout.WEST);
        footerCard.add(lblRightInfo, BorderLayout.EAST);

        container.add(footerCard);

        JScrollPane scroll = new JScrollPane(container);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel createHeroBanner() {
        JPanel hero = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient nền chuyển sắc từ Navy (#1A365D) sang Royal Blue (#2563EB)
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(26, 54, 93),
                    getWidth(), getHeight(), new Color(37, 99, 235)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
            }
        };
        hero.setOpaque(false);
        hero.setLayout(new BorderLayout(20, 10));
        hero.setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));

        JPanel leftCol = new JPanel();
        leftCol.setOpaque(false);
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));

        JLabel lblSystem = new JLabel("HỆ THỐNG QUẢN LÝ NHÂN SỰ VÀ TIỀN LƯƠNG ENTERPRISE");
        lblSystem.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSystem.setForeground(new Color(191, 219, 254));

        JLabel lblGreet = new JLabel("Xin chào, " + session.getUserInfo() + "!");
        lblGreet.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblGreet.setForeground(Color.WHITE);

        String roleStr = session.getFullRoleDisplayName();
        if (roleStr.isEmpty()) roleStr = "Chưa xác định";

        JLabel lblDesc = new JLabel("Vai trò hiện hành: " + roleStr + "  •  Hãy chọn một nghiệp vụ bên dưới hoặc sử dụng thanh thực đơn để bắt đầu.");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDesc.setForeground(new Color(224, 231, 255));

        leftCol.add(lblSystem);
        leftCol.add(Box.createVerticalStrut(6));
        leftCol.add(lblGreet);
        leftCol.add(Box.createVerticalStrut(6));
        leftCol.add(lblDesc);

        hero.add(leftCol, BorderLayout.CENTER);
        return hero;
    }

    private JPanel createStatCard(String title, String value, String subtext, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
            )
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(new Color(100, 116, 139));

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblValue.setForeground(new Color(15, 23, 42));

        JLabel lblSub = new JLabel(subtext);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(accentColor);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        card.add(lblSub, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createActionCard(String title, String desc, String badge, Runnable action, boolean enabled) {
        JPanel card = new JPanel(new BorderLayout(10, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(enabled ? new Color(226, 232, 240) : new Color(241, 245, 249), 1),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        card.setCursor(enabled ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());

        // Tiêu đề và badge
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topRow.setOpaque(false);

        JLabel lblBadge = new JLabel(badge);
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBadge.setOpaque(true);
        lblBadge.setBackground(enabled ? new Color(238, 242, 255) : new Color(241, 245, 249));
        lblBadge.setForeground(enabled ? new Color(67, 56, 202) : Color.GRAY);
        lblBadge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(enabled ? new Color(199, 210, 254) : new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(enabled ? new Color(30, 41, 59) : new Color(156, 163, 175));

        topRow.add(lblBadge);
        topRow.add(lblTitle);

        JLabel lblDesc = new JLabel(enabled ? desc : "Không khả dụng cho vai trò tài khoản hiện tại");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc.setForeground(enabled ? new Color(100, 116, 139) : new Color(156, 163, 175));

        card.add(topRow, BorderLayout.NORTH);
        card.add(lblDesc, BorderLayout.CENTER);

        if (enabled) {
            Color normalBg = Color.WHITE;
            Color hoverBg = new Color(245, 248, 255);
            Color hoverBorder = new Color(147, 197, 253);
            Color normalBorder = new Color(226, 232, 240);

            card.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    card.setBackground(hoverBg);
                    card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(hoverBorder, 1),
                        BorderFactory.createEmptyBorder(14, 16, 14, 16)
                    ));
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    card.setBackground(normalBg);
                    card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(normalBorder, 1),
                        BorderFactory.createEmptyBorder(14, 16, 14, 16)
                    ));
                }

                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    action.run();
                }
            });
        }

        return card;
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

        lblStatusUser = new JLabel("Người dùng: " + session.getUserInfo());
        lblStatusUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        String roleStr = session.getFullRoleDisplayName();
        lblStatusRole = new JLabel("Vai trò: " + (roleStr.isEmpty() ? "Chưa đăng nhập" : roleStr));
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

        // Menu Chấm công: DB_Admin, HR_Manager, Payroll_Officer (Payroll_Officer xem tổng hợp để đối soát lương)
        menuChamCong.setVisible(session.hasRole("DB_Admin", "HR_Manager", "Payroll_Officer"));
        if (menuChamCong.getItemCount() >= 2) {
            menuChamCong.getItem(0).setEnabled(session.hasRole("DB_Admin", "HR_Manager"));
        }

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
        String titleUser = session.getUserInfo();
        String roleStr = session.getFullRoleDisplayName();
        String roleSuffix = !roleStr.isEmpty() ? " [" + roleStr + "]" : "";
        setTitle(APP_TITLE + " – " + titleUser + roleSuffix);
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

    /**
     * Cập nhật lại thông tin hiển thị và phân quyền menu từ Session hiện tại.
     */
    public void updateSessionDisplay() {
        applyRolePermissions();
        if (lblStatusUser != null) {
            lblStatusUser.setText("Người dùng: " + session.getUserInfo());
        }
        if (lblStatusRole != null) {
            String roleStr = session.getFullRoleDisplayName();
            lblStatusRole.setText("Vai trò: " + (roleStr.isEmpty() ? "Chưa đăng nhập" : roleStr));
        }
        String titleUser = session.getUserInfo();
        String roleStr = session.getFullRoleDisplayName();
        String roleSuffix = !roleStr.isEmpty() ? " [" + roleStr + "]" : "";
        setTitle(APP_TITLE + " – " + titleUser + roleSuffix);

        if (tabbedPane != null) {
            int welcomeIdx = tabbedPane.indexOfTab("Trang chủ");
            if (welcomeIdx >= 0) {
                tabbedPane.setComponentAt(welcomeIdx, createWelcomePanel());
            }
        }
    }

    // ─── Getters phục vụ kiểm thử phân quyền và giao diện ─────────────

    public JMenu getMenuNhanVien() { return menuNhanVien; }
    public JMenu getMenuDanhMuc() { return menuDanhMuc; }
    public JMenu getMenuChamCong() { return menuChamCong; }
    public JMenu getMenuPhuCapKhauTru() { return menuPhuCapKhauTru; }
    public JMenu getMenuLuong() { return menuLuong; }
    public JMenu getMenuBaoCao() { return menuBaoCao; }
    public JMenu getMenuQuanTri() { return menuQuanTri; }
    public JLabel getLblStatusUser() { return lblStatusUser; }
    public JLabel getLblStatusRole() { return lblStatusRole; }
    public JTabbedPane getTabbedPane() { return tabbedPane; }

    // ═══════════════════════════════════════════════════════════════════
    //  FUNCTIONAL INTERFACE (để truyền lambda tạo Panel)
    // ═══════════════════════════════════════════════════════════════════

    @FunctionalInterface
    private interface PanelFactory {
        JPanel create();
    }
}
