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

/**
 *
 * @author Anselm Brehme
 */
public class BidderStorage implements Storage<User, Bidder>
{
    private final Database database = CubeAuctions.getDB();
    private final String TABLE = "bidder";

    public BidderStorage()
    {
        this.initialize();
        try
        {
            this.database.prepareStatement("bidder_get", "SELECT cubeuserid,notifystate FROM {{" + TABLE + "}} WHERE cubeuserid=? LIMIT 1");
            this.database.prepareStatement("bidder_getall", "SELECT cubeuserid,notifystate FROM {{" + TABLE + "}}");
            this.database.prepareStatement("bidder_store", "INSERT INTO {{" + TABLE + "}} (cubeuserid,notifystate) VALUES (?,?)");
            this.database.prepareStatement("bidder_update", "UPDATE {{" + TABLE + "}} SET notifystate=? WHERE cubeuserid=?");
            this.database.prepareStatement("bidder_delete", "DELETE FROM {{" + TABLE + "}} WHERE id=?");
            this.database.prepareStatement("bidder_clear", "DELETE FROM {{" + TABLE + "}}");
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

    public Collection<Bidder> getAll()
    {
        try
        {
            ResultSet result = this.database.preparedQuery("bidder_getall");

            Collection<Bidder> bidders = new ArrayList<Bidder>();
            while (result.next())
            {
                int cubeuserid = result.getInt("cubeuserid");
                byte notifyState = result.getByte("notifystate");
                Bidder bidder = new Bidder(cubeuserid, notifyState);
                bidders.add(bidder);
            }

            return bidders;
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the bidder from the database!", e);
        }
    }

    public void initialize()
    {
        try
        {
            this.database.exec("CREATE TABLE IF NOT EXISTS `bidder` ("
                + "`cubeuserid` int(11) NOT NULL,"
                + "`notifystate` smallint(2) NOT NULL,"
                + "PRIMARY KEY (`cubeuserid`)"
                +") ENGINE=MyISAM DEFAULT CHARSET=latin1;");
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to initialize the Bidder-Table !", ex);
        }
    }

    public Bidder get(User key)
    {
        try
        {
            ResultSet result = this.database.preparedQuery("bidder_get", key.getKey());

            if (!result.next())
            {
                return null;
            }

            int cubeuserid = result.getInt("cubeuserid");
            byte notifyState = result.getByte("notifystate");
            return new Bidder(cubeuserid, notifyState);
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the Bidder '" + key + "'!", e);
        }
    }

    public void store(Bidder model)
    {
        try
        {
            int cubeuserid = model.getKey().getKey();
            byte notifyState = model.getNotifyState();

            this.database.preparedExec("bidder_store", cubeuserid, notifyState);
        }
        catch (Exception e)
        {
            throw new StorageException("Failed to store the Bidder !", e);
        }
    }

    public void update(Bidder model)
    {
        try
        {
            this.database.preparedExec("bidder_update", model.getNotifyState(), model.getKey());
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to update the Bidder !", ex);
        }
    }

    public void merge(Bidder model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean delete(Bidder model)
    {
        return this.delete(model.getKey());
    }

    public boolean delete(User key)
    {
        try
        {
            return this.database.preparedExec("bidder_delete", key.getKey());
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to delete the Bidder !", ex);
        }
    }

    public void clear()
    {
        try
        {
            this.database.preparedExec("bidder_clear");
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to clear the database!", e);
        }
    }
}
