package de.cubeisland.CubeWar;

import Area.Area;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Monster;
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
    private PlayerMode mode = PlayerMode.NORMAL;
    private Rank rank;
    private Area team;
    
    public Hero(OfflinePlayer player) 
    {
        this.player = player;
        rank = config.cubewar_ranks.get(0);
    }
    
    public int kill(Hero hero)
    {
        this.killpoints += hero.getRank().getKmod();
        this.rank = this.rank.newRank(this);
        return this.kill_kd(hero);
    }
    
    public void kill(Monster monster)
    {
        //TODO unterscheidung KP in config wenn nicht def +1 kill -0 death
        this.killpoints += 1;
        this.rank = this.rank.newRank(this);
    }
    
    private int kill_kd(Hero hero)
    {
        if (mode.equals(PlayerMode.NORMAL))
        {
            return ++this.kills;
        }
        else if (mode.equals(PlayerMode.KILLRESET))
        {
            return ++this.kills;
        }
        else if (mode.equals(PlayerMode.HIGHLANDER))
        {
            this.kills += hero.getKills();
            return this.kills;
        }
        return -1;
    }
    
    public int die()
    {
        this.killpoints -= this.rank.getDmod();
        if (this.killpoints < config.killpoint_min) this.killpoints = config.killpoint_min;
        this.rank = this.rank.newRank(this);
        return this.die_kd();
                    
    }
    
    private int die_kd()
    {
        if (mode.equals(PlayerMode.NORMAL))
        {
            return ++this.death;
        }
        else if (mode.equals(PlayerMode.KILLRESET))
        {
            this.kills = 0;
            return ++this.death;
        }
        else if (mode.equals(PlayerMode.HIGHLANDER))
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
    
    public PlayerMode getMode()
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
    
    public void setTeam(Area team)
    {
        this.team = team;
    }
    
    public Area getTeam()
    {
        return this.team;
    }
}
