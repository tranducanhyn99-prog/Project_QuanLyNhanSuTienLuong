package com.test;

import com.ui.theme.UITheme;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Offline checks for contrast, actual button painting and display/model separation. */
public final class LightThemeRegressionTest {
    private static int checks;
    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
        checks++;
    }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            UITheme.setupGlobalUI();
            for (Color bg : new Color[]{UITheme.BG_CARD, UITheme.BG_APP, UITheme.BG_ROW_ALT, UITheme.PRIMARY_LIGHT}) {
                contrast(UITheme.TEXT_MAIN, bg, "Main text");
                contrast(UITheme.TEXT_MUTED, bg, "Secondary text");
                contrast(UITheme.TEXT_SUBTLE, bg, "Caption text");
            }
            contrast(UITheme.TEXT_SIDEBAR, UITheme.BG_SIDEBAR, "Navigation");
            contrast(Color.WHITE, UITheme.PRIMARY, "Primary button");
            contrast(Color.WHITE, UITheme.PRIMARY_HOVER, "Hovered primary button");
            contrast(Color.WHITE, UITheme.PRIMARY_ACTIVE, "Pressed primary button");
            contrast(UITheme.PRIMARY_ACTIVE, UITheme.PRIMARY_LIGHT, "Selected row");
            contrast(UITheme.SUCCESS_TEXT, UITheme.SUCCESS_BG, "Success badge");
            contrast(UITheme.WARNING_TEXT, UITheme.WARNING_BG, "Warning badge");
            contrast(UITheme.DANGER_TEXT, UITheme.DANGER_BG, "Danger badge");
            contrast(UITheme.INFO_TEXT, UITheme.INFO_BG, "Information badge");
            check(UITheme.BG_SIDEBAR.equals(Color.WHITE), "Sidebar must remain light");
            checkButtons();
            checkTable();
            checkComboBox();
            checkSpinnerFocus();
        });
        System.out.println("PASS light theme: " + checks + " assertions; no database or display required.");
    }
    private static void contrast(Color fg, Color bg, String label) {
        double a = luminance(fg), b = luminance(bg);
        double ratio = (Math.max(a, b) + .05) / (Math.min(a, b) + .05);
        check(ratio >= 4.5, label + " contrast below 4.5:1: " + String.format(Locale.ROOT, "%.2f", ratio));
    }
    private static double luminance(Color c) {
        return .2126 * linear(c.getRed()) + .7152 * linear(c.getGreen()) + .0722 * linear(c.getBlue());
    }
    private static double linear(int value) {
        double x = value / 255d;
        return x <= .04045 ? x / 12.92 : Math.pow((x + .055) / 1.055, 2.4);
    }
    private static BufferedImage paint(JComponent c) {
        c.setSize(180, 42);
        BufferedImage image = new BufferedImage(180, 42, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE); g.fillRect(0, 0, 180, 42); c.paint(g); g.dispose();
        return image;
    }
    private static void checkButtons() {
        JButton button = new JButton("Lưu thay đổi");
        UITheme.stylePrimaryButton(button);
        Color normal = new Color(paint(button).getRGB(10, 21));
        check(normal.equals(UITheme.PRIMARY), "OS button painting replaced primary background");
        button.getModel().setRollover(true);
        check(new Color(paint(button).getRGB(10, 21)).equals(UITheme.PRIMARY_HOVER), "Missing hover feedback");
        button.getModel().setArmed(true); button.getModel().setPressed(true);
        check(!new Color(paint(button).getRGB(10, 21)).equals(normal), "Missing keyboard press feedback");
        button.getModel().setPressed(false); button.getModel().setArmed(false); button.getModel().setRollover(false);
        button.setEnabled(false);
        BufferedImage disabled = paint(button);
        Color disabledBg = new Color(disabled.getRGB(10, 21));
        contrast(UITheme.TEXT_MUTED, disabledBg, "Disabled button");
        int darkTextPixels = 0;
        for (int y = 10; y < 32; y++) for (int x = 30; x < 150; x++) {
            Color pixel = new Color(disabled.getRGB(x, y));
            if (luminance(pixel) < .3) darkTextPixels++;
        }
        check(darkTextPixels > 40, "Disabled button caption is unreadable");
        button.setEnabled(true);
        check(new Color(paint(button).getRGB(10, 21)).equals(normal), "Button did not restore normal appearance");
        check(button.getForeground().equals(Color.WHITE), "Painting mutated button foreground");
        JButton modern = new UITheme.ModernButton("Đăng nhập", UITheme.PRIMARY, UITheme.PRIMARY_HOVER, Color.WHITE, UITheme.PRIMARY_BORDER);
        check(new Color(paint(modern).getRGB(10, 21)).equals(normal), "Login button uses a different background");
    }
    private static void checkTable() {
        DefaultTableModel model = new DefaultTableModel(new Object[][]{
            {"mai.le@example.com", "HOAT_DONG"},
            {"Hồ Chí Minh, Việt Nam", "DI_TRE"},
            {"1,250,000", "NGUNG_HOAT_DONG"},
            {null, "DANG_LAM_VIEC"}
        }, new Object[]{"Giá trị", "Trạng thái"});
        JTable table = new JTable(model); UITheme.styleTable(table);
        for (int row = 0; row < 2; row++) {
            JLabel label = (JLabel)table.prepareRenderer(table.getCellRenderer(row, 0), row, 0);
            check(label.getHorizontalAlignment() == SwingConstants.LEFT, "Email/address misclassified as currency");
        }
        JLabel money = (JLabel)table.prepareRenderer(table.getCellRenderer(2, 0), 2, 0);
        check(money.getHorizontalAlignment() == SwingConstants.RIGHT, "Currency must align right");
        JLabel empty = (JLabel)table.prepareRenderer(table.getCellRenderer(3, 0), 3, 0);
        check(empty.getText().isEmpty() && empty.getHorizontalAlignment() == SwingConstants.LEFT, "Renderer leaked previous cell alignment/text");
        JComponent status = (JComponent)table.prepareRenderer(table.getCellRenderer(1, 1), 1, 1);
        JLabel badge = findLabel(status);
        check("Đi trễ".equals(badge.getText()), "Status label was not localized");
        check(badge.getForeground().equals(UITheme.WARNING_TEXT), "Wrong status color");
        check(status.getBackground().equals(UITheme.BG_ROW_ALT), "Status cell breaks zebra row background");
        table.setRowSelectionInterval(1, 1);
        status = (JComponent)table.prepareRenderer(table.getCellRenderer(1, 1), 1, 1);
        check(status.getBackground().equals(UITheme.PRIMARY_LIGHT), "Selected status cell lost selection background");
        check("DI_TRE".equals(model.getValueAt(1, 1)), "Display localization changed business status code");
        table.clearSelection();
        table.setSize(600, 180); table.doLayout();
        BufferedImage image = new BufferedImage(600, 180, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics(); table.paint(graphics); graphics.dispose();
        int badgePixels = 0;
        for (int y = 4; y < 36; y++) for (int x = 310; x < 590; x++) {
            if ((image.getRGB(x, y) & 0xffffff) == (UITheme.SUCCESS_BG.getRGB() & 0xffffff)) badgePixels++;
        }
        check(badgePixels > 300, "Status badge disappeared inside the table renderer");
    }
    private static JLabel findLabel(Container parent) {
        for (Component c : parent.getComponents()) if (c instanceof JLabel) return (JLabel)c;
        throw new AssertionError("Missing status badge");
    }
    private static void checkComboBox() {
        JComboBox<String> box = new JComboBox<>(new String[]{"DANG_LAM_VIEC", "Payroll_Officer"});
        UITheme.styleComboBox(box);
        JLabel label = (JLabel)box.getRenderer().getListCellRendererComponent(new JList<>(), "DANG_LAM_VIEC", 0, false, false);
        check("Đang làm việc".equals(label.getText()), "Combo status is not readable");
        box.setSelectedIndex(1);
        check("Payroll_Officer".equals(box.getSelectedItem()), "Role localization changed authorization value");
        label = (JLabel)box.getRenderer().getListCellRendererComponent(new JList<>(), (String)box.getSelectedItem(), -1, false, false);
        check("Kế toán tiền lương".equals(label.getText()), "Role display is not localized");
    }
    private static void checkSpinnerFocus() {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(10, 1, 12, 1));
        UITheme.styleSpinner(spinner);
        JFormattedTextField editor = ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField();
        for (FocusListener listener : editor.getFocusListeners()) {
            listener.focusGained(new FocusEvent(editor, FocusEvent.FOCUS_GAINED));
        }
        check(Boolean.TRUE.equals(spinner.getClientProperty("theme.focus")), "Spinner editor focus has no outer focus ring");
        for (FocusListener listener : editor.getFocusListeners()) {
            listener.focusLost(new FocusEvent(editor, FocusEvent.FOCUS_LOST));
        }
        check(Boolean.FALSE.equals(spinner.getClientProperty("theme.focus")), "Spinner focus ring remained after blur");
    }
}
