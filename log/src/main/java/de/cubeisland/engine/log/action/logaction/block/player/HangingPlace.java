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
package de.cubeisland.engine.log.action.logaction.block.player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.hanging.HangingPlaceEvent;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.action.logaction.block.BlockActionType;
import de.cubeisland.engine.log.storage.LogEntry;

import static de.cubeisland.engine.log.action.ActionTypeCategory.BLOCK;
import static de.cubeisland.engine.log.action.ActionTypeCategory.PLAYER;
import static org.bukkit.Material.*;

/**
 * Placing Item-Frames or Painting
 * <p>Events: {@link HangingPlaceEvent}</p>
 */
public class HangingPlace extends BlockActionType
{
    @Override
    protected Set<ActionTypeCategory> getCategories()
    {
        return new HashSet<>(Arrays.asList(BLOCK, PLAYER));
    }

    @Override
    public String getName()
    {
        return "hanging-place";
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event)
    {
        if (this.isActive(event.getEntity().getWorld()))
        {
            if (event.getEntity() instanceof ItemFrame)
            {
                this.logBlockChange(event.getEntity().getLocation(),event.getPlayer(),AIR,ITEM_FRAME,null);
            }
            else if (event.getEntity() instanceof Painting)
            {
                BlockData blockData = BlockData.of(PAINTING,(byte)((Painting)event.getEntity()).getArt().getId());
                this.logBlockChange(event.getEntity().getLocation(),event.getPlayer(),AIR,blockData,null);
            }
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated(MessageType.POSITIVE, "{}{name#block} got hung up by {user}{}", time, logEntry.getNewBlock(), logEntry.getCauserUser().getDisplayName(), loc);
    }

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).HANGING_PLACE_enable;
    }
}
