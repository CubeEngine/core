package de.cubeisland.cubeengine.war.storage;

import de.cubeisland.cubeengine.core.user.CubeUser;
import de.cubeisland.cubeengine.core.user.CubeUserManager;
import de.cubeisland.cubeengine.war.CubeWar;
import static de.cubeisland.cubeengine.war.CubeWar.t;
import de.cubeisland.cubeengine.war.CubeWarConfiguration;
import de.cubeisland.cubeengine.war.groups.AreaType;
import de.cubeisland.cubeengine.war.user.PlayerMode;
import de.cubeisland.cubeengine.war.user.Rank;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;

/**
 *
 * @author Faithcaio
 */
public class User
{
    private CubeWarConfiguration config = CubeWar.getInstance().getConfiguration();
    private UserStorage userDB = UserStorage.get();
    private UserControl users = UserControl.get();
    private GroupControl groups = GroupControl.get();
    
    protected UserModel model;

    public User(UserModel model)
    {
        this.model = model;
    }

    public User(OfflinePlayer player)
    {
        this(CubeUserManager.getInstance().getCubeUser(player));
    }

    public User(CubeUser cubeUser)
    {
        this.model = new UserModel(cubeUser);
        this.userDB.store(this.model);
    }

    public void updateDB()
    {
        this.userDB.update(this.model);
    }

    public int getId()
    {
        return model.getId();
    }

    public Rank getRank()
    {
        return model.getRank();
    }

    public void kill(User user)
    {
        model.addKillpoints(user.getRank().getKmod());
        this.kill_kd(user);
        this.updateRank();
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
        model.addKillpoints(kp);
        this.updateRank();
    }
  
    public int getKills()
    {
        return model.getKills();
    }

    private void kill_kd(User user)
    {
        PlayerMode mode = model.getMode();
        switch (mode)
        {
            case NORMAL:
                model.addKills(1);
                break;
            case KILLRESET:
                model.addKills(1);
                break;
            case HIGHLANDER:
                model.addKills(user.getKills());
                break;
        }
    }

    public void updateRank()
    {
        model.setRank(Rank.newRank(model.getKillpoints()));
    }

    public void die()
    {
        model.addKillpoints(-model.getRank().getDmod());
        if (model.getKillpoints() < config.killpoint_min)
        {
            model.setKillpoints(config.killpoint_min);
        }
        this.die_kd();
        this.updateRank();
    }

    private void die_kd()
    {
        PlayerMode mode = model.getMode();
        switch (mode)
        {
            case NORMAL:
                model.addDeath(1);
                break;
            case KILLRESET:
                model.addDeath(1);
                model.setKills(0);
                break;
            case HIGHLANDER:
                model.addDeath(1);
                model.setKills(0);
                break;
        }
    }

    public Player getPlayer()
    {
        return model.getCubeUser().getPlayer();
    }

    OfflinePlayer getOfflinePlayer()
    {
        return model.getCubeUser().getOfflinePlayer();
    }

    public String getName()
    {
        return model.getCubeUser().getName();
    }

    public PlayerMode getMode()
    {
        return model.getMode();
    }

    public int getKillpoints()
    {
        return model.getKillpoints();
    }

    public void setTeam(Group team)
    {
        model.setTeam(team);
        this.updateDB();
    }

    public Group getTeam()
    {
        return model.getTeam();
    }

    public String getTeamTag()
    {
        return model.getTeam().getTag();
    }

    public boolean isRespawning()
    {
        return model.isRespawning();
    }

    public void setRespawning(boolean respawning)
    {
        model.setRespawning(respawning);
    }

    public void resetBypasses()
    {
        model.resetBypasses();
    }

    public void toggleBypass(String bypass)
    {
        if (model.getBypasses().contains(bypass))
        {
            model.removeBypass(bypass);
        }
        else
        {
            model.addBypass(bypass);
        }
    }

    public boolean hasBypass(String bypass)
    {
        return model.getBypasses().contains(bypass);
    }

    public void addInfluence(double amount)
    {
        model.addInfluence(amount);
    }

    public void looseInfluence(double amount)
    {
        model.addInfluence(-amount);
    }

    public double getBaseInfluence()
    {
        return model.getInfluence();
    }

    public double getTotalInfluence()
    {
        return model.getInfluence() * model.getRank().getImod();
    }

    public void showInfo(CommandSender sender)
    {
        sender.sendMessage(t("user_01"));
        sender.sendMessage(t("user_02", this.getName()));
        sender.sendMessage(t("user_03", model.getRank().getName(), model.getKillpoints()));
        int kills = model.getKills();
        int death = model.getDeath();
        int kd;
        if (death == 0)
        {
            kd = 0;
        }
        else
        {
            kd = (int) (kills / death * 100);
        }
        sender.sendMessage(t("user_07", (int) this.getTotalInfluence()));
        sender.sendMessage(t("user_04", kills, death, String.valueOf(kd / 100)));
        Group team = model.getTeam();
        if ((team != null) && (!team.getType().equals(AreaType.WILDLAND)))
        {
            if (team.isTrueAlly(users.getUser(sender).getTeam()))
            {
                sender.sendMessage(t("user_051", this.getTeamTag()));
            }
            else
            {
                sender.sendMessage(t("user_052", this.getTeamTag()));
            }
        }
        if (sender instanceof Player)
        {
            if (this.equals(users.getUser(sender)))
            {
                sender.sendMessage(t("user_06", groups.getGroupAtLocation((Player) sender).getTag()));
            }
        }
    }

    public int getDeath()
    {
        return model.getDeath();
    }
}
