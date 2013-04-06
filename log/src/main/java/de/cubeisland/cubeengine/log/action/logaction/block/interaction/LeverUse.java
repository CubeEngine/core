package de.cubeisland.cubeengine.log.action.logaction.block.interaction;

import org.bukkit.World;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;


/**
 * Flipping levers
 * <p>Events: {@link RightClickActionType}</p>
 */
public class LeverUse extends BlockActionType
{
    public LeverUse(Log module)
    {
        super(module, "lever-use", BLOCK, PLAYER);
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if ((logEntry.getNewBlock().data & 0x8) == 0x8)
        {
            user.sendTranslated("%s&2%s &aactivated the lever%s&a!",
                                time, logEntry.getCauserUser().getDisplayName(),loc);
        }
        else
        {
            user.sendTranslated("&s&2%s &adeactivated the lever%s&a!",
                                time, logEntry.getCauserUser().getDisplayName(),loc);
        }
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).LEVER_USE_enable;
    }
}
