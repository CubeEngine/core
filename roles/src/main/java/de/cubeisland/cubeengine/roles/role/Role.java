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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.cubeisland.cubeengine.roles.config.Priority;
import de.cubeisland.cubeengine.roles.config.RoleConfig;

public class Role implements RawDataStore
{
    protected RoleConfig config;
    protected ResolvedDataStore resolvedData;
    private long worldID;
    protected boolean isDefaultRole = false;

    public Role(RoleConfig config, long worldID)
    {
        this.config = config;
        this.worldID = worldID;
    }

    public Map<String,Boolean> getRawPermissions()
    {
        return new HashMap<String, Boolean>(config.perms.getPermissions());
    }

    public Map<String,String> getRawMetadata()
    {
        return new HashMap<String, String>(config.metadata);
    }

    public Set<String> getRawParents()
    {
        return new HashSet<String>(this.config.parents);
    }

    public void saveToConfig()
    {
        this.config.save();
    }

    private void saveConfigToNewFile()
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

    private void makeDirty()
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
        return this.worldID == 0;
    }

    public boolean inheritsFrom(Role other)
    {
        return this.resolvedData.inheritsFrom(other);
    }

    public boolean rename(String newName)
    {
        if (false)// TODO check if a role exists for that name
        {
            return false;
        }
        this.makeDirty();
        this.resolvedData.performRename(newName);

        this.saveConfigToNewFile();
        return true;
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

    public boolean addParentRole(Role pRole)
    {
        // TODO check circular dependency!
        boolean added = this.config.parents.add(pRole.getName());
        if (added)
        {
            this.makeDirty();
            this.saveToConfig();
        }
        return added;
    }

    public boolean removeParentRole(Role pRole)
    {
        if (this.config.parents.remove(pRole.getName()))
        {
            this.makeDirty();
            this.saveToConfig();
            return true;
        }
        return false;
    }

    public void clearParentRoles()
    {
        this.makeDirty();
        this.config.parents.clear();
        this.saveToConfig();
    }

    public void setParents(Set<Role> pRoles)
    {
        this.clearParentRoles();
        for (Role pRole : pRoles)
        {
            this.addParentRole(pRole);
        }
    }

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

    public void clearPermissions()
    {
        this.makeDirty();
        this.config.perms.getPermissions().clear();
    }

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
    public boolean addParent(Role role)
    {
        return this.config.parents.add(role.getName());
    }

    @Override
    public boolean removeParent(Role role)
    {
        return this.config.parents.remove(role.getName());
    }

    public void clearMetadata()
    {
        this.makeDirty();
        this.config.metadata.clear();
    }

    @Override
    public void clearParents()
    {
        this.config.parents.clear();
    }

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

    protected void setName(String name)
    {
        this.config.roleName = name;
    }

    public boolean isDefaultRole()
    {
        return this.isDefaultRole;
    }

    @Override
    public long getWorldID()
    {
        return this.worldID;
    }

    // TODO rolePermission for assign / remove from user
    // TODO getParentRoles (actual objects)
    // TODO getPermissions (resolved)
    // TODO getMetadata (resolved)

    // TODO getAllPerms (unresolved)
    // TODO getAllMetadata (unresolved)

    // TODO isDefaultRole?
    // TODO setToDefaultRole (saved in another config)
}
