package de.cubeisland.cubeengine.log.action.logaction.block.ignite;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockIgniteEvent;

import de.cubeisland.cubeengine.log.Log;

import static de.cubeisland.cubeengine.log.storage.ActionType.*;
import static org.bukkit.event.block.BlockIgniteEvent.IgniteCause.FIREBALL;
import static org.bukkit.event.block.BlockIgniteEvent.IgniteCause.LAVA;

public class LavaIgnite extends IgniteActionType
{
    public LavaIgnite(Log module)
    {
        super(module, 0x33, "lava-ignite");
    }

    @Override
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIgnite(BlockIgniteEvent event)
    {
        if (event.getCause().equals(LAVA) && this.isActive(event.getBlock().getWorld()))
        {
            this.logIgnite(event.getBlock().getState(),null);
        }
    }
}
