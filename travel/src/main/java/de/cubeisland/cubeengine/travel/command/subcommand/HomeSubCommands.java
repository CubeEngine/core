package de.cubeisland.cubeengine.travel.command.subcommand;

import java.util.Set;

import org.bukkit.Location;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.Param;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Alias;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.travel.Travel;
import de.cubeisland.cubeengine.travel.storage.Home;
import de.cubeisland.cubeengine.travel.storage.InviteManager;
import de.cubeisland.cubeengine.travel.storage.TelePointManager;
import de.cubeisland.cubeengine.travel.storage.TeleportInvite;
import de.cubeisland.cubeengine.travel.storage.TeleportPoint;

public class HomeSubCommands
{
    private final TelePointManager tpManager;
    private final InviteManager inviteManager;
    private final Travel module;

    public HomeSubCommands(Travel module)
    {
        this.module = module;
        this.tpManager = module.getTelepointManager();
        this.inviteManager = module.getInviteManager();
    }

    @Alias(names = {
        "sethome"
    })
    @Command(names = "set", desc = "Set your home", usage = "[HomeName]", min = 1, max = 1, flags = {
        @Flag(longName = "public", name = "pub")
    }, permDefault  = PermDefault.TRUE)
    public void setHome(ParameterizedContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User) context.getSender();
            Location location = sender.getLocation();
            if (context.getArgCount() == 0)
            {
                if (tpManager.hasHome("home", sender))
                {
                    sender.sendTranslated("&4You already have a home! Maybe you need /home move?");
                    return;
                }
                Home home = tpManager.createHome(location, "home", sender, TeleportPoint.Visibility.PRIVATE);
                sender.sendTranslated("&6Your home have been created!");
            }
            else
            {
                String name = context.getString(0).toLowerCase();
                TeleportPoint.Visibility visibility = TeleportPoint.Visibility.PRIVATE;
                if (context.hasFlag("pub"))
                {
                    visibility = TeleportPoint.Visibility.PUBLIC;
                    if (tpManager.getHome(name) != null)
                    {
                        if (tpManager.getHome(name).isPublic())
                        {
                            sender.sendTranslated("&4A public home by that name already exist. Please choose another name");
                            return;
                        }
                    }
                }
                if (name.contains(":") || name.length() >= 32)
                {
                    sender.sendTranslated("&4Homes may not have names that are longer then 32 characters, and they may not contain colon(:)'s!");
                    return;
                }
                if (tpManager.hasHome(context.getString(0).toLowerCase(), sender))
                {
                    sender.sendTranslated("&4You already have a home by that name! Maybe you need /home move?");
                    return;
                }
                Home home = tpManager.createHome(location, name, sender, visibility);
                sender.sendTranslated("&6Your home &9%s &6have been created!", context.getString(0));
            }
        }
        else
        {
            context.sendTranslated("&4This command can only be used by users!");
        }


    }

    @Command(desc = "Set the welcome message of homes", names = {"setgreeting", "greeting", "setwelcome", "setwelcomemsg"},
    min = 1, max = -1, permDefault = PermDefault.TRUE, params = {
            @Param(names = {"home", "h"})
    }, usage = "[Welcome message goes here] <home: [home name]>")
    public void setWelcomeMessage(ParameterizedContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User) context.getSender();
            Home home;
            if (context.hasParam("home"))
            {
                home = tpManager.getHome(sender, context.getString("home"));
                if (home == null || !home.isOwner(sender))
                {
                    sender.sendTranslated("%s is not a home your owning!");
                    return;
                }
            }
            else
            {
                home = tpManager.getHome(sender, "home");
                if (home == null || !home.isOwner(sender))
                {
                    sender.sendTranslated("You don't have a homes!");
                    return;
                }
            }

            StringBuilder message = new StringBuilder();
            for (int x = 0; x < context.getArgCount(); x++)
            {
                message.append(context.getString(x)).append(' ');
            }

            home.setWelcomeMsg(message.toString());
            home.update();
            sender.sendTranslated("The welcome message for the home is now set to: ");
            sender.sendTranslated(home.getWelcomeMsg());
        }
    }

    @Command(names = {"move", "replace"}, desc = "Move a home", usage = "[HomeName]", min = 1, max = 1, permDefault = PermDefault.TRUE)
    public void moveHome(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User) context.getSender();
            if (context.getArgCount() == 0)
            {
                Home home = this.tpManager.getHome(sender, "home");
                if (home == null)
                {
                    sender.sendTranslated("&9%s &4Could not be found!", context.getString(0));
                }
                if (!home.isOwner(sender))
                {
                    sender.sendTranslated("&4You can't move another users home");
                    return;
                }
                home.setLocation(sender.getLocation());
                home.update();
                sender.sendTranslated("&6Your home have been moved");
            }
            else if (module.getConfig().multipleHomes)
            {
                Home home = this.tpManager.getHome(sender, context.getString(0));
                if (home == null)
                {
                    sender.sendTranslated("&9%s &4Could not be found!", context.getString(0));
                }
                if (!home.isOwner(sender))
                {
                    sender.sendTranslated("&4You can't move another users home");
                    return;
                }
                home.setLocation(sender.getLocation());
                home.update();
                sender.sendTranslated("&9%s &6have been moved", home.getName());
            }
        }
        else
        {
            context.sendTranslated("&4This command can only be used by users!");
        }
    }

    @Alias(names = {
    "remhome", "removehome", "delhome", "deletehome"
    })
    @Command(names = {
    "remove", "delete", "rem", "del"
    }, desc = "Remove a home", usage = "[HomeName]",min = 1, max = 1, permDefault = PermDefault.TRUE)
    public void removeHome(CommandContext context)
    {
        if (context.getSender()  instanceof  User)
        {
            User sender = (User) context.getSender();
            if (context.getArgCount() == 0)
            {
                Home home = this.tpManager.getHome(sender, "home");
                if (home == null)
                {
                    sender.sendTranslated("&9%s &4Could not be found!", context.getString(0));
                }
                if (!home.isOwner(sender))
                {
                    sender.sendTranslated("&4You can't remove another users home");
                    return;
                }
                tpManager.deleteHome(home);
                sender.sendTranslated("&6Your home have been removed");
            }
            else if (module.getConfig().multipleHomes)
            {
                Home home = this.tpManager.getHome(sender, context.getString(0));
                if (home == null)
                {
                    sender.sendTranslated("&9%s &4Could not be found!", context.getString(0));
                }
                if (!home.isOwner(sender))
                {
                    sender.sendTranslated("&4You can't remove another players home");
                    return;
                }
                tpManager.deleteHome(home);
                sender.sendTranslated("&9%s &6have been removed", context.getString(0));
            }
        }
        else
        {
            context.sendTranslated("&4This command can only be used by users!");
        }
    }

    // TODO Unload if multihomes isn't enabled
    @Alias(names = {
    "listhomes", "homes"
    })
    @Command(names = {
        "list"
    }, desc = "List homes you can access", permDefault = PermDefault.TRUE, min = 0, max = 0, flags = {
            @Flag(name = "pub", longName = "public"),
            @Flag(name = "priv", longName = "private"),
            @Flag(name = "o", longName = "owned"),
            @Flag(name = "i", longName = "invited")
    })
    public void listHomes(ParameterizedContext context)
    {
        if (!context.isSender(User.class))
        {
            int mask = context.getFlagCount() == 0 ? tpManager.ALL : 0;
            if (context.hasFlag("pub")) mask |= tpManager.PUBLIC;
            if (context.hasFlag("priv")) mask |= tpManager.PRIVATE;

            context.sendTranslated("&eHere is a list of the homes: ");
            for (Home home : tpManager.listHomes(mask))
            {
                context.sendTranslated("  &2%s&e:&6%s", home.getOwner().getName(), home.getName());
            }
            return;
        }

        User user = (User)context.getSender();
        int mask = context.getFlagCount() == 0 ? tpManager.ALL : 0;
        if (context.hasFlag("pub")) mask |= tpManager.PUBLIC;
        if (context.hasFlag("priv")) mask |= tpManager.PRIVATE;
        if (context.hasFlag("o")) mask |= tpManager.OWNED;
        if (context.hasFlag("i")) mask |= tpManager.INVITED;
        Set<Home> homes = tpManager.listHomes(user, mask);
        if (homes.isEmpty())
        {
            user.sendTranslated("The query returned null homes!");
            return;
        }
        user.sendTranslated("&eHere is a list of the homes: ");
        for (Home home : homes)
        {
            if (home.isOwner(user))
            {
                user.sendMessage("  &6" + home.getName());
            }
            else
            {
                if (home.isPublic())
                {
                    user.sendTranslated("  &2public&e:&6%s", home.getName());
                }
                else
                {
                    user.sendTranslated("  &2%s&e:&6%s", home.getOwner().getName(), home.getName());
                }
            }
        }
    }

    @Command(names = {
    "ilist", "invited"
    }, desc = "List all players invited to your homes", min = 0, max = 0, permDefault = PermDefault.TRUE)
    public void invitedList(CommandContext context)
    {
        if (!context.isSender(User.class))
        {

            return;
        }

        User user = (User)context.getSender();
        Set<Home> homes = tpManager.listHomes(user, tpManager.OWNED);
        if (!homes.isEmpty())
        {
            user.sendTranslated("&eHere is a list of all your homes with the users invited to them:");
            for (Home home : homes)
            {
                Set<TeleportInvite> invites = inviteManager.getInvites(home.getModel());
                if (invites.size() != 0)
                {
                    context.sendTranslated("  &6%s&e:", home.getName());
                    for (TeleportInvite invite : invites)
                    {
                        context.sendMessage("    &2" + CubeEngine.getUserManager().getUser(invite.userKey).getName());
                    }
                }
            }
            return;
        }
        context.sendTranslated("&cYou don't have any homes with users invited to them!");
    }

    @Command(desc = "Invite a user to one of your homes", min = 1, max = 2, usage = "[home] <user>", permDefault = PermDefault.TRUE)
    public void invite(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User) context.getSender();
            if (context.getArgCount() == 1)
            {
                Home home = tpManager.getHome(sender, "home");
                if (home == null)
                {
                    sender.sendTranslated("&4You don't have a home!");
                    return;
                }
                if (!home.isOwner(sender))
                {
                    sender.sendTranslated("&4You can't edit another players home");
                    return;
                }
                if (home.isPublic())
                {
                    sender.sendTranslated("&4You can't invite a person to a public home >:(");
                }
                User invited = CubeEngine.getUserManager().getUser(context.getString(0), false);
                if (invited == null)
                {
                    sender.sendTranslated("&4You have to invite a user that have played on this server!");
                    return;
                }
                if (invited.equals(sender))
                {
                    sender.sendTranslated("&4You cannot invite yourself to your own home!");
                    return;
                }
                if (home.isInvited(invited))
                {
                    sender.sendTranslated("&6%s is already invited to your home!", invited.getDisplayName());
                    return;
                }
                home.invite(invited);
                if (invited.isOnline())
                {
                    invited.sendTranslated("&3%s &6has invited you to his home. To access it do: /home &9%s:home",
                                           sender.getDisplayName(), sender.getName());
                }
                sender.sendTranslated("&3%s &6Is now invited to home", context.getString(0));
            }
            else if (module.getConfig().multipleHomes)
            {
                Home home = tpManager.getHome(sender, context.getString(0));
                if (home == null)
                {
                    sender.sendTranslated("&9%s &4is not a home!", context.getString(0));
                    return;
                }
                if (!home.isOwner(sender))
                {
                    sender.sendTranslated("&4You can't edit another players home");
                    return;
                }
                if (home.isPublic())
                {
                    sender.sendTranslated("&4You can't invite a person to a public home >:(");
                }
                User invited = CubeEngine.getUserManager().getUser(context.getString(1), false);
                if (invited == null)
                {
                    sender.sendTranslated("&4You have to invite a user that have played on this server!");
                    return;
                }
                if (home.isInvited(invited))
                {
                    sender.sendTranslated("&9%s is already invited to &6%s!", invited.getDisplayName(), home.getName());
                    return;
                }
                home.invite(invited);
                if (invited.isOnline())
                {
                    invited.sendTranslated("&6%s has invited you to his home &9%s&6. To access it do: /home &9%s:%2$s",
                                           sender.getDisplayName(), context.getString(0), sender.getName());
                }
                sender.sendTranslated("&3%s &6Is now invited to &9%s", context.getString(1), context.getString(0));
            }
        }
        else {
            context.sendTranslated("&4This command can only be used by users!");
        }


    }

    @Command(desc = "Uninvite a user from one of your homes", min = 1, max = 2, usage = "[home] <user>", permDefault = PermDefault.TRUE)
    public void unInvite(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User) context.getSender();
            if (context.getArgCount() == 1)
            {
                Home home = tpManager.getHome(sender, "home");
                if (home == null)
                {
                    sender.sendTranslated("&4You don't have a home!");
                    return;
                }
                if (!home.isOwner(sender))
                {
                    sender.sendTranslated("&4You can't edit another players home");
                    return;
                }
                if (home.isPublic())
                {
                    sender.sendTranslated("&4You can't uninvite a person to a public home >:(");
                    return;
                }
                User invited = CubeEngine.getUserManager().getUser(context.getString(0), false);
                if (invited == null)
                {
                    sender.sendTranslated("&4You can't univite a user that never have played on this server!!");
                    return;
                }
                if (!home.isInvited(invited))
                {
                    sender.sendTranslated("&6%s &4Is not invited to your home!", invited.getDisplayName());
                    return;
                }
                home.unInvite(invited);
                if (invited.isOnline())
                {
                    invited.sendTranslated("You are no longer invited to %s' home", sender.getDisplayName());
                }
                sender.sendTranslated("&3%s &6Is no longer invited to home", context.getString(0));
            }
            else if (module.getConfig().multipleHomes)
            {
                Home home = tpManager.getHome(sender, context.getString(0));
                if (home == null)
                {
                    sender.sendTranslated("&9%s &4is not a home!", context.getString(0));
                    return;
                }
                if (!home.getOwner().equals(sender))
                {
                    sender.sendTranslated("&4You can't edit another players home");
                    return;
                }
                if (home.isPublic())
                {
                    sender.sendTranslated("&4You can't uninvite a person from a public home >:(");
                    return;
                }
                User invited = CubeEngine.getUserManager().getUser(context.getString(1), false);
                if (invited == null)
                {
                    sender.sendTranslated("&4You can't univite a user that never have played on this server!!");
                    return;
                }
                if (!home.isInvited(invited))
                {
                    sender.sendTranslated("&9%s &4Is not invited to &6%s&6!", invited.getDisplayName(), home.getName());
                    return;
                }
                home.unInvite(invited);
                if (invited.isOnline())
                {
                    invited.sendTranslated("You are no longer invited to %s' home &9%s", sender.getDisplayName(), context.getString(0));
                }
                sender.sendTranslated("&3%s &6Is no longer invited to &9%s", context.getString(1), context.getString(0));
            }
        }
        else {
            context.sendTranslated("&4This command can only be used by users!");
        }


    }

    @Command(names = {
    "makeprivate"
    }, desc = "Make one of your homes private", min = 1, max = 1, permDefault = PermDefault.TRUE)
    public void makePrivate(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User) context.getSender();
            Home home;
            if (context.getArgCount() == 0)
            {
                home = tpManager.getHome(sender, "home");
            }
            else if (module.getConfig().multipleHomes)
            {
                home = tpManager.getHome(sender, context.getString(0));
            }
            else
            {
                return;
            }

            if (!home.isPublic())
            {
                context.sendTranslated("&6Your home is already private!");
                return;
            }
            home.setVisibility(TeleportPoint.Visibility.PRIVATE);
            context.sendTranslated("&6Your home is now private");
            return;
        }
        context.sendTranslated("&4This command can only be used by users!");

    }

    @Command(names = {
    "makepublic"
    }, desc = "Make one of your homes public", min = 1, max = 1, permDefault = PermDefault.TRUE)
    public void makePublic(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User) context.getSender();
            Home home;
            if (context.getArgCount() == 0)
            {
                home = tpManager.getHome(sender, "home");
            }
            else if (module.getConfig().multipleHomes)
            {
                home = tpManager.getHome(sender, context.getString(0));
            }
            else
            {
                return;
            }

            if (home.isPublic())
            {
                context.sendTranslated("&6Your home is already public!");
                return;
            }
            home.setVisibility(TeleportPoint.Visibility.PUBLIC);
            context.sendTranslated("&6Your home is now public");
            return;
        }
        context.sendTranslated("&4This command can only be used by users!");

    }

    @Command(names = {
    "admin"
    }, desc = "Teleport to another users home", usage = "[User] [Home]", min = 1, max = 2, permDefault = PermDefault.OP)
    public void admin(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User) context.getSender(); //TODO console
            User user = context.getUser(0);
            Home home;
            if (user == null)
            {
                sender.sendTranslated("%s is not an user on this server!", context.getString(0));
                return;
            }

            if (context.getArgCount() == 2)
            {
                home = tpManager.getHome(user, context.getString(1));
                if (home == null)
                {
                    sender.sendTranslated("%s does not have a home named %s", user.getName(), context.getString(1));
                    return;
                }
            }
            else
            {
                home = tpManager.getHome(user, "home");
                if (home == null)
                {
                    sender.sendTranslated("%s does not have a home ", user.getName());
                    return;
                }
            }

            sender.teleport(home.getLocation());
            if (home.getWelcomeMsg() != null)
            {
                sender.sendMessage(home.getWelcomeMsg());
            }
            else
            {
                sender.sendTranslated("You have been teleported to %s's home", user.getName());
            }
            return;
        }

    }
}
