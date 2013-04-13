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
package de.cubeisland.cubeengine.core.user;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

import de.cubeisland.cubeengine.core.bukkit.BukkitCore;
import de.cubeisland.cubeengine.core.command.CommandSender;
import de.cubeisland.cubeengine.core.filesystem.FileManager;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.core.util.Cleanable;
import de.cubeisland.cubeengine.core.util.Triplet;
import de.cubeisland.cubeengine.core.util.matcher.Match;

import gnu.trove.impl.Constants;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;
import gnu.trove.set.hash.THashSet;
import org.apache.commons.lang.RandomStringUtils;

import static de.cubeisland.cubeengine.core.logger.LogLevel.WARNING;

/**
 * This Manager provides methods to access the Users and saving/loading from
 * database.
 */
public class UserManager implements Cleanable
{
    private final BukkitCore core;
    private final UserStorage storage;
    private final List<User> onlineUsers;
    private final ConcurrentHashMap<Object, User> cachedUsers;
    private final Set<DefaultAttachment> defaultAttachments;
    private final TObjectIntMap<String> scheduledForRemoval;
    public String salt;
    private final MessageDigest messageDigest;
    private final ScheduledExecutorService nativeScheduler;
    private Set<Long> allKeys;

    public UserManager(final BukkitCore core)
    {
        this.storage = new UserStorage(core);
        this.core = core;

        this.cachedUsers = new ConcurrentHashMap<Object, User>();
        this.onlineUsers = new CopyOnWriteArrayList<User>();

        final long delay = (long)core.getConfiguration().userManagerCleanup;
        this.nativeScheduler = Executors.newSingleThreadScheduledExecutor(core.getTaskManager().getThreadFactory());
        this.nativeScheduler.scheduleAtFixedRate(new UserCleanupTask(), delay, delay, TimeUnit.MINUTES);
        core.getServer().getPluginManager().registerEvents(new UserListener(), core);
        core.getServer().getPluginManager().registerEvents(new AttachmentHookListener(), core);

        this.defaultAttachments = new THashSet<DefaultAttachment>();
        this.scheduledForRemoval = new TObjectIntHashMap<String>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1);

        for (Player player : ((BukkitCore)core).getServer().getOnlinePlayers())
        {
            this.onlineUsers.add(this.getExactUser(player));
        }

        this.loadSalt();

        try
        {
            messageDigest = MessageDigest.getInstance("SHA-512");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("SHA-512 hash algorithm not available!");
        }
    }

    public boolean login(User user, String password)
    {
        if (!user.isLoggedIn())
        {
            user.loggedInState = this.checkPassword(user, password);
        }
        core.getEventManager().fireEvent(new UserAuthorizedEvent(this.core, user));
        return user.isLoggedIn();
    }

    public boolean checkPassword(User user, String password)
    {
        synchronized (this.messageDigest)
        {
            messageDigest.reset();
            password += this.salt;
            password += user.firstseen.toString();
            return Arrays.equals(user.passwd, messageDigest.digest(password.getBytes()));
        }
    }

    public void setPassword(User user, String password)
    {
        synchronized (this.messageDigest)
        {
            this.messageDigest.reset();
            password += this.salt;
            password += user.firstseen.toString();
            user.passwd = this.messageDigest.digest(password.getBytes());
            this.storage.update(user);
        }
    }

    public void resetPassword(User user)
    {
        user.passwd = null;
        this.storage.update(user);
    }

    public void resetAllPasswords()
    {
        this.storage.resetAllPasswords();
    }

    /**
     * Removes the user permanently. Data cannot be restored later on
     *
     * @param user the User
     * @return fluent interface
     */
    public void removeUser(final User user)
    {
        this.storage.delete(user); //this is async
        this.removeCachedUser(user);
    }

    public User getExactUser(String name)
    {
        return this.getUser(name, true);
    }

    /**
     * Gets a User by Offlineplayer (creates new User if not found)
     *
     * @param player the player
     * @return the User
     */
    public User getExactUser(OfflinePlayer player)
    {
        if (player == null)
        {
            return null;
        }
        if (player instanceof User)
        {
            return (User)player;
        }
        return this.getExactUser(player.getName());
    }

    /**
     * Gets a User by CommandSender (creates new User if not found)
     *
     * @param sender the sender
     * @return the User OR null if sender is not a Player
     */
    public User getExactUser(CommandSender sender)
    {
        if (sender == null)
        {
            return null;
        }
        if (sender instanceof User)
        {
            return this.getExactUser((OfflinePlayer)sender);
        }
        return null;
    }

    /**
     * Gets a User by Key in DB
     *
     * @param key the key to get the user by
     * @return the user or null if not found
     */
    public synchronized User getUser(Long key)
    {
        if (key == null)
        {
            throw new NullPointerException();
        }
        User user = this.cachedUsers.get(key);
        if (user != null)
        {
            return user;
        }
        user = this.storage.get(key);
        if (user == null)
        {
            return null;
        }
        this.cacheUser(user);
        return user;
    }

    public User getUser(OfflinePlayer player)
    {
        return this.getUser(player, false);
    }

    public User getUser(OfflinePlayer player, boolean create)
    {
        return this.getUser(player.getName(), create);
    }

    public User getUser(String playerName)
    {
        return this.getUser(playerName, false);
    }

    public synchronized User getUser(String name, boolean create)
    {
        if (name == null)
        {
            throw new NullPointerException();
        }
        User user = this.cachedUsers.get(name.toLowerCase());
        if (user == null)
        {
            user = this.loadUser(name);
        }
        if (user == null && create)
        {
            user = this.createUser(name);
        }
        return user;
    }

    private synchronized User loadUser(String playerName)
    {
        User user = this.storage.loadUser(playerName);
        if (user != null)
        {
            this.cacheUser(user);
        }
        return user;
    }

    /**
     * Adds a new User
     *
     * @return the created User
     */
    private synchronized User createUser(String name)
    {
        User user = this.cachedUsers.get(name.toLowerCase());
        if (user != null)
        {
            //User was already added
            return user;
        }
        user = new User(this.core, name);
        this.storage.store(user, false);
        this.cacheUser(user);

        return user;
    }

    private synchronized void attachDefaults(User user)
    {
        for (DefaultAttachment defaultAttachment : this.defaultAttachments)
        {
            defaultAttachment.attachTo(user);
        }
    }

    private synchronized void cacheUser(User user)
    {
        this.core.getLog().log(LogLevel.DEBUG,"User "+ user.getName()+ " cached!");
        this.cachedUsers.put(user.getName().toLowerCase(), user);
        this.cachedUsers.put(user.getId(), user);
        this.attachDefaults(user);
    }

    private synchronized void removeCachedUser(User user)
    {
        this.core.getLog().log(LogLevel.DEBUG,"Removed cached user "+ user.getName()+ "!");
        this.cachedUsers.remove(user.getName().toLowerCase());
        this.cachedUsers.remove(user.getId());
        user.detachAll();
    }

    /**
     * Returns all the users that are currently online
     *
     * @return a unmodifiable List of players
     */
    public synchronized Set<User> getOnlineUsers()
    {
        return new THashSet<User>(this.onlineUsers);
    }

    public synchronized Set<User> getLoadedUsers()
    {
        return new THashSet<User>(this.cachedUsers.values());
    }

    public void shutdown()
    {
        this.clean();

        this.scheduledForRemoval.forEachEntry(new TObjectIntProcedure<String>() {
            @Override
            public boolean execute(String a, int b)
            {
                core.getServer().getScheduler().cancelTask(b);
                return true;
            }
        });

        this.cachedUsers.clear();
        this.removeDefaultAttachments();
        this.nativeScheduler.shutdown();
        try
        {
            this.nativeScheduler.awaitTermination(2, TimeUnit.SECONDS);
            this.nativeScheduler.shutdownNow();
        }
        catch (InterruptedException ignore)
        {}
    }

    /**
     * Finds an online User
     *
     * @param name the name
     * @return a online User
     */
    public User findOnlineUser(String name)
    {
        User user = this.findUser(name);
        if (user != null && user.isOnline())
        {
            return user;
        }
        return null;
    }

    /**
     * Finds an User (can create a new User if a found player is online but not
     * yet added)
     *
     * @param name the name
     * @return a User
     */
    public User findUser(String name)
    {
        //Looking up loaded users
        if (name == null)
        {
            return null;
        }
        User user = this.cachedUsers.get(name.toLowerCase());
        if (user == null)
        {
            //Get all online Player and searching for similar names
            ArrayList<String> onlinePlayerList = new ArrayList<String>();
            for (Player player : this.core.getServer().getOnlinePlayers())
            {
                onlinePlayerList.add(player.getName());
            }
            String foundUser = Match.string().matchString(name, onlinePlayerList);
            if (foundUser == null)
            {
                //Looking up saved users
                user = this.storage.loadUser(name);
                if (user != null)
                {
                    this.cacheUser(user);
                }
            }
            else
            {
                user = this.getUser(foundUser, true);
            }
        }
        return user;
    }

    public void broadcastMessage(String message, Permission perm, Object... params)
    {
        for (User user : this.onlineUsers)
        {
            if (perm == null || perm.isAuthorized(user))
            {
                user.sendTranslated(message, params);
            }
        }
        this.core.getCommandManager().getConsoleSender().sendTranslated(message, params);
    }

    public void broadcastMessage(String message, Object... args)
    {
        this.broadcastMessage(message, null, args);
    }

    public void broadcastStatus(ChatFormat starColor, String message, CommandSender sender, Object... args)
    {
        message = ChatFormat.parseFormats(message);
        if (args != null && args.length != 0)
        {
            message = String.format(message,args); //TODO is allowed to use color ?
        }
        String name = sender.getDisplayName();
        for (User user : this.onlineUsers)
        {
            user.sendTranslated(starColor.toString() + "* &2%s &f%s", name, message);
        }
    }

    public void broadcastStatus(String message, CommandSender sender, Object... args)
    {
        this.broadcastStatus(ChatFormat.WHITE, message, sender, args);
    }

    private void loadSalt()
    {
        File file = new File(this.core.getFileManager().getDataFolder(), ".salt");
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            this.salt = reader.readLine();
            reader.close();
        }
        catch (FileNotFoundException e)
        {
            if (this.salt == null)
            {
                try
                {
                    this.salt = RandomStringUtils.randomAscii(32);
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(this.salt);
                    fileWriter.close();
                }
                catch (Exception inner)
                {
                    throw new IllegalStateException("Could not store the static salt in '" + file.getAbsolutePath() + "'!", inner);
                }
            }
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Could not store the static salt in '" + file.getAbsolutePath() + "'!", e);
        }
        FileManager.hideFile(file);
        file.setReadOnly();
    }

    private TLongObjectHashMap<Triplet<Long, String, Integer>> failedLogins = new TLongObjectHashMap<Triplet<Long, String, Integer>>();

    public Triplet<Long, String, Integer> getFailedLogin(User user)
    {
        return this.failedLogins.get(user.key);
    }

    public void addFailedLogin(User user)
    {
        Triplet<Long, String, Integer> loginFail = this.getFailedLogin(user);
        if (loginFail == null)
        {
            loginFail = new Triplet<Long, String, Integer>(System.currentTimeMillis(), user.getAddress().getAddress().getHostAddress(), 1);
            this.failedLogins.put(user.key, loginFail);
        }
        else
        {
            loginFail.setFirst(System.currentTimeMillis());
            loginFail.setSecond(user.getAddress().getAddress().getHostAddress());
            loginFail.setThird(loginFail.getThird() + 1);
        }
    }

    public void removeFailedLogins(User user)
    {
        this.failedLogins.remove(user.key);
    }

    public synchronized void kickAll(String message)
    {
        for (User user : this.cachedUsers.values())
        {
            user.kickPlayer(message);
        }
    }

    public synchronized void kickAll(String message, Object... params)
    {
        for (User user : this.cachedUsers.values())
        {
            user.kickPlayer(user.translate(message, params));
        }
    }

    public void attachToAll(Class<? extends UserAttachment> attachmentClass, Module module)
    {
        for (User user : this.getLoadedUsers())
        {
            user.attach(attachmentClass, module);
        }
    }

    public void detachFromAll(Class<? extends UserAttachment> attachmentClass)
    {
        Set<User> users = new THashSet<User>(this.cachedUsers.values());
        for (User user : users)
        {
            user.detach(attachmentClass);
        }
    }

    public void detachAllOf(Module module)
    {
        Set<User> users = new THashSet<User>(this.cachedUsers.values());
        for (User user : users)
        {
            user.detachAll(module);
        }
    }

    public synchronized void addDefaultAttachment(Class<? extends UserAttachment> attachmentClass, Module module)
    {
        DefaultAttachment attachment = new DefaultAttachment(attachmentClass, module);
        this.defaultAttachments.add(attachment);
        for (User user : new THashSet<User>(this.cachedUsers.values()))
        {
            attachment.attachTo(user);
        }
    }

    public synchronized void removeDefaultAttachment(Class<? extends UserAttachment> attachmentClass)
    {
        Iterator<DefaultAttachment> it = this.defaultAttachments.iterator();
        while (it.hasNext())
        {
            if (it.next().type == attachmentClass)
            {
                it.remove();
                return;
            }
        }
    }

    public synchronized void removeDefaultAttachments(Module module)
    {
        Iterator<DefaultAttachment> it = this.defaultAttachments.iterator();
        while (it.hasNext())
        {
            if (it.next().module == module)
            {
                it.remove();
            }
        }
    }

    public synchronized void removeDefaultAttachments()
    {
        this.defaultAttachments.clear();
    }

    public Set<Long> getAllKeys()
    {
        return this.storage.getAllKeys();
    }

    public synchronized void cleanup(Module module)
    {
        this.removeDefaultAttachments(module);
        this.detachAllOf(module);
    }

    @Override
    public void clean()
    {
        this.storage.cleanup();
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
            final User user = getUser(event.getPlayer());
            final BukkitScheduler scheduler = user.getServer().getScheduler();

            scheduler.scheduleSyncDelayedTask(core, new Runnable()
            {
                @Override
                public void run()
                {
                    onlineUsers.remove(user);
                }
            }, 1);

            final int taskId = scheduler.scheduleSyncDelayedTask(core, new Runnable() {
                @Override
                public void run()
                {
                    scheduledForRemoval.remove(user.getName());
                    user.lastseen = new Timestamp(System.currentTimeMillis());
                    storage.update(user); // is async
                    if (!user.isOnline())
                    {
                        return;
                    }
                    removeCachedUser(user);
                }
            }, core.getConfiguration().userManagerKeepUserLoaded);

            if (taskId == -1)
            {
                core.getLogger().log(WARNING, "The delayed removed of user ''{0}'' could not be scheduled... removing him now.");
                removeCachedUser(user);
            }

            scheduledForRemoval.put(user.getName(), taskId);
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onLogin(final PlayerLoginEvent event)
        {
            if (event.getResult() == PlayerLoginEvent.Result.ALLOWED)
            {
                User user = getUser(event.getPlayer(), true);
                onlineUsers.add(user);
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onJoin(final PlayerJoinEvent event)
        {
            final User user = getUser(event.getPlayer());
            if (user != null)
            {
                // user.offlinePlayer = player; TODO might cause problems to not have this
                user.refreshIP();
                final int removalTask = scheduledForRemoval.get(user.getName());
                if (removalTask > -1)
                {
                    user.getServer().getScheduler().cancelTask(removalTask);
                }
            }
        }
    }

    private class AttachmentHookListener implements Listener
    {
        @EventHandler(priority = EventPriority.MONITOR)
        public void onJoin(PlayerJoinEvent event)
        {
            for (UserAttachment attachment : getUser(event.getPlayer()).getAll())
            {
                attachment.onJoin(event.getJoinMessage());
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onQuit(PlayerQuitEvent event)
        {
            for (UserAttachment attachment : getUser(event.getPlayer()).getAll())
            {
                attachment.onQuit(event.getQuitMessage());
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onKick(PlayerKickEvent event)
        {
            for (UserAttachment attachment : getUser(event.getPlayer()).getAll())
            {
                attachment.onKick(event.getLeaveMessage());
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onChat(AsyncPlayerChatEvent event)
        {
            for (UserAttachment attachment : getUser(event.getPlayer()).getAll())
            {
                attachment.onChat(event.getFormat(), event.getMessage());
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onCommand(PlayerCommandPreprocessEvent event)
        {
            for (UserAttachment attachment : getUser(event.getPlayer()).getAll())
            {
                attachment.onCommand(event.getMessage());
            }
        }
    }

    private class UserCleanupTask implements Runnable
    {
        @Override
        public void run()
        {
            for (User user : cachedUsers.values())
            {
                if (!user.isOnline() && scheduledForRemoval.get(user.getName()) > -1) // Do not delete users that will be deleted anyway
                {
                    removeCachedUser(user);
                }
            }
        }
    }

    private final class DefaultAttachment
    {
        private final Class<? extends UserAttachment> type;
        private final Module module;

        private DefaultAttachment(Class<? extends UserAttachment> type, Module module)
        {
            this.type = type;
            this.module = module;
        }

        public void attachTo(User user)
        {
            user.attach(this.type, this.module);
        }
    }
}
