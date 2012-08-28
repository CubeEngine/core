
package de.cubeisland.cubeengine.test.database;

import de.cubeisland.cubeengine.core.storage.BasicStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;

/**
 *
 * @author Anselm Brehme
 */
public class TestManager extends BasicStorage<TestModel>
{
    public TestManager(Database database)
    {
        super(database, TestModel.class);
    }
}
