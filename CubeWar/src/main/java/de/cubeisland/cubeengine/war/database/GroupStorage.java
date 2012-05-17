/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cubeisland.cubeengine.war.database;

import de.cubeisland.cubeengine.core.persistence.Database;
import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import de.cubeisland.cubeengine.war.CubeWar;
import de.cubeisland.cubeengine.war.groups.AreaType;
import de.cubeisland.cubeengine.war.groups.Group;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 *
 * @author Faithcaio
 */
public class GroupStorage implements Storage<Group>{

    private final Database database = CubeWar.getDB();
    private final String TABLE = "groups";
    
    public GroupStorage() 
    {
        this.initialize();
        try
        {//Area IDs Safe = -10 | War = -50 | WildLand = 0 | DefaultTeam = -1 | DefaultArena = -2
         //More Teams/Arena ID = 1+...
         //ID, TAG, NAME, DESCRIPTION, ISARENA?, respawnprotect, dmgmod, pwrboost, permpwr,  flags
         //int str  str   str          bool      int (in sec)    str     int       int(NULL) int
            
         //TODO deniedCmd / protections zu DenyUsageStorage
            this.database.prepareStatement("group_get", "SELECT id,tag,name,desc,isarena,respawnprot,dmgmod,pwrboost,permpwr,flags FROM {{" + TABLE + "}} WHERE id=? LIMIT 1");
            this.database.prepareStatement("group_getall", "SELECT id,tag,name,desc,isarena,respawnprot,dmgmod,pwrboost,permpwr,flags FROM {{" + TABLE + "}}");
            this.database.prepareStatement("group_store", "INSERT INTO {{" + TABLE + "}} (id,tag,name,desc,isarena,respawnprot,dmgmod,pwrboost,permpwr,flags) VALUES (?,?,?,?,?,?,?,?,?,?)");
            this.database.prepareStatement("group_delete", "DELETE FROM {{" + TABLE + "}} WHERE id=?");
            this.database.prepareStatement("group_clear", "DELETE FROM {{" + TABLE + "}}");
            this.database.prepareStatement("group_update",   "UPDATE {{"+TABLE+"}} SET "+
                                                        "name=?, desc=?, respawnprot=?, dmgmod=?,pwrboost=?,permpwr=?,flags=? WHERE id=?");
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
                + "`tag` varchar(10) NOT NULL,"//TODO limit Tag to 10
                + "`name` varchar(20) NOT NULL,"//TODO limit name to 20
                + "`desc` varchar(42) NOT NULL,"  //TODO limit desc to 42     
                + "`isarena` smallint(2) NOT NULL,"//bool?
                + "`respawnprot` int(3) NOT NULL,"//TODO limit to 999 sec
                + "`dmgmod` string(5) NOT NULL,"//TODO limit to 5  //Format +1 -1 P30 P-30 S1 S-1
                + "`pwrboost` int(11) NOT NULL,"
                + "`permpwr` int(11) DEFAULT NULL,"
                + "`flags` int(11) NOT NULL,"
                + "PRIMARY KEY (`id`),"
                + ") ENGINE=MyISAM DEFAULT CHARSET=latin1;");
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to initialize the Groups-Table !", ex);
        }
    }

    public Group get(int key)
    {
        try
        {
            ResultSet result = this.database.preparedQuery("group_get", key);

            if (!result.next())
            {
                return null;
            }
            int id= result.getInt("id");
            String tag= result.getString("tag");
            String name= result.getString("name");
            String desc= result.getString("desc");
            boolean isarena= result.getBoolean("isarena");
            int respawnprot= result.getInt("respawnprot");
            String dmgmod= result.getString("dmgmod");
            int pwrboost= result.getInt("pwrboost");
            Integer permpwr= result.getInt("permpwr");
            int flags= result.getInt("flags");
            
            return new Group(id,tag,name,desc,isarena,respawnprot,dmgmod,pwrboost,permpwr,flags);

        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the group '" + key + "'!", e);
        }
    }

    public Collection<Group> getAll()
    {
        try
        {
            ResultSet result = this.database.preparedQuery("group_getall");

            Collection<Group> groups = new ArrayList<Group>();
            while (result.next())
            {
                int id= result.getInt("id");
                String tag= result.getString("tag");
                String name= result.getString("name");
                String desc= result.getString("desc");
                boolean isarena= result.getBoolean("isarena");
                int respawnprot= result.getInt("respawnprot");
                String dmgmod= result.getString("dmgmod");
                int pwrboost= result.getInt("pwrboost");
                Integer permpwr= result.getInt("permpwr");
                int flags= result.getInt("flags");
                groups.add(new Group(id,tag,name,desc,isarena,respawnprot,dmgmod,pwrboost,permpwr,flags));
            }

            return groups;
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the groups from the database!", e);
        }
    }

    public void store(Group model)
    {
        try
        {
            int id= model.getId();
            String tag= model.getTag();
            String name= model.getName();
            String desc= model.getDescription();
            boolean isarena = false;
            if (model.getType().equals(AreaType.ARENA)) isarena = true;
            int respawnprot= model.getPvp_respawnprotect();
            Map<Group.DmgModType,Integer> modifiers = model.getDamagemodifier();
            String dmgmod = "0"; //If wrong set to ADD 0
            for (Group.DmgModType type : Group.DmgModType.values())
            {
                Integer tmp = modifiers.get(type);
                if (tmp != null)
                {
                    switch (type)
                    {
                        case ADD:
                        {
                            dmgmod = String.valueOf(tmp);
                            break;
                        }
                        case PERCENT:
                        {
                            dmgmod = "P"+String.valueOf(tmp);
                            break;
                        } 
                        case SET:
                        {
                            dmgmod = "S"+String.valueOf(tmp);
                            break;
                        }
                    }
                    break;
                }
            }
            int pwrboost= model.getPower_boost();
            Integer permpwr= model.getPower_perm();
            int flags= model.getBits().get();
            this.database.preparedExec("group_store", id,tag,name,desc,isarena,respawnprot,dmgmod,pwrboost,permpwr,flags);
            DenyUsageStorage denyuseDB = new DenyUsageStorage();
            denyuseDB.storeByGroup(model);
        }
        catch (Exception e)
        {
            throw new StorageException("Failed to store the group !", e);
        }
    }

    public void update(Group model)
    {
        try
        {
            int id= model.getId();
            String name= model.getName();
            String desc= model.getDescription();
            boolean isarena = false;
            if (model.getType().equals(AreaType.ARENA)) isarena = true;
            int respawnprot= model.getPvp_respawnprotect();
            Map<Group.DmgModType,Integer> modifiers = model.getDamagemodifier();
            String dmgmod = "0"; //If wrong set to ADD 0
            for (Group.DmgModType type : Group.DmgModType.values())
            {
                Integer tmp = modifiers.get(type);
                if (tmp != null)
                {
                    switch (type)
                    {
                        case ADD:
                        {
                            dmgmod = String.valueOf(tmp);
                            break;
                        }
                        case PERCENT:
                        {
                            dmgmod = "P"+String.valueOf(tmp);
                            break;
                        } 
                        case SET:
                        {
                            dmgmod = "S"+String.valueOf(tmp);
                            break;
                        }
                    }
                    break;
                }
            }
            int pwrboost= model.getPower_boost();
            Integer permpwr= model.getPower_perm();
            int flags= model.getBits().get();
            this.database.preparedExec("group_update", name,desc,respawnprot,dmgmod,pwrboost,permpwr,flags, id);
        }
        catch (Exception e)
        {
            throw new StorageException("Failed to update the group !", e);
        }
    }

    public void merge(Group model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean delete(Group model)
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
}
