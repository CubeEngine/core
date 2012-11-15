package de.cubeisland.cubeengine.basics;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.storage.BasicStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.entity.Player;

public class BasicUserManager extends BasicStorage<BasicUser>
{
    private static final int REVISION = 1;

    public BasicUserManager(Database database)
    {
        super(database, BasicUser.class, REVISION);
        this.initialize();
    }

    public BasicUser getBasicUser(Player player)
    {
        return this.getBasicUser(CubeEngine.getUserManager().getExactUser(player));
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
                this.store(model);
            }
            user.attach(model);
        }
        return model;
    }

    @Override
    public void update(BasicUser model)
    {
        if (model.muted != null && model.muted.getTime() < System.currentTimeMillis())
        {
            model.muted = null; // remove muted information as it is no longer needed
        }
        super.update(model);
    }
}