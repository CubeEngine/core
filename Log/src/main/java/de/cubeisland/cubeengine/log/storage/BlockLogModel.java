package de.cubeisland.cubeengine.log.storage;

import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Index;
import de.cubeisland.cubeengine.core.storage.database.SingleKeyEntity;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import java.util.logging.Level;
import java.util.logging.Logger;

@SingleKeyEntity(tableName = "blocklogs", primaryKey = "key", autoIncrement = false, indices =
@Index(value = Index.IndexType.FOREIGN_KEY, fields = "key", f_field = "key", f_table = "logs"))
public class BlockLogModel implements Model<Long>
{
    @Attribute(type = AttrType.INT, unsigned = true)
    public Long key;
    @Attribute(type = AttrType.VARCHAR, length = 20, notnull = false)
    public String oldData;
    @Attribute(type = AttrType.VARCHAR, length = 20, notnull = false)
    public String newData;

    public BlockLogModel(Long key, BlockData oldBlockData, BlockData newBlockData)
    {
        try
        {
            this.key = key;
            this.oldData = (String) Convert.toObject(oldBlockData);
            this.newData = (String) Convert.toObject(newBlockData);
        }
        catch (ConversionException ignored)
        {}
    }

    @Override
    public Long getKey()
    {
        return this.key;
    }

    @Override
    public void setKey(Long key)
    {
        throw new UnsupportedOperationException("Not supported.");
    }
}
