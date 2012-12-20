package de.cubeisland.cubeengine.core.util.converter.generic;

import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
        for (Object value : col)
        {
            result.add(Convert.toObject(value));
        }
        return result;
    }

    /**
     * Deserializes an object back to a collection
     *
     * @param <V>            the ValueType
     * @param <S>            the Type of collection
     * @param type          the type of the collection
     * @param object         the object to convert
     * @return the converted collection
     * @throws ConversionException
     */
    @SuppressWarnings("unchecked")
    public <V, S extends Collection<V>> S fromObject(ParameterizedType type, Object object) throws ConversionException
    {
        try
        {
            if (type.getRawType() instanceof Class)
            {
                Class<S> collectionType = (Class)type.getRawType();
                S result;
                Type subType = type.getActualTypeArguments()[0];
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
                        result = (S)new LinkedList<S>(); // other collection
                    }
                }
                else
                {
                    result = collectionType.newInstance();
                }
                if (object instanceof Collection)
                {
                    Collection col = (Collection)object;
                    for (Object o : col)
                    {
                        V value = Convert.fromObject(subType, o);
                        result.add(value);
                    }
                    return result;
                }
                else
                {
                    throw new IllegalStateException("Collection-conversion failed: Cannot convert not a collection to a collection.");
                }
            }
            throw new IllegalArgumentException("Unkown Collection-Type: " + type);
        }
        catch (IllegalAccessException ex)
        {
            throw new IllegalArgumentException("Collection-conversion failed: Could not access the default constructor of: " + type.getRawType(), ex);
        }
        catch (InstantiationException ex)
        {
            throw new IllegalArgumentException("Collection-conversion failed: Could not create an instance of: " + type.getRawType(), ex);
        }
        catch (ConversionException ex)
        {
            throw new IllegalStateException("Collection-conversion failed: Error while converting the values in the collection.", ex);
        }
    }
}
