package de.cubeisland.cubeengine.core.module;

import de.cubeisland.cubeengine.core.CubeCore;
import java.util.Set;
import org.bukkit.plugin.Plugin;

/**
 * module for CubeEngine
 * 
 * @author Phillip Schichtel
 */
public interface Module extends Plugin
{
    /**
     * Returns the name of this module
     *
     * @return the module name
     */
    public String getModuleName();

    /**
     * Returns the names of the modules
     *
     * @return an array of the module names
     */
    public Set<String> getDependencies();

    /**
     * Returns a list of the modules that depend on this module
     *
     * @return the modules
     */
    public Set<String> getDependingModules();

    /**
     * Adds a module that depends on this module
     *
     * @param name the module's name
     */
    public void addDependingModule(String name);

    /**
     * Returns the core
     *
     * @return the core
     */
    public CubeCore getCore();

    /**
     * Returns the module manager
     *
     * @return the module manager
     */
    public ModuleManager getModuleManager();
}
