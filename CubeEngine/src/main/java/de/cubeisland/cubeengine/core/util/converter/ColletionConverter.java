package de.cubeisland.cubeengine.core.util.converter;

import java.util.Collection;
import java.util.LinkedList;

/**
 *
 * @author Anselm Brehme
 */
public class ColletionConverter implements GenericConverter<Collection>
{
    @Override
    public Object toObject(Collection object, Class<?> genericType) throws ConversionException
    {
        Converter converter = Convert.matchConverter(genericType);
        if (converter != null)
        {
            Collection<?> collection = (Collection<?>)object;
            Collection<Object> result = new LinkedList<Object>();
            for (Object o : collection)
            {
                result.add(converter.toObject(o));
            }
            return result;
        }
        return object;//No Converter for GenericType -> is already a Collection
    }

    @Override
    public <G> Collection fromObject(Object object, Class<G> genericType) throws ConversionException
    {
        Converter converter = Convert.matchConverter(genericType);

        if (converter != null)
        {
            Collection<?> list = (Collection<?>)object;
            if (list.isEmpty())
            {
                return (Collection)object;
            }
            Collection<G> result = new LinkedList<G>();
            for (Object o : list)
            {
                result.add((G)converter.fromObject(o));
            }
            return (Collection<G>)result;
        }
        return (Collection)object;//No Converter for GenericType -> is already a Collection
    }

}
