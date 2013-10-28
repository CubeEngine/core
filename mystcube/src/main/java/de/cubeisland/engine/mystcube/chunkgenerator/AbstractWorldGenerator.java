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
package de.cubeisland.engine.mystcube.chunkgenerator;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;

public abstract class AbstractWorldGenerator extends ChunkGenerator
{
    public static final int CHUNK_SIZE = 16;

    @Override
    public byte[][] generateBlockSections(World world, Random random, int x, int z, BiomeGrid biomes)
    {
        byte[][] result = initChunkData(world);
        this.generateBlockSections(world, random, x, z, biomes, result);
        return result;
    }

    protected byte[][] initChunkData(World world)
    {
        return new byte[world.getMaxHeight() / CHUNK_SIZE][];
    }

    public abstract void generateBlockSections(World world, Random random, int x, int z, BiomeGrid biomes, byte[][] result);

    protected void setBlock(byte[][] chunk, int x, int y, int z, Material material)
    {
        byte[] section = chunk[z >> 4];
        if (section == null)
        {
            section = chunk[y >> 4] = new byte[16* 16 * 16];
        }
        section[((y & 0xF) << 8) | (z << 4) | x] = (byte)material.getId();
    }

    protected void setBiomeForChunk(BiomeGrid biomeGrid, Biome biome)
    {
        int x,z;
        for(x = 0; x < CHUNK_SIZE; x++)
        {
            for(z = 0; z < CHUNK_SIZE; z++)
            {
                biomeGrid.setBiome(x, z, biome);
            }
        }
    }

    /**
     *
     * @param result
     * @param material
     * @param yStart
     * @param height
     * @return the uppermost layer
     */
    protected int generateLayers(byte[][] result, Material material, int yStart, int height)
    {
        int y = yStart;
        for (; y < yStart + height; y++)
        {
            int x,z;
            for(x = 0; x < CHUNK_SIZE; x++)
            {
                for(z = 0; z < CHUNK_SIZE; z++)
                {
                    setBlock(result, x, y, z, material);
                }
            }
        }
        return y;
    }

    @Override
    public boolean canSpawn(World world, int x, int z)
    {
        return true;
    }
}
