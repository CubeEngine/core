package de.cubeisland.cubeengine.roles.role;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A single role a User can have on the Server.
 */
public class Role
{
    private Priority priority;
    private String name;
    private List<Role> parentRoles;
    private Map<String, Boolean> permissions;
    private Map<String, String> metaData;

    public Role(RoleConfig config)
    {
        this.name = config.roleName;
        this.priority = config.priority;
        this.parentRoles = new ArrayList<Role>();
        this.permissions = config.perms.getPermissions();
        this.metaData = config.metadata;
    }

    public Role()
    {
        this.permissions = new LinkedHashMap<String, Boolean>();
        this.metaData = new LinkedHashMap<String, String>();
    }

    /**
     * @return the priority
     */
    public Priority getPriority()
    {
        return priority;
    }

    /**
     * @param priority the priority to set
     */
    public void setPriority(Priority priority)
    {
        this.priority = priority;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the parentRoles
     */
    public List<Role> getParentRoles()
    {
        return parentRoles;
    }

    /**
     * @param parentRoles the parentRoles to set
     */
    public void setParentRoles(List<Role> parentRoles)
    {
        this.parentRoles = parentRoles;
    }

    /**
     * @return the metaData
     */
    public Map<String, String> getMetaData()
    {
        return metaData;
    }

    /**
     * @param metaData the metaData to set
     */
    public void setMetaData(Map<String, String> metaData)
    {
        this.setMetaData(metaData);
    }

    /**
     * @return the permissions
     */
    public Map<String, Boolean> getPermissions()
    {
        return permissions;
    }

    /**
     * @param permissions the permissions to set
     */
    public void setPermissions(Map<String, Boolean> permissions)
    {
        this.permissions = permissions;
    }

    public void setPermission(String permission, Boolean isSet)
    {
        this.permissions.put(permission, isSet);
    }

    public void setMetaData(String key, String data)
    {
        this.metaData.put(key, data);
    }
}
