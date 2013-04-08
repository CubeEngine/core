package de.cubeisland.cubeengine.core.bukkit.metrics;

import org.mcstats.Metrics.Plotter;

class ModulePlotter extends Plotter
{
    public ModulePlotter(String name)
    {
        super(name);
    }

    @Override
    public int getValue()
    {
        return 1;
    }
}
