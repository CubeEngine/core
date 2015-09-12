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
package org.cubeengine.service.user;

import java.sql.Timestamp;
import java.util.Locale;
import java.util.UUID;
import javax.persistence.Transient;
import org.cubeengine.service.database.AsyncRecord;
import org.jooq.types.UInteger;
import org.spongepowered.api.entity.living.player.User;

public class UserEntity extends AsyncRecord<UserEntity>
{
    @Transient private UUID uid = null;
    @Transient private Locale locale;

    public UserEntity()
    {
        super(TableUser.TABLE_USER);
    }

    /**
     * Fills in Information for a new User
     */
    public UserEntity newUser(User player)
    {
        this.setValue(TableUser.TABLE_USER.KEY, UInteger.valueOf(0));
        this.setValue(TableUser.TABLE_USER.LASTNAME, player.getName().toLowerCase());
        this.setValue(TableUser.TABLE_USER.LASTSEEN, new Timestamp(System.currentTimeMillis()));
        this.setValue(TableUser.TABLE_USER.FIRSTSEEN, this.getValue(TableUser.TABLE_USER.LASTSEEN));
        this.setValue(TableUser.TABLE_USER.NOGC, false);
        this.setUUID(player.getUniqueId());
        return this;
    }

    public UInteger getId()
    {
        return getValue(TableUser.TABLE_USER.KEY);
    }

    public UUID getUniqueId()
    {
        if (uid == null)
        {
            uid = new UUID(this.getValue(TableUser.TABLE_USER.MOST), this.getValue(TableUser.TABLE_USER.LEAST));
        }
        return uid;
    }

    public void setUUID(UUID uid)
    {
        this.uid = uid;
        this.setValue(TableUser.TABLE_USER.LEAST, uid.getLeastSignificantBits());
        this.setValue(TableUser.TABLE_USER.MOST, uid.getMostSignificantBits());
    }

    public Locale getLocale()
    {
        if (getValue(TableUser.TABLE_USER.LANGUAGE) == null)
        {
            return null;
        }
        if (locale == null)
        {
            locale = Locale.forLanguageTag(getValue(TableUser.TABLE_USER.LANGUAGE));
        }
        return locale;
    }

    public void setLocale(Locale locale)
    {
        this.locale = locale;
        setValue(TableUser.TABLE_USER.LANGUAGE, locale.toString());
    }
}
