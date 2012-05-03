package de.cubeisland.CubeWar;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author Faithcaio
 */
public class Hero {

    private OfflinePlayer player;
    private int death = 0;
    private int kills = 0;
    private Mode mode = Mode.NORMAL;
    
    public Hero(Player player) 
    {
        this.player = player;
    }
    
    public int kill(Hero hero)
    {
        if (mode.equals(Mode.NORMAL))
        {
            return ++this.kills;
        }
        else if (mode.equals(Mode.KILLRESET))
        {
            return ++this.kills;
        }
        else if (mode.equals(Mode.HIGHLANDER))
        {
            this.kills += hero.getKills();
            return this.kills;
        }
        return -1;
    }
    
    public int die()
    {
        if (mode.equals(Mode.NORMAL))
        {
            return ++this.death;
        }
        else if (mode.equals(Mode.KILLRESET))
        {
            this.kills = 0;
            return ++this.death;
        }
        else if (mode.equals(Mode.HIGHLANDER))
        {
            this.kills = 0;
            return ++this.death;

        }
        return -1;
    }
    
    //Getter...
    public int getKills()
    {
        return this.kills;    
    }
    
    public int getDeath()
    {
        return this.death;
    }
    
    public Player getPlayer()
    {
        if (this.player.isOnline())
            return this.player.getPlayer();
        else
            return null;
    }
    
    public String getName()
    {
        return this.player.getName();
    }
}
