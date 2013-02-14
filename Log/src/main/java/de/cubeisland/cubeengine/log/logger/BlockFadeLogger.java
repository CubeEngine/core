package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.logger.config.BlockFadeConfig;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFadeEvent;

import static de.cubeisland.cubeengine.log.logger.BlockLogger.BlockChangeCause.FADE;

public class BlockFadeLogger extends BlockLogger<BlockFadeConfig>
{
    public BlockFadeLogger(Log module)
    {
        super(module, BlockFadeConfig.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event)
    {
        World world = event.getBlock().getWorld();
        BlockFadeConfig config = this.configs.get(world);
        if (config.enabled)
        {
            if ((event.getBlock().getState().getType().equals(Material.ICE) && config.logIceMelt)
                || event.getBlock().getState().getType().equals(Material.SNOW) && config.logSnowMelt)
            {
                this.logBlockChange(FADE, world, null, event.getBlock().getState(), event.getNewState());
            }
        }
    }

}
