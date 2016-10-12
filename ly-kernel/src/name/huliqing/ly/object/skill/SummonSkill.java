/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.ly.object.skill;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.util.TempVars;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import name.huliqing.ly.Factory;
import name.huliqing.ly.data.SkillData;
import name.huliqing.ly.layer.network.ActorNetwork;
import name.huliqing.ly.layer.network.PlayNetwork;
import name.huliqing.ly.layer.service.ActorService;
import name.huliqing.ly.layer.service.PlayService;
import name.huliqing.ly.network.Network;
import name.huliqing.ly.layer.service.ConfigService;
import name.huliqing.ly.layer.service.LogicService;
import name.huliqing.ly.object.Loader;
import name.huliqing.ly.object.anim.Anim;
import name.huliqing.ly.object.anim.AnimationControl;
import name.huliqing.ly.object.anim.Listener;
import name.huliqing.ly.object.anim.MoveAnim;
import name.huliqing.ly.object.entity.Entity;
import name.huliqing.ly.utils.GeometryUtils;
import name.huliqing.ly.utils.ThreadHelper;

/**
 * 召唤技
 * @author huliqing
 */
public class SummonSkill extends AbstractSkill {
    private final static Logger LOG = Logger.getLogger(SummonSkill.class.getName());
    private final ActorNetwork actorNetwork = Factory.get(ActorNetwork.class);
    private final PlayNetwork playNetwork = Factory.get(PlayNetwork.class);
    private final PlayService playService = Factory.get(PlayService.class);
    private final ActorService actorService = Factory.get(ActorService.class);
    private final ConfigService configService = Factory.get(ConfigService.class);
    private final LogicService logicService = Factory.get(LogicService.class);
    
    // 要召唤的角色的ID
    private String summonId;
    
    // 召唤的时间插值点，如果为0，则立即召唤。如果为0.5f则表示动作执行到一半时召唤 
    private float summonPoint = 0.3f;
    // summon的偏移位置该位置是以角色自身坐标为依据
    private Vector3f summonOffset = new Vector3f(0,0,0);
    // 调唤出角色的用时,单位秒
    private float summonTime = 4.0f;
    
    // ---- 内部
    private final List<SummonOper> cache = new ArrayList<SummonOper>(1);
    private SummonOper currentSummon;
    
    @Override
    public void setData(SkillData data) {
        super.setData(data); 
        this.summonId = data.getAsString("summonActorId", summonId);
        this.summonPoint = data.getAsFloat("summonPoint", summonPoint);
        this.summonOffset = data.getAsVector3f("summonOffset", summonOffset);
        this.summonTime = data.getAsFloat("summonTime", summonTime);
    }
    
    private SummonOper getOperFromCache() {
//        logger.log(Level.INFO, "SummonOper.size={0}", cache.size());
        SummonOper result = null;
        for (int i = 0; i < cache.size(); i++) {
            if (cache.get(i).isEnd()) {
                result = cache.get(i);
                break;
            }
        }
        if (result == null) { // create new one and cache.
            result = new SummonOper();
            cache.add(result);
        }
        return result;
    }

    @Override
    public void initialize() {
        super.initialize();
        
        if (summonId == null) {
            LOG.log(Level.INFO, "No summonActorId set!");
            cleanup();
            return;
        }
        
        // 提前一点点时间载入角色
        currentSummon = getOperFromCache();
        currentSummon.summonObjectId = summonId;
        currentSummon.preload();
        
        // 添加到角色所在场景
        actor.getScene().getRoot().attachChild(currentSummon);
    }

    @Override
    protected void doUpdateLogic(float tpf) {
        if (summonId == null) {
            cleanup();
            return;
        }
        
        if (!currentSummon.started) {
            if ((time / trueUseTime) >= summonPoint) {
                currentSummon.start();
            }
        }
    }
    
    private Vector3f getLocalToWorld(Entity actor, Vector3f localPos, Vector3f store) {
        if (store == null) {
            store = new Vector3f();
        }
        actor.getSpatial().getWorldRotation().mult(localPos, store);
        store.addLocal(actor.getSpatial().getWorldTranslation());
        return store;
    }
    
    // 负责召唤的工具类，Node
    private class SummonOper extends Node implements Listener {
        // 召唤的物品ID:May actor or obj.
        String summonObjectId;
        Vector3f summonPos = new Vector3f();
        HelpLoader loader = new HelpLoader();
        Future<Entity> future;
        
        // -- state
        boolean started;
        boolean init;
        float time;// 当前用时
        
        // 是否已经在载入角色
        boolean loadStarted;
        // 载入的角色，可能是actor或obj
        Entity summonActor;
        // 是否正在召出角色
        boolean showing;
        // 用于显示角色的动画处理器
        MoveAnim showAnim = new MoveAnim();
        AnimationControl animationControl = new AnimationControl(showAnim);
        
        SummonOper() {
            showAnim.addListener(this);
        }
        
        void start() {
            this.started = true;
        }
        
        /**
         * 提前载入目标,提高性能
         */
        void preload() {
            if (!loadStarted && summonObjectId != null) {
                loader.loadId = summonObjectId;
                future = ThreadHelper.submit(loader);
                loadStarted = true;
            }
        }
        
        void doInit() {
            this.showAnim.setUseTime(summonTime);
        }

        @Override
        public void updateLogicalState(float tpf) {
//            super.updateLogicalState(tpf);
            
            if (!started) {
                return;
            }
            if (!init) {
                doInit();
                init = true;
            }
            
            if (!loadStarted) {
                preload();
            }
            
            time += tpf;
            
            if (future != null && future.isDone()) {
                try {
                    summonActor = future.get();
                    future = null;
                    
                    // 根据当前角色的朝向计算召唤点
                    getLocalToWorld(actor, summonOffset, summonPos);
                    Vector3f terrainHeight = playService.getTerrainHeight(actor.getScene(), summonPos.x, summonPos.z);
                    if (terrainHeight != null) {
                        summonPos.set(terrainHeight);
                    }
                    summonPos.setY(summonPos.y + summonOffset.y);
                    setLocalTranslation(summonPos);
                    
                } catch (Exception ex) {
                    Logger.getLogger(SummonSkill.class.getName()).log(Level.SEVERE
                            , "Unload object, summonObjectId=" + summonObjectId, ex);
                    cleanup();
                    return;
                }
            }
            
//            logger.log(Level.INFO, "summon time={0}, showTime={1}, showing={2}", new Object[] {time, showTime, showing});
            // 开始展示角色。
            if (!showing && summonActor != null) {
                TempVars tv = TempVars.get();
                
                // 设置同伴等级
                actorService.setLevel(summonActor, (int) (actorService.getLevel(actor) * configService.getSummonLevelFactor()));
                actorService.setPhysicsEnabled(summonActor, false);
                actorService.setPartner(actor, summonActor);
                actorService.setColor(summonActor, new ColorRGBA(1f, 1f, 2f, 1));
                playNetwork.addEntity(summonActor);
                
                // 动画展示召唤角色，上升动画,end位置要向上加大一些，以避免召唤后角色在
                // 开启物理特性后掉到地下。
                Vector3f start = tv.vect1.set(summonPos).subtractLocal(
                        0, GeometryUtils.getModelHeight(summonActor.getSpatial()), 0);
                Vector3f end = tv.vect2.set(summonPos).addLocal(0, 0.5f, 0);
                
                actorService.setLocation(summonActor, start);
                actorService.setLookAt(summonActor, tv.vect3.set(actor.getSpatial().getWorldTranslation()).setY(start.getY()));
                
                summonActor.getSpatial().addControl(animationControl);
                showAnim.setTarget(summonActor.getSpatial());
                showAnim.setStartPos(start);
                showAnim.setEndPos(end);
                showAnim.start();
                
//                if (Config.debug) {
//                    logger.log(Level.INFO, "summon start to showing!");
//                }
                
                showing = true;
                tv.release();
            }
            
            if (summonActor != null && !showAnim.isEnd()) {
                Network.getInstance().syncTransformDirect(summonActor);
            }
        }
        
        void cleanup() {
            this.started = false;
            this.init = false;
            this.summonActor = null;
            this.summonObjectId = null;
            this.showing = false;
            this.loadStarted = false;
            this.time = 0;
            if (future != null) {
                future.cancel(true);
            }
            this.future = null;
            this.removeFromParent();
        }
        
        boolean isEnd() {
            return !started && showAnim.isEnd();
        }

        @Override
        public void onDone(Anim anim) {
            Spatial summonModel = animationControl.getSpatial();
            if (summonModel != null) {
                // 让召唤到的目标获得物理碰撞
//                Entity ac = summonModel.getControl(Actor.class);
                actorService.setLocation(summonActor, summonModel.getLocalTranslation());
                logicService.setAutoLogic(summonActor, true);
                actorNetwork.setPhysicsEnabled(summonActor, true);// 物理开需要同步
                // 移除control,animationControl不再有意义
                summonModel.removeControl(animationControl);
            }
            cleanup();
        }
    }
    
    // help to call
    private class HelpLoader implements Callable<Entity> {

        String loadId;
        
        /** 是否正在载入中...*/
        boolean calling;
        
        @Override
        public Entity call() throws Exception {
            calling = true;
            Entity result = Loader.load(loadId);
            calling = false;
            return result;
        }
    }
}
