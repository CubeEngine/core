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
package de.cubeisland.engine.core;

import java.nio.charset.Charset;

import de.cubeisland.engine.core.filesystem.FileManager;
import de.cubeisland.engine.core.i18n.I18n;
import de.cubeisland.engine.core.user.UserManager;
import de.cubeisland.engine.logscribe.Log;

/**
 * The CubeEngine provides static method to access all important Manager and the Core.
 */
public final class CubeEngine
{
    public static final Charset CHARSET = Charset.forName("UTF-8");
    private static Core core = null;
    private static Thread mainThread;

    /**
     * Standard Constructor
     */
    private CubeEngine()
    {}

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

    public static Thread getMainThread()
    {
        return mainThread;
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
     * Returns the Log
     *
     * @return the Log
     */
    public static Log getLog()
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
