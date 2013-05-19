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
package de.cubeisland.cubeengine.conomy.commands;

import java.util.Set;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Alias;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.conomy.Conomy;
import de.cubeisland.cubeengine.conomy.ConomyPermissions;
import de.cubeisland.cubeengine.conomy.account.BankAccount;
import de.cubeisland.cubeengine.conomy.account.ConomyManager;

public class BankCommands extends ContainerCommand
{
    private final ConomyManager manager;
    public BankCommands(Conomy module)
    {
        super(module, "bank", "Manages your money in banks.");
        this.manager = module.getManager();
    }

    @Alias(names = "bbalance")
    @Command(desc = "Shows the balance of the specified bank",
             usage = "[name]",
             flags = @Flag(longName = "showHidden", name = "f"),
             max = 1)
    public void balance(CommandContext context) //Show all banks of given player
    {
        if (context.hasArg(0))
        {
            // TODO hidden
            // TODO show if hidden but member
            BankAccount bankAccount = this.manager.getBankAccount(context.getString(0), false);
            if (bankAccount == null)
            {
                context.sendTranslated("&cThere is no bank-account named &6%s&c!", context.getString(0));
            }
            else
            {
                context.sendTranslated("&aBank &6%s&a Balance: &6%s", bankAccount.getName(), this.manager.format(bankAccount.balance()));
            }
        }
        else if (context.getSender() instanceof User)
        {
            Set<BankAccount> bankAccounts = this.manager.getBankAccounts((User)context.getSender());
            if (bankAccounts.size() == 1)
            {
                BankAccount bankAccount = bankAccounts.iterator().next();
                context.sendTranslated("&aBank &6%s&a Balance: &6%s", bankAccount.getName(), this.manager.format(bankAccount.balance()));
                return;
            }
        }
        context.sendTranslated("&cPlease do specify the bank you want to show the balance of!");
    }

    public void list(CommandContext context) //Lists all banks [of given player]
    {
        // TODO
    }

    @Command(desc = "Invites a user to a bank",
             usage = "<user> [bank]",
             flags = @Flag(longName = "force", name = "f"),
             max = 2, min = 1)
    public void invite(ParameterizedContext context) //Invite a player to a bank
    {
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated("&cUser &2%s&c not found!", context.getString(0));
            return;
        }
        boolean force = context.hasFlag("f")
            && ConomyPermissions.COMMAND_BANK_INVITE_FORCE.isAuthorized(context.getSender());
        if (context.hasArg(1))
        {
            BankAccount account = this.getBankAccount(context.getString(1));
            if (account == null)
            {
                context.sendTranslated("&cThere is no bank-account named &6%s&c!", context.getString(1));
                return;
            }
            if (!account.needsInvite())
            {
                context.sendTranslated("&eThis bank does not need an invite to be able to join!");
                return;
            }
            if (force || !(context.getSender() instanceof User) || account.hasAccess((User)context.getSender()))
            {
                account.invite(user);
                context.sendTranslated("&aYou invited &2%s&a to the bank &6%s&a!", user.getName(), account.getName());
                return;
            }
            context.sendTranslated("&cYou are not allowed to invite a player to this bank.");
            return;
        }
        if (context.getSender() instanceof User)
        {
            Set<BankAccount> bankAccounts = this.manager.getBankAccounts((User)context.getSender());
            if (bankAccounts.size() == 1)
            {
                BankAccount account = bankAccounts.iterator().next();
                if (!account.needsInvite())
                {
                    context.sendTranslated("&eThis bank does not need an invite to be able to join!");
                    return;
                }
                if (force || account.hasAccess((User)context.getSender()))
                {
                    account.invite(user);
                    context.sendTranslated("&aYou invited &2%s&a to the bank &6%s&a!", user.getName(), account.getName());
                    return;
                }
                context.sendTranslated("&cYou are not allowed to invite a player to this bank.");
                return;
            }
        }
        context.sendTranslated("&cPlease do specify the bank you want to invite to!");
    }

    @Command(desc = "Joins a bank",
             usage = "<bank> [user]",
             // TODO force
             max = 2, min = 1)
    public void join(CommandContext context) //Join a bank |  <bank> [player]
    {
        User user;
        boolean other = false;
        if (context.hasArg(1))
        {
            if (!ConomyPermissions.COMMAND_BANK_JOIN_OTHER.isAuthorized(context.getSender()))
            {
                context.sendTranslated("&cYou are not allowed to let someone else join a bank!");
                return;
            }
            user = context.getUser(1);
            if (user == null)
            {
                context.sendTranslated("&cUser &2%s&c not found!", context.getString(1));
                return;
            }
            other = true;
        }
        else if (context.getSender() instanceof User)
        {
            user = (User)context.getSender();
        }
        else
        {
            context.sendTranslated("&cPlease specify a player to join!");
            return;
        }
        BankAccount account = this.getBankAccount(context.getString(0));
        if (account == null)
        {
            context.sendTranslated("&cThere is no bank-account named &6%s&c!", context.getString(0));
            return;
        }
        if (account.isOwner(user))
        {
            if (other)
            {
                context.sendTranslated("&2&s&c is already owner of this bank!", user.getName());
                return;
            }
            context.sendTranslated("&cYou are already owner of this bank!");
            return;
        }
        if (account.isMember(user))
        {
            if (other)
            {
                context.sendTranslated("&2&s&c is already member of this bank!", user.getName());
                return;
            }
            context.sendTranslated("&cYou are already member of this bank!");
            return;
        }
        if (account.needsInvite() && !account.isInvited(user))
        {
            if (other)
            {
                context.sendTranslated("&2&s&c needs to be invited to join this bank!", user.getName());
                return;
            }
            context.sendTranslated("&cYou need to be invited to join this bank!");
            return;
        }
        account.promoteToMember(user);
        context.sendTranslated("&2%s&a is now a member of the &6%s&a bank!", user.getName(), account.getName());
    }

    @Command(desc = "Leaves a bank",
             usage = "[bank] [user]",
             max = 2, min = 0)
    public void leave(CommandContext context)
    {
        if (context.hasArg(0))
        {
            User user;
            boolean other = false;
            if (context.hasArg(1))
            {
                if (!ConomyPermissions.COMMAND_BANK_LEAVE_OTHER.isAuthorized(context.getSender()))
                {
                    context.sendTranslated("&cYou are not allowed to let someone else leave a bank!");
                    return;
                }
                user = context.getUser(1);
                if (user == null)
                {
                    context.sendTranslated("&cUser &2%s&c not found!", context.getString(1));
                    return;
                }
                other = true;
            }
            else if (context.getSender() instanceof User)
            {
                user = (User)context.getSender();
            }
            else
            {
                context.sendTranslated("&cPlease specify a player to leave!");
                return;
            }
            BankAccount account;
            if (context.hasArg(0))
            {
                account = this.getBankAccount(context.getString(0));
                if (account == null)
                {
                    context.sendTranslated("&cThere is no bank-account named &6%s&c!", context.getString(0));
                    return;
                }
            }
            else
            {
                Set<BankAccount> bankAccounts = this.manager.getBankAccounts(user);
                if (bankAccounts.size() == 1)
                {
                    account = bankAccounts.iterator().next();
                }
                else
                {
                    context.sendTranslated("&cPlease do specify a bank-account to leave");
                    return;
                }
            }
            if (account.hasAccess(user))
            {
                account.kickUser(user);
                if (other)
                {
                    context.sendTranslated("&2%s&a is no longer a member of the bank &6%s&a!", user.getName(), account.getName());
                    return;
                }
                context.sendTranslated("&aYou are no longer a member of the bank &6%s&a!", account.getName());
                return;
            }
            if (other)
            {
                context.sendTranslated("&2&s&c is not a member of that bank!", user.getName());
                return;
            }
            context.sendTranslated("&cYou are not a member if that bank!");
        }
    }

    public void uninvite(CommandContext context)
    {
        // TODO reject invite / uninvite
    }

    @Command(desc = "Creates a new bank",
             usage = "<name> [-nojoin]",
             flags = @Flag(longName = "nojoin", name = "nj"),
             max = 1, min = 1)
    public void create(ParameterizedContext context)
    {
        if (this.manager.bankAccountExists(context.getString(0)))
        {
            context.sendTranslated("&cThere is already a bank names &6%2&c!", context.getString(0));
        }
        else
        {
            BankAccount bankAccount = this.manager.getBankAccount(context.getString(0), true);
            if (context.getSender() instanceof User && !context.hasFlag("nj"))
            {
                bankAccount.promoteToOwner((User)context.getSender());
            }
            context.sendTranslated("&aCreated new Bank &6%s&a!", bankAccount.getName());
        }
    }

    public void delete(CommandContext context)//Deletes a bank and all its data 
    {} // TODO

    public void rename(CommandContext context)//renames bank
    {}// TODO

    @Command(desc = "Sets given user as owner for a bank",
             usage = "<bank-name> <user>",
             flags = @Flag(longName = "force", name = "f"),
             max = 2, min = 2)
    public void setOwner(ParameterizedContext context)
    {
        User user = context.getUser(1);
        if (user == null)
        {
            context.sendTranslated("&cUser &2%s&c not found!", context.getString(1));
            return;
        }
        BankAccount account = this.getBankAccount(context.getString(0));
        if (account == null)
        {
            context.sendTranslated("&cThere is no bank-account named &6%s&c!", context.getString(0));
            return;
        }
        boolean force = context.hasFlag("f") && ConomyPermissions.COMMAND_BANK_SETOWNER_FORCE.isAuthorized(context.getSender());
        if (force || context.getSender() instanceof User)
        {
            if (!account.isOwner((User)context.getSender()))
            {
                context.sendTranslated("&cYou are not allowed to set an owner for this bank!");
                return;
            }
        }
        account.promoteToOwner(user);
        context.sendTranslated("&2%s&a is now owner of the bank &6%s&a!", user.getName(), account.getName());
    }

    // TODO listinvites

    public void listmembers(CommandContext context)//list all members with their rank
    {}// TODO

    public void deposit(CommandContext context)//puts your money into the bank
    {}// TODO

    public void withdraw(CommandContext context)//takes money from the bank
    {}// TODO

    public void pay(CommandContext context)//pay AS bank to a player or other bank <name> [-bank]
    {}// TODO

    private BankAccount getBankAccount(String name)
    {
        return this.manager.getBankAccount(name, false);
    }
}
