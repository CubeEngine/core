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

import java.io.File;
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

import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.WorldLocation;
import de.cubeisland.engine.core.world.WorldSetSpawnEvent;
import de.cubeisland.engine.worlds.config.WorldConfig;
import de.cubeisland.engine.worlds.player.PlayerConfig;
import de.cubeisland.engine.worlds.player.PlayerDataConfig;

/**
 * Holds multiple parallel universes
 */
public class Multiverse implements Listener
{
    private final Worlds module;

    private World mainWorld;

    private Map<String, Universe> universes = new HashMap<>();
    private Map<World, Universe> worlds = new HashMap<>();

    private File playersDir;

    private Permission universeRootPerm;

    public Multiverse(Worlds module)
    {
        this.module = module;
        this.playersDir = this.module.getFolder().resolve("players").toFile();
        this.playersDir.mkdir();

        this.universeRootPerm = this.module.getBasePermission().createAbstractChild("universe");

        File universesFolder = this.module.getFolder().resolve("universes").toFile();
        if (universesFolder.exists() && universesFolder.list().length != 0)
        {
            for (File universeDir : universesFolder.listFiles())
            {
                if (universeDir.isDirectory())
                {
                    if (this.module.getConfig().mainUniverse == null)
                    {
                        this.module.getConfig().mainUniverse = universeDir.getName();
                        this.module.getConfig().save();
                    }
                    this.universes.put(universeDir.getName(), new Universe(universeDir, this , this.module));
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
                        File unverseDir = new File(universesFolder, entry.getKey());
                        this.universes.put(entry.getKey(), new Universe(this.module, this, unverseDir, foundWorlds));
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
                File universeDir = new File(universesFolder, entry.getKey());
                if (!universeDir.mkdirs())
                {
                    throw new IllegalStateException("Could not create folder for universe!");
                }
                this.universes.put(universeDir.getName(), new Universe(this.module, this, universeDir, entry.getValue()));
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
        if (this.module.getConfig().mainUniverse == null || this.universes.get(this.module.getConfig().mainUniverse) == null)
        {
            Universe universe = this.universes.get("world");
            if (universe == null)
            {
                universe = this.universes.values().iterator().next();
            }
            this.module.getLog().warn("No main universe set. {} is now the main universe!", universe.getName());
            this.module.getConfig().mainUniverse = universe.getName();
        }
        this.mainWorld = this.universes.get(this.module.getConfig().mainUniverse).getMainWorld();

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
        // TODO cancel changing universe if entity has inventory
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
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent event)
    {
        if (this.module.getConfig().adjustFirstSpawn && !event.getPlayer().hasPlayedBefore())
        {
            Universe universe = this.universes.get(this.module.getConfig().mainUniverse);
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
        File file = new File(this.playersDir, player.getName() + ".yml");
        PlayerConfig config;
        if (file.exists())
        {
            config = this.module.getCore().getConfigFactory().load(PlayerConfig.class, file, false);
            if (config.lastWorld != null)
            {
                Universe universe = this.getUniverse(player.getWorld());
                Universe expected = this.getUniverse(config.lastWorld);
                if (universe != expected)
                {
                    File errors = this.module.getFolder().resolve("errors").toFile();
                    errors.mkdir();
                    // expectedworld-actualworld_playername.yml
                    File errorFile = new File(errors, config.lastWorld.getName() + "-" + player.getWorld().getName() + "_" + player.getName()  + ".yml");
                    int i = 1;
                    while (errorFile.exists())
                    {
                        errorFile = new File(errors, config.lastWorld.getName() + "-" + player.getWorld().getName() + "_" + player.getName() + "_" + i++ + ".yml");
                    }
                    this.module.getLog().warn("The Player {} was not in the expected world! Overwritten Inventory is saved under /errors/{}", player.getName(), errorFile.getName());
                    PlayerDataConfig pdc = this.module.getCore().getConfigFactory().create(PlayerDataConfig.class);
                    pdc.setHead(new SimpleDateFormat().format(new Date()) + " " + player
                        .getName() + " did not spawn in " +
                                    config.lastWorld.getName() + " but instead in " + player.getWorld()
                                                                                            .getName(), "This are the items the player had when spawning. They got overwritten!");
                    pdc.setFile(errorFile);
                    pdc.applyFromPlayer(player);
                    pdc.save();
                }
                if (config.lastWorld == player.getWorld())
                {
                    return; // everything is ok
                }
                this.module.getLog().debug("{} was not in expected world but in the same universe {} instead of {}",
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
        config.lastWorld = player.getWorld(); // update last world
        config.setFile(file);
        config.save();
    }

    private void savePlayer(Player player)
    {
        File file = new File(this.playersDir, player.getName() + ".yml");
        PlayerConfig config = this.module.getCore().getConfigFactory().load(PlayerConfig.class, file);
        config.lastWorld = player.getWorld();
        config.save();
        this.module.getLog().debug("Saved last world of {}: {}", player.getName(), player.getWorld().getName());
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
        // TODO handle missing universe
        return this.worlds.get(world);
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
}
