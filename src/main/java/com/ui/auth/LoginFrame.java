package com.ui.auth;

import com.model.TaiKhoan;
import com.service.AuthService;
import com.ui.main.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * LoginFrame – Màn hình đăng nhập hệ thống.
 *
 * Luồng:
 * 1. Người dùng nhập TenDangNhap + MatKhau
 * 2. Nhấn "Đăng nhập" hoặc Enter
 * 3. AuthService.login() xác thực
 * 4. Thành công → ẩn LoginFrame, mở MainFrame
 * 5. Thất bại → JOptionPane thông báo lỗi
 *
 * @author Trần Đức Anh (TV5 – MSSV 24110155)
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
        initComponents();
        setupLayout();
        setupEvents();
        setupFrame();
    }

    // ─── Khởi tạo components ─────────────────────────────────────────

    private void initComponents() {
        txtTenDangNhap = new JTextField(20);
        txtMatKhau     = new JPasswordField(20);
        btnDangNhap    = new JButton("Đăng nhập");
        btnThoat       = new JButton("Thoát");
        lblStatus      = new JLabel(" ");

        txtTenDangNhap.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtMatKhau.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnDangNhap.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnThoat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblStatus.setForeground(Color.RED);
    }

    // ─── Bố cục giao diện ────────────────────────────────────────────

    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Tiêu đề
        JLabel lblTitle = new JLabel(APP_TITLE, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel lblSubTitle = new JLabel("Nhóm 06 – DBMS330284", SwingConstants.CENTER);
        lblSubTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubTitle.setForeground(Color.GRAY);

        JPanel pnlHeader = new JPanel(new GridLayout(2, 1));
        pnlHeader.add(lblTitle);
        pnlHeader.add(lblSubTitle);

        // Form đăng nhập
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Đăng nhập"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblUser = new JLabel("Tên đăng nhập:");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        pnlForm.add(lblUser, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        pnlForm.add(txtTenDangNhap, gbc);

        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        pnlForm.add(lblPass, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        pnlForm.add(txtMatKhau, gbc);

        // Thông báo trạng thái
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        pnlForm.add(lblStatus, gbc);

        // Nút bấm
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        pnlButtons.add(btnDangNhap);
        pnlButtons.add(btnThoat);

        mainPanel.add(pnlHeader, BorderLayout.NORTH);
        mainPanel.add(pnlForm, BorderLayout.CENTER);
        mainPanel.add(pnlButtons, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    // ─── Sự kiện ─────────────────────────────────────────────────────

    private void setupEvents() {
        btnDangNhap.addActionListener(this::handleLogin);
        btnThoat.addActionListener(e -> System.exit(0));

        // Enter trên ô mật khẩu = nhấn Đăng nhập
        txtMatKhau.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin(null);
                }
            }
        });

        // Enter trên ô username = chuyển focus sang password
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

        // Disable nút để tránh double-click
        btnDangNhap.setEnabled(false);
        lblStatus.setText("Đang đăng nhập...");
        lblStatus.setForeground(Color.BLUE);

        // Thực hiện trên SwingWorker để không block UI
        SwingWorker<TaiKhoan, Void> worker = new SwingWorker<TaiKhoan, Void>() {
            private Exception error;

            @Override
            protected TaiKhoan doInBackground() {
                try {
                    return authService.login(tenDangNhap, matKhau);
                } catch (Exception ex) {
                    error = ex;
                    return null;
                }
            }

            @Override
            protected void done() {
                btnDangNhap.setEnabled(true);
                if (error != null) {
                    lblStatus.setText(error.getMessage());
                    lblStatus.setForeground(Color.RED);
                    txtMatKhau.setText("");
                    txtMatKhau.requestFocusInWindow();
                    System.err.println("[LoginFrame] Lỗi đăng nhập: " + error.getMessage());
                } else {
                    lblStatus.setText("Đăng nhập thành công!");
                    lblStatus.setForeground(new Color(0, 128, 0));
                    openMainFrame();
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

    // ─── Cấu hình JFrame ─────────────────────────────────────────────

    private void setupFrame() {
        setTitle(APP_TITLE + " – Đăng nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 320);
        setMinimumSize(new Dimension(400, 280));
        setLocationRelativeTo(null);  // Giữa màn hình
        setResizable(false);

        // Focus vào ô tên đăng nhập khi mở
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                txtTenDangNhap.requestFocusInWindow();
            }
        });
    }

    // ─── Entry point ─────────────────────────────────────────────────

    /**
     * Điểm khởi chạy ứng dụng.
     */
    public static void main(String[] args) {
        // Thiết lập Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fallback về Metal L&F mặc định
        }

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
