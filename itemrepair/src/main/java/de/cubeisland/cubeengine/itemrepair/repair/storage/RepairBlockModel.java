package de.cubeisland.cubeengine.itemrepair.repair.storage;

import org.bukkit.Location;
import org.bukkit.block.Block;

import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Index;
import de.cubeisland.cubeengine.core.storage.database.SingleKeyEntity;
import de.cubeisland.cubeengine.core.storage.world.WorldManager;

import static de.cubeisland.cubeengine.core.storage.database.Index.IndexType.FOREIGN_KEY;
import static de.cubeisland.cubeengine.core.storage.database.Index.IndexType.UNIQUE;

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
