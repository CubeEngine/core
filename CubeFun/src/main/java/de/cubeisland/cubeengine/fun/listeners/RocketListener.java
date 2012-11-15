package de.cubeisland.cubeengine.fun.listeners;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.fun.Fun;
import de.cubeisland.cubeengine.fun.commands.help.RocketCMDInstance;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class RocketListener implements Listener, Runnable
{
    private final UserManager            userManager;

    private final Set<RocketCMDInstance> instances;
    private final Set<RocketCMDInstance> garbageCollection;

    public RocketListener(Fun module)
    {
        this.userManager = module.getUserManager();
        this.instances = new HashSet<RocketCMDInstance>();
        this.garbageCollection = new HashSet<RocketCMDInstance>();
    }

    public void addInstance(User user, int ticks)
    {
        if (!this.contains(user))
        {
            instances.add(new RocketCMDInstance(user.getName(), ticks));
        }
    }

    public Collection<User> getUsers()
    {
        Set<User> users = new HashSet<User>();
        for (RocketCMDInstance instance : instances)
        {
            users.add(instance.getUser());
        }
        return users;
    }

    public boolean contains(User user)
    {
        for (User users : this.getUsers())
        {
            if (users.getName().equals(user.getName()))
            {
                return true;
            }
        }
        return false;
    }

    public void removeInstance(User user)
    {
        for (RocketCMDInstance instance : instances)
        {
            if (instance.getName().equals(user.getName()))
            {
                if (this.garbageCollection.contains(instance))
                {
                    this.instances.remove(instance);
                    this.garbageCollection.remove(instance);
                }
                else
                {
                    this.garbageCollection.add(instance);
                }
            }
        }
    }

    public Collection<RocketCMDInstance> getInstances()
    {
        return this.instances;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event)
    {
        if (event.getEntity() instanceof Player && event.getCause() == DamageCause.FALL)
        {
            User user = this.userManager.getExactUser((Player)event.getEntity());
            if (user == null)
            {
                return;
            }

            if (this.contains(user))
            {
                event.setCancelled(true);
                this.removeInstance(user);
            }
        }
    }

    @Override
    public void run()
    {
        for (RocketCMDInstance instance : this.getInstances())
        {
            User user = instance.getUser();
            double random = Math.random();

            if (user.getVelocity().getY() > 1 && random < 0.4)
            {
                user.getWorld().createExplosion(user.getLocation(), 0, false);
            }
            if (random < 0.01)
            {
                user.setFireTicks(20 * 3);
            }

            instance.addTick();
            if (instance.getTicks() >= instance.getMaxTicks() || instance.getNumberOfAirBlocksUnderFeets() == 0)
            {
                this.removeInstance(user);
            }
        }
    }
}
