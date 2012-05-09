package de.cubeisland.CubeWar.Groups;

import de.cubeisland.CubeWar.Area.Area;
import de.cubeisland.CubeWar.Util;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.Collection;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 *
 * @author Faithcaio
 */
public class GroupControl {

    private static TIntObjectHashMap<Group> groups = new TIntObjectHashMap<Group>();
    private static GroupControl instance = null;

    public static void wipeArea()
    {
        for (Group g : groups.valueCollection())
        {
            g.resetPower_used();
            Area.remAllAll();
        }
    }
    
    public GroupControl(ConfigurationSection config) 
    {
        for (String name : config.getKeys(false))
        {
            Group newArea = new Group();
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
            if (section.getBoolean("economy.bank", false)) newArea.setBit(Group.ECONOMY_BANK);
            if (section.getBoolean("power.haspermpower"))
                newArea.setIntegerValue("power_perm", section.getInt("power.permpower"));
            else
                newArea.setIntegerValue("power_perm", null);
            newArea.setIntegerValue("power_boost", section.getInt("power.powerboost"));
            if (section.getBoolean("power.powerloss")) newArea.setBit(Group.POWER_LOSS);
            if (section.getBoolean("power.powergain")) newArea.setBit(Group.POWER_GAIN);
            if (section.getBoolean("pvp.PvP")) newArea.setBit(Group.PVP_ON);
            if (section.getBoolean("pvp.damage")) newArea.setBit(Group.PVP_DAMAGE);
            if (section.getBoolean("pvp.friendlyfire")) newArea.setBit(Group.PVP_FRIENDLYFIRE);
            newArea.setIntegerValue("pvp_spawnprotect", section.getInt("pvp.spawnprotectseconds"));
            String dmgmod = section.getString("pvp.damagemodifier");
            newArea.setIntegerValue("damagemodifier_percent", null);
            newArea.setIntegerValue("damagemodifier_set", null);
            newArea.setIntegerValue("damagemodifier_add", null);
            if (dmgmod != null)
            {
                if (dmgmod.charAt(0)=='P')
                {
                    newArea.setIntegerValue("damagemodifier_percent", Integer.valueOf(dmgmod.substring(1)));
                }else
                if (dmgmod.charAt(0)=='S')
                {
                    newArea.setIntegerValue("damagemodifier_set", Integer.valueOf(dmgmod.substring(1)));
                }
                else
                {
                   newArea.setIntegerValue("damagemodifier_add", Integer.valueOf(dmgmod.substring(1))); 
                }
            }
            if (section.getBoolean("monster.spawn")) newArea.setBit(Group.MONSTER_SPAWN);
            if (section.getBoolean("monster.damage")) newArea.setBit(Group.MONSTER_DAMAGE);
            if (section.getBoolean("build.destroy")) newArea.setBit(Group.BUILD_DESTROY);
            if (section.getBoolean("build.place")) newArea.setBit(Group.BUILD_PLACE);
            newArea.setListValue("protect", Util.convertListStringToMaterial(section.getStringList("protect")));
            if (section.getBoolean("use.fire")) newArea.setBit(Group.USE_FIRE);
            if (section.getBoolean("use.lava")) newArea.setBit(Group.USE_LAVA);
            if (section.getBoolean("use.water")) newArea.setBit(Group.USE_WATER);
            newArea.setListValue("denycommands", section.getStringList("denycommands"));
            newArea.setClosed(section.getBoolean("closed",true));
            newArea.setAutoClose(section.getBoolean("autoclose",true));
            
            groups.put(newArea.getId(), newArea);
        }
    }
    
    public Group newTeam(String tag, String name)
    {
        Group newArea = groups.get(-1).clone();
        newArea.setStringValue("tag", tag);
        newArea.setStringValue("name", name);
        int id = groups.size()-4;
        newArea.setIntegerValue("id", id);
        groups.put(id, newArea);
        return newArea;
    }
    
    public Group newArena(String tag, String name)
    {
        Group newArea = groups.get(-2).clone();
        newArea.setStringValue("tag", tag);
        newArea.setStringValue("name", name);
        int id = groups.size()-4;
        newArea.setIntegerValue("id", id);
        groups.put(id, newArea);
        return newArea;
    }
    
    public static void createInstance(ConfigurationSection config)
    {
       instance = new GroupControl(config);
    }
    
    public static GroupControl get()
    {
        return instance;
    }

    public static Group getArea(Player player)
    {
        return getArea(player.getLocation());
    }
    
    public static Collection<Group> getAreas()
    {
        return groups.valueCollection();
    }
    
    public static Group getArea(Location loc)
    {
        return Area.getGroup(loc);
    }
    
    public static Group getWildLand()
    {
        return groups.get(0);
    }
    
    public boolean setGroupValue(int id, String key, String value)
    {
        Group area = groups.get(id);
        return area.setValue(key, value);
    }
    
    public Group getGroup(int id)
    {
        return groups.get(id);
    }
    
    public Group getGroup(String tag)
    {
        for (Group area : groups.valueCollection())
        {
            if (area.getTag().equalsIgnoreCase(tag))
                return area;
        }
        return null;
    }

    public boolean freeTag(String tag)
    {
        if (this.getGroup(tag)==null) return true;
        else return false;
    }
    
    public int getRank(Group group)
    {
        int position = 1;
        int power = group.getPower_used();
        for (Group g : groups.valueCollection())
        {
            int gpower = g.getPower_used();
            if (gpower>0)
                if (power < gpower)
                    ++position;                    
        }
        return position;
    }
    
    public boolean isBalanced(Group group)
    {
        int users = 0;
        int teams = 0;
        for (Group g : groups.valueCollection())
        {
            if (!g.isBalancing())
            {
                continue;
            }
            if (g.getId()>0)
            {
                users += g.getUserSum();
                ++teams;
            }
        }
        if ((users / teams)*2 >= group.getUserSum()) return true;
        return false;
    }
}