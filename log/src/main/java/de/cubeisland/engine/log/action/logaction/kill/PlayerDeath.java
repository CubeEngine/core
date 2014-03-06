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
package de.cubeisland.engine.log.action.logaction.kill;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.fasterxml.jackson.databind.JsonNode;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.engine.log.storage.LogEntry;

import static de.cubeisland.engine.log.action.ActionTypeCategory.*;

/**
 * player-death
 * <p>Events: {@link KillActionType}</p>
 */
public class PlayerDeath extends SimpleLogActionType
{
    @Override
    protected Set<ActionTypeCategory> getCategories()
    {
        return new HashSet<>(Arrays.asList(PLAYER, ENTITY, KILL));
    }

    @Override
    public String getName()
    {
        return "player-death";
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        // TODO missing time & loc
        if (logEntry.hasCauserUser())
        {
            user.sendTranslated(MessageType.POSITIVE, "{}{user} got slaughtered by {user}{}", time, logEntry.getUserFromData().getDisplayName(), logEntry.getCauserUser().getDisplayName(), loc);
        }
        else if (logEntry.hasCauserEntity())
        {
            user.sendTranslated(MessageType.POSITIVE, "{}{user} could not escape {name#entity}{}", time, logEntry.getUserFromData().getDisplayName(), logEntry.getCauserEntity(), loc);
        }
        else // something else
        {
            JsonNode json = logEntry.getAdditional();
            DamageCause dmgC = DamageCause.valueOf(json.get("dmgC").asText());
            if (logEntry.getUserFromData() == null)
            {
                user.sendTranslated(MessageType.POSITIVE, "{}A Player died ({name#cause}){}", time, dmgC.name(), loc);//TODO get pretty name for dmgC
                return;
            }

            user.sendTranslated(MessageType.POSITIVE, "{}{user} died ({name#cause}){}", time, logEntry.getUserFromData().getName(), dmgC.name(), loc);//TODO get pretty name for dmgC
        }
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return KillActionType.isSimilarSubAction(logEntry,other);
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).death.PLAYER_DEATH_enable;
    }

    @Override
    public boolean canRollback()
    {
        return false;
    }

    @Override
    public boolean canRedo()
    {
        return false;
    }
}
