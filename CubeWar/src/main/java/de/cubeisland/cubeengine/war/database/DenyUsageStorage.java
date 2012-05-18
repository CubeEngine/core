package de.cubeisland.cubeengine.war.database;

import de.cubeisland.cubeengine.core.persistence.Database;
import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import de.cubeisland.cubeengine.war.CubeWar;
import de.cubeisland.cubeengine.war.groups.Group;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.Material;

/**
 *
 * @author Anselm
 */
public class DenyUsageStorage implements Storage<Group>
{

    private final Database database = CubeWar.getDB();
    private final String TABLE = "denyusage";

    public DenyUsageStorage()
    {
        this.initialize();
        try
        {
            this.database.prepareStatement("denyuse_getall_group", "SELECT deny FROM {{" + TABLE + "}} WHERE groupid=?");
            this.database.prepareStatement("denyuse_store", "INSERT INTO {{" + TABLE + "}} (groupid,deny) VALUES (?,?)");
            this.database.prepareStatement("denyuse_deleteall_group", "DELETE FROM {{" + TABLE + "}} WHERE groupid=?");
            this.database.prepareStatement("denyuse_clear", "DELETE FROM {{" + TABLE + "}}");
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
            this.database.exec("CREATE TABLE IF NOT EXISTS `denyusage` ("
                    + "`id` int(10) unsigned NOT NULL AUTO_INCREMENT,"
                    + "`groupid` int(10) unsigned NOT NULL,"
                    + "`deny` varchar(42) NOT NULL,"
                    + "PRIMARY KEY (`id`)"
                    + ") ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;");
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to initialize the UsageDeny-Table !", ex);
        }
    }

    public List<Material> getAllMatByGroup(Group group)
    {
        List<String> list = this.getAllByGroup(group);
        List<Material> newlist = new ArrayList<Material>();
        for (String s : list)
        {
            if (Material.matchMaterial(s) != null)
            {
                newlist.add(Material.matchMaterial(s));
            }
        }

        return newlist;
    }

    public List<String> getAllCmdByGroup(Group group)
    {
        List<String> list = this.getAllByGroup(group);
        List<String> newlist = new ArrayList<String>();
        for (String s : list)
        {
            if (s.startsWith("CMD_"))
            {
                newlist.add(s.substring(4));
            }
        }

        return newlist;
    }

    public List<String> getAllByGroup(Group group)
    {
        return this.getAllByGroup(group.getId());
    }

    public List<String> getAllByGroup(int groupId)
    {
        List<String> list = new ArrayList<String>();
        try
        {
            ResultSet result = this.database.preparedQuery("denyuse_getall_group", groupId);
            while (result.next())
            {
                list.add(result.getString("deny"));
            }
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to get All the UsageDeny !", ex);
        }
        return list;
    }

    public void store(List<String> list, Group group)
    {
        for (String s : list)
        {
            this.store(s, group.getId());
        }
    }

    public void storeByGroup(Group group)
    {
        int id = group.getId();
        for (Material m : group.getProtect())
        {
            this.store(m.toString(), id);
        }
        for (String s : group.getDenycommands())
        {
            this.store("CMD_" + s, id);
        }
    }

    public void store(String s, Group group)
    {
        this.store(s, group.getId());
    }

    public void store(String s, int groupid)
    {
        try
        {
            this.database.preparedExec("denyuse_store", groupid, s);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to store the UsageDeny !", ex);
        }
    }

    public void deleteByGroup(Group group)
    {
        this.deleteByGroup(group.getId());
    }

    public void deleteByGroup(int id)
    {
        try
        {
            this.database.preparedExec("denyuse_deleteall_group", id);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to store the UsageDeny !", ex);
        }
    }

    public void clear()
    {
        try
        {
            this.database.preparedExec("denyuse_clear");
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to clear the database!", e);
        }
    }

    public Group get(int key){throw new UnsupportedOperationException("No need.");}
    public Collection<Group> getAll(){throw new UnsupportedOperationException("No need.");}
    public void store(Group model){throw new UnsupportedOperationException("No need.");}
    public void update(Group model){throw new UnsupportedOperationException("No need.");}
    public void merge(Group model){throw new UnsupportedOperationException("No need.");}
    public boolean delete(Group model){throw new UnsupportedOperationException("No need.");}
    public boolean delete(int id){throw new UnsupportedOperationException("No need.");}
}
