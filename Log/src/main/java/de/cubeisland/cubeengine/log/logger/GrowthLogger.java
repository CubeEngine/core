package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.logger.config.GrowthConfig;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.world.StructureGrowEvent;

import static de.cubeisland.cubeengine.log.logger.BlockLogger.BlockChangeCause.FIRE;
import static de.cubeisland.cubeengine.log.logger.BlockLogger.BlockChangeCause.GROW;

public class GrowthLogger extends BlockLogger<GrowthConfig>
{
    public GrowthLogger(Log module)
    {
        super(module, GrowthConfig.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event)
    {
        World world = event.getWorld();
        GrowthConfig config = this.configs.get(world);
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
                BlockState oldBlock = event.getWorld().getBlockAt(newBlock.getLocation()).getState();
                if (!(oldBlock.getTypeId() == newBlock.getTypeId() && oldBlock.getRawData() == newBlock.getRawData()))
                    this.logBlockChange(GROW, world, player, oldBlock, newBlock);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpread(BlockSpreadEvent event)
    {
        World world = event.getBlock().getWorld();
        GrowthConfig config = this.configs.get(world);
        if (config.enabled)
        {
            if (event.getNewState().getType().equals(Material.FIRE))
            {
                if (config.logFireSpread)
                {
                    this.logBlockChange(FIRE,world,null,event.getBlock().getState(),event.getNewState());
                }
            }
            else
            {
                if (config.logOtherSpread)
                {
                    this.logBlockChange(GROW,world,null,event.getBlock().getState(),event.getNewState());
                }
            }
        }
    }
}
