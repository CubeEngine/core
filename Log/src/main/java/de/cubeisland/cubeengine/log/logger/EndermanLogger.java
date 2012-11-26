package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.SubLogConfig;
import static de.cubeisland.cubeengine.log.logger.BlockLogger.BlockChangeCause.ENDERMAN;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;

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
            if ((newState.getTypeId() == 0 && !this.config.logTake)
                || event.getBlock().getState().getTypeId() == 0 && !this.config.logPlace)
            {
                return;
            }
            this.logBlockChange(ENDERMAN, null, event.getBlock().getState(), newState);
        }
    }

    public static class EndermanConfig extends SubLogConfig
    {
        @Option("log-enderman-place")
        public boolean logPlace = false;
        @Option("log-enderman-take")
        public boolean logTake = false;

        @Override
        public String getName()
        {
            return "enderman";
        }
    }
}
