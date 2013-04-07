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
package de.cubeisland.cubeengine.log.action.logaction.kill;

import java.util.EnumSet;

import org.bukkit.World;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.EntityData;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.JsonNode;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.*;

/**
 * pet-death
 * <p>Events: {@link KillActionType}</p>
 */
public class PetDeath extends SimpleLogActionType
{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(PLAYER, ENTITY, KILL);
    }


    @Override
    public String getName()
    {
        return "pet-death";
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        EntityData killed =  logEntry.getEntityFromData();
        if (Match.entity().isTameable(killed.entityType))
        {
            JsonNode json = logEntry.getAdditional();
            if (json.get("owner") != null)
            {
                User owner = this.um.getExactUser(json.get("owner").asText());
                if (logEntry.hasCauserUser())
                {
                    user.sendTranslated("%s&aThe &6%s&a of &2%s &agot slaughtered by &2%s%s&a!",
                                       time ,killed,
                                        owner.getDisplayName(),
                                        logEntry.getCauserUser().getDisplayName(),loc);
                }
                else if (logEntry.hasCauserEntity())
                {
                    user.sendTranslated("%s&aThe &6%s&a of &2%s &acould not escape &6%s%s&a!",
                                        time,killed,
                                        owner.getDisplayName(),
                                        logEntry.getCauserEntity(),loc);
                }
                else // something else
                {
                    user.sendTranslated("%s&aThe &6%s&a of &2%s &adied%s&a!",
                                        time,killed,
                                        owner.getDisplayName(),loc);
                }
                return;
            }
        }
        user.sendTranslated("&6%s &adied! &4(Pet without owner)", logEntry.getEntityFromData());
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return KillActionType.isSimilarSubAction(logEntry,other);
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).PET_DEATH_enable;
    }

}
