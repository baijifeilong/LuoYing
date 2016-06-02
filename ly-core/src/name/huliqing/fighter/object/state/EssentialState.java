/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.fighter.object.state;

import name.huliqing.fighter.Factory;
import name.huliqing.fighter.data.StateData;
import name.huliqing.fighter.game.service.ActorService;

/**
 *
 * @author huliqing
 */
public class EssentialState extends State {
    private final static ActorService actorService = Factory.get(ActorService.class);
    
    private boolean essential;
    private boolean restore;
    
    // ---- 保存原始状态以便恢复
    private boolean oldEssential;

    @Override
    public void initData(StateData data) {
        super.initData(data);
        this.essential = data.getAsBoolean("essential", essential);
        this.restore = data.getAsBoolean("restore", restore);
    }

    @Override
    public void initialize() {
        super.initialize();
        oldEssential = actorService.isEssential(actor);
        actorService.setEssential(actor, essential);
    }

    @Override
    public void cleanup() {
        if (restore) {
            actorService.setEssential(actor, oldEssential);
        }
        super.cleanup();
    }
    
}
