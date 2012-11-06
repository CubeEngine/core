package de.cubeisland.cubeengine.log.logger.blockchange;

import de.cubeisland.cubeengine.core.util.converter.ConversionException;
import de.cubeisland.cubeengine.core.util.converter.Converter;

public class ItemDataConverter implements Converter<ItemData>
{
    public Object toObject(ItemData object) throws ConversionException
    {
        return this.toString(object);
    }

    public ItemData fromObject(Object object) throws ConversionException
    {
        if (object instanceof String)
        {
            return fromString((String)object);
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String toString(ItemData object)
    {
        if (object.data == 0)
        {
            return String.valueOf(object.mat);
        }
        return object.mat + ":" + object.data;
    }

    public ItemData fromString(String string) throws ConversionException
    {
        if (string == null)
        {
            return null;
        }
        int mat;
        Short data;
        if (string.contains(":"))
        {
            mat = Integer.parseInt(string.substring(0, string.indexOf(":")));
            data = 0;
        }
        else
        {
            mat = Integer.parseInt(string.substring(0, string.indexOf(":")));
            data = Short.parseShort(string.substring(string.indexOf(":")));
        }
        return new ItemData(mat, data);
    }
}
