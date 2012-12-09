package de.cubeisland.cubeengine.roles.role;

import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.role.config.RoleProvider;
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
        this.manager.preCalculateRoles(event.getPlayer().getName(),false);
        int worldFromId = this.module.getCore().getWorldManager().getWorldId(event.getFrom());
        int worldToId = this.module.getCore().getWorldManager().getWorldId(event.getPlayer().getWorld());
        RoleProvider fromProvider = this.manager.getProvider(worldFromId);
        RoleProvider toProvider = this.manager.getProvider(worldToId);
        if (fromProvider.equals(toProvider))
        {
            if (toProvider.getWorlds().get(worldToId).getRight())
            {
                return;
            }
        }
        this.manager.applyRole(event.getPlayer(), worldToId);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event)
    {//TODO SYNC this!!!
        this.manager.preCalculateRoles(event.getName(),false);
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event)
    {
        this.manager.preCalculateRoles(event.getPlayer().getName(),false);
        this.manager.applyRole(event.getPlayer(), this.module.getCore().getWorldManager().getWorldId(event.getPlayer().getWorld()));
    }

    
}
