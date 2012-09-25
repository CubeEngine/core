package de.cubeisland.cubeengine.core.command.exception;

import static de.cubeisland.cubeengine.core.i18n.I18n._;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Phillip
 */
public class IllegalParameterValue extends CommandException
{
    private IllegalParameterValue(String message)
    {
        super(message);
    }

    public static void illegalParameter(CommandSender sender, String category, String message, Object... params)
    {
        throw new IllegalParameterValue(_(sender, category, message, params));
    }
}