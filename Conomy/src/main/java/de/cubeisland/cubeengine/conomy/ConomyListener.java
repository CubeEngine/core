package de.cubeisland.cubeengine.conomy;

import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class ConomyListener implements Listener
{
    private final Conomy conomy;

    public ConomyListener(Conomy conomy)
    {
        this.conomy = conomy;
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event)
    {
        User user = this.conomy.getUserManager().getExactUser(event.getPlayer());
        if (!this.conomy.getAccountsManager().hasAccount(user))
        {
            this.conomy.getAccountsManager().createNewAccount(user);
        }
    }
}
