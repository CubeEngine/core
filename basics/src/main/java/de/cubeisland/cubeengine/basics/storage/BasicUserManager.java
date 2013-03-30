package de.cubeisland.cubeengine.basics.storage;

import org.bukkit.entity.Player;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.storage.SingleKeyStorage;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.basics.BasicsAttachment;

public class BasicUserManager extends SingleKeyStorage<Long, BasicUser>
{
    private final Core core;
    private static final int REVISION = 1;

    public BasicUserManager(Core core)
    {
        super(core.getDB(), BasicUser.class, REVISION);
        this.core = core;
        this.initialize();
    }

    public BasicUser getBasicUser(Player player)
    {
        return this.getBasicUser(this.core.getUserManager().getExactUser(player));
    }

    public BasicUser getBasicUser(User user)
    {
        BasicUser model = user.get(BasicsAttachment.class).getBasicUser();
        if (model == null)
        {
            model = this.get(user.getKey());
            if (model == null)
            {
                model = new BasicUser(user);
                this.store(model);
            }
            user.get(BasicsAttachment.class).setBasicUser(model);
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
