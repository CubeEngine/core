package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.command.args.*;
import de.cubeisland.cubeengine.core.util.Pair;
import gnu.trove.map.hash.THashMap;
import org.apache.commons.lang.Validate;

public class ArgumentReaderManager
{
    private static THashMap<Class<?>, ArgumentReader> argReaders = new THashMap<Class<?>, ArgumentReader>();

    static
    {
        registerArgumentReader(new EnchantmentArg());
        registerArgumentReader(new FloatArg());
        registerArgumentReader(new IntArg());
        registerArgumentReader(new ItemStackArg());
        registerArgumentReader(new LongArg());
        registerArgumentReader(new StringArg());
        registerArgumentReader(new UserArg());
        registerArgumentReader(new WorldArg());
    }

    public static <T> void registerArgumentReader(ArgumentReader<T> reader)
    {
        Validate.notNull(reader, "Reader is null! Cannot register!");
        registerArgumentReader(reader.getType(), reader);
    }

    private static <T> void registerArgumentReader(Class<?> clazz, ArgumentReader<T> reader)
    {
        argReaders.put(clazz, reader);
    }

    public static <T> Pair<Integer, T> read(Class<T> clazz, String... strings) throws InvalidArgumentException
    {
        ArgumentReader<T> reader = argReaders.get(clazz);
        if (reader == null)
        {
            for (Class argClazz : argReaders.keySet())
            {
                if (clazz.isAssignableFrom(argClazz))
                {
                    reader = argReaders.get(argClazz);
                    registerArgumentReader(clazz, reader);
                }
            }
        }
        if (reader == null)
        {
            throw new IllegalStateException("ArgumentReader for " + clazz.getCanonicalName() + " not found!");
        }
        return reader.read(strings);
    }
}
