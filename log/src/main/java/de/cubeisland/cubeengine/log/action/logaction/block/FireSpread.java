package de.cubeisland.cubeengine.log.action.logaction.block;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockSpreadEvent;

import de.cubeisland.cubeengine.log.Log;

public class FireSpread extends BlockActionType
{
    public FireSpread(Log module)
    {
        super(module, 0x30, "fire-spread");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpread(BlockSpreadEvent event)
    {
        if (event.getNewState().getType().equals(Material.FIRE))
        {
            if (this.isActive(event.getBlock().getWorld()))
            {
                this.logBlockChange(event.getBlock().getLocation(),null,
                                    event.getBlock().getState(),
                                    event.getNewState(),null);
            }
        }
    }
}
