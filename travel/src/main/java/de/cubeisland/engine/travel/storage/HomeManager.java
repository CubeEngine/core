package de.cubeisland.engine.travel.storage;

import org.bukkit.Location;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.travel.Travel;

import static de.cubeisland.engine.travel.storage.TableTeleportPoint.TABLE_TP_POINT;
import static de.cubeisland.engine.travel.storage.TeleportPointModel.TYPE_HOME;
import static de.cubeisland.engine.travel.storage.TeleportPointModel.VISIBILITY_PRIVATE;
import static de.cubeisland.engine.travel.storage.TeleportPointModel.VISIBILITY_PUBLIC;

public class HomeManager extends TelePointManager<Home>
{
    public HomeManager(Travel module, InviteManager iManager)
    {
        super(module, iManager, HomeAttachment.class);
    }

    @Override
    public void load()
    {
        for (TeleportPointModel teleportPoint : this.dsl.selectFrom(TABLE_TP_POINT).where(TABLE_TP_POINT.TYPE.eq(TYPE_HOME)).fetch())
        {
            this.addPoint(new Home(teleportPoint, this, iManager, this.module));
        }
    }

    @Override
    public Home create(User owner, String name, Location location, boolean publicVisibility)
    {
        if (this.has(owner, name))
        {
            throw new IllegalArgumentException("Tried to create duplicate home!");
        }
        TeleportPointModel model = this.dsl.newRecord(TABLE_TP_POINT).newTPPoint(location, name, owner, null, TYPE_HOME, publicVisibility ? VISIBILITY_PUBLIC : VISIBILITY_PRIVATE);
        Home home = new Home(model, this, iManager, this.module);
        model.insert();
        this.assignTeleportPoint(home, home.getOwner());
        this.addPoint(home);
        return home;
    }

}
