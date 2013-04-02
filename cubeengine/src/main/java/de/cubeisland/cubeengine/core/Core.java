package de.cubeisland.cubeengine.core;

import java.nio.charset.Charset;
import java.util.logging.Logger;

import de.cubeisland.cubeengine.core.ban.BanManager;
import de.cubeisland.cubeengine.core.bukkit.EventManager;
import de.cubeisland.cubeengine.core.bukkit.TaskManager;
import de.cubeisland.cubeengine.core.command.CommandManager;
import de.cubeisland.cubeengine.core.filesystem.FileManager;
import de.cubeisland.cubeengine.core.i18n.I18n;
import de.cubeisland.cubeengine.core.module.ModuleManager;
import de.cubeisland.cubeengine.core.permission.PermissionManager;
import de.cubeisland.cubeengine.core.storage.TableManager;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.world.WorldManager;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.InventoryGuardFactory;
import de.cubeisland.cubeengine.core.util.Version;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import de.cubeisland.cubeengine.core.webapi.ApiServer;

/**
 * This interface specifies all the methods the core of the CubeEngine has to provide.
 * The core provides essential APIs like the I18n API and all the managers like
 * the CommandManager or the TaskManager
 */
public interface Core
{
    public static final Charset CHARSET = Charset.forName("UTF-8");

    /**
     * Returns the version of the core
     *
     * @return the version
     */
    Version getVersion();

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
     * This method returns the engine logger
     *
     * @return the engine logger
     */
    Logger getLog();

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
     * This method returns the DebugMode
     *
     * @return the debugMode
     */
    boolean isDebug();

    /**
     * This method returns the TableManager
     *
     * @return the TableManager
     */
    TableManager getTableManger();

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

    BanManager getBanManager();
}
