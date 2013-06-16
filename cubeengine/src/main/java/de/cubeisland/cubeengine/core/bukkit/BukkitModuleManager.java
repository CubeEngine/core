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
package de.cubeisland.cubeengine.core.bukkit;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.module.BaseModuleManager;
import de.cubeisland.cubeengine.core.module.Inject;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.module.ModuleInfo;
import de.cubeisland.cubeengine.core.module.exception.CircularDependencyException;
import de.cubeisland.cubeengine.core.module.exception.IncompatibleCoreException;
import de.cubeisland.cubeengine.core.module.exception.IncompatibleDependencyException;
import de.cubeisland.cubeengine.core.module.exception.InvalidModuleException;
import de.cubeisland.cubeengine.core.module.exception.MissingDependencyException;
import de.cubeisland.cubeengine.core.module.exception.MissingPluginDependencyException;
import de.cubeisland.cubeengine.core.module.exception.MissingProviderException;

import static java.util.logging.Level.WARNING;

public class BukkitModuleManager extends BaseModuleManager
{
    private final BukkitCore core;
    private final PluginManager pluginManager;

    public BukkitModuleManager(BukkitCore core, ClassLoader parentClassLoader)
    {
        super(core, parentClassLoader);
        this.pluginManager = core.getServer().getPluginManager();
        this.core = core;
    }

    void init()
    {
        this.core.getServer().getScheduler().runTask(this.core, new Runnable()
        {
            @Override
            public void run()
            {
                synchronized (BukkitModuleManager.this)
                {
                    for (Module module : getModules())
                    {
                        try
                        {
                            module.onStartupFinished();
                        }
                        catch (Exception e)
                        {
                            module.getLog().log(WARNING, "An uncaught exception occurred during onFinishLoading(): " + e.getMessage(), e);
                        }
                    }
                }
            }
        });
    }

    @Override
    protected Module loadModule(String name, Map<String, ModuleInfo> moduleInfos, Stack<String> loadStack) throws CircularDependencyException, MissingDependencyException, InvalidModuleException, IncompatibleDependencyException, IncompatibleCoreException, MissingPluginDependencyException, MissingProviderException
    {
        Module module = super.loadModule(name, moduleInfos, loadStack);
        try
        {
            Field[] fields = module.getClass().getDeclaredFields();
            Map<Class<?>, Plugin> pluginClassMap = this.getPluginClassMap();
            Class<?> fieldType;
            for (Field field : fields)
            {
                fieldType = field.getType();
                if (Plugin.class.isAssignableFrom(fieldType) && field.isAnnotationPresent(Inject.class))
                {
                    Plugin plugin = pluginClassMap.get(fieldType);
                    if (plugin == null)
                    {
                        continue;
                    }
                    this.pluginManager.enablePlugin(plugin); // what their about dependencies?
                    field.setAccessible(true);
                    try
                    {
                        field.set(module, plugin);
                    }
                    catch (Exception e)
                    {
                        module.getLog().log(LogLevel.WARNING, "Failed to inject a plugin dependency: {0}", String.valueOf(plugin));
                    }
                }
            }
        }
        catch (NoClassDefFoundError e)
        {
            module.getLog().log(LogLevel.WARNING, "Failed to get the fields of the main class: " + e.getLocalizedMessage(), e);
        }
        return module;
    }

    @Override
    protected void validateModuleInfo(ModuleInfo info) throws MissingPluginDependencyException
    {
        for (String plugin : info.getPluginDependencies())
        {
            if (this.pluginManager.getPlugin(plugin) == null)
            {
                throw new MissingPluginDependencyException(plugin);
            }
        }
    }

    protected Map<Class<?>, Plugin> getPluginClassMap()
    {
        Plugin[] plugins = this.pluginManager.getPlugins();
        Map<Class<?>, Plugin> pluginClassMap = new HashMap<Class<?>, Plugin>(plugins.length);
        for (Plugin plugin : plugins)
        {
            pluginClassMap.put(plugin.getClass(), plugin);
        }
        return pluginClassMap;
    }
}
