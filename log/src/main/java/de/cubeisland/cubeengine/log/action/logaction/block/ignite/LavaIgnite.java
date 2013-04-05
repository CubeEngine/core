package de.cubeisland.cubeengine.log.action.logaction.block.ignite;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

/**
 * lava-ignite
 * <p>Events: {@link IgniteActionType}</p>
 */
public class LavaIgnite extends BlockActionType
{
    public LavaIgnite(Log module)
    {
        super(module, 0x33, "lava-ignite");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&aFire got set by lava%s&a!",time,loc);
    }
}
