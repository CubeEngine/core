package de.cubeisland.cubeengine.log.storage;

import de.cubeisland.cubeengine.core.storage.BasicStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;

public class KillLogManager extends BasicStorage<KillLog>
{
    private static final int REVISION = 1;

    public KillLogManager(Database database)
    {
        super(database, KillLog.class, REVISION);
        this.initialize();
        this.notAssignKey();
    }
}