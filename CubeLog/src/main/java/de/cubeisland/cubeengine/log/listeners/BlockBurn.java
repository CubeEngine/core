package de.cubeisland.cubeengine.log.listeners;

import de.cubeisland.cubeengine.core.util.BlockUtil;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.LogSubConfiguration;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBurnEvent;

import static de.cubeisland.cubeengine.log.LogManager.BlockChangeCause.FIRE;

public class BlockBurn extends LogListener
{
    public BlockBurn(Log module)
    {
        super(module, new BurnConfig());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event)
    {
        for (Block block : BlockUtil.getAttachedBlocks(event.getBlock()))
        {
            lm.logBreakBlock(FIRE, null, block.getState());
        }
        switch (event.getBlock().getRelative(BlockFace.UP).getType())
        {
            case WOODEN_DOOR:
            case IRON_DOOR:
            case SNOW:
            case STONE_PLATE:
            case WOOD_PLATE:
            case REDSTONE_WIRE:
            case DIODE_BLOCK_OFF:
            case DIODE_BLOCK_ON:
                lm.logBreakBlock(FIRE, null, event.getBlock().getRelative(BlockFace.UP).getState());
        }
        lm.logBreakBlock(FIRE, null, event.getBlock().getState());
    }

    public static class BurnConfig extends LogSubConfiguration
    {
        public BurnConfig()
        {
            this.actions.put(LogAction.FIRE, true);
            this.enabled = false;
        }

        @Override
        public String getName()
        {
            return "burn";
        }
    }
}
