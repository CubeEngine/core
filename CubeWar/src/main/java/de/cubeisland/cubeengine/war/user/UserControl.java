package de.cubeisland.cubeengine.war.user;

import de.cubeisland.cubeengine.war.CubeWar;
import static de.cubeisland.cubeengine.war.CubeWar.t;
import de.cubeisland.cubeengine.war.storage.UserModel;
import de.cubeisland.cubeengine.war.storage.UserStorage;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Effect;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

public class UserControl
{
    private CubeWar plugin = CubeWar.getInstance();
    private Server server = plugin.getServer();
    private Map<OfflinePlayer, WarUser> users;
    private static UserControl instance = null;

    public static UserControl get()
    {
        if (instance == null)
        {
            instance = new UserControl();
        }
        return instance;
    }

    private UserControl()
    {
        users = new HashMap<OfflinePlayer, WarUser>();
    }

    public void loadDataBase()
    {
        UserStorage userDB = UserStorage.get();
        for (UserModel model : userDB.getAll())
        {
            this.users.put(model.getCubeUser().getOfflinePlayer(), new WarUser(model));
        }
    }

    public void updateDataBase(WarUser... userToUpdate)
    {

        if (userToUpdate.length == 0)
        {
            Collection<WarUser> userlist = this.users.values();
            for (WarUser user : userlist)
            {
                user.updateDB();
            }
        }
        else
        {
            for (WarUser user : userToUpdate)
            {
                user.updateDB();
            }
        }
    }

    public WarUser getOfflineUser(OfflinePlayer user)
    {
        if (users.containsKey(user))
        {

            return users.get(user);
        }
        else
        {
            WarUser newuser = new WarUser(user);
            users.put(user, newuser);
            return newuser;
        }
    }

    public WarUser getUser(CommandSender sender)
    {
        if (sender instanceof Player)
        {
            return getOfflineUser((OfflinePlayer) sender);
        }
        return null;
    }

    public WarUser getUser(String name)
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

    public Collection<WarUser> getUsers()
    {
        return this.users.values();
    }

    public void kill(Player killerPlayer, Player killedPlayer)
    {
        WarUser killer = getUser(killerPlayer);
        WarUser killed = getUser(killedPlayer);
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
        this.updateDataBase(killer, killed);
    }

    public void kill(Player killerPlayer, Monster monster)
    {
        WarUser killer = getOfflineUser(killerPlayer);
        killer.kill(monster);
        this.updateDataBase(killer);
    }

    public String getUserKD(WarUser user, WarUser killed, int kill)
    {
        String name = user.getName();
        int k = user.getKills();
        int d = user.getDeath();
        if (kill == 0)
        {
            return t("kd", user.getRank().getName(), name, k, d, user.getKillpoints());
        }
        switch (user.getMode())
        {
            case NORMAL:
            {
                if (kill > 0)
                {
                    return t("kds-", user.getRank().getName(), name, t("kd_n-", k, d), user.getKillpoints(), user.getRank().getDmod());
                }
                else
                {
                    return t("kds+", user.getRank().getName(), name, t("kd_n+", k, d), user.getKillpoints(), killed.getRank().getKmod());
                }
            }
            case KILLRESET:
            {
                if (kill > 0)
                {
                    return t("kds-", user.getRank().getName(), name, t("kd_kr-", k, d), user.getKillpoints(), user.getRank().getDmod());
                }
                else
                {
                    return t("kds+", user.getRank().getName(), name, t("kd_kr+", k, d), user.getKillpoints(), killed.getRank().getKmod());
                }
            }
            case HIGHLANDER:
            {
                if (kill > 0)
                {
                    return t("kds-", user.getRank().getName(), name, t("kd_h-", k, d), user.getKillpoints(), user.getRank().getKmod());
                }
                else
                {
                    return t("kds+", user.getRank().getName(), name, t("kd_h+", k, d), user.getKillpoints(), killed.getRank().getKmod());
                }
            }
        }
        return "#ERROR while getting KD";
    }

    public boolean isAllied(Player player1, Player player2)
    {
        WarUser user1 = getUser(player1);
        WarUser user2 = getUser(player2);
        if (player1.equals(player2))
        {
            return true;
        }
        if (user1.getTeam().getKey() == 0)//WILDLAND
        {
            return false;
        }
        if (user2.getTeam().getKey() == 0)//WILDLAND
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
