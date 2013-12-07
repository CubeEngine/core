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
import javax.persistence.Transient;

import org.jooq.Record7;
import org.jooq.Row7;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.core.user.TableUser.TABLE_USER;

public class UserEntity extends UpdatableRecordImpl<UserEntity>
    implements Record7<UInteger, String, Boolean, Timestamp, byte[], Timestamp, String>
{
    public UserEntity()
    {
        super(TABLE_USER);
    }

    /**
     * Fills in Information for a new User
     *
     * @param player
     * @return
     */
    public UserEntity newUser(String player)
    {
        this.setKey(UInteger.valueOf(0));
        this.setPlayer(player);
        this.setLastseen(new Timestamp(System.currentTimeMillis()));
        this.setFirstseen(this.getLastseen());
        this.setPasswd(new byte[0]);
        this.setNogc(false);
        return this;
    }

    public UInteger getKey()
    {
        return (UInteger)getValue(0);
    }

    public void setKey(UInteger value)
    {
        setValue(0, value);
    }

    public String getPlayer()
    {
        return (String)getValue(1);
    }

    public void setPlayer(String value)
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
        return (Timestamp) getValue(3);
    }

    public void setLastseen(Timestamp value)
    {
        setValue(3, value);
    }

    public byte[] getPasswd()
    {
        return (byte[]) getValue(4);
    }

    public void setPasswd(byte[] value)
    {
        setValue(4, value);
    }

    public Timestamp getFirstseen()
    {
        return (Timestamp) getValue(5);
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

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public org.jooq.Record1<org.jooq.types.UInteger> key() {
        return (org.jooq.Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record7 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row7<UInteger, String, Boolean, Timestamp, byte[], Timestamp, String> fieldsRow() {
        return (Row7) super.fieldsRow();
    }

    @Override
    public Row7<UInteger, String, Boolean, Timestamp, byte[], Timestamp, String> valuesRow() {
        return (Row7) super.valuesRow();
    }

    @Override
    public org.jooq.Field<org.jooq.types.UInteger> field1() {
        return TABLE_USER.KEY;
    }

    @Override
    public org.jooq.Field<java.lang.String> field2() {
        return TABLE_USER.PLAYER;
    }

    @Override
    public org.jooq.Field<java.lang.Boolean> field3() {
        return TABLE_USER.NOGC;
    }

    @Override
    public org.jooq.Field<java.sql.Timestamp> field4() {
        return TABLE_USER.LASTSEEN;
    }

    @Override
    public org.jooq.Field<byte[]> field5() {
        return TABLE_USER.PASSWD;
    }

    @Override
    public org.jooq.Field<java.sql.Timestamp> field6() {
        return TABLE_USER.FIRSTSEEN;
    }

    @Override
    public org.jooq.Field<java.lang.String> field7() {
        return TABLE_USER.LANGUAGE;
    }

    @Override
    public org.jooq.types.UInteger value1() {
        return getKey();
    }

    @Override
    public java.lang.String value2() {
        return getPlayer();
    }

    @Override
    public java.lang.Boolean value3() {
        return (Boolean)getValue(3);
    }

    @Override
    public java.sql.Timestamp value4() {
        return getLastseen();
    }

    @Override
    public byte[] value5() {
        return getPasswd();
    }

    @Override
    public java.sql.Timestamp value6() {
        return getFirstseen();
    }

    @Override
    public java.lang.String value7() {
        return (String)getValue(6);
    }
}
