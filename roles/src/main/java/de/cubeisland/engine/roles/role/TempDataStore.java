package de.cubeisland.engine.roles.role;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class TempDataStore implements DataStore
{
    // Temporary:

    protected Map<String, Boolean> tempPermissions = new HashMap<>();
    protected Map<String, String> tempMetadata = new HashMap<>();
    protected Set<String> tempRoles = new HashSet<>();

    public Map<String, Boolean> getRawTempPermissions()
    {
        return Collections.unmodifiableMap(tempPermissions);
    }

    public Map<String, String> getRawTempMetaData()
    {
        return Collections.unmodifiableMap(tempMetadata);
    }

    public Set<String> getRawTempRoles()
    {
        return Collections.unmodifiableSet(tempRoles);
    }

    @Override
    public PermissionType setTempPermission(String perm, PermissionType set)
    {
        this.makeDirty();
        if (set == PermissionType.NOT_SET)
        {
            return PermissionType.of(this.tempPermissions.remove(perm));
        }
        else
        {
            return PermissionType.of(this.tempPermissions.put(perm, set == PermissionType.TRUE));
        }
    }

    @Override
    public String setTempMetadata(String key, String value)
    {
        this.makeDirty();
        return this.tempMetadata.put(key, value);
    }

    @Override
    public boolean removeTempMetadata(String key)
    {
        this.makeDirty();
        return this.tempMetadata.remove(key) != null;
    }

    @Override
    public boolean assignTempRole(Role role)
    {
        this.makeDirty();
        return this.tempRoles.add(role.getName());
    }

    @Override
    public boolean removeTempRole(Role role)
    {
        this.makeDirty();
        return this.tempRoles.remove(role.getName());
    }

    @Override
    public void clearTempPermissions()
    {
        this.makeDirty();
        this.tempPermissions = new HashMap<>();
    }

    @Override
    public void clearTempMetadata()
    {
        this.makeDirty();
        this.tempMetadata = new HashMap<>();
    }

    @Override
    public void clearTempRoles()
    {
        this.makeDirty();
        this.tempRoles = new HashSet<>();
    }
}
