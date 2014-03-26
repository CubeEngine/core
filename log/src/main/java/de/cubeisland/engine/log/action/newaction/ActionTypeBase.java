package de.cubeisland.engine.log.action.newaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;

import de.cubeisland.engine.bigdata.ReflectedMongoDB;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.world.ConfigWorld;
import de.cubeisland.engine.log.storage.ShowParameter;

/**
 * The Base for any Loggable Action
 * <p>The ListenerType will listen for given action
 *
 * @param <ListenerType>
 */
public abstract class ActionTypeBase<ListenerType> extends ReflectedMongoDB
{
    public Date date = new Date();
    public ConfigWorld world;
    public UUID worldUUID;
    public long x;
    public long y;
    public long z;

    private transient List<ActionTypeBase> attached;

    public final void setLocation(Location loc)
    {
        this.world = new ConfigWorld(CubeEngine.getCore().getWorldManager(), loc.getWorld());
        this.worldUUID = loc.getWorld().getUID();
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
    }

    public final List<ActionTypeBase> getAttached()
    {
        return attached;
    }

    public final boolean hasAttached()
    {
        return !(this.attached == null || this.attached.isEmpty());
    }

    public abstract boolean canAttach(ActionTypeBase action);

    public final void attach(ActionTypeBase action)
    {
        if (this.attached == null)
        {
            this.attached = new ArrayList<>();
        }
        this.attached.add(action);
    }

    public final void showAction(User user, ShowParameter show)
    {
        String msg = translateAction(user);
        user.sendMessage(msg);
        // TODO loc & time
    }

    public abstract String translateAction(User user);
}
