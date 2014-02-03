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
package de.cubeisland.engine.worlds;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.TravelAgent;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.InventoryHolder;

import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.WorldLocation;
import de.cubeisland.engine.core.world.ConfigWorld;
import de.cubeisland.engine.core.world.WorldSetSpawnEvent;
import de.cubeisland.engine.worlds.config.WorldConfig;
import de.cubeisland.engine.worlds.config.WorldsConfig;
import de.cubeisland.engine.worlds.player.PlayerConfig;
import de.cubeisland.engine.worlds.player.PlayerDataConfig;

import static de.cubeisland.engine.core.filesystem.FileExtensionFilter.YAML;

/**
 * Holds multiple parallel universes
 */
public class Multiverse implements Listener
{
    private final Worlds module;
    private WorldsConfig config;

    private final Map<String, Universe> universes = new HashMap<>();
    private final Map<World, Universe> worlds = new HashMap<>();

    private final Path dirPlayers;
    private final Path dirUniverses;
    private final Path dirErrors;

    private final Permission universeRootPerm;

    private World mainWorld;

    public Multiverse(Worlds module, WorldsConfig config) throws IOException
    {
        this.module = module;
        this.config = config;
        this.universeRootPerm = this.module.getBasePermission().childWildcard("universe");

        this.dirPlayers = this.module.getFolder().resolve("players"); // config for last world
        Files.createDirectories(this.dirPlayers);
        this.dirErrors = this.module.getFolder().resolve("errors");
        Files.createDirectories(dirErrors);
        this.dirUniverses = this.module.getFolder().resolve("universes");

        if (Files.exists(dirUniverses))
        {
            for (Path universeDir : Files.newDirectoryStream(dirUniverses))
            {
                if (Files.isDirectory(universeDir))
                {
                    String universeName = universeDir.getFileName().toString();
                    if (this.config.mainUniverse == null)
                    {
                        this.config.mainUniverse = universeName;
                        this.config.save();
                    }
                    this.universes.put(universeName, Universe.load(this.module, this, universeDir));
                }
            }
            Set<World> missingWorlds = this.module.getCore().getWorldManager().getWorlds();
            Map<String, Set<World>> found = new HashMap<>();
            for (Entry<String, Universe> entry : this.universes.entrySet())
            {
                found.put(entry.getKey(), new HashSet<>(entry.getValue().getWorlds()));
                missingWorlds.removeAll(entry.getValue().getWorlds());
            }
            if (!missingWorlds.isEmpty())
            {
                CommandSender sender = this.module.getCore().getCommandManager().getConsoleSender();
                sender.sendTranslated("&eDiscovering unknown worlds...");
                this.searchUniverses(found, missingWorlds, sender);
                sender.sendTranslated("&eFinishing research...");
                for (Entry<String, Set<World>> entry : found.entrySet())
                {
                    Universe universe = this.universes.get(entry.getKey());
                    Set<World> foundWorlds = entry.getValue();
                    if (universe == null)
                    {
                        this.universes.put(entry.getKey(), Universe.create(this.module, this,
                                               dirUniverses.resolve(entry.getKey()), foundWorlds));
                    }
                    else
                    {
                        foundWorlds.removeAll(universe.getWorlds());
                        if (foundWorlds.isEmpty())
                        {
                            continue;
                        }
                        universe.addWorlds(foundWorlds);
                    }
                    sender.sendTranslated("&eFound &6%d&e new worlds in the universe &6%s&e!", foundWorlds.size(), entry.getKey());
                }
            }
        }
        else
        {
            this.module.getLog().info("No previous Universes found! Initializing...");
            CommandSender sender = this.module.getCore().getCommandManager().getConsoleSender();
            sender.sendTranslated("&6Scraping together Matter...");
            Map<String, Set<World>> found = new HashMap<>();
            this.searchUniverses(found, this.module.getCore().getWorldManager().getWorlds(), sender);
            sender.sendTranslated("&eFinishing research...");
            for (Entry<String, Set<World>> entry : found.entrySet())
            {
                Path universeDir = dirUniverses.resolve(entry.getKey());
                Files.createDirectories(universeDir);
                this.universes.put(entry.getKey(), Universe.create(this.module, this, universeDir, entry.getValue()));
            }
            sender.sendTranslated("&eFound &6%d&e universes with &6%d&e worlds!", found.size(), this.module.getCore().getWorldManager().getWorlds().size());
        }
        for (Universe universe : this.universes.values())
        {
            for (World world : universe.getWorlds())
            {
                this.worlds.put(world, universe);
            }
        }
        if (this.config.mainUniverse == null || this.universes.get(this.config.mainUniverse) == null)
        {
            Universe universe = this.universes.get("world");
            if (universe == null)
            {
                universe = this.universes.values().iterator().next();
            }
            this.module.getLog().warn("No main universe set. {} is now the main universe!", universe.getName());
            this.config.mainUniverse = universe.getName();
            this.config.save();
        }
        this.mainWorld = this.universes.get(this.config.mainUniverse).getMainWorld();

        this.module.getCore().getEventManager().registerListener(this.module, this);
    }

    private void searchUniverses(Map<String, Set<World>> found, Collection<World> worldList, CommandSender sender)
    {
        for (World world : worldList)
        {
            String universeName;
            if (world.getName().contains("_"))
            {
                universeName = world.getName();
                universeName = universeName.substring(0, universeName.indexOf("_"));
            }
            else
            {
                universeName = world.getName();
            }
            Set<World> worlds = found.get(universeName);
            if (worlds == null)
            {
                sender.sendTranslated("&eDiscovered a new Universe! Heating up stars...");
                worlds = new HashSet<>();
                found.put(universeName, worlds);
            }
            worlds.add(world);
            switch (world.getEnvironment())
            {
            case NORMAL:
                sender.sendTranslated("&6%s&e gets formed by crushing rocks together in the universe &6%s", world.getName(), universeName);
                break;
            case NETHER:
                sender.sendTranslated("&eCooling plasma a bit to make &6%s&e in the universe &6%s", world.getName(), universeName);
                break;
            case THE_END:
                sender.sendTranslated("&eFound a cold rock named &6%s&e in the universe &6%s", world.getName(), universeName);
                break;
            }
        }

    }

    // TODO load World / unload World events

    @EventHandler
    public void onSetSpawn(WorldSetSpawnEvent event)
    {
        WorldConfig worldConfig = this.getWorldConfig(event.getWorld());
        worldConfig.spawn.spawnLocation = new WorldLocation(event.getNewLocation());
        worldConfig.updateInheritance();
        worldConfig.save();
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event)
    {
        try
        {
            Universe oldUniverse = this.getUniverse(event.getFrom());
            Universe newUniverse = this.getUniverse(event.getPlayer().getWorld());
            if (oldUniverse != newUniverse)
            {
                event.getPlayer().closeInventory();
                oldUniverse.savePlayer(event.getPlayer(), event.getFrom());
                newUniverse.loadPlayer(event.getPlayer());
            }
            this.savePlayer(event.getPlayer());
        }
        catch (UniverseCreationException e)
        {
            Path errorFile = dirErrors.resolve(event.getFrom().getName() + "_" + event.getPlayer().getName() + ".dat");
            int i = 1;
            while (Files.exists(errorFile))
            {
                errorFile = dirErrors.resolve(event.getFrom().getName() + "_" + event.getPlayer().getName() + "_" + i++ + ".dat");
            }
            this.module.getLog().warn("The Player {} ported into a universe that couldn't get created! " +
                                          "The overwritten Inventory is saved under /errors/{}", event.getPlayer().getName(), errorFile.getFileName().toString());
            PlayerDataConfig pdc = this.module.getCore().getConfigFactory().create(PlayerDataConfig.class);
            pdc.setHead(new SimpleDateFormat().format(new Date()) + " " +
                            event.getPlayer().getName() + " ported to " + event.getPlayer().getWorld().getName() +
                            " but couldn't create the universe",
                        "This are the items the player had previously. They got overwritten!");
            pdc.setFile(errorFile.toFile());
            pdc.applyFromPlayer(event.getPlayer());
            pdc.save();

            new PlayerDataConfig().applyToPlayer(event.getPlayer());
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event)
    {
        Location to = event.getTo();
        if (event.getFrom().getWorld() == to.getWorld())
        {
            return;
        }
        Universe universe = this.getUniverse(to.getWorld());
        if (!universe.checkPlayerAccess(event.getPlayer(), to.getWorld()))
        {
            event.setCancelled(true); // TODO check old location
            User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
            user.sendTranslated("&cYou are not allowed to enter the universe &6%s&c!", universe.getName());
        }
    }

    @EventHandler
    public void onPortalUse(PlayerPortalEvent event)
    {
        World world = event.getPlayer().getWorld();
        Universe universe = this.getUniverse(world);
        TravelAgent agent = event.getPortalTravelAgent();
        switch (event.getCause())
        {
        case NETHER_PORTAL:
            if (universe.hasNetherTarget(world))
            {
                event.setTo(universe.handleNetherTarget(event.getFrom(), agent));
                event.useTravelAgent(true);
            }
            break;
        case END_PORTAL:
            if (universe.hasEndTarget(world))
            {
                event.setTo(universe.handleEndTarget(event.getFrom()));
                event.useTravelAgent(event.getTo().getWorld().getEnvironment() == Environment.THE_END);
            }
            break;
        }
    }

    @EventHandler
    public void onEntityPortal(EntityPortalEvent event)
    {
        World world = event.getEntity().getWorld();
        Universe universe = this.getUniverse(world);
        TravelAgent agent = event.getPortalTravelAgent();
        if (event.getTo() == null)
        {
            return;
        }
        switch (event.getTo().getWorld().getEnvironment())
        {
        case NETHER:
            if (universe.hasNetherTarget(world))
            {
                event.setTo(universe.handleNetherTarget(event.getFrom(), agent));
                event.useTravelAgent(true);
            }
            break;
        case THE_END:
            if (universe.hasEndTarget(world))
            {
                event.setTo(universe.handleEndTarget(event.getEntity().getLocation()));
                event.useTravelAgent(true);
            }
            break;
        case NORMAL:
            if (event.getFrom().getWorld().getEnvironment() == Environment.THE_END)
            {
                event.setTo(universe.handleEndTarget(event.getFrom()));
            }
            else
            {
                event.setTo(universe.handleNetherTarget(event.getFrom(), event.getPortalTravelAgent()));
                event.useTravelAgent(true);
            }
        }
        if (this.getUniverse(event.getTo().getWorld()) != universe) // Changing universe
        {
            if (event.getEntity() instanceof Player)
            {
                return;
            }
            if (event.getEntity() instanceof InventoryHolder)
            {
                event.setCancelled(true); // TODO config allow entities with inventory to travel to other universe
            }
            else
            {
                event.setCancelled(true); // TODO config allow entities to travel to other universe
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent event)
    {
        if (this.config.adjustFirstSpawn && !event.getPlayer().hasPlayedBefore())
        {
            Universe universe = this.universes.get(this.config.mainUniverse);
            World world = universe.getMainWorld();
            WorldConfig worldConfig = universe.getWorldConfig(world);
            event.getPlayer().teleport(worldConfig.spawn.spawnLocation.getLocationIn(world));
        }
        this.checkForExpectedWorld(event.getPlayer());
    }


    @EventHandler
    public void onQuit(PlayerQuitEvent event)
    {
        Universe universe = this.getUniverse(event.getPlayer().getWorld());
        universe.savePlayer(event.getPlayer(), event.getPlayer().getWorld());
        this.savePlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOW) // Allow others e.g spawn module to adjust spawn later
    public void onRespawn(PlayerRespawnEvent event)
    {
        if (!event.isBedSpawn())
        {
            World world = event.getPlayer().getWorld();
            Universe universe = this.getUniverse(world);
            event.setRespawnLocation(universe.getRespawnLocation(world));
        }
    }

    private void checkForExpectedWorld(Player player)
    {
        Path path = dirPlayers.resolve(player.getName() + YAML.getExtention());
        PlayerConfig config;
        if (Files.exists(path))
        {
            config = this.module.getCore().getConfigFactory().load(PlayerConfig.class, path.toFile(), false);
            if (config.lastWorld != null)
            {
                Universe universe = this.getUniverse(player.getWorld());
                Universe expected = this.getUniverse(config.lastWorld.getWorld());
                if (universe != expected)
                {
                    // expectedworld-actualworld_playername.yml
                    Path errorFile = dirErrors.resolve(config.lastWorld.getName() + "-" + player.getWorld().getName() + "_" + player.getName() + ".dat");
                    int i = 1;
                    while (Files.exists(errorFile))
                    {
                        errorFile = dirErrors.resolve(config.lastWorld.getName() + "-" + player.getWorld().getName() + "_" + player.getName() + "_" + i++ + ".dat");
                    }
                    this.module.getLog().warn("The Player {} was not in the expected world! Overwritten Inventory is saved under /errors/{}", player.getName(), errorFile.getFileName().toString());
                    PlayerDataConfig pdc = this.module.getCore().getConfigFactory().create(PlayerDataConfig.class);
                    pdc.setHead(new SimpleDateFormat().format(new Date()) + " " +
                                    player.getName() + " did not spawn in " + config.lastWorld.getName() +
                                    " but instead in " + player.getWorld().getName(),
                                "This are the items the player had when spawning. They got overwritten!");
                    pdc.setFile(errorFile.toFile());
                    pdc.applyFromPlayer(player);
                    pdc.save();
                }
                if (config.lastWorld.getWorld() == player.getWorld())
                {
                    return; // everything is ok
                }
                this.module.getLog().debug("{} was not in expected world {} instead of {}",
                                    player.getName(), player.getWorld().getName(), config.lastWorld.getName());
                universe.loadPlayer(player);
                // else save new world (strange that player changed world but nvm
            }
            // else no last-world saved. why does this file exist?
        }
        else
        {
            this.module.getLog().debug("Created PlayerConfig for {}" , player.getName());
            config = this.module.getCore().getConfigFactory().create(PlayerConfig.class);
        }
        config.lastWorld = new ConfigWorld(module.getCore().getWorldManager(), player.getWorld()); // update last world
        config.setFile(path.toFile());
        config.save();
    }

    private void savePlayer(Player player)
    {
        Path path = this.dirPlayers.resolve(player.getName() + YAML.getExtention());
        PlayerConfig config = this.module.getCore().getConfigFactory().load(PlayerConfig.class, path.toFile());
        config.lastWorld = new ConfigWorld(module.getCore().getWorldManager(), player.getWorld());
        config.save();
        this.module.getLog().debug("{} is now in the world: {} ({})", player.getName(), player.getWorld().getName(), this.getUniverse(player.getWorld()).getName());
    }

    private WorldConfig getWorldConfig(World world)
    {
        return this.getUniverse(world).getWorldConfig(world);
    }

    public Permission getUniverseRootPerm()
    {
        return universeRootPerm;
    }

    public Universe getUniverse(World world)
    {
        if (world == null)
        {
            return null;
        }
        Universe universe = this.worlds.get(world);
        if (universe == null)
        {
            HashSet<World> set = new HashSet<>();
            set.add(world);
            String universeName = world.getName();
            if (world.getName().contains("_"))
            {
                universeName = world.getName().substring(0, world.getName().indexOf("_"));
                if (this.universes.containsKey(universeName))
                {
                    module.getLog().info("Added world {} to universe {}", world.getName(), universeName);
                    universe = universes.get(universeName);
                    universe.addWorlds(set);
                    return universe;
                }
            }
            module.getLog().info("Created new universe {} containing the world {}", universeName, world.getName());
            Path dirUniverse = dirUniverses.resolve(universeName);
            try
            {
                Files.createDirectories(dirUniverse);
                universe = Universe.create(module, this, dirUniverse, set);
            }
            catch (IOException e)
            {
                throw new UniverseCreationException(e);
            }
        }
        return universe;
    }

    public World loadWorld(String name)
    {
        Universe universe = this.hasWorld(name);
        return universe.loadWorld(name);
    }

    public Universe hasWorld(String name)
    {
        for (Universe universe : this.universes.values())
        {
            if (universe.hasWorld(name))
            {
                return universe;
            }
        }
        return null;
    }

    public Collection<Universe> getUniverses()
    {
        return this.universes.values();
    }


    public World getMainWorld()
    {
        return mainWorld;
    }
}
