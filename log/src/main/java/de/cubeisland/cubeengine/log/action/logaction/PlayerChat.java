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
import java.util.concurrent.TimeUnit;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.node.ArrayNode;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;

/**
 * chatting
 * <p>Events: {@link AsyncPlayerChatEvent}</p>
 */
public class PlayerChat extends SimpleLogActionType
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
        return "player-chat";
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        if (event.getMessage().trim().isEmpty()) return;
        if (this.isActive(event.getPlayer().getWorld()))
        {
            ArrayNode json = this.om.createArrayNode();
            json.add(event.getMessage());
            this.logSimple(event.getPlayer(),json.toString());
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        // TODO "actionType" spam
        if (logEntry.hasAttached())
        {
            if (logEntry.getAttached().size() >= 4)
            {
                user.sendTranslated("%s&2%s&a spammed &f\"&6%s&f\" &6x%d%s",
                                    time,logEntry.getCauserUser().getDisplayName(),
                                    logEntry.getAdditional().iterator().next().asText(),
                                    logEntry.getAttached().size()+1, loc);
            }
            else
            {
                user.sendTranslated("%s&2%s&a chatted &f\"&6%s&f\" &6x%d%s",
                                    time,logEntry.getCauserUser().getDisplayName(),
                                    logEntry.getAdditional().iterator().next().asText(),
                                    logEntry.getAttached().size()+1, loc);
            }
        }
        else
        {
            user.sendTranslated("%s&2%s&a chatted &f\"&6%s&f\"%s",
                                time,logEntry.getCauserUser().getDisplayName(),
                                logEntry.getAdditional().iterator().next().asText(), loc);
        }
    }


    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        if (!super.isSimilar(logEntry, other)) return false;
        return logEntry.causer == other.causer &&
        Math.abs(TimeUnit.MILLISECONDS.toSeconds(logEntry.timestamp.getTime() - other.timestamp.getTime())) < 30
            && logEntry.additional.iterator().next().asText().equals(other.additional.iterator().next().asText());
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).PLAYER_CHAT_enable;
    }
}
