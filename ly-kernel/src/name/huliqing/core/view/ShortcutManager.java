/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.core.view;

import com.jme3.app.SimpleApplication;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.util.SafeArrayList;
import com.jme3.util.TempVars;
import java.util.ArrayList;
import java.util.List;
import name.huliqing.core.manager.ResourceManager;
import name.huliqing.core.manager.AnimationManager;
import name.huliqing.core.LY;
import name.huliqing.core.Factory;
import name.huliqing.core.data.ItemData;
import name.huliqing.core.object.actor.Actor;
import name.huliqing.core.data.ObjectData;
import name.huliqing.core.data.SkillData;
import name.huliqing.core.data.SkinData;
import name.huliqing.core.enums.MessageType;
import name.huliqing.core.mvc.network.UserCommandNetwork;
import name.huliqing.core.mvc.service.ActorService;
import name.huliqing.core.mvc.service.ConfigService;
import name.huliqing.core.mvc.service.ProtoService;
import name.huliqing.core.object.animation.Animation;
import name.huliqing.core.object.animation.BounceMotion;
import name.huliqing.core.object.animation.CurveMove;
import name.huliqing.core.object.animation.LinearGroup;
import name.huliqing.core.xml.DataFactory;
import name.huliqing.core.save.ShortcutSave;
import name.huliqing.core.ui.UIUtils;
import name.huliqing.core.ui.UI;
import name.huliqing.core.ui.UI.Corner;
import name.huliqing.core.ui.state.UIState;

/**
 * 关于“快捷方式”的管理
 * @author huliqing
 */
public class ShortcutManager {
//    private final UserCommandNetwork userCommandNetwork = Factory.get(UserCommandNetwork.class);
    
    private final static ShortcutRoot SHORTCUT_ROOT = new ShortcutRoot();
    
    // “删除”图标
    private final static UI DELETE = UIUtils.createMultView(
            128, 128, "Interface/icon/skull.png", "Interface/icon/shortcut.png");

    // “回收”图标
    private final static UI RECYCLE = UIUtils.createMultView(
            128, 128, "Interface/icon/recycle.png", "Interface/icon/shortcut.png");
    
    private final static float SPACE = 40;
    
    public final static void init() {

        float marginTop = LY.getSettings().getHeight() * 0.1f;
        
        // setMargin后再setToCorner,因为setToCorner会立即计算位置
        DELETE.setMargin(0, marginTop, (DELETE.getWidth() + SPACE) * 0.5f, 0);
        DELETE.setToCorner(Corner.CT);
        DELETE.setVisible(false);
        
        RECYCLE.setMargin((RECYCLE.getWidth() + SPACE) * 0.5f, marginTop, 0, 0);
        RECYCLE.setToCorner(Corner.CT);
        RECYCLE.setVisible(false);
        
        UIState.getInstance().addUI(DELETE.getDisplay());
        UIState.getInstance().addUI(RECYCLE.getDisplay());
        
        // 把shortcutRoot添加到场景是为了调用updateLogicalState.以便
        // 更新快捷方式中的逻辑，比如一些技能冷却效果需要持续更新
        ((SimpleApplication)LY.getApp()).getRootNode().attachChild(SHORTCUT_ROOT);
    }
    
    /**
     * 添加快捷方式（无动画）
     * @param shortcut 
     */
    public static void addShortcutNoAnim(ShortcutView shortcut) {
        SHORTCUT_ROOT.addShortcut(shortcut);
    }
    
    /**
     * 添加快捷方式，带动画
     * @param shortcut 
     */
    public static void addShortcut(ShortcutView shortcut) {
        addShortcutNoAnim(shortcut);
        
        Animation anim = createShortcutAddAnimation(shortcut);
        
        AnimationManager.getInstance().startAnimation(anim);
    }
    
    /**
     * 检查是否删除快捷方式或是删除物品
     * @param shortcut 
     */
    public static void checkProcess(ShortcutView shortcut) {
        if (RECYCLE.isVisible() && isRecycle(shortcut)) {
            SHORTCUT_ROOT.removeShortcut(shortcut);
            String objectName = ResourceManager.getObjectName(shortcut.getData());
            LY.getPlayState().addMessage(ResourceManager.get("common.shortcutRecycle", new String[] {objectName})
                    , MessageType.info);
            
        } else if (DELETE.isVisible() && isDelete(shortcut)) {
            // delete object
            Actor actor = shortcut.getActor();
            ObjectData data = shortcut.getData();
            
            // remove20160619
//            boolean delSuccess = Factory.get(UserCommandNetwork.class).removeObject(actor, data.getId(), data.getTotal());
//            String objectName = ResourceManager.getObjectName(shortcut.getData());
//            if (delSuccess) {
//                // delete shortcut
//                SHORTCUT_ROOT.removeShortcut(shortcut);
//                Common.getPlayState().addMessage(ResourceManager.get("common.deleteSuccess", new String[] {objectName})
//                    , MessageType.info);
//            } else {
//                Common.getPlayState().addMessage(ResourceManager.get("common.deleteFail", new String[] {objectName})
//                    , MessageType.notice);
//            }

            String objectName = ResourceManager.getObjectName(data);
            Factory.get(UserCommandNetwork.class).removeObject(actor, data.getId(), data.getTotal());
            ObjectData resultData = Factory.get(ProtoService.class).getData(actor, data.getId());
            if (resultData == null || resultData.getTotal() <= 0) {
                // delete shortcut
                SHORTCUT_ROOT.removeShortcut(shortcut);
                LY.getPlayState().addMessage(ResourceManager.get("common.deleteSuccess", new String[] {objectName})
                    , MessageType.info);
            } else {
                LY.getPlayState().addMessage(ResourceManager.get("common.deleteFail", new String[] {objectName})
                    , MessageType.notice);
            }
        }
    }
    
    /**
     * 显示或隐藏“回收”图标和“删除”图标
     * @param visible 
     */
    public static void setBucketVisible(boolean visible) {
        RECYCLE.setVisible(visible);
        DELETE.setVisible(visible);
        if (visible) {
            RECYCLE.setOnTop();
            DELETE.setOnTop();
        }
    }
    
    /**
     * 设置当前界面上所有快捷方式的大小
     * @param size 
     */
    public static void setShortcutSize(float size) {
        List<ShortcutView> shortcuts = SHORTCUT_ROOT.getShortcuts();
        for (ShortcutView s : shortcuts) {
            s.setLocalScale(size);
        }
    }
    
    /**
     * 锁定或解锁当前已经存在的快捷方式，锁定后快捷方式不能再拖动。
     * 该方法不会影响后续添加的快捷方式。
     * @param locked 
     */
    public static void setShortcutLocked(boolean locked) {
        List<ShortcutView> shortcuts = SHORTCUT_ROOT.getShortcuts();
        for (ShortcutView s : shortcuts) {
            s.setDragEnabled(!locked);
        }
    }
    
    /**
     * 获取当前界面上所有快捷方式，用于保存到存档
     * @return 
     */
    public static ArrayList<ShortcutSave> getShortcutSaves() {
        ArrayList<ShortcutSave> result = new ArrayList<ShortcutSave>();
        List<ShortcutView> scs = SHORTCUT_ROOT.getShortcuts();
        if (!scs.isEmpty()) {
            for (ShortcutView sc : scs) {
                ShortcutSave ss = new ShortcutSave();
                ss.setItemId(sc.getData().getId());
                ss.setX(sc.getWorldTranslation().x);
                ss.setY(sc.getWorldTranslation().y);
                result.add(ss);
            }
        }
        return result;
    }
    
    public static ShortcutView createShortcut(Actor actor, ObjectData data) {
        ShortcutView shortcut;
        if (data instanceof SkillData) {
            shortcut = new ShortcutSkillView(64, 64, actor, data);
        } else {
            shortcut = new ShortcutView(64, 64, actor, data); // default
        }
        return shortcut;
    }
    
//    /**
//     * 清理掉界面所有快捷方式
//     */
//    public static void clearShortcuts() {
//        cleanup();
//    }
    
    /**
     * 清理界面上的所有快捷方式
     */
    public static void cleanup() {
        if (SHORTCUT_ROOT != null) {
            SHORTCUT_ROOT.clearShortcuts();
        }
    }
    
    /**
     * 载入快捷方式给指定的角色。
     * @param ss
     * @param player 
     */
    public static void loadShortcut(List<ShortcutSave> ss, Actor player) {
        if (ss == null || ss.isEmpty())
            return;
        ActorService actorService = Factory.get(ActorService.class);
        ConfigService configService = Factory.get(ConfigService.class);
        
        float shortcutSize = configService.getShortcutSize();
        for (ShortcutSave s : ss) {
            String itemId = s.getItemId();
            ObjectData data = actorService.getItem(player, itemId);
            if (data == null) {
                data = DataFactory.createData(itemId);
            }
            
            // 防止物品被删除
            if (data == null) {
                continue;
            }
            
            // 包裹中只允许存放限定的物品
            if ((!(data instanceof ItemData)) 
                    && (!(data instanceof SkinData)) 
                    && (!(data instanceof SkillData))
                    ) {
                continue;
            }
            
            // 由于skill的创建过程比较特殊，SkillData只有在创建了AnimSkill之后
            // 才能获得skillType,所以不能直接使用createProtoData方式获得的SkillData
            // 这会找不到SkillData中的skillType,所以需要从角色身上重新找回SkillData
            if (data instanceof SkillData) {
                data = player.getData().getSkillStore().getSkillById(data.getId());
            }
            
            ShortcutView shortcut = ShortcutManager.createShortcut(player, data);
            shortcut.setLocalScale(shortcutSize);
            shortcut.setLocalTranslation(s.getX(), s.getY(), 0);
            ShortcutManager.addShortcutNoAnim(shortcut);
        }
        
        // 如果系统设置锁定快捷方式，则锁定它
        if (configService.isShortcutLocked()) {
            ShortcutManager.setShortcutLocked(true);
        }
    }
    
    /**
     * 判断是否要进行回收，当快捷方式拖放到“回收站”上时
     */
    private static boolean isRecycle(ShortcutView shortcut) {
        TempVars tv = TempVars.get();
        Vector3f bucketCenter = tv.vect1.set(RECYCLE.getDisplay().getWorldTranslation());
        bucketCenter.setX(bucketCenter.x + RECYCLE.getWidth() * 0.5f);
        bucketCenter.setY(bucketCenter.y + RECYCLE.getHeight() * 0.5f);
        bucketCenter.setZ(0);

        Vector3f shortcutCenter = tv.vect2.set(shortcut.getWorldTranslation());
        shortcutCenter.setX(shortcutCenter.x + shortcut.getWidth() * 0.5f);
        shortcutCenter.setY(shortcutCenter.y + shortcut.getHeight() * 0.5f);
        shortcutCenter.setZ(0);
        
        boolean result = bucketCenter.distance(shortcutCenter) < RECYCLE.getWidth() * 0.5f;
        tv.release();
        return result;
    }
    
    /**
     * 判断是否要进行删除，当快捷方式拖放到删除图标上时。
     * @param shortcut
     * @return 
     */
    private static boolean isDelete(ShortcutView shortcut) {
        TempVars tv = TempVars.get();
        Vector3f bucketCenter = tv.vect1.set(DELETE.getDisplay().getWorldTranslation());
        bucketCenter.setX(bucketCenter.x + DELETE.getWidth() * 0.5f);
        bucketCenter.setY(bucketCenter.y + DELETE.getHeight() * 0.5f);
        bucketCenter.setZ(0);

        Vector3f shortcutCenter = tv.vect2.set(shortcut.getWorldTranslation());
        shortcutCenter.setX(shortcutCenter.x + shortcut.getWidth() * 0.5f);
        shortcutCenter.setY(shortcutCenter.y + shortcut.getHeight() * 0.5f);
        shortcutCenter.setZ(0);
        
        boolean result = bucketCenter.distance(shortcutCenter) < DELETE.getWidth() * 0.5f;
        tv.release();
        return result;
    }
    
    /**
     * 为shortcut的添加功能创建一个动画效果
     * @param shortcut
     * @return 
     */
    private static Animation createShortcutAddAnimation(ShortcutView shortcut) {
        TempVars tv = TempVars.get();
        Vector2f startPos = LY.getCursorPosition();
        tv.vect1.set(startPos.x, startPos.y, shortcut.getLocalTranslation().z); // z值不能改变，否则setOnTop会不正确
        tv.vect2.set(LY.getSettings().getWidth() - 64 - 20
                , (LY.getSettings().getHeight() - 64) * 0.5f
                , shortcut.getLocalTranslation().z);
        
        // 弧线移动效果
        CurveMove cm = new CurveMove();
        cm.setStartPosition(tv.vect1);
        cm.setEndPosition(tv.vect2);
        cm.setHeight(150);
        cm.setAnimateTime(0.5f);
        cm.setUseScale(true);
        cm.setScale(0.3f, Factory.get(ConfigService.class).getShortcutSize());
        tv.release();
        
        // 弹跳效果
        BounceMotion bm = new BounceMotion();
        bm.setHeight(100);
        bm.setAnimateTime(0.5f);
        
        // 再弹跳效果
        BounceMotion bm2 = new BounceMotion();
        bm2.setHeight(30);
        bm2.setAnimateTime(0.25f);
        
        LinearGroup lg = new LinearGroup();
        lg.addAnimation(cm);
        lg.addAnimation(bm);
        lg.addAnimation(bm2);
        lg.setTarget(shortcut);
        
        return lg;
    }
    
    /**
     * 快捷方式的根节点，
     * 注：
     * 1.这个节点并不直接保持快捷方式等子节点。只保留对其引用。
     * 2.快捷方式节点是直接保存在UIState中的.
     * 3.这个根节点会保存在场景中，调用updateLogicState来更新快捷方式中的逻辑(由于UI是屏蔽了updateLogicState的原因)。
     * 
     */
    private static class ShortcutRoot  extends Node {
        
        private final SafeArrayList<ShortcutView> shortcuts = 
                new SafeArrayList<ShortcutView>(ShortcutView.class);
        
        public void addShortcut(ShortcutView shortcut) {
            if (!shortcuts.contains(shortcut)) {
                shortcuts.add(shortcut);
                UIState.getInstance().addUI(shortcut);
            }
        }

        @Override
        public void updateLogicalState(float tpf) {
            if (shortcuts.isEmpty())
                return;
            for (ShortcutView sv : shortcuts) {
                sv.updateShortcut(tpf);
            }
        }
        
        /**
         * 获取界面所有shortcut
         * @return 
         */
        public List<ShortcutView> getShortcuts() {
            return shortcuts;
        }
        
        public void removeShortcut(ShortcutView shortcut) {
            shortcut.cleanup();
            shortcut.removeFromParent();
            shortcuts.remove(shortcut);
        }
        
        /**
         * 清理所有shortcut.(注：不清理其它类型的子节点)
         */
        public void clearShortcuts() {
            for (ShortcutView s : shortcuts.getArray()) {
                s.removeFromParent();
            }
            shortcuts.clear();
        }
    }
}