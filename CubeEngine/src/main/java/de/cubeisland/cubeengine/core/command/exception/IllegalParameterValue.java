package de.cubeisland.cubeengine.core.command.exception;

import static de.cubeisland.cubeengine.core.i18n.I18n._;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Phillip
 */
public class IllegalParameterValue extends CommandException
{
    public IllegalParameterValue(String string, int i, String paramName, Class type)
    {
        super(""); // TODO add a translatable error message
    }

    private IllegalParameterValue(String message)
    {
        super(message);
    }

    public static void illegalParameter(CommandSender sender, String category, String message, Object... params)
    {
        throw new IllegalParameterValue(_(sender, category, message, params));
    }
    
    public static void illegalParameterUser(CommandSender sender,Object... params)
    {
        throw new IllegalParameterValue(_(sender, "core", "&cThe User %s does not exist!", params));
    }
}
