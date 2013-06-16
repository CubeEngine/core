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
package de.cubeisland.cubeengine.core.bukkit.metrics;

import java.io.IOException;

import de.cubeisland.cubeengine.core.bukkit.BukkitCore;

import org.mcstats.Metrics;
import org.mcstats.Metrics.Graph;
import org.mcstats.Metrics.Plotter;


public final class MetricsInitializer
{
    private final BukkitCore core;
    private Metrics metrics;

    public MetricsInitializer(BukkitCore core)
    {
        this.core = core;
    }

    public void initialize()
    {
        if (this.core.getConfiguration().sendMetrics)
        {
            try
            {
                this.metrics = new Metrics(this.core);
            }
            catch (IOException e)
            {
                this.core.getLog().warn("The initialization of metrics failed!");
            }
            this.initializeGraphs();
        }
    }


    public void initializeGraphs()
    {

        Graph graph = this.metrics.createGraph("Number of modules");
        this.core.getServer().getPluginManager().registerEvents(new ModuleListener(graph), this.core);
        graph.addPlotter(new Plotter()
        {
            @Override
            public int getValue()
            {
                return core.getModuleManager().getModules().size();
            }
        });
    }

    public void start()
    {
        if (this.metrics != null)
        {
            this.metrics.start();
        }
    }
}
