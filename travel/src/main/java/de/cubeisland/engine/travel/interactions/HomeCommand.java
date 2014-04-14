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
package de.cubeisland.engine.travel.interactions;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandResult;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.Grouped;
import de.cubeisland.engine.core.command.reflected.Indexed;
import de.cubeisland.engine.core.permission.PermDefault;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.travel.Travel;
import de.cubeisland.engine.travel.storage.Home;
import de.cubeisland.engine.travel.storage.InviteManager;
import de.cubeisland.engine.travel.storage.TelePointManager;
import de.cubeisland.engine.travel.storage.TeleportInvite;

import static de.cubeisland.engine.core.util.formatter.MessageType.*;
import static de.cubeisland.engine.travel.storage.TeleportPointModel.VISIBILITY_PRIVATE;
import static de.cubeisland.engine.travel.storage.TeleportPointModel.VISIBILITY_PUBLIC;

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
    }

    /**
     * We need this because the children aren't available before the command is registered.
     */
    public void initChildren()
    {
        if (module.getConfig().homes.multipleHomes)
        {
            // TODO this.setUsage("<<owner:>home>");
            //this.getContextFactory().setArgBounds(new ArgBounds(0, 1));
        }
        else
        { // Set usage and argBounds according to single homes
            //this.setUsage("<owner>");
            //this.getContextFactory().setArgBounds(new ArgBounds(0, 1));

            //this.getChild("set").setUsage("").getContextFactory().setArgBounds(new ArgBounds(0, 0));
            //this.getChild("move").setUsage("").getContextFactory().setArgBounds(new ArgBounds(0, 0));
            //this.getChild("remove").setUsage("").getContextFactory().setArgBounds(new ArgBounds(0, 0));
            //this.getChild("invite").setUsage("[user]").getContextFactory().setArgBounds(new ArgBounds(1, 1));
            //this.getChild("uninvite").setUsage("[user]").getContextFactory().setArgBounds(new ArgBounds(1, 1));
            //this.getChild("setgreeting").setUsage("[welcome message]");
            //this.getChild("makeprivate").setUsage("");
            //this.getChild("makepublic").setUsage("");
        }
    }

    @Override
    public CommandResult run(CommandContext context)
    {
        if (context.isSender(User.class))
        {
            User sender = (User)context.getSender();
            if (context.getArgCount() == 0)
            {
                Home home = this.tpManager.getHome(sender, "home");
                if (home == null)
                {
                    context.sendTranslated(NEGATIVE, "You don't have a home! Use {text:/sethome}");
                    return null;
                }

                if (!home.isOwner(sender))
                {
                    sender.sendTranslated(NEGATIVE, "You don't have a home! Use {text:/sethome}");
                    return null;
                }
                Location location = home.getLocation();
                if (location == null)
                {
                    context.sendTranslated(NEGATIVE, "This home is in a world that no longer exists!");
                    return null;
                }
                sender.teleport(location, PlayerTeleportEvent.TeleportCause.COMMAND);
                if (home.getWelcomeMsg() != null)
                {
                    context.sendMessage(home.getWelcomeMsg());
                }
                else
                {
                    context.sendTranslated(POSITIVE, "You have been teleported to your home!");
                }
            }
            else if (this.module.getConfig().homes.multipleHomes)
            {
                if (context.getCore().getUserManager().findUser(context.getString(0)) != null)
                {
                    User user = this.module.getCore().getUserManager().findUser(context.getString(0));
                    if (!user.equals(context.getSender())) // user & sender are the same (home named like user)
                    {
                        Home home = this.tpManager.getHome(user, "home");
                        if (home != null && home.canAccess(sender))
                        {
                            Location location = home.getLocation();
                            if (location == null)
                            {
                                context.sendTranslated(NEGATIVE, "This home is in a world that no longer exists!");
                                return null;
                            }
                            sender.teleport(location, PlayerTeleportEvent.TeleportCause.COMMAND);
                            if (home.getWelcomeMsg() != null)
                            {
                                context.sendMessage(home.getWelcomeMsg());
                            }
                            else
                            {
                                sender.sendTranslated(POSITIVE, "You have been teleported to {user}'s default home", home.getOwnerName());
                            }
                            return null;
                        }
                    }
                }
                Home home = this.tpManager.getHome(sender, context.getString(0).toLowerCase());
                if (home == null)
                {
                    context.sendTranslated(NEGATIVE, "Home {name} not found!", context.getString(0).toLowerCase());
                    return null;
                }

                Location location = home.getLocation();
                if (location == null)
                {
                    context.sendTranslated(NEGATIVE, "This home is in a world that no longer exists!");
                    return null;
                }
                sender.teleport(location, PlayerTeleportEvent.TeleportCause.COMMAND);
                if (home.getWelcomeMsg() != null)
                {
                    context.sendMessage(home.getWelcomeMsg());
                }
                else
                {
                    if (home.isOwner(sender))
                    {
                        context.sendTranslated(POSITIVE, "You have been teleported to your home: {name}", home.getName());
                    }
                    else if (home.isPublic())
                    {
                        context.sendTranslated(POSITIVE, "You have been teleported to the public home {name}", home.getName());
                    }
                    else
                    {
                        context.sendTranslated(POSITIVE, "You have been teleported to {user}'s home: {name}", home.getOwnerName(), home.getName());
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

    @Alias(names = {"sethome"})
    @Command(names = {"set", "sethome"},
             desc = "Set your home",
             indexed = @Grouped(req = false, value = @Indexed("homename")),
             flags = {@Flag(longName = "public", name = "pub")},
             permDefault = PermDefault.TRUE)
    public void setHome(ParameterizedContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            if (this.tpManager.getNumberOfHomes(sender) >= this.module.getConfig().homes.max) // TODO permission to allow more
            {
                sender.sendTranslated(NEGATIVE, "You have reached your maximum number of homes!");
                sender.sendTranslated(NEUTRAL, "You have to delete a home to make a new one");
                return;
            }
            Location location = sender.getLocation();
            if (context.getArgCount() == 0)
            {
                Home test = this.tpManager.getHome(sender, "home");
                if (test != null && test.isOwner(sender))
                {
                    sender.sendTranslated(NEGATIVE, "You already have a home! You can move it with {text:/home move}");
                    return;
                }
                this.tpManager.createHome(location, "home", sender, VISIBILITY_PRIVATE);
                sender.sendTranslated(POSITIVE, "Your home has been created!");
            }
            else if (this.module.getConfig().homes.multipleHomes)
            {
                String name = context.getString(0).toLowerCase();
                short visibility = VISIBILITY_PRIVATE;
                if (context.hasFlag("pub"))
                {
                    visibility = VISIBILITY_PUBLIC;
                    if (this.tpManager.getHome(name) != null)
                    {
                        if (this.tpManager.getHome(name).isPublic())
                        {
                            sender.sendTranslated(NEGATIVE, "A public home by that name has already been taken. Please choose another name");
                            return;
                        }
                    }
                }
                if (name.contains(":") || name.length() >= 32)
                {
                    sender.sendTranslated(NEGATIVE, "Homes may not have names that are longer then 32 characters nor contain colon(:)'s!");
                    return;
                }
                if (this.tpManager.hasHome(name, sender))
                {
                    sender.sendTranslated(NEGATIVE, "You already have a home with that name! You can move it with {text:/home move}");
                    return;
                }
                this.tpManager.createHome(location, name, sender, visibility);
                sender.sendTranslated(POSITIVE, "Your home {name} has been created!", context.getString(0));
            }
            return;
        }
        context.sendTranslated(NEGATIVE, "Ok so I'll need your new address then. No seriously this won't work!");
    }

    @Command(desc = "Set the welcome message of homes",
             names = {"setgreeting", "greeting", "setwelcome", "setwelcomemsg"},
             permDefault = PermDefault.TRUE,
             indexed = @Grouped(req = false, value = @Indexed("welcome message"), greedy = true),
             params = @Param(names = {"home", "h"}),
             flags = @Flag(longName = "append", name = "a"))
    public void setWelcomeMessage(ParameterizedContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            Home home;
            if (context.hasParam("home"))
            {
                home = this.tpManager.getHome(sender, context.getString("home"));
                if (home == null || !home.isOwner(sender))
                {
                    sender.sendTranslated(NEGATIVE, "You don't own {name#home}!", context.getString("home"));
                    return;
                }
            }
            else
            {
                home = this.tpManager.getHome(sender, "home");
                if (home == null || !home.isOwner(sender))
                {
                    sender.sendTranslated(NEGATIVE, "You don't have a home!");
                    return;
                }
            }
            if (context.hasFlag("a"))
            {
                home.setWelcomeMsg(home.getWelcomeMsg() + context.getStrings(0));
            }
            else
            {
                home.setWelcomeMsg(context.getStrings(0));
            }
            home.update();
            sender.sendTranslated(POSITIVE, "The welcome message for the home is now set to: ");
            sender.sendMessage(home.getWelcomeMsg());
        }
    }

    @Command(names = {"move", "replace"},
             desc = "Move a home",
             indexed = @Grouped(req = false, value = @Indexed("homename")),
             permDefault = PermDefault.TRUE)
    public void moveHome(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            if (context.getArgCount() == 0 || !this.module.getConfig().homes.multipleHomes)
            {
                Home home = this.tpManager.getHome(sender, "home");
                if (home == null)
                {
                    sender.sendTranslated(NEGATIVE, "Home {name} not found!", context.getString(0));
                    return;
                }
                if (!home.isOwner(sender))
                {
                    sender.sendTranslated(NEGATIVE, "You can't move another players home");
                    return;
                }
                home.setLocation(sender.getLocation());
                home.update();
                sender.sendTranslated(POSITIVE, "Your home has been moved to you current location!");
                return;
            }
            if (this.module.getConfig().homes.multipleHomes)
            {
                Home home = this.tpManager.getHome(sender, context.getString(0));
                if (home == null)
                {
                    sender.sendTranslated(NEGATIVE, "You do not have a home named {name#home}!", context.getString(0));
                    return;
                }
                if (!home.isOwner(sender)) // TODO permission
                {
                    sender.sendTranslated(NEGATIVE, "You are not allowed to move another users home!");
                    return;
                }
                home.setLocation(sender.getLocation());
                home.update();
                sender.sendTranslated(POSITIVE, "The home {name} has been moved to your current location!", home.getName());
                return;
            }
            return;
        }
        context.sendTranslated(NEGATIVE, "I am calling the moving company right now!");
    }

    @Alias(names = {"remhome", "removehome", "delhome", "deletehome"})
    @Command(names = {"remove", "delete", "rem", "del"},
             desc = "Remove a home",
             indexed = @Grouped(req = false, value = @Indexed("homename")),
             permDefault = PermDefault.TRUE)
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
                    sender.sendTranslated(NEGATIVE, "Home {name} not found!", context.getString(0));
                    return;
                }
                if (!home.isOwner(sender))
                {
                    sender.sendTranslated(NEGATIVE, "You can't remove another players home");
                    return;
                }
                this.tpManager.deleteHome(home);
                sender.sendTranslated(POSITIVE, "Your home have been removed");
            }
            else if (this.module.getConfig().homes.multipleHomes)
            {
                Home home = this.tpManager.getHome(sender, context.getString(0));
                if (home == null)
                {
                    sender.sendTranslated(NEGATIVE, "Home {name} not found!", context.getString(0));
                    return;
                }
                if (!home.isOwner(sender))
                {
                    sender.sendTranslated(NEGATIVE, "You can't remove another players home");
                    return;
                }
                this.tpManager.deleteHome(home);
                sender.sendTranslated(NEGATIVE, "{name#home} have been removed", context.getString(0));
            }
            return;
        }
        context.sendTranslated(NEGATIVE, "So where do you want to sleep this night?");
    }

    @Alias(names = {"listhomes", "homes"})
    @Command(names = {"list", "listhomes"},
             desc = "List homes you can access",
             permDefault = PermDefault.TRUE,
             flags = {
        @Flag(name = "pub", longName = "public"),
        @Flag(name = "priv", longName = "private"),
        @Flag(name = "o", longName = "owned"),
        @Flag(name = "i", longName = "invited")})
    public void listHomes(ParameterizedContext context) throws Exception
    {
        if (!context.isSender(User.class))
        {
            this.getChild("admin").getChild("list").run(context);
            return;
        }

        User user = (User)context.getSender();
        int mask = context.getFlagCount() == 0 ? this.tpManager.ALL : 0;
        if (context.hasFlag("pub"))
        {
            mask |= this.tpManager.PUBLIC;
        }
        if (context.hasFlag("priv"))
        {
            mask |= this.tpManager.PRIVATE;
        }
        if (context.hasFlag("o"))
        {
            mask |= this.tpManager.OWNED;
        }
        if (context.hasFlag("i"))
        {
            mask |= this.tpManager.INVITED;
        }
        Set<Home> homes = this.tpManager.listHomes(user, mask);
        if (homes.isEmpty())
        {
            user.sendTranslated(NEGATIVE, "You are not invited to any home!");
            return;
        }
        user.sendTranslated(NEUTRAL, "Here is a list of your homes: ");
        for (Home home : homes)
        {
            if (home == null) continue; // TODO this should not even happen!!!
            if (home.isOwner(user))
            {
                user.sendMessage("  " + ChatFormat.GOLD + home.getName());
            }
            else
            {
                if (home.isPublic())
                {
                    user.sendTranslated(NONE, "  {text:public}:{name#home}", home.getName());
                }
                else
                {
                    user.sendTranslated(NEUTRAL, "  {user}:{name#home}", home.getOwnerName(), home.getName());
                }
            }
        }
    }

    @Command(names = {"ilist", "invited"},
             desc = "List all players invited to your homes",
             permDefault = PermDefault.TRUE)
    public void invitedList(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User user = (User)context.getSender();
            Set<Home> homes = this.tpManager.listHomes(user, this.tpManager.OWNED);
            if (!homes.isEmpty())
            {
                user.sendTranslated(NEUTRAL, "Here is a list of all your homes with the players invited to them:");
                for (Home home : homes)
                {
                    Set<TeleportInvite> invites = this.inviteManager.getInvites(home.getModel());
                    if (invites.size() != 0)
                    {
                        context.sendTranslated(NEUTRAL, "  {name#home}:", home.getName());
                        for (TeleportInvite invite : invites)
                        {
                            context.sendMessage("    " + ChatFormat.DARK_GREEN + this.module.getCore().getUserManager()
                                                                      .getUser(invite.getUserkey())
                                                                      .getDisplayName());
                        }
                    }
                }
                return;
            }
            context.sendTranslated(NEGATIVE, "You don't have any homes with players invited to them!");
            return;
        }
        context.sendTranslated(NEGATIVE, "No one will ever invite a console to his home.");
    }

    @Command(desc = "Invite a user to one of your homes",
             indexed = {@Grouped(req = false, value = @Indexed("home")),
                        @Grouped(@Indexed("user"))},
             permDefault = PermDefault.TRUE)
    public void invite(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            if (context.getArgCount() == 1 || !this.module.getConfig().homes.multipleHomes)
            {
                Home home = this.tpManager.getHome(sender, "home");
                if (home == null)
                {
                    sender.sendTranslated(NEGATIVE, "You don't have a home!");
                    return;
                }
                if (!home.isOwner(sender)) // TODO permission
                {
                    sender.sendTranslated(NEGATIVE, "You are not allowed to edit another players home");
                    return;
                }
                if (home.isPublic())
                {
                    sender.sendTranslated(NEGATIVE, "You can't invite a person to a public home...");
                    return;
                }
                User invited = context.getUser(0);
                if (invited == null)
                {
                    sender.sendTranslated(NEGATIVE, "Player {user} not found!", context.getString(0));
                    return;
                }
                if (invited.equals(sender))
                {
                    sender.sendTranslated(NEGATIVE, "You cannot invite yourself to your own home!");
                    return;
                }
                if (home.isInvited(invited))
                {
                    sender.sendTranslated(NEGATIVE, "{user} is already invited to your home!", invited);
                    return;
                }
                home.invite(invited);
                if (invited.isOnline())
                {
                    invited.sendTranslated(NEUTRAL, "{user} invited you to his home. To teleport to it use: /home {user}:{text:home}", sender, sender);
                }
                sender.sendTranslated(POSITIVE, "{user} Is now invited to your home", context.getString(0));
            }
            else if (this.module.getConfig().homes.multipleHomes)
            {
                Home home = this.tpManager.getHome(sender, context.getString(0));
                if (home == null)
                {
                    sender.sendTranslated(NEGATIVE, "Home {input} not found!", context.getString(0));
                    return;
                }
                if (!home.isOwner(sender))
                {
                    sender.sendTranslated(CRITICAL, "You can't edit another players home");
                    return;
                }
                if (home.isPublic())
                {
                    sender.sendTranslated(NEGATIVE, "You can't invite a person to a public home >:(");
                }
                User invited = CubeEngine.getUserManager().findExactUser(context.getString(1));
                if (invited == null)
                {
                    sender.sendTranslated(NEGATIVE, "That player could not be found!");
                    return;
                }
                if (home.isInvited(invited))
                {
                    sender.sendTranslated(NEGATIVE, "{user} is already invited to {name#home}!", invited, home.getName());
                    return;
                }
                home.invite(invited);
                if (invited.isOnline())
                {
                    invited.sendTranslated(NEUTRAL, "{user} has invited you to his home {name}. To access it do: /home {user}:{name#home}", sender, context.getString(0), sender, context.getString(0));
                }
                sender.sendTranslated(POSITIVE, "{user} is now invited to {name}", context.getString(1), context.getString(0));
            }
            return;
        }
        context.sendTranslated(NEGATIVE, "How about making a phone call to invite someone instead?");
    }

    @Command(desc = "Uninvite a player from one of your homes",
             indexed = {@Grouped(req = false, value = @Indexed("home")),
                        @Grouped(@Indexed("user"))},
             permDefault = PermDefault.TRUE)
    public void unInvite(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            if (context.getArgCount() == 1)
            {
                Home home = this.tpManager.getHome(sender, "home");
                if (home == null)
                {
                    sender.sendTranslated(NEGATIVE, "You don't have a home!");
                    return;
                }
                if (!home.isOwner(sender)) // TODO permission
                {
                    sender.sendTranslated(NEGATIVE, "You are not allowed to edit another players home");
                    return;
                }
                if (home.isPublic())
                {
                    sender.sendTranslated(NEGATIVE, "You can't uninvite a person from a public home...");
                    return;
                }
                User invited = CubeEngine.getUserManager().findExactUser(context.getString(0));
                if (invited == null)
                {
                    sender.sendTranslated(NEGATIVE, "You can't uninvite a player that never have played on this server!!");
                    return;
                }
                if (!home.isInvited(invited))
                {
                    sender.sendTranslated(NEGATIVE, "{user} Is not invited to your home!", invited);
                    return;
                }
                home.unInvite(invited);
                if (invited.isOnline())
                {
                    invited.sendTranslated(NEUTRAL, "You are no longer invited to {user}'s home", sender);
                }
                sender.sendTranslated(NEUTRAL, "{user} Is no longer invited to home", context.getString(0));
            }
            else if (this.module.getConfig().homes.multipleHomes)
            {
                Home home = this.tpManager.getHome(sender, context.getString(0));
                if (home == null)
                {
                    sender.sendTranslated(NEGATIVE, "{name#home} is not a home!", context.getString(0));
                    return;
                }
                if (!home.getOwner().equals(sender))
                {
                    sender.sendTranslated(NEGATIVE, "You can't edit another players home");
                    return;
                }
                if (home.isPublic())
                {
                    sender.sendTranslated(NEGATIVE, "You can't uninvite a person from a public home...");
                    return;
                }
                User invited = CubeEngine.getUserManager().findExactUser(context.getString(1));
                if (invited == null)
                {
                    sender.sendTranslated(NEGATIVE, "You can't uninvite a player that never have played on this server!!");
                    return;
                }
                if (!home.isInvited(invited))
                {
                    sender.sendTranslated(NEGATIVE, "{user} is not invited to {home#name}!", invited, home.getName());
                    return;
                }
                home.unInvite(invited);
                if (invited.isOnline())
                {
                    invited.sendTranslated(NEUTRAL, "You are no longer invited to {user}'s home {name#home}", sender.getDisplayName(), context.getString(0));
                }
                sender.sendTranslated(NEUTRAL, "{user} is no longer invited to {name#home}", context.getString(1), context.getString(0));
            }
        }
        else
        {
            context.sendTranslated(CRITICAL, "This command can only be used ingame!");
        }
    }

    @Command(names = {"makeprivate", "setprivate", "private"},
             desc = "Make one of your homes private",
             indexed = @Grouped(req = false, value = @Indexed("home")),
             permDefault = PermDefault.TRUE)
    public void makePrivate(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            Home home;
            if (context.getArgCount() == 0)
            {
                home = this.tpManager.getHome(sender, "home");
            }
            else if (this.module.getConfig().homes.multipleHomes)
            {
                home = this.tpManager.getHome(sender, context.getString(0));
            }
            else
            {
                context.sendTranslated(NEGATIVE, "MultiHomes is not enabled!");
                return;
            }
            if (home == null)
            {
                sender.sendTranslated(NEGATIVE, "You do not have a home named {name#home}!", context.getString(0) == null ? "home" : context.getString(0));
                return;
            }
            if (!home.isOwner(sender))
            {
                context.sendTranslated(NEGATIVE, "You cannot make homes of other players public!");
                return;
            }
            if (!home.isPublic())
            {
                context.sendTranslated(NEGATIVE, "Your home is already private!");
                return;
            }
            home.setVisibility(VISIBILITY_PRIVATE);
            context.sendTranslated(POSITIVE, "Your home is now private");
            return;
        }
        context.sendTranslated(CRITICAL, "This command can only be used by players!");
    }

    @Command(names = {"makepublic", "setpublic", "public"},
             desc = "Make one of your homes public",
             indexed = @Grouped(req = false, value = @Indexed("home")),
             permDefault = PermDefault.TRUE)
    public void makePublic(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            Home home;
            if (context.getArgCount() == 0)
            {
                home = this.tpManager.getHome(sender, "home");
            }
            else if (this.module.getConfig().homes.multipleHomes)
            {
                home = this.tpManager.getHome(sender, context.getString(0));
            }
            else
            {
                context.sendTranslated(NEGATIVE, "MultiHomes is not enabled!");
                return;
            }
            if (home == null)
            {
                sender.sendTranslated(NEGATIVE, "You do not have a home named {name#home}!", context.getString(0) == null ? "home" : context.getString(0));
                return;
            }
            if (!home.isOwner(sender))
            {
                context.sendTranslated(NEGATIVE, "You cannot make homes of other players public!");
                return;
            }
            if (home.isPublic())
            {
                context.sendTranslated(NEGATIVE, "Your home is already public!");
                return;
            }
            home.setVisibility(VISIBILITY_PUBLIC);
            context.sendTranslated(POSITIVE, "Your home is now public");
            return;
        }
        context.sendTranslated(CRITICAL, "This command can only be used by players!");
    }
}
