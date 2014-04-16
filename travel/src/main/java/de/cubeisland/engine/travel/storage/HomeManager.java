package de.cubeisland.engine.travel.storage;

import org.bukkit.Location;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.travel.Travel;

import static de.cubeisland.engine.travel.storage.TableTeleportPoint.TABLE_TP_POINT;
import static de.cubeisland.engine.travel.storage.TeleportPointModel.TYPE_HOME;

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
    public Home create(Location location, String name, User owner, short visibility)
    {
        if (this.has(name, owner))
        {
            throw new IllegalArgumentException("Tried to create duplicate home!");
        }
        TeleportPointModel model = this.dsl.newRecord(TABLE_TP_POINT).newTPPoint(location, name, owner, null, TYPE_HOME, visibility);
        Home home = new Home(model, this, iManager, this.module);
        model.insert();
        this.assignTeleportPoint(home, home.getOwner());
        this.addPoint(home);
        return home;
    }

}
