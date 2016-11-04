/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.luoying.object.item;

import java.util.logging.Logger;
import name.huliqing.luoying.Factory;
import name.huliqing.luoying.data.ItemData;
import name.huliqing.luoying.layer.service.ItemService;
import name.huliqing.luoying.layer.service.SkillService;
import name.huliqing.luoying.object.Loader;
import name.huliqing.luoying.object.entity.Entity;
import name.huliqing.luoying.object.module.SkillModule;
import name.huliqing.luoying.object.skill.Skill;

/**
 * 消耗物品来使用一个技能，示例：如使用复活卷轴,在使用这个物品的时候会调用一
 * 个技能来让角色执行。
 * @author huliqing
 */
public class SkillItem extends AbstractItem {
    private String skillId;

    @Override
    public void setData(ItemData data) {
        super.setData(data); 
        skillId = data.getAsString("skill");
    }
    
    @Override
    public void use(Entity actor) {
        super.use(actor);
        
        Skill skill = Loader.load(skillId);
        if (skill != null) {
            SkillModule skillModule = actor.getModuleManager().getModule(SkillModule.class);
            if (skillModule != null) {
                skill.setActor(actor);
                skillModule.playSkill(skill, false, skillModule.checkNotWantInterruptSkills(skill));
            }
        }
    }
    
    
}