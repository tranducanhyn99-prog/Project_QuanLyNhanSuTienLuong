import com.ui.auth.LoginFrame;
import com.ui.main.MainFrame;
import com.ui.baocao.BaoCaoPanel;
import com.ui.theme.UITheme;
import com.session.Session;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Rendering-only QA; no live SQL or OS DPI claim. */
public class LayoutQa {
 static Path out;
 static int checks;
 static void capture(JFrame frame,String name) throws Exception {
  frame.setSize(frame.getMinimumSize()); frame.setLocation(20,20); frame.setVisible(true); frame.validate();
  BufferedImage image=new BufferedImage(frame.getWidth(),frame.getHeight(),BufferedImage.TYPE_INT_RGB);
  Graphics2D g=image.createGraphics(); frame.printAll(g);g.dispose(); ImageIO.write(image,"png",out.resolve(name+".png").toFile());
  System.out.println(name+" logical="+frame.getSize()+" deviceTransform="+frame.getGraphicsConfiguration().getDefaultTransform());
  checkButtons(frame.getContentPane(),frame.getContentPane());
 }
 static void checkButtons(Container parent,Container root) {
  for(Component c:parent.getComponents()) {
   if(!c.isVisible())continue;
   if(c instanceof JButton && c.getWidth()>20 && !(c.getParent() instanceof JScrollBar)) {
    Rectangle bounds=SwingUtilities.convertRectangle(c.getParent(),c.getBounds(),root);
    if(!new Rectangle(0,0,root.getWidth(),root.getHeight()).contains(bounds)) throw new AssertionError("Button outside minimum frame: "+((JButton)c).getText()+" "+bounds);
    checks++;
   }
   if(c instanceof Container && !(c instanceof JScrollPane))checkButtons((Container)c,root);
  }
 }
 public static void main(String[] args)throws Exception {
  out=Paths.get(args[0]);Files.createDirectories(out);
  UITheme.setupGlobalUI();
  SwingUtilities.invokeAndWait(()->{
   try {
    LoginFrame login=new LoginFrame(); capture(login,"login-min");login.dispose();
    Session.getInstance().login(1,-1,"render_qa","DB_Admin",null);
    MainFrame main=new MainFrame();capture(main,"main-min");main.dispose();
    BaoCaoPanel report=new BaoCaoPanel();JFrame rf=new JFrame("Report render QA");rf.setMinimumSize(new Dimension(760,550));rf.add(report);capture(rf,"report-min");rf.dispose();
   } catch(Exception e){throw new RuntimeException(e);}
  });
  Thread.sleep(300);
  SwingUtilities.invokeAndWait(()->{for(Window w:Window.getWindows())w.dispose();Session.getInstance().logout();});
  System.out.println("PASS minimum-window button bounds: "+checks+" assertions. Java uiScale="+System.getProperty("sun.java2d.uiScale","default")+"; fixture session, report data not loaded.");
  System.exit(0);
 }
}
