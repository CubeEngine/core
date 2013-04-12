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
package de.cubeisland.cubeengine.roles.provider;

import java.io.File;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.roles.RoleManager;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.config.Priority;
import de.cubeisland.cubeengine.roles.config.RoleConfig;
import de.cubeisland.cubeengine.roles.exception.CircularRoleDepedencyException;
import de.cubeisland.cubeengine.roles.exception.RoleDependencyMissingException;
import de.cubeisland.cubeengine.roles.role.ConfigRole;
import de.cubeisland.cubeengine.roles.role.Role;

import gnu.trove.map.hash.THashMap;
import org.apache.commons.lang.Validate;

import static de.cubeisland.cubeengine.core.logger.LogLevel.DEBUG;

public abstract class RoleProvider
{
    protected THashMap<String, RoleConfig> configs = new THashMap<String, RoleConfig>();
    protected THashMap<String, ConfigRole> roles = new THashMap<String, ConfigRole>();
    protected File folder = null;

    public final boolean isGlobal;
    protected final Permission basePerm;

    protected Roles module;
    protected RoleManager manager;

    public RoleProvider(Roles module, boolean isGlobal, Permission basePerm)
    {
        this.module = module;
        this.isGlobal = isGlobal;
        this.manager = module.getRoleManager();
        this.basePerm = basePerm;
    }

    public Iterable<RoleConfig> getConfigs()
    {
        return this.configs.values();
    }

    public void addConfig(RoleConfig config)
    {
        this.configs.put(config.roleName.toLowerCase(Locale.ENGLISH), config);
    }

    public ConfigRole getRole(String roleName)
    {
        Validate.notNull(this.roles, "The RoleName cannot be null!");
        if (roleName.startsWith("g:"))
        {
            return this.module.getRoleManager().getGlobalRoles().get(roleName.substring(2).toLowerCase(Locale.ENGLISH));
        }
        return this.roles.get(roleName.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Initializes this RoleProvider with its configurations
     *
     * @param rolesFolder
     */
    public void loadInConfigurations(File rolesFolder)
    {
        this.folder.mkdir(); // Creates folder for this provider if not existent
        int i = 0;
        for (File configFile : this.folder.listFiles())
        {
            if (configFile.getName().endsWith(".yml"))
            {
                ++i;
                RoleConfig config = Configuration.load(RoleConfig.class, configFile);
                this.addConfig(config);
            }
        }
        this.module.getLog().log(DEBUG, i + " roles read!");
    }

    /**
     * Searches for dirty roles in this provider and recalculates all those and dependent roles.
     * Then reapply the roles to users if needed.
     */
    public void recalculateDirtyRoles()
    {
        Set<Role> dirtyChilds = new HashSet<Role>();
        for (Role role : this.roles.values())
        {
            if (role.isDirty())
            {
                dirtyChilds.add(role);
            }
        }
        this.recalculateDirtyRoles(dirtyChilds);
        this.reapplyDirtyRoles();
    }

    /**
     * Searches for dirtyRoles in the user-roles and recalculates the user-role if found.
     */
    public abstract void reapplyDirtyRoles();

    /**
     * Recalculates this set of dirty roles and also recursively their child-roles.
     *
     * @param dirtyRoles a set of dirtyRoles
     */
    private void recalculateDirtyRoles(Set<Role> dirtyRoles)
    {
        Set<Role> dirtyChilds = new HashSet<Role>();
        for (Role role : dirtyRoles)
        {
            String roleName = role.getName().toLowerCase(Locale.ENGLISH);
            this.roles.remove(roleName);
            this.roles.put(roleName, this.calculateRole(this.configs.get(roleName)));
            if (this instanceof WorldRoleProvider)
            {
                if (((WorldRoleProvider)this).getDefaultRoles().remove(role)) // replace old DefaultRole
                {
                    ((WorldRoleProvider)this).getDefaultRoles().add(this.roles.get(roleName));
                }
            }
            for (Role childRole : role.getChildRoles())
            {
                childRole.makeDirty();
                dirtyChilds.add(childRole);
            }
        }
        if (!dirtyChilds.isEmpty())
        {
            this.recalculateDirtyRoles(dirtyChilds);
        }
    }

    /**
     * Calculates all the roles of this provider.
     *
     * @return
     */
    public boolean calculateRoles()
    {
        for (RoleConfig config : this.configs.values())
        {
            ConfigRole role = this.calculateRole(config);
            if (role == null)
            {
                this.module.getLog().log(LogLevel.WARNING, config.roleName + " could not be calculated!");
                continue;
            }
            this.roles.put(role.getName().toLowerCase(Locale.ENGLISH), role);
        }
        return true;
    }

    private Stack<String> roleStack = new Stack<String>(); // stack for detecting circular dependencies

    /**
     * Calculates a Role with given RoleConfig also resolve its dependencies
     *
     * @param config
     * @return
     */
    public ConfigRole calculateRole(RoleConfig config)
    {
        try
        {
            ConfigRole role = this.getRole(config.roleName);
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
                        throw new CircularRoleDepedencyException("Cannot load role! Circular Depenency detected in " + config.roleName + "\n" + StringUtils.implode(", ", this.roleStack));
                    }
                    RoleConfig parentConfig = null;

                    if (parentName.startsWith("g:"))
                    {
                        if (this.manager.getGlobalProvider().getRole(parentName.substring(2)) != null)
                        {
                            continue;
                        }
                    }
                    else
                    {
                        parentConfig = this.configs.get(parentName);
                    }
                    if (parentConfig == null) // Dependency Missing?
                    {
                        throw new RoleDependencyMissingException("ParentRole missing for \"" + config.roleName + "\"\nUnkown role: " + parentName);
                    }
                    ConfigRole parentRole = this.calculateRole(parentConfig); // calculate parent-role
                    if (parentRole != null)
                    {
                        this.roles.put(parentRole.getName().toLowerCase(Locale.ENGLISH), parentRole);
                    }
                }
                catch (RoleDependencyMissingException ex)
                {
                    this.module.getLog().log(LogLevel.WARNING, ex.getMessage());
                }
            }
            // now all parent roles should be loaded
            TreeSet<ConfigRole> parentRoles = new TreeSet<ConfigRole>();
            for (String parentName : config.parents)
            {
                ConfigRole parentRole;
                parentName = parentName.toLowerCase(Locale.ENGLISH);
                if (parentName.startsWith("g:"))
                {
                    parentRole = this.manager.getGlobalProvider().getRole(parentName.substring(2));
                }
                else
                {
                    parentRole = this.roles.get(parentName);
                }
                if (parentRole == null)
                {
                    continue; // In case parent role was missing ingore it
                }
                parentRoles.add(parentRole); // Role was found:
            }
            Permission perm = this.basePerm.createChild(config.roleName);
            this.module.getCore().getPermissionManager().registerPermission(this.module,perm);
            role = new ConfigRole(config, parentRoles, isGlobal, perm);
            this.roleStack.pop();
            this.module.getLog().log(DEBUG, role.getName() + " calculated!");
            return role;
        }
        catch (CircularRoleDepedencyException ex)
        {
            this.module.getLog().log(LogLevel.WARNING, ex.getMessage());
            return null;
        }
    }

    /**
     * Returns all the roles of this provider
     *
     * @return
     */
    public THashMap<String, ConfigRole> getRoles()
    {
        return this.roles;
    }

    public void setRolePermission(ConfigRole role, String perm, Boolean set)
    {
        role.setPermission(perm, set);
        this.recalculateDirtyRoles();
    }

    public void setRoleMetaData(ConfigRole role, String key, String value)
    {
        role.setMetaData(key, value);
        this.recalculateDirtyRoles();
    }

    public void resetRoleMetaData(ConfigRole role, String key)
    {
        role.setMetaData(key, null);
        this.recalculateDirtyRoles();
    }

    public void clearRoleMetaData(ConfigRole role)
    {
        role.clearMetaData();
        this.recalculateDirtyRoles();
    }

    public boolean setParentRole(ConfigRole role, Role pRole) throws CircularRoleDepedencyException
    {
        this.checkCircularDependency(role, pRole);
        boolean added = role.setParentRole(pRole.getName());
        if (added)
        {
            this.recalculateDirtyRoles();
        }
        return added;
    }

    private void checkCircularDependency(Role role, Role pRole) throws CircularRoleDepedencyException
    {
        for (Role cRole : pRole.getParentRoles())
        {
            if (cRole.equals(role))
            {
                throw new CircularRoleDepedencyException("Cannot add parent!");
            }
            this.checkCircularDependency(role, cRole);
        }
    }

    public boolean removeParentRole(ConfigRole role, Role pRole)
    {
        boolean removed = role.removeParentRole(pRole.getName());
        if (removed)
        {
            this.recalculateDirtyRoles();
        }
        return removed;
    }

    public void clearParentRoles(ConfigRole role)
    {
        role.clearParentRoles();
        this.recalculateDirtyRoles();
    }

    public void setRolePriority(ConfigRole role, Priority priority)
    {
        role.setPriority(priority);
        this.recalculateDirtyRoles();
    }

    public boolean renameRole(ConfigRole role, String newName)
    {
        newName = newName.toLowerCase(Locale.ENGLISH);
        if (this.roles.containsKey(newName))
        {
            return false;
        }
        role.rename(newName);
        // Removing old role
        RoleConfig config = this.configs.remove(role.getName().toLowerCase(Locale.ENGLISH));
        this.roles.remove(role.getName());
        // Set new role
        this.configs.put(newName.toLowerCase(Locale.ENGLISH), config);
        ConfigRole newRole = this.calculateRole(config);
        newRole.setChildRoles(role.getChildRoles());
        this.roles.put(newName, newRole);
        if (this instanceof WorldRoleProvider)
        {
            this.module.getDbManager().rename((WorldRoleProvider)this, role.getName(), newName);
        }
        // Recalculate dependend roles
        this.recalculateDirtyRoles();
        return true;
    }

    public boolean createRole(String roleName)
    {
        roleName = roleName.toLowerCase(Locale.ENGLISH);
        if (this.roles.containsKey(roleName))
        {
            return false;
        }
        RoleConfig config = new RoleConfig();
        config.roleName = roleName;
        this.configs.put(roleName.toLowerCase(Locale.ENGLISH), config);
        config.onLoaded(null);
        config.setFile(new File(this.folder, roleName + ".yml"));
        config.save();
        this.roles.put(roleName, this.calculateRole(config));
        return true;
    }

    public void deleteRole(ConfigRole role)
    {
        for (ConfigRole crole : role.getChildRoles())
        {
            crole.removeParentRole(role.getName());
        }
        this.roles.remove(role.getName());
        this.configs.remove(role.getName());
        role.deleteConfigFile();
        if (this instanceof WorldRoleProvider)
        {
            for (long worldID : ((WorldRoleProvider)this).getWorlds().keys())
            {
                this.module.getDbManager().deleteRole(worldID, role.getName());
            }
        }
        this.recalculateDirtyRoles();
    }
}
