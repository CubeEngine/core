package de.cubeisland.cubeengine.fun.commands;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.fun.Fun;
import de.cubeisland.cubeengine.fun.FunPerm;
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
    private static Set<ThrowItem> throwItems = new HashSet<ThrowItem>();
    
    private final Fun module;
    private final ThrowListener throwListener;
    
    public ThrowCommands(Fun module)
    {
        this.module = module;
        this.throwListener = new ThrowListener();
        module.registerListener(throwListener);
    }
    
    private ThrowItem getThrowItem(User user)
    {
        for(ThrowItem throwItem : throwItems)
        {
            if(throwItem.getUser().getName().equals(user.getName()))
            {
                return throwItem;
            }
        }
        return null;
    }
    
    @Command
    (
        names = {"throw"},
        desc = "The CommandSender throws a certain amount of snowballs or eggs.",
        min = 1,
        max = 2,
        params = { @Param(names = {"delay", "d"}, type = Integer.class) },
        usage = "<egg|snowball|xpbottle|orb> [amount] [delay <value>]"
    )
    public void throwItem(CommandContext context)
    {
        User user = context.getSenderAsUser("fun", "&cThis command can only be used by a player!");

        ThrowItem throwItem = this.getThrowItem(user);
        if(throwItem == null)
        {
            String material = context.getString(0);
            int amount = context.getIndexed(1, Integer.class, -1);
            int delay = context.getNamed("delay", Integer.class, 3);
            Class materialClass = null;
            boolean permissions = true;

            if( (amount > this.module.getConfig().maxThrowNumber || amount < 1) && amount != -1)
            {
                illegalParameter(context, "fun", "&cThe amount has to be a number from 1 to %d", this.module.getConfig().maxThrowNumber);
            }
            if(delay > this.module.getConfig().maxThrowDelay || delay < 0)
            {
                illegalParameter(context, "fun", "&cThe delay has to be a number from 0 to %d", this.module.getConfig().maxThrowDelay);
            }
            if (material.equalsIgnoreCase("snowball"))
            {
                if(FunPerm.THROW_SNOW.isAuthorized(user))
                {
                    materialClass = Snowball.class;
                }
                else
                {
                    denyAccess(context, "fun", "You are not allowed to throw snow");
                }
            }
            else if(material.equalsIgnoreCase("egg"))
            {
                if(FunPerm.THROW_EGG.isAuthorized(user))
                {
                    materialClass = Egg.class;
                }
                else
                {
                    denyAccess(context, "fun", "You are not allowed to throw eggs");
                }
            }
            else if(material.equalsIgnoreCase("xp") || material.equalsIgnoreCase("xpbottle"))
            {
                if(FunPerm.THROW_XP.isAuthorized(user))
                {
                    materialClass = ThrownExpBottle.class;
                }
                else
                {
                    denyAccess(context, "fun", "You are not allowed to throw xp.");
                }
            }
            else if(material.equalsIgnoreCase("orb"))
            {
                if(FunPerm.THROW_ORB.isAuthorized(user))
                {
                    materialClass = ExperienceOrb.class;
                }
                else
                {
                    denyAccess(context, "fun", "You are not allowed to throw orbs");
                }

            }
            else
            {
                illegalParameter(context, "fun", "&cThe Item %s is not supported!", material);
            }

            throwItems.add( new ThrowItem(user, materialClass, amount, delay) );
        }
        else
        {
            throwItem.remove();
            user.sendMessage("You throw not longer any Item");
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
        usage = "[amount] [delay <value>] [-small] [-witherskull] [-unsafe]"
    )
    public void fireball(CommandContext context)
    {
        User user = context.getSenderAsUser("core", "&cThis command can only be used by a player!");

        ThrowItem throwItem = this.getThrowItem(user);
        if(throwItem == null)
        {
            int amount = context.getIndexed(0, Integer.class, 1);
            int delay = context.getNamed("delay", Integer.class, Integer.valueOf(3));
            Class material = null;

            if( (amount < 1 || amount > this.module.getConfig().maxFireballNumber) && amount != -1)
            {
                illegalParameter(context, "fun", "&cThe amount has to be a number from 1 to %d", this.module.getConfig().maxFireballNumber);
            }
            if(delay > this.module.getConfig().maxFireballDelay || delay < 0)
            {
                illegalParameter(context, "fun", "&cThe delay has to be a number from 0 to %d", this.module.getConfig().maxFireballDelay);
            }

            if(context.hasFlag("s"))
            {
                if(FunPerm.FIREBALL_SMALLFIREBALL.isAuthorized(user))
                {
                    material = SmallFireball.class;
                }
                else
                {
                    denyAccess(context, "fun", "You are not allowed to throw a small fireball" );
                }
            }
            else if(context.hasFlag("w"))
            {
                if(FunPerm.FIREBALL_WITHERSKULL.isAuthorized(user))
                {
                    material = WitherSkull.class;
                }
                else
                {
                    denyAccess(context, "fun", "You are not allowed to throw a wither skull." );
                }
            }
            else
            {
                if(FunPerm.FIREBALL_FIREBALL.isAuthorized(user))
                {
                    material = Fireball.class;
                }
                else
                {
                    denyAccess(context, "fun", "You are not allowed to throw a fireball." );
                }
            }

            throwItem = new ThrowItem(user, material, amount, delay);
            throwItems.add( throwItem );
            if(context.hasFlag("u"))
            {
                throwItem.setUnsafe(true);
            }
        }
        else
        {
            throwItem.remove();
            user.sendMessage("You throw not longer any item");
        }
        
    }
    
    
    private class ThrowItem implements Runnable
    {
        Class material;
        User user;
        int amount;
        boolean unsafe;
        
        int taskId;

        public ThrowItem(User user, Class materialClass, int amount, int delay)
        {
            this.user = user;
            this.material = materialClass;
            this.amount = amount;
            this.unsafe = false;
            
            this.taskId = CubeEngine.getTaskManager().scheduleSyncRepeatingTask(module, this, 0, delay);
        }
        
        public User getUser()
        {
            return this.user;
        }
        
        public void remove()
        {
            CubeEngine.getTaskManager().cancelTask(module, taskId);
            throwItems.remove(this);
        }
        
        public void setUnsafe(boolean unsafe)
        {
            this.unsafe = unsafe;
        }

        @Override
        public void run()
        {
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
            else if(material == ExperienceOrb.class)
            {
                ExperienceOrb orb = (ExperienceOrb) user.getWorld().spawnEntity(user.getLocation(), EntityType.EXPERIENCE_ORB);
                orb.setExperience(0);
                orb.setVelocity(user.getLocation().getDirection());
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
            if(!this.user.isOnline())
            {
                this.remove();
            }
            if(this.amount != -1)
            {
                if(--amount == 0)
                {
                    this.remove();
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
