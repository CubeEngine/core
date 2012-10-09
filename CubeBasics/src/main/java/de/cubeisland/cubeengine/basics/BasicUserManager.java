package de.cubeisland.cubeengine.basics;

import de.cubeisland.cubeengine.core.storage.BasicStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.user.User;

/**
 *
 * @author Anselm Brehme
 */
public class BasicUserManager extends BasicStorage<BasicUser>
{
    public BasicUserManager(Database database, int revision)
    {
        super(database, BasicUser.class, revision);
        this.initialize();
    }

    public BasicUser getBasicUser(User user)
    {
        BasicUser model = user.getAttachment(BasicUser.class);
        if (model == null)
        {
            model = this.get(user.getKey());
            if (model == null)
            {
                model = new BasicUser(user);
            }
            user.attach(model);
        }
        return model;
    }

    public void save(final BasicUser model)
    {
        this.database.queueOperation(new Runnable()
        {
            public void run()
            {
                merge(model);
            }
        });
    }

    public void remove(final int key)
    {
        this.database.queueOperation(new Runnable()
        {
            public void run()
            {
                deleteByKey(key);
            }
        });
    }
}