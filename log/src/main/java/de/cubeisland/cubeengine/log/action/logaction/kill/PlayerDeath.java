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
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.JsonNode;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.ENTITY;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.KILL;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;

/**
 * player-death
 * <p>Events: {@link KillActionType}</p>
 */
public class PlayerDeath extends SimpleLogActionType
{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(PLAYER, ENTITY, KILL);
    }

    @Override
    public String getName()
    {
        return "player-death";
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.hasCauserUser())
        {
            user.sendTranslated("&2%s &agot slaughtered by &2%s&a!",
                                logEntry.getUserFromData().getDisplayName(),
                                logEntry.getCauserUser().getDisplayName());
        }
        else if (logEntry.hasCauserEntity())
        {
            user.sendTranslated("&2%s &acould not escape &6%s&a!",
                                logEntry.getUserFromData().getDisplayName(),
                                logEntry.getCauserEntity());
        }
        else // something else
        {
            JsonNode json = logEntry.getAdditional();
            DamageCause dmgC = DamageCause.valueOf(json.get("dmgC").asText());
            user.sendTranslated("&2%s &adied! &f(&6%s&f)",
                                logEntry.getUserFromData().getDisplayName(),
                                dmgC.name());//TODO get pretty name for dmgC
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
        return this.lm.getConfig(world).PLAYER_DEATH_enable;
    }

}
