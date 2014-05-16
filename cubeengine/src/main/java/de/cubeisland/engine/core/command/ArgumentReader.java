/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.core.command;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Difficulty;
import org.bukkit.DyeColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.command.exception.InvalidArgumentException;
import de.cubeisland.engine.core.command.readers.BooleanReader;
import de.cubeisland.engine.core.command.readers.ByteReader;
import de.cubeisland.engine.core.command.readers.DifficultyReader;
import de.cubeisland.engine.core.command.readers.DoubleReader;
import de.cubeisland.engine.core.command.readers.DyeColorReader;
import de.cubeisland.engine.core.command.readers.EnchantmentReader;
import de.cubeisland.engine.core.command.readers.EntityTypeReader;
import de.cubeisland.engine.core.command.readers.EnvironmentReader;
import de.cubeisland.engine.core.command.readers.FloatReader;
import de.cubeisland.engine.core.command.readers.IntReader;
import de.cubeisland.engine.core.command.readers.IntegerOrAllReader;
import de.cubeisland.engine.core.command.readers.ItemStackReader;
import de.cubeisland.engine.core.command.readers.LogLevelReader;
import de.cubeisland.engine.core.command.readers.LongReader;
import de.cubeisland.engine.core.command.readers.OfflinePlayerReader;
import de.cubeisland.engine.core.command.readers.ProfessionReader;
import de.cubeisland.engine.core.command.readers.ShortReader;
import de.cubeisland.engine.core.command.readers.StringReader;
import de.cubeisland.engine.core.command.readers.UserListOrAllReader;
import de.cubeisland.engine.core.command.readers.UserListReader;
import de.cubeisland.engine.core.command.readers.UserOrAllReader;
import de.cubeisland.engine.core.command.readers.UserReader;
import de.cubeisland.engine.core.command.readers.WorldReader;
import de.cubeisland.engine.core.command.readers.WorldTypeReader;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.logging.LogLevel;

public abstract class ArgumentReader
{
    // TODO unregister reader from a module!!! else memory leakage
    private static final Map<Class<?>, ArgumentReader> READERS = new ConcurrentHashMap<>();

    /**
     *
     *
     * @param arg an string
     * @param locale
     * @return the number of arguments paired with the value that got read from the input array
     */
    public abstract Object read(String arg, Locale locale) throws InvalidArgumentException;

    public static void init(Core core)
    {
        registerReader(new BooleanReader(core), Boolean.class, boolean.class);
        registerReader(new ByteReader(), Byte.class, byte.class);
        registerReader(new ShortReader(), Short.class, short.class);
        registerReader(new IntReader(), Integer.class, int.class);
        registerReader(new LongReader(), Long.class, long.class);
        registerReader(new FloatReader(), Float.class, float.class);
        registerReader(new DoubleReader(), Double.class, double.class);
        registerReader(new StringReader(), String.class);
        registerReader(new EnchantmentReader(), Enchantment.class);
        registerReader(new ItemStackReader(), ItemStack.class);
        registerReader(new UserReader(core), User.class);
        registerReader(new WorldReader(core), World.class);
        registerReader(new EntityTypeReader(), EntityType.class);
        registerReader(new DyeColorReader(), DyeColor.class);
        registerReader(new ProfessionReader(), Profession.class);
        registerReader(new OfflinePlayerReader(core), OfflinePlayer.class);
        registerReader(new EnvironmentReader(), Environment.class);
        registerReader(new WorldTypeReader(), WorldType.class);
        registerReader(new DifficultyReader(), Difficulty.class);
        registerReader(new LogLevelReader(), LogLevel.class);

        registerReader(new UserOrAllReader()); // "*" or User.class
        registerReader(new UserListReader()); // "*" or Integer.class
        registerReader(new UserListOrAllReader()); // "*" or Integer.class
        registerReader(new IntegerOrAllReader()); // "*" or Integer.class
    }

    public static void registerReader(ArgumentReader reader, Class<?>... classes)
    {
        for (Class c : classes)
        {
            READERS.put(c, reader);
        }
        READERS.put(reader.getClass(), reader);
    }

    public static ArgumentReader getReader(Class<?> type)
    {
        return READERS.get(type);
    }

    public static ArgumentReader resolveReader(Class<?> type)
    {
        ArgumentReader reader = getReader(type);
        if (reader == null)
        {
            Class<?> next;
            Iterator<Class<?>> it = READERS.keySet().iterator();
            while (it.hasNext())
            {
                next = it.next();
                if (type.isAssignableFrom(next))
                {
                    reader = READERS.get(next);
                    if (reader != null)
                    {
                        registerReader(reader, type);
                        break;
                    }
                }
            }
        }
        return reader;
    }

    public static boolean hasReader(Class<?> type)
    {
        return resolveReader(type) != null;
    }

    public static void removeReader(Class type)
    {
        Iterator<Map.Entry<Class<?>, ArgumentReader>> it = READERS.entrySet().iterator();

        Map.Entry<Class<?>, ArgumentReader> entry;
        while (it.hasNext())
        {
            entry = it.next();
            if (entry.getKey() == type || entry.getValue().getClass() == type)
            {
                it.remove();
            }
        }
    }

    public static <T> T read(Class<T> clazz, String string, CommandSender sender) throws InvalidArgumentException
    {
        return read(clazz, string, sender.getLocale());
    }

    @SuppressWarnings("unchecked")
    public static <T> T read(Class<T> type, String string, Locale locale) throws InvalidArgumentException
    {
        ArgumentReader reader = resolveReader(type);
        if (reader == null)
        {
            throw new IllegalStateException("No reader found for " + type.getName() + "!");
        }
        return (T)reader.read(string, locale);
    }
}
