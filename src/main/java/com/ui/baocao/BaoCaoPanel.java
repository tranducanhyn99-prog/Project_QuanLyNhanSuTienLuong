package com.ui.baocao;

import com.model.BangLuong;
import com.model.ChiTietBangLuong;
import com.service.PayrollService;
import com.session.Session;
import com.ui.theme.UITheme;
import com.ui.theme.DatabaseTask;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
    private boolean loadingPeriods;

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
        cboKyLuong.setPreferredSize(new Dimension(285, 38));
        cboKyLuong.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean selected, boolean focus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof BangLuong) {
                    BangLuong period = (BangLuong) value;
                    label.setText("Tháng " + period.getThang() + "/" + period.getNam()
                            + (period.isDaChot() ? "  ·  Đã chốt" : "  ·  Bản nháp"));
                }
                label.setBorder(new EmptyBorder(6, 10, 6, 10));
                return label;
            }
        });

        String[] columns;
        if (session.hasRole("Employee") && !session.hasRole("DB_Admin", "HR_Manager", "Payroll_Officer")) {
            columns = new String[]{
                "Kỳ lương", "Lương cơ bản (VNĐ)", "Công chuẩn", "Công thực tế",
                "Tiền công (VNĐ)", "Phụ cấp (VNĐ)", "Khấu trừ (VNĐ)", "Thực nhận (VNĐ)", "Trạng thái"
            };
        } else {
            columns = new String[]{
                "Mã NV", "Họ tên", "Phòng ban", "Chức vụ", "Lương cơ bản (VNĐ)",
                "Công chuẩn", "Công thực tế", "Tiền công (VNĐ)", "Phụ cấp (VNĐ)", "Khấu trừ (VNĐ)", "Thực nhận (VNĐ)"
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
        tblBaoCao.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < columns.length; i++) {
            int width = columns[i].contains("VNĐ") ? 165 : 120;
            if ("Họ tên".equals(columns[i])) width = 195;
            if ("Mã NV".equals(columns[i])) width = 80;
            tblBaoCao.getColumnModel().getColumn(i).setPreferredWidth(width);
        }

        lblTongThucNhan = new JLabel("Tổng thực nhận: 0 VNĐ");
        lblTongThucNhan.setFont(UITheme.FONT_TITLE_LARGE);
        lblTongThucNhan.setForeground(UITheme.PRIMARY);

        lblTrangThai = UITheme.createBadge("", UITheme.TEXT_MUTED, UITheme.BG_CARD, UITheme.BORDER);
        lblTrangThai.setFont(UITheme.FONT_CAPTION_BOLD);

        btnChotLuong = new JButton("Chốt bảng lương");
        UITheme.stylePrimaryButton(btnChotLuong);
        btnChotLuong.setIcon(UITheme.icon("check", 16, Color.WHITE));

        btnHuyChot = new JButton("Mở lại bảng lương");
        UITheme.styleSecondaryButton(btnHuyChot);
        btnHuyChot.setIcon(UITheme.icon("edit", 16, UITheme.TEXT_MAIN));
        btnHuyChot.setVisible(false);

        btnXoaKyBaoCao = new JButton("Xóa kỳ nháp");
        UITheme.styleDangerButton(btnXoaKyBaoCao);
        btnXoaKyBaoCao.setIcon(UITheme.icon("trash", 16, UITheme.DANGER_TEXT));

        btnLamMoi = new JButton("Làm mới");
        UITheme.styleSecondaryButton(btnLamMoi);
        btnLamMoi.setIcon(UITheme.icon("refresh", 16, UITheme.TEXT_MAIN));
    }

    private void setupLayout() {
        setLayout(new BorderLayout(0, 18));
        setBackground(UITheme.BG_APP);
        setBorder(new EmptyBorder(22, 24, 22, 24));

        boolean isEmployee = session.hasRole("Employee")
                && !session.hasRole("DB_Admin", "HR_Manager", "Payroll_Officer");

        // ─── TOP: Bộ lọc kỳ lương ───────────────────────────────────
        JPanel pnlTop = UITheme.createCardPanel();
        pnlTop.setLayout(new BorderLayout(10, 0));
        pnlTop.setBorder(new EmptyBorder(14, 16, 14, 16));
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filters.setOpaque(false);

        if (!isEmployee) {
            JLabel lblChon = new JLabel("Chọn kỳ lương:");
            lblChon.setFont(UITheme.FONT_BODY);
            filters.add(lblChon);
            filters.add(cboKyLuong);
        }
        filters.add(btnLamMoi);
        pnlTop.add(filters, BorderLayout.WEST);

        // Trạng thái kỳ lương
        if (!isEmployee) pnlTop.add(lblTrangThai, BorderLayout.EAST);

        JPanel heading = new JPanel(new BorderLayout(0, 16));
        heading.setOpaque(false);
        heading.add(UITheme.createPageHeader(isEmployee ? "Phiếu lương của tôi" : "Báo cáo tiền lương",
                isEmployee ? "Theo dõi thu nhập, phụ cấp và lịch sử thanh toán của bạn."
                        : "Kiểm tra chi tiết thu nhập và hoàn tất bảng lương theo kỳ."), BorderLayout.NORTH);
        heading.add(pnlTop, BorderLayout.CENTER);
        add(heading, BorderLayout.NORTH);

        // ─── CENTER: Bảng dữ liệu ──────────────────────────────────
        JScrollPane scrollPane = new JScrollPane(tblBaoCao);
        UITheme.styleScrollPane(scrollPane);
        add(scrollPane, BorderLayout.CENTER);

        // ─── BOTTOM: Tổng cộng + Nút chốt ──────────────────────────
        JPanel pnlBottom = UITheme.createCardPanel();
        pnlBottom.setLayout(new BorderLayout(10, 12));
        pnlBottom.setBorder(new EmptyBorder(16, 18, 16, 18));

        pnlBottom.add(lblTongThucNhan, BorderLayout.NORTH);

        // Khối thao tác (Chốt, Hủy chốt, Xóa kỳ nháp) chỉ hiện cho DB_Admin/Payroll_Officer
        if (session.hasRole("DB_Admin", "Payroll_Officer")) {
            JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            pnlActions.setOpaque(false);
            pnlActions.add(btnXoaKyBaoCao);
            pnlActions.add(btnHuyChot);
            pnlActions.add(btnChotLuong);
            pnlBottom.add(pnlActions, BorderLayout.SOUTH);
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
            cboKyLuong.addActionListener(e -> { if (!loadingPeriods) loadChiTietByKyLuong(); });
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

        BangLuong previous = (BangLuong) cboKyLuong.getSelectedItem();
        clearPeriodDetails("Đang tải danh sách kỳ lương...");
        DatabaseTask.run(cboKyLuong, () -> payrollService.getDanhSachBangLuong(), list -> {
            loadingPeriods = true;
            try {
                cboKyLuong.removeAllItems();
                for (BangLuong bl : list) cboKyLuong.addItem(bl);
                if (previous != null) {
                    for (BangLuong bl : list) {
                        if (bl.getMaBangLuong() == previous.getMaBangLuong()) cboKyLuong.setSelectedItem(bl);
                    }
                }
            } finally {
                loadingPeriods = false;
            }
            loadChiTietByKyLuong();
        });
    }

    private void loadChiTietByKyLuong() {
        BangLuong selected = (BangLuong) cboKyLuong.getSelectedItem();
        clearPeriodDetails(selected == null ? "Chưa có kỳ lương nào." : "Đang tải " + selected.getDisplayLabel() + "...");
        if (selected == null) return;
        DatabaseTask.run(tblBaoCao, () -> payrollService.getChiTietByBangLuong(selected.getMaBangLuong()), list -> {
            BangLuong current = (BangLuong) cboKyLuong.getSelectedItem();
            if (current != null && current.getMaBangLuong() == selected.getMaBangLuong()) {
                populateTableAdmin(list);
                updateTrangThaiLabel(current);
            }
        });
    }

    private void clearPeriodDetails(String status) {
        DatabaseTask.invalidate(tblBaoCao);
        modelBaoCao.setRowCount(0);
        lblTongThucNhan.setText("Tổng thực nhận: 0 VNĐ");
        lblTrangThai.setText(status.startsWith("Đang tải") ? "Đang tải..." : status);
        lblTrangThai.setToolTipText(status);
        lblTrangThai.setForeground(UITheme.TEXT_MUTED);
        lblTrangThai.setBackground(UITheme.BG_CARD);
        lblTrangThai.setOpaque(false);
        lblTrangThai.setBorder(null);
        btnChotLuong.setEnabled(false);
        btnHuyChot.setEnabled(false);
        btnXoaKyBaoCao.setEnabled(false);
    }

    private void loadPhieuLuongCaNhan() {
        DatabaseTask.run(tblBaoCao, () -> payrollService.getPhieuLuongCaNhan(), this::populateTableEmployee);
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

        lblTongThucNhan.setText("Tổng thực nhận: " + formatMoney(tong) + " đ  ·  " + list.size() + " nhân viên");
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

        lblTongThucNhan.setText("Thu nhập lũy kế: " + formatMoney(tong) + " đ  ·  " + list.size() + " kỳ lương");
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

        DatabaseTask.runExclusive(this, () -> { payrollService.chotBangLuong(selected.getMaBangLuong()); return true; }, ok -> {
            JOptionPane.showMessageDialog(this, "Đã chốt bảng lương.");
            loadKyLuong();
        });
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

        DatabaseTask.runExclusive(this, () -> { payrollService.huyChotBangLuong(selected.getMaBangLuong()); return true; }, ok -> {
            JOptionPane.showMessageDialog(this, "Đã mở lại bảng lương.");
            loadKyLuong();
        });
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
            DatabaseTask.runExclusive(this, () -> { payrollService.xoaBangLuong(selected.getMaBangLuong()); return true; }, ignored -> {
            JOptionPane.showMessageDialog(this, "Đã xóa bảng lương " + selected.getDisplayLabel() + " thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadKyLuong();
                    });
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
            lblTrangThai.setText("Đã chốt");
            lblTrangThai.setForeground(UITheme.SUCCESS_TEXT);
            lblTrangThai.setBackground(UITheme.SUCCESS_BG);
            if (btnChotLuong != null) btnChotLuong.setVisible(false);
            if (btnHuyChot != null) {
                btnHuyChot.setVisible(true);
                btnHuyChot.setEnabled(canManage);
            }
            if (btnXoaKyBaoCao != null) btnXoaKyBaoCao.setVisible(false);
        } else {
            lblTrangThai.setText("Bản nháp");
            lblTrangThai.setForeground(UITheme.WARNING_TEXT);
            lblTrangThai.setBackground(UITheme.WARNING_BG);
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
        lblTrangThai.setOpaque(false);
        lblTrangThai.setHorizontalAlignment(SwingConstants.CENTER);
        lblTrangThai.setBorder(new EmptyBorder(7, 12, 7, 12));
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
