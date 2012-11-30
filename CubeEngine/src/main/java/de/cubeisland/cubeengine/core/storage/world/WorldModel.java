package de.cubeisland.cubeengine.core.storage.world;

import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.DatabaseConstructor;
import de.cubeisland.cubeengine.core.storage.database.Entity;
import de.cubeisland.cubeengine.core.storage.database.PrimaryKey;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import java.util.List;
import org.bukkit.World;

@Entity(name = "worlds")
public class WorldModel implements Model<Integer>
{
    @PrimaryKey
    @Attribute(type = AttrType.INT, unsigned = true, ai = true)
    public int key = -1;
    @Attribute(type = AttrType.VARCHAR, length = 64, notnull = false)
    public String worldName;
    @Attribute(type = AttrType.VARCHAR, length = 64, notnull = false)
    public String worldUUID;

    @DatabaseConstructor
    public WorldModel(List<Object> args) throws ConversionException
    {
        this.key = Integer.valueOf(args.get(0).toString());
        this.worldName = args.get(1).toString();
        this.worldUUID = args.get(2).toString();
    }

    public WorldModel(World world)
    {
        this.worldName = world.getName();
        this.worldUUID = world.getUID().toString();
    }

    @Override
    public Integer getKey()
    {
        return this.key;
    }

    @Override
    public void setKey(Integer key)
    {
        this.key = key;
    }
}
