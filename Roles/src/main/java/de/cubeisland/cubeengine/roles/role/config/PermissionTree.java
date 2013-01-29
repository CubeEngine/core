package de.cubeisland.cubeengine.roles.role.config;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class PermissionTree
{
    private Map<String, Boolean> permissions = new TreeMap<String, Boolean>();

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
}
