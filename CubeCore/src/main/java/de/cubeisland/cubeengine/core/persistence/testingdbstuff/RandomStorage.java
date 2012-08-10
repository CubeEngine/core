
package de.cubeisland.cubeengine.core.persistence.testingdbstuff;

import de.cubeisland.cubeengine.core.persistence.BasicStorage;
import de.cubeisland.cubeengine.core.persistence.database.Database;
import java.util.Collection;

/**
 *
 * @author Anselm Brehme
 */
public class RandomStorage extends BasicStorage<Integer, RandomModel>
{

    public RandomStorage(Database database)
    {
        super(database, RandomModel.class);
    }

    
    public RandomModel get(Integer key)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<RandomModel> getAll()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void store(RandomModel model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void update(RandomModel model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void merge(RandomModel model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean delete(RandomModel model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean delete(Integer key)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clear()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
