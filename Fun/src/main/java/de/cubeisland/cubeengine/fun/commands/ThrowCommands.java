package de.cubeisland.cubeengine.fun.commands;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.fun.Fun;
import de.cubeisland.cubeengine.fun.FunPerm;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.HashSet;
import java.util.Set;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.invalidUsage;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;

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
        desc = "The CommandSender throws arrow/snowballs/eggs/xp/orb/fireball/smallfireball/witherskull",
        max = 2,
        params = { @Param(names = {"delay", "d"}, type = Integer.class) },
        flags = { @Flag(longName = "unsafe", name = "u") },
        usage = "<material> [amount] [delay <value>] [-unsafe]"
    )
    public void throwItem(CommandContext context)
    {
        User user = context.getSenderAsUser("fun", "&cThis command can only be used by a player!");

        ThrowItem throwItem = this.getThrowItem(user);
        
        if(throwItem == null && context.getIndexed().isEmpty())
        {
            invalidUsage(context, "fun", "&cYou has to add the material you wanna throw.");
        }
        else if(throwItem != null)
        {
            throwItem.remove();
            user.sendMessage("&aYou throw not longer any item.");
        }
        else
        {
            int amount = context.getIndexed(1, Integer.class, -1);
            int delay = context.getNamed("delay", Integer.class, 3);
            
            String material = context.getString(0);
            Class<? extends Projectile> materialClass = null;

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
                    denyAccess(context, "fun", "&cYou are not allowed to throw snow");
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
                    denyAccess(context, "fun", "&cYou are not allowed to throw eggs");
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
                    denyAccess(context, "fun", "&cYou are not allowed to throw xp.");
                }
            }
            // TODO FIX ME!
//            else if(material.equalsIgnoreCase("orb"))
//            {
//                if(FunPerm.THROW_ORB.isAuthorized(user))
//                {
//                    materialClass = ExperienceOrb.class;
//                }
//                else
//                {
//                    denyAccess(context, "fun", "&cYou are not allowed to throw orbs.");
//                }
//            }
            else if(material.equalsIgnoreCase("fireball"))
            {
                if(FunPerm.THROW_FIREBALL.isAuthorized(user))
                {
                    materialClass = Fireball.class;
                }
                else
                {
                    denyAccess(context, "fun", "&cYou are not allowed to throw fireballs.");
                }
            }
            else if(material.equalsIgnoreCase("smallfireball"))
            {
                if(FunPerm.THROW_SMALLFIREBALL.isAuthorized(user))
                {
                    materialClass = SmallFireball.class;
                }
                else
                {
                    denyAccess(context, "fun", "&cYou are not allowed to throw small fireballs.");
                }
            }
            else if(material.equalsIgnoreCase("witherskull"))
            {
                if(FunPerm.THROW_WITHERSKULL.isAuthorized(user))
                {
                    materialClass = WitherSkull.class;
                }
                else
                {
                    denyAccess(context, "fun", "&cYou are not allowed to throw wither skulls.");
                }
            }
            else if(material.equalsIgnoreCase("arrow"))
            {
                if(FunPerm.THROW_ARROW.isAuthorized(user))
                {
                    materialClass = Arrow.class;
                }
                else
                {
                    denyAccess(context, "fun", "&cYou are not allowed to throw arrows.");
                }
            }
            else
            {
                illegalParameter(context, "fun", "&cThe Item %s is not supported!", material);
            }

            throwItem = new ThrowItem(user, materialClass, amount, delay);
            throwItems.add( throwItem );
            
            if(context.hasFlag("u") && ( materialClass == Fireball.class || materialClass == WitherSkull.class ) )
            {
                throwItem.setUnsafe(true);
            }
            
            if(amount == -1)
            {
                user.sendMessage("fun", "&aYou throw this item until you execute this command again.");
            }
        }
        
    }    
    
    private class ThrowItem implements Runnable
    {
        Class<? extends Projectile> material;
        User                        user;
        int                         amount;
        boolean                     unsafe;

        int taskId;

        public ThrowItem(User user, Class<? extends Projectile> materialClass, int amount, int delay)
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
            Location loc = user.getLocation();
            loc.add(loc.getDirection().multiply(2));
            if (material == Snowball.class || material == Egg.class || material == Arrow.class)
            {
                user.launchProjectile(material);
            }
            else if (material == ThrownExpBottle.class)
            {
                ThrownExpBottle bottle = (ThrownExpBottle)user.getWorld().spawnEntity(loc, EntityType.THROWN_EXP_BOTTLE);
                bottle.setShooter(user);
                bottle.setVelocity(loc.getDirection());
            }
            // TODO FIX ME AS WELL!
//            else if (material == ExperienceOrb.class)
//            {
//                ExperienceOrb orb = (ExperienceOrb)user.getWorld().spawnEntity(loc.subtract(0, 0.25, 0), EntityType.EXPERIENCE_ORB);
//                orb.setExperience(0);
//                orb.setVelocity(loc.getDirection());
//            }
            else
            {
                Explosive explosive;
                if (material == Fireball.class)
                {
                    explosive = (Fireball)user.getWorld().spawnEntity(loc, EntityType.FIREBALL);
                }
                else if (material == SmallFireball.class)
                {
                    explosive = (SmallFireball)user.getWorld().spawnEntity(loc, EntityType.SMALL_FIREBALL);
                }
                else if (material == WitherSkull.class)
                {
                    explosive = (WitherSkull)user.getWorld().spawnEntity(loc, EntityType.WITHER_SKULL);
                }
                else
                {
                    this.remove();
                    return;
                }
                explosive.setVelocity(loc.getDirection());

                if (!this.unsafe && this.material != SmallFireball.class)
                {
                    throwListener.add(explosive);
                }
                else if (!this.unsafe && this.material == SmallFireball.class)
                {
                    explosive.setFireTicks(0);
                }
            }
            if (!this.user.isOnline())
            {
                this.remove();
            }
            if (this.amount != -1)
            {
                if (--amount == 0)
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

        public boolean contains(Explosive explosive)
        {
            return this.explosive.contains(explosive);
        }

        public void remove(Explosive explosive)
        {
            this.explosive.remove(explosive);
        }

        @EventHandler
        public void onBlockDamage(EntityExplodeEvent event)
        {
            Entity entity = event.getEntity();
            if (entity != null && entity instanceof Explosive && this.contains((Explosive)entity))
            {
                event.blockList().clear();
                this.remove((Explosive)entity);
            }
        }
    }
}
