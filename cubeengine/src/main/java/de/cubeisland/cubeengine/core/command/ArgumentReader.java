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
package de.cubeisland.cubeengine.core.command;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.DyeColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.command.exception.InvalidArgumentException;
import de.cubeisland.cubeengine.core.command.readers.BooleanReader;
import de.cubeisland.cubeengine.core.command.readers.ByteReader;
import de.cubeisland.cubeengine.core.command.readers.DoubleReader;
import de.cubeisland.cubeengine.core.command.readers.DyeColorReader;
import de.cubeisland.cubeengine.core.command.readers.EnchantmentReader;
import de.cubeisland.cubeengine.core.command.readers.EntityTypeReader;
import de.cubeisland.cubeengine.core.command.readers.FloatReader;
import de.cubeisland.cubeengine.core.command.readers.IntReader;
import de.cubeisland.cubeengine.core.command.readers.ItemStackReader;
import de.cubeisland.cubeengine.core.command.readers.LongReader;
import de.cubeisland.cubeengine.core.command.readers.OfflinePlayerReader;
import de.cubeisland.cubeengine.core.command.readers.ProfessionReader;
import de.cubeisland.cubeengine.core.command.readers.ShortReader;
import de.cubeisland.cubeengine.core.command.readers.StringReader;
import de.cubeisland.cubeengine.core.command.readers.UserReader;
import de.cubeisland.cubeengine.core.command.readers.WorldReader;
import de.cubeisland.cubeengine.core.user.User;

public abstract class ArgumentReader
{
    private static final Map<Class<?>, ArgumentReader> READERS = new ConcurrentHashMap<Class<?>, ArgumentReader>();

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
    }

    public static void registerReader(ArgumentReader reader, Class<?>... classes)
    {
        assert classes.length > 0: "At least one class must be specified!";

        for (Class c : classes)
        {
            READERS.put(c, reader);
        }
    }

    public static void removeReader(Class clazz)
    {
        Iterator<Map.Entry<Class<?>, ArgumentReader>> iter = READERS.entrySet().iterator();

        Map.Entry<Class<?>, ArgumentReader> entry;
        while (iter.hasNext())
        {
            entry = iter.next();
            if (entry.getKey() == clazz || entry.getValue().getClass() == clazz)
            {
                iter.remove();
            }
        }
    }

    public static <T> T read(Class<T> clazz, String string, CommandSender sender) throws InvalidArgumentException
    {
        return read(clazz, string, sender.getLocale());
    }

    @SuppressWarnings("unchecked")
    public static <T> T read(Class<T> clazz, String string, Locale locale) throws InvalidArgumentException
    {
        ArgumentReader reader = READERS.get(clazz);
        if (reader == null)
        {
            for (Class argClazz : READERS.keySet())
            {
                if (clazz.isAssignableFrom(argClazz))
                {
                    reader = READERS.get(argClazz);
                    registerReader(reader, clazz);
                }
            }
        }
        if (reader == null)
        {
            throw new IllegalStateException("No reader found for " + clazz.getName() + "!");
        }
        return (T)reader.read(string, locale);
    }
}
