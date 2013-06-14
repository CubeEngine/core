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
package de.cubeisland.cubeengine.log.action.logaction.block.interaction;

import java.util.EnumSet;

import org.bukkit.World;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;


/**
 * Using doors
 * <p>Events: {@link RightClickActionType}</p>
 */
public class DoorUse extends BlockActionType
{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(BLOCK, PLAYER);
    }

    @Override
    public String getName()
    {
        return "door-use";
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (!((logEntry.getOldBlock().data & 0x4) == 0x4))
        {
            user.sendTranslated("%s&2%s &aopened the &6%s%s",
                                time,logEntry.getCauserUser().getDisplayName(),
                                logEntry.getOldBlock(),loc);
        }
        else
        {
            user.sendTranslated("%s&2%s &aclosed the &6%s%s",
                                time,logEntry.getCauserUser().getDisplayName(),
                                logEntry.getOldBlock(),loc);
        }
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).DOOR_USE_enable;
    }
}
