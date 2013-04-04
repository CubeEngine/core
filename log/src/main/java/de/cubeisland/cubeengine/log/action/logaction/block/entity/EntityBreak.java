package de.cubeisland.cubeengine.log.action.logaction.block.entity;

import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityBreakDoorEvent;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;

import static de.cubeisland.cubeengine.log.storage.ActionType.ENTITY_BREAK;
import static org.bukkit.Material.AIR;

public class EntityBreak extends BlockActionType
{
    public EntityBreak(Log module)
    {
        super(module, 0x06, "entity-break");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityBreakDoor(final EntityBreakDoorEvent event)
    {
        if (this.isActive(event.getEntity().getWorld()))
        {
            BlockState state = event.getBlock().getState();
            state = this.adjustBlockForDoubleBlocks(state);
            this.logBlockChange(state.getLocation(),event.getEntity(),BlockData.of(state),AIR,null);
        }
    }
}
