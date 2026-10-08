package com.ui.chamcong;

import com.model.ChamCong;
import com.model.NhanVien;
import com.model.TongHopChamCong;
import com.service.ChamCongService;
import com.service.NhanVienService;
import com.session.Session;
import com.ui.theme.UITheme;
import com.ui.theme.DatabaseTask;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Giao diện Quản lý Chấm công & Xem tổng hợp chấm công tháng.
 * Tích hợp kiến trúc 3 tầng: TV2 (DAO, Service, SP, View) và TV5 (Session, Phân quyền RBAC).
 */
public class ChamCongPanel extends JPanel {

    private final ChamCongService chamCongService = new ChamCongService();
    private final NhanVienService nhanVienService = new NhanVienService();
    private final Session session = Session.getInstance();

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    // UI Components
    private JTabbedPane tabbedPane;

    // Tab 1: Nhập & danh sách chấm công
    private JComboBox<NhanVienItem> cboNhanVien;
    private JSpinner spnNgayCC;
    private JSpinner spnGioVao;
    private JSpinner spnGioRa;
    private JComboBox<String> cboTrangThai;
    private JTextField txtGhiChu;
    private JButton btnGhiNhan;
    private JButton btnLamMoiForm;
    private JButton btnDiemDanhHangLoat;

    private JSpinner spnThangLoc;
    private JSpinner spnNamLoc;
    private JButton btnTaiLaiChiTiet;
    private JButton btnXoaChamCong;
    private JTable tblChiTiet;
    private DefaultTableModel modelChiTiet;

    // Tab 2: Tổng hợp chấm công tháng
    private JSpinner spnThangTongHop;
    private JSpinner spnNamTongHop;
    private JButton btnXemTongHop;
    private JButton btnDieuChinhCong;
    private JTable tblTongHop;
    private DefaultTableModel modelTongHop;
    private JLabel lblTongHopThongKe;

    public ChamCongPanel() {
        this(false);
    }

    public ChamCongPanel(boolean selectSummaryTab) {
        initComponents();
        applySecurityPermissions();
        loadNhanVienComboBox();
        loadDuLieuChiTiet();
        loadDuLieuTongHop();

        if (selectSummaryTab && tabbedPane.getTabCount() > 1) {
            tabbedPane.setSelectedIndex(1);
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_APP);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        add(UITheme.createPageHeader("Chấm công",
                "Theo dõi ngày làm việc, giờ vào ra và tổng hợp công theo tháng."), BorderLayout.NORTH);
        tabbedPane = new JTabbedPane();
        UITheme.styleTabbedPane(tabbedPane);

        JPanel pnlTab1 = new JPanel(new BorderLayout(0, 14));
        pnlTab1.setBackground(UITheme.BG_APP);
        pnlTab1.setBorder(new EmptyBorder(14, 0, 0, 0));

        JPanel inputCard = UITheme.createCardPanel();
        inputCard.setLayout(new BorderLayout(0, 10));
        inputCard.add(UITheme.createSectionHeader("Ghi nhận ngày công"), BorderLayout.NORTH);
        JPanel inputs = new JPanel(new GridLayout(2, 3, 16, 10));
        inputs.setOpaque(false);
        cboNhanVien = new JComboBox<>();
        UITheme.styleComboBox(cboNhanVien);
        spnNgayCC = new JSpinner(new SpinnerDateModel(new Date(), null, new Date(), java.util.Calendar.DAY_OF_MONTH));
        spnNgayCC.setEditor(new JSpinner.DateEditor(spnNgayCC, "yyyy-MM-dd"));
        UITheme.styleSpinner(spnNgayCC);
        spnGioVao = createTimeSpinner(8, 0);
        spnGioRa = createTimeSpinner(17, 0);
        cboTrangThai = new JComboBox<>(new String[]{"CO_MAT", "DI_TRE", "VE_SOM", "VANG"});
        UITheme.styleComboBox(cboTrangThai);
        txtGhiChu = new JTextField();
        UITheme.styleTextField(txtGhiChu);
        inputs.add(field("Nhân viên *", cboNhanVien));
        inputs.add(field("Ngày chấm công · yyyy-mm-dd", spnNgayCC));
        inputs.add(field("Trạng thái", cboTrangThai));
        inputs.add(field("Giờ vào · HH:mm", spnGioVao));
        inputs.add(field("Giờ ra · HH:mm", spnGioRa));
        inputs.add(field("Ghi chú", txtGhiChu));
        inputCard.add(inputs, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        btnGhiNhan = new JButton("Ghi nhận chấm công", UITheme.icon("check", 16, Color.WHITE));
        UITheme.stylePrimaryButton(btnGhiNhan);
        btnLamMoiForm = new JButton("Làm mới");
        UITheme.styleSecondaryButton(btnLamMoiForm);
        btnDiemDanhHangLoat = new JButton("Điểm danh hàng loạt");
        UITheme.styleSecondaryButton(btnDiemDanhHangLoat);
        btnDiemDanhHangLoat.setToolTipText("Ghi nhận giờ làm 08:00–17:00 hôm nay cho toàn bộ nhân viên đang làm việc");
        actions.add(btnLamMoiForm);
        actions.add(btnDiemDanhHangLoat);
        actions.add(btnGhiNhan);
        inputCard.add(actions, BorderLayout.SOUTH);
        pnlTab1.add(inputCard, BorderLayout.NORTH);

        JPanel journalCard = UITheme.createCardPanel();
        journalCard.setLayout(new BorderLayout(0, 10));
        JPanel journalHeader = new JPanel(new BorderLayout(12, 0));
        journalHeader.setOpaque(false);
        journalHeader.add(UITheme.createSectionHeader("Nhật ký chấm công"), BorderLayout.WEST);
        JPanel journalFilters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        journalFilters.setOpaque(false);
        LocalDate now = LocalDate.now();
        spnThangLoc = numberSpinner(now.getMonthValue(), 1, 12, 64);
        spnNamLoc = numberSpinner(now.getYear(), 2020, 2100, 86);
        btnTaiLaiChiTiet = new JButton("Tải lại", UITheme.icon("refresh", 16, UITheme.TEXT_MAIN));
        UITheme.styleSecondaryButton(btnTaiLaiChiTiet);
        btnXoaChamCong = new JButton("Xóa", UITheme.icon("trash", 16, UITheme.DANGER_TEXT));
        UITheme.styleDangerButton(btnXoaChamCong);
        journalFilters.add(label("Tháng"));
        journalFilters.add(spnThangLoc);
        journalFilters.add(label("Năm"));
        journalFilters.add(spnNamLoc);
        journalFilters.add(btnTaiLaiChiTiet);
        journalFilters.add(btnXoaChamCong);
        journalHeader.add(journalFilters, BorderLayout.EAST);
        journalCard.add(journalHeader, BorderLayout.NORTH);

        modelChiTiet = new DefaultTableModel(new String[]{
            "Mã CC", "Mã NV", "Họ và tên", "Ngày chấm", "Giờ vào", "Giờ ra", "Trạng thái", "Ghi chú"
        }, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblChiTiet = new JTable(modelChiTiet);
        UITheme.styleTable(tblChiTiet);
        tblChiTiet.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        configureColumns(tblChiTiet, new int[]{70, 70, 190, 120, 85, 85, 120, 220});
        JScrollPane journalScroll = new JScrollPane(tblChiTiet);
        UITheme.styleScrollPane(journalScroll);
        journalCard.add(journalScroll, BorderLayout.CENTER);
        pnlTab1.add(journalCard, BorderLayout.CENTER);

        JPanel pnlTab2 = new JPanel(new BorderLayout(0, 14));
        pnlTab2.setBackground(UITheme.BG_APP);
        pnlTab2.setBorder(new EmptyBorder(14, 0, 0, 0));
        JPanel summaryFilters = UITheme.createCardPanel();
        summaryFilters.setLayout(new BorderLayout(12, 0));
        summaryFilters.add(UITheme.createSectionHeader("Kỳ chấm công"), BorderLayout.WEST);
        JPanel periodActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        periodActions.setOpaque(false);
        spnThangTongHop = numberSpinner(now.getMonthValue(), 1, 12, 64);
        spnNamTongHop = numberSpinner(now.getYear(), 2020, 2100, 86);
        btnXemTongHop = new JButton("Xem tổng hợp");
        UITheme.stylePrimaryButton(btnXemTongHop);
        btnDieuChinhCong = new JButton("Điều chỉnh công", UITheme.icon("edit", 16, UITheme.TEXT_MAIN));
        UITheme.styleSecondaryButton(btnDieuChinhCong);
        btnDieuChinhCong.setToolTipText("Chọn nhân viên để xem và điều chỉnh chi tiết ngày công trong tháng");
        periodActions.add(label("Tháng"));
        periodActions.add(spnThangTongHop);
        periodActions.add(label("Năm"));
        periodActions.add(spnNamTongHop);
        periodActions.add(btnXemTongHop);
        periodActions.add(btnDieuChinhCong);
        summaryFilters.add(periodActions, BorderLayout.EAST);
        pnlTab2.add(summaryFilters, BorderLayout.NORTH);

        JPanel summaryCard = UITheme.createCardPanel();
        summaryCard.setLayout(new BorderLayout(0, 10));
        summaryCard.add(UITheme.createSectionHeader("Tổng hợp công theo nhân viên"), BorderLayout.NORTH);
        modelTongHop = new DefaultTableModel(new String[]{
            "Mã NV", "Họ và tên", "Tháng", "Năm", "Ngày đi làm", "Lần đi trễ", "Lần về sớm", "Ngày vắng", "Tổng giờ làm"
        }, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblTongHop = new JTable(modelTongHop);
        UITheme.styleTable(tblTongHop);
        tblTongHop.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        configureColumns(tblTongHop, new int[]{70, 200, 75, 85, 115, 115, 115, 115, 140});
        JScrollPane summaryScroll = new JScrollPane(tblTongHop);
        UITheme.styleScrollPane(summaryScroll);
        summaryCard.add(summaryScroll, BorderLayout.CENTER);
        lblTongHopThongKe = new JLabel("Chưa có dữ liệu chấm công");
        lblTongHopThongKe.setFont(UITheme.FONT_CAPTION);
        lblTongHopThongKe.setForeground(UITheme.TEXT_MUTED);
        summaryCard.add(lblTongHopThongKe, BorderLayout.SOUTH);
        pnlTab2.add(summaryCard, BorderLayout.CENTER);

        tabbedPane.addTab("Nhật ký chấm công", UITheme.icon("clock", 16, UITheme.TEXT_MUTED), pnlTab1);
        tabbedPane.addTab("Tổng hợp theo tháng", UITheme.icon("calendar", 16, UITheme.TEXT_MUTED), pnlTab2);
        add(tabbedPane, BorderLayout.CENTER);

        // Events
        btnGhiNhan.addActionListener(e -> xuLyGhiNhanChamCong());
        btnLamMoiForm.addActionListener(e -> lamMoiForm());
        btnDiemDanhHangLoat.addActionListener(e -> xuLyDiemDanhHangLoat());
        btnTaiLaiChiTiet.addActionListener(e -> loadDuLieuChiTiet());
        btnXoaChamCong.addActionListener(e -> xuLyXoaChamCong());
        btnXemTongHop.addActionListener(e -> loadDuLieuTongHop());
        spnThangLoc.addChangeListener(e -> loadDuLieuChiTiet());
        spnNamLoc.addChangeListener(e -> loadDuLieuChiTiet());
        spnThangTongHop.addChangeListener(e -> loadDuLieuTongHop());
        spnNamTongHop.addChangeListener(e -> loadDuLieuTongHop());
        btnDieuChinhCong.addActionListener(e -> moDialogDieuChinhCong());
        tblTongHop.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    moDialogDieuChinhCong();
                }
            }
        });
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UITheme.FONT_CAPTION_BOLD);
        label.setForeground(UITheme.TEXT_MUTED);
        return label;
    }

    private JPanel field(String text, JComponent input) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        JLabel label = label(text);
        label.setLabelFor(input);
        panel.add(label, BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);
        return panel;
    }

    private JSpinner numberSpinner(int value, int minimum, int maximum, int width) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, minimum, maximum, 1));
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "#"));
        UITheme.styleSpinner(spinner);
        spinner.setPreferredSize(new Dimension(width, 36));
        return spinner;
    }

    private void configureColumns(JTable table, int[] widths) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    private JSpinner createTimeSpinner(int hour, int minute) {
        SpinnerDateModel model = new SpinnerDateModel();
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "HH:mm");
        spinner.setEditor(editor);

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, hour);
        cal.set(java.util.Calendar.MINUTE, minute);
        cal.set(java.util.Calendar.SECOND, 0);
        spinner.setValue(cal.getTime());
        UITheme.styleSpinner(spinner);
        return spinner;
    }

    private void loadNhanVienComboBox() {
        cboNhanVien.removeAllItems();
        DatabaseTask.run(cboNhanVien, () -> nhanVienService.layDanhSachNhanVien(), list -> {
            for (NhanVien nv : list) {
                if ("DANG_LAM_VIEC".equals(nv.getTrangThai())) {
                    cboNhanVien.addItem(new NhanVienItem(nv.getMaNV(), nv.getHoTen()));
                }
            }
        });
    }

    private void loadDuLieuChiTiet() {
        modelChiTiet.setRowCount(0);
        int thang = (Integer) spnThangLoc.getValue();
        int nam = (Integer) spnNamLoc.getValue();
        DatabaseTask.run(tblChiTiet, () -> chamCongService.layDanhSachChamCongTheoThang(thang, nam), list -> {
            for (ChamCong cc : list) {
                modelChiTiet.addRow(new Object[]{
                    cc.getMaChamCong(),
                    cc.getMaNV(),
                    cc.getHoTen(),
                    cc.getNgayChamCong() != null ? cc.getNgayChamCong().format(dateFormatter) : "",
                    cc.getGioVao() != null ? cc.getGioVao().format(timeFormatter) : "",
                    cc.getGioRa() != null ? cc.getGioRa().format(timeFormatter) : "",
                    cc.getTrangThai(),
                    cc.getGhiChu() != null ? cc.getGhiChu() : ""
                });
            }
        });
    }

    private void loadDuLieuTongHop() {
        modelTongHop.setRowCount(0);
        int thang = (Integer) spnThangTongHop.getValue();
        int nam = (Integer) spnNamTongHop.getValue();
        DatabaseTask.run(tblTongHop, () -> chamCongService.layTongHopChamCongThang(thang, nam), list -> {
            for (TongHopChamCong th : list) {
                modelTongHop.addRow(new Object[]{
                    th.getMaNV(),
                    th.getHoTen(),
                    th.getThang(),
                    th.getNam(),
                    th.getSoNgayDiLam(),
                    th.getSoLanDiTre(),
                    th.getSoLanVeSom(),
                    th.getSoNgayVang(),
                    String.format("%.2f", th.getTongSoGioLam())
                });
            }
            lblTongHopThongKe.setText("Tổng số nhân sự có dữ liệu chấm công: " + list.size()
                    + " (Tháng " + thang + "/" + nam + ")");
        });
    }

    private void xuLyGhiNhanChamCong() {
        NhanVienItem nvItem = (NhanVienItem) cboNhanVien.getSelectedItem();
        if (nvItem == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Date dateVal = (Date) spnNgayCC.getValue();
            LocalDate ngayCC = dateVal.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            Date dateGioVao = (Date) spnGioVao.getValue();
            LocalTime gioVao = dateGioVao.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();

            LocalTime gioRa = null;
            if (spnGioRa.getValue() != null) {
                Date dateGioRa = (Date) spnGioRa.getValue();
                gioRa = dateGioRa.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
            }

            String trangThai = (String) cboTrangThai.getSelectedItem();
            String ghiChu = txtGhiChu.getText().trim();

            ChamCong cc = new ChamCong(nvItem.getMaNV(), ngayCC, gioVao, gioRa, trangThai, ghiChu);
            DatabaseTask.runExclusive(this, () -> chamCongService.chamCongDonLe(cc), success -> {

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Ghi nhận chấm công thành công cho nhân viên: " + nvItem.getHoTen()
                                + "\nMã chấm công tự sinh: " + cc.getMaChamCong(),
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                lamMoiForm();
                loadDuLieuChiTiet();
                loadDuLieuTongHop();
            } else {
                JOptionPane.showMessageDialog(this, "Ghi nhận không thành công!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
                    });
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể ghi nhận chấm công: " + ex.getMessage(),
                    "Không thể ghi nhận chấm công", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuLyDiemDanhHangLoat() {
        if(JOptionPane.showConfirmDialog(this,"Điểm danh toàn bộ nhân viên hôm nay?","Xác nhận",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
        DatabaseTask.runExclusive(this, () -> {
            List<ChamCong> rows=new ArrayList<>();
            for(NhanVien nv:nhanVienService.layDanhSachNhanVien()) {
                if("DANG_LAM_VIEC".equals(nv.getTrangThai()) && !nv.getNgayVaoLam().isAfter(LocalDate.now()))
                    rows.add(new ChamCong(nv.getMaNV(),LocalDate.now(),LocalTime.of(8,0),LocalTime.of(17,0),"CO_MAT","Điểm danh hàng loạt"));
            }
            chamCongService.nhapChamCongTheoLo(rows); return rows.size();
        }, count -> { JOptionPane.showMessageDialog(this,"Đã ghi nhận "+count+" nhân viên."); loadDuLieuChiTiet(); loadDuLieuTongHop(); });
    }

    private void lamMoiForm() {
        if (cboNhanVien.getItemCount() > 0) {
            cboNhanVien.setSelectedIndex(0);
        }
        spnNgayCC.setValue(new Date());
        cboTrangThai.setSelectedItem("CO_MAT");
        txtGhiChu.setText("");
    }

    private void xuLyXoaChamCong() {
        int selectedRow = tblChiTiet.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng chấm công cần xóa trong bảng nhật ký!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int maCC = Integer.parseInt(modelChiTiet.getValueAt(selectedRow, 0).toString());
        String hoTen = modelChiTiet.getValueAt(selectedRow, 2).toString();
        String ngayCC = modelChiTiet.getValueAt(selectedRow, 3).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa lượt chấm công:\n"
            + "• Nhân viên: " + hoTen + "\n"
            + "• Ngày: " + ngayCC + "\n"
            + "• Mã CC: " + maCC + "\n\n"
            + "Lưu ý: Sau khi xóa ngày công, bạn có thể vào tab 'Tính bảng lương' để tính lại nhằm cập nhật số liệu mới!",
            "Xác nhận xóa chấm công",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                DatabaseTask.runExclusive(this, () -> { chamCongService.xoaChamCong(maCC); return true; }, ignored -> {
                JOptionPane.showMessageDialog(this, "Đã xóa lượt chấm công thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadDuLieuChiTiet();
                loadDuLieuTongHop();
                            });
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa chấm công: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void moDialogDieuChinhCong() {
        int selectedRow = tblTongHop.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn một nhân viên từ bảng danh sách tổng hợp để xem hoặc điều chỉnh chi tiết ngày công!",
                    "Chưa chọn nhân viên", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int maNV = Integer.parseInt(modelTongHop.getValueAt(selectedRow, 0).toString());
        String hoTen = modelTongHop.getValueAt(selectedRow, 1).toString();
        int thang = Integer.parseInt(modelTongHop.getValueAt(selectedRow, 2).toString());
        int nam = Integer.parseInt(modelTongHop.getValueAt(selectedRow, 3).toString());

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        DieuChinhChamCongDialog dialog = new DieuChinhChamCongDialog(parentWindow, maNV, hoTen, thang, nam);
        dialog.setVisible(true);

        if (dialog.isDataChanged()) {
            loadDuLieuTongHop();
            loadDuLieuChiTiet();
        }
    }

    /**
     * Phân quyền RBAC theo quy chuẩn Session TV5:
     * - DB_Admin, HR_Manager: Full quyền ghi nhận, điểm danh lô, xóa công, điều chỉnh chi tiết ngày công.
     * - Payroll_Officer: Chỉ xem để đối soát lương, khóa các nút thêm mới / xóa / điều chỉnh.
     * - Employee: Chế độ chỉ xem.
     */
    private void applySecurityPermissions() {
        boolean canManage = session.hasRole("DB_Admin", "HR_Manager");
        btnGhiNhan.setEnabled(canManage);
        btnDiemDanhHangLoat.setEnabled(canManage);
        btnLamMoiForm.setEnabled(canManage);
        if (btnXoaChamCong != null) btnXoaChamCong.setEnabled(canManage);
        if (btnDieuChinhCong != null) btnDieuChinhCong.setEnabled(canManage);
        cboNhanVien.setEnabled(canManage);
        spnNgayCC.setEnabled(canManage);
        spnGioVao.setEnabled(canManage);
        spnGioRa.setEnabled(canManage);
        cboTrangThai.setEnabled(canManage);
        txtGhiChu.setEnabled(canManage);

        if (!canManage) {
            btnGhiNhan.setToolTipText("Chỉ Quản lý nhân sự hoặc Quản trị viên mới được ghi nhận chấm công.");
            btnDiemDanhHangLoat.setToolTipText("Chỉ Quản lý nhân sự hoặc Quản trị viên mới được điểm danh hàng loạt.");
            if (btnXoaChamCong != null) btnXoaChamCong.setToolTipText("Chỉ Quản lý nhân sự hoặc Quản trị viên mới được xóa chấm công.");
            if (btnDieuChinhCong != null) btnDieuChinhCong.setToolTipText("Chỉ Quản lý nhân sự hoặc Quản trị viên mới được điều chỉnh ngày công.");
        }
    }

    /**
     * Lớp hỗ trợ hiển thị nhân viên trong JComboBox
     */
    private static class NhanVienItem {
        private final int maNV;
        private final String hoTen;

        public NhanVienItem(int maNV, String hoTen) {
            this.maNV = maNV;
            this.hoTen = hoTen;
        }

        public int getMaNV() {
            return maNV;
        }

        public String getHoTen() {
            return hoTen;
        }

        @Override
        public String toString() {
            return "[" + maNV + "] " + hoTen;
        }
    }
}
