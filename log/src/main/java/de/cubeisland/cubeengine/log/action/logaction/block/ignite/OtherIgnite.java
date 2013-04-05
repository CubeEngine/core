package de.cubeisland.cubeengine.log.action.logaction.block.ignite;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

/**
 * other-ignite
 * <p>Events: {@link IgniteActionType}</p>
 */
public class OtherIgnite extends BlockActionType
{
    public OtherIgnite(Log module)
    {
        super(module, 0x38, "other-ignite");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&aFire got set by an explosion or something else%s&a!",time,loc);
    }
}
