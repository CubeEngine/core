package de.cubeisland.cubeengine.log.storage.signs;

import de.cubeisland.cubeengine.core.storage.BasicStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;

public class SignChangeLogManager extends BasicStorage<SignChangLog>
{
    private static final int REVISION = 1;

    public SignChangeLogManager(Database database)
    {
        super(database, SignChangLog.class, REVISION);
        this.initialize();
        this.notAssignKey();
    }
}