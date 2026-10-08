package com.ui.admin;

import com.model.TaiKhoan;
import com.service.AuthService;
import com.ui.theme.UITheme;
import com.ui.theme.DatabaseTask;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
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

        // Renderer màu cho trạng thái
        tblTaiKhoan.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if ("HOAT_DONG".equals(value)) {
                    setForeground(UITheme.SUCCESS_TEXT);
                    setText("Hoạt động");
                } else if ("KHOA".equals(value)) {
                    setForeground(UITheme.DANGER_TEXT);
                    setText("Bị khóa");
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });

        btnKhoaMoKhoa    = new JButton("Khóa / Mở khóa");
        btnDatLaiMatKhau = new JButton("Đặt lại mật khẩu");
        btnDoiVaiTro     = new JButton("Đổi vai trò");
        btnLamMoi        = new JButton("Làm mới danh sách");
        lblThongKe       = new JLabel("Đang tải dữ liệu...");

        UITheme.styleDangerButton(btnKhoaMoKhoa);
        UITheme.styleSecondaryButton(btnDatLaiMatKhau);
        UITheme.stylePrimaryButton(btnDoiVaiTro);
        UITheme.styleSecondaryButton(btnLamMoi);

        lblThongKe.setFont(UITheme.FONT_CAPTION);
        lblThongKe.setForeground(UITheme.TEXT_MUTED);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(0, 10));
        setBackground(UITheme.BG_APP);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        // Top Toolbar (Flat Card)
        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        pnlToolbar.setBackground(Color.WHITE);
        pnlToolbar.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(8, 14, 8, 14)
        ));

        pnlToolbar.add(btnDoiVaiTro);
        pnlToolbar.add(btnDatLaiMatKhau);
        pnlToolbar.add(btnKhoaMoKhoa);
        pnlToolbar.add(btnLamMoi);

        add(pnlToolbar, BorderLayout.NORTH);

        // Center Table
        JScrollPane scrollPane = new JScrollPane(tblTaiKhoan);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER, 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Status (Flat Card)
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        pnlBottom.setBackground(Color.WHITE);
        pnlBottom.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(6, 12, 6, 12)
        ));
        pnlBottom.add(lblThongKe);
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
                                ? tk.getNgaySuaCuoi().toLocalDate().toString() : "—";

                        modelTaiKhoan.addRow(new Object[]{
                            tk.getMaTK(),
                            tk.getTenDangNhap(),
                            tenNV,
                            maNVStr,
                            tk.getVaiTro(),
                            tk.getTrangThai(),
                            tk.getNgayTao() != null ? tk.getNgayTao().toString() : "—",
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
        if (JOptionPane.showConfirmDialog(this, password, "Mật khẩu mới (8–128 ký tự)",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
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
        String roleMoi = (String) JOptionPane.showInputDialog(this,
                String.format("Chọn vai trò mới cho tài khoản '%s':", tk.getTenDangNhap()),
                "Đổi vai trò",
                JOptionPane.QUESTION_MESSAGE,
                null,
                roles,
                tk.getVaiTro());

        if (roleMoi == null || roleMoi.equals(tk.getVaiTro())) return;

        DatabaseTask.runExclusive(this, () -> { authService.capNhatVaiTro(tk.getMaTK(), roleMoi); return true; }, ok -> {
            JOptionPane.showMessageDialog(this,
                    String.format("Đã đổi vai trò tài khoản '%s' thành '%s'!", tk.getTenDangNhap(), roleMoi),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        });
    }
}
