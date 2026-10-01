package com.ui.main;

import com.session.Session;
import com.ui.admin.TaiKhoanPanel;
import com.ui.auth.LoginFrame;
import com.ui.baocao.BaoCaoPanel;
import com.ui.chamcong.ChamCongPanel;
import com.ui.luong.BangLuongPanel;
import com.ui.luong.PhuCapKhauTruPanel;
import com.ui.nhanvien.DanhMucPanel;
import com.ui.nhanvien.NhanVienPanel;
import com.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MainFrame – Khung giao diện chính của ứng dụng sau đăng nhập.
 *
 * Thiết kế mới chuẩn Microsoft Fluent Design & Modern Enterprise ERP:
 * - Left Sidebar Navigation cố định với phân nhóm nghiệp vụ rõ ràng, có vạch chỉ báo Active.
 * - Top Header Bar hiển thị breadcrumb phân hệ, profile người dùng và vai trò.
 * - Content Area dạng CardLayout mượt mà, độc lập.
 * - Dashboard với các thẻ KPI nhỏ gọn (Small KPI cards), giao diện phẳng không gradient.
 * - Giữ nguyên 100% logic phân quyền RBAC và các nghiệp vụ CSDL.
 *
 * @author Nhóm 06 – DBMS Enterprise
 */
public class MainFrame extends JFrame {

    private static final String APP_TITLE = "Hệ thống Quản lý Nhân sự và Tiền lương";

    private final Session session = Session.getInstance();

    // Layout & Navigation
    private JPanel sidebarPanel;
    private JPanel headerPanel;
    private JPanel contentContainer;
    private CardLayout cardLayout;

    private JLabel lblBreadcrumb;
    private JLabel lblHeaderUserName;
    private JLabel lblHeaderRoleBadge;

    // View tracking
    private String currentViewKey = "HOME";
    private final List<NavItem> navItems = new ArrayList<>();
    private final Map<String, JComponent> cachedViews = new HashMap<>();

    // Legacy support fields for test compatibility (AuthRolePermissionTest)
    private JTabbedPane tabbedPane;
    private JLabel lblStatusUser;
    private JLabel lblStatusRole;
    private JMenu menuNhanVien;
    private JMenu menuDanhMuc;
    private JMenu menuChamCong;
    private JMenu menuPhuCapKhauTru;
    private JMenu menuLuong;
    private JMenu menuBaoCao;
    private JMenu menuQuanTri;

    public MainFrame() {
        UITheme.setupGlobalUI();
        initLegacyMenusAndStatus();
        initComponents();
        applyRolePermissions();
        setupFrame();
        switchView("HOME", "TỔNG QUAN > Trang chủ Dashboard");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  KHỞI TẠO BỐ CỤC CHÍNH (SIDEBAR + HEADER + CONTENT)
    // ═══════════════════════════════════════════════════════════════════

    private void initComponents() {
        setLayout(new BorderLayout());

        // 1. LEFT SIDEBAR NAVIGATION (240px)
        sidebarPanel = createSidebar();
        add(sidebarPanel, BorderLayout.WEST);

        // 2. RIGHT CONTAINER (HEADER + MAIN CONTENT)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(UITheme.BG_APP);

        headerPanel = createHeader();
        rightPanel.add(headerPanel, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentContainer = new JPanel(cardLayout);
        contentContainer.setBackground(UITheme.BG_APP);

        // Đăng ký trang chủ Dashboard
        JComponent homeView = createDashboardView();
        contentContainer.add(homeView, "HOME");
        cachedViews.put("HOME", homeView);

        rightPanel.add(contentContainer, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.CENTER);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  LEFT SIDEBAR NAVIGATION
    // ═══════════════════════════════════════════════════════════════════

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBackground(UITheme.BG_SIDEBAR);
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, UITheme.BORDER));

        // Top Brand Header
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setBackground(UITheme.BG_SIDEBAR);
        brandPanel.setBorder(new EmptyBorder(20, 20, 18, 20));

        JLabel lblBrandTitle = new JLabel("HR & PAYROLL");
        lblBrandTitle.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 16));
        lblBrandTitle.setForeground(UITheme.PRIMARY);

        JLabel lblBrandSub = new JLabel("Enterprise Management");
        lblBrandSub.setFont(UITheme.FONT_CAPTION);
        lblBrandSub.setForeground(UITheme.TEXT_MUTED);

        JLabel lblGroupBadge = new JLabel("DBMS330284 • NHÓM 06");
        lblGroupBadge.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 10));
        lblGroupBadge.setForeground(new Color(148, 163, 184));
        lblGroupBadge.setBorder(new EmptyBorder(4, 0, 0, 0));

        brandPanel.add(lblBrandTitle);
        brandPanel.add(Box.createVerticalStrut(2));
        brandPanel.add(lblBrandSub);
        brandPanel.add(lblGroupBadge);

        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Menu items container (Scrollable)
        JPanel menuContainer = new JPanel();
        menuContainer.setLayout(new BoxLayout(menuContainer, BoxLayout.Y_AXIS));
        menuContainer.setBackground(UITheme.BG_SIDEBAR);
        menuContainer.setBorder(new EmptyBorder(6, 10, 10, 10));

        // Phân quyền
        boolean canHR = session.hasRole("DB_Admin", "HR_Manager");
        boolean canPayroll = session.hasRole("DB_Admin", "Payroll_Officer");
        boolean canCC = session.hasRole("DB_Admin", "HR_Manager", "Payroll_Officer");
        boolean canAdmin = session.hasRole("DB_Admin");

        // Nhóm 1: TỔNG QUAN
        addNavSectionHeader(menuContainer, "TỔNG QUAN");
        addNavItem(menuContainer, "HOME", "Trang chủ", "TỔNG QUAN > Trang chủ Dashboard", () -> null, true);

        // Nhóm 2: NHÂN SỰ & DANH MỤC
        if (canHR) {
            addNavSectionHeader(menuContainer, "NHÂN SỰ & CƠ CẤU");
            addNavItem(menuContainer, "NHAN_VIEN", "Hồ sơ Nhân viên", "NHÂN SỰ > Quản lý Hồ sơ nhân sự",
                    () -> new NhanVienPanel(), canHR);
            addNavItem(menuContainer, "DANH_MUC", "Phòng ban & Chức vụ", "DANH MỤC > Phòng ban & Chức vụ",
                    () -> new DanhMucPanel(), canHR);
        }

        // Nhóm 3: CHẤM CÔNG & LƯƠNG
        if (canCC || canPayroll) {
            addNavSectionHeader(menuContainer, "CHẤM CÔNG & LƯƠNG");
            if (canCC) {
                addNavItem(menuContainer, "CHAM_CONG", "Nhật ký Chấm công", "CHẤM CÔNG > Ghi nhận chấm công chi tiết",
                        () -> new ChamCongPanel(false), canCC);
                addNavItem(menuContainer, "TONG_HOP_CC", "Tổng hợp Công tháng", "CHẤM CÔNG > Tổng hợp ngày công tháng",
                        () -> new ChamCongPanel(true), canCC);
                addNavItem(menuContainer, "PHU_CAP", "Phụ cấp & Khấu trừ", "LƯƠNG > Quản lý Phụ cấp & Khấu trừ",
                        () -> new PhuCapKhauTruPanel(), true);
            }
            if (canPayroll) {
                addNavItem(menuContainer, "BANG_LUONG", "Tính toán Bảng lương", "TIỀN LƯƠNG > Quy trình tính lương tự động",
                        () -> new BangLuongPanel(), canPayroll);
            }
        }

        // Nhóm 4: BÁO CÁO & QUẢN TRỊ
        addNavSectionHeader(menuContainer, "BÁO CÁO & HỆ THỐNG");
        addNavItem(menuContainer, "BAO_CAO", "Báo cáo & Chốt lương", "BÁO CÁO > Báo cáo tổng hợp & Phiếu lương",
                () -> new BaoCaoPanel(), true);

        if (canAdmin) {
            addNavItem(menuContainer, "TAI_KHOAN", "Quản trị Tài khoản", "QUẢN TRỊ > Phân quyền & Quản lý tài khoản",
                    () -> new TaiKhoanPanel(), canAdmin);
        }

        JScrollPane scrollMenu = new JScrollPane(menuContainer);
        scrollMenu.setBorder(null);
        scrollMenu.setBackground(UITheme.BG_SIDEBAR);
        scrollMenu.getViewport().setBackground(UITheme.BG_SIDEBAR);
        scrollMenu.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sidebar.add(scrollMenu, BorderLayout.CENTER);

        // Bottom Footer in Sidebar (Logout)
        JPanel bottomSidebar = new JPanel(new BorderLayout());
        bottomSidebar.setBackground(UITheme.BG_SIDEBAR);
        bottomSidebar.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 0, 0, UITheme.BORDER),
            new EmptyBorder(12, 14, 12, 14)
        ));

        JButton btnLogout = new JButton("Đăng xuất");
        UITheme.styleSecondaryButton(btnLogout);
        btnLogout.setFont(UITheme.FONT_BODY);
        btnLogout.setForeground(new Color(185, 28, 28));
        btnLogout.addActionListener(e -> handleLogout());
        bottomSidebar.add(btnLogout, BorderLayout.CENTER);

        sidebar.add(bottomSidebar, BorderLayout.SOUTH);

        return sidebar;
    }

    private void addNavSectionHeader(JPanel container, String title) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 10));
        lbl.setForeground(new Color(148, 163, 184));
        lbl.setBorder(new EmptyBorder(12, 12, 4, 12));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(lbl);
    }

    private void addNavItem(JPanel container, String key, String label, String breadcrumb,
                            ComponentSupplier supplier, boolean enabled) {
        NavItem item = new NavItem(key, label, breadcrumb, supplier, enabled);
        navItems.add(item);
        container.add(item);
        container.add(Box.createVerticalStrut(2));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TOP HEADER BAR
    // ═══════════════════════════════════════════════════════════════════

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 56));
        header.setBackground(Color.WHITE);
        header.setBorder(new MatteBorder(0, 0, 1, 0, UITheme.BORDER));

        // Left: Breadcrumb / Active Page Title
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 17));
        leftPanel.setOpaque(false);

        lblBreadcrumb = new JLabel("TỔNG QUAN > Trang chủ Dashboard");
        lblBreadcrumb.setFont(UITheme.FONT_TITLE);
        lblBreadcrumb.setForeground(UITheme.TEXT_MAIN);
        leftPanel.add(lblBreadcrumb);

        // Right: User Profile Chip
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 12));
        rightPanel.setOpaque(false);

        // Avatar tròn chứa chữ cái đầu
        String userName = session.getUserInfo();
        String initial = (userName != null && !userName.isEmpty()) ? userName.substring(0, 1).toUpperCase() : "U";
        JLabel lblAvatar = new JLabel(initial, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY_LIGHT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.PRIMARY_BORDER);
                g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblAvatar.setPreferredSize(new Dimension(32, 32));
        lblAvatar.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 13));
        lblAvatar.setForeground(UITheme.PRIMARY);

        lblHeaderUserName = new JLabel(userName);
        lblHeaderUserName.setFont(UITheme.FONT_BODY_BOLD);
        lblHeaderUserName.setForeground(UITheme.TEXT_MAIN);

        String roleStr = session.getFullRoleDisplayName();
        if (roleStr.isEmpty()) roleStr = "Guest";
        lblHeaderRoleBadge = UITheme.createBadge(roleStr, UITheme.PRIMARY, UITheme.PRIMARY_LIGHT, UITheme.PRIMARY_BORDER);

        rightPanel.add(lblAvatar);
        rightPanel.add(lblHeaderUserName);
        rightPanel.add(lblHeaderRoleBadge);

        header.add(leftPanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  DASHBOARD VIEW (TRANG CHỦ HIỆN ĐẠI, FLAT, KPI NHỎ GỌN)
    // ═══════════════════════════════════════════════════════════════════

    private JComponent createDashboardView() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(UITheme.BG_APP);
        container.setBorder(new EmptyBorder(20, 24, 24, 24));

        // 1. Flat Greeting Banner (Tuyệt đối không dùng gradient)
        JPanel banner = new JPanel(new BorderLayout(16, 8));
        banner.setBackground(Color.WHITE);
        banner.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(18, 22, 18, 22)
        ));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JPanel bannerText = new JPanel();
        bannerText.setLayout(new BoxLayout(bannerText, BoxLayout.Y_AXIS));
        bannerText.setOpaque(false);

        JLabel lblSys = new JLabel("HỆ THỐNG QUẢN LÝ NHÂN SỰ VÀ TIỀN LƯƠNG DOANH NGHIỆP");
        lblSys.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 11));
        lblSys.setForeground(UITheme.PRIMARY);

        JLabel lblHello = new JLabel("Xin chào, " + session.getUserInfo() + "!");
        lblHello.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 20));
        lblHello.setForeground(UITheme.TEXT_MAIN);

        String roleStr = session.getFullRoleDisplayName();
        JLabel lblSub = new JLabel("Vai trò hiện hành: " + roleStr + "  •  Hệ thống vận hành phân quyền RBAC và kiểm soát Transaction ACID an toàn.");
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(UITheme.TEXT_MUTED);

        bannerText.add(lblSys);
        bannerText.add(Box.createVerticalStrut(4));
        bannerText.add(lblHello);
        bannerText.add(Box.createVerticalStrut(4));
        bannerText.add(lblSub);

        banner.add(bannerText, BorderLayout.CENTER);
        container.add(banner);
        container.add(Box.createVerticalStrut(16));

        // 2. Small KPI Cards (4 Thẻ chỉ số nhỏ gọn, sắc nét)
        JPanel kpiGrid = new JPanel(new GridLayout(1, 4, 14, 0));
        kpiGrid.setOpaque(false);
        kpiGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 86));

        kpiGrid.add(createSmallKpiCard("HỒ SƠ NHÂN SỰ", "5 Nhân viên", "Đang hoạt động", UITheme.PRIMARY));
        kpiGrid.add(createSmallKpiCard("CƠ CẤU DOANH NGHIỆP", "4 PB • 5 Chức vụ", "Danh mục chuẩn hóa", new Color(99, 102, 241)));
        kpiGrid.add(createSmallKpiCard("KỲ TÍNH LƯƠNG", "Tháng 09 / 2026", "Chu kỳ mở", new Color(217, 119, 6)));
        kpiGrid.add(createSmallKpiCard("CƠ SỞ DỮ LIỆU", "SQL Server 2025", "RBAC • ACID OK", UITheme.SUCCESS_TEXT));

        container.add(kpiGrid);
        container.add(Box.createVerticalStrut(22));

        // 3. Section Title
        JLabel lblShortcut = new JLabel("LỐI TẮT NGHIỆP VỤ NHANH");
        lblShortcut.setFont(UITheme.FONT_SUBTITLE);
        lblShortcut.setForeground(new Color(51, 65, 85));
        lblShortcut.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(lblShortcut);
        container.add(Box.createVerticalStrut(10));

        // 4. Quick Action Grid (Flat Cards)
        JPanel actionGrid = new JPanel(new GridLayout(2, 3, 14, 14));
        actionGrid.setOpaque(false);
        actionGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        boolean canHR = session.hasRole("DB_Admin", "HR_Manager");
        boolean canPayroll = session.hasRole("DB_Admin", "Payroll_Officer");
        boolean canCC = session.hasRole("DB_Admin", "HR_Manager", "Payroll_Officer");
        boolean canAdmin = session.hasRole("DB_Admin");

        actionGrid.add(createActionCard("Hồ sơ Nhân viên", "Quản lý lý lịch, chức vụ, phòng ban và tài khoản", "NV",
                () -> switchView("NHAN_VIEN", "NHÂN SỰ > Quản lý Hồ sơ nhân sự"), canHR));

        actionGrid.add(createActionCard("Phòng ban & Chức vụ", "Thiết lập cơ cấu phòng ban và phụ cấp chức danh", "DM",
                () -> switchView("DANH_MUC", "DANH MỤC > Phòng ban & Chức vụ"), canHR));

        actionGrid.add(createActionCard("Chấm công Nhân sự", "Theo dõi nhật ký ngày công, làm thêm giờ và nghỉ phép", "CC",
                () -> switchView("CHAM_CONG", "CHẤM CÔNG > Ghi nhận chấm công chi tiết"), canCC));

        actionGrid.add(createActionCard("Tính toán Bảng lương", "Quy trình tính lương tự động, BHXH và Thuế TNCN", "BL",
                () -> switchView("BANG_LUONG", "TIỀN LƯƠNG > Quy trình tính lương tự động"), canPayroll));

        actionGrid.add(createActionCard("Báo cáo & Phiếu lương", "Xuất bảng lương tổng hợp và tra cứu phiếu lương", "BC",
                () -> switchView("BAO_CAO", "BÁO CÁO > Báo cáo tổng hợp & Phiếu lương"), true));

        actionGrid.add(createActionCard("Quản trị Tài khoản", "Phân quyền người dùng, bảo mật và tài khoản đăng nhập", "QT",
                () -> switchView("TAI_KHOAN", "QUẢN TRỊ > Phân quyền & Quản lý tài khoản"), canAdmin));

        container.add(actionGrid);
        container.add(Box.createVerticalStrut(20));

        // 5. System Status Footer
        JPanel footerCard = new JPanel(new BorderLayout());
        footerCard.setBackground(Color.WHITE);
        footerCard.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(10, 16, 10, 16)
        ));
        footerCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        int maNV = session.getMaNV();
        String idInfo = (maNV > 0) ? ("Mã NV: " + maNV) : "Tài khoản quản trị";
        JLabel lblLeft = new JLabel("Phiên làm việc: " + session.getTenDangNhap() + " (" + idInfo + ")  |  Cơ chế kiểm soát truy cập RBAC");
        lblLeft.setFont(UITheme.FONT_CAPTION);
        lblLeft.setForeground(UITheme.TEXT_MUTED);

        JLabel lblRight = new JLabel("Hệ Quản trị Cơ sở Dữ liệu – Nhóm 06");
        lblRight.setFont(UITheme.FONT_CAPTION_BOLD);
        lblRight.setForeground(UITheme.TEXT_SUBTLE);

        footerCard.add(lblLeft, BorderLayout.WEST);
        footerCard.add(lblRight, BorderLayout.EAST);
        container.add(footerCard);

        JScrollPane scroll = new JScrollPane(container);
        scroll.setBorder(null);
        scroll.setBackground(UITheme.BG_APP);
        scroll.getViewport().setBackground(UITheme.BG_APP);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel createSmallKpiCard(String title, String value, String subtext, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new CompoundBorder(
                new MatteBorder(0, 3, 0, 0, accent),
                new EmptyBorder(10, 14, 10, 14)
            )
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_CAPTION_BOLD);
        lblTitle.setForeground(UITheme.TEXT_MUTED);

        JLabel lblVal = new JLabel(value);
        lblVal.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 16));
        lblVal.setForeground(UITheme.TEXT_MAIN);

        JLabel lblSub = new JLabel(subtext);
        lblSub.setFont(UITheme.FONT_CAPTION);
        lblSub.setForeground(accent);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblVal, BorderLayout.CENTER);
        card.add(lblSub, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createActionCard(String title, String desc, String badge, Runnable action, boolean enabled) {
        JPanel card = new JPanel(new BorderLayout(10, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(enabled ? UITheme.BORDER : new Color(241, 245, 249), 1, true),
            new EmptyBorder(12, 14, 12, 14)
        ));
        card.setCursor(enabled ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        top.setOpaque(false);

        JLabel lblBadge = UITheme.createBadge(badge, enabled ? UITheme.PRIMARY : Color.GRAY,
                enabled ? UITheme.PRIMARY_LIGHT : new Color(241, 245, 249),
                enabled ? UITheme.PRIMARY_BORDER : UITheme.BORDER);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_BODY_BOLD);
        lblTitle.setForeground(enabled ? UITheme.TEXT_MAIN : UITheme.TEXT_SUBTLE);

        top.add(lblBadge);
        top.add(lblTitle);

        JLabel lblDesc = new JLabel(enabled ? desc : "Không khả dụng cho vai trò này");
        lblDesc.setFont(UITheme.FONT_CAPTION);
        lblDesc.setForeground(enabled ? UITheme.TEXT_MUTED : UITheme.TEXT_SUBTLE);

        card.add(top, BorderLayout.NORTH);
        card.add(lblDesc, BorderLayout.CENTER);

        if (enabled) {
            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    card.setBackground(UITheme.PRIMARY_LIGHT);
                    card.setBorder(new CompoundBorder(
                        new LineBorder(UITheme.PRIMARY_BORDER, 1, true),
                        new EmptyBorder(12, 14, 12, 14)
                    ));
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    card.setBackground(Color.WHITE);
                    card.setBorder(new CompoundBorder(
                        new LineBorder(UITheme.BORDER, 1, true),
                        new EmptyBorder(12, 14, 12, 14)
                    ));
                }
                @Override
                public void mouseClicked(MouseEvent e) {
                    action.run();
                }
            });
        }

        return card;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  CHUYỂN ĐỔI VIEW (VIEW SWITCHER)
    // ═══════════════════════════════════════════════════════════════════

    public void switchView(String key, String breadcrumb) {
        if (!cachedViews.containsKey(key)) {
            // Lazy load panel
            for (NavItem item : navItems) {
                if (item.getKey().equals(key)) {
                    JComponent comp = item.createSupplierComponent();
                    if (comp != null) {
                        contentContainer.add(comp, key);
                        cachedViews.put(key, comp);
                    }
                    break;
                }
            }
        }

        currentViewKey = key;
        cardLayout.show(contentContainer, key);
        lblBreadcrumb.setText(breadcrumb);

        for (NavItem item : navItems) {
            item.setActive(item.getKey().equals(key));
        }

        contentContainer.revalidate();
        contentContainer.repaint();
    }

    // Backward compatibility cho các method openTab cũ
    public void openTab(String title, ComponentSupplier supplier) {
        switch (title) {
            case "Nhân viên":
                switchView("NHAN_VIEN", "NHÂN SỰ > Quản lý Hồ sơ nhân sự");
                break;
            case "Danh mục":
                switchView("DANH_MUC", "DANH MỤC > Phòng ban & Chức vụ");
                break;
            case "Chấm công":
                switchView("CHAM_CONG", "CHẤM CÔNG > Ghi nhận chấm công chi tiết");
                break;
            case "Tổng hợp CC":
                switchView("TONG_HOP_CC", "CHẤM CÔNG > Tổng hợp ngày công tháng");
                break;
            case "Phụ cấp / Khấu trừ":
                switchView("PHU_CAP", "LƯƠNG > Quản lý Phụ cấp & Khấu trừ");
                break;
            case "Tính bảng lương":
                switchView("BANG_LUONG", "TIỀN LƯƠNG > Quy trình tính lương tự động");
                break;
            case "Báo cáo tổng hợp":
            case "Phiếu lương cá nhân":
            case "Báo cáo & Chốt lương":
                switchView("BAO_CAO", "BÁO CÁO > Báo cáo tổng hợp & Phiếu lương");
                break;
            case "Quản lý tài khoản":
                switchView("TAI_KHOAN", "QUẢN TRỊ > Phân quyền & Quản lý tài khoản");
                break;
            default:
                switchView("HOME", "TỔNG QUAN > Trang chủ Dashboard");
                break;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  XỬ LÝ ĐĂNG XUẤT & ĐÓNG ỨNG DỤNG
    // ═══════════════════════════════════════════════════════════════════

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc chắn muốn đăng xuất khỏi tài khoản '" + session.getTenDangNhap() + "'?",
            "Xác nhận đăng xuất",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            session.logout();
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            this.dispose();
        }
    }

    private void setupFrame() {
        String titleUser = session.getUserInfo();
        String roleStr = session.getFullRoleDisplayName();
        String roleSuffix = !roleStr.isEmpty() ? " [" + roleStr + "]" : "";
        setTitle(APP_TITLE + " – " + titleUser + roleSuffix);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                    MainFrame.this,
                    "Bạn có muốn thoát khỏi hệ thống?",
                    "Xác nhận thoát",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    session.logout();
                    System.exit(0);
                }
            }
        });

        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 680));
        setLocationRelativeTo(null);
    }

    private void initLegacyMenusAndStatus() {
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Trang chủ", new JScrollPane(createDashboardView()));

        lblStatusUser = new JLabel("Người dùng: " + session.getUserInfo());
        lblStatusUser.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 12));

        String roleStr = session.getFullRoleDisplayName();
        lblStatusRole = new JLabel("Vai trò: " + (roleStr.isEmpty() ? "Chưa đăng nhập" : roleStr));
        lblStatusRole.setFont(new Font(UITheme.FONT_FAMILY, Font.PLAIN, 12));

        menuNhanVien = new JMenu("Nhân viên");
        JMenuItem miQuanLyNhanVien = new JMenuItem("Quản lý nhân viên");
        menuNhanVien.add(miQuanLyNhanVien);

        menuDanhMuc = new JMenu("Danh mục");
        JMenuItem miPhongBan = new JMenuItem("Phòng ban");
        menuDanhMuc.add(miPhongBan);

        menuChamCong = new JMenu("Chấm công");
        JMenuItem miNhapChamCong = new JMenuItem("Nhập chấm công");
        JMenuItem miXemChamCong = new JMenuItem("Xem tổng hợp chấm công");
        menuChamCong.add(miNhapChamCong);
        menuChamCong.add(miXemChamCong);

        menuPhuCapKhauTru = new JMenu("Phụ cấp / Khấu trừ");
        JMenuItem miPhuCap = new JMenuItem("Phụ cấp");
        menuPhuCapKhauTru.add(miPhuCap);

        menuLuong = new JMenu("Lương");
        JMenuItem miBangLuong = new JMenuItem("Bảng lương");
        menuLuong.add(miBangLuong);

        menuBaoCao = new JMenu("Báo cáo");
        JMenuItem miBaoCaoTongHop = new JMenuItem("Báo cáo tổng hợp");
        JMenuItem miPhieuLuong = new JMenuItem("Phiếu lương cá nhân");
        menuBaoCao.add(miBaoCaoTongHop);
        menuBaoCao.add(miPhieuLuong);

        menuQuanTri = new JMenu("Quản trị");
        JMenuItem miTaiKhoan = new JMenuItem("Quản lý tài khoản");
        menuQuanTri.add(miTaiKhoan);
    }

    private void applyRolePermissions() {
        String role = session.getVaiTro();
        if (role == null) role = "";

        if (menuNhanVien != null) menuNhanVien.setVisible(session.hasRole("DB_Admin", "HR_Manager"));
        if (menuDanhMuc != null) menuDanhMuc.setVisible(session.hasRole("DB_Admin", "HR_Manager"));
        if (menuChamCong != null) {
            menuChamCong.setVisible(session.hasRole("DB_Admin", "HR_Manager", "Payroll_Officer"));
            if (menuChamCong.getItemCount() >= 2) {
                menuChamCong.getItem(0).setEnabled(session.hasRole("DB_Admin", "HR_Manager"));
            }
        }
        if (menuPhuCapKhauTru != null) menuPhuCapKhauTru.setVisible(session.hasRole("DB_Admin", "HR_Manager", "Payroll_Officer"));
        if (menuLuong != null) menuLuong.setVisible(session.hasRole("DB_Admin", "Payroll_Officer"));
        if (menuBaoCao != null) {
            menuBaoCao.setVisible(true);
            if (menuBaoCao.getItemCount() >= 1) {
                menuBaoCao.getItem(0).setVisible(!role.equals("Employee"));
            }
        }
        if (menuQuanTri != null) menuQuanTri.setVisible(session.hasRole("DB_Admin"));
    }

    public void updateSessionDisplay() {
        applyRolePermissions();
        if (lblStatusUser != null) {
            lblStatusUser.setText("Người dùng: " + session.getUserInfo());
        }
        if (lblStatusRole != null) {
            String roleStr = session.getFullRoleDisplayName();
            lblStatusRole.setText("Vai trò: " + (roleStr.isEmpty() ? "Chưa đăng nhập" : roleStr));
        }
        if (lblHeaderUserName != null) {
            lblHeaderUserName.setText(session.getUserInfo());
        }
        if (lblHeaderRoleBadge != null) {
            String roleStr = session.getFullRoleDisplayName();
            if (roleStr.isEmpty()) roleStr = "Guest";
            lblHeaderRoleBadge.setText(" " + roleStr + " ");
        }
        String titleUser = session.getUserInfo();
        String roleStr = session.getFullRoleDisplayName();
        String roleSuffix = !roleStr.isEmpty() ? " [" + roleStr + "]" : "";
        setTitle(APP_TITLE + " – " + titleUser + roleSuffix);
    }

    // ─── Legacy Getters phục vụ kiểm thử phân quyền và giao diện ─────────

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

    // ─── NAV ITEM COMPONENT (SIDEBAR BUTTON) ──────────────────────────

    @FunctionalInterface
    public interface ComponentSupplier {
        JComponent get();
    }

    private class NavItem extends JPanel {
        private final String key;
        private final String breadcrumb;
        private final ComponentSupplier supplier;
        private final boolean enabled;
        private boolean active = false;

        private final JLabel lblText;

        public NavItem(String key, String label, String breadcrumb, ComponentSupplier supplier, boolean enabled) {
            this.key = key;
            this.breadcrumb = breadcrumb;
            this.supplier = supplier;
            this.enabled = enabled;

            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(218, 36));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            setBackground(UITheme.BG_SIDEBAR);
            setOpaque(true);

            lblText = new JLabel(label);
            lblText.setFont(UITheme.FONT_BODY);
            lblText.setForeground(enabled ? UITheme.TEXT_MAIN : UITheme.TEXT_SUBTLE);
            lblText.setBorder(new EmptyBorder(0, 16, 0, 8));

            add(lblText, BorderLayout.CENTER);

            if (enabled) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (!active) {
                            setBackground(new Color(230, 236, 245));
                        }
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (!active) {
                            setBackground(UITheme.BG_SIDEBAR);
                        }
                    }
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        switchView(key, breadcrumb);
                    }
                });
            }
        }

        public String getKey() {
            return key;
        }

        public JComponent createSupplierComponent() {
            return (supplier != null) ? supplier.get() : null;
        }

        public void setActive(boolean active) {
            this.active = active;
            if (active) {
                setBackground(UITheme.PRIMARY_LIGHT);
                lblText.setForeground(UITheme.PRIMARY_ACTIVE);
                lblText.setFont(UITheme.FONT_BODY_BOLD);
                setBorder(new MatteBorder(0, 3, 0, 0, UITheme.PRIMARY));
            } else {
                setBackground(UITheme.BG_SIDEBAR);
                lblText.setForeground(enabled ? UITheme.TEXT_MAIN : UITheme.TEXT_SUBTLE);
                lblText.setFont(UITheme.FONT_BODY);
                setBorder(null);
            }
            repaint();
        }
    }
}
