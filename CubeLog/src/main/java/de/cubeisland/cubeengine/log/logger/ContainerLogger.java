package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.InventoryUtil;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.Logger;
import de.cubeisland.cubeengine.log.SubLogConfig;
import de.cubeisland.cubeengine.log.storage.ChestLog;
import de.cubeisland.cubeengine.log.storage.ChestLogManager;
import de.cubeisland.cubeengine.log.storage.ItemData;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Furnace;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ContainerLogger extends Logger<ContainerLogger.ContainerConfig>
{
    private ChestLogManager chestLogManager;

    public ContainerLogger()
    {
        super(LogAction.CONTAINER);
        this.config = new ContainerConfig();
        this.chestLogManager = new ChestLogManager(module.getDatabase());
    }

    private static TIntObjectHashMap<TObjectIntHashMap<ItemData>> openedInventories = new TIntObjectHashMap<TObjectIntHashMap<ItemData>>();

    public void logContainerChange(User user, ItemData data, int amount, Location loc, int type)
    {
        this.chestLogManager.store(new ChestLog(user.getKey(), data, amount, loc, type));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event)
    {
        User user = CubeEngine.getUserManager().getExactUser((Player)event.getPlayer());
        TObjectIntHashMap<ItemData> loggedItems = openedInventories.get(user.getKey());
        if (loggedItems == null)
        {
            return;
        }
        ContainerType type = ContainerType.getContainerType(event.getView().getTopInventory());
        if (this.checkLog(type))
        {
            Location loc;
            if (event.getView().getTopInventory().getHolder() instanceof DoubleChest)
            {
                loc = ((BlockState)((DoubleChest)event.getView().getTopInventory().getHolder()).getLeftSide()).getLocation();
            }
            else
            {
                loc = ((BlockState)event.getView().getTopInventory().getHolder()).getLocation();
            }
            this.logContainerChanges(user, type, loggedItems, loc);
        }
        openedInventories.remove(user.key);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event)
    {
        ContainerType type = ContainerType.getContainerType(event.getView().getTopInventory());
        if (this.checkLog(type))
        {
            if (event.getPlayer() instanceof Player)
            {
                User user = CubeEngine.getUserManager().getExactUser((Player)event.getPlayer());
                openedInventories.put(user.getKey(), new TObjectIntHashMap<ItemData>());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    //TODO figure out how it works...
    public void onInventoryClick(InventoryClickEvent event)
    {
        User user = CubeEngine.getUserManager().getExactUser((Player)event.getWhoClicked());
        TObjectIntHashMap<ItemData> log = openedInventories.get(user.key);
        if (log == null)
        {
            return;
        }
        ItemStack onCursor = event.getCursor();
        ItemStack inInvent = event.getCurrentItem();
        if (event.getSlot() == -999)
        {
            return;
        }
        if (inInvent.getTypeId() == 0 && onCursor.getTypeId() == 0)
        {
            return; //Nothing to log both empty
        }
        ItemData dataOnCursor = new ItemData(onCursor);
        ItemData datainInvent = new ItemData(inInvent);
        if (!event.isShiftClick() && event.getRawSlot() < event.getView().getTopInventory().getSize()) // No shift-click AND click on top inventory
        {
            if (onCursor == null || onCursor.getTypeId() == 0) // take items
            {
                int take = event.isLeftClick() ? inInvent.getAmount() : (inInvent.getAmount() + 1) / 2;
                log.put(datainInvent, log.get(datainInvent) - take);
            }
            else if (inInvent == null || inInvent.getTypeId() == 0) // add items
            {
                int put = event.isLeftClick() ? onCursor.getAmount() : 1;
                log.put(dataOnCursor, log.get(dataOnCursor) + put);
            }
            else
            // both filled
            {
                if (this.compareItemStacks(inInvent, onCursor)) // can be stacked together?
                {
                    int toput = event.isLeftClick() ? inInvent.getAmount() : 1;
                    if (toput > inInvent.getMaxStackSize() - inInvent.getAmount()) //if stack to big
                    {
                        toput = inInvent.getMaxStackSize() - inInvent.getAmount(); //set to missing to fill
                    }
                    log.put(datainInvent, log.get(datainInvent) + toput);
                }
                else
                // no stacking -> exchange
                {
                    log.put(datainInvent, log.get(datainInvent) - inInvent.getAmount());
                    log.put(dataOnCursor, log.get(dataOnCursor) + onCursor.getAmount());
                }
            }
        }
        else if (event.isShiftClick()) // ShiftClick
        {
            int giveOrTake = event.getRawSlot() < event.getView().getTopInventory().getSize() ? -1 : 1; //top or bot inv ?
            if (InventoryUtil.checkForPlace(giveOrTake == 1 ? event.getView().getTopInventory() : event.getView().getBottomInventory(), inInvent)) //check for enough space
            {
                log.put(datainInvent, log.get(datainInvent) + giveOrTake * inInvent.getAmount());
            }
        }
    }

    private boolean compareItemStacks(ItemStack item1, ItemStack item2)
    {
        if (item1.getTypeId() == item2.getTypeId() && item1.getDurability() == item2.getDurability())
        {
            if (item1 instanceof CraftItemStack && item2 instanceof CraftItemStack)
            {
                if (!((CraftItemStack)item1).getHandle().getTag().equals(((CraftItemStack)item1).getHandle().getTag()))
                {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean checkLog(ContainerType type)
    {
        switch (type) {
            case BREWINGSTAND:
                return this.config.logBrewingstand;
            case CHEST:
                return this.config.logChest;
            case DISPENSER:
                return this.config.logDispenser;
            case FURNACE:
                return this.config.logFurnace;
            case OTHER:
                return this.config.logOtherBlock;
            default:
                return false;
                //TODO storage minecart
        }
    }

    private void logContainerChanges(User user, ContainerType type, TObjectIntHashMap<ItemData> loggedChanges, Location loc)
    {
        boolean logged = false;
        for (ItemData data : loggedChanges.keySet())
        {
            if (loggedChanges.get(data) != 0)
            {
                this.logContainerChange(user, data, loggedChanges.get(data), loc, type.getId());
                logged = true;
            }
        }
        if (!logged && this.config.logNothing) //Player just looked into container
        {
            this.logContainerChange(user, new ItemData(0, (short)0), 0, loc, type.getId());
        }
    }

    /**
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
     //*/

    public static class ContainerConfig extends SubLogConfig
    {
        @Option(value = "log-chest")
        public boolean logChest = true;
        @Option(value = "log-furnace")
        public boolean logFurnace = false;
        @Option(value = "log-brewing")
        public boolean logBrewingstand = false;
        @Option(value = "log-dispenser")
        public boolean logDispenser = true;
        @Option(value = "log-other-block")
        public boolean logOtherBlock = true;
        @Option(value = "log-storage-minecart")
        public boolean logStorageMinecart = false;
        @Option(value = "log-looked-into-chest")
        public boolean logNothing = true;

        public ContainerConfig()
        {
            this.enabled = true;
        }

        @Override
        public String getName()
        {
            return "container";
        }
    }

    public static enum ContainerType
    {
        CHEST(
            1),
        FURNACE(
            2),
        BREWINGSTAND(
            3),
        DISPENSER(
            4),
        OTHER(
            5),
        STORAGEMINECART(
            6),
        HUMANENTITY(
            7), ;
        private final int id;
        private static final TIntObjectHashMap<ContainerType> map;

        static
        {
            map = new TIntObjectHashMap<ContainerType>();
            for (ContainerType type : values())
            {
                map.put(type.id, type);
            }
        }

        private ContainerType(int id)
        {
            this.id = id;
        }

        public static ContainerType getContainerType(int id)
        {
            return map.get(id);
        }

        public static ContainerType getContainerType(Inventory inventory)
        {
            if (inventory.getHolder() instanceof BrewingStand)
            {
                return BREWINGSTAND;
            }
            if (inventory.getHolder() instanceof Chest || inventory.getHolder() instanceof DoubleChest)
            {
                return CHEST;
            }
            if (inventory.getHolder() instanceof Furnace)
            {
                return FURNACE;
            }
            if (inventory.getHolder() instanceof Dispenser)
            {
                return DISPENSER;
            }
            if (inventory.getHolder() instanceof StorageMinecart)
            {
                return STORAGEMINECART;
            }
            if (inventory.getHolder() instanceof HumanEntity)
            {
                return HUMANENTITY;
            }
            return OTHER;
        }

        public int getId()
        {
            return this.id;
        }
    }
}
