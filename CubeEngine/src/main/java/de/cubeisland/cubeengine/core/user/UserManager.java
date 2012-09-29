package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.core.BukkitCore;
import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.storage.BasicStorage;
import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.DatabaseUpdater;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.ComponentBuilder;
import static de.cubeisland.cubeengine.core.storage.database.querybuilder.ComponentBuilder.EQUAL;
import static de.cubeisland.cubeengine.core.storage.database.querybuilder.ComponentBuilder.LESS;
import de.cubeisland.cubeengine.core.user.event.UserCreatedEvent;
import de.cubeisland.cubeengine.core.util.Cleanable;
import de.cubeisland.cubeengine.core.util.StringUtils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Anselm Brehme
 */
public class UserManager extends BasicStorage<User> implements Cleanable, Runnable, Listener
{
    private final Core core;
    private final ConcurrentHashMap<String, User> users;
    private final Server server;
    private final ScheduledExecutorService executor;

    public UserManager(final Core core)
    {
        super(core.getDB(), User.class, Core.REVISION);
        this.core = core;
        this.registerUpdaters();
        this.executor = core.getExecutor();

        this.server = ((BukkitCore)core).getServer();
        this.users = new ConcurrentHashMap<String, User>();

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
            
            this.database.prepareAndStoreStatement(modelClass, "cleanup", database.getQueryBuilder()
                .select(key).from(table)
                .where().field("lastseen").is(LESS).value()
                .and().field("nogc").is(EQUAL).value(false)
                .end().end()
            );
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
                    database.getQueryBuilder().alterTable(table).add("nogc", AttrType.BOOLEAN).rawSQL(" DEFAULT false").end().end());
                database.execute(
                    database.getQueryBuilder().alterTable(table).add("lastseen", AttrType.TIMESTAMP).rawSQL(" DEFAULT ").value().end().end(), new Timestamp(System.currentTimeMillis()));
            }
        }, 1);
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
     * Adds the user
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
        this.executor.submit(new Runnable()
        {
            @Override
            public void run()
            {
                store(user);
            }
        });
        UserCreatedEvent event = new UserCreatedEvent(this.core, user);
        server.getPluginManager().callEvent(event);
        return this;
    }

    public void updateUser(final User user)
    {
        this.executor.submit(new Runnable()
        {
            @Override
            public void run()
            {
                update(user);
            }
        });
    }

    /**
     * Removes the user permanently. Data cannot be retrieved
     *
     * @param user the User
     * @return fluent interface
     */
    public UserManager removeUser(final User user)
    {
        this.executor.submit(new Runnable()
        {
            @Override
            public void run()
            {
                delete(user);
            }
        });
        this.users.remove(user.getName());
        return this;
    }

    /**
     * Gets a User by name (creates new User if not found)
     *
     * @param name the name
     * @return the User
     */
    public User getUser(String name)
    {
        if (name == null)
        {
            return null;
        }
        User user = this.users.get(name);
        if (user == null)
        {
            user = this.getFromStorage(name);
        }
        if (user == null)
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
    public User getUser(OfflinePlayer player)
    {
        if (!player.hasPlayedBefore())
        {
            return null;
        }
        return this.getUser(player.getName());
    }

    /**
     * Gets a User by Player (creates new User if not found)
     *
     * @param player the player
     * @return the User
     */
    public User getUser(Player player)
    {
        return this.getUser(player.getName());
    }

    /**
     * Gets a User by CommandSender (creates new User if not found)
     *
     * @param sender the sender
     * @return the User OR null if sender is not a Player
     */
    public User getUser(CommandSender sender)
    {
        if (sender instanceof Player)
        {
            return this.getUser(sender.getName());
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
     * Finds an User
     *
     * @param name the name
     * @return a User
     */
    public User findUser(String name)
    {
        //Looking up loaded users
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
                user = this.getUser(StringUtils.matchString(name, onlinePlayerList));
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
            if (!user.isOnline())
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
        event.getPlayer().getServer().getScheduler().scheduleAsyncDelayedTask((Plugin)core, new Runnable()
        {
            @Override
            public void run()
            {
                User user = getUser(event.getPlayer());
                user.lastseen = new Timestamp(System.currentTimeMillis());
                update(user);
                users.remove(event.getPlayer().getName());
            }
        }, 1L);
    }

    public void cleanup()
    {
        this.executor.submit(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ResultSet result = database.preparedQuery(modelClass, table, 
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
    
    public void broadast(String category, String message, Object... args)
    {
        for (Player player : this.server.getOnlinePlayers())
        {
            this.getUser(player).sendMessage(category, message, args);
        }
    }
}