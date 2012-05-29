package de.cubeisland.cubeengine.war.commands;

import static de.cubeisland.cubeengine.war.CubeWar.t;
import de.cubeisland.cubeengine.war.Perm;
import de.cubeisland.cubeengine.war.groups.AreaType;
import de.cubeisland.cubeengine.war.groups.Group;
import de.cubeisland.cubeengine.war.groups.GroupControl;
import de.cubeisland.cubeengine.war.storage.GroupModel;
import de.cubeisland.cubeengine.war.storage.GroupStorage;
import de.cubeisland.cubeengine.war.user.WarUser;
import de.cubeisland.cubeengine.war.user.UserControl;
import de.cubeisland.libMinecraft.command.Command;
import de.cubeisland.libMinecraft.command.CommandArgs;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Faithcaio
 */
public class GroupCommands {

    private GroupControl groups = GroupControl.get();
    private UserControl users = UserControl.get();
    private GroupStorage groupDB = GroupStorage.get();
    
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
                    if (tag.length() >10)
                    {
                        sender.sendMessage(t("create_tag_long"));
                        return true;
                    }
                    if (name.length() >20)
                    {
                        sender.sendMessage(t("create_name_long"));
                        return true;
                    }
                    if (tag.equalsIgnoreCase("all"))
                    {
                        sender.sendMessage(t("create_tag_all"));
                        return true;
                    }
                    if (!groups.isTagFree(tag))
                    {
                        sender.sendMessage(t("create_tag_used",GroupControl.get().getGroup(tag).getName()));
                        return true;
                    }
                    for (int i = 3; i < args.size();++i)
                    {
                        name += " "+args.getString(i); 
                    }
                    Group team = groups.newTeam(tag, name);
                    WarUser user = users.getUser(sender);
                    if (user.getTeam().getType().equals(AreaType.WILDLAND))
                    {
                        team.addAdmin(user);
                        user.setTeam(team);
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
                    if (!groups.isTagFree(tag))
                    {
                        sender.sendMessage(t("create_tag_used",GroupControl.get().getGroup(tag).getTag()));
                        return true;
                    }
                    for (int i = 3; i < args.size();++i)
                    {
                        name += " "+args.getString(i); 
                    }
                    groups.newArena(tag, name);
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
            Group group = groups.getGroup(args.getString(0));
            String val = args.getString(2);
            if (group != null)
            {
                if (group.getKey()<1)
                {
                    sender.sendMessage(t("no_def_group"));
                    return true;
                }
                if (args.getString(1).equalsIgnoreCase("tag"))
                {
                    sender.sendMessage(t("m_tag"));
                    return true;
                }
                for (int i = 3; i < args.size();++i)
                {
                    val += " "+args.getString(i); 
                }
                if (group.setValue(args.getString(1),val))
                {
                    sender.sendMessage(t("i")+t("m_keyset",args.getString(1),val));
                    group.updateDB();
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
        if (args.size() > 1)    
        {
            String t = args.getString(0);
            WarUser user = users.getUser(args.getString(1));
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
            Group group = groups.getGroup(args.getString(0));
            if (group == null)
            {
                sender.sendMessage(t("e")+t("team_noTag",args.getString(0)));
                return true;
            }
            WarUser user = users.getUser(sender);
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
            WarUser user = users.getUser(sender);
            return this.toggleTeamPos(sender, user, user.getTeam(), "userleave");
        }
        return false;
    }
    
    @Command(usage = "<Tag>", aliases = {"peace"})
    public boolean peaceful(CommandSender sender, CommandArgs args)
    {
        if (Perm.command_peacefull.hasNotPerm(sender)) return true;
        //TODO schaden verhindern wenn user im team PvP...
        if (args.size()>0)
        {
            Group group = groups.getGroup(args.getString(0));
            group.toggleBit(GroupModel.IS_PEACEFUL);
            if (group.isPeaceful())
                sender.sendMessage(t("peace_isnow",group.getTag()));
            else
                sender.sendMessage(t("peace_isnot",group.getTag()));
            return true;
        }
        return false;
    }
    
    @Command(usage = "[-t <Tag>] <description>", aliases = {"desc"})
    public boolean description(CommandSender sender, CommandArgs args)
    {
        if (Perm.command_description.hasNotPerm(sender)) return true;
        if (args.size()>0)
        {
            Group group;   
            int pos=0;
            if (args.hasFlag("t"))
            {
                if (Perm.command_description_other.hasNotPerm(sender)) return true;
                group = groups.getGroup(args.getString(0));
                if (group == null)
                {
                    sender.sendMessage(t("m_noGroupExist",args.getString(0)));
                    return true;
                }
                if (group.getKey()<1)
                {
                    sender.sendMessage(t("no_def_group"));
                    return true;
                }
                pos = 1;
            }
            else
            {
                group = users.getUser(sender).getTeam();
                if (group.getKey()==0)
                {
                    sender.sendMessage(t("m_noTeam"));
                    return true;
                }
            }
            if (args.hasFlag("t") && args.size()<2)
            {
                sender.sendMessage(t("too_few_args"));
                return true;
            }
            String desc = args.getString(pos); 
            for (int i = pos+1; i < args.size();++i)
            {
                desc += " "+args.getString(i); 
            }
            if (desc.length() > 100)
            {
                sender.sendMessage(t("desc_long"));
                return true;
            }
            group.setDescription(desc);
            sender.sendMessage(t("desc_changed",desc));
            group.updateDB();
            return true;
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
            WarUser user = users.getUser(args.getString(0));
            if (user != null )
            {
                if (user.getTeam().getType().equals(AreaType.WILDLAND))
                {
                    sender.sendMessage(t("team_noteam",user.getName()));
                    return true;
                }
                if (Perm.command_membercontrol_BP.hasNotPerm(sender))
                    if (! user.getTeam().equals(users.getUser(sender).getTeam()))
                        if (Perm.command_kick_other.hasNotPerm(sender)) return true;
                user.getPlayer().sendMessage(t("i")+t("team_kick",user.getTeamTag()));
                return this.toggleTeamPos(sender, user, user.getTeam(), "userleave");
            }
               
        }
        return false;
    }
    
    private boolean toggleTeamPos(CommandSender sender, WarUser user, Group area, String position)
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
            if (!user.getTeam().getType().equals(AreaType.WILDLAND))
            {
                if (area.isUser(user))
                    sender.sendMessage(t("e")+t("team_joined",area.getTag()));
                else
                    sender.sendMessage(t("e")+t("team_leavefirst",user.getTeamTag()));
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
            if (user.getTeam().getType().equals(AreaType.WILDLAND))
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
            Group team = groups.getGroup(args.getString(0));
            Group team2 = groups.getGroup(args.getString(1));
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
            Group team2 = groups.getGroup(args.getString(0));
            Group team = users.getUser(sender).getTeam();
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
            Group team = groups.getGroup(args.getString(0));
            Group team2 = groups.getGroup(args.getString(1));
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
            Group team2 = groups.getGroup(args.getString(0));
            Group team = users.getUser(sender).getTeam();
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
            Group team = groups.getGroup(args.getString(0));
            Group team2 = groups.getGroup(args.getString(1));
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
            Group team2 = groups.getGroup(args.getString(0));
            Group team = users.getUser(sender).getTeam();
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
            Group group = users.getUser(sender).getTeam();
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
            WarUser user = users.getUser(args.getString(0));
            if (user == null)
            {
                sender.sendMessage(t("g_noPlayer"));
                return true;
            }
            else
            {
                Group team = users.getUser(sender).getTeam();
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
            WarUser user = users.getUser(args.getString(0));
            if (user == null)
            {
                sender.sendMessage(t("g_noPlayer"));
                return true;
            }
            else
            {
                Group team = users.getUser(sender).getTeam();
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
