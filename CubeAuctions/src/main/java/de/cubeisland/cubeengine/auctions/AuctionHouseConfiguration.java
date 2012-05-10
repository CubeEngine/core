package de.cubeisland.cubeengine.auctions;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.inventory.ItemStack;

/**
 * load in configuration file
 */
public class AuctionHouseConfiguration
{
    public final long     auction_undoTime;             //in d h m s | -1 is infinite
    public final int      auction_maxAuctions_overall;  //Overall
    public final int      auction_maxAuctions_player;   //per Player
    public final boolean  auction_maxAuctions_opIgnore; //Op ignore perPlayer limit NOT Overall Limit!
    public final long     auction_maxLength;            //in d h m s | -1 is infinite
    public final boolean  auction_opCanCheat;           //Op can Cheat Items for Auction
    public final List<ItemStack> auction_blacklist;     //Blacklist Materials
    public final String   auction_timeFormat;           //Time Format Output
    public final long     auction_standardLength;       //in d h m s
    public final List<Integer>   auction_notifyTime;    //List with time in d h m s
    public final int      auction_punish;               //Punishment in % of Bid 0-100
    public final int      auction_itemBoxLength;        //in days
    public final int      auction_comission;            //Cost for Owner in %
    public final String   auction_language;             //en / de
    public final boolean  auction_confirmID;            //need confirm to delete Auction per id
    public final long     auction_removeTime;           //in d h m s | -1 is infinite
    
    public final String   auction_database_host;            
    public final short    auction_database_port;
    public final String   auction_database_user;
    public final String   auction_database_pass;
    public final String   auction_database_name;

    public AuctionHouseConfiguration(Configuration config)
    {
        this.auction_maxAuctions_player = config.getInt("auction.maxAuctions.player");
        this.auction_maxAuctions_opIgnore = config.getBoolean("auction.maxAuctions.opIgnore");
        this.auction_maxAuctions_overall = config.getInt("auction.maxAuctions.overall");
        this.auction_opCanCheat = config.getBoolean("auction.opCanCheat");
        this.auction_timeFormat = config.getString("auction.timeFormat");
        this.auction_punish = config.getInt("auction.punish");
        this.auction_itemBoxLength = config.getInt("auction.itemBoxLength");
        this.auction_comission = config.getInt("auction.comission");
        this.auction_language = config.getString("auction.language");
        this.auction_confirmID = config.getBoolean("auction.confirmID");
        
        this.auction_database_host = config.getString("auction.database.host");
        this.auction_database_port = ((short)config.getInt("auction.database.port"));
        this.auction_database_user = config.getString("auction.database.user");
        this.auction_database_pass = config.getString("auction.database.pass");
        this.auction_database_name = config.getString("auction.database.name");
        
        this.auction_undoTime = Util.convertTimeToMillis(config.getString("auction.undoTime"));
        this.auction_removeTime = Util.convertTimeToMillis(config.getString("auction.removeTime"));
        this.auction_maxLength = Util.convertTimeToMillis(config.getString("auction.maxLength"));
        this.auction_standardLength = Util.convertTimeToMillis(config.getString("auction.standardLength"));

        this.auction_notifyTime = this.convertlist(config.getStringList("auction.notifyTime"));

        this.auction_blacklist = getItemList(config.getStringList("auction.blacklist"));
    }

/**
 * converts StringList to IntegerList
 */
    private List<Integer> convertlist(List<String> str)
    {
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < str.size(); ++i)
        {
            list.add(Util.convertTimeToMillis(str.get(i)));
        }
        return list;

    }
    
/**
 * converts StringList to ItemStack-List
 */
    private List<ItemStack> getItemList(List<String> str)
    {
        int max = str.size();
        List<ItemStack> out = new ArrayList<ItemStack>();
        for (int i = 0; i < max; ++i)
        {
            String tmp = str.get(i);
            int parambreak = tmp.indexOf(":");
            if (parambreak == -1)
            {
                out.add(new ItemStack(Material.matchMaterial(tmp), 1));
            }
            else
            {
                tmp = tmp.substring(0, parambreak);
                short tmp2;
                try
                {
                    tmp2 = Short.parseShort(tmp.substring(parambreak + 1));
                }
                catch (NumberFormatException ex)
                {
                    return null;
                }
                out.add(new ItemStack(Material.matchMaterial(tmp), 1, tmp2));
            }
        }
        return out;
    }
}
