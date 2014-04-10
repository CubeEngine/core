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
package de.cubeisland.engine.log.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.World;

import de.cubeisland.engine.bigdata.ReflectedDBObject;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.math.BlockVector3;
import de.cubeisland.engine.core.world.ConfigWorld;
import de.cubeisland.engine.log.LoggingConfiguration;
import de.cubeisland.engine.log.storage.ShowParameter;
import de.cubeisland.engine.reflect.Section;
import org.bson.types.ObjectId;

import static de.cubeisland.engine.core.util.formatter.MessageType.NONE;
import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static org.bukkit.ChatColor.GRAY;

/**
 * The Base for any Loggable Action
 */
public abstract class BaseAction extends ReflectedDBObject implements Comparable<BaseAction>
{
    public Date date = new Date();
    public Coordinate coord;
    private transient List<BaseAction> attached;

    private transient final String name;
    private transient final List<ActionCategory> categorySet;

    private static final SimpleDateFormat dateOnly = new SimpleDateFormat("yyyy-MM-dd");

    protected BaseAction(String name, ActionCategory... categories)
    {
        this.name = name;
        this.categorySet = Arrays.asList(categories);
    }

    public final String getName()
    {
        return name;
    }

    public final List<ActionCategory> getCategories()
    {
        return categorySet;
    }

    protected int countAttached()
    {
        int count = 1;
        if (this.hasAttached())
        {
            count += this.getAttached().size();
        }
        return count;
    }

    public final void setLocation(Location loc)
    {
        this.coord = new Coordinate(loc);
    }

    public final List<BaseAction> getAttached()
    {
        return attached;
    }

    public final boolean hasAttached()
    {
        return !(this.attached == null || this.attached.isEmpty());
    }

    public abstract boolean canAttach(BaseAction action);

    public final void attach(BaseAction action)
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
        String time = "";
        if (show.showDate)
        {
            if (this.hasAttached())
            {
                Date first = this.date;
                Date last = this.getAttached().get(this.getAttached().size() - 1).date;
                if (first.getTime() > last.getTime())
                {
                    first = last;
                    last = this.date;
                }
                //private static final SimpleDateFormat timeOnly = new SimpleDateFormat("HH:mm:ss");


                String fDate = dateOnly.format(first);
                if (dateOnly.format(last).equals(fDate)) // Same day
                {
                    time = GRAY + user.getTranslation(NONE, " {date#without time:format=yy-MM-dd} {date#time from:format=HH\\:mm\\:ss} - {date#time to:format=HH\\:mm\\:ss}: ", first, first, last);
                }
                else
                {
                    time = GRAY + user.getTranslation(NONE, " {date#without time:format=yy-MM-dd} {date#time from:format=HH\\:mm\\:ss} - {date#without time:format=yy-MM-dd} {date#time to:format=HH\\:mm\\:ss}:", first, first, last, last) + "\n";
                }
            }
            else
            {
                if (dateOnly.format(this.date).equals(dateOnly.format(new Date())))
                {
                    // Same day
                    time = GRAY + user.getTranslation(NONE, "{date#time from:format=HH\\:mm\\:ss}: ", this.date);
                }
                else
                {
                    time = GRAY + user.getTranslation(NONE, " {date#without time:yy-MM-dd} {date#time from:format=HH\\:mm\\:ss}: ", this.date, this.date);
                }
            }
        }
        String loc = "";
        if (show.showCoords)
        {
            loc =  "\n";
            if (this.hasAttached())
            {
                int xMin = this.coord.vector.x;
                int yMin = this.coord.vector.y;
                int zMin = this.coord.vector.z;
                int xMax = this.coord.vector.x;
                int yMax = this.coord.vector.y;
                int zMax = this.coord.vector.z;
                for (BaseAction entry : this.getAttached())
                {
                    if (entry.coord.vector.x < xMin)
                    {
                        xMin = entry.coord.vector.x;
                    }
                    else if (entry.coord.vector.x > xMax)
                    {
                        xMax = entry.coord.vector.x;
                    }
                    if (entry.coord.vector.y < yMin)
                    {
                        yMin = entry.coord.vector.y;
                    }
                    else if (entry.coord.vector.y > yMax)
                    {
                        yMax = entry.coord.vector.y;
                    }
                    if (entry.coord.vector.z < zMin)
                    {
                        zMin = entry.coord.vector.z;
                    }
                    else if (entry.coord.vector.z > zMax)
                    {
                        zMax = entry.coord.vector.z;
                    }
                }
                if (xMax == xMin && yMax == yMin && zMax == zMin)
                {
                    loc += user.getTranslation(POSITIVE, "   at {vector} in {world}", new BlockVector3(xMax, yMax, zMax), coord.world.getWorld());
                }
                else
                {
                    loc += user.getTranslation(POSITIVE, "   in between {vector} and {vector} in {world}", new BlockVector3(xMin, yMin, zMin), new BlockVector3(xMax, yMax, zMax), coord.world.getWorld());
                }
            }
            else
            {
                loc += user.getTranslation(POSITIVE, "   at {vector} in {world}", new BlockVector3(coord.vector.x, coord.vector.y, coord.vector.z), coord.world.getWorld());
            }
        }
        user.sendMessage(time + msg + loc);
    }

    public abstract boolean isActive(LoggingConfiguration config);

    public abstract String translateAction(User user);

    public final boolean isNearTimeFrame(TimeUnit unit, int i, BaseAction action)
    {
        return unit.toMillis(i) > Math.abs(this.date.getTime() - action.date.getTime());
    }

    public static class Coordinate implements Section
    {
        public ConfigWorld world;
        public UUID worldUUID;
        public Block3DVector vector;

        public Coordinate()
        {
        }

        public Coordinate(Location loc)
        {
            this.world = new ConfigWorld(CubeEngine.getCore().getWorldManager(), loc.getWorld());
            this.worldUUID = loc.getWorld().getUID();
            this.vector = new Block3DVector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }

        public World getWorld()
        {
            return world.getWorld();
        }

        public BlockVector3 toBlockVector()
        {
            return new BlockVector3(this.vector.x, this.vector.y, this.vector.z);
        }

        public boolean equals(Coordinate coordinate)
        {
            return coordinate.worldUUID.equals(this.worldUUID) && this.vector.equals(coordinate.vector);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            Coordinate that = (Coordinate)o;

            if (!vector.equals(that.vector))
            {
                return false;
            }
            if (!worldUUID.equals(that.worldUUID))
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = worldUUID.hashCode();
            result = 31 * result + vector.hashCode();
            return result;
        }

        public Location toLocation()
        {
            return new Location(this.getWorld(), this.vector.x, this.vector.y, this.vector.z);
        }

        public static class Block3DVector implements Section
        {
            public int x;
            public int y;
            public int z;

            public Block3DVector()
            {
            }

            public Block3DVector(int x, int y, int z)
            {
                this.x = x;
                this.y = y;
                this.z = z;
            }

            @Override
            public boolean equals(Object o)
            {
                if (this == o)
                {
                    return true;
                }
                if (o == null || getClass() != o.getClass())
                {
                    return false;
                }

                Block3DVector that = (Block3DVector)o;

                if (x != that.x)
                {
                    return false;
                }
                if (y != that.y)
                {
                    return false;
                }
                if (z != that.z)
                {
                    return false;
                }

                return true;
            }

            @Override
            public int hashCode()
            {
                int result = x;
                result = 31 * result + y;
                result = 31 * result + z;
                return result;
            }
        }
    }

    @Override
    public int compareTo(BaseAction action)
    {
        return ((ObjectId)this.getTarget().get("_id")).compareTo((ObjectId)action.getTarget().get("_id"));
    }
}
