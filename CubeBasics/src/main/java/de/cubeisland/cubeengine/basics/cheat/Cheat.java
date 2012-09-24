package de.cubeisland.cubeengine.basics.cheat;

import org.bukkit.entity.Player;

/**
 *
 * @author Anselm Brehme
 */
public class Cheat  //TODO remove or at least reduce this class
{
    public void ptime(Player player, long time)
    {
        player.setPlayerTime(time, false);
    }
    
    public void resetptime(Player player)
    {
        player.resetPlayerTime();
    }
}