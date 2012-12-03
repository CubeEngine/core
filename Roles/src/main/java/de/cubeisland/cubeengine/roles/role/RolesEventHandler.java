package de.cubeisland.cubeengine.roles.role;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.roles.Roles;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerLoginEvent;

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
        this.preCalculateRoles(event.getPlayer().getName());
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
        this.applyRole(event.getPlayer(), worldToId);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event)
    {
        this.preCalculateRoles(event.getName());
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event)
    {
        this.preCalculateRoles(event.getPlayer().getName());
        this.applyRole(event.getPlayer(), this.module.getCore().getWorldManager().getWorldId(event.getPlayer().getWorld()));
    }

    /**
     * Calculates the roles in each world for this player.
     *
     * @param username
     */
    public void preCalculateRoles(String username)
    {
        User user = this.module.getUserManager().getUser(username, true);
        if (user.getAttribute(this.module, "roleContainer") != null) // TODO be sure that this gets removed when reloading
        {
            return;
        }
        TIntObjectHashMap<List<Role>> rolesPerWorld = new TIntObjectHashMap<List<Role>>();
        for (RoleProvider provider : manager.getProviders())
        {
            TIntObjectHashMap<List<Role>> pRolesPerWorld = provider.getRolesFor(user);
            rolesPerWorld.putAll(pRolesPerWorld);
        }
        TIntObjectHashMap<MergedRole> roleContainer = new TIntObjectHashMap<MergedRole>();
        for (int worldId : rolesPerWorld.keys())
        {
            MergedRole mergedRole = null;
            for (MergedRole inContainer : roleContainer.valueCollection())
            {
                if (inContainer.mergedWith.equals(rolesPerWorld.get(worldId)))
                {
                    mergedRole = inContainer;
                }
            }
            if (mergedRole == null)
            {
                mergedRole = new MergedRole(rolesPerWorld.get(worldId)); // merge all assigned roles
            }
            roleContainer.put(worldId, mergedRole);
        }
        user.setAttribute(this.module, "roleContainer", roleContainer);
    }

    public void applyRole(Player player, int worldId)
    {
        User user = this.module.getUserManager().getExactUser(player);
        TIntObjectHashMap<MergedRole> roleContainer = user.getAttribute(module, "roleContainer");
        MergedRole role = roleContainer.get(worldId);
        user.setPermission(role.getPermissions(), player);
        //TODO set metadata
    }
}
