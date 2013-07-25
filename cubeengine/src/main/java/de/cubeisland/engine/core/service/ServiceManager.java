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
import java.util.Map;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.module.Module;

public class ServiceManager
{
    private Core core;

    private final Map<Class, RegisteredServiceProvider> providers = new HashMap<Class, RegisteredServiceProvider>();

    public ServiceManager(Core core)
    {
        this.core = core;
    }

    @SuppressWarnings("unchecked")
    public <S> S getServiceProvider(Class<S> service)
    {
        RegisteredServiceProvider<S> registeredServiceProvider = (RegisteredServiceProvider<S>)this.providers.get(service);
        if (registeredServiceProvider == null) return null;
        return registeredServiceProvider.getProvider();
    }

    public <S> void registerService(Class<S> service, S provider, Module module)
    {
        RegisteredServiceProvider<?> replaced = this.providers.put(service, new RegisteredServiceProvider<S>(service, provider, module));
        if (replaced == null)
        {
            module.getLog().info("Registered ServiceProvider " + provider.getClass().getName()
                                     + " the Service: " + service.getName());
        }
        else
        {
            module.getLog().info("Replaced the registered ServiceProvider ("+ replaced.getProvider().getClass().getName()
                                     +") for the Service " + service.getName() + " by " + provider.getClass().getName());
        }
    }

    public <S> void unregisterService(Class<S> service)
    {
        RegisteredServiceProvider removed = this.providers.remove(service);
        if (removed != null)
        {
            this.core.getLog().info("Unregistered ServiceProvider " + removed.getProvider().getClass().getName()
                    + " of " + removed.getModule().getName() + " for the Service " + removed.getService().getName());
        }
    }

    public void unregisterServices(Module module)
    {
        Iterator<RegisteredServiceProvider> iterator = this.providers.values().iterator();
        while (iterator.hasNext())
        {
            RegisteredServiceProvider next = iterator.next();
            if (next.getModule() == module)
            {
                iterator.remove();
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

