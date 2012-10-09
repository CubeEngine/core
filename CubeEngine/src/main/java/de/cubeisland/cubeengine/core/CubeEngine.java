package de.cubeisland.cubeengine.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cubeisland.cubeengine.core.bukkit.EventManager;
import de.cubeisland.cubeengine.core.bukkit.TaskManager;
import de.cubeisland.cubeengine.core.command.CommandManager;
import de.cubeisland.cubeengine.core.filesystem.FileManager;
import de.cubeisland.cubeengine.core.i18n.I18n;
import de.cubeisland.cubeengine.core.module.ModuleManager;
import de.cubeisland.cubeengine.core.permission.PermissionManager;
import de.cubeisland.cubeengine.core.storage.TableManager;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.user.UserManager;
import java.util.logging.Logger;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
/**
 *
 * @author Phillip Schichtel
 */
public final class CubeEngine
{
    private static Core core = null;

    /**
     * Standard Constructor
     */
    private CubeEngine()
    {
    }
    
    public static boolean isInitialized()
    {
        return core != null;
    }

    /**
     * Initializes CubeEngine
     *
     * @param coreInstance the Core
     */
    public static void initialize(Core coreInstance)
    {
        if (core == null)
        {
            if (coreInstance == null)
            {
                throw new IllegalArgumentException("The core must not be null!");
            }
            core = coreInstance;
        }
    }

    /**
     * Nulls the Core
     */
    public static void clean()
    {
        core = null;
    }

    /**
     * Returns the Core
     *
     * @return the Core
     */
    public static Core getCore()
    {
        return core;
    }

    /**
     * Returns the Database
     *
     * @return the Database
     */
    public static Database getDatabase()
    {
        return core.getDB();
    }

    /**
     * Returns the PermissionRegistration
     *
     * @return the PermissionRegistration
     */
    public static PermissionManager getPermissionRegistration()
    {
        return core.getPermissionManager();
    }

    /**
     * Returns the TableManager
     * 
     * @return the TableManager
     */
    public static TableManager getTableManager()
    {
        return core.getTableManger();
    }
    
    /**
     * Returns the UserManager
     *
     * @return the UserManager
     */
    public static UserManager getUserManager()
    {
        return core.getUserManager();
    }

    /**
     * Returns the FileManager
     *
     * @return the FileManager
     */
    public static FileManager getFileManager()
    {
        return core.getFileManager();
    }

    /**
     * Returns the Logger
     *
     * @return the Logger
     */
    public static Logger getLogger()
    {
        return core.getCoreLogger();
    }

    /**
     * Returns the ModuleManager
     *
     * @return the ModuleManager
     */
    public static ModuleManager getModuleManager()
    {
        return core.getModuleManager();
    }

    /**
     * Returns the EventManager
     *
     * @return the EventManager
     */
    public static EventManager getEventManager()
    {
        return core.getEventManager();
    }

    /**
     * Returns the CommandManager
     *
     * @return the CommandManager
     */
    public static CommandManager getCommandManager()
    {
        return core.getCommandManager();
    }
    
    /**
     * Returns the BukkitServer
     *
     * @return the BukkitServer
     */
    public static Server getServer()
    {
        return core.getServer();
    }
    
    public static I18n getI18n()
    {
        return core.getI18n();
    }
    
    /**
     * This method returns the Worker/ExecutorService
     * 
     * @return the ExecutorService
     */
    public static TaskManager getTaskManager()
    {
        return core.getTaskManager();
    }
    
    /**
     * Returns the OfflinePlayer
     * 
     * @param name the name of the player
     * @return the OfflinePlayer
     */
    public static OfflinePlayer getOfflinePlayer(String name)
    {
        return getServer().getOfflinePlayer(name);
    }

    public static CoreConfiguration getConfiguration()
    {
        return core.getConfiguration();
    }
    
    public static boolean isDebug()
    {
        return core.isDebug();
    }
    
    public static ObjectMapper getJsonObjectMapper()
    {
        return core.getJsonObjectMapper();
    }
}