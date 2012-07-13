package de.cubeisland.cubeengine.core;

import com.avaje.ebean.EbeanServer;
import org.bukkit.Server;
import org.bukkit.plugin.PluginLoader;

/**
 *
 * @author Faithcaio
 */
public class PluginWrapper
{
    private BukkitCore plugin;

    public PluginWrapper(BukkitCore plugin)
    {
        this.plugin = plugin;
    }

    public BukkitCore getBukkitPlugin()
    {
        return this.plugin;
    }

    public Server getServer()
    {
        return this.plugin.getServer();
    }

    public PluginLoader getPluginLoader()
    {
        return this.plugin.getPluginLoader();
    }

    public boolean isNaggable()
    {
        return this.plugin.isNaggable();
    }

    public void setNaggable(boolean bln)
    {
        this.plugin.setNaggable(bln);
    }

    public EbeanServer getDatabase()
    {
        return this.plugin.getDatabase();
    }
}
