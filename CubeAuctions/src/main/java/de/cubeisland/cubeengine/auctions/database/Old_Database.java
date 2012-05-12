package de.cubeisland.cubeengine.auctions.database;

import de.cubeisland.cubeengine.auctions.CubeAuctions;
import de.cubeisland.cubeengine.auctions.Manager;
import de.cubeisland.cubeengine.auctions.Util;
import de.cubeisland.cubeengine.auctions.auction.Auction;
import de.cubeisland.cubeengine.auctions.auction.AuctionItem;
import de.cubeisland.cubeengine.auctions.auction.Bid;
import de.cubeisland.cubeengine.auctions.auction.Bidder;
import java.sql.*;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author CodeInfection
 */
public class Old_Database
{
    private final String host;
    private final short port;
    private final String user;
    private final String pass;
    private final String name;

    private final Connection connection;

    public Old_Database(String user, String pass, String name)
    {
        this("localhost", (short)3306, user, pass, name);
    }

    public Old_Database(String host, short port, String user, String pass, String name)
    {
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch (Throwable t)
        {
            throw new IllegalStateException("Couldn't find the MySQL driver!", t);
        }
        this.host = host;
        this.port = port;
        this.user = user;
        this.pass = pass;
        this.name = name;
        try
        {
            this.connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + String.valueOf(this.port) + "/" + this.name, this.user, this.pass);
        }
        catch (SQLException e)
        {
            throw new IllegalStateException("Failed to connect to the database server!", e);
        }
        this.setupStructure();
    }

    public void close()
    {
        try
        {
            this.connection.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.err);
        }
    }

    private PreparedStatement createStatement(String query, Object... params) throws SQLException
    {
        PreparedStatement statement = this.connection.prepareStatement(query);
        for (int i = 0; i < params.length; ++i)
        {
            statement.setObject(i + 1, params[i]);
        }
        return statement;
    }
    
    private void setupStructure()
    {
      
        
       
        
        
        
    }

    public String getHost()
    {
        return this.host;
    }

    public int getPort()
    {
        return this.port;
    }

    public String getUser()
    {
        return this.user;
    }

    public String getPass()
    {
        return this.pass;
    }

    public String getName()
    {
        return this.name;
    }

    public ResultSet query(String query, Object... params)
    {
        try
        {
            return createStatement(query, params).executeQuery();
        }
        catch (SQLException e)
        {
            throw new IllegalStateException("Failed to execute a query!", e);
        }
    }

    public int execUpdate(String query, Object... params)
    {
        try
        {
            return createStatement(query, params).executeUpdate();
        }
        catch (SQLException e)
        {
            throw new IllegalStateException("Failed to execute a query!", e);
        }
    }

    public boolean exec(String query, Object... params)
    {
        try
        {
            return createStatement(query, params).execute();
        }
        catch (SQLException e)
        {
            throw new IllegalStateException("Failed to execute a query!", e);
        }
    }
    
/*
 * loads in DataBase
 */
    public void loadDatabase()
    {
        CubeAuctions.debug("Start loading database...");
        try{
        ResultSet bidderset =
              this.query("SELECT * FROM `bidder`");
        while (bidderset.next())
        {
            //load in Bidder
           // Bidder bidderer = Bidder.getInstance(bidderset.getInt("id"), bidderset.getString("name"));
            //bitmask
          //  bidderer.resetNotifyState(bidderset.getByte("notify"));
        }
        CubeAuctions.debug("All Bidder loaded!");
        int max = CubeAuctions.getInstance().getConfiguration().auction_maxAuctions_overall;
        for (int i=0; i<max; i++)
        {
            ResultSet set =
              this.query("SELECT * FROM `auctions` WHERE `id`=? LIMIT 1;",i);  
            if (set.next())
            {
                int id = set.getInt("id");
                ItemStack item = Util.convertItem(set.getString("item"),set.getInt("amount"));
           //     Bidder owner = Bidder.getInstance(set.getInt("ownerid"),this.getBidderString(set.getInt("ownerid")));
                long auctionEnd = set.getTimestamp("timestamp").getTime();
                //Auction newauction = new Auction (id,item,owner,auctionEnd);
                //load in auction
                //Manager.getInstance().addAuction(newauction);
                ResultSet bidset =
                  this.query("SELECT * FROM `bids` WHERE `auctionid`=? ;",i);
                while (bidset.next())
                { 
                    //sort bids by time & fill auction with bids
                    this.query("SELECT * FROM `bids` ORDER BY `timestamp` ;");
                    //load in Bids
                    /*
                    Bid bid = new Bid( bidset.getInt("id"),
                                     bidset.getInt("bidderid"),
                                     this.getBidderString(bidset.getInt("bidderid")),
                                     bidset.getDouble("amount"),
                                     bidset.getTimestamp("timestamp"));
                    Manager.getInstance().getAuction(newauction.getId()).getBids().push(bid);
                   
                   * 
                   */
                }
            }
        }
        CubeAuctions.debug("All auctions loaded!");
        //load in Subs
        ResultSet subset =
              this.query("SELECT * FROM `subscription`;");
        while (subset.next())
        {
        //    Bidder bidder = Bidder.getInstance(subset.getInt("bidderid"),this.getBidderString(subset.getInt("bidderid")));
            if (subset.getInt("type")==1)
            {//IDSub
                
        //        bidder.addDataBaseSub(subset.getInt("auctionid"));
            }
            else
            {//MatSub
       //         bidder.addDataBaseSub(Util.convertItem(subset.getString("item")));
            }
        }
        CubeAuctions.debug("All subscriptions loaded!");
        //load in auctionbox
        ResultSet itemset =
              this.query("SELECT * from `auctionbox` ORDER BY `timestamp`;");
        while (itemset.next())
        {
       //     Bidder bidder = Bidder.getInstance(itemset.getInt("bidderid"), this.getBidderString(itemset.getInt("bidderid")));
           /*
            bidder.getBox().getItemList().add(
                    new AuctionItem( itemset.getInt("id"), bidder,
                    Util.convertItem(itemset.getString("item"),itemset.getInt("amount")),
                    itemset.getTimestamp("timestamp"),
                    this.getBidderString(itemset.getInt("ownerid")),
                    itemset.getDouble("price")
                    
                    ));
                    * 
                    */
        }
        CubeAuctions.debug("All auctionboxes loaded!");
        //load in PriceList
        ResultSet priceset =
              this.query("SELECT * from `price`");
        while (priceset.next())
            Manager.getInstance().setPrice(Util.convertItem(priceset.getString("item")), priceset.getDouble("price"), priceset.getInt("amount"));
        CubeAuctions.debug("All average prices loaded!");
        }
        catch (SQLException ex){
        CubeAuctions.log("Error while loading DataBase!");
        }
        CubeAuctions.log("Database loaded succesfully");
    }
    
    private String getBidderString(int id)
    {
        try{
            ResultSet set =
              this.query("SELECT * from `bidder` where `id`=? LIMIT 1;",id);  
            if (set.next())
              return set.getString("name");
        }   
        catch (SQLException ex){}
        return null;
    }

    
}
