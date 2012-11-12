package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.bukkit.BukkitCore;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.storage.BasicStorage;
import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.DatabaseUpdater;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.ComponentBuilder;
import de.cubeisland.cubeengine.core.util.Cleanable;
import de.cubeisland.cubeengine.core.util.StringUtils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import static de.cubeisland.cubeengine.core.storage.database.querybuilder.ComponentBuilder.*;

/**
 * This Manager provides methods to access the Users and saving/loading from
 * database.
 */
public class UserManager extends BasicStorage<User> implements Cleanable, Runnable, Listener
{
    private final Core core;
    private final List<Player> onlinePlayers;
    private final ConcurrentHashMap<String, User> users;
    private final Server server;
    private final ScheduledExecutorService executor;
    private static final int REVISION = 3;

    public UserManager(final Core core)
    {
        super(core.getDB(), User.class, REVISION);
        this.core = core;
        this.registerUpdaters();
        this.executor = core.getTaskManager().getExecutorService();

        this.server = ((BukkitCore)core).getServer();
        this.users = new ConcurrentHashMap<String, User>();
        this.onlinePlayers = new CopyOnWriteArrayList<Player>(((BukkitCore)core).getServer().getOnlinePlayers());

        final long delay = (long)core.getConfiguration().userManagerCleanup;
        this.executor.scheduleAtFixedRate(this, delay, delay, TimeUnit.MINUTES);
        this.initialize();
    }

    @Override
    public void initialize()
    {
        super.initialize();
        try
        {
            this.database.prepareAndStoreStatement(User.class, "get_by_name", this.database.getQueryBuilder()
                .select().wildcard()
                .from(this.table)
                .where()
                .field("player").is(ComponentBuilder.EQUAL).value()
                .end()
                .end());

            this.database.prepareAndStoreStatement(User.class, "cleanup", database.getQueryBuilder()
                .select(key).from(table)
                .where().field("lastseen").is(LESS).value()
                .and().field("nogc").is(EQUAL).value(false)
                .end().end());
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to initialize the user manager!", e);
        }
    }

    private void registerUpdaters()
    {
        this.registerUpdater(new DatabaseUpdater()
        {
            @Override
            public void update(Database database) throws SQLException
            {
                database.execute(
                    database.getQueryBuilder().
                    alterTable(table).
                    add("nogc", AttrType.BOOLEAN).
                    defaultValue("false").
                    end().
                    end());
                database.execute(
                    database.getQueryBuilder().
                    alterTable(table).
                    add("lastseen", AttrType.TIMESTAMP).
                    defaultValue().value().
                    end().
                    end(), new Timestamp(System.currentTimeMillis()));
            }
        }, 1);
        this.registerUpdater(new DatabaseUpdater()
        {
            @Override
            public void update(Database database) throws SQLException
            {
                database.execute(
                    database.getQueryBuilder().
                    alterTable(table).
                    addUnique("player").
                    end().
                    end());
            }
        }, 2);
    }

    /**
     * Custom Getter for getting User from DB by Name
     *
     * @param playername the name
     * @return the User OR null if not found
     */
    protected User getFromStorage(String playername)
    {
        User loadedModel = null;
        try
        {
            ResultSet resulsSet = this.database.preparedQuery(modelClass, "get_by_name", playername);
            ArrayList<Object> values = new ArrayList<Object>();
            if (resulsSet.next())
            {
                values.add(resulsSet.getObject(this.key));
                for (String name : this.attributes)
                {
                    values.add(resulsSet.getObject(name));
                }
                loadedModel = this.modelConstructor.newInstance(values);
            }
        }
        catch (SQLException e)
        {
            throw new StorageException("Error while getting Model from Database", e);
        }
        catch (Exception e)
        {
            throw new StorageException("Error while creating fresh Model from Database", e);
        }
        return loadedModel;
    }

    /**
     * Adds a new User
     *
     * @param user the User
     * @return fluent interface
     */
    public UserManager addUser(final User user)
    {
        User inUse = this.users.get(user.getName());
        if (inUse != null)
        {
            //User was already added
            return this;
        }
        this.users.put(user.getName(), user);
        this.store(user);
        UserCreatedEvent event = new UserCreatedEvent(this.core, user);
        server.getPluginManager().callEvent(event);
        return this;
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
            user = new User(name);
            this.addUser(user);
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
     * @param key
     * @return
     */
    public User getUser(int key)
    {
        User user = this.get(key);
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
     * This method returns all users that are online at that moment.
     * The method IS thread-safe as it does not rely on Bukkit's code
     * but instead of our on internal player list.
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
     * Finds an User (can create a new User if a found player is online but not yet added)
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
                for (Player player : this.server.getOnlinePlayers())
                {
                    onlinePlayerList.add(player.getName());
                }
                user = this.getUser(StringUtils.matchString(name, onlinePlayerList), true);
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
        final int id = event.getPlayer().getServer().getScheduler().scheduleSyncDelayedTask((Plugin)core, new Runnable()
        {
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
        }, this.core.getConfiguration().userManagerKeepUserLoaded);
        user.removalTaskId = id;
        this.onlinePlayers.remove(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onLogin(final PlayerLoginEvent event)
    {
        this.onlinePlayers.add(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onJoin(final PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        final User user = this.users.get(player.getName());
        if (user != null)
        {
            user.offlinePlayer = player;
            if (user.removalTaskId == null)
            {
                return; // No task to cancel
            }
            user.getServer().getScheduler().cancelTask(user.removalTaskId);
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
                        deleteByKey(result.getInt("key"));
                    }
                }
                catch (Exception e)
                {
                    throw new StorageException("Error while cleaning DB", e);
                }
            }
        });
    }

    public void broadcastMessage(String category, String message, Object... args)
    {
        for (Player player : this.server.getOnlinePlayers())
        {
            this.getExactUser(player).sendMessage(category, message, args);
        }
    }
    
    public void clearAttributes(Module module)
    {
        for (User user : this.users.values())
        {
            user.clearAttributes(module);
        }
    }
}