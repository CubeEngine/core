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
package de.cubeisland.engine.roles.config;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import de.cubeisland.engine.roles.role.DataStore.PermissionValue;

public class PermissionTree
{
    private final Map<String, Boolean> permissions = new TreeMap<>();

    private void loadFromMap(Map<String, ?> map, String path)
    {
        for (String key : map.keySet())
        {
            Object mapValue = map.get(key);
            if (mapValue instanceof List)
            {
                this.loadFromList((List)mapValue, path.isEmpty() ? key : (path + "." + key));
            }
            else
            {
                throw new IllegalArgumentException("Invalid PermissionTree!");
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void loadFromList(List<?> list, String path)
    {
        for (Object value : list)
        {
            if (value instanceof String)
            {
                String permissionString = (String)value;
                boolean isSet = true;
                if (permissionString.startsWith("!") || permissionString.startsWith("^") || permissionString.startsWith("-"))
                {
                    permissionString = permissionString.substring(1);
                    isSet = false;
                }
                if (!path.isEmpty())
                {
                    permissionString = path + "." + permissionString;
                }
                this.addPermission(permissionString, isSet);
            }
            else if (value instanceof Map)
            {
                this.loadFromMap((Map<String, Object>)value, path);
            }
            else
            {
                throw new IllegalArgumentException("Invalid PermissionTree!");
            }
        }
    }

    protected void addPermission(String permission, boolean setTrue)
    {
        permission = permission.toLowerCase(Locale.ENGLISH);
        this.permissions.put(permission, setTrue);
    }

    public Map<String, Boolean> getPermissions()
    {
        return this.permissions;
    }

    public PermissionValue setPermission(String perm, PermissionValue set)
    {
        if (set == PermissionValue.RESET)
        {
            return PermissionValue.of(this.permissions.remove(perm));
        }
        return PermissionValue.of(this.permissions.put(perm, set == PermissionValue.TRUE));
    }
}
