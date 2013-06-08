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
import org.bukkit.event.player.PlayerQuitEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;

/**
 * player quits
 * <p>Events: {@link PlayerQuitEvent}</p>
 */
public class PlayerQuit extends SimpleLogActionType
{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(PLAYER);
    }

    @Override
    public boolean canRollback()
    {
        return false;
    }

    @Override
    public String getName()
    {
        return "player-quit";
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        if (this.isActive(event.getPlayer().getWorld()))
        {
            this.logSimple(event.getPlayer(),null);
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.hasAttached())
        {
            user.sendTranslated("%s&2%s&a left the server &6x%d%s",
                                time,logEntry.getCauserUser().getDisplayName(),logEntry.getAttached().size() +1 , loc);
        }
        else
        {
            user.sendTranslated("%s&2%s&a left the server%s",
                                time,logEntry.getCauserUser().getDisplayName(),loc);
        }
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
        return this.lm.getConfig(world).PLAYER_QUIT_enable;
    }
}
