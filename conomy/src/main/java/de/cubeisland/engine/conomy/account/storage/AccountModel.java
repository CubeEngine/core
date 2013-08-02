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
package de.cubeisland.engine.conomy.account.storage;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import de.cubeisland.engine.core.storage.database.AttrType;
import de.cubeisland.engine.core.storage.database.Attribute;
import de.cubeisland.engine.core.storage.database.Index;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserEntity;
import de.cubeisland.engine.core.util.Version;

import static de.cubeisland.engine.core.storage.database.Index.IndexType.UNIQUE;

@Entity
@Table(name = "accounts", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "name"}))
@Index(value = UNIQUE, fields = { "user_id", "name"})
public class AccountModel
{
    @javax.persistence.Version
    static final Version version = new Version(1);

    @Id
    @Column(name = "key")
    @Attribute(type = AttrType.INT, unsigned = true)
    private long id;
    @Column(name = "user_id")
    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.REMOVE})
    @JoinColumn(name = "user_id")
    @Attribute(type = AttrType.INT, unsigned = true)
    private UserEntity userEntity;
    @Column(length = 64)
    @Attribute(type = AttrType.VARCHAR)
    private String name;
    @Column()
    @Attribute(type = AttrType.BIGINT)
    private long value;
    @Column()
    @Attribute(type = AttrType.TINYINT)
    private int mask = 0;

    public AccountModel()
    {}

    public AccountModel(User user, String name, long balance, boolean hidden, boolean needsInvite)
    {
        this.userEntity = user.getEntity();
        this.name = name;
        this.value = balance;
        this.mask = (byte)((hidden ? 1 : 0) + (needsInvite ? 2 : 0));
    }

    public AccountModel(User user, String name, long balance, boolean hidden)
    {
        this(user, name, balance, hidden, false);
    }

    public boolean needsInvite()
    {
        return (this.mask & 2) == 2;
    }

    public boolean isHidden()
    {
        return (this.mask & 1) == 1;
    }

    public void setNeedsInvite(boolean set)
    {
        if (set)
        {
            this.mask |= 2;
        }
        else
        {
            this.mask &= ~2;
        }
    }

    public void setHidden(boolean set)
    {
        if (set)
        {
            this.mask |= 1;
        }
        else
        {
            this.mask &= ~1;
        }
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public UserEntity getUserEntity()
    {
        return userEntity;
    }

    public void setUserEntity(UserEntity userEntity)
    {
        this.userEntity = userEntity;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public long getValue()
    {
        return value;
    }

    public void setValue(long value)
    {
        this.value = value;
    }

    public int getMask()
    {
        return mask;
    }

    public void setMask(int mask)
    {
        this.mask = mask;
    }
}
