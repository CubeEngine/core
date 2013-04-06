package de.cubeisland.cubeengine.log.action.logaction.block;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFormEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.ENVIRONEMENT;


/**
 * Blocks forming
 * <p>Events: {@link BlockFormEvent}</p>
 */
public class BlockForm extends BlockActionType
{
    public BlockForm(Log module)
    {
        super(module, "block-form", BLOCK, ENVIRONEMENT);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event)
    {
        if (this.isActive(event.getBlock().getWorld()))
        {
            this.logBlockChange(null,
                                event.getBlock().getState(),
                                event.getNewState(),null);
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.hasAttached())
        {
            int amount = logEntry.getAttached().size()+1;
            user.sendTranslated("%s&6%dx %s &aformed naturally%s&a!",
                                time,amount,logEntry.getNewBlock(),loc);
        }
        else
        {
            user.sendTranslated("%s&6%s &aformed naturally%s&a!",
                                time,logEntry.getNewBlock(),loc);
        }
    }
}
