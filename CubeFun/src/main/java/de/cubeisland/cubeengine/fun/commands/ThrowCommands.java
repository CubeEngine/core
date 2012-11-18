package de.cubeisland.cubeengine.fun.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.fun.Fun;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Snowball;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;

public class ThrowCommands
{
    private final Fun module;
    
    public ThrowCommands(Fun module)
    {
        this.module = module;
    }
    
    @Command(
        names = {"throw"},
        desc = "The CommandSender throws a certain amount of snowballs or eggs. Default is one.",
        min = 1,
        max = 2,
        usage = "<egg|snowball> [amount]"
    )
    public void throwItem(CommandContext context)
    {
        User user = context.getSenderAsUser("fun", "&cThis command can only be used by a player!");

        String material = context.getString(0);
        int amount = context.getIndexed(1, Integer.class, 1);
        Class materialClass = null;

        if(amount > this.module.getConfig().maxThrowNumber || amount < 1)
        {
            illegalParameter(context, "fun", "The amount has to be a number from 1 to %d", this.module.getConfig().maxThrowNumber);
        }
        
        if (material.equalsIgnoreCase("snowball"))
        {
            materialClass = Snowball.class;
        }
        else if(material.equalsIgnoreCase("egg"))
        {
            materialClass = Egg.class;
        }
        else
        {
            illegalParameter(context, "fun", "The Item %s is not supported!", material);
        }

        ThrowItem throwItem = new ThrowItem(this.module.getUserManager(), user.getName(), materialClass);
        for (int i = 0; i < amount; i++)
        {
            this.module.getTaskManger().scheduleSyncDelayedTask(module, throwItem, i * 10);
        }
    }

    @Command(
        desc = "The CommandSender throws a certain amount of fireballs. Default is one.",
        max = 1,
        usage = "[amount]"
    )
    public void fireball(CommandContext context)
    {
        User user = context.getSenderAsUser("core", "&cThis command can only be used by a player!");

        int amount = context.getIndexed(0, Integer.class, 1);
        if(amount < 1 || amount > this.module.getConfig().maxFireballNumber)
        {
            illegalParameter(context, "fun", "The amount has to be a number from 1 to %d", this.module.getConfig().maxFireballNumber);
        }
        ThrowItem throwItem = new ThrowItem(this.module.getUserManager(), user.getName(), Fireball.class);
        for (int i = 0; i < amount; i++)
        {
            this.module.getTaskManger().scheduleSyncDelayedTask(module, throwItem, i * 10);
        }
    }
    
    
    private class ThrowItem implements Runnable
    {
        Class material;
        String name;
        UserManager userManager;

        public ThrowItem(UserManager userManager, String name, Class materialClass)
        {
            this.userManager = userManager;
            this.name = name;
            this.material = materialClass;
        }

        @Override
        public void run()
        {
            User user = userManager.getUser(name, true);
            if(material == Fireball.class)
            {
                Fireball fireball = (Fireball) user.getWorld().spawnEntity(user.getLocation().add(user.getLocation().getDirection().multiply(2)), EntityType.FIREBALL);
                fireball.setShooter(user);
                fireball.setVelocity(user.getLocation().getDirection());
            }
            else
            {
                user.launchProjectile(material);
            }
        }
    }

}
