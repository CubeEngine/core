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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.World;
import org.bukkit.World.Environment;

import de.cubeisland.engine.multiverse.config.UniverseConfig;
import de.cubeisland.engine.multiverse.config.WorldConfig;

/**
 * Represents multiple worlds in a universe
 */
public class Universe
{
    private UniverseConfig config;
    private WorldConfig defaults = null;
    private Map<World, WorldConfig> configs = new HashMap<>();
    private File universeDir;
    private Multiverse module;

    // For Loading
    public Universe(File universeDir, Multiverse module)
    {
        this.universeDir = universeDir;
        this.module = module;
    }

    // For creating new Universe
    public Universe(File universeDir, Multiverse module, List<World> worlds)
    {
        this.universeDir = universeDir;
        this.module = module;

        this.config = this.module.getCore().getConfigFactory().create(UniverseConfig.class);

        for (World world : worlds)
        {
            if (world.getName().equals(universeDir.getName()))
            {
                this.config.mainWorld = world;
                this.defaults = this.createWorldConfigFromExisting(world);
                this.defaults.spawn.spawnLocation = null;
                this.defaults.generation.seed = null;
                this.defaults.spawn.respawnWorld = this.config.mainWorld;
                this.defaults.setFile(new File(universeDir, "defaults.yml"));
                this.defaults.save();
            }
            this.configs.put(world, this.createWorldConfigFromExisting(world));
        }
        for (Entry<World, WorldConfig> entry : configs.entrySet())
        {
            WorldConfig config = entry.getValue();
            config.spawn.respawnWorld = this.config.mainWorld;

            config.setDefault(this.defaults);
            config.setFile(new File(universeDir, entry.getKey().getName() + ".yml"));
            config.updateInheritance();
            config.save();
        }
    }

    private WorldConfig createWorldConfigFromExisting(World world)
    {
        WorldConfig config = module.getCore().getConfigFactory().create(WorldConfig.class);
        if (world.getEnvironment() == Environment.NETHER)
        {
            config.scale = 8.0; // Nether is 1:8
        }
        config.spawn.spawnLocation = world.getSpawnLocation();
        if (this.defaults != null && this.config.mainWorld == world)
        {
            config.spawn.keepSpawnInMemory = true; // KEEP MAIN SPAWN LOADED
        }
        config.generation.worldType = world.getWorldType();
        config.generation.generateStructures = world.canGenerateStructures();
        config.generation.environment = world.getEnvironment();
        config.generation.seed = world.getSeed();
        // TODO config.generation.customGenerator = world.getGenerator();

        config.spawning.disable_animals = !world.getAllowAnimals();
        config.spawning.disable_monster = !world.getAllowMonsters();
        config.spawning.spawnLimit_ambient = world.getAmbientSpawnLimit();
        config.spawning.spawnLimit_animal = world.getAnimalSpawnLimit();
        config.spawning.spawnLimit_monster = world.getMonsterSpawnLimit();
        config.spawning.spawnLimit_waterAnimal = world.getWaterAnimalSpawnLimit();
        config.spawning.spawnRate_animal = world.getTicksPerAnimalSpawns();
        config.spawning.spawnRate_monster = world.getTicksPerMonsterSpawns();
        config.pvp = world.getPVP();
        config.autosave = world.isAutoSave();
        config.difficulty = world.getDifficulty();
        // TODO gamemode
        for (String rule : world.getGameRules())
        {
            String value = world.getGameRuleValue(rule);
            if (value != null)
            {
                config.gamerules.put(rule, value);
            }
        }
        return config;
    }

    // intercept PortalCreateEvent if not allowed
    // intercept EntityCreatePortalEvent if not allowed
}
