/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.ly.object.game.impl;

import com.jme3.math.Vector3f;
import java.util.List;
import name.huliqing.ly.Factory;
import name.huliqing.ly.object.actor.Actor;
import name.huliqing.ly.constants.IdConstants;
import name.huliqing.ly.constants.ResConstants;
import name.huliqing.ly.constants.StoryConstants;
import name.huliqing.ly.data.ObjectData;
import name.huliqing.ly.enums.MessageType;
import name.huliqing.ly.view.talk.Talk;
import name.huliqing.ly.view.talk.TalkImpl;
import name.huliqing.ly.view.talk.TalkListener;
import name.huliqing.ly.layer.network.ActorNetwork;
import name.huliqing.ly.layer.network.PlayNetwork;
import name.huliqing.ly.layer.network.SkillNetwork;
import name.huliqing.ly.layer.network.StateNetwork;
import name.huliqing.ly.layer.service.ActionService;
import name.huliqing.ly.layer.service.ActorService;
import name.huliqing.ly.layer.service.PlayService;
import name.huliqing.ly.layer.service.SkillService;
import name.huliqing.ly.layer.service.ViewService;
import name.huliqing.ly.object.IntervalLogic;
import name.huliqing.ly.logic.scene.ActorMultLoadHelper;
import name.huliqing.ly.manager.ResourceManager;
import name.huliqing.ly.layer.service.LogicService;
import name.huliqing.ly.layer.service.ObjectService;
import name.huliqing.ly.layer.network.ObjectNetwork;

/**
 * task2中收集完树根之后，向古柏交任务。
 * @author huliqing
 */
public class StoryGbTask2End extends IntervalLogic {
    private final PlayService playService = Factory.get(PlayService.class);
    private final ActorService actorService = Factory.get(ActorService.class);
    private final ActionService actionService = Factory.get(ActionService.class);
    private final ViewService viewService = Factory.get(ViewService.class);
    private final SkillService skillService = Factory.get(SkillService.class);
    private final ObjectService protoService = Factory.get(ObjectService.class);
    private final LogicService logicService = Factory.get(LogicService.class);
    
    private final PlayNetwork playNetwork = Factory.get(PlayNetwork.class);
    private final ActorNetwork actorNetwork = Factory.get(ActorNetwork.class);
    private final StateNetwork stateNetwork = Factory.get(StateNetwork.class);
    private final SkillNetwork skillNetwork = Factory.get(SkillNetwork.class);
    private final ObjectNetwork protoNetwork = Factory.get(ObjectNetwork.class);
    private StoryGbGame game;
    private Actor player;
    // 任务面板
    private StoryGbTaskLogic taskPanel;
    // 古柏
    private Actor gb;
    // 任务完成时的对话
    private Talk talk;
    
    private ActorMultLoadHelper gbLoader;
    private int stage;
    
    public StoryGbTask2End(StoryGbGame _game, final Actor player, Vector3f gbPos, StoryGbTaskLogic taskPanel) {
        super(0); // 频率
        this.game = _game;
        this.player = player;
        this.taskPanel = taskPanel;
        
        gbLoader = new ActorMultLoadHelper(IdConstants.ACTOR_GB) {
            @Override
            public void callback(Actor actor, int loadIndex) {
                // fix bug:先从场景中查找古柏，如果已经存在，则不需要使用载入的。
                // 避免出现两个古柏
                Actor oldGb = playService.findActor(IdConstants.ACTOR_GB);
                if (oldGb != null) {
                    gb = oldGb;
                    return;
                }
                
                gb = actor;
                actorService.setLevel(gb, 15);
                actorService.setGroup(gb, actorService.getGroup(player));
                actorService.setLocation(gb, game.getGbPosition());
                // 保护角色不死
                setProtected(actor, true);
                playNetwork.addActor(actor);
            }
        };
    }

    @Override
    protected void doLogic(float tpf) {
        // 载入gb
        if (stage == 0) {
            playService.addObject(gbLoader, false);
            stage = 1;
            return;
        }
        
        if (stage == 1) {
            if (gb != null) {
                playService.removeObject(gbLoader);
                stage = 2;
            }
            return;
        }
        
        if (stage == 2) {
            if (checkTaskOK()) {
                logicService.setAutoLogic(gb, false);
                actionService.playAction(gb, null);
                skillNetwork.playSkill(gb, skillService.getSkillWaitDefault(gb), false);
                createTalk();
                stage = 3;
            }
        }
    }
    
    private void createTalk() {
        talk = new TalkImpl();
        talk.face(gb, player, false);
        talk.speak(gb, get("talk7.gb1"));
        talk.face(player, gb, false);
        talk.speak(player, get("talk7.xy1"));
        talk.speak(gb, get("talk7.gb2"));
        talk.speak(gb, get("talk7.gb3"));
        talk.speak(gb, get("talk7.gb4"));
        // 如果“祭坛”未摧毁则提醒之。
        if (!isAltarDead()) {
            talk.face(gb, player, false);
            talk.speak(gb, get("talk7.gb5"));
        }
        talk.delay(2);
        talk.addListener(new TalkListener() {
            @Override
            public void onTalkEnd() {
                // 移除主角身上的树根
                ObjectData stumpData = protoService.getData(player, IdConstants.ITEM_GB_STUMP);
                protoNetwork.removeData(player, stumpData.getId(), taskPanel.getTotal());
                
                // 移除古柏
                playNetwork.removeObject(gb.getSpatial());
                // 停止任务面板的更新及移除提示面板
                playService.removeObject(taskPanel);
                // 保存关卡完成状态
                playService.saveCompleteStage(StoryConstants.STORY_NUM_GB);
                // 提示
                playNetwork.addMessage(ResourceManager.get(ResConstants.TASK_SUCCESS), MessageType.item);
                playNetwork.addView(viewService.loadView(IdConstants.VIEW_TEXT_SUCCESS));
            }
        });
        actorNetwork.talk(talk);
    }
    
    // 检查任务是否完成，并且是否适合开始结束对话。
    // 这要求树根数完成，player不能死，player在gb附近，并且附近没有敌人
    private boolean checkTaskOK() {
        return taskPanel.isOk() 
                && !actorService.isDead(player)
                && actorService.distance(player, gb) < 10
                && !isEnemyNear(25);
    }
    
    private boolean isAltarDead() {
        Actor actor = playService.findActor(IdConstants.ACTOR_ALTAR);
        return actor == null || actorService.isDead(actor);
    }
    
    private boolean isEnemyNear(float distance) {
        List<Actor> actors = playService.findAllActor();
        for (Actor a : actors) {
            if (!actorService.isDead(a) 
                    && actorService.isEnemy(a, player) 
                    && actorService.distance(a, player) < distance) {
                return true;
            }
        }
        return false;
    }
    
    // 设置角色为保护状态或非保护状态
    private void setProtected(Actor actor, boolean bool) {
        if (bool) {
            stateNetwork.addState(actor, IdConstants.STATE_SAFE, null);
        } else {
            stateNetwork.removeState(actor, IdConstants.STATE_SAFE);
        }
    }
    
    private String get(String rid, Object... param) {
        return ResourceManager.getOther("resource_gb", rid, param);
    }
}