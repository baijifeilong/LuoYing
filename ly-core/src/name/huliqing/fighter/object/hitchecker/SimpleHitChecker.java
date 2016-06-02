/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.fighter.object.hitchecker;

import name.huliqing.fighter.Factory;
import name.huliqing.fighter.data.HitCheckerData;
import name.huliqing.fighter.game.service.ActorService;
import name.huliqing.fighter.object.actor.Actor;

/**
 *
 * @author huliqing
 */
public class SimpleHitChecker extends AbstractHitChecker {
    private final static ActorService actorService = Factory.get(ActorService.class);
    
    private enum Group {
        /** ignore不管分组 */
        i,
        /** same必须是相同分组 */
        s,
        /** difference必须是不同分组 */
        d;
        
        static Group identify(String name) {
            Group[] values = values();
            for (Group g : values) {
                if (g.name().equals(name)) {
                    return g;
                }
            }
            throw new UnsupportedOperationException("group name=" + name);
        }
    }
    
    private enum Checker {
        /** ignore不检查 */
        i,
        /** yes */
        y,
        /** no */
        n;
        
        static Checker identify(String name) {
            for (Checker l : values()) {
                if (l.name().equals(name)) {
                    return l;
                }
            }
            throw new UnsupportedOperationException("Checker name=" + name);
        }
    }
    
    // 分组类型。
    private Group group;
    // 生命体状态
    private Checker living;
    // 生命值状态
    private Checker life;
    
    public SimpleHitChecker(HitCheckerData data) {
        super(data);
        group = Group.identify(data.getAttribute("group"));
        living = Checker.identify(data.getAttribute("living"));
        life = Checker.identify(data.getAttribute("life"));
    }

    @Override
    public boolean canHit(Actor source, Actor target) {
        // 注意：因为source有可能为null,举例来说，比如：magic有可能是没有施放者的
        // 也就没有source
        if (source != null) {
            int sourceGroup = actorService.getGroup(source);
            // 判断是否符合分组
            if (group == Group.s && sourceGroup != actorService.getGroup(target)) {
                return false;
            } else if (group == Group.d && sourceGroup == actorService.getGroup(target)) {
                return false;
            } 
        }
        
        // 判断是否符合生命体状态
        if (living == Checker.y && !target.getData().isLiving()) {
            return false;
        } else if (living == Checker.n && target.getData().isLiving()) {
            return false;
        }
        
        // 判断生命值状态是否匹配
        if (life == Checker.y && actorService.isDead(target)) {
            return false;
        } else if (life == Checker.n && !actorService.isDead(target)) {
            return false;
        }
        
        return true;
    }
    
}
