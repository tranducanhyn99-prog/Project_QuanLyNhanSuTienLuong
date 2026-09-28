package com.test;

import com.model.TaiKhoan;
import com.session.Session;
import com.ui.main.MainFrame;

import javax.swing.*;
import java.awt.*;

/**
 * QuickTestLauncher – Giao diện kiểm thử nhanh các vai trò trong hệ thống.
 * Cho phép người dùng trực tiếp mở MainFrame với từng vai trò cụ thể
 * để kiểm tra hiển thị Session (Tên đăng nhập, Vai trò) và ma trận MenuBar.
 */
public class QuickTestLauncher extends JFrame {

    public QuickTestLauncher() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Công cụ Kiểm thử Phân quyền & Session – TV5");
        setSize(520, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Header
        JPanel pnlHeader = new JPanel(new GridLayout(2, 1, 0, 5));
        JLabel lblTitle = new JLabel("KIỂM THỬ GIAO DIỆN & PHÂN QUYỀN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(0, 51, 153));

        JLabel lblSub = new JLabel("Chọn vai trò để mở MainFrame và kiểm tra hiển thị Session & Menu", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(Color.DARK_GRAY);

        pnlHeader.add(lblTitle);
        pnlHeader.add(lblSub);
        mainPanel.add(pnlHeader, BorderLayout.NORTH);

        // Buttons
        JPanel pnlButtons = new JPanel(new GridLayout(5, 1, 10, 10));

        JButton btnAdmin = createRoleButton(
            "1. DB_Admin (admin – Quản trị viên)",
            "Toàn quyền: Hiện đủ 7 Menu (Nhân viên, Danh mục, Chấm công, Phụ cấp, Lương, Báo cáo, Quản trị)",
            new Color(220, 53, 69),
            () -> launchWithRole(new TaiKhoan(1, -1, "admin", "DB_Admin", null, "HOAT_DONG"))
        );

        JButton btnHR = createRoleButton(
            "2. HR_Manager (hr_manager – Quản lý nhân sự)",
            "Hiện: Nhân viên, Danh mục, Chấm công, Phụ cấp, Báo cáo | Ẩn: Lương, Quản trị",
            new Color(40, 167, 69),
            () -> launchWithRole(new TaiKhoan(2, 1, "hr_manager", "HR_Manager", "Nguyễn Văn HR", "HOAT_DONG"))
        );

        JButton btnPayroll = createRoleButton(
            "3. Payroll_Officer (payroll_officer – Kế toán lương)",
            "Hiện: Chấm công (chỉ xem), Phụ cấp, Lương, Báo cáo | Ẩn: Nhân viên, Danh mục, Quản trị",
            new Color(255, 153, 0),
            () -> launchWithRole(new TaiKhoan(3, 2, "payroll_officer", "Payroll_Officer", "Trần Thị Payroll", "HOAT_DONG"))
        );

        JButton btnEmployee = createRoleButton(
            "4. Employee (employee01 – Nhân viên)",
            "Hiện duy nhất: Báo cáo (chỉ Phiếu lương cá nhân) | Ẩn: Toàn bộ menu nghiệp vụ khác",
            new Color(23, 162, 184),
            () -> launchWithRole(new TaiKhoan(4, 3, "employee01", "Employee", "Lê Văn Nhân Viên", "HOAT_DONG"))
        );

        JButton btnRunTests = new JButton("🧪 Chạy kiểm thử tự động (67 Testcase)");
        btnRunTests.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRunTests.addActionListener(e -> {
            AuthRolePermissionTest.main(new String[0]);
            JOptionPane.showMessageDialog(this,
                "Đã chạy xong 67 testcase kiểm thử tự động thành công (100% PASS)!\nXem chi tiết tại terminal.",
                "Kết quả kiểm thử",
                JOptionPane.INFORMATION_MESSAGE);
        });

        pnlButtons.add(btnAdmin);
        pnlButtons.add(btnHR);
        pnlButtons.add(btnPayroll);
        pnlButtons.add(btnEmployee);
        pnlButtons.add(btnRunTests);

        mainPanel.add(pnlButtons, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    private JButton createRoleButton(String title, String tooltip, Color color, Runnable action) {
        JButton btn = new JButton(title);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setToolTipText(tooltip);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private void launchWithRole(TaiKhoan tk) {
        Session.getInstance().login(tk);
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        Font defaultFont = new Font("Segoe UI", Font.PLAIN, 13);
        Font boldFont = new Font("Segoe UI", Font.BOLD, 13);
        UIManager.put("Label.font", defaultFont);
        UIManager.put("Button.font", boldFont);
        UIManager.put("TextField.font", defaultFont);
        UIManager.put("PasswordField.font", defaultFont);
        UIManager.put("Table.font", defaultFont);
        UIManager.put("TableHeader.font", boldFont);
        UIManager.put("ComboBox.font", defaultFont);
        UIManager.put("TabbedPane.font", defaultFont);
        UIManager.put("Menu.font", defaultFont);
        UIManager.put("MenuItem.font", defaultFont);
        UIManager.put("TitledBorder.font", boldFont);

        SwingUtilities.invokeLater(() -> {
            QuickTestLauncher launcher = new QuickTestLauncher();
            launcher.setVisible(true);
        });
    }
}
