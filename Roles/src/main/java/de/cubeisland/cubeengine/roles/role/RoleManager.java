package de.cubeisland.cubeengine.roles.role;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.storage.world.WorldManager;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.RolesAttachment;
import de.cubeisland.cubeengine.roles.role.config.RoleMirror;
import de.cubeisland.cubeengine.roles.storage.AssignedRole;

import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TLongObjectHashMap;

import static de.cubeisland.cubeengine.core.logger.LogLevel.DEBUG;

public class RoleManager
{
    private final File rolesFolder;
    private final Roles module;
    private TLongObjectHashMap<WorldRoleProvider> providers = new TLongObjectHashMap<WorldRoleProvider>();
    private GlobalRoleProvider globalProvider;
    private WorldManager worldManager;

    public RoleManager(Roles rolesModule)
    {
        this.module = rolesModule;
        this.worldManager = rolesModule.getCore().getWorldManager();
        this.rolesFolder = new File(rolesModule.getFolder(), "roles");
    }

    public void saveAllConfigs()
    {
        for (Configuration config : this.globalProvider.getConfigs())
        {
            config.save();
        }
        for (RoleProvider provider : this.providers.valueCollection())
        {
            for (Configuration config : provider.getConfigs())
            {
                config.save();
            }
        }
    }

    /**
     * Initializes the RoleManager and all RoleProviders and Roles for currently
     * loaded worlds.
     */
    public void init()
    {
        this.rolesFolder.mkdir();
        // Global roles:
        this.globalProvider = new GlobalRoleProvider(module);
        this.globalProvider.loadInConfigurations(rolesFolder);
        // World roles:
        this.createAllProviders();
        for (RoleProvider provider : this.providers.valueCollection())
        {
            provider.loadInConfigurations(this.rolesFolder);
        }
        this.recalculateAllRoles();
        for (WorldRoleProvider provider : this.providers.valueCollection())
        {
            provider.loadDefaultRoles(this.module.getConfiguration());
        }
    }

    public void recalculateAllRoles()
    {
        this.module.getLogger().log(DEBUG, "Calculating global Roles...");
        this.globalProvider.calculateRoles(true);
        // Calculate world roles for each world-provider:
        for (WorldRoleProvider provider : providers.valueCollection())
        {
            if (!provider.isCalculated())
            {
                this.module.getLogger().log(DEBUG, "Calculating roles for " + provider.getMainWorld() + "...");
                provider.calculateRoles(false);
            }
        }
    }

    /**
     * Clears and recreates all needed providers with their respective
     * RoleMirrors
     */
    private void createAllProviders()
    {
        this.providers.clear();
        for (RoleMirror mirror : this.module.getConfiguration().mirrors)
        {
            WorldRoleProvider provider = new WorldRoleProvider(module, mirror);
            TLongObjectHashMap<Pair<Boolean, Boolean>> worlds = provider.getWorlds();
            for (long worldId : worlds.keys())
            {
                if (this.providers.containsKey(worldId))
                {
                    this.module.getLogger().log(LogLevel.ERROR,
                            "The world " + this.module.getCore().getWorldManager().getWorld(worldId).getName() + " is mirrored multiple times!\n"
                                + "Check your configuration under mirrors." + provider.getMainWorld());
                    continue;
                }
                if (worlds.get(worldId).getLeft()) // Roles are mirrored add to provider...
                {
                    this.providers.put(worldId, provider);
                    this.module.getLogger().log(LogLevel.DEBUG,"loading role-provider for "+worldManager.getWorld(worldId).getName());
                }
            }
        }
        for (long worldId : this.module.getCore().getWorldManager().getAllWorldIds())
        {
            if (this.getProvider(worldId) == null)
            {
                this.providers.put(worldId, new WorldRoleProvider(module, worldId));
                this.module.getLogger().log(LogLevel.DEBUG,"loading missing role-provider for "+worldManager.getWorld(worldId).getName());
            }
        }
    }

    public WorldRoleProvider getProvider(Long worldID)
    {
        return this.providers.get(worldID);
    }

    public <Provider extends RoleProvider> Provider getProvider(World world)
    {
        return (Provider)(world == null ? this.globalProvider : this.getProvider(this.worldManager.getWorldId(world)));
    }

    public Collection<WorldRoleProvider> getProviders()
    {
        return this.providers.valueCollection();
    }

    public GlobalRoleProvider getGlobalProvider()
    {
        return globalProvider;
    }

    private TLongObjectHashMap<TLongObjectHashMap<List<String>>> loadedUserRoles = new TLongObjectHashMap<TLongObjectHashMap<List<String>>>();

    public TLongObjectHashMap<List<String>> loadRoles(User user)
    {
        TLongObjectHashMap<List<String>> result = this.loadedUserRoles.get(user.key);
        if (result == null)
        {
            return this.reloadRoles(user);
        }
        return result;
    }

    public TLongObjectHashMap<List<String>> reloadRoles(User user)
    {
        TLongObjectHashMap<List<String>> result = this.module.getDbManager().getRolesByUser(user);
        this.loadedUserRoles.put(user.key, result);
        return result;
    }

    /**
     * Calculates the roles in each world for this player.
     *
     * @param username
     */
    public void preCalculateRoles(String username, boolean reload)
    {
        User user = this.module.getCore().getUserManager().getUser(username, true);
        this.preCalculateRoles(user, reload);
    }

    public void preCalculateRoles(User user, boolean reload)
    {
        if (!reload && this.getRolesAttachment(user).hasRoleContainer())
        {
            return; // Roles are calculated!
        }
        TLongObjectHashMap<List<Role>> userRolesPerWorld = new TLongObjectHashMap<List<Role>>();
        for (WorldRoleProvider provider : this.getProviders())
        {
            TLongObjectHashMap<List<Role>> pRolesPerWorld = provider.getRolesFor(user, reload);
            userRolesPerWorld.putAll(pRolesPerWorld);
        }
        TLongObjectHashMap<UserSpecificRole> roleContainer = new TLongObjectHashMap<UserSpecificRole>();
        TLongObjectHashMap<THashMap<String, Boolean>> userSpecificPerms = this.module.getDbUserPerm().getForUser(user.key);
        TLongObjectHashMap<THashMap<String, String>> userSpecificMeta = this.module.getDbUserMeta().getForUser(user.key);

        for (long worldId : this.worldManager.getAllWorldIds())
        {
            this.preCalculateRole(user, roleContainer, userRolesPerWorld.get(worldId), worldId, userSpecificPerms.get(worldId), userSpecificMeta.get(worldId));
        }
        this.getRolesAttachment(user).setRoleContainer(roleContainer);
    }

    private void preCalculateRole(User user, TLongObjectHashMap<UserSpecificRole> roleContainer, List<Role> roles, long worldId, THashMap<String, Boolean> userPerms, THashMap<String, String> userMeta)
    {
        // UserSpecific Settings:
        UserSpecificRole userSpecificRole = new UserSpecificRole(this.module, user, worldId, userPerms, userMeta);
        if (roles != null)
        {
            // Roles Assigned to this user:
            MergedRole mergedRole = new MergedRole(roles); // merge all assigned roles
            // Apply inheritance
            userSpecificRole.applyInheritence(mergedRole);
        }
        roleContainer.put(worldId, userSpecificRole);
    }

    public void applyRole(Player player)
    {
        if (player == null)
        {
            throw new IllegalArgumentException("Cannot apply role to null player!");
        }
        this.applyRole(player, this.module.getCore().getWorldManager().getWorldId(player.getWorld()));
    }

    private void applyRole(Player player, long worldId)
    {
        User user = this.module.getCore().getUserManager().getExactUser(player);
        if (!Bukkit.getServer().getOnlineMode() && this.module.getConfiguration().doNotAssignPermIfOffline && !user.isLoggedIn())
        {
            user.sendMessage("roles", "&cThe server is currently running in offline-mode. Permissions will not be applied until logging in! Contact an Admin if you think this is an error.");
            this.module.getLogger().warning("Role-permissions not applied! Server is running in unsecured offline-mode!");
            return;
        }
        this.module.getLogger().log(LogLevel.DEBUG,"User-role set: "+ user.getName());
        TLongObjectHashMap<UserSpecificRole> roleContainer = this.getRoleContainer(user);
        UserSpecificRole role = roleContainer.get(worldId);
        if (role.getParentRoles().isEmpty())
        {
            Set<Role> roles = this.getProvider(worldId).getDefaultRoles();
            this.addRoles(user, player, worldId, roles.toArray(new Role[roles.size()]));
            return;
        }
        user.setPermission(role.resolvePermissions(), player);
        this.getRolesAttachment(user).setMetaData(role.getMetaData());
    }

    public void reloadAllRolesAndApply(User user, Player player)
    {
        this.getRolesAttachment(user).removeRoleContainer();
        this.preCalculateRoles(user.getName(), true);
        if (user.isOnline())
        {
            this.applyRole(player);
        }
    }

    public boolean addRoles(User user, Player player, long worldId, Role... roles)
    {
        TLongObjectHashMap<UserSpecificRole> roleContainer = this.getRoleContainer(user);
        if (roleContainer == null)
        {
            this.preCalculateRoles(user, true);
            roleContainer = this.getRoleContainer(user);
        }
        boolean added = false;
        for (Role role : roles)
        {
            if (roleContainer.get(worldId) == null
                    || !roleContainer.get(worldId).getParentRoles().contains(role))
            {
                added = true;
                this.module.getDbManager().store(new AssignedRole(user.key, worldId, role.getName()), false);
            }
        }
        if (!added)
        {
            return false;
        }
        this.getRolesAttachment(user).removeRoleContainer();
        this.reloadAllRolesAndApply(user, player);
        return true;
    }

    public TLongObjectHashMap<UserSpecificRole> getRoleContainer(User user)
    {
        return this.getRolesAttachment(user).getRoleContainer();
    }

    public RolesAttachment getRolesAttachment(User user)
    {
        return user.attachOrGet(RolesAttachment.class,this.module);
    }

    public boolean removeRole(User user, Role role, long worldId)
    {
        TLongObjectHashMap<UserSpecificRole> roleContainer = this.getRoleContainer(user);
        if (!roleContainer.get(worldId).getParentRoles().contains(role))
        {
            return false;
        }
        this.module.getDbManager().delete(user.key, role.getName(), worldId);
        this.reloadAllRolesAndApply(user, user.getPlayer());
        return true;
    }

    public Set<Role> clearRoles(User user, long worldId)
    {
        this.module.getDbManager().clear(user.key, worldId);
        Set<Role> result = this.providers.get(worldId).getDefaultRoles();

        this.addRoles(user, user.getPlayer(), worldId, result.toArray(new Role[result.size()]));
        this.getRolesAttachment(user).removeRoleContainer();
        this.reloadAllRolesAndApply(user, user.getPlayer());
        return result;
    }

    public THashMap<String, Role> getGlobalRoles()
    {
        return this.globalProvider.getRoles();
    }

    /**
     * Creates a new role
     *
     * @param roleName
     * @param world the worldId or null for global-roles
     * @return
     */
    public boolean createRole(String roleName, World world)
    {
        if (world == null)
        {
            return this.globalProvider.createRole(roleName);
        }
        else
        {
            return this.getProvider(world).createRole(roleName);
        }
    }

    /**
     * Gets the role for the specified worldId
     *
     * @param worldId the id of the world to lookup the role in
     * @param roleName the role to lookup
     * @return the role OR null if not found
     */
    public Role getRoleInWorld(long worldId, String roleName)
    {
        RoleProvider provider = this.getProvider(worldId);
        return provider.getRole(roleName);
    }
}
