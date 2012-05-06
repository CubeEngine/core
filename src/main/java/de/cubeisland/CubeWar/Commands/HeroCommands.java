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
import de.cubeisland.libMinecraft.command.CommandPermission;
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
    
    @Command(desc = "Shows Info about Player", usage = "<PlayerName>", aliases = {"kd"})
    @CommandPermission
    public boolean show(CommandSender sender, CommandArgs args)
    {
        if (args.size() > 0)    
        {
            User hero = Users.getHero(args.getString(0));
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
            User hero = Users.getHero(sender);
            hero.showInfo(sender);
            return true;
        }
        return false;
    }
}
