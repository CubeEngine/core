package de.cubeisland.cubeengine.log.action.logaction.block;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFadeEvent;

import de.cubeisland.cubeengine.log.Log;

/**
 * Created with IntelliJ IDEA.
 * User: Anselm
 * Date: 04.04.13
 * Time: 20:22
 * To change this template use File | Settings | File Templates.
 */
public class BlockFade extends BlockActionType
{
    public BlockFade(Log module)
    {
        super(module, 0x02, "block-fade");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event)
    {
        if (this.isActive(event.getBlock().getWorld()))
        {
            this.logBlockChange(event.getBlock().getLocation(),null,
                                event.getBlock().getState(),
                                event.getNewState(),null);
        }
    }
}
