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

import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.core.user.TableUser.TABLE_USER;

public class UserEntity extends UpdatableRecordImpl<UserEntity>
{
    @Transient
    private UUID uid = null;
    @Transient
    private Locale locale;

    public UserEntity()
    {
        super(TABLE_USER);
    }

    /**
     * Fills in Information for a new User
     */
    public UserEntity newUser(OfflinePlayer player)
    {
        this.setValue(TABLE_USER.KEY, UInteger.valueOf(0));
        this.setValue(TABLE_USER.LASTNAME, player.getName().toLowerCase());
        this.setValue(TABLE_USER.LASTSEEN, new Timestamp(System.currentTimeMillis()));
        this.setValue(TABLE_USER.FIRSTSEEN, this.getValue(TABLE_USER.LASTSEEN));
        this.setValue(TABLE_USER.PASSWD, new byte[0]);
        this.setValue(TABLE_USER.NOGC, false);
        this.setUUID(player.getUniqueId());
        return this;
    }

    public UInteger getKey()
    {
        return getValue(TABLE_USER.KEY);
    }

    public UUID getUUID()
    {
        if (uid == null)
        {
            uid = new UUID(this.getValue(TABLE_USER.MOST), this.getValue(TABLE_USER.LEAST));
        }
        return uid;
    }

    public void setUUID(UUID uid)
    {
        this.uid = uid;
        this.setValue(TABLE_USER.LEAST, uid.getLeastSignificantBits());
        this.setValue(TABLE_USER.MOST, uid.getMostSignificantBits());
    }

    public Locale getLocale()
    {
        if (getValue(TABLE_USER.LANGUAGE) == null)
        {
            return null;
        }
        if (locale == null)
        {
            locale = Locale.forLanguageTag(getValue(TABLE_USER.LANGUAGE));
        }
        return locale;
    }

    public void setLocale(Locale locale)
    {
        this.locale = locale;
        setValue(TABLE_USER.LANGUAGE, locale.toString());
    }
}
