package de.cubeisland.cubeengine.log.logger.container;

import de.cubeisland.cubeengine.core.storage.BasicStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;

public class ChestLogManager extends BasicStorage<ChestLog>
{
    private static final int REVISION = 1;

    public ChestLogManager(Database database)
    {
        super(database, ChestLog.class, REVISION);
        this.initialize();
        this.notAssignKey();
    }
}