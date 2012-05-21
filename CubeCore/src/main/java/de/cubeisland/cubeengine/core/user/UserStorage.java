package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import de.cubeisland.cubeengine.core.persistence.database.Database;
import de.cubeisland.cubeengine.core.util.bitmask.LongBitMask;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;

/**
 *
 * @author CodeInfection
 */
public class UserStorage implements Storage<User>
{
    private final Database database;
    private final Server server;

    public UserStorage(Database database, Server server)
    {
        this.database = database;
        this.server = server;
        try
        {
            this.database.prepareStatement("user_get",      "SELECT id,name,flags FROM {{users}} WHERE name=? LIMIT 1");
            this.database.prepareStatement("user_getall",   "SELECT id,name,flags FROM {{users}}");
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
            this.database.exec( "CREATE TABLE IF NOT EXISTS `"+this.database.getTablePrefix()+"users` ("+
                                "`id` int(11) unsigned NOT NULL AUTO_INCREMENT,"+
                                "`name` varchar(16) NOT NULL,"+
                                "`flags` int(11) NOT NULL,"+
                                "PRIMARY KEY (`id`)"+
                                ") ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;"
                               );
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to initialize the CubeUserTable !", ex);
        }
    }
    
    public void store(User model)
    {
        try
        {
            PreparedStatement ps = this.database.getStatement("user_store");
            ps.setString(1, model.getName());
            ps.setLong(2, model.getFlags().get());
            if (ps.executeUpdate() > 0) 
            {
                final ResultSet result = ps.getGeneratedKeys();
                if (result.next())
                {
                    model.setId(result.getInt("GENERATED_KEY"));
                }
            }
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to store the user!", e);
        }
    }

    public boolean delete(User object)
    {
        return delete(object.getId());
    }

    public Collection<User> getAll()
    {
        try
        {
            ResultSet result = this.database.preparedQuery("user_getall");

            Collection<User> users = new ArrayList<User>();
            int id;
            OfflinePlayer player;
            LongBitMask bitmask;
            while (result.next())
            {
                id = result.getInt("id");
                player = this.server.getOfflinePlayer(result.getString("name"));
                bitmask = new LongBitMask(result.getLong("flags"));
                
                users.add(new User(id, player, bitmask));
            }

            return users;
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the users from the database!", e);
        }
    }

    public User get(int key)
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
            return new User(id, player, bitmask);

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

    public void update(User object)
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

    public void merge(User object)
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
