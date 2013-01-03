package de.cubeisland.cubeengine.conomy.commands;

import de.cubeisland.cubeengine.conomy.Conomy;
import de.cubeisland.cubeengine.conomy.account.Account;
import de.cubeisland.cubeengine.conomy.currency.Currency;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.annotation.Alias;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.user.User;
import java.util.Collection;

public class MoneyCommand extends ContainerCommand
{
    //TODO multicurrencies
    private Conomy module;

    public MoneyCommand(Conomy module)
    {
        super(module, "money", "Manages your money.");
        this.module = module;
    }
//TODO different main currencies in different worlds?

    @Alias(names = "money")
    @Command(desc = "Shows your balance",
             usage = "[player] [in <currency>]", flags =
    {
        @Flag(longName = "all", name = "a")
    })
    public void balance(CommandContext context)
    {
        User user;
        if (context.hasIndexed(0))
        {
            user = context.getUser(0);
            if (user == null)
            {
                context.sendMessage("conomy", "&cUser %s not found!", context.getString(0));
                return;
            }
        }
        else
        {
            user = context.getSenderAsUser("conomy", "&cYou are out of money! Better go work than typing silly commands in the console.");
        }
        if (context.hasFlag("a"))
        {
            Collection<Account> accs = this.module.getAccountsManager().getAccounts(user);
            for (Account acc : accs)
            {
                context.sendMessage("conomey", "&a%s-Balance: &6%s", acc.currency.getName(), acc.currency.formatLong(acc.balance()));
            }
        }
        else
        {
            Account acc;
            if (context.hasNamed("in"))
            {
                Currency currency = this.module.getCurrencyManager().getCurrencyByName(context.getString("in"));
                if (currency == null)
                {
                    context.sendMessage("conomy", "&cCurrency %s not found!", context.getString("in"));
                    return;
                }
                acc = this.module.getAccountsManager().getAccount(user, currency);
            }
            else
            {
                acc = this.module.getAccountsManager().getAccount(user);
            }
            context.sendMessage("conomey", "&aBalance: &6%s", acc.currency.formatLong(acc.balance()));
        }
    }

    @Alias(names =
    {
        "toplist", "balancetop"
    })
    @Command(desc = "Shows the players with the highest balance.", usage = "[[fromRank]-ToRank]")
    // money top 5 shows rank 0 to 5
    // money top 50-60 shows rank 50 to 60
    public void top(CommandContext context)
    {
    }

    //TODO flag for banks
    //TODO named .as <name>
    @Alias(names =
    {
        "toplist", "balancetop"
    })
    @Command(desc = "Transfer the given amount to another account.", usage = "<player> <amount> [as <player>] [-bank]")
    //-bank flag is to pay TO a bank instead of a player
    public void pay(CommandContext context)
    {
    }
}
