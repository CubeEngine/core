package de.cubeisland.cubeengine.fun.listeners;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.fun.Fun;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

/**
 *
 * @author Wolfi
 */
public class RocketListener implements Listener, Runnable
{
    private Set<String> players = new HashSet<String>();
    private List<String> rocketPlayers = new ArrayList<String>();
    private UserManager userManager;

    public void addPlayer(User user)
    {
        String name = user.getName();
        rocketPlayers.add(name);
        players.add(name);
    }

    public void removePlayer(String name)
    {
        players.remove(name);
        while (this.getNumberOf(name) > 0)
        {
            rocketPlayers.remove(name);
        }
    }

    public RocketListener(Fun module)
    {
        this.userManager = module.getUserManager();
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event)
    {
        if (event.getEntity() instanceof Player && event.getCause() == DamageCause.FALL)
        {
            User user = userManager.getUser((Player)event.getEntity());
            if (user == null)
            {
                return;
            }
            if (players.contains(user.getName()))
            {
                event.setCancelled(true);
                removePlayer(user.getName());
            }
        }
    }

    public int getNumberOf(String name)
    {
        int number = 0;
        for (String entryName : this.rocketPlayers)
        {
            if (entryName.equals(name))
            {
                number++;
            }
        }
        return number;
    }

    public void run()
    {
        if (!this.rocketPlayers.isEmpty())
        {
            String name = this.rocketPlayers.get(0);
            if (this.getNumberOf(name) > 1)
            {
                this.rocketPlayers.remove(name);
                System.out.println("removed one instance of " + name);
            }
            else
            {
                this.removePlayer(name);
                System.out.println("removed " + name);
            }
        }
    }
}
