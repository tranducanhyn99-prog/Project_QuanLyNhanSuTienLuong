package com.ui.theme;

import com.config.DatabaseConnection;
import java.awt.Component;
import java.awt.Container;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/** Shared SwingWorker boundary: DB work off EDT, UI callbacks on EDT. */
public final class DatabaseTask {
    private static final Logger LOG = Logger.getLogger(DatabaseTask.class.getName());
    // EDT owns this map; overlapping parent/child workers share disable counts.
    private static final Map<Component, BusyState> busy = new IdentityHashMap<>();
    private static final class BusyState {
        final boolean enabled;
        int count;
        BusyState(Component component) { enabled = component.isEnabled(); }
    }
    private DatabaseTask() { }
    /** Discard pending results when the screen's selection or parent list is reset. */
    public static void invalidate(JComponent owner) {
        if (!SwingUtilities.isEventDispatchThread()) throw new IllegalStateException("Invalidate on EDT");
        owner.putClientProperty("db.request", new Object());
    }
    /** Writes and whole-form lookups must not be submitted twice. Reads may replace earlier reads. */
    public static <T> void runExclusive(JComponent owner, Callable<T> work, Consumer<T> success) {
        if (!SwingUtilities.isEventDispatchThread()) throw new IllegalStateException("Schedule on EDT");
        if (owner.getClientProperty("db.request") != null) return;
        run(owner, work, success);
    }
    public static <T> void run(JComponent owner, Callable<T> work, Consumer<T> success) {
        if (!SwingUtilities.isEventDispatchThread()) throw new IllegalStateException("Schedule on EDT");
        Object request = new Object();
        Object identity = DatabaseConnection.identityToken();
        List<Component> components = new ArrayList<>();
        remember(owner, components);
        owner.putClientProperty("db.request", request);
        new SwingWorker<T, Void>() {
            protected T doInBackground() throws Exception {
                return DatabaseConnection.withIdentity(identity, work);
            }
            protected void done() {
                for (Component component : components) {
                    BusyState state = busy.get(component);
                    if (--state.count == 0) {
                        busy.remove(component);
                        component.setEnabled(state.enabled);
                    }
                }
                if (owner.getClientProperty("db.request") != request) return;
                owner.putClientProperty("db.request", null);
                if (identity != DatabaseConnection.identityToken()) return;
                try { success.accept(get()); }
                catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    LOG.log(Level.WARNING, "Database task failed", cause);
                    if (owner.isShowing()) JOptionPane.showMessageDialog(owner, errorMessage(cause), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    else System.err.println("DB task failed: " + cause.getMessage());
                }
            }
        }.execute();
    }
    private static String errorMessage(Throwable failure) {
        // Services wrap SQLException; retain business errors but shorten infrastructure failures.
        for (Throwable error = failure; error != null; error = error.getCause()) {
            if (!(error instanceof SQLException)) continue;
            SQLException sql = (SQLException) error;
            String state = sql.getSQLState();
            if (sql instanceof SQLTimeoutException) {
                return "Thao tác dữ liệu bị hủy hoặc quá thời gian. Kiểm tra kết quả trước khi thử lại.";
            }
            if (state != null && state.startsWith("08")) {
                return "Không kết nối được SQL Server. Vui lòng thử lại khi kết nối được khôi phục.";
            }
            if (sql.getErrorCode() == 18456 || (state != null && state.startsWith("28"))) {
                return "Phiên đăng nhập không còn hợp lệ. Vui lòng đăng nhập lại.";
            }
            if (sql.getErrorCode() == 229 || sql.getErrorCode() == 15151 || sql.getErrorCode() == 15247) {
                return "Tài khoản không có quyền thực hiện thao tác này. Liên hệ quản trị viên.";
            }
        }
        return failure.getMessage();
    }
    private static void remember(Component component, List<Component> components) {
        components.add(component);
        busy.computeIfAbsent(component, BusyState::new).count++;
        component.setEnabled(false);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) remember(child, components);
        }
    }
}
