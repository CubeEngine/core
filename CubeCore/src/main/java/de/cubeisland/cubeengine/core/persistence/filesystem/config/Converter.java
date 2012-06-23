package de.cubeisland.cubeengine.core.persistence.filesystem.config;

import de.cubeisland.cubeengine.core.persistence.filesystem.config.converter.ByteConverter;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.converter.IConverter;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.converter.LocationConverter;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.converter.PlayerConverter;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.converter.ShortConverter;
import java.lang.reflect.Field;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

/**
 *
 * @author Faithcaio
 */
public class Converter
{
    public static Object convertTo(Field field, Object configElem)
    {
        Class clazz = field.getType();
        IConverter converter = null;
        if (clazz.equals(Short.class) || clazz.equals(short.class))
        {
            converter = new ShortConverter();
        }
        else if (clazz.equals(Byte.class) || clazz.equals(byte.class))
        {
            converter = new ByteConverter();
        }
        else if (clazz.equals(OfflinePlayer.class))
        {
            converter = new PlayerConverter();
        }
        else if (clazz.equals(Location.class))
        {
            converter = new LocationConverter();
        }

        if (converter == null)
        {
            return configElem;
        }
        return converter.to(configElem);
    }

    public static Object convertFrom(Object object)
    {
        IConverter converter = null;
        if (object instanceof Short)
        {
            converter = new ShortConverter();
        }
        else if (object instanceof Byte)
        {
            converter = new ByteConverter();
        }
        else if (object instanceof OfflinePlayer)
        {
            converter = new PlayerConverter();
        }
        else if (object instanceof Location)
        {
            converter = new LocationConverter();
        }

        if (converter == null)
        {
            return object;
        }
        return converter.from(object);
    }
}
