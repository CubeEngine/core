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
package de.cubeisland.engine.itemrepair.repair.storage;

import org.bukkit.Location;
import org.bukkit.block.Block;

import de.cubeisland.engine.core.storage.Model;
import de.cubeisland.engine.core.storage.database.AttrType;
import de.cubeisland.engine.core.storage.database.Attribute;
import de.cubeisland.engine.core.storage.database.Index;
import de.cubeisland.engine.core.storage.database.SingleKeyEntity;
import de.cubeisland.engine.core.world.WorldManager;

import static de.cubeisland.engine.core.storage.database.Index.IndexType.FOREIGN_KEY;
import static de.cubeisland.engine.core.storage.database.Index.IndexType.UNIQUE;

@SingleKeyEntity(tableName = "repairblocks", primaryKey = "id", autoIncrement = true,
 indices = {
    @Index(value = FOREIGN_KEY, fields = "world", f_table = "worlds", f_field = "key"),
    @Index(value = UNIQUE, fields =
        {
            "world", "x", "y" , "z"
        })
})
public class RepairBlockModel implements Model<Long>
{
    @Attribute(type = AttrType.INT, unsigned = true)
    public long id;
    @Attribute(type = AttrType.INT, unsigned = true, notnull = false)
    public long world;
    @Attribute(type = AttrType.INT, notnull = false)
    public int x;
    @Attribute(type = AttrType.INT, notnull = false)
    public int y;
    @Attribute(type = AttrType.INT, notnull = false)
    public int z;
    @Attribute(type = AttrType.VARCHAR, length = 64, notnull = false)
    public String type;

    public RepairBlockModel()
    {
    }

    public RepairBlockModel(Block block, WorldManager worldManager)
    {
        this.world = worldManager.getWorldId(block.getWorld());
        this.x = block.getX();
        this.y = block.getY();
        this.z = block.getZ();
        this.type = block.getType().name();
    }

    @Override
    public Long getId()
    {
        return id;
    }

    @Override
    public void setId(Long aLong)
    {
        this.id = aLong;
    }

    public Block getBlock(WorldManager wm)
    {
        Location loc = new Location(wm.getWorld(world),x,y,z);
        return loc.getBlock();
    }

}
