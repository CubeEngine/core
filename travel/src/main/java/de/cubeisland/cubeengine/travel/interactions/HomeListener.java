package de.cubeisland.cubeengine.travel.interactions;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.travel.Travel;
import de.cubeisland.cubeengine.travel.storage.Home;
import de.cubeisland.cubeengine.travel.storage.TelePointManager;
import de.cubeisland.cubeengine.travel.storage.TeleportPoint;

public class HomeListener implements Listener
{
    private final Travel module;
    private final TelePointManager tpManager;

    public HomeListener(Travel module)
    {
        this.module = module;
        this.tpManager = module.getTelepointManager();
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void rightClickBed(PlayerInteractEvent event)
    {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            Material block = event.getClickedBlock().getType();
            if (block == Material.BED_BLOCK || block == Material.BED)
            {
                User user = module.getCore().getUserManager().getUser(event.getPlayer());
                if (user.isSneaking())
                {
                    if (tpManager.hasHome("home", user))
                    {
                        Home home = tpManager.getHome(user, "home");
                        home.setLocation(user.getLocation());
                        home.update();
                        tpManager.update(home.getModel());
                        user.sendTranslated("&6Your home have been set!");
                    }
                    else
                    {
                        if (this.tpManager.getNumberOfHomes(user) == this.module.getConfig().maxhomes)
                        {
                            user.sendTranslated("You have reached your maximum number of homes!");
                            user.sendTranslated("You have to delete a home to make a new one");
                            return;
                        }
                        Home home = tpManager.createHome(user.getLocation(), "home", user, TeleportPoint.Visibility.PRIVATE);
                        user.sendTranslated("&6Your home have been created!");
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
