package de.cubeisland.cubeengine.roles.role.newRole;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.cubeisland.cubeengine.roles.config.Priority;
import de.cubeisland.cubeengine.roles.config.RoleConfig;

public class Role
{
    protected RoleConfig config;
    protected ResolvedDataStore resolvedData;
    private long worldID;

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

    public void setParentRoles(Set<Role> pRoles)
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

    public void clearMetadata()
    {
        this.makeDirty();
        this.config.metadata.clear();
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

    // TODO rolePermission for assign / remove from user
    // TODO getParentRoles (actual objects)
    // TODO getPermissions (resolved)
    // TODO getMetadata (resolved)

    // TODO getAllPerms (unresolved)
    // TODO getAllMetadata (unresolved)

    // TODO setToDefaultRole (saved in another config)
}
