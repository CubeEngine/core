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

import java.io.File;

import org.bukkit.Location;

import com.sun.java.swing.plaf.motif.resources.motif;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.module.service.Selector;
import de.cubeisland.engine.core.module.service.Service;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.math.BlockVector3;
import de.cubeisland.engine.core.util.math.shape.Cuboid;
import de.cubeisland.engine.core.util.math.shape.Shape;
import de.cubeisland.engine.portals.config.PortalConfig;

public class PortalCommands extends ContainerCommand
{
    private Portals module;
    private PortalManager manager;

    public PortalCommands(Portals module, PortalManager manager)
    {
        super(module, "portals", "The portal commands");
        this.module = module;
        this.manager = manager;
    }

    // TODO dest param (world)
    @Alias(names = "mvpc")
    @Command(desc = "Creates a new Portal" , usage = "<name>", min = 1, max = 1)
    public void create(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            Selector selector = this.getModule().getCore().getModuleManager().getServiceManager().getServiceImplementation(Selector.class);
            User sender = (User)context.getSender();
            if (selector.getSelection(sender) instanceof Cuboid)
            {
                if (this.manager.getPortal(context.getString(0)) == null)
                {
                    Location p1 = selector.getFirstPoint(sender);
                    Location p2 = selector.getSecondPoint(sender);
                    PortalConfig config = this.getModule().getCore().getConfigFactory().create(PortalConfig.class);
                    config.location.from = new BlockVector3(p1.getBlockX(), p1.getBlockY(), p1.getBlockZ());
                    config.location.to = new BlockVector3(p2.getBlockX(), p2.getBlockY(), p2.getBlockZ());
                    config.owner = sender.getOfflinePlayer();
                    config.world = p1.getWorld().getName();
                    config.setFile(new File(manager.portalsDir, context.getString(0) + ".yml"));
                    config.save();
                    Portal portal = new Portal(module, manager, context.getString(0), config);
                    this.manager.addPortal(portal);
                    sender.attachOrGet(PortalsAttachment.class, module).setPortal(portal);
                    context.sendTranslated("&aPortal created! Select a destination using TODO portal modify command");
                    return;
                }
                context.sendTranslated("&cA portal named &6%s&c already exists!", context.getString(0));
            }
            else
            {
                context.sendTranslated("&cPlease select a cuboid first!");
            }
            return;
        }
        context.sendTranslated("&cYou have to be ingame to do this!");
    }

    @Alias(names = "mvps")
    @Command(desc = "Selects an existing portal" , usage = "<name>", min = 1, max = 1)
    public void select(CommandContext context)
    {
        Portal portal = this.manager.getPortal(context.getString(0));
        if (portal == null)
        {
            context.sendTranslated("&cThere is no portal named &6%s", context.getString(0));
            return;
        }
        if (context.getSender() instanceof User)
        {
            ((User)context.getSender()).attachOrGet(PortalsAttachment.class, module).setPortal(portal);
            context.sendTranslated("&aPortal selected: &6%s", context.getString(0));
        }
        context.sendMessage("Y U NO IMPLEMENT ME"); // TODO
    }

    public void info()
    {
        // TODO
    }

    public void remove()
    {
        // TODO
    }

    public void debug()
    {
        // TODO
    }
}
