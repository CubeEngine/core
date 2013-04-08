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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;

import de.cubeisland.cubeengine.roles.config.PermissionTree;
import de.cubeisland.cubeengine.roles.config.Priority;

public abstract class Role implements Comparable<Role>
{
    protected String name;
    protected Priority priority;
    protected Map<String, RolePermission> perms;
    protected Map<String, Boolean> litaralPerms;
    protected TreeSet<ConfigRole> parentRoles;
    protected Set<ConfigRole> childRoles = new HashSet<ConfigRole>();
    protected Map<String, RoleMetaData> metaData;
    protected boolean isGlobal;
    private boolean dirty;

    public Role()
    {
        this.perms = new HashMap<String, RolePermission>();
        this.metaData = new HashMap<String, RoleMetaData>();
        this.parentRoles = new TreeSet<ConfigRole>();
        this.litaralPerms = new HashMap<String, Boolean>();
    }

    public Role(String name, Priority priority, PermissionTree permTree, TreeSet<ConfigRole> parentRoles, Map<String, String> metaData, boolean isGlobal)
    {
        this.name = name;
        this.priority = priority;
        this.perms = new HashMap<String, RolePermission>();
        this.litaralPerms = new HashMap<String, Boolean>();
        for (Entry<String, Boolean> entry : permTree.getPermissions().entrySet())
        {
            if (entry.getKey().endsWith("*"))
            {
                Map<String, Boolean> subperms = new HashMap<String, Boolean>();
                this.resolveBukkitPermission(entry.getKey(), entry.getValue(), subperms);
                for (Entry<String, Boolean> subEntry : subperms.entrySet())
                {
                    this.perms.put(subEntry.getKey(), new RolePermission(subEntry.getKey(), subEntry.getValue() == entry.getValue(), this));
                }
            }
            this.perms.put(entry.getKey(), new RolePermission(entry.getKey(), entry.getValue(), this));
            this.litaralPerms.put(entry.getKey(), entry.getValue());
        }
        this.isGlobal = isGlobal;
        this.metaData = new HashMap<String, RoleMetaData>();
        if (metaData != null)
        {
            for (Entry<String, String> entry : metaData.entrySet())
            {
                this.metaData.put(entry.getKey(), new RoleMetaData(entry.getKey(), entry.getValue(), this));
            }
        }
        if (parentRoles == null)
        {
            this.parentRoles = new TreeSet<ConfigRole>();
        }
        else
        {
            this.parentRoles = parentRoles;
        }
    }

    public String getName()
    {
        return name;
    }

    public Priority getPriority()
    {
        return priority;
    }

    public Set<ConfigRole> getParentRoles()
    {
        return parentRoles;
    }

    public Map<String, RolePermission> getPerms()
    {
        return perms;
    }

    public Map<String, RoleMetaData> getMetaData()
    {
        return metaData;
    }

    public boolean isGlobal()
    {
        return isGlobal;
    }

    public void setParentRoles(TreeSet<ConfigRole> parentRoles)
    {
        this.parentRoles = parentRoles;
    }

    public void applyInheritence(Role parent)
    {
        if (this.parentRoles == null)
        {
            this.parentRoles = new TreeSet<ConfigRole>();
        }
        // Inherit missing permissions:
        Map<String, RolePermission> parentPerms = parent.getPerms();
        for (String permKey : parentPerms.keySet())
        {
            if (!this.perms.containsKey(permKey))
            {
                this.perms.put(permKey, parentPerms.get(permKey));
            }
        }
        // Inherit missing metaData:
        for (Entry<String, RoleMetaData> data : parent.getMetaData().entrySet())
        {
            if (!metaData.containsKey(data.getKey()))
            {
                metaData.put(data.getKey(), data.getValue());
            }
        }
        if (parent instanceof MergedRole)
        {
            this.parentRoles.addAll(((MergedRole)parent).getMergedWith());
        }
        else
        {
            this.parentRoles.add((ConfigRole)parent);
        }
    }

    public Map<String, Boolean> resolvePermissions()
    {
        Map<String, RolePermission> tempPerm = new HashMap<String, RolePermission>();
        for (RolePermission perm : this.perms.values())
        {
            if (tempPerm.containsKey(perm.getPerm()))
            {
                if (tempPerm.get(perm.getPerm()).getPriorityValue() >= perm.getPriorityValue())
                {
                    continue;
                }
            }
            tempPerm.put(perm.getPerm(), perm);
        }
        Map<String, Boolean> result = new HashMap<String, Boolean>();
        for (RolePermission perm : tempPerm.values())
        {
            result.put(perm.getPerm(), perm.isSet());
        }
        return result;
    }

    protected void resolveBukkitPermission(String name, boolean set, Map<String, Boolean> resolvedPermissions)
    {
        Permission bukkitPerm = Bukkit.getPluginManager().getPermission(name);
        if (bukkitPerm == null)
        {
            if (name.endsWith(".*"))
            {
                // manually search for child-perms...
                String baseName = name.substring(0, name.indexOf(".*"));
                for (Permission permission : Bukkit.getPluginManager().getPermissions())
                {
                    if (permission.getName().startsWith(baseName))
                    {
                        resolvedPermissions.put(permission.getName(), set);
                    }
                }
                resolvedPermissions.put(name, set);
            }
            else
            {
               System.out.print(name + " is not a registered bukkitperm!");//TODO remove
            }
            return;
        }
        Map<String, Boolean> childPerm = bukkitPerm.getChildren();
        for (String permKey : childPerm.keySet())
        {
            this.resolveBukkitPermission(permKey, set, resolvedPermissions);
            resolvedPermissions.put(permKey, set && childPerm.get(permKey));
        }
    }

    public Map<String, Boolean> getLitaralPerms()
    {
        return litaralPerms;
    }

    public Map<String, Boolean> getAllLiteralPerms()
    {
        Map<String, Boolean> result = new HashMap<String, Boolean>();
        for (Role role : this.parentRoles)
        {
            for (Entry<String, Boolean> entry : role.getAllLiteralPerms().entrySet())
            {
                if (entry.getKey().endsWith("*"))
                {
                    ArrayList<String> toRem = new ArrayList<String>();
                    for (String perm : result.keySet())
                    {
                        if (perm.startsWith(entry.getKey().substring(0, entry.getKey().length() - 1)))
                        {
                            toRem.add(perm);
                        }
                    }
                    for (String perm : toRem)
                    {
                        result.remove(perm);
                    }
                }
                result.put(entry.getKey(), entry.getValue());
            }
        }
        for (Entry<String, Boolean> entry : this.litaralPerms.entrySet())
        {
            if (entry.getKey().endsWith("*"))
            {
                ArrayList<String> toRem = new ArrayList<String>();
                for (String perm : result.keySet())
                {
                    if (perm.startsWith(entry.getKey().substring(0, entry.getKey().length() - 1)))
                    {
                        toRem.add(perm);
                    }
                }
                for (String perm : toRem)
                {
                    result.remove(perm);
                }
            }
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public void addChild(ConfigRole role)
    {
        this.childRoles.add(role);
    }

    public boolean isDirty()
    {
        return this.dirty;
    }

    public void makeDirty()
    {
        this.dirty = true;
    }

    public Set<ConfigRole> getChildRoles()
    {
        return childRoles;
    }

    public abstract void setPermission(String perm, Boolean set);

    public abstract void setMetaData(String key, String value);

    public abstract void clearMetaData();

    public abstract boolean setParentRole(String pRole);

    public abstract boolean removeParentRole(String pRole);

    public abstract void clearParentRoles();

    public abstract void setPriority(Priority priority);

    public abstract void rename(String newName);

    public void setChildRoles(Set<ConfigRole> childRoles)
    {
        this.childRoles = childRoles;
    }

    @Override
    public int compareTo(Role o)
    {
        return this.priority.value - o.priority.value;
    }

    public boolean inheritsFrom(Role other)
    {
        if (this.getParentRoles().contains(other))
        {
            return true;
        }
        for (Role role : this.getParentRoles())
            {
            if (role.inheritsFrom(other))
            {
                return true;
            }
        }
        return false;
    }
}
