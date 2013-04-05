package de.cubeisland.cubeengine.log.action.logaction.block.explosion;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

public class CreeperExplode extends BlockActionType
{
    public CreeperExplode(Log module)
    {
        super(module, 0x11, "tnt-explode");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.hasAttached())
        {
            int amount = logEntry.getAttached().size()+1;
            if (logEntry.getCauserUser() == null)
            {
                user.sendTranslated("%s&aA Creeper-Explosion wrecked &6%dx %s&a%s!",
                                    time,amount,logEntry.getOldBlock(),loc);
            }
            else
            {
                user.sendTranslated("%s&2%s &alet a Creeper detonate and destroy &6%dx &6%s&a%s!",
                                    time,
                                    logEntry.getCauserUser().getDisplayName(),
                                    amount,
                                    logEntry.getOldBlock(),
                                    loc);
            }
        }
        else
        {
            if (logEntry.getCauserUser() == null)
            {
                user.sendTranslated("%s&aA Creeper-Explosion wrecked &6%s&a%s!",
                                    time,logEntry.getOldBlock(),loc);
            }
            else
            {
                user.sendTranslated("%s&2%s &alet a Creeper detonate and destroy &6%s&a%s!",
                                    time,
                                    logEntry.getCauserUser().getDisplayName(),
                                    logEntry.getOldBlock(),
                                    loc);
            }
        }
    }
}
