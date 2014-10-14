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
package de.cubeisland.engine.core.bukkit;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import de.cubeisland.engine.core.user.AbstractUserManager;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserAttachment;
import de.cubeisland.engine.core.user.UserEntity;
import de.cubeisland.engine.core.util.Profiler;
import gnu.trove.impl.Constants;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;

import static de.cubeisland.engine.core.user.TableUser.TABLE_USER;
import static org.bukkit.event.player.PlayerLoginEvent.Result.ALLOWED;


public class BukkitUserManager extends AbstractUserManager
{
    private final BukkitCore core;
    protected ScheduledExecutorService nativeScheduler;
    protected TObjectIntMap<UUID> scheduledForRemoval;

    public BukkitUserManager(final BukkitCore core)
    {
        super(core);
        this.core = core;

        final long delay = (long)core.getConfiguration().usermanager.cleanup;
        this.nativeScheduler = Executors.newSingleThreadScheduledExecutor(core.getTaskManager().getThreadFactory());
        this.nativeScheduler.scheduleAtFixedRate(new UserCleanupTask(), delay, delay, TimeUnit.MINUTES);
        this.scheduledForRemoval = new TObjectIntHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1);

        this.core.addInitHook(new Runnable() {
            @Override
            public void run()
            {
                core.getServer().getPluginManager().registerEvents(new UserListener(), core);
                core.getServer().getPluginManager().registerEvents(new AttachmentHookListener(), core);

                for (Player player : core.getServer().getOnlinePlayers())
                {
                    onlineUsers.add(getExactUser(player.getUniqueId()));
                }
            }
        });
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
                core.getLog().warn("Found an offline player in the online players list: {}({})", user.getDisplayName(), user.getUniqueId());
                this.onlineUsers.remove(user);
                it.remove();
            }
        }

        return users;
    }

    public void shutdown()
    {
        super.shutdown();

        this.scheduledForRemoval.forEachEntry(new TObjectIntProcedure<UUID>() {
            @Override
            public boolean execute(UUID a, int b)
            {
                core.getServer().getScheduler().cancelTask(b);
                return true;
            }
        });
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

    protected User getUser(String name, boolean create)
    {
        for (User user : this.getOnlineUsers())
        {
            if (user.getName().equalsIgnoreCase(name))
            {
                return user;
            }
        }
        UserEntity userEntity = this.database.getDSL().selectFrom(TABLE_USER)
                                             .where(TABLE_USER.LASTNAME.eq(name.toLowerCase())).fetchOne();
        if (userEntity != null)
        {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
            if (offlinePlayer.getUniqueId().equals(userEntity.getUniqueId()))
            {
                User user = new User(userEntity);
                this.cacheUser(user);
                return user;
            }
            userEntity.setValue(TABLE_USER.LASTNAME, this.core.getConfiguration().nameConflict.replace("{name}", userEntity.getValue(TABLE_USER.LASTNAME)));
            userEntity.asyncUpdate();
        }
        if (create)
        {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
            User user = new User(core, offlinePlayer);
            user.getEntity().asyncInsert();
            this.cacheUser(user);
            return user;
        }
        return null;
    }

    private User getExactUser(OfflinePlayer player)
    {
        User user = this.cachedUserByUUID.get(player.getUniqueId());
        if (user == null)
        {
            user = this.loadUserFromDatabase(player.getUniqueId());
            if (user == null)
            {
                user = new User(core, player);
                user.getEntity().asyncInsert();
            }
            this.cacheUser(user);
        }
        return user;
    }

    private class UserListener implements Listener
    {
        /**
         * Removes the user from loaded UserList when quitting the server and
         * updates lastseen in database
         *
         * @param event the PlayerQuitEvent
         */
        @EventHandler(priority = EventPriority.MONITOR)
        public void onQuit(final PlayerQuitEvent event)
        {
            final User user = getExactUser(event.getPlayer().getUniqueId());
            final BukkitScheduler scheduler = user.getServer().getScheduler();

            scheduler.runTask(core, new Runnable()
            {
                @Override
                public void run()
                {
                    synchronized (BukkitUserManager.this)
                    {
                        onlineUsers.remove(user);
                    }
                }
            });

            final BukkitTask task = scheduler.runTaskLater(core, new Runnable()
            {
                @Override
                public void run()
                {
                    scheduledForRemoval.remove(user.getUniqueId());
                    user.getEntity().setValue(TABLE_USER.LASTSEEN, new Timestamp(System.currentTimeMillis()));
                    Profiler.startProfiling("removalTask");
                    user.getEntity().asyncUpdate();
                    core.getLog().debug("BukkitUserManager:UserListener#onQuit:RemovalTask {}ms", Profiler.endProfiling("removalTask", TimeUnit.MILLISECONDS));
                    if (user.isOnline())
                    {
                        removeCachedUser(user);
                    }
                }
            }, core.getConfiguration().usermanager.keepInMemory);

            if (task == null || task.getTaskId() == -1)
            {
                core.getLog().warn("The delayed removed of player '{}' could not be scheduled... removing them now.");
                removeCachedUser(user);
                return;
            }

            scheduledForRemoval.put(user.getUniqueId(), task.getTaskId());
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onLogin(final PlayerLoginEvent event)
        {
            if (event.getResult() == ALLOWED)
            {
                User user = getExactUser(event.getPlayer());
                onlineUsers.add(user);
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onJoin(final PlayerJoinEvent event)
        {
            final User user = getExactUser(event.getPlayer());
            if (user != null)
            {
                updateLastName(user);
                user.refreshIP();
                final int removalTask = scheduledForRemoval.get(user.getUniqueId());
                if (removalTask > -1)
                {
                    user.getServer().getScheduler().cancelTask(removalTask);
                }
            }
        }
    }

    private class UserCleanupTask implements Runnable
    {
        @Override
        public void run()
        {
            for (User user : cachedUserByUUID.values())
            {
                if (!user.isOnline() && scheduledForRemoval.get(user.getUniqueId()) > -1) // Do not delete users that will be deleted anyway
                {
                    removeCachedUser(user);
                }
            }
        }
    }

    private class AttachmentHookListener implements Listener
    {
        @EventHandler(priority = EventPriority.MONITOR)
        public void onJoin(PlayerJoinEvent event)
        {
            for (UserAttachment attachment : getExactUser(event.getPlayer()).getAll())
            {
                attachment.onJoin(event.getJoinMessage());
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onQuit(PlayerQuitEvent event)
        {
            for (UserAttachment attachment : getExactUser(event.getPlayer()).getAll())
            {
                attachment.onQuit(event.getQuitMessage());
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onKick(PlayerKickEvent event)
        {
            for (UserAttachment attachment : getExactUser(event.getPlayer()).getAll())
            {
                attachment.onKick(event.getLeaveMessage());
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onChat(AsyncPlayerChatEvent event)
        {
            for (UserAttachment attachment : getExactUser(event.getPlayer()).getAll())
            {
                attachment.onChat(event.getFormat(), event.getMessage());
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onCommand(PlayerCommandPreprocessEvent event)
        {
            for (UserAttachment attachment : getExactUser(event.getPlayer()).getAll())
            {
                attachment.onCommand(event.getMessage());
            }
        }
    }
}
