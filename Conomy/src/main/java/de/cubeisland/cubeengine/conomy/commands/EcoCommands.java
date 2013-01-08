package de.cubeisland.cubeengine.conomy.commands;

import de.cubeisland.cubeengine.conomy.Conomy;
import de.cubeisland.cubeengine.conomy.account.Account;
import de.cubeisland.cubeengine.conomy.currency.Currency;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.user.User;

public class EcoCommands extends ContainerCommand
{
    private Conomy module;

    public EcoCommands(Conomy module)
    {
        super(module, "eco", "Administrative commands for Conomy.");
        this.module = module;
    }

    //TODO give a , separated list of users for all cmds
    @Command(names =
    {
        "give", "grant"
    },
             desc = "Gives money to given user",
             usage = "<player>|<-all[online]> <amount> [in <currency>]", flags =
    {
        @Flag(longName = "all", name = "a"),
        @Flag(longName = "allonline", name = "ao")
    }, params =
    @Param(names = "in", type = String.class),
             min = 1, max = 2)
    public void give(CommandContext context)
    {
        Currency currency;
        String amountString;
        if (context.hasFlag("a") || context.hasFlag("ao"))
        {
            amountString = context.getString(0);
        }
        else if (context.hasIndexed(1))
        {
            amountString = context.getString(1);
        }
        else
        {
            //TODO Show default msg for out of range /context.outOfRange(hasRange, shouldHaveRange);
            return;
        }
        if (context.hasNamed("in"))
        {
            currency = this.module.getCurrencyManager().getCurrencyByName(context.getString("in"));
            if (currency == null)
            {
                context.sendMessage("conomy", "&cCurrency %s not found!", context.getString("in"));
                return;
            }
        }
        else // try to match if fail default
        {
            currency = this.module.getCurrencyManager().matchCurrency(amountString, true).iterator().next(); // can never be empty
        }
        Long amount = currency.parse(amountString);
        if (amount == null)
        {
            context.sendMessage("conomy", "&cCould not parse amount!");
            return;
        }
        if (context.hasFlag("a"))
        {
            this.module.getAccountsManager().transactAll(currency, amount, false);
            context.sendMessage("conomy", "&aYou gave &6%s &ato every user!", currency.formatLong(amount));
        }
        else if (context.hasFlag("ao"))
        {
            this.module.getAccountsManager().transactAll(currency, amount, true);
            context.sendMessage("conomy", "&aYou gave &6%s &ato every online user!", currency.formatLong(amount));
        }
        else
        {
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
                if (!context.getSender().getName().equals(user.getName()))
                {
                    user.sendMessage("conomy", "&aYou were granted &6%s&a.", currency.formatLong(amount));
                }
            }
        }
    }

    @Command(names =
    {
        "take", "remove"
    },
             desc = "Takes money from given user",
             usage = "<player>|<-all[online]> <amount> [in <currency>]",
             flags =
    {
        @Flag(longName = "all", name = "a"),
        @Flag(longName = "allonline", name = "ao")
    }, params =
    @Param(names = "in", type = String.class),
             min = 1, max = 2)
    public void take(CommandContext context)
    {
        Currency currency;
        String amountString;
        if (context.hasFlag("a") || context.hasFlag("ao"))
        {
            amountString = context.getString(0);
        }
        else if (context.hasIndexed(1))
        {
            amountString = context.getString(1);
        }
        else
        {
            //TODO Show default msg for out of range /context.outOfRange(hasRange, shouldHaveRange);
            return;
        }
        if (context.hasNamed("in"))
        {
            currency = this.module.getCurrencyManager().getCurrencyByName(context.getString("in"));
            if (currency == null)
            {
                context.sendMessage("conomy", "&cCurrency %s not found!", context.getString("in"));
                return;
            }
        }
        else // try to match if fail default
        {
            currency = this.module.getCurrencyManager().matchCurrency(amountString, true).iterator().next(); // can never be empty
        }
        Long amount = currency.parse(amountString);
        if (amount == null)
        {
            context.sendMessage("conomy", "&cCould not parse amount!");
            return;
        }
        if (context.hasFlag("a"))
        {
            this.module.getAccountsManager().transactAll(currency, amount, false);
            context.sendMessage("conomy", "&aYou took &6%s &afrom every user!", currency.formatLong(amount));
        }
        else if (context.hasFlag("ao"))
        {
            this.module.getAccountsManager().transactAll(currency, amount, true);
            context.sendMessage("conomy", "&aYou took &6%s &afrom every online euser!", currency.formatLong(amount));
        }
        else
        {
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
                if (!context.getSender().getName().equals(user.getName()))
                {
                    user.sendMessage("conomy", "&eWithdrawed &6%s &efrom your account.", currency.formatLong(amount));
                }
            }
        }
    }

    @Command(
             desc = "Reset the money from given user",
             usage = "<player>|<-all[online]> [in <currency>]",
             flags =
    {
        @Flag(longName = "all", name = "a"),
        @Flag(longName = "allonline", name = "ao")
    }, params =
    @Param(names = "in", type = String.class),
             max = 1)
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
        else // default
        {
            currency = this.module.getCurrencyManager().getMainCurrency();
        }
        if (context.hasFlag("a"))
        {
            this.module.getAccountsManager().setAll(currency, currency.getDefaultBalance(), false);
            context.sendMessage("conomy", "&aYou resetted every user account!");
        }
        else if (context.hasFlag("ao"))
        {
            this.module.getAccountsManager().setAll(currency, currency.getDefaultBalance(), true);
            context.sendMessage("conomy", "&aYou resetted every online user account!");
        }
        else
        {
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
            context.sendMessage("conomy", "&2%s &aaccount reset to &6%s&a!", user.getName(), currency.formatLong(target.getBalance()));
            if (!context.getSender().getName().equals(user.getName()))
            {
                user.sendMessage("conomy", "&eYour balance got resetted to &6%s&e.", currency.formatLong(target.getBalance()));
            }
        }
    }

    @Command(
             desc = "Sets the money from given user",
             usage = "<player>|<-all[online]> <amount> [in <currency>]",
             flags =
    {
        @Flag(longName = "all", name = "a"),
        @Flag(longName = "allonline", name = "ao")
    }, params =
    @Param(names = "in", type = String.class),
             min = 1, max = 2)
    public void set(CommandContext context)
    {
        Currency currency;
        String amountString;
        if (context.hasFlag("a") || context.hasFlag("ao"))
        {
            amountString = context.getString(0);
        }
        else if (context.hasIndexed(1))
        {
            amountString = context.getString(1);
        }
        else
        {
            //TODO Show default msg for out of range /context.outOfRange(hasRange, shouldHaveRange);
            return;
        }
        if (context.hasNamed("in"))
        {
            currency = this.module.getCurrencyManager().getCurrencyByName(context.getString("in"));
            if (currency == null)
            {
                context.sendMessage("conomy", "&cCurrency %s not found!", context.getString("in"));
                return;
            }
        }
        else // try to match if fail default
        {
            currency = this.module.getCurrencyManager().matchCurrency(amountString, true).iterator().next(); // can never be empty
        }
        Long amount = currency.parse(amountString);
        if (amount == null)
        {
            context.sendMessage("conomy", "&cCould not parse amount!");
            return;
        }
        if (context.hasFlag("a"))
        {
            this.module.getAccountsManager().setAll(currency, currency.getDefaultBalance(), false);
            context.sendMessage("conomy", "&aYou have set every user account to &6%s&a!", currency.formatLong(amount));
        }
        else if (context.hasFlag("ao"))
        {
            this.module.getAccountsManager().setAll(currency, currency.getDefaultBalance(), true);
            context.sendMessage("conomy", "&aYou have set every online user account to &6%s&a!", currency.formatLong(amount));
        }
        else
        {
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
            target.set(amount);
            context.sendMessage("conomy", "&2%s &aaccount set to &6%s&a!", user.getName(), currency.formatLong(amount));
            if (!context.getSender().getName().equals(user.getName()))
            {
                user.sendMessage("conomy", "&eYour balance got set to &6%s&e.", currency.formatLong(amount));
            }
        }
    }

    public void scale(CommandContext context)//TODO
    {
    }
}
