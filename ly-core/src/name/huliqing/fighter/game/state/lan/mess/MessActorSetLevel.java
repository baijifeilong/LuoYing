/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.fighter.game.state.lan.mess;

import com.jme3.network.serializing.Serializable;
import name.huliqing.fighter.Factory;
import name.huliqing.fighter.game.service.ActorService;
import name.huliqing.fighter.game.service.PlayService;
import name.huliqing.fighter.object.actor.Actor;

/**
 * SC,设置角色的等级
 * @author huliqing
 */
@Serializable
public class MessActorSetLevel extends MessBase {
    
    private long actorId;
    private int level;

    public long getActorId() {
        return actorId;
    }

    public void setActorId(long actorId) {
        this.actorId = actorId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public void applyOnClient() {
        PlayService playService = Factory.get(PlayService.class);
        ActorService actorService = Factory.get(ActorService.class);
        Actor actor = playService.findActor(actorId);
        if (actor != null) {
            actorService.setLevel(actor, level);
        }
    }

}
