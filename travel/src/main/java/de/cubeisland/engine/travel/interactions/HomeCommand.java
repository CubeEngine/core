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
import de.cubeisland.engine.core.command.ArgBounds;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandResult;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.permission.PermDefault;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.travel.Travel;
import de.cubeisland.engine.travel.storage.Home;
import de.cubeisland.engine.travel.storage.InviteManager;
import de.cubeisland.engine.travel.storage.TelePointManager;
import de.cubeisland.engine.travel.storage.TeleportInvite;

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
            this.getChild("makeprivate").setUsage("");
            this.getChild("makepublic").setUsage("");
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
                Home home = this.tpManager.getHome(sender, "home");
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
                Location location = home.getLocation();
                if (location == null)
                {
                    context.sendTranslated("&cThis home is in a world that no longer exists!");
                    return null;
                }
                sender.teleport(location, PlayerTeleportEvent.TeleportCause.COMMAND);
                if (home.getWelcomeMsg() != null)
                {
                    context.sendMessage(home.getWelcomeMsg());
                }
                else
                {
                    context.sendTranslated("&aYou have been teleported to your home!");
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
                                context.sendTranslated("&cThis home is in a world that no longer exists!");
                                return null;
                            }
                            sender.teleport(location, PlayerTeleportEvent.TeleportCause.COMMAND);
                            if (home.getWelcomeMsg() != null)
                            {
                                context.sendMessage(home.getWelcomeMsg());
                            }
                            else
                            {
                                sender.sendTranslated("&aYou have been teleported to &6%s&a's default home",
                                                      home.getOwnerName());
                            }
                            return null;
                        }
                    }
                }
                Home home = this.tpManager.getHome(sender, context.getString(0).toLowerCase());
                if (home == null)
                {
                    context.sendTranslated("&6%s&c is not a home", context.getString(0).toLowerCase());
                    return null;
                }

                Location location = home.getLocation();
                if (location == null)
                {
                    context.sendTranslated("&cThis home is in a world that no longer exists!");
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
                        context.sendTranslated("&aYou have been teleported to your home: &6%s", home.getName());
                    }
                    else if (home.isPublic())
                    {
                        context.sendTranslated("&aYou have been teleported to the public home &6%s", home.getName());
                    }
                    else
                    {
                        context.sendTranslated("&aYou have been teleported to &2%s&a's home: &6%s",
                                               home.getOwnerName(), home.getName());
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
    @Command(names = {"set", "sethome"},
             desc = "Set your home", usage = "[HomeName]",
             min = 0, max = 1,
             flags = {@Flag(longName = "public", name = "pub")},
             permDefault = PermDefault.TRUE)
    public void setHome(ParameterizedContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            if (this.tpManager.getNumberOfHomes(sender) >= this.module.getConfig().homes.max) // TODO permission to allow more
            {
                sender.sendTranslated("&4You have reached your maximum number of homes!");
                sender.sendTranslated("&cYou have to delete a home to make a new one");
                return;
            }
            Location location = sender.getLocation();
            if (context.getArgCount() == 0)
            {
                Home test = this.tpManager.getHome(sender, "home");
                if (test != null && test.isOwner(sender))
                {
                    sender.sendTranslated("&cYou already have a home! Maybe you need /home move?");
                    return;
                }
                this.tpManager.createHome(location, "home", sender, VISIBILITY_PRIVATE);
                sender.sendTranslated("&aYour home has been created!");
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
                            sender.sendTranslated("&cA public home by that name already exist. Please choose another name");
                            return;
                        }
                    }
                }
                if (name.contains(":") || name.length() >= 32)
                {
                    sender.sendTranslated("&cHomes may not have names that are longer then 32 characters nor contain colon(:)'s!");
                    return;
                }
                if (this.tpManager.hasHome(name, sender))
                {
                    sender.sendTranslated("&cYou already have a home by that name! Maybe you need /home move?");
                    return;
                }
                this.tpManager.createHome(location, name, sender, visibility);
                sender.sendTranslated("&aYour home &6%s &ahas been created!", context.getString(0));
            }
            return;
        }
        context.sendTranslated("&eOk so I'll need your new Address then. &cNo seriously this won't work!");
    }

    @Command(desc = "Set the welcome message of homes",
             names = {"setgreeting", "greeting", "setwelcome", "setwelcomemsg"},
             min = 1, max = -1, permDefault = PermDefault.TRUE,
             params = {@Param(names = {"home", "h"})},
             usage = "[Welcome message goes here] <home [home name] [-append]>",
            flags = @Flag(longName = "append", name = "a"))
    public void setWelcomeMessage(ParameterizedContext context)
    {
        // TODO append the greeting message
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            Home home;
            if (context.hasParam("home"))
            {
                home = this.tpManager.getHome(sender, context.getString("home"));
                if (home == null || !home.isOwner(sender))
                {
                    sender.sendTranslated("&6%s&c is not a home you're owning!", context.getString("home"));
                    return;
                }
            }
            else
            {
                home = this.tpManager.getHome(sender, "home");
                if (home == null || !home.isOwner(sender))
                {
                    sender.sendTranslated("&cYou don't have a home!");
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
            sender.sendTranslated("&aThe welcome message for the home is now set to: ");
            sender.sendMessage(home.getWelcomeMsg());
        }
    }

    @Command(names = {"move", "replace"},
             desc = "Move a home",
             usage = "[HomeName]",
             min = 0, max = 1,
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
                sender.sendTranslated("&aYour home has been moved to you current location!");
                return;
            }
            if (this.module.getConfig().homes.multipleHomes)
            {
                Home home = this.tpManager.getHome(sender, context.getString(0));
                if (home == null)
                {
                    sender.sendTranslated("&cYou do not have a home named &6%s&c!", context.getString(0));
                    return;
                }
                if (!home.isOwner(sender)) // TODO permission
                {
                    sender.sendTranslated("&cYou are not allowed to move another users home!");
                    return;
                }
                home.setLocation(sender.getLocation());
                home.update();
                sender.sendTranslated("&aThe home &6%s&a has been moved to your current location!", home.getName());
                return;
            }
            return;
        }
        context.sendTranslated("&cI am calling the moving company right now!");
    }

    @Alias(names = {"remhome", "removehome", "delhome", "deletehome"})
    @Command(names = {"remove", "delete", "rem", "del"},
             desc = "Remove a home", usage = "[HomeName]",
             min = 0, max = 1,
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
                    sender.sendTranslated("&6%s &cCould not be found!", context.getString(0));
                    return;
                }
                if (!home.isOwner(sender))
                {
                    sender.sendTranslated("&cYou can't remove another users home");
                    return;
                }
                this.tpManager.deleteHome(home);
                sender.sendTranslated("&aYour home have been removed");
            }
            else if (this.module.getConfig().homes.multipleHomes)
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
                this.tpManager.deleteHome(home);
                sender.sendTranslated("&6%s &chave been removed", context.getString(0));
            }
            return;
        }
        context.sendTranslated("&cSo where do you want to sleep this night?");
    }

    @Alias(names = {"listhomes", "homes"})
    @Command(names = {"list", "listhomes"},
             desc = "List homes you can access",
             permDefault = PermDefault.TRUE, min = 0, max = 0,
             flags = {
        @Flag(name = "pub", longName = "public"),
        @Flag(name = "priv", longName = "private"),
        @Flag(name = "o", longName = "owned"),
        @Flag(name = "i", longName = "invited")},
             usage = "<-public> <-private> <-owned> <-invited>")
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
            user.sendTranslated("&cYou are not invited to any home!");
            return;
        }
        user.sendTranslated("&eHere is a list of your homes: ");
        for (Home home : homes)
        {
            if (home == null) continue; // TODO this should not even happen!!!
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
                    user.sendTranslated("  &2%s&e:&6%s", home.getOwnerName(), home.getName());
                }
            }
        }
    }

    @Command(names = {"ilist", "invited"},
             desc = "List all players invited to your homes",
             min = 0, max = 0,
             permDefault = PermDefault.TRUE)
    public void invitedList(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User user = (User)context.getSender();
            Set<Home> homes = this.tpManager.listHomes(user, this.tpManager.OWNED);
            if (!homes.isEmpty())
            {
                user.sendTranslated("&eHere is a list of all your homes with the users invited to them:");
                for (Home home : homes)
                {
                    Set<TeleportInvite> invites = this.inviteManager.getInvites(home.getModel());
                    if (invites.size() != 0)
                    {
                        context.sendTranslated("  &6%s&e:", home.getName());
                        for (TeleportInvite invite : invites)
                        {
                            context.sendMessage("    &2" + this.module.getCore().getUserManager()
                                                                      .getUser(invite.getUserkey().longValue())
                                                                      .getName());
                        }
                    }
                }
                return;
            }
            context.sendTranslated("&cYou don't have any homes with users invited to them!");
            return;
        }
        context.sendTranslated("&cNo one will ever invite a console to his home.");
    }

    @Command(desc = "Invite a user to one of your homes",
             min = 1, max = 2,
             usage = "[home] <user>",
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
                    sender.sendTranslated("&cYou don't have a home!");
                    return;
                }
                if (!home.isOwner(sender)) // TODO permission
                {
                    sender.sendTranslated("&cYou are not allowed to edit another players home");
                    return;
                }
                if (home.isPublic())
                {
                    sender.sendTranslated("&cYou can't invite a person to a public home >:(");
                    return;
                }
                User invited = context.getUser(0);
                if (invited == null)
                {
                    sender.sendTranslated("&cUser &2%s&c not found!", context.getString(0));
                    return;
                }
                if (invited.equals(sender))
                {
                    sender.sendTranslated("&cYou cannot invite yourself to your own home!");
                    return;
                }
                if (home.isInvited(invited))
                {
                    sender.sendTranslated("&2%s&c is already invited to your home!", invited.getDisplayName());
                    return;
                }
                home.invite(invited);
                if (invited.isOnline())
                {
                    invited.sendTranslated("&2%s&e invited you to his home. To teleport to it use: /home &2%s&e:&6home", sender.getDisplayName(), sender.getName());
                }
                sender.sendTranslated("&2%s&a Is now invited to your home", context.getString(0));
            }
            else if (this.module.getConfig().homes.multipleHomes)
            {
                Home home = this.tpManager.getHome(sender, context.getString(0));
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
            return;
        }
        context.sendTranslated("&cHow about making a phone call to invite someone instead?");
    }

    @Command(desc = "Uninvite a user from one of your homes",
             min = 1, max = 2,
             usage = "[home] <user>",
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
                    sender.sendTranslated("&cYou don't have a home!");
                    return;
                }
                if (!home.isOwner(sender)) // TODO permission
                {
                    sender.sendTranslated("&cYou are not allowed to edit another players home");
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
            else if (this.module.getConfig().homes.multipleHomes)
            {
                Home home = this.tpManager.getHome(sender, context.getString(0));
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
                    invited.sendTranslated("&eYou are no longer invited to &2%s&e's home &6%s", sender.getDisplayName(), context.getString(0));
                }
                sender.sendTranslated("&2%s &eIs no longer invited to &6%s", context.getString(1), context.getString(0));
            }
        }
        else
        {
            context.sendTranslated("&4This command can only be used by users!");
        }
    }

    @Command(names = {"makeprivate", "setprivate", "private"},
             desc = "Make one of your homes private",
             usage = "[home]",
             min = 0, max = 1, permDefault = PermDefault.TRUE)
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
                context.sendTranslated("&cMultiHomes is not enabled!");
                return;
            }
            if (home == null)
            {
                sender.sendTranslated("&cYou do not have a home named &6%s&c!",
                                      context.getString(0) == null ? "home" : context.getString(0));
                return;
            }
            if (!home.isOwner(sender))
            {
                context.sendTranslated("&cYou cannot make homes of other players public!");
                return;
            }
            if (!home.isPublic())
            {
                context.sendTranslated("&cYour home is already private!");
                return;
            }
            home.setVisibility(VISIBILITY_PRIVATE);
            context.sendTranslated("&aYour home is now private");
            return;
        }
        context.sendTranslated("&4This command can only be used by users!");
    }

    @Command(names = {"makepublic", "setpublic", "public"},
             desc = "Make one of your homes public",
             min = 0, max = 1,
             permDefault = PermDefault.TRUE,
             usage = "[home]")
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
                context.sendTranslated("&cMultiHomes is not enabled!");
                return;
            }
            if (home == null)
            {
                sender.sendTranslated("&cYou do not have a home named &6%s&c!",
                                      context.getString(0) == null ? "home" : context.getString(0));
                return;
            }
            if (!home.isOwner(sender))
            {
                context.sendTranslated("&cYou cannot make homes of other players public!");
                return;
            }
            if (home.isPublic())
            {
                context.sendTranslated("&cYour home is already public!");
                return;
            }
            home.setVisibility(VISIBILITY_PUBLIC);
            context.sendTranslated("&aYour home is now public");
            return;
        }
        context.sendTranslated("&4This command can only be used by users!");
    }
}
