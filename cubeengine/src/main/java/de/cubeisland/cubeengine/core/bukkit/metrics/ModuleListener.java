package de.cubeisland.cubeengine.core.bukkit.metrics;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import de.cubeisland.cubeengine.core.module.event.ModuleDisabledEvent;
import de.cubeisland.cubeengine.core.module.event.ModuleEnabledEvent;

import gnu.trove.map.hash.THashMap;
import org.mcstats.Metrics.Graph;

class ModuleListener implements Listener
{
    private final Graph moduleGraph;
    private final THashMap<String, ModulePlotter> plotters;

    ModuleListener(Graph moduleGraph)
    {
        this.moduleGraph = moduleGraph;
        this.plotters = new THashMap<String, ModulePlotter>();
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
