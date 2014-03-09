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
import de.cubeisland.engine.core.util.formatter.MessageType;

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
            context.sendTranslated(MessageType.NEGATIVE, "Could not parse amount! {input}", amountString);
            return;
        }
        String format = manager.format(amount);
        if (context.getString(0).equalsIgnoreCase("*"))
        {
            this.manager.transactionAll(false, true, amount);
            context.sendTranslated(MessageType.POSITIVE, "You gave {input#amount} to every bank!", format);
        }
        else
        {
            String[] banks = StringUtils.explode(",", context.getString(0));
            for (String bankString : banks)
            {
                BankAccount target = this.manager.getBankAccount(bankString, false);
                if (target == null)
                {
                    context.sendTranslated(MessageType.NEGATIVE, "There is no bank account named {input#name}!", bankString);
                    continue;
                }
                this.manager.transaction(null, target, amount, true);
                context.sendTranslated(MessageType.POSITIVE, "You gave {input#amount} to the bank {input#bank}!", format, bankString);
                for (User user : this.module.getCore().getUserManager().getOnlineUsers())
                {
                    if (target.isOwner(user))
                    {
                        user.sendTranslated(MessageType.POSITIVE, "{user} granted {input#amount} to your bank {input#bank}!", context.getSender(), format, bankString);
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
            context.sendTranslated(MessageType.NEGATIVE, "Could not parse amount!");
            return;
        }
        String format = manager.format(amount);
        if (context.getString(0).equalsIgnoreCase("*"))
        {
            this.manager.transactionAll(false, true, -amount);
            context.sendTranslated(MessageType.POSITIVE, "You took {input#amount} from every bank!", format);
        }
        else
        {
            String[] banks = StringUtils.explode(",", context.getString(0));
            for (String bankString : banks)
            {
                BankAccount target = this.manager.getBankAccount(bankString, false);
                if (target == null)
                {
                    context.sendTranslated(MessageType.NEGATIVE, "There is no bank account named {input#bank}!", bankString);
                    return;
                }
                this.manager.transaction(target, null, amount, true);
                context.sendTranslated(MessageType.POSITIVE, "You took {input#amount} from the bank {input#bank}!", format, bankString);
                for (User onlineUser : this.module.getCore().getUserManager().getOnlineUsers())
                {
                    if (target.isOwner(onlineUser))
                    {
                        onlineUser.sendTranslated(MessageType.POSITIVE, "{user} charged your bank {input#bank} for {input#amount}!", context.getSender(), bankString, format);
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
            context.sendTranslated(MessageType.POSITIVE, "You resetted every bank account!");
        }
        else
        {
            String[] banks = StringUtils.explode(",", context.getString(0));
            for (String bankString : banks)
            {
                BankAccount target = this.manager.getBankAccount(bankString, false);
                if (target == null)
                {
                    context.sendTranslated(MessageType.NEGATIVE, "There is no bank account named {input#bank}!", bankString);
                    return;
                }
                target.reset();
                String format = this.manager.format(this.manager.getDefaultBalance());
                context.sendTranslated(MessageType.POSITIVE, "The account of the bank {input#bank} got reset to {input#balance}!", bankString, format);
                for (User onlineUser : this.module.getCore().getUserManager().getOnlineUsers())
                {
                    if (target.isOwner(onlineUser))
                    {
                        onlineUser.sendTranslated(MessageType.POSITIVE, "{user} resetted the money of your bank {input#bank} to {input#balance}!", context.getSender(), bankString, format);
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
            context.sendTranslated(MessageType.NEGATIVE, "Could not parse amount!");
            return;
        }
        String format = this.manager.format(amount);
        if (context.getString(0).equalsIgnoreCase("*"))
        {
            this.manager.setAll(false, true, amount);
            context.sendTranslated(MessageType.POSITIVE, "You have set every bank account to {input#balance}!", format);
        }
        else
        {
            String[] banks = StringUtils.explode(",", context.getString(0));
            for (String bankString : banks)
            {
                BankAccount target = this.manager.getBankAccount(bankString, false);
                if (target == null)
                {
                    context.sendTranslated(MessageType.NEGATIVE, "There is no bank account named {input#bank}!", bankString);
                    return;
                }
                target.set(amount);
                context.sendTranslated(MessageType.POSITIVE, "The account of the bank {input#bank} got set to {input#balance}!", bankString, format);
                for (User onlineUser : this.module.getCore().getUserManager().getOnlineUsers())
                {
                    if (target.isOwner(onlineUser))
                    {
                        onlineUser.sendTranslated(MessageType.POSITIVE, "{user} set the money of your bank {input#bank} to {input#balance}!", context.getSender(), bankString, format);
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
            context.sendTranslated(MessageType.NEGATIVE, "Invalid factor: {input#factor}", context.getString(1));
            return;
        }
        if (context.getString(0).equals("*"))
        {
            this.manager.scaleAll(false, true, factor);
            context.sendTranslated(MessageType.POSITIVE, "Scaled the balance of every bank by {decimal#factor}!", factor);
            return;
        }
        String[] banks = StringUtils.explode(",", context.getString(0));
        for (String bankString : banks)
        {
            BankAccount account = this.manager.getBankAccount(bankString, false);
            if (account == null)
            {
                context.sendTranslated(MessageType.NEGATIVE, "There is no bank account named {input#bank}!", bankString);
                return;
            }
            account.scale(factor);
            context.sendTranslated(MessageType.POSITIVE, "Scaled the balance of the bank {input#bank} by {decimal#factor}!", bankString, factor);
            for (User onlineUser : this.module.getCore().getUserManager().getOnlineUsers())
            {
                if (account.isOwner(onlineUser))
                {
                    onlineUser.sendTranslated(MessageType.POSITIVE, "{user} scaled the money of your bank {input#bank} by {decimal#factor}", context.getSender().getName(), bankString, factor);
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
                context.sendTranslated(MessageType.NEGATIVE, "There is no bank account named {input#bank}!", bankString);
                return;
            }
            if (target.isHidden())
            {
                context.sendTranslated(MessageType.POSITIVE, "The bank {input#bank} is already hidden!", bankString);
            }
            else
            {
                target.setHidden(true);
                context.sendTranslated(MessageType.POSITIVE, "The bank {input#bank} is now hidden!", bankString);
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
                context.sendTranslated(MessageType.NEGATIVE, "There is no bank account named {input#bank}!", bankString);
                return;
            }
            if (target.isHidden())
            {
                target.setHidden(false);
                context.sendTranslated(MessageType.POSITIVE, "The bank {input#bank} is no longer hidden!", bankString);
            }
            else
            {
                context.sendTranslated(MessageType.POSITIVE, "The bank {input#bank} was not hidden!", bankString);
            }
        }
    }
}
