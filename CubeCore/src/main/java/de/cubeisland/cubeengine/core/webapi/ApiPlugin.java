package de.cubeisland.cubeengine.core.webapi;

import org.bukkit.plugin.Plugin;

/**
 *
 * @author Phillip Schichtel
 */
public interface ApiPlugin extends Plugin
{
    public ApiConfiguration getApiConfiguration();
}