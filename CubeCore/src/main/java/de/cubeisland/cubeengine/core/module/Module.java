package de.cubeisland.cubeengine.core.module;

import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.module.event.ModuleDisabledEvent;
import de.cubeisland.cubeengine.core.module.event.ModuleEnabledEvent;
import de.cubeisland.cubeengine.core.module.event.ModuleLoadedEvent;
import de.cubeisland.cubeengine.core.persistence.database.Database;
import de.cubeisland.cubeengine.core.util.Validate;
import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;

/**
 * module for CubeEngine
 *
 * @author Phillip Schichtel
 */
public abstract class Module
{
    private boolean initialized = false;
    private CubeCore core;
    private ModuleInfo info;
    private Logger logger;
    private Set<Module> dependingModules = new HashSet<Module>();
    private ModuleClassLoader classLoader;
    private File folder;
    private boolean enabled;
    private PluginWrapper pluginWrapper;

    protected final void initialize(CubeCore core, ModuleInfo info, PluginWrapper pluginWrapper, Logger logger, File folder, ModuleClassLoader classLoader)
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
            core.getPluginManager().callEvent(new ModuleLoadedEvent(core, this));
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
    public CubeCore getCore()
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
        return this.core.getDB();
    }

    public PluginManager getPluginManager()
    {
        return this.core.getPluginManager();
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

    public Server getServer()
    {
        return this.core.getServer();
    }

    public final boolean isEnabled()
    {
        return this.enabled;
    }

    public PluginWrapper getPluginWrapper()
    {
        return this.pluginWrapper;
    }

    protected final void enable()
    {
        if (!this.enabled)
        {
            try
            {
                this.enabled = true;  //Module has to be enabled for bukkit
                this.onEnable();
                this.core.getPluginManager().callEvent(new ModuleEnabledEvent(this.core, this));
            }
            catch (Exception ex)
            {
                this.enabled = false;
            }
        }
    }

    protected final void disable()
    {
        if (this.enabled)
        {
            this.onDisable();
            this.core.getPluginManager().callEvent(new ModuleDisabledEvent(this.core, this));
            this.enabled = false;
        }
    }
}
