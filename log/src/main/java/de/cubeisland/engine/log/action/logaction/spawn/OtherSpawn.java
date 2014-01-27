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
package de.cubeisland.engine.log.action.logaction.spawn;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.World;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.engine.log.storage.LogEntry;

import static de.cubeisland.engine.log.action.ActionTypeCategory.ENTITY;
import static de.cubeisland.engine.log.action.ActionTypeCategory.SPAWN;

/**
 * other spawning (by player)
 * <p>Events: {@link EntitySpawnActionType}</p>
 */
public class OtherSpawn extends SimpleLogActionType
{
    @Override
    protected Set<ActionTypeCategory> getCategories()
    {
        return new HashSet<>(Arrays.asList(SPAWN, ENTITY));
    }

    @Override
    public boolean canRollback()
    {
        return false;
    }
    @Override
    public String getName()
    {
        return "other-spawn";
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {//TODO get player in data once possible
        user.sendTranslated("%s&6%s &aspawned%s",
                           time, logEntry.getCauserEntity(),loc);
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        if (!super.isSimilar(logEntry, other)) return false;
        return logEntry.getCauser().equals(other.getCauser())
            && logEntry.getWorld() == other.getWorld()
            && logEntry.getVector().equals(other.getVector());
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).OTHER_SPAWN_enable;
    }

    @Override
    public boolean canRedo()
    {
        return false; // TODO possible
    }
}
