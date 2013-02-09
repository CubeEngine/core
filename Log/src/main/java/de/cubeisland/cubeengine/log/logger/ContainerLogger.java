package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.InventoryUtil;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.Logger;
import de.cubeisland.cubeengine.log.logger.config.ContainerConfig;
import de.cubeisland.cubeengine.log.storage.ItemData;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.*;

public class ContainerLogger extends Logger<ContainerConfig>
{
    public ContainerLogger(Log module) {
        super(module, ContainerConfig.class);
    }

    private static TIntObjectHashMap<TObjectIntHashMap<ItemData>> openedInventories = new TIntObjectHashMap<TObjectIntHashMap<ItemData>>();


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event)
    {
        User user = CubeEngine.getUserManager().getExactUser((Player)event.getPlayer());
        TObjectIntHashMap<ItemData> loggedItems = openedInventories.get(user.getKey().intValue());
        if (loggedItems == null)
        {
            return;
        }
        ContainerType type = ContainerType.getContainerType(event.getView().getTopInventory());
        World world = this.getLocationForHolder(event.getView().getTopInventory().getHolder()).getWorld();
        if (this.checkLog(type, world))
        {//TODO inventories that are not a Blockstate
            Location loc;
            Inventory topInv = event.getView().getTopInventory();
            if (topInv.getHolder() instanceof DoubleChest)
            {
                loc = ((BlockState)((DoubleChest)topInv.getHolder()).getLeftSide()).getLocation();
            }
            else if (!(topInv instanceof AnvilInventory || topInv instanceof BeaconInventory
                    || topInv instanceof CraftingInventory || topInv instanceof EnchantingInventory
                    || topInv instanceof MerchantInventory || topInv instanceof PlayerInventory))
            {
                loc = ((BlockState)event.getView().getTopInventory().getHolder()).getLocation();
            }
            else
            {
                return;
            }
            this.logContainerChanges(user, type, loggedItems, world, loc);
        }
        openedInventories.remove(user.key.intValue());
    }

    private Location getLocationForHolder(InventoryHolder holder) {
        if (holder instanceof Entity)
        {
            return ((Entity)holder).getLocation();
        }
        else if (holder instanceof DoubleChest)
        {
            return ((DoubleChest)holder).getLocation();//TODO get a blocklocation
        }
        else if (holder instanceof BlockState)
        {
            return ((BlockState)holder).getLocation();
        }
        if (holder == null)
        {
            this.module.getLogger().warning("Inventory Holder is null! Logging is impossible.");
        }
        else
        {
            this.module.getLogger().warning("Unknown InventoryHolder:" + holder.toString());
        }
        return null;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event)
    {
        if (this.canLog(event.getView().getTopInventory()))
        {
            World world = this.getLocationForHolder(event.getView().getTopInventory().getHolder()).getWorld();
            ContainerType type = ContainerType.getContainerType(event.getView().getTopInventory());
            if (this.checkLog(type, world))
            {
                if (event.getPlayer() instanceof Player)
                {
                    User user = CubeEngine.getUserManager().getExactUser((Player)event.getPlayer());
                    openedInventories.put(user.getKey().intValue(), new TObjectIntHashMap<ItemData>());
                }
            }
        }
    }

    private boolean canLog(Inventory inventory)
    {
        switch (inventory.getType())
        {
            case CHEST:
            case DISPENSER:
            case FURNACE:
            case BREWING:
                return true;
            //Cannot hold items after closing view:
            case WORKBENCH:
            case CRAFTING:
            case ENCHANTING:
            case ANVIL:
            case MERCHANT:
            // special cases:
            case CREATIVE: //no need to log
            case PLAYER: //no need to log
            case ENDER_CHEST: //TODO could log this but should we?
            case BEACON: //TODO should be able to log this
                return false;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    //TODO figure out how it works...
    public void onInventoryClick(InventoryClickEvent event)
    {
        User user = CubeEngine.getUserManager().getExactUser((Player)event.getWhoClicked());
        TObjectIntHashMap<ItemData> log = openedInventories.get(user.key.intValue());
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
                if (inInvent.isSimilar(onCursor)) // can be stacked together?
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

    private boolean checkLog(ContainerType type, World world)
    {
        ContainerConfig config = this.configs.get(world);
        if (config.enabled)
        {
            switch (type)
            {
                case BREWINGSTAND:
                    return config.logBrewingstand;
                case CHEST:
                    return config.logChest;
                case DISPENSER:
                    return config.logDispenser;
                case FURNACE:
                    return config.logFurnace;
                case OTHER:
                    return config.logOtherBlock;
                default:
                    return false;
                    //TODO storage minecart
            }
        }
        return false;
    }

    private void logContainerChanges(User user, ContainerType type, TObjectIntHashMap<ItemData> loggedChanges, World world, Location loc)
    {
        boolean logged = false;
        for (ItemData data : loggedChanges.keySet())
        {
            if (loggedChanges.get(data) != 0)
            {
                this.module.getLogManager().logChestLog(user.key.intValue(), world, loc, data, loggedChanges.get(data), type.getId());
                logged = true;
            }
        }
        if (!logged &&  this.configs.get(world).logNothing) //Player just looked into container
        {
            this.module.getLogManager().logChestLog(user.key.intValue(), world, loc, new ItemData(0, (short)0), 0, type.getId());
        }
    }

    /**
     * private TObjectIntHashMap<ItemData> compressInventory(ItemStack[] items)
     * { TObjectIntHashMap<ItemData> map = new TObjectIntHashMap<ItemData>();
     * for (ItemStack item : items) { if (item == null) { continue; } ItemData
     * itemData = new ItemData(item); Integer amount = map.get(itemData); if
     * (amount == null) { amount = 0; } amount += item.getAmount();
     * map.put(itemData, amount); } return map; } //
     */


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
