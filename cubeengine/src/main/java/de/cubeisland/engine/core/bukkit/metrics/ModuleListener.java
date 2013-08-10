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
package de.cubeisland.engine.core.bukkit.metrics;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import de.cubeisland.engine.core.module.event.ModuleDisabledEvent;
import de.cubeisland.engine.core.module.event.ModuleEnabledEvent;

import gnu.trove.map.hash.THashMap;
import org.mcstats.Metrics.Graph;

class ModuleListener implements Listener
{
    private final Graph moduleGraph;
    private final THashMap<String, ModulePlotter> plotters;

    ModuleListener(Graph moduleGraph)
    {
        this.moduleGraph = moduleGraph;
        this.plotters = new THashMap<>();
    }

    @EventHandler
    public void moduleEnable(ModuleEnabledEvent event)
    {
        final String module = event.getModule().getName();
        ModulePlotter plotter = new ModulePlotter(module);
        this.moduleGraph.addPlotter(plotter);
        this.plotters.put(module, plotter);
    }

    @EventHandler
    public void moduleDisable(ModuleDisabledEvent event)
    {
        ModulePlotter plotter = this.plotters.remove(event.getModule().getName());
        if (plotter != null)
        {
            this.moduleGraph.removePlotter(plotter);
        }
    }
}
