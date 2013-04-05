package de.cubeisland.cubeengine.log.action.logaction.block.interaction;

import org.bukkit.Material;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

public class ComparatorChange extends BlockActionType
{
    public ComparatorChange(Log module)
    {
        super(module, 0x4A, "comparator-change");
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
}
