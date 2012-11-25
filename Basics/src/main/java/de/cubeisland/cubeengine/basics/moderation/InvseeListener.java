package de.cubeisland.cubeengine.basics.moderation;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.user.User;
import gnu.trove.map.hash.THashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;

public class InvseeListener implements Listener
{
    private Basics basics;

    public InvseeListener(Basics basics)
    {
        this.basics = basics;
    }

    private Map<User, Boolean> openedInventories = new THashMap<User, Boolean>();

    public void addInventory(User sender, boolean canModify)
    {
        basics.registerListener(this);
        this.openedInventories.put(sender, canModify);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        if (event.getView().getTopInventory().getType() == InventoryType.PLAYER)
        {
            if (event.getWhoClicked() instanceof Player)
            {
                User sender = basics.getUserManager().getExactUser((Player)event.getWhoClicked());
                if (openedInventories.containsKey(sender))
                {
                    if (!openedInventories.get(sender))
                    {
                        event.setCancelled(!openedInventories.get(sender));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event)
    {
        for (User user : openedInventories.keySet())
        {
            if (user.getName().equals(event.getPlayer().getName()))
            {
                openedInventories.remove(user);
                if (openedInventories.isEmpty())
                {
                    basics.getEventManager().unregisterListener(this.basics, this);
                }
                return;
            }
        }
    }
}
