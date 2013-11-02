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
package de.cubeisland.engine.core.bukkit;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import de.cubeisland.engine.core.module.BaseModuleManager;
import de.cubeisland.engine.core.module.Inject;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.module.ModuleInfo;
import de.cubeisland.engine.core.module.exception.MissingPluginDependencyException;
import de.cubeisland.engine.core.module.exception.ModuleException;

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
                        catch (Exception ex)
                        {
                            module.getLog().warn(ex, "An uncaught exception occurred during onFinishLoading()");
                        }
                    }
                }
            }
        });
    }

    @Override
    protected Module loadModule(String name, Map<String, ModuleInfo> moduleInfos, Stack<String> loadStack) throws ModuleException
    {
        Module module = super.loadModule(name, moduleInfos, loadStack);
        if (module == null)
        {
            return null;
        }
        try
        {
            Field[] fields = module.getClass().getDeclaredFields();
            Map<Class<?>, Plugin> pluginClassMap = this.getPluginClassMap();
            Class<?> fieldType;
            for (Field field : fields)
            {
                fieldType = field.getType();
                Inject injectAnnotation = field.getAnnotation(Inject.class);
                if (Plugin.class.isAssignableFrom(fieldType) && injectAnnotation != null)
                {
                    Plugin plugin = pluginClassMap.get(fieldType);
                    if (plugin != null)
                    {
                        this.pluginManager.enablePlugin(plugin); // what their about dependencies?
                        field.setAccessible(true);
                        try
                        {
                            field.set(module, plugin);
                        }
                        catch (Exception e)
                        {
                            module.getLog().warn("Failed to inject a plugin dependency: {}", String.valueOf(plugin));
                        }
                    }
                }
            }
        }
        catch (NoClassDefFoundError e)
        {
            module.getLog().warn("Failed to get the fields of the main class!");
            module.getLog().debug(e.getLocalizedMessage(), e);
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
        Map<Class<?>, Plugin> pluginClassMap = new HashMap<>(plugins.length);
        for (Plugin plugin : plugins)
        {
            pluginClassMap.put(plugin.getClass(), plugin);
        }
        return pluginClassMap;
    }
}
