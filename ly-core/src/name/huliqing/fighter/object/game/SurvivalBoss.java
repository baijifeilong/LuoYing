/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.fighter.object.game;

import com.jme3.util.TempVars;
import java.util.List;
import name.huliqing.fighter.Factory;
import name.huliqing.fighter.constants.IdConstants;
import name.huliqing.fighter.constants.ResConstants;
import name.huliqing.fighter.object.DataLoaderFactory;
import name.huliqing.fighter.data.TalentData;
import name.huliqing.fighter.enums.MessageType;
import name.huliqing.fighter.enums.SkillType;
import name.huliqing.fighter.game.network.ActorNetwork;
import name.huliqing.fighter.game.network.PlayNetwork;
import name.huliqing.fighter.game.service.ActorService;
import name.huliqing.fighter.game.service.LogicService;
import name.huliqing.fighter.game.service.PlayService;
import name.huliqing.fighter.game.service.SkillService;
import name.huliqing.fighter.game.service.TalentService;
import name.huliqing.fighter.game.service.ViewService;
import name.huliqing.fighter.loader.Loader;
import name.huliqing.fighter.logic.scene.ActorBuildLogic;
import name.huliqing.fighter.object.IntervalLogic;
import name.huliqing.fighter.object.actor.Actor;
import name.huliqing.fighter.object.logic.PositionLogic;
import name.huliqing.fighter.object.view.TextView;
import name.huliqing.fighter.object.view.View;

/**
 *
 * @author huliqing
 */
public class SurvivalBoss extends IntervalLogic {
    private final ActorService actorService = Factory.get(ActorService.class);
    private final TalentService talentService = Factory.get(TalentService.class);
    private final ViewService viewService = Factory.get(ViewService.class);
    private final LogicService logicService = Factory.get(LogicService.class);
    private final SkillService skillService = Factory.get(SkillService.class);
    private final PlayService playService = Factory.get(PlayService.class);
    private final PlayNetwork playNetwork = Factory.get(PlayNetwork.class);
    private final ActorNetwork actorNetwork = Factory.get(ActorNetwork.class);
    
    private SurvivalGame game;
    private SurvivalLevelLogic levelLogic;
    private ActorBuildLogic actorBuilder;
    
    private int raptorLevel;
    private boolean raptorAdded;
    
    private int sinbadLevel;
    private boolean sinbadAdded;
    
    private int trexLevel;
    private boolean trexAdded;
    
    private int bossLevel;
    private boolean bossAdded;
    
    private Actor boss;
    private boolean bossDead;
    
    public SurvivalBoss(SurvivalGame game, ActorBuildLogic actorBuilder, SurvivalLevelLogic levelLogic) {
        super(5);
        this.game = game;
        this.actorBuilder = actorBuilder;
        this.levelLogic = levelLogic;
        int bossLevelUp = levelLogic.getMaxLevel() / 4;
        raptorLevel = bossLevelUp;
        sinbadLevel = bossLevelUp * 2;
        trexLevel = bossLevelUp * 3;
        bossLevel = bossLevelUp * 4;
    }

    @Override
    protected void doLogic(float tpf) {
        
        if (bossAdded && !bossDead) {
            if (actorService.isDead(boss)) {
                killAllEnemy();
                killAllLogic();
                View successView = viewService.loadView(IdConstants.VIEW_TEXT_SUCCESS);
                successView.setUseTime(-1);
                playNetwork.addView(successView);
                playNetwork.addMessage(ResConstants.TASK_SUCCESS, MessageType.item);
                bossDead = true;
            }
        }
        
        if (levelLogic.getLevel() >= bossLevel) {
            if (!bossAdded) {
                boss = loadBoss();
                bossAdded = true;
            }
            return;
        }
        
        if (levelLogic.getLevel() >= trexLevel) {
            if (!trexAdded) {
                actorBuilder.addId(IdConstants.ACTOR_TREX);
                trexAdded = true;
            }
            return;
        }
        
        if (levelLogic.getLevel() >= sinbadLevel) {
            if (!sinbadAdded) {
                actorBuilder.addId(IdConstants.ACTOR_SINBAD);
                sinbadAdded = true;
            }
            return;
        }
         
        if (levelLogic.getLevel() >= raptorLevel) {
            if (!raptorAdded) {
                actorBuilder.addId(IdConstants.ACTOR_RAPTOR);
                raptorAdded = true;
            }
        }

    }
    
    // 查询出BOSS所有的小弟并杀死,结束游戏
    private void killAllEnemy() {
        List<Actor> enemies = actorService.findNearestFriendly(boss, Float.MAX_VALUE, null);
        if (!enemies.isEmpty()) {
            for (Actor a : enemies) {
                actorNetwork.kill(a);
            }
        }
    }
    
    private void killAllLogic() {
        playService.removeObject(levelLogic);
        playService.removeObject(actorBuilder);
        playService.removeObject(this);
    }
    
    private Actor loadBoss() {
        Actor locBoss = actorService.loadActor(IdConstants.ACTOR_FAIRY);
        locBoss.setLocation(actorBuilder.getRandomPosition());
        actorService.setLevel(locBoss, levelLogic.getMaxLevel() + 5);
        actorService.setGroup(locBoss, game.GROUP_ENEMY);
        skillService.playSkill(locBoss, skillService.getSkill(locBoss, SkillType.wait).getId(), false);
        
        // 添加逻辑
        addPositionLogic(locBoss);
                
        // 为BOSS添加特殊天赋
        TalentData attack = DataLoaderFactory.createTalentData(IdConstants.TALENT_ATTACK);
        TalentData defence = DataLoaderFactory.createTalentData(IdConstants.TALENT_DEFENCE);
        TalentData defenceMagic = DataLoaderFactory.createTalentData(IdConstants.TALENT_DEFENCE_MAGIC);
        TalentData lifeRestore = DataLoaderFactory.createTalentData(IdConstants.TALENT_LIFE_RESTORE);
        TalentData moveSpeed = DataLoaderFactory.createTalentData(IdConstants.TALENT_MOVE_SPEED);
        attack.setLevel(attack.getMaxLevel());
        defence.setLevel(defence.getMaxLevel());
        defenceMagic.setLevel(defenceMagic.getMaxLevel());
        moveSpeed.setLevel(moveSpeed.getMaxLevel());
        lifeRestore.setMaxLevel(15);
        lifeRestore.setLevel(15);
        talentService.addTalent(locBoss, attack);
        talentService.addTalent(locBoss, defence);
        talentService.addTalent(locBoss, defenceMagic);
        talentService.addTalent(locBoss, lifeRestore);
        talentService.addTalent(locBoss, moveSpeed);
        
        // 添加BOSS，并让所有小弟都跟随BOSS
        playNetwork.addActor(locBoss);
        List<Actor> xd = actorService.findNearestFriendly(locBoss, interval, null);
        if (!xd.isEmpty()) {
            for (Actor a : xd) {
                actorNetwork.setFollow(a, locBoss.getData().getUniqueId());
            }
        }
        
        TextView view = (TextView) viewService.loadView(IdConstants.VIEW_WARN);
        view.setText("Boss");
        playNetwork.addView(view);
        
        return locBoss;
    }
    
    private void addPositionLogic(Actor actor) {
        TempVars tv = TempVars.get();
        tv.vect1.set(game.treasurePos);
        tv.vect1.setY(playService.getTerrainHeight(tv.vect1.x, tv.vect1.z));
        PositionLogic runLogic = (PositionLogic) Loader.loadLogic(IdConstants.LOGIC_POSITION);
        runLogic.setInterval(3);
        runLogic.setPosition(tv.vect1);
        runLogic.setNearestDistance(game.nearestDistance);
        logicService.addLogic(actor, runLogic);
        tv.release();
    }
}
