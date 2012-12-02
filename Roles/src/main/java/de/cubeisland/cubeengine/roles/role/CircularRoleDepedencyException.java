package de.cubeisland.cubeengine.roles.role;

public class CircularRoleDepedencyException extends Exception
{
    public CircularRoleDepedencyException(String string)
    {
        super(string);
    }
}
