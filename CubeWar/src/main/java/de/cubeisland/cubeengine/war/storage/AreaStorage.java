package de.cubeisland.cubeengine.war.storage;

import de.cubeisland.cubeengine.core.persistence.Database;
import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import de.cubeisland.cubeengine.war.CubeWar;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import org.bukkit.Chunk;
import org.bukkit.Server;

/**
 *
 * @author Faithcaio
 */
public class AreaStorage implements Storage<AreaModel>
{

    private final Database database = CubeWar.getDB();
    private final String TABLE = "area";
    private Server server = CubeWar.getInstance().getServer();
    private static AreaStorage instance = null;

    public static AreaStorage get()
    {
        if (instance == null)
        {
            instance = new AreaStorage();
        }
        return instance;
    }
    

    public AreaStorage()
    {
        this.initialize();
        try
        {
            this.database.prepareStatement("area_get", "SELECT groupid {{" + TABLE + "}} WHERE id=? LIMIT 1");
            this.database.prepareStatement("area_getall", "SELECT * FROM {{" + TABLE + "}}");
            this.database.prepareStatement("area_store", "INSERT INTO {{" + TABLE + "}} (id,world,x,z,groupid) VALUES (?,?,?,?,?)");
            this.database.prepareStatement("area_delete", "DELETE FROM {{" + TABLE + "}} WHERE id=?");
           
            this.database.prepareStatement("area_clear", "DELETE FROM {{" + TABLE + "}}");
            this.database.prepareStatement("area_update", "UPDATE {{" + TABLE + "}} SET groupid=? WHERE id=?");
            
            this.database.prepareStatement("area_delete_group", "DELETE FROM {{" + TABLE + "}} WHERE groupid=?");
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
                    + "`id` int(11) NOT NULL,"
                    + "`world` varchar(42) NOT NULL,"
                    + "`x` int(10) NOT NULL,"
                    + "`z` int(11) NOT NULL,"
                    + "`groupid` int(11) NOT NULL,"
                    + "PRIMARY KEY (`id`)"
                    + ") ENGINE=MyISAM DEFAULT CHARSET=latin1;");
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to initialize the AreaStorage-Table !", ex);
        }
    }

    public Collection<AreaModel> getAll()
    {
        Collection<AreaModel> areas = new ArrayList<AreaModel>();
        try
        {
            ResultSet result = this.database.preparedQuery("area_getall");
            while (result.next())
            {
                Chunk chunk = server.getWorld("world").getChunkAt(result.getInt("x"), result.getInt("z"));
                Group group = GroupControl.get().getGroup(result.getInt("groupid"));
                areas.add(new AreaModel(chunk,group));
            }

            return areas;
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the areas from the database!", e);
        }
    }

    public void store(AreaModel model)
    {
        try
        {
            this.database.preparedExec("area_store", model.getId(), model.getWorld().getName(), model.getX(), model.getZ(), model.getGroup().getId());
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to store the Chunk X:" + model.getX() + " Z:" + model.getZ() + " !", ex);
        }
    }

    public void update(AreaModel model)
    {
        try
        {
            this.database.preparedExec("area_update", model.getGroup().getId(), model.getId());
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to update the Chunk X:" + model.getX() + " Z:" + model.getZ() + " !", ex);
        }
    }

    public boolean delete(AreaModel model)
    {
        return this.delete(model.getId());
    }

    public boolean delete(int id)
    {
        try
        {
            return this.database.preparedExec("area_delete", id);
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
    
    public AreaModel get(int key)
    {
        try
        {
            ResultSet result = this.database.preparedQuery("area_getall");
            if (!result.next())
            {
                return null;
            }
            Chunk chunk = server.getWorld("world").getChunkAt(result.getInt("x"), result.getInt("z"));
            Group group = GroupControl.get().getGroup(result.getInt("groupid"));
            return new AreaModel(chunk,group);
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the area from the database!", e);
        }
    }
    
    public void merge(AreaModel model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
    
}
