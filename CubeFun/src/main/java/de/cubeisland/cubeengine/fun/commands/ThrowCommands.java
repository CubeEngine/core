package de.cubeisland.cubeengine.fun.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.fun.Fun;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ThrowCommands
{
    private final Fun module;
    private final ThrowListener throwListener;
    
    public ThrowCommands(Fun module)
    {
        this.module = module;
        this.throwListener = new ThrowListener();
        module.registerListener(throwListener);
    }
    
    @Command
    (
        desc = "The CommandSender throws an experience orb",
        max = 1,
        usage = "[value]"
    )
    public void throwOrb(CommandContext context)
    {
        User user = context.getSenderAsUser("fun", "&cThis command can only be used by a player!");
        
        int value = context.getIndexed(0, Integer.class, Integer.valueOf(10));
        ExperienceOrb orb = (ExperienceOrb) user.getWorld().spawnEntity(user.getLocation().add(user.getLocation().getDirection().multiply(2)), EntityType.EXPERIENCE_ORB);
        orb.setVelocity(user.getLocation().getDirection());
        orb.setExperience(value);
    }
    
    @Command
    (
        names = {"throw"},
        desc = "The CommandSender throws a certain amount of snowballs or eggs. Default is one.",
        min = 1,
        max = 2,
        params = { @Param(names = {"delay", "d"}, type = Integer.class) },
        usage = "<egg|snowball|xpbottle> [amount]"
    )
    public void throwItem(CommandContext context)
    {
        User user = context.getSenderAsUser("fun", "&cThis command can only be used by a player!");

        String material = context.getString(0);
        int amount = context.getIndexed(1, Integer.class, 1);
        int delay = context.getNamed("delay", Integer.class, Integer.valueOf(3));
        Class materialClass = null;

        if(amount > this.module.getConfig().maxThrowNumber || amount < 1)
        {
            illegalParameter(context, "fun", "The amount has to be a number from 1 to %d", this.module.getConfig().maxThrowNumber);
        }
        if(delay > this.module.getConfig().maxThrowDelay || delay < 0)
        {
            illegalParameter(context, "fun", "The delay has to be a number from 0 to %d", this.module.getConfig().maxThrowDelay);
        }
        if (material.equalsIgnoreCase("snowball"))
        {
            materialClass = Snowball.class;
        }
        else if(material.equalsIgnoreCase("egg"))
        {
            materialClass = Egg.class;
        }
        else if(material.equalsIgnoreCase("xp") || material.equalsIgnoreCase("xpbottle"))
        {
            materialClass = ThrownExpBottle.class;
        }
        else
        {
            illegalParameter(context, "fun", "The Item %s is not supported!", material);
        }

        ThrowItem throwItem = new ThrowItem(this.module.getUserManager(), user.getName(), materialClass);
        for (int i = 0; i < amount; i++)
        {
            this.module.getTaskManger().scheduleSyncDelayedTask(module, throwItem, i * delay);
        }
    }

    @Command
    (
        desc = "The CommandSender throws a certain amount of fireballs. Default is one.",
        max = 1,
        flags = 
            {
                @Flag(longName = "unsafe", name = "u"),
                @Flag(longName = "small", name = "s"),
                @Flag(longName = "witherskull", name = "w")
            },
        params = { @Param(names = {"delay", "d"}, type = Integer.class) },
        usage = "[amount]"
    )
    public void fireball(CommandContext context)
    {
        User user = context.getSenderAsUser("core", "&cThis command can only be used by a player!");

        int amount = context.getIndexed(0, Integer.class, 1);
        int delay = context.getNamed("delay", Integer.class, Integer.valueOf(3));
        
        if(amount < 1 || amount > this.module.getConfig().maxFireballNumber)
        {
            illegalParameter(context, "fun", "The amount has to be a number from 1 to %d", this.module.getConfig().maxFireballNumber);
        }
        if(delay > this.module.getConfig().maxFireballDelay || delay < 0)
        {
            illegalParameter(context, "fun", "The delay has to be a number from 0 to %d", this.module.getConfig().maxFireballDelay);
        }
        
        ThrowItem throwItem;
        if(context.hasFlag("s"))
        {
            throwItem = new ThrowItem(this.module.getUserManager(), user.getName(), SmallFireball.class);
        }
        else if(context.hasFlag("w"))
        {
            throwItem = new ThrowItem(this.module.getUserManager(), user.getName(), WitherSkull.class);
        }
        else
        {
            throwItem = new ThrowItem(this.module.getUserManager(), user.getName(), Fireball.class);
        }
        
        if(context.hasFlag("u"))
        {
            throwItem.setUnsafe(true);
        }
        
        for (int i = 0; i < amount; i++)
        {
            this.module.getTaskManger().scheduleSyncDelayedTask(module, throwItem, i * delay);
        }
    }
    
    
    private class ThrowItem implements Runnable
    {
        Class material;
        String name;
        UserManager userManager;
        boolean unsafe;

        public ThrowItem(UserManager userManager, String name, Class materialClass)
        {
            this.userManager = userManager;
            this.name = name;
            this.material = materialClass;
            this.unsafe = false;
        }
        
        public void setUnsafe(boolean unsafe)
        {
            this.unsafe = unsafe;
        }

        @Override
        public void run()
        {
            User user = userManager.getUser(name, true);
            if(material == Snowball.class || material == Egg.class)
            {
                user.launchProjectile(material);
            }
            else if(material == ThrownExpBottle.class)
            {
                ThrownExpBottle bottle = (ThrownExpBottle) user.getWorld().spawnEntity(user.getLocation().add(user.getLocation().getDirection().multiply(2)), EntityType.THROWN_EXP_BOTTLE);
                bottle.setShooter(user);
                bottle.setVelocity(user.getLocation().getDirection());
            }
            else
            {
                Explosive explosive = null;
                if(material == Fireball.class)
                {
                    explosive = (Fireball) user.getWorld().spawnEntity(user.getLocation().add(user.getLocation().getDirection().multiply(2)), EntityType.FIREBALL);
                }
                else if(material == SmallFireball.class)
                {
                    explosive = (SmallFireball) user.getWorld().spawnEntity(user.getLocation().add(user.getLocation().getDirection().multiply(2)), EntityType.SMALL_FIREBALL);
                }
                else if(material == WitherSkull.class)
                {
                    explosive = (WitherSkull) user.getWorld().spawnEntity(user.getLocation().add(user.getLocation().getDirection().multiply(2)), EntityType.WITHER_SKULL);
                }
                else
                {
                    return;
                }
                explosive.setVelocity(user.getLocation().getDirection());
                
                if(!this.unsafe && this.material != SmallFireball.class)
                {
                    throwListener.add(explosive);
                }
                else if(!this.unsafe && this.material == SmallFireball.class)
                {
                    ((SmallFireball)explosive).setFireTicks(0);
                }
            }
            
        }
    }
    
    public class ThrowListener implements Listener
    {
        Set<Explosive> explosive;
        
        public ThrowListener()
        {
            this.explosive = new HashSet<Explosive>();
        }
        
        public void add(Explosive explosive)
        {
            this.explosive.add(explosive);
        }
        
        public boolean contains(Object object)
        {
            return this.explosive.contains(object);
        }
        
        public void remove(Object object)
        {
            this.explosive.remove(object);
        }
        
        @EventHandler
        public void onBlockDamage(EntityExplodeEvent event)
        {
            try
            {
                if (this.contains(event.getEntity()))
                {
                    event.blockList().clear();
                    remove(event.getEntity());
                }
            }
            catch (NullPointerException ignored)
            {}
        }
    }

}
