package com.ui.baocao;

import com.model.BangLuong;
import com.model.ChiTietBangLuong;
import com.service.PayrollService;
import com.session.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

/**
 * BaoCaoPanel – Giao diện báo cáo bảng lương chi tiết.
 *
 * Dữ liệu đọc từ View vw_BangLuongChiTiet (không dùng inline SQL).
 *
 * Chế độ hiển thị theo vai trò:
 * - DB_Admin / HR_Manager / Payroll_Officer → xem toàn bộ nhân viên
 * - Employee → chỉ xem phiếu lương cá nhân
 *
 * Tính năng:
 * - Bộ lọc theo kỳ lương (ComboBox)
 * - Bảng JTable hiển thị chi tiết (HoTen, LuongCoBan, NgayCongTT, TienCong, PhuCap, KhauTru, ThucNhan)
 * - Tổng cộng thực nhận ở cuối bảng
 * - Nút Chốt lương (chỉ hiện cho DB_Admin/Payroll_Officer, kỳ CHUA_CHOT)
 *
 * @author Trần Đức Anh (TV5 – MSSV 24110155)
 */
public class BaoCaoPanel extends JPanel {

    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0");

    private final PayrollService payrollService = new PayrollService();
    private final Session session = Session.getInstance();

    // UI
    private JComboBox<BangLuong> cboKyLuong;
    private JTable               tblBaoCao;
    private DefaultTableModel    modelBaoCao;
    private JLabel               lblTongThucNhan;
    private JLabel               lblTrangThai;
    private JButton              btnChotLuong;
    private JButton              btnLamMoi;

    public BaoCaoPanel() {
        initComponents();
        setupLayout();
        setupEvents();
        loadKyLuong();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  KHỞI TẠO
    // ═══════════════════════════════════════════════════════════════════

    private void initComponents() {
        cboKyLuong = new JComboBox<>();
        cboKyLuong.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        String[] columns;
        if (session.hasRole("Employee") && !session.hasRole("DB_Admin", "HR_Manager", "Payroll_Officer")) {
            // Employee chỉ xem phiếu lương cá nhân
            columns = new String[]{
                "Kỳ lương", "Lương CB", "Ngày công chuẩn", "Ngày công TT",
                "Tiền công", "Phụ cấp", "Khấu trừ", "Thực nhận", "Trạng thái"
            };
        } else {
            columns = new String[]{
                "Mã NV", "Họ tên", "Phòng ban", "Chức vụ", "Lương CB",
                "NC Chuẩn", "NC Thực tế", "Tiền công", "Phụ cấp", "Khấu trừ", "Thực nhận"
            };
        }

        modelBaoCao = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tblBaoCao = new JTable(modelBaoCao);
        tblBaoCao.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblBaoCao.setRowHeight(25);
        tblBaoCao.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Căn phải cho cột số tiền
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int i = 0; i < tblBaoCao.getColumnCount(); i++) {
            String colName = tblBaoCao.getColumnName(i);
            if (colName.contains("Lương") || colName.contains("Tiền") ||
                colName.contains("Phụ cấp") || colName.contains("Khấu trừ") ||
                colName.contains("Thực nhận") || colName.contains("NC")) {
                tblBaoCao.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
            }
        }

        lblTongThucNhan = new JLabel("Tổng thực nhận: 0");
        lblTongThucNhan.setFont(new Font("Segoe UI", Font.BOLD, 14));

        lblTrangThai = new JLabel("");
        lblTrangThai.setFont(new Font("Segoe UI", Font.BOLD, 13));

        btnChotLuong = new JButton("Chốt bảng lương");
        btnChotLuong.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnChotLuong.setBackground(new Color(220, 53, 69));
        btnChotLuong.setForeground(Color.WHITE);
        btnChotLuong.setFocusPainted(false);

        btnLamMoi = new JButton("Làm mới");
        btnLamMoi.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }

    private void setupLayout() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        boolean isEmployee = session.hasRole("Employee")
                && !session.hasRole("DB_Admin", "HR_Manager", "Payroll_Officer");

        // ─── TOP: Bộ lọc kỳ lương ───────────────────────────────────
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlTop.setBorder(BorderFactory.createTitledBorder(
            isEmployee ? "Phiếu lương cá nhân" : "Báo cáo bảng lương chi tiết"
        ));

        if (!isEmployee) {
            pnlTop.add(new JLabel("Chọn kỳ lương:"));
            pnlTop.add(cboKyLuong);
        }
        pnlTop.add(btnLamMoi);

        // Trạng thái kỳ lương
        pnlTop.add(Box.createHorizontalStrut(20));
        pnlTop.add(lblTrangThai);

        add(pnlTop, BorderLayout.NORTH);

        // ─── CENTER: Bảng dữ liệu ──────────────────────────────────
        JScrollPane scrollPane = new JScrollPane(tblBaoCao);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Chi tiết"));
        add(scrollPane, BorderLayout.CENTER);

        // ─── BOTTOM: Tổng cộng + Nút chốt ──────────────────────────
        JPanel pnlBottom = new JPanel(new BorderLayout(10, 5));
        pnlBottom.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        pnlBottom.add(lblTongThucNhan, BorderLayout.WEST);

        // Nút chốt chỉ hiện cho DB_Admin/Payroll_Officer
        if (session.hasRole("DB_Admin", "Payroll_Officer")) {
            JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            pnlActions.add(btnChotLuong);
            pnlBottom.add(pnlActions, BorderLayout.EAST);
        }

        add(pnlBottom, BorderLayout.SOUTH);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  SỰ KIỆN
    // ═══════════════════════════════════════════════════════════════════

    private void setupEvents() {
        boolean isEmployee = session.hasRole("Employee")
                && !session.hasRole("DB_Admin", "HR_Manager", "Payroll_Officer");

        if (!isEmployee) {
            cboKyLuong.addActionListener(e -> loadChiTietByKyLuong());
        }

        btnLamMoi.addActionListener(e -> {
            if (isEmployee) {
                loadPhieuLuongCaNhan();
            } else {
                loadKyLuong();
            }
        });

        btnChotLuong.addActionListener(e -> handleChotLuong());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  LOAD DỮ LIỆU
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Load danh sách kỳ lương vào ComboBox.
     */
    private void loadKyLuong() {
        boolean isEmployee = session.hasRole("Employee")
                && !session.hasRole("DB_Admin", "HR_Manager", "Payroll_Officer");

        if (isEmployee) {
            loadPhieuLuongCaNhan();
            return;
        }

        SwingWorker<List<BangLuong>, Void> worker = new SwingWorker<List<BangLuong>, Void>() {
            @Override
            protected List<BangLuong> doInBackground() throws Exception {
                return payrollService.getDanhSachBangLuong();
            }

            @Override
            protected void done() {
                try {
                    List<BangLuong> list = get();
                    cboKyLuong.removeAllItems();
                    for (BangLuong bl : list) {
                        cboKyLuong.addItem(bl);
                    }
                    if (!list.isEmpty()) {
                        cboKyLuong.setSelectedIndex(0);
                    } else {
                        modelBaoCao.setRowCount(0);
                        lblTongThucNhan.setText("Tổng thực nhận: 0");
                        lblTrangThai.setText("Chưa có kỳ lương nào.");
                        lblTrangThai.setForeground(Color.GRAY);
                    }
                } catch (Exception ex) {
                    showError("Lỗi tải danh sách kỳ lương: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * Load chi tiết theo kỳ lương được chọn.
     */
    private void loadChiTietByKyLuong() {
        BangLuong selected = (BangLuong) cboKyLuong.getSelectedItem();
        if (selected == null) return;

        // Cập nhật trạng thái
        updateTrangThaiLabel(selected);

        SwingWorker<List<ChiTietBangLuong>, Void> worker = new SwingWorker<List<ChiTietBangLuong>, Void>() {
            @Override
            protected List<ChiTietBangLuong> doInBackground() throws Exception {
                return payrollService.getChiTietByBangLuong(selected.getMaBangLuong());
            }

            @Override
            protected void done() {
                try {
                    List<ChiTietBangLuong> list = get();
                    populateTableAdmin(list);
                } catch (Exception ex) {
                    showError("Lỗi tải chi tiết bảng lương: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * Load phiếu lương cá nhân (Employee).
     */
    private void loadPhieuLuongCaNhan() {
        SwingWorker<List<ChiTietBangLuong>, Void> worker = new SwingWorker<List<ChiTietBangLuong>, Void>() {
            @Override
            protected List<ChiTietBangLuong> doInBackground() throws Exception {
                return payrollService.getPhieuLuongCaNhan();
            }

            @Override
            protected void done() {
                try {
                    List<ChiTietBangLuong> list = get();
                    populateTableEmployee(list);
                } catch (Exception ex) {
                    showError("Lỗi tải phiếu lương: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  POPULATE TABLE
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Điền bảng cho DB_Admin / HR_Manager / Payroll_Officer.
     */
    private void populateTableAdmin(List<ChiTietBangLuong> list) {
        modelBaoCao.setRowCount(0);
        BigDecimal tong = BigDecimal.ZERO;

        for (ChiTietBangLuong ct : list) {
            modelBaoCao.addRow(new Object[]{
                ct.getMaNV(),
                ct.getHoTen(),
                ct.getTenPB(),
                ct.getTenCV(),
                formatMoney(ct.getLuongCoBan()),
                ct.getNgayCongChuan(),
                ct.getNgayCongThucTe(),
                formatMoney(ct.getTienCong()),
                formatMoney(ct.getTongPhuCap()),
                formatMoney(ct.getTongKhauTru()),
                formatMoney(ct.getThucNhan())
            });
            tong = tong.add(ct.getThucNhan() != null ? ct.getThucNhan() : BigDecimal.ZERO);
        }

        lblTongThucNhan.setText("Tổng thực nhận: " + formatMoney(tong) + " đ  |  Số nhân viên: " + list.size());
    }

    /**
     * Điền bảng cho Employee (phiếu lương cá nhân).
     */
    private void populateTableEmployee(List<ChiTietBangLuong> list) {
        modelBaoCao.setRowCount(0);
        BigDecimal tong = BigDecimal.ZERO;

        for (ChiTietBangLuong ct : list) {
            String kyLuong = "T" + ct.getThang() + "/" + ct.getNam();
            modelBaoCao.addRow(new Object[]{
                kyLuong,
                formatMoney(ct.getLuongCoBan()),
                ct.getNgayCongChuan(),
                ct.getNgayCongThucTe(),
                formatMoney(ct.getTienCong()),
                formatMoney(ct.getTongPhuCap()),
                formatMoney(ct.getTongKhauTru()),
                formatMoney(ct.getThucNhan()),
                "DA_CHOT".equals(ct.getTrangThaiBangLuong()) ? "Đã chốt" : "Chưa chốt"
            });
            tong = tong.add(ct.getThucNhan() != null ? ct.getThucNhan() : BigDecimal.ZERO);
        }

        lblTongThucNhan.setText("Tổng lũy kế: " + formatMoney(tong) + " đ  |  Số kỳ: " + list.size());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  CHỐT LƯƠNG
    // ═══════════════════════════════════════════════════════════════════

    private void handleChotLuong() {
        BangLuong selected = (BangLuong) cboKyLuong.getSelectedItem();
        if (selected == null) {
            showError("Vui lòng chọn kỳ lương!");
            return;
        }

        if (selected.isDaChot()) {
            JOptionPane.showMessageDialog(this,
                "Kỳ lương này đã được chốt trước đó!",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn chốt bảng lương: " + selected.getDisplayLabel() + "?\n\n"
            + "Sau khi chốt:\n"
            + "• Không thể sửa hoặc xóa chi tiết lương\n"
            + "• Không thể chốt lại lần nữa",
            "Xác nhận chốt bảng lương",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) return;

        btnChotLuong.setEnabled(false);
        btnChotLuong.setText("Đang chốt...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private Exception error;

            @Override
            protected Void doInBackground() {
                try {
                    payrollService.chotBangLuong(selected.getMaBangLuong());
                } catch (Exception ex) {
                    error = ex;
                }
                return null;
            }

            @Override
            protected void done() {
                btnChotLuong.setEnabled(true);
                btnChotLuong.setText("Chốt bảng lương");

                if (error != null) {
                    showError(error.getMessage());
                } else {
                    JOptionPane.showMessageDialog(BaoCaoPanel.this,
                        "Đã chốt bảng lương " + selected.getDisplayLabel() + " thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadKyLuong(); // Reload để cập nhật trạng thái
                }
            }
        };
        worker.execute();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  UTILITY
    // ═══════════════════════════════════════════════════════════════════

    private void updateTrangThaiLabel(BangLuong bl) {
        if (bl.isDaChot()) {
            lblTrangThai.setText("● ĐÃ CHỐT");
            lblTrangThai.setForeground(new Color(40, 167, 69));
            btnChotLuong.setEnabled(false);
        } else {
            lblTrangThai.setText("○ CHƯA CHỐT");
            lblTrangThai.setForeground(new Color(255, 153, 0));
            btnChotLuong.setEnabled(true);
        }
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0";
        return MONEY_FORMAT.format(amount);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
