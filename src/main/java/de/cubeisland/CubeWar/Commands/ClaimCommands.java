package de.cubeisland.CubeWar.Commands;

import de.cubeisland.CubeWar.Area.Area;
import static de.cubeisland.CubeWar.CubeWar.t;
import de.cubeisland.CubeWar.Groups.Group;
import de.cubeisland.CubeWar.Groups.GroupControl;
import de.cubeisland.CubeWar.Perm;
import de.cubeisland.CubeWar.User.User;
import de.cubeisland.CubeWar.User.Users;
import de.cubeisland.libMinecraft.command.Command;
import de.cubeisland.libMinecraft.command.CommandArgs;
import de.cubeisland.libMinecraft.command.RequiresPermission;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Faithcaio
 */
public class ClaimCommands {

    public ClaimCommands() 
    {
    
    }
    
    
        @Command(usage = "[Radius] [Tag]")
    @RequiresPermission
    public boolean claim(CommandSender sender, CommandArgs args)
    {
        if (sender instanceof Player)
        {
            Player player = (Player)sender;
            User user = Users.getUser(player);
            Location loc = player.getLocation();
            if (args.isEmpty()) 
            {
                if ((Perm.command_claim_bypass.hasPerm(sender))
                  ||(Perm.command_claim_ownTeam.hasPerm(sender)))
                        this.claim(player.getLocation(), 0, user.getTeam(), player, user);
                return true;
            }
            if (args.size() > 0) 
            {
                int rad;
                try { rad = args.getInt(1); }
                catch (NumberFormatException ex)
                {
                    sender.sendMessage(t("claim_invalid_radius",args.getString(1)));
                    return true;
                }
                if (rad > 5)//TODO maxRadius in config
                {
                    sender.sendMessage(t("claim_big_radius",rad));
                    return true;
                }  
                if (rad > 0) 
                    if ((Perm.command_claim_bypass.hasNotPerm(sender))
                     && (Perm.command_claim_radius.hasNotPerm(sender)))
                        return true;
                this.claim(player.getLocation(), 0, user.getTeam(), player, user);
                return true;
            }
            
            if (args.size() > 1) 
            {
                int rad;
                try { rad = args.getInt(1); }
                catch (NumberFormatException ex)
                {
                    sender.sendMessage(t("claim_invalid_radius",args.getString(1)));
                    return true;
                }
                //TODO maxRadius in config
                if (rad > 5)
                {
                    sender.sendMessage(t("claim_big_radius",rad));
                    return true;
                }
                Group team = GroupControl.get().getGroup(args.getString(0));
                if (team == null)
                {
                    sender.sendMessage(t("claim_invalid_team",args.getString(0)));
                    return true;
                }
                if (Perm.command_claim_bypass.hasNotPerm(sender))
                {
                    if (!team.equals(user.getTeam()))
                        if (Perm.command_claim_otherTeam.hasNotPerm(sender))
                            return true;
                    if (rad > 0)
                        if (Perm.command_claim_radius.hasNotPerm(sender))
                            return true;     
                }
                this.claim(player.getLocation(), rad, team, player, user);
                return true;
            }
        }
        return false;
    }
    
    private void claim(Location loc, int radius, Group g, Player player, User user)
    {
        if (g == null)
        {
            player.sendMessage(t("claim_noteam"));
            return;
        }
        if (Area.getGroup(loc).equals(user.getTeam()))
        {
            player.sendMessage(t("claim_deny_own"));
            return;
        }
        if (Area.getGroup(loc)!=null)
        {
            if (Perm.command_claim_bypass.hasNotPerm(player))
            {
                if (Perm.command_claim_fromother.hasNotPerm(player)) return;
                if (Perm.command_claim_peaceful.hasNotPerm(player)) return;
            }
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
            Group group = Area.addChunk(loc.getChunk(), user.getTeam());
            if (group == null)
                group = GroupControl.getWildLand();
            if (Perm.command_claim_bypass.hasPerm(player))
                player.sendMessage(t("claim_claimed_bypass",group.getTag(),user.getTeamTag()));
            else
            {
                if (group.equals(GroupControl.getWildLand()))
                    player.sendMessage(t("claim_claimed_wild",user.getTeamTag()));
                else
                    player.sendMessage(t("claim_claimed_enemy",group.getTag(),user.getTeamTag()));
            }
            return;
        }
        for (Chunk chunk : chunks)
        {
            //TODO check if enough power etc...
            Group group = Area.addChunk(chunk, user.getTeam());
 //TODO count claim & claim type + outputmsg
        }
    }
    
    @Command(usage = "[radius]|[all] [Tag]|[all]")
    @RequiresPermission
    public boolean unclaim(CommandSender sender, CommandArgs args)
    {
        Player player;
        Location loc;
        User user;
        if (sender instanceof Player)
        {
            player = (Player)sender;            
            loc = player.getLocation();
            user = Users.getUser(sender);
        }
        else
        {
            //TODO msg not console cannot do this
            return true;
        }
        if (args.isEmpty())
        {
            if (Perm.command_unclaim_ownTeam.hasPerm(sender));
                this.unclaim(loc, 0, user.getTeam());
            return true;
        }
        int rad;
        try
        {
            rad = args.getInt(0);
        }
        catch (NumberFormatException ex)
        {
            if (args.getString(0).equalsIgnoreCase("all"))
                rad = -1;
            else
            {
                //TODO msg
                return true;
            }
        }
        if (args.size()>0)
        {
            if (Perm.command_unclaim_bypass.hasNotPerm(sender))
            {
                if (Perm.command_unclaim_radius.hasNotPerm(sender))
                    return true;
                if (rad == -1)
                    if (Perm.command_unclaim_ownTeam_all.hasNotPerm(sender))  
                        return true;
            }
            this.unclaim(loc, rad, user.getTeam());
        }
        if (args.size()>1)
        {
            Group group = GroupControl.get().getGroup(args.getString(1));
            if (group == null)
            {
                if (!args.getString(1).equalsIgnoreCase("all"))//TODO erstllen grp mit tag all verbieten!!!
                //TODO msg keine Grp
                return true;
            }
            if (Perm.command_unclaim_bypass.hasNotPerm(sender))
            {
                if (group == null)
                {
                    if (Perm.command_unclaim_allTeam.hasNotPerm(sender))
                        return true;
                    if (rad == -1)
                        if (Perm.command_unclaim_allTeam_all.hasNotPerm(sender))  
                            return true;
                }
                else
                {
                    if (group.equals(user.getTeam()))
                    {
                        if (Perm.command_unclaim_radius.hasNotPerm(sender))
                            return true;
                        if (rad == -1)
                            if (Perm.command_unclaim_ownTeam_all.hasNotPerm(sender))  
                                return true;
                    }
                    else
                    {
                        if (Perm.command_unclaim_otherTeam.hasNotPerm(sender))
                            return true;
                        if (rad == -1)
                            if (Perm.command_unclaim_otherTeam_all.hasNotPerm(sender))  
                                return true;
                    }
                }
            }
            this.unclaim(loc, rad, group);
        }
        return false;    
    }
    
    private void unclaim(Location loc, int radius, Group group)
    {
        if (radius == 0)
        {
            Area.remChunk(loc);
        }
        else if (radius < 0)
        {
            if (radius < 1)
            {
                //TODO msg
                return;
            }
            if (group == null)
            Area.remAll(group);
        }
        else
        {
            List<Chunk> chunks = new ArrayList<Chunk>(); 
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
            for (Chunk chunk : chunks)
            {
                Area.remChunk(chunk);
                //TODO msg chunk deleted
            }
        }
                    
                
    }
}
