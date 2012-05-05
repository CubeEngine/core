package de.cubeisland.CubeWar.Commands;

import Area.AreaControl;
import static de.cubeisland.CubeWar.CubeWar.t;
import de.cubeisland.libMinecraft.command.Command;
import de.cubeisland.libMinecraft.command.CommandArgs;
import de.cubeisland.libMinecraft.command.CommandPermission;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Faithcaio
 */
public class GroupCommands {

    AreaControl areacontrol = AreaControl.get();
    
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
            areacontrol.newTeam(args.getString(0), args.getString(1));
            return true;
        }
        else
        {
            return false;
        }
    }
    
    @Command(desc = "Creates a new Arena", usage = "<ArenaTag> <ArenaName>", aliases = {"ca"})
    @CommandPermission
    public boolean createArena(CommandSender sender, CommandArgs args)
    {
        if (args.size() > 1)
        {
            areacontrol.newArena(args.getString(0), args.getString(1));
            return true;
        }
        else
        {
            return false;
        }
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
                    Integer area = AreaControl.get().getTeamArea(args.getString(0).substring(1));
                    String val = args.getString(2);
                    if (area != null)
                    {
                        for (int i = 3; i < args.size();++i)
                        {
                           val += " "+args.getString(i); 
                        }
                        if (areacontrol.setAreaValue(area, args.getString(1), val))
                            return true;
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
               //TODO own Team 
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
            Integer area = AreaControl.get().getArenaArea(args.getString(0));
            String val = args.getString(2);
            if (area != null)
            {
                for (int i = 3; i < args.size();++i)
                {
                    val += " "+args.getString(i); 
                }
                if (areacontrol.setAreaValue(area, args.getString(1), val))
                    return true;
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
    
    
   
    
}
