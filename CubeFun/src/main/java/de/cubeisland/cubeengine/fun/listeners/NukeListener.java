package de.cubeisland.cubeengine.fun.listeners;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class NukeListener implements Listener
{
    private final Set<TNTPrimed> noBlockDamageSet;
    
    public NukeListener()
    {
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
    
    public boolean contains(TNTPrimed tnt)
    {
        return noBlockDamageSet.contains(tnt);
    }
    
    @EventHandler
    public void onBlockDamage(EntityExplodeEvent event)
    {
        try
        {
            if(event.getEntityType() == EntityType.PRIMED_TNT && this.contains((TNTPrimed)event.getEntity()))
            {
                event.blockList().clear();
                remove((TNTPrimed)event.getEntity());
            }
        }
        catch(NullPointerException ignored){}
    }
}
