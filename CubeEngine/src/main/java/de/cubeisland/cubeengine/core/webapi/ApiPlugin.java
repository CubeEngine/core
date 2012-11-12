package de.cubeisland.cubeengine.core.webapi;

import org.bukkit.plugin.Plugin;

public interface ApiPlugin extends Plugin
{
    public ApiConfiguration getApiConfiguration();
}