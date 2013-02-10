package de.cubeisland.cubeengine.signmarket.storage;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Index;
import de.cubeisland.cubeengine.core.storage.database.SingleKeyEntity;
import org.bukkit.Location;

@SingleKeyEntity(autoIncrement = true, primaryKey = "key", tableName = "signmarketblocks",
indices = {
        @Index(value = Index.IndexType.FOREIGN_KEY, fields = "world", f_field = "key", f_table = "worlds", onDelete = "CASCADE"),
        @Index(value = Index.IndexType.INDEX, fields = {"x","y","z"})
})
public class SignMarketBlockModel implements Model<Long>
{
    @Attribute(type = AttrType.INT, unsigned = true)
    public long key;
    @Attribute(type = AttrType.INT, unsigned = true)
    public long world;
    @Attribute(type = AttrType.INT)
    public int x;
    @Attribute(type = AttrType.INT)
    public int y;
    @Attribute(type = AttrType.INT)
    public int z;

    public SignMarketBlockModel(Long marketSignId, Location location)
    {
        this.key = marketSignId;
        this.world = CubeEngine.getCore().getWorldManager().getWorldId(location.getWorld());
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
    }

    @Override
    public Long getKey() {
        return key;
    }

    @Override
    public void setKey(Long key)
    {
        this.key = key;
    }

    public SignMarketBlockModel() {
    }
}
