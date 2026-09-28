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
    private JButton              btnHuyChot;
    private JButton              btnXoaKyBaoCao;
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

        // Nút Chốt bảng lương: vẽ đồ họa trực tiếp đảm bảo nền ĐỎ nổi bật, chữ trắng sắc nét
        btnChotLuong = new JButton("Chốt bảng lương") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (!isEnabled()) {
                    g2.setColor(new Color(226, 232, 240));
                } else if (getModel().isPressed()) {
                    g2.setColor(new Color(153, 27, 27)); // Crimson đậm khi nhấn
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(185, 28, 28)); // Đỏ sáng khi di chuột
                } else {
                    g2.setColor(new Color(220, 38, 38)); // Đỏ nổi bật
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnChotLuong.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnChotLuong.setForeground(Color.WHITE);
        btnChotLuong.setFocusPainted(false);
        btnChotLuong.setContentAreaFilled(false);
        btnChotLuong.setBorderPainted(false);
        btnChotLuong.setOpaque(false);
        btnChotLuong.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnChotLuong.setPreferredSize(new Dimension(150, 34));

        // Nút Mở lại bảng lương (Hủy chốt): vẽ nền Cam Hổ Phách sang trọng
        btnHuyChot = new JButton("Mở lại bảng lương (Hủy chốt)") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (!isEnabled()) {
                    g2.setColor(new Color(226, 232, 240));
                } else if (getModel().isPressed()) {
                    g2.setColor(new Color(180, 83, 9));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(217, 119, 6));
                } else {
                    g2.setColor(new Color(245, 158, 11));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnHuyChot.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnHuyChot.setForeground(Color.WHITE);
        btnHuyChot.setFocusPainted(false);
        btnHuyChot.setContentAreaFilled(false);
        btnHuyChot.setBorderPainted(false);
        btnHuyChot.setOpaque(false);
        btnHuyChot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnHuyChot.setPreferredSize(new Dimension(215, 34));
        btnHuyChot.setVisible(false);

        // Nút Xóa kỳ lương nháp
        btnXoaKyBaoCao = new JButton("Xóa kỳ lương (chưa chốt)");
        btnXoaKyBaoCao.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnXoaKyBaoCao.setForeground(new Color(185, 28, 28));
        btnXoaKyBaoCao.setPreferredSize(new Dimension(185, 34));
        btnXoaKyBaoCao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

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

        // Khối thao tác (Chốt, Hủy chốt, Xóa kỳ nháp) chỉ hiện cho DB_Admin/Payroll_Officer
        if (session.hasRole("DB_Admin", "Payroll_Officer")) {
            JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            pnlActions.add(btnXoaKyBaoCao);
            pnlActions.add(btnHuyChot);
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
        btnHuyChot.addActionListener(e -> handleHuyChotLuong());
        btnXoaKyBaoCao.addActionListener(e -> handleXoaKyBaoCao());
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

    private void handleHuyChotLuong() {
        BangLuong selected = (BangLuong) cboKyLuong.getSelectedItem();
        if (selected == null) {
            showError("Vui lòng chọn kỳ lương!");
            return;
        }

        if (!selected.isDaChot()) {
            JOptionPane.showMessageDialog(this,
                "Kỳ lương này chưa chốt, không cần mở lại!",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn MỞ LẠI (HỦY CHỐT) bảng lương " + selected.getDisplayLabel() + "?\n\n"
            + "Sau khi mở lại:\n"
            + "• Kỳ lương sẽ trở về trạng thái CHƯA CHỐT (Bản nháp)\n"
            + "• Bạn có thể vào phân hệ Chấm công sửa/xóa ngày công, tính lại lương hoặc xóa kỳ lương này",
            "Xác nhận mở lại bảng lương",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) return;

        btnHuyChot.setEnabled(false);
        btnHuyChot.setText("Đang mở lại...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private Exception error;

            @Override
            protected Void doInBackground() {
                try {
                    payrollService.huyChotBangLuong(selected.getMaBangLuong());
                } catch (Exception ex) {
                    error = ex;
                }
                return null;
            }

            @Override
            protected void done() {
                btnHuyChot.setEnabled(true);
                btnHuyChot.setText("Mở lại bảng lương (Hủy chốt)");

                if (error != null) {
                    showError(error.getMessage());
                } else {
                    JOptionPane.showMessageDialog(BaoCaoPanel.this,
                        "Đã mở lại bảng lương " + selected.getDisplayLabel() + " thành công!\nHiện tại bạn có thể điều chỉnh ngày công và tính lại bảng lương.",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadKyLuong();
                }
            }
        };
        worker.execute();
    }

    private void handleXoaKyBaoCao() {
        BangLuong selected = (BangLuong) cboKyLuong.getSelectedItem();
        if (selected == null) {
            showError("Vui lòng chọn kỳ lương cần xóa!");
            return;
        }

        if (selected.isDaChot()) {
            JOptionPane.showMessageDialog(this,
                "Bảng lương này ĐÃ CHỐT, không thể xóa trực tiếp!\nVui lòng bấm 'Mở lại bảng lương (Hủy chốt)' trước nếu muốn điều chỉnh.",
                "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn XÓA bảng lương " + selected.getDisplayLabel() + " (trạng thái CHƯA CHỐT)?\n\n"
            + "Toàn bộ chi tiết lương nháp của tháng này sẽ được xóa để bạn có thể kiểm tra lại dữ liệu và tính lại từ đầu.",
            "Xác nhận xóa bảng lương",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) return;

        try {
            payrollService.xoaBangLuong(selected.getMaBangLuong());
            JOptionPane.showMessageDialog(this, "Đã xóa bảng lương " + selected.getDisplayLabel() + " thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadKyLuong();
        } catch (Exception ex) {
            showError("Lỗi khi xóa bảng lương: " + ex.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  UTILITY
    // ═══════════════════════════════════════════════════════════════════

    private void updateTrangThaiLabel(BangLuong bl) {
        boolean canManage = session.hasRole("DB_Admin", "Payroll_Officer");
        if (bl.isDaChot()) {
            lblTrangThai.setText("● ĐÃ CHỐT");
            lblTrangThai.setForeground(new Color(22, 163, 74));
            if (btnChotLuong != null) btnChotLuong.setVisible(false);
            if (btnHuyChot != null) {
                btnHuyChot.setVisible(true);
                btnHuyChot.setEnabled(canManage);
            }
            if (btnXoaKyBaoCao != null) btnXoaKyBaoCao.setVisible(false);
        } else {
            lblTrangThai.setText("○ CHƯA CHỐT (Bản nháp)");
            lblTrangThai.setForeground(new Color(217, 119, 6));
            if (btnChotLuong != null) {
                btnChotLuong.setVisible(true);
                btnChotLuong.setEnabled(canManage);
            }
            if (btnHuyChot != null) btnHuyChot.setVisible(false);
            if (btnXoaKyBaoCao != null) {
                btnXoaKyBaoCao.setVisible(true);
                btnXoaKyBaoCao.setEnabled(canManage);
            }
        }
        revalidate();
        repaint();
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0";
        return MONEY_FORMAT.format(amount);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
