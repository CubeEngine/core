package de.cubeisland.cubeengine.core.command.result.confirm;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandManager;
import de.cubeisland.cubeengine.core.command.CommandResult;
import de.cubeisland.cubeengine.core.command.ContextFactory;
import de.cubeisland.cubeengine.core.command.CubeCommand;
import de.cubeisland.cubeengine.core.command.HelpContext;
import de.cubeisland.cubeengine.core.module.Module;

public class ConfirmCommand extends CubeCommand
{
    private final CommandManager commandManager;

    public ConfirmCommand(Module module, ContextFactory contextFactory, CommandManager commandManager)
    {
        super(module, "confirm", "Confirm a command", contextFactory);
        this.commandManager = commandManager;
    }

    @Override
    public CommandResult run(CommandContext context) throws Exception
    {
        if (!commandManager.hasPendingConfirmation(context.getSender()))
        {
            context.sendTranslated("You don't have any pending confirmations!");
        }
        commandManager.getPendingConfirmation(context.getSender()).run();
        return null;
    }

    @Override
    public void help(HelpContext context) throws Exception
    {
        context.sendTranslated("Usage: %s", this.getUsage(context));
    }
}
