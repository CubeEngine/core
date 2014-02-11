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

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.logaction.ActionTypeContainer;
import de.cubeisland.engine.log.storage.LogEntry;

/**
 * Container-ActionType for spawning
 * <p>Events: {@link CreatureSpawnEvent}</p>
 * <p>External Actions:
 * {@link NaturalSpawn},
 * {@link SpawnerSpawn},
 * {@link OtherSpawn},
 */
public class EntitySpawnActionType extends ActionTypeContainer
{
    public EntitySpawnActionType()
    {
        super("ENTITY_SPAWN");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event)
    {
        World world = event.getEntity().getWorld();
        switch (event.getSpawnReason())
        {
        case NATURAL:
        case JOCKEY:
        case CHUNK_GEN:
        case VILLAGE_DEFENSE:
        case VILLAGE_INVASION:
            NaturalSpawn naturalSpawn = this.manager.getActionType(NaturalSpawn.class);
            if (naturalSpawn.isActive(world))
            {
                naturalSpawn.logSimple(event.getEntity(),null);
            }
            return;
        case SPAWNER:
            SpawnerSpawn spawnerSpawn = this.manager.getActionType(SpawnerSpawn.class);
            if (spawnerSpawn.isActive(world))
            {
                spawnerSpawn.logSimple(event.getEntity(),null);
            }
            return;
        case EGG:
        case BUILD_SNOWMAN:
        case BUILD_IRONGOLEM:
        case BUILD_WITHER:
        case BREEDING:
            OtherSpawn otherSpawn = this.manager.getActionType(OtherSpawn.class);
            if (otherSpawn.isActive(world))
            {
                otherSpawn.logSimple(event.getEntity(),null);
            }
        //case SPAWNER_EGG: //is already done
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        throw new UnsupportedOperationException();
    }
}
