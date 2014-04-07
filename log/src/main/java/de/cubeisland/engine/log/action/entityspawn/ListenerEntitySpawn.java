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
package de.cubeisland.engine.log.action.entityspawn;

import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.action.LogListener;

import static org.bukkit.Material.MONSTER_EGG;
import static org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;

/**
 * A Listener for Entity Actions
 * <p>Events:
 * {@link CreatureSpawnEvent}
 * {@link PlayerInteractEvent}
 * <p>Actions:
 * {@link SpawnNatural}
 * {@link SpawnSpawner}
 * {@link SpawnOther}
 * {@link SpawnEgg}
 */
public class ListenerEntitySpawn extends LogListener
{
    public ListenerEntitySpawn(Log module)
    {
        super(module, SpawnNatural.class, SpawnSpawner.class, SpawnOther.class, SpawnEgg.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event)
    {
        LivingEntity entity = event.getEntity();
        World world = entity.getWorld();
        switch (event.getSpawnReason())
        {
        case NATURAL:
        case JOCKEY:
        case CHUNK_GEN:
        case VILLAGE_DEFENSE:
        case VILLAGE_INVASION:
            SpawnNatural naturalSpawn = this.newAction(SpawnNatural.class, world);
            if (naturalSpawn != null)
            {
                naturalSpawn.setLocation(entity.getLocation());
                naturalSpawn.setEntity(entity);
                this.logAction(naturalSpawn);
            }
            return;
        case SPAWNER:
            SpawnSpawner spawnerSpawn = this.newAction(SpawnSpawner.class, world);
            if (spawnerSpawn != null)
            {
                spawnerSpawn.setLocation(entity.getLocation());
                spawnerSpawn.setEntity(entity);
                this.logAction(spawnerSpawn);
            }
            return;
        case EGG:
        case BUILD_SNOWMAN:
        case BUILD_IRONGOLEM:
        case BUILD_WITHER:
        case BREEDING:
            SpawnOther otherSpawn = this.newAction(SpawnOther.class, world);
            if (otherSpawn != null)
            {
                otherSpawn.setLocation(entity.getLocation());
                otherSpawn.setEntity(entity);
                this.logAction(otherSpawn);
            }
            return;
        case SPAWNER_EGG:
            // TODO preplanned
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMonsterEggUse(PlayerInteractEvent event)
    {
        if (event.getAction() == RIGHT_CLICK_BLOCK && event.getPlayer().getItemInHand().getType() == MONSTER_EGG)
        {
            if (this.isActive(SpawnEgg.class, event.getPlayer().getWorld()))
            {
// TODO
            }
        }
    }
    /*
    if (itemInHand.getType() == MONSTER_EGG)
        {
            MonsterEggUse monsterEggUse = this.manager.getActionType(MonsterEggUse.class);
            if (monsterEggUse.isActive(state.getWorld()))
            {
                monsterEggUse.logSimple(event.getClickedBlock().getRelative(event.getBlockFace()).getLocation(),
                                        event.getPlayer(),new ItemData(itemInHand).serialize(this.om));
            }
        }
     */
}
