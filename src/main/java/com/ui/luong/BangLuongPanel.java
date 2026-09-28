package com.ui.luong;

import com.model.BangLuong;
import com.model.ChiTietBangLuong;
import com.service.PayrollService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
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
        pnlTop.setBorder(BorderFactory.createTitledBorder("Tính bảng lương tháng"));

        spnThang = new JSpinner(new SpinnerNumberModel(today.getMonthValue(), 1, 12, 1));
        spnNam = new JSpinner(new SpinnerNumberModel(today.getYear(), 2020, 2100, 1));
        spnNgayCongChuan = new JSpinner(new SpinnerNumberModel(26, 1, 31, 1));

        JButton btnTinhLuong = new JButton("Tính / Cập nhật lương");
        btnTinhLuong.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JButton btnXemKy = new JButton("Xem kỳ");
        JButton btnTaiLai = new JButton("Tải lại");
        JButton btnXoaKy = new JButton("Xóa kỳ (chưa chốt)");
        btnXoaKy.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnXoaKy.setForeground(new Color(185, 28, 28));

        pnlTop.add(new JLabel("Tháng:"));
        pnlTop.add(spnThang);
        pnlTop.add(new JLabel("Năm:"));
        pnlTop.add(spnNam);
        pnlTop.add(new JLabel("Ngày công chuẩn:"));
        pnlTop.add(spnNgayCongChuan);
        pnlTop.add(btnTinhLuong);
        pnlTop.add(btnXemKy);
        pnlTop.add(btnTaiLai);
        pnlTop.add(Box.createHorizontalStrut(10));
        pnlTop.add(btnXoaKy);
        add(pnlTop, BorderLayout.NORTH);

        modelBangLuong = new DefaultTableModel(new String[]{
            "Mã BL", "Tháng", "Năm", "NCC", "Trạng thái", "Số NV", "Tổng thực nhận", "Ngày tạo", "Ngày chốt"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblBangLuong = new JTable(modelBangLuong);
        tblBangLuong.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        modelChiTiet = new DefaultTableModel(new String[]{
            "Mã CT", "Mã NV", "Họ tên", "Lương CB", "Ngày công", "Tiền công", "Phụ cấp", "Khấu trừ", "Thực nhận"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblChiTiet = new JTable(modelChiTiet);

        JScrollPane scrollBangLuong = new JScrollPane(tblBangLuong);
        scrollBangLuong.setBorder(BorderFactory.createTitledBorder("Danh sách kỳ lương"));

        JScrollPane scrollChiTiet = new JScrollPane(tblChiTiet);
        scrollChiTiet.setBorder(BorderFactory.createTitledBorder("Chi tiết bảng lương"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollBangLuong, scrollChiTiet);
        splitPane.setResizeWeight(0.45);
        add(splitPane, BorderLayout.CENTER);

        btnTinhLuong.addActionListener(e -> xuLyTinhLuong());
        btnXemKy.addActionListener(e -> xuLyXemKy());
        btnTaiLai.addActionListener(e -> loadBangLuong());
        btnXoaKy.addActionListener(e -> xuLyXoaKyLuong());
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
            "Tính / Cập nhật bảng lương tháng " + thang + "/" + nam + " với " + ngayCongChuan + " ngày công chuẩn?\n\n"
            + "Ghi chú: Nếu kỳ lương đã tồn tại ở trạng thái CHƯA CHỐT, hệ thống sẽ tính lại toàn bộ theo ngày công mới nhất.",
            "Xác nhận tính lương",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int maBangLuong = payrollService.tinhBangLuongThang(thang, nam, ngayCongChuan);
            JOptionPane.showMessageDialog(this, "Tính / Cập nhật bảng lương tháng " + thang + "/" + nam + " thành công!\nMã bảng lương: " + maBangLuong, "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadBangLuong();
            selectBangLuong(maBangLuong);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Tính lương thất bại: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuLyXoaKyLuong() {
        int row = tblBangLuong.getSelectedRow();
        int maBangLuong = -1;
        String kyStr = "";

        if (row >= 0) {
            maBangLuong = Integer.parseInt(modelBangLuong.getValueAt(row, 0).toString());
            String trangThai = modelBangLuong.getValueAt(row, 4).toString();
            int thang = Integer.parseInt(modelBangLuong.getValueAt(row, 1).toString());
            int nam = Integer.parseInt(modelBangLuong.getValueAt(row, 2).toString());
            kyStr = "Tháng " + thang + "/" + nam;

            if ("DA_CHOT".equals(trangThai)) {
                JOptionPane.showMessageDialog(this,
                    "Kỳ lương " + kyStr + " đã chốt, không thể xóa trực tiếp!\nVui lòng vào tab Báo cáo mở lại (hủy chốt) bảng lương trước nếu muốn điều chỉnh.",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } else {
            int thang = (Integer) spnThang.getValue();
            int nam = (Integer) spnNam.getValue();
            try {
                BangLuong bl = payrollService.timBangLuongTheoKy(thang, nam);
                if (bl == null) {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy bảng lương tháng " + thang + "/" + nam + " để xóa!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                if ("DA_CHOT".equals(bl.getTrangThai())) {
                    JOptionPane.showMessageDialog(this,
                        "Kỳ lương tháng " + thang + "/" + nam + " đã chốt, không thể xóa trực tiếp!\nVui lòng vào tab Báo cáo mở lại (hủy chốt) bảng lương trước.",
                        "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                maBangLuong = bl.getMaBangLuong();
                kyStr = "Tháng " + thang + "/" + nam;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi kiểm tra kỳ lương: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa kỳ lương " + kyStr + " (trạng thái CHƯA CHỐT)?\n\n"
            + "Hành động này sẽ xóa toàn bộ chi tiết lương nháp của tháng này để bạn có thể chỉnh sửa ngày công và tính lại.",
            "Xác nhận xóa kỳ lương",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                payrollService.xoaBangLuong(maBangLuong);
                JOptionPane.showMessageDialog(this, "Đã xóa kỳ lương " + kyStr + " thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadBangLuong();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi xóa kỳ lương: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void xuLyXemKy() {
        int thang = (Integer) spnThang.getValue();
        int nam = (Integer) spnNam.getValue();

        try {
            BangLuong bangLuong = payrollService.timBangLuongTheoKy(thang, nam);
            if (bangLuong == null) {
                modelChiTiet.setRowCount(0);
                JOptionPane.showMessageDialog(this, "Chưa có bảng lương tháng " + thang + "/" + nam + ".");
                return;
            }
            selectBangLuong(bangLuong.getMaBangLuong());
            loadChiTietBangLuong(bangLuong.getMaBangLuong());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Không thể tải kỳ lương: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách bảng lương: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Lỗi tải chi tiết bảng lương: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
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
