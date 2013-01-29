package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.logger.config.BlockGrowConfig;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.StructureGrowEvent;

import static de.cubeisland.cubeengine.log.logger.BlockLogger.BlockChangeCause.GROW;

public class BlockGrowLogger extends BlockLogger<BlockGrowConfig>
{
    public BlockGrowLogger(Log module) {
        super(module, BlockGrowConfig.class);
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event)
    {
        World world = event.getWorld();
        BlockGrowConfig config = this.configs.get(world);
        if (config.enabled)
        {
            Player player = null;
            if (event.isFromBonemeal())
            {
                if (!config.logPlayer)
                {
                    return;
                }
                player = event.getPlayer();
            }
            else if (!config.logNatural)
            {
                return;
            }
            for (BlockState newBlock : event.getBlocks())
            {
                BlockState oldBlock =  event.getWorld().getBlockAt(newBlock.getLocation()).getState();
                if (!(oldBlock.getTypeId() == newBlock.getTypeId()
                &&  oldBlock.getRawData() == newBlock.getRawData()))
                    this.logBlockChange(GROW, world, player,oldBlock , newBlock);
            }
        }
    }
}
