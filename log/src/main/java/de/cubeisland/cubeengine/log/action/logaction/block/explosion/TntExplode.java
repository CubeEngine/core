package de.cubeisland.cubeengine.log.action.logaction.block.explosion;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Type.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Type.ENTITY;
import static de.cubeisland.cubeengine.log.action.ActionType.Type.PLAYER;

/**
 * TNT-Explosions
 * <p>Events: {@link ExplodeActionType}</p>
 */
public class TntExplode extends BlockActionType
{
    public TntExplode(Log module)
    {
        super(module, "tnt-explode", BLOCK, PLAYER);
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.hasAttached())
        {
            int amount = logEntry.getAttached().size()+1;
            if (logEntry.hasCauserUser())
            {
                user.sendTranslated("%s&aA TNT-Explosion induced by &2%s&a got rid of &6%dx %s&a%s!",
                                    time,logEntry.getCauserUser().getDisplayName(),amount,
                                    logEntry.getOldBlock(),loc);
            }
            else
            {
                user.sendTranslated("%s&aA TNT-Explosion got rid of &6%dx %s&a%s!",
                                    time,amount,logEntry.getOldBlock(),loc);
            }
        }
        else
        {
            if (logEntry.hasCauserUser())
            {
                user.sendTranslated("%s&aA TNT-Explosion induced by &2%s&a got rid of &6%s&a%s!",
                                    time,logEntry.getCauserUser().getDisplayName(),
                                    logEntry.getOldBlock(),loc);
            }
            else
            {
                user.sendTranslated("%s&aA TNT-Explosion got rid of &6%s&a%s!",
                                    time,logEntry.getOldBlock(),loc);
            }
        }
    }
}
