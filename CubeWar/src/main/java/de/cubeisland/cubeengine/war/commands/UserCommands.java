package de.cubeisland.cubeengine.war.commands;

import de.cubeisland.cubeengine.war.CubeWar;
import static de.cubeisland.cubeengine.war.CubeWar.t;
import de.cubeisland.cubeengine.war.Perm;
import de.cubeisland.cubeengine.war.user.User_old;
import de.cubeisland.cubeengine.war.user.UserControl_old;
import de.cubeisland.libMinecraft.command.Command;
import de.cubeisland.libMinecraft.command.CommandArgs;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Faithcaio
 */
public class UserCommands 
{

    private UserControl_old users = CubeWar.getInstance().getUserControl();
    
    public UserCommands() 
    {
    
    }
    
    @Command(usage = "[Player]", aliases = {"show"})
    public boolean whois(CommandSender sender, CommandArgs args)
    {
        if (Perm.command_whois.hasNotPerm(sender)) return true;
        if (args.size() > 0)    
        {
            if (Perm.command_whois_other.hasNotPerm(sender)) return true;
            User_old user = users.getUser(args.getString(0));
            if (user == null)
            {
                sender.sendMessage(t("e")+t("g_noplayer"));
                return true;
            }
            user.showInfo(sender);
            return true;
        }
        if (args.isEmpty())
        {
            User_old user = users.getUser(sender);
            user.showInfo(sender);
            return true;
        }
        return false;
    }
    
    @Command(usage = "buy <amount>")
    public boolean influence(CommandSender sender, CommandArgs args)
    {
        if (args.size()>1)
        {
            User_old user = users.getUser(sender);
            int amount = args.getInt(1);
            double price = CubeWar.getInstance().getConfiguration().influenceCost * amount;
            //TODO buy for money...
            //KP kaufen für IP ???
            user.addInfluence(amount);
        }
       
        
        return false;
    }
    
    
    //TODO Bounty auslagern in CubeBountyHunter oder CubeHunter
    //bounty adding etc
    /*
     * 
    @Command(usage = "set <Player> <bounty>" )//aliases = {""}
    public void bounty(CommandSender sender, CommandArgs args)
    {
        if (Perm.command_bounty.hasNotPerm(sender)) return;
        
    }
    * 
    */
}
