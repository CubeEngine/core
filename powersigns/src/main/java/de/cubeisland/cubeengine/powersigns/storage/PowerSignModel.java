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
package de.cubeisland.cubeengine.powersigns.storage;

import org.bukkit.Location;

import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Index;
import de.cubeisland.cubeengine.core.storage.database.SingleKeyEntity;
import de.cubeisland.cubeengine.powersigns.signtype.SignTypeInfo;

import static de.cubeisland.cubeengine.core.storage.database.Index.IndexType.FOREIGN_KEY;
import static de.cubeisland.cubeengine.core.storage.database.Index.IndexType.UNIQUE;

@SingleKeyEntity(tableName = "powerSign", primaryKey = "id", autoIncrement = true, indices = {
    @Index(value = FOREIGN_KEY, fields = "owner_id", f_table = "user", f_field = "key"),
    @Index(value = FOREIGN_KEY, fields = "world", f_table = "worlds", f_field = "key"),
    @Index(value = UNIQUE, fields = {"x","y","z","world"})
})
public class PowerSignModel implements Model<Long>
{
    @Attribute(type = AttrType.INT, unsigned = true)
    public Long id = -1L;
    @Attribute(type = AttrType.INT, unsigned = true, notnull = false)
    public Long owner_id;
    @Attribute(type = AttrType.VARCHAR, length = 6)
    public String PSID;

    @Attribute(type = AttrType.INT)
    public int x;
    @Attribute(type = AttrType.INT)
    public int y;
    @Attribute(type = AttrType.INT)
    public int z;
    @Attribute(type = AttrType.INT, unsigned = true)
    public long world;

    @Attribute(type = AttrType.INT)
    public int chunkX;
    @Attribute(type = AttrType.INT)
    public int chunkZ;

    @Attribute(type = AttrType.TEXT, notnull = false)
    public String data;

    public PowerSignModel()
    {
    }

    public PowerSignModel(SignTypeInfo info)
    {
        this.owner_id = info.getCreator();
        this.PSID = info.getType().getPSID();
        Location location = info.getLocation();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.world = info.getWorldID();
        this.chunkX = location.getChunk().getX();
        this.chunkZ = location.getChunk().getZ();

        this.data = info.serializeData();
    }

    @Override
    public Long getId()
    {
        return this.id;
    }

    @Override
    public void setId(Long id)
    {
        this.id = id;
    }
}
