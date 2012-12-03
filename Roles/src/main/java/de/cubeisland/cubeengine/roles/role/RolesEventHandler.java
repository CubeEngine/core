package de.cubeisland.cubeengine.roles.role;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.roles.Roles;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.List;
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
        if (user.getAttribute(this.module, "roleContainer") != null)
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

    public void applyRole(String username)
    {
        //TODO do assign permissions in the user. give Map <String,Boolean> or a String and boolean
        //
        /*
         Player player = (Player) p;

         Permission positive = plugin.getServer().getPluginManager().getPermission(player.getName());
         Permission negative = plugin.getServer().getPluginManager().getPermission("^" + player.getName());

         if (positive != null)
         {
         plugin.getServer().getPluginManager().removePermission(positive);
         }
         if (negative != null)
         {
         plugin.getServer().getPluginManager().removePermission(negative);
         }

         Map<String, Boolean> po = new HashMap<String, Boolean>();
         Map<String, Boolean> ne = new HashMap<String, Boolean>();

         for (String key : permissions.keySet())
         {
         if (permissions.get(key))
         {
         po.put(key, true);
         }
         else
         {
         ne.put(key, false);
         }
         }

         positive = new Permission(player.getName(), PermissionDefault.FALSE, po);
         negative = new Permission("^" + player.getName(), PermissionDefault.FALSE, ne);

         plugin.getServer().getPluginManager().addPermission(positive);
         plugin.getServer().getPluginManager().addPermission(negative);
         PermissionAttachment att = null;
         for (PermissionAttachmentInfo pai : player.getEffectivePermissions())
         {
         if (pai.getAttachment() != null && pai.getAttachment().getPlugin() != null)
         {
         if (pai.getAttachment().getPlugin() instanceof Permissions)
         {
         att = pai.getAttachment();
         break;
         }
         }
         }
         // only if null
         if (att == null)
         {
         att = player.addAttachment(plugin);
         att.setPermission(player.getName(), true);
         att.setPermission("^" + player.getName(), true);
         }
         // recalculate permissions
         player.recalculatePermissions();
         return att; 
         //*/
    }
}
