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
package name.huliqing.luoying.loader;

import name.huliqing.luoying.data.ModuleData;
import name.huliqing.luoying.data.PhysicsShapeData;
import name.huliqing.luoying.object.Loader;
import name.huliqing.luoying.xml.DataLoader;
import name.huliqing.luoying.xml.Proto;

/**
 *
 * @author huliqing
 */
public class PhysicsModuleDataLoader implements DataLoader<ModuleData>{

    @Override
    public void load(Proto proto, ModuleData data) {
        String physicsShape = proto.getAsString("physicsShape");
        if (physicsShape != null) {
            PhysicsShapeData psd = Loader.loadData(physicsShape);
            data.setAttribute("physicsShape", psd);
        }
    }
    
}
