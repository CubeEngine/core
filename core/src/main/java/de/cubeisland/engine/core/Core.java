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

import de.cubeisland.engine.core.ban.BanManager;
import de.cubeisland.engine.core.bukkit.EventManager;
import de.cubeisland.engine.core.command.CommandManager;
import de.cubeisland.engine.core.filesystem.FileManager;
import de.cubeisland.engine.core.i18n.I18n;
import de.cubeisland.engine.core.logging.LogFactory;
import de.cubeisland.engine.core.module.ModuleManager;
import de.cubeisland.engine.core.permission.PermissionManager;
import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.task.TaskManager;
import de.cubeisland.engine.core.user.UserManager;
import de.cubeisland.engine.core.util.InventoryGuardFactory;
import de.cubeisland.engine.core.util.Version;
import de.cubeisland.engine.core.util.matcher.Match;
import de.cubeisland.engine.core.webapi.ApiServer;
import de.cubeisland.engine.core.world.WorldManager;
import de.cubeisland.engine.logging.Log;
import de.cubeisland.engine.reflect.Reflector;

/**
 * This interface specifies all the methods the core of the CubeEngine has to provide.
 * The core provides essential APIs like the I18n API and all the managers like
 * the CommandManager or the TaskManager
 */
public interface Core
{
    /**
     * Returns the version of the core
     *
     * @return the version
     */
    Version getVersion();

    /**
     * Returns the source version used to build the core
     *
     * @return the source version
     */
    String getSourceVersion();

    /**
     * The method returns the database
     *
     * @return the database instance
     */
    Database getDB();

    /**
     * The method returns the permission registration
     *
     * @return an instance of a permission registration
     */
    PermissionManager getPermissionManager();

    /**
     * The method returns the event manager
     *
     * @return the instance of the event manager
     */
    EventManager getEventManager();

    /**
     * This method returns the user manager
     *
     * @return the instance of the user manager
     */
    UserManager getUserManager();

    /**
     * This method returns the file manager
     *
     * @return the instance of the file manager
     */
    FileManager getFileManager();

    /**
     * This method returns the engine logging
     *
     * @return the engine logging
     */
    Log getLog();

    /**
     * This method returns the module manager
     *
     * @return the instance of the module manager
     */
    ModuleManager getModuleManager();

    /**
     * This method returns the internationalization API
     *
     * @return the I18n API
     */
    I18n getI18n();

    /**
     * This method returns the engine configuration
     *
     * @return the engine configuration
     */
    CoreConfiguration getConfiguration();

    /**
     * This method returns the command manager
     *
     * @return the instance of the command manager
     */
    CommandManager getCommandManager();

    /**
     * This method returns the TaskManager
     *
     * @return the TaskManager
     */
    TaskManager getTaskManager();

    /**
     * This method returns the web API server
     *
     * @return the API server
     */
    ApiServer getApiServer();

    /**
     * This method returns the WorldManager
     *
     * @return the world manager
     */
    WorldManager getWorldManager();

    /**
     * This method returns the MatcherManager containing all matchers
     *
     * @return the global matcher manager
     */
    Match getMatcherManager();

    /**
     * Returns the inventory guard to protect inventories
     *
     * @return the global inventory guard instance
     */
    InventoryGuardFactory getInventoryGuard();

    /**
     * Returns the ban-manager
     *
     * @return the ban-manager
     */
    BanManager getBanManager();

    LogFactory getLogFactory();

    Reflector getConfigFactory();
}
