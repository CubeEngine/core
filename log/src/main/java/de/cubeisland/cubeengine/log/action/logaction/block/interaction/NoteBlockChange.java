package de.cubeisland.cubeengine.log.action.logaction.block.interaction;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;


/**
 * Changing NoteblockNotes
 * <p>Events: {@link RightClickActionType}</p>
 */
public class NoteBlockChange extends BlockActionType
{
    public NoteBlockChange(Log module)
    {
        super(module, "noteblock-change", BLOCK, PLAYER);
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        int clicks = logEntry.getNewBlock().data;
        user.sendTranslated("%s&2%s &aset the noteblock to &6%d&a clicks%s&a!",
                            time, logEntry.getCauserUser().getDisplayName(), clicks,loc);
        // TODO attach (show the actual change no change -> fiddled around but did not change anything)
    }
}
