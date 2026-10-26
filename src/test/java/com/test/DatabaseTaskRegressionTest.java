package com.test;

import com.config.DatabaseConnection;
import com.ui.theme.DatabaseTask;
import java.awt.Component;
import java.awt.Container;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

/** Exercises repeat Swing workflows without opening any database connection. */
public final class DatabaseTaskRegressionTest {
    private static int checks;

    private static final class Form {
        final JPanel panel = new JPanel();
        final JComboBox<String> employee = new JComboBox<>(new String[]{"Employee A", "Employee B"});
        final JSpinner month = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
        final JSpinner date = new JSpinner(new SpinnerDateModel(new Date(), null, null, 5));
        final JButton save = new JButton("Save");

        Form() {
            panel.add(employee);
            panel.add(month);
            panel.add(date);
            panel.add(save);
        }
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        DatabaseConnection.authenticated("offline-regression", "unused-no-connection");
        try {
            repeatEmployeeAndPeriodSelection();
            overlappingParentAndChildTasks();
            overlappingCompoundControlTasks();
            failedTaskCanBeRetried();
            permissionDisabledControlsStayDisabled();
            staleReadDoesNotOverwriteNewSelection();
            System.out.println("PASS DatabaseTask regressions: " + checks
                    + " assertions; offline, no database writes.");
        } finally {
            DatabaseConnection.logout();
        }
    }

    private static void repeatEmployeeAndPeriodSelection() throws Exception {
        Form[] holder = new Form[1];
        Map<Component, Boolean> states = new IdentityHashMap<>();
        AtomicInteger writes = new AtomicInteger();
        CountDownLatch first = new CountDownLatch(1), second = new CountDownLatch(1);
        SwingUtilities.invokeAndWait(() -> {
            holder[0] = new Form();
            snapshot(holder[0].panel, states);
            DatabaseTask.runExclusive(holder[0].panel, writes::incrementAndGet, ignored -> first.countDown());
        });
        await(first, "First save did not complete");
        SwingUtilities.invokeAndWait(() -> {
            assertRestored(states, "First save");
            holder[0].employee.setSelectedIndex(1);
            holder[0].month.setValue(2);
            check("Employee B".equals(holder[0].employee.getSelectedItem()), "Cannot choose the next employee");
            check(Integer.valueOf(2).equals(holder[0].month.getValue()), "Cannot choose another payroll period");
            DatabaseTask.runExclusive(holder[0].panel, writes::incrementAndGet, ignored -> second.countDown());
        });
        await(second, "Second save was blocked after the first save");
        SwingUtilities.invokeAndWait(() -> assertRestored(states, "Second save"));
        check(writes.get() == 2, "Repeat workflow did not execute both operations");
    }

    private static void overlappingParentAndChildTasks() throws Exception {
        Form[] holder = new Form[1];
        Map<Component, Boolean> states = new IdentityHashMap<>();
        CountDownLatch parentGate = new CountDownLatch(1), childGate = new CountDownLatch(1);
        CountDownLatch parentDone = new CountDownLatch(1), childDone = new CountDownLatch(1);
        try {
            SwingUtilities.invokeAndWait(() -> {
                holder[0] = new Form();
                snapshot(holder[0].panel, states);
                DatabaseTask.run(holder[0].panel, () -> waitFor(parentGate), ignored -> parentDone.countDown());
                DatabaseTask.run(holder[0].employee, () -> waitFor(childGate), ignored -> childDone.countDown());
            });
            parentGate.countDown();
            await(parentDone, "Parent task did not complete");
            SwingUtilities.invokeAndWait(() -> {
                check(holder[0].panel.isEnabled(), "Completed parent remained disabled");
                check(!holder[0].employee.isEnabled(), "Parent completion enabled an active child task");
            });
            childGate.countDown();
            await(childDone, "Child task did not complete");
            SwingUtilities.invokeAndWait(() -> assertRestored(states, "Overlapping tasks"));
        } finally {
            parentGate.countDown();
            childGate.countDown();
        }
    }

    private static void failedTaskCanBeRetried() throws Exception {
        Form[] holder = new Form[1];
        Map<Component, Boolean> states = new IdentityHashMap<>();
        AtomicInteger callbacks = new AtomicInteger();
        CountDownLatch retried = new CountDownLatch(1);
        SwingUtilities.invokeAndWait(() -> {
            holder[0] = new Form();
            snapshot(holder[0].panel, states);
            DatabaseTask.runExclusive(holder[0].panel, () -> {
                throw new IllegalArgumentException("Expected offline save failure");
            }, ignored -> callbacks.incrementAndGet());
        });
        awaitUi(() -> holder[0].panel.getClientProperty("db.request") == null, "Failed operation stayed busy");
        SwingUtilities.invokeAndWait(() -> {
            assertRestored(states, "Failed save");
            check(callbacks.get() == 0, "Failed operation called success callback");
            DatabaseTask.runExclusive(holder[0].panel, () -> true, ignored -> retried.countDown());
        });
        await(retried, "Save could not be retried after failure");
        SwingUtilities.invokeAndWait(() -> assertRestored(states, "Retried save"));
    }

    private static void overlappingCompoundControlTasks() throws Exception {
        Form[] holder = new Form[1];
        JComponent[] arrow = new JComponent[1];
        Map<Component, Boolean> states = new IdentityHashMap<>();
        CountDownLatch parentGate = new CountDownLatch(1), childGate = new CountDownLatch(1);
        CountDownLatch parentDone = new CountDownLatch(1), childDone = new CountDownLatch(1);
        try {
            SwingUtilities.invokeAndWait(() -> {
                holder[0] = new Form();
                snapshot(holder[0].panel, states);
                for (Component child : holder[0].employee.getComponents()) {
                    if (child instanceof AbstractButton) arrow[0] = (JComponent) child;
                }
                check(arrow[0] != null, "Combo box has no dropdown button to test");
                DatabaseTask.run(holder[0].employee, () -> waitFor(parentGate), ignored -> parentDone.countDown());
                DatabaseTask.run(arrow[0], () -> waitFor(childGate), ignored -> childDone.countDown());
            });
            parentGate.countDown();
            await(parentDone, "Combo task did not complete");
            SwingUtilities.invokeAndWait(() -> {
                check(holder[0].employee.isEnabled(), "Completed combo remained disabled");
                check(!arrow[0].isEnabled(), "Restoring the combo enabled its still-busy dropdown button");
            });
            childGate.countDown();
            await(childDone, "Dropdown task did not complete");
            SwingUtilities.invokeAndWait(() -> assertRestored(states, "Compound-control overlap"));
        } finally {
            parentGate.countDown();
            childGate.countDown();
        }
    }

    private static void permissionDisabledControlsStayDisabled() throws Exception {
        Form[] holder = new Form[1];
        Map<Component, Boolean> states = new IdentityHashMap<>();
        CountDownLatch done = new CountDownLatch(1);
        SwingUtilities.invokeAndWait(() -> {
            holder[0] = new Form();
            holder[0].employee.setEnabled(false);
            holder[0].month.setEnabled(false);
            holder[0].save.setEnabled(false);
            snapshot(holder[0].panel, states);
            DatabaseTask.run(holder[0].panel, () -> true, ignored -> done.countDown());
        });
        await(done, "Read-only form load did not complete");
        SwingUtilities.invokeAndWait(() -> assertRestored(states, "Permission-disabled form"));
    }

    private static void staleReadDoesNotOverwriteNewSelection() throws Exception {
        Form[] holder = new Form[1];
        Map<Component, Boolean> states = new IdentityHashMap<>();
        CountDownLatch oldGate = new CountDownLatch(1), latestDone = new CountDownLatch(1);
        AtomicInteger result = new AtomicInteger();
        try {
            SwingUtilities.invokeAndWait(() -> {
                holder[0] = new Form();
                snapshot(holder[0].panel, states);
                DatabaseTask.run(holder[0].panel, () -> waitFor(oldGate), ignored -> result.set(1));
                DatabaseTask.invalidate(holder[0].panel);
                DatabaseTask.run(holder[0].panel, () -> 2, value -> {
                    result.set(value);
                    latestDone.countDown();
                });
            });
            await(latestDone, "Latest read did not complete");
            oldGate.countDown();
            awaitUi(() -> holder[0].panel.isEnabled(), "Stale read left the form disabled");
            SwingUtilities.invokeAndWait(() -> {
                check(result.get() == 2, "Stale result overwrote the latest selection");
                check(holder[0].panel.getClientProperty("db.request") == null, "Completed reads left a pending request");
                assertRestored(states, "Stale read");
            });
        } finally {
            oldGate.countDown();
        }
    }

    private static boolean waitFor(CountDownLatch gate) throws InterruptedException {
        if (!gate.await(5, TimeUnit.SECONDS)) throw new AssertionError("Worker release timed out");
        return true;
    }

    private static void await(CountDownLatch latch, String message) throws InterruptedException {
        check(latch.await(5, TimeUnit.SECONDS), message);
    }

    private static void awaitUi(BooleanSupplier condition, String message) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        boolean[] ready = new boolean[1];
        do {
            SwingUtilities.invokeAndWait(() -> ready[0] = condition.getAsBoolean());
            if (ready[0]) break;
            Thread.sleep(10);
        } while (System.nanoTime() < deadline);
        check(ready[0], message);
    }

    private static void snapshot(Component component, Map<Component, Boolean> states) {
        states.put(component, component.isEnabled());
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) snapshot(child, states);
        }
    }

    private static void assertRestored(Map<Component, Boolean> states, String operation) {
        for (Map.Entry<Component, Boolean> state : states.entrySet()) {
            Component component = state.getKey();
            check(component.isEnabled() == state.getValue(), operation + " changed enabled state of "
                    + component.getClass().getSimpleName());
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }
}
