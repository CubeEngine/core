package de.cubeisland.cubeengine.log.action.logaction.block.interaction;

import org.bukkit.World;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;

/**
 * Changing Repeater Ticks
 * <p>Events: {@link RightClickActionType}</p>
 */
public class RepeaterChange extends BlockActionType
{
    public RepeaterChange(Log module)
    {
        super(module, "repeater-change", BLOCK, PLAYER);
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        int delay = (logEntry.getNewBlock().data >> 2) + 1;
        user.sendTranslated("%s&2%s &aset the repeater to &6%d &aticks delay%s&a!",
                            time,logEntry.getCauserUser().getDisplayName(), delay,loc);
        // TODO attach (show the actual change no change -> fiddled around but did not change anything)
    }

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).REPEATER_CHANGE_enable;
    }
}
