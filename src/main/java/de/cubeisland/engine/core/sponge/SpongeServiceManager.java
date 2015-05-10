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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import de.cubeisland.engine.core.module.service.ServiceManager;

public class SpongeServiceManager extends ServiceManager // TODO delegate registered services to Sponge ServiceManager once Modularity is in use
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
}
