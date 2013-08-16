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
package de.cubeisland.engine.test.tests.worldgenerator;

import java.io.IOException;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

import de.cubeisland.engine.test.tests.Test;

public class WorldGeneratorTest extends Test
{
    public static final String TEST_WORLD_NAME = "world_test";

    private final de.cubeisland.engine.test.Test module;

    public WorldGeneratorTest(de.cubeisland.engine.test.Test module)
    {
        this.module = module;
    }

    @Override
    public void onLoad()
    {
        module.getCore().getWorldManager().registerGenerator(module, "test", new TestGenerator());
    }

    @Override
    public void onStartupFinished()
    {
        World world = module.getCore().getWorldManager().createWorld(WorldCreator.name(TEST_WORLD_NAME)
                                                                          .generator("CubeEngine:test:test")
                                                                          .generateStructures(false)
                                                                          .type(WorldType.FLAT)
                                                                          .environment(Environment.NORMAL).seed(1231));
        if (world != null)
        {
            world.setAmbientSpawnLimit(0);
            world.setAnimalSpawnLimit(0);
            world.setMonsterSpawnLimit(0);
            world.setSpawnFlags(false, false);
        }
        this.setSuccess(true);
    }

    @Override
    public void onDisable()
    {
        try
        {
            module.getCore().getWorldManager().deleteWorld(TEST_WORLD_NAME);
        }
        catch (IOException e)
        {
            module.getLog().warn("Failed to delete the test world!");
            module.getLog().debug(e.getLocalizedMessage(), e);
        }
    }
}
