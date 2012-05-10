package de.cubeisland.cubeengine.auctions.database;

import de.cubeisland.cubeengine.auctions.Util;
import de.cubeisland.cubeengine.auctions.auction.Auction;
import de.cubeisland.cubeengine.auctions.auction.Bidder;
import de.cubeisland.cubeengine.core.persistence.Database;
import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import de.cubeisland.cubeengine.core.user.CubeUser;
import de.cubeisland.cubeengine.core.user.CubeUserManager;
import de.cubeisland.libMinecraft.bitmask.LongBitMask;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;



/**
 *
 * @author Faithcaio
 */
public class AuctionStorage implements Storage<Integer, Auction>{

    private final Database database;
    private final Server server;
    private CubeUserManager cuManager;

    public AuctionStorage(Database db, Server server)
    {
        this.database = db;
        this.server = server;
    }

    public Collection<Auction> getAll()
    {
        try
        {
            ResultSet result = this.database.query("SELECT `id`,`item`,`amount`,`cubeuserid`,`timestamp` FROM {{PREFIX}}auctions");

            Collection<Auction> auctions = new ArrayList<Auction>();
            while (result.next())
            {
                int id = result.getInt("id");
                ItemStack item = Util.convertItem(result.getString("item"), result.getShort("amount"));
                CubeUser owner = cuManager.getCubeUser(result.getInt("cubeuserid"));//TODO convert to CubeUser;
                long auctionEnd = result.getTimestamp("timestamp").getTime();
                auctions.add(new Auction(id, item, (Bidder)owner, auctionEnd));
                //Constructor:
                    //public Auction(int id,ItemStack item, Bidder owner, long auctionEnd)
            }

            return auctions;
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the auctions from the database!", e);
        }
    }

    public Auction getByKey(Integer key)
    {
        try
        {
            ResultSet result = this.database.query("SELECT `id`,`item`,`amount`,`cubeuserid`,`timestamp` FROM {{PREFIX}}auctions WHERE id=? LIMIT 1", key);

            if (!result.next())
            {
                return null;
            }
            int id = result.getInt("id");
            ItemStack item = Util.convertItem(result.getString("item"),result.getShort("amount"));
            CubeUser owner = cuManager.getCubeUser(result.getInt("cubeuserid"));//TODO convert to CubeUser
            long auctionEnd = result.getTimestamp("timestamp").getTime();
            return new Auction(id, item, (Bidder)owner, auctionEnd);
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the auction '" + key + "'!", e);
        }
    }

    public boolean store(Auction... object)
    {
        for (Auction auction : object)
        {
            int id = auction.getId();
            int cubeUserId = auction.getOwner().getId();//TODO CubeUser ID
            String item = Util.convertItem(auction.getItemStack());
            int amount = auction.getItemStack().getAmount();
            Timestamp time = auction.getTimestamp();
            this.database.query("INSERT INTO {{PREFIX}}auctions (`id`, `cubeuserid`, `item`, `amount`, `timestamp`)"+
                                "VALUES (?, ?, ?, ?, ?)", id, cubeUserId, item, amount, time); 
        }
        return true; //TODO
       
    }

    public int delete(Auction... object)
    {
        List<Integer> keys = new ArrayList<Integer>();
        for (Auction auction : object)
        {
            keys.add(auction.getId());
        }
        return deleteByKey((Integer[])keys.toArray());
    }

    public int deleteByKey(Integer... keys)
    {
        int dels = 0;
        for (int i : keys)
        {
            this.database.query("DELETE FROM {{PREFIX}}auctions WHERE id=?", i);
            ++dels;
        }
        return dels;
    }

    public Database getDatabase()
    {
        return this.database;
    }
}
