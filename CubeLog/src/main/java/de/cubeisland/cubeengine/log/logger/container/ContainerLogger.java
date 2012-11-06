package de.cubeisland.cubeengine.log.logger.container;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.logger.LogAction;
import de.cubeisland.cubeengine.log.logger.Logger;
import de.cubeisland.cubeengine.log.logger.SubLogConfig;
import de.cubeisland.cubeengine.log.logger.blockchange.ItemData;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Furnace;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
                this.logContainerChanges(user, type, oldItems, this.compressInventory(event.getView().getTopInventory().getContents()), ((BlockState)event.getView().getTopInventory().getHolder()).getLocation());
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
        OTHER(5);
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
            if (inventory.getHolder() instanceof HumanEntity || inventory.getHolder() instanceof StorageMinecart)
            {
                return null;
            }
            return OTHER;
        }

        public int getId()
        {
            return this.id;
        }
    }
}
