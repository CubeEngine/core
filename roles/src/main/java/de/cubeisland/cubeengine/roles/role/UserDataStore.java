package de.cubeisland.cubeengine.roles.role;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

public class UserDataStore implements RawDataStore
{
    protected Set<String> roles;
    protected Map<String,Boolean> permissions;
    protected Map<String,String> metadata;

    protected final RolesAttachment attachment;
    protected final long worldID;

    public UserDataStore(RolesAttachment attachment, long worldID)
    {
        this.attachment = attachment;
        this.worldID = worldID;
    }

    @Override
    public Map<String, Boolean> getRawPermissions()
    {
       return this.permissions;
    }

    @Override
    public Map<String, String> getRawMetadata()
    {
        return this.metadata;
    }

    @Override
    public Set<String> getRawAssignedRoles()
    {
       return this.roles;
    }

    @Override
    public Set<Role> getAssignedRoles()
    {
        return Collections.unmodifiableSet(this.attachment.getResolvedData(this.worldID).assignedRoles);
    }

    @Override
    public String getName()
    {
        return this.attachment.getHolder().getName();
    }

    @Override
    public void setPermission(String perm, Boolean set)
    {
        if (set == null)
        {
            this.permissions.remove(perm);
        }
        else
        {
            this.permissions.put(perm,set);
        }
        this.makeDirty();
    }

    @Override
    public void setMetadata(String key, String value)
    {
        if (value == null)
        {
            this.metadata.remove(key);
        }
        else
        {
            this.metadata.put(key,value);
        }
        this.makeDirty();
    }

    @Override
    public boolean assignRole(Role role)
    {
        String roleName = role.getName();
        if (role.isGlobal())
        {
            roleName = "g:" + roleName;
        }
        if (this.roles.add(roleName))
        {
            this.makeDirty();
            return true;
        }
        return false;
    }

    @Override
    public boolean removeRole(Role role)
    {
        String roleName = role.getName();
        if (role.isGlobal())
        {
            roleName = "g:" + roleName;
        }
        if (this.roles.remove(roleName))
        {
            this.makeDirty();
            return true;
        }
        return false;
    }

    @Override
    public void clearPermissions()
    {
      this.permissions = new THashMap<String, Boolean>();
        this.makeDirty();
    }

    @Override
    public void clearMetadata()
    {
        this.metadata = new THashMap<String, String>();
        this.makeDirty();
    }

    @Override
    public void clearAssignedRoles()
    {
        this.roles = new THashSet<String>();
        this.makeDirty();
    }

    @Override
    public void setPermissions(Map<String, Boolean> perms)
    {
       this.permissions = new THashMap<String, Boolean>(perms);
        this.makeDirty();
    }

    @Override
    public void setMetadata(Map<String, String> metadata)
    {
        this.metadata = new THashMap<String, String>(metadata);
        this.makeDirty();
    }

    @Override
    public void setAssignedRoles(Set<Role> roles)
    {
        this.clearAssignedRoles();
        for (Role role : roles)
        {
            this.roles.add(role.getName());
        }
        this.makeDirty();
    }

    @Override
    public long getWorldID()
    {
        return this.worldID;
    }

    public Long getUserID()
    {
        return this.attachment.getHolder().key;
    }

    @Override
    public Map<String, Boolean> getAllRawPermissions()
    {
        return this.getRawPermissions();
    }

    @Override
    public Map<String, String> getAllRawMetadata()
    {
       return this.getRawMetadata();
    }

    protected void makeDirty()
    {
        this.attachment.makeDirty(worldID);
    }
}
