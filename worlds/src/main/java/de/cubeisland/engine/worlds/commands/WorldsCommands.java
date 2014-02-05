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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.Pair;
import de.cubeisland.engine.worlds.Multiverse;
import de.cubeisland.engine.worlds.Universe;
import de.cubeisland.engine.worlds.Worlds;
import de.cubeisland.engine.worlds.config.WorldConfig;

import static de.cubeisland.engine.core.filesystem.FileExtensionFilter.YAML;

public class WorldsCommands extends ContainerCommand
{
    private final Multiverse multiverse;

    public WorldsCommands(Worlds module, Multiverse multiverse)
    {
        super(module, "worlds", "Worlds commands");
        this.multiverse = multiverse;
    }

    @Command(desc = "Creates a new world",
             usage = "<name> {universe} [env <environement>] [seed <seed>] [type <type>] [struct <true|false>] [gen <generator>] [-recreate] [-noload]",
    params = {
    @Param(names = {"environment","env"}, type = Environment.class), // TODO reader
    @Param(names = "seed"),
    @Param(names = {"worldtype","type"}, type = WorldType.class), // TODO reader
    @Param(names = {"structure","struct"}, type = Boolean.class),
    @Param(names = {"generator","gen"})},
    max = 2, min = 1, flags = {
    @Flag(longName = "recreate",name = "r"),
    @Flag(longName = "noload",name = "no"),
    })
    public void create(ParameterizedContext context)
    {
        World world = this.getModule().getCore().getWorldManager().getWorld(0);
        if (world != null)
        {
            if (context.hasFlag("r"))
            {
                context.sendTranslated("&cYou have to unload a world before recreating it!");
            }
            else
            {
                context.sendTranslated("&cA world named &6%s&c already exists and is loaded!", world.getName());
            }
            return;
        }
        Path path = Bukkit.getServer().getWorldContainer().toPath().resolve(context.getString(0));
        if (Files.exists(path))
        {
            if (context.hasFlag("r"))
            {
                try
                {
                    Path newPath = path.resolveSibling(context.getString(0) + "_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
                        .format(new Date()));
                    Files.move(path, newPath);
                    context.sendTranslated("&aOld world moved to &6%s", path.getFileName().toString());
                }
                catch (IOException e)
                {
                    context.sendTranslated("&4Could not backup old world folder! Aborting Worldcreation");
                    return;
                }
            }
            else
            {
                context.sendTranslated("&cA world named &6%s&c already exists but is not loaded!", context.getString(0));
                return;
            }
        }
        WorldConfig config = this.getModule().getCore().getConfigFactory().create(WorldConfig.class);
        Path dir;
        Universe universe;
        if (context.hasArg(1))
        {
            universe = multiverse.getUniverse(context.getString(1));
            if (universe == null)
            {
                universe = multiverse.createUniverse(context.getString(1));
            }
            dir = universe.getDirectory();
        }
        else if (context.getSender() instanceof User)
        {
            universe = multiverse.getUniverseFrom(((User)context.getSender()).getWorld());
            dir = universe.getDirectory();
        }
        else
        {
            context.sendTranslated("&cYou have to provide a universe in which to create the world!");
            context.sendMessage(context.getCommand().getUsage(context));
            return;
        }
        config.setFile(dir.resolve(context.getString(0) + YAML.getExtention()).toFile());
        if (context.hasParam("env"))
        {
            config.generation.environment = context.getParam("env", Environment.NORMAL);
        }
        if (context.hasParam("seed"))
        {
            config.generation.seed = context.getString("seed");
        }
        if (context.hasParam("type"))
        {
            config.generation.worldType = context.getParam("type", WorldType.NORMAL);
        }
        if (context.hasParam("struct"))
        {
            config.generation.generateStructures = context.getParam("struct", true);
        }
        if (context.hasParam("gen"))
        {
            config.generation.customGenerator = context.getString("gen");
        }
        config.save();
        if (!context.hasFlag("no"))
        {
            try
            {
                universe.reload();
            }
            catch (IOException e)
            {
                context.sendTranslated("&4A critical Error occured while creating the world!");
                this.getModule().getLog().error(e, e.getLocalizedMessage());
            }
        }
    }

    @Command(desc = "Loads a world from configuration", usage = "<world>", min = 1, max = 1)
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

    @Command(desc = "Unload a loaded world", usage = "<world> [-f]", max = 1, min = 1,
             flags = @Flag(longName = "force", name = "f"))
    public void unload(ParameterizedContext context)
    {
        World world = this.getModule().getCore().getWorldManager().getWorld(context.getString(0));
        if (world != null)
        {
            World tpWorld = this.multiverse.getUniverseFrom(world).getMainWorld();
            if (tpWorld == world)
            {
                tpWorld = this.multiverse.getMainWorld();
                if (tpWorld == world)
                {
                    context.sendTranslated("&cCannot unload main world of main universe!");
                    // TODO how to change main world
                    return;
                }
            }
            if (context.hasFlag("f") && !world.getPlayers().isEmpty())
            {
                Location spawnLocation = tpWorld.getSpawnLocation();
                spawnLocation.setX(spawnLocation.getX() + 0.5);
                spawnLocation.setZ(spawnLocation.getZ() + 0.5);
                for (Player player : world.getPlayers())
                {
                    if (!player.teleport(spawnLocation))
                    {
                        context.sendTranslated("&cCould not teleport every player out of the world to unload!");
                        return;
                    }
                }
                context.sendTranslated("&aTeleported all players out of &6%s", world.getName());
            }
            if (this.getModule().getCore().getWorldManager().unloadWorld(world, true))
            {
                context.sendTranslated("&aUnloaded the world &6%s&a!", world.getName());
            }
            else
            {
                context.sendTranslated("&cCould not unload &6%s", world.getName());
                if (!world.getPlayers().isEmpty())
                {
                    context.sendTranslated("&eThere are players still on that map! (&6%d&e)", world.getPlayers().size());
                }
            }
            return;
        }
        context.sendTranslated("&aThe world does not exist");
    }

    @Command(desc = "Remove a world", usage = "<world> [-f]",
    flags = @Flag(name = "f", longName = "folder"), max = 1, min = 1)
    public void remove(CommandContext context)
    {
        World world = this.getModule().getCore().getWorldManager().getWorld(context.getString(0));
        if (world != null)
        {
            context.sendTranslated("&cYou have to unload the world first!");
            return;
        }
        Universe universe = multiverse.hasWorld(context.getString(0));
        if (universe == null)
        {
            context.sendTranslated("&cWorld &6%s&c not found!", context.getString(0));
            return;
        }
        universe.removeWorld(context.getString(0));
        context.sendTranslated("&cConfiguration for the world &6%s&c removed!", world.getName());
        // TODO folder flag permission
    }

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

    @Command(desc = "Show info about a world", usage = "<world>", max = 1, min = 1)
    public void info(CommandContext context)
    {
        WorldConfig config = multiverse.getWorldConfig(context.getString(0));
        if (config == null)
        {
            context.sendTranslated("&cWorld &6%s&c not found!", context.getString(0));
        }
        context.sendTranslated("&aWorldInformation for &6%s&a:", context.getString(0));
        context.sendMessage("TODO"); // TODO
    }
    // info

    @Command(desc = "Lists the players in a world", usage = "<world>", min = 1, max = 1)
    public void listplayers(CommandContext context)
    {
        World world = this.getModule().getCore().getWorldManager().getWorld(context.getString(0));
        if (world == null)
        {
            context.sendTranslated("&cWorld &6%s&c not found!", context.getString(0));
            return;
        }
        if (world.getPlayers().isEmpty())
        {
            context.sendTranslated("&eThere are no players in &6%s", world.getName());
        }
        else
        {
            context.sendTranslated("&aThe following players are in &6%s", world.getName());
            String s = ChatFormat.parseFormats(" &e-&6 ");
            for (Player player : world.getPlayers())
            {
                context.sendMessage(s + player.getName());
            }
        }
    }

    // create nether & create end commands / auto link to world / only works for NORMAL Env worlds

    @Command(desc = "Sets the main world")
    public void setMainWorld(CommandContext context)
    {
        context.sendMessage("TODO"); // TODO
    }
    // set main world (of universe) (of universes)
    // set main universe

    @Command(desc = "Moves a world into another universe")
    public void move(CommandContext context)
    {
        context.sendMessage("TODO"); // TODO
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
            WorldConfig worldConfig = this.multiverse.getUniverseFrom(world).getWorldConfig(world);
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
