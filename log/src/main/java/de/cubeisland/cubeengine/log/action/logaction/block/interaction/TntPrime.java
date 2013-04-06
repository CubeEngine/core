package de.cubeisland.cubeengine.log.action.logaction.block.interaction;

import java.util.EnumSet;

import org.bukkit.World;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;

/**
 * Igniting Tnt with a lighter
 * <p>Events: {@link RightClickActionType}</p>
 */
public class TntPrime extends BlockActionType
{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(BLOCK, PLAYER);
    }

    @Override
    public String getName()
    {
        return "tnt-prime";
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.hasAttached())
        {
            int amount = logEntry.getAttached().size()+1;
            user.sendTranslated("%s&2%s &aignited &6%d &aTNT!%s",
                                time,logEntry.getCauserUser().getDisplayName(),amount,loc);
        }
        else
        {
            user.sendTranslated("%s&2%s &aignited TNT!%s",
                                time,logEntry.getCauserUser().getDisplayName(),loc);
        }
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).TNT_PRIME_enable;
    }
}
