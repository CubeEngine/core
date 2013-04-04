package de.cubeisland.cubeengine.log.action.logaction.block.entity;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.EntityBlockFormEvent;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;

import static de.cubeisland.cubeengine.log.storage.ActionType.ENTITY_FORM;

public class EntityForm extends BlockActionType
{
    public EntityForm(Log module)
    {
        super(module, 0x27, "entity-form");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityBlockForm(final EntityBlockFormEvent event)
    {
        if (this.isActive(event.getBlock().getWorld()))
        {
            this.logBlockChange(event.getEntity(),event.getBlock().getState(),event.getNewState(),null);
        }
    }
}
