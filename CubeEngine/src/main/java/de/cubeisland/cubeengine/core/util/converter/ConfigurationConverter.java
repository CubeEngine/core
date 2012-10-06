package de.cubeisland.cubeengine.core.util.converter;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.InvalidConfigurationException;
import java.util.Map;

/**
 *
 * @author Anselm Brehme
 */
public class ConfigurationConverter<T extends Configuration> implements Converter<T>
{
    @Override
    public T toObject(T object) throws ConversionException
    {
        throw new UnsupportedOperationException("The ConfigurationConverter needs the basepath!");
    }

    public Object toObject(T object, String basepath) throws ConversionException
    {
        try
        {
            if (object.getCodec() == null)
            {
                throw new InvalidConfigurationException("No Codec given for a SubConfiguration!");
            }
            return object.getCodec().saveIntoMap(object, basepath);
        }
        catch (Exception e)
        {
            throw new ConversionException("Error while Converting SubConfiguration!", e);
        }
    }

    @Override
    public T fromObject(Object object) throws ConversionException
    {
        throw new UnsupportedOperationException("The ConfigurationConverter needs an ConfigurationObject!");
    }

    public T fromObject(Object object, T config) throws ConversionException
    {
        config.getCodec().loadIntoFields(config, (Map)object);
        return config;
    }

    @Override
    public String toString(T object)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public T fromString(String string) throws ConversionException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
