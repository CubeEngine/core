/*
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
package org.cubeengine.libcube.service;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class MonitoringService
{
    private final CollectorRegistry registry;
    private final Set<Collector> registeredCollectors = new HashSet<>();

    @Inject
    public MonitoringService()
    {
        this.registry = new CollectorRegistry();
    }

    public CollectorRegistry getRegistry()
    {
        return registry;
    }

    public boolean register(Collector collector)
    {
        if (registeredCollectors.contains(collector))
        {
            return false;
        }
        this.registry.register(collector);
        this.registeredCollectors.add(collector);
        return true;
    }
}
