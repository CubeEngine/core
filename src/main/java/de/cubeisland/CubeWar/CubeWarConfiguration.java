package de.cubeisland.CubeWar;

import org.bukkit.configuration.Configuration;

/**
 * load in configuration file
 */
public class CubeWarConfiguration
{
    public String cubewar_language;
    
    public CubeWarConfiguration(Configuration config)
    {
        this.cubewar_language = config.getString("cubewar.language");
    }
}
