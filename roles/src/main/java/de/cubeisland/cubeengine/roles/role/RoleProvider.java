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
import java.util.Locale;
import java.util.Stack;
import java.util.TreeSet;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.config.RoleConfig;
import de.cubeisland.cubeengine.roles.exception.CircularRoleDependencyException;

import gnu.trove.map.hash.THashMap;

import static de.cubeisland.cubeengine.core.logger.LogLevel.DEBUG;

public abstract class RoleProvider
{
    protected Roles module;
    protected RolesManager manager;

    protected THashMap<String, RoleConfig> configs = new THashMap<String, RoleConfig>();
    protected THashMap<String, Role> roles = new THashMap<String, Role>();
    protected Permission basePerm;
    protected File folder;
    protected final long mainWorldId;

    protected RoleProvider(Roles module, RolesManager manager, long mainWorldId)
    {
        this.module = module;
        this.manager = manager;
        this.mainWorldId = mainWorldId;
    }

    public Role getRole(String name)
    {
        return this.roles.get(name.toLowerCase());
    }

    public Collection<Role> getRoles()
    {
        return this.roles.values();
    }

    public abstract File getFolder();

    public void loadConfigurations()
    {
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
        this.module.getLog().log(DEBUG, i + " role-configs read!");
    }

    public void reloadRoles()
    {
        for (RoleConfig config : this.configs.values())
        {
            Role role = new Role(config, this.mainWorldId);
            this.roles.put(role.getName().toLowerCase(Locale.ENGLISH), role);
        }
    }

    public void recalculateRoles()
    {
        Stack<String> roleStack = new Stack<String>(); // stack for detecting circular dependencies
        for (Role role : this.roles.values())
        {
            this.calculateRole(role, roleStack);
        }
    }

    public Role calculateRole(Role role, Stack<String> roleStack)
    {
        if (role.isDirty())
        {
            try
            {
                roleStack.push(role.getName());
                for (String parentName : role.getRawParents())
                {
                    if (roleStack.contains(parentName)) // Circular Dependency?
                    {
                        throw new CircularRoleDependencyException("Cannot load role! Circular Dependency detected in " + role.getName() + "\n" + StringUtils
                            .implode(", ", roleStack));
                    }
                    Role parentRole = this.getRole(parentName);
                    if (parentRole == null) // Dependency Missing?
                    {
                        this.module.getLog().log(LogLevel.WARNING, "ParentRole missing for \"" + role.getName() + "\"\nUnknown role: " + parentName);
                    }
                    this.calculateRole(parentRole,roleStack);
                }
                // now all parent roles should be loaded
                TreeSet<Role> parentRoles = new TreeSet<Role>();
                for (String parentName : role.getRawParents())
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
                this.module.getLog().log(DEBUG, role.getName() + " calculated!");
                return role;
            }
            catch (CircularRoleDependencyException ex)
            {
                this.module.getLog().log(LogLevel.WARNING, ex.getMessage());
                return null;
            }
        }
        return role;
    }

    public void recalculateDirtyRoles()
    {

    }

    public long getMainWorldId()
    {
        return mainWorldId;
    }

    // TODO create Role

    // TODO handle renaming a role
    // TODO defaultRole
}
