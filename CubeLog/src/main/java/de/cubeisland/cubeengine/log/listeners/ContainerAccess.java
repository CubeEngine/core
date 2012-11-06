package de.cubeisland.cubeengine.log.listeners;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.LogManager.ContainerType;
import de.cubeisland.cubeengine.log.LogSubConfiguration;
import de.cubeisland.cubeengine.log.storage.ItemData;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

public class ContainerAccess extends LogListener
{
    private static TIntObjectHashMap<TObjectIntHashMap<ItemData>> openedInventories = new TIntObjectHashMap<TObjectIntHashMap<ItemData>>();

    public ContainerAccess(Log module)
    {
        super(module, new ContainerAccessConfig());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event)
    {
        ContainerType type = ContainerType.getContainerType(event.getView().getTopInventory());
        if (type == null)
        {
            return; // HumanEntity / StorageMinecart
            //TODO StorageMinecart save some kind of ID in NBT-Data
        }
        if (event.getPlayer() instanceof Player)
        {
            User user = CubeEngine.getUserManager().getExactUser((Player)event.getPlayer());
            TObjectIntHashMap<ItemData> oldItems = openedInventories.get(user.getKey());
            this.logContainerChanges(user, type, oldItems, this.compressInventory(event.getView().getTopInventory().getContents()), ((BlockState)event.getView().getTopInventory().getHolder()).getLocation());
            openedInventories.remove(user.getKey());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event)
    {
        ContainerType type = ContainerType.getContainerType(event.getView().getTopInventory());
        if (type == null)
        {
            return; // HumanEntity / StorageMinecart
        }
        if (event.getPlayer() instanceof Player)
        {
            User user = CubeEngine.getUserManager().getExactUser((Player)event.getPlayer());
            openedInventories.put(user.getKey(), this.compressInventory(event.getView().getTopInventory().getContents())); //saveTopInventory
        }
    }

    private void logContainerChanges(User user, ContainerType type, TObjectIntHashMap<ItemData> oldInv, TObjectIntHashMap<ItemData> newInv, Location loc)
    {
        //compare:
        TObjectIntHashMap<ItemData> diff = new TObjectIntHashMap<ItemData>();
        for (ItemData data : oldInv.keySet())
        {
            Integer amount_old = oldInv.get(data);
            Integer amount_new = newInv.get(data);
            if (amount_new == null || amount_new == 0)
            {
                //Item removed
                diff.put(data, -amount_old);
            }
            else
            {
                //Item amount changed
                diff.put(data, amount_new - amount_old);
            }
        }
        for (ItemData data : newInv.keySet())
        {
            if (!oldInv.containsKey(data))
            {
                //Item added
                diff.put(data, newInv.get(data));
            }
        }
        //Diff created log every change:
        boolean logged = false;
        for (ItemData data : diff.keySet())
        {
            if (diff.get(data) != 0)
            {
                lm.logContainerChange(user, data, diff.get(data), loc, type.getId());
                logged = true;
            }
        }
        if (!logged)
        {
            lm.logContainerChange(user, new ItemData(0, (short)0), 0, loc, type.getId());
        }
    }

    private TObjectIntHashMap<ItemData> compressInventory(ItemStack[] items)
    {
        TObjectIntHashMap<ItemData> map = new TObjectIntHashMap<ItemData>();
        for (ItemStack item : items)
        {
            if (item == null)
            {
                continue;
            }
            ItemData itemData = new ItemData(item);
            Integer amount = map.get(itemData);
            if (amount == null)
            {
                amount = 0;
            }
            amount += item.getAmount();
            map.put(itemData, amount);
        }
        return map;
    }

    public static class ContainerAccessConfig extends LogSubConfiguration
    {
        public ContainerAccessConfig()
        {
            this.actions.put(LogAction.DISPENSERACCESS, false);
            this.actions.put(LogAction.CHESTACCESS, false);
            this.actions.put(LogAction.FURNACEACCESS, false);
            this.actions.put(LogAction.BREWINGSTANDACCESS, false);
            this.enabled = false;
        }

        @Override
        public String getName()
        {
            return "containeraccess";
        }
    }
}