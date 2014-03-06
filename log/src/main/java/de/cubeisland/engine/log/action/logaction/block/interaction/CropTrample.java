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
package de.cubeisland.engine.log.action.logaction.block.interaction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.action.logaction.block.BlockActionType;
import de.cubeisland.engine.log.storage.LogEntry;

import static de.cubeisland.engine.log.action.ActionTypeCategory.BLOCK;
import static de.cubeisland.engine.log.action.ActionTypeCategory.PLAYER;


/**
 * Trampling Crops
 * <p>Events: {@link RightClickActionType}</p>
 */
public class CropTrample extends BlockActionType

{
    @Override
    protected Set<ActionTypeCategory> getCategories()
    {
        return new HashSet<>(Arrays.asList(BLOCK, PLAYER));
    }

    @Override
    public String getName()
    {
        return "crop-trample";
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.hasAttached())
        {
            if (logEntry.getOldBlock().material.equals(Material.SOIL))
            {
                logEntry = logEntry.getAttached().first(); // replacing SOIL log with the crop log as the destroyed SOIL is implied
            }
        }
        user.sendTranslated(MessageType.POSITIVE, "{}{user} trampeled down {name#block}{}", time, logEntry.getCauserUser().getDisplayName(), logEntry.getOldBlock(), loc);
    }

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).block.CROP_TRAMPLE_enable;
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        if (logEntry.getActionType() == other.getActionType()
            && logEntry.getWorld() == other.getWorld()
            && logEntry.getCauser().equals(other.getCauser())
            && logEntry.getAdditional() == other.getAdditional()
            && nearTimeFrame(logEntry, other))
        {
            Location loc1 = logEntry.getLocation();
            Location loc2 = other.getLocation();
            if (loc1.getX() == loc2.getX() && loc1.getZ() == loc2.getZ()
                && Math.abs(loc1.getBlockY() - loc2.getBlockY()) == 1)
            {
                return true;
            }
         }
        return false;

    }

    @Override
    protected boolean nearTimeFrame(LogEntry logEntry, LogEntry other)
    {
        return Math.abs(logEntry.getTimestamp().getTime() - other.getTimestamp().getTime()) < 50;
    }
}
