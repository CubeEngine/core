/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.log.action.newaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;

import de.cubeisland.engine.bigdata.ReflectedMongoDB;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.math.BlockVector3;
import de.cubeisland.engine.core.world.ConfigWorld;
import de.cubeisland.engine.log.storage.ShowParameter;
import de.cubeisland.engine.reflect.Section;

/**
 * The Base for any Loggable Action
 * <p>The ListenerType will listen for given action
 *
 * @param <ListenerType>
 */
public abstract class ActionTypeBase<ListenerType> extends ReflectedMongoDB
{
    public Date date = new Date();

    public Coordinate coord;

    protected int countAttached()
    {
        int count = 1;
        if (this.hasAttached())
        {
            count += this.getAttached().size();
        }
        return count;
    }

    public static class Coordinate implements Section
    {
        public ConfigWorld world;
        public UUID worldUUID;
        public int[] xz;
        public int y;

        public Coordinate(Location loc)
        {
            this.world = new ConfigWorld(CubeEngine.getCore().getWorldManager(), loc.getWorld());
            this.worldUUID = loc.getWorld().getUID();
            this.xz = new int[2];
            this.xz[0] = loc.getBlockX();
            this.y = loc.getBlockY();
            this.xz[1] = loc.getBlockZ();
        }

        public World getWorld()
        {
            return world.getWorld();
        }

        public BlockVector3 toBlockVector()
        {
            return new BlockVector3(xz[0], y, xz[1]);
        }

        public boolean compareTo(Coordinate coordinate)
        {
            return coordinate.worldUUID.equals(this.worldUUID)
                && Arrays.equals(coordinate.xz, this.xz)
                && coordinate.y == this.y;
        }
    }

    private transient List<ActionTypeBase> attached;

    public final void setLocation(Location loc)
    {
        this.coord = new Coordinate(loc);
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
