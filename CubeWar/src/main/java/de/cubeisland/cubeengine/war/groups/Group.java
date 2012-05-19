package de.cubeisland.cubeengine.war.groups;

import de.cubeisland.cubeengine.war.CubeWar;
import static de.cubeisland.cubeengine.war.CubeWar.t;
import de.cubeisland.cubeengine.war.storage.GroupModel;
import de.cubeisland.cubeengine.war.storage.GroupStorage;
import de.cubeisland.cubeengine.war.user.User;
import de.cubeisland.cubeengine.war.user.UserControl;
import java.util.ArrayList;
import java.util.List;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Faithcaio
 */
public class Group
{
    private static final Economy econ = CubeWar.getInstance().getEconomy();
    private GroupStorage groupDB = GroupStorage.get();
    GroupControl groups;
    UserControl users;
    protected GroupModel model;

    Group(GroupModel model)
    {
        this.model = model;
        groups = GroupControl.get();
        users = UserControl.get();
    }

    //TODO Method for setting Values cannot use old anymore
    //TODO Bank ist enfernt später mit CubeConomy wieder einfügen
    public int getId()
    {
        return model.getId();
    }

    public void updateDB()
    {
        this.groupDB.update(this.model);
    }

    public String getTag()
    {
        return model.getTag();
    }

    public int getInfluence_used()
    {
        return model.getInfluence_used();
    }

    public void addInfluence_used()
    {
        model.addInfluence_used(1);
    }

    public void remInfluence_used()
    {
        model.addInfluence_used(-1);
    }

    public void resetInfluence_used()
    {
        model.setInfluence_used(0);
    }

    boolean isBalancing()
    {
        return model.hasBit(GroupModel.AUTO_CLOSE);
    }

    public List<User> getUserList()
    {
        List<User> list = new ArrayList<User>();
        list.addAll(0, model.getAdminlist());
        list.addAll(0, model.getModlist());
        list.addAll(0, model.getUserlist());
        return list;
    }

    public int getUserSum()
    {
        return this.getUserList().size();
    }

    public boolean isEnemy(Group g)
    {
        return model.getEnemy().contains(g);
    }

    public boolean isneutral(Group g)
    {
        return (!(model.getEnemy().contains(g) || model.getAlly().contains(g)));
    }

    public void sendInfo(CommandSender sender)
    {
        sender.sendMessage(t("g_01", model.getTag()));
        sender.sendMessage(t("g_02", model.getName()));
        sender.sendMessage(t("g_03", model.getDescription()));
        sender.sendMessage(t("g_04", groups.getRank(this),
                t("g_05", model.getInfluence_used(), model.getInfluence_max())));
        User user = users.getUser(sender);
        Group team = user.getTeam();
        if (team != null && ((team.equals(this)) || (team.isAlly(this) && this.isAlly(team))))
        {
            sender.sendMessage(t("g_06"));
        }
        else
        {
            sender.sendMessage(t("g_07"));
        }
        String pvp;
        if (this.model.hasBit(GroupModel.PVP_ON))
        {
            if (this.model.hasBit(GroupModel.PVP_FRIENDLYFIRE))
            {
                pvp = t("g_081");
            }
            else
            {
                pvp = t("g_083");
            }
        }
        else
        {
            pvp = t("g_082");
        }
        sender.sendMessage(pvp);
        String allies = "";
        String enemies = "";
        for (Group group : groups.getGroups())
        {
            if (!this.equals(group))
            {
                if (this.isTrueAlly(group))
                {
                    allies += ", " + group.getTag();
                }
                else if ((group.isEnemy(this)) || this.isEnemy(group))
                {
                    enemies += ", " + group.getTag();
                }
            }
        }
        if (!allies.isEmpty())
        {
            sender.sendMessage(t("g_09", allies.substring(2)));
        }
        if (!enemies.isEmpty())
        {
            sender.sendMessage(t("g_10", enemies.substring(2)));
        }
        if (this.model.hasBit(GroupModel.ECONOMY_BANK))
        {
            sender.sendMessage(t("g_11", econ.bankBalance("#" + this.getTag())));
        }
        List<User> list = this.getUserList();
        sender.sendMessage(t("g_15", list.size(), this.getKPSum()));
        String onplayer = "";
        List<User> onlist = new ArrayList<User>();
        for (User player : list)
        {
            if (player.getPlayer() != null)
            {
                onplayer += ", " + player.getName();
                onlist.add(player);
            }
        }
        if (!onplayer.isEmpty())
        {
            sender.sendMessage(t("g_12", onlist.size(), onplayer.substring(2)));
        }
        list.removeAll(onlist);
        String offplayer = "";
        for (User player : list)
        {
            if (player.getPlayer() != null)
            {
                offplayer += ", " + player.getName();
            }
        }
        if (!offplayer.isEmpty())
        {
            sender.sendMessage(t("g_13", list.size(), offplayer.substring(2)));
        }
        if (!this.isUser(user))
        {
            if (this.isClosed()
                    || (this.isBalancing() && !this.isBalanced(user)))
            {
                sender.sendMessage(t("g_14"));
            }
        }
    }

    public boolean isTrueAlly(Group g)
    {
        if (this.equals(g))
        {
            return true;
        }
        if (this.model.getAlly().contains(g))
        {
            return g.isAlly(this);
        }
        else
        {
            return false;
        }
    }

    public AreaType getType()
    {
        return model.getType();
    }

    public int getKPSum()
    {
        int kp = 0;
        for (User tmp : this.getUserList())
        {
            kp += tmp.getKillpoints();
        }
        return kp;
    }

    public boolean isBalanced(User user)
    {
        if (this.model.getInvited().contains(user.getName()))
        {
            return true; //Invited Player can always join!
        }
        return GroupControl.get().isBalanced(this);
    }

    public boolean isClosed()
    {
        return model.hasBit(GroupModel.IS_CLOSED);
    }

    public void addAdmin(User user)
    {
        if (user == null)
        {
            return;
        }
        this.model.getAdminlist().add(user);
        this.model.getModlist().remove(user);
        this.model.getUserlist().remove(user);
        user.setTeam(this);
    }

    public void delAdmin(User user)
    {
        this.model.getAdminlist().remove(user);
        user.setTeam(groups.getWildLand());
    }

    public boolean isAdmin(User user)
    {
        return this.model.getAdminlist().contains(user);
    }

    public void addMod(User user)
    {
        if (user == null)
        {
            return;
        }
        this.model.getModlist().add(user);
        this.model.getAdminlist().remove(user);
        this.model.getUserlist().remove(user);
        user.setTeam(this);
    }

    public void delMod(User user)
    {
        this.model.getModlist().remove(user);
        this.model.getAdminlist().remove(user);
        user.setTeam(groups.getWildLand());
    }

    public boolean isMod(User user)
    {
        if (this.model.getAdminlist().contains(user))
        {
            return true;
        }
        return this.model.getModlist().contains(user);
    }

    public void addUser(User user)
    {
        if (user == null)
        {
            return;
        }
        this.model.getUserlist().add(user);
        this.model.getModlist().remove(user);
        this.model.getAdminlist().remove(user);
        user.setTeam(this);
    }

    public void delUser(User user)
    {
        this.model.getUserlist().remove(user);
        this.model.getModlist().remove(user);
        this.model.getAdminlist().remove(user);
        user.setTeam(groups.getWildLand());
    }

    public boolean isUser(User user)
    {
        if (this.model.getAdminlist().contains(user))
        {
            return true;
        }
        if (this.model.getModlist().contains(user))
        {
            return true;
        }
        return this.model.getUserlist().contains(user);
    }

    public boolean invite(User user)
    {
        if (this.model.getInvited().contains(user.getName()))
        {
            return false;
        }
        this.model.getInvited().add(user.getName());
        return true;
    }

    public boolean uninvite(User user)
    {
        if (this.model.getInvited().contains(user.getName()))
        {
            return false;
        }
        this.model.getInvited().remove(user.getName());
        return true;
    }

    public boolean isInvited(User user)
    {
        if (this.model.hasBit(GroupModel.IS_CLOSED))
        {
            if (this.model.getInvited().contains(user.getName()))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        return true;
    }

    public void adjustMaxInfluence()
    {
        int influence = 0;
        for (User user : this.getUserList())
        {
            influence += user.getTotalInfluence();
        }
        this.model.setInfluence_max(influence);
    }

    public void setneutral(Group g)
    {
        this.model.getEnemy().remove(g);
        this.model.getEnemy().remove(g);
    }

    public void setally(Group g)
    {
        this.model.getAlly().add(g);
        this.model.getEnemy().remove(g);
    }

    public void setenemy(Group g)
    {
        this.model.getAlly().remove(g);
        this.model.getEnemy().add(g);
    }

    public boolean isAlly(Group g)
    {
        return model.getAlly().contains(g);
    }

    public void sendToTeam(String msg)
    {
        Player[] players = CubeWar.getInstance().getServer().getOnlinePlayers();
        for (Player player : players)
        {
            if (this.isUser(users.getOfflineUser(player)))
            {
                player.sendMessage(msg);
            }
        }
    }

    public void sendToAlly(String msg)
    {
        this.sendToTeam(msg);
        for (Group theally : this.model.getAlly())
        {
            if (theally.isAlly(this))
            {
                theally.sendToTeam(msg);
            }
        }
    }

    public boolean isPeaceful()
    {
        return this.model.hasBit(GroupModel.IS_PEACEFUL);
    }

    public int getRespawnProtect()
    {
        return this.model.getRespawnProtection();
    }

    public int getInfluence_max()
    {
        return this.model.getInfluence_max();
    }
    
    public String getName()
    {
        return this.model.getName();
    }

    public void setDescription(String desc)
    {
        this.model.setDescription(desc);
    }

    public void toggleBit(int bit)
    {
        this.model.toggleBit(bit);
    }

    public boolean hasBit(int bit)
    {
        return this.model.hasBit(bit);
    }

    public Integer getDmgMod_P()
    {
        return this.model.getDmg_mod_percent();
    }
    
    public Integer getDmgMod_S()
    {
        return this.model.getDmg_mod_set();
    }
    
    public Integer getDmgMod_A()
    {
        return this.model.getDmg_mod_add();
    }
}
