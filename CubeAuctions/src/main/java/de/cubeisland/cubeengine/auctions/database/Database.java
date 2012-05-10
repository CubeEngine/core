package de.cubeisland.cubeengine.auctions.database;

import de.cubeisland.cubeengine.auctions.auction.Auction;
import de.cubeisland.cubeengine.auctions.auction.AuctionItem;
import de.cubeisland.cubeengine.auctions.auction.Bid;
import de.cubeisland.cubeengine.auctions.auction.Bidder;
import de.cubeisland.cubeengine.auctions.CubeAuctions;
import de.cubeisland.cubeengine.auctions.Manager;
import de.cubeisland.cubeengine.auctions.Util;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author CodeInfection
 */
public class Database
{
    private final String host;
    private final short port;
    private final String user;
    private final String pass;
    private final String name;

    private final Connection connection;

    public Database(String user, String pass, String name)
    {
        this("localhost", (short)3306, user, pass, name);
    }

    public Database(String host, short port, String user, String pass, String name)
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
//##########################################################
    private void createTableIfNoExist(String tablename, EntityIdentifier id, EntityProperty... properties)
    {
        String exec = "";
        exec += "CREATE TABLE IF NOT EXISTS `"+tablename+"` (";//Create table with tablename
        exec += id.toDBCreateString();//Identifier ; Type ;  Not null?
        for (int i=0; i<properties.length; ++i)
            exec += id.toDBCreateString();//Property ; Type ;  Not null?
        exec += "PRIMARY KEY("+id.toDBString()+")";//Identifier as Primary Key
        for (int i=0; i<properties.length; ++i)
            exec += properties[i].toDBCreateForeignKey();//Property Foreign Key ...
        exec += ") ENGINE=MyISAM DEFAULT CHARSET=latin1;";
    }
//##########################################################
    
    private void setupStructure()
    {
        this.exec(      "CREATE TABLE IF NOT EXISTS `auctions` ("+
                        "`id` int(10) unsigned NOT NULL,"+
                        "`ownerid` int(11) NOT NULL,"+
                        "`item` varchar(42) NOT NULL,"+
                        "`amount` int(11) NOT NULL,"+
                        "`timestamp` timestamp NOT NULL,"+
                        "PRIMARY KEY (`id`),"+
                        "FOREIGN KEY (ownerid) REFERENCES bidder(id)"+
                        ") ENGINE=MyISAM DEFAULT CHARSET=latin1;"
                 );
        this.exec(      "CREATE TABLE IF NOT EXISTS `bidder` ("+
                        "`id` int(11) NOT NULL AUTO_INCREMENT,"+
                        "`name` varchar(16) NOT NULL,"+
                        "`type` tinyint(1) NOT NULL COMMENT 'is ServerBidder?',"+
                        "`notify` smallint(2) NOT NULL,"+
                        "PRIMARY KEY (`id`)"+
                        ") ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;"
                 );
        this.exec(      "CREATE TABLE IF NOT EXISTS `bids` ("+
                        "`id` int(11) NOT NULL AUTO_INCREMENT,"+
                        "`auctionid` int(11) NOT NULL,"+
                        "`bidderid` int(11) NOT NULL,"+
                        "`amount` int(11) NOT NULL,"+
                        "`timestamp` timestamp NOT NULL,"+
                        "PRIMARY KEY (`id`),"+
                        "FOREIGN KEY (auctionid) REFERENCES auctions(id),"+
                        "FOREIGN KEY (bidderid) REFERENCES bidder(id)"+
                        ") ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;"
                 );
        this.exec(       "CREATE TABLE IF NOT EXISTS `auctionbox` ("+
                        "`id` int(11) NOT NULL AUTO_INCREMENT,"+
                        "`bidderid` int(11) NOT NULL,"+
                        "`item` varchar(42) NOT NULL COMMENT 'ID:DATA Ench1:Val Ench2:Val ...',"+
                        "`amount` int(11) NOT NULL,"+
                        "`price` decimal(11,2) NOT NULL,"+
                        "`timestamp` timestamp NOT NULL,"+
                        "`ownerid` int(11) NOT NULL COMMENT 'Bidder who started auction',"+
                        "PRIMARY KEY (`id`),"+
                        "FOREIGN KEY (bidderid) REFERENCES bidder(id)"+
                        ") ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;"
                 );
        this.exec(      "CREATE TABLE IF NOT EXISTS `subscription` ("+
                        "`id` int(11) NOT NULL AUTO_INCREMENT,"+
                        "`bidderid` int(11) NOT NULL,"+
                        "`auctionid` int(11) DEFAULT NULL,"+
                        "`type` tinyint(1) NOT NULL,"+
                        "`item` varchar(42) DEFAULT NULL COMMENT 'ID:DATA Ench1:Val Ench2:Val ...',"+
                        "PRIMARY KEY (`id`),"+
                        "FOREIGN KEY (auctionid) REFERENCES auctions(id),"+
                        "FOREIGN KEY (bidderid) REFERENCES bidder(id)"+
                        ") ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;"
                 );
        this.exec(      "CREATE TABLE IF NOT EXISTS `price` ("+
                        "`id` int(11) NOT NULL AUTO_INCREMENT,"+
                        "`item` varchar(42) DEFAULT NULL COMMENT 'ID:DATA Ench1:Val Ench2:Val ...',"+
                        "`price` decimal(11,2) NOT NULL,"+
                        "`amount` int(11) NOT NULL,"+
                        "PRIMARY KEY (`id`)"+
                        ") ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;"
                 );
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
            Bidder bidderer = Bidder.getInstance(bidderset.getInt("id"), bidderset.getString("name"));
            //bitmask
            bidderer.resetNotifyState(bidderset.getByte("notify"));
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
                Bidder owner = Bidder.getInstance(set.getInt("ownerid"),this.getBidderString(set.getInt("ownerid")));
                long auctionEnd = set.getTimestamp("timestamp").getTime();
                Auction newauction = new Auction (id,item,owner,auctionEnd);
                //load in auction
                Manager.getInstance().addAuction(newauction);
                ResultSet bidset =
                  this.query("SELECT * FROM `bids` WHERE `auctionid`=? ;",i);
                while (bidset.next())
                { 
                    //sort bids by time & fill auction with bids
                    this.query("SELECT * FROM `bids` ORDER BY `timestamp` ;");
                    //load in Bids
                    Bid bid = new Bid( bidset.getInt("id"),
                                     bidset.getInt("bidderid"),
                                     this.getBidderString(bidset.getInt("bidderid")),
                                     bidset.getDouble("amount"),
                                     bidset.getTimestamp("timestamp"));
                    Manager.getInstance().getAuction(newauction.getId()).getBids().push(bid);
                   
                }
            }
        }
        CubeAuctions.debug("All auctions loaded!");
        //load in Subs
        ResultSet subset =
              this.query("SELECT * FROM `subscription`;");
        while (subset.next())
        {
            Bidder bidder = Bidder.getInstance(subset.getInt("bidderid"),this.getBidderString(subset.getInt("bidderid")));
            if (subset.getInt("type")==1)
            {//IDSub
                
                bidder.addDataBaseSub(subset.getInt("auctionid"));
            }
            else
            {//MatSub
                bidder.addDataBaseSub(Util.convertItem(subset.getString("item")));
            }
        }
        CubeAuctions.debug("All subscriptions loaded!");
        //load in auctionbox
        ResultSet itemset =
              this.query("SELECT * from `auctionbox` ORDER BY `timestamp`;");
        while (itemset.next())
        {
            Bidder bidder = Bidder.getInstance(itemset.getInt("bidderid"), this.getBidderString(itemset.getInt("bidderid")));
            bidder.getBox().getItemList().add(
                    new AuctionItem( bidder,
                    Util.convertItem(itemset.getString("item"),itemset.getInt("amount")),
                    itemset.getTimestamp("timestamp"),
                    this.getBidderString(itemset.getInt("ownerid")),
                    itemset.getDouble("price"),
                    itemset.getInt("id")
                    ));
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

    public Database updateEntity(DatabaseEntity entity) throws SQLException
    {
        String idName = null;
        Object idValue = null;
        Map<String, Object> fields = new HashMap<String, Object>();

        String propName;
        for (Field field : entity.getClass().getDeclaredFields())
        {
            try
            {
                if (field.isAnnotationPresent(EntityIdentifier.class))
                {
                    field.setAccessible(true);
                    idName = field.getAnnotation(EntityIdentifier.class).name();
                    if ("".equals(idName))
                    {
                        idName = field.getName();
                    }
                    idValue = field.get(entity);
                }
                else if (field.isAnnotationPresent(EntityProperty.class))
                {
                    field.setAccessible(true);
                    propName = field.getAnnotation(EntityProperty.class).name();
                    if ("".equals(propName))
                    {
                        propName = field.getName();
                    }
                    fields.put(propName, field.get(entity));
                }
            }
            catch (IllegalAccessException e)
            {}
        }

        if (idName == null)
        {
            throw new IllegalArgumentException("The given entity does not contain an identifier!");
        }

        this.update(new String[] {entity.getTable()}, fields, new Condition(quoteName(idName) + " = ?", idValue), 1, -1);

        return this;
    }

    public int update(String[] tables, Map<String, Object> fields) throws SQLException
    {
        return this.update(tables, fields, null, 0, -1);
    }

    public int update(String[] tables, Map<String, Object> fields, Condition condition) throws SQLException
    {
        return this.update(tables, fields, condition, 0, -1);
    }

    public int update(String[] tables, Map<String, Object> fields, Condition condition, int limit) throws SQLException
    {
        return this.update(tables, fields, condition, limit, -1);
    }

    public int update(String[] tables, Map<String, Object> fields, Condition condition, int limit, int offset) throws SQLException
    {
        if (fields == null || fields.size() < 1)
        {
            return 0;
        }
        Iterator<String> fieldNames = fields.keySet().iterator();
        Collection<Object> params = fields.values();
        
        StringBuilder query = new StringBuilder("UPDATE ").append(generateTableList(tables)).append(" SET ");
        query.append(fieldNames.next()).append(" = ?");
        while (fieldNames.hasNext())
        {
            query.append(", ").append(fieldNames.next()).append(" = ?");
        }

        if (condition != null)
        {
            query.append(" WHERE ").append(condition.condition);
            params.addAll(condition.params);
        }
        
        if (limit > 0)
        {
            query.append(" LIMIT ").append(limit);
        }

        if (offset >= 0)
        {
            query.append(" OFFSET ").append(offset);
        }

        return this.execUpdate(query.toString(), params);
    }

    private String generateTableList(String... tables)
    {
        if (tables.length == 0)
        {
            return "";
        }
        if (tables.length > 1)
        {
            int i = 0;
            StringBuilder sb = new StringBuilder(quoteName(tables[i++]));
            for (; i < tables.length; ++i)
            {
                sb.append(", ").append(quoteName(tables[i]));
            }
            return sb.toString();
        }
        return quoteName(tables[0]);
    }

    public static String quoteName(String name)
    {
        int startOffset = 0;
        int delimOffset = name.indexOf(".");
        if (delimOffset < 0)
        {
            return "`" + name + "`";
        }
        else
        {
            StringBuilder nameBuilder = new StringBuilder();
            do
            {
                nameBuilder.append("´").append(name.substring(startOffset, delimOffset)).append("´");
                startOffset = delimOffset + 1;
            }
            while ((delimOffset = name.indexOf(".", delimOffset)) >= 0);
            nameBuilder.append("´").append(name.substring(startOffset)).append("´");

            return nameBuilder.toString();
        }
    }
}
