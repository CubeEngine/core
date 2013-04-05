package de.cubeisland.cubeengine.log.action.logaction.spawn;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

public class SpawnerSpawn extends SimpleLogActionType

{
    public SpawnerSpawn(Log module)
    {
        super(module, 0x82, "spawner-spawn");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&6%s &aspawned from a spawner%s!",
                            time,this.getPrettyName(logEntry.getCauserEntity()),loc);
    }
}
