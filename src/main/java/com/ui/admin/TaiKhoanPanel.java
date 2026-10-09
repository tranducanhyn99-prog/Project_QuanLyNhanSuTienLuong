package com.ui.admin;

import com.model.TaiKhoan;
import com.service.AuthService;
import com.ui.theme.UITheme;
import com.ui.theme.DatabaseTask;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * TaiKhoanPanel – Quản trị danh sách tài khoản người dùng (chỉ DB_Admin).
 *
 * Chức năng:
 * - Xem danh sách toàn bộ tài khoản trong hệ thống
 * - Khóa / Mở khóa tài khoản (phục vụ demo Tình huống 6 bảo mật)
 * - Đặt lại mật khẩu (PBKDF2-SHA256)
 * - Cập nhật vai trò (DB_Admin, HR_Manager, Payroll_Officer, Employee)
 * - Tải lại danh sách
 *
 * @author Trần Đức Anh (TV5 – MSSV 24110155)
 */
public class TaiKhoanPanel extends JPanel {

    private final AuthService authService = new AuthService();
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private JTable            tblTaiKhoan;
    private DefaultTableModel modelTaiKhoan;
    private JButton           btnKhoaMoKhoa;
    private JButton           btnDatLaiMatKhau;
    private JButton           btnDoiVaiTro;
    private JButton           btnLamMoi;
    private JLabel            lblThongKe;

    private List<TaiKhoan>    danhSachHienTai;

    public TaiKhoanPanel() {
        initComponents();
        setupLayout();
        setupEvents();
        loadData();
    }

    private void initComponents() {
        String[] columns = {
            "Mã TK", "Tên đăng nhập", "Nhân viên liên kết", "Mã NV",
            "Vai trò", "Trạng thái", "Ngày tạo", "Ngày sửa cuối"
        };

        modelTaiKhoan = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tblTaiKhoan = new JTable(modelTaiKhoan);
        UITheme.styleTable(tblTaiKhoan);
        tblTaiKhoan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblTaiKhoan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {80, 170, 210, 80, 180, 130, 140, 140};
        for (int i = 0; i < widths.length; i++) {
            tblTaiKhoan.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        tblTaiKhoan.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setText(roleLabel(value == null ? "" : value.toString()));
                setFont(UITheme.FONT_BODY);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                setBackground(isSelected ? UITheme.PRIMARY_LIGHT : row % 2 == 0 ? Color.WHITE : UITheme.BG_ROW_ALT);
                setForeground(isSelected ? UITheme.PRIMARY_ACTIVE : UITheme.TEXT_MAIN);
                return c;
            }
        });

        btnKhoaMoKhoa    = new JButton("Khóa / Mở khóa");
        btnDatLaiMatKhau = new JButton("Đặt lại mật khẩu");
        btnDoiVaiTro     = new JButton("Đổi vai trò");
        btnLamMoi        = new JButton("Làm mới");
        lblThongKe       = new JLabel("Đang tải dữ liệu...");

        UITheme.styleDangerButton(btnKhoaMoKhoa);
        UITheme.styleSecondaryButton(btnDatLaiMatKhau);
        UITheme.stylePrimaryButton(btnDoiVaiTro);
        UITheme.styleSecondaryButton(btnLamMoi);
        btnDoiVaiTro.setIcon(UITheme.icon("shield", 16, Color.WHITE));
        btnDatLaiMatKhau.setIcon(UITheme.icon("settings", 16, UITheme.TEXT_MAIN));
        btnKhoaMoKhoa.setIcon(UITheme.icon("shield", 16, UITheme.DANGER_TEXT));
        btnLamMoi.setIcon(UITheme.icon("refresh", 16, UITheme.TEXT_MAIN));

        lblThongKe.setFont(UITheme.FONT_CAPTION);
        lblThongKe.setForeground(UITheme.TEXT_MUTED);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(0, 18));
        setBackground(UITheme.BG_APP);
        setBorder(new EmptyBorder(22, 24, 22, 24));

        // Top Toolbar (Flat Card)
        JPanel pnlToolbar = UITheme.createCardPanel();
        pnlToolbar.setLayout(new BorderLayout(10, 0));
        pnlToolbar.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        actions.add(btnDoiVaiTro);
        actions.add(btnDatLaiMatKhau);
        actions.add(btnKhoaMoKhoa);
        pnlToolbar.add(actions, BorderLayout.WEST);
        pnlToolbar.add(btnLamMoi, BorderLayout.EAST);

        JPanel heading = new JPanel(new BorderLayout(0, 16));
        heading.setOpaque(false);
        heading.add(UITheme.createPageHeader("Tài khoản & phân quyền",
                "Quản lý quyền truy cập và bảo vệ tài khoản trong hệ thống."), BorderLayout.NORTH);
        heading.add(pnlToolbar, BorderLayout.CENTER);
        add(heading, BorderLayout.NORTH);

        // Center Table
        JScrollPane scrollPane = new JScrollPane(tblTaiKhoan);
        UITheme.styleScrollPane(scrollPane);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Status (Flat Card)
        JPanel pnlBottom = UITheme.createCardPanel();
        pnlBottom.setLayout(new BorderLayout(12, 0));
        pnlBottom.setBorder(new EmptyBorder(12, 16, 12, 16));
        pnlBottom.add(lblThongKe, BorderLayout.WEST);
        JLabel hint = new JLabel("Chọn tài khoản để thao tác");
        hint.setFont(UITheme.FONT_CAPTION);
        hint.setForeground(UITheme.TEXT_MUTED);
        pnlBottom.add(hint, BorderLayout.EAST);
        add(pnlBottom, BorderLayout.SOUTH);
    }

    private void setupEvents() {
        btnLamMoi.addActionListener(e -> loadData());
        btnKhoaMoKhoa.addActionListener(e -> handleKhoaMoKhoa());
        btnDatLaiMatKhau.addActionListener(e -> handleDatLaiMatKhau());
        btnDoiVaiTro.addActionListener(e -> handleDoiVaiTro());
    }

    private void loadData() {
        DatabaseTask.run(tblTaiKhoan, () -> authService.layDanhSachTaiKhoan(), list -> {
                    danhSachHienTai = list;
                    modelTaiKhoan.setRowCount(0);
                    int hoatDongCount = 0;
                    int khoaCount = 0;

                    for (TaiKhoan tk : danhSachHienTai) {
                        String tenNV = tk.getHoTenNV() != null ? tk.getHoTenNV() : "(Không có / Quản trị)";
                        String maNVStr = tk.getMaNV() > 0 ? String.valueOf(tk.getMaNV()) : "—";
                        String ngaySua = tk.getNgaySuaCuoi() != null
                                ? dateFormat.format(tk.getNgaySuaCuoi()) : "—";

                        modelTaiKhoan.addRow(new Object[]{
                            tk.getMaTK(),
                            tk.getTenDangNhap(),
                            tenNV,
                            maNVStr,
                            tk.getVaiTro(),
                            tk.getTrangThai(),
                            tk.getNgayTao() != null ? dateFormat.format(tk.getNgayTao()) : "—",
                            ngaySua
                        });

                        if ("HOAT_DONG".equals(tk.getTrangThai())) hoatDongCount++;
                        else khoaCount++;
                    }

                    lblThongKe.setText(String.format("Tổng số: %d tài khoản (%d hoạt động, %d bị khóa)",
                            danhSachHienTai.size(), hoatDongCount, khoaCount));
        });
    }

    private TaiKhoan getSelectedTaiKhoan() {
        int selectedRow = tblTaiKhoan.getSelectedRow();
        if (selectedRow >= 0) selectedRow = tblTaiKhoan.convertRowIndexToModel(selectedRow);
        if (selectedRow < 0 || danhSachHienTai == null || selectedRow >= danhSachHienTai.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một tài khoản trong bảng!",
                    "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return danhSachHienTai.get(selectedRow);
    }

    private void handleKhoaMoKhoa() {
        TaiKhoan tk = getSelectedTaiKhoan();
        if (tk == null) return;

        String trangThaiMoi = "HOAT_DONG".equals(tk.getTrangThai()) ? "KHOA" : "HOAT_DONG";
        String hanhDong = "KHOA".equals(trangThaiMoi) ? "khóa" : "mở khóa";

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Bạn có chắc chắn muốn %s tài khoản '%s'?", hanhDong, tk.getTenDangNhap()),
                "Xác nhận " + hanhDong, JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        DatabaseTask.runExclusive(this, () -> { authService.doiTrangThaiTaiKhoan(tk.getMaTK(), trangThaiMoi); return true; }, ok -> {
            JOptionPane.showMessageDialog(this, String.format("Đã %s tài khoản '%s' thành công!", hanhDong, tk.getTenDangNhap()),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        });
    }

    private void handleDatLaiMatKhau() {
        TaiKhoan tk = getSelectedTaiKhoan();
        if (tk == null) return;

        JPasswordField password = new JPasswordField(24);
        UITheme.styleTextField(password);
        JPanel form = new JPanel(new BorderLayout(0, 10));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(8, 4, 8, 4));
        JLabel label = new JLabel("Mật khẩu mới cho " + tk.getTenDangNhap());
        label.setFont(UITheme.FONT_BODY_BOLD);
        label.setForeground(UITheme.TEXT_MAIN);
        label.setLabelFor(password);
        JLabel hint = new JLabel("Sử dụng từ 8 đến 128 ký tự.");
        hint.setFont(UITheme.FONT_CAPTION);
        hint.setForeground(UITheme.TEXT_MUTED);
        form.add(label, BorderLayout.NORTH);
        form.add(password, BorderLayout.CENTER);
        form.add(hint, BorderLayout.SOUTH);
        if (JOptionPane.showConfirmDialog(this, form, "Đặt lại mật khẩu",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        char[] entered = password.getPassword();
        String matKhauMoi = new String(entered);
        java.util.Arrays.fill(entered, '\0');
        password.setText("");
        if (matKhauMoi.isEmpty()) return;

        DatabaseTask.runExclusive(this, () -> { authService.datLaiMatKhau(tk.getMaTK(), matKhauMoi); return true; }, ok -> {
            JOptionPane.showMessageDialog(this,
                    String.format("Đã đặt lại mật khẩu cho tài khoản '%s' thành công!", tk.getTenDangNhap()),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void handleDoiVaiTro() {
        TaiKhoan tk = getSelectedTaiKhoan();
        if (tk == null) return;

        String[] roles = {"DB_Admin", "HR_Manager", "Payroll_Officer", "Employee"};
        JComboBox<String> roleChoice = new JComboBox<>(roles);
        UITheme.styleComboBox(roleChoice);
        roleChoice.setSelectedItem(tk.getVaiTro());
        roleChoice.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean selected, boolean focus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focus);
                label.setText(roleLabel(value == null ? "" : value.toString()));
                label.setBorder(new EmptyBorder(7, 10, 7, 10));
                return label;
            }
        });
        JPanel form = new JPanel(new BorderLayout(0, 12));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(8, 4, 8, 4));
        JLabel label = new JLabel("Vai trò của tài khoản " + tk.getTenDangNhap());
        label.setFont(UITheme.FONT_BODY_BOLD);
        label.setForeground(UITheme.TEXT_MAIN);
        label.setLabelFor(roleChoice);
        form.add(label, BorderLayout.NORTH);
        form.add(roleChoice, BorderLayout.CENTER);
        if (JOptionPane.showConfirmDialog(this, form, "Đổi vai trò",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        String roleMoi = (String) roleChoice.getSelectedItem();

        if (roleMoi == null || roleMoi.equals(tk.getVaiTro())) return;

        DatabaseTask.runExclusive(this, () -> { authService.capNhatVaiTro(tk.getMaTK(), roleMoi); return true; }, ok -> {
            JOptionPane.showMessageDialog(this,
                    String.format("Đã đổi vai trò tài khoản '%s' thành '%s'!", tk.getTenDangNhap(), roleLabel(roleMoi)),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        });
    }

    private String roleLabel(String role) {
        switch (role) {
            case "DB_Admin": return "Quản trị hệ thống";
            case "HR_Manager": return "Quản lý nhân sự";
            case "Payroll_Officer": return "Chuyên viên tiền lương";
            case "Employee": return "Nhân viên";
            default: return role;
        }
    }
}
