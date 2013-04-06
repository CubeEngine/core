package de.cubeisland.cubeengine.log.action.logaction.worldedit;

import org.bukkit.Material;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

public class WorldEditActionType extends BlockActionType
{
    public WorldEditActionType(Log module)
    {
        super(module, "worldedit");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.getNewBlock().material.equals(Material.AIR))
        {
            user.sendTranslated("&2%s &aused worldedit to remove &6%s&a!",
                                logEntry.getCauserUser().getDisplayName(),
                                logEntry.getOldBlock());
        }
        else if (logEntry.getOldBlock().material.equals(Material.AIR))
        {
            user.sendTranslated("&2%s &aused worldedit to place &6%s&a!",
                                logEntry.getCauserUser().getDisplayName(),
                                logEntry.getNewBlock());
        }
        else
        {
            user.sendTranslated("&2%s &aused worldedit to replace &6%s&a with &6%s&a!",
                                logEntry.getCauserUser().getDisplayName(),
                                logEntry.getOldBlock(),
                                logEntry.getNewBlock());
        }
    }
}
