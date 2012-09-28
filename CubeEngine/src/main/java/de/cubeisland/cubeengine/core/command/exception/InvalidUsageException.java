package de.cubeisland.cubeengine.core.command.exception;

import de.cubeisland.cubeengine.core.command.CommandContext;
import static de.cubeisland.cubeengine.core.i18n.I18n._;
import org.bukkit.command.CommandSender;

/**
 *
 * @author CodeInfection
 */
public class InvalidUsageException extends CommandException
{
    private InvalidUsageException(String message)
    {
        super(message);
    }
    
    public static void invalidUsage(CommandContext context, String category, String message, Object... params)
    {
        invalidUsage(context.getSender(), category, message, params);
    }
    
    public static void invalidUsage(CommandSender sender, String category, String message, Object... params)
    {
        throw new InvalidUsageException(_(sender, category, message, params));
    }
}
