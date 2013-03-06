package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.core.attachment.UserAttachment;
import de.cubeisland.cubeengine.core.bukkit.BukkitCore;
import de.cubeisland.cubeengine.core.filesystem.FileManager;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.storage.SingleKeyStorage;
import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.ComponentBuilder;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.core.util.Cleanable;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.Triplet;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.THashSet;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static de.cubeisland.cubeengine.core.i18n.I18n._;
import static de.cubeisland.cubeengine.core.storage.database.querybuilder.ComponentBuilder.EQUAL;
import static de.cubeisland.cubeengine.core.storage.database.querybuilder.ComponentBuilder.LESS;

/**
 * This Manager provides methods to access the Users and saving/loading from
 * database.
 */
public class UserManager extends SingleKeyStorage<Long, User> implements Cleanable, Runnable
{
    private final BukkitCore core;
    private final List<Player> onlinePlayers;
    private final ConcurrentHashMap<String, User> users;
    private final ScheduledExecutorService executor;
    private static final int REVISION = 3;
    private final Set<Class<? extends UserAttachment>> defaultAttachments;
    public static String salt; // TODO not acceptable!

    public UserManager(final BukkitCore core)
    {
        super(core.getDB(), User.class, REVISION);
        this.core = core;
        this.executor = core.getTaskManager().getExecutorService();

        this.users = new ConcurrentHashMap<String, User>();
        this.onlinePlayers = new CopyOnWriteArrayList<Player>(core.getServer().getOnlinePlayers());

        final long delay = (long)core.getConfiguration().userManagerCleanup;
        this.executor.scheduleAtFixedRate(this, delay, delay, TimeUnit.MINUTES);
        core.getServer().getPluginManager().registerEvents(new UserListener(), core);

        this.defaultAttachments = new THashSet<Class<? extends UserAttachment>>();
        this.initialize();
        this.loadSalt();
    }

    @Override
    public void initialize()
    {
        super.initialize();
        try
        {
            this.database.storeStatement(User.class, "get_by_name", this.database.getQueryBuilder().select().wildcard().from(this.tableName).where().field("player").is(ComponentBuilder.EQUAL).value().end().end());

            this.database.storeStatement(User.class, "cleanup", database.getQueryBuilder().select(dbKey).from(tableName).where().field("lastseen").is(LESS).value().and().field("nogc").is(EQUAL).value(false).end().end());

            this.database.storeStatement(User.class, "clearpw", database.getQueryBuilder().update(tableName).set("passwd").end().end());
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to initialize the user-manager!", e);
        }
    }

    public void resetAllPasswords()
    {
        try
        {
            this.database.preparedUpdate(modelClass, "clearpw", (Object)null);
        }
        catch (SQLException ex)
        {
            throw new StorageException("An SQL-Error occurred while clearing passwords", ex);
        }
    }

    /**
     * Adds a new User
     *
     * @return the created User
     */
    private User createUser(String name)
    {
        User inUse = this.users.get(name);
        if (inUse != null)
        {
            //User was already added
            return inUse;
        }
        User user = new User(name);
        this.users.put(user.getName(), user);
        this.store(user, false);
        this.attachDefaults(user);
        return user;
    }

    /**
     * Custom Getter for getting User from DB by Name
     *
     * @param playerName the name
     * @return the User OR null if not found
     */
    protected User getFromStorage(String playerName)
    {
        User loadedModel = null;
        try
        {
            ResultSet resulsSet = this.database.preparedQuery(modelClass, "get_by_name", playerName);
            ArrayList<Object> values = new ArrayList<Object>();
            if (resulsSet.next())
            {
                for (String name : this.allFields)
                {
                    values.add(resulsSet.getObject(name));
                }
                loadedModel = this.modelConstructor.newInstance(values);
            }
        }
        catch (SQLException e)
        {
            throw new StorageException("An SQL-Error occurred while creating a new Model from database", e,this.database.getStoredStatement(modelClass,"get_by_name"));
        }
        catch (Exception e)
        {
            throw new StorageException("An unknown error occurred while creating a new Model from database", e);
        }
        this.attachDefaults(loadedModel);
        return loadedModel;
    }

    private synchronized void attachDefaults(User user)
    {
        for (Class<? extends UserAttachment> attachmentClass : this.defaultAttachments)
        {
            user.attach(attachmentClass);
        }
    }

    /**
     * Removes the user permanently. Data cannot be retrieved later
     *
     * @param user the User
     * @return fluent interface
     */
    public UserManager removeUser(final User user)
    {
        this.delete(user); //this is async
        this.users.remove(user.getName());
        return this;
    }

    /**
     * Gets a User by name
     *
     * @param name the name
     * @return the User
     */
    public User getUser(String name, boolean createIfMissing)
    {
        if (name == null)
        {
            return null;
        }
        User user = this.users.get(name);
        if (user == null)
        {
            user = this.getFromStorage(name);
            if (user != null)
            {
                this.users.put(name, user);
            }
        }
        if (user == null && createIfMissing)
        {
            user = this.createUser(name);
        }
        return user;
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
        if (!player.hasPlayedBefore())
        {
            return null;
        }
        return this.getUser(player.getName(), true);
    }

    /**
     * Gets a User by Player (creates new User if not found)
     *
     * @param player the player
     * @return the User
     */
    public User getExactUser(Player player)
    {
        if (player == null)
        {
            return null;
        }
        if (player instanceof User)
        {
            return (User)player;
        }
        return this.getUser(player.getName(), true);
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
            return (User)sender;
        }
        if (sender instanceof Player)
        {
            return this.getUser(sender.getName(), true);
        }
        return null;
    }

    /**
     * Gets a User by Key in DB
     *
     * @param key the key to get the user by
     * @return the user or null if not found
     */
    public User getUser(Long key)
    {
        if (key == null)
        {
            return null;
        }
        User user = this.get(key);
        if (user == null)
        {
            return null;
        }
        User savedUser = this.users.get(user.getName());
        if (savedUser == null)
        {
            this.users.put(user.getName(), user);
            return user;
        }
        return savedUser;
    }

    /**
     * This is a thread safe version of Bukkit's Server.getOnlinePlayers()
     *
     * @return a unmodifiable List of players
     */
    public List<Player> getOnlinePlayers()
    {
        return Collections.unmodifiableList(this.onlinePlayers);
    }

    /**
     * This method returns all users that are online at that moment. The method
     * IS thread-safe as it does not rely on Bukkit's code but instead of our on
     * internal player list.
     *
     * @return an array of users
     */
    public List<User> getOnlineUsers()
    {
        final List<User> onlineUsers = new ArrayList<User>();

        for (Player player : this.onlinePlayers)
        {
            onlineUsers.add(this.getExactUser(player));
        }

        return onlineUsers;
    }

    public Collection<User> getLoadedUsers()
    {
        return this.users.values();
    }

    @Override
    public void clean()
    {
        this.users.clear();
        this.removeDefaultAttachments();
        this.executor.shutdown();
        try
        {
            this.executor.awaitTermination(2, TimeUnit.SECONDS);
            this.executor.shutdownNow();
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
        User user = this.users.get(name);
        if (user == null)
        {
            //Looking up saved users
            user = this.getFromStorage(name);
            if (user != null)
            {
                this.users.put(name, user);
            }
            if (user == null) //then NO user with exact name
            {
                //Get all online Player and searching for similar names
                ArrayList<String> onlinePlayerList = new ArrayList<String>();
                for (Player player : this.core.getServer().getOnlinePlayers())
                {
                    onlinePlayerList.add(player.getName());
                }
                user = this.getUser(Match.string().matchString(name, onlinePlayerList), true);
            }
            if (user != null)
            {
                this.users.put(user.getName(), user);//Adds User to loaded users
            }
        }
        return user;
    }

    @Override
    public void run()
    {
        for (User user : this.users.values())
        {
            if (!user.isOnline() && user.removalTaskId != null) // Do not delete users that will be deleted anyway
            {
                this.users.remove(user.getName());
            }
        }
    }

    /**
     * Searches for too old UserData and remove it.
     */
    public void cleanup()
    {
        this.database.queueOperation(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ResultSet result = database.preparedQuery(User.class, "cleanup",
                                                              new Timestamp(System.currentTimeMillis() - StringUtils.convertTimeToMillis(core.getConfiguration().userManagerCleanupDatabase)));

                    while (result.next())
                    {
                        deleteByKey(result.getLong("key"));
                    }
                }
                catch (SQLException e)
                {
                    // TODO this exception will be uncaught
                    throw new StorageException("An SQL-Error occurred while cleaning the user-table", e, database.getStoredStatement(modelClass, "cleanup"));
                }
                catch (Exception e)
                {
                    // TODO this exception will be uncaught
                    throw new StorageException("An unknown Error occurred while cleaning the user-table", e);
                }
            }
        });
    }

    public void broadcastMessage(String category, String message, Permission perm, Object... args)
    {
        for (Player player : this.core.getServer().getOnlinePlayers())
        {
            if (perm == null || perm.isAuthorized(player))
            {
                this.getExactUser(player).sendMessage(category, message, args);
            }
        }
        Bukkit.getServer().getConsoleSender().sendMessage(_(category, message, args));
    }

    public void broadcastMessage(String category, String message, Object... args)
    {
        this.broadcastMessage(category, message, null, args);
    }

    public void broadcastStatus(String message, String username)
    {
        message = ChatFormat.parseFormats(message);
        for (Player player : this.core.getServer().getOnlinePlayers())
        {
            this.getExactUser(player).sendMessage("* &2" + username + " &f" + message); // not yet configurable
        }
    }

    public void broadcastStatus(String category, String message, String username, Object... args)
    {
        message = "* &2" + username + " &f" + message;
        this.broadcastMessage(category, message, args);
    }

    private void loadSalt()
    {
        File file = new File(this.core.getFileManager().getDataFolder(), ".salt");
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            salt = reader.readLine();
            reader.close();
        }
        catch (FileNotFoundException e)
        {
            if (salt == null)
            {
                try
                {
                    salt = RandomStringUtils.randomAscii(32);
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(salt);
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

    public void kickAll(String message)
    {
        for (User user : this.users.values())
        {
            user.kickPlayer(message);
        }
    }

    public void kickAll(String category, String message, Object... args)
    {
        for (User user : this.users.values())
        {
            user.kickPlayer(_(user, category, message, args));
        }
    }

    public void attachToAll(Class<? extends UserAttachment> attachmentClass)
    {
        for (Player player : this.core.getServer().getOnlinePlayers())
        {
            this.getExactUser(player).attach(attachmentClass);
        }
    }

    public void detachFromAll(Class<? extends UserAttachment> attachmentClass)
    {
        for (Player player : this.core.getServer().getOnlinePlayers())
        {
            this.getExactUser(player).detach(attachmentClass);
        }
    }

    public synchronized void addDefaultAttachment(Class<? extends UserAttachment> attachmentClass)
    {
        this.defaultAttachments.add(attachmentClass);
    }

    public synchronized void removeDefaultAttachment(Class<? extends UserAttachment> attachmentClass)
    {
        this.defaultAttachments.remove(attachmentClass);
    }

    public synchronized void removeDefaultAttachments()
    {
        this.defaultAttachments.clear();
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
        private void onQuit(final PlayerQuitEvent event)
        {
            Player player = event.getPlayer();
            final User user = getExactUser(player);
            user.removalTaskId = event.getPlayer().getServer().getScheduler().scheduleSyncDelayedTask(core, new Runnable() {
                @Override
                public void run()
                {
                    user.lastseen = new Timestamp(System.currentTimeMillis());
                    update(user); // is async
                    if (user.isOnline())
                    {
                        return;
                    }
                    users.remove(event.getPlayer().getName());
                }
            }, core.getConfiguration().userManagerKeepUserLoaded);
            onlinePlayers.remove(player);
        }

        @EventHandler(priority = EventPriority.MONITOR)
        private void onLogin(final PlayerLoginEvent event)
        {
            if (event.getResult() == PlayerLoginEvent.Result.ALLOWED)
            {
                onlinePlayers.add(event.getPlayer());
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        private void onJoin(final PlayerJoinEvent event)
        {
            Player player = event.getPlayer();
            final User user = users.get(player.getName());
            if (user != null)
            {
                user.offlinePlayer = player;
                user.refreshIP();
                if (user.removalTaskId == null)
                {
                    return; // No task to cancel
                }
                user.getServer().getScheduler().cancelTask(user.removalTaskId);
            }
        }
    }
}
