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
package de.cubeisland.engine.core.storage.database.mysql;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import de.cubeisland.engine.core.storage.database.AttrType;
import de.cubeisland.engine.core.storage.database.Attribute;

@Entity
@Table(name = "hasuser_test")
public class HasUserEntity
{
    @Id
    @Attribute(type = AttrType.INT, unsigned = true)
    private long key;
    @ManyToOne(cascade = {CascadeType.REMOVE, CascadeType.REFRESH})
    @Column(name = "userEntity_id", nullable = false)
    @Attribute(type = AttrType.INT, unsigned = true)
    private UserEntityTest userEntity;

    public long getKey()
    {
        return key;
    }

    public void setKey(long key)
    {
        this.key = key;
    }

    public UserEntityTest getUserEntity()
    {
        return userEntity;
    }

    public void setUserEntity(UserEntityTest userEntity)
    {
        this.userEntity = userEntity;
    }
}

