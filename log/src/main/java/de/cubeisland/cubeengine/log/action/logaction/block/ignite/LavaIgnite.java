package de.cubeisland.cubeengine.log.action.logaction.block.ignite;

import org.bukkit.World;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.*;

/**
 * lava-ignite
 * <p>Events: {@link IgniteActionType}</p>
 */
public class LavaIgnite extends BlockActionType
{
    public LavaIgnite(Log module)
    {
        super(module, BLOCK, ENVIRONEMENT);
    }

    @Override
    public String getName()
    {
        return "lava-ignite";
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&aFire got set by lava%s&a!",time,loc);
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).LAVA_IGNITE_enable;
    }
}
