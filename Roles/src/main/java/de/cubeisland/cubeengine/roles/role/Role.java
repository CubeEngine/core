package de.cubeisland.cubeengine.roles.role;

import de.cubeisland.cubeengine.roles.role.config.PermissionTree;
import de.cubeisland.cubeengine.roles.role.config.Priority;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;

public abstract class Role
{

    protected String name;
    protected Priority priority;
    protected Map<String, RolePermission> perms;
    protected List<Role> parentRoles;
    protected Map<String, String> metaData;
    protected boolean isGlobal;

    public Role()
    {
        this.perms = new HashMap<String, RolePermission>();
        this.metaData = new HashMap<String, String>();
        this.parentRoles = new ArrayList<Role>();
    }

    public Role(String name, Priority priority, PermissionTree permTree, List<Role> parentRoles, Map<String, String> metaData, boolean isGlobal)
    {
        this.name = name;
        this.priority = priority;
        this.perms = new HashMap<String, RolePermission>();
        for (Entry<String, Boolean> entry : permTree.getPermissions().entrySet())
        {
            if (entry.getKey().endsWith("*"))
            {
                Map<String, Boolean> subperms = new HashMap<String, Boolean>();
                this.resolveBukkitPermission(entry.getKey(), subperms);
                for (Entry<String, Boolean> subEntry : subperms.entrySet())
                {
                    this.perms.put(entry.getKey(), new RolePermission(subEntry.getKey(), subEntry.getValue(), this));
                }
            }
            this.perms.put(entry.getKey(), new RolePermission(entry.getKey(), entry.getValue(), this));
        }
        this.isGlobal = isGlobal;
        if (metaData == null)
        {
            this.metaData = new HashMap<String, String>();
        }
        else
        {
            this.metaData = metaData;
        }
        if (parentRoles == null)
        {
            this.parentRoles = new ArrayList<Role>();
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

    public List<Role> getParentRoles()
    {
        return parentRoles;
    }

    public Map<String, RolePermission> getPerms()
    {
        return perms;
    }

    public Map<String, String> getMetaData()
    {
        return metaData;
    }

    public boolean isGlobal()
    {
        return isGlobal;
    }

    public void setParentRoles(List<Role> parentRoles)
    {
        this.parentRoles = parentRoles;
    }

    public void applyInheritence(Role parent)
    {
        if (this.parentRoles == null)
        {
            this.parentRoles = new ArrayList<Role>();
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
        for (Entry<String, String> data : parent.getMetaData().entrySet())
        {
            if (!metaData.containsKey(data.getKey()))
            {
                metaData.put(data.getKey(), data.getValue());
            }
        }
        if (parent instanceof MergedRole)
        {
            this.parentRoles.addAll(((MergedRole) parent).getMergedWith());
        }
        else
        {
            this.parentRoles.add(parent);
        }
    }

    public void setMetaData(String key, String value)
    {
        this.metaData.put(key, value);
    }

    public Map<String, Boolean> resolvePermissions()
    {
        Map<String, RolePermission> tempPerm = new HashMap<String, RolePermission>();
        for (RolePermission perm : this.perms.values())
        {
            /* TODO check if this already works
            if (perm.getPerm().endsWith("*"))
            {
                Map<String, Boolean> childPerm = new HashMap<String, Boolean>();
                this.resolveBukkitPermission(perm.getPerm(), childPerm);
                for (String permKey : childPerm.keySet())
                {
                    if (tempPerm.containsKey(permKey))
                    {
                        if (tempPerm.get(permKey).getPriorityValue() >= perm.getPriorityValue())
                        {
                            continue;
                        }
                    }
                    tempPerm.put(permKey,
                            new RolePermission(permKey,
                            ((perm.isSet() && childPerm.get(permKey)) || (!perm.isSet() && !childPerm.get(permKey))),
                            perm.getOrigin()));
                }
            }*/
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

    private void resolveBukkitPermission(String name, Map<String, Boolean> childs)
    {
        Map<String, Boolean> childPerm = Bukkit.getPluginManager().getPermission(name).getChildren();
        for (String permKey : childPerm.keySet())
        {
            if (permKey.endsWith("*"))
            {
                this.resolveBukkitPermission(permKey, childs);
            }
            childs.put(permKey, childPerm.get(permKey));
        }
    }
}
