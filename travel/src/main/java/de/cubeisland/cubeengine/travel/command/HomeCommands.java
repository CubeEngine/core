package de.cubeisland.cubeengine.travel.command;

import org.bukkit.event.player.PlayerTeleportEvent;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.travel.Travel;
import de.cubeisland.cubeengine.travel.storage.Home;
import de.cubeisland.cubeengine.travel.storage.TelePointManager;

public class HomeCommands
{
    private final TelePointManager tpManager;
    private final Travel module;

    public HomeCommands(Travel module)
    {
        this.module = module;
        this.tpManager = module.getTelepointManager();
    }

    @Command(desc = "Teleport to a home", usage = "<<owner:>home>", permDefault = PermDefault.TRUE, min = 0, max = 1)
    public void home(CommandContext context)
    {
        if (context.getSender()instanceof User)
        {
            User sender = (User)context.getSender();
            if (context.getArgCount() == 0)
            {
                Home home = tpManager.getHome(sender, "home");
                if (home == null)
                {
                    context.sendTranslated("&4You don't have a home! do /setHome");
                    return;
                }

                if (!home.isOwner(sender))
                {
                    sender.sendTranslated("&4You don't have a main home!");
                    return;
                }

                sender.teleport(home.getLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
                if (home.getWelcomeMsg() != null)
                {
                    context.sendMessage(home.getWelcomeMsg());
                }
                else
                {
                    context.sendTranslated("&6You have been teleported to your home!");
                }
            }
            else if (module.getConfig().multipleHomes)
            {
                if (CubeEngine.getUserManager().findUser(context.getString(0)) != null)
                {
                    User user = CubeEngine.getUserManager().findUser(context.getString(0));
                    Home home = tpManager.getHome(user, "home");
                    if (home != null && home.canAccess(sender))
                    {
                        sender.teleport(home.getLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
                        if (home.getWelcomeMsg() != null)
                        {
                            context.sendMessage(home.getWelcomeMsg());
                        }
                        else
                        {
                            sender.sendTranslated("&6You have been teleported to &9%s's &6default home", home.getOwner().getDisplayName());
                        }
                        return;
                    }
                }
                Home home = tpManager.getHome(sender, context.getString(0).toLowerCase());
                if (home == null)
                {
                    context.sendTranslated("&9" + context.getString(0).toLowerCase() + " &4 is not a home");
                    return;
                }

                sender.teleport(home.getLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
                if (home.getWelcomeMsg() != null)
                {
                    context.sendMessage(home.getWelcomeMsg());
                }
                else
                {
                    if (home.isOwner(sender))
                    {
                        context.sendTranslated("&6You have been teleported to your home: &9%s", home.getName());
                    }
                    else if (home.isPublic())
                    {
                        context.sendTranslated("&6You have been teleported to the public home &9%s", home.getName());
                    }
                    else
                    {
                        context.sendTranslated("&6You have been teleported to &3%s&6's home: &9%s", home.getOwner().getDisplayName(), home.getName());
                    }
                }
            }
            else
            {
                context.sendTranslated("This command does not take any arguments");
            }
        }
        else
        {
            context.sendTranslated("&4This command can only be used by users!");
        }
    }

}
