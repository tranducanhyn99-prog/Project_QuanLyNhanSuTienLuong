package com.ui.luong;

import com.model.BangLuong;
import com.model.ChiTietBangLuong;
import com.service.PayrollService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class BangLuongPanel extends JPanel {

    private final PayrollService payrollService = new PayrollService();
    private final NumberFormat moneyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private JSpinner spnThang;
    private JSpinner spnNam;
    private JSpinner spnNgayCongChuan;
    private JTable tblBangLuong;
    private JTable tblChiTiet;
    private DefaultTableModel modelBangLuong;
    private DefaultTableModel modelChiTiet;

    public BangLuongPanel() {
        initComponents();
        loadBangLuong();
    }

    private void initComponents() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        LocalDate today = LocalDate.now();
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlTop.setBorder(BorderFactory.createTitledBorder("Tinh bang luong thang"));

        spnThang = new JSpinner(new SpinnerNumberModel(today.getMonthValue(), 1, 12, 1));
        spnNam = new JSpinner(new SpinnerNumberModel(today.getYear(), 2020, 2100, 1));
        spnNgayCongChuan = new JSpinner(new SpinnerNumberModel(26, 1, 31, 1));

        JButton btnTinhLuong = new JButton("Tinh luong");
        JButton btnXemKy = new JButton("Xem ky");
        JButton btnTaiLai = new JButton("Tai lai");

        pnlTop.add(new JLabel("Thang:"));
        pnlTop.add(spnThang);
        pnlTop.add(new JLabel("Nam:"));
        pnlTop.add(spnNam);
        pnlTop.add(new JLabel("Ngay cong chuan:"));
        pnlTop.add(spnNgayCongChuan);
        pnlTop.add(btnTinhLuong);
        pnlTop.add(btnXemKy);
        pnlTop.add(btnTaiLai);
        add(pnlTop, BorderLayout.NORTH);

        modelBangLuong = new DefaultTableModel(new String[]{
            "Ma BL", "Thang", "Nam", "NCC", "Trang thai", "So NV", "Tong thuc nhan", "Ngay tao", "Ngay chot"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblBangLuong = new JTable(modelBangLuong);
        tblBangLuong.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        modelChiTiet = new DefaultTableModel(new String[]{
            "Ma CT", "Ma NV", "Ho ten", "Luong CB", "Ngay cong", "Tien cong", "Phu cap", "Khau tru", "Thuc nhan"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblChiTiet = new JTable(modelChiTiet);

        JScrollPane scrollBangLuong = new JScrollPane(tblBangLuong);
        scrollBangLuong.setBorder(BorderFactory.createTitledBorder("Danh sach ky luong"));

        JScrollPane scrollChiTiet = new JScrollPane(tblChiTiet);
        scrollChiTiet.setBorder(BorderFactory.createTitledBorder("Chi tiet bang luong"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollBangLuong, scrollChiTiet);
        splitPane.setResizeWeight(0.45);
        add(splitPane, BorderLayout.CENTER);

        btnTinhLuong.addActionListener(e -> xuLyTinhLuong());
        btnXemKy.addActionListener(e -> xuLyXemKy());
        btnTaiLai.addActionListener(e -> loadBangLuong());
        tblBangLuong.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblBangLuong.getSelectedRow();
                if (row >= 0) {
                    int maBangLuong = Integer.parseInt(modelBangLuong.getValueAt(row, 0).toString());
                    loadChiTietBangLuong(maBangLuong);
                }
            }
        });
    }

    private void xuLyTinhLuong() {
        int thang = (Integer) spnThang.getValue();
        int nam = (Integer) spnNam.getValue();
        int ngayCongChuan = (Integer) spnNgayCongChuan.getValue();

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Tinh bang luong thang " + thang + "/" + nam + " voi " + ngayCongChuan + " ngay cong chuan?",
            "Xac nhan tinh luong",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int maBangLuong = payrollService.tinhBangLuongThang(thang, nam, ngayCongChuan);
            JOptionPane.showMessageDialog(this, "Tinh luong thanh cong. Ma bang luong: " + maBangLuong);
            loadBangLuong();
            selectBangLuong(maBangLuong);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Tinh luong that bai: " + e.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuLyXemKy() {
        int thang = (Integer) spnThang.getValue();
        int nam = (Integer) spnNam.getValue();

        try {
            BangLuong bangLuong = payrollService.timBangLuongTheoKy(thang, nam);
            if (bangLuong == null) {
                modelChiTiet.setRowCount(0);
                JOptionPane.showMessageDialog(this, "Chua co bang luong thang " + thang + "/" + nam + ".");
                return;
            }
            selectBangLuong(bangLuong.getMaBangLuong());
            loadChiTietBangLuong(bangLuong.getMaBangLuong());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Khong the tai ky luong: " + e.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadBangLuong() {
        modelBangLuong.setRowCount(0);
        try {
            List<BangLuong> list = payrollService.layDanhSachBangLuong();
            for (BangLuong bl : list) {
                modelBangLuong.addRow(new Object[]{
                    bl.getMaBangLuong(),
                    bl.getThang(),
                    bl.getNam(),
                    bl.getNgayCongChuan(),
                    bl.getTrangThai(),
                    bl.getSoNhanVien(),
                    formatMoney(bl.getTongThucNhan()),
                    formatDateTime(bl.getNgayTao()),
                    formatDateTime(bl.getNgayChot())
                });
            }
            modelChiTiet.setRowCount(0);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Loi tai danh sach bang luong: " + e.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadChiTietBangLuong(int maBangLuong) {
        modelChiTiet.setRowCount(0);
        try {
            List<ChiTietBangLuong> list = payrollService.layChiTietBangLuong(maBangLuong);
            for (ChiTietBangLuong ct : list) {
                modelChiTiet.addRow(new Object[]{
                    ct.getMaChiTiet(),
                    ct.getMaNV(),
                    ct.getHoTen(),
                    formatMoney(ct.getLuongCoBan()),
                    ct.getNgayCongThucTe(),
                    formatMoney(ct.getTienCong()),
                    formatMoney(ct.getTongPhuCap()),
                    formatMoney(ct.getTongKhauTru()),
                    formatMoney(ct.getThucNhan())
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Loi tai chi tiet bang luong: " + e.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void selectBangLuong(int maBangLuong) {
        for (int i = 0; i < modelBangLuong.getRowCount(); i++) {
            int value = Integer.parseInt(modelBangLuong.getValueAt(i, 0).toString());
            if (value == maBangLuong) {
                tblBangLuong.setRowSelectionInterval(i, i);
                tblBangLuong.scrollRectToVisible(tblBangLuong.getCellRect(i, 0, true));
                return;
            }
        }
    }

    private String formatMoney(BigDecimal value) {
        return moneyFormat.format(value != null ? value : BigDecimal.ZERO);
    }

    private String formatDateTime(LocalDateTime value) {
        return value != null ? dateTimeFormatter.format(value) : "";
    }
}
