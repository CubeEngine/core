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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.TravelAgent;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.util.McUUID;
import de.cubeisland.engine.core.util.Pair;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.WorldLocation;
import de.cubeisland.engine.core.world.ConfigWorld;
import de.cubeisland.engine.core.world.WorldManager;
import de.cubeisland.engine.reflect.Reflector;
import de.cubeisland.engine.reflect.codec.YamlCodec;
import de.cubeisland.engine.worlds.config.UniverseConfig;
import de.cubeisland.engine.worlds.config.WorldConfig;
import de.cubeisland.engine.worlds.player.PlayerDataConfig;

import static de.cubeisland.engine.core.filesystem.FileExtensionFilter.DAT;
import static de.cubeisland.engine.core.filesystem.FileExtensionFilter.YAML;

/**
 * Represents multiple worlds in a universe
 */
public class Universe
{
    private final Worlds module;
    private final Multiverse multiverse;
    private final WorldManager wm;

    private UniverseConfig universeConfig;
    private WorldConfig defaultConfig = null;

    private final Map<String, WorldConfig> worldConfigs = new HashMap<>();
    private final Set<World> worlds = new HashSet<>();
    private final Map<String, Permission> worldPerms = new HashMap<>();

    private Permission universeAccessPerm;

    private final Path dirUniverse;
    private final Path dirPlayers;
    private final Path fileUniverse;
    private final Path fileDefaults;

    private Universe(Worlds module, Multiverse multiverse, Path dirUniverse) throws IOException
    {
        this.module = module;
        this.wm = module.getCore().getWorldManager();
        this.multiverse = multiverse;

        this.dirUniverse = dirUniverse;
        this.dirPlayers = dirUniverse.resolve("players");
        Files.createDirectories(dirPlayers);

        this.updateToUUID();

        this.fileDefaults = dirUniverse.resolve("default.yml");
        this.fileUniverse =  dirUniverse.resolve("config.yml");
    }

    private void updateToUUID()
    {
        try
        {
            Map<String,Path> playerNames = new HashMap<>();
            for (Path path : Files.newDirectoryStream(this.dirPlayers, DAT))
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
            this.module.getLog().info("Converting {} PlayerDataConfig for {}", playerNames.size(), this.getName());
            Map<String,UUID> uuids = McUUID.getUUIDForNames(playerNames.keySet());
            Reflector reflector = this.module.getCore().getConfigFactory();
            for (Entry<String, UUID> entry : uuids.entrySet())
            {
                if (entry.getValue() != null)
                {
                    Path oldPath = playerNames.get(entry.getKey());
                    PlayerDataConfig load = reflector.load(PlayerDataConfig.class, oldPath.toFile(), false);
                    load.setFile(this.dirPlayers.resolve(entry.getValue().toString() + DAT.getExtention()).toFile());
                    load.lastName = entry.getKey();
                    load.save();
                    Files.delete(oldPath);
                }
            }
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e); // TODO better exception
        }
    }

    public static Universe load(Worlds module, Multiverse multiverse, Path dirUniverse) throws IOException
    {
        Universe universe = new Universe(module, multiverse, dirUniverse);
        universe.reload();
        return universe;
    }

    public void reload() throws IOException
    {
        this.defaultConfig = module.getCore().getConfigFactory().load(WorldConfig.class, this.fileDefaults.toFile(), true);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(this.dirUniverse, YAML))
        {
            for (Path path : stream)
            {
                if (!(path.equals(fileDefaults) || path.equals(fileUniverse)))
                {
                    WorldConfig config = this.defaultConfig.loadChild(path.toFile());
                    if (config.autoLoad)
                    {
                        this.loadOrCreateWorld(config, StringUtils.stripFileExtension(path.getFileName().toString()));
                    }
                    else
                    {
                        this.worldConfigs.put(StringUtils.stripFileExtension(path.getFileName().toString()), config);
                    }
                }
            }
        }
        this.universeConfig = this.module.getCore().getConfigFactory().load(UniverseConfig.class, fileUniverse.toFile());
        if (this.worlds.isEmpty())
        {
            module.getLog().warn("The universe {} has no worlds!", this.getName());
        }
        else
        {
            if (this.universeConfig.mainWorld == null)
            {
                World world = this.worlds.iterator().next();
                module.getLog().warn("The universe {} had no main world! {} is now the main world", dirUniverse.getFileName().toString(), world.getName());
                this.universeConfig.mainWorld = new ConfigWorld(this.wm, world);
                this.universeConfig.save();
            }
            for (WorldConfig worldConfig : this.worldConfigs.values())
            {
                if (worldConfig.spawn.respawnWorld == null)
                {
                    worldConfig.spawn.respawnWorld = new ConfigWorld(this.wm, this.universeConfig.mainWorld.getName());
                    worldConfig.save();
                }
            }
            World mainWorld = this.universeConfig.mainWorld.getWorld();
            if (mainWorld == null)
            {
                if (this.worlds.isEmpty())
                {
                    module.getLog().warn("Unknown world set as the main world! Universe has no active worlds!");
                    this.universeConfig.mainWorld = null;
                }
                else
                {
                    mainWorld = this.worlds.iterator().next();
                    module.getLog().warn("Unknown world set as the main world! Mainworld {} replaced with {}!", this.universeConfig.mainWorld, mainWorld
                        .getName());
                    this.universeConfig.mainWorld = new ConfigWorld(this.wm, mainWorld);
                }
                this.universeConfig.save();
            }
        }
        this.generatePermissions();
    }

    public static Universe create(Worlds module, Multiverse multiverse, Path dirUniverse, Set<World> worlds) throws IOException
    {
        Universe universe = new Universe(module, multiverse, dirUniverse);
        universe.create(worlds);
        return universe;
    }

    private void create(Set<World> worlds)
    {
        this.universeConfig = this.module.getCore().getConfigFactory().create(UniverseConfig.class);
        this.universeConfig.setFile(this.fileUniverse.toFile());

        for (World world : worlds)
        {
            if (world.getName().equals(this.getName()))
            {
                this.universeConfig.mainWorld = new ConfigWorld(this.wm, world.getName());
                this.universeConfig.save();

                this.defaultConfig = this.createWorldConfigFromExisting(world);
                this.defaultConfig.spawn.spawnLocation = null;
                this.defaultConfig.generation.worldType = null;
                this.defaultConfig.generation.seed = null;
                this.defaultConfig.spawn.respawnWorld = new ConfigWorld(this.wm, this.universeConfig.mainWorld.getName());
                this.defaultConfig.setFile(fileDefaults.toFile());

                this.defaultConfig.save();
            }
            this.worldConfigs.put(world.getName(), this.createWorldConfigFromExisting(world));
        }
        for (Entry<String, WorldConfig> entry : worldConfigs.entrySet())
        {
            WorldConfig worldConfig = entry.getValue();
            worldConfig.spawn.respawnWorld = this.universeConfig.mainWorld;

            worldConfig.setDefault(this.defaultConfig);
            worldConfig.setFile(dirUniverse.resolve(entry.getKey() + YAML.getExtention()).toFile());
            worldConfig.updateInheritance();
            worldConfig.save();
        }
        this.generatePermissions();
    }

    private World loadOrCreateWorld(WorldConfig config, String name)
    {
        World world = this.wm.getWorld(name);
        for (Universe universe : this.multiverse.getUniverses())
        {
            if (universe == this)
            {
                continue;
            }
            if (universe.hasWorld(name))
            {
                module.getLog().warn("The world {} is already part of an other universe {} and cannot be added to the universe {}!",
                                     name, universe.getName(), this.getName());
                module.getLog().warn("Please check your configuration!");
                return null;
            }
        }
        if (world == null) // world loaded?
        {
            if (config.generation.environment == null)
            {
                config.generation.environment = Environment.NORMAL;
                module.getLog().warn("Environment for {} in {} was not set and defaulted to NORMAL!", name, this.getName());
                config.save();
            }
            if (config.generation.seed == null)
            {
                config.generation.seed = StringUtils.randomString(new Random(), 16, "qwertzuiopasdfghhjjklyxcvbnmQWERTZUIOPASDFGHJKLYXCVBNM12345677890");
                module.getLog().warn("{} in {} had no seed, so a random seed was created!", name, this.getName());
                config.save();
            }
            if (Files.exists(Bukkit.getServer().getWorldContainer().toPath().resolve(name))) // world is just not loaded yet
            {
                module.getLog().info("Loading World {}...", name);
                world = this.wm.createWorld(WorldCreator.name(name));
            }
            else // World does not exist
            {
                module.getLog().info("Creating new World {}...", name);
                world = this.wm.createWorld(config.applyToCreator(WorldCreator.name(name)));
            }
            if (config.spawn.spawnLocation == null)
            {
                config.spawn.spawnLocation = new WorldLocation(world.getSpawnLocation());
            }
            config.updateInheritance();
            config.save();
        }
        else
        {
            module.getLog().info("{} is already loaded!", name);
            config.applyGenerationFromWorld(world); // not really needed but so were sure the data is correct
            config.updateInheritance();
            config.save();
        }
        config.applyToWorld(world); // apply configured
        this.worlds.add(world);
        this.worldConfigs.put(world.getName(), config);
        return world;
    }

    private void generatePermissions()
    {
        if (!this.universeConfig.freeAccess)
        {
            this.universeAccessPerm = this.multiverse.getUniverseRootPerm().childWildcard("access").child(this.getName());
            this.module.getCore().getPermissionManager().registerPermission(module, this.universeAccessPerm);
        }
        Permission worldAccess = this.multiverse.getUniverseRootPerm().childWildcard("world-access");
        for (Entry<String, WorldConfig> entry : this.worldConfigs.entrySet())
        {
            if (!entry.getValue().access.free)
            {
                Permission perm = worldAccess.child(entry.getKey());
                this.module.getCore().getPermissionManager().registerPermission(module, perm);
                this.worldPerms.put(entry.getKey(), perm);
            }

        }
    }

    private WorldConfig createWorldConfigFromExisting(World world)
    {
        WorldConfig config = module.getCore().getConfigFactory().create(WorldConfig.class);
        if (world.getEnvironment() == Environment.NETHER)
        {
            config.scale = 8.0; // Nether is 1:8
        }
        if (this.defaultConfig != null && this.universeConfig.mainWorld.getName().equals(world.getName()))
        {
            config.spawn.keepSpawnInMemory = true; // KEEP MAIN SPAWN LOADED
        }
        config.applyFromWorld(world);
        return config;
    }

    public String getName()
    {
        return this.dirUniverse.getFileName().toString();
    }

    public Set<World> getWorlds()
    {
        return this.worlds;
    }

    public void addWorlds(Set<World> worlds)
    {
        for (World world : worlds)
        {
            WorldConfig config = this.createWorldConfigFromExisting(world);
            config.setDefault(this.defaultConfig);
            config.setFile(dirUniverse.resolve(world.getName() + YAML.getExtention()).toFile());
            config.updateInheritance();
            config.save();
            this.worldConfigs.put(world.getName(), config);
            this.worlds.add(world);
        }
    }

    public WorldConfig getWorldConfig(World world)
    {
        return this.worldConfigs.get(world.getName());
    }

    public void savePlayer(Player player, World world)
    {
        PlayerDataConfig config = this.module.getCore().getConfigFactory().create(PlayerDataConfig.class);
        config.applyFromPlayer(player);

        config.setFile(dirPlayers.resolve(player.getUniqueId() + DAT.getExtention()).toFile());
        config.save();

        // TODO remove yaml saving
        YamlCodec codec = this.module.getCore().getConfigFactory().getCodecManager().getCodec(YamlCodec.class);
        try
        {
            codec.saveReflected(config, new FileOutputStream(dirPlayers.resolve(player.getUniqueId() + YAML.getExtention()).toFile()));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        this.module.getLog().debug("PlayerData for {} in {} ({}) saved", player.getDisplayName(), world.getName(), this.getName());
    }

    public void loadPlayer(Player player)
    {
        Path path = dirPlayers.resolve(player.getUniqueId() + DAT.getExtention());
        if (Files.exists(path))
        {
            PlayerDataConfig load = this.module.getCore().getConfigFactory().load(PlayerDataConfig.class, path.toFile());
            load.applyToPlayer(player);
        }
        else
        {
            this.module.getLog().debug("Created PlayerDataConfig for {} in the {} universe" , player.getDisplayName(), this.getName());
            PlayerDataConfig save = this.module.getCore().getConfigFactory().create(PlayerDataConfig.class);
            save.applyToPlayer(player);
            this.savePlayer(player, player.getWorld());
        }
        if (!(this.universeConfig.keepFlyMode || module.perms().KEEP_FLYMODE.isAuthorized(player)))
        {
            player.setFlying(player.isFlying());
        }
        if (!module.perms().KEEP_GAMEMODE.isAuthorized(player))
        {
            if (this.universeConfig.enforceGameMode != null)
            {
                player.setGameMode(this.universeConfig.enforceGameMode);
            }
            else if (!(this.universeConfig.keepGameMode))
            {
                player.setGameMode(this.worldConfigs.get(player.getWorld().getName()).gameMode);
            }
        }
        this.module.getLog().debug("PlayerData for {} in {} ({}) applied", player.getDisplayName(), player.getWorld().getName(), this.getName());
    }

    public World getMainWorld()
    {
        return this.universeConfig.mainWorld.getWorld();
    }

    public boolean checkPlayerAccess(Player player, World world)
    {
        if (this.universeConfig.freeAccess || this.universeAccessPerm.isAuthorized(player))
        {
            Permission permission = this.worldPerms.get(world.getName());
            if (permission == null || permission.isAuthorized(player))
            {
                return true;
            }
        }
        return false;
    }

    public Location handleNetherTarget(Location from, TravelAgent agent)
    {
        WorldConfig fromConfig = this.worldConfigs.get(from.getWorld().getName());
        World toWorld = this.wm.getWorld(fromConfig.netherTarget);
        WorldConfig toConfig = this.multiverse.getUniverseFrom(toWorld).getWorldConfig(toWorld);
        double factor = fromConfig.scale / toConfig.scale;
        agent.setSearchRadius((int)(128 / (factor * 8)));
        agent.setCreationRadius((int)(16 / (factor * 8)));
        return new Location(toWorld, from.getX() * factor, from.getY(), from.getZ() * factor, from.getYaw(), from.getPitch());
    }

    public Location handleEndTarget(Location from)
    {
        WorldConfig fromConfig = this.worldConfigs.get(from.getWorld().getName());
        World toWorld = this.wm.getWorld(fromConfig.endTarget);
        if (toWorld.getEnvironment() == Environment.THE_END)
        {
            return new Location(toWorld, 100, 50, 0, from.getYaw(), from.getPitch()); // everything else wont work when using the TravelAgent
        }
        Location spawnLocation = toWorld.getSpawnLocation();
        spawnLocation.setYaw(from.getYaw());
        spawnLocation.setPitch(from.getPitch());
        return spawnLocation;
    }

    public boolean hasNetherTarget(World world)
    {
        WorldConfig worldConfig = this.worldConfigs.get(world.getName());
        return worldConfig.netherTarget != null &&
            this.wm.getWorld(worldConfig.netherTarget) != null;
    }

    public boolean hasEndTarget(World world)
    {
        WorldConfig worldConfig = this.worldConfigs.get(world.getName());
        if (worldConfig.endTarget != null)
        {
            World target = this.wm.getWorld(worldConfig.endTarget);
            if (target != null)
            {
                if (world.getEnvironment() != Environment.THE_END)
                {
                    if (target.getEnvironment() == Environment.THE_END)
                    {
                        return true;
                    }
                    this.module.getLog().warn("End target {} coming from {} is not a End world!", target.getName(), world.getName());
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public Location getRespawnLocation(World world)
    {
        WorldConfig worldConfig = this.getWorldConfig(world);
        if (worldConfig.spawn.respawnWorld == null)
        {
            return this.getSpawnLocation(this.getMainWorld());
        }
        World respawnWorld = worldConfig.spawn.respawnWorld.getWorld();
        if (respawnWorld == null)
        {
            this.module.getLog().warn("Unknown respawn world for {}", world.getName());
            return this.getSpawnLocation(world);
        }
        return this.multiverse.getUniverseFrom(respawnWorld).getSpawnLocation(respawnWorld);
    }

    public Location getSpawnLocation(World world)
    {
        return this.getWorldConfig(world).spawn.spawnLocation.getLocationIn(world);
    }

    public boolean hasWorld(String name)
    {
        return this.worldConfigs.containsKey(name);
    }

    public World loadWorld(String name)
    {
        return this.loadOrCreateWorld(this.worldConfigs.get(name), name);
    }

    public List<Pair<String, WorldConfig>> getAllWorlds()
    {
        ArrayList<Pair<String, WorldConfig>> list = new ArrayList<>();
        for (Entry<String, WorldConfig> entry : this.worldConfigs.entrySet())
        {
            list.add(new Pair<>(entry.getKey(), entry.getValue()));
        }
        return list;
    }

    public void removeWorld(String name)
    {
        WorldConfig config = this.worldConfigs.remove(name);
        config.getFile().delete();
    }

    public Path getDirectory()
    {
        return this.dirUniverse;
    }

    public WorldConfig getWorldConfig(String name)
    {
        return this.worldConfigs.get(name);
    }

    public UniverseConfig getConfig()
    {
        return this.universeConfig;
    }
}
