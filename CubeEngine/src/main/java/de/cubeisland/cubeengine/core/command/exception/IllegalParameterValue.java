package de.cubeisland.cubeengine.core.command.exception;

import de.cubeisland.cubeengine.core.command.CommandContext;
import org.bukkit.command.CommandSender;

import static de.cubeisland.cubeengine.core.i18n.I18n._;

/**
 * This exception is thrown when a user performed a command with invalid parameters.
 * Use illegalParameter to throw an exception insinde a command. The exception will be caught.
 */
public class IllegalParameterValue extends InvalidUsageException
{   
    private IllegalParameterValue(String message)
    {
        super(message);
    }

    public static void illegalParameter(CommandContext context, String category, String message, Object... params)
    {
        illegalParameter(context.getSender(), category, message, params);
    }

    public static void illegalParameter(CommandSender sender, String category, String message, Object... params)
    {
        throw new IllegalParameterValue(_(sender, category, message, params));
    }
}