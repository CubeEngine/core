package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.BlockUtil;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.logger.config.BlockBreakConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.inventory.ItemStack;

import static de.cubeisland.cubeengine.log.logger.BlockLogger.BlockChangeCause.PLAYER;

public class BlockBreakLogger extends BlockLogger<BlockBreakConfig>
{

    public BlockBreakLogger(Log module)
    {
        super(module, BlockBreakConfig.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event)
    {
        if (event.getBlock().getState().getTypeId() == 0)
        {
            return;
        }
        if (event.getBlock().getState() instanceof Sign)
        {
            this.module.getLoggerManager().getLogger(SignChangeLogger.class).logSignBreak(event.getPlayer(), (Sign)event.getBlock().getState());
        }
        for (Block block : BlockUtil.getAttachedBlocks(event.getBlock()))
        {
            this.log(PLAYER, event.getPlayer(), block.getState());
        }
        switch (event.getBlock().getRelative(BlockFace.UP).getType())
        {
            case WOODEN_DOOR:
            case IRON_DOOR:
            case SNOW:
            case SEEDS:
            case LONG_GRASS:
            case SUGAR_CANE_BLOCK:
            case PUMPKIN_STEM:
            case MELON_STEM:
            case NETHER_WARTS:
            case DEAD_BUSH:
            case SAPLING:
            case YELLOW_FLOWER:
            case RED_ROSE:
            case RED_MUSHROOM:
            case BROWN_MUSHROOM:
            case STONE_PLATE:
            case WOOD_PLATE:
            case REDSTONE_WIRE:
            case DIODE_BLOCK_OFF:
            case DIODE_BLOCK_ON:
            case CACTUS:
                this.log(PLAYER, event.getPlayer(), event.getBlock().getRelative(BlockFace.UP).getState());
        }
        this.log(PLAYER, event.getPlayer(), event.getBlock().getState());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event)
    {
        if (event.getBlockClicked().getRelative(BlockFace.UP).getType().equals(Material.WATER_LILY))
        {
            this.log(PLAYER, event.getPlayer(), event.getBlockClicked().getRelative(BlockFace.UP).getState());
        }
        this.log(PLAYER, event.getPlayer(), event.getBlockClicked().getState());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event)
    {
        Location location = event.getEntity().getLocation();
        Long cause = null;
        if (event instanceof HangingBreakByEntityEvent)
        {
            Entity entity = ((HangingBreakByEntityEvent)event).getRemover();
            if (entity instanceof Player)
            {
                User user = this.module.getUserManager().getExactUser((Player)entity);
                cause = user.key;
            }
            else
            {
                System.out.println(entity.getType());
                // TODO arrows ?
                return;
            }
        }
        else
        {
            switch (event.getCause())
            {
                case EXPLOSION:
                    cause = -1L;
                    break;
                case PHYSICS:
                    cause = -2L;
                    break;
                case OBSTRUCTION:
                    cause = -3L;
                    break;
                case DEFAULT:
                    cause = -4L;
            }
        }
        if (event.getEntity().getType().equals(EntityType.ITEM_FRAME))
        {
            ItemFrame itemFrame = (ItemFrame)event.getEntity();
            ItemStack item = itemFrame.getItem();
        //    this.logHangingBlockBreak(cause,location,Material.ITEM_FRAME,0,item);
        }
        else if (event.getEntity().getType().equals(EntityType.PAINTING))
        {
            Painting painting = (Painting)event.getEntity();
            int art = painting.getArt().getId();
          //  this.logHangingBlockBreak(cause,location,Material.PAINTING,art,null);
        }
    }

    private void log(BlockChangeCause cause, Player player, BlockState oldState)
    {
        World world = oldState.getWorld();
        BlockBreakConfig config = this.configs.get(world);
        if (config.enabled)
        {
            if (!config.noLogging.contains(oldState.getType()))
            {
                this.logBlockChange(cause, world, player, oldState, null);
            }
        }
    }

}
