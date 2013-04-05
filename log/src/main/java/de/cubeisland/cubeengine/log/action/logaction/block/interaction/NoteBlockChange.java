package de.cubeisland.cubeengine.log.action.logaction.block.interaction;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;


/**
 * Changing NoteblockNotes
 * <p>Events: {@link RightClickActionType}</p>
 */
public class NoteblockChange extends BlockActionType
{
    public NoteblockChange(Log module)
    {
        super(module, 0x47, "noteblock-change");
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
