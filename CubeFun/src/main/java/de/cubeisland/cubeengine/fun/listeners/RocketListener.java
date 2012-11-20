package de.cubeisland.cubeengine.fun.listeners;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.fun.Fun;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class RocketListener implements Listener, Runnable
{
    private final UserManager userManager;
    
    private final Set<RocketCMDInstance> instances;
    private final Set<RocketCMDInstance> garbageCollection;

    public RocketListener(Fun module)
    {
        this.userManager = module.getUserManager();
        this.instances = new HashSet<RocketCMDInstance>();
        this.garbageCollection = new HashSet<RocketCMDInstance>();
    }

    public void addInstance(User user, int height)
    {
        if (!this.contains(user))
        {
            instances.add(new RocketCMDInstance(user.getName(), height));
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

            if ( instance.getNumberOfAirBlocksUnderFeets() == 0)
            {
                this.removeInstance(user);
            }
        }
    }
    
    private class RocketCMDInstance
    {
        private final String name;
        private final int height;
        private boolean back;
        
        private RocketCMDInstance(String name, int height) 
        {
            this.name = name;
            this.height = height;
            this.back = false;
        }
        
        public void setBack()
        {
            this.back = true;
        }
        
        public boolean getBack()
        {
            return this.back;
        }
        
        public int getHeight()
        {
            return this.height;
        }
        
        public User getUser()
        {
            return CubeEngine.getUserManager().getUser(name, true);
        }
        
        public String getName()
        {
            return this.name;
        }
        
        public int getNumberOfAirBlocksUnderFeets()
        {
            Location location = this.getUser().getLocation().subtract(0, 1, 0);
            int numberOfAirBlocks = 0;

            while (location.getBlock().getType() == Material.AIR)
            {
                numberOfAirBlocks++;
                location.subtract(0, 1, 0);
            }

            return numberOfAirBlocks;
        }
    }
}
