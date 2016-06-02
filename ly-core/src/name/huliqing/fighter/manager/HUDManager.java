/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.fighter.manager;

import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import java.util.List;
import name.huliqing.fighter.Common;
import name.huliqing.fighter.ui.LinearLayout;
import name.huliqing.fighter.ui.Text;
import name.huliqing.fighter.ui.UI;
import name.huliqing.fighter.ui.UI.Corner;

/**
 * 处理所有界面上的一切信息，UI,Message等,弹窗等。
 * @author huliqing
 */
public class HUDManager {
    private static Node localRoot;
    private static Messager messager;
    private static boolean enabled = true;
    
    // 初始化UI
    public static void init(Node appGuiRoot) {
        localRoot = new Node("HUDLocalRoot");
        appGuiRoot.attachChild(localRoot);
        
        messager = new Messager(Common.getSettings().getWidth(), Common.getSettings().getHeight() * 0.25f);
        localRoot.attachChild(messager);
    }
    
    public static void cleanup() {
        if (localRoot != null) {
            localRoot.removeFromParent();
            localRoot = null;
        }
    }
    
    // ==== message    
    
    public static void setEnabled(boolean enabled) {
        HUDManager.enabled = enabled;
    }
    
    public static void setDragEnabled(boolean dragEnabled) {
        messager.setDragEnabled(dragEnabled);
    }
    
    public static void showMessage(String mess, ColorRGBA color) {
        if (!enabled) {
            return;
        }
        messager.addMessage(mess, color);
    }
    
    public static void setDisplayTime(float displayTime) {
        messager.setUseTime(displayTime);
    }
    
    // =========================================================================
    private static class Messager extends Node {
//        private Logger logger = Logger.getLogger(Messager.class.getName());
        private int limit = 6; // 最多同时显示多少条信息
        private float useTime = 30;
        private float time;
        
        private LinearLayout messPanel;
        private int visible;
        
        // 设定的固定高度，由于文字长短不一，实际的messPanel的高度可能会大于这个值
        private float fixedHeight;
        // 每一行的默认平均高度
        private float avgHeight;

        public Messager(float width, float height) {
            fixedHeight = height;
            avgHeight = fixedHeight / limit;
            
            messPanel = new LinearLayout();
            messPanel.setWidth(width);
            messPanel.setHeight(fixedHeight);
            messPanel.setToCorner(Corner.LB);
            messPanel.setMargin(5, 0, 0, 5);
//            messPanel.setDebug(true);
            attachChild(messPanel);
            
            for (int i = 0; i < limit; i++) {
                Text text = new Text(""); // height/limit 确保字体大小不会超过行高
                text.setFontSize(avgHeight);
                text.setWidth(messPanel.getWidth());
                text.setHeight(avgHeight);
                text.setVisible(false);
//                text.setDebug(true);
                messPanel.addView(text);
            }
        }

        @Override
        public void updateLogicalState(float tpf) {
            super.updateLogicalState(tpf);
            update(tpf);
        }

        private void update(float tpf) {
            if (time > useTime) {
                cleanup();
                time = 0;
            }
            time += tpf;
        }

        public void addMessage(String mess, ColorRGBA color) {
            Text text = null;
            if (visible >= limit) {
                text = (Text) messPanel.getViews().get(0);
                // 取第一个放到最后
                messPanel.removeView(text);
                messPanel.addView(text);
            } else {
                text = (Text) messPanel.getViews().get(visible);
                text.setVisible(true);
                visible++;
            }
            text.setHeight(avgHeight);
            text.setText(mess);
            text.setFontColor(color);
            time = 0;
            
            // 检查并重置高度
            checkAndResizePanel();
            
//            logger.log(Level.INFO, "visiable={0}", visible + "");
        }
        
        private void checkAndResizePanel() {
            List<UI> cuis = messPanel.getViews();
            float trueHeight = 0;
            for (UI ui : cuis) {
                trueHeight += ui.getHeight();
            }
            if (trueHeight > fixedHeight) {
                messPanel.setHeight(trueHeight);
            } else {
                messPanel.setHeight(fixedHeight);
            }
        }

        public void cleanup() {
            List<UI> cui = messPanel.getViews();
            for (UI ui : cui) {
                ui.setVisible(false);
            }
            visible = 0;
        }

        public void setUseTime(float useTime) {
            this.useTime = useTime;
        }
        
        public void setDragEnabled(boolean dragEnabled) {
            this.messPanel.setDragEnabled(dragEnabled);
        }
    }
}
