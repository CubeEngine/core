package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.BukkitDependend;
import de.cubeisland.cubeengine.CubeEngine;
import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.storage.BasicStorage;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.ComponentBuilder;
import de.cubeisland.cubeengine.core.user.event.UserCreatedEvent;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.worker.Cleanable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Anselm Brehme
 */
public class UserManager extends BasicStorage<User> implements Cleanable
{
    private final Core core;
    private final ConcurrentHashMap<String, User> users;//TODO clean this up from time to time; need Concurrent Map
    private final Server server;

    public UserManager(Core core, Server server)
    {
        super(core.getDB(), User.class);
        this.core = core;
        this.initialize();

        this.server = server;
        this.users = new ConcurrentHashMap<String, User>();
        
        final UserManager uM = this;
        Thread thread = new Thread(new Runnable()
        {
            public void run()
            {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {

                    @Override
                    public void run()
                    {
                        uM.cleanUp();
                    }
                }, 0, 10*1000);//All 10 sec //TODO later 10 min
            }
        });
        thread.start();
    }

    @Override
    protected void prepareStatements(String key, String[] fields) throws SQLException
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

    public UserManager addUser(User user)
    {
        this.store(user);
        this.users.put(user.getName(), user);
        UserCreatedEvent event = new UserCreatedEvent(this.core, user);
        server.getPluginManager().callEvent(event);
        return this;
    }

    public UserManager removeUser(User user)
    {
        this.delete(user);
        this.users.remove(user.getName());

        return this;
    }

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

    public User getUser(OfflinePlayer player)
    {
        return this.getUser(player.getName());
    }

    public User getUser(Player player)
    {
        return this.getUser(player.getName());
    }

    public User getUser(CommandSender sender)
    {
        return this.getUser(sender.getName());
    }

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

    public void clean()
    {
        this.users.clear();
    }

    /**
     * Finds an online User
     *
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
     * @return a User
     */
    @BukkitDependend("Uses BukkitServer to get all OnlinePlayers")
    public User findUser(String name)//TODO tests for this
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
        }
        if (user == null) //then NO user with exact name
        {
            //Get all online Player and searching for similar names
            Player[] players = CubeEngine.getServer().getOnlinePlayers();
            for (Player player : players)
            {
                if (StringUtils.wordDistance(name, player.getName()) <= 2)//Max Worddistance 2
                {
                    user = this.get(player.getName());//Gets User from DB
                    this.users.put(user.getName(), user);//Adds User to loaded users
                    return user;//First found is returned
                }
            }
        }
        return user;
    }

    public void cleanUp()//removes all users that are not online
    {
        for (User user : users.values())
        {
            if (!user.isOnline())
            {
                users.remove(user.getName());
            }
        }
    }
}