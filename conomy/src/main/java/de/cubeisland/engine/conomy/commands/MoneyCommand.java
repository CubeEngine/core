/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.conomy.commands;

import java.util.Collection;

import de.cubeisland.engine.conomy.Conomy;
import de.cubeisland.engine.conomy.account.Account;
import de.cubeisland.engine.conomy.account.ConomyManager;
import de.cubeisland.engine.conomy.account.UserAccount;
import de.cubeisland.engine.conomy.account.storage.AccountModel;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandResult;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.StringUtils;

import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;
import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

public class MoneyCommand extends ContainerCommand
{
    private final Conomy module;
    private final ConomyManager manager;

    public MoneyCommand(Conomy module)
    {
        super(module, "money", "Manages your money.");
        this.module = module;
        this.manager = module.getManager();
    }

    @Override
    public CommandResult run(CommandContext context) throws Exception
    {
        if (context.hasArg(0))
        {
            return super.run(context);
        }
        this.balance((ParameterizedContext)context);
        return null;
    }
    
    private UserAccount getUserAccount(User user)
    {
        return this.manager.getUserAccount(user, this.module.getConfig().autocreateUserAcc);
    }

    @Alias(names = {"balance", "moneybalance", "pmoney"})
    @Command(desc = "Shows your balance",
             usage = "[player]",
             flags = @Flag(longName = "showHidden", name = "f"),
             max = 1)
    public void balance(ParameterizedContext context)
    {
        User user;
        boolean showHidden = context.hasFlag("f") && module.perms().USER_SHOWHIDDEN.isAuthorized(context.getSender());
        if (context.hasArg(0))
        {
            user = context.getUser(0);
            if (user == null)
            {
                context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getString(0));
                return;
            }
        }
        else
        {
            if (!(context.getSender() instanceof User))
            {
                context.sendTranslated(NEGATIVE, "If you are out of money, better go work than typing silly commands in the console.");
                return;
            }
            user = (User)context.getSender();
        }
        UserAccount account = this.getUserAccount(user);
        if (account != null)
        {
            if (!account.isHidden() || showHidden || account.getName().equalsIgnoreCase(user.getName()))
            {
                context.sendTranslated(POSITIVE, "{user}'s Balance: {input#balance}", user, manager.format(account.balance()));
                return;
            }
        }
        context.sendTranslated(NEGATIVE, "No account found for {user}!", user);
    }

    @Alias(names = {"toplist", "balancetop", "topmoney"})
    @Command(desc = "Shows the players with the highest balance.",
             usage = "[[fromRank]-ToRank]", max = 1,
             flags = @Flag(longName = "showhidden", name = "f"))
    public void top(ParameterizedContext context)
    {
        boolean showHidden = context.hasFlag("f");
        if (showHidden)
        {
            if (!module.perms().USER_SHOWHIDDEN.isAuthorized(context.getSender()))
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
                context.sendTranslated(NEGATIVE, "Invalid rank!");
                return;
            }
        }
        Collection<AccountModel> models = this.manager.getTopAccounts(true, false, fromRank, toRank, showHidden);
        int i = fromRank;
        if (fromRank == 1)
        {
            context.sendTranslated(POSITIVE, "Top Balance ({amount})", models.size());
        }
        else
        {
            context.sendTranslated(POSITIVE, "Top Balance from {integer} to {integer}", fromRank, fromRank + models.size() -1);
        }
        for (AccountModel account : models)
        {
            context.sendMessage("" + i++ + ChatFormat.WHITE + "- " + ChatFormat.DARK_GREEN +
                                    this.module.getCore().getUserManager().getUser(account.getUserId()).getName() +
                                    ChatFormat.WHITE + ": " + ChatFormat.GOLD + (manager.format((double)account.getValue() / manager.fractionalDigitsFactor())));
        }
    }

    @Alias(names = {"pay"})
    @Command(names = {"pay", "give"},
             desc = "Transfer the given amount to another account.",
             usage = "<player> [as <player>] <amount>",
             params = @Param(names = "as", type = User.class),
             flags = @Flag(longName = "force", name = "f"),
             min = 2, max = 2)
    public void pay(ParameterizedContext context)
    {
        String amountString = context.getString(1);
        Double amount = manager.parse(amountString, context.getSender().getLocale());
        if (amount == null)
        {
            context.sendTranslated(NEGATIVE, "Invalid amount!");
            return;
        }
        if (amount < 0)
        {
            context.sendTranslated(NEGATIVE, "What are you trying to do?");
            return;
        }
        String format = manager.format(amount);
        User sender;
        boolean asSomeOneElse = false;
        if (context.hasParam("as"))
        {
            if (!module.perms().COMMAND_PAY_ASOTHER.isAuthorized(context.getSender()))
            {
                context.sendTranslated(NEGATIVE, "You are not allowed to pay money as someone else!");
                return;
            }
            sender = context.getUser("as");
            if (sender == null)
            {
                context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getString("as"));
                return;
            }
            asSomeOneElse = true;
        }
        else
        {
            if (!(context.getSender() instanceof User))
            {
                context.sendTranslated(NEGATIVE, "Please specify a player to use their account.");
                return;
            }
            sender = (User)context.getSender();
        }
        Account source = this.manager.getUserAccount(sender, false);
        if (source == null)
        {
            if (asSomeOneElse)
            {
                context.sendTranslated(NEGATIVE, "{user} does not have an account!", sender);
            }
            else
            {
                context.sendTranslated(NEGATIVE, "You do not have an account!");
            }
            return;
        }
        String[] users = StringUtils.explode(",", context.getString(0));
        for (String userString : users)
        {
            User user = this.module.getCore().getUserManager().findUser(userString);
            if (user == null)
            {
                context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getString(0));
                continue;
            }
            Account target = this.manager.getUserAccount(user, false);
            if (target == null)
            {
                context.sendTranslated(NEGATIVE, "{user} does not have an account!", user);
                continue;
            }
            if (!(context.hasFlag("f") && module.perms().COMMAND_MONEY_PAY_FORCE.isAuthorized(context.getSender()))) //force allowed
            {
                if (!source.has(amount))
                {
                    if (asSomeOneElse)
                    {
                        context.sendTranslated(NEGATIVE, "{user} cannot afford {input#amount}!", sender.getName(), format);
                    }
                    else
                    {
                        context.sendTranslated(NEGATIVE, "You cannot afford {input#amount}!", format);
                    }
                    return;
                }
            }
            if (this.manager.transaction(source, target, amount, false))
            {
                if (asSomeOneElse)
                {
                    context.sendTranslated(POSITIVE, "{input#amount} transferred from {user}'s to {user}'s account!", format, sender, user);
                }
                else
                {
                    context.sendTranslated(POSITIVE, "{input#amount} transferred to {user}'s account!", format, user);
                }
                user.sendTranslated(POSITIVE, "{user} just paid you {input#amount}!", sender, format);
            }
            else
            {
                context.sendTranslated(NEGATIVE, "The Transaction was not successful!");
            }
        }
    }
}
