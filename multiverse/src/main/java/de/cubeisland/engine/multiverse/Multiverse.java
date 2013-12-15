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
package de.cubeisland.engine.multiverse;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.TravelAgent;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;

import de.cubeisland.engine.configuration.codec.ConverterManager;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.config.codec.NBTCodec;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.world.WorldSetSpawnEvent;
import de.cubeisland.engine.multiverse.config.MultiverseConfig;
import de.cubeisland.engine.multiverse.config.WorldConfig;
import de.cubeisland.engine.multiverse.config.WorldLocation;
import de.cubeisland.engine.multiverse.converter.DiffcultyConverter;
import de.cubeisland.engine.multiverse.converter.EnvironmentConverter;
import de.cubeisland.engine.multiverse.converter.GameModeConverter;
import de.cubeisland.engine.multiverse.converter.InventoryConverter;
import de.cubeisland.engine.multiverse.converter.PotionEffectConverter;
import de.cubeisland.engine.multiverse.converter.WorldLocationConverter;
import de.cubeisland.engine.multiverse.converter.WorldTypeConverter;
import de.cubeisland.engine.multiverse.player.PlayerConfig;
import de.cubeisland.engine.multiverse.player.PlayerDataConfig;

public class Multiverse extends Module implements Listener
{
    private MultiverseConfig config;
    private World mainWorld;

    private Map<String, Universe> universes = new HashMap<>();
    private Map<World, Universe> worlds = new HashMap<>();

    private File playersDir;

    private Permission universeRootPerm;

    @Override
    public void onLoad()
    {
        ConverterManager manager = this.getCore().getConfigFactory().getDefaultConverterManager();
        manager.registerConverter(Difficulty.class, new DiffcultyConverter());
        manager.registerConverter(Environment.class, new EnvironmentConverter());
        manager.registerConverter(GameMode.class, new GameModeConverter());
        manager.registerConverter(WorldType.class, new WorldTypeConverter());
        manager.registerConverter(WorldLocation.class, new WorldLocationConverter());
///*TODO remove
        manager.registerConverter(Inventory.class, new InventoryConverter(Bukkit.getServer()));
        manager.registerConverter(PotionEffect.class, new PotionEffectConverter());

//*/
        NBTCodec codec = this.getCore().getConfigFactory().getCodecManager().getCodec(NBTCodec.class);
        manager = codec.getConverterManager();
        manager.registerConverter(Inventory.class, new InventoryConverter(Bukkit.getServer()));
        manager.registerConverter(PotionEffect.class, new PotionEffectConverter());
    }

    @Override
    public void onEnable()
    {
        this.config = this.loadConfig(MultiverseConfig.class);
        this.playersDir = this.getFolder().resolve("players").toFile();
        this.playersDir.mkdir();

        this.universeRootPerm = this.getBasePermission().createAbstractChild("universe");

        new MultiversePermissions(this);

        File universesFolder = this.getFolder().resolve("universes").toFile();
        if (universesFolder.exists() && universesFolder.list().length != 0)
        {
            for (File universeDir : universesFolder.listFiles())
            {
                if (universeDir.isDirectory())
                {
                    if (this.config.mainUniverse == null)
                    {
                        this.config.mainUniverse = universeDir.getName();
                        this.config.save();
                    }
                    this.universes.put(universeDir.getName(), new Universe(universeDir, this));
                }
            }
            Set<World> missingWorlds = this.getCore().getWorldManager().getWorlds();
            Map<String, Set<World>> found = new HashMap<>();
            for (Entry<String, Universe> entry : this.universes.entrySet())
            {
                found.put(entry.getKey(), new HashSet<>(entry.getValue().getWorlds()));
                missingWorlds.removeAll(entry.getValue().getWorlds());
            }
            if (!missingWorlds.isEmpty())
            {
                CommandSender sender = this.getCore().getCommandManager().getConsoleSender();
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
                        this.universes.put(entry.getKey(), new Universe(unverseDir, this, foundWorlds));
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
            this.getLog().info("No previous Universes found! Initializing...");
            CommandSender sender = this.getCore().getCommandManager().getConsoleSender();
            sender.sendTranslated("&6Scraping together Matter...");
            Map<String, Set<World>> found = new HashMap<>();
            this.searchUniverses(found, this.getCore().getWorldManager().getWorlds(), sender);
            sender.sendTranslated("&eFinishing research...");
            for (Entry<String, Set<World>> entry : found.entrySet())
            {
                File universeDir = new File(universesFolder, entry.getKey());
                if (!universeDir.mkdirs())
                {
                    throw new IllegalStateException("Could not create folder for universe!");
                }
                this.universes.put(universeDir.getName(), new Universe(universeDir, this, entry.getValue()));
            }
            sender.sendTranslated("&eFound &6%d&e universes with &6%d&e worlds!", found.size(), this.getCore().getWorldManager().getWorlds().size());
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
            this.getLog().warn("No main universe set. {} is now the main universe!", universe.getName());
            this.config.mainUniverse = universe.getName();
        }
        this.mainWorld = this.universes.get(this.config.mainUniverse).getMainWorld();
        this.getCore().getEventManager().registerListener(this, this);
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
    public void onChat(AsyncPlayerChatEvent event)
    {
        if (event.getMessage().equals("load"))
        {

            Universe universe = this.getUniverse(event.getPlayer().getWorld());
            if (universe != null)
            {
                // TODO handle missing universe for world
                universe.loadPlayer(event.getPlayer());
            }

        }
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
            User user = this.getCore().getUserManager().getExactUser(event.getPlayer().getName());
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
                    System.out.print(agent.getSearchRadius() + " <-S:PRE:C->"+ agent.getCreationRadius());
                    event.setTo(universe.handleNetherTarget(event.getPlayer().getLocation(), agent));
                    System.out.print(agent.getSearchRadius() + " <-S:N:C->"+ agent.getCreationRadius());
                    event.useTravelAgent(true);
                }
                break;
            case END_PORTAL:
                if (universe.hasEndTarget(world))
                {
                    event.setTo(universe.handleEndTarget(event.getPlayer().getLocation(), agent));
                    event.useTravelAgent(true);
                }
                break;
        }
    }

    @EventHandler
    public void onEntityPortal(EntityPortalEvent event)
    {
        System.out.print("Before: " + event.getTo() + " " + event.getEntityType().name());
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
        case NORMAL:
            if (universe.hasNetherTarget(world))
            {
                System.out.print(agent.getSearchRadius() + " <-S:A-PRE:C->"+ agent.getCreationRadius());
                event.setTo(universe.handleNetherTarget(event.getEntity().getLocation(), agent));
                System.out.print(agent.getSearchRadius() + " <-S:A-N:C->"+ agent.getCreationRadius());
                System.out.print("After" + event.getTo());
                event.useTravelAgent(true);
            }
            break;
        case THE_END:
            if (universe.hasEndTarget(world))
            {
                event.setTo(universe.handleEndTarget(event.getEntity().getLocation(), agent));
                event.useTravelAgent(true);
            }
            break;
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

    private void checkForExpectedWorld(Player player)
    {
        File file = new File(this.playersDir, player.getName() + ".yml");
        PlayerConfig config;
        if (file.exists())
        {
            config = this.getCore().getConfigFactory().load(PlayerConfig.class, file, false);
            if (config.lastWorld != null)
            {
                Universe universe = this.getUniverse(player.getWorld());
                Universe expected = this.getUniverse(config.lastWorld);
                if (universe != expected)
                {
                    File errors = this.getFolder().resolve("errors").toFile();
                    errors.mkdir();
                    // expectedworld-actualworld_playername.yml
                    File errorFile = new File(errors, config.lastWorld.getName() + "-" + player.getWorld().getName() + "_" + player.getName()  + ".yml");
                    int i = 1;
                    while (errorFile.exists())
                    {
                        errorFile = new File(errors, config.lastWorld.getName() + "-" + player.getWorld().getName() + "_" + player.getName() + "_" + i++ + ".yml");
                    }
                    this.getLog().warn("The Player {} was not in the expected world! Overwritten Inventory is saved under /errors/{}", player.getName(), errorFile.getName());
                    PlayerDataConfig pdc = this.getCore().getConfigFactory().create(PlayerDataConfig.class);
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
                this.getLog().debug("{} was not in expected world but in the same universe {} instead of {}",
                                    player.getName(), player.getWorld().getName(), config.lastWorld.getName());
                universe.loadPlayer(player);
                // else save new world (strange that player changed world but nvm
            }
            // else no last-world saved. why does this file exist?
        }
        else
        {
            this.getLog().debug("Created PlayerConfig for {}" , player.getName());
            config = this.getCore().getConfigFactory().create(PlayerConfig.class);
        }
        config.lastWorld = player.getWorld(); // update last world
        config.setFile(file);
        config.save();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event)
    {
        Universe universe = this.getUniverse(event.getPlayer().getWorld());
        universe.savePlayer(event.getPlayer(), event.getPlayer().getWorld());
        this.savePlayer(event.getPlayer());
    }

    private void savePlayer(Player player)
    {
        File file = new File(this.playersDir, player.getName() + ".yml");
        PlayerConfig config = this.getCore().getConfigFactory().load(PlayerConfig.class, file);
        config.lastWorld = player.getWorld();
        config.save();
        this.getLog().debug("Saved last world of {}: {}", player.getName(), player.getWorld().getName());
    }

    private WorldConfig getWorldConfig(World world)
    {
        Universe universe = this.getUniverse(world);
        if (universe == null)
        {
            // TODO
        }
        return universe.getWorldConfig(world);
    }

    public Permission getUniverseRootPerm()
    {
        return universeRootPerm;
    }
    
    public Universe getUniverse(World world)
    {
        return this.worlds.get(world);
    }
}
