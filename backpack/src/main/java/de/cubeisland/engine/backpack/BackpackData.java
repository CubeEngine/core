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
package de.cubeisland.engine.backpack;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.configuration.Configuration;
import de.cubeisland.engine.core.config.codec.NBTCodec;

public class BackpackData extends Configuration<NBTCodec>
{
    public boolean allowItemsIn = true;
    public int pages = 1;
    public int size = 6;
    public Map<Integer, ItemStack> contents = new HashMap<>();

    @Override
    public void onSave()
    {
        for (Integer next : contents.keySet())
        {
            if (contents.get(next) == null)
            {
                contents.remove(next);
            }
        }
    }
}
