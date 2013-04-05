package de.cubeisland.cubeengine.log.action.logaction.spawn;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

public class NaturalSpawn extends SimpleLogActionType

{
    public NaturalSpawn(Log module)
    {
        super(module, 0x81, "natural-spawn");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&6%s &aspawned naturally%s&a!",
                            time,this.getPrettyName(logEntry.getCauserEntity()),loc);
    }
}
