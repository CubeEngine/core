package de.cubeisland.cubeengine.log.action.logaction.block.entity;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.EntityBlockFormEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.ENTITY;

/**
 * Usually Snow-Golems making snow
 * <p>Events: {@link EntityBlockFormEvent}</p>
 */
public class EntityForm extends BlockActionType
{
    public EntityForm(Log module)
    {
        super(module, "entity-form", BLOCK, ENTITY);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityBlockForm(final EntityBlockFormEvent event)
    {
        if (this.isActive(event.getBlock().getWorld()))
        {
            this.logBlockChange(event.getEntity(),event.getBlock().getState(),event.getNewState(),null);
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&6%s &aformed &6%s%s&a!",
                            time,logEntry.getCauserEntity(),
                            logEntry.getNewBlock(),loc);
    }
}
