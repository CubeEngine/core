package de.cubeisland.cubeengine.fly.database;


import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.storage.BasicStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.user.User;

/**
 *
 * @author Anselm Brehme
 */
public class FlyManager extends BasicStorage<FlyModel>
{
    public FlyManager(Database database, int revision)
    {
        super(database, FlyModel.class, revision);
        this.initialize();
    }

    public FlyModel getFlyModel(User user)
    {
        FlyModel model = user.getAttachment(FlyModel.class);
        if (model == null)
        {
            model = this.get(user.getKey());
            if (model == null)
            {
                model = new FlyModel(user);
            }
            user.attach(model);
        }
        return model;
    }

    public void save(final FlyModel model)
    {
        CubeEngine.getExecutor().submit(new Runnable()
        {
            public void run()
            {
                merge(model);
            }
        });
    }

    public void remove(final int key)
    {
        CubeEngine.getExecutor().submit(new Runnable()
        {
            public void run()
            {
                deleteByKey(key);
            }
        });
    }    
}

