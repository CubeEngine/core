package de.cubeisland.cubeengine.war;

import de.cubeisland.cubeengine.war.groups.GroupControl;
import de.cubeisland.cubeengine.war.user.Rank;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

/**
 * load in configuration file
 */
public class CubeWarConfiguration
{
    public String cubewar_language;
    public TIntObjectHashMap<Rank> cubewar_ranks;
    public int killpoint_min;
    public int killpoint_max;
    public int max_claim;
    public Map<String,Integer> killKP = new HashMap<String,Integer>();
    public List<String> IGPerm_leader;
    public List<String> IGPerm_mod;
    public List<String> IGPerm_member;
    public List<String> IGPerm_user;
    
    public CubeWarConfiguration(Configuration config)
    {
        this.cubewar_language = config.getString("cubewar.language");
        this.cubewar_ranks = new TIntObjectHashMap<Rank>();
        ConfigurationSection ranksection = config.getConfigurationSection("cubewar.ranks");
        for (String rankname : ranksection.getKeys(false))
        {
            ConfigurationSection cursection = ranksection.getConfigurationSection(rankname);
            Rank rank = new Rank(rankname,cursection.getInt("deathmodifier"),cursection.getInt("killmodifier"),cursection.getInt("killpointlimit"));
            this.cubewar_ranks.put(cursection.getInt("killpointlimit"), rank);
        }
        this.killpoint_min = config.getInt("cubewar.killpoint.min");
        this.killpoint_max = config.getInt("cubewar.killpoint.max");
        ConfigurationSection kps = config.getConfigurationSection("cubewar.killpoint.kp");
        for (String key : kps.getKeys(false))
        {
            this.killKP.put(key, config.getInt(key));
        }
        this.max_claim = config.getInt("cubewar.claim.maxclaim");
        
        this.IGPerm_leader = config.getStringList("cubewar.IGperm.leader");
        this.IGPerm_mod = config.getStringList("cubewar.IGperm.mod");
        this.IGPerm_member = config.getStringList("cubewar.IGperm.member");
        this.IGPerm_user = config.getStringList("cubewar.IGperm.user");
        
        GroupControl.createInstance(config.getConfigurationSection("cubewar.area"));
    }
}
