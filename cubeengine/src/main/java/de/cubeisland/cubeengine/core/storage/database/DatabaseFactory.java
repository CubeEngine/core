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
package de.cubeisland.cubeengine.core.storage.database;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.storage.database.mysql.MySQLDatabaseConfiguration;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Creates new instance of database.
 */
public class DatabaseFactory
{
    private static final HashMap<String, Class<? extends DatabaseConfiguration>> databases = new HashMap<String, Class<? extends DatabaseConfiguration>>();

    static
    {
        registerDatabase("mysql", MySQLDatabaseConfiguration.class);
    }

    /**
     * Creates a new database.
     *
     * @param name       the databaseType
     * @param configFile the configurationFile for the database
     * @return the prepared database
     */
    public static Database loadDatabase(String name, File configFile)
    {
        assert name != null: "The name must not be null!";
        assert configFile != null: "The config file must not be null!";

        Class<? extends DatabaseConfiguration> configClazz = databases.get(name.toLowerCase(Locale.ENGLISH));
        if (configClazz != null)
        {
            try
            {
                DatabaseConfiguration config = Configuration.load(configClazz, configFile);
                return config.getDatabaseClass().getConstructor(DatabaseConfiguration.class).newInstance(config);
            }
            catch (Exception e)
            {
                Throwable t = e;
                if (t instanceof InvocationTargetException)
                {
                    t = e.getCause();
                }
                org.slf4j.Logger logger = CubeEngine.getLog();
                logger.error("Couldn't establish the database dataSource: " + t.getLocalizedMessage(), t);
            }
        }
        return null;
    }

    public static void registerDatabase(String name, Class<? extends DatabaseConfiguration> clazz)
    {
        databases.put(name.toLowerCase(Locale.ENGLISH), clazz);
    }
}
