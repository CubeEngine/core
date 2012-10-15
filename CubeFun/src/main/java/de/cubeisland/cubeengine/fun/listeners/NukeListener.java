package de.cubeisland.cubeengine.fun.listeners;

import de.cubeisland.cubeengine.fun.Fun;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class NukeListener implements Listener
{
    private final Fun module;
    private final Set<TNTPrimed> noBlockDamageSet;
    
    public NukeListener(Fun module)
    {
        this.module = module;
        this.noBlockDamageSet = new HashSet<TNTPrimed>();
    }
    
    public void add(TNTPrimed tnt)
    {
        noBlockDamageSet.add(tnt);
    }
    
    public void remove(TNTPrimed tnt)
    {
        noBlockDamageSet.remove(tnt);
    }
    
    public boolean contains(Object tnt)
    {
        return noBlockDamageSet.contains(tnt);
    }
    
    @EventHandler
    public void onBlockDamage(EntityExplodeEvent event)
    {
        if(event.getEntityType() == EntityType.PRIMED_TNT && this.contains(event.getEntity()))
        {
            event.setCancelled(true);
            remove((TNTPrimed)event.getEntity());
        }
    }
}
