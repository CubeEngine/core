package de.cubeisland.cubeengine.basics.general;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;

/**
 *
 * @author Faithcaio
 */
public class General
{
    public static void me(Player player, String message)
    {
        player.getServer().broadcastMessage("*"+ player.getName() +" "+ message);
    }
    
    public static boolean msg(Player sender, Player sendTo ,String message)
    {
        if (sendTo == null ) return false;
        sendTo.sendMessage(sender.getName() + " -> You " + message);
        sendTo.sendMessage("You -> " + sendTo.getName()+ " " + message);
        //TODO translation
        return true;
    }
    
    public static List<Player> near(Player player, int radius)
    {
        List<Player> nearPlayers = new ArrayList<Player>();
        //TODO
        return nearPlayers;
    }
    
    public static double seen(Player player)
    {
        return player.getLastPlayed();
    }

    public static void suicide(Player player)
    {
        player.setHealth(0);
        player.getServer().broadcastMessage(player.getName()+" killed himself!");
        //TODO translation different death msg
    }
    
}
