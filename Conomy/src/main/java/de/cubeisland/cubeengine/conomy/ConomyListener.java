package de.cubeisland.cubeengine.conomy;

import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class ConomyListener implements Listener
{
    private final Conomy conomy;
    private final ConomyAPI api;

    public ConomyListener(Conomy conomy)
    {
        this.conomy = conomy;
        this.api = conomy.getApi();
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event)
    {
        User user = this.conomy.getUserManager().getExactUser(event.getPlayer());
        if (!this.api.hasAccount(user))
        {
            this.api.createUserAccount(user);
            this.api.resetAllAccountsToDefault(user);
        }
    }
}
