package de.cubeisland.cubeengine.fly.database;

import de.cubeisland.cubeengine.core.storage.BasicStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.user.User;

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
}
