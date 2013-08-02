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
package de.cubeisland.engine.basics.storage;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import de.cubeisland.engine.core.storage.database.AttrType;
import de.cubeisland.engine.core.storage.database.Attribute;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserEntity;
import de.cubeisland.engine.core.util.Version;

@Entity
@Table(name = "ignorelist")
// TODO updater!!! adding the id field
public class IgnoreList
{
    @javax.persistence.Version
    static final Version version = new Version(1);

    @Id
    public long id; // Ebean requires this
    @Column(name = "key") // TODO change
    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.REMOVE})
    @Attribute(type = AttrType.INT, unsigned = true)
    public UserEntity userEntity;
    @Column(nullable = false)
    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.REMOVE})
    @Attribute(type = AttrType.INT, unsigned = true)
    public UserEntity ignore;

    public IgnoreList()
    {
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public IgnoreList(User user, User ignore)
    {
        this.userEntity = user.getEntity();
        this.ignore = ignore.getEntity();
    }

    public UserEntity getUserEntity()
    {
        return userEntity;
    }

    public void setUserEntity(UserEntity userEntity)
    {
        this.userEntity = userEntity;
    }

    public UserEntity getIgnore()
    {
        return ignore;
    }

    public void setIgnore(UserEntity ignore)
    {
        this.ignore = ignore;
    }
}
