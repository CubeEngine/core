package de.cubeisland.cubeengine.log.action.logaction.block.ignite;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockIgniteEvent;

import de.cubeisland.cubeengine.log.Log;

import static de.cubeisland.cubeengine.log.storage.ActionType.*;
import static org.bukkit.event.block.BlockIgniteEvent.IgniteCause.FIREBALL;

public class FireballIgnite extends IgniteActionType
{
    public FireballIgnite(Log module)
    {
        super(module, 0x31, "fireball-ignite");
    }

    @Override
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIgnite(BlockIgniteEvent event)
    {
        if (event.getCause().equals(FIREBALL) && this.isActive(event.getBlock().getWorld()))
        {
            this.logIgnite(event.getBlock().getState(),null);
            //TODO get shooter if shooter is attacking player log player too
            //event.getIgnitingEntity()
        }
    }
}
