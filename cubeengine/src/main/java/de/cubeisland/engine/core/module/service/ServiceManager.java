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
package de.cubeisland.engine.core.module.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.module.ModuleClassLoader;
import de.cubeisland.engine.core.module.ModuleInfo;
import de.cubeisland.engine.core.module.service.Service.Priority;

public class ServiceManager
{
    private Core core;

    private final Map<Class<?>, Service<?>> services = new HashMap<>();

    public ServiceManager(Core core)
    {
        this.core = core;
    }

    @SuppressWarnings("unchecked")
    public <S> Service<S> getService(Class<S> service)
    {
        synchronized (this.services)
        {
            return (Service<S>)this.services.get(service);
        }
    }

    public <S> S getServiceImplementation(Class<S> service)
    {
        return this.getService(service).getImplementation();
    }

    public <S> Service<S> registerService(Module module, Class<S> interfaceClass, S implementation)
    {
        return this.registerService(module, interfaceClass, implementation, Priority.NORMAL);
    }

    @SuppressWarnings("unchecked")
    public <S> Service<S> registerService(Module module, Class<S> interfaceClass, S implementation, Priority priority)
    {
        assert interfaceClass.isInterface(): "Services have to be interfaces!";

        synchronized (this.services)
        {
            Service<S> service = (Service<S>)this.services.get(interfaceClass);
            if (service == null)
            {
                Module m = this.getModuleFromClass(interfaceClass);
                this.services.put(interfaceClass, service = new Service<>(m != null ? m : module, interfaceClass));
            }
            service.addImplementation(module, implementation, priority);
            return service;
        }
    }

    private Module getModuleFromClass(Class interfaceClass)
    {
        ClassLoader classLoader = interfaceClass.getClassLoader();
        if (classLoader instanceof ModuleClassLoader)
        {
            ModuleInfo info = ((ModuleClassLoader)classLoader).getModuleInfo();
            return this.core.getModuleManager().getModule(info.getId());
        }
        return null;
    }

    public void unregisterService(Class interfaceClass)
    {
        synchronized (this.services)
        {
            this.services.remove(interfaceClass);
        }
    }

    public void unregisterServices(Module module)
    {
        synchronized (this.services)
        {
            Iterator<Entry<Class<?>, Service<?>>> it = this.services.entrySet().iterator();
            while (it.hasNext())
            {
                if (it.next().getValue().getModule() == module)
                {
                    it.remove();
                }
            }
        }
    }
}

