package de.cubeisland.CubeWar.Commands;

import Groups.Group;
import Groups.GroupControl;
import static de.cubeisland.CubeWar.CubeWar.t;
import de.cubeisland.CubeWar.Hero;
import de.cubeisland.CubeWar.Heroes;
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
    
    @Command(desc = "Creates a new Team", usage = "<TeamTag> <TeamName>", aliases = {"ct"})
    @CommandPermission
    public boolean createTeam(CommandSender sender, CommandArgs args)
    {
        args.size();
        if (args.size() > 1)
        {
            Group newarea = groupcontrol.newTeam(args.getString(0), args.getString(1));
            newarea.addAdmin(Heroes.getHero(sender));
            sender.sendMessage(t("i")+"Team TAG created!");
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
            groupcontrol.newArena(args.getString(0), args.getString(1));
            sender.sendMessage(t("i")+"Arena TAG created!");
            return true;
        }
        else
            return false;
    }
    
    @Command(desc = "modifies a Team", usage = "[#TeamTag] <Key> <Value>", aliases = {"mt"})
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
                            sender.sendMessage(t("i")+"Key was Set!");
                            return true;
                        }
                        else
                            sender.sendMessage(t("e")+"Invalid Key or Value");
                    }
                    else
                        sender.sendMessage(t("e")+"Team does not exist");
                }
                else
                    return false;
            }
            else
            {
               
                Hero hero = Heroes.getHero(sender);
                if (hero == null)
                {
                    sender.sendMessage(t("e")+"Player not found!");
                    return true;
                }
                Group area = hero.getTeam();
                if (area == null)
                {
                    sender.sendMessage(t("e")+"You are not in a Team");
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
                            sender.sendMessage(t("i")+"Key was Set!");
                            return true;
                        }
                        else
                            sender.sendMessage(t("e")+"Invalid Key or Value");
                }
            }
            return true;
        }
        else
        {
            return false;
        }
    }
    
    
    @Command(desc = "modifies a Team", usage = "<ArenaTag> <Key> <Value>", aliases = {"ma"})
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
                    sender.sendMessage(t("i")+"Key was Set!");
                    return true;
                }
                else
                    sender.sendMessage(t("e")+"Invalid Key or Value");
            }
            else
                sender.sendMessage(t("e")+"Team does not exist");
            
            return true;
        }
        else
        {
            return false;
        }
    }
    
    @Command(desc = "Toggles Admin State of a Player", usage = "[TeamTag] <PlayerName>", aliases = {"admin","ta"})
    @CommandPermission
    public boolean teamAdmin(CommandSender sender, CommandArgs args)
    {
        if (args.size() > 1)
        {
            Integer areaId = groupcontrol.getTeamGroup(args.getString(0));
            if (areaId == null)
            {
                sender.sendMessage(t("e")+"TeamTag not found!");
                return true;
            }
            Hero hero = Heroes.getHero(args.getString(1));
            Group area = groupcontrol.getGroup(areaId);
            return this.toggleTeamPos(sender, hero, area, "admin");

        }
        else
        if (args.size() > 0)    
        {
            Hero hero = Heroes.getHero(args.getString(0));
            Group area = hero.getTeam();
            return this.toggleTeamPos(sender, hero, area, "admin");
        }
        return false;
    }
    
    @Command(desc = "Toggles Mod State of a Player", usage = "[TeamTag] <PlayerName>", aliases = {"mod","tm"})
    @CommandPermission
    public boolean teamMod(CommandSender sender, CommandArgs args)
    {
        if (args.size() > 1)
        {
            Integer areaId = groupcontrol.getTeamGroup(args.getString(0));
            if (areaId == null)
            {
                sender.sendMessage(t("e")+"TeamTag not found!");
                return true;
            }
            Hero hero = Heroes.getHero(args.getString(1));
            Group area = groupcontrol.getGroup(areaId);
            return this.toggleTeamPos(sender, hero, area, "mod");

        }
        else
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
                sender.sendMessage(t("e")+"TeamTag not found!");
                return true;
            }
            Hero hero = Heroes.getHero(sender);
            Group area = groupcontrol.getGroup(areaId);
            return this.toggleTeamPos(sender, hero, area, "userjoin");
        }
        return false;
    }
    
    @Command(desc = "Leaves a team", usage = "<TeamTag>")
    @CommandPermission
    public boolean leave(CommandSender sender, CommandArgs args)
    {
        if (args.size() > 0)
        {
            Integer areaId = groupcontrol.getTeamGroup(args.getString(0));
            if (areaId == null)
            {
                sender.sendMessage(t("e")+"TeamTag not found!");
                return true;
            }
            Hero hero = Heroes.getHero(sender);
            Group area = groupcontrol.getGroup(areaId);
            return this.toggleTeamPos(sender, hero, area, "userleave");
        }
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
            sender.sendMessage(t("e")+"Player not found!");
            return true;
        }
        
        if (position.equalsIgnoreCase("admin"))
        {
            if (area.isAdmin(hero))
            {
                area.delAdmin(hero);
                sender.sendMessage(t("i")+"PLAYER is no longer admin of TAG");
                return true;
            }
            else
            {
                if (area.isUser(hero))
                {
                    area.addAdmin(hero);
                    sender.sendMessage(t("i")+"PLAYER is now admin of TAG");
                }
                else
                    sender.sendMessage(t("e")+"PLAYER has to join TAG first");
             }
            return true;
        }
        
        if (position.equalsIgnoreCase("mod"))
        {
            if (area.isMod(hero))
            {
                area.delMod(hero);
                sender.sendMessage(t("i")+"PLAYER is no longer mod of TAG");
                return true;
            }
            else
            {
                if (area.isUser(hero))
                {
                    area.addMod(hero);
                    sender.sendMessage(t("i")+"PLAYER is now mod of TAG");
                }
                else
                    sender.sendMessage(t("e")+"PLAYER has to join TAG first");
            }
            return true;
        }
        
        if (position.equalsIgnoreCase("userjoin"))
        {
            if (hero.getTeam()!=null)
            {
                if (area.isUser(hero))
                    sender.sendMessage(t("e")+"PLAYER is already user of TAG");
                else
                    sender.sendMessage(t("e")+"You must leave TAG first to join an other Team");
                return true;
            }
            else
            {
                area.addUser(hero);
                sender.sendMessage(t("i")+"PLAYER is now user of TAG");
            }
            return true;
        }
        
        if (position.equalsIgnoreCase("userleave"))
        {
            if (hero.getTeam()==null)
            {
                sender.sendMessage(t("e")+"PLAYER has no Team to leave");
                return true;
            }
            if (area == null) area = hero.getTeam();
            area.delUser(hero);
            sender.sendMessage(t("i")+"PLAYER is no longer user of TAG");
            return true;
        }
        return false;
    }
    
    @Command(desc = "Propose Alliance to this faction", usage = "<TEAMTAG> [TEAMTAG]")
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
        }
        if (args.size() > 0)
        {
            Group team2 = groupcontrol.getGroup(args.getString(0));
            Group team = Heroes.getHero(sender).getTeam();
            if (team != null)
                team.setally(team2);
        }
        return false;
    }
    
    @Command(desc = "Make an other Team to your enemy", usage = "<TEAMTAG> [TEAMTAG]")
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
        }
        if (args.size() > 0)
        {
            Group team2 = groupcontrol.getGroup(args.getString(0));
            Group team = Heroes.getHero(sender).getTeam();
            if (team != null)
                team.setenemy(team2);
        }
        return false;
    }
    
    @Command(desc = "Set Team to Neutral ", usage = "<TEAMTAG> [TEAMTAG]")
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
        }
        if (args.size() > 0)
        {
            Group team2 = groupcontrol.getGroup(args.getString(0));
            Group team = Heroes.getHero(sender).getTeam();
            if (team != null)
                team.setneutral(team2);
        }
        return false;
    }
}
