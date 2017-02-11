/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.editor.tools.terrain;

import com.jme3.gde.terraineditor.tools.AbstractTerrainToolAction;
import com.jme3.gde.terraineditor.tools.RaiseTerrainToolAction;
import com.jme3.math.Vector3f;
import name.huliqing.editor.edit.controls.entity.EntityControlTile;

/**
 * 地形上升工具
 * @author huliqing
 */
public class RaiseTool extends AdjustTerrainTool {
    
    public RaiseTool(String name, String tips, String icon) {
        super(name, tips, icon);
    }

    @Override
    protected AbstractTerrainToolAction createAction(float radius, float weight, Vector3f markerWorldLoc, EntityControlTile terrain) {
        RaiseTerrainToolAction action = new RaiseTerrainToolAction(terrain, markerWorldLoc, radius, weight);
        setModified(true);
        return action;
    }
  
}