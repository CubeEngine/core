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

import de.cubeisland.engine.conomy.Conomy;
import de.cubeisland.engine.conomy.account.Account;
import de.cubeisland.engine.conomy.account.ConomyManager;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.StringUtils;

import static de.cubeisland.engine.core.util.formatter.MessageType.*;

public class EcoCommands extends ContainerCommand
{
    private final Conomy module;
    private final ConomyManager manager;

    public EcoCommands(Conomy module)
    {
        super(module, "eco", "Administrative commands for Conomy.");
        this.module = module;
        this.manager = module.getManager();
    }

    @Command(names = {"give", "grant"},
             desc = "Gives money to one or all players.",
             usage = "<player>|* <amount> [-o]",
             flags = @Flag(longName = "online", name = "o"),
             min = 2, max = 2)
    public void give(ParameterizedContext context)
    {
        String amountString = context.getString(1);
        Double amount = this.manager.parse(amountString, context.getSender().getLocale());
        if (amount == null)
        {
            context.sendTranslated(NEGATIVE, "{input#amount} is not a valid number!", amountString);
            return;
        }
        String format = manager.format(amount);
        if ("*".equalsIgnoreCase(context.getString(0)))
        {
            if (context.hasFlag("o"))
            {
                if (this.manager.transactionAllOnline(amount))
                {
                    context.sendTranslated(POSITIVE, "You gave {input#amount} to every online player!", format);
                }
                else
                {
                    context.sendTranslated(NEGATIVE, "Please try again!");
                }
            }
            else
            {
                this.manager.transactionAll(true, false, amount);
                context.sendTranslated(POSITIVE, "You gave {input#amount} to every player!", format);
            }
        }
        else
        {
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
                if (this.manager.transaction(null, target, amount, true))
                {
                    context.sendTranslated(POSITIVE, "You gave {input#amount} to {user}!", format, user.getName());
                    if (!context.getSender().equals(user) && user.isOnline())
                    {
                        user.sendTranslated(POSITIVE, "You were granted {input#amount}.", format);
                    }
                }
                else
                {
                    context.sendTranslated(NEGATIVE, "Could not give the money to {user}!", user);
                }
            }
        }
    }

    @Command(names = {"take", "remove"},
             desc = "Takes money from given user",
             usage = "<player>|* <amount> [-o]",
             flags = @Flag(longName = "online", name = "o"),
             min = 1, max = 2)
    public void take(ParameterizedContext context)
    {
        String amountString = context.getString(1);
        Double amount = manager.parse(amountString, context.getSender().getLocale());
        if (amount == null)
        {
            context.sendTranslated(NEGATIVE, "Could not parse amount!");
            return;
        }
        String format = manager.format(amount);
        if ("*".equalsIgnoreCase(context.getString(0)))
        {
            if (context.hasFlag("o"))
            {
                if (this.manager.transactionAllOnline(-amount))
                {
                    context.sendTranslated(POSITIVE, "You took {input#amount} from every online player!", format);
                }
                else
                {
                    context.sendTranslated(NEGATIVE, "Please try again!");
                }
            }
            else
            {
                this.manager.transactionAll(true, false, -amount);
                context.sendTranslated(POSITIVE, "You took {input#amount} from every player!", format);
            }
        }
        else
        {
            String[] users = StringUtils.explode(",", context.getString(0));
            for (String userString : users)
            {
                User user = this.module.getCore().getUserManager().findUser(userString);
                if (user == null)
                {
                    context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getString(0));
                    return;
                }
                Account target = this.manager.getUserAccount(user, false);
                if (target == null)
                {
                    context.sendTranslated(NEGATIVE, "{user} does not have an account!", user);
                    return;
                }
                this.manager.transaction(target, null, amount, true);
                context.sendTranslated(POSITIVE, "You took {input#amount} from {user}!", format, user);
                if (!context.getSender().equals(user) && user.isOnline())
                {
                    user.sendTranslated(NEUTRAL, "Withdrew {input#amount} from your account.", format);
                }
            }
        }
    }

    @Command(desc = "Reset the money from given user",
             usage = "<player>|* [-o]",
             flags = @Flag(longName = "online", name = "o"),
             min = 1, max = 1)
    public void reset(ParameterizedContext context)
    {
        if ("*".equalsIgnoreCase(context.getString(0)))
        {
            if (context.hasFlag("o"))
            {
                if (this.manager.setAllOnline(this.manager.getDefaultBalance()))
                {
                    context.sendTranslated(POSITIVE, "You reset every online players' account!");
                }
                else
                {
                    context.sendTranslated(NEGATIVE, "Please try again!");
                }
            }
            else
            {
                this.manager.setAll(true, false, this.manager.getDefaultBalance());
                context.sendTranslated(POSITIVE, "You reset every players' account!");
            }
        }
        else
        {
            String[] users = StringUtils.explode(",", context.getString(0));
            for (String userString : users)
            {
                User user = this.module.getCore().getUserManager().findUser(userString);
                if (user == null)
                {
                    context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getString(0));
                    return;
                }
                Account target = this.manager.getUserAccount(user, false);
                if (target == null)
                {
                    context.sendTranslated(NEGATIVE, "{user} does not have an account!", user);
                    return;
                }
                target.reset();
                String format = this.manager.format(this.manager.getDefaultBalance());
                context.sendTranslated(POSITIVE, "{user} account reset to {input#balance}!", user, format);
                if (!context.getSender().equals(user) && user.isOnline())
                {
                    user.sendTranslated(NEUTRAL, "Your balance got reset to {input#balance}.", format);
                }
            }
        }
    }

    @Command(desc = "Sets the money of a given player",
             usage = "<player>|* <amount> [-o]",
             flags = @Flag(longName = "online", name = "o"),
             min = 2, max = 2)
    public void set(ParameterizedContext context)
    {
        String amountString = context.getString(1);
        Double amount = manager.parse(amountString, context.getSender().getLocale());
        if (amount == null)
        {
            context.sendTranslated(NEGATIVE, "That isn't a valid amount!");
            return;
        }
        String format = this.manager.format(amount);
        if ("*".equalsIgnoreCase(context.getString(0)))
        {
            if (context.hasFlag("o"))
            {
                if (this.manager.setAllOnline(amount))
                {
                    context.sendTranslated(POSITIVE, "You have set every online player account to {input#balance}!", format);
                }
                else
                {
                    context.sendTranslated(NEGATIVE, "Please try again!");
                }
            }
            else
            {
                this.manager.setAll(true, false, amount);
                context.sendTranslated(POSITIVE, "You have set every player account to {input#balance}!", format);
            }
        }
        else
        {
            String[] users = StringUtils.explode(",", context.getString(0));
            for (String userString : users)
            {
                User user = this.module.getCore().getUserManager().findUser(userString);
                if (user == null)
                {
                    context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getString(0));
                    return;
                }
                Account target = this.manager.getUserAccount(user, false);
                if (target == null)
                {
                    context.sendTranslated(NEGATIVE, "{user} does not have an account!", user);
                    return;
                }
                target.set(amount);
                context.sendTranslated(POSITIVE, "{user} account set to {input#balance}!", user, format);
                if (!context.getSender().equals(user) && user.isOnline())
                {
                    user.sendTranslated(NEUTRAL, "Your balance has been set to {input#balance}.", format);
                }
            }
        }
    }

    @Command(desc = "Scales the money of a given player",
             usage = "<player>|* <factor> [-o]",
             flags = @Flag(longName = "online", name = "o"),
             min = 2, max = 2)
    public void scale(ParameterizedContext context)
    {
        Float factor = context.getArg(1, Float.class, null);
        if (factor == null)
        {
            context.sendTranslated(NEGATIVE, "Invalid factor: {input#factor}", context.getString(1));
            return;
        }
        if ("*".equals(context.getString(0)))
        {
            if (context.hasFlag("o"))
            {
                if (this.manager.scaleAllOnline(factor))
                {
                    context.sendTranslated(POSITIVE, "Scaled the balance of every online player by {decimal#factor}!", factor);
                }
                else
                {
                    context.sendTranslated(NEGATIVE, "Please try again!");
                }
            }
            else
            {
                this.manager.scaleAll(true, false, factor);
                context.sendTranslated(POSITIVE, "Scaled the balance of every player by {decimal#factor}!", factor);
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
                return;
            }
            Account account = this.manager.getUserAccount(user, false);
            if (account == null)
            {
                context.sendTranslated(NEGATIVE, "{user} does not have an account!", user);
                return;
            }
            account.scale(factor);
            context.sendTranslated(POSITIVE, "Scaled the balance of {user} by {decimal#factor}!", user, factor);
        }
    }

    @Command(desc = "Hides the account of a given player",
             usage = "<player>|*",
             min = 1, max = 1)
    public void hide(ParameterizedContext context)
    {
        if ("*".equals(context.getString(0)))
        {
            this.manager.hideAll(true, false);
            return;
        }
        String[] users = StringUtils.explode(",", context.getString(0));
        for (String userString : users)
        {
            User user = this.module.getCore().getUserManager().findUser(userString);
            if (user == null)
            {
                context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getString(0));
                return;
            }
            Account target = this.manager.getUserAccount(user, false);
            if (target == null)
            {
                context.sendTranslated(NEGATIVE, "{user} does not have an account!", user);
                return;
            }
            boolean isHidden = target.isHidden();
            if (isHidden)
            {
                context.sendTranslated(NEUTRAL, "{user}'s account is already hidden!", user);
            }
            else
            {
                target.setHidden(true);
                context.sendTranslated(POSITIVE, "{user}'s account is now hidden!", user);
            }
        }
    }

    @Command(desc = "Unhides the account of a given player",
             usage = "<player>|*",
             min = 1, max = 1)
    public void unhide(ParameterizedContext context)
    {
        if ("*".equals(context.getString(0)))
        {
            this.manager.unhideAll(true, false);
            return;
        }
        String[] users = StringUtils.explode(",", context.getString(0));
        for (String userString : users)
        {
            User user = this.module.getCore().getUserManager().findUser(userString);
            if (user == null)
            {
                context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getString(0));
                return;
            }
            Account target = this.manager.getUserAccount(user, false);
            if (target == null)
            {
                context.sendTranslated(NEGATIVE, "{user} does not have an account!", user);
                return;
            }
            boolean isHidden = target.isHidden();

            if (isHidden)
            {
                target.setHidden(false);
                context.sendTranslated(POSITIVE, "{user}'s account is no longer hidden!", user);
            }
            else
            {
                context.sendTranslated(NEUTRAL, "{user}'s account was not hidden!", user);
            }
        }
    }

    @Command(desc = "Deletes a users account.",
             usage = "<player>",
             min = 1, max = 1)
    public void delete(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getString(0));
            return;
        }
        if (this.manager.deleteUserAccount(user))
        {
            context.sendTranslated(POSITIVE, "Deleted the account of {user}", user);
        }
        else
        {
            context.sendTranslated(NEUTRAL, "{user} did not have an account to delete!", user);
        }
    }

    @Command(desc = "Creates a new account",
             usage = "[player]",
             flags = @Flag(longName = "force", name = "f"),
             min = 0, max = 1)
    public void create(ParameterizedContext context)
    {
        if (context.hasArg(0))
        {
            if (!module.perms().ECO_CREATE_OTHER.isAuthorized(context.getSender()))
            {
                context.sendTranslated(NEGATIVE, "You are not allowed to create account for other users!");
                return;
            }
            User user = context.getUser(0);
            if (user == null)
            {
                if (module.perms().ECO_CREATE_FORCE.isAuthorized(context.getSender()))
                {
                    if (!context.hasFlag("f"))
                    {
                        context.sendTranslated(NEUTRAL, "{user} has never played on this server!", context.getString(0));
                        context.sendTranslated(POSITIVE, "Use the -force flag to create the account anyway.");
                        return;
                    }
                    else
                    {
                        user = this.module.getCore().getUserManager().findExactUser(context.getString(0));
                    }
                }
                else
                {
                    context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getString(0));
                    return;
                }
            }
            if (this.manager.getUserAccount(user, false) != null)
            {
                context.sendTranslated(POSITIVE, "{user} already has an account!", user);
                return;
            }
            this.manager.getUserAccount(user, true);
            context.sendTranslated(POSITIVE, "Created account for {user}!", user);
        }
        else if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            if (this.manager.getUserAccount(sender, false) != null)
            {
                context.sendTranslated(NEGATIVE, "You already have an account!");
                return;
            }
            this.manager.getUserAccount(sender, true);
            context.sendTranslated(POSITIVE, "Your account has now been created!");
        }
    }
}
