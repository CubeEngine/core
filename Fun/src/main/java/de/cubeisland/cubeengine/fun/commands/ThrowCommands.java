package de.cubeisland.cubeengine.fun.commands;

import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.Param;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.permission.PermissionManager;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import de.cubeisland.cubeengine.fun.Fun;
import de.cubeisland.cubeengine.fun.FunPerm;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ThrowCommands
{
    private final Map<String, ThrowTask> thrownItems;
    // entities that can't be safe due to bukkit flaws
    private final EnumSet<EntityType> BUGGED_ENTITIES = EnumSet.of(EntityType.SMALL_FIREBALL, EntityType.FIREBALL);

    private static final String BASE_THROW_PERM = FunPerm.BASE + "throw.";

    private final Fun fun;
    private final ThrowListener throwListener;

    public ThrowCommands(Fun fun)
    {
        this.fun = fun;
        this.thrownItems = new THashMap<String, ThrowTask>();
        this.throwListener = new ThrowListener();
        fun.registerListener(this.throwListener);

        PermissionManager perm = fun.getCore().getPermissionManager();
        for (EntityType type : EntityType.values())
        {
            if (type.isSpawnable())
            {
                perm.registerPermission(fun, BASE_THROW_PERM + type.name().toLowerCase(Locale.ENGLISH).replace("_", "-"), PermDefault.OP);
            }
        }
    }

    @Command
    (
        names = "throw", 
        desc = "Throw something!", 
        max = 2, 
        params = @Param(names = { "delay", "d" }, type = Integer.class), 
        flags = @Flag(longName = "unsafe", name = "u"), 
        usage = "<material> [amount] [delay <value>] [-unsafe]"
    )
    public void throwCommand(ParameterizedContext context)
    {
        if (!(context.getSender() instanceof User))
        {
            context.sendMessage("fun", "&cThis command can only be used by a player!");
            return;
        }
        
        User user = (User)context.getSender();
        EntityType type = null;
        boolean showNotification = true;
        boolean unsafe = context.hasFlag("u");

        ThrowTask task = this.thrownItems.remove(user.getName());
        if (task != null)
        {
            if (!context.hasArg(0) || (type = Match.entity().any(context.getString(0))) == task.getType() && task.getInterval() == context.getParam("delay", task.getInterval()) && task.getPreventDamage() != unsafe && !context.hasArg(1))
            {
                task.stop(true);
                return;
            }
            task.stop(showNotification = false);
        }

        if (context.getArgCount() == 0)
        {
            context.sendMessage("fun", "&cYou have to add the material you want to throw.");
            return;
        }

        int amount = context.getArg(1, Integer.class, -1);
        if ((amount > this.fun.getConfig().maxThrowNumber || amount < 1) && amount != -1)
        {
            context.sendMessage("fun", "&cThe amount has to be a number from 1 to %d", this.fun.getConfig().maxThrowNumber);
            return;
        }

        int delay = context.getParam("delay", 3);
        if (delay > this.fun.getConfig().maxThrowDelay || delay < 0)
        {
            context.sendMessage("fun", "&cThe delay has to be a number from 0 to %d", this.fun.getConfig().maxThrowDelay);
            return;
        }
        
        if(unsafe && !FunPerm.COMMAND_THROW_UNSAFE.isAuthorized( context.getSender() ) )
        {
            context.sendMessage( "fun", "&cYou are not allowed to execute this command in unsafe-mode." );
            return;
        }

        String object = context.getString(0);
        if (type == null)
        {
            type = Match.entity().any(object);
        }
        if (type == null)
        {
            context.sendMessage("fun", "&cThe given object was not found!");
            return;
        }
        if (!type.isSpawnable())
        {
            context.sendMessage("fun", "&cThe Item %s is not supported!", object);
            return;
        }

        if (!user.hasPermission(BASE_THROW_PERM + type.name().toLowerCase(Locale.ENGLISH).replace("_", "-")))
        {
            context.sendMessage("fun", "&cYou are not allowed to throw this");
            return;
        }

        if ((BUGGED_ENTITIES.contains(type) || Match.entity().isMonster(type)) && !unsafe)
        {
            context.sendMessage("fun", "&eThis object can only be thrown in unsafe mode. Add -u to enable the unsafe mode.");
            return;
        }

        task = new ThrowTask(user, type, amount, delay, !unsafe);
        if (task.start(showNotification))
        {
            this.thrownItems.put(user.getName(), task);
        }
        else
        {
            context.sendMessage("fun", "&cFailed to throw this!");
        }
    }

    private class ThrowTask implements Runnable
    {
        private final EntityType type;
        private final User user;
        private final int interval;
        private final boolean save;
        private final boolean preventDamage;
        private int amount;
        private int taskId;

        public ThrowTask(User user, EntityType type, int amount, int interval, boolean preventDamage)
        {
            this.user = user;
            this.type = type;
            this.amount = amount;
            this.interval = interval;
            this.preventDamage = preventDamage;
            this.save = this.isSafe(type.getEntityClass());
        }

        private boolean isSafe(Class entityClass)
        {
            if (Explosive.class.isAssignableFrom(entityClass))
            {
                return false;
            }
            if (Arrow.class == entityClass)
            {
                return false;
            }
            return true;
        }

        public User getUser()
        {
            return this.user;
        }

        public EntityType getType()
        {
            return this.type;
        }

        public int getInterval()
        {
            return this.interval;
        }

        public boolean getPreventDamage()
        {
            return this.preventDamage;
        }

        public boolean start()
        {
            return this.start(true);
        }

        public boolean start(boolean notify)
        {
            if (this.amount == -1 && notify)
            {
                this.user.sendMessage("fun", "&aStarted throwing!");
                this.user.sendMessage("fun", "&aYou will keep throwing until you run this command again.");
            }
            this.taskId = fun.getCore().getTaskManager().scheduleSyncRepeatingTask(fun, this, 0, this.interval);
            return this.taskId != -1;
        }

        public void stop()
        {
            this.stop(true);
        }

        public void stop(boolean notify)
        {
            if (this.taskId != -1)
            {
                if (notify)
                {
                    if (this.amount == -1)
                    {
                        this.user.sendMessage("fun", "&aYou are no longer throwing.");
                    }
                    else
                    {
                        this.user.sendMessage("fun", "&aAll objects thrown.");
                    }
                }
                fun.getCore().getTaskManager().cancelTask(fun, this.taskId);
                this.taskId = -1;
            }
        }

        @SuppressWarnings("unchecked")
        private void throwItem()
        {
            final Location location = this.user.getEyeLocation();
            final Vector direction = location.getDirection();
            location.add(direction).add(direction);

            Entity entity;
            if (Projectile.class.isAssignableFrom(this.type.getEntityClass()))
            {
                entity = this.user.launchProjectile((Class<? extends Projectile>)this.type.getEntityClass());
            }
            else
            {
                entity = this.user.getWorld().spawnEntity(location, type);
                entity.setVelocity(direction.multiply(8));
                if (entity instanceof ExperienceOrb)
                {
                    ((ExperienceOrb)entity).setExperience(0);
                }
            }
            if (this.preventDamage && !this.save)
            {
                throwListener.add(entity);
            }
        }

        @Override
        public void run()
        {
            this.throwItem();
            if (this.amount > 0)
            {
                this.amount--;
            }
            if (amount == 0)
            {
                this.stop();
                thrownItems.remove(this.user.getName());
            }
        }
    }

    public class ThrowListener implements Listener
    {
        private final Set<Entity> entities;
        private Entity removal;

        public ThrowListener()
        {
            this.entities = new THashSet<Entity>();
            this.removal = null;
        }

        public void add(Entity entity)
        {
            this.entities.add(entity);
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event)
        {
            ThrowTask task = thrownItems.remove(event.getPlayer().getName());
            if (task != null)
            {
                task.stop();
            }
        }

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void onBlockDamage(EntityExplodeEvent event)
        {
            if (this.handleEntity(event.getEntity()))
            {
                event.blockList().clear();
            }
        }

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void onEntityByEntityDamage(EntityDamageByEntityEvent event)
        {
            if (this.handleEntity(event.getEntity()))
            {
                event.setDamage(0);
            }
        }

        private boolean handleEntity(final Entity entity)
        {
            if (this.entities.contains(entity) && this.removal != entity)
            {
                fun.getCore().getTaskManager().scheduleSyncDelayedTask(fun, new Runnable() {
                    @Override
                    public void run()
                    {
                        entities.remove(removal);
                        removal = null;
                    }
                });
                return true;
            }
            return false;
        }
    }
}
