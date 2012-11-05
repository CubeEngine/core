package de.cubeisland.cubeengine.log.listeners;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.LogManager;
import de.cubeisland.cubeengine.log.LogManager.ContainerType;
import de.cubeisland.cubeengine.log.LogSubConfiguration;
import de.cubeisland.cubeengine.log.storage.BlockData;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class ContainerAccess extends LogListener
{
    private static TIntObjectHashMap<THashMap<BlockData, Integer>> logbuffer = new TIntObjectHashMap<THashMap<BlockData, Integer>>();

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
            return;
        }
        //TODO
        //log data of the player closing the inventory
    }

    public void onInventoryClick(InventoryClickEvent event)
    {
        //check what is Topinventory
        //event.getView().getTopInventory()
        //map Player-> map(BlockData -> amount)
        //TODO
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event)
    {
        //log looked into inventory: save in same table as item 0;
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