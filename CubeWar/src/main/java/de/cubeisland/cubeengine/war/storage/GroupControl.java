package de.cubeisland.cubeengine.war.storage;

import de.cubeisland.cubeengine.war.Util;
import de.cubeisland.cubeengine.war.groups.AreaType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 *
 * @author Faithcaio
 */
public class GroupControl
{
    private Map<Integer, Group> groups;
    private static GroupControl instance = null;
    GroupStorage groupDB;

    public static GroupControl get()
    {
        return instance;
    }

    private GroupControl(ConfigurationSection config)
    {
        instance = this;
        
        groups = new HashMap<Integer, Group>();

        for (String name : config.getKeys(false))
        {
            GroupModel newModel = new GroupModel(0);
            ConfigurationSection section = config.getConfigurationSection(name);
            if (name.equalsIgnoreCase("safezone"))
            {

                newModel.setType(AreaType.SAFEZONE);
                newModel.setId(-10);
                newModel.setStringVal("SAFE", "SafeZone", "It's safe");
            }
            else if (name.equalsIgnoreCase("warland"))
            {
                newModel.setType(AreaType.WARLAND);
                newModel.setId(-50);
                newModel.setStringVal("WAR", "WarLand", "War everywhere");
            }
            else if (name.equalsIgnoreCase("wildland"))
            {
                newModel.setType(AreaType.WILDLAND);
                newModel.setId(0);
                newModel.setStringVal("WILD", "WildLand", "Unclaimed Land");
            }
            else if (name.equalsIgnoreCase("team_default"))
            {
                newModel.setType(AreaType.TEAMZONE);
                newModel.setId(-1);
                newModel.setStringVal("TEAM_DEFAULT", "Default Team", "A Team");
            }
            else if (name.equalsIgnoreCase("arena_default"))
            {
                newModel.setType(AreaType.ARENA);
                newModel.setId(-2);
                newModel.setStringVal("ARENA_DEFAULT", "Default Arena", "An Arena");
            }
            if (section.getBoolean("economy.bank", false))
            {
                newModel.setBit(GroupModel.ECONOMY_BANK);
            }
            if (section.getBoolean("power.powerloss"))
            {
                newModel.setBit(GroupModel.POWER_LOSS);
            }
            if (section.getBoolean("power.powergain"))
            {
                newModel.setBit(GroupModel.POWER_GAIN);
            }
            if (section.getBoolean("pvp.PvP"))
            {
                newModel.setBit(GroupModel.PVP_ON);
            }
            if (section.getBoolean("pvp.damage"))
            {
                newModel.setBit(GroupModel.PVP_DAMAGE);
            }
            if (section.getBoolean("pvp.friendlyfire"))
            {
                newModel.setBit(GroupModel.PVP_FRIENDLYFIRE);
            }
            Integer influence_perm;
            if (section.getBoolean("power.haspermpower"))
            {
                influence_perm = section.getInt("power.permpower");
            }
            else
            {
                influence_perm = null;
            }
            int influence_boost = section.getInt("power.powerboost", 0);
            Integer respawnProtection = section.getInt("pvp.spawnprotectseconds");
            Integer dmg_mod_percent = null;
            Integer dmg_mod_set = null;
            Integer dmg_mod_add = null;
            String dmgmod = section.getString("pvp.damagemodifier");
            if (dmgmod != null)
            {
                if (dmgmod.charAt(0) == 'P')
                {
                    dmg_mod_percent = Integer.valueOf(dmgmod.substring(1));
                }
                else if (dmgmod.charAt(0) == 'S')
                {
                    dmg_mod_set = Integer.valueOf(dmgmod.substring(1));
                }
                else
                {
                    dmg_mod_add = Integer.valueOf(dmgmod);
                }
            }
            newModel.setIntVal(influence_perm, influence_boost, respawnProtection, dmg_mod_percent, dmg_mod_set, dmg_mod_add);

            if (section.getBoolean("monster.spawn"))
            {
                newModel.setBit(GroupModel.MONSTER_SPAWN);
            }
            if (section.getBoolean("monster.damage"))
            {
                newModel.setBit(GroupModel.MONSTER_DAMAGE);
            }
            if (section.getBoolean("build.destroy"))
            {
                newModel.setBit(GroupModel.BUILD_DESTROY);
            }
            if (section.getBoolean("build.place"))
            {
                newModel.setBit(GroupModel.BUILD_PLACE);
            }
            if (section.getBoolean("use.fire"))
            {
                newModel.setBit(GroupModel.USE_FIRE);
            }
            if (section.getBoolean("use.lava"))
            {
                newModel.setBit(GroupModel.USE_LAVA);
            }
            if (section.getBoolean("use.water"))
            {
                newModel.setBit(GroupModel.USE_WATER);
            }
            newModel.setListVal(Util.convertListStringToMaterial(section.getStringList("protect")),
                    section.getStringList("denycommands"),
                    new ArrayList<String>());
            if (section.getBoolean("closed"))
            {
                newModel.setBit(GroupModel.IS_CLOSED);
            }
            if (section.getBoolean("autoclose"))
            {
                newModel.setBit(GroupModel.AUTO_CLOSE);
            }

            groups.put(newModel.getId(), new Group(newModel));
        }
    }

    public void newTeam(String tag, String name)
    {
        this.newGroup(tag, name, -1);
    }

    public void newArena(String tag, String name)
    {
        this.newGroup(tag, name, -2);
    }
    
    public void newGroup(String tag, String name, int parent)
    {
        GroupModel newModel = this.groups.get(parent).model.deepCopy();
        newModel.setTag(tag);
        newModel.setName(name);
        int id = groups.size() - 4;
        newModel.setId(id);
        groups.put(id, new Group(newModel));
        groupDB.store(newModel);
    }
    
    public void loadDataBase()
    {
        groupDB = GroupStorage.get();
        for (GroupModel model : groupDB.getAll())
        {
            this.groups.put(model.getId(), new Group(model));
        }
    }

    public void updateDataBase(Group... groupToUpdate)
    {

        if (groupToUpdate.length == 0)
        {
            Collection<Group> grouplist = this.groups.values();
            for (Group group : grouplist)
            {
                group.updateDB();
            }
        }
        else
        {
            for (Group group : groupToUpdate)
            {
                group.updateDB();
            }
        }
    }
    
    public Collection<Group> getGroups()
    {
        return groups.values();
    }
    
    public Group getGroupAtLocation(Player player)
    {
        return getGroupAtLocation(player.getLocation());
    }
    
    public Group getGroupAtLocation(Location loc)
    {

        return areas.getGroup(loc);//TODO areaStorage etc...
    }
    
    public Group getWildLand()
    {
        return groups.get(0);
    }
    
    public Group getGroup(int id)
    {
        return groups.get(id);
    }
    
    public Group getGroup(String tag)
    {
        for (Group group : groups.values())
        {
            if (group.getTag().equalsIgnoreCase(tag))
            {
                return group;
            }
        }
        return null;
    }
    
    public boolean isTagFree(String tag)
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
    
    public int getRank(Group group)
    {
        int position = 1;
        int power = group.getInfluence_used();
        for (Group g : groups.values())
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
    
    public boolean isBalanced(Group group)
    {
        int users = 0;
        int teams = 0;
        for (Group g : groups.values())
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
