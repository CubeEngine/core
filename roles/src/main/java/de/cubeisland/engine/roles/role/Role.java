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
package de.cubeisland.engine.roles.role;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.permissions.Permissible;

import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.roles.config.Priority;
import de.cubeisland.engine.roles.config.RoleConfig;
import de.cubeisland.engine.roles.exception.CircularRoleDependencyException;
import de.cubeisland.engine.roles.role.resolved.ResolvedMetadata;
import de.cubeisland.engine.roles.role.resolved.ResolvedPermission;

import gnu.trove.map.hash.THashMap;

public class Role implements RawDataStore,Comparable<Role>
{
    protected RoleConfig config;
    protected ResolvedDataStore resolvedData;
    protected boolean isDefaultRole = false;
    protected RoleProvider roleProvider;
    protected Permission rolePermission;

    public Role(RoleProvider roleProvider, RoleConfig config)
    {
        this.config = config;
        this.roleProvider = roleProvider;
        this.rolePermission = roleProvider.basePerm.createChild(config.roleName);
    }

    public Map<String,Boolean> getRawPermissions()
    {
        return Collections.unmodifiableMap(config.perms.getPermissions());
    }

    public Map<String,String> getRawMetadata()
    {
        return Collections.unmodifiableMap(config.metadata);
    }

    public Set<String> getRawAssignedRoles()
    {
        return Collections.unmodifiableSet(this.config.parents);
    }

    public void saveToConfig()
    {
        this.config.save();
        if (this.resolvedData != null)
        {
            for (ResolvedDataStore resolvedDataStore : this.resolvedData.dependentData)
            {
                if (resolvedDataStore.rawDataStore instanceof Role)
                {
                    ((Role)resolvedDataStore.rawDataStore).saveToConfig();
                }
            }
        }
    }

    protected void saveConfigToNewFile()
    {
        this.config.getFile().delete();
        this.config.setFile(new File(this.config.getFile().getParentFile(), this.config.roleName + ".yml"));
        this.saveToConfig();
    }

    public void reloadFromConfig()
    {
        this.config.reload();
        this.resolvedData = null;
    }

    public String getName()
    {
        return this.config.roleName;
    }

    protected void makeDirty()
    {
        if (this.resolvedData != null)
        {
            this.resolvedData.makeDirty();
        }
    }

    public boolean isDirty()
    {
        if (this.resolvedData == null) return true;
        return this.resolvedData.isDirty();
    }

    public boolean isGlobal()
    {
        return this.roleProvider instanceof GlobalRoleProvider;
    }

    public boolean inheritsFrom(Role other)
    {
        return this.resolvedData.inheritsFrom(other);
    }

    public boolean rename(String newName)
    {
        this.makeDirty();
        return this.roleProvider.renameRole(this,newName);
    }

    public int getPriorityValue()
    {
        return this.config.priority.value;
    }

    public void setPriorityValue(int value)
    {
        this.makeDirty();
        this.config.priority = Priority.getByValue(value);
    }

    @Override
    public void setAssignedRoles(Set<Role> pRoles)
    {
        this.clearAssignedRoles();
        for (Role pRole : pRoles)
        {
            this.assignRole(pRole);
        }
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
    }

    @Override
    public void clearPermissions()
    {
        this.makeDirty();
        this.config.perms.getPermissions().clear();
    }

    @Override
    public void setPermissions(Map<String, Boolean> perms)
    {
        this.clearPermissions();
        for (Entry<String, Boolean> entry : perms.entrySet())
        {
            if (entry.getValue() == null)
            {
                continue;
            }
            this.setPermission(entry.getKey(),entry.getValue());
        }
    }

    @Override
    public void setMetadata(String key, String value)
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
    }

    @Override
    public boolean assignRole(Role role)
    {
        if (this.inheritsFrom(role))
        {
            throw new CircularRoleDependencyException("Cannot add parentrole!");
        }
        String roleName = role.getName();
        if (role.isGlobal())
        {
            roleName = "g:" + roleName;
        }
        boolean added = this.config.parents.add(roleName);
        if (added)
        {
            this.makeDirty();
        }
        return added;
    }

    @Override
    public boolean removeRole(Role role)
    {
        String roleName = role.getName();
        if (role.isGlobal())
        {
            roleName = "g:" + roleName;
        }
        if (this.config.parents.remove(roleName))
        {
            this.makeDirty();
            return true;
        }
        return false;
    }

    @Override
    public void clearMetadata()
    {
        this.makeDirty();
        this.config.metadata.clear();
    }

    @Override
    public void clearAssignedRoles()
    {
        this.makeDirty();
        this.config.parents.clear();
    }

    @Override
    public void setMetadata(Map<String,String> data)
    {
        this.clearMetadata();
        for (Entry<String, String> entry : data.entrySet())
        {
            this.setMetadata(entry.getKey(),entry.getValue());
        }
    }

    public void deleteRole()
    {
        this.makeDirty();
        this.config.getFile().delete();
        this.resolvedData.performDeleteRole();
    }

    public boolean isDefaultRole()
    {
        return this.isDefaultRole;
    }

    /**
     * Sets the whether this role should be a default role
     *
     * @param set
     * @return false if this role is a global role or already set
     */
    public boolean setDefaultRole(boolean set)
    {
        if (!this.isGlobal())
        {
            if (this.isDefaultRole != set)
            {
                this.isDefaultRole = set;
                ((WorldRoleProvider)this.roleProvider).setDefaultRole(this, set);
                return true;
            }
        }
        return false;
    }

    @Override
    public long getWorldID()
    {
        return this.roleProvider.mainWorldId;
    }

    @Override
    public Set<Role> getAssignedRoles()
    {
        return Collections.unmodifiableSet(this.resolvedData.assignedRoles);
    }

    public boolean canAssignAndRemove(Permissible permissible)
    {
        return this.rolePermission.isAuthorized(permissible);
    }

    public Map<String, ResolvedPermission> getPermissions()
    {
        return Collections.unmodifiableMap(this.resolvedData.permissions);
    }

    public Map<String, ResolvedMetadata> getMetadata()
    {
        return Collections.unmodifiableMap(this.resolvedData.metadata);
    }

    @Override
    public Map<String, Boolean> getAllRawPermissions()
    {
        Map<String,Boolean> result = new THashMap<String, Boolean>();
        for (Role assignedRole : this.resolvedData.assignedRoles)
        {
            result.putAll(assignedRole.getAllRawPermissions());
        }
        result.putAll(this.getRawPermissions());
        return result;
    }

    @Override
    public Map<String, String> getAllRawMetadata()
    {
        Map<String,String> result = new THashMap<String, String>();
        for (Role assignedRole : this.resolvedData.assignedRoles)
        {
            result.putAll(assignedRole.getAllRawMetadata());
        }
        result.putAll(this.getRawMetadata());
        return result;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Role role = (Role)o;

        if (getName() != null ? !getName().equals(role.getName()) : role.getName() != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return getName() != null ? getName().hashCode() : 0;
    }

    @Override
    public int compareTo(Role o)
    {
        return this.getPriorityValue() - o.getPriorityValue();
    }
}
