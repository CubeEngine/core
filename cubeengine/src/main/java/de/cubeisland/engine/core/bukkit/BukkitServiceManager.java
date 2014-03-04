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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;

import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.module.ModuleClassLoader;
import de.cubeisland.engine.core.module.service.ServiceManager;

public class BukkitServiceManager extends ServiceManager implements ServicesManager
{
    private final BukkitCore core;
    private final ServicesManager servicesManager;
    private final Map<Class<?>, Module> serviceMap;
    private final Map<Module, List<Object>> providerMap;

    public BukkitServiceManager(BukkitCore core)
    {
        super(core);
        this.core = core;
        this.servicesManager = core.getServer().getServicesManager();
        this.serviceMap = new HashMap<>();
        this.providerMap = new HashMap<>();
    }

    public <T> void register(Class<T> service, T provider, Module module, ServicePriority priority)
    {
        this.servicesManager.register(service, provider, this.core, priority);
        synchronized (this.serviceMap)
        {
            if (!this.serviceMap.containsKey(service))
            {
                if (service.getClassLoader() instanceof ModuleClassLoader)
                {
                    this.serviceMap.put(service, module);
                }
            }
        }

        synchronized (this.providerMap)
        {
            List<Object> providers = this.providerMap.get(module);
            if (providers == null)
            {
                this.providerMap.put(module, providers = new ArrayList<>());
            }
            providers.add(provider);
        }
    }

    public void unregisterAll(Module module)
    {
        synchronized (this.providerMap)
        {
            List<Object> providers = this.providerMap.remove(module);
            if (providers != null)
            {
                for (Object provider : providers)
                {
                    this.unregister(provider);
                }
            }
        }
        synchronized (this.serviceMap)
        {
            Iterator<Entry<Class<?>, Module>> it = this.serviceMap.entrySet().iterator();
            Entry<Class<?>, Module> entry;
            while (it.hasNext())
            {
                entry = it.next();
                if (entry.getValue().equals(module))
                {
                    for (RegisteredServiceProvider<?> registration : getRegistrations(entry.getKey()))
                    {
                        this.unregister(entry.getKey(), registration.getProvider());
                    }
                    it.remove();
                }
            }
        }
    }

    @Override
    public <T> T load(Class<T> service)
    {
        return servicesManager.load(service);
    }

    @Override
    public <T> RegisteredServiceProvider<T> getRegistration(Class<T> service)
    {
        return servicesManager.getRegistration(service);
    }

    @Override
    public List<RegisteredServiceProvider<?>> getRegistrations(Plugin plugin)
    {
        return servicesManager.getRegistrations(plugin);
    }

    @Override
    public <T> Collection<RegisteredServiceProvider<T>> getRegistrations(Class<T> service)
    {
        return servicesManager.getRegistrations(service);
    }

    @Override
    public Collection<Class<?>> getKnownServices()
    {
        return servicesManager.getKnownServices();
    }

    @Override
    public <T> boolean isProvidedFor(Class<T> service)
    {
        return servicesManager.isProvidedFor(service);
    }

    @Override
    @Deprecated
    public <T> void register(Class<T> service, T provider, Plugin plugin, ServicePriority priority)
    {
        throw new UnsupportedOperationException("Module should not use this!");
    }

    @Override
    @Deprecated
    public void unregisterAll(Plugin plugin)
    {
        throw new UnsupportedOperationException("Module should not use this!");
    }

    @Override
    public void unregister(Class<?> service, Object provider)
    {
        servicesManager.unregister(service, provider);
    }

    @Override
    public void unregister(Object provider)
    {
        servicesManager.unregister(provider);
    }

    @Override
    public void unregisterServices(Module module)
    {
        super.unregisterServices(module);
        this.unregisterAll(module);
    }
}
