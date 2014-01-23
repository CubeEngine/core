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

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.conomy.Conomy;
import de.cubeisland.engine.conomy.account.Account;
import de.cubeisland.engine.conomy.account.ConomyManager;

public class EcoCommands extends ContainerCommand
{
    private Conomy module;
    private ConomyManager manager;

    public EcoCommands(Conomy module)
    {
        super(module, "eco", "Administrative commands for Conomy.");
        this.module = module;
        this.manager = module.getManager();
    }

    @Command(names = {"give", "grant"},
             desc = "Gives money to given user or all [online] users",
             usage = "<player>|* <amount> [-o]",
             flags = @Flag(longName = "online", name = "o"),
             min = 2, max = 2)
    public void give(ParameterizedContext context)
    {
        String amountString = context.getString(1);
        Double amount = this.manager.parse(amountString, context.getSender().getLocale());
        if (amount == null)
        {
            context.sendTranslated("&cCould not parse amount! %s", amountString);
            return;
        }
        String format = manager.format(amount);
        if (context.getString(0).equalsIgnoreCase("*"))
        {
            if (context.hasFlag("o"))
            {
                if (this.manager.transactionAllOnline(amount))
                {
                    context.sendTranslated("&aYou gave &6%s&a to every online user!", format);
                }
                else
                {
                    context.sendTranslated("&cPlease try again!");
                }
            }
            else
            {
                this.manager.transactionAll(true, false, amount);
                context.sendTranslated("&aYou gave &6%s&a to every user!", format);
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
                    context.sendTranslated("&cUser %s not found!", context.getString(0));
                    continue;
                }
                Account target = this.manager.getUserAccount(user, false);
                if (target == null)
                {
                    context.sendTranslated("&2%s &cdoes not have an account!", user.getName());
                    continue;
                }
                if (this.manager.transaction(null, target, amount, true))
                {
                    context.sendTranslated("&aYou gave &6%s&a to &2%s&a!", format, user.getName());
                    if (!context.getSender().getName().equals(user.getName()))
                    {
                        user.sendTranslated("&aYou were granted &6%s&a.", format);
                    }
                }
                else
                {
                    context.sendTranslated("&cCould not give the money to &2&s&c!", user.getName());
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
            context.sendTranslated("&cCould not parse amount!");
            return;
        }
        String format = manager.format(amount);
        if (context.getString(0).equalsIgnoreCase("*"))
        {
            if (context.hasFlag("o"))
            {
                if (this.manager.transactionAllOnline(-amount))
                {
                    context.sendTranslated("&aYou took &6%s&a from every online user!", format);
                }
                else
                {
                    context.sendTranslated("&cPlease try again!");
                }
            }
            else
            {
                this.manager.transactionAll(true, false, -amount);
                context.sendTranslated("&aYou took &6%s&a from every user!", format);
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
                    context.sendTranslated("&cUser %s not found!", context.getString(0));
                    return;
                }
                Account target = this.manager.getUserAccount(user, false);
                if (target == null)
                {
                    context.sendTranslated("&2%s &cdoes not have an account!", user.getName());
                    return;
                }
                this.manager.transaction(target, null, amount, true);
                context.sendTranslated("&aYou took &6%s &afrom &2%s&a!", format, user.getName());
                if (!context.getSender().getName().equals(user.getName()))
                {
                    user.sendTranslated("&eWithdrawed &6%s &efrom your account.", format);
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
        if (context.getString(0).equalsIgnoreCase("*"))
        {
            if (context.hasFlag("o"))
            {
                if (this.manager.setAllOnline(this.manager.getDefaultBalance()))
                {
                    context.sendTranslated("&aYou resetted every online user account!");
                }
                else
                {
                    context.sendTranslated("&cPlease try again!");
                }
            }
            else
            {
                this.manager.setAll(true, false, this.manager.getDefaultBalance());
                context.sendTranslated("&aYou resetted every user account!");
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
                    context.sendTranslated("&cUser %s not found!", context.getString(0));
                    return;
                }
                Account target = this.manager.getUserAccount(user, false);
                if (target == null)
                {
                    context.sendTranslated("&2%s &cdoes not have an account!",
                                           user.getName());
                    return;
                }
                target.reset();
                String format = this.manager.format(this.manager.getDefaultBalance());
                context.sendTranslated("&2%s &aaccount reset to &6%s&a!", user.getName(), format);
                if (!context.getSender().getName().equals(user.getName()))
                {
                    user.sendTranslated("&eYour balance got resetted to &6%s&e.", format);
                }
            }
        }
    }

    @Command(desc = "Sets the money from given user",
             usage = "<player>|* <amount> [-o]",
             flags = @Flag(longName = "online", name = "o"),
             min = 2, max = 2)
    public void set(ParameterizedContext context)
    {
        String amountString = context.getString(1);
        Double amount = manager.parse(amountString, context.getSender().getLocale());
        if (amount == null)
        {
            context.sendTranslated("&cCould not parse amount!");
            return;
        }
        String format = this.manager.format(amount);
        if (context.getString(0).equalsIgnoreCase("*"))
        {
            if (context.hasFlag("o"))
            {
                if (this.manager.setAllOnline(amount))
                {
                    context.sendTranslated("&aYou have set every online user account to &6%s&a!", format);
                }
                else
                {
                    context.sendTranslated("&cPlease try again!");
                }
            }
            else
            {
                this.manager.setAll(true, false, amount);
                context.sendTranslated("&aYou have set every user account to &6%s&a!", format);
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
                    context.sendTranslated("&cUser %s not found!", context.getString(0));
                    return;
                }
                Account target = this.manager.getUserAccount(user, false);
                if (target == null)
                {
                    context.sendTranslated("&2%s &cdoes not have an account!",
                                           user.getName());
                    return;
                }
                target.set(amount);
                context.sendTranslated("&2%s &aaccount set to &6%s&a!", user.getName(), format);
                if (!context.getSender().getName().equals(user.getName()))
                {
                    user.sendTranslated("&eYour balance got set to &6%s&e.", format);
                }
            }
        }
    }

    @Command(desc = "Scales the money from given users",
             usage = "<player>|* <factor> [-o]",
             flags = @Flag(longName = "online", name = "o"),
             min = 2, max = 2)
    public void scale(ParameterizedContext context)
    {
        Float factor = context.getArg(1, Float.class, null);
        if (factor == null)
        {
            context.sendTranslated("&cInvalid factor: &6%s",context.getString(1));
            return;
        }
        if (context.getString(0).equals("*"))
        {
            if (context.hasFlag("o"))
            {
                if (this.manager.scaleAllOnline(factor))
                {
                    context.sendTranslated("&aScaled the balance of every online user by &6%f&a!", factor);
                }
                else
                {
                    context.sendTranslated("&cPlease try again!");
                }
            }
            else
            {
                this.manager.scaleAll(true, false, factor);
                context.sendTranslated("&aScaled the balance of every user by &6%f&a!", factor);
            }
            return;
        }
        String[] users = StringUtils.explode(",", context.getString(0));
        for (String userString : users)
        {
            User user = this.module.getCore().getUserManager().findUser(userString);
            if (user == null)
            {
                context.sendTranslated("&cUser %s not found!", context.getString(0));
                return;
            }
            Account account = this.manager.getUserAccount(user, false);
            if (account == null)
            {
                context.sendTranslated("&2%s&c does not have an account!", user.getName());
                return;
            }
            account.scale(factor);
            context.sendTranslated("&aScaled the balance of &2%s&a by &6%f&a!", user.getName(), factor);
        }
    }

    @Command(desc = "Hides the account of given player",
             usage = "<player>|*",
             min = 1, max = 1)
    public void hide(ParameterizedContext context)
    {
        if (context.getString(0).equals("*"))
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
                context.sendTranslated("&cUser %s not found!", context.getString(0));
                return;
            }
            Account target = this.manager.getUserAccount(user, false);
            if (target == null)
            {
                context.sendTranslated("&2%s&c does not have an account!", user.getName());
                return;
            }
            boolean isHidden = target.isHidden();
            if (isHidden)
            {
                context.sendTranslated("&2%s's&e account is already hidden!", user.getName());
            }
            else
            {
                target.setHidden(true);
                context.sendTranslated("&2%s's&a account is now hidden!", user.getName());
            }
        }
    }

    @Command(desc = "Unhides the account of given player",
             usage = "<player>|*",
             min = 1, max = 1)
    public void unhide(ParameterizedContext context)
    {
        if (context.getString(0).equals("*"))
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
                context.sendTranslated("&cUser %s not found!", context.getString(0));
                return;
            }
            Account target = this.manager.getUserAccount(user, false);
            if (target == null)
            {
                context.sendTranslated("&2%s &cdoes not have an account!",user.getName());
                return;
            }
            boolean isHidden = target.isHidden();

            if (isHidden)
            {
                target.setHidden(false);
                context.sendTranslated("&2%s's&a account is no longer hidden!", user.getName());
            }
            else
            {
                context.sendTranslated("&2%s's&e account was not hidden!", user.getName());
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
            context.sendTranslated("&cUser %s not found!", context.getString(0));
            return;
        }
        if (this.manager.deleteUserAccount(user))
        {
            context.sendTranslated("&aDeleted the account of &2%s", user.getName());
        }
        else
        {
            context.sendTranslated("&2%s&e did not have an account to delete!", user.getName());
        }
    }

    @Command(desc = "Creates a new user-account",
             usage = "[player]",
             flags = @Flag(longName = "force", name = "f"),
             min = 0, max = 1)
    public void create(ParameterizedContext context)
    {
        if (context.hasArg(0))
        {
            if (!module.perms().ECO_CREATE_OTHER.isAuthorized(context.getSender()))
            {
                context.sendTranslated("&cYou are not allowed to create account for other users!");
                return;
            }
            User user = context.getUser(0);
            if (user == null)
            {
                if (module.perms().ECO_CREATE_FORCE.isAuthorized(context.getSender()))
                {
                    if (!context.hasFlag("f"))
                    {
                        context.sendTranslated("&2%s&e has never played on this server!", context.getString(0));
                        context.sendTranslated("&aUse the -force flag to create the account anyways.");
                        return;
                    }
                    else
                    {
                        user = this.module.getCore().getUserManager().getUser(context.getString(0), true);
                    }
                }
                else
                {
                    context.sendTranslated("&cUser %s not found!", context.getString(0));
                    return;
                }
            }
            if (this.manager.getUserAccount(user, false) != null)
            {
                context.sendTranslated("&2%s&a already had an account!", user.getName());
                return;
            }
            this.manager.getUserAccount(user, true);
            context.sendTranslated("&aCreated account for &2%s&a!", user.getName());
        }
        else if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            if (this.manager.getUserAccount(sender, false) != null)
            {
                context.sendTranslated("You already had an account!");
                return;
            }
            this.manager.getUserAccount(sender, true);
            context.sendTranslated("&aYour account got created!");
        }
    }
}
