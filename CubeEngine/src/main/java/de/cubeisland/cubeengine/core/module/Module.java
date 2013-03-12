package de.cubeisland.cubeengine.core.module;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.bukkit.EventManager;
import de.cubeisland.cubeengine.core.bukkit.TaskManager;
import de.cubeisland.cubeengine.core.command.CommandHolder;
import de.cubeisland.cubeengine.core.command.CommandManager;
import de.cubeisland.cubeengine.core.command.CubeCommand;
import de.cubeisland.cubeengine.core.filesystem.FileManager;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.storage.ModuleRegistry;
import de.cubeisland.cubeengine.core.storage.SimpleModuleRegistry;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.user.UserManager;
import org.apache.commons.lang.Validate;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;

import static de.cubeisland.cubeengine.core.logger.LogLevel.ERROR;
import static de.cubeisland.cubeengine.core.logger.LogLevel.WARNING;

/**
 * Module for CubeEngine.
 */
public abstract class Module
{
    private boolean initialized = false;
    private Core core;
    private ModuleInfo info;
    private Logger logger;
    private ModuleLoader loader;
    private ModuleRegistry registry = null;
    private ModuleClassLoader classLoader;
    private File folder;
    private boolean enabled;

    final void initialize(Core core, ModuleInfo info, File folder, Logger logger, ModuleLoader loader, ModuleClassLoader classLoader)
    {
        if (!this.initialized)
        {
            this.initialized = true;
            this.core = core;
            this.info = info;
            this.loader = loader;
            this.classLoader = classLoader;
            this.folder = folder;
            this.enabled = false;

            this.logger = logger;
        }
    }

    /**
     * Returns the lower-cased name of the module
     *
     * @return the lower-cased name of the module
     */
    public String getId()
    {
        return this.info.getId();
    }

    /**
     * Returns the name of this module
     *
     * @return the module name
     */
    public String getName()
    {
        return this.info.getName();
    }

    /**
     * Returns the revision of this module
     *
     * @return the revision number
     */
    public int getRevision()
    {
        return this.info.getRevision();
    }

    /**
     * This method return the module info
     *
     * @return the module info
     */
    public ModuleInfo getInfo()
    {
        return this.info;
    }

    /**
     * This method returns the module logger
     *
     * @return the module logger
     */
    public Logger getLogger()
    {
        return this.logger;
    }

    /**
     * Returns the core
     *
     * @return the core
     */
    public Core getCore()
    {
        return this.core;
    }

    /**
     * Returns the module manager
     *
     * @return the module manager
     */
    public ModuleManager getModuleManager()
    {
        return this.core.getModuleManager();
    }

    /**
     * Returns the command manager
     *
     * @return the command manager
     */
    public CommandManager getCommandManager()
    {
        return this.core.getCommandManager();
    }

    /**
     * Returns the event manager
     *
     * @return the event manager
     */
    public EventManager getEventManager()
    {
        return this.core.getEventManager();
    }

    /**
     * This method returns the ClassLoader which loaded this module
     *
     * @return the ClassLoader
     */
    public ModuleClassLoader getClassLoader()
    {
        return this.classLoader;
    }

    /**
     * This method returns the module specific folder
     *
     * @return the module folder or null if it could not be created
     */
    public File getFolder()
    {
        if (!this.folder.exists())
        {
            if (!this.folder.mkdirs())
            {
                return null;
            }
        }
        return this.folder;
    }

    /**
     * This method returns the database of the engine
     *
     * @return the database
     */
    public Database getDatabase()
    {
        return this.core.getDB();
    }

    /**
     * This method will be called if the module was not found in the module
     * registration
     */
    public void install()
    {}

    /**
     * This method will be called if a module gets uninstalled
     */
    public void uninstall()
    {}

    /**
     * This method will be called if the currently loaded module revision is
     * higher than the one stored in the registry
     *
     * @param oldRevision the old revision form the database
     */
    public void update(int oldRevision)
    {}

    /**
     * This method gets called right after the module initialization
     */
    public void onLoad()
    {}

    /**
     * This method gets called when the module got enabled
     */
    public void onEnable()
    {}

    /**
     * This method gets called when the module got disabled
     */
    public void onDisable()
    {}

    /**
     * This method should be overridden to do reloading
     */
    public void reload()
    {}

    @Override
    public int hashCode()
    {
        return this.info.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj != null && obj instanceof Module)
        {
            return this.info.equals(((Module)obj).info);
        }
        return false;
    }

    /**
     * This method returns a resource from the module jar as an InputStream
     *
     * @param path the path to the resource
     * @return the InputStream for the resource or null if the it wasn't found
     */
    public InputStream getResource(String path)
    {
        Validate.notNull(path, "The path must not be null!");
        return this.getClass().getResourceAsStream(path);
    }

    /**
     * This method checks whether this module is currently enabled
     *
     * @return true if the module is enabled, otherwise false
     */
    public final boolean isEnabled()
    {
        return this.enabled;
    }

    /**
     * This method returns the file manager
     *
     * @return the file manager
     */
    public FileManager getFileManager()
    {
        return this.core.getFileManager();
    }

    /**
     * This method returns the user manager
     *
     * @return the user manager
     */
    public UserManager getUserManager()
    {
        return this.core.getUserManager();
    }

    /**
     * This method enables the module
     *
     * @return the enabled state of the module
     */
    final boolean enable()
    {
        if (!this.enabled)
        {
            try
            {
                this.onEnable();
                this.enabled = true;
            }
            catch (Throwable t)
            {
                this.getLogger().log(ERROR, t.getClass().getSimpleName() + " while enabling: " + t.getLocalizedMessage(), t);
            }
        }
        return this.enabled;
    }

    /**
     * This method disables the module
     */
    final void disable()
    {
        if (this.enabled)
        {
            try
            {
                this.onDisable();
            }
            catch (Throwable t)
            {
                this.getLogger().log(WARNING, t.getClass().getSimpleName() + " while disabling: " + t.getLocalizedMessage(), t);
            }
            this.enabled = false;
        }
    }

    public ModuleLoader getLoader()
    {
        return this.loader;
    }

    public void registerPermissions(Permission[] permissions)
    {
        this.core.getPermissionManager().registerPermissions(this, permissions);
    }

    public void registerCommand(CubeCommand command, String... parents)
    {
        this.core.getCommandManager().registerCommand(command, parents);
    }

    public void registerCommands(CommandHolder commandHolder, String... parents)
    {
        this.core.getCommandManager().registerCommands(this, commandHolder, parents);
    }

    public void registerCommands(Object commandHolder, Class<? extends CubeCommand> commandType, String... parents)
    {
        this.core.getCommandManager().registerCommands(this, commandHolder, commandType, parents);
    }

    public void registerListener(Listener listener)
    {
        this.core.getEventManager().registerListener(this, listener);
    }

    public void removeListener(Listener listener)
    {
        this.core.getEventManager().removeListener(this, listener);
    }

    public TaskManager getTaskManger()
    {
        return this.core.getTaskManager();
    }

    public ModuleRegistry getRegistry()
    {
        if (this.registry == null)
        {
            this.registry = new SimpleModuleRegistry(this, this.loader.getRegistry());
        }
        return this.registry;
    }
}
