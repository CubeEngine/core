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
package de.cubeisland.engine.log.action.logaction.interact;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.DyeColor;
import org.bukkit.World;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.engine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.JsonNode;

import static de.cubeisland.engine.log.action.ActionTypeCategory.ENTITY;
import static de.cubeisland.engine.log.action.ActionTypeCategory.PLAYER;

/**
 * Dyeing sheeps or wolfcollars
 * <p>Events: {@link InteractEntityActionType}</p>
 */
public class EntityDye extends SimpleLogActionType
{
    @Override
    protected Set<ActionTypeCategory> getCategories()
    {
        return new HashSet<>(Arrays.asList(PLAYER, ENTITY));
    }

    @Override
    public boolean canRollback()
    {
        return false;
    }

    @Override
    public String getName()
    {
        return "entity-dye";
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        JsonNode json = logEntry.getAdditional();
        DyeColor color = DyeColor.valueOf(json.get("nColor").asText());
        user.sendTranslated("%s&2%s&a dyed a &6%s&a in &6%s%s",
                            time,logEntry.getCauserUser().getDisplayName(),
                            logEntry.getEntityFromData(),
                            color.name(), loc); //TODO get Pretty name for color
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        if (!super.isSimilar(logEntry, other)) return false;
        return logEntry.getCauser().equals(other.getCauser())
            && logEntry.getWorld() == other.getWorld()
            && logEntry.getData() == other.getData(); //same entity
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).ENTITY_DYE_enable;
    }

    @Override
    public boolean canRedo()
    {
        return false; // TODO possible
    }
}
