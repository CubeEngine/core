package de.cubeisland.cubeengine.log.storage;

import de.cubeisland.cubeengine.core.storage.BasicStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;

public class BlockLogManager extends BasicStorage<BlockLog>
{
    private static final int REVISION = 1;

    public BlockLogManager(Database database)
    {
        super(database, BlockLog.class, REVISION);
        this.initialize();
        this.notAssignKey();
    }
}