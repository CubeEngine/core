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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.avaje.ebean.annotation.NamedUpdate;
import com.avaje.ebean.annotation.NamedUpdates;
import de.cubeisland.engine.core.storage.database.AttrType;
import de.cubeisland.engine.core.storage.database.Attribute;
import de.cubeisland.engine.core.util.Version;

@Entity
@Table(name = "user")
@NamedUpdates(
    {@NamedUpdate(name = "clearPw", update = "UPDATE user SET passwd = :passwd"),
     @NamedUpdate(name = "cleanUp", update = "DELETE FROM user WHERE lastseen < :lastseen AND NOT nogc"),
    }
)
public class UserEntity
{
    @javax.persistence.Version
    static final Version version = new Version(1);
    public static final Long NO_ID = -1L;

    @Id
    @Column(name = "key") // TODO change to Id
    @Attribute(type = AttrType.INT, unsigned = true)
    private long id;
    @Column(nullable = false, length = 16, unique = true)
    @Attribute(type = AttrType.VARCHAR)
    private String player;
    @Column(nullable = false)
    @Attribute(type = AttrType.BOOLEAN)
    private boolean nogc = false;
    @Column(nullable = false)
    @Attribute(type = AttrType.DATETIME)
    private Timestamp lastseen;
    @Column(length = 128)
    @Attribute(type = AttrType.VARBINARY)
    private byte[] passwd;
    @Column(nullable = false)
    @Attribute(type = AttrType.DATETIME)
    private Timestamp firstseen;
    @Column(name = "language", length = 5)
    @Attribute(type = AttrType.VARCHAR)
    private Locale locale = null;

    public UserEntity()
    {
    }

    UserEntity(String playerName)
    {
        this.id = NO_ID;
        this.player = playerName;
        this.lastseen = new Timestamp(System.currentTimeMillis());
        this.firstseen = this.lastseen;
        this.passwd = new byte[0];
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getPlayer()
    {
        return player;
    }

    public void setPlayer(String player)
    {
        this.player = player;
    }

    public boolean isNogc()
    {
        return nogc;
    }

    public void setNogc(boolean nogc)
    {
        this.nogc = nogc;
    }

    public Timestamp getLastseen()
    {
        return lastseen;
    }

    public void setLastseen(Timestamp lastseen)
    {
        this.lastseen = lastseen;
    }

    public byte[] getPasswd()
    {
        return passwd;
    }

    public void setPasswd(byte[] passwd)
    {
        this.passwd = passwd;
    }

    public Timestamp getFirstseen()
    {
        return firstseen;
    }

    public void setFirstseen(Timestamp firstseen)
    {
        this.firstseen = firstseen;
    }

    public Locale getLocale()
    {
        return locale;
    }

    public void setLocale(Locale locale)
    {
        this.locale = locale;
    }
}
