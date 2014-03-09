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
package de.cubeisland.engine.log.action.logaction.worldedit;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.action.logaction.block.BlockActionType;
import de.cubeisland.engine.log.storage.LogEntry;

import static de.cubeisland.engine.log.action.ActionTypeCategory.BLOCK;
import static de.cubeisland.engine.log.action.ActionTypeCategory.PLAYER;

public class WorldEditActionType extends BlockActionType
{
    @Override
    protected Set<ActionTypeCategory> getCategories()
    {
        return new HashSet<>(Arrays.asList(PLAYER, BLOCK));
    }

    @Override
    public String getName()
    {
        return "worldedit";
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.hasAttached())
        {
            int attached = logEntry.getAttached().size() +1;
            if (logEntry.getNewBlock().material.equals(Material.AIR))
            {
                user.sendTranslated(MessageType.POSITIVE, "{}{user} used worldedit to remove {name#block} x{amount}{}", time, logEntry.getCauserUser().getDisplayName(), logEntry.getOldBlock(), attached, loc);
            }
            else if (logEntry.getOldBlock().material.equals(Material.AIR))
            {
                user.sendTranslated(MessageType.POSITIVE, "{}{user} used worldedit to place {name#block} x{amount}{}", time, logEntry.getCauserUser().getDisplayName(), logEntry.getNewBlock(), attached, loc);
            }
            else
            {
                user.sendTranslated(MessageType.POSITIVE, "{}{user} used worldedit to replace {name#block} with {name#block} x{amount}{}", time, logEntry.getCauserUser().getDisplayName(), logEntry.getOldBlock(), logEntry.getNewBlock(), attached, loc);
            }
        }
        else
        {
            if (logEntry.getNewBlock().material.equals(Material.AIR))
            {
                user.sendTranslated(MessageType.POSITIVE, "{}{user} used worldedit to remove {name#block}{}", time, logEntry.getCauserUser().getDisplayName(), logEntry.getOldBlock(), loc);
            }
            else if (logEntry.getOldBlock().material.equals(Material.AIR))
            {
                user.sendTranslated(MessageType.POSITIVE, "{}{user} used worldedit to place {name#block}{}", time, logEntry.getCauserUser().getDisplayName(), logEntry.getNewBlock(), loc);
            }
            else
            {
                user.sendTranslated(MessageType.POSITIVE, "{}{user} used worldedit to replace {name#block} with {name#block}{}", time, logEntry.getCauserUser().getDisplayName(), logEntry.getOldBlock(), logEntry.getNewBlock(), loc);
            }
        }
    }

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).block.WORLDEDIT_enable;
    }
}
