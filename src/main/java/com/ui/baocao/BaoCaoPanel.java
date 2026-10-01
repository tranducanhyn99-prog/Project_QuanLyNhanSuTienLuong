package com.ui.baocao;

import com.model.BangLuong;
import com.model.ChiTietBangLuong;
import com.service.PayrollService;
import com.session.Session;
import com.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
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
        UITheme.styleComboBox(cboKyLuong);

        String[] columns;
        if (session.hasRole("Employee") && !session.hasRole("DB_Admin", "HR_Manager", "Payroll_Officer")) {
            columns = new String[]{
                "Kỳ lương", "Lương CB (VNĐ)", "Ngày công chuẩn", "Ngày công TT",
                "Tiền công (VNĐ)", "Phụ cấp (VNĐ)", "Khấu trừ (VNĐ)", "Thực nhận (VNĐ)", "Trạng thái"
            };
        } else {
            columns = new String[]{
                "Mã NV", "Họ tên", "Phòng ban", "Chức vụ", "Lương CB (VNĐ)",
                "NC Chuẩn", "NC Thực tế", "Tiền công (VNĐ)", "Phụ cấp (VNĐ)", "Khấu trừ (VNĐ)", "Thực nhận (VNĐ)"
            };
        }

        modelBaoCao = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tblBaoCao = new JTable(modelBaoCao);
        UITheme.styleTable(tblBaoCao);

        lblTongThucNhan = new JLabel("Tổng thực nhận: 0 VNĐ");
        lblTongThucNhan.setFont(UITheme.FONT_BODY_BOLD);
        lblTongThucNhan.setForeground(UITheme.PRIMARY);

        lblTrangThai = new JLabel("");
        lblTrangThai.setFont(UITheme.FONT_BODY_BOLD);

        btnChotLuong = new JButton("Chốt bảng lương");
        UITheme.styleDangerButton(btnChotLuong);
        btnChotLuong.setPreferredSize(new Dimension(150, 32));

        btnHuyChot = new JButton("Mở lại bảng lương (Hủy chốt)");
        UITheme.stylePrimaryButton(btnHuyChot);
        btnHuyChot.setPreferredSize(new Dimension(215, 32));
        btnHuyChot.setVisible(false);

        btnXoaKyBaoCao = new JButton("Xóa kỳ lương (chưa chốt)");
        UITheme.styleDangerButton(btnXoaKyBaoCao);
        btnXoaKyBaoCao.setPreferredSize(new Dimension(185, 32));

        btnLamMoi = new JButton("Làm mới");
        UITheme.styleSecondaryButton(btnLamMoi);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(0, 10));
        setBackground(UITheme.BG_APP);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        boolean isEmployee = session.hasRole("Employee")
                && !session.hasRole("DB_Admin", "HR_Manager", "Payroll_Officer");

        // ─── TOP: Bộ lọc kỳ lương ───────────────────────────────────
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        pnlTop.setBackground(Color.WHITE);
        pnlTop.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(8, 14, 8, 14)
        ));

        if (!isEmployee) {
            JLabel lblChon = new JLabel("Chọn kỳ lương:");
            lblChon.setFont(UITheme.FONT_BODY);
            pnlTop.add(lblChon);
            pnlTop.add(cboKyLuong);
        }
        pnlTop.add(btnLamMoi);

        // Trạng thái kỳ lương
        pnlTop.add(Box.createHorizontalStrut(20));
        pnlTop.add(lblTrangThai);

        add(pnlTop, BorderLayout.NORTH);

        // ─── CENTER: Bảng dữ liệu ──────────────────────────────────
        JScrollPane scrollPane = new JScrollPane(tblBaoCao);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER, 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // ─── BOTTOM: Tổng cộng + Nút chốt ──────────────────────────
        JPanel pnlBottom = new JPanel(new BorderLayout(10, 0));
        pnlBottom.setBackground(Color.WHITE);
        pnlBottom.setBorder(new CompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(10, 16, 10, 16)
        ));

        pnlBottom.add(lblTongThucNhan, BorderLayout.WEST);

        // Khối thao tác (Chốt, Hủy chốt, Xóa kỳ nháp) chỉ hiện cho DB_Admin/Payroll_Officer
        if (session.hasRole("DB_Admin", "Payroll_Officer")) {
            JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            pnlActions.setOpaque(false);
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
        if (isShowing()) {
            JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
        } else {
            System.err.println("Bỏ qua dialog thông báo lỗi báo cáo: " + message);
        }
    }
}
