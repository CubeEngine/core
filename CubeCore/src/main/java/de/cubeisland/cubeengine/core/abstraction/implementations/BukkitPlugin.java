package de.cubeisland.cubeengine.core.abstraction.implementations;

import de.cubeisland.cubeengine.core.abstraction.Configuration;
import de.cubeisland.cubeengine.core.abstraction.PluginDescription;
import java.io.File;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
public class BukkitPlugin implements de.cubeisland.cubeengine.core.abstraction.Plugin
{
    private final Plugin plugin;
    private final Configuration config;
    private final PluginDescription desc;

    public BukkitPlugin(Plugin plugin)
    {
        this.plugin = plugin;
        this.config = new BukkitConfigration(plugin.getConfig());
        this.desc = new BukkitPluginDescription(plugin.getDescription());
    }

    public Plugin getHandle()
    {
        return this.plugin;
    }

    public String getName()
    {
        return this.desc.getName();
    }

    public String getVersion()
    {
        return this.desc.getVersion();
    }

    public void enable()
    {
        this.plugin.getServer().getPluginManager().enablePlugin(this.plugin);
    }

    public void disable()
    {
        this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
    }

    public void reload()
    {
        this.disable();
        this.enable();
    }

    public File getDataFolder()
    {
        return this.plugin.getDataFolder();
    }

    public Configuration getConfiguration()
    {
        return this.config;
    }

    public boolean isEnabled()
    {
        return this.plugin.isEnabled();
    }

    public PluginDescription getDescription()
    {
        return this.desc;
    }
}
