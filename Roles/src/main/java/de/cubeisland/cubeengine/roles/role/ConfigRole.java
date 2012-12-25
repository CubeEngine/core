package de.cubeisland.cubeengine.roles.role;

import de.cubeisland.cubeengine.roles.role.config.Priority;
import de.cubeisland.cubeengine.roles.role.config.RoleConfig;
import java.io.File;
import java.util.TreeSet;

public class ConfigRole extends Role
{
    private RoleConfig config;

    public ConfigRole(RoleConfig config, TreeSet<Role> parentRoles, boolean isGlobal)
    {
        super(config.roleName, config.priority, config.perms, parentRoles, config.metadata, isGlobal);
        this.applyInheritence(new MergedRole(parentRoles));
        this.config = config;
        for (Role role : parentRoles)
        {
            role.addChild(this);
        }
    }

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
}
