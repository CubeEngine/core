package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.logger.config.EndermanConfig;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import static de.cubeisland.cubeengine.log.logger.BlockLogger.BlockChangeCause.ENDERMAN;

public class EndermanLogger extends BlockLogger<EndermanConfig>
{
    public EndermanLogger(Log module) {
        super(module, EndermanConfig.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event)
    {
        World world = event.getBlock().getWorld();
        EndermanConfig config = this.configs.get(world);
        if (config.enabled)
        {
            if (event.getEntityType().equals(EntityType.ENDERMAN))
            {
                BlockState newState = event.getBlock().getState();
                newState.setType(event.getTo());
                if ((newState.getTypeId() == 0 && !config.logTake)
                    || event.getBlock().getState().getTypeId() == 0 && !config.logPlace)
                {
                    return;
                }
                this.logBlockChange(ENDERMAN, world, null, event.getBlock().getState(), newState);
            }
        }
    }


}
