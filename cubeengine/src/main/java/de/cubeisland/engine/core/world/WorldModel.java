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

import org.bukkit.World;

import de.cubeisland.engine.core.storage.Model;
import de.cubeisland.engine.core.storage.database.AttrType;
import de.cubeisland.engine.core.storage.database.Attribute;
import de.cubeisland.engine.core.storage.database.Index;
import de.cubeisland.engine.core.storage.database.Index.IndexType;
import de.cubeisland.engine.core.storage.database.SingleKeyEntity;

@SingleKeyEntity(tableName = "worlds", primaryKey = "key", autoIncrement = true,
indices = @Index(value = IndexType.UNIQUE, fields = "worldUUID"))
public class WorldModel implements Model<Long>
{
    @Attribute(type = AttrType.INT, unsigned = true)
    public Long key = -1L;
    @Attribute(type = AttrType.VARCHAR, length = 64, notnull = false)
    public String worldName;
    @Attribute(type = AttrType.VARCHAR, length = 64, notnull = false)
    public String worldUUID;

    public WorldModel()
    {}

    public WorldModel(World world)
    {
        this.worldName = world.getName();
        this.worldUUID = world.getUID().toString();
    }

    public WorldModel(long key, String worldName, String uuid)
    {
        this.key = key;
        this.worldName = worldName;
        this.worldUUID = uuid;
    }

    @Override
    public Long getId()
    {
        return this.key;
    }

    @Override
    public void setId(Long id)
    {
        this.key = id;
    }
}
