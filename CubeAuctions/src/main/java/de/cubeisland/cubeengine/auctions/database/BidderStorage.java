package de.cubeisland.cubeengine.auctions.database;

import de.cubeisland.cubeengine.auctions.auction.Bidder;
import de.cubeisland.cubeengine.core.persistence.Database;
import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.Server;

/**
 *
 * @author Faithcaio
 */
public class BidderStorage implements Storage<Integer, Bidder>
{

    private final Database database;
    private final Server server;

    public BidderStorage(Database db, Server server)
    {
        this.database = db;
        this.server = server;
    }

    public Database getDatabase()
    {
        return this.database;
    }

    public Collection<Bidder> getAll()
    {
        try
        {
            ResultSet result = this.database.query("SELECT `cubeuserid`,`notifystate` FROM {{PREFIX}}bidder");

            Collection<Bidder> bidder = new ArrayList<Bidder>();
            while (result.next())
            {
                int cubeuserid = result.getInt("cubeuserid");
                byte notifyState = result.getByte("notifystate");
                bidder.add(new Bidder(cubeuserid , notifyState));
            }

            return bidder;
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the bidder from the database!", e);
        }
    }

    public Bidder getByKey(Integer key)
    {
        try
        {
            ResultSet result = this.database.query("SELECT `cubeuserid`,`notifystate` FROM {{PREFIX}}bidder WHERE cubeuserid=? LIMIT 1", key);

            if (!result.next())
            {
                return null;
            } 
            
            int cubeuserid = result.getInt("cubeuserid");
            byte notifyState = result.getByte("notifystate");
            return new Bidder(cubeuserid , notifyState);
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the Bidder '" + key + "'!", e);
        }
    }

    public boolean store(Bidder... object)
    {
        try
        {
            for (Bidder bidder : object)
            {

                int cubeuserid = bidder.getId();
                byte notifyState = bidder.getNotifyState();

                this.database.query("INSERT INTO {{PREFIX}}bidder (`cubeuserid`, `notifystate`)"+
                                    "VALUES (?, ?)", cubeuserid, notifyState); 
            }
            return true;
        }
        catch (Exception e)
        {
            throw new StorageException("Failed to store the Bidder !", e);
        }
        
    }

    public int delete(Bidder... object)
    {
        List<Integer> keys = new ArrayList<Integer>();
        for (Bidder bidder : object)
        {
            keys.add(bidder.getId());
        }
        return deleteByKey((Integer[])keys.toArray());
    }

    public int deleteByKey(Integer... keys)
    {
        int dels = 0;
        for (int i : keys)
        {
            this.database.query("DELETE FROM {{PREFIX}}bids WHERE cubeuserid=?", i);
            ++dels;
        }
        return dels;
    }

}
