package de.cubeisland.cubeengine.war.storage;

import de.cubeisland.cubeengine.core.persistence.Database;
import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import de.cubeisland.cubeengine.war.CubeWar;
import de.cubeisland.cubeengine.war.groups.AreaType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Faithcaio
 */
public class GroupStorage implements Storage<GroupModel>
{
    private final Database database;
    private final String TABLE = "groups";
    private static GroupStorage instance = null;

    public static GroupStorage get()
    {
        if (instance == null)
        {
            instance = new GroupStorage();
        }
        return instance;
    }

    public GroupStorage()
    {
        database = CubeWar.getDB();

        try
        {
            this.database.prepareStatement("group_get", "SELECT * FROM {{" + TABLE + "}} WHERE id=? LIMIT 1");
            this.database.prepareStatement("group_getall", "SELECT * FROM {{" + TABLE + "}}");//
            this.database.prepareStatement("group_store", "INSERT INTO {{" + TABLE + "}} (id,tag,name,description,isarena,respawnprot,dmgmod,influenceboost,perminfluence,flags) VALUES (?,?,?,?,?,?,?,?,?,?)");
            this.database.prepareStatement("group_delete", "DELETE FROM {{" + TABLE + "}} WHERE id=?");
            this.database.prepareStatement("group_clear", "DELETE FROM {{" + TABLE + "}}");
            this.database.prepareStatement("group_update", "UPDATE {{" + TABLE + "}} SET "
                    + "name=?, description=?, respawnprot=?, dmgmod=?,influenceboost=?,perminfluence=?,flags=? WHERE id=?");

            this.initialize();
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to prepare the statements!", e);
        }
    }

    public void initialize()
    {
        try
        {
            this.database.exec("CREATE TABLE IF NOT EXISTS `groups` ("
                    + "`id` int(10) unsigned NOT NULL,"
                    + "`tag` varchar(10) NOT NULL,"
                    + "`name` varchar(20) NOT NULL,"
                    + "`description` varchar(100) NOT NULL,"
                    + "`isarena` smallint(2) NOT NULL,"
                    + "`respawnprot` int(3) NOT NULL,"
                    + "`dmgmod` varchar(5) NOT NULL,"//Format +1 -1 P30 P-30 S1 S-1
                    + "`influenceboost` int(11) NOT NULL,"
                    + "`perminfluence` int(11) DEFAULT NULL,"
                    + "`flags` int(11) NOT NULL,"
                    + "PRIMARY KEY (`id`)"
                    + ") ENGINE=MyISAM DEFAULT CHARSET=latin1;");
            //TODO einbinden der Listen
            /*
                private List<Material> protect = new ArrayList<Material>();
                private List<String> denyCmd = new ArrayList<String>();
                private List<String> invited = new ArrayList<String>();
                private List<Group> enemy = new ArrayList<Group>();
                private List<Group> ally = new ArrayList<Group>();
             */
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to initialize the Groups-Table !", ex);
        }
    }

    public GroupModel get(int key)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<GroupModel> getAll()
    {
        try
        {
            ResultSet result = this.database.preparedQuery("group_getall");

            Collection<GroupModel> groups = new ArrayList<GroupModel>();
            while (result.next())
            {
                int id = result.getInt("id");
                String tag = result.getString("tag");
                String name = result.getString("name");
                String description = result.getString("description");
                boolean isarena = result.getBoolean("isarena");
                AreaType type;
                if (isarena) type = AreaType.ARENA;
                else type = AreaType.TEAMZONE;
                int respawnprot = result.getInt("respawnprot");
                String dmgmod = result.getString("dmgmod");
                Integer dmg_mod_percent = null;
                Integer dmg_mod_set = null;
                Integer dmg_mod_add = null;
                if (dmgmod.startsWith("P"))
                {
                    dmg_mod_percent = Integer.parseInt(dmgmod.substring(1));
                }
                else if (dmgmod.startsWith("S"))
                {
                    dmg_mod_set = Integer.parseInt(dmgmod.substring(1));
                }
                else
                {
                    dmg_mod_add = Integer.parseInt(dmgmod);
                }
                int influenceboost = result.getInt("influenceboost");
                Integer perminfluence = result.getInt("perminfluence");
                int flags = result.getInt("flags");
                GroupModel group = new GroupModel(id);
                group.setStringVal(tag, name, description);
                group.setIntVal(flags, perminfluence, influenceboost, respawnprot, dmg_mod_percent, dmg_mod_set, dmg_mod_add);
                group.setType(type);
                groups.add(group);
            }

            return groups;
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the groups from the database!", e);
        }
    }

    public void store(GroupModel model)
    {
        try
        {
            int id = model.getId();
            String tag = model.getTag();
            String name = model.getName();
            String description = model.getDescription();
            boolean isarena = false;
            if (model.getType().equals(AreaType.ARENA))
            {
                isarena = true;
            }
            int respawnprot = model.getRespawnProtection();
            Integer dmgmodifier = model.getDmg_mod_set();
            String dmgmod = "0";
            if (dmgmodifier != null)
            {
                dmgmod = "S"+String.valueOf(dmgmodifier);
            }
            else
            {
                dmgmodifier = model.getDmg_mod_percent();
                if (dmgmodifier != null)
                {
                    dmgmod = "P"+String.valueOf(dmgmodifier);
                }
                else
                {
                    dmgmodifier = model.getDmg_mod_add();
                    if (dmgmodifier != null)
                    {
                        dmgmod = String.valueOf(dmgmodifier);
                    }
                }
            }
            int pwrboost = model.getInfluence_boost();
            Integer permpwr = model.getInfluence_perm();
            int flags = model.getBitMaskValue();
            
            this.database.preparedExec("group_store", id, tag, name, description, isarena, respawnprot, dmgmod, pwrboost, permpwr, flags);
        }
        catch (Exception e)
        {
            throw new StorageException("Failed to store the group !", e);
        }
    }

    public void update(GroupModel model)
    {
        try
        {
            int id = model.getId();
            String name = model.getName();
            String description = model.getDescription();
            int respawnprot = model.getRespawnProtection();
            Integer dmgmodifier = model.getDmg_mod_set();
            String dmgmod = "0";
            if (dmgmodifier != null)
            {
                dmgmod = "S"+String.valueOf(dmgmodifier);
            }
            else
            {
                dmgmodifier = model.getDmg_mod_percent();
                if (dmgmodifier != null)
                {
                    dmgmod = "P"+String.valueOf(dmgmodifier);
                }
                else
                {
                    dmgmodifier = model.getDmg_mod_add();
                    if (dmgmodifier != null)
                    {
                        dmgmod = String.valueOf(dmgmodifier);
                    }
                }
            }
            int pwrboost = model.getInfluence_boost();
            Integer permpwr = model.getInfluence_perm();
            int flags = model.getBitMaskValue();
            this.database.preparedExec("group_update", name, description, respawnprot, dmgmod, pwrboost, permpwr, flags, id);
        }
        catch (Exception e)
        {
            throw new StorageException("Failed to update the group !", e);
        }
    }

    public boolean delete(GroupModel model)
    {
        return this.delete(model.getId());
    }

    public boolean delete(int id)
    {
        try
        {
            return this.database.preparedExec("group_delete", id);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to delete the group !", ex);
        }
    }

    public void clear()
    {
        try
        {
            this.database.preparedExec("group_clear");
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to clear the database!", e);
        }
    }
    
    public void merge(GroupModel model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
