package de.cubeisland.cubeengine.core;

import com.avaje.ebean.EbeanServer;
import org.bukkit.Server;
import org.bukkit.plugin.PluginLoader;

/**
 *
 * @author Anselm Brehme
 */
public class PluginWrapper
{
    private BukkitBootstrapper bootstrapper;

    public PluginWrapper(BukkitBootstrapper bootstrapper)
    {
        this.bootstrapper = bootstrapper;
    }

    public BukkitBootstrapper getBukkitPlugin()
    {
        return this.bootstrapper;
    }

    public Server getServer()
    {
        return this.bootstrapper.getServer();
    }

    public PluginLoader getPluginLoader()
    {
        return this.bootstrapper.getPluginLoader();
    }

    public boolean isNaggable()
    {
        return this.bootstrapper.isNaggable();
    }

    public void setNaggable(boolean bln)
    {
        this.bootstrapper.setNaggable(bln);
    }

    public EbeanServer getDatabase()
    {
        return this.bootstrapper.getDatabase();
    }
}
