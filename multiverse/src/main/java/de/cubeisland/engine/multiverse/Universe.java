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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import de.cubeisland.engine.configuration.codec.YamlCodec;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.multiverse.config.UniverseConfig;
import de.cubeisland.engine.multiverse.config.WorldConfig;
import de.cubeisland.engine.multiverse.config.WorldLocation;
import de.cubeisland.engine.multiverse.player.PlayerDataConfig;

import static de.cubeisland.engine.multiverse.MultiversePermissions.KEEP_FLYMODE;
import static de.cubeisland.engine.multiverse.MultiversePermissions.KEEP_GAMEMODE;

/**
 * Represents multiple worlds in a universe
 */
public class Universe
{
    private UniverseConfig universeConfig;
    private WorldConfig worldConfigDefaults = null;
    private Map<World, WorldConfig> worldConfigs = new HashMap<>();
    private File universeDir;
    private File playersDir;
    private Multiverse module;
    private Set<World> worlds;

    private Permission universeAccessPerm;
    private Map<World, Permission> worldPerms = new HashMap<>();
    private World mainWorld;

    // For Loading
    public Universe(File universeDir, Multiverse module)
    {
        this.universeDir = universeDir;
        this.playersDir = new File(universeDir, "players");
        this.playersDir.mkdir();
        this.module = module;
        this.worlds = new HashSet<>();
        File universeFile = new File(universeDir, "config.yml");
        File defaultsFile = new File(universeDir, "defaults.yml");
        if (!defaultsFile.exists())
        {
            module.getLog().warn("defaults.yml is missing for the universe {}! Regenerating...", universeDir.getName());
        }
        this.worldConfigDefaults = module.getCore().getConfigFactory().load(WorldConfig.class, defaultsFile, true);
        for (File file : universeDir.listFiles())
        {
            if (!(file.equals(defaultsFile) || file.equals(universeFile)))
            {
                if (file.getName().endsWith(".yml"))
                {
                    String name = file.getName().substring(0, file.getName().indexOf(".yml"));
                    WorldConfig config = this.worldConfigDefaults.loadChild(file);
                    if (!config.autoLoad)
                    {
                        continue;
                    }
                    World world = this.module.getCore().getWorldManager().getWorld(name);
                    if (world == null) // world loaded?
                    {
                        if (config.generation.environment == null || config.generation.seed == null)
                        {
                            module.getLog().info("Insufficient Generation Information to load {}!", name);
                            continue;
                        }
                        if (new File(Bukkit.getServer().getWorldContainer(), name).exists()) // world is just not loaded yet
                        {
                            module.getLog().info("Loading World {}...", name);
                        }
                        else // World does not exist
                        {
                            module.getLog().info("Creating new World {}...", name);
                        }
                        WorldCreator creator = WorldCreator.name(name);
                        config.applyToCreator(creator);
                        world = this.module.getCore().getWorldManager().createWorld(creator);
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
                        config.applyGenerationFromWorld(world);
                        config.updateInheritance();
                        config.save();
                    }
                    this.worlds.add(world);
                    this.worldConfigs.put(world, config);
                    config.applyToWorld(world);
                }
                else if (!file.isDirectory())
                {
                    module.getLog().warn("File with unknown ending! {}" , file.getName());
                }
            }
        }
        this.universeConfig = this.module.getCore().getConfigFactory().load(UniverseConfig.class, universeFile);
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
        this.mainWorld = this.module.getCore().getWorldManager().getWorld(this.universeConfig.mainWorld);
        this.generatePermissions();
    }

    private void generatePermissions()
    {
        if (!this.universeConfig.freeAccess)
        {
            this.universeAccessPerm = this.module.getUniverseRootPerm().createAbstractChild("access").createChild(universeDir.getName());
            this.module.getCore().getPermissionManager().registerPermission(module, this.universeAccessPerm);
        }
        Permission worldAccess = this.module.getUniverseRootPerm().createAbstractChild("world-access");
        for (Entry<World, WorldConfig> entry : this.worldConfigs.entrySet())
        {
            if (!entry.getValue().freeAccess)
            {
                Permission perm = worldAccess.createChild(entry.getKey().getName());
                this.module.getCore().getPermissionManager().registerPermission(module, perm);
                this.worldPerms.put(entry.getKey(), perm);
            }

        }
    }

    // For creating new Universe
    public Universe(File universeDir, Multiverse module, Set<World> worlds)
    {
        this.universeDir = universeDir;
        this.module = module;
        this.worlds = new HashSet<>(worlds);
        this.universeConfig = this.module.getCore().getConfigFactory().create(UniverseConfig.class);
        this.universeConfig.setFile(new File(universeDir, "config.yml"));

        for (World world : worlds)
        {
            if (world.getName().equals(universeDir.getName()))
            {
                this.universeConfig.mainWorld = world.getName();
                this.universeConfig.save();

                this.worldConfigDefaults = this.createWorldConfigFromExisting(world);
                this.worldConfigDefaults.spawn.spawnLocation = null;
                this.worldConfigDefaults.generation.worldType = null;
                this.worldConfigDefaults.generation.seed = null;
                this.worldConfigDefaults.spawn.respawnWorld = this.universeConfig.mainWorld;
                this.worldConfigDefaults.setFile(new File(universeDir, "defaults.yml"));

                this.worldConfigDefaults.save();
            }
            this.worldConfigs.put(world, this.createWorldConfigFromExisting(world));
        }
        for (Entry<World, WorldConfig> entry : worldConfigs.entrySet())
        {
            WorldConfig worldConfig = entry.getValue();
            worldConfig.spawn.respawnWorld = this.universeConfig.mainWorld;

            worldConfig.setDefault(this.worldConfigDefaults);
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
        if (this.worldConfigDefaults != null && this.universeConfig.mainWorld.equals(world.getName()))
        {
            config.spawn.keepSpawnInMemory = true; // KEEP MAIN SPAWN LOADED
        }
        config.applyFromWorld(world);
        return config;
    }

    public String getName()
    {
        return this.universeDir.getName();
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
            config.setDefault(this.worldConfigDefaults);
            config.setFile(new File(universeDir, world.getName() + ".yml"));
            config.updateInheritance();
            config.save();
            this.worldConfigs.put(world, config);
            this.worlds.add(world);
        }
    }

    public WorldConfig getWorldConfig(World world)
    {
        return this.worldConfigs.get(world);
    }

    public void savePlayer(Player player)
    {
        this.module.getLog().debug("{} saved for {} in {}" , player.getName(), this.getName(), player.getWorld().getName());
        PlayerDataConfig config = this.module.getCore().getConfigFactory().create(PlayerDataConfig.class);
        config.applyFromPlayer(player);

        config.setFile(new File(playersDir, player.getName() +".dat"));
        YamlCodec codec = this.module.getCore().getConfigFactory().getCodecManager().getCodec(YamlCodec.class);
        try
        {
            codec.saveConfig(config, new FileOutputStream(new File(universeDir, "players" + File.separator +player.getName() +".yml")));
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
        File file = new File(playersDir, player.getName() +".dat");
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
            this.savePlayer(player);
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

    // intercept PortalCreateEvent if not allowed
    // intercept EntityCreatePortalEvent if not allowed
}
