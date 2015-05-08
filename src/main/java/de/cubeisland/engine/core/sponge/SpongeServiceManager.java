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
package de.cubeisland.engine.core.sponge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.google.common.base.Optional;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.module.ModuleClassLoader;
import de.cubeisland.engine.core.module.service.ServiceManager;
import org.spongepowered.api.service.ProviderExistsException;
import org.spongepowered.api.service.ProvisioningException;
import org.spongepowered.api.service.ServiceReference;

public class SpongeServiceManager extends ServiceManager implements org.spongepowered.api.service.ServiceManager
{
    private final SpongeCore core;
    private final org.spongepowered.api.service.ServiceManager servicesManager;
    private final Map<Object, List<Object>> providerMap;

    public SpongeServiceManager(SpongeCore core)
    {
        super(core);
        this.core = core;
        this.servicesManager = core.getGame().getServiceManager();
        this.providerMap = new HashMap<>();
    }

    @Override
    public <T> void setProvider(Object plugin, Class<T> service, T provider) throws ProviderExistsException
    {
        List<Object> list = providerMap.get(plugin);
        if (list == null)
        {
            list = new ArrayList<>();
            providerMap.put(plugin, list);
        }
        list.add(provider);
        servicesManager.setProvider(plugin, service, provider);
    }

    @Override
    public <T> Optional<T> provide(Class<T> service)
    {
        return provide(service);
    }

    @Override
    public <T> ServiceReference<T> potentiallyProvide(Class<T> service)
    {
        return potentiallyProvide(service);
    }

    @Override
    public <T> T provideUnchecked(Class<T> service) throws ProvisioningException
    {
        return provideUnchecked(service);
    }

    //------------------------------------------- TODO remove old stuff

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
}
