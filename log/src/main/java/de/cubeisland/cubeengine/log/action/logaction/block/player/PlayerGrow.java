package de.cubeisland.cubeengine.log.action.logaction.block.player;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

public class PlayerGrow extends BlockActionType
{
    public PlayerGrow(Log module)
    {
        super(module, 0x24, "player-grow");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.hasAttached())
        {
            int amount = logEntry.getAttached().size()+1;
            user.sendTranslated("%s&2%s let grow &6%d %s%s&a!",
                                time,logEntry.getCauserUser().getDisplayName(),
                                amount,logEntry.getNewBlock(),loc);
        }
        else
        {
            if (logEntry.hasReplacedBlock())
            {
                user.sendTranslated("%s&2%s let grow &6%s&a into &6%s%s&a!",
                                    time,logEntry.getCauserUser().getDisplayName(),
                                    logEntry.getNewBlock(),
                                    logEntry.getOldBlock(),loc);
            }
            else
            {
                user.sendTranslated("%s&2%s let grow &6%s%s&a!",
                                    time,logEntry.getCauserUser().getDisplayName(),
                                    logEntry.getNewBlock(),loc);
            }
        }
    }
}
