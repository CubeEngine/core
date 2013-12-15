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
package de.cubeisland.engine.multiverse.config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

import de.cubeisland.engine.configuration.Section;
import de.cubeisland.engine.configuration.YamlConfiguration;

public class WorldConfig extends YamlConfiguration
{
    public List<String> alias = new ArrayList<>();

    public Generation generation = new Generation();

    public class Generation implements Section
    {
        public WorldType worldType = WorldType.NORMAL;
        public boolean generateStructures = true;
        public Environment environment;
        public String seed = "";
        public String customGenerator = null; // TODO not supported yet
    }

    @Override
    public void onLoaded(File loadedFrom)
    {
        if (this.getDefault() == this)
        {
            this.generation.environment = null;
            this.generation.seed = null;
        }
        if (this.generation.environment == Environment.THE_END)
        {
            this.netherTarget =  null;
        }
        if (this.generation.environment == Environment.NETHER)
        {
            this.endTarget =  null;
        }
    }

    public boolean freeAccess = true;
    public boolean autoLoad = true;
    public double scale = 1.0;

    public Spawn spawn = new Spawn();

    public class Spawn implements Section
    {
        public String respawnWorld; // empty means main universe world
        public boolean allowBedRespawn = true; // TODO
        public boolean keepSpawnInMemory = false;
        public WorldLocation spawnLocation;
    }
    public Access access = new Access();

    public class Access implements Section
    {
        public boolean world = true; // if false need perm to access world
        public boolean interceptTeleport = false;
    }

    public Spawning spawning = new Spawning();

    public class Spawning implements Section
    {
        public boolean disable_animals = false;
        public boolean disable_monster = false;  // ^ world.setSpawnFlags();

        public Integer spawnLimit_ambient = 15;
        public Integer spawnLimit_animal = 15;
        public Integer spawnLimit_monster = 70;
        public Integer spawnLimit_waterAnimal = 5;

        public Integer spawnRate_animal = 400;
        public Integer spawnRate_monster = 1;
    }

    public Map<String, String> gamerules = new HashMap<>();
    public boolean pvp = true;
    public boolean autosave = true;

    public GameMode gameMode = GameMode.SURVIVAL;
    public Difficulty difficulty = Difficulty.NORMAL;

    public void applyToWorld(World world)
    {// TODO if anything is null take from world ; update inheritance & save
        boolean save = false;
        world.setKeepSpawnInMemory(this.spawn.keepSpawnInMemory);
        if (this.spawn.spawnLocation != null)
        {
            world.setSpawnLocation((int)this.spawn.spawnLocation.x, (int)this.spawn.spawnLocation.y, (int)this.spawn.spawnLocation.z);
        }
        else
        {
            save = true;
            this.spawn.spawnLocation = new WorldLocation(world.getSpawnLocation());
        }

        world.setSpawnFlags(!this.spawning.disable_monster, !this.spawning.disable_animals);
        world.setAmbientSpawnLimit(this.spawning.spawnLimit_ambient);
        world.setAnimalSpawnLimit(this.spawning.spawnLimit_animal);
        world.setMonsterSpawnLimit(this.spawning.spawnLimit_monster);
        world.setWaterAnimalSpawnLimit(this.spawning.spawnLimit_waterAnimal);
        world.setTicksPerAnimalSpawns(this.spawning.spawnRate_animal);
        world.setTicksPerMonsterSpawns(this.spawning.spawnRate_monster);
        for (Entry<String, String> entry : this.gamerules.entrySet())
        {
            world.setGameRuleValue(entry.getKey(), entry.getValue());
        }
        world.setPVP(this.pvp);
        world.setAutoSave(this.autosave);
        world.setDifficulty(this.difficulty);

        if (save)
        {
            this.updateInheritance();
            this.save();
        }
    }

    public void applyFromWorld(World world)
    {
        this.spawn.spawnLocation = new WorldLocation(world.getSpawnLocation());

        this.generation.worldType = world.getWorldType();
        this.generation.generateStructures = world.canGenerateStructures();
        this.generation.environment = world.getEnvironment();
        this.generation.seed = String.valueOf(world.getSeed());
        // TODO this.generation.customGenerator = world.getGenerator();
        this.spawning.disable_animals = !world.getAllowAnimals();
        this.spawning.disable_monster = !world.getAllowMonsters();
        this.spawning.spawnLimit_ambient = world.getAmbientSpawnLimit();
        this.spawning.spawnLimit_animal = world.getAnimalSpawnLimit();
        this.spawning.spawnLimit_monster = world.getMonsterSpawnLimit();
        this.spawning.spawnLimit_waterAnimal = world.getWaterAnimalSpawnLimit();
        this.spawning.spawnRate_animal = (int)world.getTicksPerAnimalSpawns();
        this.spawning.spawnRate_monster = (int)world.getTicksPerMonsterSpawns();
        this.pvp = world.getPVP();
        this.autosave = world.isAutoSave();
        this.difficulty = world.getDifficulty();
        // TODO gamemode
        for (String rule : world.getGameRules())
        {
            String value = world.getGameRuleValue(rule);
            if (value != null)
            {
                this.gamerules.put(rule, value);
            }
        }
    }

    public WorldCreator applyToCreator(WorldCreator creator)
    {
        if (generation.worldType != null)
        {
            creator.type(generation.worldType);
        }
        if (generation.environment != null)
        {
            creator.environment(generation.environment);
        }
        creator.generateStructures(generation.generateStructures);
        if (generation.seed != null)
        {
            try
            {
                creator.seed(Long.valueOf(generation.seed));
            }
            catch (NumberFormatException ex)
            {
                creator.seed(generation.seed.hashCode());
            }
        }
        // TODO custom generator
        return creator;
    }

    public void applyGenerationFromWorld(World world)
    {
        this.generation.generateStructures = world.canGenerateStructures();
        this.generation.seed = String.valueOf(world.getSeed());
        this.generation.environment = world.getEnvironment();
        this.generation.worldType = world.getWorldType();
        // TODO generator
    }

    public String netherTarget;
    public String endTarget;
}
