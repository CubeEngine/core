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
import de.cubeisland.cubeengine.core.util.StringUtils;

public class EcoCommands extends ContainerCommand
{
    private Conomy module;

    public EcoCommands(Conomy module)
    {
        super(module, "eco", "Administrative commands for Conomy.");
        this.module = module;
    }

    @Command(names = {"give", "grant"},
            desc = "Gives money to given user or all [online] users",
            usage = "<player>|* [-o] <amount> [in <currency>]",
            flags = @Flag(longName = "online", name = "o"),
            params = @Param(names = {"in", "c", "currency"}, type = String.class), min = 2, max = 2)
    public void give(CommandContext context)
    {
        Currency currency;
        String amountString = context.getString(1);
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
        if (context.getString(0).equalsIgnoreCase("*"))
        {
            if (context.hasFlag("o"))
            {
                this.module.getAccountsManager().transactAll(currency, amount, true);
                context.sendMessage("conomy", "&aYou gave &6%s &ato every online user!", currency.formatLong(amount));
            }
            else
            {
                this.module.getAccountsManager().transactAll(currency, amount, false);
                context.sendMessage("conomy", "&aYou gave &6%s &ato every user!", currency.formatLong(amount));
            }
        }
        else
        {
            String[] users =  StringUtils.explode(",", context.getString(0));
            for (String userString : users)
            {
                User user = this.module.getUserManager().findUser(userString);
                if (user == null)
                {
                    context.sendMessage("conomy", "&cUser %s not found!", context.getString(0));
                    continue;
                }
                Account target = this.module.getAccountsManager().getAccount(user, currency);
                if (target == null)
                {
                    context.sendMessage("conomy", "&2%s &cdoes not have an account for &6%s&c!",
                            user.getName(), currency.getName());
                    continue;
                }
                this.module.getAccountsManager().transaction(null, target, amount);
                context.sendMessage("conomy", "&aYou gave &6%s &ato &2%s&a!", currency.formatLong(amount), user.getName());
                if (!context.getSender().getName().equals(user.getName()))
                {
                    user.sendMessage("conomy", "&aYou were granted &6%s&a.", currency.formatLong(amount));
                }
            }
        }
    }

    @Command(names = { "take", "remove"},
            desc = "Takes money from given user",
            usage = "<player>|* [-o] <amount> [in <currency>]",
            flags = @Flag(longName = "online", name = "o"),
            params = @Param(names = {"in", "c", "currency"}, type = String.class), min = 1, max = 2)
    public void take(CommandContext context)
    {
        Currency currency;
        String amountString = context.getString(1);
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
        if (context.getString(0).equalsIgnoreCase("*"))
        {
            if (context.hasFlag("o"))
            {
                this.module.getAccountsManager().transactAll(currency, -amount, true);
                context.sendMessage("conomy", "&aYou took &6%s &afrom every online euser!", currency.formatLong(amount));
            }
            else
            {
                this.module.getAccountsManager().transactAll(currency, -amount, false);
                context.sendMessage("conomy", "&aYou took &6%s &afrom every user!", currency.formatLong(amount));
            }
        }
        else
        {
            String[] users =  StringUtils.explode(",", context.getString(0));
            for (String userString : users)
            {
                User user = this.module.getUserManager().findUser(userString);
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
                this.module.getAccountsManager().transaction(null, target, -amount);
                context.sendMessage("conomy", "&aYou took &6%s &afrom &2%s&a!", currency.formatLong(amount), user.getName());
                if (!context.getSender().getName().equals(user.getName()))
                {
                    user.sendMessage("conomy", "&eWithdrawed &6%s &efrom your account.", currency.formatLong(amount));
                }
            }
        }
    }

    @Command(desc = "Reset the money from given user",
            usage = "<player>|* [-o] [in <currency>]",
            flags = @Flag(longName = "online", name = "o"),
            params = @Param(names = {"in", "c", "currency"}, type = String.class), max = 1)
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
        if (context.getString(0).equalsIgnoreCase("*"))
        {
            if (context.hasFlag("o"))
            {
                this.module.getAccountsManager().setAll(currency, currency.getDefaultBalance(), true);
                context.sendMessage("conomy", "&aYou resetted every online user account!");
            }
            else
            {
                this.module.getAccountsManager().setAll(currency, currency.getDefaultBalance(), false);
                context.sendMessage("conomy", "&aYou resetted every user account!");
            }
        }
        else
        {
            String[] users =  StringUtils.explode(",", context.getString(0));
            for (String userString : users)
            {
                User user = this.module.getUserManager().findUser(userString);
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
    }

    @Command(desc = "Sets the money from given user",
            usage = "<player>|* [-o] <amount> [in <currency>]",
            flags = @Flag(longName = "online", name = "o"),
            params = @Param(names = { "in", "c", "currency" }, type = String.class), min = 1, max = 2)
    public void set(CommandContext context)
    {
        Currency currency;
        String amountString = context.getString(1);
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
        // try to match if fail default
        {
            currency = this.module.getCurrencyManager().matchCurrency(amountString, true).iterator().next(); // can never be empty
        }
        Long amount = currency.parse(amountString);
        if (amount == null)
        {
            context.sendMessage("conomy", "&cCould not parse amount!");
            return;
        }
        if (context.getString(0).equalsIgnoreCase("*"))
        {
            if (context.hasFlag("o"))
            {
                this.module.getAccountsManager().setAll(currency, currency.getDefaultBalance(), true);
                context.sendMessage("conomy", "&aYou have set every online user account to &6%s&a!", currency.formatLong(amount));
            }
            else
            {
                this.module.getAccountsManager().setAll(currency, currency.getDefaultBalance(), false);
                context.sendMessage("conomy", "&aYou have set every user account to &6%s&a!", currency.formatLong(amount));
            }
        }
        else
        {
            String[] users =  StringUtils.explode(",", context.getString(0));
            for (String userString : users)
            {
                User user = this.module.getUserManager().findUser(userString);
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
    }

    public void scale(CommandContext context)//TODO
    {}

    @Command(desc = "Hides the account of given player",
            usage = "<player> [in <currency>]",
            params = @Param(names = { "in", "c", "currency" }, type = String.class), min = 1, max = 2)
    public void hide(CommandContext context)
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
        String[] users =  StringUtils.explode(",", context.getString(0));
        for (String userString : users)
        {
            User user = this.module.getUserManager().findUser(userString);
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
            boolean isHidden = target.isHidden();

            if (isHidden)
            {
                context.sendMessage("conomny","&2%s's %eaccount in &6%s &eis already hidden!", user.getName(), currency.getName());
            }
            else
            {
                target.setHidden(true);
                context.sendMessage("conomny","&2%s's %aaccount in &6%s &ais now hidden!", user.getName(), currency.getName());
            }
        }
    }

    @Command(desc = "Unhides the account of given player",
            usage = "<player> [in <currency>]",
            params = @Param(names = { "in", "c", "currency" }, type = String.class), min = 1, max = 2)
    public void unhide(CommandContext context)
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
        String[] users =  StringUtils.explode(",", context.getString(0));
        for (String userString : users)
        {
            User user = this.module.getUserManager().findUser(userString);
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
            boolean isHidden = target.isHidden();

            if (isHidden)
            {
                target.setHidden(false);
                context.sendMessage("conomny","&2%s's %aaccount in &6%s &ais no longer hidden!", user.getName(), currency.getName());
            }
            else
            {
                context.sendMessage("conomny","&2%s's %eaccount in &6%s &ewas not hidden!", user.getName(), currency.getName());
            }
        }
    }
}
