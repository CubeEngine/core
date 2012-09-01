package de.cubeisland.cubeengine.test;

import de.cubeisland.cubeengine.CubeEngine;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.StringUtils;
import java.util.logging.Level;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 *
 * @author Anselm Brehme
 */
public class TestListener implements Listener
{
    CubeTest test;
    
    public TestListener(CubeTest test)
    {
        this.test = test;
    }

    @EventHandler
    public void playerInteract(final PlayerInteractEvent event)
    {
        UserManager uM = CubeEngine.getUserManager();
        User user = uM.getUser(event.getPlayer());
        user.sendMessage("INTERACT!");
        User founduser = uM.findUser(user.getName());
        founduser.sendMessage("I found You!");
        founduser = uM.findUser(user.getName().substring(1));
        founduser.sendMessage("I still found You!1");
        founduser = uM.findUser(user.getName().substring(2));
        founduser.sendMessage("I still found You!2");
        founduser = uM.findUser(user.getName().substring(4));
        if (founduser == null)
        {
            user.sendMessage("Could not find you!4");
        }
    }
    
    @EventHandler
    public void playerJoin(final PlayerJoinEvent event)
    {
        test.getLogger().log(Level.INFO, "{0} joined!", event.getPlayer().getName());
    }
}
