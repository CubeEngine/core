package de.cubeisland.cubeengine.war.groups;

import de.cubeisland.cubeengine.war.CubeWar;
import de.cubeisland.cubeengine.war.Util;
import de.cubeisland.cubeengine.war.area.AreaControl_old;
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
public class GroupControl_old
{

    private TIntObjectHashMap<Group_old> groups = new TIntObjectHashMap<Group_old>();
    private static GroupControl_old instance = null;
    private AreaControl_old areas;

    public void wipeArea()
    {
        for (Group_old g : groups.valueCollection())
        {
            g.resetInfluence_used();
            areas.remAllAll();
        }
    }

    public void loadDB()
    {
        areas = CubeWar.getInstance().getAreas();
        
        GroupStorage groupDB = CubeWar.getInstance().getGroupDB();
        for (Group_old group : groupDB.getAll())
        {
            group.loadDenyUsage();
            groups.put(group.getId(), group);
        }

    }

    public GroupControl_old(ConfigurationSection config)
    {
        for (String name : config.getKeys(false))
        {
            Group_old newGroup = new Group_old();
            ConfigurationSection section = config.getConfigurationSection(name);
            if (name.equalsIgnoreCase("safezone"))
            {
                newGroup.setType(AreaType.SAFEZONE);
                newGroup.setIntegerValue("id", -10);
                newGroup.setStringValue("name", "SafeZone");
                newGroup.setStringValue("tag", "SAFE");
                newGroup.setStringValue("description", "It's safe");
            }
            else if (name.equalsIgnoreCase("warland"))
            {
                newGroup.setType(AreaType.WARLAND);
                newGroup.setIntegerValue("id", -50);
                newGroup.setStringValue("name", "WarLand");
                newGroup.setStringValue("tag", "WAR");
                newGroup.setStringValue("description", "War everywhere");
            }
            else if (name.equalsIgnoreCase("wildland"))
            {
                newGroup.setType(AreaType.WILDLAND);
                newGroup.setIntegerValue("id", 0);
                newGroup.setStringValue("name", "WildLand");
                newGroup.setStringValue("tag", "WILD");
                newGroup.setStringValue("description", "Unclaimed Land");
            }
            else if (name.equalsIgnoreCase("team_default"))
            {
                newGroup.setType(AreaType.TEAMZONE);
                newGroup.setIntegerValue("id", -1);
                newGroup.setStringValue("name", "TEAM_DEFAULT");
                newGroup.setStringValue("tag", "Def_Team");
                newGroup.setStringValue("description", "A Team");
            }
            else if (name.equalsIgnoreCase("arena_default"))
            {
                newGroup.setType(AreaType.ARENA);
                newGroup.setIntegerValue("id", -2);
                newGroup.setStringValue("name", "ARENA_DEFAULT");
                newGroup.setStringValue("tag", "Def_Arena");
                newGroup.setStringValue("description", "An Arena");
            }
            if (section.getBoolean("economy.bank", false))
            {
                newGroup.setBit(Group_old.ECONOMY_BANK);
            }
            if (section.getBoolean("power.haspermpower"))
            {
                newGroup.setIntegerValue("power_perm", section.getInt("power.permpower"));
            }
            else
            {
                newGroup.setIntegerValue("power_perm", null);
            }
            newGroup.setIntegerValue("power_boost", section.getInt("power.powerboost"));
            if (section.getBoolean("power.powerloss"))
            {
                newGroup.setBit(Group_old.POWER_LOSS);
            }
            if (section.getBoolean("power.powergain"))
            {
                newGroup.setBit(Group_old.POWER_GAIN);
            }
            if (section.getBoolean("pvp.PvP"))
            {
                newGroup.setBit(Group_old.PVP_ON);
            }
            if (section.getBoolean("pvp.damage"))
            {
                newGroup.setBit(Group_old.PVP_DAMAGE);
            }
            if (section.getBoolean("pvp.friendlyfire"))
            {
                newGroup.setBit(Group_old.PVP_FRIENDLYFIRE);
            }
            newGroup.setIntegerValue("pvp_spawnprotect", section.getInt("pvp.spawnprotectseconds"));
            String dmgmod = section.getString("pvp.damagemodifier");
            newGroup.setIntegerValue("damagemodifier_percent", null);
            newGroup.setIntegerValue("damagemodifier_set", null);
            newGroup.setIntegerValue("damagemodifier_add", null);
            if (dmgmod != null)
            {
                if (dmgmod.charAt(0) == 'P')
                {
                    newGroup.setIntegerValue("damagemodifier_percent", Integer.valueOf(dmgmod.substring(1)));
                }
                else if (dmgmod.charAt(0) == 'S')
                {
                    newGroup.setIntegerValue("damagemodifier_set", Integer.valueOf(dmgmod.substring(1)));
                }
                else
                {
                    newGroup.setIntegerValue("damagemodifier_add", Integer.valueOf(dmgmod.substring(1)));
                }
            }
            if (section.getBoolean("monster.spawn"))
            {
                newGroup.setBit(Group_old.MONSTER_SPAWN);
            }
            if (section.getBoolean("monster.damage"))
            {
                newGroup.setBit(Group_old.MONSTER_DAMAGE);
            }
            if (section.getBoolean("build.destroy"))
            {
                newGroup.setBit(Group_old.BUILD_DESTROY);
            }
            if (section.getBoolean("build.place"))
            {
                newGroup.setBit(Group_old.BUILD_PLACE);
            }
            newGroup.setListValue("protect", Util.convertListStringToMaterial(section.getStringList("protect")));
            if (section.getBoolean("use.fire"))
            {
                newGroup.setBit(Group_old.USE_FIRE);
            }
            if (section.getBoolean("use.lava"))
            {
                newGroup.setBit(Group_old.USE_LAVA);
            }
            if (section.getBoolean("use.water"))
            {
                newGroup.setBit(Group_old.USE_WATER);
            }
            newGroup.setListValue("denycommands", section.getStringList("denycommands"));
            newGroup.setClosed(section.getBoolean("closed", true));
            newGroup.setAutoClose(section.getBoolean("autoclose", true));

            groups.put(newGroup.getId(), newGroup);
        }
    }

    public Group_old newTeam(String tag, String name)
    {
        Group_old newGroup = groups.get(-1).clone();
        newGroup.setStringValue("tag", tag);
        newGroup.setStringValue("name", name);
        int id = groups.size() - 4;
        newGroup.setIntegerValue("id", id);
        groups.put(id, newGroup);
        GroupStorage groupDB = CubeWar.getInstance().getGroupDB();
        groupDB.store(newGroup);
        return newGroup;
    }

    public Group_old newArena(String tag, String name)
    {
        Group_old newGroup = groups.get(-2).clone();
        newGroup.setStringValue("tag", tag);
        newGroup.setStringValue("name", name);
        int id = groups.size() - 4;
        newGroup.setIntegerValue("id", id);
        groups.put(id, newGroup);
        return newGroup;
    }

    public static void createInstance(ConfigurationSection config)
    {
        instance = new GroupControl_old(config);
    }

    public static GroupControl_old get()
    {
        return instance;
    }

    public Group_old getGroup(Player player)
    {
        return getGroup(player.getLocation());
    }

    public Collection<Group_old> getGroups()
    {
        return groups.valueCollection();
    }

    public Group_old getGroup(Location loc)
    {

        return areas.getGroup(loc);
    }

    public Group_old getWildLand()
    {
        return groups.get(0);
    }

    public boolean setGroupValue(int id, String key, String value)
    {
        Group_old area = groups.get(id);
        return area.setValue(key, value);
    }

    public Group_old getGroup(int id)
    {
        return groups.get(id);
    }

    public Group_old getGroup(String tag)
    {
        for (Group_old group : groups.valueCollection())
        {
            if (group.getTag().equalsIgnoreCase(tag))
            {
                return group;
            }
        }
        return null;
    }

    public boolean freeTag(String tag)
    {
        if (this.getGroup(tag) == null)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public int getRank(Group_old group)
    {
        int position = 1;
        int power = group.getInfluence_used();
        for (Group_old g : groups.valueCollection())
        {
            int gpower = g.getInfluence_used();
            if (gpower > 0)
            {
                if (power < gpower)
                {
                    ++position;
                }
            }
        }
        return position;
    }

    public boolean isBalanced(Group_old group)
    {
        int users = 0;
        int teams = 0;
        for (Group_old g : groups.valueCollection())
        {
            if (!g.isBalancing())
            {
                continue;
            }
            if (g.getId() > 0)
            {
                users += g.getUserSum();
                ++teams;
            }
        }
        if ((users / teams) * 2 >= group.getUserSum())
        {
            return true;
        }
        return false;
    }
}