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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class TempDataStore implements DataStore
{
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
    public PermissionValue setTempPermission(String perm, PermissionValue set)
    {
        this.makeDirty();
        if (set == PermissionValue.RESET)
        {
            return PermissionValue.of(this.tempPermissions.remove(perm));
        }
        else
        {
            return PermissionValue.of(this.tempPermissions.put(perm, set == PermissionValue.TRUE));
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

    public void resetTempData()
    {
        this.clearTempRoles();
        this.clearTempPermissions();
        this.clearTempMetadata();
    }
}
