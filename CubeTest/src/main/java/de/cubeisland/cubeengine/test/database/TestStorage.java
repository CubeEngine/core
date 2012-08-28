
package de.cubeisland.cubeengine.test.database;

import de.cubeisland.cubeengine.core.storage.BasicStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;

/**
 *
 * @author Anselm Brehme
 */
public class TestStorage extends BasicStorage<TestModel>
{
    public TestStorage(Database database)
    {
        super(database, TestModel.class);
    }
}
