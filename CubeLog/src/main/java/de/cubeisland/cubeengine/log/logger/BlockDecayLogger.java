package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.log.SubLogConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.LeavesDecayEvent;

import static de.cubeisland.cubeengine.log.logger.BlockLogger.BlockChangeCause.DECAY;

public class BlockDecayLogger extends BlockLogger<BlockDecayLogger.BlockDecayConfig>
{
    public BlockDecayLogger()
    {
        this.config = new BlockDecayConfig();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event)
    {
        this.logBlockChange(DECAY, null, event.getBlock().getState(), null);
    }

    public static class BlockDecayConfig extends SubLogConfig
    {
        @Override
        public String getName()
        {
            return "block-decay";
        }
    }
}