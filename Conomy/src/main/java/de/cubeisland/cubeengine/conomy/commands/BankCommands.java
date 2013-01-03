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
    {
    }

    public void list(CommandContext context) //Lists all banks [of given player]
    {
    }

    public void invite(CommandContext context) //Invite a player to a bank 
    {
    }

    public void join(CommandContext context) //Join a bank |  <bank> [player]
    {
    }

    public void leave(CommandContext context)//Leave a bank |  [bank](needed if not only in 1 bank) [player]
    {
    }

    public void create(CommandContext context)//Creates a new Bank and joins / flag for not joining
    {
    }

    public void delete(CommandContext context)//Deletes a bank and all its data 
    {
    }

    public void rename(CommandContext context)//renames bank
    {
    }

    public void setRank(CommandContext context)//Sets the rank of a player in a bank
    //owner / moderator / full-member / member
    //in&out&manage&delete / in&out&manage / in&out / in
    {
    }

    public void listmembers(CommandContext context)//list all members with their rank
    {
    }

    public void deposit(CommandContext context)//puts your money into the bank
    {
    }

    public void withdraw(CommandContext context)//takes money from the bank
    {
    }

    public void pay(CommandContext context)//pay AS bank to a player or other bank <name> [-bank]
    {
    }
}
