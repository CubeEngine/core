package de.cubeisland.cubeengine.auctions;

import de.cubeisland.cubeengine.auctions.auction.Auction;
import de.cubeisland.cubeengine.auctions.auction.AuctionItem;
import de.cubeisland.cubeengine.auctions.auction.Bidder;
import static de.cubeisland.cubeengine.auctions.CubeAuctions.t;
import de.cubeisland.cubeengine.auctions.database.Database;
import java.util.LinkedList;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.time.DateFormatUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a box containing all Items from fished auctions
 * 
 * @author Faithcaio
 */
public class AuctionBox
{
    private LinkedList<AuctionItem> itemList;
    private final Bidder bidder;
    private Economy econ = CubeAuctions.getInstance().getEconomy();
    private final Database db;

/**
 * creates a new AuctionBox for bidder
 */ 
    public AuctionBox(Bidder bidder)
    {
        this.db = CubeAuctions.getInstance().getDB();
        this.bidder = bidder;
        this.itemList = new LinkedList<AuctionItem>();
    }
    
/**
 * fills Box with auction
 */ 
    public void addItem(Auction auction)
    {
        this.itemList.add(new AuctionItem(auction));
    }
    
/**
 * try to give Next AuctionItem to the player
 */ 
    public boolean giveNextItem()
    {
        Player player = this.bidder.getPlayer();

        if (this.itemList.isEmpty())
        {
            return false;
        }

        AuctionItem auctionItem = this.itemList.getFirst();
        ItemStack item = auctionItem.getItem();
        ItemStack tmp = player.getInventory().addItem(auctionItem.cloneItem().getItem()).get(0);

        if (auctionItem.getOwner().equals(this.bidder.getName()))
        {
            player.sendMessage(t("i") + " " + t("cont_rec_ab", item.getType().toString() + "x" + item.getAmount()));
        }
        else
        {
            player.sendMessage(t("i") + " " + t("cont_rec", item.getType().toString() + "x" + item.getAmount(),
                econ.format(auctionItem.getPrice()), auctionItem.getOwner(),
                DateFormatUtils.formatUTC(auctionItem.getDate(), "MMM dd"))
            );
        }

        if (tmp == null)
        {
            player.updateInventory();
            db.execUpdate("DELETE FROM `auctionbox` WHERE `id`=?", auctionItem.getId());
            this.itemList.removeFirst();
            return true;
        }
        else
        {
            player.sendMessage(t("i") + " " + t("cont_rec_remain"));

            db.execUpdate("UPDATE `auctionbox` SET `amount`=? WHERE `id`=?", tmp.getAmount(), auctionItem.getId());
            item.setAmount(tmp.getAmount());
            player.updateInventory();
            return true;
        }
    }

/**
 * @return list of AuctionItems stored in this Box
 */ 
    public LinkedList<AuctionItem> getItemList()
    {
        return this.itemList;
    }
}
