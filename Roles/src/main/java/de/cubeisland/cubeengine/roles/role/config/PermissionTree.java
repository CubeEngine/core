package de.cubeisland.cubeengine.roles.role.config;

import java.util.LinkedHashMap;
import java.util.LinkedList;
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

    public Object convertToConfigObject()
    {
        Map<String, Object> splitValues = new LinkedHashMap<String, Object>();
        //Fill result map with split values
        for (String permission : this.permissions.keySet())
        {
            this.putInMap(splitValues, permission, this.permissions.get(permission));
        }
        return mergeSplitValues(splitValues);
    }

    @SuppressWarnings("unchecked")
    private List<Object> mergeSplitValues(Map<String, Object> baseMap)
    {
        List<Object> result = new LinkedList<Object>();
        for (String baseKey : baseMap.keySet())
        {
            Object baseValue = baseMap.get(baseKey);
            if (baseKey.startsWith(" "))
            {
                result.add(((Boolean)baseValue ? "" : "-") + baseKey.trim());
            }
            else
            {
                Map<String, Object> baseValueMap = (Map<String, Object>)baseValue;
                List<Object> values = this.mergeSplitValues(baseValueMap);
                Map<String, Object> subMap = new LinkedHashMap<String, Object>();

                int size = baseValueMap.size();
                if (size == 1)
                {
                    if (values.size() == 1)
                    {
                        for (Object subValue : values)
                        {
                            if (subValue instanceof String)
                            {
                                if (((String)subValue).startsWith("-"))
                                {
                                    result.add("-" + baseKey + "." + ((String)subValue).substring(1));
                                }
                                else
                                {
                                    result.add(baseKey + "." + subValue);
                                }
                            }
                            else
                            {
                                Object subKey = ((Map)subValue).keySet().iterator().next();
                                subMap.put(baseKey + "." + subKey, ((Map)subValue).get(subKey));
                                result.add(subMap);
                            }
                        }
                    }
                    else
                    // multiple values add as subMap
                    {
                        subMap.put(baseKey, values);
                        result.add(subMap);
                    }
                }
                else
                {
                    subMap.put(baseKey, values);
                    result.add(subMap);
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> putInMap(Map<String, Object> map, String path, boolean value)
    {
        String base = this.getBasePath(path);
        if (base.isEmpty())
        {
            map.put(" " + path, value);
            return map;
        }
        Map<String, Object> subMap = (Map<String, Object>)map.get(base); // this should never give an exception if it does something went wrong!
        if (subMap == null) // sub map not yet existant?
        {
            subMap = new LinkedHashMap<String, Object>();
            map.put(base, subMap);
        }
        return this.putInMap(subMap, this.getSubPath(path, base), value); // create map for subPath
    }

    private String getBasePath(String path)
    {
        if (path.contains("."))
        {
            return path.substring(0, path.indexOf("."));
        }
        return "";
    }

    private String getSubPath(String path, String basePath)
    {
        if (path.contains(basePath))
        {
            return path.substring(path.indexOf(basePath) + basePath.length() + 1);
        }
        return null;
    }

    public Map<String, Boolean> getPermissions()
    {
        return this.permissions;
    }
}
