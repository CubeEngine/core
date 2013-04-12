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
package de.cubeisland.cubeengine.travel.storage;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.*;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;

import org.apache.commons.lang.Validate;

import org.bukkit.Location;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SingleKeyEntity(tableName = "teleportpoints", primaryKey = "key", autoIncrement = true,
                 indices = {
                     @Index(value = Index.IndexType.FOREIGN_KEY, fields = "owner", f_table = "user", f_field = "key"), @Index(value = Index.IndexType.FOREIGN_KEY, fields = "world", f_table = "worlds", f_field = "key"), @Index(value = Index.IndexType.UNIQUE, fields = {"owner", "name", "type"})
                 })
public class TeleportPoint implements Model<Long>
{

    // Database values
    @Attribute(type = AttrType.INT, unsigned = true)
    public Long key;
    @Attribute(type = AttrType.INT, unsigned = true, name = "owner")
    public Long ownerKey;
    @Attribute(type = AttrType.SMALLINT, length = 1, name = "type")
    public int typeId;
    @Attribute(type = AttrType.SMALLINT, length = 1, name = "visibility")
    public int visibilityId;
    @Attribute(type = AttrType.INT, unsigned = true, name = "world")
    public Long worldKey;
    @Attribute(type = AttrType.DOUBLE, name = "x")
    public double x;
    @Attribute(type = AttrType.DOUBLE, name = "y")
    public double y;
    @Attribute(type = AttrType.DOUBLE, name = "z")
    public double z;
    @Attribute(type = AttrType.FLOAT, name = "yaw")
    public float yaw;
    @Attribute(type = AttrType.FLOAT, name = "pitch")
    public float pitch;

    // Database and "normal" values
    @Attribute(type = AttrType.VARCHAR, length = 32)
    public String name;
    @Attribute(type = AttrType.LONGTEXT, notnull = false, name = "welcomemsg")
    public String welcomeMsg;

    // "Normal" values
    public Location location;
    public User owner;
    public Type type;
    public Visibility visibility;

    @DatabaseConstructor
    public TeleportPoint(List<Object> args) throws ConversionException
    {
        this.key = Long.valueOf(args.get(0).toString());
        this.ownerKey = Long.valueOf(args.get(1).toString());
        this.typeId = Integer.valueOf(args.get(2).toString());
        this.visibilityId = Integer.valueOf(args.get(3).toString());
        this.worldKey = Long.valueOf(args.get(4).toString());
        this.x = Double.valueOf(args.get(5).toString());
        this.y = Double.valueOf(args.get(6).toString());
        this.z = Double.valueOf(args.get(7).toString());
        this.yaw = Float.valueOf(args.get(8).toString());
        this.pitch = Float.valueOf(args.get(9).toString());
        this.name = args.get(10).toString();
        this.welcomeMsg = args.get(11).toString();

        this.location = new Location(CubeEngine.getCore().getWorldManager().getWorld(worldKey), x, y, z, yaw, pitch);
        this.owner = CubeEngine.getUserManager().getUser(ownerKey);
        this.type = Type.values()[typeId];
        this.visibility = Visibility.values()[visibilityId];
    }

    public TeleportPoint(Location location, String name, User owner, String welcomeMsg, Type type, Visibility visibility)
    {
        Validate.notNull(location);
        Validate.notEmpty(name);
        Validate.notNull(owner);
        Validate.notNull(type);
        Validate.notNull(visibility);
        if (welcomeMsg == null)
        {
            welcomeMsg = "";
        }

        // Load the "normal" values
        this.location = location;
        this.name = name;
        this.owner = owner;
        this.welcomeMsg = welcomeMsg;
        this.type = type;
        this.visibility = visibility;

        // Load the values used in the DB
        this.ownerKey = owner.getKey();
        this.typeId = type.ordinal();
        this.visibilityId = visibility.ordinal();
        this.worldKey = CubeEngine.getCore().getWorldManager().getWorldId(location.getWorld());
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    @Override
    public Long getKey()
    {
        return key;
    }

    @Override
    public void setKey(Long key)
    {
        this.key = key;
    }

    /**
     * Enum to reflect the type a teleport point is
     */
    public enum Type
    {
        HOME,
        WARP
    }

    /**
     * Enum to reflect whether the teleport point is public or private
     */
    public enum Visibility
    {
        PUBLIC,
        PRIVATE
    }
}
