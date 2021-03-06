///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package name.huliqing.ly.state;
//
//import com.jme3.app.Application;
//import com.jme3.network.HostedConnection;
//import com.jme3.network.Message;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Iterator;
//import java.util.List;
//import name.huliqing.luoying.data.ConnData;
//import name.huliqing.luoying.Config;
//import name.huliqing.luoying.Factory;
//import name.huliqing.luoying.constants.IdConstants;
//import name.huliqing.luoying.data.ActorData;
//import name.huliqing.luoying.data.GameData;
//import name.huliqing.luoying.layer.network.ActorNetwork;
//import name.huliqing.luoying.layer.network.PlayNetwork;
//import name.huliqing.luoying.layer.service.ActorService;
//import name.huliqing.luoying.layer.service.ConfigService;
//import name.huliqing.luoying.layer.service.LogicService;
//import name.huliqing.luoying.layer.service.PlayService;
//import name.huliqing.luoying.layer.service.SkillService;
//import name.huliqing.luoying.network.GameServer;
//import name.huliqing.luoying.mess.MessPlayLoadSavedActor;
//import name.huliqing.luoying.mess.MessPlayLoadSavedActorResult;
//import name.huliqing.luoying.mess.MessPlayClientExit;
//import name.huliqing.luoying.mess.MessSCClientList;
//import name.huliqing.luoying.network.GameServer.ServerState;
//import name.huliqing.ly.view.shortcut.ShortcutManager;
//import name.huliqing.luoying.object.actor.Actor;
//import name.huliqing.luoying.object.game.Game;
//import name.huliqing.luoying.save.ClientData;
//import name.huliqing.luoying.save.SaveConfig;
//import name.huliqing.luoying.save.SaveHelper;
//import name.huliqing.luoying.save.SaveStory;
//import name.huliqing.luoying.save.ShortcutSave;
//
///**
// * 故事模式下的服务端，允许客户端连接，并且允许保存客户端的资料。
// * @author huliqing
// */
//public abstract class StoryServerPlayState extends NetworkServerPlayState {
//    private final ActorService actorService = Factory.get(ActorService.class);
//    private final PlayService playService = Factory.get(PlayService.class);
//    private final ConfigService configService = Factory.get(ConfigService.class);
//    private final LogicService logicService = Factory.get(LogicService.class);
//    private final SkillService skillService = Factory.get(SkillService.class);
//    private final ActorNetwork actorNetwork = Factory.get(ActorNetwork.class);
//
//    // 存档数据
//    private final SaveStory saveStory;
//    
//    /**
//     * 故事模式，剧情模式
//     * @param app
//     * @param gameData 游戏设置
//     * @param saveStory 存档，如果是新游戏则该参数设置为null.
//     */
//    public StoryServerPlayState(Application app, GameData gameData, SaveStory saveStory) {
//        super(app, gameData);
//        this.saveStory = saveStory != null ? saveStory : new SaveStory();
//    }
//    
//    public SaveStory getSaveStory() {
//        return saveStory;
//    }
//
//    @Override
//    public void exit() {
//        // 在退出之前保存玩家资料
//        saveStory();
//        super.exit(); 
//    }
//    
//    @Override
//    public void changeGameState(GameState newGameState) {
//        // 故事模式在切换游戏之前先保存一次存档.
//        // 注：这里先把服务端状态设置为loading，以阻止在切换状态过程中处理来自客户端的消息。
//        gameServer.setServerState(ServerState.loading);
//        if (gameState != null) {
//            saveStory();
//        }
//        // 切换游戏
//        super.changeGameState(newGameState);
//        gameServer.setGameData(gameData);
//        newGameState.getGame().addListener(new GameListener() {
//            @Override
//            public void onGameStarted(Game game) {
//                gameServer.setServerState(ServerState.running);
//                loadPlayer();
//            }
//        });
//    }
//    
//    /**
//     * 保存整个存档，当故事模式在退出时要保存整个存档，包含主角，其它客户端玩家资料，及所有玩家的宠物等。
//     */
//    private void saveStory() {
//        String gameId = gameState.getGame().getData().getId();
//        List<Actor> actors = gameState.getActors();
//        
//        // 保存所有故事模式下的主角
//        Actor player = gameState.getPlayer();
//        storeClient(saveStory, actors, configService.getClientId(), player.getData().getUniqueId(), gameId);
//        
//        // 保存当前所有客户端的资料
//        Collection<HostedConnection> conns = gameServer.getServer().getConnections();
//        if (conns != null && conns.size() > 0) {
//            for (HostedConnection hc : conns) {
//                ConnData cd = hc.getAttribute(ConnData.CONN_ATTRIBUTE_KEY);
//                // 如果客户端刚刚连接或还没有选角色
//                if (cd.getClientId() == null || cd.getEntityId() <= 0)
//                    continue;
//                storeClient(saveStory, actors, cd.getClientId(), cd.getEntityId(), gameId);
//            }
//        }
//        
//        // 单独保存主角，这是为了兼容v2.4及之前的版本,后续可能会取消这个特殊的保存方式.
//        saveStory.setPlayer(player.getData());
//        
//        // 保存快捷方式
//        saveStory.setShortcuts(ShortcutManager.getShortcutSaves());
//        
//        // 存档到系统文件 
//        SaveHelper.saveStoryLast(saveStory);
//        
//        // 保存全局配置
//        SaveConfig saveConfig = new SaveConfig();
//        saveConfig.setConfig(configService.getConfig());
//        SaveHelper.saveConfig(saveConfig);
//    }
//    
//    /**
//     * 单独保存一个客户端资料到存档中，但是并不立即保存到系统文件,其中包含客户端的宠物资料.
//     * @param saveStory 存档资料，如果没有则应该生成一个空的存档
//     * @param actors 场景中的所有角色
//     * @param clientId 客户端标识
//     * @param clientPlayerId 客户端玩家所控制的角色的唯一ID，必须包含在actors中
//     * @param gameId 当前游戏id.
//     */
//    private void storeClient(SaveStory saveStory, List<Actor> actors, String clientId, long clientPlayerId, String gameId) {
//        ArrayList<ActorData> savedActors = saveStory.getActors();
//        List<ClientData> savedClients = saveStory.getClientDatas();
//        
//        // ---- 先移除客户端角色及宠物及对应关系
//        // 移除客户端角色及其宠物从存档资料中移除
//        Iterator<ActorData> sait = savedActors.iterator();
//        while (sait.hasNext()) {
//            ActorData actorData = sait.next();
//            // 如果是玩家
//            if (actorData.getUniqueId() == clientPlayerId) {
//                sait.remove();
//                continue;
//            }
//            // 如果是玩家的宠物
//            Actor actor = actorService.loadActor(actorData);
//            if (actorService.getOwner(actor) == clientPlayerId) {
//                sait.remove();
//            }
//        }
//        
//        // 移除对应关系
//        Iterator<ClientData> scit = savedClients.iterator();
//        while (scit.hasNext()) {
//            ClientData clientData = scit.next();
//            if (clientData.getClientId().equals(clientId)) {
//                scit.remove();
//            }
//        }
//        
//        // ---- 重新保存客户端资料及宠物及对应关系
//        // 重新添加客户端玩家及其宠物资料到存档中，注意宠物必须满足以下条件才可以:
//        // 1.属于当前玩家
//        // 2.必须是活着的（生命值大于0）
//        // 3.必须是生物类角色，不要保存防御塔或其它建筑之类
//        savedActors.add(findActor(actors, clientPlayerId).getData());
//        for (Actor a : actors) {
//            if (actorService.getOwner(a) == clientPlayerId 
//                    && !actorService.isDead(a) 
//                    && actorService.isBiology(a)) {
//                savedActors.add(a.getData());
//            }
//        }
//        // 添加客户端与角色及场景的对应关系
//        ClientData clientData = new ClientData();
//        clientData.setClientId(clientId);
//        clientData.setActorId(clientPlayerId);
//        clientData.setGameId(gameId);
//        savedClients.add(clientData);
//    }
//    
//    private Actor findActor(List<Actor> actors, long actorId) {
//        for (Actor actor : actors) {
//            if (actor.getData().getUniqueId() == actorId) {
//                return actor;
//            }
//        }
//        return null;
//    }
//    
//     /**
//     * 载入当前主角玩家存档，如果没有存档则新开游戏
//     */
//    private void loadPlayer() {
//        Actor player;
//        if (saveStory.getPlayer() != null) {
//            // 载入玩家主角
//            player = actorService.loadActor(saveStory.getPlayer());
//            List<ShortcutSave> ss = saveStory.getShortcuts();
//            ShortcutManager.loadShortcut(ss, player);
//            
//            // 载入玩家主角的宠物(这里还不需要载入其他玩家的角色及宠物,由其他玩家重新连接的时候再载入)
//            ArrayList<ActorData> actors = saveStory.getActors();
//            for (ActorData data : actors) {
//                Actor actor = actorService.loadActor(data);
//                if  (actorService.getOwner(actor) == player.getData().getUniqueId()) {
//                    addObject(actor.getSpatial(), false);
//                }
//            }
//        } else {
//            player = actorService.loadActor(Config.debug ? IdConstants.ACTOR_PLAYER_TEST : IdConstants.ACTOR_PLAYER);
//            actorService.setLevel(player, 10);
//            logicService.resetPlayerLogic(player);
//        }
//        // 故事模式玩家始终队伍分组为1
//        actorService.setTeam(player, 1);
//        skillService.playSkill(player, skillService.getSkillWaitDefault(player), false);
//        
//        addObject(player, false);
//        setPlayer(player);
//    }
//    
//    // 从saveStory中载入指定clientId的角色
//    private boolean loadClient(SaveStory saveStory, ClientData clientData) {
//        List<ActorData> actors =  saveStory.getActors();
//        ActorData clientPlayerData = null;
//        for (ActorData data : actors) {
//            if (data.getUniqueId() == clientData.getActorId()) {
//                clientPlayerData = data;
//                break;
//            }
//        }
//        if (clientPlayerData == null) {
//            return false;
//        }
//        
//        // 载入客户端玩家及其宠物，注：这里要用Network,因为服务端和客户端已经为running状态.
//        PlayNetwork playNetwork = Factory.get(PlayNetwork.class);
//        Actor  clientPlayer= actorService.loadActor(clientPlayerData);
//
//        addPlayer(clientPlayer);
//        
//        for (ActorData data : actors) {
//            Actor actor = actorService.loadActor(data);
//            if (actorService.getOwner(actor) == clientPlayerData.getUniqueId()) {
//                skillService.playSkill(actor, skillService.getSkillWaitDefault(actor), false);
//                playNetwork.addActor(actor);                    
//            }
//        }
//        return true;
//    }
//
//    @Override
//    public void kickClient(int connId) {
//        // 踢出之前先把玩家的资料保存起来
//        HostedConnection conn = gameServer.getServer().getConnection(connId);
//        if (conn != null) {
//            ConnData cd = conn.getAttribute(ConnData.CONN_ATTRIBUTE_KEY);
//            if (cd != null && cd.getClientId() != null && cd.getEntityId() > 0) {
//                storeClient(saveStory, gameState.getActors(), cd.getClientId(), cd.getEntityId(), gameState.getGame().getData().getId());
//                SaveHelper.saveStoryLast(saveStory);
//            }
//        }
//        super.kickClient(connId);
//    }
//
//    @Override
//    protected void addPlayer(Actor actor) {
//        super.addPlayer(actor);
//        // 让客户端玩家角色的group始终和当前故事模式中的主角色的分组一致。
//        Actor mainPlayer = gameState.getPlayer();
//        if (mainPlayer != null) {
//            actorNetwork.setGroup(actor, actorService.getGroup(mainPlayer));
//        }
//    }
//
//    @Override
//    protected boolean processMessage(GameServer gameServer, HostedConnection source, Message m) {
//        // 如果客户端偿试要求载入存档，则从存档中载入，如果不存在存档（新游戏）或存档中没有指定客户端的资料则返回false
//        // 以通知客户端自己去选择一个角色来进行游戏
//        if (m instanceof MessPlayLoadSavedActor) {
//            // 找出客户端的存档资料，注意saveStory可能为null（在“新游戏"情况下）
//            if (saveStory != null) {
//                List<ClientData> clientDatas = saveStory.getClientDatas();
//                ConnData cd = source.getAttribute(ConnData.CONN_ATTRIBUTE_KEY);
//                for (ClientData clientData : clientDatas) {
//                    if (clientData.getClientId().equals(cd.getClientId())) {
//                        // 载入角色客户端角色的资料
//                        boolean result = loadClient(saveStory, clientData);
//                        if (result) {
//                            cd.setEntityId(clientData.getActorId());
//                        }
//
//                        // 通知客户端,这样客户端可以确定是否需要弹出“角色选择面板”来重新选择一个角色进行游戏
//                        MessPlayLoadSavedActorResult messResult = new MessPlayLoadSavedActorResult(result);
//                        messResult.setActorId(clientData.getActorId());
//                        gameServer.send(source, messResult);
//
//                        // 记住客户端所选择的角色
//                        Actor actor = playService.findActor(cd.getEntityId());
//                        cd.setEntityId(actor.getData().getUniqueId());
//                        cd.setEntityName(actor.getData().getName());
//                        
//                        // 更新本地（服务端）客户端列表
//                        onClientListUpdated();
//                        // 通知所有客户更新“客户端列表”
//                        gameServer.broadcast(new MessSCClientList(gameServer.getClients()));
//                        
//                        return true;
//                    }
//                }
//            }
//            gameServer.send(source, new MessPlayLoadSavedActorResult(false));
//            return true;
//        }
//
//        // 当服务端（故事模式）接收到客户端退出游戏的消息时，服务端要保存客户端的资料,以便下次客户端连接时能够继续
//        if (m instanceof MessPlayClientExit) {
//            ConnData cd = source.getAttribute(ConnData.CONN_ATTRIBUTE_KEY);
//            if (cd.getClientId() != null && cd.getEntityId() > 0) {
//                storeClient(saveStory, gameState.getActors(), cd.getClientId(), cd.getEntityId(), gameState.game.getData().getId());
//                SaveHelper.saveStoryLast(saveStory);
//            }
//            return super.processMessage(gameServer, source, m);
//        }
//
//        return super.processMessage(gameServer, source, m);
//    }
//     
//}
