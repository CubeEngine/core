package de.cubeisland.cubeengine.war.user;

import de.cubeisland.cubeengine.war.CubeWar;
import static de.cubeisland.cubeengine.war.CubeWar.t;
import de.cubeisland.cubeengine.war.database.UserStorage;
import de.cubeisland.cubeengine.war.groups.AreaType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Effect;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

/**
 *
 * @author Faithcaio
 */
public class UserControl
{

    private Map<OfflinePlayer, User> users = new HashMap<OfflinePlayer, User>();
    private Server server = CubeWar.getInstance().getServer();
    private UserStorage userDB;

    public UserControl()
    {
    }

    public void loadDB()
    {
        userDB = CubeWar.getInstance().getUserDB();
        for (User user : userDB.getAll())
        {
            users.put(user.getOfflinePlayer(), user);
        }
    }

    public void kill(Player killerPlayer, Player killedPlayer)
    {
        User killer = getUser(killerPlayer);
        User killed = getUser(killedPlayer);
        if (killer.equals(killed))
        {
            //Suicide
            killerPlayer.sendMessage(t("selfown"));
            killed.die();
            killerPlayer.playEffect(killerPlayer.getLocation(), Effect.POTION_BREAK, 0);
        }
        else
        {
            if (killer.getMode().equals(PlayerMode.HIGHLANDER))
            {
                killerPlayer.sendMessage(t("killer", getUserKD(killed, killed, killed.getKills() + 1)));
            }
            else
            {
                killerPlayer.sendMessage(t("killer", getUserKD(killed, killed, 1)));
            }
            killedPlayer.sendMessage(t("killed", getUserKD(killer, killed, -1)));
            killer.kill(killed);
            killed.die();
        }
        userDB.update(killed);
        userDB.update(killer);
    }

    public void kill(Player killerPlayer, Monster monster)
    {
        User killer = getOfflineUser(killerPlayer);
        killer.kill(monster);
        userDB.update(killer);
    }

    public String getUserKD(User user, User killed, int kill)
    {
        String name = user.getName();
        int k = user.getKills();
        int d = user.getDeath();
        if (kill == 0)
        {
            return t("kd", user.getRank().getName(), name, k, d, user.getKp());
        }
        switch (user.getMode())
        {
            case NORMAL:
            {
                if (kill > 0)
                {
                    return t("kds-", user.getRank().getName(), name, t("kd_n-", k, d), user.getKp(), user.getRank().getDmod());
                }
                else
                {
                    return t("kds+", user.getRank().getName(), name, t("kd_n+", k, d), user.getKp(), killed.getRank().getKmod());
                }
            }
            case KILLRESET:
            {
                if (kill > 0)
                {
                    return t("kds-", user.getRank().getName(), name, t("kd_kr-", k, d), user.getKp(), user.getRank().getDmod());
                }
                else
                {
                    return t("kds+", user.getRank().getName(), name, t("kd_kr+", k, d), user.getKp(), killed.getRank().getKmod());
                }
            }
            case HIGHLANDER:
            {
                if (kill > 0)
                {
                    return t("kds-", user.getRank().getName(), name, t("kd_h-", k, d), user.getKp(), user.getRank().getKmod());
                }
                else
                {
                    return t("kds+", user.getRank().getName(), name, t("kd_h+", k, d), user.getKp(), killed.getRank().getKmod());
                }
            }
        }
        return "#ERROR while getting KD";
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
            userDB.store(newuser);
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

    public User getUser(String name)
    {
        if (server.getPlayer(name) != null)
        {
            return getOfflineUser(server.getPlayer(name));
        }
        OfflinePlayer[] list = server.getOfflinePlayers();
        for (OfflinePlayer player : list)
        {
            if (player.getName().equalsIgnoreCase(name))
            {
                return getOfflineUser(player);
            }
        }
        return null;
    }
    
    public Collection<User> getUsers()
    {
        return this.users.values();
    }

    public boolean isAllied(Player player1, Player player2)
    {
        User user1 = getUser(player1);
        User user2 = getUser(player2);
        if (player1.equals(player2))
        {
            return true;
        }
        if (user1.getTeam().getType().equals(AreaType.WILDLAND))
        {
            return false;
        }
        if (user2.getTeam().getType().equals(AreaType.WILDLAND))
        {
            return false;
        }
        if (user1.getTeam().equals(user2.getTeam()))
        {
            return true;
        }
        if (user1.getTeam().isTrueAlly(user2.getTeam()))
        {
            return true;
        }
        return false;
    }
}
