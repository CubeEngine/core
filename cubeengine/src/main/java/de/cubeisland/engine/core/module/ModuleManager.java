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
package de.cubeisland.engine.core.module;

import java.nio.file.Path;
import java.util.Collection;

import de.cubeisland.engine.core.module.exception.CircularDependencyException;
import de.cubeisland.engine.core.module.exception.IncompatibleCoreException;
import de.cubeisland.engine.core.module.exception.IncompatibleDependencyException;
import de.cubeisland.engine.core.module.exception.InvalidModuleException;
import de.cubeisland.engine.core.module.exception.MissingDependencyException;
import de.cubeisland.engine.core.module.exception.MissingPluginDependencyException;
import de.cubeisland.engine.core.module.exception.ModuleException;
import de.cubeisland.engine.core.module.service.ServiceManager;
import de.cubeisland.engine.core.util.Cleanable;

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
     * @param modulePath the file to load the module from
     * @return the loaded module
     *
     * @throws InvalidModuleException           if the file is not a valid module
     * @throws CircularDependencyException      if the module defines a circular dependencies
     * @throws MissingDependencyException       if the module has a missing dependency
     * @throws IncompatibleDependencyException  if the module needs a newer dependency
     * @throws IncompatibleCoreException        if the module depends on a newer core
     * @throws MissingPluginDependencyException if the module depends on a missing plugin
     */
    Module loadModule(Path modulePath) throws ModuleException;

    /**
     * This method loads all modules from a directory
     *
     * @param directory the directory to load from
     */
    void loadModules(Path directory);

    /**
     * Enables a module
     *
     * @param module the module
     * @return true if it succeeded
     */
    boolean enableModule(Module module);

    /**
     * This method enables all modules or at least all that don't provide world chunkgenerator
     */
    void enableModules();

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
    void reloadModule(Module module) throws ModuleException;

    /**
     * Reloads the given module
     *
     * @param module the module to reload
     * @param fromFile whether to reload the module from file
     * @throws RuntimeException when loading the module from file fails
     */
    void reloadModule(Module module, boolean fromFile) throws ModuleException;

    /**
     * Reloads all modules
     *
     * @return the number of reloaded modules
     */
    int reloadModules();

    /**
     * Reloads all modules
     *
     * @param fromFile whether to reload the modules from file
     * @return the number of reloaded modules
     */
    int reloadModules(boolean fromFile);

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

    /**
     * Gets the service manager
     *
     * @return the service manager
     */
    ServiceManager getServiceManager();
}
