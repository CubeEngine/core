package de.cubeisland.cubeengine.signmarket;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.InventoryUtil;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


public class MarketSignInventoryListener implements Listener
{
    private TLongObjectHashMap<Inventory> viewingInventories = new TLongObjectHashMap<Inventory>();
    private TLongObjectHashMap<Inventory> editingInventories = new TLongObjectHashMap<Inventory>();
    private final Signmarket module;

    public MarketSignInventoryListener(Signmarket module) {
        this.module = module;
    }

    public void openInventoryCanEdit(User user, MarketSign marketSign)
    {
        Inventory inventory = this.prepareInventory(marketSign);
        this.editingInventories.put(user.key,inventory);
        user.openInventory(inventory);
    }

    public void openInventoryCannotEdit(User user, MarketSign marketSign)
    {
        Inventory inventory = this.prepareInventory(marketSign);
        this.viewingInventories.put(user.key,inventory);
        user.openInventory(inventory);
    }

    private Inventory prepareInventory(MarketSign marketSign)
    {
        Inventory inventory = marketSign.getInventory();

        return inventory;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event)
    {
        if (event.getInventory().getHolder() instanceof MarketSign)
        {
            Inventory inventory = event.getInventory();
            HumanEntity humanEntity = event.getPlayer();
            if (humanEntity instanceof Player)
            {
                User user = this.module.getUserManager().getExactUser((Player)humanEntity);
                this.viewingInventories.remove(user.key);// no changes can be made
                if (this.editingInventories.remove(user.key) != null)
                {
                    ((MarketSign)inventory.getHolder()).saveToDatabase(); // save possible changes
                }
            }
            else
            {
                throw new IllegalStateException("Only Players can look into market-signs");
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        if (event.getInventory().getHolder() instanceof MarketSign)
        {
            HumanEntity humanEntity = event.getWhoClicked();
            if (humanEntity instanceof Player)
            {
                User user = this.module.getUserManager().getExactUser((Player) humanEntity);
                MarketSign marketSign = (MarketSign) event.getInventory().getHolder();
                if (this.viewingInventories.containsKey(user.key))
                {
                    event.setCancelled(true); // changes are not allowed
                    ((Player) humanEntity).updateInventory();
                    return;
                }
                if (this.editingInventories.containsKey(user.key))
                {
                    ItemStack cursor = event.getCursor();
                    ItemStack invent = event.getCurrentItem();

                    if ((invent == null || invent.getTypeId() == 0) || (cursor == null || cursor.getTypeId() == 0))
                    {
                        if (marketSign.getItem().isSimilar(invent)
                         || marketSign.getItem().isSimilar(cursor))
                        {
                            if (!marketSign.isBuySign()) // sell sign does not allow putting in top
                            {
                                if (event.getRawSlot() <= event.getInventory().getSize()) // top
                                {
                                    if (cursor != null && cursor.getTypeId() !=0)
                                    {
                                        event.setCancelled(true);
                                        ((Player) humanEntity).updateInventory();
                                        return;
                                    }
                                }
                                else
                                {
                                    if (invent != null && invent.getTypeId() != 0)
                                    {
                                        event.setCancelled(true);
                                        ((Player) humanEntity).updateInventory();
                                        return;
                                    }
                                }
                            }
                            return;
                        }
                    } // else swapping items is not allowed -> cancel
                    event.setCancelled(true);
                    ((Player) humanEntity).updateInventory();
                    return;
                }
                marketSign.setStock(InventoryUtil.getAmountOf(event.getInventory(),marketSign.getItem()));
                marketSign.updateSign();
            }
            else
            {
                throw new IllegalStateException("Only Players can look into market-signs");
            }
        }
    }
}
