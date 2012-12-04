package de.cubeisland.cubeengine.basics.moderation.kit;

import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.CompositeKey;
import de.cubeisland.cubeengine.core.storage.database.DatabaseConstructor;
import de.cubeisland.cubeengine.core.storage.database.Entity;
import de.cubeisland.cubeengine.core.storage.database.ForeignKey;
import java.util.List;

@Entity(
        name = "kitsgiven", 
        compositeKeys = @CompositeKey({"userId", "kitName"}))
public class KitsGiven implements Model<Integer>
{
    @ForeignKey(table = "user", field = "key")
    @Attribute(type = AttrType.INT, unsigned = true)
    public int userId;
    @Attribute(type = AttrType.VARCHAR, length = 50)
    public String kitName;
    @Attribute(type = AttrType.INT, unsigned = true)
    public int amount;

    @DatabaseConstructor
    public KitsGiven(List<Object> args)
    {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public Integer getKey()
    {
        throw new UnsupportedOperationException("Not supported!");
    }

    @Override
    public void setKey(Integer key)
    {
        throw new UnsupportedOperationException("Not supported!");
    }
}
