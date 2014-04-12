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
package de.cubeisland.engine.core.user;

import java.sql.Timestamp;
import java.util.Locale;
import java.util.UUID;
import javax.persistence.Transient;

import org.bukkit.OfflinePlayer;

import org.jooq.Field;
import org.jooq.Record9;
import org.jooq.Row9;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.core.user.TableUser.TABLE_USER;

public class UserEntity extends UpdatableRecordImpl<UserEntity> implements Record9<UInteger, String, Boolean, Timestamp, byte[], Timestamp, String, Long, Long>
{
    public UserEntity()
    {
        super(TABLE_USER);
    }

    /**
     * Fills in Information for a new User
     */
    public UserEntity newUser(OfflinePlayer player)
    {
        this.setKey(UInteger.valueOf(0));
        this.setLastName(player.getName().toLowerCase());
        this.setLastseen(new Timestamp(System.currentTimeMillis()));
        this.setFirstseen(this.getLastseen());
        this.setPasswd(new byte[0]);
        this.setNogc(false);
        this.setUUID(player.getUniqueId());
        return this;
    }

    private UUID uid = null;

    public UUID getUUID()
    {
        if (uid == null)
        {
            uid = new UUID(this.getUUIDMost(), this.getUUIDLeast());
        }
        return uid;
    }

    public void setUUID(UUID uid)
    {
        this.uid = uid;
        this.setUUIDLeast(uid.getLeastSignificantBits());
        this.setUUIDMost(uid.getMostSignificantBits());
    }

    public UInteger getKey()
    {
        return (UInteger)getValue(0);
    }

    public void setKey(UInteger value)
    {
        setValue(0, value);
    }

    public String getLastName()
    {
        return (String)getValue(1);
    }

    public void setLastName(String value)
    {
        setValue(1, value);
    }

    public boolean isNogc()
    {
        return (boolean)getValue(2);
    }

    public void setNogc(boolean value)
    {
        setValue(2, value);
    }

    public Timestamp getLastseen()
    {
        return (Timestamp)getValue(3);
    }

    public void setLastseen(Timestamp value)
    {
        setValue(3, value);
    }

    public byte[] getPasswd()
    {
        return (byte[])getValue(4);
    }

    public void setPasswd(byte[] value)
    {
        setValue(4, value);
    }

    public Timestamp getFirstseen()
    {
        return (Timestamp)getValue(5);
    }

    public void setFirstseen(Timestamp value)
    {
        setValue(5, value);
    }

    @Transient
    private Locale locale;

    public Locale getLocale()
    {
        if (getValue(6) == null)
        {
            return null;
        }
        if (locale == null)
        {
            locale = Locale.forLanguageTag((String)getValue(6));
        }
        return locale;
    }

    public void setLocale(Locale locale)
    {
        this.locale = locale;
        setValue(6, locale.toString());
    }

    public void setUUIDLeast(Long value)
    {
        setValue(7, value);
    }

    public Long getUUIDLeast()
    {
        return (Long)getValue(7);
    }

    public void setUUIDMost(Long value)
    {
        setValue(8, value);
    }

    public Long getUUIDMost()
    {
        return (Long)getValue(8);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public org.jooq.Record1<UInteger> key()
    {
        return (org.jooq.Record1)super.key();
    }

    // -------------------------------------------------------------------------
    // Record7 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row9<UInteger, String, Boolean, Timestamp, byte[], Timestamp, String, Long, Long> fieldsRow()
    {
        return (Row9)super.fieldsRow();
    }

    @Override
    public Row9<UInteger, String, Boolean, Timestamp, byte[], Timestamp, String, Long, Long> valuesRow()
    {
        return (Row9)super.valuesRow();
    }

    @Override
    public Field<UInteger> field1()
    {
        return TABLE_USER.KEY;
    }

    @Override
    public Field<String> field2()
    {
        return TABLE_USER.LASTNAME;
    }

    @Override
    public Field<Boolean> field3()
    {
        return TABLE_USER.NOGC;
    }

    @Override
    public Field<Timestamp> field4()
    {
        return TABLE_USER.LASTSEEN;
    }

    @Override
    public Field<byte[]> field5()
    {
        return TABLE_USER.PASSWD;
    }

    @Override
    public Field<Timestamp> field6()
    {
        return TABLE_USER.FIRSTSEEN;
    }

    @Override
    public Field<String> field7()
    {
        return TABLE_USER.LANGUAGE;
    }

    @Override
    public Field<Long> field8()
    {
        return TABLE_USER.LEAST;
    }

    @Override
    public Field<Long> field9()
    {
        return TABLE_USER.MOST;
    }

    @Override
    public UInteger value1()
    {
        return getKey();
    }

    @Override
    public String value2()
    {
        return getLastName();
    }

    @Override
    public Boolean value3()
    {
        return (Boolean)getValue(3);
    }

    @Override
    public Timestamp value4()
    {
        return getLastseen();
    }

    @Override
    public byte[] value5()
    {
        return getPasswd();
    }

    @Override
    public Timestamp value6()
    {
        return getFirstseen();
    }

    @Override
    public String value7()
    {
        return (String)getValue(6);
    }

    @Override
    public Long value8()
    {
        return getUUIDLeast();
    }

    @Override
    public Long value9()
    {
        return getUUIDMost();
    }
}
