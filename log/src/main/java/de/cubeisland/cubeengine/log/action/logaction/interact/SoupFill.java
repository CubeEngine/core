package de.cubeisland.cubeengine.log.action.logaction.interact;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

/**
 * Filling soup with mooshrooms
 * <p>Events: {@link InteractEntityActionType}</p>
 */
public class SoupFill extends SimpleLogActionType
{
    public SoupFill(Log module)
    {
        super(module, 0x58, "soup-fill");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&2%s &amade soup with a mooshroom!",
                            time, logEntry.getCauserUser().getDisplayName(),loc);
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return logEntry.world == other.world
            && logEntry.causer == other.causer;
    }
}
