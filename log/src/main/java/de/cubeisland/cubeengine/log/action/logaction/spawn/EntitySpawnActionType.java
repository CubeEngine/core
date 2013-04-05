package de.cubeisland.cubeengine.log.action.logaction.spawn;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;

public class EntitySpawnActionType extends SimpleLogActionType
{
    public EntitySpawnActionType(Log module)
    {
        super(module, -1, "ENTITY_SPAWN");
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
            return;
        //case SPAWNER_EGG: //is already done
        }
    }
}
