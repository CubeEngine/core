package de.cubeisland.cubeengine.core.abstraction;

import java.util.Set;

/**
 *
 * @author CodeInfection
 */
public interface PluginManager
{
    public Plugin getPlugin(String name);

    public Set<Plugin> getPlugins();

    public void enablePlugin(Plugin plugin);

    public void disablePlugin(Plugin plugin);

    public void reloadPlugin(Plugin plugin);
}
