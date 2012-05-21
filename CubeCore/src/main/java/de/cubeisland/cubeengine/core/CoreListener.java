package de.cubeisland.cubeengine.core;

import de.cubeisland.cubeengine.core.permission.Perm;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 *
 * @author Faithcaio
 */
public class CoreListener implements Listener
{
    private final UserManager cuManager;
    
    public CoreListener(UserManager cuManager)
    {
        this.cuManager = cuManager;
    }
    
    @EventHandler
    public void goesOnline(final PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        if (Perm.USE.isAuthorized(player))
        {
            if (cuManager.getUser(player) == null)
                cuManager.addUser(new User(player));
            //else: User is already created
        }
        //else: Player has no Permission to use any CubeEngine Module
        
    }
}
