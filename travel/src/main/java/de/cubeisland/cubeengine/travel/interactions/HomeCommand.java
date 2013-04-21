/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.cubeengine.travel.interactions;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.command.ArgBounds;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandResult;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.CubeCommand;
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

public class HomeCommand extends ContainerCommand
{
    private final TelePointManager tpManager;
    private final InviteManager inviteManager;
    private final Travel module;

    public HomeCommand(Travel module)
    {
        super(module, "home", "Teleport to your home");
        this.module = module;
        this.tpManager = module.getTelepointManager();
        this.inviteManager = module.getInviteManager();

        if (module.getConfig().multipleHomes)
        {
            this.setUsage("<<owner:>home>");
            this.getContextFactory().setArgBounds(new ArgBounds(0, 1));
        }
        else
        { // Set usage and argBounds according to single homes
            this.setUsage("<owner>");
            this.getContextFactory().setArgBounds(new ArgBounds(0, 1));

            this.getChild("set").setUsage("").getContextFactory().setArgBounds(new ArgBounds(0, 0));
            this.getChild("move").setUsage("").getContextFactory().setArgBounds(new ArgBounds(0, 0));
            this.getChild("remove").setUsage("").getContextFactory().setArgBounds(new ArgBounds(0, 0));
            this.getChild("invite").setUsage("[user]").getContextFactory().setArgBounds(new ArgBounds(1, 1));
            this.getChild("uninvite").setUsage("[user]").getContextFactory().setArgBounds(new ArgBounds(1, 1));
            this.getChild("setgreeting").setUsage("[welcome message]");
        }
    }

    @Override
    public CommandResult run(CommandContext context) throws Exception
    {
        if (context.isSender(User.class))
        {
            User sender = (User)context.getSender();
            if (context.getArgCount() == 0)
            {
                Home home = tpManager.getHome(sender, "home");
                if (home == null)
                {
                    context.sendTranslated("&cYou don't have a home! do &6/setHome");
                    return null;
                }

                if (!home.isOwner(sender))
                {
                    sender.sendTranslated("&cYou don't have a home! do &6/setHome");
                    return null;
                }

                sender.teleport(home.getLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
                if (home.getWelcomeMsg() != null)
                {
                    context.sendMessage(home.getWelcomeMsg());
                }
                else
                {
                    context.sendTranslated("&aYou have been teleported to your home!");
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
                            sender.sendTranslated("&aYou have been teleported to &6%s&a's default home",
                                                  home.getOwner().getDisplayName());
                        }
                        return null;
                    }
                }
                Home home = tpManager.getHome(sender, context.getString(0).toLowerCase());
                if (home == null)
                {
                    context.sendTranslated("&6%s &cis not a home", context.getString(0).toLowerCase());
                    return null;
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
                        context.sendTranslated("&aYou have been teleported to your home: &6%s", home.getName());
                    }
                    else if (home.isPublic())
                    {
                        context.sendTranslated("&aYou have been teleported to the public home &6%s", home.getName());
                    }
                    else
                    {
                        context.sendTranslated("&aYou have been teleported to &2%s&a's home: &6%s",
                                               home.getOwner().getDisplayName(), home.getName());
                    }
                }
            }
        }
        else
        {
            return super.run(context);
        }
        return null;
    }

    @Alias(names = {
        "sethome"
    })
    @Command(names = "set", desc = "Set your home", usage = "[HomeName]", min = 0, max = 1, flags = {
        @Flag(longName = "public", name = "pub")
    }, permDefault = PermDefault.TRUE)
    public void setHome(ParameterizedContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            if (this.tpManager.getNumberOfHomes(sender) == this.module.getConfig().maxhomes)
            {
                sender.sendTranslated("&4You have reached your maximum number of homes!");
                sender.sendTranslated("&cYou have to delete a home to make a new one");
                return;
            }
            Location location = sender.getLocation();
            if (context.getArgCount() == 0)
            {
                Home test = tpManager.getHome(sender, "home");
                if (test != null && test.isOwner(sender))
                {
                    sender.sendTranslated("&cYou already have a home! Maybe you need /home move?");
                    return;
                }
                tpManager.createHome(location, "home", sender, TeleportPoint.Visibility.PRIVATE);
                sender.sendTranslated("&aYour home have been created!");
            }
            else if (module.getConfig().multipleHomes)
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
                            sender
                                .sendTranslated("&cA public home by that name already exist. Please choose another name");
                            return;
                        }
                    }
                }
                if (name.contains(":") || name.length() >= 32)
                {
                    sender
                        .sendTranslated("&cHomes may not have names that are longer then 32 characters, and they may not contain colon(:)'s!");
                    return;
                }
                if (tpManager.hasHome(context.getString(0).toLowerCase(), sender))
                {
                    sender.sendTranslated("&cYou already have a home by that name! Maybe you need /home move?");
                    return;
                }
                Home home = tpManager.createHome(location, name, sender, visibility);
                sender.sendTranslated("&aYour home &6%s &ahave been created!", context.getString(0));
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
    }, usage = "[Welcome message goes here] <home [home name]>")
    public void setWelcomeMessage(ParameterizedContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            Home home;
            if (context.hasParam("home"))
            {
                home = tpManager.getHome(sender, context.getString("home"));
                if (home == null || !home.isOwner(sender))
                {
                    sender.sendTranslated("&6%s &cis not a home you're owning!");
                    return;
                }
            }
            else
            {
                home = tpManager.getHome(sender, "home");
                if (home == null || !home.isOwner(sender))
                {
                    sender.sendTranslated("&cYou don't have a home!");
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
            sender.sendTranslated("&aThe welcome message for the home is now set to: ");
            sender.sendMessage(home.getWelcomeMsg());
        }
    }

    @Command(names = {"move", "replace"}, desc = "Move a home", usage = "[HomeName]", min = 0, max = 1, permDefault = PermDefault.TRUE)
    public void moveHome(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            if (context.getArgCount() == 0)
            {
                Home home = this.tpManager.getHome(sender, "home");
                if (home == null)
                {
                    sender.sendTranslated("&6%s &cCould not be found!", context.getString(0));
                    return;
                }
                if (!home.isOwner(sender))
                {
                    sender.sendTranslated("&cYou can't move another users home");
                    return;
                }
                home.setLocation(sender.getLocation());
                home.update();
                sender.sendTranslated("&aYour home have been moved");
            }
            else if (module.getConfig().multipleHomes)
            {
                Home home = this.tpManager.getHome(sender, context.getString(0));
                if (home == null)
                {
                    sender.sendTranslated("&96%s &cCould not be found!", context.getString(0));
                    return;
                }
                if (!home.isOwner(sender))
                {
                    sender.sendTranslated("&cYou can't move another users home");
                    return;
                }
                home.setLocation(sender.getLocation());
                home.update();
                sender.sendTranslated("&6%s &ahave been moved", home.getName());
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
    }, desc = "Remove a home", usage = "[HomeName]", min = 0, max = 1, permDefault = PermDefault.TRUE)
    public void removeHome(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            if (context.getArgCount() == 0)
            {
                Home home = this.tpManager.getHome(sender, "home");
                if (home == null)
                {
                    sender.sendTranslated("&6%s &cCould not be found!", context.getString(0));
                    return;
                }
                if (!home.isOwner(sender))
                {
                    sender.sendTranslated("&cYou can't remove another users home");
                    return;
                }
                tpManager.deleteHome(home);
                sender.sendTranslated("&aYour home have been removed");
            }
            else if (module.getConfig().multipleHomes)
            {
                Home home = this.tpManager.getHome(sender, context.getString(0));
                if (home == null)
                {
                    sender.sendTranslated("&6%s &cCould not be found!", context.getString(0));
                    return;
                }
                if (!home.isOwner(sender))
                {
                    sender.sendTranslated("&cYou can't remove another players home");
                    return;
                }
                tpManager.deleteHome(home);
                sender.sendTranslated("&6%s &chave been removed", context.getString(0));
            }
        }
        else
        {
            context.sendTranslated("&4This command can only be used by users!");
        }
    }

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
    }, usage = "<-public> <-private> <-owned> <-invited>")
    public void listHomes(ParameterizedContext context) throws Exception
    {
        if (!context.isSender(User.class))
        {
            this.getChild("admin").getChild("list").run(context);
            return;
        }

        User user = (User)context.getSender();
        int mask = context.getFlagCount() == 0 ? tpManager.ALL : 0;
        if (context.hasFlag("pub"))
        {
            mask |= tpManager.PUBLIC;
        }
        if (context.hasFlag("priv"))
        {
            mask |= tpManager.PRIVATE;
        }
        if (context.hasFlag("o"))
        {
            mask |= tpManager.OWNED;
        }
        if (context.hasFlag("i"))
        {
            mask |= tpManager.INVITED;
        }
        Set<Home> homes = tpManager.listHomes(user, mask);
        if (homes.isEmpty())
        {
            user.sendTranslated("&cThe query returned null homes!");
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
            User sender = (User)context.getSender();
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
                    sender.sendTranslated("&cYou can't invite a person to a public home >:(");
                }
                User invited = CubeEngine.getUserManager().getUser(context.getString(0), false);
                if (invited == null)
                {
                    sender.sendTranslated("&cThat user could not be found!");
                    return;
                }
                if (invited.equals(sender))
                {
                    sender.sendTranslated("&cYou cannot invite yourself to your own home!");
                    return;
                }
                if (home.isInvited(invited))
                {
                    sender.sendTranslated("&2%s &cis already invited to your home!", invited.getDisplayName());
                    return;
                }
                home.invite(invited);
                if (invited.isOnline())
                {
                    invited
                        .sendTranslated("&2%s &ehas invited you to his home. To access it do: /home &2%s&e:&6home", sender
                            .getDisplayName(), sender.getName());
                }
                sender.sendTranslated("&2%s &aIs now invited to your home", context.getString(0));
            }
            else if (module.getConfig().multipleHomes)
            {
                Home home = tpManager.getHome(sender, context.getString(0));
                if (home == null)
                {
                    sender.sendTranslated("&6%s &cis not a home!", context.getString(0));
                    return;
                }
                if (!home.isOwner(sender))
                {
                    sender.sendTranslated("&4You can't edit another players home");
                    return;
                }
                if (home.isPublic())
                {
                    sender.sendTranslated("&cYou can't invite a person to a public home >:(");
                }
                User invited = CubeEngine.getUserManager().getUser(context.getString(1), false);
                if (invited == null)
                {
                    sender.sendTranslated("&cThat user could not be found!");
                    return;
                }
                if (home.isInvited(invited))
                {
                    sender.sendTranslated("&2%s &cis already invited to &6%s!", invited.getDisplayName(), home.getName());
                    return;
                }
                home.invite(invited);
                if (invited.isOnline())
                {
                    invited.sendTranslated("&2%s &ehas invited you to his home &6%s&e. To access it do: /home &2%s&e:&6%s",
                                        sender.getDisplayName(), context.getString(0), sender.getName(), context.getString(0));
                }
                sender.sendTranslated("&2%s &aIs now invited to &6%s", context.getString(1), context.getString(0));
            }
        }
        else
        {
            context.sendTranslated("&4This command can only be used by users!");
        }
    }

    @Command(desc = "Uninvite a user from one of your homes", min = 1, max = 2, usage = "[home] <user>", permDefault = PermDefault.TRUE)
    public void unInvite(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            if (context.getArgCount() == 1)
            {
                Home home = tpManager.getHome(sender, "home");
                if (home == null)
                {
                    sender.sendTranslated("&cYou don't have a home!");
                    return;
                }
                if (!home.isOwner(sender))
                {
                    sender.sendTranslated("&cYou can't edit another players home");
                    return;
                }
                if (home.isPublic())
                {
                    sender.sendTranslated("&cYou can't uninvite a person from a public home >:(");
                    return;
                }
                User invited = CubeEngine.getUserManager().getUser(context.getString(0), false);
                if (invited == null)
                {
                    sender.sendTranslated("&cYou can't uninvite a user that never have played on this server!!");
                    return;
                }
                if (!home.isInvited(invited))
                {
                    sender.sendTranslated("&2%s &cIs not invited to your home!", invited.getDisplayName());
                    return;
                }
                home.unInvite(invited);
                if (invited.isOnline())
                {
                    invited.sendTranslated("&eYou are no longer invited to &2%s&e's home", sender.getDisplayName());
                }
                sender.sendTranslated("&2%s &eIs no longer invited to home", context.getString(0));
            }
            else if (module.getConfig().multipleHomes)
            {
                Home home = tpManager.getHome(sender, context.getString(0));
                if (home == null)
                {
                    sender.sendTranslated("&6%s &cis not a home!", context.getString(0));
                    return;
                }
                if (!home.getOwner().equals(sender))
                {
                    sender.sendTranslated("&cYou can't edit another players home");
                    return;
                }
                if (home.isPublic())
                {
                    sender.sendTranslated("&cYou can't uninvite a person from a public home >:(");
                    return;
                }
                User invited = CubeEngine.getUserManager().getUser(context.getString(1), false);
                if (invited == null)
                {
                    sender.sendTranslated("&cYou can't univite a user that never have played on this server!!");
                    return;
                }
                if (!home.isInvited(invited))
                {
                    sender.sendTranslated("&2%s &cIs not invited to &6%s&c!", invited.getDisplayName(), home.getName());
                    return;
                }
                home.unInvite(invited);
                if (invited.isOnline())
                {
                    invited
                        .sendTranslated("&eYou are no longer invited to &2%s&e's home &6%s", sender.getDisplayName(), context
                            .getString(0));
                }
                sender
                    .sendTranslated("&2%s &eIs no longer invited to &6%s", context.getString(1), context.getString(0));
            }
        }
        else
        {
            context.sendTranslated("&4This command can only be used by users!");
        }
    }

    @Command(names = {
        "makeprivate", "setprivate", "private"
    }, desc = "Make one of your homes private", min = 0, max = 1, permDefault = PermDefault.TRUE)
    public void makePrivate(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
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
                context.sendTranslated("&cYour home is already private!");
                return;
            }
            home.setVisibility(TeleportPoint.Visibility.PRIVATE);
            context.sendTranslated("&aYour home is now private");
            return;
        }
        context.sendTranslated("&4This command can only be used by users!");
    }

    @Command(names = {
        "makepublic", "setpublic", "public"
    }, desc = "Make one of your homes public", min = 0, max = 1, permDefault = PermDefault.TRUE)
    public void makePublic(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
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
                context.sendTranslated("&cYour home is already public!");
                return;
            }
            home.setVisibility(TeleportPoint.Visibility.PUBLIC);
            context.sendTranslated("&aYour home is now public");
            return;
        }
        context.sendTranslated("&4This command can only be used by users!");
    }
}
