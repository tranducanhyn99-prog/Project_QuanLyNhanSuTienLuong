package com.ui.auth;

import com.model.TaiKhoan;
import com.service.AuthService;
import com.ui.main.MainFrame;
import com.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * LoginFrame – Màn hình đăng nhập hệ thống chuẩn Enterprise Desktop.
 *
 * Phong cách thiết kế: Modern SaaS ERP (Gusto / Rippling style)
 * - Khung đăng nhập phẳng, nền xám thanh lịch #F8FAFC.
 * - Thẻ đăng nhập màu trắng viền mỏng #E2E8F0, góc bo mềm mại.
 * - Nút bấm tự vẽ (Custom Antialiased Painting) triệt tiêu lỗi trắng-trên-trắng.
 * - Hỗ trợ các nút chọn nhanh tài khoản mẫu kiểm thử (Admin, HR, Kế toán, Nhân viên).
 *
 * @author Nhóm 06 – DBMS Enterprise
 */
public class LoginFrame extends JFrame {

    private static final String APP_TITLE = "Hệ thống Quản lý Nhân sự và Tiền lương";

    private final AuthService authService = new AuthService();

    private JTextField     txtTenDangNhap;
    private JPasswordField txtMatKhau;
    private JButton        btnDangNhap;
    private JButton        btnThoat;
    private JLabel         lblStatus;
    private boolean loginInProgress;

    public LoginFrame() {
        UITheme.setupGlobalUI();
        initComponents();
        setupLayout();
        setupEvents();
        setupFrame();
    }

    private void initComponents() {
        txtTenDangNhap = new JTextField(20);
        txtMatKhau     = new JPasswordField(20);

        // Nút đăng nhập ModernButton tự vẽ nền Indigo, chữ trắng nổi bật 100%
        btnDangNhap = new UITheme.ModernButton(
            "Đăng nhập vào hệ thống",
            UITheme.PRIMARY,
            UITheme.PRIMARY_HOVER,
            Color.WHITE,
            UITheme.PRIMARY_HOVER
        );
        btnDangNhap.setPreferredSize(new Dimension(360, 42));

        btnThoat = new UITheme.ModernButton(
            "Thoát",
            Color.WHITE,
            new Color(241, 245, 249),
            UITheme.TEXT_MUTED,
            UITheme.BORDER_INPUT
        );
        btnThoat.setPreferredSize(new Dimension(100, 36));

        lblStatus = new JLabel(" ");
        lblStatus.setFont(UITheme.FONT_CAPTION_BOLD);
        lblStatus.setForeground(UITheme.DANGER_TEXT);
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);

        UITheme.styleTextField(txtTenDangNhap);
        UITheme.stylePasswordField(txtMatKhau);
        txtTenDangNhap.setPreferredSize(new Dimension(360, 38));
        txtMatKhau.setPreferredSize(new Dimension(360, 38));
    }

    private void setupLayout() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(UITheme.BG_APP);

        // Card trung tâm
        JPanel card = new JPanel(new BorderLayout(0, 16));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(32, 36, 28, 36)
        ));
        card.setPreferredSize(new Dimension(460, 540));

        // 1. Header trong card
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        // Logo icon
        JLabel lblLogo = new JLabel("💼", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY_LIGHT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(UITheme.PRIMARY_BORDER);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 16, 16));
                super.paintComponent(g);
                g2.dispose();
            }
        };
        lblLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        lblLogo.setPreferredSize(new Dimension(56, 56));
        lblLogo.setMaximumSize(new Dimension(56, 56));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblBrand = new JLabel("HR & PAYROLL ENTERPRISE");
        lblBrand.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 17));
        lblBrand.setForeground(UITheme.TEXT_MAIN);
        lblBrand.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Hệ thống Quản lý Nhân sự & Tiền lương");
        lblSub.setFont(UITheme.FONT_CAPTION);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(lblLogo);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(lblBrand);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(lblSub);

        // 2. Form fields
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);

        JLabel lblUser = new JLabel("Tên đăng nhập:");
        lblUser.setFont(UITheme.FONT_BODY_BOLD);
        lblUser.setForeground(UITheme.TEXT_MAIN);

        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setFont(UITheme.FONT_BODY_BOLD);
        lblPass.setForeground(UITheme.TEXT_MAIN);

        formPanel.add(lblUser);
        formPanel.add(Box.createVerticalStrut(6));
        formPanel.add(txtTenDangNhap);
        formPanel.add(Box.createVerticalStrut(12));
        formPanel.add(lblPass);
        formPanel.add(Box.createVerticalStrut(6));
        formPanel.add(txtMatKhau);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(lblStatus);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(btnDangNhap);

        // 3. Quick Login Credentials (tiện ích demo & kiểm thử)
        JPanel samplePanel = new JPanel(new BorderLayout(0, 6));
        samplePanel.setOpaque(false);
        samplePanel.setBorder(new CompoundBorder(
            new LineBorder(new Color(241, 245, 249), 1, true),
            new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel lblSampleTitle = new JLabel("TÀI KHOẢN TEST NHANH:");
        lblSampleTitle.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 10));
        lblSampleTitle.setForeground(UITheme.TEXT_MUTED);

        JPanel chipsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        chipsPanel.setOpaque(false);

        addQuickLoginChip(chipsPanel, "Admin", "admin", "");
        addQuickLoginChip(chipsPanel, "HR Manager", "hr_manager", "");
        addQuickLoginChip(chipsPanel, "Kế toán", "payroll_officer", "");
        addQuickLoginChip(chipsPanel, "Nhân viên", "employee01", "");

        samplePanel.add(lblSampleTitle, BorderLayout.NORTH);
        samplePanel.add(chipsPanel, BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        if (Boolean.getBoolean("app.demo")) footerPanel.add(samplePanel, BorderLayout.CENTER);

        card.add(headerPanel, BorderLayout.NORTH);
        card.add(formPanel, BorderLayout.CENTER);
        card.add(footerPanel, BorderLayout.SOUTH);

        root.add(card);
        setContentPane(root);
    }

    private void addQuickLoginChip(JPanel parent, String label, String user, String pass) {
        JButton btn = new JButton(label);
        btn.setUI(new UITheme.RoundedButtonUI(12, UITheme.PRIMARY_BORDER));
        btn.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 11));
        btn.setForeground(UITheme.PRIMARY);
        btn.setBackground(UITheme.PRIMARY_LIGHT);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(4, 10, 4, 10));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            txtTenDangNhap.setText(user);
            txtMatKhau.setText(pass);
            lblStatus.setText("Đã nạp tài khoản: " + label);
            lblStatus.setForeground(UITheme.INFO_TEXT);
        });
        parent.add(btn);
    }

    private void setupEvents() {
        btnDangNhap.addActionListener(this::handleLogin);
        btnThoat.addActionListener(e -> System.exit(0));

        txtMatKhau.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin(null);
                }
            }
        });

        txtTenDangNhap.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtMatKhau.requestFocusInWindow();
                }
            }
        });
    }

    private void handleLogin(ActionEvent e) {
        if (loginInProgress) return;
        String tenDangNhap = txtTenDangNhap.getText().trim();
        char[] entered = txtMatKhau.getPassword();
        String matKhau = new String(entered);
        java.util.Arrays.fill(entered, '\0');

        if (tenDangNhap.isEmpty() || matKhau.isEmpty()) {
            lblStatus.setText("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            lblStatus.setForeground(UITheme.DANGER_TEXT);
            return;
        }

        loginInProgress = true;
        btnDangNhap.setEnabled(false);
        txtTenDangNhap.setEnabled(false);
        txtMatKhau.setEnabled(false);
        lblStatus.setText("Đang xác thực thông tin...");
        lblStatus.setForeground(UITheme.INFO_TEXT);

        SwingWorker<TaiKhoan, Void> worker = new SwingWorker<TaiKhoan, Void>() {
            @Override
            protected TaiKhoan doInBackground() throws Exception {
                return authService.login(tenDangNhap, matKhau);
            }

            @Override
            protected void done() {
                try {
                    TaiKhoan taiKhoan = get();
                    if (taiKhoan == null) {
                        finishLoginAttempt();
                        lblStatus.setText("Tên đăng nhập hoặc mật khẩu không chính xác!");
                        lblStatus.setForeground(UITheme.DANGER_TEXT);
                        txtMatKhau.setText("");
                        txtMatKhau.requestFocusInWindow();
                        return;
                    }

                    lblStatus.setText("Đăng nhập thành công!");
                    lblStatus.setForeground(UITheme.SUCCESS_TEXT);
                    txtMatKhau.setText("");
                    openMainFrame();
                } catch (Exception ex) {
                    finishLoginAttempt();
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    String msg = cause.getMessage();
                    if (msg == null || msg.trim().isEmpty()) {
                        msg = "Đăng nhập thất bại!";
                    }
                    lblStatus.setText(msg);
                    lblStatus.setForeground(UITheme.DANGER_TEXT);
                    txtMatKhau.setText("");
                    txtMatKhau.requestFocusInWindow();
                }
            }
        };
        worker.execute();
    }

    private void finishLoginAttempt() {
        loginInProgress = false;
        btnDangNhap.setEnabled(true);
        txtTenDangNhap.setEnabled(true);
        txtMatKhau.setEnabled(true);
    }

    private void openMainFrame() {
        MainFrame mainFrame = new MainFrame();
        mainFrame.setVisible(true);
        this.dispose();
    }

    private void setupFrame() {
        setTitle(APP_TITLE + " – Đăng nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 620);
        setMinimumSize(new Dimension(500, 600));
        setLocationRelativeTo(null);
        setResizable(false);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                txtTenDangNhap.requestFocusInWindow();
            }
        });
    }

    public static void main(String[] args) {
        UITheme.setupGlobalUI();
        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}
