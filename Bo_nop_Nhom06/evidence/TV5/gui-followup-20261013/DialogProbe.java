import com.service.DanhMucService;
import com.session.Session;
import com.ui.main.MainFrame;
import com.ui.theme.UITheme;
import javax.swing.*;
public class DialogProbe {
 public static void main(String[] args){UITheme.setupGlobalUI();Session.getInstance().login(1,-1,"visual_qa","DB_Admin",null);SwingUtilities.invokeLater(()->{MainFrame f=new MainFrame();f.setSize(f.getMinimumSize());f.setLocation(20,20);f.setVisible(true);try{new DanhMucService().themPhongBan("QA","invalid");throw new AssertionError("Invalid phone accepted");}catch(Exception e){JOptionPane.showMessageDialog(f,e.getMessage(),"TV5 Phone native visual QA",JOptionPane.ERROR_MESSAGE);}finally{f.dispose();Session.getInstance().logout();System.exit(0);}});}
}
