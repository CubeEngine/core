package de.cubeisland.cubeengine.conomy.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.annotation.Alias;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.module.Module;

public class MoneyCommand extends ContainerCommand
{
    //TODO multicurrencies
    public MoneyCommand(Module module)
    {
        super(module, "money", "Manages your money.");
    }

    @Alias(names = "money")
    @Command(desc = "Shows your balance", usage = "[player]")
    public void balance(CommandContext context)
    {}

    @Alias(names = {
        "toplist", "balancetop"
    })
    @Command(desc = "Shows the players with the highest balance.", usage = "<page>")
    public void top(CommandContext context)
    {}

    //TODO flag for banks
    //TODO named .as <name>
    @Alias(names = {
        "toplist", "balancetop"
    })
    @Command(desc = "Transfer the given amount to another account.", usage = "<player> <amount> [as <player>]|[asBank <bank>] [-bank]")
    public void pay(CommandContext context)
    {}
}
