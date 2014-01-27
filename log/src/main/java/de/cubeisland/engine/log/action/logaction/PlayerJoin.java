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
package de.cubeisland.engine.log.action.logaction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.node.ArrayNode;

import static de.cubeisland.engine.log.action.ActionTypeCategory.PLAYER;

/**
 * player joins
 * <p>Events: {@link PlayerJoinEvent}</p>
 */
public class PlayerJoin extends SimpleLogActionType
{
    @Override
    protected Set<ActionTypeCategory> getCategories()
    {
        return new HashSet<>(Arrays.asList(PLAYER));
    }

    @Override
    public boolean canRollback()
    {
        return false;
    }

    @Override
    public String getName()
    {
        return "player-join";
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event)
    {

        if (this.isActive(event.getPlayer().getWorld()))
        {
            ArrayNode data = null;
            if (this.lm.getConfig(event.getPlayer().getWorld()).PLAYER_JOIN_ip)
            {
                data = this.om.createArrayNode();
                data.add(event.getPlayer().getAddress().getAddress().getHostAddress());
            }
            this.logSimple(event.getPlayer(), data == null ? null : data.toString());
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.hasAttached())
        {
            user.sendTranslated("%s&2%s&a joined the server &6x%d%s",
                                time,logEntry.getCauserUser().getDisplayName(),logEntry.getAttached().size() +1 , loc);
        }
        else
        {
            user.sendTranslated("%s&2%s&a joined the server%s",
                                time, logEntry.getCauserUser().getDisplayName(),loc);
        }
        //TODO ip if known
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        if (!super.isSimilar(logEntry, other)) return false;
        return logEntry.getCauser().equals(other.getCauser())
            && logEntry.getWorld() == other.getWorld();
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).PLAYER_JOIN_enable;
    }

    @Override
    public boolean canRedo()
    {
        return false;
    }
}
