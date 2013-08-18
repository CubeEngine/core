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

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

public class TestGenerator extends ChunkGenerator
{
    public static final int CHUNK_SIZE = 16;

    @Override
    public byte[][] generateBlockSections(World world, Random random, int x, int z, BiomeGrid biomes)
    {
        byte[][] chunk = this.initChunkData(world);

        this.generateLayer(chunk, 0, 1, Material.BEDROCK);
        this.generateLayer(chunk, 1, 1, Material.GRASS);

        return chunk;
    }

    protected byte[][] initChunkData(World world)
    {
        return new byte[world.getMaxHeight() / CHUNK_SIZE][];
    }

    protected void setBlock(byte[][] chunk, int x, int y, int z, Material material)
    {
        byte[] section = chunk[z >> 4];
        if (section == null)
        {
            section = chunk[y >> 4] = new byte[16* 16 * 16];
        }
        section[((y & 0xF) << 8) | (z << 4) | x] = (byte)material.getId();
    }

    protected void generateLayer(byte[][] chunk, int lowerOffset, int height, Material material)
    {
        for (int y = 0; y < height; ++y)
        {
            for (int z = 0; z < CHUNK_SIZE; ++z)
            {
                for (int x = 0; x < CHUNK_SIZE; ++x)
                {
                    this.setBlock(chunk, x, lowerOffset + y, z, material);
                }
            }
        }
    }

    @Override
    public boolean canSpawn(World world, int x, int z)
    {
        return true;
    }
}
