package de.cubeisland.cubeengine.conomy.commands;

import de.cubeisland.cubeengine.conomy.Conomy;
import de.cubeisland.cubeengine.conomy.account.Account;
import de.cubeisland.cubeengine.conomy.currency.Currency;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.user.User;

public class EcoCommands extends ContainerCommand
{
    private Conomy module;

    public EcoCommands(Conomy module)
    {
        super(module, "eco", "Administrative commands for Conomy.");
        this.module = module;
    }

    @Command(names =
    {
        "give", "grant"
    },
             desc = "Gives money to given user",
             usage = "<player> <amount> [in <currency>]",
             min = 2, max = 2)
    public void give(CommandContext context)
    {
        Currency currency;
        if (context.hasNamed("in"))
        {
            currency = this.module.getCurrencyManager().getCurrencyByName(context.getString("in"));
            if (currency == null)
            {
                context.sendMessage("conomy", "&cCurrency %s not found!", context.getString("in"));
                return;
            }
        }
        else
        {
            currency = this.module.getCurrencyManager().getMainCurrency(); //TODO choose currency / or match with formatting
        }
        String amountString = context.getString(1);
        Long amount = currency.parse(amountString);
        if (amount == null)
        {
            context.sendMessage("conomy", "&cCould not parse amount!");
            return;
        }
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendMessage("conomy", "&cUser %s not found!", context.getString(0));
            return;
        }
        Account target = this.module.getAccountsManager().getAccount(user, currency);
        if (target == null)
        {
            context.sendMessage("conomy", "&2%s &cdoes not have an account for &6%s&c!",
                    user.getName(), currency.getName());
            return;
        }
        if (this.module.getAccountsManager().transaction(null, target, amount, true))
        {
            context.sendMessage("conomy", "&aYou gave &6%s &ato &2%s&a!", currency.formatLong(amount), user.getName());
        }
    }

    @Command(names =
    {
        "take", "remove"
    },
             desc = "Takes money from given user",
             usage = "<player> <amount> [in <currency>]",
             min = 2, max = 2)
    public void take(CommandContext context)
    {
        Currency currency;
        if (context.hasNamed("in"))
        {
            currency = this.module.getCurrencyManager().getCurrencyByName(context.getString("in"));
            if (currency == null)
            {
                context.sendMessage("conomy", "&cCurrency %s not found!", context.getString("in"));
                return;
            }
        }
        else
        {
            currency = this.module.getCurrencyManager().getMainCurrency(); //TODO choose currency / or match with formatting
        }
        String amountString = context.getString(1);
        Long amount = currency.parse(amountString);
        if (amount == null)
        {
            context.sendMessage("conomy", "&cCould not parse amount!");
            return;
        }
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendMessage("conomy", "&cUser %s not found!", context.getString(0));
            return;
        }
        Account target = this.module.getAccountsManager().getAccount(user, currency);
        if (target == null)
        {
            context.sendMessage("conomy", "&2%s &cdoes not have an account for &6%s&c!",
                    user.getName(), currency.getName());
            return;
        }
        if (this.module.getAccountsManager().transaction(null, target, -amount, true))
        {
            context.sendMessage("conomy", "&aYou took &6%s &afrom &2%s&a!", currency.formatLong(amount), user.getName());
        }
    }

    @Command(
             desc = "Reset the money from given user",
             usage = "<player> [in <currency>]",
             min = 1, max = 1)
    public void reset(CommandContext context)
    {
        Currency currency;
        if (context.hasNamed("in"))
        {
            currency = this.module.getCurrencyManager().getCurrencyByName(context.getString("in"));
            if (currency == null)
            {
                context.sendMessage("conomy", "&cCurrency %s not found!", context.getString("in"));
                return;
            }
        }
        else
        {
            currency = this.module.getCurrencyManager().getMainCurrency(); //TODO choose currency / or match with formatting
        }
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendMessage("conomy", "&cUser %s not found!", context.getString(0));
            return;
        }
        Account target = this.module.getAccountsManager().getAccount(user, currency);
        if (target == null)
        {
            context.sendMessage("conomy", "&2%s &cdoes not have an account for &6%s&c!",
                    user.getName(), currency.getName());
            return;
        }
        target.resetToDefault();
        context.sendMessage("conomy", "&2%s &aaccount reset to &6%sa!", user.getName(), currency.formatLong(target.getBalance()));
    }

    @Command(
             desc = "Sets the money from given user",
             usage = "<player> <amount> [in <currency>]",
             min = 2, max = 2)
    public void set(CommandContext context)
    {
        Currency currency;
        if (context.hasNamed("in"))
        {
            currency = this.module.getCurrencyManager().getCurrencyByName(context.getString("in"));
            if (currency == null)
            {
                context.sendMessage("conomy", "&cCurrency %s not found!", context.getString("in"));
                return;
            }
        }
        else
        {
            currency = this.module.getCurrencyManager().getMainCurrency(); //TODO choose currency / or match with formatting
        }
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendMessage("conomy", "&cUser %s not found!", context.getString(0));
            return;
        }
        Account target = this.module.getAccountsManager().getAccount(user, currency);
        if (target == null)
        {
            context.sendMessage("conomy", "&2%s &cdoes not have an account for &6%s&c!",
                    user.getName(), currency.getName());
            return;
        }
        String amountString = context.getString(1);
        Long amount = currency.parse(amountString);
        if (amount == null)
        {
            context.sendMessage("conomy", "&cCould not parse amount!");
            return;
        }
        target.set(amount);
        context.sendMessage("conomy", "&2%s &aaccount set to &6%s&a!", user.getName(), currency.formatLong(amount));
    }

    public void scale(CommandContext context)
    {
    }
}
