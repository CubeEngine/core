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

/**
 *
 * @author Anselm Brehme
 */
public class ModerationListener implements Listener
{
    private Basics module;

    public ModerationListener(Basics module)
    {
        this.module = module;
    }
    private Map<User, Boolean> openedInventories = new THashMap<User, Boolean>();

    public void addInventory(User sender, boolean canModify)
    {
        module.registerListener(this);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        if (event.getWhoClicked() instanceof Player)
        {
            User sender = module.getUserManager().getUser((Player)event.
                getWhoClicked());
            if (openedInventories.containsKey(sender))
            {
                event.setCancelled(!openedInventories.get(sender));
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
                module.getEventManager().unregisterListener(this.module, this);
                return;
            }
        }
    }
}
