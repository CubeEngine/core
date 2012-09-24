/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cubeisland.cubeengine.fun.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.fun.Fun;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Wolfi
 */
public class FunCommands 
{
    Fun module;
    
    public FunCommands(Fun module) 
    {
        this.module = module;
    }
    
    @Command(
        names = {"lightning", "strike"},
        desc = "strucks a player by lightning.",
        min = 1,
        max = 1,
        usage = "/lightning <player>"
    )
    public void lightning(CommandContext context)
    {
        CommandSender sender = context.getSender();
        User user = context.getUser(0);
        
        if(user == null)
        {
            sender.sendMessage("Do not know a player with that name");
            return;
        }
        
        user.getWorld().strikeLightning(user.getLocation());
    }
    
    @Command(
        desc = "The CommandSender throws a certain amount of snowballs. Default is one.",
        max = 1,
        usage = "/snowball [amount]"
    )
    public void snowball(CommandContext context)
    {
        if(!(context.getSender() instanceof Player))
        {
            context.getSender().sendMessage("baehmm in your face");
            return;
        }
        
        User user = this.module.getUserManager().getUser(context.getSender());
        int amount = context.getIndexed(0, Integer.class, 1);
        
        user.sendMessage("In the future you will throw " + amount + " snowballs");
    }
    
}
