package de.cubeisland.cubeengine.log.action.logaction.block.ignite;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockIgniteEvent;

import de.cubeisland.cubeengine.log.Log;

import static de.cubeisland.cubeengine.log.storage.ActionType.*;
import static org.bukkit.event.block.BlockIgniteEvent.IgniteCause.ENDER_CRYSTAL;
import static org.bukkit.event.block.BlockIgniteEvent.IgniteCause.EXPLOSION;
import static org.bukkit.event.block.BlockIgniteEvent.IgniteCause.FIREBALL;

public class OtherIgnite extends IgniteActionType
{
    public OtherIgnite(Log module)
    {
        super(module, 0x38, "other-ignite");
    }

    @Override
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIgnite(BlockIgniteEvent event)
    {
        if ((event.getCause().equals(ENDER_CRYSTAL) || event.getCause().equals(EXPLOSION))
            && this.isActive(event.getBlock().getWorld()))
        {
            this.logIgnite(event.getBlock().getState(),null);
        }
    }
}
