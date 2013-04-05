package de.cubeisland.cubeengine.log.action.logaction.block.entity;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

/**
 * Enderman picking up blocks.
 * <p>Events: {@link EntityChangeActionType}</p>
 */
public class EndermanPickup  extends BlockActionType
{
    public EndermanPickup(Log module)
    {
        super(module, 0x07, "enderman-place");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&6%s &agot picked up by an enderman%s!",
                            logEntry.getOldBlock());
    }
}
