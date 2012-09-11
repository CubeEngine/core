package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.user.User;

/**
 *
 * @author CodeInfection
 */
public class PermissionDeniedException extends Exception
{
    private final User user;
    
    public PermissionDeniedException(User user, String message)
    {
        super(message);
        this.user = user;
    }
    
    public User getUser()
    {
        return this.user;
    }
}
