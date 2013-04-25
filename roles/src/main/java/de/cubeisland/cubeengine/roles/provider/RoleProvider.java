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
import de.cubeisland.cubeengine.roles.exception.CircularRoleDependencyException;
import de.cubeisland.cubeengine.roles.exception.RoleDependencyMissingException;

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
