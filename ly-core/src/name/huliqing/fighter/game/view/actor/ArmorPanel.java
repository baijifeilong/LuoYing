/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.fighter.game.view.actor;

import java.util.ArrayList;
import java.util.List;
import name.huliqing.fighter.Factory;
import name.huliqing.fighter.constants.ResConstants;
import name.huliqing.fighter.data.AttributeApply;
import name.huliqing.fighter.manager.ResourceManager;
import name.huliqing.fighter.object.actor.Actor;
import name.huliqing.fighter.data.ProtoData;
import name.huliqing.fighter.data.SkinData;
import name.huliqing.fighter.game.network.UserCommandNetwork;
import name.huliqing.fighter.game.service.PlayService;
import name.huliqing.fighter.game.service.SkinService;
import name.huliqing.fighter.ui.ListView;
import name.huliqing.fighter.ui.Row;
import name.huliqing.fighter.ui.UI;

/**
 *
 * @author huliqing
 */
public class ArmorPanel extends ListView<SkinData> implements ActorPanel{
    private final UserCommandNetwork userCommandNetwork = Factory
            .get(UserCommandNetwork.class);
    private final PlayService playService = Factory.get(PlayService.class);
    private final SkinService skinService = Factory.get(SkinService.class);
    private Actor actor;
    private List<SkinData> datas = new ArrayList<SkinData>();
    
    public ArmorPanel(float width, float height) {
        super(width, height);
    }
    
    @Override
    protected Row createEmptyRow() {
        final ArmorRow row = new ArmorRow();
        row.setRowClickListener(new Listener() {
            @Override
            public void onClick(UI ui, boolean isPress) {
                if (!isPress) {
                    userCommandNetwork.useObject(actor, row.getData());
                    refreshPageData();
                }
            }
        });
        row.setShortcutListener(new Listener() {
            @Override
            public void onClick(UI ui, boolean isPress) {
                if (!isPress) {
                    playService.addShortcut(actor, row.getData());
                }
            }
        });
        return row;
    }

    @Override
    public void setPanelVisible(boolean visible) {
        this.setVisible(visible);
    }

    @Override
    public void setPanelUpdate(Actor actor) {
        this.actor = actor;
        getDatas();
        super.refreshPageData();
    }

    @Override
    public List<SkinData> getDatas() {
        if (actor != null) {
            datas.clear();
            
            return skinService.getArmorSkins(actor, datas);
        }
        return datas;
    }

    @Override
    public boolean removeItem(SkinData data) {
        throw new UnsupportedOperationException();
    }
    
    private class ArmorRow extends ItemRow<ProtoData> {

        public ArmorRow() {
            super();
        }
        
        @Override
        protected void display(ProtoData data) {
            SkinData sd = (SkinData) data;
            
            icon.setIcon(sd.getIcon());
            body.setNameText(ResourceManager.getObjectName(sd));
            body.setDesText(sd.getDes());
            
            num.setText(String.valueOf(sd.getTotal()));
            
            setBackgroundVisible(sd.isUsing());
        }

        @Override
        protected void clickEffect(boolean isPress) {
            super.clickEffect(isPress);
            setBackgroundVisible(((SkinData)data).isUsing());
        }
        
        @Override
        public void onRelease() {
            SkinData sd = ((SkinData)data);
            setBackgroundVisible(sd.isUsing());
        }
    }
    
}
