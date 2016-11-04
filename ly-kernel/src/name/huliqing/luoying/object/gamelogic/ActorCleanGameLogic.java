/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.luoying.object.gamelogic;

import java.util.ArrayList;
import java.util.List;
import name.huliqing.luoying.LuoYing;
import name.huliqing.luoying.Factory;
import name.huliqing.luoying.object.actor.Actor;
import name.huliqing.luoying.constants.ActorConstants;
import name.huliqing.luoying.data.GameLogicData;
import name.huliqing.luoying.layer.network.PlayNetwork;
import name.huliqing.luoying.layer.service.ElService;
import name.huliqing.luoying.layer.service.PlayService;
import name.huliqing.luoying.object.el.SBooleanEl;
import name.huliqing.luoying.object.entity.Entity;

/**
 * 场景清洁器,用于清理场景中已经死亡的角色之类的功能
 * @author huliqing
 * @param <T>
 */
public class ActorCleanGameLogic<T extends GameLogicData> extends AbstractGameLogic<T> {
//    private final ActorService actorService = Factory.get(ActorService.class);
    private final PlayService playService = Factory.get(PlayService.class);
    private final ElService elService = Factory.get(ElService.class);
    private final PlayNetwork playNetwork = Factory.get(PlayNetwork.class);
    
    // 默认角色死亡后被清理出战场的时间
    private float cleanInterval = 10;
    
    // 这条表达式用于判断哪些角色可以清理
    private SBooleanEl checkEl;
    
    // ---- inner
    private final List<Entity> temps = new ArrayList<Entity>();

    @Override
    public void setData(T data) {
        super.setData(data);
        cleanInterval = data.getAsFloat("cleanInterval", cleanInterval);
        checkEl = elService.createSBooleanEl(data.getAsString("checkEl", "#{false}"));
    }
    
    @Override
    protected void doLogic(float tpf) {
        List<Actor> actors = playService.getEntities(Actor.class, null);
        if (actors == null || actors.isEmpty())
            return;
        
        // 记录需要被清理的角色
        Long deadTime;
        for (Entity a : actors) {
            
            // remove20161102
//            // “Player”、“未死亡”、“必要”的角色都不能移除
//            if (!actorService.isDead(a) || actorService.isPlayer(a) || actorService.isEssential(a)) {
//                a.getSpatial().getUserDataKeys().remove(ActorConstants.USER_DATA_DEAD_TIME_FLAG);
//                continue;
//            }
//            deadTime = (Long) a.getSpatial().getUserData(ActorConstants.USER_DATA_DEAD_TIME_FLAG);
//            if (deadTime == null) {
//                a.getSpatial().setUserData(ActorConstants.USER_DATA_DEAD_TIME_FLAG, LuoYing.getGameTime());
//            } else {
//                if (LuoYing.getGameTime() - deadTime > cleanInterval * 1000) {
//                    a.getSpatial().getUserDataKeys().remove(ActorConstants.USER_DATA_DEAD_TIME_FLAG);
//                    temps.add(a);
//                }
//            }

            if (checkEl.setSource(a.getAttributeManager()).getValue()) {
                deadTime = (Long) a.getSpatial().getUserData(ActorConstants.USER_DATA_DEAD_TIME_FLAG);
                if (deadTime == null) {
                    a.getSpatial().setUserData(ActorConstants.USER_DATA_DEAD_TIME_FLAG, LuoYing.getGameTime());
                } else {
                    if (LuoYing.getGameTime() - deadTime > cleanInterval * 1000) {
                        a.getSpatial().getUserDataKeys().remove(ActorConstants.USER_DATA_DEAD_TIME_FLAG);
                        temps.add(a);
                    }
                }
            }
        }
        
        // 清理角色
        if (!temps.isEmpty()) {
            for (Entity a : temps) {
                playNetwork.removeEntity(a);
            }
            temps.clear();
        }
    }
    
    @Override
    public void cleanup() {
        temps.clear();
        super.cleanup();
    }
}