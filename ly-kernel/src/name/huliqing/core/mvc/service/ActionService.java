/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.core.mvc.service;

import name.huliqing.core.mvc.network.ActionNetwork;
import name.huliqing.core.object.action.Action;
import name.huliqing.core.object.actor.Actor;

/**
 * 
 * @author huliqing
 */
public interface ActionService extends ActionNetwork{
    
    /**
     * 载入一个Action
     * @param actionId
     * @return 
     */
    Action loadAction(String actionId);
    
    /**
     * 判断角色当前是否正在执行战斗行为。
     * @param actor
     * @return 
     */
    boolean isPlayingFight(Actor actor);
    
    /**
     * 是否正在执行跑路行为
     * @param actor
     * @return 
     */
    boolean isPlayingRun(Actor actor);
    
    /**
     * 判断目标角色是否正在跟随
     * @param actor
     * @return 
     */
    boolean isPlayingFollow(Actor actor);
    
    /**
     * 获取当前角色正在执行的行为,如果没有任何行为则返回null.
     * @param actor
     * @return 
     */
    Action getPlayingAction(Actor actor);

}
