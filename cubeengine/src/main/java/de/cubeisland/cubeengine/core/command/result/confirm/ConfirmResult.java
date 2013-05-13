package de.cubeisland.cubeengine.core.command.result.confirm;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandResult;
import de.cubeisland.cubeengine.core.command.CommandSender;
import de.cubeisland.cubeengine.core.module.Module;

/**
 * A result that should be confirmed via the /confirm command
 */
public class ConfirmResult implements CommandResult
{
    private final Runnable runnable;
    private final CommandSender sender;
    private final Module module;
    private String message = "";

    public ConfirmResult(Runnable runnable, CommandContext context)
    {
        this.runnable = runnable;
        this.sender = context.getSender();
        this.module = context.getCommand().getModule();
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    @Override
    public void show(CommandContext context)
    {
        context.getCore().getCommandManager().registerConfirmResult(this, this.module, sender);
        context.sendTranslated(message, context.getCommand().getName());
    }

    public void run()
    {
        this.runnable.run();
    }
}
