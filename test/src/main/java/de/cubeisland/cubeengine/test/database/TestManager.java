package de.cubeisland.cubeengine.test.database;

import de.cubeisland.cubeengine.core.storage.SingleKeyStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.DatabaseUpdater;

public class TestManager extends SingleKeyStorage<Long, TestModel>
{
    public TestManager(Database database)
    {
        super(database, TestModel.class, 42);
        this.registerUpdater(new DatabaseUpdater()
        {
            @Override
            public void update(Database database)
            {
                //TODO update test if someone wants to do it (it worked already with Usermanager)
                }
        }, 1, 2, 3);
        this.initialize();
    }
}
