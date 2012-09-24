/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cubeisland.cubeengine.fun.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.fun.Fun;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Wolfi
 */
public class FunCommands 
{
    Fun module;
    UserManager userManager;
    
    public FunCommands(Fun module) 
    {
        this.userManager = module.getUserManager();
        this.module = module;
    }
    
    @Command(
        names = {"lightning", "strike"},
        desc = "strucks a player by lightning.",
        max = 1,
        usage = "/lightning <player>"
    )
    public void lightning(CommandContext context)
    {
        CommandSender sender = context.getSender();
        User user = this.userManager.getUser(sender);
        Location location = null;
        
        if(context.getIndexed().isEmpty() && sender instanceof Player)
        {
            location = user.getTargetBlock(null, 200).getLocation();
        }
        else if(!context.getIndexed().isEmpty())
        {
            user = context.getUser(0);
            
            if(user == null)
            {
                context.getSender().sendMessage("Do not know a player with that name");
                return;
            }
            
            location = user.getLocation();
        }
        else 
        {
            sender.sendMessage("This command can only be used by a player!");
            return;
        }
        
        user.getWorld().strikeLightning(location); 
    }
    
    @Command(
        names = {"throw"},
        desc = "The CommandSender throws a certain amount of snowballs. Default is one.",
        min = 1,
        max = 2,
        usage = "/throw <egg|snowball> [amount]"
    )
    public void throwItem(CommandContext context)
    {
        User user = this.userManager.getUser(context.getSender());
        
        if(user == null)
        {
            context.getSender().sendMessage("This command can only be used by a player!");
            return;
        }
        
        String material = context.getString(0);
        int amount = 1;
        Class materialClass;
        
        if(context.hasIndexed(1))
        {
            amount = context.getIndexed(1, Integer.class, 1);
        }
        
        if(material.equalsIgnoreCase("snowball"))
        {
            materialClass = Snowball.class;
        }
        else if(material.equalsIgnoreCase("egg"))
        {
            materialClass = Egg.class;
        }
        else
        {
            user.sendMessage("item " + material + " is not supported");
            return;
        }
        
        ThrowItem throwItem = new ThrowItem(this.userManager, user.getName(), materialClass);        
        for(int i = 0; i < amount; i++)
        {
            this.module.getCore().getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this.module.getCore(), throwItem, i * 10);
        }
    }
    
    @Command(
        desc = "The CommandSender throws a certain amount of fireballs. Default is one.",
        max = 2,
        //flags = {@Flag(longName = "small", name = "s")},
        usage = "/fireball [amount] [small]"
    )
    public void fireball(CommandContext context)
    {
        User user = this.userManager.getUser(context.getSender());
        
        if(user == null)
        {
            context.getSender().sendMessage("This command can only be used by a player!");
            return;
        }
        
        int amount = 1;
        Class material;
        
        if(context.hasIndexed(0))
        {
            amount = context.getIndexed(0, Integer.class, 1);
        }
        
        if(context.hasIndexed(1) && context.getString(1).equalsIgnoreCase("small"))
        {
            material = SmallFireball.class;
        }
        else
        {
            material = Fireball.class;
        }
        
        ThrowItem throwItem = new ThrowItem(this.userManager, user.getName(), material);        
        for(int i = 0; i < amount; i++)
        {
            this.module.getCore().getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this.module.getCore(), throwItem, i * 10);
        }
    }
    
}
