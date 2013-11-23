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
package de.cubeisland.engine.core.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.module.Module;

public class ServiceManager
{
    private Core core;

    private final Map<Class, LinkedList<RegisteredServiceProvider>> providers = new HashMap<>();

    public ServiceManager(Core core)
    {
        this.core = core;
    }

    @SuppressWarnings("unchecked")
    public <S> S getServiceProvider(Class<S> service)
    {
        LinkedList<RegisteredServiceProvider> providers = this.providers.get(service);
        if (providers == null || providers.isEmpty())
        {
            return null;
        }
        RegisteredServiceProvider<S> registeredServiceProvider = providers.getLast();
        return registeredServiceProvider.getProvider();
    }

    public <S> void registerService(Class<S> service, S provider, Module module)
    {
        LinkedList<RegisteredServiceProvider> providers = this.providers.get(service);
        if (providers == null)
        {
            providers = new LinkedList<>();
            this.providers.put(service, providers);
        }
        RegisteredServiceProvider last = providers.isEmpty() ? null : providers.getLast();
        providers.addLast(new RegisteredServiceProvider<>(service, provider, module));
        if (last == null)
        {
            module.getLog().info("Registered ServiceProvider {} for the Service: {}",
                                 provider.getClass().getName(),
                                 service.getName());
        }
        else
        {
            module.getLog().info("Replaced the registered ServiceProvider ({}) for the Service {} by {}",
                                 last.getProvider().getClass().getName(),
                                 service.getName(),
                                 provider.getClass().getName());
        }
    }

    public <S> void unregisterService(Class<S> service, Module module)
    {
        LinkedList<RegisteredServiceProvider> providers = this.providers.get(service);
        if (providers == null || providers.isEmpty())
        {
            return;
        }
        RegisteredServiceProvider remove = null;
        for (RegisteredServiceProvider provider : providers)
        {
            if (provider.getModule().getId().equals(module.getId()))
            {
                remove = provider;
                break;
            }
        }
        if (remove != null)
        {
            providers.remove(remove);
            this.core.getLog().info("Unregistered ServiceProvider {} of {} for the Service {}",
                                    remove.getProvider().getClass().getName(),
                                    remove.getModule().getName(),
                                    remove.getService().getName());
        }
    }

    public void unregisterServices(Module module)
    {
        for (LinkedList<RegisteredServiceProvider> providers : this.providers.values())
        {
            Iterator<RegisteredServiceProvider> iterator = providers.iterator();
            while (iterator.hasNext())
            {
                RegisteredServiceProvider next = iterator.next();
                if (next.getModule().getId().equals(module.getId()))
                {
                    iterator.remove();
                    module.getLog().info("Unregistered ServiceProvider {} of {} for the Service {}",
                                            next.getProvider().getClass().getName(),
                                            next.getModule().getName(),
                                            next.getService().getName());
                }
            }
        }
    }

    public boolean isServiceRegistered(String serviceString)
    {
        for (Class service : this.providers.keySet())
        {
            if (service.getName().equals(serviceString))
            {
                return true;
            }
        }
        return false;
    }
}

