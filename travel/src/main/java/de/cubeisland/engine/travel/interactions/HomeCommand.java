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

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandResult;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.exception.IncorrectUsageException;
import de.cubeisland.engine.core.command.exception.PermissionDeniedException;
import de.cubeisland.engine.core.command.parameterized.CommandParameterIndexed;
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
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.travel.Travel;
import de.cubeisland.engine.travel.storage.Home;
import de.cubeisland.engine.travel.storage.HomeManager;
import de.cubeisland.engine.travel.storage.InviteManager;
import de.cubeisland.engine.travel.storage.TeleportInvite;

import static de.cubeisland.engine.core.util.ChatFormat.DARK_GREEN;
import static de.cubeisland.engine.core.util.ChatFormat.YELLOW;
import static de.cubeisland.engine.core.util.formatter.MessageType.*;
import static de.cubeisland.engine.travel.storage.TeleportPointModel.VISIBILITY_PRIVATE;
import static de.cubeisland.engine.travel.storage.TeleportPointModel.VISIBILITY_PUBLIC;
import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

public class HomeCommand extends ContainerCommand
{
    private final HomeManager manager;
    private final InviteManager iManager;
    private final Travel module;

    public HomeCommand(Travel module)
    {
        super(module, "home", "Teleport to your home");
        this.getContextFactory().removeLastIndexed();
        this.addIndexed(new CommandParameterIndexed(new String[]{"home"}, String.class, false, true, 1));
        this.addIndexed(new CommandParameterIndexed(new String[]{"owner"}, User.class, false, true, 1));
        this.module = module;
        this.manager = module.getHomeManager();
        this.iManager = module.getInviteManager();
    }

    @Override
    public CommandResult run(CommandContext context)
    {
        if (!context.isSender(User.class))
        {
            return super.run(context);
        }
        Permission tpPerm = context.getCommand().getChild("tp").getPermission();
        if (tpPerm.isAuthorized(context.getSender()))
        {
            this.tp(context);
            return null;
        }
        throw new PermissionDeniedException(tpPerm);
    }

    @Command(desc = "Teleport to a home",
    indexed = {@Grouped(req = false, value = @Indexed("home")),
               @Grouped(req = false, value = @Indexed(value = "owner", type = User.class))})
    @OnlyIngame
    public void tp(CommandContext context)
    {
        User user;
        if (context.hasArg(1))
        {
            user = context.getUser(1);
            if (user == null)
            {
                context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getString(1));
                return;
            }
        }
        else
        {
            user = (User)context.getSender();
        }
        User sender = (User)context.getSender();
        String name = context.getString(0, "home");
        Home home = this.manager.find(user, name);
        if (home == null)
        {
            if (user.equals(sender))
            {
                context.sendTranslated(NEGATIVE, "You have no home named {name#home}! Use {text:/sethome} to set your home", name);
            }
            else
            {
                context.sendTranslated(NEGATIVE, "{user} has no home named {name#home}! Use {text:/sethome} to set your home", user, name);
            }
            return;
        }
        if (module.getPermissions().HOME_TP_OTHER.isAuthorized(sender) || home.isPublic() || home.isOwner(sender) || home.isInvited(sender))
        {
            Location location = home.getLocation();
            if (location == null)
            {
                context.sendTranslated(NEGATIVE, "This home {name} from {user} is in a world that no longer exists!", home.getName(), home.getOwnerName());
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
            return;
        }
        context.sendTranslated(NEGATIVE, "You do not have access to any home named {name#home}!", name);
    }

    @Alias(names = {"sethome"})
    @Command(names = {"set", "sethome"},
             desc = "Set your home",
             indexed = @Grouped(req = false, value = @Indexed("name")),
             flags = {@Flag(longName = "public", name = "pub")})
    @OnlyIngame("Ok so I'll need your new address then. No seriously this won't work!")
    public void setHome(ParameterizedContext context)
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
        this.manager.create(sender, name, sender.getLocation(), context.hasFlag("pub"));
        context.sendTranslated(POSITIVE, "Your home has been created!");
    }

    @Command(desc = "Set the welcome message of homes",
             names = {"setgreeting", "greeting", "setwelcome", "setwelcomemsg"},
             indexed = {@Grouped(@Indexed("home")),
                        @Grouped(req = false, value = @Indexed("welcome message"), greedy = true)},
             params = @Param(names = "owner", type = User.class, permission = "other"),
             flags = @Flag(longName = "append", name = "a"))
    public void setWelcomeMessage(ParameterizedContext context)
    {
        User user;
        if (context.hasParam("owner"))
        {
            user = context.getUser("owner");
            if (user == null)
            {
                context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getString("owner"));
                return;
            }
        }
        else if (context.isSender(User.class))
        {
            user = (User)context.getSender();
        }
        else
        {
            throw new IncorrectUsageException("You need to provide a owner");
        }
        String name = context.getString(0);
        Home home = this.manager.find(user, name);
        if (home == null)
        {
            if (user.equals(context.getSender()))
            {
                context.sendTranslated(NEGATIVE, "You do not own a home named {name#home}!", name);
            }
            else
            {
                context.sendTranslated(NEGATIVE, "{user} has no home named {name#home}!", user, name);
            }
            return;
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
        context.sendTranslated(POSITIVE, "The welcome message for the home {name} is now set to:", home.getName());
        context.sendMessage(home.getWelcomeMsg());
    }

    @Command(names = {"move", "replace"}, desc = "Move a home",
             indexed = {
                 @Grouped(req = false, value = @Indexed("name")),
                 @Grouped(req = false, value = @Indexed(value = "owner", type = User.class))
             })
    @OnlyIngame("I am calling the moving company right now!")
    public void moveHome(CommandContext context)
    {
        User user;
        if (context.hasArg(1))
        {
            user = context.getUser(1);
            if (user == null)
            {
                context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getString(1));
                return;
            }
        }
        else
        {
            user = (User)context.getSender();
        }
        User sender = (User)context.getSender();
        String name = context.getString(0, "home");
        Home home = this.manager.find(user, name);
        if (home == null)
        {

            context.sendTranslated(NEGATIVE, "There is no home named {name#home}! Use {text:/sethome} to set your home", name);
            return;
        }
        if (module.getPermissions().HOME_MOVE_OTHER.isAuthorized(sender) || home.isOwner(sender))
        {
            home.setLocation(sender.getLocation());
            home.update();
            if (user.equals(sender))
            {
                context.sendTranslated(POSITIVE, "Your home {name} has been moved to your current location!", name);
                return;
            }
            context.sendTranslated(POSITIVE, "The home {name} of {user} has been moved to your current location", name, user);
            return;
        }
        context.sendTranslated(NEGATIVE, "You do not have the right to change this home!");
    }

    @Alias(names = {"remhome", "removehome", "delhome", "deletehome"})
    @Command(names = {"remove", "delete", "rem", "del"}, desc = "Remove a home",
             indexed = {@Grouped(req = false, value = @Indexed("name")),
                        @Grouped(req = false, value = @Indexed(value = "owner", type = User.class))})
    public void removeHome(CommandContext context)
    {
        User user;
        if (context.hasArg(1))
        {
            user = context.getUser(1);
            if (user == null)
            {
                context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getString(1));
                return;
            }
        }
        else if (context.isSender(User.class))
        {
            user = (User)context.getSender();
        }
        else
        {
            throw new IncorrectUsageException("So where do you want to sleep this night?");
        }
        String name = context.getString(0, "home");
        Home home = this.manager.find(user, name);
        if (home == null)
        {
            context.sendTranslated(NEGATIVE, "{user} has no home named {name#home}!", user, name);
            return;
        }
        if (module.getPermissions().HOME_REMOVE_OTHER.isAuthorized(context.getSender()) ||
            (context.isSender(User.class) && home.isOwner((User)context.getSender())))
        {
            this.manager.delete(home);
            if (user.equals(context.getSender()))
            {
                context.sendTranslated(POSITIVE, "Your home {name} has been removed!", name);
                return;
            }
            context.sendTranslated(POSITIVE, "The home {name} of {user} has been removed", name, user);
            return;
        }
        context.sendTranslated(NEGATIVE, "You do not have the right to remove this home!");
    }

    @Alias(names = {"listhomes", "homes"})
    @Command(names = {"list", "listhomes"},
             desc = "Lists homes a player can access",
             flags = {@Flag(name = "pub", longName = "public"),
                      @Flag(name = "o", longName = "owned"),
                      @Flag(name = "i", longName = "invited")},
             indexed = @Grouped(req = false, value = @Indexed(value = "player", type = User.class)))
    public void list(ParameterizedContext context) throws Exception
    {
        User user;
        if (context.hasArg(0) && module.getPermissions().HOME_LIST_OTHER.isAuthorized(context.getSender()))
        {
            if ("*".equals(context.getString(0)))
            {
                this.listAll(context);
                return;
            }
            user = context.getUser(0);
            if (user == null)
            {
                context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getString(0));
                return;
            }
        }
        else if (context.isSender(User.class))
        {
            user = (User)context.getSender();
        }
        else
        {
            this.listAll(context);
            return;
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
        listHomes(context, user, homes);
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
        this.listHomes(context, null, this.manager.list(true, true));
    }

    private void listHomes(ParameterizedContext context, User user, Set<Home> homes)
    {
        for (Home home : homes)
        {
            if (home.isPublic())
            {
                if (home.isOwner(user))
                {
                    context.sendTranslated(NEUTRAL, "  {name#home} ({text:public})", home.getName());
                }
                else
                {
                    context.sendTranslated(NEUTRAL, "  {user}:{name#home} ({text:public})", home.getOwnerName(), home.getName());
                }
            }
            else
            {
                if (home.isOwner(user))
                {
                    context.sendTranslated(NEUTRAL, "  {name#home} ({text:private})", home.getName());
                }
                else
                {
                    context.sendTranslated(NEUTRAL, "  {user}:{name#home} ({text:private})", home.getOwnerName(), home.getName());
                }
            }
        }
    }

    @Command(names = {"ilist", "invited"},
             desc = "List all players invited to your homes",
             indexed = @Grouped(req = false, value = @Indexed("home")),
             params = @Param(names = "owner", type = User.class, permission = "other"))
    public void invitedList(ParameterizedContext context)
    {
        User user;
        if (context.hasParam("owner"))
        {
            user = context.getUser("owner");
            if (user == null)
            {
                context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getString(0));
                return;
            }
        }
        else if (context.isSender(User.class))
        {
            user = (User)context.getSender();
        }
        else
        {
            throw new IncorrectUsageException("No one will ever invite a console to his home");
        }
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

    @Command(desc = "Invite a user to one of your homes",
             indexed = {@Grouped(req = false, value = @Indexed("home")),
                        @Grouped(@Indexed(value = "player", type = User.class))})
    @OnlyIngame("How about making a phone call to invite someone instead?")
    public void invite(CommandContext context)
    {
        User sender = (User)context.getSender();
        String name = context.hasArg(1) ? context.getString(0, "home") : "home";
        Home home = this.manager.find(sender, name);
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
            invited.sendTranslated(NEUTRAL, "{user} invited you to their home. To teleport to it use: /home {user} {name#home}", sender, sender, name);
        }
        context.sendTranslated(POSITIVE, "{user} is now invited to your home {name}", context.getString(0), name);
    }

    @Command(desc = "Uninvite a player from one of your homes",
             indexed = {@Grouped(req = false, value = @Indexed("home")),
                        @Grouped(@Indexed(value = "player", type = User.class))})
    @OnlyIngame
    public void unInvite(CommandContext context)
    {
        User sender = (User)context.getSender();
        String name = context.hasArg(1) ? context.getString(0, "home") : "home";
        Home home = this.manager.find(sender, name);
        if (home == null || !home.isOwner(sender))
        {
            context.sendTranslated(NEGATIVE, "You do not own a home named {name#home}!", name);
            return;
        }
        if (home.isPublic())
        {
            context.sendTranslated(NEGATIVE, "This home is public make it private to disallow other to access it.");
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
            invited.sendTranslated(NEUTRAL, "You are no longer invited to {user}'s home {name#home}", sender, name);
        }
        context.sendTranslated(POSITIVE, "{user} is no longer invited to your home {name}", context.getString(0), name);
    }

    @Command(names = {"makeprivate", "setprivate", "private"},
             desc = "Make one of your homes private",
             indexed = {@Grouped(req = false, value = @Indexed("home")),
                        @Grouped(req = false, value = @Indexed(value = "owner", type = User.class))})
    public void makePrivate(CommandContext context)
    {
        User user;
        if (context.hasArg(1) && module.getPermissions().HOME_CHANGE_OTHER.isAuthorized(context.getSender()))
        {
            user = context.getUser(1);
            if (user == null)
            {
                context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getString(1));
                return;
            }
        }
        else if (context.isSender(User.class))
        {
            user = (User)context.getSender();
        }
        else
        {
            throw new IncorrectUsageException("You need to provide a player");
        }
        String name = context.getString(0, "home");
        Home home = this.manager.find(user, name);
        if (home == null)
        {
            context.sendTranslated(NEGATIVE, "{user} has no home named {name#home}!", user, name);
            return;
        }
        if (!home.isPublic())
        {
            context.sendTranslated(NEGATIVE, "This home is already private!");
            return;
        }
        home.setVisibility(VISIBILITY_PRIVATE);
        if (context.getSender().equals(user))
        {
            context.sendTranslated(POSITIVE, "Your home {name} is now private", home.getName());
        }
        else
        {
            context.sendTranslated(POSITIVE, "The home {name} of {user} is now private", home.getOwnerName(), home.getName());
        }
    }

    @Command(names = {"makepublic", "setpublic", "public"},
             desc = "Make one of your homes public",
             indexed = {@Grouped(req = false, value = @Indexed(value = "owner", type = User.class))})
    public void makePublic(CommandContext context)
    {
        User user;
        if (context.hasArg(1) && module.getPermissions().HOME_CHANGE_OTHER.isAuthorized(context.getSender()))
        {
            user = context.getUser(1);
            if (user == null)
            {
                context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getString(1));
                return;
            }
        }
        else if (context.isSender(User.class))
        {
            user = (User)context.getSender();
        }
        else
        {
            throw new IncorrectUsageException("You need to provide a player");
        }
        String name = context.getString(0, "home");
        Home home = this.manager.find(user, name);
        if (home == null)
        {
            context.sendTranslated(NEGATIVE, "{user} has no home named {name#home}!", user, name);
            return;
        }
        if (home.isPublic())
        {
            context.sendTranslated(NEGATIVE, "This home is already public!");
            return;
        }
        home.setVisibility(VISIBILITY_PUBLIC);
        if (context.getSender().equals(user))
        {
            context.sendTranslated(POSITIVE, "Your home {name} is now public", home.getName());
        }
        else
        {
            context.sendTranslated(POSITIVE, "The home {name} of {user} is now public", home.getOwnerName(), home.getName());
        }
    }

    @Alias(names = {"clearhomes"})
    @Command(desc = "Clear all homes (of an user)",
             flags = {@Flag(name = "pub", longName = "public"),
                      @Flag(name = "priv", longName = "private")},
             indexed = @Grouped(req = false, value = @Indexed(value = "player", type = User.class)))
    public CommandResult clear(final ParameterizedContext context)
    {
        if (this.module.getConfig().clearOnlyFromConsole && !(context.getSender() instanceof ConsoleCommandSender))
        {
            context.sendMessage("This command has been disabled for ingame use via the configuration");
            return null;
        }
        if (context.getArgCount() > 0)
        {
            if (context.getUser(0) == null)
            {
                context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getString(0));
                return null;
            }
            else
            {
                if (context.hasFlag("pub"))
                {
                    context.sendTranslated(NEUTRAL, "Are you sure you want to delete all public homes ever created by {user}?", context.getString(0));
                    context.sendTranslated(NEUTRAL, "To delete all the public homes, do: {text:/confirm} before 30 seconds has passed");
                }
                else if (context.hasFlag("priv"))
                {
                    context.sendTranslated(NEUTRAL, "Are you sure you want to delete all private homes ever created by {user}?", context.getString(0));
                    context.sendTranslated(NEUTRAL, "To delete all the private homes, do: {text:/confirm} before 30 seconds has passed");
                }
                else
                {
                    context.sendTranslated(NEUTRAL, "Are you sure you want to delete all homes ever created by {user}?", context.getString(0));
                    context.sendTranslated(NEUTRAL, "To delete all the homes, do: &{text:/confirm} before 30 seconds has passed");
                }
            }
        }
        else
        {
            if (context.hasFlag("pub"))
            {
                context.sendTranslated(NEUTRAL, "Are you sure you want to delete all public homes ever created on this server!?");
                context.sendTranslated(NEUTRAL, "To delete all the public homes of every user, do: {text:/confirm} before 30 seconds has passed");
            }
            else if (context.hasFlag("priv"))
            {
                context.sendTranslated(NEUTRAL, "Are you sure you want to delete all private homes ever created on this server?");
                context.sendTranslated(NEUTRAL, "To delete all the private homes of every user, do: {text:/confirm} before 30 seconds has passed");
            }
            else
            {
                context.sendTranslated(NEUTRAL, "Are you sure you want to delete all homes ever created on this server!?");
                context.sendTranslated(NEUTRAL, "To delete all the homes of every user, do: {text:/confirm} before 30 seconds has passed");
            }
        }
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
}
