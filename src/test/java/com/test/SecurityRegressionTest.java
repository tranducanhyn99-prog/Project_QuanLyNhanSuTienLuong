package com.test;

import com.config.DatabaseConnection;
import com.dao.*;
import com.model.*;
import com.service.*;
import com.session.Session;
import com.ui.theme.DatabaseTask;
import com.ui.luong.BangLuongPanel;
import com.ui.baocao.BaoCaoPanel;
import com.ui.chamcong.ChamCongPanel;
import com.ui.nhanvien.NhanVienPanel;
import com.util.PasswordUtil;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import javax.swing.*;

/** Offline checks exercise Java boundaries with a recording JDBC driver, not SQL permissions. */
public final class SecurityRegressionTest {
    private static int checks;
    private static final String PASSWORD="Regression_Only!42";
    private static String stored, lastUser, lastSql;
    private static Map<Integer,Object> lastParameters;
    private static boolean active=true;
    private static int connections;
    private static volatile BiFunction<String,Map<Integer,Object>,ResultSet> queryOverride;
    private static final AtomicInteger queriesOnEdt = new AtomicInteger();
    private static void check(boolean condition,String message) {
        if(!condition) throw new AssertionError(message);
        checks++;
    }
    @FunctionalInterface private interface Action { void run() throws Exception; }
    private static void denied(Action action) throws Exception {
        try { action.run(); throw new AssertionError("Expected rejection"); }
        catch(SecurityException expected) { checks++; }
    }
    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless","true");
        System.setProperty("db.url","jdbc:prj-regression:offline");
        DriverManager.registerDriver(new RecordingDriver());
        stored=PasswordUtil.hashPassword(PASSWORD);
        String second=PasswordUtil.hashPassword(PASSWORD);
        check(!stored.equals(second),"Different salts required");
        check(PasswordUtil.verifyPassword(PASSWORD,stored),"PBKDF2 verification");
        check(!PasswordUtil.verifyPassword("wrong",stored),"Wrong password");
        for(String invalid:new String[]{"", "pbkdf2-sha256$0$bad$bad", "pbkdf2-sha256$2147483647$bad$bad", "unknown$600000$a$b"})
            check(!PasswordUtil.verifyPassword(PASSWORD,invalid),"Malformed hash accepted");
        check(PasswordUtil.verifyPassword(PASSWORD,PasswordUtil.hashSHA256(PASSWORD)),"Legacy read migration support");
        Session.getInstance().logout();
        for(Object service:new Object[]{new PayrollService(),new NhanVienService(),new DanhMucService(),new ChamCongService(),new PhuCapKhauTruService()}) {
            for(Method method:service.getClass().getDeclaredMethods()) {
                if(!Modifier.isPublic(method.getModifiers()) || method.getName().equals("validateChamCong")) continue;
                Object[] parameters=Arrays.stream(method.getParameterTypes()).map(t -> t==int.class?1:t==boolean.class?false:null).toArray();
                denied(() -> {
                    try { method.invoke(service,parameters); }
                    catch(InvocationTargetException ex) {
                        if(ex.getCause() instanceof Exception) throw (Exception)ex.getCause();
                        throw ex;
                    }
                });
            }
        }
        check(connections==0,"Unsigned service call reached JDBC");
        Session.getInstance().login(1,7,"hr","HR_Manager","HR");
        denied(() -> new PayrollService().xoaBangLuong(1));
        denied(() -> new PhuCapKhauTruService().xoaKyLuongChuaChot(1,2026));
        NhanVien employee=new NhanVien(); employee.setMaNV(7); employee.setHoTen("QA");
        employee.setNgaySinh(LocalDate.of(1990,1,1)); employee.setNgayVaoLam(LocalDate.of(2020,2,3));
        employee.setCccd("123456789012"); employee.setSoDienThoai("0901234567"); employee.setEmail("qa@example.com");
        employee.setLuongCoBan(BigDecimal.TEN); employee.setMaPB(1); employee.setMaCV(1);
        denied(() -> new NhanVienService().themNhanVien(employee,true,"evil",PASSWORD,"DB_Admin"));
        AuthService auth=new AuthService();
        TaiKhoan account=auth.login("employee",PASSWORD);
        check(account.getMatKhau()==null && Session.getInstance().hasRole("Employee"),"Auth profile/hash exposure");
        // Synthetic Java elevation never replaces the actual SQL credentials.
        Session.getInstance().login(1,7,"fake_admin","DB_Admin","QA");
        new NhanVienDAO().update(employee);
        check("employee".equals(lastUser),"Session mutation changed SQL identity");
        check(lastSql.contains("NgayVaoLam = ?") && java.sql.Date.valueOf(employee.getNgayVaoLam()).equals(lastParameters.get(12)),"Hire date not bound to UPDATE");
        ChamCong attendance=new ChamCong(); attendance.setMaChamCong(1); attendance.setNgayChamCong(LocalDate.of(2020,2,4));
        int before=connections; new ChamCongDAO().updateChamCong(attendance);
        check(connections==before+1 && java.sql.Date.valueOf(attendance.getNgayChamCong()).equals(lastParameters.get(5)),"Attendance update/date/precheck regression");
        Object oldToken=DatabaseConnection.identityToken();
        auth.logout(); auth.login("employee",PASSWORD);
        denied(() -> DatabaseConnection.withIdentity(oldToken, () -> { DatabaseConnection.getConnection(); return null; }));
        try { auth.login("employee","wrong"); throw new AssertionError("Invalid SQL password accepted"); }
        catch(SQLException expected) { check("28000".equals(expected.getSQLState()),"Wrong exception for auth failure"); }
        check(!Session.getInstance().isLoggedIn(),"Failed auth retained session");
        stored=PasswordUtil.hashSHA256(PASSWORD); auth.login("legacy",PASSWORD);
        check(stored.startsWith("pbkdf2-sha256$"),"Legacy login did not migrate");
        active=false; denied(() -> auth.login("employee",PASSWORD)); active=true;
        auth.login("employee",PASSWORD);
        checkWorkerOrdering();
        checkExclusiveAndSessionChange(auth);
        checkPayrollPanels();
        checkEmployeeForm();
        auth.logout();
        System.out.println("PASS offline Java regressions: "+checks+" assertions. SQL behavior requires live SQL tests.");
    }

    private static void checkWorkerOrdering() throws Exception {
        JPanel panel=new JPanel(); JButton child=new JButton(); JButton disabled=new JButton(); disabled.setEnabled(false);
        panel.add(child); panel.add(disabled);
        CountDownLatch release=new CountDownLatch(1),started=new CountDownLatch(1),newDone=new CountDownLatch(1);
        AtomicInteger applied=new AtomicInteger();
        SwingUtilities.invokeAndWait(() -> {
            DatabaseTask.run(child, () -> { started.countDown(); release.await(5,TimeUnit.SECONDS); return 1; }, value -> applied.addAndGet(100));
            DatabaseTask.run(panel, () -> 2, value -> {
                check(SwingUtilities.isEventDispatchThread(),"Callback outside EDT");
                check(!child.isEnabled(),"Parent completion enabled busy child");
                newDone.countDown();
            });
            DatabaseTask.run(child, () -> 3, value -> { applied.addAndGet(value); });
        });
        check(started.await(5,TimeUnit.SECONDS) && newDone.await(5,TimeUnit.SECONDS),"Worker did not progress");
        release.countDown();
        CountDownLatch restored=new CountDownLatch(1);
        SwingUtilities.invokeAndWait(() -> {
            javax.swing.Timer timer=new javax.swing.Timer(10,null);
            timer.addActionListener(event -> { if(child.isEnabled()) { timer.stop(); restored.countDown(); } });
            timer.start();
        });
        check(restored.await(5,TimeUnit.SECONDS),"Overlapping worker controls stayed disabled");
        SwingUtilities.invokeAndWait(() -> {
            check(applied.get()==3,"Stale worker overwrote latest result");
            check(panel.isEnabled() && child.isEnabled() && !disabled.isEnabled(),"Original enabled state not restored");
        });
    }

    private static void checkExclusiveAndSessionChange(AuthService auth) throws Exception {
        JPanel owner = new JPanel();
        CountDownLatch started = new CountDownLatch(1), release = new CountDownLatch(1);
        AtomicInteger writes = new AtomicInteger(), callbacks = new AtomicInteger();
        SwingUtilities.invokeAndWait(() -> {
            DatabaseTask.runExclusive(owner, () -> {
                check(!SwingUtilities.isEventDispatchThread(), "DB task ran on EDT");
                writes.incrementAndGet(); started.countDown();
                if (!release.await(5,TimeUnit.SECONDS)) throw new AssertionError("Worker release timed out");
                return true;
            }, value -> callbacks.incrementAndGet());
            DatabaseTask.runExclusive(owner, () -> { writes.incrementAndGet(); return true; }, value -> callbacks.incrementAndGet());
        });
        check(started.await(5,TimeUnit.SECONDS), "Exclusive worker did not start");
        // Even relogin as the same username must invalidate old callbacks.
        auth.logout(); auth.login("employee", PASSWORD);
        release.countDown();
        awaitUi(owner::isEnabled, "Session switch left owner disabled");
        check(writes.get() == 1 && callbacks.get() == 0, "Duplicate write or stale session callback");
        java.io.PrintStream originalError = System.err;
        java.io.ByteArrayOutputStream expectedError = new java.io.ByteArrayOutputStream();
        try {
            System.setErr(new java.io.PrintStream(expectedError));
            SwingUtilities.invokeAndWait(() -> DatabaseTask.run(owner, () -> { throw new SQLException("Expected offline failure"); }, value -> callbacks.incrementAndGet()));
            awaitUi(owner::isEnabled, "Failure left owner disabled");
        } finally { System.setErr(originalError); }
        check(expectedError.toString().contains("Expected offline failure"), "Worker failure was swallowed");
        check(callbacks.get() == 0, "Failed work invoked success callback");
    }

    private static void checkPayrollPanels() throws Exception {
        Session.getInstance().login(1,7,"employee","Payroll_Officer","QA");
        CountDownLatch[] delay = {new CountDownLatch(0), new CountDownLatch(0)};
        queryOverride = (sql, parameters) -> {
            if (sql.contains("SELECT OBJECT_ID")) return rows(Collections.singletonMap("1",1));
            if (sql.contains("FROM dbo.BANGLUONG bl")) return rows(
                    Map.of("MaBangLuong",1,"Thang",9,"Nam",2026,"TrangThai","CHUA_CHOT"),
                    Map.of("MaBangLuong",2,"Thang",10,"Nam",2026,"TrangThai","DA_CHOT"));
            if (sql.contains("WHERE MaBangLuong = ?")) {
                int period = (Integer) parameters.get(1);
                if (period == 1) {
                    delay[0].countDown();
                    try { if (!delay[1].await(5,TimeUnit.SECONDS)) throw new AssertionError("Detail release timed out"); }
                    catch (InterruptedException ex) { Thread.currentThread().interrupt(); throw new AssertionError(ex); }
                }
                return rows(Map.of("MaBangLuong",period,"MaChiTiet",period,"MaNV",period,"HoTen","Period " + period));
            }
            return rows();
        };
        try {
            BangLuongPanel[] payroll = new BangLuongPanel[1];
            SwingUtilities.invokeAndWait(() -> payroll[0] = new BangLuongPanel());
            JTable headers = field(payroll[0],"tblBangLuong"), details = field(payroll[0],"tblChiTiet");
            awaitUi(() -> headers.isEnabled() && headers.getRowCount() == 2, "Payroll header load failed");
            delay[0] = new CountDownLatch(1); delay[1] = new CountDownLatch(1);
            SwingUtilities.invokeAndWait(() -> headers.setRowSelectionInterval(0,0));
            check(delay[0].await(5,TimeUnit.SECONDS), "Old payroll detail did not start");
            SwingUtilities.invokeAndWait(() -> invoke(payroll[0],"loadBangLuong"));
            awaitUi(() -> headers.isEnabled() && headers.getRowCount() == 2, "Payroll reload failed");
            delay[1].countDown();
            awaitUi(details::isEnabled, "Invalidated payroll detail remained busy");
            SwingUtilities.invokeAndWait(() -> check(details.getRowCount() == 0, "Header reload resurrected stale details"));
            SwingUtilities.invokeAndWait(() -> invoke(payroll[0],"loadBangLuong",2));
            awaitUi(() -> headers.isEnabled() && details.isEnabled() && details.getRowCount() == 1, "Async payroll selection failed");
            SwingUtilities.invokeAndWait(() -> {
                check(headers.getSelectedRow() == 1, "Calculated period was not selected after header load");
                check(details.getValueAt(0,0).equals(2), "Selected payroll details mismatch");
            });

            delay[0] = new CountDownLatch(1); delay[1] = new CountDownLatch(1);
            BaoCaoPanel[] report = new BaoCaoPanel[1];
            SwingUtilities.invokeAndWait(() -> report[0] = new BaoCaoPanel());
            check(delay[0].await(5,TimeUnit.SECONDS), "Report period A did not start");
            JComboBox<?> periods = field(report[0],"cboKyLuong");
            JTable reportTable = field(report[0],"tblBaoCao");
            JButton close = field(report[0],"btnChotLuong"), reopen = field(report[0],"btnHuyChot");
            SwingUtilities.invokeAndWait(() -> {
                check(reportTable.getRowCount() == 0 && !close.isEnabled(), "Old report/action visible during load");
                periods.setSelectedIndex(1);
            });
            awaitUi(() -> reportTable.getRowCount() == 1 && "Period 2".equals(reportTable.getValueAt(0,1)), "Report period B failed");
            delay[1].countDown();
            awaitUi(reportTable::isEnabled, "Report remained busy");
            SwingUtilities.invokeAndWait(() -> {
                check("Period 2".equals(reportTable.getValueAt(0,1)), "Period A overwrote period B");
                check(!close.isVisible() && reopen.isVisible() && reopen.isEnabled(), "Report actions mismatch current period");
            });

            ChamCongPanel[] attendance = new ChamCongPanel[1];
            SwingUtilities.invokeAndWait(() -> attendance[0] = new ChamCongPanel());
            JComboBox<?> employees = field(attendance[0],"cboNhanVien");
            JTable attendanceDetails = field(attendance[0],"tblChiTiet"), summary = field(attendance[0],"tblTongHop");
            awaitUi(() -> attendanceDetails.isEnabled() && summary.isEnabled()
                    && employees.getClientProperty("db.request") == null, "Attendance loads did not complete");
            JButton add = field(attendance[0],"btnGhiNhan"), adjust = field(attendance[0],"btnDieuChinhCong");
            SwingUtilities.invokeAndWait(() -> check(!employees.isEnabled() && !add.isEnabled() && !adjust.isEnabled(),
                    "Worker reenabled Payroll attendance write controls"));
            check(queriesOnEdt.get() == 0, "Panel JDBC ran on EDT");
        } finally {
            delay[1].countDown(); queryOverride = null;
        }
    }

    private static void awaitUi(BooleanSupplier condition, String message) throws Exception {
        CountDownLatch ready = new CountDownLatch(1);
        javax.swing.Timer[] timer = new javax.swing.Timer[1];
        SwingUtilities.invokeAndWait(() -> {
            timer[0] = new javax.swing.Timer(10, event -> {
                if (condition.getAsBoolean()) { timer[0].stop(); ready.countDown(); }
            });
            timer[0].start();
        });
        boolean completed = ready.await(5,TimeUnit.SECONDS);
        SwingUtilities.invokeAndWait(() -> timer[0].stop());
        check(completed,message);
    }

    private static void checkEmployeeForm() throws Exception {
        Session.getInstance().login(1,7,"employee","HR_Manager","QA");
        CountDownLatch comboStarted = new CountDownLatch(1), comboRelease = new CountDownLatch(1);
        queryOverride = (sql, parameters) -> {
            if (sql.contains("FROM PHONGBAN")) {
                comboStarted.countDown();
                try { if (!comboRelease.await(5,TimeUnit.SECONDS)) throw new AssertionError("Department release timed out"); }
                catch (InterruptedException ex) { Thread.currentThread().interrupt(); throw new AssertionError(ex); }
                // A department can stop accepting new hires while old employees retain their association.
                if (sql.contains("WHERE TrangThai")) return rows(Map.of("MaPB",1,"TenPB","Active","TrangThai","HOAT_DONG"));
                return rows(Map.of("MaPB",1,"TenPB","Active","TrangThai","HOAT_DONG"),
                        Map.of("MaPB",2,"TenPB","Previous department","TrangThai","NGUNG_HOAT_DONG"));
            }
            if (sql.contains("FROM CHUCVU")) return rows(Map.of("MaCV",1,"TenCV","New position"),Map.of("MaCV",2,"TenCV","Previous position"));
            if (sql.contains("vw_NhanVien_PhongBan_ChucVu")) {
                Map<String,Object> employee = new HashMap<>();
                employee.put("MaNV",7); employee.put("HoTen","QA"); employee.put("NgaySinh",java.sql.Date.valueOf("1990-01-01"));
                employee.put("NgayVaoLam",java.sql.Date.valueOf("2020-02-03")); employee.put("DiaChi","Existing address");
                employee.put("GioiTinh","Nam"); employee.put("CCCD","123456789012"); employee.put("SoDienThoai","0901234567");
                employee.put("Email","qa@example.com"); employee.put("LuongCoBan",BigDecimal.TEN); employee.put("TrangThai","DANG_LAM_VIEC");
                employee.put("MaPB",2); employee.put("MaCV",2); employee.put("TenPB","Previous department"); employee.put("TenCV","Previous position");
                return rows(employee);
            }
            return rows();
        };
        try {
            NhanVienPanel[] panel = new NhanVienPanel[1];
            SwingUtilities.invokeAndWait(() -> panel[0] = new NhanVienPanel());
            JTable employees = field(panel[0],"tblNhanVien");
            JComboBox<?> departments = field(panel[0],"cboPhongBan"), positions = field(panel[0],"cboChucVu"), roles = field(panel[0],"cboVaiTro");
            check(comboStarted.await(5,TimeUnit.SECONDS), "Department load did not start");
            awaitUi(() -> employees.isEnabled() && employees.getRowCount() == 1, "Employee table load failed");
            SwingUtilities.invokeAndWait(() -> employees.setRowSelectionInterval(0,0));
            comboRelease.countDown();
            awaitUi(() -> departments.isEnabled() && positions.getItemCount() == 2, "Employee combos did not complete");
            SwingUtilities.invokeAndWait(() -> {
                try {
                    JTextField hireDate = field(panel[0],"txtNgayVaoLam"), address = field(panel[0],"txtDiaChi");
                    check("2020-02-03".equals(hireDate.getText()) && "Existing address".equals(address.getText()), "Employee edit lost date/address");
                    check(((PhongBan) departments.getSelectedItem()).getMaPB() == 2 && ((ChucVu) positions.getSelectedItem()).getMaCV() == 2,
                            "Slow combo load reassigned existing department/position");
                    check(roles.getItemCount() == 1 && "Employee".equals(roles.getItemAt(0)), "HR can select privileged role");
                    Method readForm = NhanVienPanel.class.getDeclaredMethod("layThongTinForm"); readForm.setAccessible(true);
                    NhanVien draft = (NhanVien) readForm.invoke(panel[0]);
                    check(draft.getNgayVaoLam().equals(LocalDate.of(2020,2,3)) && "Existing address".equals(draft.getDiaChi())
                            && draft.getMaPB() == 2 && draft.getMaCV() == 2, "Employee form snapshot changed persisted fields");
                } catch (ReflectiveOperationException ex) { throw new AssertionError(ex); }
            });
        } finally { comboRelease.countDown(); queryOverride = null; }
    }
    @SuppressWarnings("unchecked") private static <T> T field(Object owner, String name) throws ReflectiveOperationException {
        Field field = owner.getClass().getDeclaredField(name); field.setAccessible(true); return (T) field.get(owner);
    }
    private static void invoke(Object owner, String name, Object... arguments) {
        try {
            Class<?>[] types = Arrays.stream(arguments).map(value -> value instanceof Integer ? int.class : value.getClass()).toArray(Class<?>[]::new);
            Method method = owner.getClass().getDeclaredMethod(name,types); method.setAccessible(true); method.invoke(owner,arguments);
        } catch (ReflectiveOperationException ex) { throw new AssertionError(ex); }
    }
    @SafeVarargs private static ResultSet rows(Map<String,Object>... values) {
        int[] index = {-1};
        return proxy(ResultSet.class,(p,m,a) -> {
            if (m.getName().equals("next")) return ++index[0] < values.length;
            if (m.getName().startsWith("get") && a != null && a.length > 0) {
                Object value = values[index[0]].get(a[0].toString());
                if (m.getName().equals("getBigDecimal")) return value != null ? value : BigDecimal.ZERO;
                return value != null ? value : defaultValue(m.getReturnType());
            }
            return defaultValue(m.getReturnType());
        });
    }

    @SuppressWarnings("unchecked") private static <T> T proxy(Class<T> type,InvocationHandler handler) {
        return (T)Proxy.newProxyInstance(type.getClassLoader(),new Class<?>[]{type},handler);
    }
    private static Object defaultValue(Class<?> type) {
        if(type==boolean.class) return false; if(type==int.class) return 0; if(type==long.class) return 0L;
        if(type==double.class) return 0.0; if(type==float.class) return 0.0f; return null;
    }
    private static ResultSet profile(String user) {
        int[] row={0};
        return proxy(ResultSet.class,(p,m,a) -> {
            if(m.getName().equals("next")) return ++row[0]==1;
            if(m.getName().equals("getInt")) return 7;
            if(m.getName().equals("getString")) {
                switch((String)a[0]) {
                    case "MatKhau": return stored;
                    case "TrangThai": return active?"HOAT_DONG":"KHOA";
                    case "VaiTro": return "Employee";
                    case "TenDangNhap": return user;
                    default: return "QA";
                }
            }
            return defaultValue(m.getReturnType());
        });
    }
    private static Statement statement(String sql,String user,Class<? extends Statement> type) {
        Map<Integer,Object> parameters=new HashMap<>();
        return proxy(type,(p,m,a) -> {
            if(m.getName().startsWith("set") && a!=null && a.length==2 && a[0] instanceof Integer) parameters.put((Integer)a[0],a[1]);
            if(m.getName().equals("executeQuery")) {
                if (queryOverride != null && !sql.contains("sp_LayTaiKhoanHienTai")) {
                    if (SwingUtilities.isEventDispatchThread()) queriesOnEdt.incrementAndGet();
                    return queryOverride.apply(sql,parameters);
                }
                return profile(user);
            }
            if(m.getName().equals("executeUpdate")) { lastSql=sql; lastParameters=parameters; return 1; }
            if(m.getName().equals("execute") && sql.contains("sp_MigrateMatKhau")) stored=(String)parameters.get(1);
            return defaultValue(m.getReturnType());
        });
    }
    private static final class RecordingDriver implements Driver {
        public boolean acceptsURL(String url) { return url.startsWith("jdbc:prj-regression:"); }
        public Connection connect(String url,Properties properties) throws SQLException {
            if(!acceptsURL(url)) return null;
            if(!PASSWORD.equals(properties.getProperty("password"))) throw new SQLException("Invalid password","28000");
            connections++; lastUser=properties.getProperty("user"); String user=lastUser;
            return proxy(Connection.class,(p,m,a) -> {
                if(m.getName().equals("prepareCall")) return statement((String)a[0],user,CallableStatement.class);
                if(m.getName().equals("prepareStatement")) return statement((String)a[0],user,PreparedStatement.class);
                return defaultValue(m.getReturnType());
            });
        }
        public DriverPropertyInfo[] getPropertyInfo(String url,Properties properties) { return new DriverPropertyInfo[0]; }
        public int getMajorVersion(){return 1;} public int getMinorVersion(){return 0;}
        public boolean jdbcCompliant(){return false;} public Logger getParentLogger(){return Logger.getGlobal();}
    }
}
