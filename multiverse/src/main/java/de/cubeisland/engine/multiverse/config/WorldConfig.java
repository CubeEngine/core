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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldType;

import de.cubeisland.engine.configuration.Section;
import de.cubeisland.engine.configuration.YamlConfiguration;

public class WorldConfig extends YamlConfiguration
{
    public List<String> alias = new ArrayList<>();
    public double scale = 1.0;

    public Spawn spawn = new Spawn();

    public class Spawn implements Section
    {
        public World respawnWorld; // empty means main universe world
        public boolean allowBedRespawn = true; // TODO
        public boolean keepSpawnInMemory = false;
        public Location spawnLocation; // TODO only xyz yaw pitch double precision
    }
    public Access access = new Access();

    public class Access implements Section
    {
        public boolean world = true; // if false need perm to access world
        public boolean interceptTeleport = false;
    }

    public Generation generation = new Generation();

    public class Generation implements Section
    {
        public WorldType worldType = WorldType.NORMAL;
        public Environment environment = Environment.NORMAL;
        public boolean generateStructures = true;
        public String customGenerator = null; // TODO not supported yet
        public Long seed;
    }

    public Spawning spawning = new Spawning();

    public class Spawning implements Section
    {
        public boolean disable_animals = false;
        public boolean disable_monster = false;  // ^ world.setSpawnFlags();

        public Integer spawnLimit_ambient;
        public Integer spawnLimit_animal;
        public Integer spawnLimit_monster;
        public Integer spawnLimit_waterAnimal;

        public Integer spawnRate_animal;
        public Integer spawnRate_monster;
    }

    public Map<String, String> gamerules = new HashMap<>();
    public boolean pvp = true;
    public boolean autosave = true;

    public GameMode gameMode = GameMode.SURVIVAL;
    public Difficulty difficulty = Difficulty.NORMAL;

    public void applyToWorld(World world)
    {
        world.setKeepSpawnInMemory(this.spawn.keepSpawnInMemory);
        if (this.spawn.spawnLocation != null)
        {
            world.setSpawnLocation(this.spawn.spawnLocation.getBlockX(), this.spawn.spawnLocation.getBlockY(), this.spawn.spawnLocation.getBlockZ());
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
    }
}
