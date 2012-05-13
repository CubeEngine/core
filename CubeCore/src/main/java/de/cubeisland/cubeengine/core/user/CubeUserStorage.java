package de.cubeisland.cubeengine.core.user;

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
    private final Database database;
    private final Server server;

    public CubeUserStorage(Database db, Server server)
    {
        this.database = db;
        this.server = server;

        try
        {
            this.database.prepareStatement("user_get",      "SELECT id,name,flags FROM {{users}} WHERE name=? LIMIT 1");
            this.database.prepareStatement("user_getall",   "SELECT id,name,flags FROM {{users}}");
            this.database.prepareStatement("user_store",    "INSERT INTO {{users}} (id,name,flags) VALUES (?,?,?)");
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<CubeUser> getAll()
    {
        try
        {
            ResultSet result = this.database.query("");

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
            ResultSet result = this.database.preparedQuery("user_get", key);

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

    public void store(CubeUser model)
    {
        try
        {
            this.database.preparedExec("user_store", model.getId(), model.getName(), model.getFlags().get());
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
