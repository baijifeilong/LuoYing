/*
 * LuoYing is a program used to make 3D RPG game.
 * Copyright (c) 2014-2016 Huliqing <31703299@qq.com>
 * 
 * This file is part of LuoYing.
 *
 * LuoYing is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LuoYing is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LuoYing.  If not, see <http://www.gnu.org/licenses/>.
 */
///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package name.huliqing.luoying.object.action;
//
//import com.jme3.bullet.collision.PhysicsCollisionEvent;
//import com.jme3.math.FastMath;
//import com.jme3.math.Vector3f;
//import com.jme3.util.TempVars;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import name.huliqing.core.Config;
//import name.huliqing.core.Factory;
//import name.huliqing.core.enums.SkillType;
//import name.huliqing.core.mvc.network.SkillNetwork;
//import name.huliqing.core.mvc.service.ActorService;
//import name.huliqing.core.mvc.service.PlayService;
//import name.huliqing.core.mvc.service.SkillService;
//import name.huliqing.core.mvc.service.StateService;
//import name.huliqing.core.object.actor.Actor;
//import name.huliqing.core.object.actor.PhysicsListener;
//import name.huliqing.core.utils.MathUtils;
//
///**
// * 使用侦听物理碰撞的方式来检查角色是否发现障碍物。该方法目前还存在BUG，
// * 暂不应该使用，目前在发现碰撞后的处理方式上仍存在问题，可能发生角色碰撞后
// * 锁定在一起无法移动，特别是跟随类的角色。
// * @deprecated 
// * @author huliqing
// */
//public class SimpleDetour extends Detour implements PhysicsListener {
//    private static final Logger LOG = Logger.getLogger(SimpleDetour.class.getName());
//    
//    // 遇到障碍物时的调转方向 =》　1:left; -1:right; 0: none;
//    private int direction = 1;
//    private final PlayService playService = Factory.get(PlayService.class);
//    private final StateService stateService = Factory.get(StateService.class);
//    private final ActorService actorService = Factory.get(ActorService.class);
//    private final SkillService skillService = Factory.get(SkillService.class);
//    private final SkillNetwork skillNetwork = Factory.get(SkillNetwork.class);
//    
//    private boolean needDetour;
//    
//    public SimpleDetour() {}
//    
//    public SimpleDetour(Action action) {
//        super(action);
//        checkInterval = 0;
//    }
//
//    @Override
//    public void setActor(Actor actor) {
//        super.setActor(actor);
//        actor.addActorPhysicsListener(this);
//        
//    }
//    
//    @Override
//    protected boolean isNeedDetour() {
//        return needDetour;
//    }
//
//    @Override
//    protected void tryDetour(int count) {
//        if (count == 0) {
//            direction = MathUtils.randomPON();
//        }
//        float lockTime = 0.01f * count;
//        
//        TempVars tv = TempVars.get();
//        float angle = 0;
//        if (lockTime < 0.5f) {
//            angle = 10 * (count + 1) * FastMath.DEG_TO_RAD * direction;
//        } else {
//            angle = 20 + (FastMath.nextRandomFloat() * 340) * FastMath.DEG_TO_RAD * direction;
//        }
//        
//        Vector3f walkDir = actorService.getWalkDirection(actor);
//        MathUtils.rotate(walkDir, angle, Vector3f.UNIT_Y, tv.vect2);
//        tv.vect1.set(tv.vect2);
//        skillNetwork.playWalk(actor, skillService.getSkill(actor, SkillType.run).getId(), tv.vect1, autoFacing, true);
//        needDetour = false;
//        tv.release();
//        
//        action.lock(lockTime);
//        if (Config.debug) {
//            LOG.log(Level.INFO, "SimpleDetour, tryDetour actor={0}, count={1}, angle={2}, lockTime={3}"
//                    , new Object[] {actor.getData().getId(), count, angle, lockTime});
//        }
//        
//    }
//
//    @Override
//    public void collision(Actor source, Object other, PhysicsCollisionEvent event) {
//        if (other == playService.getTerrain()) {
//            return;
//        }
////        LOG.log(Level.INFO, "SimpleDetour found collision, Actor={0}, Other={1}"
////                , new Object[] {source.getData().getId(), other});
//        needDetour = true;
//    }
//    
//}
