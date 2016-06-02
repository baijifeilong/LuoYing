/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.fighter.data;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import com.jme3.util.SafeArrayList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import name.huliqing.fighter.enums.Sex;
import name.huliqing.fighter.object.actor.SkillStore;
import name.huliqing.fighter.object.actor.ItemStore;
import name.huliqing.fighter.utils.ConvertUtils;
import name.huliqing.fighter.utils.MathUtils;

/**
 * @author huliqing
 */
@Serializable
public class ActorData extends ProtoData {
    
    // 角色名称
    private String name = "";
    
    // 角色等级
    private int level;
    
    // 当前的经验值,每经过一个等级后清0
    private int xp;
    
    // 角色所在分组
    private int group;
    
    // 角色性别
    private Sex sex;
    
    // 角色种族,0表示未设置
    private String race;
    
    // 角色是否必要的,不能移除的。
    private boolean essential;
    
    // 角色的基本皮肤，基本皮肤是用来在切换装备后“修补皮肤”缺失的问题。比如当
    // 穿上一套上下连身的法袍（同时包含lowerBody和upperBody）后，再使用一件只
    // 包含upperBody的装备来换上时，由于法袍被替换，这时角色身上将丢失lowerBody
    // 的装备，这时就需要从skinBase中查看是否有lowerBody的基本皮肤来补上，否则角
    // 色会缺少下半身的装备模型。
    // 注意：基本皮肤中不要指定哪些包含两个或两个以上type部位的皮肤。比如：
    // 同时包含lowerBody和upperBody的skin,如上下连身的套装之类的皮肤，这类皮肤
    // 会造成在换装备后进行修补的时候又替换掉已经穿上的装备。
    private List<SkinData> skinBase;
    
    // 角色物品信息,如:金币,装备,武器,杂物等
    private ItemStore itemStore;
    
    // 角色技能信息
    private SkillStore skillStore;
  
    // remove20160408
//    // 标记是否锁住了所有技能,只要锁住，则任何技能都不能再执行．
//    // 即使使用了force强制也不能执行技能．
//    private boolean skillLocked;
    
    // 角色物品的掉落设置
    private DropData drop;
    
    // 角色的逻辑列表
    private List<LogicData> logics;
    
    // 角色的状态列表，注：类型为SafeArrayList,部分Service中使用到循环删除的情况
    private List<StateData> states;
    
    // 状态抵抗设置
    private ResistData resist;
    
    // 角色的当前目标(唯一ID)
    private long target = -1;
    
    // 是否自动AI,如果关闭AI功能，则角色将完全停止运行所有逻辑
    private boolean autoAi = true;
    
    // 是否自动侦察敌人，如果关闭该功能，则角色不会查找敌人。
    private boolean autoDetect = true;
    
    // 角色的两个默认的行为，当角色处于玩家控制时需要这两个行为来执行打架和跑路
    // 如果这两个行为没有设置，则ActionService在必要时会为目标actor创建两个默认的行为
    // See ActionService.playFight,playRun
    private String actionDefFight;
    private String actionDefRun;
    
    // 标记武器是处于takeOn或takeOff状态,即取出或挂起的状态。
    // 处于使用中(isUsing=true)的武器存在两种状态，“挂起（待机状态）”，“取出（或者叫战斗状态）”
    private boolean weaponTakeOn;
    
    // 角色使用的武器插槽优先顺序，当设置了这个值之后，角色的武器在收起时将优先
    // 存放在这些指定的插槽上，否则将根据武器的定义的插槽存放。
    private List<String> slots;
    
    // 角色的所有属性设置，包含lifeAttributes
    private Map<String, AttributeData> attributes;
    // 用于决定角色是否死亡的属性,当这个属性值小于或等于0时，角色将被视为"死亡"
    private String lifeAttribute;
    // 用于决定角色的视角
    private String viewAttribute;
    // 角色的升级设置,关联到一个elLevel id
    private String levelUpEl;
    // 角色的经验掉落设置,关联到一个elXpDrop id
    private String xpDropEl;
    
    // 当前角色的主人(所有者)的ID,这是一个唯一ID
    private long ownerId = -1;
    
    // 角色颜色,对于一些召唤类角色需要
    private ColorRGBA color;

    // 当前可用的天赋点数,天赋点数可用到角色的天赋上，用后天赋点数会减少
    private int talentPoints;
    // 角色拥有的天赋列表
    private ArrayList<TalentData> talents;
    // 天赋点数的奖励公式
    private String talentPointsLevelEl;
    
    // 角色所在的队伍,0或小于0表示无分组
    private int team;
    
    // 是否为活着的生命体,活着是指能生物类,如人物角色,怪兽,野兽等. 像防御塔,宝箱等角色为非生物
    private boolean living;
    
    // 跟随目标,这是一个角色唯一ID, 表示当前角色要跟随的目标角色.如果该值小于0或等于0则表示无跟随目标.
    private long followTarget;
    
    // 角色出生地,坐标位置,暂时不同步到客户端
    // TODO: sync to client
    private transient Vector3f bornPlace;
    
    // 角色的对话面板id
    private String chat;
    
    // 任务列表
    private List<TaskData> tasks;
    
    // 被锁定的技能列表，二进制表示，其中每1个二进制位表示一个技能类型。
    // 如果指定的位为1，则表示这个技能类型被锁定，则这类技能将不能执行。
    // 默认值0表示没有任何锁定。
    private long skillLockedState;
    
    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(name.getBytes(), "name", null);
        oc.write(level, "level", 0);
        oc.write(xp, "xp", 0);
        oc.write(group, "group", 0);
        oc.write(sex, "sex", null);
        oc.write(race, "race", null);
        oc.write(essential, "essential", false);
        if (skinBase != null)
            oc.writeSavableArrayList(new ArrayList<SkinData>(skinBase), "skinBase", null);
        oc.write(itemStore, "itemStore", null);
        oc.write(skillStore, "skillStore", null);
//        oc.write(skillLocked, "skillLocked", false);// remove20160408
        oc.write(drop, "drop", null);
        if (logics != null)
            oc.writeSavableArrayList(new ArrayList<LogicData>(logics), "logics", null);
        if (states != null)
            oc.writeSavableArrayList(new ArrayList<StateData>(states), "states", null);
        oc.write(resist, "resistData", null);
        oc.write(target, "target", -1);
        oc.write(autoAi, "autoAi", true);
        oc.write(autoDetect, "autoDetect", true);
        oc.write(actionDefFight, "actionDefFight", null);
        oc.write(actionDefRun, "actionDefRun", null);
        oc.write(weaponTakeOn, "weaponTakeOn", false);
        if (slots != null)
            oc.write(slots.toArray(new String[]{}), "slots", null);
        
        // 这里把attributes转换成arrayList是因为jme在read的时候会把map转换成无序的(即使是LinkedHashMap)
        // 所以为了保证在读入的时候顺序正确，需要把map转换成list.
        if (attributes != null) {
            oc.writeSavableArrayList(new ArrayList<AttributeData>(attributes.values()), "attributes", null);
        }
        
        oc.write(lifeAttribute, "lifeAttribute", null);
        oc.write(viewAttribute, "viewAttribute", null);
        oc.write(levelUpEl, "levelUpEl", null);
        oc.write(xpDropEl, "xpDropEl", null);
        oc.write(ownerId, "ownerId", -1);
        oc.write(color, "color", null);
        oc.write(talentPoints, "talentPoints", 0);
        oc.writeSavableArrayList(talents, "talents", null);
        oc.write(talentPointsLevelEl, "talentPointsLevelEl", null);
//        oc.write(team, "team", 0);
        oc.write(living, "living", false);
        oc.write(followTarget, "followTarget", -1);
        if (bornPlace != null) {
            oc.write(bornPlace, "bornPlace", null);
        }
        oc.write(chat, "chat", null);
        if (tasks != null)
            oc.writeSavableArrayList(new ArrayList<TaskData>(tasks), "tasks", null);
        oc.write(skillLockedState, "skillLockedState", 0);
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule ic = im.getCapsule(this);
        name = new String(ic.readByteArray("name", "".getBytes()), "utf-8");
        level = ic.readInt("level", 0);
        xp = ic.readInt("xp", 0);
        group = ic.readInt("group", 0);
        sex = ic.readEnum("sex", Sex.class, null);
        race = ic.readString("race", null);
        essential = ic.readBoolean("essential", false);
        skinBase = ic.readSavableArrayList("skinBase", null);
        itemStore = (ItemStore) ic.readSavable("itemStore", null);
        skillStore = (SkillStore) ic.readSavable("skillStore", null);
//        skillLocked = ic.readBoolean("skillLocked", false);// remove20160408
        drop = (DropData) ic.readSavable("drop", null);
        logics = ic.readSavableArrayList("logics", null);
        states = ic.readSavableArrayList("states", null);
        resist = (ResistData) ic.readSavable("resistData", null);
        target = ic.readLong("target", -1);
        autoAi = ic.readBoolean("autoAi", true);
        autoDetect = ic.readBoolean("autoDetect", true);
        actionDefFight = ic.readString("actionDefFight", null);
        actionDefRun = ic.readString("actionDefRun", null);
        weaponTakeOn = ic.readBoolean("weaponTakeOn", false);
        slots = ConvertUtils.toList(ic.readStringArray("slots", null));
        
        ArrayList<AttributeData> ads = ic.readSavableArrayList("attributes", null);
        if (ads != null) {
            if (attributes == null) {
                attributes = new LinkedHashMap<String, AttributeData>(ads.size());
            }
            for (AttributeData ad : ads) {
                attributes.put(ad.getId(), ad);
            }
        }
        lifeAttribute = ic.readString("lifeAttribute", null);
        viewAttribute = ic.readString("viewAttribute", null);
        levelUpEl = ic.readString("levelUpEl", null);
        xpDropEl = ic.readString("xpDropEl", null);
        ownerId = ic.readLong("ownerId", -1);
        color = (ColorRGBA) ic.readSavable("color", null);
        talentPoints = ic.readInt("talentPoints", 0);
        talents = ic.readSavableArrayList("talents", null);
        talentPointsLevelEl = ic.readString("talentPointsLevelEl", null);
//        team = ic.readInt("team", 0);
        living = ic.readBoolean("living", false);
        followTarget = ic.readLong("followTarget", -1);
        bornPlace = (Vector3f)ic.readSavable("bornPlace", null);
        chat = ic.readString("chat", null);
        tasks = ic.readSavableArrayList("tasks", null);
        skillLockedState = ic.readLong("skillLockedState", 0);
    }
    
    public ActorData() {}
    
    public ActorData(String id) {
        super(id);
    }

    /**
     * 获取角色的基本皮肤,如果没有基本皮肤则返回null.
     * @return 
     */
    public List<SkinData> getSkinBase() {
        return skinBase;
    }

    /**
     * 设置角色的基本皮肤
     * @param skinBase 
     */
    public void setSkinBase(List<SkinData> skinBase) {
        this.skinBase = skinBase;
    }

    /**
     * 获取角色的物品列表，包含：武器，服装，杂物等等。
     * @return 
     */
    public ItemStore getItemStore() {
        if (itemStore == null) {
            itemStore = new ItemStore();
        }
        return itemStore;
    }

    public void setItemStore(ItemStore itemStore) {
        this.itemStore = itemStore;
    }

    public SkillStore getSkillStore() {
        if (skillStore == null) {
            skillStore = new SkillStore();
        }
        return skillStore;
    }

    public void setSkillStore(SkillStore skillStore) {
        this.skillStore = skillStore;
    }

    /**
     * 获取角色物品的掉落设置，
     * @return 
     */
    public DropData getDrop() {
        return drop;
    }

    /**
     * 设置角色的物品掉落设置
     * @param drop 
     */
    public void setDrop(DropData drop) {
        this.drop = drop;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    // remove20160408
//    public boolean isSkillLocked() {
//        return skillLocked;
//    }
//
//    public void setSkillLocked(boolean skillLocked) {
//        this.skillLocked = skillLocked;
//    }

    /**
     * 获取逻辑列表，如果没有设置过，则返回一个空列表
     * @return 
     */
    public List<LogicData> getLogics() {
        if (logics == null) {
            logics = new ArrayList<LogicData>();
        }
        return logics;
    }

    public void setLogics(List<LogicData> logics) {
        this.logics = logics;
    }

    /**
     * 获取状态列表，如果没有设置过，则返回一个空列表
     * @return 
     */
    public List<StateData> getStates() {
        if (states == null) {
            states = new ArrayList<StateData>();
        }
        return states;
    }

    public void setStates(List<StateData> states) {
        this.states = states;
    }

    /**
     * 获取任务列表，如果没有设置过任务则返回一个空列表
     * @return 
     */
    public List<TaskData> getTasks() {
        if (tasks == null) {
            tasks = new ArrayList<TaskData>();
        }
        return tasks;
    }

    public void setTasks(List<TaskData> tasks) {
        this.tasks = tasks;
    }

    public long getTarget() {
        return target;
    }

    public void setTarget(long target) {
        this.target = target;
    }

    public boolean isAutoAi() {
        return autoAi;
    }

    public void setAutoAi(boolean autoAi) {
        this.autoAi = autoAi;
    }

    public boolean isAutoDetect() {
        return autoDetect;
    }

    public void setAutoDetect(boolean autoDetect) {
        this.autoDetect = autoDetect;
    }
    
    public ColorRGBA getColor() {
        return color;
    }

    public void setColor(ColorRGBA color) {
        if (this.color == null) {
            this.color = new ColorRGBA(color);
        } else {
            this.color.set(color);
        }
    }

    /**
     * 获取角色默认的“战斗”行为的id
     * @return 
     */
    public String getActionDefFight() {
        return actionDefFight;
    }

    public void setActionDefFight(String actionDefFight) {
        this.actionDefFight = actionDefFight;
    }

    /**
     * 获取角色默认的“跑路”行为的id
     * @return 
     */
    public String getActionDefRun() {
        return actionDefRun;
    }

    public void setActionDefRun(String actionDefRun) {
        this.actionDefRun = actionDefRun;
    }

    /**
     * 标记武器是处于takeOn或takeOff状态,即取出或挂起的状态。
     * 处于使用中(isUsing=true)的武器存在两种状态，“挂起（待机状态）”，“取出（或者叫战斗状态）”
     * @return 
     */
    public boolean isWeaponTakeOn() {
        return weaponTakeOn;
    }

    public void setWeaponTakeOn(boolean weaponTakeOn) {
        this.weaponTakeOn = weaponTakeOn;
    }

    /**
     * 角色使用的武器插槽优先顺序，当设置了这个值之后，角色的武器在收起时将优先
     * 存放在这些指定的插槽上，否则将根据武器的定义的插槽存放。
     * @return 
     */
    public List<String> getSlots() {
        return slots;
    }

    /**
     * 角色使用的武器插槽优先顺序，当设置了这个值之后，角色的武器在收起时将优先
     * 存放在这些指定的插槽上，否则将根据武器的定义的插槽存放。
     * @param slots 
     */
    public void setSlots(List<String> slots) {
        this.slots = slots;
    }

    public Map<String, AttributeData> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, AttributeData> attributes) {
        this.attributes = attributes;
    }
    
    public String getLifeAttribute() {
        return lifeAttribute;
    }

    public void setLifeAttribute(String lifeAttribute) {
        this.lifeAttribute = lifeAttribute;
    }
    
    public AttributeData getLifeAttributeData() {
        if (attributes == null || lifeAttribute == null) {
            return null;
        }
        return attributes.get(lifeAttribute);
    }

    public String getViewAttribute() {
        return viewAttribute;
    }

    public void setViewAttribute(String viewAttribute) {
        this.viewAttribute = viewAttribute;
    }

    public AttributeData getViewAttributeData() {
        if (attributes == null || viewAttribute == null) {
            return null;
        }
        return attributes.get(viewAttribute);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getLevelUpEl() {
        return levelUpEl;
    }

    public void setLevelUpEl(String levelUpEl) {
        this.levelUpEl = levelUpEl;
    }

    public String getXpDropEl() {
        return xpDropEl;
    }

    public void setXpDropEl(String xpDropEl) {
        this.xpDropEl = xpDropEl;
    }
    
    /**
     * 获取当前的经验值,该值表示距离下一个等级，目前已经获得的经验值
     * @return 
     */
    public int getXp() {
        return xp;
    }

    /**
     * 设置当前的经验值，该值表示距离下一个等级，目前已经获得的经验值
     * @param xp 
     */
    public void setXp(int xp) {
        this.xp = MathUtils.clamp(xp, 0, Integer.MAX_VALUE);
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null)
            return;
        this.name = name;
    }
    
    /**
     * 获取角色的派系。
     * @return 
     */
    public int getGroup() {
        return group;
    }

    /**
     * 设置角色的派系
     * @param group 
     */
    public void setGroup(int group) {
        this.group = group;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }
    
    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public boolean isEssential() {
        return essential;
    }

    public void setEssential(boolean essential) {
        this.essential = essential;
    }
    
    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }
    
    public ResistData getResist() {
        return resist;
    }

    public void setResist(ResistData resistData) {
        this.resist = resistData;
    }

    public int getTalentPoints() {
        return talentPoints;
    }

    public void setTalentPoints(int talentPoints) {
        this.talentPoints = talentPoints;
    }

    public List<TalentData> getTalents() {
        return talents;
    }

    public void setTalents(ArrayList<TalentData> talents) {
        this.talents = talents;
    }

    public String getTalentPointsLevelEl() {
        return talentPointsLevelEl;
    }

    public void setTalentPointsLevelEl(String talentPointsLevelEl) {
        this.talentPointsLevelEl = talentPointsLevelEl;
    }

    /**
     * 获取角色队伍分组，默认0表示无队伍分组
     * @return 
     */
    public int getTeam() {
        return team;
    }
    
    public void setTeam(int team) {
        this.team = team;
    }

    /**
     * 判断角色是否为生物
     * @return 
     */
    public boolean isLiving() {
        return living;
    }

    /**
     * 设置角色是否为生命类型
     * @param living 
     */
    public void setLiving(boolean living) {
        this.living = living;
    }

    /**
     * 获取当前角色的跟随目标的唯一ID,如果该值小于或等于0,则表示当前角色无跟随目标.
     * @return 目标角色唯一ID
     */
    public long getFollowTarget() {
        return followTarget;
    }

    /**
     * 设置角色的跟随目标(唯一ID),如果设置为小于或等于0的值则表示当前角色无跟随目标.
     * @param followTarget 
     */
    public void setFollowTarget(long followTarget) {
        this.followTarget = followTarget;
    }

    /**
     * 获取角色的出生位置
     * @return 
     */
    public Vector3f getBornPlace() {
        return bornPlace;
    }

    /**
     * 设置角色的出生位置
     * @param bornPlace 
     */
    public void setBornPlace(Vector3f bornPlace) {
        this.bornPlace = bornPlace;
    }

    /**
     * 获取角色的对话面板ID，如果没有设置则返回null.
     * @return 
     */
    public String getChat() {
        return chat;
    }

    public void setChat(String chat) {
        this.chat = chat;
    }

    /**
     * 获取技能的锁定状态，返回的整数中每一个二进制位表示一个被锁定的技能类
     * 型，当一个技能类型被锁定时，这个技能将不能被执行。
     * @return 
     */
    public long getSkillLockedState() {
        return skillLockedState;
    }

    /**
     * 设置技能的锁定状态.
     * @param skillLockedState 
     * @see #getSkillLockedState() 
     */
    public void setSkillLockedState(long skillLockedState) {
        this.skillLockedState = skillLockedState;
    }
    
    /**
     * @deprecated 暂不开放这个功能,作用不大
     * 判断角色是否打开了batch优化,默认false.不可动态设置。只能在xml中初始
     * 化配置.打开了batch功能之后角色的装备不可穿和脱.对于一些不需要动态更
     * 换装备和武器的角色可以打开该选项进行优化。如：固定装备的NPC。
     * @return 
     */
    public boolean isBatchEnabled() {
        return getAsBoolean("batch", false);
    }
}
