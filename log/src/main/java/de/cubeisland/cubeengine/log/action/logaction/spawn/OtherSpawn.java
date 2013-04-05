package de.cubeisland.cubeengine.log.action.logaction.spawn;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

public class OtherSpawn extends SimpleLogActionType

{
    public OtherSpawn(Log module)
    {
        super(module, 0x83, "other-spawn");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {//TODO get player in data once possible
        user.sendTranslated("%s&6%s &aspawned%s&a!",
                           time, this.getPrettyName(logEntry.getCauserEntity()),loc);
    }
}
