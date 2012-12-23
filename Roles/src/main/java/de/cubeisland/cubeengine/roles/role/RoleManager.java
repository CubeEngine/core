package de.cubeisland.cubeengine.roles.role;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.exception.CircularRoleDepedencyException;
import de.cubeisland.cubeengine.roles.exception.RoleDependencyMissingException;
import de.cubeisland.cubeengine.roles.role.config.RoleConfig;
import de.cubeisland.cubeengine.roles.role.config.RoleMirror;
import de.cubeisland.cubeengine.roles.role.config.RoleProvider;
import de.cubeisland.cubeengine.roles.storage.AssignedRole;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import org.bukkit.entity.Player;

public class RoleManager
{
    private THashMap<String, RoleConfig> globalConfigs = new THashMap<String, RoleConfig>();
    private THashMap<String, Role> globalRoles = new THashMap<String, Role>();
    private final File rolesFolder;
    private final Roles module;
    private TLongObjectHashMap<RoleProvider> providers = new TLongObjectHashMap<RoleProvider>();

    public RoleManager(Roles rolesModule)
    {
        this.module = rolesModule;
        this.rolesFolder = new File(rolesModule.getFolder(), "roles");
    }

    public void saveAllConfigs()
    {
        for (Configuration config : this.globalConfigs.values())
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
        this.module.getLogger().debug("Loading global roles...");
        int i = 0;
        for (File file : rolesFolder.listFiles())
        {
            if (file.getName().endsWith(".yml"))
            {
                RoleConfig config = Configuration.load(RoleConfig.class, file);
                this.globalConfigs.put(config.roleName, config);
            }
        }
        this.module.getLogger().debug(i + " global roles loaded!");
        // World roles:
        this.createAllProviders();
        for (RoleProvider provider : this.providers.valueCollection())
        {
            provider.init(this.rolesFolder);
        }
        this.recalculateAllRoles();
        for (RoleProvider provider : this.providers.valueCollection())
        {
            provider.loadDefaultRoles(this.module.getConfiguration());
        }
    }
    private Stack<String> roleStack;

    public void recalculateAllRoles()
    {
        this.roleStack = new Stack<String>();
        // Calculate global roles:
        this.module.getLogger().debug("Calculating global Roles...");
        for (String roleName : this.globalConfigs.keySet())
        {
            Role role = this.calculateGlobalRole(this.globalConfigs.get(roleName));
            if (role == null)
            {
                this.module.getLogger().log(LogLevel.WARNING, roleName + " could not be calculated!");
            }
            this.globalRoles.put(roleName, role);
        }
        // Calculate world roles for each world-provider:
        for (RoleProvider provider : providers.valueCollection())
        {
            if (!provider.isCalculated())
            {
                this.module.getLogger().debug("Calculating roles for " + provider.getMainWorld() + "...");
                provider.calculateRoles(this.globalRoles, false);
            }
        }
    }

    private Role calculateGlobalRole(RoleConfig config)
    {
        try
        {
            Role role = this.globalRoles.get(config.roleName);
            if (role != null)
            {
                return role;
            }
            this.roleStack.push(config.roleName);
            for (String parentName : config.parents)
            {
                try
                {
                    if (this.roleStack.contains(parentName)) // Circular Dependency?
                    {
                        throw new CircularRoleDepedencyException("Cannot load role! Circular Depenency detected in " + config.roleName + "\n" + StringUtils.implode(", ", roleStack));
                    }
                    RoleConfig parentConfig = this.globalConfigs.get(parentName);
                    if (parentConfig == null) // Dependency Missing?
                    {
                        throw new RoleDependencyMissingException("Could not find the role " + parentName);
                    }
                    this.calculateGlobalRole(parentConfig); // calculate parent-role
                }
                catch (RoleDependencyMissingException ex)
                {
                    this.module.getLogger().log(LogLevel.WARNING, ex.getMessage());
                }
            }
            List<Role> parentRoles = new ArrayList<Role>();
            for (String parentName : config.parents)
            {
                try
                {
                    Role parentRole = this.globalRoles.get(parentName);
                    if (parentRole == null)
                    {
                        throw new RoleDependencyMissingException("Parent role " + parentName + " for " + config.roleName + " is missing.");
                    }
                    parentRoles.add(parentRole); // Role was found:
                }
                catch (RoleDependencyMissingException ex)
                {
                    this.module.getLogger().log(LogLevel.WARNING, ex.getMessage());
                }
            }
            role = new ConfigRole(config, parentRoles, true);

            this.roleStack.pop();
            this.module.getLogger().debug(role.getName() + " loaded!");
            return role;
        }
        catch (CircularRoleDepedencyException ex)
        {
            this.module.getLogger().log(LogLevel.WARNING, ex.getMessage());
            return null;
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
            RoleProvider provider = new RoleProvider(mirror);
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
                }
            }
        }
        for (long worldId : this.module.getCore().getWorldManager().getAllWorldIds())
        {
            if (this.getProvider(worldId) == null)
            {
                this.providers.put(worldId, new RoleProvider(worldId));
            }
        }
    }

    public RoleProvider getProvider(Long worldID)
    {
        return this.providers.get(worldID);
    }

    public Collection<RoleProvider> getProviders()
    {
        return this.providers.valueCollection();
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
        User user = this.module.getUserManager().getUser(username, true);
        if (user.getAttribute(this.module, "roleContainer") != null)
        {
            return; // Roles are calculated!
        }
        TLongObjectHashMap<List<Role>> userRolesPerWorld = new TLongObjectHashMap<List<Role>>();
        for (RoleProvider provider : this.getProviders())
        {
            TLongObjectHashMap<List<Role>> pRolesPerWorld = provider.getRolesFor(user, reload);
            userRolesPerWorld.putAll(pRolesPerWorld);
        }
        TLongObjectHashMap<MergedRole> roleContainer = new TLongObjectHashMap<MergedRole>();

        TLongObjectHashMap<THashMap<String, Boolean>> userSpecificPerms = this.module.getDbUserPerm().getForUser(user.key);
        TLongObjectHashMap<THashMap<String, String>> userSpecificMeta = this.module.getDbUserMeta().getForUser(user.key);

        for (long worldId : userRolesPerWorld.keys())
        {
            this.preCalculateRole(user, roleContainer, userRolesPerWorld.get(worldId), worldId, userSpecificPerms.get(worldId), userSpecificMeta.get(worldId));
        }
        user.setAttribute(this.module, "roleContainer", roleContainer);
    }

    public void preCalculateRole(User user, TLongObjectHashMap<MergedRole> roleContainer, List<Role> roles, long worldId, THashMap<String, Boolean> userPerms, THashMap<String, String> userMeta)
    {
        // UserSpecific Settings:
        MergedRole userSpecificRole = new MergedRole(user.getName(), userPerms, userMeta);
        // Roles Assigned to this user:
        MergedRole mergedRole = null;
        for (MergedRole inContainer : roleContainer.valueCollection())
        {
            if (inContainer.getMergedWith().equals(roles))
            {
                mergedRole = inContainer;
            }
        }
        if (mergedRole == null)
        {
            mergedRole = new MergedRole(roles); // merge all assigned roles
        }
        userSpecificRole.applyInheritence(mergedRole);
        roleContainer.put(worldId, userSpecificRole);
    }

    public void applyRole(Player player, long worldId)
    {
        User user = this.module.getUserManager().getExactUser(player);
        TLongObjectHashMap<MergedRole> roleContainer = user.getAttribute(module, "roleContainer");
        MergedRole role = roleContainer.get(worldId);
        if (role == null)
        {
            Set<Role> roles = this.getProvider(worldId).getDefaultRoles();
            this.addRoles(user, player, worldId, roles.toArray(new Role[roles.size()]));
            return;
        }
        user.setPermission(role.resolvePermissions(), player);
        user.setAttribute(this.module, "metadata", role.getMetaData());
    }

    public void reloadAndApplyRole(User user, Player player, long worldId)
    {
        user.removeAttribute(this.module, "roleContainer");
        this.preCalculateRoles(user.getName(), true);
        this.applyRole(player, worldId);
    }

    public boolean addRoles(User user, Player player, long worldId, Role... roles)
    {
        TLongObjectHashMap<MergedRole> roleContainer = user.getAttribute(module, "roleContainer");
        boolean added = false;
        for (Role role : roles)
        {
            if (roleContainer.get(worldId) == null
                    || !roleContainer.get(worldId).getParentRoles().contains(role))
            {
                added = true;
                this.module.getDbManager().store(new AssignedRole(user.key, worldId, role.getName()));
            }
        }
        if (!added)
        {
            return false;
        }
        user.removeAttribute(this.module, "roleContainer");
        this.reloadAndApplyRole(user, player, worldId);
        return true;
    }

    public boolean removeRole(User user, Role role, long worldId)
    {
        TLongObjectHashMap<MergedRole> roleContainer = user.getAttribute(module, "roleContainer");
        if (!roleContainer.get(worldId).getParentRoles().contains(role))
        {
            return false;
        }
        this.module.getDbManager().delete(user.key, role.getName(), worldId);
        user.removeAttribute(this.module, "roleContainer");
        this.reloadAndApplyRole(user, user.getPlayer(), worldId);
        return true;
    }

    public Set<Role> clearRoles(User user, long worldId)
    {
        this.module.getDbManager().clear(user.key, worldId);
        Set<Role> result = this.getProvider(worldId).getDefaultRoles();

        this.addRoles(user, user.getPlayer(), worldId, result.toArray(new Role[result.size()]));
        user.removeAttribute(this.module, "roleContainer");
        this.reloadAndApplyRole(user, user.getPlayer(), worldId);
        return result;
    }
}
