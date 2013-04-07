package de.cubeisland.cubeengine.log.action.logaction.block.ignite;

import java.util.EnumSet;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockSpreadEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.ENVIRONEMENT;

/**
 * Fire spreading
 * <p>Events: {@link BlockSpreadEvent}</p>
 */
public class FireSpread extends BlockActionType
{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(BLOCK, ENVIRONEMENT);
    }

    @Override
    public String getName()
    {
        return  "fire-spread";
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpread(BlockSpreadEvent event)
    {
        if (event.getNewState().getType().equals(Material.FIRE))
        {
            if (this.isActive(event.getBlock().getWorld()))
            {
                this.logBlockChange(null,
                                    event.getBlock().getState(),
                                    event.getNewState(),null);
            }
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&aFire spreaded%s&a!",time,loc);
    }

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).FIRE_SPREAD_enable;
    }
}
