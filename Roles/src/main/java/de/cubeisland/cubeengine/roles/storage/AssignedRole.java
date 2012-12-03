package de.cubeisland.cubeengine.roles.storage;

import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.DatabaseConstructor;
import de.cubeisland.cubeengine.core.storage.database.Entity;
import de.cubeisland.cubeengine.core.storage.database.ForeignKey;
import de.cubeisland.cubeengine.core.storage.database.PrimaryKey;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import java.util.List;

@Entity(name = "roles")
public class AssignedRole implements Model<Integer>
{

    @PrimaryKey
    @Attribute(type = AttrType.INT, unsigned = true, ai = true)
    public int key;
    @ForeignKey(table = "user", field = "key")
    @Attribute(type = AttrType.INT, unsigned = true)
    public int userId;
    @ForeignKey(table = "worlds", field = "key")
    @Attribute(type = AttrType.INT, unsigned = true)
    public int worldId;
    @Attribute(type = AttrType.VARCHAR, length = 255)
    public String roleName;

    @DatabaseConstructor
    public AssignedRole(List<Object> args) throws ConversionException
    {
        this.key = Integer.valueOf(args.get(0).toString());
        this.userId = Integer.valueOf(args.get(1).toString());
        this.worldId = Integer.valueOf(args.get(2).toString());
        this.roleName = args.get(3).toString();
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
