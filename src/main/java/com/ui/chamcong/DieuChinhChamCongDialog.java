package com.ui.chamcong;

import com.model.ChamCong;
import com.service.ChamCongService;
import com.session.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Hộp thoại điều chỉnh chi tiết ngày công của một nhân viên trong tháng.
 * Mở trực tiếp từ bảng Tổng hợp chấm công (Drill-down).
 *
 * Chức năng:
 * - Xem toàn bộ nhật ký chấm công của nhân viên trong tháng được chọn
 * - Thêm ngày công bổ sung (đi công tác, quên chấm công)
 * - Cập nhật trạng thái / giờ vào - ra của ngày công bất kỳ
 * - Xóa lượt chấm công bị chấm nhầm
 */
public class DieuChinhChamCongDialog extends JDialog {

    private final int maNV;
    private final String hoTen;
    private final int thang;
    private final int nam;

    private final ChamCongService chamCongService = new ChamCongService();
    private final Session session = Session.getInstance();
    private boolean dataChanged = false;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    // UI
    private JTable tblChiTiet;
    private DefaultTableModel modelChiTiet;

    private JTextField txtMaCC;
    private JSpinner spnNgayCC;
    private JSpinner spnGioVao;
    private JSpinner spnGioRa;
    private JComboBox<String> cboTrangThai;
    private JTextField txtGhiChu;

    private JButton btnThemCong;
    private JButton btnCapNhat;
    private JButton btnXoa;
    private JButton btnLamMoiForm;
    private JButton btnDong;

    public DieuChinhChamCongDialog(Window owner, int maNV, String hoTen, int thang, int nam) {
        super(owner, "Chi tiết & Điều chỉnh ngày công – " + hoTen + " (Tháng " + thang + "/" + nam + ")", ModalityType.APPLICATION_MODAL);
        this.maNV = maNV;
        this.hoTen = hoTen;
        this.thang = thang;
        this.nam = nam;

        initComponents();
        loadData();
        applyPermissions();
    }

    private void initComponents() {
        setSize(850, 600);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(8, 8));

        // ─── 1. NORTH: Header Banner ─────────────────────────────────
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(new Color(30, 58, 138));
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel lblTitle = new JLabel("ĐIỀU CHỈNH CHẤM CÔNG NHÂN SỰ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Nhân viên: " + hoTen + " (Mã NV: " + maNV + ")  |  Kỳ làm việc: Tháng " + thang + "/" + nam);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(224, 231, 255));

        pnlHeader.add(lblTitle, BorderLayout.NORTH);
        pnlHeader.add(lblSub, BorderLayout.SOUTH);
        add(pnlHeader, BorderLayout.NORTH);

        // ─── 2. CENTER: Bảng ngày công & Form thao tác ───────────────
        JPanel pnlCenter = new JPanel(new BorderLayout(6, 6));
        pnlCenter.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        // Bảng dữ liệu chi tiết
        modelChiTiet = new DefaultTableModel(new String[]{
            "Mã CC", "Ngày chấm", "Giờ vào", "Giờ ra", "Trạng thái", "Ghi chú"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblChiTiet = new JTable(modelChiTiet);
        tblChiTiet.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblChiTiet.setRowHeight(24);
        tblChiTiet.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer centerRend = new DefaultTableCellRenderer();
        centerRend.setHorizontalAlignment(JLabel.CENTER);
        tblChiTiet.getColumnModel().getColumn(0).setCellRenderer(centerRend);
        tblChiTiet.getColumnModel().getColumn(1).setCellRenderer(centerRend);
        tblChiTiet.getColumnModel().getColumn(2).setCellRenderer(centerRend);
        tblChiTiet.getColumnModel().getColumn(3).setCellRenderer(centerRend);
        tblChiTiet.getColumnModel().getColumn(4).setCellRenderer(centerRend);

        JScrollPane scrollTable = new JScrollPane(tblChiTiet);
        scrollTable.setBorder(BorderFactory.createTitledBorder("Danh sách ngày công trong tháng " + thang + "/" + nam));

        // Form thao tác bên dưới
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(BorderFactory.createTitledBorder("Thông tin điều chỉnh ngày công"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtMaCC = new JTextField();
        txtMaCC.setEditable(false);

        // Giới hạn ngày trong tháng được chọn
        LocalDate startMonth = LocalDate.of(nam, thang, 1);
        int maxDays = startMonth.lengthOfMonth();
        LocalDate endMonth = LocalDate.of(nam, thang, maxDays);
        if (endMonth.isAfter(LocalDate.now())) {
            endMonth = LocalDate.now();
        }

        Date initDate = Date.from(startMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date minDate = Date.from(startMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date maxDate = Date.from(endMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
        if (minDate.after(maxDate)) minDate = maxDate;

        spnNgayCC = new JSpinner(new SpinnerDateModel(maxDate, minDate, maxDate, Calendar.DAY_OF_MONTH));
        spnNgayCC.setEditor(new JSpinner.DateEditor(spnNgayCC, "yyyy-MM-dd"));

        spnGioVao = createTimeSpinner(8, 0);
        spnGioRa = createTimeSpinner(17, 0);

        cboTrangThai = new JComboBox<>(new String[]{"CO_MAT", "DI_TRE", "VE_SOM", "VANG"});
        txtGhiChu = new JTextField(20);

        // Dòng 0: Mã CC (ẩn/read-only) & Ngày chấm
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        pnlForm.add(new JLabel("Mã CC:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.3;
        pnlForm.add(txtMaCC, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        pnlForm.add(new JLabel("Ngày chấm:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.7;
        pnlForm.add(spnNgayCC, gbc);

        // Dòng 1: Giờ vào & Giờ ra
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        pnlForm.add(new JLabel("Giờ vào:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.3;
        pnlForm.add(spnGioVao, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        pnlForm.add(new JLabel("Giờ ra:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.7;
        pnlForm.add(spnGioRa, gbc);

        // Dòng 2: Trạng thái & Ghi chú
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        pnlForm.add(new JLabel("Trạng thái:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.3;
        pnlForm.add(cboTrangThai, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        pnlForm.add(new JLabel("Ghi chú / Lý do:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.7;
        pnlForm.add(txtGhiChu, gbc);

        // Dòng 3: Nút bấm
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        JPanel pnlBtnForm = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        btnThemCong = new JButton("Thêm ngày công mới");
        btnThemCong.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnThemCong.setBackground(new Color(22, 163, 74));
        btnThemCong.setForeground(Color.WHITE);

        btnCapNhat = new JButton("Cập nhật ngày công");
        btnCapNhat.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCapNhat.setEnabled(false);

        btnXoa = new JButton("Xóa ngày công");
        btnXoa.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnXoa.setForeground(new Color(185, 28, 28));
        btnXoa.setEnabled(false);

        btnLamMoiForm = new JButton("Làm mới form");

        pnlBtnForm.add(btnThemCong);
        pnlBtnForm.add(btnCapNhat);
        pnlBtnForm.add(btnXoa);
        pnlBtnForm.add(btnLamMoiForm);
        pnlForm.add(pnlBtnForm, gbc);

        JSplitPane splitCenter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollTable, pnlForm);
        splitCenter.setResizeWeight(0.55);
        pnlCenter.add(splitCenter, BorderLayout.CENTER);
        add(pnlCenter, BorderLayout.CENTER);

        // ─── 3. SOUTH: Bottom bar ────────────────────────────────────
        JPanel pnlSouth = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        pnlSouth.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        btnDong = new JButton("Đóng & Áp dụng về bảng tổng hợp");
        btnDong.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDong.setPreferredSize(new Dimension(250, 32));
        pnlSouth.add(btnDong);
        add(pnlSouth, BorderLayout.SOUTH);

        // ─── 4. Events ───────────────────────────────────────────────
        tblChiTiet.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblChiTiet.getSelectedRow();
                if (row >= 0) {
                    fillFormFromRow(row);
                }
            }
        });

        btnThemCong.addActionListener(e -> xuLyThemCong());
        btnCapNhat.addActionListener(e -> xuLyCapNhat());
        btnXoa.addActionListener(e -> xuLyXoa());
        btnLamMoiForm.addActionListener(e -> lamMoiForm());
        btnDong.addActionListener(e -> dispose());
    }

    private JSpinner createTimeSpinner(int hour, int minute) {
        SpinnerDateModel model = new SpinnerDateModel();
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "HH:mm");
        spinner.setEditor(editor);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        spinner.setValue(cal.getTime());
        return spinner;
    }

    private void loadData() {
        modelChiTiet.setRowCount(0);
        try {
            List<ChamCong> list = chamCongService.layChamCongTheoThang(maNV, thang, nam);
            for (ChamCong cc : list) {
                modelChiTiet.addRow(new Object[]{
                    cc.getMaChamCong(),
                    cc.getNgayChamCong() != null ? cc.getNgayChamCong().format(dateFormatter) : "",
                    cc.getGioVao() != null ? cc.getGioVao().format(timeFormatter) : "",
                    cc.getGioRa() != null ? cc.getGioRa().format(timeFormatter) : "",
                    cc.getTrangThai(),
                    cc.getGhiChu() != null ? cc.getGhiChu() : ""
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải chi tiết chấm công: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fillFormFromRow(int row) {
        txtMaCC.setText(modelChiTiet.getValueAt(row, 0).toString());
        String ngayStr = modelChiTiet.getValueAt(row, 1).toString();
        try {
            LocalDate ld = LocalDate.parse(ngayStr, dateFormatter);
            spnNgayCC.setValue(Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        } catch (Exception ignored) {}

        String vaoStr = modelChiTiet.getValueAt(row, 2).toString();
        String raStr = modelChiTiet.getValueAt(row, 3).toString();
        try {
            LocalTime ltVao = LocalTime.parse(vaoStr, timeFormatter);
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, ltVao.getHour());
            cal.set(Calendar.MINUTE, ltVao.getMinute());
            spnGioVao.setValue(cal.getTime());
        } catch (Exception ignored) {}

        try {
            if (!raStr.isEmpty()) {
                LocalTime ltRa = LocalTime.parse(raStr, timeFormatter);
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, ltRa.getHour());
                cal.set(Calendar.MINUTE, ltRa.getMinute());
                spnGioRa.setValue(cal.getTime());
            }
        } catch (Exception ignored) {}

        cboTrangThai.setSelectedItem(modelChiTiet.getValueAt(row, 4).toString());
        txtGhiChu.setText(modelChiTiet.getValueAt(row, 5).toString());

        btnCapNhat.setEnabled(session.hasRole("DB_Admin", "HR_Manager"));
        btnXoa.setEnabled(session.hasRole("DB_Admin", "HR_Manager"));
    }

    private void lamMoiForm() {
        txtMaCC.setText("");
        cboTrangThai.setSelectedItem("CO_MAT");
        txtGhiChu.setText("");
        tblChiTiet.clearSelection();
        btnCapNhat.setEnabled(false);
        btnXoa.setEnabled(false);
    }

    private void xuLyThemCong() {
        Date d = (Date) spnNgayCC.getValue();
        LocalDate ngayCC = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        Date vao = (Date) spnGioVao.getValue();
        LocalTime gioVao = vao.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();

        Date ra = (Date) spnGioRa.getValue();
        LocalTime gioRa = ra.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();

        String trangThai = (String) cboTrangThai.getSelectedItem();
        String ghiChu = txtGhiChu.getText().trim();

        ChamCong cc = new ChamCong(maNV, ngayCC, gioVao, gioRa, trangThai, ghiChu.isEmpty() ? "Bổ sung công tác / điều chỉnh" : ghiChu);
        try {
            boolean ok = chamCongService.chamCongDonLe(cc);
            if (ok) {
                dataChanged = true;
                JOptionPane.showMessageDialog(this, "Đã thêm ngày công " + ngayCC + " thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadData();
                lamMoiForm();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi thêm ngày công: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuLyCapNhat() {
        if (txtMaCC.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng ngày công cần cập nhật!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int maCC = Integer.parseInt(txtMaCC.getText());
        Date d = (Date) spnNgayCC.getValue();
        LocalDate ngayCC = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        Date vao = (Date) spnGioVao.getValue();
        LocalTime gioVao = vao.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();

        Date ra = (Date) spnGioRa.getValue();
        LocalTime gioRa = ra.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();

        String trangThai = (String) cboTrangThai.getSelectedItem();
        String ghiChu = txtGhiChu.getText().trim();

        ChamCong cc = new ChamCong(maNV, ngayCC, gioVao, gioRa, trangThai, ghiChu);
        cc.setMaChamCong(maCC);

        try {
            boolean ok = chamCongService.capNhatChamCong(cc);
            if (ok) {
                dataChanged = true;
                JOptionPane.showMessageDialog(this, "Cập nhật ngày công mã " + maCC + " thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadData();
                lamMoiForm();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật ngày công: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuLyXoa() {
        if (txtMaCC.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng ngày công cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int maCC = Integer.parseInt(txtMaCC.getText());
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa lượt chấm công mã " + maCC + " của nhân viên " + hoTen + "?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                chamCongService.xoaChamCong(maCC);
                dataChanged = true;
                JOptionPane.showMessageDialog(this, "Đã xóa ngày công thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadData();
                lamMoiForm();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi xóa ngày công: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public boolean isDataChanged() {
        return dataChanged;
    }

    private void applyPermissions() {
        boolean canManage = session.hasRole("DB_Admin", "HR_Manager");
        btnThemCong.setEnabled(canManage);
        btnLamMoiForm.setEnabled(canManage);
        if (!canManage) {
            btnThemCong.setToolTipText("Chỉ Quản lý nhân sự hoặc Quản trị viên mới được điều chỉnh công.");
        }
    }
}
