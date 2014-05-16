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
package de.cubeisland.engine.portals;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.World;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.Grouped;
import de.cubeisland.engine.core.command.reflected.Indexed;
import de.cubeisland.engine.core.module.service.Selector;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.WorldLocation;
import de.cubeisland.engine.core.util.math.BlockVector3;
import de.cubeisland.engine.core.util.math.shape.Cuboid;
import de.cubeisland.engine.portals.config.Destination;

import static de.cubeisland.engine.core.util.formatter.MessageType.*;

public class PortalModifyCommand extends ContainerCommand
{
    private final PortalManager manager;

    public PortalModifyCommand(Portals module, PortalManager manager)
    {
        super(module, "modify", "modifies a portal");
        this.registerAlias(new String[]{"mvpm"}, new String[0]);
        this.manager = manager;
    }

    @Command(desc = "Changes the owner of a portal",
             indexed = {@Grouped(@Indexed(label = "owner", type = User.class)),
                        @Grouped(req = false, value = @Indexed(label = "portal"))})
    public void owner(CommandContext context)
    {
        User user = context.getArg(0);
        Portal portal = null;
        if (context.hasArg(1))
        {
            portal = manager.getPortal(context.<String>getArg(1));
            if (portal == null)
            {
                context.sendTranslated(NEGATIVE, "Portal {input} not found!", context.getArg(1));
                return;
            }
        }
        else if (context.getSender() instanceof User)
        {
            portal = ((User)context.getSender()).attachOrGet(PortalsAttachment.class, getModule()).getPortal();
        }
        if (portal == null)
        {
            context.sendTranslated(NEGATIVE, "You need to define a portal to use!");
            context.sendMessage(context.getCommand().getUsage(context));
            return;
        }
        portal.config.owner = user.getOfflinePlayer();
        portal.config.save();
        context.sendTranslated(POSITIVE, "{user} is now the owner of {name#portal}!", user, portal.getName());
    }

    @Alias(names = "mvpd")
    @Command(names = {"destination","dest"}, desc = "changes the destination of the selected portal",
        indexed = {@Grouped(@Indexed(label = {"!here","world","p:<portal>"})),
                   @Grouped(req = false, value = @Indexed(label = "portal"))})
    public void destination(CommandContext context)
    {
        Portal portal = null;
        if (context.hasArg(1))
        {
            portal = manager.getPortal(context.<String>getArg(1));
            if (portal == null)
            {
                context.sendTranslated(NEGATIVE, "Portal {input} not found!", context.getArg(1));
                return;
            }
        }
        else if (context.getSender() instanceof User)
        {
            portal = ((User)context.getSender()).attachOrGet(PortalsAttachment.class, getModule()).getPortal();
        }
        if (portal == null)
        {
            context.sendTranslated(NEGATIVE, "You need to define a portal to use!");
            context.sendMessage(context.getCommand().getUsage(context));
            return;
        }
        String arg0 = context.getArg(0);
        if ("here".equalsIgnoreCase(arg0))
        {
            if (!(context.getSender() instanceof User))
            {
                context.sendTranslated(NEUTRAL, "The Portal Agency will bring you your portal for just {text:$ 1337} within {amount} weeks", new Random().nextInt(51)+1);
                return;
            }
            portal.config.destination = new Destination(((User)context.getSender()).getLocation());
        }
        else if (arg0.startsWith("p:"))
        {
            Portal destPortal = manager.getPortal(arg0.substring(2));
            if (destPortal == null)
            {
                context.sendTranslated(NEGATIVE, "Portal {input} not found!", arg0.substring(2));
                return;
            }
            portal.config.destination = new Destination(destPortal);
        }
        else
        {
            World world = this.getModule().getCore().getWorldManager().getWorld(arg0);
            if (world == null)
            {
                context.sendTranslated(NEGATIVE, "World {input} not found!", arg0);
                return;
            }
            portal.config.destination = new Destination(world);
        }
        portal.config.save();
        context.sendTranslated(POSITIVE, "Portal destination set!");
    }

    @Command(desc = "Changes a portals location",
             indexed = @Grouped(req = false, value = @Indexed(label = "portal")))
    public void location(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            Selector selector = this.getModule().getCore().getModuleManager().getServiceManager().getServiceImplementation(Selector.class);
            if (selector.getSelection(sender) instanceof Cuboid)
            {
                Portal portal = sender.attachOrGet(PortalsAttachment.class, getModule()).getPortal();
                if (context.hasArg(0))
                {
                    portal = manager.getPortal(context.<String>getArg(0));
                    if (portal == null)
                    {
                        context.sendTranslated(NEGATIVE, "Portal {input} not found!", context.getArg(0));
                        return;
                    }
                }
                if (portal == null)
                {
                    context.sendTranslated(NEGATIVE, "You need to define a portal!");
                    context.sendMessage(context.getCommand().getUsage(context));
                    return;
                }
                Location p1 = selector.getFirstPoint(sender);
                Location p2 = selector.getSecondPoint(sender);
                portal.config.location.from = new BlockVector3(p1.getBlockX(), p1.getBlockY(), p1.getBlockZ());
                portal.config.location.to = new BlockVector3(p2.getBlockX(), p2.getBlockY(), p2.getBlockZ());
                portal.config.save();
                context.sendTranslated(POSITIVE, "Portal {name} updated to your current selection!", portal.getName());
                return;
            }
            context.sendTranslated(NEGATIVE, "Please select a cuboid first!");
            return;
        }
        context.sendTranslated(NEGATIVE, "You have to be ingame to do this!");
    }

    @Command(desc = "Modifies the location where a player exits when teleporting a portal",
             indexed = @Grouped(req = false, value = @Indexed(label = "portal")))
    public void exit(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            Portal portal = sender.attachOrGet(PortalsAttachment.class, getModule()).getPortal();
            if (context.hasArg(0))
            {
                portal = manager.getPortal(context.<String>getArg(0));
                if (portal == null)
                {
                    context.sendTranslated(NEGATIVE, "Portal {input} not found!", context.getArg(0));
                    return;
                }
            }
            if (portal == null)
            {
                context.sendTranslated(NEGATIVE, "You need to define a portal!");
                context.sendMessage(context.getCommand().getUsage(context));
                return;
            }
            Location location = sender.getLocation();
            if (portal.config.world.getWorld() != location.getWorld())
            {
                context.sendTranslated(NEGATIVE, "A portals exit cannot be in an other world than its location!");
                return;
            }
            portal.config.location.destination = new WorldLocation(location);
            portal.config.save();
            context.sendTranslated(POSITIVE, "The portal exit of portal {name} was set to your current location!", portal.getName());
            return;
        }
        context.sendTranslated(NEGATIVE, "You have to be ingame to do this!");
    }

    @Command(desc = "Toggles safe teleportation for this portal",
             indexed = @Grouped(req = false, value = @Indexed(label = "portal")))
    public void togglesafe(CommandContext context)
    {
        Portal portal = null;
        if (context.hasArg(0))
        {
            portal = manager.getPortal(context.<String>getArg(0));
            if (portal == null)
            {
                context.sendTranslated(NEGATIVE, "Portal {input} not found!", context.getArg(0));
                return;
            }
        }
        else if (context.getSender() instanceof User)
        {
            portal = ((User)context.getSender()).attachOrGet(PortalsAttachment.class, getModule()).getPortal();
        }
        if (portal == null)
        {
            context.sendTranslated(NEGATIVE, "You need to define a portal!");
            context.sendMessage(context.getCommand().getUsage(context));
            return;
        }
        portal.config.safeTeleport = !portal.config.safeTeleport;
        portal.config.save();
        if (portal.config.safeTeleport)
        {
            context.sendTranslated(POSITIVE, "The portal {name} will not teleport to an unsafe destination", portal.getName());
        }
        else
        {
            context.sendTranslated(POSITIVE, "The portal {name} will also teleport to an unsafe destination", portal.getName());
        }
    }

    @Command(desc = "Toggles whether entities can teleport with this portal",
             indexed = @Grouped(req = false, value = @Indexed(label = "portal")))
    public void entity(CommandContext context)
    {
        Portal portal = null;
        if (context.hasArg(0))
        {
            portal = manager.getPortal(context.<String>getArg(0));
            if (portal == null)
            {
                context.sendTranslated(NEGATIVE, "Portal {input} not found!", context.getArg(0));
                return;
            }
        }
        else if (context.getSender() instanceof User)
        {
            portal = ((User)context.getSender()).attachOrGet(PortalsAttachment.class, getModule()).getPortal();
        }
        if (portal == null)
        {
            context.sendTranslated(NEGATIVE, "You need to define a portal!");
            context.sendMessage(context.getCommand().getUsage(context));
            return;
        }
        portal.config.teleportNonPlayers = !portal.config.teleportNonPlayers;
        portal.config.save();
        if (portal.config.teleportNonPlayers)
        {
            context.sendTranslated(POSITIVE, "The portal {name} will teleport entities too", portal.getName());
        }
        else
        {
            context.sendTranslated(POSITIVE, "The portal {name} will only teleport players", portal.getName());
        }
    }
}
