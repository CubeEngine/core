/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.cubeengine.roles.role;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.Stack;

import de.cubeisland.cubeengine.core.config.Configuration;

import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.config.RoleConfig;
import de.cubeisland.cubeengine.roles.exception.CircularRoleDependencyException;

import gnu.trove.map.hash.THashMap;



public abstract class RoleProvider
{
    protected Roles module;
    protected RolesManager manager;

    protected THashMap<String, RoleConfig> configs;
    protected THashMap<String, Role> roles;
    protected Permission basePerm;
    protected File folder;
    protected final long mainWorldId;

    protected RoleProvider(Roles module, RolesManager manager, long mainWorldId)
    {
        this.module = module;
        this.manager = manager;
        this.mainWorldId = mainWorldId;
    }

    /**
     * Gets the role with given name in the worlds managed by this RoleProvider
     *
     * @param name
     * @return
     */
    public Role getRole(String name)
    {
        Role role = this.roles.get(name.toLowerCase());
        if (role != null && role.isDirty())
        {
            this.calculateRole(role,new Stack<String>());
        }
        return role;
    }

    /**
     * Gets all available roles in the worlds managed by this RoleProvider
     *
     * @return
     */
    public Collection<Role> getRoles()
    {
        return this.roles.values();
    }

    /**
     * Gets the folder where the configurations of this RoleProvider are saved.
     *
     * @return
     */
    protected abstract File getFolder();

    /**
     * Loads in the configurations. Also removes all currently loaded roles.
     */
    protected void loadConfigurations()
    {
        this.configs = new THashMap<String, RoleConfig>();
        this.roles = new THashMap<String, Role>();
        for (User user : this.module.getCore().getUserManager().getLoadedUsers())
        {
            RolesAttachment rolesAttachment = user.get(RolesAttachment.class);
            if (rolesAttachment != null)
            {
                rolesAttachment.flushResolvedData();
            }
        }
        this.getFolder().mkdir(); // Creates folder for this provider if not existent
        int i = 0;
        for (File configFile : this.getFolder().listFiles())
        {
            if (configFile.getName().endsWith(".yml"))
            {
                ++i;
                RoleConfig config = Configuration.load(RoleConfig.class, configFile);
                this.configs.put(config.roleName.toLowerCase(), config);
            }
        }
        this.module.getLog().debug(this.getFolder().getName()+ ": " + i + " role-configs read!");
    }

    /**
     * Loads in all roles from the previously loaded configurations
     */
    protected void reloadRoles()
    {
        for (RoleConfig config : this.configs.values())
        {
            Role role = new Role(this, config);
            this.roles.put(role.getName().toLowerCase(Locale.ENGLISH), role);
        }
    }

    /**
     * ReCalculates all roles
     */
    public void recalculateRoles()
    {
        Stack<String> roleStack = new Stack<String>(); // stack for detecting circular dependencies
        for (Role role : this.roles.values())
        {
            this.calculateRole(role, roleStack);
        }

    }

    /**
     * Calculates a single role, resolving/calculating its dependencies
     *
     * @param role
     * @param roleStack
     * @return
     */
    protected Role calculateRole(Role role, Stack<String> roleStack)
    {
        if (role.isDirty())
        {
            try
            {
                roleStack.push(role.getName());
                for (String parentName : role.getRawAssignedRoles())
                {
                    if (roleStack.contains(parentName)) // Circular Dependency?
                    {
                        throw new CircularRoleDependencyException("Cannot load role! Circular Dependency detected in " + role.getName() + "\n" + StringUtils
                            .implode(", ", roleStack));
                    }
                    Role parentRole = this.getRole(parentName);
                    if (parentRole == null) // Dependency Missing?
                    {
                        this.module.getLog().warn("ParentRole missing for \"" + role.getName() + "\"\nUnknown role: " + parentName);
                    }
                    this.calculateRole(parentRole,roleStack);
                }
                // now all parent roles should be loaded
                Set<Role> parentRoles = new HashSet<Role>();
                for (String parentName : role.getRawAssignedRoles())
                {
                    Role parentRole = this.getRole(parentName);
                    if (parentRole == null)
                    {
                        continue; // In case parent role was missing ignore it
                    }
                    parentRoles.add(parentRole);
                }
                Permission perm = this.basePerm.createChild(role.getName());
                this.module.getCore().getPermissionManager().registerPermission(this.module,perm);
                ResolvedDataStore data = new ResolvedDataStore(role);
                data.calculate(parentRoles);
                role.resolvedData = data;
                roleStack.pop();
                this.module.getLog().debug("   - " + role.getName() + " calculated!");
                return role;
            }
            catch (CircularRoleDependencyException ex)
            {
                this.module.getLog().warn(ex.getMessage());
                return null;
            }
        }
        return role;
    }

    /**
     * Returns the ID of the main world of this RoleProvider.
     *
     * @return the main-worldID or 0 if GlobalProvider
     */
    public long getMainWorldId()
    {
        return mainWorldId;
    }

    /**
     * Creates a new Role with given name
     *
     * @param roleName
     * @return
     */
    public Role createRole(String roleName)
    {
        if (roleName.length() > 255)
        {
            throw new IllegalArgumentException("The max. length for rolenames is 255!");
        }
        roleName = roleName.toLowerCase(Locale.ENGLISH);
        if (this.roles.containsKey(roleName))
        {
            return null;
        }
        RoleConfig config = new RoleConfig();
        config.roleName = roleName;
        this.configs.put(roleName,config);
        config.onLoaded(null);
        config.setFile(new File(this.folder, roleName + ".yml"));
        config.save();
        Role role = new Role(this, config);
        this.roles.put(roleName,role);
        this.calculateRole(role, new Stack<String>());
        return role;
    }

    /**
     * Deletes a role forever.
     *
     * @param role
     */
    protected void deleteRole(Role role)
    {
        for (ResolvedDataStore crole : role.resolvedData.dependentData)
        {
            crole.rawDataStore.removeRole(role);
        }
        this.roles.remove(role.getName());
        this.configs.remove(role.getName());

        this.manager.rm.deleteRole(this.mainWorldId, role.getName());
    }

    /**
     * Renames a role
     *
     * @param role
     * @param newName
     * @return
     */
    protected boolean renameRole(Role role, String newName)
    {
        if (this.getRole(newName) != null)
        {
            return false;
        }
        for (ResolvedDataStore resolvedDataStore : role.resolvedData.dependentData)
        {
            resolvedDataStore.rawDataStore.removeRole(role);
        }
        this.configs.remove(role.getName());
        this.roles.remove(role.getName());
        role.config.roleName = newName;
        this.configs.put(role.getName(),role.config);
        this.roles.put(role.getName(),role);
        for (ResolvedDataStore resolvedDataStore : role.resolvedData.dependentData)
        {
            resolvedDataStore.rawDataStore.assignRole(role);
        }
        role.saveConfigToNewFile();

        return true;
    }
}
