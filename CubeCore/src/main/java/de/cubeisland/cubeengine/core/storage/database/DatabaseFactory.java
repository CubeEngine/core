package de.cubeisland.cubeengine.core.storage.database;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.storage.database.mysql.MySQLDatabase;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Level;

/**
 *
 * @author Anselm Brehme
 */
public class DatabaseFactory
{
    static
    {
        databases = new HashMap<String, Class<? extends Database>>();
        registerDatabase("mysql", MySQLDatabase.class);
    }
    public static HashMap<String, Class<? extends Database>> databases;

    public static Database loadDatabase(String name)
    {
        try
        {
            return databases.get(name.toLowerCase(Locale.ENGLISH)).newInstance();
        }
        catch (Exception e)
        {
            CubeEngine.getLogger().log(Level.SEVERE, "Couldn't establish the database connection: " + e.getLocalizedMessage(), e);
            return null;
        }
    }

    public static void registerDatabase(String name, Class<? extends Database> clazz)
    {
        databases.put(name.toLowerCase(Locale.ENGLISH), clazz);
    }
}
