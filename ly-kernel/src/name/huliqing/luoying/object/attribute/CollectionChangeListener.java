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
package name.huliqing.luoying.object.attribute;

/**
 * Collection监听接口,用于监听列表的变化
 * @author huliqing
 */
public interface CollectionChangeListener<E> {
    
    /**
     * 当列表中添加了一个新元素时该方法被调用。
     * @param added 新添加的元素
     */
    void onAdded(E added);

    /**
     * 当列表移除了一个元素时该方法被调用。
     * @param removed 已被移除的元素
     */
    void onRemoved(E removed);
    
}
