package de.cubeisland.CubeWar;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author Faithcaio
 */
public class Hero {

    private final CubeWarConfiguration config = CubeWar.getInstance().getConfiguration();
    
    private OfflinePlayer player;
    private int death = 0;
    private int kills = 0;
    private int killpoints = 0;
    private Mode mode = Mode.NORMAL;
    private Rank rank = config.cubewar_ranks.get(0);
    
    public Hero(Player player) 
    {
        this.player = player;
        rank = rank.newRank(this);
    }
    
    public int kill(Hero hero)
    {
        this.killpoints += hero.getRank().getKmod();
        return this.kill_kd(hero);
    }
    
    private int kill_kd(Hero hero)
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
        this.killpoints -= this.rank.getDmod();
        return this.die_kd();
    }
    
    private int die_kd()
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
    
    public Mode getMode()
    {
        return this.mode;
    }
    
    public int getKp()
    {
        return this.killpoints;
    }
    
    public Rank getRank()
    {
        return this.rank;
    }
}
