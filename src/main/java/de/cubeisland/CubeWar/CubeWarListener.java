package de.cubeisland.CubeWar;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 *
 * @author Faithcaio
 */
public class CubeWarListener implements Listener
{

    public CubeWarListener() 
    {
    
    }
    
    @EventHandler
    public void death(final EntityDeathEvent event)
    {
        if (event.getEntity() instanceof Player)
        {
            if (event.getEntity().getKiller()!=null)
                Heroes.kill(event.getEntity().getKiller(), (Player)event.getEntity());
        }
    }
}
