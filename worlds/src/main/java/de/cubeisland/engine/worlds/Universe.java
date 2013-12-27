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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.TravelAgent;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import de.cubeisland.engine.configuration.codec.YamlCodec;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.util.Pair;
import de.cubeisland.engine.core.util.WorldLocation;
import de.cubeisland.engine.worlds.config.UniverseConfig;
import de.cubeisland.engine.worlds.config.WorldConfig;
import de.cubeisland.engine.worlds.player.PlayerDataConfig;

import static de.cubeisland.engine.worlds.WorldsPermissions.KEEP_FLYMODE;
import static de.cubeisland.engine.worlds.WorldsPermissions.KEEP_GAMEMODE;

/**
 * Represents multiple worlds in a universe
 */
public class Universe
{
    private final Worlds module;
    private final Multiverse multiverse;

    private UniverseConfig universeConfig;
    private WorldConfig defaults = null;
    private Map<World, WorldConfig> worldConfigs = new HashMap<>();
    private Map<String, WorldConfig> worldConfigMap = new HashMap<>();

    private World mainWorld;
    private Set<World> worlds = new HashSet<>();

    private Permission universeAccessPerm;
    private Map<World, Permission> worldPerms = new HashMap<>();

    private final File dirUniverse;
    private final File dirPlayers;
    private final File fileUniverse;
    private final File fileDefaults;

    private Universe(File universeDir, Worlds module, Multiverse multiverse)
    {
        assert universeDir != null : "UniverseDirectory cannot be null!";
        assert universeDir.isDirectory() : "UniverseDirectory must be a directory!";
        this.module = module;
        this.multiverse = multiverse;
        this.dirUniverse = universeDir;
        this.dirPlayers = new File(universeDir, "players");
        this.dirPlayers.mkdir();
        this.fileDefaults = new File(universeDir, "defaults.yml");
        this.fileUniverse =  new File(universeDir, "config.yml");
    }

    // For Loading
    public Universe(File universeDir, Multiverse multiverse, Worlds module)
    {
        this(universeDir, module, multiverse);
        if (!fileDefaults.exists())
        {
            module.getLog().warn("defaults.yml is missing for the universe {}! Regenerating...", universeDir.getName());
        }
        this.defaults = module.getCore().getConfigFactory().load(WorldConfig.class, fileDefaults, true);
        for (File file : universeDir.listFiles())
        {
            if (!file.equals(fileDefaults) && !file.equals(fileUniverse) && !file.isDirectory() && file.getName().endsWith(".yml"))
            {
                WorldConfig config = this.defaults.loadChild(file);
                if (config.autoLoad)
                {
                    this.loadOrCreateWorld(config, file.getName().substring(0, file.getName().indexOf(".yml")));
                }
                else
                {
                    this.worldConfigMap.put(file.getName().substring(0, file.getName().indexOf(".yml")), config);
                }
            }
        }
        this.universeConfig = this.module.getCore().getConfigFactory().load(UniverseConfig.class, fileUniverse);
        if (this.universeConfig.mainWorld == null)
        {
            World world = this.worlds.iterator().next();
            module.getLog().warn("The universe {} had no mainworld! {} is now the main world", universeDir.getName(), world.getName());
            this.universeConfig.save();
            for (WorldConfig worldConfig : this.worldConfigs.values())
            {
                if (worldConfig.spawn.respawnWorld == null)
                {
                    worldConfig.spawn.respawnWorld = this.universeConfig.mainWorld;
                    worldConfig.save();
                }
            }
        }
        this.mainWorld = this.universeConfig.mainWorld == null ? null : this.module.getCore().getWorldManager().getWorld(this.universeConfig.mainWorld);
        if (this.mainWorld == null)
        {
            if (this.worlds.isEmpty())
            {
                module.getLog().warn("Unknown world set as mainworld! Universe has no active worlds!");
                this.universeConfig.mainWorld = null;
            }
            else
            {
                this.mainWorld = this.worlds.iterator().next();
                module.getLog().warn("Unknown world set as mainworld! Mainworld {} replaced with {}!", this.universeConfig.mainWorld, this.mainWorld.getName());
                this.universeConfig.mainWorld = this.mainWorld.getName();
            }
            this.universeConfig.save();
        }
        this.generatePermissions();
    }

    private World loadOrCreateWorld(WorldConfig config, String name)
    {
        World world = this.module.getCore().getWorldManager().getWorld(name);
        if (world == null) // world loaded?
        {
            if (config.generation.environment == null || config.generation.seed == null)
            {
                module.getLog().warn("Insufficient Generation Information to load {} in {}!", name, this.getName());
                return null;
            }
            if (new File(Bukkit.getServer().getWorldContainer(), name).exists()) // world is just not loaded yet
            {
                module.getLog().info("Loading World {}...", name);
            }
            else // World does not exist
            {
                module.getLog().info("Creating new World {}...", name);
            }
            world = this.module.getCore().getWorldManager().createWorld(config.applyToCreator(WorldCreator.name(name)));
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
        this.worldConfigs.put(world, config);
        this.worldConfigMap.put(world.getName(), config);
        return world;
    }

    private void generatePermissions()
    {
        if (!this.universeConfig.freeAccess)
        {
            this.universeAccessPerm = this.multiverse.getUniverseRootPerm().createAbstractChild("access").createChild(dirUniverse.getName());
            this.module.getCore().getPermissionManager().registerPermission(module, this.universeAccessPerm);
        }
        Permission worldAccess = this.multiverse.getUniverseRootPerm().createAbstractChild("world-access");
        for (Entry<World, WorldConfig> entry : this.worldConfigs.entrySet())
        {
            if (!entry.getValue().access.free)
            {
                Permission perm = worldAccess.createChild(entry.getKey().getName());
                this.module.getCore().getPermissionManager().registerPermission(module, perm);
                this.worldPerms.put(entry.getKey(), perm);
            }

        }
    }

    // For creating new Universe
    public Universe(Worlds module, Multiverse multiverse, File universeDir, Set<World> worlds)
    {
        this(universeDir, module, multiverse);
        this.universeConfig = this.module.getCore().getConfigFactory().create(UniverseConfig.class);
        this.universeConfig.setFile(this.fileUniverse);

        for (World world : worlds)
        {
            if (world.getName().equals(universeDir.getName()))
            {
                this.universeConfig.mainWorld = world.getName();
                this.universeConfig.save();

                this.defaults = this.createWorldConfigFromExisting(world);
                this.defaults.spawn.spawnLocation = null;
                this.defaults.generation.worldType = null;
                this.defaults.generation.seed = null;
                this.defaults.spawn.respawnWorld = this.universeConfig.mainWorld;
                this.defaults.setFile(new File(universeDir, "defaults.yml"));

                this.defaults.save();
            }
            this.worldConfigs.put(world, this.createWorldConfigFromExisting(world));
        }
        for (Entry<World, WorldConfig> entry : worldConfigs.entrySet())
        {
            WorldConfig worldConfig = entry.getValue();
            worldConfig.spawn.respawnWorld = this.universeConfig.mainWorld;

            worldConfig.setDefault(this.defaults);
            worldConfig.setFile(new File(universeDir, entry.getKey().getName() + ".yml"));
            worldConfig.updateInheritance();
            worldConfig.save();
        }
        this.generatePermissions();
    }

    private WorldConfig createWorldConfigFromExisting(World world)
    {
        WorldConfig config = module.getCore().getConfigFactory().create(WorldConfig.class);
        if (world.getEnvironment() == Environment.NETHER)
        {
            config.scale = 8.0; // Nether is 1:8
        }
        if (this.defaults != null && this.universeConfig.mainWorld.equals(world.getName()))
        {
            config.spawn.keepSpawnInMemory = true; // KEEP MAIN SPAWN LOADED
        }
        config.applyFromWorld(world);
        return config;
    }

    public String getName()
    {
        return this.dirUniverse.getName();
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
            config.setDefault(this.defaults);
            config.setFile(new File(dirUniverse, world.getName() + ".yml"));
            config.updateInheritance();
            config.save();
            this.worldConfigs.put(world, config);
            this.worldConfigMap.put(world.getName(), config);
            this.worlds.add(world);
        }
    }

    public WorldConfig getWorldConfig(World world)
    {
        return this.worldConfigs.get(world);
    }

    public void savePlayer(Player player, World world)
    {
        this.module.getLog().debug("{} saved for {} in {}" , player.getName(), this.getName(), world.getName());
        PlayerDataConfig config = this.module.getCore().getConfigFactory().create(PlayerDataConfig.class);
        config.applyFromPlayer(player);

        config.setFile(new File(dirPlayers, player.getName() +".dat"));
        YamlCodec codec = this.module.getCore().getConfigFactory().getCodecManager().getCodec(YamlCodec.class);
        try
        {
            codec.saveConfig(config, new FileOutputStream(new File(dirUniverse, "players" + File.separator +player.getName() +".yml")));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        config.save();
    }

    public void loadPlayer(Player player)
    {
        this.module.getLog().debug("{} loaded for {} in {}", player.getName(), this.getName(), player.getWorld()
                                                                                                     .getName());
        File file = new File(dirPlayers, player.getName() +".dat");
        if (file.exists())
        {
            PlayerDataConfig load = this.module.getCore().getConfigFactory().load(PlayerDataConfig.class, file);
            load.applyToPlayer(player);
        }
        else
        {
            this.module.getLog().debug("Created PlayerDataConfig for {} in the {} universe" , player.getName(), this.getName());
            PlayerDataConfig save = this.module.getCore().getConfigFactory().create(PlayerDataConfig.class);
            save.applyToPlayer(player);
            this.savePlayer(player, player.getWorld());
        }
        if (!(this.universeConfig.keepFlyMode || KEEP_FLYMODE.isAuthorized(player)))
        {
            player.setFlying(player.isFlying());
        }
        if (!(this.universeConfig.keepGameMode || KEEP_GAMEMODE.isAuthorized(player)))
        {
            player.setGameMode(this.worldConfigs.get(player.getWorld()).gameMode);
        }
    }

    public World getMainWorld()
    {
        return this.mainWorld;
    }

    public boolean checkPlayerAccess(Player player, World world)
    {
        if (this.universeConfig.freeAccess || this.universeAccessPerm.isAuthorized(player))
        {
            Permission permission = this.worldPerms.get(world);
            if (permission == null || permission.isAuthorized(player))
            {
                return true;
            }
        }
        return false;
    }

    public Location handleNetherTarget(Location from, TravelAgent agent)
    {
        WorldConfig fromConfig = this.worldConfigs.get(from.getWorld());
        World toWorld = this.module.getCore().getWorldManager().getWorld(fromConfig.netherTarget);
        WorldConfig toConfig = this.multiverse.getUniverse(toWorld).getWorldConfig(toWorld);
        double factor = fromConfig.scale / toConfig.scale;
        agent.setSearchRadius((int)(128 / (factor * 8)));
        agent.setCreationRadius((int)(16 / (factor * 8)));
        return new Location(toWorld, from.getX() * factor, from.getY(), from.getZ() * factor, from.getYaw(), from.getPitch());
    }

    public Location handleEndTarget(Location from)
    {
        WorldConfig fromConfig = this.worldConfigs.get(from.getWorld());
        World toWorld = this.module.getCore().getWorldManager().getWorld(fromConfig.endTarget);
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
        WorldConfig worldConfig = this.worldConfigs.get(world);
        return worldConfig.netherTarget != null &&
            this.module.getCore().getWorldManager().getWorld(worldConfig.netherTarget) != null;
    }

    public boolean hasEndTarget(World world)
    {
        WorldConfig worldConfig = this.worldConfigs.get(world);
        if (worldConfig.endTarget != null)
        {
            World target = this.module.getCore().getWorldManager().getWorld(worldConfig.endTarget);
            if (target != null)
            {
                if (world.getEnvironment() != Environment.THE_END)
                {
                    if (target.getEnvironment() == Environment.THE_END)
                    {
                        return true;
                    }
                    this.module.getLog().warn("EndTarget {} coming from {} is not a EndWorld!", target.getName(), world.getName());
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
        World respawnWorld = this.module.getCore().getWorldManager().getWorld(worldConfig.spawn.respawnWorld);
        if (respawnWorld == null)
        {
            this.module.getLog().warn("Unknown respawn world for {}", world.getName());
            return this.getSpawnLocation(world);
        }
        return this.multiverse.getUniverse(respawnWorld).getSpawnLocation(respawnWorld);
    }

    public Location getSpawnLocation(World world)
    {
        return this.getWorldConfig(world).spawn.spawnLocation.getLocationIn(world);
    }

    public boolean hasWorld(String name)
    {
        WorldConfig worldConfig = this.worldConfigMap.get(name);
        return worldConfig == null;
    }

    public World loadWorld(String name)
    {
        return this.loadOrCreateWorld(this.worldConfigMap.get(name), name);
    }

    public List<Pair<String, WorldConfig>> getAllWorlds()
    {
        ArrayList<Pair<String, WorldConfig>> list = new ArrayList<>();
        for (Entry<String, WorldConfig> entry : this.worldConfigMap.entrySet())
        {
            list.add(new Pair<>(entry.getKey(), entry.getValue()));
        }
        return list;
    }
}
