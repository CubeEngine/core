package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.command.readers.EnchantmentReader;
import de.cubeisland.cubeengine.core.command.readers.FloatReader;
import de.cubeisland.cubeengine.core.command.readers.IntReader;
import de.cubeisland.cubeengine.core.command.readers.ItemStackReader;
import de.cubeisland.cubeengine.core.command.readers.LongReader;
import de.cubeisland.cubeengine.core.command.readers.StringReader;
import de.cubeisland.cubeengine.core.command.readers.UserReader;
import de.cubeisland.cubeengine.core.command.readers.WorldReader;
import de.cubeisland.cubeengine.core.util.Pair;
import org.apache.commons.lang.Validate;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ArgumentReader<T>
{
    private static final Map<Class, ArgumentReader> READERS = new ConcurrentHashMap<Class, ArgumentReader>();

    static
    {
        registerReader(new EnchantmentReader());
        registerReader(new FloatReader());
        registerReader(new IntReader());
        registerReader(new ItemStackReader());
        registerReader(new LongReader());
        registerReader(new StringReader());
        registerReader(new UserReader());
        registerReader(new WorldReader());
    }

    private final Class<T> type;

    public ArgumentReader(Class<T> type)
    {
        this.type = type;
    }

    /**
     *
     * @param args an string array of arguments
     * @return the number of arguments paired with the value that got read from the input array
     * @throws InvalidArgumentException
     */
    public abstract Pair<Integer, T> read(String... args) throws InvalidArgumentException;

    public Class<T> getType()
    {
        return this.type;
    }

    public static <T> void registerReader(ArgumentReader<T> reader)
    {
        Validate.notNull(reader, "Reader is null! Cannot register!");
        registerReader(reader.getType(), reader);
    }

    public static <T> void registerReader(Class clazz, ArgumentReader<T> reader)
    {
        READERS.put(clazz, reader);
    }

    public static void unregisterReader(Class clazz)
    {
        Iterator<Map.Entry<Class, ArgumentReader>> iter = READERS.entrySet().iterator();

        Map.Entry<Class, ArgumentReader> entry;
        while (iter.hasNext())
        {
            entry = iter.next();
            if (entry.getKey() == clazz || entry.getValue().getClass() == clazz)
            {
                iter.remove();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Pair<Integer, T> read(Class<T> clazz, String... strings) throws InvalidArgumentException
    {
        ArgumentReader<T> reader = READERS.get(clazz);
        if (reader == null)
        {
            for (Class argClazz : READERS.keySet())
            {
                if (clazz.isAssignableFrom(argClazz))
                {
                    reader = READERS.get(argClazz);
                    registerReader(clazz, reader);
                }
            }
        }
        if (reader == null)
        {
            throw new IllegalStateException("No reader found for " + clazz.getName() + "!");
        }
        return reader.read(strings);
    }
}
