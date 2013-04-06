package de.cubeisland.cubeengine.log.action.logaction.block.ignite;

import org.bukkit.World;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.*;

/**
 * other-ignite
 * <p>Events: {@link IgniteActionType}</p>
 */
public class OtherIgnite extends BlockActionType
{
    public OtherIgnite(Log module)
    {
        super(module, BLOCK, ENVIRONEMENT);
    }

    @Override
    public String getName()
    {
        return "other-ignite";
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&aFire got set by an explosion or something else%s&a!",time,loc);
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).OTHER_IGNITE_enable;
    }
}
