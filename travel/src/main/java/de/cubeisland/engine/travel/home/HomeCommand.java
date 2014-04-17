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
package de.cubeisland.engine.travel.home;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandResult;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.Grouped;
import de.cubeisland.engine.core.command.reflected.Indexed;
import de.cubeisland.engine.core.command.reflected.OnlyIngame;
import de.cubeisland.engine.core.command.result.confirm.ConfirmResult;
import de.cubeisland.engine.core.command.sender.ConsoleCommandSender;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.travel.TpPointCommand;
import de.cubeisland.engine.travel.Travel;
import de.cubeisland.engine.travel.storage.TeleportInvite;

import static de.cubeisland.engine.core.util.ChatFormat.DARK_GREEN;
import static de.cubeisland.engine.core.util.ChatFormat.YELLOW;
import static de.cubeisland.engine.core.util.formatter.MessageType.*;
import static de.cubeisland.engine.travel.storage.TeleportPointModel.VISIBILITY_PRIVATE;
import static de.cubeisland.engine.travel.storage.TeleportPointModel.VISIBILITY_PUBLIC;
import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

public class HomeCommand extends TpPointCommand
{
    private final HomeManager manager;
    private final Travel module;

    public HomeCommand(Travel module)
    {
        super(module, "home", "Teleport to your home");
        this.module = module;
        this.manager = module.getHomeManager();
        this.delegateChild(new ContextFilter()
        {
            @Override
            public String delegateTo(CommandContext context)
            {
                return context.isSender(User.class) ? "tp" : null;
            }
        });
    }

    @OnlyIngame
    @Command(desc = "Teleport to a home",
             indexed = {@Grouped(req = false, value = @Indexed("home")),
                        @Grouped(req = false, value = @Indexed(value = "owner", type = User.class))})
    public void tp(CommandContext context)
    {
        User user = this.getUser(context, 1);
        User sender = (User)context.getSender();
        String name = context.getString(0, "home");
        Home home = this.manager.findOne(user, name);
        if (home == null)
        {
            homeNotFoundMessage(context, user, name);
            context.sendTranslated(NEUTRAL, "Use {text:/sethome} to set your home");
            return;
        }
        if (!home.canAccess(sender))
        {
            if (user.equals(sender))
            {
                homeNotFoundMessage(context, user, name);
                return;
            }
            context.ensurePermission(module.getPermissions().HOME_TP_OTHER);
        }
        Location location = home.getLocation();
        if (location == null)
        {
            homeInDeletedWorldMessage(context, user, home);
            return;
        }
        if (sender.teleport(location, COMMAND))
        {
            if (home.getWelcomeMsg() != null)
            {
                context.sendMessage(home.getWelcomeMsg());
                return;
            }
            if (home.isOwner(sender))
            {
                context.sendTranslated(POSITIVE, "You have been teleported to your home {name}!", home.getName());
                return;
            }
            context.sendTranslated(POSITIVE, "You have been teleported to the home {name} of {user}!", home.getName(), home.getOwnerName());
            return;
        }
        context.sendTranslated(CRITICAL, "The teleportation got aborted!");
    }

    @Alias(names = {"sethome"})
    @Command(names = {"set", "sethome", "create", "createhome"},
             desc = "Set your home",
             indexed = @Grouped(req = false, value = @Indexed("name")),
             flags = {@Flag(longName = "public", name = "pub", permission = "public")})
    @OnlyIngame("Ok so I'll need your new address then. No seriously this won't work!")
    public void create(ParameterizedContext context)
    {
        User sender = (User)context.getSender();
        if (this.manager.getCount(sender) >= this.module.getConfig().homes.max && !module.getPermissions().HOME_SET_MORE.isAuthorized(context.getSender()))
        {
            context.sendTranslated(NEGATIVE, "You have reached your maximum number of homes!");
            context.sendTranslated(NEUTRAL, "You have to delete a home to make a new one");
            return;
        }
        String name = context.getString(0, "home");
        if (name.contains(":") || name.length() >= 32)
        {
            context.sendTranslated(NEGATIVE, "Homes may not have names that are longer then 32 characters nor contain colon(:)'s!");
            return;
        }
        if (this.manager.has(sender, name))
        {
            context.sendTranslated(NEGATIVE, "The home already exists! You can move it with {text:/home move}");
            return;
        }
        Home home = this.manager.create(sender, name, sender.getLocation(), context.hasFlag("pub"));
        context.sendTranslated(POSITIVE, "Your home {name} has been created!", home.getName());
    }

    @Command(desc = "Set the welcome message of homes",
             names = {"greeting", "setgreeting", "setwelcome", "setwelcomemsg"},
             indexed = {@Grouped(@Indexed("home")),
                        @Grouped(req = false, value = @Indexed("welcome message"), greedy = true)},
             params = @Param(names = "owner", type = User.class, permission = "other"),
             flags = @Flag(longName = "append", name = "a"))
    public void greeting(ParameterizedContext context)
    {
        User user = this.getUser(context, "owner");
        String name = context.getString(0);
        Home home = this.manager.getExact(user, name);
        if (home == null)
        {
            homeNotFoundMessage(context, user, name);
            return;
        }
        if (context.hasFlag("a"))
        {
            home.setWelcomeMsg(home.getWelcomeMsg() + context.getStrings(1));
        }
        else
        {
            home.setWelcomeMsg(context.getStrings(1));
        }
        home.update();
        if (home.isOwner(context.getSender()))
        {
            context.sendTranslated(POSITIVE, "The welcome message for your home {name} is now set to:", home.getName());
        }
        else
        {
            context.sendTranslated(POSITIVE, "The welcome message for the home {name} of {user} is now set to:", home.getName(), user);
        }
        context.sendMessage(home.getWelcomeMsg());
    }

    @OnlyIngame("I am calling the moving company right now!")
    @Command(names = {"move", "replace"}, desc = "Move a home",
             indexed = {@Grouped(req = false, value = @Indexed("name")),
                        @Grouped(req = false, value = @Indexed(value = "owner", type = User.class))})
    public void move(CommandContext context)
    {
        User user = this.getUser(context, 1);
        String name = context.getString(0, "home");
        Home home = this.manager.getExact(user, name);
        if (home == null)
        {
            homeNotFoundMessage(context, user, name);
            context.sendTranslated(NEUTRAL, "Use {text:/sethome} to set your home");
            return;
        }
        if (!home.isOwner(context.getSender()))
        {
            context.ensurePermission(module.getPermissions().HOME_MOVE_OTHER);
        }
        User sender = (User)context.getSender();
        home.setLocation(sender.getLocation());
        home.update();
        if (home.isOwner(sender))
        {
            context.sendTranslated(POSITIVE, "Your home {name} has been moved to your current location!", home.getName());
            return;
        }
        context.sendTranslated(POSITIVE, "The home {name} of {user} has been moved to your current location", home.getName(), user);
    }

    @Alias(names = {"remhome", "removehome", "delhome", "deletehome"})
    @Command(names = {"remove", "delete", "rem", "del"}, desc = "Remove a home",
             indexed = {@Grouped(req = false, value = @Indexed("name")),
                        @Grouped(req = false, value = @Indexed(value = "owner", type = User.class))})
    public void remove(CommandContext context)
    {
        User user = this.getUser(context, 1);
        String name = context.getString(0, "home");
        Home home = this.manager.getExact(user, name);
        if (home == null)
        {
            homeNotFoundMessage(context, user, name);
            return;
        }
        if (!home.isOwner(context.getSender()))
        {
            context.ensurePermission(module.getPermissions().HOME_REMOVE_OTHER);
        }
        this.manager.delete(home);
        if (user.equals(context.getSender()))
        {
            context.sendTranslated(POSITIVE, "Your home {name} has been removed!", name);
            return;
        }
        context.sendTranslated(POSITIVE, "The home {name} of {user} has been removed", name, user);
    }

    @Command(desc = "Rename a home",
             indexed = {@Grouped(@Indexed("home")),
                        @Grouped(@Indexed("new name"))},
             params = @Param(names = "owner", type = User.class))
    public void rename(ParameterizedContext context)
    {
        User user = getUser(context, "owner");
        String name = context.getString(0);
        Home home = manager.getExact(user, name);
        if (home == null)
        {
            homeNotFoundMessage(context, user, name);
            return;
        }
        if (!home.isOwner(context.getSender()))
        {
            context.ensurePermission(module.getPermissions().HOME_RENAME_OTHER);
        }
        String newName = context.getString(1);
        if (name.contains(":") || name.length() >= 32)
        {
            context.sendTranslated(NEGATIVE, "Homes may not have names that are longer than 32 characters or contain colon(:)'s!");
            return;
        }
        if (manager.rename(home, newName))
        {
            if (home.isOwner(context.getSender()))
            {
                context.sendTranslated(POSITIVE, "Your home {name} has been renamed to {name}", home.getName(), newName);
                return;
            }
            context.sendTranslated(POSITIVE, "The home {name} of {user} has been renamed to {name}", home.getName(),
                                   user, newName);
            return;
        }
        context.sendTranslated(POSITIVE, "Could not rename the home to {name}", newName);
    }

    @Alias(names = {"listhomes", "homes"})
    @Command(names = {"list", "listhomes"},
             desc = "Lists homes a player can access",
             flags = {@Flag(name = "pub", longName = "public"),
                      @Flag(name = "o", longName = "owned"),
                      @Flag(name = "i", longName = "invited")},
             indexed = @Grouped(req = false, value = @Indexed(value = "owner", type = User.class)))
    public void list(ParameterizedContext context) throws Exception
    {
        if ((context.hasArg(0) && "*".equals(context.getString(0))) || !(context.hasArg(0) || context.isSender(User.class)))
        {
            context.ensurePermission(module.getPermissions().HOME_LIST_OTHER);
            this.listAll(context);
            return;
        }
        User user = this.getUser(context, 0);
        if (!user.equals(context.getSender()))
        {
            context.ensurePermission(module.getPermissions().HOME_LIST_OTHER);
        }
        Set<Home> homes = this.manager.list(user, context.hasFlag("o"), context.hasFlag("pub"), context.hasFlag("i"));
        if (homes.isEmpty())
        {
            if (user.equals(context.getSender()))
            {
                context.sendTranslated(NEGATIVE, "No homes are available to you!");
                return;
            }
            context.sendTranslated(NEGATIVE, "No homes are available to {user}!", user);
            return;
        }
        if (user.equals(context.getSender()))
        {
            context.sendTranslated(NEUTRAL, "The following homes are available to you:");
        }
        else
        {
            context.sendTranslated(NEUTRAL, "The following homes are available to {user}:", user.getDisplayName());
        }
        showList(context, user, homes);
    }

    private void listAll(ParameterizedContext context)
    {
        int count = this.manager.getCount();
        if (count == 0)
        {
            context.sendTranslated(POSITIVE, "There are no homes set.");
            return;
        }
        context.sendTranslatedN(POSITIVE, count, "There is one home set:", "There are {amount} homes set:", count);
        this.showList(context, null, this.manager.list(true, true));
    }

    @Command(names = {"ilist", "invited"},
             desc = "List all players invited to your homes",
             indexed = @Grouped(req = false, value = @Indexed("home")),
             params = @Param(names = "owner", type = User.class, permission = "other"))
    public void invitedList(ParameterizedContext context)
    {
        User user = this.getUser(context, "owner");
        Set<Home> homes = new HashSet<>();
        for (Home home : this.manager.list(user, true, false, false))
        {
            if (!home.getInvited().isEmpty())
            {
                homes.add(home);
            }
        }
        if (homes.isEmpty())
        {
            if (user.equals(context.getSender()))
            {
                context.sendTranslated(NEGATIVE, "You have no homes with players invited to them!");
                return;
            }
            context.sendTranslated(NEGATIVE, "{user} has no homes with players invited to them!", user);
            return;
        }
        if (user.equals(context.getSender()))
        {
            context.sendTranslated(NEUTRAL, "Your following homes have players invited to them:");
        }
        else
        {
            context.sendTranslated(NEUTRAL, "The following homes of {user} have players invited to them:", user);
        }
        for (Home home : homes)
        {
            Set<TeleportInvite> invites = this.iManager.getInvites(home.getModel());
            if (!invites.isEmpty())
            {
                context.sendMessage(YELLOW + "  " + home.getName() + ":");
                for (TeleportInvite invite : invites)
                {
                    context.sendMessage("    " + DARK_GREEN + this.module.getCore().getUserManager().getUser(invite.getUserkey()).getDisplayName());
                }
            }
        }
    }

    @OnlyIngame("How about making a phone call to invite someone instead?")
    @Command(desc = "Invite a user to one of your homes",
             indexed = {@Grouped(req = false, value = @Indexed("home")),
                        @Grouped(@Indexed(value = "player", type = User.class))})
    public void invite(CommandContext context)
    {
        User sender = (User)context.getSender();
        String name = context.hasArg(1) ? context.getString(0, "home") : "home";
        Home home = this.manager.getExact(sender, name);
        if (home == null || !home.isOwner(sender))
        {
            context.sendTranslated(NEGATIVE, "You do not own a home named {name#home}!", name);
            return;
        }
        if (home.isPublic())
        {
            context.sendTranslated(NEGATIVE, "You can't invite a person to a public home.");
            return;
        }
        User invited = context.hasArg(1) ? context.getUser(1) : context.getUser(0);
        if (invited == null)
        {
            context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getString(0));
            return;
        }
        if (invited.equals(sender))
        {
            context.sendTranslated(NEGATIVE, "You cannot invite yourself to your own home!");
            return;
        }
        if (home.isInvited(invited))
        {
            context.sendTranslated(NEGATIVE, "{user} is already invited to your home!", invited);
            return;
        }
        home.invite(invited);
        if (invited.isOnline())
        {
            invited.sendTranslated(NEUTRAL, "{user} invited you to their home. To teleport to it use: /home {name#home} {user}", sender, home.getName(), sender);
        }
        context.sendTranslated(POSITIVE, "{user} is now invited to your home {name}", invited, home.getName());
    }

    @OnlyIngame
    @Command(desc = "Uninvite a player from one of your homes",
             indexed = {@Grouped(req = false, value = @Indexed("home")),
                        @Grouped(@Indexed(value = "player", type = User.class))})
    public void unInvite(CommandContext context)
    {
        User sender = (User)context.getSender();
        String name = context.hasArg(1) ? context.getString(0, "home") : "home";
        Home home = this.manager.getExact(sender, name);
        if (home == null || !home.isOwner(sender))
        {
            context.sendTranslated(NEGATIVE, "You do not own a home named {name#home}!", name);
            return;
        }
        if (home.isPublic())
        {
            context.sendTranslated(NEGATIVE, "This home is public. Make it private to disallow others to access it.");
            return;
        }
        User invited = context.hasArg(1) ? context.getUser(1) : context.getUser(0);
        if (invited == null)
        {
            context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getString(0));
            return;
        }
        if (invited.equals(sender))
        {
            context.sendTranslated(NEGATIVE, "You cannot uninvite yourself from your own home!");
            return;
        }
        if (!home.isInvited(invited))
        {
            context.sendTranslated(NEGATIVE, "{user} is not invited to your home!", invited);
            return;
        }
        home.unInvite(invited);
        if (invited.isOnline())
        {
            invited.sendTranslated(NEUTRAL, "You are no longer invited to {user}'s home {name#home}", sender, home.getName());
        }
        context.sendTranslated(POSITIVE, "{user} is no longer invited to your home {name}", invited, home.getName());
    }

    @Command(names = {"private", "makeprivate", "setprivate"},
             desc = "Make one of your homes private",
             indexed = {@Grouped(req = false, value = @Indexed("home")),
                        @Grouped(req = false, value = @Indexed(value = "owner", type = User.class))})
    public void makePrivate(CommandContext context)
    {
        User user = this.getUser(context, 1);
        if (!user.equals(context.getSender()))
        {
            context.ensurePermission(module.getPermissions().HOME_PRIVATE_OTHER);
        }
        String name = context.getString(0, "home");
        Home home = this.manager.findOne(user, name);
        if (home == null)
        {
            homeNotFoundMessage(context, user, name);
            return;
        }
        if (!home.isPublic())
        {
            context.sendTranslated(NEGATIVE, "This home is already private!");
            return;
        }
        home.setVisibility(VISIBILITY_PRIVATE);
        if (home.isOwner(context.getSender()))
        {
            context.sendTranslated(POSITIVE, "Your home {name} is now private", home.getName());
            return;
        }
        context.sendTranslated(POSITIVE, "The home {name} of {user} is now private", home.getOwnerName(), home.getName());
    }

    @Command(names = {"public", "makepublic", "setpublic"},
             desc = "Make one of your homes public",
             indexed = {@Grouped(req = false, value = @Indexed("home")),
                        @Grouped(req = false, value = @Indexed(value = "owner", type = User.class))})
    public void makePublic(CommandContext context)
    {
        User user = this.getUser(context, 1);
        if (!user.equals(context.getSender()))
        {
            context.ensurePermission(module.getPermissions().HOME_PUBLIC_OTHER);
        }
        String name = context.getString(0, "home");
        Home home = this.manager.findOne(user, name);
        if (home == null)
        {
            homeNotFoundMessage(context, user, name);
            return;
        }
        if (home.isPublic())
        {
            context.sendTranslated(NEGATIVE, "This home is already public!");
            return;
        }
        home.setVisibility(VISIBILITY_PUBLIC);
        if (home.isOwner(context.getSender()))
        {
            context.sendTranslated(POSITIVE, "Your home {name} is now public", home.getName());
            return;
        }
        context.sendTranslated(POSITIVE, "The home {name} of {user} is now public", home.getOwnerName(), home.getName());
    }

    @Alias(names = {"clearhomes"})
    @Command(desc = "Clear all homes (of an user)",
             flags = {@Flag(name = "pub", longName = "public"),
                      @Flag(name = "priv", longName = "private")},
             indexed = @Grouped(req = false, value = @Indexed(value = "owner", type = User.class)))
    public CommandResult clear(final ParameterizedContext context)
    {
        if (this.module.getConfig().clearOnlyFromConsole && !(context.getSender() instanceof ConsoleCommandSender))
        {
            context.sendTranslated(NEGATIVE, "This command has been disabled for ingame use via the configuration");
            return null;
        }
        if (context.hasArg(0))
        {
            if (context.getUser(0) == null)
            {
                context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getString(0));
                return null;
            }
            if (context.hasFlag("pub"))
            {
                context.sendTranslated(NEUTRAL, "Are you sure you want to delete all public homes ever created by {user}?", context.getString(0));
            }
            else if (context.hasFlag("priv"))
            {
                context.sendTranslated(NEUTRAL, "Are you sure you want to delete all private homes ever created by {user}?", context.getString(0));
            }
            else
            {
                context.sendTranslated(NEUTRAL, "Are you sure you want to delete all homes ever created by {user}?", context.getString(0));
            }
        }
        else
        {
            if (context.hasFlag("pub"))
            {
                context.sendTranslated(NEUTRAL, "Are you sure you want to delete all public homes ever created on this server!?");
            }
            else if (context.hasFlag("priv"))
            {
                context.sendTranslated(NEUTRAL, "Are you sure you want to delete all private homes ever created on this server?");
            }
            else
            {
                context.sendTranslated(NEUTRAL, "Are you sure you want to delete all homes ever created on this server!?");
            }
        }
        context.sendTranslated(NEUTRAL, "Confirm with: {text:/confirm} before 30 seconds have passed to delete the homes");
        return new ConfirmResult(new Runnable()
        {
            @Override
            public void run()
            {
                if (context.getArgCount() == 0)
                { // No user
                    manager.massDelete(context.hasFlag("priv"), context.hasFlag("pub"));
                    context.sendTranslated(POSITIVE, "The homes are now deleted");
                }
                else
                {
                    User user = context.getUser(0);
                    manager.massDelete(user, context.hasFlag("priv"), context.hasFlag("pub"));
                    context.sendTranslated(POSITIVE, "Deleted homes.");
                }
            }
        }, context);
    }

    private void homeInDeletedWorldMessage(CommandContext context, User user, Home home)
    {
        if (home.isOwner(user))
        {
            context.sendTranslated(NEGATIVE, "Your home {name} is in a world that no longer exists!", home.getName());
            return;
        }
        context.sendTranslated(NEGATIVE, "The home {name} of {user} is in a world that no longer exists!", home.getName(), home.getOwnerName());
    }

    private void homeNotFoundMessage(CommandContext context, User user, String name)
    {
        if (context.getSender().equals(user))
        {
            context.sendTranslated(NEGATIVE, "You have no home named {name#home}!", name);
            return;
        }
        context.sendTranslated(NEGATIVE, "{user} has no home named {name#home}!", user, name);
    }
}
