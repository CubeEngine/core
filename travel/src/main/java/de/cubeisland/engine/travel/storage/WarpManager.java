package de.cubeisland.engine.travel.storage;
import org.bukkit.Location;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.travel.Travel;

import static de.cubeisland.engine.travel.storage.TableTeleportPoint.TABLE_TP_POINT;
import static de.cubeisland.engine.travel.storage.TeleportPointModel.TYPE_WARP;
import static de.cubeisland.engine.travel.storage.TeleportPointModel.VISIBILITY_PRIVATE;
import static de.cubeisland.engine.travel.storage.TeleportPointModel.VISIBILITY_PUBLIC;

public class WarpManager extends TelePointManager<Warp>
{
    public WarpManager(Travel module, InviteManager iManager)
    {
        super(module, iManager, WarpAttachment.class);
    }

    @Override
    public void load()
    {
        for (TeleportPointModel teleportPoint : this.dsl.selectFrom(TABLE_TP_POINT).where(TABLE_TP_POINT.TYPE.eq(TYPE_WARP)).fetch())
        {
            this.addPoint(new Warp(teleportPoint, this, iManager, this.module));
        }
    }

    @Override
    public Warp create(User owner, String name, Location location, boolean publicVisibility)
    {
        if (this.has(owner, name))
        {
            throw new IllegalArgumentException("Tried to create duplicate warp!");
        }
        TeleportPointModel model = this.dsl.newRecord(TABLE_TP_POINT).newTPPoint(location, name, owner, null, TYPE_WARP, publicVisibility ? VISIBILITY_PUBLIC : VISIBILITY_PRIVATE);
        Warp warp = new Warp(model, this, iManager, this.module);
        model.insert();
        this.assignTeleportPoint(warp, warp.getOwner());
        this.addPoint(warp);
        return warp;
    }
}
