package de.cubeisland.cubeengine.core.command.exception;

import org.bukkit.command.CommandSender;

/**
 *
 * @author CodeInfection
 */
public class PermissionDeniedException extends Exception
{
    private final CommandSender sender;
    
    public PermissionDeniedException(CommandSender sender, String message)
    {
        super(message);
        this.sender = sender;
    }
    
    public CommandSender getSender()
    {
        return this.sender;
    }
}
