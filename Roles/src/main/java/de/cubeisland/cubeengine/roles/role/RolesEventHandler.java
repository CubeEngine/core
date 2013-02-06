package de.cubeisland.cubeengine.roles.role;

import de.cubeisland.cubeengine.core.module.event.FinishedLoadModulesEvent;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserAuthorizedEvent;
import de.cubeisland.cubeengine.roles.Roles;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class RolesEventHandler implements Listener
{
    private Roles module;
    private RoleManager manager;

    public RolesEventHandler(Roles module)
    {
        this.manager = module.getManager();
        this.module = module;
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event)
    {
        this.manager.preCalculateRoles(event.getPlayer().getName(), false);
        long worldFromId = this.module.getCore().getWorldManager().getWorldId(event.getFrom());
        long worldToId = this.module.getCore().getWorldManager().getWorldId(event.getPlayer().getWorld());
        WorldRoleProvider fromProvider = this.manager.getProvider(worldFromId);
        WorldRoleProvider toProvider = this.manager.getProvider(worldToId);
        if (fromProvider.equals(toProvider))
        {
            if (toProvider.getWorlds().get(worldToId).getRight())
            {
                return;
            }
        }
        this.manager.applyRole(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event)
    {//TODO SYNC this!!!
        this.manager.preCalculateRoles(event.getName(), false);
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event)
    {
        this.manager.preCalculateRoles(event.getPlayer().getName(), false);
        this.manager.applyRole(event.getPlayer());
    }

    @EventHandler
    public void onAllModulesLoaded(FinishedLoadModulesEvent event)
    {
        manager.init();
        for (User user : module.getUserManager().getOnlineUsers()) // reapply roles on reload
        {
            user.removeAttribute(module, "roleContainer"); // remove potential old calculated roles
            manager.preCalculateRoles(user.getName(), false);
            manager.applyRole(user.getPlayer());
        }
    }

    @EventHandler
    public void onAuthorized(UserAuthorizedEvent event)
    {
        this.manager.reloadAllRolesAndApply(event.getUser(), event.getUser().getPlayer());
    }
}
