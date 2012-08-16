package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.core.storage.Storage;
import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.mysql.MySQLDatabase;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;

/**
 *
 * @author Phillip Schichtel
 */
public class UserStorage implements Storage<User>
{
    private final MySQLDatabase database;
    private final Server server;

    public UserStorage(MySQLDatabase database, Server server)
    {
        this.database = database;
        this.server = server;
        try
        {
            this.database.prepareAndStoreStatement(User.class, "get",      "SELECT id,name,language FROM users WHERE id=? LIMIT 1");
            this.database.prepareAndStoreStatement(User.class, "getall",   "SELECT id,name,language FROM users");
            this.database.prepareAndStoreStatement(User.class, "store",    "INSERT INTO users (name,flags,language) VALUES (?,?,?)");
            this.database.prepareAndStoreStatement(User.class, "update",   "UPDATE users SET language=? WHERE id=?");
            this.database.prepareAndStoreStatement(User.class, "merge",    "INSERT INTO users (name,language) VALUES (?,?) ON DUPLICATE KEY UPDATE language=values(language)");
            this.database.prepareAndStoreStatement(User.class, "delete",   "DELETE FROM users WHERE id=? LIMIT 1");
            this.database.prepareAndStoreStatement(User.class, "clear",    "DELETE FROM users");
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
            this.database.execute(
                this.database.buildQuery().initialize()
                .createTable("users", true)
                .startFields()
                .field("id", AttrType.INT, 0, true, true, true)
                .field("name", AttrType.VARCHAR,16,true)
                .field("language", AttrType.VARCHAR,10,true)
                .primaryKey("id")
                .endFields()
                .engine("MyISAM").defaultcharset("latin1").autoIncrement(1)
                .endCreateTable().end()
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
            PreparedStatement ps = this.database.getStoredStatement(User.class, "store");
            ps.setString(1, user.getName());
            ps.setString(2, user.getLanguage());
            user.setId(this.database.getLastInsertedId(ps));
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to store the user!", e);
        }
    }

    public boolean delete(User object)
    {
        return this.deleteByKey(object.getId());
    }

    public Collection<User> getAll()
    {
        try
        {
            ResultSet result = this.database.preparedQuery(User.class, "getall");

            Collection<User> users = new ArrayList<User>();
            int id;
            OfflinePlayer player;
            String language;
            while (result.next())
            {
                id = result.getInt("id");
                player = this.server.getOfflinePlayer(result.getString("name"));
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

    public User get(Object key)
    {
        try
        {
            ResultSet result = this.database.preparedQuery(User.class, "get", key);

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

    public boolean deleteByKey(Object id)
    {
        try
        {
            return this.database.preparedUpdate(User.class, "delete", id) > 0;
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
            this.database.preparedUpdate(User.class, "update", object.getLanguage(), object.getId());
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
            this.database.preparedUpdate(User.class, "merge", user.getName(), user.getLanguage());
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
            this.database.preparedExecute(User.class, "clear");
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to clear the database!", e);
        }
    }
}