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

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandResult;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.CommandParameterIndexed;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.Grouped;
import de.cubeisland.engine.core.command.reflected.Indexed;
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
        this.addIndexed(new CommandParameterIndexed(new String[]{"[owner:]home"}, String.class, false, true, 1));
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
        User sender = (User)context.getSender();
        String name = context.getString(0, "home");
        Home home = this.manager.find(sender, name);
        if (home == null)
        {
            context.sendTranslated(NEGATIVE, "You do not have access to any home named {name#home}! Use {text:/sethome}", name);
            return null;
        }
        Location location = home.getLocation();
        if (location == null)
        {
            context.sendTranslated(NEGATIVE, "This home {name} from {user} is in a world that no longer exists!", home.getName(), home.getOwnerName());
            return null;
        }
        if (sender.teleport(location, COMMAND))
        {
            if (home.getWelcomeMsg() != null)
            {
                context.sendMessage(home.getWelcomeMsg());
            }
            else
            {
                if (home.isOwner(sender))
                {
                    context.sendTranslated(POSITIVE, "You have been teleported to your home {name}!", home.getName());
                }
                else
                {
                    context.sendTranslated(POSITIVE, "You have been teleported to the home {name} of {user}!", home.getName(), home.getOwnerName());
                }
            }
            return null;
        }
        context.sendTranslated(CRITICAL, "The teleportation got aborted!");
        return null;
    }

    @Alias(names = {"sethome"})
    @Command(names = {"set", "sethome"},
             desc = "Set your home",
             indexed = @Grouped(req = false, value = @Indexed("name")),
             flags = {@Flag(longName = "public", name = "pub")})
    public void setHome(ParameterizedContext context)
    {
        if (!(context.getSender() instanceof User))
        {
            return;
            // TODO throw new PermissionDeniedException("Ok so I'll need your new address then. No seriously this won't work!");
        }
        User sender = (User)context.getSender();
        if (this.manager.getCount(sender) >= this.module.getConfig().homes.max) // TODO permission to allow more
        {
            sender.sendTranslated(NEGATIVE, "You have reached your maximum number of homes!");
            sender.sendTranslated(NEUTRAL, "You have to delete a home to make a new one");
            return;
        }
        String name = context.getString(0, "home");
        if (name.contains(":") || name.length() >= 32)
        {
            sender.sendTranslated(NEGATIVE, "Homes may not have names that are longer then 32 characters nor contain colon(:)'s!");
            return;
        }
        if (this.manager.has(sender, name))
        {
            sender.sendTranslated(NEGATIVE, "The home already exists! You can move it with {text:/home move}");
            return;
        }
        this.manager.create(sender, name, sender.getLocation(), context.hasFlag("pub"));
        sender.sendTranslated(POSITIVE, "Your home has been created!");
    }

    @Command(desc = "Set the welcome message of homes",
             names = {"setgreeting", "greeting", "setwelcome", "setwelcomemsg"},
             indexed = @Grouped(req = false, value = @Indexed("welcome message"), greedy = true),
             params = @Param(names = {"home", "h"}),
             flags = @Flag(longName = "append", name = "a"))
    public void setWelcomeMessage(ParameterizedContext context)
    {
        if (!(context.getSender() instanceof User))
        {
            return;
            // TODO message
        }
        User sender = (User)context.getSender();
        String name = context.getString("home", "home");
        Home home = this.manager.find(sender, name);
        if (home == null || !home.isOwner(sender))
        {
            sender.sendTranslated(NEGATIVE, "You do not own a home named {name#home}!", name);
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
        sender.sendTranslated(POSITIVE, "The welcome message for the home is now set to:");
        sender.sendMessage(home.getWelcomeMsg());
    }

    @Command(names = {"move", "replace"},
             desc = "Move a home",
             indexed = @Grouped(req = false, value = @Indexed("name")))
    public void moveHome(CommandContext context)
    {
        if (!(context.getSender() instanceof User))
        {
            return;
            // TODO         context.sendTranslated(NEGATIVE, "I am calling the moving company right now!");
        }
        User sender = (User)context.getSender();
        String name = context.getString(0, "home");
        Home home = this.manager.find(sender, name);
        if (home == null || !home.isOwner(sender))
        {
            sender.sendTranslated(NEGATIVE, "You do not own a home named {name#home}!", name);
            return;
        }
        home.setLocation(sender.getLocation());
        home.update();
        sender.sendTranslated(POSITIVE, "Your home {name} has been moved to your current location!", name);
    }

    @Alias(names = {"remhome", "removehome", "delhome", "deletehome"})
    @Command(names = {"remove", "delete", "rem", "del"},
             desc = "Remove a home",
             indexed = @Grouped(req = false, value = @Indexed("homename")))
    public void removeHome(CommandContext context)
    {
        if (!(context.getSender() instanceof User))
        {
            return;
            // TODO          context.sendTranslated(NEGATIVE, "So where do you want to sleep this night?");
        }
        User sender = (User)context.getSender();
        String name = context.getString(0, "home");
        Home home = this.manager.find(sender, name);
        if (home == null || !home.isOwner(sender))
        {
            sender.sendTranslated(NEGATIVE, "You do not own a home named {name#home}!", name);
            return;
        }
        this.manager.delete(home);
        sender.sendTranslated(POSITIVE, "Your home named {name} has been removed", name);
    }

    @Alias(names = {"listhomes", "homes"})
    @Command(names = {"list", "listhomes"},
             desc = "List homes you can access",
             flags = {@Flag(name = "pub", longName = "public"),
                      @Flag(name = "o", longName = "owned"),
                      @Flag(name = "i", longName = "invited")})
    public void list(ParameterizedContext context) throws Exception
    {
        if (!(context.getSender() instanceof User))
        {
            return;
            // TODO
        }
        User user = (User)context.getSender();
        Set<Home> homes = this.manager.list(user, context.hasFlag("o"), context.hasFlag("pub"), context.hasFlag("i"));
        if (homes.isEmpty())
        {
            user.sendTranslated(NEGATIVE, "No homes are available to you!");
            return;
        }
        user.sendTranslated(NEUTRAL, "The following homes are available to you:");
        for (Home home : homes)
        {
            if (home.isPublic())
            {
                if (home.isOwner(user))
                {
                    user.sendTranslated(NEUTRAL, "  {name#home} ({text:public})", home.getName());
                }
                else
                {
                    user.sendTranslated(NEUTRAL, "  {user}:{name#home} ({text:public})", home.getOwnerName(), home.getName());
                }
            }
            else
            {
                if (home.isOwner(user))
                {
                    user.sendTranslated(NEUTRAL, "  {name#home} ({text:private})", home.getName());
                }
                else
                {
                    user.sendTranslated(NEUTRAL, "  {user}:{name#home} ({text:private})", home.getOwnerName(), home.getName());
                }
            }
        }
    }

    @Command(names = {"ilist", "invited"},
             desc = "List all players invited to your homes")
    public void invitedList(CommandContext context)
    {
        if (!(context.getSender() instanceof User))
        {
            context.sendTranslated(NEGATIVE, "No one will ever invite a console to his home.");
            return; // TODO
        }
        User user = (User)context.getSender();
        Set<Home> homes = this.manager.list(user, true, false, false);
        if (homes.isEmpty())
        {
            context.sendTranslated(NEGATIVE, "You don't have any homes with players invited to them!");
            return;
        }
        user.sendTranslated(NEUTRAL, "Here is a list of all your homes with the players invited to them:");
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
                        @Grouped(@Indexed("user"))})
    public void invite(CommandContext context)
    {
        if (!(context.getSender() instanceof User))
        {
            context.sendTranslated(NEGATIVE, "How about making a phone call to invite someone instead?");
            return; // TODO
        }
        User sender = (User)context.getSender();
        String name = context.hasArg(1) ? context.getString(0, "home") : "home";
        Home home = this.manager.find(sender, name);
        if (home == null || !home.isOwner(sender))
        {
            sender.sendTranslated(NEGATIVE, "You do not own a home named {name#home}!", name);
            return;
        }
        if (home.isPublic())
        {
            sender.sendTranslated(NEGATIVE, "You can't invite a person to a public home.");
            return;
        }
        User invited = context.hasArg(1) ? context.getUser(1) : context.getUser(0);
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
            invited.sendTranslated(NEUTRAL, "{user} invited you to their home. To teleport to it use: /home {name#user} {name#home}", sender, name);
        }
        sender.sendTranslated(POSITIVE, "{user} is now invited to your home {name}", context.getString(0), name);
    }

    @Command(desc = "Uninvite a player from one of your homes",
             indexed = {@Grouped(req = false, value = @Indexed("home")),
                        @Grouped(@Indexed("user"))})
    public void unInvite(CommandContext context)
    {
        if (!(context.getSender() instanceof User))
        {
            context.sendTranslated(NEGATIVE, "This command can only be used ingame!");
            return;
        }
        User sender = (User)context.getSender();
        String name = context.hasArg(1) ? context.getString(0, "home") : "home";
        Home home = this.manager.find(sender, name);
        if (home == null || !home.isOwner(sender))
        {
            sender.sendTranslated(NEGATIVE, "You do not own a home named {name#home}!", name);
            return;
        }
        if (home.isPublic())
        {
            sender.sendTranslated(NEGATIVE, "This home is public make it private to disallow other to access it.");
            return;
        }
        User invited = context.hasArg(1) ? context.getUser(1) : context.getUser(0);
        if (invited == null)
        {
            sender.sendTranslated(NEGATIVE, "Player {user} not found!", context.getString(0));
            return;
        }
        if (invited.equals(sender))
        {
            sender.sendTranslated(NEGATIVE, "You cannot uninvite yourself from your own home!");
            return;
        }
        if (!home.isInvited(invited))
        {
            sender.sendTranslated(NEGATIVE, "{user} is not invited to your home!", invited);
            return;
        }
        home.unInvite(invited);
        if (invited.isOnline())
        {
            invited.sendTranslated(NEUTRAL, "You are no longer invited to {user}'s home {name#home}", sender, name);
        }
        sender.sendTranslated(POSITIVE, "{user} is no longer invited to your home {name}", context.getString(0), name);
    }

    @Command(names = {"makeprivate", "setprivate", "private"},
             desc = "Make one of your homes private",
             indexed = @Grouped(req = false, value = @Indexed("home")))
    public void makePrivate(CommandContext context)
    {
        if (!(context.getSender() instanceof User))
        {
            context.sendTranslated(CRITICAL, "This command can only be used by players!");
            return;
        }
        User sender = (User)context.getSender();
        String name = context.getString(0, "home");
        Home home = this.manager.find(sender, name);
        if (home == null || !home.isOwner(sender))
        {
            sender.sendTranslated(NEGATIVE, "You do not own a home named {name#home}!", name);
            return;
        }
        if (!home.isPublic())
        {
            sender.sendTranslated(NEGATIVE, "This home is already private!");
            return;
        }
        home.setVisibility(VISIBILITY_PRIVATE);
        context.sendTranslated(POSITIVE, "Your home {name} is now private", name);
    }

    @Command(names = {"makepublic", "setpublic", "public"},
             desc = "Make one of your homes public",
             indexed = @Grouped(req = false, value = @Indexed("home")))
    public void makePublic(CommandContext context)
    {
        if (!(context.getSender() instanceof User))
        {
            context.sendTranslated(CRITICAL, "This command can only be used by players!");
            return;
        }
        User sender = (User)context.getSender();
        String name = context.getString(0, "home");
        Home home = this.manager.find(sender, name);
        if (home == null || !home.isOwner(sender))
        {
            sender.sendTranslated(NEGATIVE, "You do not own a home named {name#home}!", name);
            return;
        }
        if (home.isPublic())
        {
            sender.sendTranslated(NEGATIVE, "This home is already public!");
            return;
        }
        home.setVisibility(VISIBILITY_PUBLIC);
        context.sendTranslated(POSITIVE, "Your home {name} is now public", name);
    }
}
