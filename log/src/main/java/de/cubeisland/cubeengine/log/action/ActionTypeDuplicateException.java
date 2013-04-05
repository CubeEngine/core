package de.cubeisland.cubeengine.log.action;

public class ActionTypeDuplicateException extends RuntimeException
{
    public ActionTypeDuplicateException(ActionType type)
    {
        super(type.actionTypeID + ": "+ type.name);
    }
}
