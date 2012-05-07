package de.cubeisland.CubeWar.User;

import de.cubeisland.CubeWar.CubeWar;
import static de.cubeisland.CubeWar.CubeWar.t;
import de.cubeisland.CubeWar.CubeWarConfiguration;
import de.cubeisland.CubeWar.Groups.Group;
import de.cubeisland.CubeWar.Groups.GroupControl;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

/**
 *
 * @author Faithcaio
 */
public class User {

    private final CubeWarConfiguration config = CubeWar.getInstance().getConfiguration();
    
    private OfflinePlayer player;
    private int death = 0;
    private int kills = 0;
    private int killpoints = 0;
    private PlayerMode mode = PlayerMode.NORMAL;
    private Rank rank;
    private Group team;
    private boolean respawning;
    
    public User(OfflinePlayer player) 
    {
        this.player = player;
        rank = config.cubewar_ranks.get(0);
    }
    
    public int kill(User user)
    {
        this.killpoints += user.getRank().getKmod();
        this.rank = this.rank.newRank(this);
        return this.kill_kd(user);
    }
    
    public void kill(Monster monster)
    {
        //TODO unterscheidung KP in config wenn nicht def +1 kill -0 death
        this.killpoints += 1;
        this.rank = this.rank.newRank(this);
    }
    
    private int kill_kd(User user)
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
            this.kills += user.getKills();
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
    
    public void setTeam(Group team)
    {
        this.team = team;
    }
    
    public Group getTeam()
    {
        return this.team;
    }
    
    public String getTeamTag()
    {
        if (this.team != null)
            return this.team.getTag();
        else
            return t("none");
    }
    
    public void showInfo(CommandSender sender)
    {
        sender.sendMessage(t("user_01"));
        sender.sendMessage(t("user_02",this.getName()));
        sender.sendMessage(t("user_03",this.rank.getName(),this.killpoints));
        int kd;
        if (this.death == 0)
            kd = 0;
        else
            kd = (int)(this.kills/this.death *100);
        sender.sendMessage(t("user_04",this.kills,this.death,String.valueOf(kd/100)));
        if (Users.getUser(sender).getTeam().isTrueAlly(this.team))
            sender.sendMessage(t("user_051",this.getTeamTag()));
        else
            sender.sendMessage(t("user_052",this.getTeamTag()));
        if (sender instanceof Player)
            if (this.equals(Users.getUser(sender)))
                sender.sendMessage(t("user_06",GroupControl.getArea((Player)sender).getTag()));
    }

    /**
     * @return the respawning
     */
    public boolean isRespawning()
    {
        return respawning;
    }

    /**
     * @param respawning the respawning to set
     */
    public void setRespawning(boolean respawning)
    {
        this.respawning = respawning;
    }
}
