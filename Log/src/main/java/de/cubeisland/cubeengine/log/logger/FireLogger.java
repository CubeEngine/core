package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.core.util.BlockUtil;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.logger.config.FireConfig;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;

import static de.cubeisland.cubeengine.log.logger.BlockLogger.BlockChangeCause.FIRE;
import static de.cubeisland.cubeengine.log.logger.BlockLogger.BlockChangeCause.LAVA;
import static de.cubeisland.cubeengine.log.logger.BlockLogger.BlockChangeCause.PLAYER;

public class FireLogger extends BlockLogger<FireConfig>
{
    public FireLogger(Log module)
    {
        super(module, FireConfig.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event)
    {
        World world = event.getBlock().getWorld();
        FireConfig config = this.configs.get(world);
        if (config.enabled && config.blockBurn)
        {
            for (Block block : BlockUtil.getAttachedBlocks(event.getBlock())) // attached blockss
            {
                this.logBlockChange(FIRE, world, null, block.getState(), null);
            }
            switch (event.getBlock().getRelative(BlockFace.UP).getType())
            // blocks on top that get destroyed
            {
                case WOODEN_DOOR:
                case IRON_DOOR:
                case SNOW:
                case STONE_PLATE:
                case WOOD_PLATE:
                case REDSTONE_WIRE:
                case DIODE_BLOCK_OFF:
                case DIODE_BLOCK_ON:
                    this.logBlockChange(FIRE, world, null, event.getBlock().getRelative(BlockFace.UP).getState(), null);
            }
            this.logBlockChange(FIRE, world, null, event.getBlock().getState(), null);
        }
    }

    public void onIgnite(BlockIgniteEvent event)
    {
        World world = event.getBlock().getWorld();
        FireConfig config = this.configs.get(world);
        if (config.enabled)
        {
            switch (event.getCause())
            {
                case FIREBALL:
                    if (config.fireballIgnite)
                    {
                        this.setFire(FIRE, world, null, event.getBlock().getState()); //TODO change cause
                    }
                    break;
                case FLINT_AND_STEEL:
                    if (config.flintAndSteelIgnite)
                    {
                        this.setFire(PLAYER, world, event.getPlayer(), event.getBlock().getState());
                    }
                    break;
                case LAVA:
                    if (config.lavaFireSpread)
                    {
                        this.setFire(LAVA, world, null, event.getBlock().getState());
                    }
                    break;
                case LIGHTNING:
                    if (config.lightningIgnite)
                    {
                        this.setFire(FIRE, world, null, event.getBlock().getState()); //TODO change cause
                    }
                    break;
                case SPREAD:
                    if (config.fireSpread)
                    {
                        this.setFire(FIRE, world, null, event.getBlock().getState());
                    }
                    break;
            }

        }
    }

    private void setFire(BlockChangeCause cause, World world, Player player, BlockState oldState)
    {
        BlockState newState = oldState.getBlock().getState();
        newState.setType(Material.FIRE);
        this.logBlockChange(cause,world,player,oldState,newState);
    }
}
