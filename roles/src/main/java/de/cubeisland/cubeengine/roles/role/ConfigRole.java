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

import de.cubeisland.cubeengine.roles.config.Priority;
import de.cubeisland.cubeengine.roles.config.RoleConfig;
import java.io.File;
import java.util.TreeSet;

import org.bukkit.permissions.Permissible;

import de.cubeisland.cubeengine.core.permission.Permission;

public class ConfigRole extends Role
{
    private RoleConfig config;
    private Permission rolePermission;

    /**
     * Creates a new ConfigRole from given config with parents
     *
     * @param config the config to load from
     * @param parentRoles the declared parent roles
     * @param isGlobal true if this is a global role
     * @param permission the permission of this role;
     */
    public ConfigRole(RoleConfig config, TreeSet<Role> parentRoles, boolean isGlobal, Permission permission)
    {
        super(config.roleName, config.priority, config.perms, parentRoles, config.metadata, isGlobal);
        this.applyInheritence(new MergedRole(parentRoles));
        this.config = config;
        for (Role role : parentRoles)
        {
            role.addChild(this);
        }
        this.rolePermission = permission;
    }
    //TODO save config when change is made with ingame cmd

    public void saveConfigToFile()
    {
        this.config.save();
    }

    public void saveConfigToNewFile()
    {
        this.config.getFile().delete();
        this.config.setFile(new File(this.config.getFile().getParentFile(), this.config.roleName + ".yml"));
        this.config.save();
    }

    @Override
    public void setPermission(String perm, Boolean set)
    {
        this.makeDirty();
        if (set != null)
        {
            this.config.perms.getPermissions().put(perm, set);
        }
        else
        {
            this.config.perms.getPermissions().remove(perm);
        }
        this.saveConfigToFile();
    }

    @Override
    public void setMetaData(String key, String value)
    {
        this.makeDirty();
        if (value != null)
        {
            this.config.metadata.put(key, value);
        }
        else
        {
            this.config.metadata.remove(key);
        }
        this.saveConfigToFile();
    }

    @Override
    public void clearMetaData()
    {
        this.makeDirty();
        this.config.metadata.clear();
        this.saveConfigToFile();
    }

    @Override
    public boolean setParentRole(String pRole)
    {
        this.makeDirty();
        boolean added = this.config.parents.add(pRole);
        this.saveConfigToFile();
        return added;
    }

    @Override
    public boolean removeParentRole(String pRole)
    {
        if (this.config.parents.remove(pRole))
        {
            this.makeDirty();
            this.saveConfigToFile();
            return true;
        }
        return false;
    }

    @Override
    public void clearParentRoles()
    {
        this.makeDirty();
        this.config.parents.clear();
        this.saveConfigToFile();
    }

    @Override
    public void setPriority(Priority priority)
    {
        this.makeDirty();
        this.config.priority = priority;
        this.saveConfigToFile();
    }

    @Override
    public void rename(String newName)
    {
        this.makeDirty();
        this.config.roleName = newName;
        this.saveConfigToNewFile();
        for (Role role : this.childRoles)
        {
            role.removeParentRole(this.name);
            role.setParentRole(newName);
        }
    }

    public void deleteConfigFile()
    {
        this.config.getFile().delete();
    }

    /**
     * Returns true if the permissible has the permission to assign this role to another player
     *
     * @param permissible
     * @return
     */
    public boolean canAssignAndRemove(Permissible permissible)
    {
        return this.rolePermission.isAuthorized(permissible);
    }
}
