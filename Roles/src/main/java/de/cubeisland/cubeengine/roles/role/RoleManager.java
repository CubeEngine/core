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
import de.cubeisland.cubeengine.roles.role.config.RoleProvider;
import de.cubeisland.cubeengine.roles.storage.AssignedRole;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import org.bukkit.entity.Player;

public class RoleManager
{

    private THashMap<String, RoleConfig> globalConfigs = new THashMap<String, RoleConfig>();
    private THashMap<String, Role> globalRoles = new THashMap<String, Role>();
    private final File rolesFolder;
    private final Roles module;
    private TIntObjectHashMap<RoleProvider> providers = new TIntObjectHashMap<RoleProvider>();

    public RoleManager(Roles rolesModule)
    {
        this.module = rolesModule;
        this.rolesFolder = new File(rolesModule.getFolder(), "roles");
        this.init();
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

    public void init()
    {
        this.loadProviders();
        this.rolesFolder.mkdir();
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
        for (RoleProvider provider : this.providers.valueCollection())
        {
            provider.init(rolesFolder);
        }
        this.calculateRoles();
        for (RoleProvider provider : this.providers.valueCollection())
        {
            provider.loadDefaultRoles(this.module.getConfiguration());
        }
    }
    private Stack<String> roleStack;

    public void calculateRoles()
    {
        roleStack = new Stack<String>();
        // Calculate global roles:
        for (String roleName : this.globalConfigs.keySet())
        {
            Role role = this.calculateGlobalRole(this.globalConfigs.get(roleName));
            if (role == null)
            {
                this.module.getLogger().log(LogLevel.WARNING, roleName + " could not be calculated!");
            }
            this.globalRoles.put(roleName, role);
        }
        // Calculate world roles:
        for (RoleProvider provider : providers.valueCollection())
        {
            provider.calculateRoles(this.globalRoles);
        }
    }
//TODO make sure when calculating ALL other plugins are loaded
    //and/or all permissions are registered

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
                        throw new RoleDependencyMissingException("Needed parent role " + parentName + " for " + config.roleName + " not found.");
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
            return role;
        }
        catch (CircularRoleDepedencyException ex)
        {
            this.module.getLogger().log(LogLevel.WARNING, ex.getMessage());
            return null;
        }
    }

    private void loadProviders()
    {
        this.providers.clear();
        for (RoleProvider provider : this.module.getConfiguration().providers)
        {
            TIntObjectHashMap<Pair<Boolean, Boolean>> worlds = provider.getWorlds();
            for (int worldId : worlds.keys())
            {
                if (this.providers.containsKey(worldId))
                {
                    this.module.getLogger().log(LogLevel.ERROR,
                            "The world " + this.module.getCore().getWorldManager().getWorld(worldId).getName() + " is mirrored multiple times!\n"
                            + "Check your configuration under mirrors." + provider.mainWorld);
                    continue;
                }
                if (worlds.get(worldId).getLeft()) // Roles are mirrored add to provider...
                {
                    this.providers.put(worldId, provider);
                }
            }
        }
        for (int worldId : this.module.getCore().getWorldManager().getAllWorldIds())
        {
            if (this.getProvider(worldId) == null)
            {
                this.providers.put(worldId, new RoleProvider(worldId));
            }
        }
    }

    public RoleProvider getProvider(Integer worldID)
    {
        return this.providers.get(worldID);
    }

    public Collection<RoleProvider> getProviders()
    {
        return this.providers.valueCollection();
    }
    private TIntObjectHashMap<TIntObjectHashMap<List<String>>> loadedUserRoles = new TIntObjectHashMap<TIntObjectHashMap<List<String>>>();

    public TIntObjectHashMap<List<String>> loadRoles(User user)
    {
        TIntObjectHashMap<List<String>> result = this.loadedUserRoles.get(user.key);
        if (result == null)
        {
            return this.reloadRoles(user);
        }
        return result;
    }

    public TIntObjectHashMap<List<String>> reloadRoles(User user)
    {
        TIntObjectHashMap<List<String>> result = this.module.getDbManager().getRolesByUser(user);
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
            return;
        }
        TIntObjectHashMap<List<Role>> rolesPerWorld = new TIntObjectHashMap<List<Role>>();
        for (RoleProvider provider : this.getProviders())
        {
            TIntObjectHashMap<List<Role>> pRolesPerWorld = provider.getRolesFor(user, reload);
            rolesPerWorld.putAll(pRolesPerWorld);
        }
        TIntObjectHashMap<MergedRole> roleContainer = new TIntObjectHashMap<MergedRole>();

        TIntObjectHashMap<THashMap<String, Boolean>> userSpecificPerms = this.module.getDbUserPerm().getForUser(user.key);
        TIntObjectHashMap<THashMap<String, String>> userSpecificMeta = this.module.getDbUserMeta().getForUser(user.key);

        for (int worldId : rolesPerWorld.keys())
        {
            // UserSpecific Settings:
            MergedRole userSpecificRole = new MergedRole(username, userSpecificPerms.get(worldId), userSpecificMeta.get(worldId));
            // Roles Assigned to this user:
            MergedRole mergedRole = null;
            for (MergedRole inContainer : roleContainer.valueCollection())
            {
                if (inContainer.getMergedWith().equals(rolesPerWorld.get(worldId)))
                {
                    mergedRole = inContainer;
                }
            }
            if (mergedRole == null)
            {
                List<Role> roles = rolesPerWorld.get(worldId);
                mergedRole = new MergedRole(roles); // merge all assigned roles
            }
            userSpecificRole.applyInheritence(mergedRole);
            roleContainer.put(worldId, userSpecificRole);
        }
        user.setAttribute(this.module, "roleContainer", roleContainer);
    }

    public void applyRole(Player player, int worldId)
    {
        User user = this.module.getUserManager().getExactUser(player);
        TIntObjectHashMap<MergedRole> roleContainer = user.getAttribute(module, "roleContainer");
        MergedRole role = roleContainer.get(worldId);
        if (role == null)
        {
            List<Role> roles = this.getProvider(worldId).getDefaultRoles();
            this.addRoles(user, worldId, roles.toArray(new Role[roles.size()]));
            return;
        }
        user.setPermission(role.resolvePermissions(), player);
        user.setAttribute(this.module, "metadata", role.getMetaData());
    }

    public boolean addRoles(User user, int worldId, Role... roles)
    {
        TIntObjectHashMap<MergedRole> roleContainer = user.getAttribute(module, "roleContainer");
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
        this.preCalculateRoles(user.getName(), true); //TODO only recalculate & apply if needed/what is needed
        this.applyRole(user, worldId);
        return true;
    }

    public boolean removeRole(User user, Role role, int worldId)
    {
        TIntObjectHashMap<MergedRole> roleContainer = user.getAttribute(module, "roleContainer");
        if (!roleContainer.get(worldId).getParentRoles().contains(role))
        {
            return false;
        }
        this.module.getDbManager().delete(user.key, role.getName(), worldId);
        user.removeAttribute(this.module, "roleContainer");
        this.preCalculateRoles(user.getName(), true); //TODO only recalculate & apply if needed/what is needed
        this.applyRole(user, worldId);
        return true;
    }

    public List<Role> clearRoles(User user, int worldId)
    {
        this.module.getDbManager().clear(user.key, worldId);
        List<Role> result = this.getProvider(worldId).getDefaultRoles();

        this.addRoles(user, worldId, result.toArray(new Role[result.size()]));
        user.removeAttribute(this.module, "roleContainer");
        this.preCalculateRoles(user.getName(), true);
        this.applyRole(user, worldId);
        return result;
    }
}
