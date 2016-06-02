/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.fighter.save;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import java.io.IOException;
import java.util.ArrayList;
import name.huliqing.fighter.data.ActorData;
import name.huliqing.fighter.data.ConfigData;

/**
 * 关卡存档器
 * @author huliqing
 */
public class SaveStory implements Savable{
    
    private String saveName;
    
    private long saveTime;
    
    // 已经完成的故事关卡ID
    private int storyCount;
    
    // 玩家数据
    private ActorData player;
    
    private ArrayList<ShortcutSave> shortcuts = new ArrayList<ShortcutSave>();

    public String getSaveName() {
        return saveName;
    }

    public void setSaveName(String saveName) {
        this.saveName = saveName;
    }

    public long getSaveTime() {
        return saveTime;
    }

    public void setSaveTime(long saveTime) {
        this.saveTime = saveTime;
    }

    public int getStoryCount() {
        return storyCount;
    }

    public void setStoryCount(int storyCount) {
        this.storyCount = storyCount;
    }

    public ActorData getPlayer() {
        return player;
    }

    public void setPlayer(ActorData player) {
        this.player = player;
    }

    public ArrayList<ShortcutSave> getShortcuts() {
        return shortcuts;
    }

    public void setShortcuts(ArrayList<ShortcutSave> shortcuts) {
        this.shortcuts = shortcuts;
    }
    
    @Override
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(saveName, "saveName", null);
        oc.write(saveTime, "saveTime", 0);
        oc.write(storyCount, "storyCount", 0);
        oc.write(player, "player", null);
        oc.writeSavableArrayList(shortcuts, "shortcuts", null);
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        InputCapsule ic = im.getCapsule(this);
        saveName = ic.readString("saveName", "unknow");
        saveTime = ic.readLong("saveTime", 0);
        storyCount = ic.readInt("storyCount", 0);
        player = (ActorData) ic.readSavable("player", null);
        ArrayList<ShortcutSave> tempShortcuts = ic.readSavableArrayList("shortcuts", null);
        shortcuts.clear();
        if (tempShortcuts != null) {
            shortcuts.addAll(tempShortcuts);
        }
    }
    
}
