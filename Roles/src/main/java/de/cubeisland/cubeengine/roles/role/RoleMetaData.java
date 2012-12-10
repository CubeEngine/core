package de.cubeisland.cubeengine.roles.role;

public class RoleMetaData
{
    private String key;
    private String value;
    private Role origin;

    public RoleMetaData(String key, String value, Role origin)
    {
        this.key = key;
        this.value = value;
        this.origin = origin;
    }

    public Role getOrigin()
    {
        return origin;
    }

    public String getKey()
    {
        return key;
    }

    public String getValue()
    {
        return value;
    }
    
    public int getPriorityValue()
    {
        return this.origin.priority.value;
    }
    
}
