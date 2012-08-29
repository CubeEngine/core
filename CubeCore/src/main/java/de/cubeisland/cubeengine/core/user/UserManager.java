package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.storage.BasicStorage;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.ComponentBuilder;
import de.cubeisland.cubeengine.core.user.event.UserCreatedEvent;
import gnu.trove.map.hash.THashMap;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Anselm Brehme
 */
public class UserManager extends BasicStorage<User>
{
    private final Core core;
    private final THashMap<String, User> users;
    private final Server server;

    public UserManager(Core core, Server server)
    {
        super(core.getDB(), User.class);
        this.core = core;
        this.initialize();

        this.server = server;

        this.users = new THashMap<String, User>();
        for (User user : this.getAll())
        {
            this.users.put(user.getName(), user);
        }
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
            .end()
            );
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
                loadedModel = (User)modelClass.getConstructors()[0].newInstance(values.toArray());
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
            this.addUser(new User(name));
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
}