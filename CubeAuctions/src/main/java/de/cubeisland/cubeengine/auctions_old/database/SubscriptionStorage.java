package de.cubeisland.cubeengine.auctions_old.database;

import de.cubeisland.cubeengine.auctions_old.CubeAuctions;
import de.cubeisland.cubeengine.auctions_old.auction.Bidder;
import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import de.cubeisland.cubeengine.core.persistence.database.Database;
import de.cubeisland.cubeengine.core.user.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Anselm Brehme
 */
public class SubscriptionStorage implements Storage<User, Bidder>
{
    private final Database database = CubeAuctions.getDB();
    private final String TABLE = "subscription";

    public SubscriptionStorage()
    {
        this.initialize();
        try
        {
            //this.database.prepareStatement("sub_getall", "SELECT id,cubeuserid,sub FROM {{" + TABLE + "}}");
            this.database.prepareStatement("sub_get_user", "SELECT id,cubeuserid,sub FROM {{" + TABLE + "}} WHERE cubeuserid=?");
            this.database.prepareStatement("sub_store", "INSERT INTO {{" + TABLE + "}} (cubeuserid,sub) VALUES (?,?)");
            this.database.prepareStatement("sub_delete_sub", "DELETE FROM {{" + TABLE + "}} WHERE sub=?");
            this.database.prepareStatement("sub_delete_sub_user", "DELETE FROM {{" + TABLE + "}} WHERE sub=? && cubeuserid=?");
            this.database.prepareStatement("sub_clear", "DELETE FROM {{" + TABLE + "}}");
            //this.database.prepareStatement("sub_update", "UPDATE {{" + TABLE + "}} SET price=? timesold=? WHERE id=?");
            //this.database.prepareStatement("auction_merge",    "INSERT INTO {{"+TABLE+"}} (name,flags) VALUES (?,?) ON DUPLICATE KEY UPDATE flags=values(flags)");
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to prepare the statements!", e);
        }
    }

    public Database getDatabase()
    {
        return this.database;
    }

    public List<String> getListByUser(User key)
    {
        try
        {
            ResultSet result = this.database.preparedQuery("sub_get_user", key.getKey());
            List<String> list = new ArrayList<String>();
            while (result.next())
            {
                list.add(result.getString("sub"));
            }
            return list;
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the Subscriptions for CubeUser '" + key + "'!", e);
        }
    }

    public boolean store(User key, String sub)
    {
        try
        {
            return this.database.preparedExec("sub_store", key.getKey(), sub);
        }
        catch (Exception e)
        {
            throw new StorageException("Failed to store the Subscriptions !", e);
        }
    }

    public void deleteAllOfSub(String sub)
    {
        try
        {
            //macht nur Sinn bei Löschung von AuktionsSubs zb bei Ablauf
            this.database.preparedExec("sub_delete_sub", sub);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to delete the Subscriptions !", ex);
        }

    }

    public void deleteSubByUser(User key, String sub)
    {//Löschen von Sub von einem Spieler
        try
        {
            this.database.preparedExec("sub_delete_sub_user", sub, key.getKey());
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to delete the Subscription !", ex);
        }

    }

    public void initialize()
    {
        try
        {
            this.database.exec("CREATE TABLE IF NOT EXISTS `subscription` ("
                    + "`id` int(11) NOT NULL AUTO_INCREMENT,"
                    + "`cubeuserid` int(11) NOT NULL,"
                    + "`sub` varchar(42) NOT NULL,"
                    + "PRIMARY KEY (`id`),"
                    + "FOREIGN KEY (`cubeuserid`) REFERENCES bidder(id)"
                    + ") ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;");
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to initialize the Subscription-Table !", ex);
        }
    }

    public void clear()
    {
        try
        {
            this.database.preparedExec("sub_clear");
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to clear the database!", e);
        }
    }

    public Bidder get(User key)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<Bidder> getAll()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void store(Bidder model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void update(Bidder model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void merge(Bidder model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean delete(Bidder model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean delete(User key)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}