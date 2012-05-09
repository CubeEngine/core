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
public class CubeUserStorage implements Storage<String, CubeUser>
{
    private final Database database;
    private final Server server;

    public CubeUserStorage(Database db, Server server)
    {
        this.database = db;
        this.server = server;
    }

    public Database getDatabase()
    {
        return this.database;
    }

    public Collection<CubeUser> getAll()
    {
        try
        {
            ResultSet result = this.database.query("SELECT `name`,`flags` FROM {{PREFIX}}users");

            OfflinePlayer player;
            LongBitMask bitmask;
            Collection<CubeUser> users = new ArrayList<CubeUser>();
            while (result.next())
            {
                player = this.server.getOfflinePlayer(result.getString("name"));
                bitmask = new LongBitMask(result.getLong("flags"));
                users.add(new CubeUser(player, bitmask));
            }

            return users;
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the users from the database!", e);
        }
    }

    public CubeUser getByKey(String key)
    {
        try
        {
            ResultSet result = this.database.query("SELECT `name`,`flags` FROM {{PREFIX}}users WHERE name=? LIMIT 1", key);

            if (!result.next())
            {
                return null;
            }

            OfflinePlayer player = this.server.getOfflinePlayer(result.getString("name"));
            LongBitMask bitmask = new LongBitMask(result.getLong("flags"));
            return new CubeUser(player, bitmask);

        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the user '" + key + "'!", e);
        }

    }

    public boolean store(CubeUser... object)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int delete(CubeUser... object)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int deleteByKey(String... keys)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
