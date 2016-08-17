/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.core.mvc.service;

import com.jme3.math.Vector3f;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import name.huliqing.core.LY;
import name.huliqing.core.Config;
import name.huliqing.core.Factory;
import name.huliqing.core.constants.ResConstants;
import name.huliqing.core.constants.SkillConstants;
import name.huliqing.core.data.SkillData;
import name.huliqing.core.enums.SkillType;
import name.huliqing.core.mvc.dao.SkillDao;
import name.huliqing.core.data.AttributeUse;
import name.huliqing.core.enums.MessageType;
import name.huliqing.core.loader.Loader;
import name.huliqing.core.manager.ResourceManager;
import name.huliqing.core.xml.DataFactory;
import name.huliqing.core.object.actor.Actor;
import name.huliqing.core.object.actor.SkillListener;
import name.huliqing.core.object.actormodule.SkillActorModule;
import name.huliqing.core.object.skill.Skill;
import name.huliqing.core.object.skill.Walk;

/**
 *
 * @author huliqing
 */
public class SkillServiceImpl implements SkillService {
    private final static Logger LOG = Logger.getLogger(SkillServiceImpl.class.getName());

    private SkinService skinService;
    private AttributeService attributeService;
    private ElService elService;
    private SkillDao skillDao;
    private PlayService playService;
    private ActorService actorService;
    
    @Override
    public void inject() {
        attributeService = Factory.get(AttributeService.class);
        skinService = Factory.get(SkinService.class);
        elService = Factory.get(ElService.class);
        skillDao = Factory.get(SkillDao.class);
        playService = Factory.get(PlayService.class);
        actorService = Factory.get(ActorService.class);
    }

    @Override
    public Skill loadSkill(String skillId) {
        return Loader.loadSkill(skillId);
    }
    
    @Override
    public void addSkill(Actor actor, String skillId) {
        SkillData sd = DataFactory.createData(skillId);
        actor.getData().getSkillStore().add(sd);
    }
    
    @Override
    public SkillData getSkill(Actor actor, String skillId) {
        List<SkillData> skills = skillDao.getSkills(actor.getData());
        if (skills == null)
            return null;
        for (SkillData sd : skills) {
            if (sd.getId().equals(skillId)) {
                return sd;
            }
        }
        return null;
    }
    
    /**
     * 获取角色身上指定的技能，如果存在多个相同类型的技能，则返回第一个找到
     * 的就可以。
     * @param actor
     * @param skillType
     * @return 
     */
    @Override
    public SkillData getSkill(Actor actor, SkillType skillType) {
        return skillDao.getSkillFirst(actor.getData(), skillType);
    }

    @Override
    public Skill getSkillInstance(Actor actor, String skillId) {
        SkillData skillData = getSkill(actor, skillId);
        if (skillData != null) {
            SkillActorModule module = actor.getModule(SkillActorModule.class);
            if (module != null) {
                return module.findSkill(skillData);
            }
        }
        return null;
    }
    
    @Override
    public List<SkillData> getSkill(Actor actor) {
        return skillDao.getSkills(actor.getData());
    }
    
    @Override
    public SkillData getSkillRandom(Actor actor, SkillType skillType) {
        int wt = skinService.getWeaponState(actor);
        SkillData randomSkill = skillDao.getSkillRandom(actor.getData(), skillType, wt);
        if (randomSkill != null) {
            return randomSkill;
        }
        return null;
    }

    @Override
    public SkillData getSkillRandomDefend(Actor actor) {
        int wt = skinService.getWeaponState(actor);
        SkillData defendSkillData = skillDao.getSkillRandom(actor.getData(), SkillType.defend, wt);
        if (defendSkillData != null) {
            return defendSkillData;
        }
        return null;
    }

    @Override
    public SkillData getSkillRandomDuck(Actor actor) {
        int wt = skinService.getWeaponState(actor);
        SkillData duckSkillData = skillDao.getDuckSkillRandom(actor.getData(), wt);
        if (duckSkillData != null) {
            return duckSkillData;
        }
        return null;
    }
    
    @Override
    public SkillData getSkillRandomWalk(Actor actor) {
        int wt = skinService.getWeaponState(actor);
        return skillDao.getSkillRandom(actor.getData(), SkillType.walk, wt);
    }
    
    @Override
    public boolean playFaceTo(Actor actor, Vector3f position) {
        if (isLocked(actor, SkillType.walk) && isLocked(actor, SkillType.run)) {
            return false;
        }
        
        SkillActorModule module = actor.getModule(SkillActorModule.class);
        if (module == null) {
            return false;
        }
        
        boolean result = module.playFaceTo(position);
        return result;
    }
    
    // 检查并升级技能
    private void skillLevelUp(Actor actor, SkillData data) {
        if (data.getLevel() >= data.getMaxLevel()) 
            return;
        if (data.getLevelUpEl() == null)
            return;
        int levelPoints = (int) elService.getLevelEl(data.getLevelUpEl(), data.getLevel() + 1);
        if (data.getSkillPoints() >= levelPoints) {
            data.setLevel(data.getLevel() + 1);
            data.setSkillPoints(data.getSkillPoints() - levelPoints);
            if (playService.getPlayer() == actor) {
                playService.addMessage(
                        ResourceManager.get(ResConstants.SKILL_LEVEL_UP, new Object[]{ResourceManager.getObjectName(data)})
                        , MessageType.item);
            }
            skillLevelUp(actor, data);
        }
    }
    
    @Override
    public boolean isPlayable(Actor actor, Skill skill, boolean force) {
        return checkStateCode(actor, skill, force) == SkillConstants.STATE_OK;
    }
    
    @Override
    public int checkStateCode(Actor actor, Skill skill, boolean force) {
        // 1.强制执行，则忽略所有任何检查,即使武器不符合条件
        if (force) {
            return SkillConstants.STATE_OK;
        }
        
        SkillData data = skill.getSkillData();
        
        // 2.如果技能被锁定中，则不能执行
        if (isLocked(actor, data.getSkillType())) {
            return SkillConstants.STATE_SKILL_LOCKED;
        }
        
        // 3.武器状态检查,有一些技能需要拿特定的武器才能执行。
        List<Integer> wts = data.getWeaponStateLimit();
        if (wts != null) {
            int weaponState = skinService.getWeaponState(actor);
            if (!wts.contains(weaponState)) {
                return SkillConstants.STATE_WEAPON_NOT_ALLOW;
            }
            
            if (!skinService.isWeaponTakeOn(actor)) {
                return SkillConstants.STATE_WEAPON_NEED_TAKE_ON;
            }
        }
        
        // 6.角色需要达到指定等级才能使用技能
        if (actor.getData().getLevel() < data.getNeedLevel()) {
            return SkillConstants.STATE_NEED_LEVEL;
        }
        
        // 7.冷却中
        if (isCooldown(data)) {
            return SkillConstants.STATE_SKILL_COOLDOWN;
        }
        
        // 8.属性值不够用
        List<AttributeUse> uas = data.getUseAttributes();
        if (uas != null) {
            for (AttributeUse ua : uas) {
                if (attributeService.getDynamicValue(actor, ua.getAttribute()) < ua.getAmount()) {
                    return SkillConstants.STATE_MANA_NOT_ENOUGH;
                }
            }
        }
        
        // 9.如果新技能自身判断不能执行，例如加血技能或许就不可能给敌军执行。
        // 有很多特殊技能是不能对一些特定目标执行的，所以这里需要包含技能自身的判断
//        int stateCode = actor.getSkillProcessor().findSkill(data).canPlay();
        skill.setActor(actor); // 有时候skill是从外部载入的，没有设置actor，所以这里必须设置。
        int stateCode = skill.canPlay();
        if (stateCode != SkillConstants.STATE_OK) {
            return stateCode;
        }
        
        // 10.侦听器监听，允许一些侦听器阻止技能的执行,比如角色身上存在的一些状态
        // 效果,这些状态可能会监听角色技能的执行，并判断是否允许执行这个技能。
        // 比如“晕眩”、“缠绕”等状态就可能监测并侦听角色的技能，以阻止一些技能
        // 的执行。
        SkillActorModule module = actor.getModule(SkillActorModule.class);
        if (module == null) {
            return SkillConstants.STATE_UNDEFINE;
        }
        List<SkillListener> skillListeners = module.getSkillListeners();
        if (skillListeners != null && !skillListeners.isEmpty()) {
            for (SkillListener sl : skillListeners) {
                if (!sl.onSkillHookCheck(actor, skill)) {
                    return SkillConstants.STATE_HOOK;
                }
            }
        }
        
        // ---- 
        
        // 11.如果当前正在执行的所有技能都可以被新技能覆盖，则直接返回OK.
        // 注意：overlaps判断要放在interrupts判断之前，因为如果可以覆盖正在执行
        // 的技能就不需要中断它们
        long runningStates = module.getRunningSkillStates();
        if ((data.getOverlaps() & runningStates) == runningStates) {
            return SkillConstants.STATE_OK;
        }
        
        // 12.当正在执行中的某些技能不能被新技能中断则也不能执行新技能。
        // 只要有一个正在执行的技能不能被中断，则不能执行新技能
        if ((data.getInterrupts() & runningStates) != runningStates) {
            return SkillConstants.STATE_CAN_NOT_INTERRUPT;
        }
        
        return SkillConstants.STATE_OK;
    }

    @Override
    public boolean isCooldown(SkillData skillData) {
        return LY.getGameTime() - skillData.getLastPlayTime() 
                < skillData.getCooldown() * 1000;
    }

    @Override
    public boolean isSkillLearned(Actor actor, String skillId) {
        List<SkillData> skills = actor.getData().getSkillStore().getSkills();
        if (skills == null || skills.isEmpty()) {
            return false;
        }
        for (SkillData sd : skills) {
            if (sd.getId().equals(skillId)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean playSkill(Actor actor, Skill skill, boolean force) {
        int code = checkStateCode(actor, skill, force);
        if (code != SkillConstants.STATE_OK) {
            if (Config.debug) {
                LOG.log(Level.INFO, "Could not PlaySkill, actor={0}, skill={1}, force={2}, errorCode={3}"
                        , new Object[]{actor.getData().getName(), skill.getSkillData().getId(), force, code});
            }
            return false;
        }
        
        // --1. 执行技能
        SkillActorModule c = actor.getModule(SkillActorModule.class);
        if (c == null) {
            return false;
        }
        c.playSkill(skill);
        
        // --2. 技能消耗
        SkillData data = skill.getSkillData();
        List<AttributeUse> uas = data.getUseAttributes();
        if (uas != null) {
            for (AttributeUse ua : uas) {
                attributeService.applyDynamicValue(actor, ua.getAttribute(), ua.getAmount() * -1);
                attributeService.clampDynamicValue(actor, ua.getAttribute());
            }
        }

        // --3.记录最近一次执行时间
        data.setLastPlayTime(LY.getGameTime());

        // --4.检查技能升级
        data.setSkillPoints(data.getSkillPoints() + 1);
        skillLevelUp(actor, data);
        
        return true;
    }

    @Override
    public boolean playSkill(Actor actor, String skillId, boolean force) {
        SkillActorModule c = actor.getModule(SkillActorModule.class);
        if (c == null) {
            return false;
        }
        
        SkillData skillData = getSkill(actor, skillId);
        Skill skill = c.findSkill(skillData);
        return playSkill(actor, skill, force);
    }

    @Override
    public boolean playWalk(Actor actor, String skillId, Vector3f dir, boolean faceToDir, boolean force) {
        SkillActorModule c = actor.getModule(SkillActorModule.class);
        if (c == null) {
            return false;
        }
        
        SkillData sd = getSkill(actor, skillId);
        Skill skill = c.findSkill(sd);
        
        if (skill instanceof Walk) {
            Walk wSkill = (Walk) skill;
            wSkill.setWalkDirection(dir);
            if (faceToDir) {
                wSkill.setViewDirection(dir);
            }
        }
        return playSkill(actor, sd.getId(), force);
    }
    
    @Override
    public boolean isPlayingSkill(Actor actor) {
        SkillActorModule module = actor.getModule(SkillActorModule.class);
        if (module != null) {
            return module.isPlayingSkill();
        }
        return false;
    }
    
    @Override
    public boolean isPlayingSkill(Actor actor, SkillType skillType) {
        SkillActorModule c = actor.getModule(SkillActorModule.class);
        if (c != null) {
            return c.isPlayingSkill(skillType);
        }
        return false;
    }

    @Override
    public boolean isWaiting(Actor actor) {
        SkillActorModule c = actor.getModule(SkillActorModule.class);
        if (c != null) {
            return c.isWaiting();
        }
        return false;
    }

    @Override
    public boolean isRunning(Actor actor) {
        SkillActorModule c = actor.getModule(SkillActorModule.class);
        if (c != null) {
            return c.isRunning();
        }
        return false;
    }
    
    @Override
    public boolean isDucking(Actor actor) {
        SkillActorModule c = actor.getModule(SkillActorModule.class);
        return c != null ? c.isDucking() : false;
    }

    @Override
    public boolean isAttacking(Actor actor) {
        SkillActorModule c = actor.getModule(SkillActorModule.class);
        return c != null ? c.isAttacking() : false;
    }

    @Override
    public boolean isDefending(Actor actor) {
        SkillActorModule c = actor.getModule(SkillActorModule.class);
        return c != null ? c.isDefending(): false;
    }

    @Override
    public Skill getPlayingSkill(Actor actor, SkillType skillType) {
        SkillActorModule module = actor.getModule(SkillActorModule.class);
        if (module != null) {
            return module.getPlayingSkill(skillType);
        }
        return null;
    }

    @Override
    public long getPlayingSkillStates(Actor actor) {
        SkillActorModule module = actor.getModule(SkillActorModule.class);
        if (module != null) {
            return module.getPlayingSkillStates();
        }
        return 0;
    }

    @Override
    public void lockSkill(Actor actor, SkillType... skillType) {
        long lockedState = actor.getData().getSkillLockedState();
        for (SkillType st : skillType) {
            lockedState |= 1 << st.getValue();
        }
        actor.getData().setSkillLockedState(lockedState);
    }

    @Override
    public void lockSkillAll(Actor actor) {
        actor.getData().setSkillLockedState(0xFFFFFFFF);
    }

    @Override
    public void unlockSkill(Actor actor, SkillType... skillType) {
        long lockedState = actor.getData().getSkillLockedState();
        for (SkillType st : skillType) {
            lockedState &= ~(1 << st.getValue());
        }
        actor.getData().setSkillLockedState(lockedState);
    }

    @Override
    public void unlockSkillAll(Actor actor) {
        actor.getData().setSkillLockedState(0);
    }
    
    @Override
    public boolean isLocked(Actor actor, SkillType skillType) {
        return (actor.getData().getSkillLockedState() & (1 << skillType.getValue())) != 0;
    }

    @Override
    public void lockSkillChannels(Actor actor, String... channels) {
        actorService.setChannelLock(actor, true, channels);
    }

    @Override
    public void unlockSkillChannels(Actor actor, String... channels) {
        actorService.setChannelLock(actor, false, channels);
    }
    
    @Override
    public float getSkillTrueUseTime(Actor actor, SkillData skillData) {
        SkillActorModule module = actor.getModule(SkillActorModule.class);
        Skill skill = module.findSkill(skillData);
        return skill.getTrueUseTime();
    }

    @Override
    public void addSkillListener(Actor actor, SkillListener skillListener) {
        SkillActorModule module = actor.getModule(SkillActorModule.class);
        module.addSkillListener(skillListener);
    }

    @Override
    public boolean removeSkillListener(Actor actor, SkillListener skillListener) {
        SkillActorModule module = actor.getModule(SkillActorModule.class);
        return module.removeSkillListener(skillListener);
    }
    
}