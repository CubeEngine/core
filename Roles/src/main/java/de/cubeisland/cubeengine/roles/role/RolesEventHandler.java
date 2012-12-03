package de.cubeisland.cubeengine.roles.role;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.roles.Roles;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class RolesEventHandler implements Listener
{
    private final Roles module;
    private final RoleManager manager;

    public RolesEventHandler(Roles module)
    {
        this.manager = module.getManager();
        this.module = module;
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event)
    {
        //TODO check if need to change permissions
        this.applyRole(event.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event)
    {
        this.preCalculateRoles(event.getName());
    }

    /**
     * Calculates the roles in each world for this player.
     * 
     * @param username 
     */
    public void preCalculateRoles(String username)
    {
        User user = this.module.getUserManager().getUser(username, true);
        TIntObjectHashMap<Role> roleContainer = new TIntObjectHashMap<Role>();
        for (RoleProvider provider : manager.getProviders())
        {
            TIntObjectHashMap<Role> pRoleContainer = provider.getMergedRolesFor(user);
            roleContainer.putAll(pRoleContainer);
        }
        user.setAttribute(this.module, "roleContainer", roleContainer);
    }

    /**
     * Sets the permissions and metaData for this player
     * 
     * @param username 
     */
    public void applyRole(String username)
    {
        //TODO   
    }
}
