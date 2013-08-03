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
package de.cubeisland.engine.core.world;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.bukkit.World;

import de.cubeisland.engine.core.storage.database.AttrType;
import de.cubeisland.engine.core.storage.database.Attribute;


@Entity
@Table(name = "worlds")
// TODO change from String UUID -> 2 Longs
// TODO updater
public class WorldEntity
{
    @Id
    @Column(name = "key") // TODO change to Id
    @Attribute(type = AttrType.INT, unsigned = true)
    private Long id = -1L;
    @Column(length = 64, nullable = false)
    @Attribute(type = AttrType.VARCHAR)
    private String worldName;
    @Column(length = 64, unique = true, nullable = false)
    @Attribute(type = AttrType.VARCHAR)
    private String worldUUID;

    public WorldEntity()
    {}

    public WorldEntity(World world)
    {
        this.worldName = world.getName();
        this.worldUUID = world.getUID().toString();
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getWorldName()
    {
        return worldName;
    }

    public void setWorldName(String worldName)
    {
        this.worldName = worldName;
    }

    public String getWorldUUID()
    {
        return worldUUID;
    }

    public void setWorldUUID(String worldUUID)
    {
        this.worldUUID = worldUUID;
    }
}
