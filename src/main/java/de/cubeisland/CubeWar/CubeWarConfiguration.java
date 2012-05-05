package de.cubeisland.CubeWar;

import Groups.GroupControl;
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
    public int killpoint_min;
    public int killpoint_max;
    
    public CubeWarConfiguration(Configuration config)
    {
        this.cubewar_language = config.getString("cubewar.language");
        this.cubewar_ranks = new HashMap<Integer,Rank>();
        ConfigurationSection ranksection = config.getConfigurationSection("cubewar.ranks");
        for (String rankname : ranksection.getKeys(false))
        {
            ConfigurationSection cursection = ranksection.getConfigurationSection(rankname);
            Rank rank = new Rank(rankname,cursection.getInt("deathmodifier"),cursection.getInt("killmodifier"),cursection.getInt("killpointlimit"));
            this.cubewar_ranks.put(cursection.getInt("killpointlimit"), rank);
        }
        this.killpoint_min = config.getInt("cubewar.killpoint.min");
        this.killpoint_max = config.getInt("cubewar.killpoint.max");
        
        GroupControl.createInstance(config.getConfigurationSection("cubewar.area"));
    }
}
