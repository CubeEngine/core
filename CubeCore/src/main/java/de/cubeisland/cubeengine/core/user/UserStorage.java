package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.core.storage.BasicStorage;
import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.database.Database;
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
public class UserStorage extends BasicStorage<User>
{
    private final Server server;

    public UserStorage(Database database, Server server)
    {
        super(database, User.class);
        this.server = server;
    }
    
    public void store(User user)
    {
        try
        {
            PreparedStatement ps = this.database.getStoredStatement(model, "store");
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
            ResultSet result = this.database.preparedQuery(model, "getall");

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
            ResultSet result = this.database.preparedQuery(model, "get", key);

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
            return this.database.preparedUpdate(model, "delete", id) > 0;
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
            this.database.preparedUpdate(model, "update", object.getLanguage(), object.getId());
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
            this.database.preparedUpdate(model, "merge", user.getName(), user.getLanguage());
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
            this.database.preparedExecute(model, "clear");
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to clear the database!", e);
        }
    }
}