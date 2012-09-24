package de.cubeisland.cubeengine.basics.moderator;

import org.bukkit.entity.Player;

/**
 *
 * @author Anselm Brehme
 */
public class Moderator //TODO remove or at least reduce this class
{
    public void broadcast(Player player, String message)
    {
        player.getServer().broadcastMessage(message);
    }
    
    public void clearinventory(Player player)
    {
        player.getInventory().clear();
        //?? save inventory to restore later ??
        player.updateInventory();
        player.sendMessage("Inventory cleared!");
        //TODO msg woanders
    }
    
    public void sudoCmd(Player player, String command)
    {
        player.chat("/"+command);
    }
    
    public void sudoMsg(Player player, String message)
    {
        player.chat(message);
    }
}
