package de.cubeisland.cubeengine.core;

import java.io.File;
import java.util.logging.Logger;

import de.cubeisland.cubeengine.core.filesystem.FileManager;
import de.cubeisland.cubeengine.core.i18n.I18n;
import de.cubeisland.cubeengine.core.user.UserManager;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The CubeEngine provides static method to access all important Manager and the Core.
 */
public final class CubeEngine
{
    private static final boolean WINDOWS = File.separatorChar == '\\' && File.pathSeparatorChar == ';';
    private static Core core = null;
    private static Thread mainThread;

    /**
     * Standard Constructor
     */
    private CubeEngine()
    {}

    public static boolean runsOnWindows()
    {
        return WINDOWS;
    }

    /**
     * Checks whether the CubeEngine class has been initialized.
     *
     * @return true if the class is initialized
     */
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
        if (!isInitialized())
        {
            if (coreInstance == null)
            {
                throw new IllegalArgumentException("The core must not be null!");
            }
            core = coreInstance;
            mainThread = Thread.currentThread();
        }
    }

    /**
     * Nulls the Core
     */
    public static void clean()
    {
        core = null;
        mainThread = null;
    }

    public static boolean isMainThread()
    {
        return mainThread == Thread.currentThread();
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
    public static Logger getLog()
    {
        return core.getLog();
    }

    /**
     * Returns the I18n API
     *
     * @return the I18 API
     */
    public static I18n getI18n()
    {
        return core.getI18n();
    }

    /**
     * Returns the core configuration.
     *
     * @return the core configuration
     */
    public static CoreConfiguration getConfiguration()
    {
        return core.getConfiguration();
    }
}
