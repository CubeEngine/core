package de.cubeisland.cubeengine.log.storage;

import de.cubeisland.cubeengine.core.util.converter.ConversionException;
import de.cubeisland.cubeengine.core.util.converter.Converter;
import org.bukkit.Material;

public class BlockDataConverter implements Converter<BlockData>
{
    public Object toObject(BlockData object) throws ConversionException
    {
        return this.toString(object);
    }

    public BlockData fromObject(Object object) throws ConversionException
    {
        if (object instanceof String)
        {
            return fromString((String)object);
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String toString(BlockData object)
    {
        if (object.data == 0)
        {
            return String.valueOf(object.mat.getId());
        }
        return object.mat.getId() + ":" + object.data;
    }

    public BlockData fromString(String string) throws ConversionException
    {
        if (string == null)
        {
            return null;
        }
        Material mat;
        Byte data;
        if (!string.contains(":"))
        {
            mat = Material.matchMaterial(string);
            data = 0;
        }
        else
        {
            mat = Material.matchMaterial(string.substring(0, string.indexOf(":")));
            data = Byte.parseByte(string.substring(string.indexOf(":")));
        }
        if (mat == null)
        {
            return null;
        }
        return new BlockData(mat, data);
    }
}
