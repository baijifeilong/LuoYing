/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.core.object.skill;

import name.huliqing.core.Factory;
import name.huliqing.core.data.SkillData;
import name.huliqing.core.mvc.service.ActorService;

/**
 * 让角色“等待”的技能
 * @author huliqing
 * @param <T>
 */
public class WaitSkill<T extends SkillData> extends AbstractSkill<T> {
    
    private final ActorService actorService = Factory.get(ActorService.class);

    @Override
    protected void init() {
        super.init();
        // 对于一些没有“Wait动画”的角色必须想办法让它静止下来
        if (data.getAnimation() == null) {
            actorService.reset(actor);
        }
    }

    @Override
    protected void doUpdateLogic(float tpf) {
    }

    
}