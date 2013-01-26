package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.logger.config.BlockFormConfig;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFormEvent;

import static de.cubeisland.cubeengine.log.logger.BlockLogger.BlockChangeCause.FORM;

public class BlockFormLogger extends    BlockLogger<BlockFormConfig>
{
    public BlockFormLogger(Log module) {
        super(module, BlockFormConfig.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event)
    {
        World world = event.getBlock().getWorld();
        BlockFormConfig config = this.configs.get(world);
        if (config.enabled)
        {
            if ((event.getNewState().getType().equals(Material.ICE) && config.logIceForm)
                || event.getNewState().getType().equals(Material.SNOW) && config.logSnowForm)
            {
                this.logBlockChange(FORM,world,null, event.getBlock().getState(), event.getNewState());
            }
        }
    }


}
