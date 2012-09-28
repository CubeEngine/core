package de.cubeisland.cubeengine.test.database;

import de.cubeisland.cubeengine.core.storage.BasicStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.DatabaseUpdater;

/**
 *
 * @author Anselm Brehme
 */
public class TestManager extends BasicStorage<TestModel>
{
    public TestManager(Database database)
    {
        super(database, TestModel.class, 42);//TODO
        this.registerUpdater(new DatabaseUpdater()
        {
            public void update(Database database)
            {
                //update blubb
            }
        }, 1, 2, 3);
        this.initialize();
    }
}
