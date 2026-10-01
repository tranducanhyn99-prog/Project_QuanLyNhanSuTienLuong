package com.ui.auth;

import com.model.TaiKhoan;
import com.service.AuthService;
import com.session.Session;
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

/**
 * LoginFrame – Màn hình đăng nhập hệ thống chuẩn Enterprise Desktop.
 *
 * Phong cách Microsoft Fluent Design:
 * - Khung đăng nhập phẳng, nền xám nhạt thanh lịch.
 * - Thẻ đăng nhập màu trắng viền mỏng 1px #E2E8F0.
 * - Font Segoe UI sắc nét, ô nhập liệu thoáng đãng (34px height).
 * - Nút bấm Primary Blue Accent (#0066CC).
 * - Giữ nguyên 100% logic xác thực AuthService và lưu Session.
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
        btnDangNhap    = new JButton("Đăng nhập");
        btnThoat       = new JButton("Thoát");
        lblStatus      = new JLabel(" ");

        UITheme.styleTextField(txtTenDangNhap);
        UITheme.stylePasswordField(txtMatKhau);
        UITheme.stylePrimaryButton(btnDangNhap);
        UITheme.styleSecondaryButton(btnThoat);

        btnDangNhap.setPreferredSize(new Dimension(140, 36));
        btnThoat.setPreferredSize(new Dimension(100, 36));

        lblStatus.setFont(UITheme.FONT_CAPTION);
        lblStatus.setForeground(UITheme.DANGER_TEXT);
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private void setupLayout() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(UITheme.BG_APP);

        // Card trung tâm
        JPanel card = new JPanel(new BorderLayout(0, 16));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(28, 32, 28, 32)
        ));
        card.setPreferredSize(new Dimension(420, 360));

        // Header trong card
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel lblBrand = new JLabel("HR & PAYROLL ENTERPRISE");
        lblBrand.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 15));
        lblBrand.setForeground(UITheme.PRIMARY);
        lblBrand.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Đăng nhập tài khoản hệ thống");
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(lblBrand);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(lblSub);

        // Form fields
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblUser = new JLabel("Tên đăng nhập:");
        lblUser.setFont(UITheme.FONT_BODY_BOLD);
        lblUser.setForeground(UITheme.TEXT_MAIN);
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formPanel.add(lblUser, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        formPanel.add(txtTenDangNhap, gbc);

        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setFont(UITheme.FONT_BODY_BOLD);
        lblPass.setForeground(UITheme.TEXT_MAIN);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(lblPass, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        formPanel.add(txtMatKhau, gbc);

        // Status Label
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        formPanel.add(lblStatus, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(btnDangNhap);
        btnPanel.add(btnThoat);

        card.add(headerPanel, BorderLayout.NORTH);
        card.add(formPanel, BorderLayout.CENTER);
        card.add(btnPanel, BorderLayout.SOUTH);

        root.add(card);
        setContentPane(root);
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
        String tenDangNhap = txtTenDangNhap.getText();
        String matKhau = new String(txtMatKhau.getPassword());

        btnDangNhap.setEnabled(false);
        lblStatus.setText("Đang xác thực thông tin...");
        lblStatus.setForeground(UITheme.INFO_TEXT);

        SwingWorker<TaiKhoan, Void> worker = new SwingWorker<TaiKhoan, Void>() {
            @Override
            protected TaiKhoan doInBackground() throws Exception {
                return authService.login(tenDangNhap, matKhau);
            }

            @Override
            protected void done() {
                btnDangNhap.setEnabled(true);
                try {
                    TaiKhoan taiKhoan = get();
                    if (taiKhoan == null) {
                        lblStatus.setText("Tên đăng nhập hoặc mật khẩu không chính xác!");
                        lblStatus.setForeground(UITheme.DANGER_TEXT);
                        txtMatKhau.setText("");
                        txtMatKhau.requestFocusInWindow();
                        return;
                    }

                    Session.getInstance().login(taiKhoan);

                    lblStatus.setText("Đăng nhập thành công!");
                    lblStatus.setForeground(UITheme.SUCCESS_TEXT);

                    openMainFrame();
                } catch (Exception ex) {
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

    private void openMainFrame() {
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
            this.dispose();
        });
    }

    private void setupFrame() {
        setTitle(APP_TITLE + " – Đăng nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 420);
        setMinimumSize(new Dimension(440, 380));
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
