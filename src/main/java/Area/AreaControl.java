package Area;

import de.cubeisland.CubeWar.Util;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;

/**
 *
 * @author Faithcaio
 */
public class AreaControl {

    Map<Integer,Area> areas = new HashMap<Integer,Area>();
    private static AreaControl instance = null;
    
    public AreaControl(ConfigurationSection config) 
    {
        for (String name : config.getKeys(false))
        {
            Area newArea = new Area();
            ConfigurationSection section = config.getConfigurationSection(name);
            if (name.equalsIgnoreCase("safezone"))
            {
                newArea.setType(AreaType.SAFEZONE);
                newArea.setId(-10);
                newArea.setName("SafeZone");
                newArea.setTag("SAFE");
                newArea.setDescription("It's safe");
            }else
            if (name.equalsIgnoreCase("warland"))
            {
                newArea.setType(AreaType.WARLAND);
                newArea.setId(-50);
                newArea.setName("WarLand");
                newArea.setTag("WAR");
                newArea.setDescription("War everywhere");
            }else
            if (name.equalsIgnoreCase("wildland"))
            {
                newArea.setType(AreaType.WILDLAND);
                newArea.setId(0);
                newArea.setName("WildLand");
                newArea.setTag("WILD");
                newArea.setDescription("Unclaimed Land");
            }else
            if (name.equalsIgnoreCase("team_default"))
            {
                newArea.setType(AreaType.TEAMZONE);
                newArea.setId(-1);
                newArea.setName("TEAM_DEFAULT");
                newArea.setTag("Def_Team");
                newArea.setDescription("A Team");
            }else
            if (name.equalsIgnoreCase("arena_default"))
            {
                newArea.setType(AreaType.ARENA);
                newArea.setId(-2);
                newArea.setName("ARENA_DEFAULT");
                newArea.setTag("Def_Arena");
                newArea.setDescription("An Arena");
            }
            if (section.getBoolean("economy.bank", false)) newArea.setBit(Area.ECONOMY_BANK);
            if (section.getBoolean("power.haspermpower"))
                newArea.setPower_perm(section.getInt("power.permpower"));
            else
                newArea.setPower_perm(null);
            newArea.setPower_boost(section.getInt("power.powerboost"));
            if (section.getBoolean("power.powerloss")) newArea.setBit(Area.POWER_LOSS);
            if (section.getBoolean("power.powergain")) newArea.setBit(Area.POWER_GAIN);
            if (section.getBoolean("pvp.PvP")) newArea.setBit(Area.PVP_ON);
            if (section.getBoolean("pvp.damage")) newArea.setBit(Area.PVP_DAMAGE);
            if (section.getBoolean("pvp.friendlyfire")) newArea.setBit(Area.PVP_FRIENDLYFIRE);
            newArea.setPvp_spawnprotect(section.getInt("pvp.spawnprotectseconds"));
            if (section.getBoolean("monster.spawn")) newArea.setBit(Area.MONSTER_SPAWN);
            if (section.getBoolean("monster.damage")) newArea.setBit(Area.MONSTER_DAMAGE);
            if (section.getBoolean("build.destroy")) newArea.setBit(Area.BUILD_DESTROY);
            if (section.getBoolean("build.place")) newArea.setBit(Area.BUILD_PLACE);
            newArea.setProtect(Util.convertListStringToMaterial(section.getStringList("protect")));
            if (section.getBoolean("use.fire")) newArea.setBit(Area.USE_FIRE);
            if (section.getBoolean("use.lava")) newArea.setBit(Area.USE_LAVA);
            if (section.getBoolean("use.water")) newArea.setBit(Area.USE_WATER);
            newArea.setDenycommands(section.getStringList("denycommands"));
        }
    }
    
    public static void createInstance(ConfigurationSection config)
    {
       instance = new AreaControl(config);
    }
    
    public static AreaControl get()
    {
        return instance;
    }
    
}