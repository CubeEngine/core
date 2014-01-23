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
package de.cubeisland.engine.fun.commands;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.matcher.Match;
import de.cubeisland.engine.fun.Fun;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

public class ThrowCommands
{
    private final Map<String, ThrowTask> thrownItems;
    // entities that can't be safe due to bukkit flaws
    private final EnumSet<EntityType> BUGGED_ENTITIES = EnumSet.of(EntityType.SMALL_FIREBALL, EntityType.FIREBALL);

    private final Fun module;
    private final ThrowListener throwListener;

    public ThrowCommands(Fun module)
    {
        this.module = module;
        this.thrownItems = new THashMap<>();
        this.throwListener = new ThrowListener();
        module.getCore().getEventManager().registerListener(module, this.throwListener);
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
            context.sendTranslated("&cThis command can only be used by a player!");
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
            context.sendTranslated("&cYou have to add the material you want to throw.");
            return;
        }

        int amount = context.getArg(1, Integer.class, -1);
        if ((amount > this.module.getConfig().command.throwSection.maxAmount || amount < 1) && amount != -1)
        {
            context.sendTranslated("&cThe amount has to be a number from 1 to %d", this.module.getConfig().command.throwSection.maxAmount);
            return;
        }

        int delay = context.getParam("delay", 3);
        if (delay > this.module.getConfig().command.throwSection.maxDelay || delay < 0)
        {
            context.sendTranslated("&cThe delay has to be a number from 0 to %d", this.module.getConfig().command.throwSection.maxDelay);
            return;
        }
        
        if(unsafe && !module.perms().COMMAND_THROW_UNSAFE.isAuthorized( context.getSender() ) )
        {
            context.sendTranslated("&cYou are not allowed to execute this command in unsafe-mode.");
            return;
        }

        String object = context.getString(0);
        if (type == null)
        {
            type = Match.entity().any(object);
        }
        if (type == null)
        {
            context.sendTranslated("&cThe given object was not found!");
            return;
        }
        if (!type.isSpawnable())
        {
            context.sendTranslated("&cThe Item %s is not supported!", object);
            return;
        }

        if (!user.hasPermission(module.perms().COMMAND_THROW.getName() + "." + type.name().toLowerCase(Locale.ENGLISH).replace("_", "-"))) // TODO these should get registered!!!
        {
            context.sendTranslated("&cYou are not allowed to throw this");
            return;
        }

        if ((BUGGED_ENTITIES.contains(type) || Match.entity().isMonster(type)) && !unsafe)
        {
            context.sendTranslated("&eThis object can only be thrown in unsafe mode. Add -u to enable the unsafe mode.");
            return;
        }

        task = new ThrowTask(user, type, amount, delay, !unsafe);
        if (task.start(showNotification))
        {
            this.thrownItems.put(user.getName(), task);
        }
        else
        {
            context.sendTranslated("&cFailed to throw this!");
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
                this.user.sendTranslated("&aStarted throwing!");
                this.user.sendTranslated("&aYou will keep throwing until you run this command again.");
            }
            this.taskId = module.getCore().getTaskManager().runTimer(module, this, 0, this.interval);
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
                        this.user.sendTranslated("&aYou are no longer throwing.");
                    }
                    else
                    {
                        this.user.sendTranslated("&aAll objects thrown.");
                    }
                }
                module.getCore().getTaskManager().cancelTask(module, this.taskId);
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
            this.entities = new THashSet<>();
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
                module.getCore().getTaskManager().runTask(module, new Runnable()
                {
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
