package de.cubeisland.cubeengine.core.module;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.filesystem.FileManager;
import de.cubeisland.cubeengine.core.module.event.ModuleDisabledEvent;
import de.cubeisland.cubeengine.core.module.event.ModuleEnabledEvent;
import de.cubeisland.cubeengine.core.module.event.ModuleLoadedEvent;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.Validate;
import de.cubeisland.cubeengine.core.util.log.ModuleLogger;
import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

/**
 * module for CubeEngine
 *
 * @author Phillip Schichtel
 */
public abstract class Module
{
    private boolean initialized = false;
    private Core core;
    private ModuleInfo info;
    private ModuleLogger logger;
    private Set<Module> dependingModules = new HashSet<Module>();
    private ModuleClassLoader classLoader;
    private File folder;
    private boolean enabled;
    private PluginWrapper pluginWrapper;

    protected final void initialize(Core core, ModuleInfo info, PluginWrapper pluginWrapper, File folder, ModuleClassLoader classLoader)
    {
        if (!this.initialized)
        {
            this.core = core;
            this.info = info;
            this.classLoader = classLoader;
            this.folder = folder;
            this.enabled = false;
            this.pluginWrapper = pluginWrapper;

            this.logger = new ModuleLogger(this);

            this.onLoad();
            core.getEventManager().fireEvent(new ModuleLoadedEvent(core, this));
        }
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
    public ModuleLogger getLogger()
    {
        return this.logger;
    }

    /**
     * Returns a list of the modules that depend on this module
     *
     * @return the modules
     */
    public Set<Module> getDependingModules()
    {
        return this.dependingModules;
    }

    /**
     * Adds a module that depends on this module
     *
     * @param module the module's name
     */
    public void addDependingModule(Module module)
    {
        this.dependingModules.add(module);
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
     * This method returns the classloader which loaded this module
     *
     * @return the classloader
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
     * This method will be called if the module was not found in the module registration
     */
    public void install()
    {}
    
    /**
     * This method will be called if a module gets uninstalled
     */
    public void uninstall()
    {}
    
    /**
     * This method will be called if the currently loaded module revision is higher
     * than the one stored in the registry
     *
     * @param oldRevision the old revision form the database
     */
    public void update(int oldRevision)
    {}

    /**
     * This method gets called right after the module initialization
     */
    public void onLoad()
    {
    }

    /**
     * This method gets called when the module got enabled
     */
    public void onEnable()
    {
    }

    /**
     * This method gets called when the module got disabled
     */
    public void onDisable()
    {
    }

    /**
     * This method should be overridden to do reloading
     */
    public void reload()
    {
    }

    @Override
    public int hashCode()
    {
        return this.getName().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (obj instanceof Module)
        {
            return this.getName().equals(((Module)obj).getName());
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
     * This method returns a wrapper to use the module as a plugin for server
     * specific stuff
     *
     * @return the PluginWrapper of this module
     */
    public PluginWrapper getPluginWrapper()
    {
        return this.pluginWrapper;
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
    protected final boolean enable()
    {
        if (!this.enabled)
        {
            this.enabled = true;
            try
            {
                this.onEnable();
                this.core.getEventManager().fireEvent(new ModuleEnabledEvent(this.core, this));
            }
            catch (Throwable t)
            {
                this.logger.log(Level.SEVERE, t.getClass().getSimpleName() + " while enabling: " + t.getLocalizedMessage(), t);
            }
            this.enabled = false;
        }
        return this.enabled;
    }

    /**
     * This method disables the module
     */
    protected final void disable()
    {
        if (this.enabled)
        {
            try
            {
                this.onDisable();
            }
            catch (Throwable t)
            {
                this.logger.log(Level.SEVERE, t.getClass().getSimpleName() + " while disabling: " + t.getLocalizedMessage(), t);
            }
            this.core.getEventManager().fireEvent(new ModuleDisabledEvent(this.core, this));
            this.enabled = false;
        }
    }
}