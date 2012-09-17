package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.core.BukkitCore;
import de.cubeisland.cubeengine.core.BukkitDependend;
import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.storage.BasicStorage;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.DatabaseUpdater;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.ComponentBuilder;
import static de.cubeisland.cubeengine.core.storage.database.querybuilder.ComponentBuilder.LESS;
import static de.cubeisland.cubeengine.core.storage.database.querybuilder.ComponentBuilder.EQUAL;
import de.cubeisland.cubeengine.core.user.event.UserCreatedEvent;
import de.cubeisland.cubeengine.core.util.Cleanable;
import de.cubeisland.cubeengine.core.util.StringUtils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Locale;
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
        this.initialize();
        this.executor = core.getExecutor();

        this.server = ((BukkitCore)core).getServer();
        this.users = new ConcurrentHashMap<String, User>();

        final long delay = (long)core.getConfiguration().userManagerCleanup;
        this.executor.scheduleAtFixedRate(this, delay, delay, TimeUnit.MINUTES);
        this.core.getEventManager().registerCoreListener(this);
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
                    database.getQueryBuilder().alterTable(table).add("lastseen", AttrType.DATETIME).rawSQL(" DEFAULT CURRENT_TIMESTAMP").end().end());
            }
        }, 1);
    }

    @Override
    protected void prepareStatements(String key, String[] fields)
    {
        try
        {
            super.prepareStatements(key, fields);
            String[] allFields = new String[fields.length + 1];
            allFields[0] = key;
            System.arraycopy(fields, 0, allFields, 1, fields.length);
            this.database.prepareAndStoreStatement(User.class, "getByName", this.database.getQueryBuilder()
                .select(allFields)
                .from(this.table)
                .where()
                .field("player").is(ComponentBuilder.EQUAL).value()
                .end()
                .end());
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while preparing statements for " + this.table, ex);
        }
    }

    /**
     * Custom Getter for getting User from DB by Name
     *
     * @param playername the name
     * @return the User OR null if not found
     */
    public User get(String playername)
    {
        User loadedModel = null;
        try
        {
            ResultSet resulsSet = this.database.preparedQuery(modelClass, "getByName", playername);
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
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while getting Model from Database", ex);
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("Error while creating fresh Model from Database", ex);
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
        User user = this.users.get(name);
        if (user == null)
        {
            user = this.get(name);
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
        if (user != null)
        {
            if (user.isOnline())
            {
                return user;
            }
        }
        return null;
    }

    /**
     * Finds an User
     *
     * @param name the name
     * @return a User
     */
    @BukkitDependend("Uses BukkitServer to get all OnlinePlayers")
    public User findUser(String name)
    {
        //Looking up loaded users
        User user = this.users.get(name);
        if (user == null)
        {
            //Looking up saved users
            user = this.get(name);
            if (user != null)
            {
                this.users.put(name, user);
            }
            if (user == null) //then NO user with exact name
            {
                //Get all online Player and searching for similar names
                Player[] players = this.server.getOnlinePlayers();
                int distance = 5;
                int ld;
                for (Player player : players)
                {
                    String playername = player.getName();
                    if (distance == 1)//if closest found stop searching
                    {
                        break;
                    }
                    if ((name.length() < (playername.length() - 3)) || (name.length() > (playername.length() + 3)))
                    {
                        continue;//length differ by more than 3
                    }
                    ld = StringUtils.getLevenshteinDistance(name.toLowerCase(Locale.ENGLISH), playername.toLowerCase(Locale.ENGLISH));
                    if (ld <= 2)//Max Worddistance 2
                    {
                        if (ld < distance)//Get best match
                        {
                            distance = ld;
                            user = this.getUser(playername);
                        }
                    }
                }
                if (user == null)
                {
                    //Search if name is part of playername
                    int index = 16;
                    for (Player player : players)
                    {
                        if (index == 0) //Found a player that begins with name
                        {
                            break;
                        }
                        //ld = StringUtils.getLevenshteinDistance(name.toLowerCase(Locale.ENGLISH), player.getName().toLowerCase(Locale.ENGLISH));
                        int ind = player.getName().toLowerCase(Locale.ENGLISH).indexOf(name.toLowerCase(Locale.ENGLISH));
                        if (ind != -1)
                        {
                            //name is in playername -> adjust ld
                            if (ind < index) //Lower Index is better match
                            {
                                index = ind;
                                user = this.getUser(player);
                            }
                        }
                    }
                    if ((user == null) && (name.length() > 3))//Search for typo in first part of name (only if name has 4 chars or more)
                    {
                        distance = 3;
                        for (Player player : players)
                        {
                            if (distance == 1)
                            {
                                break;
                            }
                            if (player.getName().length() >= name.length())
                            {
                                String partName = player.getName().substring(0, name.length());
                                ld = StringUtils.getLevenshteinDistance(name.toLowerCase(Locale.ENGLISH), partName.toLowerCase(Locale.ENGLISH));
                                if (ld <= 2)
                                {
                                    if (ld < distance)//Get best match
                                    {
                                        distance = ld;
                                        user = this.getUser(player.getName());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (user != null)
        {
            this.users.put(user.getName(), user);//Adds User to loaded users
        }
        return user;
    }

    @Override
    public void run()
    {
        for (User user : users.values())
        {
            if (!user.isOnline())
            {
                users.remove(user.getName());
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
        event.getPlayer().getServer().getScheduler().scheduleSyncDelayedTask((Plugin)core, new Runnable()
        {
            @Override
            public void run()
            {
                User user = getUser(event.getPlayer());
                user.lastseen = new Timestamp(System.currentTimeMillis());
                update(user);
                users.remove(event.getPlayer().getName());
            }
        }, 1);
    }

    public void cleanDB()
    {
        try
        {
            ResultSet result =
                this.database.query(
                this.database.getQueryBuilder()
                .select(this.key).from(this.table)
                .where().field("lastseen").is(LESS).value()
                .and().field("nogc").is(EQUAL).value(false)
                .end().end(),
                new Timestamp(System.currentTimeMillis()
                - StringUtils.convertTimeToMillis(this.core.getConfiguration().userManagerCleanupDatabase)));
            while (result.next())
            {
                this.deleteByKey(result.getInt("key"));
            }
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("Error while cleaning DB", ex);
        }
    }
}