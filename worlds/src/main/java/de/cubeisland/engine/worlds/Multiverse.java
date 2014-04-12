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
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.TravelAgent;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
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
import de.cubeisland.engine.core.util.McUUID;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.WorldLocation;
import de.cubeisland.engine.core.world.ConfigWorld;
import de.cubeisland.engine.core.world.WorldManager;
import de.cubeisland.engine.core.world.WorldSetSpawnEvent;
import de.cubeisland.engine.reflect.Reflector;
import de.cubeisland.engine.worlds.config.WorldConfig;
import de.cubeisland.engine.worlds.config.WorldsConfig;
import de.cubeisland.engine.worlds.player.PlayerConfig;
import de.cubeisland.engine.worlds.player.PlayerDataConfig;

import static de.cubeisland.engine.core.filesystem.FileExtensionFilter.YAML;
import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;
import static de.cubeisland.engine.core.util.formatter.MessageType.NEUTRAL;

/**
 * Holds multiple parallel universes
 */
public class Multiverse implements Listener
{
    private final Worlds module;
    private final WorldManager wm;
    private WorldsConfig config;

    private final Map<String, Universe> universes = new HashMap<>(); // universeName -> Universe
    private final Map<String, Universe> worlds = new HashMap<>(); // worldName -> belonging to Universe

    private final Path dirPlayers;
    private final Path dirUniverses;
    private final Path dirErrors;

    private final Permission universeRootPerm;

    public Multiverse(Worlds module, WorldsConfig config) throws IOException
    {
        this.module = module;
        this.wm = module.getCore().getWorldManager();
        this.config = config;
        this.universeRootPerm = this.module.getBasePermission().childWildcard("universe");

        this.dirPlayers = this.module.getFolder().resolve("players"); // config for last world
        Files.createDirectories(this.dirPlayers);

        this.updateToUUID();

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
            Set<World> missingWorlds = this.wm.getWorlds();
            Map<String, Set<World>> found = new HashMap<>();
            for (Entry<String, Universe> entry : this.universes.entrySet())
            {
                found.put(entry.getKey(), new HashSet<>(entry.getValue().getWorlds()));
                missingWorlds.removeAll(entry.getValue().getWorlds());
            }
            if (!missingWorlds.isEmpty())
            {
                CommandSender sender = this.module.getCore().getCommandManager().getConsoleSender();
                sender.sendTranslated(NEUTRAL, "Discovering unknown worlds...");
                this.searchUniverses(found, missingWorlds, sender);
                sender.sendTranslated(NEUTRAL, "Finishing research...");
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
                    sender.sendTranslated(NEUTRAL, "Found {amount} new worlds in the universe {name#universe}!", foundWorlds.size(), entry.getKey());
                }
            }
        }
        else
        {
            this.module.getLog().info("No previous Universes found! Initializing...");
            CommandSender sender = this.module.getCore().getCommandManager().getConsoleSender();
            sender.sendTranslated(NEUTRAL, "Scraping together Matter...");
            Map<String, Set<World>> found = new HashMap<>();
            this.searchUniverses(found, this.wm.getWorlds(), sender);
            sender.sendTranslated(NEUTRAL, "Finishing research...");
            for (Entry<String, Set<World>> entry : found.entrySet())
            {
                Path universeDir = dirUniverses.resolve(entry.getKey());
                Files.createDirectories(universeDir);
                this.universes.put(entry.getKey(), Universe.create(this.module, this, universeDir, entry.getValue()));
            }
            sender.sendTranslated(NEUTRAL, "Found {amount#universes} universes with {amount#worlds} worlds!", found.size(), this.wm.getWorlds().size());
        }
        for (Universe universe : this.universes.values())
        {
            for (World world : universe.getWorlds())
            {
                this.worlds.put(world.getName(), universe);
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
        this.module.getCore().getEventManager().registerListener(this.module, this);
    }

    private void updateToUUID()
    {
        try
        {
            Map<String,Path> playerNames = new HashMap<>();
            for (Path path : Files.newDirectoryStream(this.dirPlayers, YAML))
            {
                String name = StringUtils.stripFileExtension(path.getFileName().toString());
                if (!McUUID.UUID_PATTERN.matcher(name).find())
                {
                    playerNames.put(name, path);
                }
            }
            if (playerNames.isEmpty())
            {
                return;
            }
            this.module.getLog().info("Converting {} PlayerConfigs...", playerNames.size());
            Map<String,UUID> uuids = McUUID.getUUIDForNames(playerNames.keySet());
            Reflector reflector = this.module.getCore().getConfigFactory();
            for (Entry<String, UUID> entry : uuids.entrySet())
            {
                Path oldPath = playerNames.get(entry.getKey());
                PlayerConfig load = reflector.load(PlayerConfig.class, oldPath.toFile(), false);
                load.setFile(this.dirPlayers.resolve(entry.getValue().toString() + YAML.getExtention()).toFile());
                load.lastName = entry.getKey();
                load.save();
                Files.delete(oldPath);
            }
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e); // TODO better exception
        }
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
                sender.sendTranslated(NEUTRAL, "Discovered a new Universe! Heating up stars...");
                worlds = new HashSet<>();
                found.put(universeName, worlds);
            }
            worlds.add(world);
            switch (world.getEnvironment())
            {
            case NORMAL:
                sender.sendTranslated(NEUTRAL, "{world} gets formed by crushing rocks together in the universe {name#universe}", world, universeName);
                break;
            case NETHER:
                sender.sendTranslated(NEUTRAL, "Cooling plasma a bit to make {world} in the universe {name#universe}", world, universeName);
                break;
            case THE_END:
                sender.sendTranslated(NEUTRAL, "Found a cold rock named {world} in the universe {name#universe}", world, universeName);
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
            Universe oldUniverse = this.getUniverseFrom(event.getFrom());
            Universe newUniverse = this.getUniverseFrom(event.getPlayer().getWorld());
            if (oldUniverse != newUniverse)
            {
                event.getPlayer().closeInventory();
                oldUniverse.savePlayer(event.getPlayer(), event.getFrom());
                newUniverse.loadPlayer(event.getPlayer());
            }
            // TODO else need to change gamemode?
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
            this.module.getLog().warn("The Player {} teleported into a universe that couldn't get created! " +
                                          "The overwritten Inventory is saved under /errors/{}", event.getPlayer().getName(), errorFile.getFileName().toString());
            PlayerDataConfig pdc = this.module.getCore().getConfigFactory().create(PlayerDataConfig.class);
            pdc.setHead(new SimpleDateFormat().format(new Date()) + " " +
                            event.getPlayer().getDisplayName() + "(" + event.getPlayer().getUniqueId() + ") ported to " + event.getPlayer().getWorld().getName() +
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
        Universe universe = this.getUniverseFrom(to.getWorld());
        if (!universe.checkPlayerAccess(event.getPlayer(), to.getWorld()))
        {
            event.setCancelled(true); // TODO check if player has access to the world he is currently in
            User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getUniqueId());
            user.sendTranslated(NEGATIVE, "You are not allowed to enter the universe {name#universe}!", universe.getName());
        }
    }

    @EventHandler
    public void onPortalUse(PlayerPortalEvent event)
    {
        World world = event.getPlayer().getWorld();
        Universe universe = this.getUniverseFrom(world);
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
        Universe fromUniverse = this.getUniverseFrom(world);
        TravelAgent agent = event.getPortalTravelAgent();
        if (event.getTo() == null)
        {
            return;
        }
        switch (event.getTo().getWorld().getEnvironment())
        {
        case NETHER:
            if (fromUniverse.hasNetherTarget(world))
            {
                event.setTo(fromUniverse.handleNetherTarget(event.getFrom(), agent));
                event.useTravelAgent(true);
            }
            break;
        case THE_END:
            if (fromUniverse.hasEndTarget(world))
            {
                event.setTo(fromUniverse.handleEndTarget(event.getEntity().getLocation()));
                event.useTravelAgent(true);
            }
            break;
        case NORMAL:
            if (event.getFrom().getWorld().getEnvironment() == Environment.THE_END)
            {
                event.setTo(fromUniverse.handleEndTarget(event.getFrom()));
            }
            else
            {
                event.setTo(fromUniverse.handleNetherTarget(event.getFrom(), event.getPortalTravelAgent()));
                event.useTravelAgent(true);
            }
        }
        if (this.getUniverseFrom(event.getTo().getWorld()) != fromUniverse) // Changing universe
        {
            if (event.getEntity() instanceof Player)
            {
                return;
            }
            Universe toUniverse = this.getUniverseFrom(event.getTo().getWorld());
            if (fromUniverse.getConfig().entityTp.enable && toUniverse.getConfig().entityTp.enable)
            {
                if (event.getEntity() instanceof InventoryHolder)
                {
                    if (fromUniverse.getConfig().entityTp.inventory && toUniverse.getConfig().entityTp.inventory)
                    {
                        return;
                    }
                    else
                    {
                        event.setCancelled(true);
                    }
                }
            }
            else
            {
                event.setCancelled(true);
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
        Universe universe = this.getUniverseFrom(event.getPlayer().getWorld());
        universe.savePlayer(event.getPlayer(), event.getPlayer().getWorld());
        this.savePlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOW) // Allow others e.g spawn module to adjust spawn later
    public void onRespawn(PlayerRespawnEvent event)
    {
        if (!event.isBedSpawn())
        {
            World world = event.getRespawnLocation().getWorld();
            event.setRespawnLocation(this.getUniverseFrom(world).getRespawnLocation(world));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBedLeave(final PlayerBedLeaveEvent event)
    {
        // TODO #waitingForBukkit for a better solution https://bukkit.atlassian.net/browse/BUKKIT-1916
        if (!this.getUniverseFrom(event.getBed().getWorld()).getWorldConfig(event.getBed().getWorld()).spawn.allowBedRespawn)
        {
            // Wait until spawn is set & reset it
            final Location spawnLocation = event.getPlayer().getBedSpawnLocation();
            this.module.getCore().getTaskManager().runTaskDelayed(module, new Runnable()
            {
                @Override
                public void run()
                {
                      event.getPlayer().setBedSpawnLocation(spawnLocation, true);
                }
            }, 1);
        }
    }

    private void checkForExpectedWorld(Player player)
    {
        Path path = dirPlayers.resolve(player.getUniqueId() + YAML.getExtention());
        PlayerConfig config;
        if (Files.exists(path))
        {
            config = this.module.getCore().getConfigFactory().load(PlayerConfig.class, path.toFile(), false);
            if (config.lastWorld != null)
            {
                Universe universe = this.getUniverseFrom(player.getWorld());
                Universe expected = this.getUniverseFrom(config.lastWorld.getWorld());
                if (universe != expected)
                {
                    // expectedworld-actualworld_playername.yml
                    Path errorFile = dirErrors.resolve(player.getWorld().getName() + "_" + player.getName() + ".dat");
                    int i = 1;
                    while (Files.exists(errorFile))
                    {
                        errorFile = dirErrors.resolve(player.getWorld().getName() + "_" + player.getName() + "_" + i++ + ".dat");
                    }
                    this.module.getLog().warn("The Player {} was not in the expected world! Overwritten Inventory is saved under /errors/{}", player.getName(), errorFile.getFileName().toString());
                    PlayerDataConfig pdc = this.module.getCore().getConfigFactory().create(PlayerDataConfig.class);
                    pdc.setHead(new SimpleDateFormat().format(new Date()) + " " +
                                    player.getDisplayName() + "(" + player.getUniqueId() + ") did not spawn in " + config.lastWorld.getName() +
                                    " but instead in " + player.getWorld().getName(),
                                "These are the items the player had when spawning. They were overwritten!");
                    pdc.setFile(errorFile.toFile());
                    pdc.applyFromPlayer(player);
                    pdc.save();
                }
                if (config.lastWorld.getWorld() == player.getWorld())
                {
                    return; // everything is ok
                }
                this.module.getLog().debug("{} was not in expected world {} instead of {}",
                                    player.getDisplayName(), player.getWorld().getName(), config.lastWorld.getName());
                universe.loadPlayer(player);
                // else save new world (strange that player changed world but nvm
            }
            // else no last-world saved. why does this file exist?
        }
        else
        {
            this.module.getLog().debug("Created PlayerConfig for {}" , player.getDisplayName());
            config = this.module.getCore().getConfigFactory().create(PlayerConfig.class);
        }
        config.lastName = player.getName();
        config.lastWorld = new ConfigWorld(this.wm, player.getWorld()); // update last world
        config.setFile(path.toFile());
        config.save();
    }

    private void savePlayer(Player player)
    {
        Path path = this.dirPlayers.resolve(player.getUniqueId() + YAML.getExtention());
        PlayerConfig config = this.module.getCore().getConfigFactory().load(PlayerConfig.class, path.toFile());
        config.lastWorld = new ConfigWorld(this.wm, player.getWorld());
        config.save();
        this.module.getLog().debug("{} is now in the world: {} ({})", player.getDisplayName(), player.getWorld().getName(), this.getUniverseFrom(player
                                                                                                                                              .getWorld()).getName());
    }

    private WorldConfig getWorldConfig(World world)
    {
        return this.getUniverseFrom(world).getWorldConfig(world);
    }


    public WorldConfig getWorldConfig(String name)
    {
        for (Universe universe : this.universes.values())
        {
            if (universe.hasWorld(name))
            {
                return universe.getWorldConfig(name);
            }
        }
        return null;
    }

    public Permission getUniverseRootPerm()
    {
        return universeRootPerm;
    }

    public Universe getUniverseFrom(World world)
    {
        if (world == null)
        {
            return null;
        }
        Universe universe = this.worlds.get(world.getName());
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

    public Universe createUniverse(String name)
    {
        Universe universe = universes.get(name);
        if (universe == null)
        {
            try
            {
                return Universe.create(module, this, dirUniverses.resolve(name), new HashSet<World>());
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
        return this.getMainUniverse().getMainWorld();
    }

    public Universe getUniverse(String name)
    {
        return this.universes.get(name);
    }

    public Universe getMainUniverse()
    {
        return this.universes.get(this.config.mainUniverse);
    }
}
