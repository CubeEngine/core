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
        // TODO problem logging items multiple times when 2 players looking into the same inventory
        User user = CubeEngine.getUserManager().getExactUser((Player)event.getPlayer());
        TObjectIntHashMap<ItemData> oldItems = openedInventories.get(user.getKey());
        if (oldItems == null)
        {
            return;
        }
        ContainerType type = ContainerType.getContainerType(event.getView().getTopInventory());
        if (this.checkLog(type))
        {
            if (event.getPlayer() instanceof Player)
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
                this.logContainerChanges(user, type, oldItems, this.compressInventory(event.getView().getTopInventory().getContents()), loc);
                openedInventories.remove(user.getKey());
            }
        }
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
                openedInventories.put(user.getKey(), this.compressInventory(event.getView().getTopInventory().getContents())); //saveTopInventory
            }
        }
    }
/*
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true) //TODO figure out how it works...
    public void onInventoryClick(InventoryClickEvent event)
    {
        // TODO check if is logging this
        User user = CubeEngine.getUserManager().getExactUser((Player)event.getWhoClicked());
        TObjectIntHashMap<ItemData> log = openedInventories.get(user.key);
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
        if (!event.isShiftClick() && event.getSlot() < event.getView().getTopInventory().getSize()) // No shift-click AND click on top inventory
        {
            if (event.isLeftClick())
            {
                if (onCursor == null || onCursor.getTypeId() == 0) // take items
                {
                    log.put(datainInvent, (Integer)log.get(datainInvent) == null ? -inInvent.getAmount() : log.get(datainInvent) - inInvent.getAmount());
                }
                else if (inInvent == null || inInvent.getTypeId() == 0) // add items
                {
                    log.put(dataOnCursor, (Integer)log.get(dataOnCursor) == null ? onCursor.getAmount() : log.get(dataOnCursor) + onCursor.getAmount());
                }
                else // both filled
                {
                    if (this.compareItemStacks(inInvent, onCursor)) // can be stacked together?
                    {
                        //action in+on = in+on
                        //left:  32+1  = 16+17
                        //left:  16+1  = 16+1
                        //left:  32+31 = 6X 31+32  21X 16+47 <- thats the problem!!!
                        
                        
                        //################TODO what happens here??
                        int space = inInvent.getMaxStackSize() - inInvent.getAmount();
                        if (space < 0) // oversized stack -> remove missing space (sometimes exchange why?)
                        {
                            log.put(datainInvent, (Integer)log.get(datainInvent) == null ? space : log.get(datainInvent) - space);
                        }
                        else // normal stack -> add items
                        {
                            if (space == 0) // No space
                            {
                                if ((onCursor.getMaxStackSize() - onCursor.getAmount()) < 0) // overstacked -> exchange
                                {
                                    int exchangeAmount = onCursor.getAmount() - inInvent.getAmount();
                                    log.put(datainInvent, (Integer)log.get(datainInvent) == null ? exchangeAmount : log.get(datainInvent) + exchangeAmount);
                                } // else to nothing
                                return;
                            }
                            log.put(datainInvent, (Integer)log.get(datainInvent) == null ? space : log.get(datainInvent) + space);
                        }
                        //################TODO what happens here??
                    }
                    else // no stacking -> exchange
                    {
                        log.put(datainInvent, (Integer)log.get(datainInvent) == null ? -inInvent.getAmount() : log.get(datainInvent) - inInvent.getAmount());
                        log.put(dataOnCursor, (Integer)log.get(dataOnCursor) == null ? onCursor.getAmount() : log.get(dataOnCursor) + onCursor.getAmount());
                    }
                }
            }
            else if (event.isRightClick())
            {
                if (onCursor == null || onCursor.getTypeId() == 0) // take half items (rounded up)
                {
                    int half = (int)Math.ceil(inInvent.getAmount() * 0.5);
                    log.put(datainInvent, (Integer)log.get(datainInvent) == null ? -half : log.get(datainInvent) - half);
                }
                else if (inInvent == null || inInvent.getTypeId() == 0) // add 1 item
                {
                    log.put(dataOnCursor, (Integer)log.get(dataOnCursor) == null ? 1 : log.get(dataOnCursor) + 1);
                }
                else // both filled
                {
                    if (this.compareItemStacks(inInvent, onCursor)) // can be stacked together?
                    {
                        //################TODO what happens here??
                        int space = inInvent.getMaxStackSize() - inInvent.getAmount();
                        if (space < 0) // oversized stack -> remove (sometimes exchange (with signs) why?)
                        {
                            log.put(datainInvent, (Integer)log.get(datainInvent) == null ? space : log.get(datainInvent) - space);
                        }
                        else // normal stack -> add items
                        {
                            if (space == 0) // No space -> do nothing
                            {
                                return;
                            }
                            log.put(datainInvent, (Integer)log.get(datainInvent) == null ? space : log.get(datainInvent) + space);
                        }
                        //################TODO what happens here??
                    }
                    else // no stacking -> exchange
                    {
                        log.put(datainInvent, (Integer)log.get(datainInvent) == null ? -inInvent.getAmount() : log.get(datainInvent) - inInvent.getAmount());
                        log.put(dataOnCursor, (Integer)log.get(dataOnCursor) == null ? onCursor.getAmount() : log.get(dataOnCursor) + onCursor.getAmount());
                    }
                }
            }
        }
        else // ShiftClick
        {
            if (event.getSlot() < event.getView().getTopInventory().getSize()) // Click on Top
            {
                if (InventoryUtil.checkForPlace(event.getView().getBottomInventory(), inInvent)) // place -> remove
                {
                    log.put(datainInvent, (Integer)log.get(datainInvent) == null ? -inInvent.getAmount() : log.get(datainInvent) - inInvent.getAmount());

                }
                // else no place -> no move
            }
            else // Click on Bottom
            {
                if (InventoryUtil.checkForPlace(event.getView().getTopInventory(), inInvent)) // place -> add
                {
                    log.put(datainInvent, (Integer)log.get(datainInvent) == null ? inInvent.getAmount() : log.get(datainInvent) + inInvent.getAmount());
                }
                // else no place -> no move
            }
        }
    }

    private boolean compareItemStacks(ItemStack item1, ItemStack item2)
    {
        if (item1.getTypeId() == item2.getTypeId())
        {
            if (item1.getDurability() == item2.getDurability())
            {
                if (item1 instanceof CraftItemStack && item2 instanceof CraftItemStack)
                {
                    if (((CraftItemStack)item1).getHandle().getTag().equals(((CraftItemStack)item1).getHandle().getTag()))
                    {
                        return true;
                    }
                }
                else
                {
                    return true;
                }
            }
        }
        return true;
    }
    * */

    private boolean checkLog(ContainerType type)
    {
        switch (type)
        {
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

    private void logContainerChanges(User user, ContainerType type, TObjectIntHashMap<ItemData> oldInv, TObjectIntHashMap<ItemData> newInv, Location loc)
    {
        //Compare and compute difference
        TObjectIntHashMap<ItemData> diff = new TObjectIntHashMap<ItemData>();
        for (ItemData data : oldInv.keySet())
        {
            Integer amount_old = oldInv.get(data);
            Integer amount_new = newInv.get(data);
            if (amount_new == null || amount_new == 0)
            {
                diff.put(data, -amount_old); // Item removed
            }
            else
            {
                diff.put(data, amount_new - amount_old); // Item amount changed
            }
        }
        for (ItemData data : newInv.keySet())
        {
            if (!oldInv.containsKey(data))
            {
                diff.put(data, newInv.get(data)); // Item added
            }
        }
        //difference created! Logging every change:
        boolean logged = false;
        for (ItemData data : diff.keySet())
        {
            if (diff.get(data) != 0)
            {
                this.logContainerChange(user, data, diff.get(data), loc, type.getId());
                logged = true;
            }
        }
        if (!logged && this.config.logNothing) //Player just looked into container
        {
            this.logContainerChange(user, new ItemData(0, (short)0), 0, loc, type.getId());
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
        CHEST(1),
        FURNACE(2),
        BREWINGSTAND(3),
        DISPENSER(4),
        OTHER(5),
        STORAGEMINECART(6),
        HUMANENTITY(7),;
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
