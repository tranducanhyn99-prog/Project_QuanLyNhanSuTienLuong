package com.ui.admin;

import com.model.TaiKhoan;
import com.service.AuthService;

import javax.swing.*;
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
 * - Đặt lại mật khẩu (hash SHA-256)
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
        tblTaiKhoan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblTaiKhoan.setRowHeight(26);
        tblTaiKhoan.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblTaiKhoan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Renderer màu cho trạng thái
        tblTaiKhoan.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if ("HOAT_DONG".equals(value)) {
                    setForeground(new Color(0, 130, 0));
                    setText("● Hoạt động");
                } else if ("KHOA".equals(value)) {
                    setForeground(Color.RED);
                    setText("■ Bị khóa");
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });

        btnKhoaMoKhoa    = new JButton("Khóa / Mở khóa");
        btnDatLaiMatKhau = new JButton("Đặt lại mật khẩu");
        btnDoiVaiTro     = new JButton("Đổi vai trò");
        btnLamMoi        = new JButton("Làm mới");
        lblThongKe       = new JLabel("Đang tải dữ liệu...");

        btnKhoaMoKhoa.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDatLaiMatKhau.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnDoiVaiTro.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLamMoi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblThongKe.setFont(new Font("Segoe UI", Font.ITALIC, 12));
    }

    private void setupLayout() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Toolbar
        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlToolbar.setBorder(BorderFactory.createTitledBorder("Tác vụ quản trị"));

        pnlToolbar.add(btnKhoaMoKhoa);
        pnlToolbar.add(btnDatLaiMatKhau);
        pnlToolbar.add(btnDoiVaiTro);
        pnlToolbar.add(btnLamMoi);

        add(pnlToolbar, BorderLayout.NORTH);

        // Center Table
        JScrollPane scrollPane = new JScrollPane(tblTaiKhoan);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Danh sách tài khoản"));
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Status
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
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
        SwingWorker<List<TaiKhoan>, Void> worker = new SwingWorker<List<TaiKhoan>, Void>() {
            @Override
            protected List<TaiKhoan> doInBackground() throws Exception {
                return authService.layDanhSachTaiKhoan();
            }

            @Override
            protected void done() {
                try {
                    danhSachHienTai = get();
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
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(TaiKhoanPanel.this,
                            "Lỗi tải danh sách tài khoản: " + ex.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private TaiKhoan getSelectedTaiKhoan() {
        int selectedRow = tblTaiKhoan.getSelectedRow();
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

        try {
            authService.doiTrangThaiTaiKhoan(tk.getMaTK(), trangThaiMoi);
            JOptionPane.showMessageDialog(this, String.format("Đã %s tài khoản '%s' thành công!", hanhDong, tk.getTenDangNhap()),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDatLaiMatKhau() {
        TaiKhoan tk = getSelectedTaiKhoan();
        if (tk == null) return;

        String matKhauMoi = JOptionPane.showInputDialog(this,
                String.format("Nhập mật khẩu mới cho tài khoản '%s':\n(Mặc định: 123456)", tk.getTenDangNhap()),
                "123456");

        if (matKhauMoi == null || matKhauMoi.trim().isEmpty()) return;

        try {
            authService.datLaiMatKhau(tk.getMaTK(), matKhauMoi.trim());
            JOptionPane.showMessageDialog(this,
                    String.format("Đã đặt lại mật khẩu cho tài khoản '%s' thành công!", tk.getTenDangNhap()),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
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

        try {
            authService.capNhatVaiTro(tk.getMaTK(), roleMoi);
            JOptionPane.showMessageDialog(this,
                    String.format("Đã đổi vai trò tài khoản '%s' thành '%s'!", tk.getTenDangNhap(), roleMoi),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
