package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.persistence.Database;
import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import de.cubeisland.libMinecraft.bitmask.LongBitMask;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;

/**
 *
 * @author CodeInfection
 */
public class CubeUserStorage implements Storage<CubeUser>
{
    private final Database database = CubeCore.getDB();
    private final Server server = CubeCore.getInstance().getServer();

    public CubeUserStorage()
    {
        this.initialize();
        try
        {
            this.database.prepareStatement("user_get",      "SELECT id,name,flags FROM {{users}} WHERE name=? LIMIT 1");
            this.database.prepareStatement("user_getall",   "SELECT id,name,flags FROM {{users}}");
            this.database.prepareStatement("user_store_server",    "INSERT INTO {{users}} (id,name,flags) VALUES (0,?,?)");//TODO es wird 1 statt 0 eingesetzt
            this.database.prepareStatement("user_store",    "INSERT INTO {{users}} (name,flags) VALUES (?,?)");
            this.database.prepareStatement("user_update",   "UPDATE {{users}} SET flags=? WHERE id=?");
            this.database.prepareStatement("user_merge",    "INSERT INTO {{users}} (name,flags) VALUES (?,?) ON DUPLICATE KEY UPDATE flags=values(flags)");
            this.database.prepareStatement("user_delete",   "DELETE FROM {{users}} WHERE id=?");
            this.database.prepareStatement("user_clear",    "DELETE FROM {{users}}");
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
            this.database.exec( "CREATE TABLE IF NOT EXISTS `users` ("+
                                "`id` int(11) NOT NULL AUTO_INCREMENT,"+
                                "`name` varchar(16) NOT NULL,"+
                                "`flags` int(11) NOT NULL,"+
                                "PRIMARY KEY (`id`)"+
                                ") ENGINE=MyISAM DEFAULT CHARSET=latin1;"
                               );
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to initialize the CubeUserTable !", ex);
        }
    }

    public Collection<CubeUser> getAll()
    {
        try
        {
            ResultSet result = this.database.preparedQuery("user_getall");

            Collection<CubeUser> users = new ArrayList<CubeUser>();
            int id;
            OfflinePlayer player;
            LongBitMask bitmask;
            while (result.next())
            {
                id = result.getInt("id");
                player = this.server.getOfflinePlayer(result.getString("name"));
                bitmask = new LongBitMask(result.getLong("flags"));
                
                users.add(new CubeUser(id, player, bitmask));
            }

            return users;
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the users from the database!", e);
        }
    }

    public CubeUser get(int key)
    {
        try
        {
            ResultSet result = this.database.preparedQuery("user_get", key);//WHERE name=? LIMIT 1 das wird so nichts

            if (!result.next())
            {
                return null;
            }
            
            int id = result.getInt("id");
            OfflinePlayer player = this.server.getOfflinePlayer(result.getString("name"));
            LongBitMask bitmask = new LongBitMask(result.getLong("flags"));
            return new CubeUser(id, player, bitmask);

        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the user '" + key + "'!", e);
        }
    }
    
    public int getCubeUserId(String name)
    {
        try
        {
            ResultSet result = this.database.preparedQuery("user_get", name);

            if (!result.next())
            {
                return -1;
            }
            
            return result.getInt("id");

        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the user '" + name + "'!", e);
        }
    }

    public void store(CubeUser model)
    {
        try
        {
            if (model.getId() == 0)
                this.database.preparedExec("user_store_server", "#Server", 0);
            else
                this.database.preparedExec("user_store", model.getName(), model.getFlags().get());
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to store the user!", e);
        }
    }

    public boolean delete(CubeUser object)
    {
        return delete(object.getId());
    }

    public boolean delete(int id)
    {
        try
        {
            return this.database.preparedUpdate("user_delete", id) > 0;
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to delete the entry!", e);
        }
    }

    public void update(CubeUser object)
    {
        try
        {
            this.database.preparedUpdate("user_update", object.getFlags(), object.getId());
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to update the entry!", e);
        }
    }

    public void merge(CubeUser object)
    {
        try
        {
            this.database.preparedUpdate("user_merge", object.getFlags());
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to merge the entry!", e);
        }
    }

    public void clear()
    {
        try
        {
            this.database.preparedExec("user_clear");
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to clear the database!", e);
        }
    }
}
