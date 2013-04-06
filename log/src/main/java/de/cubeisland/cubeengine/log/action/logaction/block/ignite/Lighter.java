package de.cubeisland.cubeengine.log.action.logaction.block.ignite;

import org.bukkit.World;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;

/**
 * lighter-ignite
 * <p>Events: {@link IgniteActionType}</p>
 */
public class Lighter extends BlockActionType
{
    public Lighter(Log module)
    {
        super(module, "lighter-ignite", BLOCK, PLAYER);
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&2%s &aset fire%s&a!",
                            time, logEntry.getCauserUser().getDisplayName(),loc);
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).LIGHTER_IGNITE_enable;
    }
}
