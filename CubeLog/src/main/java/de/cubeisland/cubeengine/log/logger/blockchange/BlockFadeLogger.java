package de.cubeisland.cubeengine.log.logger.blockchange;

import de.cubeisland.cubeengine.log.logger.SubLogConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFadeEvent;

import static de.cubeisland.cubeengine.log.logger.blockchange.BlockLogger.BlockChangeCause.FADE;

public class BlockFadeLogger extends BlockLogger<BlockFadeLogger.BlockFadeConfig>
{
    public BlockFadeLogger()
    {
        this.config = new BlockFadeConfig();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event)
    {
        this.logBlockChange(FADE, null, event.getBlock().getState(), event.getNewState());
    }

    public static class BlockFadeConfig extends SubLogConfig
    {
        @Override
        public String getName()
        {
            return "block-fade";
        }
    }
}
