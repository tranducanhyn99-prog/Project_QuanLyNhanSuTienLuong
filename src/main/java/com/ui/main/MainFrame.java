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
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Light application workspace with role-aware navigation. */
public class MainFrame extends JFrame {
    private static final String APP_TITLE = "PeopleOS · Quản lý Nhân sự & Tiền lương";
    private final Session session = Session.getInstance();
    private JPanel sidebarPanel;
    private JPanel headerPanel;
    private JPanel contentContainer;
    private CardLayout cardLayout;
    private JLabel lblBreadcrumb;
    private JLabel lblHeaderUserName;
    private JLabel lblHeaderRoleBadge;
    private String currentViewKey = "HOME";
    private final List<NavItem> navItems = new ArrayList<>();
    private final Map<String, JComponent> cachedViews = new HashMap<>();

    // Retained for integrations and existing role permission tests.
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
        session.requireRoles("DB_Admin", "HR_Manager", "Payroll_Officer", "Employee");
        UITheme.setupGlobalUI();
        initLegacyMenusAndStatus();
        initComponents();
        applyRolePermissions();
        setupFrame();
        switchView("HOME", "Không gian làm việc  /  Tổng quan");
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        sidebarPanel = createSidebar();
        add(sidebarPanel, BorderLayout.WEST);
        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setBackground(UITheme.BG_APP);
        headerPanel = createHeader();
        workspace.add(headerPanel, BorderLayout.NORTH);
        cardLayout = new CardLayout();
        contentContainer = new JPanel(cardLayout);
        contentContainer.setBackground(UITheme.BG_APP);
        JComponent home = createDashboardView();
        cachedViews.put("HOME", home);
        contentContainer.add(home, "HOME");
        workspace.add(contentContainer, BorderLayout.CENTER);
        add(workspace, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(246, 0));
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, UITheme.BORDER));
        JPanel brand = new JPanel(new BorderLayout(12, 0));
        brand.setOpaque(false);
        brand.setBorder(new EmptyBorder(28, 23, 25, 20));
        JLabel mark = new JLabel(UITheme.icon("users", 26, Color.WHITE), SwingConstants.CENTER);
        mark.setOpaque(true);
        mark.setBackground(UITheme.PRIMARY);
        mark.setPreferredSize(new Dimension(44, 44));
        JPanel brandText = column();
        brandText.add(label("PeopleOS", 23, Font.BOLD, UITheme.TEXT_MAIN));
        brandText.add(label("NHÂN SỰ & TIỀN LƯƠNG", 10, Font.BOLD, UITheme.TEXT_MUTED));
        brand.add(mark, BorderLayout.WEST);
        brand.add(brandText, BorderLayout.CENTER);
        sidebar.add(brand, BorderLayout.NORTH);

        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(Color.WHITE);
        menu.setBorder(new EmptyBorder(0, 12, 16, 12));
        boolean hr = session.hasRole("DB_Admin", "HR_Manager");
        boolean payroll = session.hasRole("DB_Admin", "Payroll_Officer");
        boolean attendance = session.hasRole("DB_Admin", "HR_Manager", "Payroll_Officer");
        boolean admin = session.hasRole("DB_Admin");
        addNavSectionHeader(menu, "KHÔNG GIAN LÀM VIỆC");
        addNavItem(menu, "HOME", "Tổng quan", "Không gian làm việc  /  Tổng quan", () -> null, true);
        if (hr) {
            addNavSectionHeader(menu, "QUẢN LÝ NHÂN SỰ");
            addNavItem(menu, "NHAN_VIEN", "Hồ sơ nhân viên", "Nhân sự  /  Hồ sơ nhân viên", NhanVienPanel::new, true);
            addNavItem(menu, "DANH_MUC", "Phòng ban & Chức vụ", "Nhân sự  /  Phòng ban & Chức vụ", DanhMucPanel::new, true);
        }
        if (attendance || payroll) {
            addNavSectionHeader(menu, "CHẤM CÔNG & TIỀN LƯƠNG");
            if (attendance) {
                addNavItem(menu, "CHAM_CONG", "Nhật ký chấm công", "Chấm công  /  Nhật ký chấm công", () -> new ChamCongPanel(false), true);
                addNavItem(menu, "TONG_HOP_CC", "Tổng hợp công tháng", "Chấm công  /  Tổng hợp công tháng", () -> new ChamCongPanel(true), true);
                addNavItem(menu, "PHU_CAP", "Phụ cấp & Khấu trừ", "Tiền lương  /  Phụ cấp & Khấu trừ", PhuCapKhauTruPanel::new, true);
            }
            if (payroll) addNavItem(menu, "BANG_LUONG", "Bảng lương", "Tiền lương  /  Bảng lương", BangLuongPanel::new, true);
        }
        addNavSectionHeader(menu, "BÁO CÁO & HỆ THỐNG");
        addNavItem(menu, "BAO_CAO", "Báo cáo & Phiếu lương", "Báo cáo  /  Báo cáo & Phiếu lương", BaoCaoPanel::new, true);
        if (admin) addNavItem(menu, "TAI_KHOAN", "Quản lý tài khoản", "Hệ thống  /  Quản lý tài khoản", TaiKhoanPanel::new, true);
        JScrollPane scroll = new JScrollPane(menu);
        UITheme.styleScrollPane(scroll);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        sidebar.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(0, 14));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(16, 18, 22, 18));
        RoundedPanel help = new RoundedPanel(UITheme.PRIMARY_LIGHT, UITheme.PRIMARY_BORDER, 16);
        help.setLayout(new BorderLayout(0, 5));
        help.setBorder(new EmptyBorder(14, 14, 14, 14));
        help.add(label("Mọi việc, trong tầm tay", 12, Font.BOLD, UITheme.PRIMARY_ACTIVE), BorderLayout.NORTH);
        help.add(label("Chọn phân hệ để bắt đầu ngày mới.", 10, Font.PLAIN, UITheme.TEXT_MUTED), BorderLayout.CENTER);
        footer.add(help, BorderLayout.CENTER);
        JButton logout = new JButton("Đăng xuất");
        UITheme.styleSecondaryButton(logout);
        logout.setIcon(UITheme.icon("logout", 16, UITheme.TEXT_MUTED));
        logout.addActionListener(e -> handleLogout());
        footer.add(logout, BorderLayout.SOUTH);
        sidebar.add(footer, BorderLayout.SOUTH);
        return sidebar;
    }

    private void addNavSectionHeader(JPanel container, String title) {
        JLabel heading = label(title, 10, Font.BOLD, UITheme.TEXT_MUTED);
        heading.setBorder(new EmptyBorder(18, 13, 9, 0));
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(heading);
    }

    private void addNavItem(JPanel container, String key, String text, String breadcrumb,
                            ComponentSupplier supplier, boolean enabled) {
        NavItem item = new NavItem(key, text, breadcrumb, supplier, enabled);
        navItems.add(item);
        container.add(item);
        container.add(Box.createVerticalStrut(4));
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 78));
        header.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, UITheme.BORDER), new EmptyBorder(14, 28, 14, 28)));
        JPanel title = column();
        title.add(label("PEOPLEOS / WORKSPACE", 10, Font.BOLD, UITheme.TEXT_MUTED));
        title.add(Box.createVerticalStrut(4));
        lblBreadcrumb = label("Không gian làm việc  /  Tổng quan", 14, Font.BOLD, UITheme.TEXT_MAIN);
        title.add(lblBreadcrumb);
        header.add(title, BorderLayout.CENTER);

        JPanel user = new JPanel(new BorderLayout(10, 0));
        user.setOpaque(false);
        String name = session.getDisplayName();
        String initial = name == null || name.isEmpty() ? "U" : name.substring(0, 1).toUpperCase(Locale.ROOT);
        JLabel avatar = new JLabel(initial, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY_LIGHT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 15));
        avatar.setForeground(UITheme.PRIMARY);
        avatar.setPreferredSize(new Dimension(42, 42));
        user.add(avatar, BorderLayout.WEST);
        JPanel identity = column();
        lblHeaderUserName = label(name, 13, Font.BOLD, UITheme.TEXT_MAIN);
        lblHeaderRoleBadge = label(session.getVaiTroDisplayName(), 11, Font.PLAIN, UITheme.TEXT_MUTED);
        identity.add(lblHeaderUserName);
        identity.add(Box.createVerticalStrut(3));
        identity.add(lblHeaderRoleBadge);
        user.add(identity, BorderLayout.CENTER);
        user.setPreferredSize(new Dimension(245, 42));
        header.add(user, BorderLayout.EAST);
        return header;
    }

    private JComponent createDashboardView() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        DashboardPanel dashboard = new DashboardPanel();
        dashboard.setOpaque(true);
        dashboard.setBackground(UITheme.BG_APP);
        dashboard.setBorder(new EmptyBorder(28, 28, 28, 28));
        JPanel pageTitle = new JPanel(new BorderLayout());
        pageTitle.setOpaque(false);
        pageTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        pageTitle.setMinimumSize(new Dimension(0, 66));
        JPanel pageText = column();
        pageText.add(label("Tổng quan", 26, Font.BOLD, UITheme.TEXT_MAIN));
        pageText.add(Box.createVerticalStrut(5));
        pageText.add(label("Một không gian gọn gàng cho đội ngũ của bạn.", 13, Font.PLAIN, UITheme.TEXT_MUTED));
        pageTitle.add(pageText, BorderLayout.CENTER);
        JPanel dateWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        dateWrap.setOpaque(false);
        dateWrap.add(UITheme.createBadge(today.format(DateTimeFormatter.ofPattern("dd / MM / yyyy")),
                UITheme.TEXT_MUTED, Color.WHITE, UITheme.BORDER));
        pageTitle.add(dateWrap, BorderLayout.EAST);
        pageTitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));
        dashboard.add(pageTitle);
        dashboard.add(Box.createVerticalStrut(22));

        RoundedPanel welcome = new RoundedPanel(new Color(238, 242, 255), new Color(216, 223, 253), 20);
        welcome.setLayout(new BorderLayout(18, 0));
        welcome.setBorder(new EmptyBorder(24, 25, 24, 22));
        welcome.setMaximumSize(new Dimension(Integer.MAX_VALUE, 164));
        welcome.setMinimumSize(new Dimension(0, 164));
        welcome.setPreferredSize(new Dimension(900, 164));
        JPanel welcomeText = column();
        welcomeText.add(label("CHÀO MỪNG TRỞ LẠI", 10, Font.BOLD, UITheme.PRIMARY));
        welcomeText.add(Box.createVerticalStrut(8));
        welcomeText.add(label("Xin chào, " + session.getDisplayName() + "!", 24, Font.BOLD, UITheme.TEXT_MAIN));
        welcomeText.add(Box.createVerticalStrut(8));
        welcomeText.add(label("Sẵn sàng cho một ngày làm việc hiệu quả?", 13, Font.PLAIN, UITheme.TEXT_MUTED));
        welcomeText.add(Box.createVerticalStrut(4));
        welcomeText.add(label("Các công cụ bạn cần đều ở ngay bên dưới.", 13, Font.PLAIN, UITheme.TEXT_MUTED));
        welcome.add(welcomeText, BorderLayout.CENTER);
        welcome.add(new WorkspaceArt(), BorderLayout.EAST);
        dashboard.add(welcome);
        dashboard.add(Box.createVerticalStrut(20));

        JPanel metrics = new JPanel(new GridLayout(1, 3, 16, 0));
        metrics.setOpaque(false);
        metrics.setAlignmentX(Component.LEFT_ALIGNMENT);
        metrics.setMinimumSize(new Dimension(0, 118));
        metrics.setPreferredSize(new Dimension(900, 118));
        metrics.setMaximumSize(new Dimension(Integer.MAX_VALUE, 118));
        metrics.add(createSmallKpiCard("Phân hệ khả dụng", String.valueOf(navItems.size() - 1),
                "Theo vai trò của bạn", "building", UITheme.PRIMARY));
        metrics.add(createSmallKpiCard("Tháng hiện tại", today.format(DateTimeFormatter.ofPattern("MM / yyyy")),
                "Lịch làm việc hiện tại", "calendar", UITheme.INFO_TEXT));
        metrics.add(createSmallKpiCard("Tài khoản của bạn", session.getMaNV() > 0 ? "NV #" + session.getMaNV() : session.getVaiTroDisplayName(),
                session.getTenDangNhap(), "users", UITheme.SUCCESS_TEXT));
        dashboard.add(metrics);
        dashboard.add(Box.createVerticalStrut(26));

        JPanel shortcutHeading = new JPanel(new BorderLayout());
        shortcutHeading.setOpaque(false);
        shortcutHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        shortcutHeading.setMinimumSize(new Dimension(0, 30));
        shortcutHeading.add(label("Bắt đầu công việc", 18, Font.BOLD, UITheme.TEXT_MAIN), BorderLayout.WEST);
        shortcutHeading.add(label("Các lối tắt dành cho bạn", 12, Font.PLAIN, UITheme.TEXT_MUTED), BorderLayout.EAST);
        shortcutHeading.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        dashboard.add(shortcutHeading);
        dashboard.add(Box.createVerticalStrut(12));
        List<JPanel> actions = new ArrayList<>();
        if (session.hasRole("DB_Admin", "HR_Manager")) {
            actions.add(createActionCard("Hồ sơ nhân viên", "Thông tin, phòng ban và chức vụ của đội ngũ.", "users",
                    () -> switchView("NHAN_VIEN", "Nhân sự  /  Hồ sơ nhân viên"), true));
            actions.add(createActionCard("Cơ cấu tổ chức", "Quản lý phòng ban và danh mục chức vụ.", "building",
                    () -> switchView("DANH_MUC", "Nhân sự  /  Phòng ban & Chức vụ"), true));
        }
        if (session.hasRole("DB_Admin", "HR_Manager", "Payroll_Officer")) {
            actions.add(createActionCard("Theo dõi chấm công", "Ngày công, nghỉ phép và thời gian làm thêm.", "clock",
                    () -> switchView("CHAM_CONG", "Chấm công  /  Nhật ký chấm công"), true));
        }
        if (session.hasRole("DB_Admin", "Payroll_Officer")) {
            actions.add(createActionCard("Quản lý bảng lương", "Tính lương và kiểm tra kết quả từng kỳ.", "wallet",
                    () -> switchView("BANG_LUONG", "Tiền lương  /  Bảng lương"), true));
        }
        actions.add(createActionCard("Báo cáo & Phiếu lương", "Tra cứu kết quả và thông tin tiền lương.", "chart",
                () -> switchView("BAO_CAO", "Báo cáo  /  Báo cáo & Phiếu lương"), true));
        if (session.hasRole("DB_Admin")) {
            actions.add(createActionCard("Quản lý tài khoản", "Cập nhật người dùng và vai trò làm việc.", "settings",
                    () -> switchView("TAI_KHOAN", "Hệ thống  /  Quản lý tài khoản"), true));
        }
        int columns = actions.size() == 1 ? 1 : actions.size() < 4 ? 2 : 3;
        int rows = (actions.size() + columns - 1) / columns;
        JPanel actionGrid = new JPanel(new GridLayout(rows, columns, 16, 16));
        actionGrid.setOpaque(false);
        actionGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionGrid.setMinimumSize(new Dimension(0, 158));
        for (JPanel action : actions) actionGrid.add(action);
        actionGrid.setPreferredSize(new Dimension(900, rows * 158 + (rows - 1) * 16));
        actionGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, rows * 158 + (rows - 1) * 16));
        dashboard.add(actionGrid);
        dashboard.actionGrid = actionGrid;
        dashboard.actionCount = actions.size();
        dashboard.add(Box.createVerticalStrut(24));

        RoundedPanel note = new RoundedPanel(Color.WHITE, UITheme.BORDER, 14);
        note.setLayout(new BorderLayout(12, 0));
        note.setBorder(new EmptyBorder(16, 18, 16, 18));
        note.add(new JLabel(UITheme.icon("check", 22, UITheme.SUCCESS_TEXT)), BorderLayout.WEST);
        JPanel noteText = column();
        noteText.add(label("Không gian làm việc của bạn đã sẵn sàng", 12, Font.BOLD, UITheme.TEXT_MAIN));
        noteText.add(Box.createVerticalStrut(3));
        noteText.add(label("Bạn đang làm việc với vai trò " + session.getVaiTroDisplayName() + ".", 12, Font.PLAIN, UITheme.TEXT_MUTED));
        note.add(noteText, BorderLayout.CENTER);
        note.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        note.setMinimumSize(new Dimension(0, 76));
        dashboard.add(note);
        dashboard.add(Box.createVerticalGlue());
        JScrollPane scroll = new JScrollPane(dashboard);
        UITheme.styleScrollPane(scroll);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UITheme.BG_APP);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    private JPanel createSmallKpiCard(String title, String value, String subtext, String icon, Color accent) {
        RoundedPanel card = new RoundedPanel(Color.WHITE, UITheme.BORDER, 16);
        card.setLayout(new BorderLayout(10, 6));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        JPanel text = column();
        text.add(label(title, 12, Font.PLAIN, UITheme.TEXT_MUTED));
        text.add(Box.createVerticalStrut(5));
        text.add(label(value, value.length() > 12 ? 20 : 27, Font.BOLD, UITheme.TEXT_MAIN));
        text.add(Box.createVerticalStrut(3));
        text.add(label(subtext, 11, Font.PLAIN, UITheme.TEXT_MUTED));
        card.add(text, BorderLayout.CENTER);
        JPanel iconWrap = new JPanel(new BorderLayout());
        iconWrap.setOpaque(false);
        iconWrap.add(new JLabel(UITheme.icon(icon, 24, accent)), BorderLayout.NORTH);
        card.add(iconWrap, BorderLayout.EAST);
        return card;
    }

    private JPanel createActionCard(String title, String description, String icon, Runnable action, boolean enabled) {
        RoundedPanel card = new RoundedPanel(Color.WHITE, UITheme.BORDER, 16);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(17, 18, 15, 18));
        JPanel top = new JPanel(new BorderLayout(10, 0));
        top.setOpaque(false);
        top.add(new JLabel(UITheme.icon(icon, 23, UITheme.PRIMARY)), BorderLayout.WEST);
        top.add(label(title, 13, Font.BOLD, UITheme.TEXT_MAIN), BorderLayout.CENTER);
        card.add(top, BorderLayout.NORTH);
        JLabel desc = label("<html>" + description + "</html>", 12, Font.PLAIN, UITheme.TEXT_MUTED);
        desc.setVerticalAlignment(SwingConstants.TOP);
        card.add(desc, BorderLayout.CENTER);
        JButton open = new JButton("Mở phân hệ", UITheme.icon("arrow", 15, UITheme.PRIMARY));
        open.setHorizontalTextPosition(SwingConstants.LEFT);
        open.setHorizontalAlignment(SwingConstants.LEFT);
        open.setForeground(UITheme.PRIMARY);
        open.setFont(UITheme.FONT_BODY_BOLD);
        open.setContentAreaFilled(false);
        open.setBorderPainted(false);
        open.setBorder(new EmptyBorder(2, 0, 0, 0));
        open.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        open.setEnabled(enabled);
        open.getAccessibleContext().setAccessibleName("Mở " + title);
        open.addActionListener(e -> action.run());
        card.add(open, BorderLayout.SOUTH);
        if (enabled) {
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    card.setBackground(new Color(248, 249, 255));
                    card.outline = UITheme.PRIMARY_BORDER;
                    card.repaint();
                }
                @Override public void mouseExited(MouseEvent e) {
                    card.setBackground(Color.WHITE);
                    card.outline = UITheme.BORDER;
                    card.repaint();
                }
                @Override public void mouseClicked(MouseEvent e) { action.run(); }
            });
        }
        return card;
    }

    private static JPanel column() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private static JLabel label(String text, int size, int style, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(UITheme.FONT_FAMILY, style, size));
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

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
        lblBreadcrumb.setText(breadcrumb.replace(" > ", "  /  "));

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

        setSize(1360, 880);
        setMinimumSize(new Dimension(1024, 680));
        setLocationRelativeTo(null);
    }

    private void initLegacyMenusAndStatus() {
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Trang chủ", new JScrollPane(new JPanel()));

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
            lblHeaderUserName.setText(session.getDisplayName());
        }
        if (lblHeaderRoleBadge != null) {
            String roleStr = session.getFullRoleDisplayName();
            if (roleStr.isEmpty()) roleStr = "Guest";
            lblHeaderRoleBadge.setText(session.getVaiTroDisplayName());
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

    @FunctionalInterface
    public interface ComponentSupplier { JComponent get(); }

    private class NavItem extends RoundedPanel {
        private final String key;
        private final String breadcrumb;
        private final ComponentSupplier supplier;
        private final boolean enabled;
        private boolean active;
        private final JLabel lblText;
        private final JLabel iconLabel;
        private final JLabel activeDot;

        NavItem(String key, String text, String breadcrumb, ComponentSupplier supplier, boolean enabled) {
            super(Color.WHITE, null, 12);
            this.key = key;
            this.breadcrumb = breadcrumb;
            this.supplier = supplier;
            this.enabled = enabled;
            setLayout(new BorderLayout(11, 0));
            setBorder(new EmptyBorder(0, 13, 0, 12));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setPreferredSize(new Dimension(220, 43));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
            iconLabel = new JLabel(UITheme.icon(navIcon(key), 18, UITheme.TEXT_MUTED));
            lblText = label(text, 12, Font.PLAIN, UITheme.TEXT_MUTED);
            activeDot = new JLabel(UITheme.icon("arrow", 13, UITheme.PRIMARY));
            activeDot.setVisible(false);
            add(iconLabel, BorderLayout.WEST);
            add(lblText, BorderLayout.CENTER);
            add(activeDot, BorderLayout.EAST);
            setFocusable(enabled);
            getAccessibleContext().setAccessibleName(text);
            if (enabled) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { if (!active) setBackground(UITheme.BG_APP); }
                    @Override public void mouseExited(MouseEvent e) { if (!active) setBackground(Color.WHITE); }
                    @Override public void mouseClicked(MouseEvent e) { switchView(key, breadcrumb); }
                });
                getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "open");
                getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "open");
                getActionMap().put("open", new AbstractAction() {
                    @Override public void actionPerformed(ActionEvent e) { switchView(key, breadcrumb); }
                });
                addFocusListener(new FocusAdapter() {
                    @Override public void focusGained(FocusEvent e) { outline = UITheme.PRIMARY_BORDER; repaint(); }
                    @Override public void focusLost(FocusEvent e) { outline = null; repaint(); }
                });
            }
        }

        String getKey() { return key; }
        JComponent createSupplierComponent() { return supplier == null ? null : supplier.get(); }
        void setActive(boolean selected) {
            active = selected;
            setBackground(selected ? UITheme.PRIMARY_LIGHT : Color.WHITE);
            Color color = selected ? UITheme.PRIMARY_ACTIVE : UITheme.TEXT_MUTED;
            lblText.setForeground(color);
            lblText.setFont(new Font(UITheme.FONT_FAMILY, selected ? Font.BOLD : Font.PLAIN, 12));
            iconLabel.setIcon(UITheme.icon(navIcon(key), 18, color));
            activeDot.setVisible(selected);
            repaint();
        }
    }

    private static String navIcon(String key) {
        switch (key) {
            case "HOME": return "home";
            case "NHAN_VIEN": return "users";
            case "DANH_MUC": return "building";
            case "CHAM_CONG": return "clock";
            case "TONG_HOP_CC": return "calendar";
            case "PHU_CAP":
            case "BANG_LUONG": return "wallet";
            case "BAO_CAO": return "chart";
            default: return "settings";
        }
    }

    private static class DashboardPanel extends JPanel implements Scrollable {
        private JPanel actionGrid;
        private int actionCount;
        private int actionColumns;

        DashboardPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        }

        private void sizeActions() {
            if (actionGrid != null) {
                int width = getParent() == null ? getWidth() : getParent().getWidth();
                int columns = actionCount == 1 ? 1 : width >= 940 ? 3 : 2;
                if (columns != actionColumns) {
                    actionColumns = columns;
                    int rows = (actionCount + columns - 1) / columns;
                    int height = rows * 158 + (rows - 1) * 16;
                    actionGrid.setLayout(new GridLayout(rows, columns, 16, 16));
                    actionGrid.setPreferredSize(new Dimension(900, height));
                    actionGrid.setMinimumSize(new Dimension(0, height));
                    actionGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
                }
            }
        }

        @Override public Dimension getPreferredSize() {
            sizeActions();
            return super.getPreferredSize();
        }

        @Override public void doLayout() {
            sizeActions();
            super.doLayout();
        }

        @Override public Dimension getPreferredScrollableViewportSize() { return new Dimension(900, 750); }
        @Override public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) { return 20; }
        @Override public int getScrollableBlockIncrement(Rectangle visible, int orientation, int direction) { return Math.max(20, visible.height - 40); }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    private static class RoundedPanel extends JPanel {
        protected Color outline;
        private final int radius;
        RoundedPanel(Color background, Color outline, int radius) {
            this.outline = outline;
            this.radius = radius;
            setOpaque(false);
            setBackground(background);
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            if (outline != null) {
                g2.setColor(outline);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Decorative workspace illustration; no business values are represented. */
    private static class WorkspaceArt extends JPanel {
        WorkspaceArt() {
            setOpaque(false);
            setPreferredSize(new Dimension(190, 112));
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int x = Math.max(0, (getWidth() - 178) / 2);
            int y = Math.max(0, (getHeight() - 108) / 2);
            g2.translate(x, y);
            g2.setColor(new Color(222, 228, 255));
            g2.fillOval(24, 0, 112, 112);
            g2.setColor(new Color(207, 217, 254));
            g2.fillRoundRect(44, 14, 124, 82, 12, 12);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(28, 5, 124, 88, 12, 12);
            g2.setColor(UITheme.PRIMARY_LIGHT);
            g2.fillRoundRect(41, 19, 38, 9, 6, 6);
            g2.setColor(new Color(212, 220, 242));
            g2.fillRoundRect(41, 40, 94, 5, 4, 4);
            g2.fillRoundRect(41, 52, 72, 5, 4, 4);
            g2.fillRoundRect(41, 64, 84, 5, 4, 4);
            g2.setColor(UITheme.PRIMARY);
            g2.fillRoundRect(5, 63, 51, 43, 12, 12);
            UITheme.icon("users", 26, Color.WHITE).paintIcon(this, g2, 17, 72);
            g2.setColor(new Color(209, 250, 229));
            g2.fillOval(134, 74, 34, 34);
            UITheme.icon("check", 18, UITheme.SUCCESS_TEXT).paintIcon(this, g2, 142, 82);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        UITheme.setupGlobalUI();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
