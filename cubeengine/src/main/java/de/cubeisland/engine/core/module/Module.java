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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import de.cubeisland.engine.configuration.Configuration;
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.logging.LogFactory;
import de.cubeisland.engine.core.logging.LoggingUtil;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.storage.ModuleRegistry;
import de.cubeisland.engine.core.storage.SimpleModuleRegistry;
import de.cubeisland.engine.core.util.Version;
import de.cubeisland.engine.logging.Log;
import de.cubeisland.engine.logging.LogTarget;
import de.cubeisland.engine.logging.filter.PrefixFilter;
import de.cubeisland.engine.logging.target.file.AsyncFileTarget;


/**
 * Module for CubeEngine.
 */
public abstract class Module
{
    private boolean initialized = false;
    private Core core;
    private ModuleInfo info;
    private Log log;
    private ModuleLoader loader;
    private ModuleRegistry registry = null;
    private ClassLoader classLoader;
    private Path folder;
    private boolean enabled;
    private Permission modulePermission;

    protected Module()
    {}

    void initialize(Core core, ModuleInfo info, Path folder, ModuleLoader loader, ClassLoader classLoader)
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
            LogFactory logFactory = core.getLogFactory();
            this.log = logFactory.getLog(this.getClass(), this.getName());
            this.log.addTarget(new AsyncFileTarget(LoggingUtil.getLogFile(core, this.getName()),
                                                   LoggingUtil.getFileFormat(true, true),
                                                   true, LoggingUtil.getCycler(),
                                                   this.core.getTaskManager().getThreadFactory()));
            LogTarget parentTarget = this.log.addDelegate(logFactory.getParent());
            parentTarget.appendFilter(new PrefixFilter("[" + this.getName() + "] "));
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
    public Version getVersion()
    {
        return this.info.getVersion();
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
     * This method returns the module log
     *
     * @return the module log
     */
    public Log getLog()
    {
        return this.log;
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
     * This method returns the ClassLoader which loaded this module
     *
     * @return the ClassLoader
     */
    public ClassLoader getClassLoader()
    {
        return this.classLoader;
    }

    /**
     * This method returns the module specific folder
     *
     * @return the module folder or null if it could not be created
     */
    public Path getFolder()
    {
        try
        {
            Files.createDirectories(this.folder);
        }
        catch (IOException ex)
        {
            this.log.error(ex, "Failed to create the data folder!");
        }
        return this.folder;
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
     * This method gets called right after the module initialization.
     */
    public void onLoad()
    {}

    /**
     * This method gets called when the module got enabled.
     */
    public void onEnable()
    {}

    /**
     * This method gets called when the module got disabled.
     */
    public void onDisable()
    {}

    /**
     * This method gets called when the server startup is done.
     */
    public void onStartupFinished()
    {}

    /**
     * This method gets called when a uncaught exception reached root of a thread created by this module
     *
     * @param t the thread
     * @param e the exception
     */
    public void onUncaughtException(Thread t, Throwable e)
    {
        this.getLog().error("An uncaught exception occurred! This is a critical error and should be reported!");
        this.getLog().error("The thread: {}", t.getName());
        this.getLog().error("The error: {}", e.getLocalizedMessage(), e);
    }

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
        assert path != null: "The path must not be null!";
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
                this.getLog().error(t, "{} while enabling!", t.getClass().getSimpleName());
            }
        }
        return this.enabled;
    }

    /**
     * This method disables the module
     *
     * @return true if and only if the module has been disabled
     */
    final boolean disable()
    {
        if (this.enabled)
        {
            try
            {
                this.onDisable();
                if (this.modulePermission != null)
                {
                    Permission.BASE.detach(this.modulePermission);
                    this.modulePermission = null;
                }
            }
            catch (Throwable t)
            {
                this.getLog().warn(t, "{} while disabling!", t.getClass().getSimpleName());
            }
            this.enabled = false;
            return true;
        }
        return false;
    }

    public ModuleRegistry getRegistry()
    {
        if (this.registry == null)
        {
            this.registry = new SimpleModuleRegistry(this, this.loader.getRegistry());
        }
        return this.registry;
    }

    public Permission getBasePermission()
    {
        if (modulePermission == null)
        {
            modulePermission = Permission.BASE.childWildcard(this.getId());
        }
        return modulePermission;
    }

    /**
     * Loads and saves from config.{@link de.cubeisland.engine.configuration.codec.ConfigurationCodec#getExtension()} in the module folder
     *
     * @param clazz the configurations class
     * @return the loaded configuration
     */
    protected final <T extends Configuration> T loadConfig(Class<T> clazz)
    {
        T config = this.core.getConfigFactory().create(clazz);
        config.setFile(this.getFolder().resolve("config." + config.getCodec().getExtension()).toFile());
        if (config.reload(true))
        {
            this.getLog().info("Saved new configuration file! config.{}" , config.getCodec().getExtension());
        }
        return config;
    }
}
