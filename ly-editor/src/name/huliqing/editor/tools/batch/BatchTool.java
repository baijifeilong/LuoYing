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
package name.huliqing.editor.tools.batch;

import name.huliqing.editor.edit.SimpleJmeEdit;
import name.huliqing.editor.events.Event;
import name.huliqing.editor.toolbar.EntityBatchToolbar;
import name.huliqing.editor.tools.AbstractTool;

/**
 * BatchTool,用于将场景中的一些相同类型的实体进行批量Batch到BatchEntity节点中。
 * 该工具会动态的为场景创建BatchEntity.将场景中的指定的实体自动分配到不同的BatchEntity中。
 * 可指定要Batch的区域的大小, 指定要自动创建的BatchEntity的数量
 * @author huliqing
 */
public class BatchTool extends AbstractTool<SimpleJmeEdit, EntityBatchToolbar> {


    public BatchTool(String name, String tips, String icon) {
        super(name, tips, icon);
    }

    @Override
    protected void onToolEvent(Event e) {
        // ignore
    }

    @Override
    public void initialize(SimpleJmeEdit edit, EntityBatchToolbar toolbar) {
        super.initialize(edit, toolbar);
        
        
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }
    
    
}
