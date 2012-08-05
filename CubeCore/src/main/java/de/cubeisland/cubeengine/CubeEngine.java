package de.cubeisland.cubeengine;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.command.CommandManager;
import de.cubeisland.cubeengine.core.event.EventListener;
import de.cubeisland.cubeengine.core.event.EventManager;
import de.cubeisland.cubeengine.core.i18n.I18n;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.module.ModuleManager;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.permission.PermissionRegistration;
import de.cubeisland.cubeengine.core.persistence.database.Database;
import de.cubeisland.cubeengine.core.persistence.filesystem.FileManager;
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

    private CubeEngine()
    {}

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

    public static void clean()
    {
        core = null;
    }

    public static Core getCore()
    {
        return core;
    }

    public static Database getDatabase()
    {
        return core.getDB();
    }

    public static PermissionRegistration getPermissionRegistration()
    {
        return core.getPermissionRegistration();
    }

    public static UserManager getUserManager()
    {
        return core.getUserManager();
    }

    public static FileManager getFileManager()
    {
        return core.getFileManager();
    }

    public static Logger getLogger()
    {
        return core.getCoreLogger();
    }

    public static ModuleManager getModuleManager()
    {
        return core.getModuleManager();
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

    public static EventManager getEventManager()
    {
        return core.getEventManager();
    }

    public static CommandManager getCommandManager()
    {
        return core.getCommandManager();
    }
}
