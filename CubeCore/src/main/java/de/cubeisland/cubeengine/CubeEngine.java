package de.cubeisland.cubeengine;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.command.CommandManager;
import de.cubeisland.cubeengine.core.event.EventManager;
import de.cubeisland.cubeengine.core.filesystem.FileManager;
import de.cubeisland.cubeengine.core.module.ModuleManager;
import de.cubeisland.cubeengine.core.permission.PermissionRegistration;
import de.cubeisland.cubeengine.core.storage.database.mysql.MySQLDatabase;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import java.util.logging.Logger;
import org.bukkit.command.CommandSender;

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
    public static MySQLDatabase getDatabase()
    {
        return core.getDB();
    }

    /**
     * Returns the PermissionRegistration
     *
     * @return the PermissionRegistration
     */
    public static PermissionRegistration getPermissionRegistration()
    {
        return core.getPermissionRegistration();
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

    @BukkitDependend("Uses Bukkit's CommandSender")
    public static String _(CommandSender sender, String category, String text, Object... params)
    {
        if (sender instanceof User)
        {
            return _((User)sender, category, text, params);
        }
        return _(category, text, params);
    }

    public static String _(User user, String category, String text, Object... params)
    {
        return _(user.getLanguage(), category, text, params);
    }

    public static String _(String category, String text, Object... params)
    {
        return _(core.getI18n().getDefaultLanguage(), category, text, params);
    }

    public static String _(String language, String category, String text, Object... params)
    {
        return core.getI18n().translate(language, category, language, params);
    }
}