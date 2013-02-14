package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.logger.config.BlockDecayConfig;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.LeavesDecayEvent;

import static de.cubeisland.cubeengine.log.logger.BlockLogger.BlockChangeCause.DECAY;

public class BlockDecayLogger extends BlockLogger<BlockDecayConfig>
{
    public BlockDecayLogger(Log module)
    {
        super(module, BlockDecayConfig.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event)
    {
        World world = event.getBlock().getWorld();
        BlockDecayConfig config = this.configs.get(world);
        if (config.enabled)
        {
            this.logBlockChange(DECAY, world, null, event.getBlock().getState(), null);
        }
    }

}
