package de.cubeisland.engine.roles.role;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class TempDataStore implements DataStore
{
    private Map<String, Boolean> tempPermissions = new HashMap<>();
    private Map<String, String> tempMetaData = new HashMap<>();
    private Set<String> tempRoles = new HashSet<>();

    public Map<String, Boolean> getRawTempPermissions()
    {
        return tempPermissions;
    }

    public Map<String, String> getRawTempMetaData()
    {
        return tempMetaData;
    }

    public Set<String> getRawTempRoles()
    {
        return tempRoles;
    }

    @Override
    public PermissionType setTempPermission(String perm, PermissionType set)
    {
        Boolean replaced;
        if (set == PermissionType.NOT_SET)
        {
            replaced = this.tempPermissions.remove(perm);
        }
        else
        {
            replaced = this.tempPermissions.put(perm, set == PermissionType.TRUE);
        }
        return PermissionType.of(replaced);
    }

    @Override
    public String setTempMetadata(String key, String value)
    {
        return this.tempMetaData.put(key, value);
    }

    @Override
    public boolean removeTempMetadata(String key)
    {
        return this.tempMetaData.remove(key) != null;
    }

    @Override
    public boolean assignTempRole(Role role)
    {
        return this.tempRoles.add(role.getName());
    }

    @Override
    public boolean removeTempRole(Role role)
    {
        return this.tempRoles.remove(role.getName());
    }

    @Override
    public void clearTempPermissions()
    {
        this.tempPermissions = new HashMap<>();
    }

    @Override
    public void clearTempMetadata()
    {
        this.tempMetaData = new HashMap<>();
    }

    @Override
    public void clearTempRoles()
    {
        this.tempRoles = new HashSet<>();
    }
}
