
package de.cubeisland.cubeengine.test.database;

import de.cubeisland.cubeengine.core.persistence.BasicStorage;
import de.cubeisland.cubeengine.core.persistence.database.Database;
import java.util.Collection;

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

    public TestModel get(Object key)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<TestModel> getAll()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void store(TestModel model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void update(TestModel model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void merge(TestModel model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean delete(TestModel model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean deleteByKey(Object key)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clear()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
