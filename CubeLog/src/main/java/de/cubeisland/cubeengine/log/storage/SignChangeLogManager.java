package de.cubeisland.cubeengine.log.storage;

import de.cubeisland.cubeengine.core.storage.BasicStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;

public class SignChangeLogManager extends BasicStorage<SignChangeLog>
{
    private static final int REVISION = 1;

    public SignChangeLogManager(Database database)
    {
        super(database, SignChangeLog.class, REVISION);
        this.initialize();
        this.notAssignKey();
    }
}
