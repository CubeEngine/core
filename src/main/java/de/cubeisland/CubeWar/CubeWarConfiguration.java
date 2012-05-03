package de.cubeisland.CubeWar;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

/**
 * load in configuration file
 */
public class CubeWarConfiguration
{
    public String cubewar_language;
    public Map<Integer,Rank> cubewar_ranks;
    
    public CubeWarConfiguration(Configuration config)
    {
        this.cubewar_language = config.getString("cubewar.language");
        this.cubewar_ranks = new HashMap<Integer,Rank>();
        ConfigurationSection ranksection = config.getConfigurationSection("cubewar.ranks");
        int i=0;
        for (String rankname : ranksection.getKeys(false))
        {
            ConfigurationSection cursection = ranksection.getConfigurationSection(rankname);
            Rank rank = new Rank(rankname,cursection.getInt("deathmodifier"),cursection.getInt("killmodifier"));
            this.cubewar_ranks.put(++i, rank);
        }
    }
}
