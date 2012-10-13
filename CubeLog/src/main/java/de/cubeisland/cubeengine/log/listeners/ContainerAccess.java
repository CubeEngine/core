package de.cubeisland.cubeengine.log.listeners;

import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.LogSubConfiguration;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

/**
 *
 * @author Anselm Brehme
 */
public class ContainerAccess extends LogListener
{
    public ContainerAccess(Log module)
    {
        super(module, new ContainerAccessConfig());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event)
    {
        //TODO
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event)
    {
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
        @Option(value = "actions", genericType = Boolean.class)
        public Map<LogAction, Boolean> actions = new EnumMap<LogAction, Boolean>(LogAction.class);

        @Override
        public String getName()
        {
            return "containeraccess";
        }
    }
}