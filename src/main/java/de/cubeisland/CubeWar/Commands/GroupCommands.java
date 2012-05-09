package de.cubeisland.CubeWar.Commands;

import static de.cubeisland.CubeWar.CubeWar.t;
import de.cubeisland.CubeWar.Groups.Group;
import de.cubeisland.CubeWar.Groups.GroupControl;
import de.cubeisland.CubeWar.Perm;
import de.cubeisland.CubeWar.User.User;
import de.cubeisland.CubeWar.User.Users;
import de.cubeisland.libMinecraft.command.Command;
import de.cubeisland.libMinecraft.command.CommandArgs;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Faithcaio
 */
public class GroupCommands {

    GroupControl groupcontrol = GroupControl.get();
    
    public GroupCommands() 
    {
    
    }
    
    @Command(usage = "Team|Arena <Tag> <Name>", aliases = {"c"})
    public boolean create(CommandSender sender, CommandArgs args)
    {
        if (Perm.command_create_BP.hasNotPerm(sender))
            if (Perm.command_create.hasNotPerm(sender)) return true;
        if (args.size()>0)
        {
            String t = args.getString(0);
            if ((t.equalsIgnoreCase("Team"))||(t.equalsIgnoreCase("t")))
            {
                if (Perm.command_create_BP.hasNotPerm(sender))
                    if (Perm.command_create_team.hasNotPerm(sender)) return true;
                if (args.size() > 2)
                {
                    String tag = args.getString(1);
                    String name = args.getString(2);
                    if (tag.equalsIgnoreCase("all"))
                    {
                        sender.sendMessage(t("create_tag_all"));
                        return true;
                    }
                    if (!groupcontrol.freeTag(tag))
                    {
                        sender.sendMessage(t("create_tag_used",GroupControl.get().getGroup(tag).getName()));
                        return true;
                    }
                    for (int i = 3; i < args.size();++i)
                    {
                        name += " "+args.getString(i); 
                    }
                    Group team = groupcontrol.newTeam(tag, name);
                    User user = Users.getUser(sender);
                    if (user.getTeam() == null)
                    {
                        team.addAdmin(Users.getUser(sender));
                        sender.sendMessage(t("i")+t("ct1", tag, name));
                    }
                    else
                    {
                        sender.sendMessage(t("i")+t("ct2", tag, name));
                    }    
                    return true;
                }
                else
                    return false;
            }
            else if ((t.equalsIgnoreCase("Arena"))||(t.equalsIgnoreCase("a")))
            {
                if (Perm.command_create_BP.hasNotPerm(sender))
                    if (Perm.command_create_arena.hasNotPerm(sender)) return true;
                if (args.size() > 2)
                {
                    String tag = args.getString(1);
                    String name = args.getString(2);
                    if (!groupcontrol.freeTag(tag))
                    {
                        sender.sendMessage(t("create_tag_used",GroupControl.get().getGroup(tag).getTag()));
                        return true;
                    }
                    for (int i = 3; i < args.size();++i)
                    {
                        name += " "+args.getString(i); 
                    }
                    groupcontrol.newArena(tag, name);
                    sender.sendMessage(t("i")+t("ca", tag, name));
                    return true;
                }
                else
                    return false;
            }
        }
        return false;

    }
    
    @Command(usage = "<Tag> <Key> <Value>", aliases = {"m"})
    public boolean modify(CommandSender sender, CommandArgs args)
    {
        if (Perm.command_modify_BP.hasNotPerm(sender))
            if (Perm.command_modify.hasNotPerm(sender)) return true;
        if (args.size() > 2)
        {
            Group group = GroupControl.get().getGroup(args.getString(0).substring(1));
            String val = args.getString(2);
            if (group != null)
            {
                if (args.getString(1).equalsIgnoreCase("tag"))
                {
                    sender.sendMessage(t("m_tag"));
                    return true;
                }
                for (int i = 3; i < args.size();++i)
                {
                    val += " "+args.getString(i); 
                }
                if (groupcontrol.setGroupValue(group.getId(), args.getString(1), val))
                {
                    sender.sendMessage(t("i")+t("m_keyset",args.getString(1),val));
                    return true;
                }
                else
                    sender.sendMessage(t("e")+t("m_invalid"));
            }
            else
                sender.sendMessage(t("e")+t("m_noGroupExist",args.getString(0)));
            return true;
        }
        else
            return false;
    }
    
    @Command(usage = "admin|mod <Player>", aliases = {"pos","lead"})
    public boolean position(CommandSender sender, CommandArgs args)
    {
        if (Perm.command_position_BP.hasNotPerm(sender))
            if (Perm.command_position.hasNotPerm(sender)) return true;
        if (args.size() > 2)    
        {
            String t = args.getString(0);
            User user = Users.getUser(args.getString(1));
            Group area = user.getTeam();
            if (t.equalsIgnoreCase("admin")||t.equalsIgnoreCase("a"))
            {
                if (Perm.command_position_BP.hasNotPerm(sender))
                    if (Perm.command_position_admin.hasNotPerm(sender)) return true;
                return this.toggleTeamPos(sender, user, area, "admin");
            }
            else if (t.equalsIgnoreCase("mod")||t.equalsIgnoreCase("m"))
            {
                if (Perm.command_position_BP.hasNotPerm(sender))
                    if (Perm.command_position_mod.hasNotPerm(sender)) return true;
                return this.toggleTeamPos(sender, user, area, "mod");
            }
        }
        return false;
    }

    @Command(usage = "<Tag>")
    public boolean join(CommandSender sender, CommandArgs args)
    {
        if (Perm.command_membercontrol_BP.hasNotPerm(sender))
        if (Perm.command_join.hasNotPerm(sender)) return true;
        if (args.size() > 0)
        {
            Group group = groupcontrol.getGroup(args.getString(0));
            if (group == null)
            {
                sender.sendMessage(t("e")+t("team_noTag",args.getString(0)));
                return true;
            }
            User user = Users.getUser(sender);
            return this.toggleTeamPos(sender, user, group, "userjoin");
        }
        return false;
    }
    
    @Command(usage = "")
    public boolean leave(CommandSender sender, CommandArgs args)
    {
        if (Perm.command_membercontrol_BP.hasNotPerm(sender))
            if (Perm.command_leave.hasNotPerm(sender)) return true;
        if (args.isEmpty())
        {
            User user = Users.getUser(sender);
            return this.toggleTeamPos(sender, user, user.getTeam(), "userleave");
        }
        return false;
    }
    
    @Command(usage = "<Player>")
    public boolean kick(CommandSender sender, CommandArgs args)
    {
        if (Perm.command_membercontrol_BP.hasNotPerm(sender))
            if (Perm.command_kick.hasNotPerm(sender)) return true;
        if (args.size() > 0)
        {
            User user = Users.getUser(args.getString(0));
            if (user != null )
            {
                if (user.getTeam() == null)
                {
                    sender.sendMessage(t("team_noteam",user.getName()));
                    return true;
                }
                if (Perm.command_membercontrol_BP.hasNotPerm(sender))
                    if (! user.getTeam().equals(Users.getUser(sender).getTeam()))
                        if (Perm.command_kick_other.hasNotPerm(sender)) return true;
                user.getPlayer().sendMessage(t("i")+t("team_kick",user.getTeamTag()));
                return this.toggleTeamPos(sender, user, user.getTeam(), "userleave");
            }
               
        }
        return false;
    }
    
    private boolean toggleTeamPos(CommandSender sender, User user, Group area, String position)
    {
        if (user == null)
        {
            sender.sendMessage(t("e")+t("g_noPlayer"));
            return true;
        }
        if (area == null)
        {
            sender.sendMessage(t("g_noGroup"));
            return true;
        }
        if (position.equalsIgnoreCase("admin"))
        {
            if (area.isAdmin(user))
            {
                area.addUser(user);
                sender.sendMessage(t("i")+t("team_nolonger_admin",area.getTag()));
                return true;
            }
            else
            {
                if (area.isUser(user))
                {
                    area.addAdmin(user);
                    sender.sendMessage(t("i")+t("team_isnow_admin",area.getTag()));
                }
                else
                {
                    if (user.getName().equals(sender.getName()))
                        sender.sendMessage(t("e")+t("team_joinfirst_you",area.getTag()));
                    else
                        sender.sendMessage(t("e")+t("team_joinfirst",user.getName(),area.getTag()));
                }
             }
            return true;
        }
        
        if (position.equalsIgnoreCase("mod"))
        {
            if (area.isMod(user))
            {
                area.addUser(user);
                sender.sendMessage(t("i")+t("team_nolonger_mod",area.getTag()));
                return true;
            }
            else
            {
                if (area.isUser(user))
                {
                    area.addMod(user);
                    sender.sendMessage(t("i")+t("team_isnow_mod",area.getTag()));
                }
                else
                {
                    if (user.getName().equals(sender.getName()))
                        sender.sendMessage(t("e")+t("team_joinfirst_you",area.getTag()));
                    else
                        sender.sendMessage(t("e")+t("team_joinfirst",user.getName(),area.getTag()));
                }    
                    
            }
            return true;
        }
        
        if (position.equalsIgnoreCase("userjoin"))
        {
            if (user.getTeam()!=null)
            {
                if (area.isUser(user))
                    sender.sendMessage(t("e")+t("team_joined",area.getTag()));
                else
                    sender.sendMessage(t("e")+t("team_leavefirst",area.getTag()));
                return true;
            }
            else
            {
                if (area.isInvited(user))
                {
                    if (!area.isBalanced(user))
                    {
                        sender.sendMessage(t("team_isunbalanced"));
                        return true;
                    }
                    area.addUser(user);
                    sender.sendMessage(t("i")+t("team_isnow_mem",area.getTag()));
                }
                else
                    sender.sendMessage(t("g_14"));
            }
            return true;
        }
        
        if (position.equalsIgnoreCase("userleave"))
        {
            if (user.getTeam()==null)
            {
                sender.sendMessage(t("e")+t("team_noleave",user.getName()));
                return true;
            }
            if (area == null) area = user.getTeam();
            area.delUser(user);
            if (user.getName().equals(sender.getName()))
                sender.sendMessage(t("i")+t("team_nolonger_mem",area.getTag()));
            else
                sender.sendMessage(t("i")+t("team_nolonger_mem_other",user.getName(),area.getTag()));
            return true;
        }
        return false;
    }
    
    @Command(usage = "<Tag> [Tag]")
    public boolean ally(CommandSender sender, CommandArgs args)
    {
        if (Perm.command_relation_BP.hasNotPerm(sender))
            if (Perm.command_relation_change.hasNotPerm(sender)) return true;
        if (args.size() > 1)
        {
            if (Perm.command_relation_BP.hasNotPerm(sender))
                if (Perm.command_relation_change_other.hasNotPerm(sender)) return true;
            Group team = groupcontrol.getGroup(args.getString(0));
            Group team2 = groupcontrol.getGroup(args.getString(1));
            if (team == null)
            {
                sender.sendMessage(t("m_noTeamExist",args.getString(0)));
                return true;
            }
            if (team.equals(team2))
            {
                sender.sendMessage(t("pro")+t("rel_self"));
                return true;
            }
            team.setally(team2);
            team2.setally(team);
            team.sendToAlly(t("rel_ally_both",team.getTag(),team2.getTag()));
            return true;
        }
        if (args.size() > 0)
        {
            Group team2 = groupcontrol.getGroup(args.getString(0));
            Group team = Users.getUser(sender).getTeam();
            if (team == null)
            {
                sender.sendMessage(t("m_noTeam"));
                return true;
            }
            if (team.equals(team2))
            {
                sender.sendMessage(t("pro")+t("rel_self"));
                return true;
            }
            if (team != null)
            {
                team.setally(team2);
                if (team2.isAlly(team))
                {
                    team.sendToAlly(t("rel_ally_both",team.getTag(),team2.getTag()));
                }
                else
                {
                    team.sendToTeam(t("rel_ally_propose",team2.getTag()));
                    team2.sendToTeam(t("rel_ally_proposal",team.getTag()));
                }
                return true;
            }
        }
        return false;
    }
    
    @Command(usage = "<Tag> [Tag]")
    public boolean enemy(CommandSender sender, CommandArgs args)
    {
        if (Perm.command_relation_BP.hasNotPerm(sender))
            if (Perm.command_relation_change.hasNotPerm(sender)) return true;
        if (args.size() > 1)
        {
            if (Perm.command_relation_BP.hasNotPerm(sender))
                if (Perm.command_relation_change_other.hasNotPerm(sender)) return true;
            Group team = groupcontrol.getGroup(args.getString(0));
            Group team2 = groupcontrol.getGroup(args.getString(1));
            if (team == null)
            {
                sender.sendMessage(t("m_noTeamExist",args.getString(0)));
                return true;
            }
            if (team.equals(team2))
            {
                sender.sendMessage(t("pro")+t("rel_self"));
                return true;
            }
            team.setenemy(team2);
            team2.setenemy(team);
            team.sendToTeam(t("rel_enemy_mortal",team2.getTag()));
            team2.sendToTeam(t("rel_enemy_mortal",team.getTag()));
            return true;
        }
        if (args.size() > 0)
        {
            Group team2 = groupcontrol.getGroup(args.getString(0));
            Group team = Users.getUser(sender).getTeam();
            if (team == null)
            {
                sender.sendMessage(t("m_noTeam"));
                return true;
            }
            if (team.equals(team2))
            {
                sender.sendMessage(t("pro")+t("rel_self"));
                return true;
            }
            if (team != null)
            {
                team.setenemy(team2);
                if (team2.isEnemy(team))
                {
                    team.sendToTeam(t("rel_enemy_mortal",team2.getTag()));
                    team2.sendToTeam(t("rel_enemy_mortal",team.getTag()));
                }
                else
                {
                    team.sendToTeam(t("rel_enemy_declare",team2.getTag()));
                    team2.sendToTeam(t("rel_enemy_declared",team.getTag()));
                }
                return true;
            }
        }
        return false;
    }
    
    @Command(usage = "<Tag> [Tag]")
    public boolean neutral(CommandSender sender, CommandArgs args)
    {
        if (Perm.command_relation_BP.hasNotPerm(sender))
            if (Perm.command_relation_change.hasNotPerm(sender)) return true;
        if (args.size() > 1)
        {
            if (Perm.command_relation_BP.hasNotPerm(sender))
                if (Perm.command_relation_change_other.hasNotPerm(sender)) return true;
            Group team = groupcontrol.getGroup(args.getString(0));
            Group team2 = groupcontrol.getGroup(args.getString(1));
            if (team == null)
            {
                sender.sendMessage(t("m_noTeamExist",args.getString(0)));
                return true;
            }
            if (team.equals(team2))
            {
                sender.sendMessage(t("pro")+t("rel_self"));
                return true;
            }
            team.setneutral(team2);
            team2.setneutral(team);
            team.sendToTeam(t("rel_neutral_both",team2.getTag()));
            team2.sendToTeam(t("rel_neutral_both",team.getTag()));
            return true;
        }
        if (args.size() > 0)
        {
            Group team2 = groupcontrol.getGroup(args.getString(0));
            Group team = Users.getUser(sender).getTeam();
            if (team == null)
            {
                sender.sendMessage(t("m_noTeam"));
                return true;
            }
            if (team.equals(team2))
            {
                sender.sendMessage(t("pro")+t("rel_self"));
                return true;
            }
            if (team != null)
            {
                team.setneutral(team2);
                if (team2.isneutral(team))
                {
                    team.sendToTeam(t("rel_neutral_both",team2.getTag()));
                    team2.sendToTeam(t("rel_neutral_both",team.getTag()));
                }
                else if (team2.isEnemy(team))
                {
                    team.sendToTeam(t("rel_neutral_stopwar",team2.getTag()));
                    team2.sendToTeam(t("rel_neutral_stopwar2",team.getTag()));
                }
                else if (team2.isAlly(team))
                {
                    team.sendToTeam(t("rel_neutral_noally",team2.getTag()));
                    team2.sendToTeam(t("rel_neutral_noally2",team.getTag()));
                }
                return true;
            }
        }
        return false;
    }
    
    @Command(usage = "[Tag]")
    public boolean info(CommandSender sender, CommandArgs args)
    {
        if (Perm.command_info.hasNotPerm(sender)) return true;
        if (args.size() > 0)    
        {
            Group group = GroupControl.get().getGroup(args.getString(0));
            if (group == null)
            {
                sender.sendMessage(t("e")+t("m_noGroupExist",args.getString(0)));
                return true;
            }
            if (Perm.command_info_other.hasNotPerm(sender)) return true;
            group.sendInfo(sender);
            return true;
        }
        if (args.isEmpty())
        {
            Group group = Users.getUser(sender).getTeam();
            if (group == null)
            {
                sender.sendMessage(t("e")+t("m_noTeam"));
                return true;
            }
            group.sendInfo(sender);
            return true;
        }
        return false;
    }
    
    @Command(usage = "<Player>")
    public boolean invite(CommandSender sender, CommandArgs args)
    {
        if (Perm.command_membercontrol_BP.hasNotPerm(sender))
            if (Perm.command_invite.hasNotPerm(sender)) return true;
        if (args.size()>0)
        {
            User user = Users.getUser(args.getString(0));
            if (user == null)
            {
                sender.sendMessage(t("g_noPlayer"));
                return true;
            }
            else
            {
                Group team = Users.getUser(sender).getTeam();
                if (team == null)
                {
                    sender.sendMessage(t("m_noTeam"));
                    return true;
                }
                else
                {
                    if (team.invite(user))
                        sender.sendMessage(t("invite_user"));
                    else
                        sender.sendMessage(t("invite_user_already"));
                    return true;
                }
            }
        }
        return false;
    }
    
    @Command(usage = "<Player>")
    public boolean uninvite(CommandSender sender, CommandArgs args)
    {
        if (Perm.command_membercontrol_BP.hasNotPerm(sender))
            if (Perm.command_uninvite.hasNotPerm(sender)) return true;
        if (args.size()>0)
        {
            User user = Users.getUser(args.getString(0));
            if (user == null)
            {
                sender.sendMessage(t("g_noPlayer"));
                return true;
            }
            else
            {
                Group team = Users.getUser(sender).getTeam();
                if (team == null)
                {
                    sender.sendMessage(t("m_noTeam"));
                    return true;
                }
                else
                {
                    if (team.uninvite(user))
                        sender.sendMessage(t("uninvite_user"));
                    else
                        sender.sendMessage(t("uninvite_user_notinvited"));
                    return true;
                }
            }
        }
        return false;
    }
    
    
}
