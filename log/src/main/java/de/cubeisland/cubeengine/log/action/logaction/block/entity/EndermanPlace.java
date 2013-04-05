package de.cubeisland.cubeengine.log.action.logaction.block.entity;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

/**
 * Enderman placing blocks.
 * <p>Events: {@link EntityChangeActionType}</p>
 */
public class EndermanPlace extends BlockActionType
{
    public EndermanPlace(Log module)
    {
        super(module, 0x26, "enderman-place");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&6%s &agot placed by an enderman%s&a!",
                            time,logEntry.getNewBlock(),loc);
    }
}
