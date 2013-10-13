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
package de.cubeisland.engine.mystcube;

import java.util.Random;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.mystcube.blockpopulator.VillagePopulator;
import de.cubeisland.engine.mystcube.chunkgenerator.FlatMapGenerator;

public class Mystcube extends Module
{
    private MystcubeConfig config;

    @Override
    public void onStartupFinished()
    {
        WorldCreator worldCreator = WorldCreator.name("world_myst_flat")
                        .generator("CubeEngine:mystcube:flat")
                        .generateStructures(false)
                        .type(WorldType.FLAT)
                        .environment(Environment.NORMAL)
            ;
        World world = this.getCore().getWorldManager().createWorld(worldCreator);
        if (world != null)
        {
            world.setAmbientSpawnLimit(0);
            world.setAnimalSpawnLimit(0);
            world.setMonsterSpawnLimit(0);
            world.setSpawnFlags(false, false);

            new VillagePopulator().populate(world, new Random(), world.getSpawnLocation().getChunk());
        }
    }

    @Override
    public void onLoad()
    {
        this.getCore().getWorldManager().registerGenerator(this, "flat", new FlatMapGenerator());
    }
}
