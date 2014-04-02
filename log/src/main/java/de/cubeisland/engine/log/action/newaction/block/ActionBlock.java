/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.log.action.newaction.block;

import java.lang.reflect.Constructor;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.material.MaterialData;

import de.cubeisland.engine.log.action.newaction.BaseAction;
import de.cubeisland.engine.reflect.Section;

public abstract class ActionBlock<ListenerType> extends BaseAction<ListenerType>
{
    public BlockSection oldBlock;
    public BlockSection newBlock;

    public void setOldBlock(BlockState state)
    {
        this.oldBlock = new BlockSection(state);
    }

    public void setNewBlock(BlockState state)
    {
        this.newBlock = new BlockSection(state);
    }

    public void setOldBlock(Material mat)
    {
        this.oldBlock = new BlockSection(mat);
    }

    public void setNewBlock(Material mat)
    {
        this.newBlock = new BlockSection(mat);
    }

    public static class BlockSection implements Section
    {
        public Material material;
        public Byte data;

        public BlockSection()
        {
        }

        public BlockSection(BlockState state)
        {
            this(state.getType());
            this.data = state.getRawData();
        }

        public BlockSection(Material material)
        {
            this.material = material;
            this.data = 0;
        }

        /**
         * Returns true if this BlockSection is one of given materials
         */
        public boolean is(Material... materials)
        {
            for (Material mat : materials)
            {
                if (this.material == mat)
                {
                    return true;
                }
            }
            return false;
        }

        public String name()
        {
            return this.material.name();
        }

        public <T extends MaterialData> T as(Class<T> clazz)
        {
            try
            {
                Constructor<T> constructor = clazz.getConstructor(Material.class);
                T instance = constructor.newInstance(material);
                instance.setData(this.data);
                return instance;
            }
            catch (ReflectiveOperationException e)
            {
                return null;
            }
        }
    }
}
