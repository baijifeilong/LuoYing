/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.core.mvc.network;

import name.huliqing.core.Inject;
import name.huliqing.core.object.actor.Actor;

/**
 *
 * @author huliqing
 */
public interface ItemNetwork extends Inject  {
    
    /**
     * 给角色添加物品
     * @param actor
     * @param itemId
     * @param count 
     */
    void addItem(Actor actor, String itemId, int count);

    /**
     * 移除角色身上的物品
     * @param actor
     * @param itemId
     * @param count 要移除的数量,可能比角色实际拥有的数量大
     */
    void removeItem(Actor actor, String itemId, int count);
    
    /**
     * 让角色使用物品
     * @param actor
     * @param itemId  
     */
    void useItem(Actor actor, String itemId);
    
}
