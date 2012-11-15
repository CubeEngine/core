package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.SubLogConfig;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFadeEvent;

import static de.cubeisland.cubeengine.log.logger.BlockLogger.BlockChangeCause.FADE;

public class BlockFadeLogger extends BlockLogger<BlockFadeLogger.BlockFadeConfig>
{
    public BlockFadeLogger()
    {
        this.config = new BlockFadeConfig();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event)
    {
        if ((event.getBlock().getState().getType().equals(Material.ICE) && this.config.logIceMelt)
            || event.getBlock().getState().getType().equals(Material.SNOW) && this.config.logSnowMelt)
        {
            this.logBlockChange(FADE, null, event.getBlock().getState(), event.getNewState());
        }
    }

    public static class BlockFadeConfig extends SubLogConfig
    {
        @Option(value = "log-snow-melt")
        public boolean logSnowMelt = false;
        @Option(value = "log-ice-melt")
        public boolean logIceMelt = false;

        @Override
        public String getName()
        {
            return "block-fade";
        }
    }
}