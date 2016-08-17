/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.core.mvc.service;

import name.huliqing.core.Inject;
import name.huliqing.core.data.ObjectData;
import name.huliqing.core.object.actor.Actor;

/**
 *
 * @author huliqing
 */
public interface ProtoService extends Inject {
    
    /**
     * 创建一个物体
     * @param id
     * @return 
     */
    ObjectData createData(String id);
    
    /**
     * 从角色身上获取物品,如果角色存上不存在该物品则返回null.
     * @param actor
     * @param id 
     * @return  
     */
    ObjectData getData(Actor actor, String id);
    
    /**
     * 给指定角色添加物体
     * @param actor
     * @param data
     * @param count 
     */
    void addData(Actor actor, ObjectData data, int count);
    
    /**
     * 让指定角色使用物体
     * @param actor
     * @param data 
     */
    void useData(Actor actor, ObjectData data);
    
    /**
     * 从角色身上移除指定的物体
     * @param actor
     * @param data
     * @param count 
     */
    void removeData(Actor actor, ObjectData data, int count);
    
    /**
     * 同步指定角色身上的物体数量
     * @param actor
     * @param objectId
     * @param total 
     */
    void syncDataTotal(Actor actor, String objectId, int total);
    
    /**
     * 获取物体的价值
     * @param data
     * @return 
     */
    float getCost(ObjectData data);
}