/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.fighter.object.talent;

import name.huliqing.fighter.data.TalentData;
import name.huliqing.fighter.object.actor.Actor;

/**
 * 天赋，注：天赋是不会结束的，一旦获得就一直存在，除非手动移除。
 * 所以没有isEnd()方法。
 * @author huliqing
 */
public interface Talent {
    
    TalentData getData();
    
    /**
     * 初始化天赋,当给指定角色添加天赋时该方法应该被调用一次，以进行初始化。
     */
    void init();
    
    /**
     * 天赋逻辑
     * @param tpf 
     */
    void update(float tpf);
    
    /**
     * 清理并释放资源，当天赋从目标身上移除时应该执行一次该方法。
     */
    void cleanup();
    
    void setActor(Actor actor);
    
    /**
     * 更新天赋到指定的级别
     * @param level 
     */
    void updateLevel(int level);
}
