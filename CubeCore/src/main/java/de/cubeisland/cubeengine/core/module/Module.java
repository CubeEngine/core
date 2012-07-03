package de.cubeisland.cubeengine.core.module;

import de.cubeisland.cubeengine.core.CubeCore;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

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

    public final void initialize(CubeCore core, ModuleInfo info, Logger logger)
    {
        if (!initialized)
        {
            this.core = core;
            this.info = info;
            this.logger = logger;
        }
    }

    /**
     * Returns the name of this module
     *
     * @return the module name
     */
    public String getName()
    {
        return this.info.name;
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

    public void onEnable()
    {}

    public void onDisable()
    {}

    public void reload()
    {}

    @Override
    public int hashCode()
    {
        return this.info.name.hashCode();
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
            return this.info.name.equals(((Module)obj).info.name);
        }
        return false;
    }
}
