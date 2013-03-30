package de.cubeisland.cubeengine.roles.role;

public class RolePermission
{
    private String perm;
    private boolean isSet;
    private final Role origin;

    public RolePermission(String perm, boolean isSet, Role origin)
    {
        this.perm = perm;
        this.isSet = isSet;
        this.origin = origin;
    }

    public String getPerm()
    {
        return perm;
    }

    public boolean isSet()
    {
        return isSet;
    }

    public int getPriorityValue()
    {
        return this.origin.priority.value;
    }

    public Role getOrigin()
    {
        return origin;
    }
}
