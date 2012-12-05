package de.cubeisland.cubeengine.roles.exception;

public class CircularRoleDepedencyException extends Exception
{
    public CircularRoleDepedencyException(String string)
    {
        super(string);
    }
}
