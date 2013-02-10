package de.cubeisland.cubeengine.signmarket;

import de.cubeisland.cubeengine.core.user.User;
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
        if (marketSign.isAdminSign())
        {
            inventory.setItem(5, marketSign.getItem());
        }
        else
        {
            ItemStack item = marketSign.getItem();
            item.setAmount(marketSign.getAmount());
            inventory.addItem(item);
        }
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
                if (this.viewingInventories.remove(user.key) != null)
                {
                    ((MarketSign)inventory.getHolder()).resetInventory(); // no changes can be made
                }
                else if (this.editingInventories.remove(user.key) != null)
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
                MarketSign marketSign = (MarketSign) event.getInventory();
                if (this.viewingInventories.containsKey(user.key))
                {
                    event.setCancelled(true); // changes are not allowed
                }
                if (this.editingInventories.containsKey(user.key))
                {
                    if (!event.getCurrentItem().isSimilar(marketSign.getItem())) // Cancel if item is not accepted
                    {
                        event.setCancelled(true);
                    }
                }
            }
            else
            {
                throw new IllegalStateException("Only Players can look into market-signs");
            }
        }
    }
}
