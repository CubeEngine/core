package de.cubeisland.cubeengine.log.action.logaction.block;

import java.util.EnumSet;

import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFormEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.ENVIRONEMENT;


/**
 * Blocks forming
 * <p>Events: {@link BlockFormEvent}</p>
 */
public class BlockForm extends BlockActionType
{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(BLOCK, ENVIRONEMENT);
    }
    @Override
    public String getName()
    {
        return "block-form";
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

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).BLOCK_FORM_enable;
    }

    @Override
    public void logBlockChange(Entity causer, BlockState oldBlock, BlockState newBlock, String additional)
    {
        if (this.lm.getConfig(newBlock.getWorld()).BLOCK_FORM_ignore.contains(newBlock.getType()))
        {
            return;
        }
        super.logBlockChange(causer, oldBlock, newBlock, additional);
    }
}
