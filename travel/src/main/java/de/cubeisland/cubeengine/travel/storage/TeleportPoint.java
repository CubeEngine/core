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

import java.util.Map;

import org.bukkit.Location;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.DatabaseConstructor;
import de.cubeisland.cubeengine.core.storage.database.Index;
import de.cubeisland.cubeengine.core.storage.database.SingleKeyEntity;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;

import org.apache.commons.lang.Validate;

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
    public String welcomeMsg = "";

    public String ownerName;

    // "Normal" values
    protected Location location;
    protected User owner;
    public Type type;
    public Visibility visibility;

    @DatabaseConstructor
    public TeleportPoint(Map<String, Object> args) throws ConversionException
    {
        this.key = Long.valueOf(args.get("key").toString());
        this.ownerKey = Long.valueOf(args.get("owner").toString());
        this.typeId = Integer.valueOf(args.get("type").toString());
        this.visibilityId = Integer.valueOf(args.get("visibility").toString());
        this.worldKey = Long.valueOf(args.get("world").toString());
        this.x = Double.valueOf(args.get("x").toString());
        this.y = Double.valueOf(args.get("y").toString());
        this.z = Double.valueOf(args.get("z").toString());
        this.yaw = Float.valueOf(args.get("yaw").toString());
        this.pitch = Float.valueOf(args.get("pitch").toString());
        this.name = args.get("name").toString();
        if (args.get("welcomemsg") != null)
        {
            this.welcomeMsg = args.get("welcomemsg").toString();
        }
        if (args.get("player") != null)
        {
            this.ownerName = args.get("player").toString();
        }

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
        this.ownerName = owner.getName();
        this.welcomeMsg = welcomeMsg;
        this.type = type;
        this.visibility = visibility;

        // Load the values used in the DB
        this.ownerKey = owner.getId();
        this.typeId = type.ordinal();
        this.visibilityId = visibility.ordinal();
        this.worldKey = owner.getCore().getWorldManager().getWorldId(location.getWorld());
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    @Override
    public Long getId()
    {
        return key;
    }

    @Override
    public void setId(Long id)
    {
        this.key = id;
    }

    protected User getOwner()
    {
        if (this.owner == null)
        {
            // TODO get rid of CubeEngine
            this.owner = CubeEngine.getCore().getUserManager().getUser(ownerKey);
        }
        return this.owner;
    }

    public String getOwnerName()
    {
        return this.ownerName;
    }

    protected Location getLocation()
    {
        if (this.location == null)
        {
            // TODO get rid of CubeEngine
            this.location = new Location(CubeEngine.getCore().getWorldManager().getWorld(worldKey), x, y, z, yaw, pitch);
        }
        return this.location;
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
