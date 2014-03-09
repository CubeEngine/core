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

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.core.util.matcher.Match;
import de.cubeisland.engine.log.LogAttachment;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.engine.log.storage.EntityData;
import de.cubeisland.engine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.JsonNode;

import static de.cubeisland.engine.log.action.ActionTypeCategory.*;

/**
 * pet-death
 * <p>Events: {@link KillActionType}</p>
 */
public class PetDeath extends SimpleLogActionType
{
    @Override
    protected Set<ActionTypeCategory> getCategories()
    {
        return new HashSet<>(Arrays.asList(PLAYER, ENTITY, KILL));
    }

    @Override
    public String getName()
    {
        return "pet-death";
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        EntityData killed = logEntry.getEntityFromData();
        if (Match.entity().isTameable(killed.entityType))
        {
            JsonNode json = logEntry.getAdditional();
            if (json.get("owner") != null)
            {
                User owner = this.um.getExactUser(json.get("owner").asText());
                if (logEntry.hasCauserUser())
                {
                    user.sendTranslated(MessageType.POSITIVE, "{}The {name#killed} of {user} got slaughtered by {user}{}", time, killed, owner.getDisplayName(), logEntry.getCauserUser().getDisplayName(), loc);
                }
                else if (logEntry.hasCauserEntity())
                {
                    user.sendTranslated(MessageType.POSITIVE, "{}The {name#killed} of {user} could not escape {name#entity}{}", time, killed, owner.getDisplayName(), logEntry.getCauserEntity(), loc);
                }
                else // something else
                {
                    user.sendTranslated(MessageType.POSITIVE, "{}The {name#killed} of {user} died{}", time, killed, owner.getDisplayName(), loc);
                }
                return;
            }
        }
        user.sendTranslated(MessageType.POSITIVE, "{}{name#entity} died! (Pet without owner){}", time, logEntry.getEntityFromData(), loc);
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return KillActionType.isSimilarSubAction(logEntry,other);
    }

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).death.PET_DEATH_enable;
    }

    @Override
    public boolean rollback(LogAttachment attachment, LogEntry logEntry, boolean force, boolean preview)
    {
        return KillActionType.rollbackDeath(attachment, logEntry, force, preview);
    }

    @Override
    public boolean canRollback()
    {
        return true;
    }

    @Override
    public boolean canRedo()
    {
        return false;
    }
}
