package de.cubeisland.cubeengine.war.user;

import de.cubeisland.cubeengine.war.CubeWar;
import static de.cubeisland.cubeengine.war.CubeWar.t;
import de.cubeisland.cubeengine.war.CubeWarConfiguration;
import de.cubeisland.cubeengine.war.groups.Group;
import de.cubeisland.cubeengine.war.groups.GroupControl;
import java.util.HashSet;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;

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
    private boolean fly_disable;
    private double bounty; //TODO Kopfgeld
    private HashSet<String> bypasses;
    
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
        int kp=0;
        if (monster instanceof Chicken) kp = config.killKP.get("Chicken");
        if (monster instanceof Cow) kp = config.killKP.get("Cow");
        if (monster instanceof MushroomCow) kp = config.killKP.get("Mooshroom");
        if (monster instanceof Ocelot) kp = config.killKP.get("Ocelot");
        if (monster instanceof Pig) kp = config.killKP.get("Pig");
        if (monster instanceof Sheep) kp = config.killKP.get("Sheep");
        if (monster instanceof Squid) kp = config.killKP.get("Squid");
        if (monster instanceof Villager) kp = config.killKP.get("Villager");
        if (monster instanceof Enderman) kp = config.killKP.get("Enderman");
        if (monster instanceof Wolf) kp = config.killKP.get("Wolf");
        if (monster instanceof PigZombie) kp = config.killKP.get("ZombiePigman");
        if (monster instanceof Blaze) kp = config.killKP.get("Blaze");
        if (monster instanceof CaveSpider) kp = config.killKP.get("CaveSpider");
        if (monster instanceof Creeper) kp = config.killKP.get("Creeper");
        if (monster instanceof Ghast) kp = config.killKP.get("Ghast");
        if (monster instanceof MagmaCube) kp = config.killKP.get("MagmaCube");
        if (monster instanceof Silverfish) kp = config.killKP.get("Silverfish");
        if (monster instanceof Skeleton) kp = config.killKP.get("Skeleton");
        if (monster instanceof Slime) kp = config.killKP.get("Slime");
        if (monster instanceof Spider) kp = config.killKP.get("Spider");
        if (monster instanceof Zombie) kp = config.killKP.get("Zombie");
        if (monster instanceof Snowman) kp = config.killKP.get("SnowGolem");
        if (monster instanceof IronGolem) kp = config.killKP.get("IronGolem");
        if (monster instanceof EnderDragon) kp = config.killKP.get("EnderDragon");
        if (monster instanceof Giant) kp = config.killKP.get("Giant");
        this.killpoints += kp;
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
        if (this.team != null)
            if (this.team.isTrueAlly(Users.getUser(sender).getTeam()))
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

    /**
     * @return the fly_disable
     */
    public boolean isFly_disable() {
        return fly_disable;
    }

    /**
     * @param fly_disable the fly_disable to set
     */
    public void setFly_disable(boolean fly_disable) {
        this.fly_disable = fly_disable;
    }
    
    public void unsetBypasses()
    {
        this.bypasses.clear();
    }
    
    public void toggleBypass(String bypass)
    {
        if (this.hasBypass(bypass))
            this.bypasses.remove(bypass);
        else
            this.bypasses.add(bypass);
    }
    
    public boolean hasBypass(String bypass)
    {
        return this.bypasses.contains(bypass);
    }
    
    
}
