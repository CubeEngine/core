package de.cubeisland.cubeengine.basics.moderator;

import org.bukkit.entity.Player;

/**
 *
 * @author Faithcaio
 */
public class Moderator
{
    public static void broadcast(Player player, String message)
    {
        player.getServer().broadcastMessage(message);
    }
    
    public static void clearinventory(Player player)
    {
        player.getInventory().clear();
        //?? save inventory to restore later ??
        player.updateInventory();
        player.sendMessage("Inventory cleared!");
        //TOdO msg
    }
    
    public static void kill(Player player)
    {
        player.setHealth(0);
        player.getServer().broadcastMessage(player.getName() + " found his death!");
    }
    
    public static void ping(Player player)
    {
        player.sendMessage("Pong!");
    }
    
    public static void removeDrops(Player player, int radius)
    {
        //TODO
    }
   
    public static void removeArrows(Player player, int radius)
    {
        //TODO
    }
    
    public static void removeBoats(Player player, int radius)
    {
        //TODO
    }
    
    public static void removeMinecarts(Player player, int radius)
    {
        //TODO
    }
    
    public static void removeXp(Player player, int radius)
    {
        //TODO
    }
    public static void removePaintings(Player player, int radius)
    {
        //TODO
    }
    
    public static void sudoCmd(Player player, String command)
    {
        player.chat("/"+command);
    }
    
    public static void sudoMsg(Player player, String message)
    {
        player.chat(message);
    }
            
}
