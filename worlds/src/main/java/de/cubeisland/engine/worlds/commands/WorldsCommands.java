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
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.exception.IncorrectUsageException;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.Grouped;
import de.cubeisland.engine.core.command.reflected.Indexed;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.Pair;
import de.cubeisland.engine.core.world.ConfigWorld;
import de.cubeisland.engine.core.world.WorldManager;
import de.cubeisland.engine.worlds.Multiverse;
import de.cubeisland.engine.worlds.Universe;
import de.cubeisland.engine.worlds.Worlds;
import de.cubeisland.engine.worlds.config.WorldConfig;

import static de.cubeisland.engine.core.filesystem.FileExtensionFilter.YAML;
import static de.cubeisland.engine.core.util.formatter.MessageType.*;

public class WorldsCommands extends ContainerCommand
{
    private Worlds module;
    private final Multiverse multiverse;
    private final WorldManager wm;

    public WorldsCommands(Worlds module, Multiverse multiverse)
    {
        super(module, "worlds", "Worlds commands");
        this.module = module;
        this.multiverse = multiverse;
        this.wm = module.getCore().getWorldManager();
    }

    @Command(desc = "Creates a new universe",
             indexed = @Grouped(@Indexed(label = "name")))
    public void createuniverse(ParameterizedContext context)
    {
        context.sendMessage("TODO");
        // TODO universe create cmd
    }

    @Command(desc = "Creates a new world",
             indexed = {@Grouped(@Indexed(label = "name")),
                        @Grouped(req = false, value = @Indexed(label = "universe"))},
             params = {@Param(names = {"environment","env"}, type = Environment.class),
                      @Param(names = "seed"),
                      @Param(names = {"worldtype","type"}, type = WorldType.class),
                      @Param(names = {"structure","struct"}, label = "true|false", type = Boolean.class),
                      @Param(names = {"generator","gen"})},
             flags = {@Flag(longName = "recreate",name = "r"),
                     @Flag(longName = "noload",name = "no")})
    public void create(ParameterizedContext context)
    {
        World world = this.wm.getWorld(0);
        if (world != null)
        {
            if (context.hasFlag("r"))
            {
                context.sendTranslated(NEGATIVE, "You have to unload a world before recreating it!");
            }
            else
            {
                context.sendTranslated(NEGATIVE, "A world named {world} already exists and is loaded!", world);
            }
            return;
        }
        Path path = Bukkit.getServer().getWorldContainer().toPath().resolve(context.<String>getArg(0));
        if (Files.exists(path))
        {
            if (context.hasFlag("r"))
            {
                try
                {
                    Path newPath = path.resolveSibling(context.getArg(0) + "_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
                        .format(new Date()));
                    Files.move(path, newPath);
                    context.sendTranslated(POSITIVE, "Old world moved to {name#folder}", path.getFileName().toString());
                }
                catch (IOException e)
                {
                    context.sendTranslated(CRITICAL, "Could not backup old world folder! Aborting Worldcreation");
                    return;
                }
            }
            else
            {
                context.sendTranslated(NEGATIVE, "A world named {name#world} already exists but is not loaded!", context.getArg(
                    0));
                return;
            }
        }
        WorldConfig config = this.getModule().getCore().getConfigFactory().create(WorldConfig.class);
        Path dir;
        Universe universe;
        if (context.hasArg(1))
        {
            universe = multiverse.getUniverse(context.<String>getArg(1));
            if (universe == null)
            {
                universe = multiverse.createUniverse(context.<String>getArg(1));
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
            context.sendTranslated(NEGATIVE, "You have to provide a universe in which to create the world!");
            context.sendMessage(context.getCommand().getUsage(context));
            return;
        }
        config.setFile(dir.resolve(context.getArg(0) + YAML.getExtention()).toFile());
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
                context.sendTranslated(CRITICAL, "A critical Error occured while creating the world!");
                this.getModule().getLog().error(e, e.getLocalizedMessage());
            }
        }
    }

    @Command(desc = "Loads a world from configuration",
             indexed = {@Grouped(@Indexed(label = "world")),
                        @Grouped(req = false, value = @Indexed(label = "universe"))})
    public void load(CommandContext context)
    {
        World world = this.wm.getWorld(context.<String>getArg(0));
        if (world != null)
        {
            context.sendTranslated(POSITIVE, "The world {world} is already loaded!", world);
            return;
        }
        if (multiverse.hasWorld(context.<String>getArg(0)) != null)
        {
            if (context.hasArg(1))
            {
                throw new IncorrectUsageException("You've given too many arguments.");
            }
            world = multiverse.loadWorld(context.<String>getArg(0));
            if (world != null)
            {
                context.sendTranslated(POSITIVE, "World {world} loaded!", world);
            }
            else
            {
                context.sendTranslated(NEGATIVE, "Could not load {name#world}", context.getArg(0));
            }
        }
        else if (Files.exists(Bukkit.getServer().getWorldContainer().toPath().resolve(context.<String>getArg(0))))
        {

            Universe universe;
            if (context.hasArg(1))
            {
                universe = this.multiverse.getUniverse(context.<String>getArg(1));
                if (universe == null)
                {
                    context.sendTranslated(NEGATIVE, "Universe {name} not found!", context.getArg(1));
                    return;
                }
            }
            else if (context.getSender() instanceof User)
            {
                universe = this.multiverse.getUniverseFrom(((User)context.getSender()).getWorld());
            }
            else
            {
                context.sendTranslated(NEGATIVE, "You need to specify a universe to load the world into!");
                return;
            }
            world = this.wm.createWorld(new WorldCreator(context.<String>getArg(0)));
            Set<World> worldToAdd = new HashSet<>();
            worldToAdd.add(world);
            universe.addWorlds(worldToAdd);
            context.sendTranslated(POSITIVE, "World {world} loaded!", world);
        }
        else
        {
            context.sendTranslated(NEGATIVE, "World {input} not found!", context.getArg(0));
        }
    }

    @Command(desc = "Unload a loaded world",
             indexed = @Grouped(@Indexed(label = "world", type = World.class)),
             flags = @Flag(longName = "force", name = "f"))
    public void unload(ParameterizedContext context)
    {
        World world = context.getArg(0);
        World tpWorld = this.multiverse.getUniverseFrom(world).getMainWorld();
        if (tpWorld == world)
        {
            tpWorld = this.multiverse.getMainWorld();
            if (tpWorld == world)
            {
                context.sendTranslated(NEGATIVE, "Cannot unload main world of main universe!");
                context.sendTranslated(NEUTRAL, "/worlds setmainworld <world>");
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
                    context.sendTranslated(NEGATIVE, "Could not teleport every player out of the world to unload!");
                    return;
                }
            }
            context.sendTranslated(POSITIVE, "Teleported all players out of {world}", world);
        }
        if (this.wm.unloadWorld(world, true))
        {
            context.sendTranslated(POSITIVE, "Unloaded the world {world}!", world);
        }
        else
        {
            context.sendTranslated(NEGATIVE, "Could not unload {world}", world);
            if (!world.getPlayers().isEmpty())
            {
                int amount = world.getPlayers().size();
                context.sendTranslatedN(NEUTRAL, amount, "There is still one player on that map!",
                                        "There are still {amount} players on that map!", world.getPlayers().size());
            }
        }
    }

    @Command(desc = "Remove a world", indexed = @Grouped(@Indexed(label = "world")),
    flags = @Flag(name = "f", longName = "folder"))
    public void remove(ParameterizedContext context)
    {
        World world = this.wm.getWorld(context.<String>getArg(0));
        if (world != null)
        {
            context.sendTranslated(NEGATIVE, "You have to unload the world first!");
            return;
        }
        Universe universe = multiverse.hasWorld(context.<String>getArg(0));
        if (universe == null)
        {
            context.sendTranslated(NEGATIVE, "World {input} not found!", context.getArg(0));
            return;
        }
        universe.removeWorld(context.<String>getArg(0));
        if (context.hasFlag("f") && module.perms().REMOVE_WORLDFOLDER.isAuthorized(context.getSender()))
        {
            Path path = Bukkit.getServer().getWorldContainer().toPath().resolve(context.<String>getArg(0));
            try
            {
                Files.delete(path);
            }
            catch (IOException e)
            {
                module.getLog().error(e, "Error while deleting world folder!");
            }
            context.sendTranslated(NEGATIVE, "Configuration and folder for the world {name#world} removed!", context.getArg(
                0));
        }
        else
        {
            context.sendTranslated(NEGATIVE, "Configuration for the world {name#world} removed!", context.getArg(0));
        }
    }

    @Command(desc = "Lists all worlds")
    public void list(CommandContext context)
    {
        context.sendTranslated(POSITIVE, "The following worlds do exist:");
        for (Universe universe : this.multiverse.getUniverses())
        {
            for (Pair<String, WorldConfig> pair : universe.getAllWorlds())
            {
                World world = this.wm.getWorld(pair.getLeft());
                if (world == null)
                {
                    context.sendTranslated(POSITIVE, "{name#world} {input#environement:color=INDIGO} {text:(not loaded):color=RED} in the universe {name}", pair.getLeft(), pair.getRight().generation.environment.name(), universe.getName());
                }
                else
                {
                    context.sendTranslated(POSITIVE, "{name#world} {input#environement:color=INDIGO} in the universe {name}", pair.getLeft(), pair.getRight().generation.environment.name(), universe.getName());
                }
            }
        }
    }
    // list / list worlds that you can enter

    @Command(desc = "Show info about a world", indexed = @Grouped(@Indexed(label = "world")))
    public void info(CommandContext context)
    {
        WorldConfig config = multiverse.getWorldConfig(context.<String>getArg(0));
        if (config == null)
        {
            context.sendTranslated(NEGATIVE, "World {input} not found!", context.getArg(0));
        }
        context.sendTranslated(POSITIVE, "World information for {input#world}:", context.getArg(0));
        context.sendMessage("TODO"); // TODO finish worlds info cmd
    }
    // info

    @Command(desc = "Lists the players in a world", indexed = @Grouped(@Indexed(label = "world", type = World.class)))
    public void listplayers(CommandContext context)
    {
        World world = context.getArg(0);
        if (world.getPlayers().isEmpty())
        {
            context.sendTranslated(NEUTRAL, "There are no players in {world}", world);
        }
        else
        {
            context.sendTranslated(POSITIVE, "The following players are in {world}", world);
            String s = ChatFormat.YELLOW + "  -" + ChatFormat.GOLD;
            for (Player player : world.getPlayers())
            {
                context.sendMessage(s + player.getDisplayName());
            }
        }
    }

    // create nether & create end commands / auto link to world / only works for NORMAL Env worlds

    @Command(desc = "Sets the main world", indexed = @Grouped(@Indexed(label = "world", type = World.class)))
    public void setMainWorld(CommandContext context)
    {
        World world = context.getArg(0);
        Universe universe = multiverse.getUniverseFrom(world);
        universe.getConfig().mainWorld = new ConfigWorld(this.wm, world);
        context.sendTranslated(POSITIVE, "{world} is now the main world of the universe {name}", world, universe.getName());
    }
    // set main world (of universe) (of universes)
    // set main universe

    @Command(desc = "Moves a world into another universe",
             indexed = {@Grouped(@Indexed(label = "world", type = World.class)),
                        @Grouped(@Indexed(label = "universe"))})
    public void move(CommandContext context)
    {
        World world = context.getArg(0);
        Universe universe = this.multiverse.getUniverse(context.<String>getArg(1));
        if (universe == null)
        {
            context.sendTranslated(NEGATIVE, "Universe {input} not found!", context.getArg(1));
            return;
        }
        if (universe.hasWorld(world.getName()))
        {
            context.sendTranslated(NEGATIVE, "{world} is already in the universe {name}", world, universe.getName());
            return;
        }
        Universe oldUniverse = multiverse.getUniverseFrom(world);
        WorldConfig worldConfig = multiverse.getWorldConfig(world.getName());
        try
        {
            oldUniverse.removeWorld(world.getName());
            oldUniverse.reload();

            worldConfig.setFile(universe.getDirectory().resolve(world.getName() + YAML.getExtention()).toFile());
            worldConfig.save();

            universe.reload();
        }
        catch (IOException e)
        {
            context.sendTranslated(CRITICAL, "Could not reload the universes");
            this.getModule().getLog().error(e, "Error while reloading after moving world to universe");
            return;
        }
        for (Player player : world.getPlayers())
        {
            User user = this.getModule().getCore().getUserManager().getExactUser(player.getUniqueId());
            user.sendTranslated(POSITIVE, "The world you are in got moved into an other universe!");
            oldUniverse.savePlayer(user, world);
            universe.loadPlayer(user);
        }
        context.sendTranslated(POSITIVE, "World successfully moved!");
    }
    // move to other universe

    @Command(desc = "Teleports to the spawn of a world", indexed = @Grouped(@Indexed(label = {"world","u:<universe>"})))
    public void spawn(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User user = (User)context.getSender();
            String name = context.getArg(0);
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
                            context.sendTranslated(POSITIVE, "You are now at the spawn of {world} (main world of the universe {name})", world, name);
                            return;
                        } // else tp failed
                        return;
                    }
                }
                context.sendTranslated(NEGATIVE, "Universe {input} not found!", name);
                return;
            }
            World world = this.wm.getWorld(name);
            if (world == null)
            {
                context.sendTranslated(NEGATIVE, "World {input} not found!", name);
                return;
            }
            WorldConfig worldConfig = this.multiverse.getUniverseFrom(world).getWorldConfig(world);
            if (user.safeTeleport(worldConfig.spawn.spawnLocation.getLocationIn(world), TeleportCause.COMMAND, false))
            {
                context.sendTranslated(POSITIVE, "You are now at the spawn of {world}!", world);
                return;
            } // else tp failed
            return;
        }
        context.sendTranslated(NEGATIVE, "This command can only be used ingame!");
    }

    @Command(desc = "Loads a player's state for their current world", indexed = @Grouped(@Indexed(label = "player", type = User.class)))
    public void loadPlayer(CommandContext context)
    {
        User user = context.getArg(0);
        Universe universe = multiverse.getUniverseFrom(user.getWorld());
        universe.loadPlayer(user);
        context.sendTranslated(POSITIVE, "Loaded {user}'s data from file!", user);
    }

    @Command(desc = "Save a player's state for their current world", indexed = @Grouped(@Indexed(label = "player", type = User.class)))
    public void savePlayer(CommandContext context)
    {
        User user = context.getArg(0);
        Universe universe = multiverse.getUniverseFrom(user.getWorld());
        universe.savePlayer(user, user.getWorld());
        context.sendTranslated(POSITIVE, "Saved {user}'s data to file!", user.getDisplayName());
    }
}
