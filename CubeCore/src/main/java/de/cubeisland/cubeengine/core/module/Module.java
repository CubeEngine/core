package de.cubeisland.cubeengine.core.module;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.event.EventListener;
import de.cubeisland.cubeengine.core.module.event.ModuleDisabledEvent;
import de.cubeisland.cubeengine.core.module.event.ModuleEnabledEvent;
import de.cubeisland.cubeengine.core.module.event.ModuleLoadedEvent;
import de.cubeisland.cubeengine.core.persistence.database.Database;
import de.cubeisland.cubeengine.core.persistence.filesystem.FileManager;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.Validate;
import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private Logger logger;
    private Set<Module> dependingModules = new HashSet<Module>();
    private ModuleClassLoader classLoader;
    private File folder;
    private boolean enabled;
    private PluginWrapper pluginWrapper;

    protected final void initialize(Core core, ModuleInfo info, PluginWrapper pluginWrapper, Logger logger, File folder, ModuleClassLoader classLoader)
    {
        if (!initialized)
        {
            this.core = core;
            this.info = info;
            this.logger = logger;
            this.classLoader = classLoader;
            this.folder = folder;
            this.enabled = false;
            this.pluginWrapper = pluginWrapper;

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

    public ModuleInfo getInfo()
    {
        return this.info;
    }

    public Logger getLogger()
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

    public ModuleClassLoader getClassLoader()
    {
        return this.classLoader;
    }

    public File getFolder()
    {
        if (!this.folder.exists())
        {
            this.folder.mkdirs();
        }
        return this.folder;
    }

    public Database getDatabase()
    {
        return this.core.getDatabase();
    }

    public void onLoad()
    {}

    public void onEnable()
    {}

    public void onDisable()
    {}

    public void reload()
    {}

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

    public InputStream getResource(String path)
    {
        Validate.notNull(path, "The path must not be null!");
        return this.getClass().getResourceAsStream(path);
    }

    public final boolean isEnabled()
    {
        return this.enabled;
    }

    public PluginWrapper getPluginWrapper()
    {
        return this.pluginWrapper;
    }

    public void registerEvents(EventListener listener)
    {
        this.core.getEventManager().registerListener(listener, this);
    }

    public void unregisterEvents(EventListener listener)
    {
        this.core.getEventManager().unregisterListener(listener);
    }

    public void unregisterEvents()
    {
        this.core.getEventManager().unregisterListener(this);
    }

    public FileManager getFileManager()
    {
        return this.core.getFileManager();
    }

    public UserManager getUserManager()
    {
        return this.core.getUserManager();
    }

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
