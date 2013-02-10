package de.cubeisland.cubeengine.core.module;

import de.cubeisland.cubeengine.core.module.exception.CircularDependencyException;
import de.cubeisland.cubeengine.core.module.exception.IncompatibleCoreException;
import de.cubeisland.cubeengine.core.module.exception.IncompatibleDependencyException;
import de.cubeisland.cubeengine.core.module.exception.InvalidModuleException;
import de.cubeisland.cubeengine.core.module.exception.MissingDependencyException;
import de.cubeisland.cubeengine.core.module.exception.MissingPluginDependencyException;
import de.cubeisland.cubeengine.core.util.Cleanable;

import java.io.File;
import java.util.*;

/**
 * This class manages the modules.
 */
public interface ModuleManager extends Cleanable
{
    Module getModule(String id);

    <T extends Module> T getModule(Class<T> mainClass);

    /**
     * This method returns a collection of the modules
     *
     * @return the modules
     */
    Collection<Module> getModules();

    /**
     * Loads a module
     *
     * @param moduleFile the file to load the module from
     * @return the loaded module
     *
     * @throws InvalidModuleException           if the file is not a valid module
     * @throws CircularDependencyException      if the module defines a circular dependencies
     * @throws MissingDependencyException       if the module has a missing dependency
     * @throws IncompatibleDependencyException  if the module needs a newer dependency
     * @throws IncompatibleCoreException        if the module depends on a newer core
     * @throws MissingPluginDependencyException if the module depends on a missing plugin
     */
    Module loadModule(File moduleFile) throws InvalidModuleException, CircularDependencyException, MissingDependencyException, IncompatibleDependencyException, IncompatibleCoreException, MissingPluginDependencyException;

    /**
     * This method loads all modules from a directory
     *
     * @param directory the directory to load from
     */
    void loadModules(File directory);

    /**
     * Enables a module
     *
     * @param module the module
     * @return true if it succeeded
     */
    boolean enableModule(Module module);

    /**
     * This method enables all modules that provide world generators
     */
    void enableWorldGeneratorModules();

    /**
     * This method enables all modules or at least all that don't provide world generators
     *
     * @param worldGenerators whether to also load the world generator-providing modules
     */
    void enableModules(boolean worldGenerators);

    /**
     * This method disables a module
     *
     * @param module the module
     */
    void disableModule(Module module);

    /**
     * This method tries to unload a module be removing as many references as possible.
     * this means:
     * - disable all modules that depend in the given module
     * - disable the module
     * - remove its ClassLoader and all the reference to it
     * - remove the module from the module map
     *
     * @param module the module to unload
     */
    void unloadModule(Module module);

    /**
     * Reloads the given module
     *
     * @param module the module to reload
     */
    void reloadModule(Module module);

    /**
     * This method disables all modules
     */
    void disableModules();

    /**
     * This method disables all modules
     */
    void unloadModules();

    /**
     * Returns a dummy module
     *
     * @return the singleton instance of the dummy CoreModule
     */
    CoreModule getCoreModule();
}
