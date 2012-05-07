package de.cubeisland.CubeWar.Commands;

import de.cubeisland.CubeWar.Area.Area;
import static de.cubeisland.CubeWar.CubeWar.t;
import de.cubeisland.CubeWar.Groups.Group;
import de.cubeisland.CubeWar.Groups.GroupControl;
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
    
    
        @Command(usage = "[Tag] [Radius]")
    @RequiresPermission
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
                if (team == null)
                {
                    sender.sendMessage(t("claim_invalid_team",args.getString(0)));
                    return true;
                }
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
                this.claim(player.getLocation(), rad, team, player, user);
                return true;
            }
            if (args.size() > 0) 
            {
                Group team = GroupControl.get().getGroup(args.getString(0));
                if (team == null)
                {
                    sender.sendMessage(t("claim_invalid_team",args.getString(0)));
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
            //TODO Permission andere claimen und peaceful nie
            if (1==2)
            {
                player.sendMessage(t("claim_deny_other"));
                return;
            }
            if (2==3)            //TODO Permission
            {
                player.sendMessage(t("claim_deny_other_never"));
                return;
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
            chunks.add(loc.getChunk());
        }
        for (Chunk chunk : chunks)
        {
            Group group = Area.addChunk(chunk, user.getTeam());
            if (1==2)
            {//TODO BYPASS MODE
                player.sendMessage(t("claim_claimed_bypass",group.getTag(),user.getTeamTag()));
                return;
            }
            if (group == null)
                player.sendMessage(t("claim_claimed_wild",user.getTeamTag()));
            else
                player.sendMessage(t("claim_claimed_enemy",group.getTag(),user.getTeamTag()));
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
            //TODO Permission check
            this.unclaim(loc, 0, user.getTeam());
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
            //TODO Permission checks
            this.unclaim(loc, rad, user.getTeam());
        }
        if (args.size()>1)
        {
            //TODO Permission checks
            Group group = GroupControl.get().getGroup(args.getString(1));
            if (group == null)
            {
                if (!args.getString(1).equalsIgnoreCase("all"))//TODO erstllen grp mit tag all verbieten!!!
                //TODO msg keine Grp
                return true;
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
