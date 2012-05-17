package de.cubeisland.cubeengine.war.groups;

import de.cubeisland.cubeengine.war.Util;
import de.cubeisland.cubeengine.war.area.Area;
import de.cubeisland.cubeengine.war.database.AreaStorage;
import de.cubeisland.cubeengine.war.database.GroupStorage;
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
    private static GroupStorage groupDB = new GroupStorage();
    
    public static void wipeArea()
    {
        for (Group g : groups.valueCollection())
        {
            g.resetPower_used();
            Area.remAllAll();
        }
    }

    public static void loadDB()
    {
        for (Group group : groupDB.getAll())
        {
            group.loadDenyUsage();
            groups.put(group.getId(), group);
        }
        
    }
    
    
    public GroupControl(ConfigurationSection config) 
    {
        for (String name : config.getKeys(false))
        {
            Group newGroup = new Group();
            ConfigurationSection section = config.getConfigurationSection(name);
            if (name.equalsIgnoreCase("safezone"))
            {
                newGroup.setType(AreaType.SAFEZONE);
                newGroup.setIntegerValue("id", -10);
                newGroup.setStringValue("name", "SafeZone");
                newGroup.setStringValue("tag", "SAFE");
                newGroup.setStringValue("description", "It's safe");
            }else
            if (name.equalsIgnoreCase("warland"))
            {
                newGroup.setType(AreaType.WARLAND);
                newGroup.setIntegerValue("id", -50);
                newGroup.setStringValue("name", "WarLand");
                newGroup.setStringValue("tag", "WAR");
                newGroup.setStringValue("description", "War everywhere");
            }else
            if (name.equalsIgnoreCase("wildland"))
            {
                newGroup.setType(AreaType.WILDLAND);
                newGroup.setIntegerValue("id", 0);
                newGroup.setStringValue("name", "WildLand");
                newGroup.setStringValue("tag", "WILD");
                newGroup.setStringValue("description", "Unclaimed Land");
            }else
            if (name.equalsIgnoreCase("team_default"))
            {
                newGroup.setType(AreaType.TEAMZONE);
                newGroup.setIntegerValue("id", -1);
                newGroup.setStringValue("name", "TEAM_DEFAULT");
                newGroup.setStringValue("tag", "Def_Team");
                newGroup.setStringValue("description", "A Team");
            }else
            if (name.equalsIgnoreCase("arena_default"))
            {
                newGroup.setType(AreaType.ARENA);
                newGroup.setIntegerValue("id", -2);
                newGroup.setStringValue("name", "ARENA_DEFAULT");
                newGroup.setStringValue("tag", "Def_Arena");
                newGroup.setStringValue("description", "An Arena");
            }
            if (section.getBoolean("economy.bank", false)) newGroup.setBit(Group.ECONOMY_BANK);
            if (section.getBoolean("power.haspermpower"))
                newGroup.setIntegerValue("power_perm", section.getInt("power.permpower"));
            else
                newGroup.setIntegerValue("power_perm", null);
            newGroup.setIntegerValue("power_boost", section.getInt("power.powerboost"));
            if (section.getBoolean("power.powerloss")) newGroup.setBit(Group.POWER_LOSS);
            if (section.getBoolean("power.powergain")) newGroup.setBit(Group.POWER_GAIN);
            if (section.getBoolean("pvp.PvP")) newGroup.setBit(Group.PVP_ON);
            if (section.getBoolean("pvp.damage")) newGroup.setBit(Group.PVP_DAMAGE);
            if (section.getBoolean("pvp.friendlyfire")) newGroup.setBit(Group.PVP_FRIENDLYFIRE);
            newGroup.setIntegerValue("pvp_spawnprotect", section.getInt("pvp.spawnprotectseconds"));
            String dmgmod = section.getString("pvp.damagemodifier");
            newGroup.setIntegerValue("damagemodifier_percent", null);
            newGroup.setIntegerValue("damagemodifier_set", null);
            newGroup.setIntegerValue("damagemodifier_add", null);
            if (dmgmod != null)
            {
                if (dmgmod.charAt(0)=='P')
                {
                    newGroup.setIntegerValue("damagemodifier_percent", Integer.valueOf(dmgmod.substring(1)));
                }else
                if (dmgmod.charAt(0)=='S')
                {
                    newGroup.setIntegerValue("damagemodifier_set", Integer.valueOf(dmgmod.substring(1)));
                }
                else
                {
                   newGroup.setIntegerValue("damagemodifier_add", Integer.valueOf(dmgmod.substring(1))); 
                }
            }
            if (section.getBoolean("monster.spawn")) newGroup.setBit(Group.MONSTER_SPAWN);
            if (section.getBoolean("monster.damage")) newGroup.setBit(Group.MONSTER_DAMAGE);
            if (section.getBoolean("build.destroy")) newGroup.setBit(Group.BUILD_DESTROY);
            if (section.getBoolean("build.place")) newGroup.setBit(Group.BUILD_PLACE);
            newGroup.setListValue("protect", Util.convertListStringToMaterial(section.getStringList("protect")));
            if (section.getBoolean("use.fire")) newGroup.setBit(Group.USE_FIRE);
            if (section.getBoolean("use.lava")) newGroup.setBit(Group.USE_LAVA);
            if (section.getBoolean("use.water")) newGroup.setBit(Group.USE_WATER);
            newGroup.setListValue("denycommands", section.getStringList("denycommands"));
            newGroup.setClosed(section.getBoolean("closed",true));
            newGroup.setAutoClose(section.getBoolean("autoclose",true));
            
            groups.put(newGroup.getId(), newGroup);
        }
    }
    
    public Group newTeam(String tag, String name)
    {
        Group newGroup = groups.get(-1).clone();
        newGroup.setStringValue("tag", tag);
        newGroup.setStringValue("name", name);
        int id = groups.size()-4;
        newGroup.setIntegerValue("id", id);
        groups.put(id, newGroup);
        groupDB.store(newGroup);
        return newGroup;
    }
    
    public Group newArena(String tag, String name)
    {
        Group newGroup = groups.get(-2).clone();
        newGroup.setStringValue("tag", tag);
        newGroup.setStringValue("name", name);
        int id = groups.size()-4;
        newGroup.setIntegerValue("id", id);
        groups.put(id, newGroup);
        return newGroup;
    }
    
    public static void createInstance(ConfigurationSection config)
    {
       instance = new GroupControl(config);
    }
    
    public static GroupControl get()
    {
        return instance;
    }

    public static Group getGroup(Player player)
    {
        return getGroup(player.getLocation());
    }
    
    public static Collection<Group> getGroups()
    {
        return groups.valueCollection();
    }
    
    public static Group getGroup(Location loc)
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
    
    public static Group getGroup(int id)
    {
        return groups.get(id);
    }
    
    public Group getGroup(String tag)
    {
        for (Group group : groups.valueCollection())
        {
            if (group.getTag().equalsIgnoreCase(tag))
                return group;
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