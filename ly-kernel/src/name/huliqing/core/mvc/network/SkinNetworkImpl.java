/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.core.mvc.network;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import name.huliqing.core.Factory;
import name.huliqing.core.data.SkinData;
import name.huliqing.core.mvc.service.SkinService;
import name.huliqing.core.network.Network;
import name.huliqing.core.mess.MessSkinWeaponTakeOn;
import name.huliqing.core.object.actor.Actor;
import name.huliqing.core.object.actor.SkinListener;

/**
 *
 * @author huliqing
 */
public class SkinNetworkImpl implements SkinNetwork {
    private static final Logger LOG = Logger.getLogger(SkinNetworkImpl.class.getName());
    private final static Network network = Network.getInstance();
    private SkinService skinService;
    
    @Override
    public void inject() {
        skinService = Factory.get(SkinService.class);
    }
    
    @Override
    public void attachSkin(Actor actor, SkinData skinData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void detachSkin(Actor actor, SkinData skinData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isCanTakeOnWeapon(Actor actor) {
        return skinService.isCanTakeOnWeapon(actor);
    }

    @Override
    public boolean isCanTakeOffWeapon(Actor actor) {
        return skinService.isCanTakeOffWeapon(actor);
    }

    @Override
    public boolean takeOnWeapon(Actor actor, boolean force) {
        if (!network.isClient()) {
            boolean can = force || skinService.isCanTakeOnWeapon(actor);
            if (can) {
                if (network.hasConnections()) {
                    MessSkinWeaponTakeOn mess = new MessSkinWeaponTakeOn();
                    mess.setActorId(actor.getData().getUniqueId());
                    mess.setTakeOn(true);
                    network.broadcast(mess);
                }
                
                skinService.takeOnWeapon(actor, true);
            }
            return can;
        }
        return false;
    }

    @Override
    public boolean takeOffWeapon(Actor actor, boolean force) {
        if (!network.isClient()) {
            boolean can = force || skinService.isCanTakeOffWeapon(actor);
            if (can) {
                if (network.hasConnections()) {
                    MessSkinWeaponTakeOn mess = new MessSkinWeaponTakeOn();
                    mess.setActorId(actor.getData().getUniqueId());
                    mess.setTakeOn(false);
                    network.broadcast(mess);
                }
                
                skinService.takeOffWeapon(actor, true);
            }
            return can;
        }
        return false;
    }

    @Override
    public List<SkinData> getArmorSkins(Actor actor, List<SkinData> store) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<SkinData> getWeaponSkins(Actor actor, List<SkinData> store) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<SkinData> getCurrentWeaponSkin(Actor actor) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getWeaponState(Actor actor) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isWeaponTakeOn(Actor actor) {
        return skinService.isWeaponTakeOn(actor);
    }

    @Override
    public boolean isWeapon(SkinData skinData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addSkinListener(Actor actor, SkinListener skinListener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean removeSkinListener(Actor actor, SkinListener skinListener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
}