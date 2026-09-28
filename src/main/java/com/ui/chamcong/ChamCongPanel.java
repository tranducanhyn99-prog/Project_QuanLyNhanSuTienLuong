package com.ui.chamcong;

import com.model.ChamCong;
import com.model.NhanVien;
import com.model.TongHopChamCong;
import com.service.ChamCongService;
import com.service.NhanVienService;
import com.session.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
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
        loadNhanVienComboBox();
        loadDuLieuChiTiet();
        loadDuLieuTongHop();
        applySecurityPermissions();

        if (selectSummaryTab && tabbedPane.getTabCount() > 1) {
            tabbedPane.setSelectedIndex(1);
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // ─── TAB 1: GHI NHẬN & THEO DÕI CHẤM CÔNG ─────────────────────
        JPanel pnlTab1 = new JPanel(new BorderLayout(8, 8));
        pnlTab1.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        // Form nhập bên trái / trên
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(BorderFactory.createTitledBorder("Thông tin chấm công (dbo.sp_GhiNhanChamCong)"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Dòng 0: Nhân viên
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        pnlForm.add(new JLabel("Nhân viên (*):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        cboNhanVien = new JComboBox<>();
        pnlForm.add(cboNhanVien, gbc);

        // Dòng 1: Ngày chấm công
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        pnlForm.add(new JLabel("Ngày chấm công:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        spnNgayCC = new JSpinner(new SpinnerDateModel(new Date(), null, new Date(), java.util.Calendar.DAY_OF_MONTH));
        spnNgayCC.setEditor(new JSpinner.DateEditor(spnNgayCC, "yyyy-MM-dd"));
        pnlForm.add(spnNgayCC, gbc);

        // Dòng 2: Giờ vào / Giờ ra
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        pnlForm.add(new JLabel("Giờ vào (HH:mm):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JPanel pnlGio = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        spnGioVao = createTimeSpinner(8, 0);
        spnGioRa = createTimeSpinner(17, 0);
        pnlGio.add(spnGioVao);
        pnlGio.add(new JLabel("  Giờ ra (HH:mm):"));
        pnlGio.add(spnGioRa);
        pnlForm.add(pnlGio, gbc);

        // Dòng 3: Trạng thái
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        pnlForm.add(new JLabel("Trạng thái:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        cboTrangThai = new JComboBox<>(new String[]{"CO_MAT", "DI_TRE", "VE_SOM", "VANG"});
        pnlForm.add(cboTrangThai, gbc);

        // Dòng 4: Ghi chú
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        pnlForm.add(new JLabel("Ghi chú:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtGhiChu = new JTextField(25);
        pnlForm.add(txtGhiChu, gbc);

        // Dòng 5: Các nút hành động
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        btnGhiNhan = new JButton("Ghi nhận chấm công");
        btnGhiNhan.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnGhiNhan.setBackground(new Color(30, 130, 76));
        btnGhiNhan.setForeground(Color.WHITE);

        btnLamMoiForm = new JButton("Làm mới form");

        btnDiemDanhHangLoat = new JButton("Điểm danh hôm nay (Lô All-or-Nothing)");
        btnDiemDanhHangLoat.setToolTipText("Thực thi nhập chấm công cả công ty trong 1 Database Transaction đảm bảo nguyên tử");

        pnlButtons.add(btnGhiNhan);
        pnlButtons.add(btnLamMoiForm);
        pnlButtons.add(btnDiemDanhHangLoat);
        pnlForm.add(pnlButtons, gbc);

        // Danh sách chi tiết bảng chấm công
        JPanel pnlDanhSach = new JPanel(new BorderLayout(5, 5));
        pnlDanhSach.setBorder(BorderFactory.createTitledBorder("Nhật ký chấm công"));

        JPanel pnlFilterChiTiet = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        LocalDate now = LocalDate.now();
        spnThangLoc = new JSpinner(new SpinnerNumberModel(now.getMonthValue(), 1, 12, 1));
        spnNamLoc = new JSpinner(new SpinnerNumberModel(now.getYear(), 2020, 2100, 1));
        btnTaiLaiChiTiet = new JButton("Tải nhật ký");
        btnXoaChamCong = new JButton("Xóa dòng chấm công");
        btnXoaChamCong.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnXoaChamCong.setForeground(new Color(185, 28, 28));

        pnlFilterChiTiet.add(new JLabel("Tháng:"));
        pnlFilterChiTiet.add(spnThangLoc);
        pnlFilterChiTiet.add(new JLabel("Năm:"));
        pnlFilterChiTiet.add(spnNamLoc);
        pnlFilterChiTiet.add(btnTaiLaiChiTiet);
        pnlFilterChiTiet.add(Box.createHorizontalStrut(12));
        pnlFilterChiTiet.add(btnXoaChamCong);
        pnlDanhSach.add(pnlFilterChiTiet, BorderLayout.NORTH);

        modelChiTiet = new DefaultTableModel(new String[]{
            "Mã CC", "Mã NV", "Họ tên", "Ngày chấm", "Giờ vào", "Giờ ra", "Trạng thái", "Ghi chú"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblChiTiet = new JTable(modelChiTiet);
        tblChiTiet.setRowHeight(22);
        tblChiTiet.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pnlDanhSach.add(new JScrollPane(tblChiTiet), BorderLayout.CENTER);

        JSplitPane splitTab1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pnlForm, pnlDanhSach);
        splitTab1.setResizeWeight(0.38);
        pnlTab1.add(splitTab1, BorderLayout.CENTER);

        // ─── TAB 2: TỔNG HỢP CHẤM CÔNG THÁNG (VIEW) ───────────────────
        JPanel pnlTab2 = new JPanel(new BorderLayout(8, 8));
        pnlTab2.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JPanel pnlFilterTongHop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlFilterTongHop.setBorder(BorderFactory.createTitledBorder("Bộ lọc dữ liệu View vw_TongHopChamCongThang"));
        spnThangTongHop = new JSpinner(new SpinnerNumberModel(now.getMonthValue(), 1, 12, 1));
        spnNamTongHop = new JSpinner(new SpinnerNumberModel(now.getYear(), 2020, 2100, 1));
        btnXemTongHop = new JButton("Xem tổng hợp tháng");
        btnXemTongHop.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnDieuChinhCong = new JButton("Xem & Điều chỉnh ngày công NV");
        btnDieuChinhCong.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDieuChinhCong.setForeground(new Color(30, 64, 175));
        btnDieuChinhCong.setToolTipText("Xem toàn bộ ngày công của nhân viên đã chọn trong tháng và thêm/sửa/xóa ngày công");

        pnlFilterTongHop.add(new JLabel("Tháng:"));
        pnlFilterTongHop.add(spnThangTongHop);
        pnlFilterTongHop.add(new JLabel("Năm:"));
        pnlFilterTongHop.add(spnNamTongHop);
        pnlFilterTongHop.add(btnXemTongHop);
        pnlFilterTongHop.add(Box.createHorizontalStrut(12));
        pnlFilterTongHop.add(btnDieuChinhCong);
        pnlTab2.add(pnlFilterTongHop, BorderLayout.NORTH);

        modelTongHop = new DefaultTableModel(new String[]{
            "Mã NV", "Họ tên", "Tháng", "Năm", "Số ngày đi làm", "Số lần đi trễ", "Số lần về sớm", "Số ngày vắng", "Tổng số giờ làm"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblTongHop = new JTable(modelTongHop);
        tblTongHop.setRowHeight(24);
        tblTongHop.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Căn giữa các cột số
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < tblTongHop.getColumnCount(); i++) {
            if (i != 1) {
                tblTongHop.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        JScrollPane scrollTongHop = new JScrollPane(tblTongHop);
        scrollTongHop.setBorder(BorderFactory.createTitledBorder("Bảng tổng hợp công theo nhân viên"));
        pnlTab2.add(scrollTongHop, BorderLayout.CENTER);

        lblTongHopThongKe = new JLabel("Tổng số nhân sự có dữ liệu chấm công: 0");
        lblTongHopThongKe.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblTongHopThongKe.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        pnlTab2.add(lblTongHopThongKe, BorderLayout.SOUTH);

        // Add tabs
        tabbedPane.addTab("Nhập & Nhật ký chấm công", pnlTab1);
        tabbedPane.addTab("Tổng hợp công theo tháng (View TV2)", pnlTab2);
        add(tabbedPane, BorderLayout.CENTER);

        // Events
        btnGhiNhan.addActionListener(e -> xuLyGhiNhanChamCong());
        btnLamMoiForm.addActionListener(e -> lamMoiForm());
        btnDiemDanhHangLoat.addActionListener(e -> xuLyDiemDanhHangLoat());
        btnTaiLaiChiTiet.addActionListener(e -> loadDuLieuChiTiet());
        btnXoaChamCong.addActionListener(e -> xuLyXoaChamCong());
        btnXemTongHop.addActionListener(e -> loadDuLieuTongHop());
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
        return spinner;
    }

    private void loadNhanVienComboBox() {
        cboNhanVien.removeAllItems();
        try {
            List<NhanVien> list = nhanVienService.layDanhSachNhanVien();
            for (NhanVien nv : list) {
                if ("DANG_LAM_VIEC".equals(nv.getTrangThai())) {
                    cboNhanVien.addItem(new NhanVienItem(nv.getMaNV(), nv.getHoTen()));
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không thể tải danh sách nhân viên: " + ex.getMessage(),
                    "Lỗi tải dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadDuLieuChiTiet() {
        modelChiTiet.setRowCount(0);
        int thang = (Integer) spnThangLoc.getValue();
        int nam = (Integer) spnNamLoc.getValue();
        try {
            List<ChamCong> list = chamCongService.layDanhSachChamCongTheoThang(thang, nam);
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
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải nhật ký chấm công: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadDuLieuTongHop() {
        modelTongHop.setRowCount(0);
        int thang = (Integer) spnThangTongHop.getValue();
        int nam = (Integer) spnNamTongHop.getValue();
        try {
            List<TongHopChamCong> list = chamCongService.layTongHopChamCongThang(thang, nam);
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
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải tổng hợp chấm công tháng: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
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
            boolean success = chamCongService.chamCongDonLe(cc);

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
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể ghi nhận chấm công: " + ex.getMessage(),
                    "Lỗi nghiệp vụ / CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuLyDiemDanhHangLoat() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn tự động điểm danh CÓ MẶT cho toàn bộ nhân viên đang làm việc cho ngày hôm nay?\n"
                        + "Lưu ý: Quá trình chạy trong 1 Transaction đảm bảo All-or-Nothing (nếu 1 NV đã chấm công, toàn bộ sẽ rollback).",
                "Xác nhận điểm danh theo lô", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            List<NhanVien> listNV = nhanVienService.layDanhSachNhanVien();
            List<ChamCong> danhSach = new ArrayList<>();
            LocalDate today = LocalDate.now();
            LocalTime gioVao = LocalTime.of(8, 0);
            LocalTime gioRa = LocalTime.of(17, 0);

            for (NhanVien nv : listNV) {
                if ("DANG_LAM_VIEC".equals(nv.getTrangThai())) {
                    ChamCong cc = new ChamCong(nv.getMaNV(), today, gioVao, gioRa, "CO_MAT", "Điểm danh hàng loạt hệ thống");
                    danhSach.add(cc);
                }
            }

            if (danhSach.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có nhân viên nào đang hoạt động để điểm danh!",
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            chamCongService.nhapChamCongTheoLo(danhSach);

            JOptionPane.showMessageDialog(this,
                    "Đã điểm danh theo lô thành công cho " + danhSach.size() + " nhân viên trong một Transaction an toàn!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

            loadDuLieuChiTiet();
            loadDuLieuTongHop();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Giao dịch nhập theo lô đã bị hủy (ROLLBACK) do phát sinh lỗi:\n" + ex.getMessage(),
                    "Lỗi Transaction nhập lô", JOptionPane.ERROR_MESSAGE);
        }
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
                chamCongService.xoaChamCong(maCC);
                JOptionPane.showMessageDialog(this, "Đã xóa lượt chấm công thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadDuLieuChiTiet();
                loadDuLieuTongHop();
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
        int thang = (Integer) spnThangTongHop.getValue();
        int nam = (Integer) spnNamTongHop.getValue();

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
            btnGhiNhan.setToolTipText("Chỉ Quản lý nhân sự (HR_Manager) hoặc Quản trị viên mới được ghi nhận chấm công.");
            btnDiemDanhHangLoat.setToolTipText("Chỉ Quản lý nhân sự (HR_Manager) hoặc Quản trị viên mới được điểm danh hàng loạt.");
            if (btnXoaChamCong != null) btnXoaChamCong.setToolTipText("Chỉ Quản lý nhân sự (HR_Manager) hoặc Quản trị viên mới được xóa chấm công.");
            if (btnDieuChinhCong != null) btnDieuChinhCong.setToolTipText("Chỉ Quản lý nhân sự (HR_Manager) hoặc Quản trị viên mới được điều chỉnh ngày công.");
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
