/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.fxswing;

import com.jme3.app.Application;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javax.swing.JFrame;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

/**
 * @author huliqing
 */
public class JfxSwing {
 
    /**
     * 主界面
     */
    private JFrame mainFrame;
    
    /**
     * JWindow,包含JFX根节点JFXPanel
     */
    private JWindow jfxWindow;
    
    /**
     * JME app
     */
    private Application jmeApp;
    
    /**
     * JFX根节点，包含于JFXPanel下面。
     */
    private Pane jfxRoot;
    
    public JfxSwing() {}

    /**
     * 获取主界面
     * @return 
     */
    public JFrame getMainFrame() {
        return mainFrame;
    }

    public void setMainFrame(JFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    /**
     * 获取JME Application
     * @return 
     */
    public Application getJmeApp() {
        return jmeApp;
    }

    public void setJmeApp(Application jmeApp) {
        this.jmeApp = jmeApp;
    }

    /**
     * 获取JFX UI根节点容器,用这个根节点容器来添加JFX 组件，如：
     * <pre>
     * <code>
     * Platform.runLater(() -> {
     *      Button btn = new Button("This is a new button");
     *      jfxSwing.getJfxRoot().getChildren().add(btn);
     *  });
     * </code>
     * </pre>
     * @return 
     */
    public Pane getJfxRoot() {
        return jfxRoot;
    }

    public void setJfxRoot(Pane jfxRoot) {
        this.jfxRoot = jfxRoot;
    }

    /**
     * 获取JFX UI JWindow窗器，这个容器包含JFX根节点，层次关系是这样的: 
     * MainFrame -> JfxWindow -> JFXPanel -> JfxRoot
     * @return 
     */
    public JWindow getJfxWindow() {
        return jfxWindow;
    }

    public void setJfxWindow(JWindow jfxWindow) {
        this.jfxWindow = jfxWindow;
    }
    
    /**
     * Run task on swing thread.
     * @param runnable 
     */
    public static void runOnSwing(Runnable runnable) {
        SwingUtilities.invokeLater(runnable);
    }
    
    /**
     * Run task on jfx thread.
     * @param runnable 
     */
    public static void runOnJfx(Runnable runnable) {
        Platform.runLater(runnable);
    }
}