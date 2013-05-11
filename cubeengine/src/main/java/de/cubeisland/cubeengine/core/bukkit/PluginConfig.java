package de.cubeisland.cubeengine.core.bukkit;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Option;

/**
 * Configuration class to parse the custom CubeEngine values of plugin.yml
 */
@Codec("yml")
public class PluginConfig extends Configuration
{
    @Option("source-version")
    public String sourceVersion;
}
