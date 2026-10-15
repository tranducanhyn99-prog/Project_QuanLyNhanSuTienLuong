import com.config.DatabaseConnection;
import com.service.AuthService;
import com.session.Session;
import com.ui.main.MainFrame;
import com.ui.theme.UITheme;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.BooleanSupplier;
import javax.swing.*;
import javax.imageio.ImageIO;

/** Live QA of production Swing handlers, automated (not a manual walkthrough). */
public class LiveGuiQa {
 static MainFrame frame;static JComponent panel;static Path out;static String prefix;
 static java.util.List<String> messages=new ArrayList<>();static javax.swing.Timer dialogs;static int checks;
 static String nextPassword="TV5_QA_Reset!2026";static String nextRole="HR_Manager";
 static <T>T edt(Callable<T> action)throws Exception {
  final Object[] value=new Object[1];final Throwable[] error=new Throwable[1];
  SwingUtilities.invokeAndWait(()->{try{value[0]=action.call();}catch(Throwable e){error[0]=e;}});
  if(error[0]!=null)throw new RuntimeException(error[0]);return (T)value[0];
 }
 static Object field(Object obj,String name)throws Exception {Field f=obj.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(obj);}
 static <T>T find(Container parent,Class<T> type){for(Component c:parent.getComponents()){if(type.isInstance(c))return type.cast(c);if(c instanceof Container){T found=find((Container)c,type);if(found!=null)return found;}}return null;}
 static void check(boolean ok,String msg){if(!ok)throw new AssertionError(msg);checks++;System.out.println("PASS "+msg);}
 static void await(BooleanSupplier condition,String description)throws Exception {
  long end=System.currentTimeMillis()+20000;while(System.currentTimeMillis()<end){if(edt(()->condition.getAsBoolean()))return;Thread.sleep(50);}throw new AssertionError("Timeout: "+description);
 }
 static boolean idle(){return panel.getClientProperty("db.request")==null;}
 static void screenshot(String name)throws Exception{edt(()->{BufferedImage image=new BufferedImage(frame.getWidth(),frame.getHeight(),BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();frame.printAll(g);g.dispose();ImageIO.write(image,"png",out.resolve(name+".png").toFile());return null;});}
 static void responder(){dialogs=new javax.swing.Timer(100,e->{for(Window window:Window.getWindows())if(window instanceof JDialog&&window.isShowing()){
  JOptionPane option=find((Container)window,JOptionPane.class);if(option==null)continue;
  JPasswordField password=find((Container)window,JPasswordField.class);if(password!=null)password.setText(nextPassword);
  JComboBox<?> role=find((Container)window,JComboBox.class);if(role!=null)role.setSelectedItem(nextRole);
  String text=flatten(option.getMessage());System.out.println("DIALOG "+((JDialog)window).getTitle()+" | "+text);messages.add(text);
  try{BufferedImage image=new BufferedImage(window.getWidth(),window.getHeight(),BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();window.printAll(g);g.dispose();ImageIO.write(image,"png",out.resolve("dialog-"+messages.size()+".png").toFile());}catch(Exception ex){throw new RuntimeException(ex);}
  option.setValue(option.getOptionType()==JOptionPane.YES_NO_OPTION?JOptionPane.YES_OPTION:JOptionPane.OK_OPTION);
 }});dialogs.start();}
 static String flatten(Object o){if(o==null)return "";if(o instanceof Object[]){StringJoiner j=new StringJoiner(" ");for(Object a:(Object[])o)j.add(flatten(a));return j.toString();}if(o instanceof JLabel)return ((JLabel)o).getText();if(o instanceof Container){StringJoiner j=new StringJoiner(" ");for(Component c:((Container)o).getComponents())j.add(flatten(c));return j.toString();}return o.toString();}
 static void click(String name)throws Exception{edt(()->{((JButton)field(panel,name)).doClick();return null;});}
 static void admin(boolean permitted)throws Exception {
  edt(()->{frame.openTab("Quản lý tài khoản",()->null);return null;});panel=edt(()->find(frame.getContentPane(),com.ui.admin.TaiKhoanPanel.class));
  JTable table=(JTable)field(panel,"tblTaiKhoan");await(()->table.getRowCount()==5,"five accounts");
  select(table,prefix+"b");screenshot("admin-list");check(table.getRowCount()==5,"GUI loads five owned profiles");
  int before=messages.size();click("btnKhoaMoKhoa");final int lockStart=before;await(()->messages.size()>=lockStart+2&&idle(),"lock result");
  await(()->table.isEnabled(),"table reload");
  String expected=permitted?"KHOA":"HOAT_DONG";check(status(table,prefix+"b").equals(expected),"lock profile "+expected);
  check(messages.get(messages.size()-1).contains(permitted?"thành công":"không có quyền"),"lock message matches server rights");
  if(permitted){check(loginDisabled(),"SQL owned login disabled after GUI lock");try(Connection c=DatabaseConnection.openForLogin(prefix+"b",System.getenv("QA_B_PASSWORD"))){throw new AssertionError("Disabled login accepted");}catch(SQLException ex){check(ex.getErrorCode()==18470,"locked SQL login rejects authentication (18470)");}}
  screenshot(permitted?"locked":"lock-denied");
  if(permitted){select(table,prefix+"b");before=messages.size();click("btnKhoaMoKhoa");final int b=before;await(()->messages.size()>=b+2&&idle(),"unlock result");await(()->table.isEnabled(),"unlock reload");check(status(table,prefix+"b").equals("HOAT_DONG"),"unlock profile active");check(!loginDisabled(),"SQL owned login enabled after GUI unlock");try(Connection c=DatabaseConnection.openForLogin(prefix+"b",System.getenv("QA_B_PASSWORD"))){check(c.isValid(2),"unlocked SQL login authenticates original password");}screenshot("unlocked");}
  select(table,prefix+"b");before=messages.size();click("btnDatLaiMatKhau");final int b=before;await(()->messages.size()>=b+2&&idle(),"reset result");
  check(messages.get(messages.size()-1).contains(permitted?"thành công":"không có quyền"),"reset message matches server rights");screenshot(permitted?"reset":"reset-denied");
  if(permitted){select(table,prefix+"b");before=messages.size();click("btnDoiVaiTro");final int c=before;await(()->messages.size()>=c+2&&idle(),"role result");await(()->table.isEnabled(),"role reload");check(statusRole(table,prefix+"b").equals("HR_Manager"),"role profile HR_Manager");screenshot("role-changed");}
 }
 static int row(JTable t,String user){for(int i=0;i<t.getRowCount();i++)if(user.equals(t.getValueAt(i,1)))return i;throw new AssertionError("Missing user: "+user);}
 static void select(JTable t,String user)throws Exception{edt(()->{int r=row(t,user);t.setRowSelectionInterval(r,r);return null;});}
 static String status(JTable t,String user){return t.getValueAt(row(t,user),5).toString();}
 static String statusRole(JTable t,String user){return t.getValueAt(row(t,user),4).toString();}
 static boolean loginDisabled()throws Exception{try(Connection c=DatabaseConnection.getConnection();PreparedStatement s=c.prepareStatement("SELECT is_disabled FROM sys.server_principals WHERE name=?")){s.setString(1,prefix+"b");try(ResultSet r=s.executeQuery()){if(!r.next())throw new AssertionError("Owned login metadata unavailable");return r.getBoolean(1);}}}
 static JButton button(Container c,String text){for(Component x:c.getComponents()){if(x instanceof JButton&&text.equals(((JButton)x).getText()))return (JButton)x;if(x instanceof Container){JButton found=button((Container)x,text);if(found!=null)return found;}}return null;}
 static void text(String name,String value)throws Exception{edt(()->{((JTextField)field(panel,name)).setText(value);return null;});}
 static void operation(String method,int dialogCount)throws Exception{int previous=messages.size();edt(()->{Method m=panel.getClass().getDeclaredMethod(method);m.setAccessible(true);m.invoke(panel);return null;});await(()->messages.size()>=previous+dialogCount&&idle(),method+" completes");}
 static int scalar(String sql,Object... args)throws Exception{try(Connection c=DatabaseConnection.getConnection();PreparedStatement s=c.prepareStatement(sql)){for(int i=0;i<args.length;i++)s.setObject(i+1,args[i]);try(ResultSet r=s.executeQuery()){if(!r.next())throw new AssertionError("Scalar has no row");return r.getInt(1);}}}
 static void optionalPhone()throws Exception{
  edt(()->{frame.openTab("Danh mục",()->null);return null;});panel=edt(()->find(frame.getContentPane(),com.ui.nhanvien.DanhMucPanel.class));JTable table=(JTable)field(panel,"tblPhongBan");await(()->table.isEnabled()&&table.getRowCount()>0,"catalogs");String name="TV5 Phone optional "+prefix;text("txtTenPB",name);text("txtSdtPB","");operation("xuLyThemPB",1);int id=scalar("SELECT MaPB FROM dbo.PHONGBAN WHERE TenPB=?",name);check(scalar("SELECT COUNT(*) FROM dbo.PHONGBAN WHERE MaPB=? AND SoDienThoai IS NULL",id)==1,"GUI blank phone creates SQL NULL");text("txtMaPB",String.valueOf(id));text("txtTenPB",name);text("txtSdtPB","   ");operation("xuLySuaPB",1);check(scalar("SELECT COUNT(*) FROM dbo.PHONGBAN WHERE MaPB=? AND SoDienThoai IS NULL",id)==1,"GUI whitespace phone update persists SQL NULL");screenshot("optional-phone-null");text("txtMaPB",String.valueOf(id));operation("xuLyXoaPB",2);check(scalar("SELECT COUNT(*) FROM dbo.PHONGBAN WHERE MaPB=?",id)==0,"optional-phone owned fixture removed");
 }
 static void phoneFinal()throws Exception{
  edt(()->{frame.openTab("Danh mục",()->null);return null;});panel=edt(()->find(frame.getContentPane(),com.ui.nhanvien.DanhMucPanel.class));JTable table=(JTable)field(panel,"tblPhongBan");await(()->table.isEnabled()&&table.getRowCount()>0,"catalogs");String name="TV5 Phone final "+prefix;text("txtTenPB",name);text("txtSdtPB","invalid");operation("xuLyThemPB",1);check(messages.get(messages.size()-1).equals("SĐT phòng ban: 10–15 chữ số hoặc để trống."),"invalid add phone shows final compact text");check(scalar("SELECT COUNT(*) FROM dbo.PHONGBAN WHERE TenPB=?",name)==0,"invalid add creates no SQL row");text("txtSdtPB","");operation("xuLyThemPB",1);int id=scalar("SELECT MaPB FROM dbo.PHONGBAN WHERE TenPB=?",name);check(scalar("SELECT COUNT(*) FROM dbo.PHONGBAN WHERE MaPB=? AND SoDienThoai IS NULL",id)==1,"blank add remains SQL NULL");text("txtMaPB",String.valueOf(id));text("txtTenPB",name);text("txtSdtPB","invalid");operation("xuLySuaPB",1);check(messages.get(messages.size()-1).equals("SĐT phòng ban: 10–15 chữ số hoặc để trống."),"invalid update phone shows final compact text");check(scalar("SELECT COUNT(*) FROM dbo.PHONGBAN WHERE MaPB=? AND SoDienThoai IS NULL",id)==1,"invalid update preserves SQL NULL row");text("txtSdtPB","   ");operation("xuLySuaPB",1);check(scalar("SELECT COUNT(*) FROM dbo.PHONGBAN WHERE MaPB=? AND SoDienThoai IS NULL",id)==1,"whitespace update remains SQL NULL");text("txtMaPB",String.valueOf(id));operation("xuLyXoaPB",2);check(scalar("SELECT COUNT(*) FROM dbo.PHONGBAN WHERE MaPB=?",id)==0,"phone final fixture cleaned");
 }
 static void attendance()throws Exception {
  edt(()->{frame.openTab("Chấm công",()->null);return null;});panel=edt(()->find(frame.getContentPane(),com.ui.chamcong.ChamCongPanel.class));JTable journal=(JTable)field(panel,"tblChiTiet"),summary=(JTable)field(panel,"tblTongHop");JComboBox<?> staff=(JComboBox<?>)field(panel,"cboNhanVien");await(()->journal.isEnabled()&&summary.isEnabled()&&staff.getItemCount()==3,"attendance loaded");
  int attempts=scalar("SELECT CONVERT(INT,current_value) FROM sys.sequences WHERE name='QA_AttendanceAttempts'");int before=messages.size();click("btnDiemDanhHangLoat");final int fail=before;await(()->messages.size()>=fail+2&&idle(),"batch duplicate rejection");
  check(scalar("SELECT COUNT(*) FROM dbo.CHAMCONG WHERE NgayChamCong=CAST(GETDATE() AS DATE)")==1,"GUI batch failure preserves only preexisting final-row duplicate");check(scalar("SELECT CONVERT(INT,current_value) FROM sys.sequences WHERE name='QA_AttendanceAttempts'")-attempts==2,"two real inserts reached SQL before duplicate, then rolled back");check(scalar("SELECT COUNT(*) FROM dbo.CHAMCONG cc JOIN dbo.TAIKHOAN tk ON tk.MaNV=cc.MaNV WHERE tk.TenDangNhap IN (?,?) AND NgayChamCong=CAST(GETDATE() AS DATE)",prefix+"a",prefix+"b")==0,"A/B batch rows absent after Java transaction rollback");screenshot("batch-rollback");
  int demo=scalar("SELECT MaNV FROM dbo.NHANVIEN WHERE Email='employee01@example.invalid'");edt(()->{for(int i=0;i<journal.getRowCount();i++)if(Integer.parseInt(journal.getValueAt(i,1).toString())==demo&&journal.getValueAt(i,3).toString().equals(java.time.LocalDate.now().toString()))journal.setRowSelectionInterval(i,i);return null;});before=messages.size();click("btnXoaChamCong");final int del=before;await(()->messages.size()>=del+2&&idle(),"remove owned duplicate fixture");check(scalar("SELECT COUNT(*) FROM dbo.CHAMCONG WHERE NgayChamCong=CAST(GETDATE() AS DATE)")==0,"GUI removes owned duplicate fixture before valid batch");
  before=messages.size();click("btnDiemDanhHangLoat");final int success=before;await(()->messages.size()>=success+2&&idle(),"valid batch committed");check(scalar("SELECT COUNT(*) FROM dbo.CHAMCONG WHERE NgayChamCong=CAST(GETDATE() AS DATE)")==3,"GUI valid attendance batch commits all three rows");await(()->journal.isEnabled()&&summary.isEnabled(),"attendance refreshed");screenshot("batch-success");
  int a=scalar("SELECT MaNV FROM dbo.TAIKHOAN WHERE TenDangNhap=?",prefix+"a");edt(()->{((JTabbedPane)field(panel,"tabbedPane")).setSelectedIndex(1);for(int i=0;i<summary.getRowCount();i++)if(Integer.parseInt(summary.getValueAt(i,0).toString())==a)summary.setRowSelectionInterval(i,i);return null;});SwingUtilities.invokeLater(()->{try{((JButton)field(panel,"btnDieuChinhCong")).doClick();}catch(Exception e){throw new RuntimeException(e);}});
  await(()->Arrays.stream(Window.getWindows()).anyMatch(w->w instanceof com.ui.chamcong.DieuChinhChamCongDialog&&w.isShowing()),"adjustment dialog opens");com.ui.chamcong.DieuChinhChamCongDialog adjust=edt(()->(com.ui.chamcong.DieuChinhChamCongDialog)Arrays.stream(Window.getWindows()).filter(w->w instanceof com.ui.chamcong.DieuChinhChamCongDialog&&w.isShowing()).findFirst().get());JTable rows=(JTable)field(adjust,"tblChiTiet");await(()->rows.isEnabled()&&rows.getRowCount()==2,"A October adjustment rows");
  edt(()->{for(int i=0;i<rows.getRowCount();i++)if(rows.getValueAt(i,1).toString().equals(java.time.LocalDate.now().toString()))rows.setRowSelectionInterval(i,i);((JComboBox<?>)field(adjust,"cboTrangThai")).setSelectedItem("DI_TRE");Calendar time=Calendar.getInstance();time.set(Calendar.HOUR_OF_DAY,9);time.set(Calendar.MINUTE,0);time.set(Calendar.SECOND,0);((JSpinner)field(adjust,"spnGioVao")).setValue(time.getTime());return null;});before=messages.size();edt(()->{((JButton)field(adjust,"btnCapNhat")).doClick();return null;});final int change=before;await(()->messages.size()>change&&adjust.getRootPane().getClientProperty("db.request")==null,"adjustment commits");check(scalar("SELECT COUNT(*) FROM dbo.CHAMCONG WHERE MaNV=? AND NgayChamCong=CAST(GETDATE() AS DATE) AND TrangThai=N'DI_TRE' AND GioVao='09:00'",a)==1,"GUI monthly adjustment updates actual attendance status/time");check(adjust.isDataChanged(),"adjustment signals parent reload");
  edt(()->{BufferedImage image=new BufferedImage(adjust.getWidth(),adjust.getHeight(),BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();adjust.printAll(g);g.dispose();ImageIO.write(image,"png",out.resolve("monthly-adjustment.png").toFile());((JButton)field(adjust,"btnDong")).doClick();return null;});await(()->summary.isEnabled(),"parent summary reloaded");check(scalar("SELECT SoLanDiTre FROM dbo.vw_TongHopChamCongThang WHERE MaNV=? AND Thang=10 AND Nam=2026",a)==1,"SQL monthly summary matches adjusted late day");screenshot("monthly-summary-reloaded");
 }
 static void allowances()throws Exception{
  openReport();int before=messages.size();click("btnChotLuong");final int close=before;await(()->messages.size()>=close+2&&idle(),"close payroll before source freeze tests");JLabel state=(JLabel)field(panel,"lblTrangThai");await(()->state.getText().equals("Đã chốt"),"closed period loaded");
  edt(()->{frame.openTab("Phụ cấp / Khấu trừ",()->null);return null;});panel=edt(()->find(frame.getContentPane(),com.ui.luong.PhuCapKhauTruPanel.class));JTable pc=(JTable)field(panel,"tblPhuCap"),kt=(JTable)field(panel,"tblKhauTru");JComboBox<?> employees=(JComboBox<?>)field(panel,"cbNhanVienPC");await(()->employees.getItemCount()>0&&pc.isEnabled()&&kt.isEnabled(),"allowance fixtures loaded");String p="TV5_GUI_PC_"+prefix,k="TV5_GUI_KT_"+prefix;
  text("txtTenPhuCap",p);text("txtSoTienPC","7000");operation("handleThemPhuCap",1);check(scalar("SELECT COUNT(*) FROM dbo.PHUCAPNHANVIEN WHERE TenPhuCap=?",p)==0,"GUI rejects allowance insert on closed period");text("txtTenKhauTru",k);text("txtSoTienKT","3000");operation("handleThemKhauTru",1);check(scalar("SELECT COUNT(*) FROM dbo.KHAUTRUNHANVIEN WHERE TenKhauTru=?",k)==0,"GUI rejects deduction insert on closed period");
  edt(()->{pc.setRowSelectionInterval(0,0);return null;});int original=(Integer)pc.getValueAt(0,0);operation("handleXoaPhuCap",2);check(scalar("SELECT COUNT(*) FROM dbo.PHUCAPNHANVIEN WHERE MaPCNV=?",original)==1,"GUI rejects deleting existing allowance on closed period");operation("handleXoaKyLuong",2);check(scalar("SELECT COUNT(*) FROM dbo.BANGLUONG WHERE Thang=10 AND Nam=2026 AND TrangThai='DA_CHOT'")==1,"GUI rejects deleting closed payroll header");screenshot("closed-period-source-protected");
  edt(()->{frame.openTab("Báo cáo tổng hợp",()->null);return null;});panel=edt(()->find(frame.getContentPane(),com.ui.baocao.BaoCaoPanel.class));click("btnLamMoi");JTable report=(JTable)field(panel,"tblBaoCao");await(()->report.isEnabled()&&report.getRowCount()>0,"closed report refreshed");before=messages.size();click("btnHuyChot");final int reopen=before;await(()->messages.size()>=reopen+2&&idle(),"reopen for allowances");
  edt(()->{frame.openTab("Phụ cấp / Khấu trừ",()->null);return null;});panel=edt(()->find(frame.getContentPane(),com.ui.luong.PhuCapKhauTruPanel.class));operation("handleThemPhuCap",1);check(scalar("SELECT COUNT(*) FROM dbo.PHUCAPNHANVIEN WHERE TenPhuCap=? AND SoTien=7000",p)==1,"GUI allowance inserted on draft period");operation("handleThemKhauTru",1);check(scalar("SELECT COUNT(*) FROM dbo.KHAUTRUNHANVIEN WHERE TenKhauTru=? AND SoTien=3000",k)==1,"GUI deduction inserted on draft period");screenshot("allowance-deduction-added");
  await(()->pc.isEnabled()&&kt.isEnabled(),"source tables reloaded");edt(()->{for(int i=0;i<pc.getRowCount();i++)if(p.equals(pc.getValueAt(i,3)))pc.setRowSelectionInterval(i,i);return null;});operation("handleXoaPhuCap",2);check(scalar("SELECT COUNT(*) FROM dbo.PHUCAPNHANVIEN WHERE TenPhuCap=?",p)==0,"GUI owned allowance deleted");await(()->kt.isEnabled(),"deductions loaded");edt(()->{for(int i=0;i<kt.getRowCount();i++)if(k.equals(kt.getValueAt(i,3)))kt.setRowSelectionInterval(i,i);return null;});operation("handleXoaKhauTru",2);check(scalar("SELECT COUNT(*) FROM dbo.KHAUTRUNHANVIEN WHERE TenKhauTru=?",k)==0,"GUI owned deduction deleted");operation("handleXoaKyLuong",2);check(scalar("SELECT COUNT(*) FROM dbo.BANGLUONG WHERE Thang=10 AND Nam=2026")==0,"GUI draft payroll header deleted");check(scalar("SELECT COUNT(*) FROM dbo.CHITIETBANGLUONG ct LEFT JOIN dbo.BANGLUONG b ON b.MaBangLuong=ct.MaBangLuong WHERE b.MaBangLuong IS NULL")==0,"draft delete leaves no orphan salary details");screenshot("draft-payroll-deleted");
 }
 static void catalogsAndEmployees()throws Exception {
  edt(()->{frame.openTab("Danh mục",()->null);return null;});panel=edt(()->find(frame.getContentPane(),com.ui.nhanvien.DanhMucPanel.class));JTable pb=(JTable)field(panel,"tblPhongBan"),cv=(JTable)field(panel,"tblChucVu");await(()->pb.isEnabled()&&cv.isEnabled()&&pb.getRowCount()>0,"catalogs loaded");
  String pn="TV5 GUI PB "+prefix,cn="TV5 GUI CV "+prefix;
  text("txtTenPB",pn);text("txtSdtPB","invalid");operation("xuLyThemPB",1);check(scalar("SELECT COUNT(*) FROM dbo.PHONGBAN WHERE TenPB=?",pn)==0,"invalid department phone rejected without row");
  text("txtSdtPB","0900000201");operation("xuLyThemPB",1);int pi=scalar("SELECT MaPB FROM dbo.PHONGBAN WHERE TenPB=?",pn);check(pi>0,"GUI department created");text("txtMaPB",String.valueOf(pi));text("txtTenPB",pn+" Updated");text("txtSdtPB","invalid");operation("xuLySuaPB",1);check(scalar("SELECT COUNT(*) FROM dbo.PHONGBAN WHERE MaPB=? AND TenPB=?",pi,pn)==1,"invalid update phone preserves department SQL row");text("txtSdtPB","0900000202");operation("xuLySuaPB",1);check(scalar("SELECT COUNT(*) FROM dbo.PHONGBAN WHERE MaPB=? AND TenPB=?",pi,pn+" Updated")==1,"GUI department updated in SQL");screenshot("catalog-department-updated");text("txtMaPB",String.valueOf(pi));operation("xuLyXoaPB",2);check(scalar("SELECT COUNT(*) FROM dbo.PHONGBAN WHERE MaPB=?",pi)==0,"GUI unused department deleted");
  text("txtTenCV",cn);text("txtPhuCapCV","-1");operation("xuLyThemCV",1);check(scalar("SELECT COUNT(*) FROM dbo.CHUCVU WHERE TenCV=?",cn)==0,"negative position allowance rejected");text("txtPhuCapCV","5000");operation("xuLyThemCV",1);int ci=scalar("SELECT MaCV FROM dbo.CHUCVU WHERE TenCV=?",cn);check(ci>0,"GUI position created");text("txtMaCV",String.valueOf(ci));text("txtTenCV",cn+" Updated");text("txtPhuCapCV","7500");operation("xuLySuaCV",1);check(scalar("SELECT CONVERT(INT,PhuCapChucVu) FROM dbo.CHUCVU WHERE MaCV=?",ci)==7500,"GUI position allowance updated");screenshot("catalog-position-updated");text("txtMaCV",String.valueOf(ci));operation("xuLyXoaCV",2);check(scalar("SELECT COUNT(*) FROM dbo.CHUCVU WHERE MaCV=?",ci)==0,"GUI unused position deleted");
  edt(()->{frame.openTab("Nhân viên",()->null);return null;});panel=edt(()->find(frame.getContentPane(),com.ui.nhanvien.NhanVienPanel.class));JTable staff=(JTable)field(panel,"tblNhanVien");JComboBox<?> departments=(JComboBox<?>)field(panel,"cboPhongBan");await(()->staff.isEnabled()&&departments.getItemCount()>0,"employee lists loaded");edt(()->{find(panel,JTabbedPane.class).setSelectedIndex(1);return null;});
  String email="tv5_gui_employee_"+prefix+"@example.invalid";text("txtHoTen","TV5 GUI Employee");text("txtNgaySinh","2008-10-14");text("txtNgayVaoLam","2026-10-13");text("txtCCCD","681000000001");text("txtSoDienThoai","0681000001");text("txtEmail",email);text("txtLuongCoBan","1000000");operation("xuLyThemNhanVien",1);check(scalar("SELECT COUNT(*) FROM dbo.NHANVIEN WHERE Email=?",email)==0,"GUI rejects day before eighteenth birthday");
  text("txtNgaySinh","1990-01-01");edt(()->{((JCheckBox)field(panel,"chkCapTaiKhoan")).setSelected(true);return null;});text("txtTenDangNhap",prefix+"b");text("txtMatKhau","TV5_QA_Employee!2026");operation("xuLyThemNhanVien",1);check(scalar("SELECT COUNT(*) FROM dbo.NHANVIEN WHERE Email=?",email)==0,"GUI duplicate profile rolls back employee insertion");check(scalar("SELECT COUNT(*) FROM dbo.TAIKHOAN WHERE TenDangNhap=?",prefix+"b")==1,"duplicate-profile failure preserves existing owned B");
  edt(()->{((JCheckBox)field(panel,"chkCapTaiKhoan")).setSelected(false);return null;});operation("xuLyThemNhanVien",1);int employee=scalar("SELECT MaNV FROM dbo.NHANVIEN WHERE Email=?",email);check(employee>0,"GUI employee created without unrequested SQL login");
  text("txtMaNV",String.valueOf(employee));text("txtHoTen","TV5 GUI Employee Updated");text("txtNgaySinh","1990-01-01");text("txtNgayVaoLam","2026-10-13");text("txtCCCD","681000000001");text("txtSoDienThoai","0681000001");text("txtEmail",email);text("txtLuongCoBan","1100000");operation("xuLyCapNhatNhanVien",1);check(scalar("SELECT CONVERT(INT,LuongCoBan) FROM dbo.NHANVIEN WHERE MaNV=?",employee)==1100000,"GUI employee update persists salary");screenshot("employee-updated");text("txtMaNV",String.valueOf(employee));operation("xuLyXoaNhanVien",2);check(scalar("SELECT COUNT(*) FROM dbo.NHANVIEN WHERE MaNV=?",employee)==0,"GUI unused employee deleted");check(scalar("SELECT COUNT(*) FROM dbo.LICHSULUONG WHERE MaNV=?",employee)==0,"employee delete cleans owned salary history");
  int a=scalar("SELECT MaNV FROM dbo.TAIKHOAN WHERE TenDangNhap=?",prefix+"a");text("txtMaNV",String.valueOf(a));operation("xuLyXoaNhanVien",2);check(scalar("SELECT COUNT(*) FROM dbo.NHANVIEN WHERE MaNV=?",a)==1,"GUI rejects deleting employee with payroll/attendance");screenshot("employee-delete-protected");
 }
 static void switchLogin(String suffix,String password)throws Exception {
  edt(()->{frame.dispose();return null;});new AuthService().logout();new AuthService().login(prefix+suffix,password);
  edt(()->{frame=new MainFrame();frame.setSize(frame.getMinimumSize());frame.setLocation(20,20);frame.setVisible(true);return null;});
 }
 static void openReport()throws Exception {edt(()->{frame.openTab("Báo cáo tổng hợp",()->null);return null;});panel=edt(()->find(frame.getContentPane(),com.ui.baocao.BaoCaoPanel.class));JTable table=(JTable)field(panel,"tblBaoCao");await(()->table.isEnabled()&&table.getRowCount()>0,"report rows");}
 static void waitFile(String name)throws Exception{long end=System.currentTimeMillis()+30000;Path p=out.getParent().resolve(name);while(!Files.exists(p)&&System.currentTimeMillis()<end)Thread.sleep(50);check(Files.exists(p),"external QA marker "+name);}
 static void sessionSwitch()throws Exception {
  openReport();JTable oldTable=(JTable)field(panel,"tblBaoCao");Files.writeString(out.getParent().resolve("report-ready.txt"),"admin report initially loaded");waitFile("read-lock-held.txt");
  edt(()->{Method load=panel.getClass().getDeclaredMethod("loadChiTietByKyLuong");load.setAccessible(true);load.invoke(panel);return null;});await(()->!oldTable.isEnabled(),"old report read in flight");Files.writeString(out.getParent().resolve("old-read-pending.txt"),"admin read started");
  waitFile("old-read-verified.txt");switchLogin("a",System.getenv("QA_A_PASSWORD"));check(Session.getInstance().hasRole("Employee"),"new Employee identity active while old Admin request pending");
  edt(()->{frame.openTab("Phiếu lương cá nhân",()->null);return null;});panel=edt(()->find(frame.getContentPane(),com.ui.baocao.BaoCaoPanel.class));JTable own=(JTable)field(panel,"tblBaoCao");Files.writeString(out.getParent().resolve("new-session-ready.txt"),"employee report request submitted before old read released");waitFile("read-lock-released.txt");
  await(()->own.isEnabled()&&own.getRowCount()==2,"new employee report loaded");await(()->oldTable.isEnabled(),"old admin worker completed");
  check(oldTable.getRowCount()==0,"old Admin callback discarded after real logout/login identity change");check(own.getColumnCount()==9,"new Employee report columns preserve own-slip schema");
  java.util.List<com.model.ChiTietBangLuong> slips=new com.service.PayrollService().getPhieuLuongCaNhan();for(com.model.ChiTietBangLuong slip:slips)check(slip.getMaNV()==Session.getInstance().getMaNV(),"post-switch SQL slip belongs to new Employee identity");
  check(edt(()->((java.util.List<?>)field(frame,"navItems")).size())==2,"post-switch Employee menu has only home/report");screenshot("employee-after-pending-admin-read");
 }
 static void payrollLifecycle()throws Exception {
  edt(()->{frame.openTab("Tính bảng lương",()->null);return null;});panel=edt(()->find(frame.getContentPane(),com.ui.luong.BangLuongPanel.class));JTable payroll=(JTable)field(panel,"tblBangLuong");await(()->payroll.isEnabled()&&payroll.getRowCount()==2,"two draft periods");
  edt(()->{((JSpinner)field(panel,"spnThang")).setValue(10);((JSpinner)field(panel,"spnNam")).setValue(2026);return null;});int before=messages.size();edt(()->{button(panel,"Tính / cập nhật lương").doClick();return null;});final int start=before;await(()->messages.size()>=start+2&&idle(),"GUI calculation");
  com.service.PayrollService service=new com.service.PayrollService();com.model.BangLuong period=service.timBangLuong(10,2026);check(period!=null&&!period.isDaChot()&&period.getTongThucNhan().compareTo(new java.math.BigDecimal("1230000"))==0,"GUI calculation matches source SQL October total 1230000");screenshot("payroll-calculated");
  openReport();before=messages.size();click("btnChotLuong");final int closeStart=before;await(()->messages.size()>=closeStart+2&&idle(),"GUI close payroll");JLabel state=(JLabel)field(panel,"lblTrangThai");await(()->state.getText().equals("Đã chốt"),"closed report state");check(service.timBangLuong(10,2026).isDaChot(),"GUI close commits DA_CHOT in SQL");screenshot("payroll-closed");
  switchLogin("a",System.getenv("QA_A_PASSWORD"));openReport();JTable own=(JTable)field(panel,"tblBaoCao");java.util.List<com.model.ChiTietBangLuong> slips=service.getPhieuLuongCaNhan();check(own.getRowCount()==2&&slips.size()==2,"Employee A GUI shows two own periods");for(com.model.ChiTietBangLuong slip:slips)check(slip.getMaNV()==Session.getInstance().getMaNV(),"Employee slip only mapped MaNV");
  check(edt(()->((java.util.List<?>)field(frame,"navItems")).size())==2,"Employee navigation contains HOME and BAO_CAO only");screenshot("employee-a-own-slips");
  for(String suffix:new String[]{"hr","payroll"}){switchLogin(suffix,System.getenv("QA_"+suffix.toUpperCase()+"_PASSWORD"));String role=suffix.equals("hr")?"HR_Manager":"Payroll_Officer";check(Session.getInstance().hasRole(role),"real SQL "+role+" login");java.util.List<?> nav=edt(()->(java.util.List<?>)field(frame,"navItems"));boolean admin=false,pay=false,hr=false;for(Object item:nav){String key=(String)field(item,"key");admin|=key.equals("TAI_KHOAN");pay|=key.equals("BANG_LUONG");hr|=key.equals("NHAN_VIEN");}check(!admin&&pay==suffix.equals("payroll")&&hr==suffix.equals("hr"),"real "+role+" navigation matches role");screenshot("role-"+suffix);}
  switchLogin("admin",System.getenv("QA_ADMIN_PASSWORD"));openReport();before=messages.size();click("btnHuyChot");final int reopen=before;await(()->messages.size()>=reopen+2&&idle(),"GUI reopen payroll");JLabel newState=(JLabel)field(panel,"lblTrangThai");await(()->newState.getText().equals("Bản nháp"),"reopened report state");check(!service.timBangLuong(10,2026).isDaChot(),"GUI reopen commits CHUA_CHOT in SQL");screenshot("payroll-reopened");
 }
 static void reportOutage()throws Exception {
  edt(()->{frame.openTab("Báo cáo tổng hợp",()->null);return null;});panel=edt(()->find(frame.getContentPane(),com.ui.baocao.BaoCaoPanel.class));
  JTable table=(JTable)field(panel,"tblBaoCao");JComboBox<?> periods=(JComboBox<?>)field(panel,"cboKyLuong");
  await(()->periods.getItemCount()>=2&&table.isEnabled()&&table.getRowCount()>0,"report fixture loaded");screenshot("report-before-outage");
  edt(()->{for(int i=0;i<12;i++)periods.setSelectedIndex(i%2);periods.setSelectedIndex(0);return null;});
  await(()->table.isEnabled()&&table.getRowCount()>0,"latest period completed");
  com.model.BangLuong selected=(com.model.BangLuong)edt(()->periods.getSelectedItem());
  java.util.List<com.model.ChiTietBangLuong> expected=new com.service.PayrollService().getChiTietByBangLuong(selected.getMaBangLuong());
  check(table.getRowCount()==expected.size(),"rapid period switch leaves selected period row count");
  java.math.BigDecimal total=java.math.BigDecimal.ZERO;for(com.model.ChiTietBangLuong row:expected)total=total.add(row.getThucNhan());
  JLabel label=(JLabel)field(panel,"lblTongThucNhan");String money=new java.text.DecimalFormat("#,##0").format(total);
  check(label.getText().contains(money),"rapid period switch leaves selected period SQL total "+money);screenshot("report-period-switch");
  Path pause=out.getParent().resolve("relay.pause");Files.writeString(pause,"QA report outage");Thread.sleep(500);
  int previous=messages.size();click("btnLamMoi");await(()->messages.size()>previous,"report read failure dialog");
  check(messages.get(messages.size()-1).contains("Không kết nối"),"report outage has short connection message");
  check(((JButton)field(panel,"btnLamMoi")).isEnabled(),"report reload button restored after failure");check(table.getRowCount()==0,"old report rows cleared on failed reload");screenshot("report-outage");
  Files.delete(pause);Thread.sleep(500);click("btnLamMoi");await(()->table.isEnabled()&&table.getRowCount()>0,"report recovery");
  check(periods.getItemCount()>=2,"report periods load again after relay recovery");screenshot("report-recovered");
 }
 static void inflightWrite()throws Exception {
  edt(()->{frame.openTab("Quản lý tài khoản",()->null);return null;});panel=edt(()->find(frame.getContentPane(),com.ui.admin.TaiKhoanPanel.class));JTable table=(JTable)field(panel,"tblTaiKhoan");
  await(()->table.getRowCount()==5,"admin loaded before SQL blocker");select(table,prefix+"b");
  Files.writeString(out.getParent().resolve("admin-ready.txt"),"GUI list loaded before external SQL blocker");
  Path held=out.getParent().resolve("lock-held.txt");long end=System.currentTimeMillis()+30000;while(!Files.exists(held)&&System.currentTimeMillis()<end)Thread.sleep(50);check(Files.exists(held),"QA SQL blocker holds target profile");
  int previous=messages.size();SwingUtilities.invokeLater(()->{try{((JButton)field(panel,"btnKhoaMoKhoa")).doClick();}catch(Exception e){throw new RuntimeException(e);}});
  await(()->panel.getClientProperty("db.request")!=null,"SQL write scheduled");Files.writeString(out.getParent().resolve("write-pending.txt"),"GUI production lock handler pending");
  long start=System.nanoTime();edt(()->{((JButton)field(panel,"btnKhoaMoKhoa")).doClick();return null;});
  check((System.nanoTime()-start)/1000000<1000,"EDT responds while write is blocked; repeated click ignored while button disabled");screenshot("write-in-flight");
  await(()->messages.size()>=previous+2&&idle(),"in-flight connection error");check(messages.get(messages.size()-1).contains("Không kết nối"),"in-flight disconnect displays connection failure");
  check(((JButton)field(panel,"btnKhoaMoKhoa")).isEnabled(),"write button restores after disconnect");screenshot("write-disconnected");
  Path restored=out.getParent().resolve("sql-rollback-verified.txt");end=System.currentTimeMillis()+30000;while(!Files.exists(restored)&&System.currentTimeMillis()<end)Thread.sleep(50);check(Files.exists(restored),"external DBA verifies rollback before reload/retry");
  click("btnLamMoi");await(()->table.isEnabled()&&table.getRowCount()==5,"reload after write outage");check(status(table,prefix+"b").equals("HOAT_DONG"),"interrupted lock remains active; no automatic retry");screenshot("write-recovered");
 }
 public static void main(String[] args)throws Exception {
  String mode=args[0];prefix=args[1];out=Paths.get(args[2]);Files.createDirectories(out);
  if(!prefix.matches("tv5f_[a-f0-9]{8}_"))throw new IllegalArgumentException("Owned fixture prefix required");
  try {
   UITheme.setupGlobalUI();new AuthService().login(prefix+"admin",System.getenv("QA_ADMIN_PASSWORD"));
   edt(()->{frame=new MainFrame();frame.setSize(frame.getMinimumSize());frame.setLocation(20,20);frame.setVisible(true);responder();return null;});
   if(mode.startsWith("admin"))admin(mode.equals("admin-allowed"));else if(mode.equals("report-outage"))reportOutage();else if(mode.equals("inflight-write"))inflightWrite();else if(mode.equals("payroll-lifecycle"))payrollLifecycle();else if(mode.equals("session-switch"))sessionSwitch();else if(mode.equals("tv1-crud"))catalogsAndEmployees();else if(mode.equals("phone-optional"))optionalPhone();else if(mode.equals("tv3-allowances"))allowances();else if(mode.equals("tv2-attendance"))attendance();else if(mode.equals("phone-final"))phoneFinal();else throw new IllegalArgumentException("Unknown mode");
   edt(()->{frame.dispose();return null;});new AuthService().logout();
   if(mode.equals("admin-allowed")){
    try{new AuthService().login(prefix+"b",System.getenv("QA_B_PASSWORD"));throw new AssertionError("Old password accepted");}catch(SQLException expected){check(expected.getErrorCode()==18456,"old SQL password rejected");}
    new AuthService().login(prefix+"b",nextPassword);check(Session.getInstance().hasRole("HR_Manager"),"new password authenticates profile and changed SQL role");
   } else if(mode.equals("admin-denied")){new AuthService().login(prefix+"b",System.getenv("QA_B_PASSWORD"));check(Session.getInstance().hasRole("Employee"),"denied reset preserves original login password and role");}
   System.out.println("PASS live GUI "+mode+": "+checks+" assertions. Production handlers; dialogs automated; no manual-test claim.");
  } finally {new AuthService().logout();edt(()->{if(dialogs!=null)dialogs.stop();for(Window w:Window.getWindows())w.dispose();return null;});}
  System.exit(0);
 }
}
