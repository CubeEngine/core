package de.cubeisland.cubeengine;

import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.i18n.I18n;
import de.cubeisland.cubeengine.core.module.ModuleManager;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.permission.PermissionRegistration;
import de.cubeisland.cubeengine.core.persistence.database.Database;
import de.cubeisland.cubeengine.core.persistence.filesystem.FileManager;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import java.util.logging.Logger;

/**
 *
 * @author CubeIsland-Dev
 */
public final class CubeEngine
{
    private static CubeCore core = null;

    private CubeEngine()
    {}

    public static void initialize(CubeCore coreModule)
    {
        if (core == null)
        {
            if (coreModule == null)
            {
                throw new IllegalArgumentException("The core module must not be null!");
            }
            core = coreModule;
        }
    }

    public static void clean()
    {
        core = null;
    }

    public static CubeCore getCore()
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

    public static void registerPermissions(Permission[] permissions)
    {
        getPermissionRegistration().registerPermissions(permissions);
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

    public static String _(User user, String category, String text, Object... params)
    {
        return _(user.getLanguage(), category, text, params);
    }

    public static String _(User user, String text, Object... params)
    {
        final String className = Thread.currentThread().getStackTrace()[2].getClassName();
        return _(user.getLanguage(), className.substring(25, className.indexOf(".", 26)), text, params);
    }

    public static String _(String category, String text, Object... params)
    {
        return _(I18n.SOURCE_LANGUAGE, category, text, params);
    }

    public static String _(String language, String category, String text, Object... params)
    {
        return core.getI18n().translate(language, category, language, params);
    }
}
