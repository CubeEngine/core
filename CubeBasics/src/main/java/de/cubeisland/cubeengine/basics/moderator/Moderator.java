package de.cubeisland.cubeengine.basics.moderator;

import org.bukkit.entity.Player;

/**
 *
 * @author Anselm Brehme
 */
public class Moderator //TODO remove or at least reduce this class
{
    public void sudoCmd(Player player, String command)
    {
        player.chat("/"+command);
    }
    
    public void sudoMsg(Player player, String message)
    {
        player.chat(message);
    }
}