package de.cubeisland.cubeengine.core.util.converter.generic;

import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import java.lang.reflect.Modifier;
import java.util.*;

public class CollectionConverter
{
    /**
     * Returns the converted collection
     *
     * @param col the collection to convert
     * @return the converted collection
     * @throws ConversionException
     */
    public Object toObject(Collection col) throws ConversionException
    {
        Collection<Object> result = new LinkedList<Object>();
        if (col.isEmpty())
        {
            return result;
        }
        Converter valConverter = Convert.matchConverter(col.iterator().next().getClass());
        if (valConverter == null)
        {
            throw new IllegalStateException("Converter not found for: " + col.iterator().next().getClass().getCanonicalName());
        }
        for (Object value : col)
        {
            result.add(valConverter.toObject(value));
        }
        return result;
    }

    /**
     * Deserializes an object back to a collection
     *
     * @param <V>            the ValueType
     * @param <S>            the Type of collection
     * @param collectionType the CollectionClass
     * @param object         the object to convert
     * @param valueType      the ValueTypeClass
     * @return the converted collection
     * @throws ConversionException
     */
    public <V, S extends Collection<V>> S fromObject(Class<S> collectionType, Object object, Class<V> valueType) throws ConversionException
    {
        try
        {
            S result;
            if (collectionType.isInterface() || Modifier.isAbstract(collectionType.getModifiers()))
            {
                if (Set.class.isAssignableFrom(collectionType))
                {
                    if (SortedSet.class.isAssignableFrom(collectionType))
                    {
                        result = (S)new TreeSet<V>();
                    }
                    else
                    {
                        result = (S)new HashSet<V>();
                    }
                }
                else if (List.class.isAssignableFrom(collectionType))
                {
                    result = (S)new LinkedList<S>();
                }
                else
                {
                    result = (S)new LinkedList<S>(); // other collections ??
                }
            }
            else
            {
                result = collectionType.newInstance();
            }
            if (object instanceof Collection)
            {
                Converter<V> valConverter = Convert.matchConverter(valueType);
                Collection col = (Collection)object;
                for (Object o : col)
                {
                    result.add(valConverter.fromObject(o));
                }
                return result;
            }
            else
            {
                throw new IllegalStateException("Collection-conversion failed: Cannot convert not a collection to a collection.");
            }
        }
        catch (IllegalAccessException ex)
        {
            throw new IllegalArgumentException("Collection-conversion failed: Could not access the default constructor of: " + collectionType.getCanonicalName(), ex);
        }
        catch (InstantiationException ex)
        {
            throw new IllegalArgumentException("Collection-conversion failed: Could not create an instance of: " + collectionType.getCanonicalName(), ex);
        }
        catch (ConversionException ex)
        {
            throw new IllegalStateException("Collection-conversion failed: Error while converting the values in the collection.", ex);
        }
    }
}