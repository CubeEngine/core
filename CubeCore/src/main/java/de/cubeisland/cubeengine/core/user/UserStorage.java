package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import de.cubeisland.cubeengine.core.persistence.database.Database;
import de.cubeisland.cubeengine.core.util.bitmask.LongBitMask;
import java.sql.PreparedStatement;
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
            this.database.prepareStatement("user_get",      "SELECT id,name,language FROM {{users}} WHERE id=? LIMIT 1");
            this.database.prepareStatement("user_getall",   "SELECT id,name,language FROM {{users}}");
            this.database.prepareStatement("user_store",    "INSERT INTO {{users}} (name,flags,language) VALUES (?,?,?)");
            this.database.prepareStatement("user_update",   "UPDATE {{users}} SET language=? WHERE id=?");
            this.database.prepareStatement("user_merge",    "INSERT INTO {{users}} (name,language) VALUES (?,?) ON DUPLICATE KEY UPDATE language=values(language)");
            this.database.prepareStatement("user_delete",   "DELETE FROM {{users}} WHERE id=? LIMIT 1");
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
            this.database.exec(
                "CREATE TABLE IF NOT EXISTS `{{users}}` (" +
                "  `id` int(11) unsigned NOT NULL AUTO_INCREMENT," +
                "  `name` varchar(16) NOT NULL," +
                "  `language` varchar(10) NOT NULL," +
                "  PRIMARY KEY (`id`)" +
                ") ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;"
            );
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to initialize the UserTable !", ex);
        }
    }
    
    public void store(User user)
    {
        try
        {
            PreparedStatement ps = this.database.getStatement("user_store");
            ps.setString(1, user.getName());
            ps.setString(2, user.getLanguage());
            this.database.assignId(ps,user);
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
            String language;
            while (result.next())
            {
                id = result.getInt("id");
                player = this.server.getOfflinePlayer(result.getString("name"));
                bitmask = new LongBitMask(result.getLong("flags"));
                language = result.getString("language");
                User user = new User(id, player, language);
                users.add(user);
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
            ResultSet result = this.database.preparedQuery("user_get", key);

            if (!result.next())
            {
                return null;
            }
            
            int id = result.getInt("id");
            OfflinePlayer player = this.server.getOfflinePlayer(result.getString("name"));
            String language = result.getString("language");
            return new User(id, player, language);

        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the user '" + key + "'!", e);
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
            this.database.preparedUpdate("user_update", object.getLanguage(), object.getId());
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to update the entry!", e);
        }
    }

    public void merge(User user)
    {
        try
        {
            this.database.preparedUpdate("user_merge", user.getName(), user.getLanguage());
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
