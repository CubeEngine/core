package de.cubeisland.CubeWar.Commands;

import de.cubeisland.CubeWar.Area.Area;
import static de.cubeisland.CubeWar.CubeWar.t;
import de.cubeisland.CubeWar.Groups.Group;
import de.cubeisland.CubeWar.Groups.GroupControl;
import de.cubeisland.CubeWar.User.User;
import de.cubeisland.CubeWar.User.Users;
import de.cubeisland.libMinecraft.command.Command;
import de.cubeisland.libMinecraft.command.CommandArgs;
import de.cubeisland.libMinecraft.command.CommandPermission;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
            team.addAdmin(Users.getUser(sender));
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
               
                User hero = Users.getUser(sender);
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
            User hero = Users.getUser(args.getString(0));
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
            User hero = Users.getUser(args.getString(0));
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
            User hero = Users.getUser(sender);
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
            User hero = Users.getUser(sender);
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
            User hero = Users.getUser(args.getString(0));
            //TODO Permission if sender can kick PLAYER out of his team
            if (hero.getPlayer() != null )
                hero.getPlayer().sendMessage(t("i")+t("team_kick",hero.getTeamTag()));
             return this.toggleTeamPos(sender, hero, null, "userleave");
        }
        return false;
    }
    
    private boolean toggleTeamPos(CommandSender sender, User hero, Group area, String position)
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
    
    @Command(desc = "Make an other Team to your enemy", usage = "<TeamTag> [TeamTag]")
    @CommandPermission
    public boolean enemy(CommandSender sender, CommandArgs args)
    {
        if (args.size() > 1)
        {
            //TODO Permission if sender can change EnemyMode of other Teams
            Group team = groupcontrol.getGroup(args.getString(0));
            Group team2 = groupcontrol.getGroup(args.getString(1));
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
    
    @Command(desc = "Set TeamRelation to Neutral", usage = "<TeamTag> [TeamTag]")
    @CommandPermission
    public boolean neutral(CommandSender sender, CommandArgs args)
    {
        if (args.size() > 1)
        {
            //TODO Permission if sender can change NeutralMode of other Teams
            Group team = groupcontrol.getGroup(args.getString(0));
            Group team2 = groupcontrol.getGroup(args.getString(1));
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
    
    @Command(desc = "Shows Info about Groups", usage = "<Tag>")
    @CommandPermission
    public boolean info(CommandSender sender, CommandArgs args)
    {
        if (args.size() > 0)    
        {
            Group group = GroupControl.get().getGroup(args.getString(0));
            if (group == null)
            {
                sender.sendMessage(t("e")+t("m_noGroupExist",args.getString(0)));
                return true;
            }
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
    
    @Command(desc = "Claims Land", usage = "<[Tag] [Radius]")
    @CommandPermission
    public boolean claim(CommandSender sender, CommandArgs args)
    {
        if (sender instanceof Player)
        {
            Player player = (Player)sender;
            User user = Users.getUser(player);
            Location loc = player.getLocation();
            if (args.size() > 1) 
            {
                Group team = GroupControl.get().getGroup(args.getString(0));
                int rad;
                try { rad = args.getInt(1); }
                catch (NumberFormatException ex)
                {
                    sender.sendMessage(t("Invalid Radius"));//TODO msg translat
                    return true;
                }
                this.claim(player.getLocation(), rad, team, player, user);
                return true;
            }
            if (args.size() > 0) 
            {
                Group team = GroupControl.get().getGroup(args.getString(0));
                if (team == null)
                {
                    sender.sendMessage(t("Invalid eam"));//TODO msg translat
                    return true;
                }
                this.claim(player.getLocation(), 0, team, player, user);
                return true;
            }
            if (args.isEmpty()) 
            {
                this.claim(player.getLocation(), 0, user.getTeam(), player, user);
                return true;
            }
        }
        return false;
    }
    
    private void claim(Location loc, int radius, Group g, Player player, User user)
    {
        if (Area.getGroup(loc).equals(user.getTeam()))
        {
            player.sendMessage(t("claim_own"));//TODO msg translat
            return;
        }
        if (Area.getGroup(loc)!=null)
        {
            //TODO Permission etc.
        }
        List<Chunk> chunks = new ArrayList<Chunk>();
        if (radius != 0)
        {
            World world = loc.getWorld();
            int x = (int)loc.getX();
            int z = (int)loc.getZ();
            for (int i = -radius; i <= radius; ++i)
            {
                for (int j = -radius; j <= radius; ++j)
                {
                    chunks.add(world.getChunkAt(x+i*16,z+j*16));
                }
            }
        }
        else
        {
            chunks.add(loc.getChunk());
        }
        for (Chunk chunk : chunks)
        {
            Area.addChunk(chunk, user.getTeam());
            player.sendMessage(t("claim"));//TODO msg translat
        }
    }
}
