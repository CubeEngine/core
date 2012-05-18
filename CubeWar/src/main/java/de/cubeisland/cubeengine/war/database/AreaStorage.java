package de.cubeisland.cubeengine.war.database;

import de.cubeisland.cubeengine.core.persistence.Database;
import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import de.cubeisland.cubeengine.war.CubeWar;
import de.cubeisland.cubeengine.war.area.AreaControl;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import org.bukkit.Server;

/**
 *
 * @author Anselm
 */
public class AreaStorage implements Storage<AreaControl>
{

    private final Database database = CubeWar.getDB();
    private final String TABLE = "area";
    private Server server = CubeWar.getInstance().getServer();
    //TODO add multiverse support

    public AreaStorage()
    {
        this.initialize();
        try
        {
            this.database.prepareStatement("area_get", "SELECT groupid {{" + TABLE + "}} WHERE x=? && z=? LIMIT 1");
            this.database.prepareStatement("area_getall", "SELECT * FROM {{" + TABLE + "}}");
            this.database.prepareStatement("area_store", "INSERT INTO {{" + TABLE + "}} (groupid,x,z) VALUES (?,?,?)");
            this.database.prepareStatement("area_delete", "DELETE FROM {{" + TABLE + "}} WHERE x=? && z=?");
            this.database.prepareStatement("area_delete_group", "DELETE FROM {{" + TABLE + "}} WHERE groupid=?");
            this.database.prepareStatement("area_clear", "DELETE FROM {{" + TABLE + "}}");
            this.database.prepareStatement("area_update", "UPDATE {{" + TABLE + "}} SET groupid=? WHERE x=? && z=?");
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
            this.database.exec("CREATE TABLE IF NOT EXISTS `area` ("
                    + "`id` int(11) NOT NULL AUTO_INCREMENT,"
                    + "`groupid` int(11) NOT NULL,"
                    + "`x` int(10) NOT NULL,"
                    + "`z` int(11) NOT NULL,"
                    + "PRIMARY KEY (`id`)"
                    + ") ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;");
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to initialize the UsageDeny-Table !", ex);
        }
    }

    

    /**
     *
     * @return Area filled with all Areas from DB
     */
    public AreaControl load()
    {
        AreaControl areas = CubeWar.getInstance().getAreas();
        try
        {
            ResultSet result = this.database.preparedQuery("area_getall");
            while (result.next())
            {
                areas.load(server.getWorld("world").getChunkAt(result.getInt("x"), result.getInt("z")), result.getInt("groupid"));
            }

            return areas;
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the areas from the database!", e);
        }
    }

    public void store(int groupid, int x, int z)
    {
        try
        {
            this.database.preparedExec("area_store", groupid, x, z);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to store the Chunk X:" + x + " Z:" + z + " !", ex);
        }
    }

    public void delete(int x, int z)
    {
        try
        {
            this.database.preparedExec("area_delete", x, z);
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to delete the Chunk!", e);
        }
    }

    public void deleteByGroup(int groupid)
    {
        try
        {
            this.database.preparedExec("area_delete_group", groupid);
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to delete the Chunk!", e);
        }
    }

    public void clear()
    {
        try
        {
            this.database.preparedExec("area_clear");
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to clear the database!", e);
        }
    }

    public AreaControl get(int key){throw new UnsupportedOperationException("No need.");}
    public Collection<AreaControl> getAll(){throw new UnsupportedOperationException("No need.");}
    public void store(AreaControl model){throw new UnsupportedOperationException("No need.");}
    public void update(AreaControl model){throw new UnsupportedOperationException("No need.");}
    public void merge(AreaControl model){throw new UnsupportedOperationException("No need.");}
    public boolean delete(AreaControl model){throw new UnsupportedOperationException("No need.");}
    public boolean delete(int id){throw new UnsupportedOperationException("No need.");}
    public int get(int x, int z){throw new UnsupportedOperationException("No need.");}
}
