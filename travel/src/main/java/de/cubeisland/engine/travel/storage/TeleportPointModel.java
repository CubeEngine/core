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
package de.cubeisland.engine.travel.storage;

import javax.persistence.Transient;

import org.bukkit.Location;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.user.User;
import org.apache.commons.lang.Validate;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record12;
import org.jooq.Row12;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.travel.storage.TableTeleportPoint.TABLE_TP_POINT;

public class TeleportPointModel extends UpdatableRecordImpl<TeleportPointModel> implements Record12<UInteger, UInteger, Short, Short, UInteger, Double, Double, Double, Double, Double, String, String>
{
    @Transient
    private transient Location location;

    public TeleportPointModel()
    {
        super(TABLE_TP_POINT);
    }

    public TeleportPointModel newTPPoint(Location location, String name, User owner, String welcomeMsg, short type,
                                         short visibility)
    {
        Validate.notNull(location);
        Validate.notEmpty(name);
        Validate.notNull(owner);
        Validate.notNull(type);
        Validate.notNull(visibility);

        // Set transient values
        this.location = location;

        // Set persistent values
        this.setName(name);
        this.setOwnerKey(owner.getEntity().getKey());
        this.setType(type);
        this.setVisibility(visibility);
        this.setWorld(UInteger.valueOf(owner.getCore().getWorldManager().getWorldId(location.getWorld())));
        this.setX(location.getX());
        this.setY(location.getY());
        this.setZ(location.getZ());
        this.setYaw((double)location.getYaw());
        this.setPitch((double)location.getPitch());
        this.setWelcomemsg(welcomeMsg);

        return this;
    }

    public Location getLocation()
    {
        if (this.location == null)
        {
            this.location = new Location(CubeEngine.getCore().getWorldManager().getWorld(getWorld().longValue()),
                                         getX(), getY(), getZ(), getYaw().floatValue(), getPitch().floatValue());
        }
        return this.location;
    }

    public void setLocation(Location location)
    {
        this.location = location;
        this.setX(location.getX());
        this.setY(location.getY());
        this.setZ(location.getZ());
        this.setYaw((double)location.getYaw());
        this.setPitch((double)location.getPitch());
        this.setWorld(UInteger.valueOf(CubeEngine.getCore().getWorldManager().getWorldId(location.getWorld())));
    }

    public static final short TYPE_HOME = 0;
    public static final short TYPE_WARP = 1;
    public static final short VISIBILITY_PUBLIC = 0;
    public static final short VISIBILITY_PRIVATE = 1;

    public void setKey(UInteger value)
    {
        setValue(0, value);
    }

    public UInteger getKey()
    {
        return (UInteger)getValue(0);
    }

    public void setOwnerKey(UInteger value)
    {
        setValue(1, value);
    }

    public UInteger getOwnerKey()
    {
        return (UInteger)getValue(1);
    }

    public void setType(Short value)
    {
        setValue(2, value);
    }

    public Short getType()
    {
        return (Short)getValue(2);
    }

    public void setVisibility(Short value)
    {
        setValue(3, value);
    }

    public Short getVisibility()
    {
        return (Short)getValue(3);
    }

    public void setWorld(UInteger value)
    {
        setValue(4, value);
    }

    public UInteger getWorld()
    {
        return (UInteger)getValue(4);
    }

    public void setX(Double value)
    {
        setValue(5, value);
    }

    public Double getX()
    {
        return (Double)getValue(5);
    }

    public void setY(Double value)
    {
        setValue(6, value);
    }

    public Double getY()
    {
        return (Double)getValue(6);
    }

    public void setZ(Double value)
    {
        setValue(7, value);
    }

    public Double getZ()
    {
        return (Double)getValue(7);
    }

    public void setYaw(Double value)
    {
        setValue(8, value);
    }

    public Double getYaw()
    {
        return (Double)getValue(8);
    }

    public void setPitch(Double value)
    {
        setValue(9, value);
    }

    public Double getPitch()
    {
        return (Double)getValue(9);
    }

    public void setName(String value)
    {
        setValue(10, value);
    }

    public String getName()
    {
        return (String)getValue(10);
    }

    public void setWelcomemsg(String value)
    {
        setValue(11, value);
    }

    public String getWelcomemsg()
    {
        return (String)getValue(11);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Record1<UInteger> key()
    {
        return (Record1)super.key();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Row12<UInteger, UInteger, Short, Short, UInteger, Double, Double, Double, Double, Double, String, String> fieldsRow()
    {
        return (Row12)super.fieldsRow();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Row12<UInteger, UInteger, Short, Short, UInteger, Double, Double, Double, Double, Double, String, String> valuesRow()
    {
        return (Row12)super.valuesRow();
    }

    @Override
    public Field<UInteger> field1()
    {
        return TABLE_TP_POINT.KEY;
    }

    @Override
    public Field<UInteger> field2()
    {
        return TABLE_TP_POINT.OWNER;
    }

    @Override
    public Field<Short> field3()
    {
        return TABLE_TP_POINT.TYPE;
    }

    @Override
    public Field<Short> field4()
    {
        return TABLE_TP_POINT.VISIBILITY;
    }

    @Override
    public Field<UInteger> field5()
    {
        return TABLE_TP_POINT.WORLD;
    }

    @Override
    public Field<Double> field6()
    {
        return TABLE_TP_POINT.X;
    }

    @Override
    public Field<Double> field7()
    {
        return TABLE_TP_POINT.Y;
    }

    @Override
    public Field<Double> field8()
    {
        return TABLE_TP_POINT.Z;
    }

    @Override
    public Field<Double> field9()
    {
        return TABLE_TP_POINT.YAW;
    }

    @Override
    public Field<Double> field10()
    {
        return TABLE_TP_POINT.PITCH;
    }

    @Override
    public Field<String> field11()
    {
        return TABLE_TP_POINT.NAME;
    }

    @Override
    public Field<String> field12()
    {
        return TABLE_TP_POINT.WELCOMEMSG;
    }

    @Override
    public UInteger value1()
    {
        return getKey();
    }

    @Override
    public UInteger value2()
    {
        return getOwnerKey();
    }

    @Override
    public Short value3()
    {
        return getType();
    }

    @Override
    public Short value4()
    {
        return getVisibility();
    }

    @Override
    public UInteger value5()
    {
        return getWorld();
    }

    @Override
    public Double value6()
    {
        return getX();
    }

    @Override
    public Double value7()
    {
        return getY();
    }

    @Override
    public Double value8()
    {
        return getZ();
    }

    @Override
    public Double value9()
    {
        return getYaw();
    }

    @Override
    public Double value10()
    {
        return getPitch();
    }

    @Override
    public String value11()
    {
        return getName();
    }

    @Override
    public String value12()
    {
        return getWelcomemsg();
    }
}
