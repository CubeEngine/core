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
package de.cubeisland.cubeengine.log.action.logaction;

import java.util.EnumSet;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerExpChangeEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.node.ArrayNode;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;

/**
 * experience gain
 * <p>Events: {@link PlayerExpChangeEvent}</p>
 */
public class XpPickup extends SimpleLogActionType
{
    @Override
    public boolean canRollback()
    {
        return false;
    }

    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(PLAYER);
    }

    @Override
    public String getName()
    {
        return "xp-pickup";
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExpPickup(PlayerExpChangeEvent event)
    {
        if (this.isActive(event.getPlayer().getWorld()))
        {
            ArrayNode json = this.om.createArrayNode();
            json.add(event.getAmount());
            this.logSimple(event.getPlayer(),json.toString());
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        //TODO attached count total amount
        int amount = logEntry.getAdditional().iterator().next().asInt();
        user.sendTranslated("%s&2%s&a earned &6%d experience%s",
                            time, logEntry.getCauserUser().getDisplayName(), amount,loc);
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        if (!super.isSimilar(logEntry, other)) return false;
        return logEntry.world == other.world
            && logEntry.causer == other.causer;
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).XP_PICKUP_enable;
    }
}
