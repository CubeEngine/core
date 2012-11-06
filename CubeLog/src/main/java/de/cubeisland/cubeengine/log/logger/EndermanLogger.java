package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.log.SubLogConfig;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import static de.cubeisland.cubeengine.log.logger.BlockLogger.BlockChangeCause.ENDERMAN;

public class EndermanLogger extends BlockLogger<EndermanLogger.EndermanConfig>
{
    public EndermanLogger()
    {
        this.config = new EndermanConfig();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event)
    {
        if (event.getEntityType().equals(EntityType.ENDERMAN))
        {
            BlockState newState = event.getBlock().getState();
            newState.setType(event.getTo());
            this.logBlockChange(ENDERMAN, null, event.getBlock().getState(), newState);
        }
    }

    public static class EndermanConfig extends SubLogConfig
    {
        @Override
        public String getName()
        {
            return "enderman";
        }
    }
}
