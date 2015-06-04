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
package de.cubeisland.engine.module.service.user;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import com.google.common.base.Optional;
import de.cubeisland.engine.modularity.asm.marker.Enable;
import de.cubeisland.engine.modularity.asm.marker.ServiceImpl;
import de.cubeisland.engine.modularity.asm.marker.Version;
import de.cubeisland.engine.module.core.filesystem.FileManager;
import de.cubeisland.engine.module.core.i18n.I18n;
import de.cubeisland.engine.module.core.sponge.CoreModule;
import de.cubeisland.engine.module.core.sponge.EventManager;
import de.cubeisland.engine.module.core.util.Profiler;
import de.cubeisland.engine.module.core.util.converter.UserConverter;
import de.cubeisland.engine.module.service.command.CommandManager;
import de.cubeisland.engine.module.service.database.Database;
import de.cubeisland.engine.module.service.task.TaskManager;
import de.cubeisland.engine.reflect.Reflector;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.entity.player.PlayerChatEvent;
import org.spongepowered.api.event.entity.player.PlayerJoinEvent;
import org.spongepowered.api.event.entity.player.PlayerKickEvent;
import org.spongepowered.api.event.entity.player.PlayerQuitEvent;
import org.spongepowered.api.event.message.CommandEvent;


@ServiceImpl(UserManager.class)
@Version(1)
public class SpongeUserManager extends AbstractUserManager implements UserManager
{
    private final CoreModule core;
    private EventManager em;
    private TaskManager tm;
    private Reflector reflector;
    protected ScheduledExecutorService nativeScheduler;
    protected Map<UUID, UUID> scheduledForRemoval = new HashMap<>();

    @Inject
    public SpongeUserManager(final CoreModule core, Database database, EventManager em, TaskManager tm, Reflector reflector, CommandManager cm, I18n i18n, FileManager fm)
    {
        super(core, database, cm, i18n, fm, em);
        this.core = core;
        this.em = em;
        this.tm = tm;
        this.reflector = reflector;
    }

    @Enable
    public void onEnable()
    {
        super.onEnable();
        final long delay = (long)core.getConfiguration().usermanager.cleanup;
        this.nativeScheduler = Executors.newSingleThreadScheduledExecutor(core.getProvided(ThreadFactory.class));
        this.nativeScheduler.scheduleAtFixedRate(new UserCleanupTask(), delay, delay, TimeUnit.MINUTES);

        em.registerListener(core, new UserListener());
        em.registerListener(core, new AttachmentHookListener());

        reflector.getDefaultConverterManager().registerConverter(new UserConverter(this), User.class);
    }

    @Override
    public synchronized Set<User> getOnlineUsers()
    {
        Set<User> users = super.getOnlineUsers();
        Iterator<User> it = users.iterator();

        User user;
        while (it.hasNext())
        {
            user = it.next();
            if (!user.isOnline())
            {
                core.getLog().warn("Found an offline player in the online players list: {}({})", user.getDisplayName(),
                                   user.getUniqueId());
                this.onlineUsers.remove(user);
                it.remove();
            }
        }

        return users;
    }

    @Override
    public void shutdown()
    {
        super.shutdown();

        for (UUID id : this.scheduledForRemoval.values())
        {
            tm.cancelTask(core, id);
        }

        this.scheduledForRemoval.clear();
        this.scheduledForRemoval = null;

        this.nativeScheduler.shutdown();
        try
        {
            this.nativeScheduler.awaitTermination(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException ignored)
        {
            Thread.currentThread().interrupt();
        }
        finally
        {
            this.nativeScheduler.shutdownNow();
            this.nativeScheduler = null;
        }
    }

    @Override
    protected User getUser(String name, boolean create)
    {
        for (User user : this.getOnlineUsers())
        {
            if (user.getName().equalsIgnoreCase(name))
            {
                return user;
            }
        }
        UserEntity userEntity = this.database.getDSL().selectFrom(TableUser.TABLE_USER).where(
            TableUser.TABLE_USER.LASTNAME.eq(name.toLowerCase())).fetchOne();
        if (userEntity != null)
        {
            org.spongepowered.api.entity.player.User offlinePlayer = getOfflinePlayer(name);
            if (offlinePlayer.getUniqueId().equals(userEntity.getUniqueId()))
            {
                User user = new User(core, userEntity);
                this.cacheUser(user);
                return user;
            }
            userEntity.setValue(TableUser.TABLE_USER.LASTNAME, this.core.getConfiguration().nameConflict.replace(
                "{name}", userEntity.getValue(TableUser.TABLE_USER.LASTNAME)));
            userEntity.updateAsync();
        }
        if (create)
        {
            org.spongepowered.api.entity.player.User offlinePlayer = getOfflinePlayer(name);
            User user = new User(core, offlinePlayer);
            user.getEntity().insertAsync();
            this.cacheUser(user);
            return user;
        }
        return null;
    }

    private org.spongepowered.api.entity.player.User getOfflinePlayer(String name)
    {
        Optional<Player> player = core.getGame().getServer().getPlayer(name);
        return player.orNull();
        // TODO actually get User when offline
    }

    private User getExactUser(org.spongepowered.api.entity.player.User player, boolean login)
    {
        CompletableFuture<Integer> future = null;
        User user = this.cachedUserByUUID.get(player.getUniqueId());
        if (user == null)
        {
            user = this.loadUserFromDatabase(player.getUniqueId());
            if (user == null)
            {
                user = new User(core, player);
                future = user.getEntity().insertAsync();
            }
            this.cacheUser(user);
        }
        if (login)
        {
            UserLoadedEvent event = new UserLoadedEvent(core, user);
            if (future != null)
            {
                future.thenAccept(cnt -> tm.runTask(core, () -> em.fireEvent(event)));
            }
            else
            {
                em.fireEvent(event);
            }
        }
        return user;
    }

    private User getExactUser(org.spongepowered.api.entity.player.User player)
    {
        return this.getExactUser(player, false);
    }

    public class UserListener
    {
        /**
         * Removes the user from loaded UserList when quitting the server and
         * updates lastseen in database
         *
         * @param event the PlayerQuitEvent
         */
        @Subscribe(order = Order.POST)
        public void onQuit(final PlayerQuitEvent event)
        {
            final User user = getExactUser(event.getUser().getUniqueId());
            tm.runTask(core, () -> {
                synchronized (SpongeUserManager.this)
                {
                    if (!user.isOnline())
                    {
                        onlineUsers.remove(user);
                    }
                }
            });

            Optional<UUID> uid = tm.runTaskDelayed(core, () -> {
                scheduledForRemoval.remove(user.getUniqueId());
                user.getEntity().setValue(TableUser.TABLE_USER.LASTSEEN, new Timestamp(System.currentTimeMillis()));
                Profiler.startProfiling("removalTask");
                user.getEntity().updateAsync();
                core.getLog().debug("BukkitUserManager:UserListener#onQuit:RemovalTask {}ms", Profiler.endProfiling(
                    "removalTask", TimeUnit.MILLISECONDS));
                if (user.isOnline())
                {
                    removeCachedUser(user);
                }
            }, core.getConfiguration().usermanager.keepInMemory);

            if (!uid.isPresent())
            {
                core.getLog().warn("The delayed removed of player '{}' could not be scheduled... removing them now.");
                removeCachedUser(user);
                return;
            }

            scheduledForRemoval.put(user.getUniqueId(), uid.get());
        }

        @Subscribe(order = Order.EARLY)
        public void onJoin(final PlayerJoinEvent event)
        {
            final User user = getExactUser(event.getUser());
            if (user != null)
            {
                onlineUsers.add(user);

                updateLastName(user);
                user.refreshIP();
                final UUID removalTask = scheduledForRemoval.get(user.getUniqueId());
                if (removalTask != null)
                {
                    tm.cancelTask(core, removalTask);
                }
            }
        }
    }

    private class UserCleanupTask implements Runnable
    {
        @Override
        public void run()
        {
            cachedUserByUUID.values().stream().filter(user -> !user.isOnline()).filter(
                user -> scheduledForRemoval.containsKey(user.getUniqueId())).forEach(
                SpongeUserManager.this::removeCachedUser);
        }
    }

    public class AttachmentHookListener
    {
        @Subscribe(order = Order.POST)
        public void onJoin(PlayerJoinEvent event)
        {
            for (UserAttachment attachment : getExactUser(event.getUser()).getAll())
            {
                attachment.onJoin(event.getMessage());
            }
        }

        @Subscribe(order = Order.POST)
        public void onQuit(PlayerQuitEvent event)
        {
            for (UserAttachment attachment : getExactUser(event.getUser()).getAll())
            {
                attachment.onQuit(event.getMessage());
            }
        }

        @Subscribe(order = Order.POST)
        public void onKick(PlayerKickEvent event)
        {
            for (UserAttachment attachment : getExactUser(event.getUser()).getAll())
            {
                attachment.onKick(event.getMessage());
            }
        }

        @Subscribe(order = Order.POST)
        public void onChat(PlayerChatEvent event)
        {
            for (UserAttachment attachment : getExactUser(event.getUser()).getAll())
            {
                // TODO attachment.onChat(event.getFormat(), event.getMessage());
            }
        }

        @Subscribe(order = Order.POST)
        public void onCommand(CommandEvent event)
        {
            if (event.getSource() instanceof Player)
            {
                for (UserAttachment attachment : getExactUser((Player)event.getSource()).getAll())
                {
                    attachment.onCommand(event.getCommand() + " " + event.getArguments());
                }
            }
        }
    }
}