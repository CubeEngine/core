package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.core.persistence.Database;
import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import de.cubeisland.libMinecraft.bitmask.LongBitMask;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;

/**
 *
 * @author CodeInfection
 */
public class CubeUserStorage implements Storage<String, CubeUser>
//Wärs mit ner ID nicht schneller aus der Datenbank zu holen?
//Den CubeUser müsste ich ja sehr oft benutzen wenn ich zb einen Bidder identifiziere
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
        this.createStructure();
        try
        {
            ResultSet result = this.database.query("SELECT `id`,`name`,`flags` FROM {{PREFIX}}users");

            Collection<CubeUser> users = new ArrayList<CubeUser>();
            while (result.next())
            {
                int id = result.getInt("id");
                OfflinePlayer player = this.server.getOfflinePlayer(result.getString("name"));
                LongBitMask bitmask = new LongBitMask(result.getLong("flags"));
                users.add(new CubeUser(id, player, bitmask));
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
        this.createStructure();
        try
        {
            ResultSet result = this.database.query("SELECT `id`,`name`,`flags` FROM {{PREFIX}}users WHERE name=? LIMIT 1", key);

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
    
    public CubeUser getByKey(int key)
    {
        this.createStructure();
        try
        {
            ResultSet result = this.database.query("SELECT `id`,`name`,`flags` FROM {{PREFIX}}users WHERE id=? LIMIT 1", key);

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

    public boolean store(CubeUser... object)
    {
        this.createStructure();
        for (CubeUser cubeUser : object)
        {
            String name = cubeUser.getName();
            int id = cubeUser.getId();
            LongBitMask bitmask = cubeUser.getFlags();
            this.database.query("INSERT INTO {{PREFIX}}users (`id`, `name`, `flags`)"+
                                "VALUES (?, ?, ?)", id, name, bitmask.get()); 
        }
        return true; //TODO return false
    }

    public int delete(CubeUser... object)
    {
        List<Integer> keys = new ArrayList<Integer>();
        for (CubeUser cubeUser : object)
        {
            keys.add(cubeUser.getId());
        }
        return deleteByKey((Integer[])keys.toArray());
    }

    public int deleteByKey(String... keys)
    {
        int dels = 0;
        for (String s : keys)
        {
            this.database.query("DELETE FROM {{PREFIX}}users WHERE name=?", s);
            ++dels;
        }
        return dels;
    }
    
    public int deleteByKey(Integer... keys)
    {
        int dels = 0;
        for (int i : keys)
        {
            this.database.query("DELETE FROM {{PREFIX}}users WHERE id=?", i);
            ++dels;
        }
        return dels;
    }
    
    public void createStructure()
    {
        this.database.exec( "CREATE TABLE IF NOT EXISTS `users` ("+
                            "`id` int(11) NOT NULL,"+
                            "`name` varchar(11) NOT NULL,"+
                            "`flags` int(11) NOT NULL,"+
                            "PRIMARY KEY (`id`)"+
                            ") ENGINE=MyISAM DEFAULT CHARSET=latin1;"
                    );
    }
    
    public int getNextId()
    {
        this.createStructure();
        try
        {
            ResultSet result = this.database.query("SELECT `id` FROM {{PREFIX}}users ORDER BY id DESC LIMIT 1");
            if (!result.next())
            {
                return 1;
            }
            return result.getInt("id")+1;
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to get next BidId !", e);
        }
    }

}
