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
package de.cubeisland.engine.worlds.commands;

import org.bukkit.World;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.Pair;
import de.cubeisland.engine.worlds.Multiverse;
import de.cubeisland.engine.worlds.Universe;
import de.cubeisland.engine.worlds.config.WorldConfig;

public class WorldCommands extends ContainerCommand
{
    private final Multiverse multiverse;

    public WorldCommands(Module module, Multiverse multiverse)
    {
        super(module, "worlds", "Worlds commands");
        this.multiverse = multiverse;
    }

    @Command(desc = "Creates and loads a new world")
    public void create(ParameterizedContext context)
    {
        // TODO
    }
    // create name environement seed generator worldtype structures?

    @Command(desc = "Loads a world from configuration", usage = "<world>",
    min = 1, max = 1)
    public void load(CommandContext context)
    {
        World world = this.getModule().getCore().getWorldManager().getWorld(context.getString(0));
        if (world != null)
        {
            context.sendTranslated("&aThe world %s is already loaded!", world.getName());
            return;
        }
        if (multiverse.hasWorld(context.getString(0)) != null)
        {
            world = multiverse.loadWorld(context.getString(0));
            if (world != null)
            {
                context.sendTranslated("&aWorld &6%s&a loaded!" , world.getName());
            }
            else
            {
                context.sendTranslated("&cCould not load &6%s", context.getString(0));
            }
        }
        else
        {
            context.sendTranslated("&cWorld &6%s&c not found!", context.getString(0));
        }
    }

    @Command(desc = "Unload a loaded world",
             flags = @Flag(longName = "force", name = "f")
    )
    public void unload(ParameterizedContext context) // -f to tp players out of that world
    {
        // TODO force flag
        World world = this.getModule().getCore().getWorldManager().getWorld(context.getString(0));
        if (world != null)
        {
            if (this.getModule().getCore().getWorldManager().unloadWorld(world, true))
            {
                context.sendTranslated("&aUnloaded the world &6%s&a!", world.getName());
            }
            else
            {
                context.sendTranslated("&cCould not unload &6%s", world.getName());
            }
            return;
        }
        context.sendTranslated("&aThe world does not exist");
    }
    // unload (-f teleports all players out of this world)

    @Command(desc = "Remove a world")
    public void remove(CommandContext context) // -f unload and tp players out
    {
        // TODO
    }
    // remove/delete

    @Command(desc = "Lists all worls")
    public void list(CommandContext context)
    {
        context.sendTranslated("&aThe following worlds do exist:");
        for (Universe universe : this.multiverse.getUniverses())
        {
            for (Pair<String, WorldConfig> pair : universe.getAllWorlds())
            {
                World world = this.getModule().getCore().getWorldManager().getWorld(pair.getLeft());
                if (world == null)
                {
                    context.sendTranslated("&6%s &9%s &c(not loaded)&a in the universe &6%s", pair.getLeft(), pair.getRight().generation.environment.name(), universe.getName());
                }
                else
                {
                    context.sendTranslated("&6%s &9%s&a in the universe &6%s", pair.getLeft(), pair.getRight().generation.environment.name(), universe.getName());
                }
            }
        }
    }
    // list / list worlds that you can enter

    @Command(desc = "Show info about a world")
    public void info(CommandContext context)
    {
        // TODO
    }
    // info

    @Command(desc = "Lists the players in a world")
    public void listplayers(CommandContext context)
    {
        // TODO
    }
    // listplayers in world/universe

    @Command(desc = "Reloads the module")
    public void reload(CommandContext context)
    {
        // TODO
    }
    // reload

    @Command(desc = "Sets the main world")
    public void setMainWorld(CommandContext context)
    {
        // TODO
    }
    // set main world (of universe) (of universes)
    // set main universe

    @Command(desc = "Moves a world into another universe")
    public void move(CommandContext context)
    {
        // TODO
    }
    // move to other universe

    @Command(desc = "Teleports to the spawn of a world", min = 1, max = 1,
    usage = "<u:<universe>|<world>")
    public void spawn(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User user = (User)context.getSender();
            String name = context.getString(0);
            if (name.startsWith("u:"))
            {
                name = name.substring(2);
                for (Universe universe : this.multiverse.getUniverses())
                {
                    if (universe.getName().equalsIgnoreCase(name))
                    {
                        World world = universe.getMainWorld();
                        WorldConfig worldConfig = universe.getWorldConfig(world);
                        if (user.safeTeleport(worldConfig.spawn.spawnLocation.getLocationIn(world), TeleportCause.COMMAND, false))
                        {
                            context.sendTranslated("&aYou are now at the spawn of &6%s&a (main world of the universe &6%s&a)", world.getName(), name);
                            return;
                        } // else tp failed
                        return;
                    }
                }
                context.sendTranslated("&cUniverse &6%s&c not found!", name);
                return;
            }
            World world = this.getModule().getCore().getWorldManager().getWorld(name);
            if (world == null)
            {
                context.sendTranslated("&cWorld &6%s&c not found!");
                return;
            }
            WorldConfig worldConfig = this.multiverse.getUniverse(world).getWorldConfig(world);
            if (user.safeTeleport(worldConfig.spawn.spawnLocation.getLocationIn(world), TeleportCause.COMMAND, false))
            {
                context.sendTranslated("&aYou are now at the spawn of &6%s&a!", name);
                return;
            } // else tp failed
            return;
        }
        context.sendTranslated("&cThis command can only be used ingame!");
    }
}
