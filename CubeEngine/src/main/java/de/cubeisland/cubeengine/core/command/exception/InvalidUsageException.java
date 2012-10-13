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
    private final boolean showUsage;

    protected InvalidUsageException(String message, boolean showUsage)
    {
        super(message);
        this.showUsage = showUsage;
    }

    protected InvalidUsageException(String message)
    {
        this(message, true);
    }

    public boolean showUsage()
    {
        return this.showUsage;
    }

    public static void invalidSender(CommandSender sender, String category, String message, Object... params)
    {
        throw new InvalidUsageException(_(sender, category, message, params), false);
    }

    public static void invalidUsage(CommandContext context, String category, String message, Object... params)
    {
        invalidUsage(context.getSender(), category, message, params);
    }

    public static void invalidUsage(CommandContext context)
    {
        invalidUsage(context, "core", "Invalid Usage!");
    }

    public static void invalidUsage(CommandSender sender, String category, String message, Object... params)
    {
        throw new InvalidUsageException(_(sender, category, message, params));
    }
}
