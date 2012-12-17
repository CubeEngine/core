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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class ArgumentReader<T>
{
    private static final ConcurrentMap<Class<?>, ArgumentReader> readers = new ConcurrentHashMap<Class<?>, ArgumentReader>();

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

    public static <T> void registerReader(Class<?> clazz, ArgumentReader<T> reader)
    {
        readers.put(clazz, reader);
    }

    @SuppressWarnings("unchecked")
    public static <T> Pair<Integer, T> read(Class<T> clazz, String... strings) throws InvalidArgumentException
    {
        ArgumentReader<T> reader = readers.get(clazz);
        if (reader == null)
        {
            for (Class argClazz : readers.keySet())
            {
                if (clazz.isAssignableFrom(argClazz))
                {
                    reader = readers.get(argClazz);
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
