package de.cubeisland.cubeengine.log.action.logaction.block.interaction;

import org.bukkit.Material;
import org.bukkit.World;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;

/**
 * Changing comparator state
 * <p>Events: {@link RightClickActionType}</p>
 */
public class ComparatorChange extends BlockActionType
{
    public ComparatorChange(Log module)
    {
        super(module, "comparator-change", BLOCK, PLAYER);
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.getNewBlock().material.equals(Material.REDSTONE_COMPARATOR_ON))
        {
            user.sendTranslated("%s&2%s &aactivated the comparator%s&a!",
                                time,logEntry.getCauserUser().getDisplayName(),loc);
        }
        else
        {
            user.sendTranslated("%s&2%s &adeactivated the comparator%s&a!",
                                time,logEntry.getCauserUser().getDisplayName(),loc);
        }
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).COMPARATPR_CHANGE_enable;
    }
}
