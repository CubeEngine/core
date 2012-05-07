/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cubeisland.CubeWar.Commands;

import static de.cubeisland.CubeWar.CubeWar.t;
import de.cubeisland.CubeWar.User.User;
import de.cubeisland.CubeWar.User.Users;
import de.cubeisland.libMinecraft.command.Command;
import de.cubeisland.libMinecraft.command.CommandArgs;
import de.cubeisland.libMinecraft.command.RequiresPermission;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Faithcaio
 */
public class HeroCommands 
{

    public HeroCommands() 
    {
    
    }
    
    @Command(usage = "<PlayerName>", aliases = {"kd"})
    @RequiresPermission
    public boolean show(CommandSender sender, CommandArgs args)
    {
        if (args.size() > 0)    
        {
            User hero = Users.getUser(args.getString(0));
            if (hero == null)
            {
                sender.sendMessage(t("e")+t("g_noplayer"));
                return true;
            }
            hero.showInfo(sender);
            return true;
        }
        if (args.isEmpty())
        {
            User hero = Users.getUser(sender);
            hero.showInfo(sender);
            return true;
        }
        return false;
    }
}
