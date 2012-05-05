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
                newArea.setIntegerValue("id", -10);
                newArea.setStringValue("name", "SafeZone");
                newArea.setStringValue("tag", "SAFE");
                newArea.setStringValue("description", "It's safe");
            }else
            if (name.equalsIgnoreCase("warland"))
            {
                newArea.setType(AreaType.WARLAND);
                newArea.setIntegerValue("id", -50);
                newArea.setStringValue("name", "WarLand");
                newArea.setStringValue("tag", "WAR");
                newArea.setStringValue("description", "War everywhere");
            }else
            if (name.equalsIgnoreCase("wildland"))
            {
                newArea.setType(AreaType.WILDLAND);
                newArea.setIntegerValue("id", 0);
                newArea.setStringValue("name", "WildLand");
                newArea.setStringValue("tag", "WILD");
                newArea.setStringValue("description", "Unclaimed Land");
            }else
            if (name.equalsIgnoreCase("team_default"))
            {
                newArea.setType(AreaType.TEAMZONE);
                newArea.setIntegerValue("id", -1);
                newArea.setStringValue("name", "TEAM_DEFAULT");
                newArea.setStringValue("tag", "Def_Team");
                newArea.setStringValue("description", "A Team");
            }else
            if (name.equalsIgnoreCase("arena_default"))
            {
                newArea.setType(AreaType.ARENA);
                newArea.setIntegerValue("id", -2);
                newArea.setStringValue("name", "ARENA_DEFAULT");
                newArea.setStringValue("tag", "Def_Arena");
                newArea.setStringValue("description", "An Arena");
            }
            if (section.getBoolean("economy.bank", false)) newArea.setBit(Area.ECONOMY_BANK);
            if (section.getBoolean("power.haspermpower"))
                newArea.setIntegerValue("power_perm", section.getInt("power.permpower"));
            else
                newArea.setIntegerValue("power_perm", null);
            newArea.setIntegerValue("power_boost", section.getInt("power.powerboost"));
            if (section.getBoolean("power.powerloss")) newArea.setBit(Area.POWER_LOSS);
            if (section.getBoolean("power.powergain")) newArea.setBit(Area.POWER_GAIN);
            if (section.getBoolean("pvp.PvP")) newArea.setBit(Area.PVP_ON);
            if (section.getBoolean("pvp.damage")) newArea.setBit(Area.PVP_DAMAGE);
            if (section.getBoolean("pvp.friendlyfire")) newArea.setBit(Area.PVP_FRIENDLYFIRE);
            newArea.setIntegerValue("pvp_spawnprotect", section.getInt("pvp.spawnprotectseconds"));
            if (section.getBoolean("monster.spawn")) newArea.setBit(Area.MONSTER_SPAWN);
            if (section.getBoolean("monster.damage")) newArea.setBit(Area.MONSTER_DAMAGE);
            if (section.getBoolean("build.destroy")) newArea.setBit(Area.BUILD_DESTROY);
            if (section.getBoolean("build.place")) newArea.setBit(Area.BUILD_PLACE);
            newArea.setListValue("protect", Util.convertListStringToMaterial(section.getStringList("protect")));
            if (section.getBoolean("use.fire")) newArea.setBit(Area.USE_FIRE);
            if (section.getBoolean("use.lava")) newArea.setBit(Area.USE_LAVA);
            if (section.getBoolean("use.water")) newArea.setBit(Area.USE_WATER);
            newArea.setListValue("denycommands", section.getStringList("denycommands"));
            
            areas.put(newArea.getId(), newArea);
        }
    }
    
    public Area newTeam(String tag, String name)
    {
        Area newArea = areas.get(-1).clone();
        newArea.setStringValue("tag", tag);
        newArea.setStringValue("name", name);
        //TODO DATABASE Get ID!!!!!!!
        int id = 1;
        newArea.setIntegerValue("id", id);
        areas.put(id, newArea);
        //#############################
        return newArea;
    }
    
    public Area newArena(String tag, String name)
    {
        Area newArea = areas.get(-2).clone();
        newArea.setStringValue("tag", tag);
        newArea.setStringValue("name", name);
        //TODO DATABASE Get ID!!!!!!!
        int id = 1;
        newArea.setIntegerValue("id", id);
        areas.put(id, newArea);
        //#############################
        return newArea;
    }
    
    public static void createInstance(ConfigurationSection config)
    {
       instance = new AreaControl(config);
    }
    
    public static AreaControl get()
    {
        return instance;
    }
    
    public Integer getTeamArea(String tag)
    {
        for (Area area : areas.values())
        {
            if (area.getTag().equalsIgnoreCase(tag))
                if (area.getType().equals(AreaType.TEAMZONE))
                    return area.getId();
        }    
        return null;
    }
    
    public Integer getArenaArea(String tag)
    {
        for (Area area : areas.values())
        {
            if (area.getTag().equalsIgnoreCase(tag))
                if (area.getType().equals(AreaType.ARENA))
                    return area.getId();
        }
        return null;
    }
    
    public boolean setAreaValue(int id, String key, String value)
    {
        Area area = areas.get(id);
        return area.setValue(key, value);
    }
    
    public Area getArea(int id)
    {
        return areas.get(id);
    }
    
}