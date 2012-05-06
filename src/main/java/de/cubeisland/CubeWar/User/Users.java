package de.cubeisland.CubeWar.User;

import de.cubeisland.CubeWar.CubeWar;
import static de.cubeisland.CubeWar.CubeWar.t;
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
public class Users {

    private static Map<OfflinePlayer,User> heroes= new HashMap<OfflinePlayer,User>();
    private static Server server = CubeWar.getInstance().getServer();

    public Users() 
    {
    
    }
    
    public static void kill(Player killerPlayer,Player killedPlayer)
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
                killerPlayer.sendMessage(t("killer",getUserKD(killed, killed, killed.getKills()+1)));
            else
                killerPlayer.sendMessage(t("killer",getUserKD(killed, killed, 1)));
            killedPlayer.sendMessage(t("killed",getUserKD(killer, killed, -1)));
            killer.kill(killed);
            killed.die();
        }
    }
    
    public static void kill(Player killerPlayer, Monster monster)
    {
        User killer = getOfflineUser(killerPlayer);
        killer.kill(monster);           
    }
    
    public static String getUserKD(User hero, User killed, int kill)
    {
        String name = hero.getName();
        int k = hero.getKills();
        int d = hero.getDeath();  
        if (kill==0)
        {
            return t("kd",hero.getRank().getName(),name,k,d,hero.getKp());
        }
        switch (hero.getMode())
        {
            case NORMAL:
            {
                if (kill > 0) return t("kds-",hero.getRank().getName(),name,t("kd_n-",k,d),hero.getKp(),hero.getRank().getDmod());
                else return t("kds+",hero.getRank().getName(),name,t("kd_n+",k,d),hero.getKp(),killed.getRank().getKmod());
            }
            case KILLRESET:
            {
                if (kill > 0) return t("kds-",hero.getRank().getName(),name,t("kd_kr-",k,d),hero.getKp(),hero.getRank().getDmod());
                else return t("kds+",hero.getRank().getName(),name,t("kd_kr+",k,d),hero.getKp(),killed.getRank().getKmod());
            }
            case HIGHLANDER:  
            {
                if (kill > 0) return t("kds-",hero.getRank().getName(),name,t("kd_h-",k,d),hero.getKp(),hero.getRank().getKmod());
                else return t("kds+",hero.getRank().getName(),name,t("kd_h+",k,d),hero.getKp(),killed.getRank().getKmod());
            }
        }
        return "#ERROR while getting KD";
    }
    
    public static User getOfflineUser(OfflinePlayer hero)
    {
        if (heroes.containsKey(hero))
            return heroes.get(hero);
        else
        {
            heroes.put(hero, new User(hero));
            return heroes.get(hero);        
        }
    }
    
    public static User getUser(CommandSender sender)
    {
        if (sender instanceof Player)
        {
            return getOfflineUser((OfflinePlayer)sender);
        }
        return null;
    }
    
    public static User getUser(String name)
    {
        if (server.getPlayer(name)!=null) 
            return getOfflineUser(server.getPlayer(name));
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
    
    public static boolean isAllied(Player player1, Player player2)
    {
        User user1 = Users.getUser(player1);
        User user2 = Users.getUser(player2);
        if (player1.equals(player2)) return true;
        if (user1.getTeam()==null) return false;
        if (user2.getTeam()==null) return false;
        if (user1.getTeam().equals(user2.getTeam())) return true;
        if (user1.getTeam().isTrueAlly(user2.getTeam())) return true;    
        return false;
    }

}
