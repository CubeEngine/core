package de.cubeisland.cubeengine.roles.role;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import de.cubeisland.cubeengine.roles.Roles;
import gnu.trove.map.hash.THashMap;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

public class RoleManager
{
    private THashMap<String, RoleConfig> configs = new THashMap<String, RoleConfig>();
    private THashMap<String, RoleConfig> globalConfigs = new THashMap<String, RoleConfig>();
    private THashMap<String, Role> globalRoles = new THashMap<String, Role>();
    private THashMap<String, Role> roles = new THashMap<String, Role>();
    private final File roleFolder;
    private final File globalRoleFolder;
    private final Roles module;

    public RoleManager(Roles rolesModule)
    {
        this.module = rolesModule;
        /**
         * Configuration.load(RoleConfig.class, new File(this.getFolder(),
         * "guest.yml")); Configuration.load(RoleConfig.class, new
         * File(this.getFolder(), "member.yml"));
         * Configuration.load(RoleConfig.class, new File(this.getFolder(),
         * "moderator.yml")); Configuration.load(RoleConfig.class, new
         * File(this.getFolder(), "admin.yml"));
         */
        this.roleFolder = new File(rolesModule.getFolder(), "roles");
        this.globalRoleFolder = new File(rolesModule.getFolder(), "globalroles");
        this.roleFolder.mkdir();
        this.globalRoleFolder.mkdir();
        this.module.getLogger().debug("Loading global roles...");
        int i = 0;
        for (File file : globalRoleFolder.listFiles())
        {

            if (file.getName().endsWith(".yml"))
            {
                RoleConfig config = Configuration.load(RoleConfig.class, file);
                this.globalConfigs.put(config.roleName, config);
            }

        }
        this.module.getLogger().debug(i + " global roles loaded!");
        rolesModule.getLogger().debug("Loading roles...");
        i = 0;
        for (File file : roleFolder.listFiles())
        {
            i++;
            if (file.getName().endsWith(".yml"))
            {
                RoleConfig config = Configuration.load(RoleConfig.class, file);
                this.configs.put(config.roleName, config);
            }
        }
        this.module.getLogger().debug(i + " roles loaded!");
        this.calculateRoles();
    }
    private Stack<String> roleStack;

    private void calculateRoles()
    {
        roleStack = new Stack<String>();
        // Calculate global roles:
        for (String roleName : this.globalConfigs.keySet())
        {
            this.calculateRole(this.globalConfigs.get(roleName), true);
        }
        // Calculate global roles:
        for (String roleName : this.configs.keySet())
        {
            this.calculateRole(this.configs.get(roleName), false);
        }
    }

    private boolean calculateRole(RoleConfig config, boolean global)
    {
        try
        {
            Role role;
            role = global ? this.globalRoles.get(config.roleName) : this.roles.get(config.roleName);
            if (role != null)
            {
                return true;
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
                    if (global)
                    {
                        parentConfig = this.globalConfigs.get(parentName);
                    }
                    else
                    {
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
                            parentConfig = this.configs.get(parentName);
                        }
                    }
                    if (parentConfig == null) // Dependency Missing?
                    {
                        throw new RoleDependencyMissingException("Could not find the role " + parentName);
                    }
                    this.calculateRole(parentConfig, global); // calculate parent-role
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
                    if (global)
                    {
                        parentRole = this.globalRoles.get(parentName);
                    }
                    else
                    {
                        if (parentName.startsWith("g:"))
                        {
                            parentRole = this.globalRoles.get(parentName.substring(2));
                        }
                        else
                        {
                            parentRole = this.roles.get(parentName);
                        }
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
            Role mergedParentRole = this.mergeRoles(parenRoles); //merge together parentRoles  
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
            this.roleStack.pop();
            if (global)
            {
                this.globalRoles.put(config.roleName, role);
            }
            else
            {
                this.roles.put(config.roleName, role);
            }
        }
        catch (CircularRoleDepedencyException ex)
        {
            this.module.getLogger().log(LogLevel.WARNING, ex.getMessage());
            return false;
        }
        return true;
    }

    private Role mergeRoles(Collection<Role> roles)
    {
        if (roles.size() == 1)
        {
            return roles.iterator().next();
        }
        Role result = new Role();
        Map<String, Pair<Boolean, Priority>> permissions = new HashMap<String, Pair<Boolean, Priority>>();
        Map<String, Pair<String, Priority>> metaData = new HashMap<String, Pair<String, Priority>>();
        for (Role role : roles)
        {
            for (Entry<String, Boolean> permission : role.getPermissions().entrySet())
            {
                if (permissions.containsKey(permission.getKey()))
                {
                    if (role.getPriority().value < permissions.get(permission.getKey()).getRight().value)
                    {
                        continue;
                    }
                }
                permissions.put(permission.getKey(), new Pair<Boolean, Priority>(permission.getValue(), role.getPriority()));
            }
            for (Entry<String, String> data : role.getMetaData().entrySet())
            {
                if (metaData.containsKey(data.getKey()))
                {
                    if (role.getPriority().value < metaData.get(data.getKey()).getRight().value)
                    {
                        continue;
                    }
                }
                metaData.put(data.getKey(), new Pair<String, Priority>(data.getValue(), role.getPriority()));
            }
        }
        for (String permission : permissions.keySet())
        {
            result.setPermission(permission, permissions.get(permission).getLeft());
        }
        for (String data : metaData.keySet())
        {
            result.setMetaData(data, metaData.get(data).getLeft());
        }
        return result;
    }
}
