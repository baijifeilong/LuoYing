///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package name.huliqing.ly.state;
//
//import com.jme3.app.Application;
//import com.jme3.app.state.AbstractAppState;
//import com.jme3.app.state.AppStateManager;
//import com.jme3.scene.Spatial;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import name.huliqing.ly.Ly;
//import name.huliqing.luoying.Factory;
//import name.huliqing.luoying.data.GameData;
//import name.huliqing.luoying.object.actor.Actor;
//import name.huliqing.ly.enums.MessageType;
//import name.huliqing.ly.layer.service.GameService;
//import name.huliqing.luoying.layer.service.SceneService;
//import name.huliqing.ly.view.TeamView;
//import name.huliqing.ly.view.talk.SpeakManager;
//import name.huliqing.ly.view.talk.TalkManager;
//import name.huliqing.luoying.object.PlayManager;
//import name.huliqing.luoying.object.PlayObject;
//import name.huliqing.ly.object.NetworkObject;
//import name.huliqing.luoying.object.game.Game;
//import name.huliqing.luoying.object.scene.Scene;
//import name.huliqing.luoying.object.scene.Scene.SceneListener;
//import name.huliqing.ly.object.view.View;
//import name.huliqing.luoying.ui.state.UIState;
//
///**
// * @author huliqing
// */
//public abstract class GameState extends AbstractAppState {
//    private final GameService gameService = Factory.get(GameService.class);
//    private final SceneService sceneService = Factory.get(SceneService.class);
//    
//    public interface PlayListener {
//        
//        /**
//         * 当向游戏中添加了物体时触发，注:这个object并不一定是Spatial,也可能是其它类型，如：Actor,View,UI等
//         * @param object
//         */
//        void onObjectAdded(Object object);
//        
//        /**
//         * 当从游戏中移除物体时触发该方法,注:这个object并不一定是Spatial,也可能是其它类型，如：Actor,View,UI等
//         * @param object
//         */
//        void onObjectRemoved(Object object);
//        
//        /**
//         * 当退出PlayState时
//         */
//        void onExit();
//        
//    }
//    
//    protected Application app;
//    
//    // 管理所有的PlayObject的运行
//    protected final PlayManager playManager = new PlayManager(Ly.getApp(), PlayObject.class);
//    
//    // 侦听器
//    protected final List<PlayListener> listeners = new ArrayList<PlayListener>();
//    
//    // 场景中需要与客户端进行同步的对象。
//    protected final Map<Long, NetworkObject> networkObjects = new HashMap<Long, NetworkObject>();
//    
//    protected Game game;
//    protected Scene scene;
//    
//    public GameState(GameData gameData) {
//        game = gameService.loadGame(gameData);
//    }
//    
//    public GameState(String gameId) {
//        game = gameService.loadGame(gameId);
//    }
//    
//    @Override
//    public void initialize(final AppStateManager stateManager, Application app) {
//        super.initialize(stateManager, app);
//        this.app = app;
//        this.listeners.clear();
//        this.networkObjects.clear();
//        
//        // 添加场景及游戏逻辑
//        scene = sceneService.loadScene(game.getData().getSceneData());
//        scene.addSceneListener(new SceneListener() {
//            @Override
//            public void onSceneInitialized(Scene scene) {
//                // 载入场景之后开始游戏。
//                game.setScene(scene);
//                stateManager.attach(game);
//            }
//            @Override
//            public void onSceneObjectAdded(Scene scene, Spatial objectAdded) {}
//            
//            @Override
//            public void onSceneObjectRemoved(Scene scene, Spatial objectRemoved) {}
//        });
//        
//        stateManager.attach(scene);
//        
//        
//        // 添加Speak和Talk逻辑
//        addObject(SpeakManager.getInstance(), false);
//        addObject(TalkManager.getInstance(), false);
//        
//    }
//
//    @Override
//    public void update(float tpf) {
//        playManager.update(tpf);
//    }
//
//    @Override
//    public void stateDetached(AppStateManager stateManager) {
//        if (game != null) {
//            stateManager.detach(game);
//        }
//        if (scene != null) {
//            stateManager.detach(scene);
//        }
//        super.stateDetached(stateManager);
//    }
//    
//    @Override
//    public void cleanup() {
//        listeners.clear();
//        networkObjects.clear();
//        playManager.cleanup();
//        UIState.getInstance().clearUI();
//        super.cleanup();
//    }
//    
//     /**
//     * 退出当前PlayState,并返回到开始界面。
//     */
//    public void exit() {
//        for (PlayListener lis : listeners) {
//            lis.onExit();
//        }
//    }
//
//    public Game getGame() {
//        return game;
//    }
//    
//    public Scene getScene() {
//        return scene;
//    }
//    
//    /**
//     * 添加一个侦听器
//     * @param listener 
//     */
//    public final void addListener(PlayListener listener) {
//        if (!listeners.contains(listener)) {
//            listeners.add(listener);
//        }
//    }
//    
//    /**
//     * 移除一个侦听器
//     * @param listener 
//     * @return  
//     */
//    public final boolean removeListener(PlayListener listener) {
//        return listeners.remove(listener);
//    }
//    
//    /**
//     * 给场景中添加物体
//     * @param object 
//     * @param gui 
//     */
//    public void addObject(Object object, boolean gui) {
//        if (object instanceof PlayObject) {
//            playManager.attach((PlayObject) object);
//        }
//        
//        // 检查并放到同步对象中
//        if (object instanceof NetworkObject) {
//            NetworkObject so = (NetworkObject) object;
//            networkObjects.put(so.getSyncId(), so);
//        }
//        
//        // 触发侦听器
//        for (PlayListener lis : listeners) {
//            lis.onObjectAdded(object);
//        }
//    }
//    
//    /**
//     * 从场景中移除物体
//     * @param object 
//     */
//    public void removeObject(Object object) {
//        // 检查并从同步对象中移除
//        if (object instanceof NetworkObject) {
//            NetworkObject so = (NetworkObject) object;
//            networkObjects.remove(so.getSyncId());
//        }
//        
//        if (object instanceof PlayObject) {
//            playManager.detach((PlayObject) object);
//        }
//        
//        // 触发侦听
//        for (PlayListener lis : listeners) {
//            lis.onObjectRemoved(object);
//        }
//    }
//    
//    /**
//     * 获取场景中的同步对象
//     * @param objectId
//     * @return 
//     */
//    public final NetworkObject getSyncObjects(long objectId) {
//        return networkObjects.get(objectId);
//    }
//    
//    public Application getApp() {
//        return this.app;
//    }
//    
//    /**
//     * 添加HUD提示信息
//     * @param message 
//     * @param messageType
//     */
//    public abstract void addMessage(String message, MessageType messageType);
//    
//    /**
//     * 判断节点是否存在于场景中。
//     * @param spatial
//     * @return 
//     */
//    public abstract boolean isInScene(Spatial spatial);
//
//    /**
//     * 获取当前场景所有活动对象，包括player,如果没有，则返回empty.
//     * 不要返回null.
//     * @return 
//     */
//    public abstract List<Actor> getActors();
//    
//    /**
//     * 获取玩家角色，如果不存在玩家角色，则返回null.
//     * @return 
//     */
//    public abstract Actor getPlayer();
//     
//    /**
//     * 把目标设置为玩家
//     * @param actor 
//     */
//    public abstract void setPlayer(Actor actor);
//    
//    /**
//     * 获取当前的目标对象
//     * @return 
//     */
//    public abstract Actor getTarget();
//    
//    /**
//     * 设置当前界面的主目标对象
//     * @param target 
//     */
//    public abstract void setTarget(Actor target);
//        
//    /**
//     * 获取视图组件,视图组件是需要同步到客户端的。
//     * @return 
//     */
//    public abstract List<View> getViews();
//    
//    /**
//     * 切换显示当前界面的所有UI.注意:该方法将只影响当前已经存在的UI,对后续
//     * 添加到场景中的UI不会有影响.也就是,如果想要只显示某个特殊UI,则先设置
//     * setUIVisiable(false),然后再把特定UI添加到UI根节点中
//     * @param visiable 
//     */
//    public abstract void setUIVisiable(boolean visiable);
//    
//    /**
//     * 获取队伍面板
//     * @return 
//     */
//    public abstract TeamView getTeamView();
//    
//    /**
//     * 获取工具栏
//     * @return 
//     */
//    public abstract MenuTool getMenuTool(); 
//
//    /**
//     * 攻击
//     */
//    public abstract void attack();
//}
