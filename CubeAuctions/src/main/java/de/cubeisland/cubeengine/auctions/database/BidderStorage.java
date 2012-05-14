package de.cubeisland.cubeengine.auctions.database;

import de.cubeisland.cubeengine.auctions.CubeAuctions;
import de.cubeisland.cubeengine.auctions.auction.Bidder;
import de.cubeisland.cubeengine.core.persistence.Database;
import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Faithcaio
 */
public class BidderStorage implements Storage<Bidder>
{
    private final Database database = CubeAuctions.getDB();
    private final String TABLE = "bidder";

    public BidderStorage()
    {

        try
        {
            this.database.prepareStatement("user_get", "SELECT cubeuserid,notifystate FROM {{" + TABLE + "}} WHERE cubeuserid=? LIMIT 1");
            this.database.prepareStatement("user_getall", "SELECT cubeuserid,notifystate FROM {{" + TABLE + "}}");
            this.database.prepareStatement("user_store", "INSERT INTO {{" + TABLE + "}} (cubeuserid,notifystate) VALUES (?,?)");
            this.database.prepareStatement("user_update", "UPDATE {{" + TABLE + "}} SET notifystate=? WHERE cubeuserid=?");
            this.database.prepareStatement("user_delete", "DELETE FROM {{" + TABLE + "}} WHERE id=?");
            this.database.prepareStatement("user_clear", "DELETE FROM {{" + TABLE + "}}");
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
            ResultSet result = this.database.preparedQuery("user_getall");

            Collection<Bidder> bidder = new ArrayList<Bidder>();
            while (result.next())
            {
                int cubeuserid = result.getInt("cubeuserid");
                byte notifyState = result.getByte("notifystate");
                bidder.add(new Bidder(cubeuserid, notifyState));
            }

            return bidder;
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
                + //"FOREIGN KEY (`cubeuserid`) REFERENCES cubeuser(id)"+
                ") ENGINE=MyISAM DEFAULT CHARSET=latin1;");
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to initialize the Bidder-Table !", ex);
        }
    }

    public Bidder get(int key)
    {
        try
        {
            ResultSet result = this.database.preparedQuery("bidder_get", key);

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
        this.initialize();
        try
        {
            int cubeuserid = model.getId();
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
            this.database.preparedExec("bidder_update", model.getNotifyState(), model.getId());
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
        return this.delete(model.getId());
    }

    public boolean delete(int id)
    {
        try
        {
            return this.database.preparedExec("bidder_delete", id);
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
