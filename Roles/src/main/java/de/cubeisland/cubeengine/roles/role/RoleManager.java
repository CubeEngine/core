package de.cubeisland.cubeengine.roles.role;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import de.cubeisland.cubeengine.roles.Roles;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

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
        this.loadProviders();
        this.rolesFolder = new File(rolesModule.getFolder(), "roles");
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
        for (File file : rolesFolder.listFiles())
        {
            if (file.isDirectory())
            {
                //check if is a world
                Integer worldID = CubeEngine.getCore().getWorldManager().getWorldId(file.getName());
                if (worldID == null)
                {
                    this.module.getLogger().log(LogLevel.WARNING, "The world " + file.getName() + " does not exist. Ignoring folder...");
                }
                this.module.getLogger().debug("Loading roles for the world " + file.getName() + ":");
                i = 0;
                for (File configFile : file.listFiles())
                {
                    ++i;
                    if (file.getName().endsWith(".yml"))
                    {
                        RoleConfig config = Configuration.load(RoleConfig.class, configFile);
                        this.getProvider(worldID).addConfig(config);
                    }
                }
                this.module.getLogger().debug(i + " roles loaded!");
            }
        }
        this.calculateRoles();
    }
    private Stack<String> roleStack;

    private void calculateRoles()
    {
        roleStack = new Stack<String>();
        // Calculate global roles:
        for (String roleName : this.globalConfigs.keySet())
        {
            Role role = this.calculateGlobalRole(this.globalConfigs.get(roleName));
            if (role == null)
            {
                //TODO msg not loaded
            }
            this.globalRoles.put(roleName, role);
        }
        // Calculate world roles:
        for (RoleProvider provider : providers.valueCollection())
        {
            for (RoleConfig config : provider.getConfigs())
            {
                Role role = this.calculateRole(config, provider);
                if (role == null)
                {
                    //TODO msg not loaded
                }
                provider.setRole(role);
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
            role = new Role(config);
            List<Role> parenRoles = new ArrayList<Role>();
            for (String parentName : config.parents)
            {
                try
                {
                    Role parentRole = this.globalRoles.get(parentName);
                    if (parentRole == null)
                    {
                        throw new RoleDependencyMissingException("Needed parent role " + parentName + " for " + config.roleName + " not found.");
                    }
                    parenRoles.add(parentRole); // Role was found:
                }
                catch (RoleDependencyMissingException ex)
                {
                    this.module.getLogger().log(LogLevel.WARNING, ex.getMessage());
                }
            }
            role.setParentRoles(parenRoles); // Add all roles found
            Role mergedParentRole = new MergedRole(parenRoles); //merge together parentRoles  
            this.applyInheritence(role, mergedParentRole);
            this.roleStack.pop();
            return role;
        }
        catch (CircularRoleDepedencyException ex)
        {
            this.module.getLogger().log(LogLevel.WARNING, ex.getMessage());
            return null;
        }
    }

    private void applyInheritence(Role role, Role mergedParentRole)
    {
        //now calculate inheritance:
        // 1. resolve permissions
        Map<String, Boolean> permissions = role.getPermissions();
        for (Entry<String, Boolean> perm : mergedParentRole.getPermissions().entrySet())
        {
            if (!permissions.containsKey(perm.getKey())) //if not overridden
            {
                permissions.put(perm.getKey(), perm.getValue()); //add to inheritance
            }
        }
        // 2. resolve metadata
        Map<String, String> metaData = role.getMetaData();
        for (Entry<String, String> data : mergedParentRole.getMetaData().entrySet())
        {
            if (!metaData.containsKey(data.getKey())) //if not overridden
            {
                metaData.put(data.getKey(), data.getValue()); //add to inheritance
            }
        }
    }

    private Role calculateRole(RoleConfig config, RoleProvider provider)
    {
        try
        {
            Role role = provider.getRole(config.roleName);
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
                    RoleConfig parentConfig;

                    if (parentName.startsWith("g:"))
                    {
                        if (this.globalRoles.containsKey(parentName.substring(2)))
                        {
                            continue;
                        }
                        throw new RoleDependencyMissingException("Could not find the global role " + parentName);
                    }
                    else
                    {
                        parentConfig = provider.getConfig(parentName);
                    }
                    if (parentConfig == null) // Dependency Missing?
                    {
                        throw new RoleDependencyMissingException("Could not find the role " + parentName);
                    }
                    this.calculateRole(parentConfig, provider); // calculate parent-role
                }
                catch (RoleDependencyMissingException ex)
                {
                    this.module.getLogger().log(LogLevel.WARNING, ex.getMessage());
                }
            }
            // now all parent roles should be loaded
            role = new Role(config);
            List<Role> parenRoles = new ArrayList<Role>();
            for (String parentName : config.parents)
            {
                try
                {
                    Role parentRole;

                    if (parentName.startsWith("g:"))
                    {
                        parentRole = this.globalRoles.get(parentName.substring(2));
                    }
                    else
                    {
                        parentRole = provider.getRole(parentName);
                    }
                    if (parentRole == null)
                    {
                        throw new RoleDependencyMissingException("Needed parent role " + parentName + " for " + config.roleName + " not found.");
                    }
                    parenRoles.add(parentRole); // Role was found:
                }
                catch (RoleDependencyMissingException ex)
                {
                    this.module.getLogger().log(LogLevel.WARNING, ex.getMessage());
                }
            }
            role.setParentRoles(parenRoles); // Add all roles found
            Role mergedParentRole = new MergedRole(parenRoles); //merge together parentRoles  
            this.applyInheritence(role, mergedParentRole);
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

    private RoleProvider getProvider(Integer worldID)
    {
        return this.providers.get(worldID);
    }

    public Collection<RoleProvider> getProviders()
    {
        return this.providers.valueCollection();
    }
    private TIntObjectHashMap<TIntObjectHashMap<List<String>>> loadedUserRoles;

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
}
