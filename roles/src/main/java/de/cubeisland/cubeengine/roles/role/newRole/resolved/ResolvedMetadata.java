package de.cubeisland.cubeengine.roles.role.newRole.resolved;

public class ResolvedMetadata extends ResolvedData
{
    private String value;

    public ResolvedMetadata(de.cubeisland.cubeengine.roles.role.newRole.Role origin, String key, String value)
    {
        super(origin,key);
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }
}
