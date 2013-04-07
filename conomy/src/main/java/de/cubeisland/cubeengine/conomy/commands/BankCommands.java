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

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.module.Module;

public class BankCommands extends ContainerCommand
{
    // perhaps do /bank info ... commands if this gets to much in here

    public BankCommands(Module module)
    {
        super(module, "bank", "Manages your money in banks.");
    }

    //admin flag to always allow
    //TODO extra table for who is allowed to acces a bank   

    public void balance(CommandContext context) //Show all banks of given player
    {}

    public void list(CommandContext context) //Lists all banks [of given player]
    {}

    public void invite(CommandContext context) //Invite a player to a bank 
    {}

    public void join(CommandContext context) //Join a bank |  <bank> [player]
    {}

    public void leave(CommandContext context)//Leave a bank |  [bank](needed if not only in 1 bank) [player]
    {}

    public void create(CommandContext context)//Creates a new Bank and joins / flag for not joining
    {}

    public void delete(CommandContext context)//Deletes a bank and all its data 
    {}

    public void rename(CommandContext context)//renames bank
    {}

    public void setRank(CommandContext context)//Sets the rank of a player in a bank
    //owner / moderator / full-member / member
    //in&out&manage&delete / in&out&manage / in&out / in
    {}

    public void listmembers(CommandContext context)//list all members with their rank
    {}

    public void deposit(CommandContext context)//puts your money into the bank
    {}

    public void withdraw(CommandContext context)//takes money from the bank
    {}

    public void pay(CommandContext context)//pay AS bank to a player or other bank <name> [-bank]
    {}

    /* from pay
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
     */
}
