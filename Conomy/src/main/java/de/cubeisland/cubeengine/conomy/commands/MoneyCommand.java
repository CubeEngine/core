package de.cubeisland.cubeengine.conomy.commands;

import de.cubeisland.cubeengine.conomy.Conomy;
import de.cubeisland.cubeengine.conomy.account.Account;
import de.cubeisland.cubeengine.conomy.account.storage.AccountModel;
import de.cubeisland.cubeengine.conomy.currency.Currency;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.annotation.Alias;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.user.User;
import java.util.Collection;

public class MoneyCommand extends ContainerCommand
{
    private Conomy module;

    public MoneyCommand(Conomy module)
    {
        super(module, "money", "Manages your money.");
        this.module = module;
    }

    @Alias(names =
    {
        "money",//TODO this does not WORK  :( fix it @Quick_Wango
        "balance", "moneybalance"
    })
    @Command(desc = "Shows your balance",
             usage = "[player] [in <currency>]|[-a]", flags =
    @Flag(longName = "all", name = "a"), params =
    @Param(names = "in", type = String.class), max = 1)
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
                context.sendMessage("conomey", "&2%s's &a%s-Balance: &6%s", user.getName(), acc.getCurrency().getName(), acc.getCurrency().formatLong(acc.getBalance()));
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
            context.sendMessage("conomey", "&2%s's &aBalance: &6%s", user.getName(), acc.getCurrency().formatLong(acc.getBalance()));
        }
    }

    @Alias(names =
    {
        "toplist", "balancetop"
    })
    @Command(desc = "Shows the players with the highest balance.",
             usage = "[[fromRank]-ToRank] [in <currency>]", params =
    @Param(names = "in", type = String.class))
    public void top(CommandContext context)
    {
        int fromRank = 1;
        int toRank = 10;
        if (context.hasIndexed(0))
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
        if (context.hasNamed("in"))
        {
            currency = this.module.getCurrencyManager().getCurrencyByName(context.getString("in"));
            if (currency == null)
            {
                context.sendMessage("conomy", "&cCurrency %s not found!", context.getString("in"));
                return;
            }
        }
        models = this.module.getAccountsStorage().getTopAccounts(currency, fromRank, toRank);
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
                    this.module.getUserManager().getUser(account.user_id).getName(), currency.formatLong(account.value));
        }
    }

    @Alias(names =
    {
        "pay"
    })
    @Command(names =
    {
        "pay", "give"
    },
             desc = "Transfer the given amount to another account.",
             usage = "<player> [as <player>] <amount> [-bank]",
             params =
    {
        @Param(names = "as", type = User.class),
        @Param(names = "in", type = String.class)
    },
             flags =
    {
        @Flag(longName = "bank", name = "b"),
        @Flag(longName = "force", name = "f")
    },
             min = 2, max = 2)
    public void pay(CommandContext context)
    {
        //TODO later try to autodetect currency if not given
        //if containing Symbols of only one currency
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
        if (context.hasNamed("as"))
        {
            sender = context.getUser("as");
            if (sender == null)
            {
                context.sendMessage("conomy", "&cUser %s not found!", context.getString("as"));
                return;
            }
        }
        else
        {
            sender = context.getSenderAsUser("conomy", "&cPlease specify a user to use his account.");
        }
        if (context.hasFlag("b"))
        {
            Account source = this.module.getAccountsManager().getAccount(sender, currency);
            Account target = this.module.getAccountsManager().getAccount(context.getString(0), currency);
            if (source == null)
            {
                context.sendMessage("conomy", "&cCannot find user-account for &2%s&c!", sender.getName());
                return;
            }
            if (target == null)
            {
                context.sendMessage("conomy", "&cCannot find bank-account &6%s&c!", context.getString(0));
                return;
            }
            if (this.module.getAccountsManager().transaction(source, target, amount, context.hasFlag("f")))
            {
                context.sendMessage("conomy", "&6%s &atransfered from &2%s's &ato the bank.account &6%s!", formattedAmount, sender.getName(), context.getString(0));
            }
            else if (context.hasNamed("as"))
            {
                context.sendMessage("conomy", "&2%s &ccannot afford &6%s&c!", sender.getName(), currency.formatLong(amount));
            }
            else
            {
                context.sendMessage("conomy", "&cYou cannot afford &6%s&c!", currency.formatLong(amount));
            }
        }
        else
        {
            User user = context.getUser(0);
            if (user == null)
            {
                context.sendMessage("conomy", "&cUser %s not found!", context.getString(0));
                return;
            }
            Account source = this.module.getAccountsManager().getAccount(sender, currency);
            Account target = this.module.getAccountsManager().getAccount(user, currency);
            if (source == null || target == null)
            {
                context.sendMessage("conomy", "&2%s &cdoes not have an account for &6%s&c!",
                        source == null ? sender.getName() : user.getName(), currency.getName());
                return;
            }
            if (this.module.getAccountsManager().transaction(source, target, amount, context.hasFlag("f")))
            {
                context.sendMessage("conomy", "&6%s &atransfered from &2%s's &ato &2%s's &aaccount!", formattedAmount, sender.getName(), user.getName());
                user.sendMessage("conomy", "&2%s &ajust send you &6%s!", sender.getName(), formattedAmount);
            }
            else if (context.hasNamed("as"))
            {
                context.sendMessage("conomy", "&2%s &ccannot afford &6%s&c!", sender.getName(), currency.formatLong(amount));
            }
            else
            {
                context.sendMessage("conomy", "&cYou cannot afford &6%s&c!", currency.formatLong(amount));
            }
        }
    }
}
