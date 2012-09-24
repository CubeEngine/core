package de.cubeisland.cubeengine.core.command.exception;

import static de.cubeisland.cubeengine.core.i18n.I18n._;
import org.bukkit.command.CommandSender;

/**
 *
 * @author CodeInfection
 */
public class PermissionDeniedException extends CommandException
{
    private PermissionDeniedException(String message)
    {
        super(message);
    }
    
    public static void denyAccess(CommandSender sender, String category, String message, Object... params)
    {
        throw new PermissionDeniedException(_(sender, category, message, params));
    }
}
