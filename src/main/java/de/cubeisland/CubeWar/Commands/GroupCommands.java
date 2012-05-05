package de.cubeisland.CubeWar.Commands;

import Groups.Group;
import Groups.GroupControl;
import static de.cubeisland.CubeWar.CubeWar.t;
import Hero.Hero;
import Hero.Heroes;
import de.cubeisland.libMinecraft.command.Command;
import de.cubeisland.libMinecraft.command.CommandArgs;
import de.cubeisland.libMinecraft.command.CommandPermission;
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
    
    @Command(desc = "Creates a new Team", usage = "<TeamTag> <TeamName>", aliases = {"ct","c"})
    @CommandPermission
    public boolean createTeam(CommandSender sender, CommandArgs args)
    {
        args.size();
        if (args.size() > 1)
        {
            String tag = args.getString(0);
            String name = args.getString(1);
            if (!groupcontrol.freeTag(tag))
            {
                //TODO msg Tag already used
                return true;
            }
            for (int i = 2; i < args.size();++i)
            {
                name += " "+args.getString(i); 
            }
            Group team = groupcontrol.newTeam(tag, name);
            team.addAdmin(Heroes.getHero(sender));
            sender.sendMessage(t("i")+t("ct", tag, name));

            return true;
        }
        else
            return false;
    }
    
    @Command(desc = "Creates a new Arena", usage = "<ArenaTag> <ArenaName>", aliases = {"ca"})
    @CommandPermission
    public boolean createArena(CommandSender sender, CommandArgs args)
    {
        if (args.size() > 1)
        {
            String tag = args.getString(0);
            String name = args.getString(1);
            if (!groupcontrol.freeTag(tag))
            {
                //TODO msg Tag already used
                return true;
            }
            for (int i = 2; i < args.size();++i)
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
    
    @Command(desc = "Modifies a Team", usage = "[#TeamTag] <Key> <Value>", aliases = {"mt","m"})
    @CommandPermission
    public boolean modifyTeam(CommandSender sender, CommandArgs args)
    {
        if (args.size() > 1)
        {
            if (args.getString(0).charAt(0)=='#')
            {
                if (args.size() > 2)
                {
                    Integer area = GroupControl.get().getTeamGroup(args.getString(0).substring(1));
                    String val = args.getString(2);
                    if (area != null)
                    {
                        for (int i = 3; i < args.size();++i)
                        {
                           val += " "+args.getString(i); 
                        }
                        if (groupcontrol.setGroupValue(area, args.getString(1), val))
                        {
                            sender.sendMessage(t("i")+t("m_keyset",args.getString(1),val));
                            return true;
                        }
                        else
                            sender.sendMessage(t("e")+t("m_invalid"));
                    }
                    else
                        sender.sendMessage(t("e")+t("m_noTeamExist"));
                }
                else
                    return false;
            }
            else
            {
               
                Hero hero = Heroes.getHero(sender);
                if (hero == null)
                {
                    sender.sendMessage(t("e")+t("g_noPlayer"));
                    return true;
                }
                Group area = hero.getTeam();
                if (area == null)
                {
                    sender.sendMessage(t("e")+t("m_noTeam"));
                    return true;
                }
                else
                {
                    String val = args.getString(1);
                    for (int i = 2; i < args.size();++i)
                        {
                           val += " "+args.getString(i); 
                        }
                        if (groupcontrol.setGroupValue(area.getId(), args.getString(0), val))
                        {
                            sender.sendMessage(t("i")+t("m_keyset",args.getString(0),val));
                            return true;
                        }
                        else
                            sender.sendMessage(t("e")+t("m_invalid"));
                }
            }
            return true;
        }
        else
        {
            return false;
        }
    }
    
    
    @Command(desc = "modifies an Arena", usage = "<ArenaTag> <Key> <Value>", aliases = {"ma"})
    @CommandPermission
    public boolean modifyArena(CommandSender sender, CommandArgs args)
    {
        if (args.size() > 2)
        {
            Integer area = GroupControl.get().getArenaGroup(args.getString(0));
            String val = args.getString(2);
            if (area != null)
            {
                for (int i = 3; i < args.size();++i)
                {
                    val += " "+args.getString(i); 
                }
                if (groupcontrol.setGroupValue(area, args.getString(1), val))
                {
                    sender.sendMessage(t("i")+t("m_keyset",args.getString(1),val));
                    return true;
                }
                else
                    sender.sendMessage(t("e")+t("m_invalid"));
            }
            else
                sender.sendMessage(t("e")+t("m_noArenaExist"));
            
            return true;
        }
        else
        {
            return false;
        }
    }
    
    @Command(desc = "Toggles Admin State of a Player", usage = "<PlayerName>", aliases = {"admin","ta"})
    @CommandPermission
    public boolean teamAdmin(CommandSender sender, CommandArgs args)
    {
        if (args.size() > 0)    
        {
            Hero hero = Heroes.getHero(args.getString(0));
            Group area = hero.getTeam();
            return this.toggleTeamPos(sender, hero, area, "admin");
        }
        return false;
    }
    
    @Command(desc = "Toggles Mod State of a Player", usage = "<PlayerName>", aliases = {"mod","tm"})
    @CommandPermission
    public boolean teamMod(CommandSender sender, CommandArgs args)
    {
        if (args.size() > 0)    
        {
            Hero hero = Heroes.getHero(args.getString(0));
            Group area = hero.getTeam();
            return this.toggleTeamPos(sender, hero, area, "mod");
        }
        return false;
    }
    
    @Command(desc = "Joins a team", usage = "<TeamTag>")
    @CommandPermission
    public boolean join(CommandSender sender, CommandArgs args)
    {
        if (args.size() > 0)
        {
            Integer areaId = groupcontrol.getTeamGroup(args.getString(0));
            if (areaId == null)
            {
                sender.sendMessage(t("e")+t("team_noTag",args.getString(0)));
                return true;
            }
            Hero hero = Heroes.getHero(sender);
            Group area = groupcontrol.getGroup(areaId);
            return this.toggleTeamPos(sender, hero, area, "userjoin");
        }
        return false;
    }
    
    @Command(desc = "Leaves a team", usage = "")
    @CommandPermission
    public boolean leave(CommandSender sender, CommandArgs args)
    {
        if (args.isEmpty())
        {
            Hero hero = Heroes.getHero(sender);
            return this.toggleTeamPos(sender, hero, null, "userleave");
        }
        return false;
    }
    
    @Command(desc = "Kicks a Player out of his team", usage = "<Player>")
    @CommandPermission
    public boolean kick(CommandSender sender, CommandArgs args)
    {
        if (args.size() > 0)
        {
            Hero hero = Heroes.getHero(args.getString(0));
            //TODO Permission if sender can kick PLAYER out of his team
            return this.toggleTeamPos(sender, hero, null, "userleave");
        }
        return false;
    }
    
    private boolean toggleTeamPos(CommandSender sender, Hero hero, Group area, String position)
    {
        if (hero == null)
        {
            sender.sendMessage(t("e")+t("g_noPlayer"));
            return true;
        }
        
        if (position.equalsIgnoreCase("admin"))
        {
            if (area.isAdmin(hero))
            {
                area.delAdmin(hero);
                sender.sendMessage(t("i")+t("team_nolonger",hero.getName(),"admin",area.getTag()));
                return true;
            }
            else
            {
                if (area.isUser(hero))
                {
                    area.addAdmin(hero);
                    sender.sendMessage(t("i")+t("team_isnow",hero.getName(),"admin",area.getTag()));
                }
                else
                    sender.sendMessage(t("e")+t("team_joinfirst",hero.getName(),area.getTag()));
             }
            return true;
        }
        
        if (position.equalsIgnoreCase("mod"))
        {
            if (area.isMod(hero))
            {
                area.delMod(hero);
                sender.sendMessage(t("i")+t("team_nolonger",hero.getName(),"mod",area.getTag()));
                return true;
            }
            else
            {
                if (area.isUser(hero))
                {
                    area.addMod(hero);
                    sender.sendMessage(t("i")+t("team_isnow",hero.getName(),"mod",area.getTag()));
                }
                else
                    sender.sendMessage(t("e")+t("team_joinfirst",hero.getName(),area.getTag()));
            }
            return true;
        }
        
        if (position.equalsIgnoreCase("userjoin"))
        {
            if (hero.getTeam()!=null)
            {
                if (area.isUser(hero))
                    sender.sendMessage(t("e")+t("team_joined",hero.getName(),area.getTag()));
                else
                    sender.sendMessage(t("e")+t("team_leavefirst",area.getTag()));
                return true;
            }
            else
            {
                area.addUser(hero);
                sender.sendMessage(t("i")+t("team_isnow",hero.getName(),"user",area.getTag()));
            }
            return true;
        }
        
        if (position.equalsIgnoreCase("userleave"))
        {
            if (hero.getTeam()==null)
            {
                sender.sendMessage(t("e")+t("team_noleave",hero.getName()));
                return true;
            }
            if (area == null) area = hero.getTeam();
            area.delUser(hero);
            sender.sendMessage(t("i")+t("team_nolonger",hero.getName(),"user",area.getTag()));
            return true;
        }
        return false;
    }
    
    @Command(desc = "Propose Alliance to this faction", usage = "<TeamTag> [TeamTag]")
    @CommandPermission
    public boolean ally(CommandSender sender, CommandArgs args)
    {
        if (args.size() > 1)
        {
            //TODO Permission if sender can change AllyMode of other Teams
            Group team = groupcontrol.getGroup(args.getString(0));
            Group team2 = groupcontrol.getGroup(args.getString(1));
            team.setally(team2);
            team2.setally(team);
            team.sendToAlly(t("rel_ally_both",team.getTag(),team2.getTag()));
            return true;
        }
        if (args.size() > 0)
        {
            Group team2 = groupcontrol.getGroup(args.getString(0));
            Group team = Heroes.getHero(sender).getTeam();
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
    
    @Command(desc = "Make an other Team to your enemy", usage = "<TeamTag> [TeamTag]")
    @CommandPermission
    public boolean enemy(CommandSender sender, CommandArgs args)
    {
        if (args.size() > 1)
        {
            //TODO Permission if sender can change EnemyMode of other Teams
            Group team = groupcontrol.getGroup(args.getString(0));
            Group team2 = groupcontrol.getGroup(args.getString(1));
            team.setenemy(team2);
            team2.setenemy(team);
            team.sendToTeam(t("rel_enemy_mortal",team2.getTag()));
            team2.sendToTeam(t("rel_enemy_mortal",team.getTag()));
            return true;
        }
        if (args.size() > 0)
        {
            Group team2 = groupcontrol.getGroup(args.getString(0));
            Group team = Heroes.getHero(sender).getTeam();
            if (team != null)
            {
                team.setenemy(team2);
                if (team2.isenemy(team))
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
    
    @Command(desc = "Set TeamRelation to Neutral", usage = "<TeamTag> [TeamTag]")
    @CommandPermission
    public boolean neutral(CommandSender sender, CommandArgs args)
    {
        if (args.size() > 1)
        {
            //TODO Permission if sender can change NeutralMode of other Teams
            Group team = groupcontrol.getGroup(args.getString(0));
            Group team2 = groupcontrol.getGroup(args.getString(1));
            team.setneutral(team2);
            team2.setneutral(team);
            team.sendToTeam(t("rel_neutral_both",team2.getTag()));
            team2.sendToTeam(t("rel_neutral_both",team.getTag()));
            return true;
        }
        if (args.size() > 0)
        {
            Group team2 = groupcontrol.getGroup(args.getString(0));
            Group team = Heroes.getHero(sender).getTeam();
            if (team != null)
            {
                team.setneutral(team2);
                if (team2.isneutral(team))
                {
                    team.sendToTeam(t("rel_neutral_both",team2.getTag()));
                    team2.sendToTeam(t("rel_neutral_both",team.getTag()));
                }
                else if (team2.isenemy(team))
                {
                    team.sendToTeam(t("rel_neutral_stopwar",team2.getTag()));
                    team2.sendToTeam(t("rel_neutral_stopwar2",team.getTag()));
                }
                else if (team2.isenemy(team))
                {
                    team.sendToTeam(t("rel_neutral_noally",team2.getTag()));
                    team2.sendToTeam(t("rel_neutral_noally2",team.getTag()));
                }
                return true;
            }
        }
        return false;
    }
}
