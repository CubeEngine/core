package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.command.exception.InvalidArgumentException;
import de.cubeisland.cubeengine.core.command.readers.*;
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
        registerReader(new EntityTypeReader());
        registerReader(new DyeColorReader());
        registerReader(new ProfessionReader());
        registerReader(new OfflinePlayerReader());
    }

    private final Class<T> type;

    public ArgumentReader(Class<T> type)
    {
        this.type = type;
    }

    /**
     *
     * @param arg an string
     * @return the number of arguments paired with the value that got read from the input array
     */
    public abstract T read(String arg) throws InvalidArgumentException;

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
    public static <T> T read(Class<T> clazz, String string) throws InvalidArgumentException
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
        return reader.read(string);
    }
}
