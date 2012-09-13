
package de.cubeisland.cubeengine.test.database;

import de.cubeisland.cubeengine.core.storage.BasicStorage;
import de.cubeisland.cubeengine.core.storage.DatabaseUpdater;
import de.cubeisland.cubeengine.core.storage.database.Database;

/**
 *
 * @author Anselm Brehme
 */
public class TestManager extends BasicStorage<TestModel>
{
    public TestManager(Database database)
    {
        super(database, TestModel.class,42);//TODO
        this.registerUpdater(new DatabaseUpdater() {

            public <T extends Database> void update(T database)
            {
                //update blubb
            }
        }, 1,2,3);
    }
}
