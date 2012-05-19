package de.cubeisland.cubeengine.war.storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Faithcaio
 */
public class UserControl
{
    private Map<OfflinePlayer, User> users = new HashMap<OfflinePlayer, User>();
    
    public void loadDataBase()
    {
        UserStorage userDB = new UserStorage();//TODO aus CubeWar holen
        Collection<UserModel> modellist = userDB.getAll();
        for (UserModel model : modellist)
        {
            this.users.put(model.getCubeUser().getOfflinePlayer(), new User(model));
        }
    }
    
    public User getOfflineUser(OfflinePlayer user)
    {
        if (users.containsKey(user))
        {

            return users.get(user);
        }
        else
        {
            User newuser = new User(user);
            users.put(user, newuser);
            return newuser;
        }
    }

    public User getUser(CommandSender sender)
    {
        if (sender instanceof Player)
        {
            return getOfflineUser((OfflinePlayer) sender);
        }
        return null;
    }
}
