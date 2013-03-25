package de.cubeisland.cubeengine.conomy.commands;

import java.util.Collection;

import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.Param;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Alias;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.conomy.Conomy;
import de.cubeisland.cubeengine.conomy.ConomyPermissions;
import de.cubeisland.cubeengine.conomy.account.Account;
import de.cubeisland.cubeengine.conomy.account.storage.AccountModel;
import de.cubeisland.cubeengine.conomy.currency.Currency;

public class MoneyCommand extends ContainerCommand
{
    private Conomy module;

    public MoneyCommand(Conomy module)
    {
        super(module, "money", "Manages your money.");
        this.module = module;
    }

    //    @Override
    //    public void run(CommandContext context) {
    //        super.run(context);
    //        /*
    //        if (context.hasIndexed(0))
    //        {
    //            super.run(context);
    //        }
    //        else
    //        {
    //            this.balance(context); // Alias for balance //TODO this is broken because of flags checks in balance
    //        }*/
    //    }

    @Alias(names = {
        "balance", "moneybalance"
    })
    @Command(desc = "Shows your balance", usage = "[player] [in <currency>]|[-a]", flags = {
    @Flag(longName = "all", name = "a"),
            @Flag(longName = "showHidden", name = "f")
    }, params = @Param(names = "in", type = String.class), max = 1)
    public void balance(ParameterizedContext context)
    {
        User user;
        boolean showHidden = context.hasFlag("f");
        if (showHidden)
        {
            if (!ConomyPermissions.ACCOUNT_SHOWHIDDEN.isAuthorized(context.getSender()))
                showHidden = false;
        }
        if (context.hasArg(0))
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
            if (!(context.getSender() instanceof User))
            {
                context.sendMessage("conomy", "&cYou are out of money! Better go work than typing silly commands in the console.");
                return;
            }
            user = (User)context.getSender();
        }
        if (context.hasFlag("a"))
        {
            Collection<Account> accs = this.module.getAccountsManager().getAccounts(user);
            for (Account acc : accs)
            {
                if (!acc.isHidden() || showHidden)
                    context.sendMessage("conomey", "&2%s's &a%s-Balance: &6%s", user.getName(), acc.getCurrency().getName(), acc.getCurrency().formatLong(acc.getBalance()));
            }
        }
        else
        {
            Account acc;
            if (context.hasParam("in"))
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
            if (!acc.isHidden() || showHidden)
                context.sendMessage("conomy", "&2%s's &aBalance: &6%s", user.getName(), acc.getCurrency().formatLong(acc.getBalance()));
            else
                context.sendMessage("conomy", "&cNo account found for &2%s&c!", user.getName());
        }
    }

    @Alias(names = {
        "toplist", "balancetop"
    })
    @Command(desc = "Shows the players with the highest balance.", usage = "[[fromRank]-ToRank] [in <currency>]", params = @Param(names = "in", type = String.class), flags = @Flag(longName = "showhidden", name = "f"))
    public void top(ParameterizedContext context)
    {
        boolean showHidden = context.hasFlag("f");
        if (showHidden)
        {
            if (!ConomyPermissions.ACCOUNT_SHOWHIDDEN.isAuthorized(context.getSender()))
                showHidden = false;
        }
        int fromRank = 1;
        int toRank = 10;
        if (context.hasArg(0))
        {
            try
            {
                String range = context.getString(0);
                if (range.contains("-"))
                {
                    fromRank = Integer.parseInt(range.substring(0, range.indexOf("-")));
                    range = range.substring(range.indexOf("-") + 1);
                }
                toRank = Integer.parseInt(range);
            }
            catch (NumberFormatException e)
            {
                context.sendMessage("conomy", "&cInvalid rank!");
                return;
            }
        }
        Collection<AccountModel> models;
        Currency currency = this.module.getAccountsManager().getMainCurrency();
        if (context.hasParam("in"))
        {
            currency = this.module.getCurrencyManager().getCurrencyByName(context.getString("in"));
            if (currency == null)
            {
                context.sendMessage("conomy", "&cCurrency %s not found!", context.getString("in"));
                return;
            }
        }
        models = this.module.getAccountsStorage().getTopAccounts(currency, fromRank, toRank, showHidden);
        int i = fromRank;
        if (fromRank == 1)
        {
            context.sendMessage("conomy", "&aTop Balance &f(&6%d&f)", models.size());
        }
        else
        {
            context.sendMessage("conomy", "&aTop Balance from &6%d &ato &6%d", fromRank, fromRank + models.size());
        }
        for (AccountModel account : models)
        {
            context.sendMessage("conomy", "&a%d &f- &2%s&f: &6%s", i++,
                    this.module.getCore().getUserManager().getUser(account.user_id).getName(), currency.formatLong(account.value));
        }
    }

    @Alias(names = {
        "pay"
    })
    @Command(names = {
        "pay", "give"
    }, desc = "Transfer the given amount to another account.", usage = "<player> [as <player>] <amount>", params = {
        @Param(names = "as", type = User.class),
        @Param(names = "in", type = String.class)
    }, flags = @Flag(longName = "force", name = "f"), min = 2, max = 2)
    public void pay(ParameterizedContext context)
    {
        Currency currency;
        String amountString = context.getString(1);
        if (context.hasParam("in"))
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
            currency = this.module.getCurrencyManager().matchCurrency(amountString);
        }
        Long amount = currency.parse(amountString);
        if (amount == null)
        {
            context.sendMessage("conomy", "&cCould not parse amount!");
            return;
        }
        String formattedAmount = currency.formatLong(amount);
        User sender;
        boolean asSomeOneElse = false;
        if (context.hasParam("as"))
        {
            sender = context.getUser("as");
            if (sender == null)
            {
                context.sendMessage("conomy", "&cUser %s not found!", context.getString("as"));
                return;
            }
            asSomeOneElse = true;
        }
        else
        {
            if (!(context.getSender() instanceof User))
            {
                context.sendMessage("conomy", "&cPlease specify a user to use his account.");
                return;
            }
            sender = (User)context.getSender();
        }
        Account source = this.module.getAccountsManager().getAccount(sender, currency);
        if (source == null)
        {
            context.sendMessage("conomy", "&2%s &cdoes not have an account for &6%s&c!",
                    sender.getName(), currency.getName());
            return;
        }
        String[] users = StringUtils.explode(",", context.getString(0));
        for (String userString : users)
        {
            User user = this.module.getCore().getUserManager().findUser(userString);
            if (user == null)
            {
                context.sendMessage("conomy", "&cUser %s not found!", context.getString(0));
                continue;
            }
            Account target = this.module.getAccountsManager().getAccount(user, currency);
            if (target == null)
            {
                context.sendMessage("conomy", "&2%s &cdoes not have an account for &6%s&c!",
                        sender.getName(), currency.getName());
                continue;
            }
            if (!(context.hasFlag("f") && ConomyPermissions.COMMAND_PAY_FORCE.isAuthorized(context.getSender()))) //force allowed
            {
                if (!source.canAfford(amount))
                {
                    if (asSomeOneElse)
                    {
                        context.sendMessage("conomy", "&2%s &ccannot afford &6%s&c!", sender.getName(), currency.formatLong(amount));
                    }
                    else
                    {
                        context.sendMessage("conomy", "&cYou cannot afford &6%s&c!", currency.formatLong(amount));
                    }
                    return;
                }
            }
            if (this.module.getAccountsManager().transaction(source, target, amount))
            {
                if (asSomeOneElse)
                {
                    context.sendMessage("conomy", "&6%s &atransfered from &2%s's &ato &2%s's &aaccount!", formattedAmount, sender.getName(), user.getName());
                }
                else
                {
                    context.sendMessage("conomy", "&6%s &atransfered to &2%s's &aaccount!", formattedAmount, user.getName());
                }
                user.sendMessage("conomy", "&2%s &ajust send you &6%s!", sender.getName(), formattedAmount);
            }
        }
    }
}
