package de.cubeisland.cubeengine.war;

import de.cubeisland.cubeengine.war.groups.GroupControl_old;
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
    public Map<String, Integer> killKP = new HashMap<String, Integer>();
    public List<String> IGPerm_leader;
    public List<String> IGPerm_mod;
    public List<String> IGPerm_member;
    public List<String> IGPerm_user;
    public int fly_block;
    public final String war_database_host;
    public final short war_database_port;
    public final String war_database_user;
    public final String war_database_pass;
    public final String war_database_name;
    public final double influencePerMin;
    public final double influencePerMinOnline;
    public final int afterDaysOffline;
    public final double loosePerMin;
    public final double influenceCost;

    public CubeWarConfiguration(Configuration config)
    {
        this.cubewar_language = config.getString("cubewar.language");
        this.cubewar_ranks = new TIntObjectHashMap<Rank>();
        ConfigurationSection ranksection = config.getConfigurationSection("cubewar.ranks");
        for (String rankname : ranksection.getKeys(false))
        {
            ConfigurationSection cursection = ranksection.getConfigurationSection(rankname);
            Rank rank = new Rank(rankname, cursection.getInt("deathmodifier"), cursection.getInt("killmodifier"), 
                                           cursection.getInt("killpointlimit"), cursection.getDouble("influence"));
            this.cubewar_ranks.put(cursection.getInt("killpointlimit"), rank);
        }
        this.killpoint_min = config.getInt("cubewar.killpoint.min");
        this.killpoint_max = config.getInt("cubewar.killpoint.max");
        ConfigurationSection kps = config.getConfigurationSection("cubewar.killpoint.kp");
        for (String key : kps.getKeys(false))
        {
            this.killKP.put(key, kps.getInt(key));
        }
        this.max_claim = config.getInt("cubewar.claim.maxclaim");

        this.IGPerm_leader = config.getStringList("cubewar.IGperm.leader");
        this.IGPerm_mod = config.getStringList("cubewar.IGperm.mod");
        this.IGPerm_member = config.getStringList("cubewar.IGperm.member");
        this.IGPerm_user = config.getStringList("cubewar.IGperm.user");
        this.fly_block = config.getInt("cubewar.fly.block_sec_after_hit");

        this.war_database_host = config.getString("cubewar.database.host");
        this.war_database_port = ((short) config.getInt("cubewar.database.port"));
        this.war_database_user = config.getString("cubewar.database.user");
        this.war_database_pass = config.getString("cubewar.database.pass");
        this.war_database_name = config.getString("cubewar.database.name");
        this.influencePerMin = config.getDouble("cubewar.influence.gainPerMin");
        this.influencePerMinOnline = config.getDouble("cubewar.influence.gainPerMinOnline");
        this.afterDaysOffline = config.getInt("cubewar.influence.loose.afterDaysOffline");
        this.loosePerMin = config.getInt("cubewar.influence.loose.loosePerMin");
        this.influenceCost = config.getDouble("cubewar.influence.buy");

        GroupControl_old.createInstance(config.getConfigurationSection("cubewar.area"));
    }
}
