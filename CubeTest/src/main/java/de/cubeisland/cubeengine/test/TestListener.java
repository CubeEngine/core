package de.cubeisland.cubeengine.test;

import de.cubeisland.cubeengine.CubeEngine;
import de.cubeisland.cubeengine.core.event.EventListener;
import de.cubeisland.cubeengine.core.user.User;
import java.util.logging.Level;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 *
 * @author Anselm Brehme
 */
public class TestListener implements Listener, EventListener
{
    CubeTest test;
    
    public TestListener(CubeTest test)
    {
        this.test = test;
    }

    @EventHandler
    public void playerInteract(final PlayerInteractEvent event)
    {
        User user = CubeEngine.getUserManager().getUser(event.getPlayer());
        user.sendMessage("INTERACT!");
    }
    
    @EventHandler
    public void playerInteract(final PlayerJoinEvent event)
    {
        User user = CubeEngine.getUserManager().getUser(event.getPlayer());
        user.sendMessage("JOIN!");
        test.getLogger().log(Level.INFO, "{0} joined!", event.getPlayer().getName());
    }
}
