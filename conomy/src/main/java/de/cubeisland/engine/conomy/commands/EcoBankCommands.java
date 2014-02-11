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
import de.cubeisland.engine.conomy.account.BankAccount;
import de.cubeisland.engine.conomy.account.ConomyManager;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.StringUtils;

public class EcoBankCommands extends ContainerCommand
{
    private final Conomy module;
    private final ConomyManager manager;

    public EcoBankCommands(Conomy module)
    {
        super(module, "bank", "Administrative commands for Conomy-Banks.");
        this.module = module;
        this.manager = module.getManager();
    }

    @Command(names = {"give", "grant"},
             desc = "Gives money to a bank or all banks",
             usage = "<bank>|* <amount>",
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
            this.manager.transactionAll(false, true, amount);
            context.sendTranslated("&aYou gave &6%s&a to every bank!", format);
        }
        else
        {
            String[] banks = StringUtils.explode(",", context.getString(0));
            for (String bankString : banks)
            {
                BankAccount target = this.manager.getBankAccount(bankString, false);
                if (target == null)
                {
                    context.sendTranslated("&cThere is no bank account named &6%s!", bankString);
                    continue;
                }
                this.manager.transaction(null, target, amount, true);
                context.sendTranslated("&aYou gave &6%s&a to the bank &6%s&a!", format, bankString);
                for (User user : this.module.getCore().getUserManager().getOnlineUsers())
                {
                    if (target.isOwner(user))
                    {
                        user.sendTranslated("&2%s&a granted &6%s&a to your bank &6%s&a!", context.getSender().getName(), format, bankString);
                    }
                }
            }
        }
    }

    @Command(names = {"take", "remove"},
             desc = "Takes money from given bank or all banks",
             usage = "<bank>|* <amount>",
             min = 2, max = 2)
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
            this.manager.transactionAll(false, true, -amount);
            context.sendTranslated("&aYou took &6%s&a from every bank!", format);
        }
        else
        {
            String[] banks = StringUtils.explode(",", context.getString(0));
            for (String bankString : banks)
            {
                BankAccount target = this.manager.getBankAccount(bankString, false);
                if (target == null)
                {
                    context.sendTranslated("&cThere is no bank account named &6%s!", bankString);
                    return;
                }
                this.manager.transaction(target, null, amount, true);
                context.sendTranslated("&aYou took &6%s&a from the bank &6%s&a!", format, bankString);
                for (User onlineUser : this.module.getCore().getUserManager().getOnlineUsers())
                {
                    if (target.isOwner(onlineUser))
                    {
                        onlineUser.sendTranslated("&2%s&a charged your bank &6%s&a for &6%s&a!", context.getSender().getName(), bankString, format);
                    }
                }
            }
        }
    }

    @Command(desc = "Reset the money from given banks",
             usage = "<bank>|*",
             min = 1, max = 1)
    public void reset(ParameterizedContext context)
    {
        if (context.getString(0).equalsIgnoreCase("*"))
        {
            this.manager.setAll(false, true, this.manager.getDefaultBankBalance());
            context.sendTranslated("&aYou resetted every bank account!");
        }
        else
        {
            String[] banks = StringUtils.explode(",", context.getString(0));
            for (String bankString : banks)
            {
                BankAccount target = this.manager.getBankAccount(bankString, false);
                if (target == null)
                {
                    context.sendTranslated("&cThere is no bank account named &6%s!", bankString);
                    return;
                }
                target.reset();
                String format = this.manager.format(this.manager.getDefaultBalance());
                context.sendTranslated("The account of the bank &6%s&a got reset to &6%s&a!", bankString, format);
                for (User onlineUser : this.module.getCore().getUserManager().getOnlineUsers())
                {
                    if (target.isOwner(onlineUser))
                    {
                        onlineUser.sendTranslated("&2%s&a resetted the money of your bank &6%s&a to &6%s&a!", context.getSender().getName(), bankString, format);
                    }
                }
            }
        }
    }

    @Command(desc = "Sets the money from given banks",
             usage = "<bank>|* <amount>",
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
            this.manager.setAll(false, true, amount);
            context.sendTranslated("&aYou have set every bank account to &6%s&a!", format);
        }
        else
        {
            String[] banks = StringUtils.explode(",", context.getString(0));
            for (String bankString : banks)
            {
                BankAccount target = this.manager.getBankAccount(bankString, false);
                if (target == null)
                {
                    context.sendTranslated("&cThere is no bank account named &6%s!", bankString);
                    return;
                }
                target.set(amount);
                context.sendTranslated("The account of the bank &6%s&a got set to &6%s&a!", bankString, format);
                for (User onlineUser : this.module.getCore().getUserManager().getOnlineUsers())
                {
                    if (target.isOwner(onlineUser))
                    {
                        onlineUser.sendTranslated("&2%s&a set the money of your bank &6%s&a to &6%s&a!", context.getSender().getName(), bankString, format);
                    }
                }
            }
        }
    }

    @Command(desc = "Scales the money from given banks",
             usage = "<bank>|* <factor>",
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
            this.manager.scaleAll(false, true, factor);
            context.sendTranslated("&aScaled the balance of every bank by &6%f&a!", factor);
            return;
        }
        String[] banks = StringUtils.explode(",", context.getString(0));
        for (String bankString : banks)
        {
            BankAccount account = this.manager.getBankAccount(bankString, false);
            if (account == null)
            {
                context.sendTranslated("&cThere is no bank account named &6%s!", bankString);
                return;
            }
            account.scale(factor);
            context.sendTranslated("&aScaled the balance of the bank &6%s&a by &6%f&a!", bankString, factor);
            for (User onlineUser : this.module.getCore().getUserManager().getOnlineUsers())
            {
                if (account.isOwner(onlineUser))
                {
                    onlineUser.sendTranslated("&2%s&a scaled the money of your bank &6%s&a by &6%f&a!", context.getSender().getName(), bankString, factor);
                }
            }
        }
    }

    @Command(desc = "Hides the account of given bank",
             usage = "<bank>|*",
             min = 1, max = 1)
    public void hide(ParameterizedContext context)
    {
        if (context.getString(0).equals("*"))
        {
            this.manager.hideAll(false, true);
            return;
        }
        String[] banks = StringUtils.explode(",", context.getString(0));
        for (String bankString : banks)
        {
            BankAccount target = this.manager.getBankAccount(bankString, false);
            if (target == null)
            {
                context.sendTranslated("&cThere is no bank account named &6%s!", bankString);
                return;
            }
            if (target.isHidden())
            {
                context.sendTranslated("&aThe bank &6%s&a is already hidden!", bankString);
            }
            else
            {
                target.setHidden(true);
                context.sendTranslated("&aThe bank &6%s&a is now hidden!", bankString);
            }
        }
    }

    @Command(desc = "Unhides the account of given banks",
             usage = "<bank>|*",
             min = 1, max = 1)
    public void unhide(ParameterizedContext context)
    {
        if (context.getString(0).equals("*"))
        {
            this.manager.unhideAll(false, true);
            return;
        }
        String[] banks = StringUtils.explode(",", context.getString(0));
        for (String bankString : banks)
        {
            BankAccount target = this.manager.getBankAccount(bankString, false);
            if (target == null)
            {
                context.sendTranslated("&cThere is no bank account named &6%s!", bankString);
                return;
            }
            if (target.isHidden())
            {
                target.setHidden(false);
                context.sendTranslated("&aThe bank &6%s&a is no longer hidden!", bankString);
            }
            else
            {
                context.sendTranslated("&aThe bank &6%s&a was not hidden!", bankString);
            }
        }
    }
}
