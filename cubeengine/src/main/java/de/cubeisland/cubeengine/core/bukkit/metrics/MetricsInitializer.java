package de.cubeisland.cubeengine.core.bukkit.metrics;

import java.io.IOException;

import de.cubeisland.cubeengine.core.bukkit.BukkitCore;

import org.mcstats.Metrics;
import org.mcstats.Metrics.Graph;
import org.mcstats.Metrics.Plotter;

import static de.cubeisland.cubeengine.core.logger.LogLevel.NOTICE;

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
                this.core.getLog().log(NOTICE, "The initialization of metrics failed!");
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
